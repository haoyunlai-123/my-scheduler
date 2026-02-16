package com.my.scheduler.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobResponse {
    private Long id;
    private String name;
    private String scheduleType;
    private String scheduleExpr;
    private String handlerType;
    private String handlerParam;
    private String routeStrategy;
    private Integer retryMax;
    private Long timeoutMs;
    private Integer enabled; // 与表一致：tinyint
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
