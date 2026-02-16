package com.my.scheduler.executor.registry;

import com.my.scheduler.common.dto.*;
import com.my.scheduler.executor.config.ExecutorProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RegistryClient {

    private final RestTemplate restTemplate;
    private final ExecutorProperties props;

    // 注册后缓存
    private volatile Long executorId;

    public RegistryClient(RestTemplate restTemplate, ExecutorProperties props) {
        this.restTemplate = restTemplate;
        this.props = props;
    }

    public Long getExecutorId() {
        return executorId;
    }

    /**
     * 注册到调度中心，获取 executorId；如果已经注册过了，则直接返回。
     * 用于向注册中心发送请求，注册当前 Executor 实例，并获取一个唯一的 executorId。
     * 这个 ID 将用于后续的心跳和任务执行请求中，以标识当前 Executor。
     */
    public synchronized void registerIfNeeded() {
        if (executorId != null) return;

        String url = props.getAdmin().getBaseUrl() + "/api/executors/register";
        ExecutorRegisterRequest req = new ExecutorRegisterRequest();
        req.setName(props.getExecutor().getName());
        req.setAddress(props.getExecutor().getAddress());
        req.setMeta(props.getExecutor().getMeta());

        ApiResponse<ExecutorRegisterResponse> resp =
                restTemplate.postForObject(url, req, ApiResponse.class);

        // 由于泛型擦除，resp.data 可能是 LinkedHashMap；我们稳妥点用二次读取方式：
        if (resp == null || resp.getCode() != 0 || resp.getData() == null) {
            throw new IllegalStateException("register failed: " + (resp == null ? "null" : resp.getMsg()));
        }

        // 兼容 LinkedHashMap
        Object data = resp.getData();
        Long id;
        if (data instanceof ExecutorRegisterResponse r) {
            id = r.getExecutorId();
        } else if (data instanceof java.util.Map<?, ?> m) {
            Object v = m.get("executorId");
            id = v == null ? null : Long.valueOf(String.valueOf(v));
        } else {
            id = Long.valueOf(String.valueOf(data));
        }

        if (id == null) throw new IllegalStateException("register returned null id");
        this.executorId = id;
    }

    /**
     * 发送心跳请求，保持与调度中心的连接活跃；如果尚未注册，则先注册。
     */
    public void heartbeat() {
        if (executorId == null) {
            registerIfNeeded();
        }
        String url = props.getAdmin().getBaseUrl() + "/api/executors/heartbeat";
        ExecutorHeartbeatRequest req = new ExecutorHeartbeatRequest();
        req.setExecutorId(executorId);

        ApiResponse<?> resp = restTemplate.postForObject(url, req, ApiResponse.class);
        if (resp == null || resp.getCode() != 0) {
            throw new IllegalStateException("heartbeat failed: " + (resp == null ? "null" : resp.getMsg()));
        }
    }
}
