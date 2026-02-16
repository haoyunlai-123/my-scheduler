package com.my.scheduler.common.dto.job;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class JobCreateRequest {

    @NotBlank
    private String name;

    /** CRON / FIXED_RATE */
    @NotBlank
    private String scheduleType;

    /** cron 表达式 或 intervalMs（数字字符串） */
    @NotBlank
    private String scheduleExpr;

    /** HTTP / BEAN */
    @NotBlank
    private String handlerType;

    /** json 字符串，HTTP 时存 url/method/body 等；BEAN 时存 handlerName/params 等 */
    private String handlerParam;

    /** ROUND_ROBIN（先只支持这个） */
    private String routeStrategy = "ROUND_ROBIN";

    @Min(0)
    @Max(10)
    private Integer retryMax = 0;

    @Min(100)
    private Long timeoutMs = 10_000L;

    private Boolean enabled = true;
}
