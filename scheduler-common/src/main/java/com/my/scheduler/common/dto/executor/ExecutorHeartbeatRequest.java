package com.my.scheduler.common.dto.executor;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExecutorHeartbeatRequest {
    @NotNull
    private Long executorId;
}
