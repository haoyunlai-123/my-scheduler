package com.my.scheduler.admin.controller;

import com.my.scheduler.admin.repository.JobInstanceMapper;
import com.my.scheduler.common.dto.ApiResponse;
import com.my.scheduler.common.dto.executor.ExecutionReportRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@RestController
@RequestMapping("/api/executions")
public class ExecutionController {

    private final JobInstanceMapper jobInstanceMapper;

    public ExecutionController(JobInstanceMapper jobInstanceMapper) {
        this.jobInstanceMapper = jobInstanceMapper;
    }

    @PostMapping("/report")
    public ApiResponse<Void> report(@Valid @RequestBody ExecutionReportRequest req) {
        log.info("report execution result: {}", req);
        String status = req.getStatus();
        if (!"SUCCESS".equals(status) && !"FAILED".equals(status)) {
            return ApiResponse.fail("invalid status: " + status);
        }

        LocalDateTime endTime = epochToLdt(req.getEndTimeEpochMs());
        Long duration = req.getDurationMs() == null ? 0L : req.getDurationMs();

        String err = req.getErrorMsg();
        if (err != null && err.length() > 2000) err = err.substring(0, 2000);

        jobInstanceMapper.markFinished(req.getInstanceId(), status, endTime, duration, err);
        log.info("marked instance {} as {}, duration: {} ms", req.getInstanceId(), status, duration);
        return ApiResponse.ok();
    }

    private LocalDateTime epochToLdt(Long epochMs) {
        if (epochMs == null) return LocalDateTime.now();
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMs), ZoneId.systemDefault());
    }
}
