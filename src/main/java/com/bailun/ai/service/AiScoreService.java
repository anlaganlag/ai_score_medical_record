package com.bailun.ai.service;

import com.bailun.ai.dto.AiScoreRequest;
import com.bailun.ai.entity.AiScoreMedicalRecord;

/**
 * AI评分服务接口
 */
public interface AiScoreService {
    
    /**
     * 生成AI评分
     */
    AiScoreMedicalRecord generateAiScore(AiScoreRequest request);
    
    /**
     * 获取AI评分报告
     */
    AiScoreMedicalRecord getAiScoreReport(Long patientId);
    
    /**
     * 保存专家点评
     */
    boolean saveExpertComment(Long patientId, String expertComment);
} 