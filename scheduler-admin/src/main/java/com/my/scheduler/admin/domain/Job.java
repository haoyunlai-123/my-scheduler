package com.my.scheduler.admin.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Job {
    private Long id;
    private String name;
    private String scheduleType;
    private String scheduleExpr;
    private String handlerType;
    private String handlerParam;
    private String routeStrategy;
    private Integer retryMax;
    private Long timeoutMs;
    private Integer enabled; // 0/1
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
