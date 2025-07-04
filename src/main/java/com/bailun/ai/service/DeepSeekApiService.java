package com.bailun.ai.service;

import com.bailun.ai.dto.AiScoreResponse;

/**
 * DeepSeek API服务接口
 */
public interface DeepSeekApiService {
    
    /**
     * 评估病历记录
     */
    AiScoreResponse evaluateMedicalRecord(String medicalRecordJson);
} 