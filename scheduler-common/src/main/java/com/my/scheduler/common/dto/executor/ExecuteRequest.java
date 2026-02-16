package com.my.scheduler.common.dto.executor;

import lombok.Data;

/**
 * admin向executor发送的执行请求
 */
@Data
public class ExecuteRequest {
    private Long instanceId;
    private Long jobId;

    /** HTTP / BEAN */
    private String handlerType;

    /** handlerParam 原样带过去：HTTP 时 json：{url,method,body} */
    private String handlerParam;

    private Long timeoutMs;
}
