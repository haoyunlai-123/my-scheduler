package com.my.scheduler.admin.service;

import com.my.scheduler.admin.domain.ExecutorNode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RouterService {

    // 每个 jobId 一个轮询计数器（内存态，重启会重置，但足够简历版）
    private final ConcurrentHashMap<Long, AtomicInteger> rr = new ConcurrentHashMap<>();

    public ExecutorNode pickRoundRobin(Long jobId, List<ExecutorNode> nodes) {
        if (nodes == null || nodes.isEmpty()) return null;
        AtomicInteger idx = rr.computeIfAbsent(jobId, k -> new AtomicInteger(0));
        int i = Math.abs(idx.getAndIncrement());
        return nodes.get(i % nodes.size());
    }
}
