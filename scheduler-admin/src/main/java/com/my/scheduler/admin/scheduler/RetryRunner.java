package com.my.scheduler.admin.scheduler;

import com.my.scheduler.admin.domain.JobInstance;
import com.my.scheduler.admin.repository.JobInstanceMapper;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@EnableScheduling
public class RetryRunner {

    private final JobInstanceMapper jobInstanceMapper;

    public RetryRunner(JobInstanceMapper jobInstanceMapper) {
        this.jobInstanceMapper = jobInstanceMapper;
    }

    @Scheduled(fixedDelay = 3000)
    public void retryLoop() {
        List<JobInstance> list = jobInstanceMapper.selectRetryable(100);
        if (list == null || list.isEmpty()) return;

        for (JobInstance ins : list) {
            // 退回 WAITING 并 retry_count+1
            jobInstanceMapper.backToWaitingForRetry(ins.getId());
        }
    }
}
