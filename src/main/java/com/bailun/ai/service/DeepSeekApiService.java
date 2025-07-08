package com.bailun.ai.service;

import com.bailun.ai.dto.AiScoreResponse;
import com.bailun.ai.dto.AiScoreDebugResponse;
import com.bailun.ai.dto.TreatmentInfoDTO;

import java.util.Map;

/**
 * DeepSeek API服务接口
 */
public interface DeepSeekApiService {
    
    /**
     * 评估病历记录
     */
    AiScoreResponse evaluateMedicalRecord(String medicalRecordJson);
    
    /**
     * 评估病历记录（包含调试信息）
     */
    AiScoreDebugResponse evaluateMedicalRecordWithDebug(String medicalRecordJson);
    
    /**
     * 评估病历记录 - 使用结构化数据
     * @param patientData 患者数据
     * @param treatmentInfo 诊疗信息
     */
    AiScoreResponse evaluateMedicalRecord(Map<String, Object> patientData, TreatmentInfoDTO treatmentInfo);
    
    /**
     * 评估病历记录（包含调试信息）- 使用结构化数据
     * @param patientData 患者数据
     * @param treatmentInfo 诊疗信息
     */
    AiScoreDebugResponse evaluateMedicalRecordWithDebug(Map<String, Object> patientData, TreatmentInfoDTO treatmentInfo);
} 