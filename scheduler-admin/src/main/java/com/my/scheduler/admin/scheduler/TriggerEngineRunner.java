package com.my.scheduler.admin.scheduler;

import com.my.scheduler.admin.domain.Job;
import com.my.scheduler.admin.domain.JobInstance;
import com.my.scheduler.admin.domain.JobScheduleState;
import com.my.scheduler.admin.repository.JobInstanceMapper;
import com.my.scheduler.admin.repository.JobMapper;
import com.my.scheduler.admin.repository.JobScheduleStateMapper;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling
public class TriggerEngineRunner {

    private final JobMapper jobMapper;
    private final JobInstanceMapper jobInstanceMapper;
    private final JobScheduleStateMapper stateMapper;

    public TriggerEngineRunner(JobMapper jobMapper,
                               JobInstanceMapper jobInstanceMapper,
                               JobScheduleStateMapper stateMapper) {
        this.jobMapper = jobMapper;
        this.jobInstanceMapper = jobInstanceMapper;
        this.stateMapper = stateMapper;
    }

    @Scheduled(fixedDelay = 500)
    public void triggerLoop() {
        LocalDateTime now = LocalDateTime.now();

        // 只扫描到期状态，避免扫全表 job
        List<JobScheduleState> due = stateMapper.selectDue(now, 100);
        if (due == null || due.isEmpty()) return;

        for (JobScheduleState st : due) {
            Job job = jobMapper.selectById(st.getJobId());
            if (job == null || job.getEnabled() == null || job.getEnabled() == 0) {
                continue;
            }
            if (!"FIXED_RATE".equalsIgnoreCase(job.getScheduleType())) {
                continue;
            }

            long intervalMs = parseIntervalMs(job.getScheduleExpr());

            // 核心：CAS 推进 next_trigger_time（只成功一次），成功才生成 instance
            LocalDateTime expected = st.getNextTriggerTime();
            LocalDateTime next = expected.plusNanos(intervalMs * 1_000_000);

            int ok = stateMapper.compareAndSetNextTime(job.getId(), expected, next);
            if (ok == 1) {
                JobInstance ins = new JobInstance();
                ins.setJobId(job.getId());
                ins.setTriggerTime(expected);  // 用“应触发时间”更准确
                ins.setExecutorId(null);
                ins.setStatus("WAITING");
                ins.setRetryCount(0);
                jobInstanceMapper.insert(ins);
            }
        }
    }

    private long parseIntervalMs(String expr) {
        long ms = Long.parseLong(expr.trim());
        if (ms < 100) throw new IllegalArgumentException("interval too small: " + ms);
        return ms;
    }
}
