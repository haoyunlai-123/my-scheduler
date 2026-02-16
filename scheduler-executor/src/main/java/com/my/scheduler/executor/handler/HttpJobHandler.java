package com.my.scheduler.executor.handler;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class HttpJobHandler {

    private final RestTemplate restTemplate;

    public HttpJobHandler(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * executor 传入的 handlerParam 是一个 json 字符串，包含 url、method、body 等信息
     * 例如：{"url":"http://example.com/api","method":"POST","body":{"key":"value"}}
     * admin将任务通过请求发送给executor,executor执行任务**其实就是根据传入的任务参数阻塞发http请求**
     * @param handlerParam
     * @return
     */
    @SuppressWarnings("unchecked")
    public String execute(String handlerParam) {
        // handlerParam 是 json 字符串：{"url":"...","method":"GET","body":"..."}
        // 为了避免引入 Jackson 复杂映射，可以先用 Spring 自带的 Jackson ObjectMapper（boot 默认有）
        try {
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> m = om.readValue(handlerParam, Map.class);

            String url = String.valueOf(m.get("url"));
            String method = String.valueOf(m.getOrDefault("method", "GET")).toUpperCase();
            Object body = m.get("body");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<?> entity = (body == null) ? new HttpEntity<>(headers) : new HttpEntity<>(body, headers);

            ResponseEntity<String> resp = restTemplate.exchange(
                    url,
                    HttpMethod.valueOf(method),
                    entity,
                    String.class
            );
            return "HTTP " + resp.getStatusCode().value() + ": " + (resp.getBody() == null ? "" : resp.getBody());
        } catch (Exception e) {
            throw new RuntimeException("http handler error: " + e.getMessage(), e);
        }
    }
}
