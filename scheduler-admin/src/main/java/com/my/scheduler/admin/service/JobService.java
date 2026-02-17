package com.my.scheduler.admin.service;

import com.my.scheduler.admin.domain.Job;
import com.my.scheduler.admin.domain.JobInstance;
import com.my.scheduler.admin.domain.JobScheduleState;
import com.my.scheduler.admin.repository.JobInstanceMapper;
import com.my.scheduler.admin.repository.JobMapper;
import com.my.scheduler.admin.repository.JobScheduleStateMapper;
import com.my.scheduler.common.dto.job.JobCreateRequest;
import com.my.scheduler.common.dto.job.JobUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobService {

    private final JobMapper jobMapper;
    private final JobInstanceMapper jobInstanceMapper;
    private final JobScheduleStateMapper stateMapper;

    public JobService(JobMapper jobMapper, JobInstanceMapper jobInstanceMapper, JobScheduleStateMapper stateMapper) {
        this.jobMapper = jobMapper;
        this.jobInstanceMapper = jobInstanceMapper;
        this.stateMapper = stateMapper;
    }

    @Transactional
    public Long create(JobCreateRequest req) {
        Job job = new Job();
        job.setName(req.getName());
        job.setScheduleType(req.getScheduleType());
        job.setScheduleExpr(req.getScheduleExpr());
        job.setHandlerType(req.getHandlerType());
        job.setHandlerParam(req.getHandlerParam());
        job.setRouteStrategy(req.getRouteStrategy());
        job.setRetryMax(req.getRetryMax());
        job.setTimeoutMs(req.getTimeoutMs());
        job.setEnabled(Boolean.TRUE.equals(req.getEnabled()) ? 1 : 0);

        jobMapper.insert(job);

        if ("FIXED_RATE".equalsIgnoreCase(job.getScheduleType()) && job.getEnabled() == 1) {
            long intervalMs = parseIntervalMs(job.getScheduleExpr());
            JobScheduleState st = new JobScheduleState();
            st.setJobId(job.getId());
            st.setNextTriggerTime(LocalDateTime.now().plusNanos(intervalMs * 1_000_000));
            stateMapper.insert(st);
        }

        return job.getId();
    }

    @Transactional
    public void update(JobUpdateRequest req) {
        Job exist = jobMapper.selectById(req.getId());
        if (exist == null) {
            throw new IllegalArgumentException("job not found: " + req.getId());
        }

        exist.setName(req.getName());
        exist.setScheduleType(req.getScheduleType());
        exist.setScheduleExpr(req.getScheduleExpr());
        exist.setHandlerType(req.getHandlerType());
        exist.setHandlerParam(req.getHandlerParam());
        exist.setRouteStrategy(req.getRouteStrategy());
        exist.setRetryMax(req.getRetryMax());
        exist.setTimeoutMs(req.getTimeoutMs());

        jobMapper.update(exist);

        if ("FIXED_RATE".equalsIgnoreCase(exist.getScheduleType())) {
            long intervalMs = parseIntervalMs(exist.getScheduleExpr());
            var st = stateMapper.selectByJobId(exist.getId());
            if (st == null) {
                JobScheduleState ns = new JobScheduleState();
                ns.setJobId(exist.getId());
                ns.setNextTriggerTime(LocalDateTime.now().plusNanos(intervalMs * 1_000_000));
                stateMapper.insert(ns);
            } else {
                // 简单做法：直接把 expected=当前值 CAS 到 now+interval（失败也没关系，下轮再试）
                LocalDateTime expected = st.getNextTriggerTime();
                LocalDateTime next = LocalDateTime.now().plusNanos(intervalMs * 1_000_000);
                stateMapper.compareAndSetNextTime(exist.getId(), expected, next);
            }
        }

    }

    public Job get(Long id) {
        return jobMapper.selectById(id);
    }

    public List<Job> list() {
        return jobMapper.selectAll();
    }

    @Transactional
    public void enable(Long id) {
        ensureExist(id);
        jobMapper.updateEnabled(id, 1);

        Job job = jobMapper.selectById(id);
        if ("FIXED_RATE".equalsIgnoreCase(job.getScheduleType())) {
            long intervalMs = parseIntervalMs(job.getScheduleExpr());
            var st = stateMapper.selectByJobId(id);
            LocalDateTime next = LocalDateTime.now().plusNanos(intervalMs * 1_000_000);
            if (st == null) {
                JobScheduleState ns = new JobScheduleState();
                ns.setJobId(id);
                ns.setNextTriggerTime(next);
                stateMapper.insert(ns);
            } else {
                stateMapper.compareAndSetNextTime(id, st.getNextTriggerTime(), next);
            }
        }

    }

    @Transactional
    public void disable(Long id) {
        ensureExist(id);
        jobMapper.updateEnabled(id, 0);
    }

    /** 手动触发一次：只生成 instance（WAITING），派发执行下一步做 */
    @Transactional
    public Long triggerOnce(Long jobId) {
        Job job = ensureExist(jobId);
        if (job.getEnabled() == null || job.getEnabled() == 0) {
            throw new IllegalStateException("job disabled: " + jobId);
        }

        JobInstance ins = new JobInstance();
        ins.setJobId(jobId);
        ins.setTriggerTime(LocalDateTime.now());
        ins.setExecutorId(null);        // 下一步派发时再决定
        ins.setStatus("WAITING");
        ins.setRetryCount(0);

        jobInstanceMapper.insert(ins);
        return ins.getId();
    }

    public List<JobInstance> latestInstances(Long jobId, int limit) {
        ensureExist(jobId);
        return jobInstanceMapper.selectByJobId(jobId, limit);
    }

    private Job ensureExist(Long id) {
        Job job = jobMapper.selectById(id);
        if (job == null) throw new IllegalArgumentException("job not found: " + id);
        return job;
    }

    /**
     * 简单起见，FIXED_RATE 的 scheduleExpr 就直接用毫秒数了，实际可以更复杂一些，比如支持时间单位（ms/s/m/h）等
     * @param expr
     * @return
     */
    private long parseIntervalMs(String expr) {
        try {
            long ms = Long.parseLong(expr.trim());
            if (ms < 100) throw new IllegalArgumentException("interval too small: " + ms);
            return ms;
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid FIXED_RATE scheduleExpr(ms): " + expr);
        }
    }

}
