/**
 * @file AiConfigUpdateRequest
 * @project SlothNote
 * @module 公共配置 / AI 配置
 * @description 接收管理端提交的 AI 配置更新参数。
 * @logic 1. 支持连接参数更新；2. apiKey 为空时由服务层保留旧值；3. 支持生成参数调整。
 * @dependencies 无强依赖
 * @index_tags AI配置, 更新请求, 管理端, 参数校验
 * @author holic512
 */
package org.example.backend.common.dto.ai;

import lombok.Data;

@Data
public class AiConfigUpdateRequest {

    private String providerName;
    private String baseUrl;
    private String apiKey;
    private String model;
    private Double temperature;
    private Integer maxTokens;
    private Double plannerTemperature;
    private Integer plannerMaxTokens;
    private Boolean enabled;
}
