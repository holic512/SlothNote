/**
 * @file AiConfigService
 * @project SlothNote
 * @module 公共配置 / AI 配置
 * @description 统一维护系统级 AI 配置，供管理端编辑并供用户 AI 服务读取。
 * @logic 1. 自动创建单例配置；2. 管理端返回脱敏视图；3. 保存时校验参数；4. 运行时返回完整启用配置。
 * @dependencies Repository: SystemAiConfigRepository, DTO: AiConfigDto/AiConfigUpdateRequest
 * @index_tags AI配置, 系统配置, 脱敏, OpenAI兼容, 运行时配置
 * @author holic512
 */
package org.example.backend.common.config.ai;

import org.example.backend.common.dto.ai.AiConfigDto;
import org.example.backend.common.dto.ai.AiConfigTestRequest;
import org.example.backend.common.dto.ai.AiConfigUpdateRequest;
import org.example.backend.common.entity.SystemAiConfig;
import org.example.backend.common.repository.SystemAiConfigRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class AiConfigService {

    private static final Long SINGLETON_ID = 1L;
    private static final String DEFAULT_PROVIDER = "openai-compatible";
    private static final String DEFAULT_TEST_PROMPT = "请用一句中文回复：AI 配置测试成功";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SystemAiConfigRepository systemAiConfigRepository;

    public AiConfigService(SystemAiConfigRepository systemAiConfigRepository) {
        this.systemAiConfigRepository = systemAiConfigRepository;
    }

    @Transactional
    public AiConfigDto getPublicConfig() {
        return toDto(getOrCreateConfig());
    }

    @Transactional
    public AiConfigDto updateConfig(AiConfigUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AI 配置不能为空");
        }

        SystemAiConfig config = getOrCreateConfig();
        config.setProviderName(defaultIfBlank(request.getProviderName(), DEFAULT_PROVIDER));
        config.setBaseUrl(trimToNull(request.getBaseUrl()));
        config.setModel(trimToNull(request.getModel()));

        String apiKey = trimToNull(request.getApiKey());
        if (apiKey != null) {
            config.setApiKey(apiKey);
        }

        config.setTemperature(validateTemperature(request.getTemperature(), "temperature"));
        config.setMaxTokens(validatePositiveInt(request.getMaxTokens(), "maxTokens"));
        config.setPlannerTemperature(validateTemperature(request.getPlannerTemperature(), "plannerTemperature"));
        config.setPlannerMaxTokens(validatePositiveInt(request.getPlannerMaxTokens(), "plannerMaxTokens"));
        config.setEnabled(Boolean.TRUE.equals(request.getEnabled()) ? 1 : 0);

        if (config.getEnabled() == 1) {
            validateRunnable(config);
        }

        return toDto(systemAiConfigRepository.save(config));
    }

    @Transactional(readOnly = true)
    public RuntimeConfig requireEnabledConfig() {
        SystemAiConfig config = systemAiConfigRepository.findById(SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("AI 配置未初始化，请先在管理端系统设置中配置 AI 服务"));
        validateRunnable(config);
        return new RuntimeConfig(
                config.getProviderName(),
                config.getBaseUrl().trim(),
                config.getApiKey().trim(),
                config.getModel().trim(),
                config.getTemperature(),
                config.getMaxTokens(),
                config.getPlannerTemperature(),
                config.getPlannerMaxTokens()
        );
    }

    @Transactional(readOnly = true)
    public String testConfig(AiConfigTestRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AI 测试配置不能为空");
        }
        RuntimeConfig runtimeConfig = buildTestRuntimeConfig(request);
        String prompt = defaultIfBlank(request.getPrompt(), DEFAULT_TEST_PROMPT);
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", runtimeConfig.model(),
                    "temperature", runtimeConfig.temperature(),
                    "max_tokens", Math.min(runtimeConfig.maxTokens(), 512),
                    "messages", List.of(Map.of("role", "user", "content", prompt))
            );
            return callAiForSingleMessage(runtimeConfig, requestBody);
        } catch (Exception ex) {
            throw new IllegalStateException("AI 配置测试失败：" + ex.getMessage(), ex);
        }
    }

    private SystemAiConfig getOrCreateConfig() {
        return systemAiConfigRepository.findById(SINGLETON_ID).orElseGet(() -> {
            SystemAiConfig config = new SystemAiConfig();
            config.setId(SINGLETON_ID);
            config.setProviderName(DEFAULT_PROVIDER);
            config.setEnabled(0);
            return systemAiConfigRepository.save(config);
        });
    }

    private void validateRunnable(SystemAiConfig config) {
        if (config.getEnabled() == null || config.getEnabled() != 1) {
            throw new IllegalStateException("AI 配置未启用，请先在管理端系统设置中启用 AI 服务");
        }
        validateConnectivity(config);
    }

    private void validateConnectivity(SystemAiConfig config) {
        if (isBlank(config.getBaseUrl())) {
            throw new IllegalStateException("AI Base URL 未配置");
        }
        if (isBlank(config.getApiKey())) {
            throw new IllegalStateException("AI API Key 未配置");
        }
        if (isBlank(config.getModel())) {
            throw new IllegalStateException("AI 模型名称未配置");
        }
    }

    private RuntimeConfig buildTestRuntimeConfig(AiConfigUpdateRequest request) {
        SystemAiConfig config = systemAiConfigRepository.findById(SINGLETON_ID)
                .map(this::copyConfig)
                .orElseGet(this::newDefaultConfig);
        config.setProviderName(defaultIfBlank(request.getProviderName(), defaultIfBlank(config.getProviderName(), DEFAULT_PROVIDER)));
        config.setBaseUrl(defaultIfBlank(request.getBaseUrl(), config.getBaseUrl()));
        config.setModel(defaultIfBlank(request.getModel(), config.getModel()));

        String apiKey = trimToNull(request.getApiKey());
        if (apiKey != null) {
            config.setApiKey(apiKey);
        }

        config.setTemperature(request.getTemperature() == null ? defaultDouble(config.getTemperature(), 0.7) : validateTemperature(request.getTemperature(), "temperature"));
        config.setMaxTokens(request.getMaxTokens() == null ? defaultInteger(config.getMaxTokens(), 4096) : validatePositiveInt(request.getMaxTokens(), "maxTokens"));
        config.setPlannerTemperature(request.getPlannerTemperature() == null ? defaultDouble(config.getPlannerTemperature(), 0.1) : validateTemperature(request.getPlannerTemperature(), "plannerTemperature"));
        config.setPlannerMaxTokens(request.getPlannerMaxTokens() == null ? defaultInteger(config.getPlannerMaxTokens(), 256) : validatePositiveInt(request.getPlannerMaxTokens(), "plannerMaxTokens"));

        validateConnectivity(config);
        return new RuntimeConfig(
                defaultIfBlank(config.getProviderName(), DEFAULT_PROVIDER),
                config.getBaseUrl().trim(),
                config.getApiKey().trim(),
                config.getModel().trim(),
                config.getTemperature(),
                config.getMaxTokens(),
                config.getPlannerTemperature(),
                config.getPlannerMaxTokens()
        );
    }

    private SystemAiConfig newDefaultConfig() {
        SystemAiConfig config = new SystemAiConfig();
        config.setId(SINGLETON_ID);
        config.setProviderName(DEFAULT_PROVIDER);
        config.setEnabled(0);
        return config;
    }

    private SystemAiConfig copyConfig(SystemAiConfig source) {
        SystemAiConfig config = new SystemAiConfig();
        config.setId(source.getId());
        config.setProviderName(source.getProviderName());
        config.setBaseUrl(source.getBaseUrl());
        config.setApiKey(source.getApiKey());
        config.setModel(source.getModel());
        config.setTemperature(source.getTemperature());
        config.setMaxTokens(source.getMaxTokens());
        config.setPlannerTemperature(source.getPlannerTemperature());
        config.setPlannerMaxTokens(source.getPlannerMaxTokens());
        config.setEnabled(source.getEnabled());
        return config;
    }

    private String callAiForSingleMessage(RuntimeConfig aiConfig, Map<String, Object> requestBody) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + aiConfig.apiKey());
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.execute(resolveChatCompletionsUrl(aiConfig.baseUrl()), HttpMethod.POST, httpRequest -> {
            httpRequest.getHeaders().addAll(headers);
            httpRequest.getBody().write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }, response -> {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                return "";
            }
            return choices.get(0).path("message").path("content").asText("");
        });
    }

    private String resolveChatCompletionsUrl(String apiBaseUrl) {
        String normalized = apiBaseUrl == null ? "" : apiBaseUrl.trim();
        if (normalized.isEmpty()) {
            throw new IllegalStateException("AI Base URL 未配置");
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.endsWith("/chat/completions")) {
            return normalized;
        }
        if (normalized.endsWith("/v1")) {
            return normalized + "/chat/completions";
        }
        return normalized + "/v1/chat/completions";
    }

    private AiConfigDto toDto(SystemAiConfig config) {
        String apiKey = trimToNull(config.getApiKey());
        return new AiConfigDto(
                defaultIfBlank(config.getProviderName(), DEFAULT_PROVIDER),
                nullToEmpty(config.getBaseUrl()),
                nullToEmpty(config.getModel()),
                config.getTemperature(),
                config.getMaxTokens(),
                config.getPlannerTemperature(),
                config.getPlannerMaxTokens(),
                config.getEnabled() != null && config.getEnabled() == 1,
                apiKey != null,
                maskApiKey(apiKey)
        );
    }

    private Double validateTemperature(Double value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        if (value < 0 || value > 2) {
            throw new IllegalArgumentException(fieldName + " 必须在 0 到 2 之间");
        }
        return value;
    }

    private Integer validatePositiveInt(Integer value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " 必须大于 0");
        }
        return value;
    }

    private Double defaultDouble(Double value, Double defaultValue) {
        return value == null ? defaultValue : value;
    }

    private Integer defaultInteger(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String maskApiKey(String apiKey) {
        if (apiKey == null) {
            return null;
        }
        if (apiKey.length() <= 8) {
            return "********";
        }
        return apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4);
    }

    private String defaultIfBlank(String value, String defaultValue) {
        String normalized = trimToNull(value);
        return normalized == null ? defaultValue : normalized;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public record RuntimeConfig(
            String providerName,
            String baseUrl,
            String apiKey,
            String model,
            Double temperature,
            Integer maxTokens,
            Double plannerTemperature,
            Integer plannerMaxTokens
    ) {
    }
}
