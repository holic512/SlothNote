/**
 * @file MailConfigService
 * @project SlothNote
 * @module 公共配置 / 邮箱配置
 * @description 统一维护系统级 SMTP 邮箱配置，供管理端编辑并供邮件发送客户端读取。
 * @logic 1. 自动创建单例配置；2. 管理端返回脱敏视图；3. 保存时校验参数；4. 运行时创建 JavaMailSender。
 * @dependencies Repository: SystemMailConfigRepository, DTO: MailConfigDto/MailConfigUpdateRequest
 * @index_tags 邮箱配置, SMTP, 系统配置, 脱敏, JavaMailSender
 * @author holic512
 */
package org.example.backend.common.config.mail;

import org.example.backend.common.dto.mail.MailConfigDto;
import org.example.backend.common.dto.mail.MailConfigUpdateRequest;
import org.example.backend.common.entity.SystemMailConfig;
import org.example.backend.common.repository.SystemMailConfigRepository;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Service
public class MailConfigService {

    private static final Long SINGLETON_ID = 1L;
    private static final int DEFAULT_PORT = 465;
    private static final String DEFAULT_PROTOCOL = "smtps";
    private static final String DEFAULT_ENCODING = StandardCharsets.UTF_8.name();

    private final SystemMailConfigRepository systemMailConfigRepository;

    public MailConfigService(SystemMailConfigRepository systemMailConfigRepository) {
        this.systemMailConfigRepository = systemMailConfigRepository;
    }

    @Transactional
    public MailConfigDto getPublicConfig() {
        return toDto(getOrCreateConfig());
    }

    @Transactional
    public MailConfigDto updateConfig(MailConfigUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("邮箱配置不能为空");
        }

        SystemMailConfig config = getOrCreateConfig();
        applyRequest(config, request);

        if (config.getEnabled() == 1) {
            validateRunnable(config);
        }

