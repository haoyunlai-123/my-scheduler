package com.my.scheduler.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 接口响应对象
 * @param <T>
 */
@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private int code;      // 0 ok
    private String msg;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "OK", data);
    }

    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<>(0, "OK", null);
    }

    public static <T> ApiResponse<T> fail(String msg) {
        return new ApiResponse<>(-1, msg, null);
    }
}
