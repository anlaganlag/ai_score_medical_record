package com.bailun.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 提示词模板配置属性类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai.prompt")
public class PromptProperties {
    
    /**
     * 模板定义
     */
    private Template template;
    
    /**
     * 模板变量
     */
    private Map<String, String> variables;
    
    @Data
    public static class Template {
        private String system;
        private String user;
    }
} 