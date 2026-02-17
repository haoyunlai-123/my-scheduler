package com.my.scheduler.admin.infra;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "scheduler")
public class SchedulerProperties {

    private Executor executor = new Executor();

    @Data
    public static class Executor {
        private long heartbeatTimeoutMs = 15000;
    }
}
