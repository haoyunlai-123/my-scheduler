package com.my.scheduler.admin.scheduler;

import com.my.scheduler.admin.domain.JobInstance;
import com.my.scheduler.admin.repository.JobInstanceMapper;
import com.my.scheduler.admin.service.DispatchService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@EnableScheduling
public class DispatcherRunner {

    private final JobInstanceMapper jobInstanceMapper;
    private final DispatchService dispatchService;

    public DispatcherRunner(JobInstanceMapper jobInstanceMapper, DispatchService dispatchService) {
        this.jobInstanceMapper = jobInstanceMapper;
        this.dispatchService = dispatchService;
    }

    @Scheduled(fixedDelay = 1000)
    public void dispatchLoop() {
        List<JobInstance> list = jobInstanceMapper.selectWaitingForDispatch(50);
        if (list == null || list.isEmpty()) return;

        for (JobInstance ins : list) {
            dispatchService.dispatchOne(ins);
        }
    }
}
