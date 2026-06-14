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
import org.example.backend.common.dto.ai.AiConfigUpdateRequest;
import org.example.backend.common.entity.SystemAiConfig;
import org.example.backend.common.repository.SystemAiConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiConfigService {

    private static final Long SINGLETON_ID = 1L;
    private static final String DEFAULT_PROVIDER = "openai-compatible";

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
