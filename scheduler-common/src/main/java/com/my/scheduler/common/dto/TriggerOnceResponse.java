package com.my.scheduler.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TriggerOnceResponse {
    private Long jobId;
    private Long instanceId;
    private boolean accepted;
}
