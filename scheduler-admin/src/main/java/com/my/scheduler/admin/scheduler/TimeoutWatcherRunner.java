package com.my.scheduler.admin.scheduler;

import com.my.scheduler.admin.domain.JobInstance;
import com.my.scheduler.admin.repository.JobInstanceMapper;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling
public class TimeoutWatcherRunner {

    private final JobInstanceMapper jobInstanceMapper;

    public TimeoutWatcherRunner(JobInstanceMapper jobInstanceMapper) {
        this.jobInstanceMapper = jobInstanceMapper;
    }

    @Scheduled(fixedDelay = 2000)
    public void scanTimeout() {
        LocalDateTime now = LocalDateTime.now();
        List<JobInstance> list = jobInstanceMapper.selectRunningTimeout(now, 100);
        if (list == null || list.isEmpty()) return;

        for (JobInstance ins : list) {
            long durationMs = 0L;
            if (ins.getStartTime() != null) {
                durationMs = Duration.between(ins.getStartTime(), now).toMillis();
            }
            jobInstanceMapper.markTimeout(
                    ins.getId(),
                    now,
                    durationMs,
                    "timeout detected by admin watcher"
            );
        }
    }
}
