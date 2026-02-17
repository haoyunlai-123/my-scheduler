package com.my.scheduler.admin.scheduler;

import com.my.scheduler.admin.infra.SchedulerProperties;
import com.my.scheduler.admin.repository.ExecutorNodeMapper;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@EnableScheduling
public class ExecutorHealthRunner {

    private final ExecutorNodeMapper executorNodeMapper;
    private final SchedulerProperties props;

    public ExecutorHealthRunner(ExecutorNodeMapper executorNodeMapper, SchedulerProperties props) {
        this.executorNodeMapper = executorNodeMapper;
        this.props = props;
    }

    @Scheduled(fixedDelay = 3000)
    public void markOffline() {
        long timeoutMs = props.getExecutor().getHeartbeatTimeoutMs();
        LocalDateTime threshold = LocalDateTime.now().minusNanos(timeoutMs * 1_000_000);
        executorNodeMapper.markOfflineBefore(threshold);
    }
}
