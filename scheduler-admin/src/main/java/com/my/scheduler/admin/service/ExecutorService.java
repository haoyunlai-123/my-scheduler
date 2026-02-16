package com.my.scheduler.admin.service;

import com.my.scheduler.admin.domain.ExecutorNode;
import com.my.scheduler.admin.repository.ExecutorNodeMapper;
import com.my.scheduler.common.dto.executor.ExecutorRegisterRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ExecutorService {

    private final ExecutorNodeMapper mapper;

    public ExecutorService(ExecutorNodeMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 注册执行器节点
     * @param req
     * @return
     */
    @Transactional
    public Long register(ExecutorRegisterRequest req) {
        LocalDateTime now = LocalDateTime.now();

        ExecutorNode exist = mapper.selectByAddress(req.getAddress());
        if (exist == null) {
            ExecutorNode node = new ExecutorNode();
            node.setName(req.getName());
            node.setAddress(req.getAddress());
            node.setMeta(req.getMeta());
            node.setStatus("ONLINE");
            node.setLastHeartbeatAt(now);
            mapper.insert(node);
            return node.getId();
        }

        // 已存在：更新名称、meta、心跳、状态
        mapper.updateOnRegister(exist.getId(), req.getName(), req.getMeta(), now, "ONLINE");
        return exist.getId();
    }

    /**
     * 执行器心跳
     * @param executorId
     */
    @Transactional
    public void heartbeat(Long executorId) {
        LocalDateTime now = LocalDateTime.now();
        ExecutorNode node = mapper.selectById(executorId);
        if (node == null) {
            throw new IllegalArgumentException("executor not found: " + executorId);
        }
        mapper.updateHeartbeat(executorId, now, "ONLINE");
    }
}
