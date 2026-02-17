package com.my.scheduler.admin.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobScheduleState {
    private Long jobId;
    private LocalDateTime nextTriggerTime;
    private LocalDateTime updatedAt;
}
