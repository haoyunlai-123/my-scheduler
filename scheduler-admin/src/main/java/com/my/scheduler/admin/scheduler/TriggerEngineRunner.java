package com.my.scheduler.admin.scheduler;

import com.my.scheduler.admin.domain.Job;
import com.my.scheduler.admin.domain.JobInstance;
import com.my.scheduler.admin.domain.JobScheduleState;
import com.my.scheduler.admin.repository.JobInstanceMapper;
import com.my.scheduler.admin.repository.JobMapper;
import com.my.scheduler.admin.repository.JobScheduleStateMapper;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import java.time.*;
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
        List<JobScheduleState> due = stateMapper.selectDue(now, 200);
        if (due == null || due.isEmpty()) return;

        for (JobScheduleState st : due) {
            Job job = jobMapper.selectById(st.getJobId());
            if (job == null || job.getEnabled() == null || job.getEnabled() == 0) {
                continue;
            }

            String type = job.getScheduleType();
            if (type == null) continue;

            LocalDateTime expected = st.getNextTriggerTime();
            LocalDateTime next;

            try {
                if ("FIXED_RATE".equalsIgnoreCase(type)) {
                    long intervalMs = parseIntervalMs(job.getScheduleExpr());
                    next = expected.plusNanos(intervalMs * 1_000_000);
                } else if ("CRON".equalsIgnoreCase(type)) {
                    next = calcNextCron(job.getScheduleExpr(), expected);
                    // 如果 next 计算不出来，说明 cron 不合法或不会再触发，直接跳过
                    if (next == null) continue;
                } else {
                    continue;
                }
            } catch (Exception e) {
                // 表达式错误：为了避免一直卡在 due 队列里刷日志，这里把 next 往后推 1 分钟
                next = now.plusMinutes(1);
            }

            // 核心：CAS 推进 next_trigger_time，成功才生成 instance（防重复）
            int ok = stateMapper.compareAndSetNextTime(job.getId(), expected, next);
            if (ok == 1) {
                JobInstance ins = new JobInstance();
                ins.setJobId(job.getId());
                ins.setTriggerTime(expected);  // 记录“应触发时间”
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

    /**
     * 以 expected 为基准计算 cron 的下一次触发时间
     * 注意：CronExpression.next 接收 ZonedDateTime
     */
    private LocalDateTime calcNextCron(String cron, LocalDateTime expected) {
        CronExpression ce = CronExpression.parse(cron.trim());

        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime base = expected.atZone(zone);

        // 下一次触发点
        ZonedDateTime next = ce.next(base);

        // 防止某些 cron 表达式在极端情况下返回 <= base（理论上不会，但加一道保险）
        if (next != null && !next.isAfter(base)) {
            next = ce.next(base.plusSeconds(1));
        }
        return next == null ? null : next.toLocalDateTime();
    }
}
