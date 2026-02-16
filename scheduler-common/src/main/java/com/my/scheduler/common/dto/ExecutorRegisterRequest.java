package com.my.scheduler.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ExecutorRegisterRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String address; // host:port

    private String meta;    // json string, optional
}

