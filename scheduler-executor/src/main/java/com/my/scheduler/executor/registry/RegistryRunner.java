package com.my.scheduler.executor.registry;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class RegistryRunner {

    private static final Logger log = LoggerFactory.getLogger(RegistryRunner.class);

    private final RegistryClient client;

    public RegistryRunner(RegistryClient client) {
        this.client = client;
    }

    @PostConstruct
    public void init() {
        try {
            client.registerIfNeeded();
            log.info("executor registered, id={}", client.getExecutorId());
        } catch (Exception e) {
            log.error("register failed on startup", e);
            // 不要让启动失败：后续心跳会再次尝试
        }
    }

    @Scheduled(fixedDelay = 5000)
    public void heartbeatLoop() {
        try {
            client.heartbeat();
            log.debug("heartbeat ok, id={}", client.getExecutorId());
        } catch (Exception e) {
            log.warn("heartbeat failed: {}", e.getMessage());
        }
    }
}
