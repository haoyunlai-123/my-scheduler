package com.my.scheduler.executor.controller;

import com.my.scheduler.common.dto.*;
import com.my.scheduler.common.dto.executor.ExecuteRequest;
import com.my.scheduler.common.dto.executor.ExecuteResponse;
import com.my.scheduler.common.dto.executor.ExecutionReportRequest;
import com.my.scheduler.executor.handler.HttpJobHandler;
import com.my.scheduler.executor.report.ReportClient;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.*;

@Slf4j
@RestController
@RequestMapping("/api")
public class ExecuteController {

    private final HttpJobHandler httpJobHandler;
    private final ReportClient reportClient;

    private final ConcurrentHashMap<Long, Boolean> inflight = new ConcurrentHashMap<>();

    // 简单线程池：后续可配置化
    private final ExecutorService pool = Executors.newFixedThreadPool(8);

    public ExecuteController(HttpJobHandler httpJobHandler, ReportClient reportClient) {
        this.httpJobHandler = httpJobHandler;
        this.reportClient = reportClient;
    }

    @PostMapping("/execute")
    public ApiResponse<ExecuteResponse> execute(@Valid @RequestBody ExecuteRequest req) {
        // 快速接收，异步执行
        if (inflight.putIfAbsent(req.getInstanceId(), true) != null) {
            return ApiResponse.ok(new ExecuteResponse(false));
        }

        // 立刻 ack
        pool.submit(() -> {
            try {
                // do execute
                runAndReport(req);
                log.info("execute req={}", req);
            } finally {
                inflight.remove(req.getInstanceId());
            }
        });
        return ApiResponse.ok(new ExecuteResponse(true));
    }

    private ApiResponse<ExecuteResponse> runAndReport(ExecuteRequest req) {
        long start = System.currentTimeMillis();
        String status = "SUCCESS";
        String err = null;
        String summary = null;

        try {
            if ("HTTP".equalsIgnoreCase(req.getHandlerType())) {
                summary = httpJobHandler.execute(req.getHandlerParam());
                if (inflight.putIfAbsent(req.getInstanceId(), Boolean.TRUE) != null) {
                    log.warn("duplicate execute ignored, instanceId={}", req.getInstanceId());
                    return ApiResponse.ok(new ExecuteResponse(false));
                }
            } else {
                throw new UnsupportedOperationException("unsupported handlerType: " + req.getHandlerType());
            }
        } catch (Exception e) {
            status = "FAILED";
            err = e.getMessage();
            if (err != null && err.length() > 2000) err = err.substring(0, 2000);
        }

        long end = System.currentTimeMillis();

        ExecutionReportRequest report = new ExecutionReportRequest();
        report.setInstanceId(req.getInstanceId());
        report.setStatus(status);
        report.setStartTimeEpochMs(start);
        report.setEndTimeEpochMs(end);
        report.setDurationMs(end - start);
        report.setErrorMsg(err);
        report.setResultSummary(summary);

        log.info("execution report: {}", report);
        reportClient.report(report);
        return null;
    }
}
