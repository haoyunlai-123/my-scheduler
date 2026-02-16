package com.my.scheduler.common.dto.executor;

import lombok.Data;

/**
 * executor执行结果上报请求
 */
@Data
public class ExecutionReportRequest {
    private Long instanceId;

    /** SUCCESS / FAILED */
    private String status;

    private Long startTimeEpochMs;
    private Long endTimeEpochMs;
    private Long durationMs;

    /** 失败时写入错误摘要（控制长度） */
    private String errorMsg;

    /** 可选：执行结果摘要 */
    private String resultSummary;
}
