package com.my.scheduler.common.dto.executor;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * executor接收任务响应
 */
@Data
@AllArgsConstructor
public class ExecuteResponse {
    /** 是否已接收（接收成功即可，不代表执行成功） */
    private boolean accepted;
}
