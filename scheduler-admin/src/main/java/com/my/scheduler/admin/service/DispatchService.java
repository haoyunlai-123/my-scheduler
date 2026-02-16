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

    public DispatchService(ExecutorNodeMapper executorNodeMapper,
                           JobInstanceMapper jobInstanceMapper,
                           JobMapper jobMapper,
                           RestTemplate restTemplate) {
        this.executorNodeMapper = executorNodeMapper;
        this.jobInstanceMapper = jobInstanceMapper;
        this.jobMapper = jobMapper;
        this.restTemplate = restTemplate;
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

        // 选 executor：先简单选最近心跳
        List<ExecutorNode> executors = executorNodeMapper.selectOnlineOrderByHeartbeatDesc(5);
        if (executors == null || executors.isEmpty()) {
            // 保持 WAITING，让后面有 executor 再派发（或你也可以标 FAILED）
            return false;
        }

        ExecutorNode target = executors.get(0);

        // 先尝试把 WAITING -> RUNNING（乐观锁，避免重复派发）
        LocalDateTime now = LocalDateTime.now();
        int updated = jobInstanceMapper.markRunning(ins.getId(), target.getId(), now);
        if (updated == 0) {
            return false;
        }

        // 调用 executor 接收任务
        ExecuteRequest req = new ExecuteRequest();
        req.setInstanceId(ins.getId());
        req.setJobId(job.getId());
        req.setHandlerType(job.getHandlerType());
        req.setHandlerParam(job.getHandlerParam());
        req.setTimeoutMs(job.getTimeoutMs());

        String url = "http://" + target.getAddress() + "/api/execute";
        try {
            ApiResponse<?> resp = restTemplate.postForObject(url, req, ApiResponse.class);
            if (resp == null || resp.getCode() != 0) {
                jobInstanceMapper.markFinished(ins.getId(), "FAILED", LocalDateTime.now(), 0L,
                        "executor accept failed: " + (resp == null ? "null" : resp.getMsg()));
                return false;
            }

            // accepted 字段可能因为泛型变 Map，这里不强依赖它，能返回 0 就算接收成功
            return true;
        } catch (Exception e) {
            jobInstanceMapper.markFinished(ins.getId(), "FAILED", LocalDateTime.now(), 0L,
                    "executor call error: " + e.getMessage());
            return false;
        }
    }
}
