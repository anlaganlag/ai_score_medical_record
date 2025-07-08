package com.bailun.ai.dto;

import lombok.Data;

/**
 * AI评分调试响应类，包含提示词和API交互信息
 */
@Data
public class AiScoreDebugResponse {
    
    /**
     * AI评分结果
     */
    private AiScoreResponse scoreResponse;
    
    /**
     * AI评分使用的提示词
     */
    private String aiPrompt;
    
    /**
     * AI API请求JSON数据
     */
    private String aiRequestJson;
    
    /**
     * AI API响应JSON数据
     */
    private String aiResponseJson;
} 