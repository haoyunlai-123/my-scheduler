package com.my.scheduler.admin.service;

import com.my.scheduler.admin.domain.ExecutorNode;
import com.my.scheduler.admin.domain.Job;
import com.my.scheduler.admin.domain.JobInstance;
import com.my.scheduler.admin.repository.ExecutorNodeMapper;
import com.my.scheduler.admin.repository.JobInstanceMapper;
import com.my.scheduler.admin.repository.JobMapper;
import com.my.scheduler.common.dto.ApiResponse;
import com.my.scheduler.common.dto.executor.ExecuteRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DispatchService {

    private final ExecutorNodeMapper executorNodeMapper;
    private final JobInstanceMapper jobInstanceMapper;
    private final JobMapper jobMapper;
    private final RestTemplate restTemplate;
    private final RouterService routerService;

    public DispatchService(ExecutorNodeMapper executorNodeMapper,
                           JobInstanceMapper jobInstanceMapper,
                           JobMapper jobMapper,
                           RestTemplate restTemplate, RouterService routerService) {
        this.executorNodeMapper = executorNodeMapper;
        this.jobInstanceMapper = jobInstanceMapper;
        this.jobMapper = jobMapper;
        this.restTemplate = restTemplate;
        this.routerService = routerService;
    }

    /**
     * 派发一条 instance（只负责“接收成功”即可）
     * admin选择一个 executor，将任务包装成request通过restTemplate发给executor执行
     * @param ins
     * @return
     */
    public boolean dispatchOne(JobInstance ins) {
        Job job = jobMapper.selectById(ins.getJobId());
        if (job == null) {
            jobInstanceMapper.markFinished(ins.getId(), "FAILED", LocalDateTime.now(), 0L, "job not found");
            return false;
        }

        // 选 executor：取一批 ONLINE
        List<ExecutorNode> executors = executorNodeMapper.selectOnlineOrderByHeartbeatDesc(10);
        if (executors == null || executors.isEmpty()) {
            return false; // 没有在线节点，保持 WAITING
        }

        // RoundRobin 选一个起点
        ExecutorNode start = routerService.pickRoundRobin(job.getId(), executors);
        int startIndex = executors.indexOf(start);
        if (startIndex < 0) startIndex = 0;

        // 最多尝试 executors.size() 次
        for (int attempt = 0; attempt < executors.size(); attempt++) {
            ExecutorNode target = executors.get((startIndex + attempt) % executors.size());

            // 先把 WAITING -> RUNNING（乐观锁，避免重复派发）
            LocalDateTime now = LocalDateTime.now();
            int updated = jobInstanceMapper.markRunning(ins.getId(), target.getId(), now);
            if (updated == 0) {
                return false; // 可能已被别的线程处理
            }

            boolean accepted = callExecutorAccept(target, ins, job);
            if (accepted) {
                return true; // 接收成功即可，后续靠 report 更新最终状态
            }

            // accept 失败：把 instance 退回 WAITING 再尝试下一个节点（关键！）
            // 为了简单，加一个 mapper 方法：markWaitingFromRunning
            jobInstanceMapper.markWaitingFromRunning(ins.getId(), target.getId());
        }

        // 尝试完仍失败：标 FAILED
        jobInstanceMapper.markFinished(ins.getId(), "FAILED", LocalDateTime.now(), 0L, "all executors accept failed");
        return false;
    }

    private boolean callExecutorAccept(ExecutorNode target, JobInstance ins, Job job) {
        ExecuteRequest req = new ExecuteRequest();
        req.setInstanceId(ins.getId());
        req.setJobId(job.getId());
        req.setHandlerType(job.getHandlerType());
        req.setHandlerParam(job.getHandlerParam());
        req.setTimeoutMs(job.getTimeoutMs());

        String url = "http://" + target.getAddress() + "/api/execute";
        try {
            ApiResponse<?> resp = restTemplate.postForObject(url, req, ApiResponse.class);
            return resp != null && resp.getCode() == 0;
        } catch (Exception e) {
            return false;
        }
    }

}
