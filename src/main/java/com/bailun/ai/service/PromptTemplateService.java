package com.bailun.ai.service;

import com.bailun.ai.dto.TreatmentInfoDTO;

import java.util.Map;

/**
 * 提示词模板服务接口
 */
public interface PromptTemplateService {
    
    /**
     * 构建系统提示词
     * @return 完整的系统提示词
     */
    String buildSystemPrompt();
    
    /**
     * 构建用户提示词
     * @param patientData 患者数据
     * @param treatmentInfo 诊疗信息
     * @return 用户提示词
     */
    String buildUserPrompt(Map<String, Object> patientData, TreatmentInfoDTO treatmentInfo);
    
    /**
     * 构建完整的提示词（系统提示词 + 用户提示词）
     * @param patientData 患者数据
     * @param treatmentInfo 诊疗信息
     * @return 完整提示词
     */
    String buildFullPrompt(Map<String, Object> patientData, TreatmentInfoDTO treatmentInfo);
} 