package com.bailun.ai.service.impl;

import com.bailun.ai.config.PromptProperties;
import com.bailun.ai.dto.TreatmentInfoDTO;
import com.bailun.ai.service.PromptTemplateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final PromptProperties promptProperties;
    
    @Override
    public String buildSystemPrompt() {
        log.debug("构建系统提示词");
        
        String systemTemplate = promptProperties.getTemplate().getSystem();
        Map<String, String> variables = promptProperties.getVariables();
        
        String prompt = systemTemplate
            .replace("${scoring_dimensions}", variables.get("scoring_dimensions"))
            .replace("${patient_info_fields}", variables.get("patient_info_fields"))
            .replace("${treatment_info_fields}", variables.get("treatment_info_fields"))
            .replace("${output_format}", variables.get("output_format"))
            .replace("${scoring_criteria}", variables.get("scoring_criteria"));
            
        log.debug("系统提示词构建完成，长度: {}", prompt.length());
        return prompt;
    }
    
    @Override
    public String buildUserPrompt(Map<String, Object> patientData, TreatmentInfoDTO treatmentInfo) {
        // 记录调试日志，显示正在构建用户提示词，并输出患者姓名和主诉信息用于调试
        log.debug("构建用户提示词，患者数据: {}, 初步诊断: {}", 
                 patientData != null ? patientData.get("name") : "null",
                 treatmentInfo != null ? treatmentInfo.getInitDiagnosis() : "null");
        
        // 从配置属性中获取用户提示词模板
        String userTemplate = promptProperties.getTemplate().getUser();
        
        try {
            // 将患者数据对象序列化为JSON字符串，如果为null则使用空对象"{}"
            String patientDataJson = patientData != null ? 
                objectMapper.writeValueAsString(patientData) : "{}";
            // 将诊疗信息对象序列化为JSON字符串，如果为null则使用空对象"{}"
            String treatmentDataJson = treatmentInfo != null ? 
                objectMapper.writeValueAsString(treatmentInfo) : "{}";
            
            // 使用字符串替换将模板中的占位符替换为实际的JSON数据
            // ${patient_data} 替换为患者数据JSON
            // ${treatment_data} 替换为诊疗信息JSON
            String prompt = userTemplate
                .replace("${patient_data}", patientDataJson)
                .replace("${treatment_data}", treatmentDataJson);
                
            // 记录构建完成的日志，包含最终提示词的长度
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