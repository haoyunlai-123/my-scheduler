package com.my.scheduler.executor.report;

import com.my.scheduler.common.dto.ApiResponse;
import com.my.scheduler.common.dto.executor.ExecutionReportRequest;
import com.my.scheduler.executor.config.ExecutorProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ReportClient {

    private final RestTemplate restTemplate;
    private final ExecutorProperties props;

    public ReportClient(RestTemplate restTemplate, ExecutorProperties props) {
        this.restTemplate = restTemplate;
        this.props = props;
    }

    /**
     * 用于将executor执行结果上报给调度中心
     * @param req
     */
    public void report(ExecutionReportRequest req) {
        String url = props.getAdmin().getBaseUrl() + "/api/executions/report";
        ApiResponse<?> resp = restTemplate.postForObject(url, req, ApiResponse.class);
        if (resp == null || resp.getCode() != 0) {
            // 上报失败先不抛异常导致线程崩掉，打印即可（后续可以做重试队列）
            System.err.println("report failed: " + (resp == null ? "null" : resp.getMsg()));
        }
    }
}
