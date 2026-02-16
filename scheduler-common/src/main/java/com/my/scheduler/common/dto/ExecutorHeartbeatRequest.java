package com.my.scheduler.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExecutorHeartbeatRequest {
    @NotNull
    private Long executorId;
}
