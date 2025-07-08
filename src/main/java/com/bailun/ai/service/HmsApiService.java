package com.bailun.ai.service;

import com.bailun.ai.dto.PatientInfoDTO;
import com.bailun.ai.dto.TreatmentInfoDTO;

import java.util.Map;

/**
 * HMS API服务接口
 */
public interface HmsApiService {
    
    /**
     * 获取患者信息
     */
    PatientInfoDTO getPatientInfo(Long patientId);
    
    /**
     * 获取患者信息原始数据
     */
    Map<String, Object> getPatientInfoRaw(Long patientId);
    
    /**
     * 获取诊疗信息
     */
    TreatmentInfoDTO getTreatmentInfo(Long patientId);
} 