package org.example.backend.common.config;

import org.example.backend.common.util.file.LocalStorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LocalStorageProperties props;

    @Autowired
    public WebConfig(LocalStorageProperties props) {
        this.props = props;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String root = props.getRootDir();
        if (root == null) return;
        String location = normalize(root);
        registry.addResourceHandler("/files/**")
                .addResourceLocations(location);
    }

    private String normalize(String p) {
        String path = p.replace('\\', '/');
        if (!path.endsWith("/")) path = path + "/";
        if (!path.startsWith("file:")) path = "file:" + path;
        return path;
    }
}