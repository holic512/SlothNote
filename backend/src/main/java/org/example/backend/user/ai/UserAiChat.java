package org.example.backend.user.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/user/ai")
public class UserAiChat {

    @Value("${xfyun.spark.api-key}")
    private String apiKey;

    @Value("${xfyun.spark.api-url}")
    private String apiUrl;

    @Value("${xfyun.spark.model}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicBoolean> completedFlags = new ConcurrentHashMap<>();

    /**
     * 处理AI聊天请求，返回SSE流式响应
     */
    @PostMapping("/chat")
    public SseEmitter chat(@RequestBody Map<String, Object> request) {
        String userMessage = (String) request.get("text");
        String userId = (String) request.get("user");
        
        // 创建SSE发射器，超时时间设为5分钟
        SseEmitter emitter = new SseEmitter(300000L);
        emitters.put(userId, emitter);
        
        // 创建完成状态标志
        AtomicBoolean completed = new AtomicBoolean(false);
        completedFlags.put(userId, completed);

        // 构造请求数据
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "generalv3.5"); // 指定模型
        
        // 构建消息数组
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", "你是知识渊博的助理"));
        messages.add(Map.of("role", "user", "content", userMessage));
        requestBody.put("messages", messages);
        
        // 启用流式输出
        requestBody.put("stream", true);
        
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        
        Thread chatThread = new Thread(() -> {
            try {
                String jsonBody = objectMapper.writeValueAsString(requestBody);
                
                // 发起REST请求
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.execute(apiUrl, HttpMethod.POST, httpRequest -> {
                    httpRequest.getHeaders().addAll(headers);
                    httpRequest.getBody().write(jsonBody.getBytes());
                }, response -> {
                    try {
                        BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.getBody())
                        );
                        String line;
                        
                        // 逐行读取响应内容
                        while ((line = reader.readLine()) != null && !completed.get()) {
                            if (line.startsWith("data:")) {
                                String content = line.substring(5).trim();
                                if (!content.isEmpty() && !content.equals("[DONE]")) {
                                    try {
                                        // 解析JSON数据
                                        JsonNode jsonData = objectMapper.readTree(content);
                                        JsonNode choices = jsonData.get("choices");
                                        
                                        if (choices != null && choices.isArray() && choices.size() > 0) {
                                            JsonNode delta = choices.get(0).get("delta");
                                            if (delta != null && delta.has("content")) {
                                                String deltaContent = delta.get("content").asText();
                                                
                                                // 构造一个带角色信息的增量内容对象
                                                Map<String, Object> streamChunk = new HashMap<>();
                                                streamChunk.put("role", "assistant");
                                                streamChunk.put("content", deltaContent);
                                                
                                                // 发送数据，注明是assistant的角色
                                                emitter.send(objectMapper.writeValueAsString(streamChunk), MediaType.APPLICATION_JSON);
                                            }
                                        }
                                    } catch (Exception e) {
                                        System.err.println("解析JSON失败: " + e.getMessage() + " for content: " + content);
                                    }
                                } else if (content.equals("[DONE]")) {
                                    completed.set(true);
                                    emitter.complete();
                                    break;
                                }
                            }
                        }
                        
                        // 流程正常结束
                        if (!completed.get()) {
                            completed.set(true);
                            emitter.complete();
                        }
                    } catch (IOException e) {
                        completed.set(true);
                        emitter.completeWithError(e);
                    }
                    return null;
                });
            } catch (Exception e) {
                completed.set(true);
                emitter.completeWithError(e);
            }
        });
        
        // 设置守护线程运行请求处理
        chatThread.setDaemon(true);
        chatThread.start();

        // 设置回调处理
        emitter.onCompletion(() -> {
            emitters.remove(userId);
            completedFlags.remove(userId);
            System.out.println("SSE连接已完成: " + userId);
        });
        
        emitter.onTimeout(() -> {
            emitters.remove(userId);
            completedFlags.remove(userId);
            System.out.println("SSE连接超时: " + userId);
        });
        
        emitter.onError((e) -> {
            emitters.remove(userId);
            completedFlags.remove(userId);
            System.err.println("SSE连接错误: " + e.getMessage());
        });

        return emitter;
    }

    /**
     * 终止特定用户的聊天
     */
    @PostMapping("/stop")
    public ResponseEntity<String> stopChat(@RequestBody Map<String, String> request) {
        String userId = request.get("user");
        SseEmitter emitter = emitters.get(userId);
        AtomicBoolean completed = completedFlags.get(userId);
        
        if (emitter != null) {
            if (completed != null) {
                completed.set(true);
            }
            emitter.complete();
            emitters.remove(userId);
            completedFlags.remove(userId);
            return ResponseEntity.ok("对话已终止");
        }
        return ResponseEntity.notFound().build();
    }
}