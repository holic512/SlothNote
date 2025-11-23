package org.example.backend.common.util.file;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 本地文件存储配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "storage.local")
public class LocalStorageProperties {

    /**
     * 文件在服务器上的根目录
     * 例如：D:/flower-files 或 /data/flower-files
     */
    private String rootDir;

    /**
     * 对外访问的基础 URL 前缀
     * 例如：http://localhost:8080/files/
     * 或 Nginx 反向代理后的 https://cdn.xxx.com/files/
     */
    private String publicBaseUrl;

    /**
     * 是否按照日期分目录存储，如 /image/2025/11/15/xxx.jpg
     */
    private Boolean enableDatePath = true;

    /**
     * 生成文件名时是否保留原始扩展名
     */
    private Boolean keepExtension = true;
}

