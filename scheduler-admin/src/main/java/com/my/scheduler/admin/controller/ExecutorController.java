package com.my.scheduler.admin.controller;

import com.my.scheduler.admin.service.ExecutorService;
import com.my.scheduler.common.dto.*;
import com.my.scheduler.common.dto.executor.ExecutorHeartbeatRequest;
import com.my.scheduler.common.dto.executor.ExecutorRegisterRequest;
import com.my.scheduler.common.dto.executor.ExecutorRegisterResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/executors")
public class ExecutorController {

    private final ExecutorService executorService;

    public ExecutorController(ExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * 执行器注册
     * @param req
     * @return
     */
    @PostMapping("/register")
    public ApiResponse<ExecutorRegisterResponse> register(@Valid @RequestBody ExecutorRegisterRequest req) {
        Long id = executorService.register(req);
        return ApiResponse.ok(new ExecutorRegisterResponse(id));
    }

    /**
     * 执行器心跳
     * @param req
     * @return
     */
    @PostMapping("/heartbeat")
    public ApiResponse<Void> heartbeat(@Valid @RequestBody ExecutorHeartbeatRequest req) {
        executorService.heartbeat(req.getExecutorId());
        return ApiResponse.ok();
    }
}

