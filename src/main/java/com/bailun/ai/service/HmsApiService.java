package com.bailun.ai.service;

import com.bailun.ai.dto.PatientInfoDTO;
import com.bailun.ai.dto.TreatmentInfoDTO;

/**
 * HMS API服务接口
 */
public interface HmsApiService {
    
    /**
     * 获取患者信息
     */
    PatientInfoDTO getPatientInfo(Long patientId);
    
    /**
     * 获取诊疗信息
     */
    TreatmentInfoDTO getTreatmentInfo(Long patientId);
} 