        return toDto(systemMailConfigRepository.save(config));
    }

    @Transactional(readOnly = true)
    public RuntimeConfig requireEnabledConfig() {
        SystemMailConfig config = systemMailConfigRepository.findById(SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("邮箱配置未初始化，请先在管理端系统设置中配置邮箱服务"));
        validateRunnable(config);
        return toRuntimeConfig(config);
    }

    @Transactional(readOnly = true)
    public RuntimeConfig buildTestConfig(MailConfigUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("邮箱测试配置不能为空");
        }
        SystemMailConfig config = systemMailConfigRepository.findById(SINGLETON_ID)
                .map(this::copyConfig)
                .orElseGet(this::newDefaultConfig);
        applyRequest(config, request);
        validateConnectivity(config);
        return toRuntimeConfig(config);
    }

    public JavaMailSenderImpl createMailSender(RuntimeConfig config) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(config.host());
        sender.setPort(config.port());
        sender.setUsername(config.username());
        sender.setPassword(config.password());
        sender.setProtocol(config.protocol());
        sender.setDefaultEncoding(config.defaultEncoding());

        Properties properties = sender.getJavaMailProperties();
        properties.put("mail.transport.protocol", config.protocol());
        properties.put("mail.smtp.auth", String.valueOf(config.smtpAuth()));
        properties.put("mail.smtp.ssl.enable", String.valueOf(config.sslEnable()));
        properties.put("mail.smtp.starttls.enable", String.valueOf(config.starttlsEnable()));
        properties.put("mail.smtp.starttls.required", String.valueOf(config.starttlsRequired()));
        properties.put("mail.smtps.auth", String.valueOf(config.smtpAuth()));
        properties.put("mail.smtps.ssl.enable", String.valueOf(config.sslEnable()));
        properties.put("mail.smtps.starttls.enable", String.valueOf(config.starttlsEnable()));
        properties.put("mail.smtps.starttls.required", String.valueOf(config.starttlsRequired()));
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.writetimeout", "10000");
        properties.put("mail.smtps.connectiontimeout", "10000");
        properties.put("mail.smtps.timeout", "10000");
        properties.put("mail.smtps.writetimeout", "10000");
        return sender;
    }

    private void applyRequest(SystemMailConfig config, MailConfigUpdateRequest request) {
        config.setHost(trimToNull(request.getHost()));
        config.setPort(validatePort(request.getPort()));
        config.setUsername(trimToNull(request.getUsername()));
        config.setProtocol(defaultIfBlank(request.getProtocol(), DEFAULT_PROTOCOL).toLowerCase());
        config.setDefaultEncoding(validateEncoding(defaultIfBlank(request.getDefaultEncoding(), DEFAULT_ENCODING)));
        config.setFromAddress(trimToNull(request.getFromAddress()));
        config.setFromName(trimToNull(request.getFromName()));
        config.setSmtpAuth(toInt(valueOrDefault(request.getSmtpAuth(), true)));
        config.setSslEnable(toInt(valueOrDefault(request.getSslEnable(), true)));
        config.setStarttlsEnable(toInt(valueOrDefault(request.getStarttlsEnable(), true)));
        config.setStarttlsRequired(toInt(valueOrDefault(request.getStarttlsRequired(), true)));
        config.setEnabled(Boolean.TRUE.equals(request.getEnabled()) ? 1 : 0);

        String password = trimToNull(request.getPassword());
        if (password != null) {
            config.setPassword(password);
        }
    }

    private SystemMailConfig getOrCreateConfig() {
        return systemMailConfigRepository.findById(SINGLETON_ID).orElseGet(() -> systemMailConfigRepository.save(newDefaultConfig()));
    }

    private SystemMailConfig newDefaultConfig() {
        SystemMailConfig config = new SystemMailConfig();
        config.setId(SINGLETON_ID);
        config.setPort(DEFAULT_PORT);
        config.setProtocol(DEFAULT_PROTOCOL);
        config.setDefaultEncoding(DEFAULT_ENCODING);
        config.setSmtpAuth(1);
        config.setSslEnable(1);
        config.setStarttlsEnable(1);
        config.setStarttlsRequired(1);
        config.setEnabled(0);
        return config;
    }

    private SystemMailConfig copyConfig(SystemMailConfig source) {
        SystemMailConfig config = new SystemMailConfig();
        config.setId(source.getId());
        config.setHost(source.getHost());
        config.setPort(source.getPort());
        config.setUsername(source.getUsername());
        config.setPassword(source.getPassword());
        config.setProtocol(source.getProtocol());
        config.setDefaultEncoding(source.getDefaultEncoding());
        config.setFromAddress(source.getFromAddress());
        config.setFromName(source.getFromName());
        config.setSmtpAuth(source.getSmtpAuth());
        config.setSslEnable(source.getSslEnable());
        config.setStarttlsEnable(source.getStarttlsEnable());
        config.setStarttlsRequired(source.getStarttlsRequired());
        config.setEnabled(source.getEnabled());
        return config;
    }

    private void validateRunnable(SystemMailConfig config) {
        if (config.getEnabled() == null || config.getEnabled() != 1) {
            throw new IllegalStateException("邮箱配置未启用，请先在管理端系统设置中启用邮箱服务");
        }
        validateConnectivity(config);
    }

    private void validateConnectivity(SystemMailConfig config) {
        if (isBlank(config.getHost())) {
            throw new IllegalStateException("SMTP Host 未配置");
        }
        if (config.getPort() == null || config.getPort() <= 0 || config.getPort() > 65535) {
            throw new IllegalStateException("SMTP Port 必须在 1 到 65535 之间");
        }
        if (isBlank(config.getUsername())) {
            throw new IllegalStateException("SMTP 用户名未配置");
        }
        if (isBlank(config.getPassword())) {
            throw new IllegalStateException("SMTP 密码或授权码未配置");
        }
        if (isBlank(config.getFromAddress())) {
            throw new IllegalStateException("发件人邮箱未配置");
        }
    }

    private RuntimeConfig toRuntimeConfig(SystemMailConfig config) {
        return new RuntimeConfig(
                config.getHost().trim(),
                config.getPort(),
                config.getUsername().trim(),
                config.getPassword().trim(),
                defaultIfBlank(config.getProtocol(), DEFAULT_PROTOCOL),
                defaultIfBlank(config.getDefaultEncoding(), DEFAULT_ENCODING),
                config.getFromAddress().trim(),
                defaultIfBlank(config.getFromName(), config.getFromAddress().trim()),
                toBoolean(config.getSmtpAuth()),
                toBoolean(config.getSslEnable()),
                toBoolean(config.getStarttlsEnable()),
                toBoolean(config.getStarttlsRequired())
        );
    }

    private MailConfigDto toDto(SystemMailConfig config) {
        String password = trimToNull(config.getPassword());
        return new MailConfigDto(
                nullToEmpty(config.getHost()),
                config.getPort() == null ? DEFAULT_PORT : config.getPort(),
                nullToEmpty(config.getUsername()),
                defaultIfBlank(config.getProtocol(), DEFAULT_PROTOCOL),
                defaultIfBlank(config.getDefaultEncoding(), DEFAULT_ENCODING),
                nullToEmpty(config.getFromAddress()),
                nullToEmpty(config.getFromName()),
                toBoolean(config.getSmtpAuth()),
                toBoolean(config.getSslEnable()),
                toBoolean(config.getStarttlsEnable()),
                toBoolean(config.getStarttlsRequired()),
                config.getEnabled() != null && config.getEnabled() == 1,
                password != null,
                maskSecret(password)
        );
    }

    private Integer validatePort(Integer value) {
        if (value == null) {
            return DEFAULT_PORT;
        }
        if (value <= 0 || value > 65535) {
            throw new IllegalArgumentException("SMTP Port 必须在 1 到 65535 之间");
        }
        return value;
    }

    private String validateEncoding(String value) {
        if (!Charset.isSupported(value)) {
            throw new IllegalArgumentException("邮箱默认编码不受支持");
        }
        return value;
    }

    private String maskSecret(String secret) {
        if (secret == null) {
            return null;
        }
        if (secret.length() <= 8) {
            return "********";
        }
        return secret.substring(0, 3) + "..." + secret.substring(secret.length() - 3);
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

    private Boolean valueOrDefault(Boolean value, Boolean defaultValue) {
        return value == null ? defaultValue : value;
    }

    private Integer toInt(boolean value) {
        return value ? 1 : 0;
    }

    private boolean toBoolean(Integer value) {
        return value != null && value == 1;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public record RuntimeConfig(
            String host,
            Integer port,
            String username,
            String password,
            String protocol,
            String defaultEncoding,
            String fromAddress,
            String fromName,
            Boolean smtpAuth,
            Boolean sslEnable,
            Boolean starttlsEnable,
            Boolean starttlsRequired
    ) {
    }
}
