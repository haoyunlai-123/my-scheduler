package com.my.scheduler.executor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "scheduler")
public class ExecutorProperties {

    private Admin admin = new Admin();
    private Executor executor = new Executor();

    @Data
    public static class Admin {
        private String baseUrl;
    }

    @Data
    public static class Executor {
        private String name;
        private String address;
        private String meta;
    }
}
