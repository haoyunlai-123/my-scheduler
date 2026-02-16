package com.my.scheduler.admin.controller;

import com.my.scheduler.admin.domain.Job;
import com.my.scheduler.admin.domain.JobInstance;
import com.my.scheduler.admin.service.JobService;
import com.my.scheduler.common.dto.*;
import com.my.scheduler.common.dto.job.JobCreateRequest;
import com.my.scheduler.common.dto.job.JobUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody JobCreateRequest req) {
        return ApiResponse.ok(jobService.create(req));
    }

    @PutMapping
    public ApiResponse<Void> update(@Valid @RequestBody JobUpdateRequest req) {
        jobService.update(req);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}")
    public ApiResponse<Job> get(@PathVariable Long id) {
        return ApiResponse.ok(jobService.get(id));
    }

    @GetMapping
    public ApiResponse<List<Job>> list() {
        return ApiResponse.ok(jobService.list());
    }

    @PostMapping("/{id}/enable")
    public ApiResponse<Void> enable(@PathVariable Long id) {
        jobService.enable(id);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/disable")
    public ApiResponse<Void> disable(@PathVariable Long id) {
        jobService.disable(id);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/trigger")
    public ApiResponse<TriggerOnceResponse> trigger(@PathVariable Long id) {
        Long instanceId = jobService.triggerOnce(id);
        return ApiResponse.ok(new TriggerOnceResponse(instanceId));
    }

    @GetMapping("/{id}/instances")
    public ApiResponse<List<JobInstance>> instances(@PathVariable Long id,
                                                    @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(jobService.latestInstances(id, Math.min(limit, 200)));
    }
}
