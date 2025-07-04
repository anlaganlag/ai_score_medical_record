package com.bailun.ai.dto;

import lombok.Data;

/**
 * 诊疗信息DTO
 */
@Data
public class TreatmentInfoDTO {
    
    private Long patientId;
    private String mainSuit;
    private String initDiagnosis;
    private String healingProject;
    private String nowDiseaseHistory;
    private String oldDiseaseHistory;
    private String weightPre;
    private String pressurePre;
    private String bodyInspect;
    private String assayInspect;
    private String diagnosis;
    private String diseaseReasonNames;
    private String dialysisPlan;
    private String medic;
    private String nurse;
    private String facilityName;
    private String startDialyzeTime;
    private String endDialyzeTime;
} 