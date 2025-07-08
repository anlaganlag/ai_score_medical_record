package com.bailun.ai.service.impl;

import com.bailun.ai.dto.TreatmentInfoDTO;
import com.bailun.ai.service.PromptTemplateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 提示词模板服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromptTemplateServiceImpl implements PromptTemplateService {
    
    private final ObjectMapper objectMapper;
    
    @Value("${ai.prompt.template.system}")
    private String systemTemplate;
    
    @Value("${ai.prompt.template.user}")
    private String userTemplate;
    
    @Value("${ai.prompt.variables.scoring_dimensions}")
    private String scoringDimensions;
    
    @Value("${ai.prompt.variables.patient_info_fields}")
    private String patientInfoFields;
    
    @Value("${ai.prompt.variables.treatment_info_fields}")
    private String treatmentInfoFields;
    
    @Value("${ai.prompt.variables.output_format}")
    private String outputFormat;
    
    @Value("${ai.prompt.variables.scoring_criteria}")
    private String scoringCriteria;
    
    @Override
    public String buildSystemPrompt() {
        log.debug("构建系统提示词");
        
        String prompt = systemTemplate
            .replace("${scoring_dimensions}", scoringDimensions)
            .replace("${patient_info_fields}", patientInfoFields)
            .replace("${treatment_info_fields}", treatmentInfoFields)
            .replace("${output_format}", outputFormat)
            .replace("${scoring_criteria}", scoringCriteria);
            
        log.debug("系统提示词构建完成，长度: {}", prompt.length());
        return prompt;
    }
    
    @Override
    public String buildUserPrompt(Map<String, Object> patientData, TreatmentInfoDTO treatmentInfo) {
        log.debug("构建用户提示词，患者数据: {}, 诊疗信息: {}", 
                 patientData != null ? patientData.get("name") : "null",
                 treatmentInfo != null ? treatmentInfo.getMainSuit() : "null");
        
        try {
            String patientDataJson = patientData != null ? 
                objectMapper.writeValueAsString(patientData) : "{}";
            String treatmentDataJson = treatmentInfo != null ? 
                objectMapper.writeValueAsString(treatmentInfo) : "{}";
            
            String prompt = userTemplate
                .replace("${patient_data}", patientDataJson)
                .replace("${treatment_data}", treatmentDataJson);
                
            log.debug("用户提示词构建完成，长度: {}", prompt.length());
            return prompt;
            
        } catch (JsonProcessingException e) {
            log.error("序列化患者数据或诊疗信息失败", e);
            // 降级处理：使用简化的提示词
            return userTemplate
                .replace("${patient_data}", patientData != null ? patientData.toString() : "{}")
                .replace("${treatment_data}", treatmentInfo != null ? treatmentInfo.toString() : "{}");
        }
    }
    
    @Override
    public String buildFullPrompt(Map<String, Object> patientData, TreatmentInfoDTO treatmentInfo) {
        log.debug("构建完整提示词");
        
        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(patientData, treatmentInfo);
        
        String fullPrompt = systemPrompt + "\n\n" + userPrompt;
        
        log.debug("完整提示词构建完成，总长度: {}", fullPrompt.length());
        return fullPrompt;
    }
} 