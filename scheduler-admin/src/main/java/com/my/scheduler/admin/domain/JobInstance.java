package com.my.scheduler.admin.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobInstance {
    private Long id;
    private Long jobId;
    private LocalDateTime triggerTime;
    private Long executorId;
    private String status; // WAITING/RUNNING/SUCCESS/FAILED/TIMEOUT
    private Integer retryCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMs;
    private String lastError;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private java.time.LocalDateTime deadlineTime;
}
