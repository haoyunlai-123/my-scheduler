package com.my.scheduler.admin.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 执行器节点
 */
@Data
public class ExecutorNode {
    private Long id;
    private String name;
    private String address;
    private String status; // ONLINE/OFFLINE
    private LocalDateTime lastHeartbeatAt;
    private String meta;
}

