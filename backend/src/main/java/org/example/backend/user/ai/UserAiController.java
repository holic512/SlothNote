package org.example.backend.user.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("user/ai")
public class UserAiController {

    @Value("${xfyun.spark.api-key}")
    private String apiKey;

    @Value("${xfyun.spark.api-url}")
    private String apiUrl;

    @Value("${xfyun.spark.model}")
    private String model;

    @PostMapping("/explain")
    public ResponseEntity<Map<String, Object>> explainText(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        if (text == null || text.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Text cannot be empty"));
        }

        try {
            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            // 构建请求体 - 修正后的版本
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);

            List<Map<String, String>> messages = new ArrayList<>();
            // 系统角色消息
            messages.add(new HashMap<String, String>() {{
                put("role", "system");
                put("content", "你是一个专业的文本解释助手，能够清晰准确地解释任何文本内容");
            }});

            // 用户角色消息 - 必须包含content
            messages.add(new HashMap<String, String>() {{
                put("role", "user");
                put("content", "请解释以下文本内容: " + text);
            }});

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1024);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 发送请求
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class);

            // 处理响应
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("explanation", extractExplanation(response.getBody()));
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(response.getStatusCode())
                        .body(Collections.singletonMap("error",
                                "Failed to get explanation from AI: " +
                                        (response.getBody() != null ? response.getBody().toString() : "No response body")));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Collections.singletonMap("error", "Internal server error: " + e.getMessage()));
        }
    }

    private String extractExplanation(Map<String, Object> response) {
        try {
            // 修正后的解析逻辑
            if (response.containsKey("choices") && response.get("choices") instanceof List) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    if (choice.containsKey("message")) {
                        Map<String, Object> message = (Map<String, Object>) choice.get("message");
                        if (message.containsKey("content")) {
                            return message.get("content").toString();
                        }
                    }
                }
            }
            return "No explanation available from API response";
        } catch (Exception e) {
            return "Error parsing API response: " + e.getMessage();
        }
    }
}