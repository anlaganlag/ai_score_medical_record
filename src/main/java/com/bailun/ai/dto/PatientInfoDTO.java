package com.bailun.ai.dto;

import lombok.Data;

/**
 * 患者信息DTO
 */
@Data
public class PatientInfoDTO {
    
    private Long id;
    private String name;
    private String allergy;
    private String tumor;
    private String smoking;
    private String tipple;
    private String dialyzeAge;
    private String firstReceiveTime;
    private String stature;
    private String initWeight;
    private String eyesight;
    private String bloodType;
    private String rh;
    private String erythrocyte;
    private String state;
    private String signature;
} 