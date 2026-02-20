package com.my.scheduler.common.dto.executor;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExecutionReportResponse {
    private boolean accepted;   // 是否被 admin 接受写库
    private String reason;      // ignored 的原因
}