package com.my.scheduler.common.dto.job;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class JobUpdateRequest {

    @NotNull
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String scheduleType;

    @NotBlank
    private String scheduleExpr;

    @NotBlank
    private String handlerType;

    private String handlerParam;

    private String routeStrategy = "ROUND_ROBIN";

    @Min(0)
    @Max(10)
    private Integer retryMax = 0;

    @Min(100)
    private Long timeoutMs = 10_000L;
}
