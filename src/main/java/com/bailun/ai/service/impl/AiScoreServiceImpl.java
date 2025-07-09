package com.bailun.ai.service.impl;

import com.bailun.ai.dto.AiScoreRequest;
import com.bailun.ai.dto.AiScoreResponse;
import com.bailun.ai.dto.AiScoreDebugResponse;
import com.bailun.ai.dto.PatientInfoDTO;
import com.bailun.ai.dto.TreatmentInfoDTO;
import com.bailun.ai.entity.AiScoreMedicalRecord;
import com.bailun.ai.repository.AiScoreMedicalRecordRepository;
import com.bailun.ai.service.AiScoreService;
import com.bailun.ai.service.DeepSeekApiService;
import com.bailun.ai.service.HmsApiService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI评分服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiScoreServiceImpl implements AiScoreService {
    
    private final AiScoreMedicalRecordRepository repository;
    private final HmsApiService hmsApiService;
    private final DeepSeekApiService deepSeekApiService;
    private final ObjectMapper objectMapper;
    
    private static final int MAX_COMMENT_LENGTH = 100;
    private static final String AI_MODEL = "Qwen/Qwen3-8B";

    @Override
    @Transactional
    public AiScoreMedicalRecord generateAiScore(AiScoreRequest request) {
        log.info("开始生成AI评分，患者ID: {}", request.getPatientId());
        
        // 1. 获取患者基本信息
        PatientInfoDTO patientInfo = hmsApiService.getPatientInfo(request.getPatientId());
        if (patientInfo == null) {
            throw new IllegalArgumentException("患者信息不存在，患者ID: " + request.getPatientId());
        }
        
        // 添加调试日志
        log.info("获取到的患者信息 - ID: {}, 姓名: '{}'", patientInfo.getId(), patientInfo.getName());
        
        // 2. 获取诊疗信息
        TreatmentInfoDTO treatmentInfo = hmsApiService.getTreatmentInfo(request.getPatientId());
        
        // 3. 获取完整的患者数据
        Map<String, Object> patientData = hmsApiService.getPatientInfoRaw(request.getPatientId());
        
        try {
            // 4. 详细日志记录数据状态
            log.info("患者数据获取完成，patientData size: {}, treatmentInfo: {}", 
                patientData != null ? patientData.size() : 0, 
                treatmentInfo != null ? "不为空" : "为空");
            
            if (patientData != null) {
                log.debug("患者数据内容: {}", objectMapper.writeValueAsString(patientData));
            }
            
            if (treatmentInfo != null) {
                log.debug("诊疗信息内容: {}", objectMapper.writeValueAsString(treatmentInfo));
            }
            
            // 4. 调用AI评分（使用结构化数据，包含调试信息）
            log.info("开始调用AI评分服务");
            AiScoreDebugResponse debugResponse = deepSeekApiService.evaluateMedicalRecordWithDebug(patientData, treatmentInfo);
            AiScoreResponse aiResponse = debugResponse.getScoreResponse();
            log.info("AI评分调用成功，总分: {}", aiResponse.getTotalScore());
            
            // 5. 保存评分结果（包含调试信息）
            AiScoreMedicalRecord record = buildScoreRecordWithDebug(
                request.getPatientId(), 
                patientInfo.getName(), 
                aiResponse, 
                debugResponse,
                patientData,
                treatmentInfo
            );
            AiScoreMedicalRecord savedRecord = repository.save(record);
            
            log.info("AI评分生成完成，患者ID: {}, 总分: {}", request.getPatientId(), aiResponse.getTotalScore());
            return savedRecord;
            
        } catch (JsonProcessingException e) {
            log.error("序列化数据失败，患者ID: {}", request.getPatientId(), e);
            throw new RuntimeException("数据处理失败", e);
        } catch (Exception e) {
            log.error("AI评分生成失败，患者ID: {}", request.getPatientId(), e);
            throw new RuntimeException("评分生成失败：" + e.getMessage(), e);
        }
    }

    @Override
    public AiScoreMedicalRecord getAiScoreReport(Long patientId) {
        log.info("获取AI评分报告，患者ID: {}", patientId);
        // 获取所有记录，然后取最新的一条，避免NonUniqueResultException
        List<AiScoreMedicalRecord> records = repository.findAllByPatientIdAndDeletedFalseOrderByCreatedTimeDesc(patientId);
        return records.isEmpty() ? null : records.get(0);
    }

    @Override
    @Transactional
    public boolean saveExpertComment(Long patientId, String expertComment) {
        log.info("保存专家点评，患者ID: {}", patientId);
        
        // 验证点评长度
        if (expertComment != null && expertComment.length() > MAX_COMMENT_LENGTH) {
            throw new IllegalArgumentException("专家点评不能超过" + MAX_COMMENT_LENGTH + "字");
        }
        
        // 获取所有记录，然后取最新的一条，避免NonUniqueResultException
        List<AiScoreMedicalRecord> records = repository.findAllByPatientIdAndDeletedFalseOrderByCreatedTimeDesc(patientId);
        if (records.isEmpty()) {
            return false;
        }
        
        AiScoreMedicalRecord record = records.get(0);
        record.setExpertComment(expertComment);
        repository.save(record);
        log.info("专家点评保存成功，患者ID: {}", patientId);
        return true;
    }
    
    /**
     * 构建评分记录
     */
    private AiScoreMedicalRecord buildScoreRecord(Long patientId, String patientName, AiScoreResponse aiResponse) throws JsonProcessingException {
        AiScoreMedicalRecord record = new AiScoreMedicalRecord();
        record.setPatientId(patientId);
        record.setPatientName(patientName);
        record.setScoreTotal(aiResponse.getTotalScore());
        record.setScoreLevel(aiResponse.getLevel());
        record.setScoreResult(objectMapper.writeValueAsString(aiResponse));
        record.setAiModel(AI_MODEL);
        return record;
    }
    
    /**
     * 构建评分记录（包含调试信息）
     */
    private AiScoreMedicalRecord buildScoreRecordWithDebug(
            Long patientId, 
            String patientName, 
            AiScoreResponse aiResponse, 
            AiScoreDebugResponse debugResponse,
            Map<String, Object> patientData,
            TreatmentInfoDTO treatmentInfo) throws JsonProcessingException {
        
        AiScoreMedicalRecord record = new AiScoreMedicalRecord();
        record.setPatientId(patientId);
        record.setPatientName(patientName);
        record.setScoreTotal(aiResponse.getTotalScore());
        record.setScoreLevel(aiResponse.getLevel());
        record.setScoreResult(objectMapper.writeValueAsString(aiResponse));
        record.setAiModel(AI_MODEL);
        
        // 保存调试信息
        record.setAiPrompt(debugResponse.getAiPrompt());
        record.setAiRequestJson(debugResponse.getAiRequestJson());
        record.setAiResponseJson(debugResponse.getAiResponseJson());
        
        // 保存患者基本信息和诊疗信息 - 使用原始的patientId而不是patientInfo.getId()
        if (patientData != null) {
            record.setPatientBasicInfo(objectMapper.writeValueAsString(patientData));
        }
        
        if (treatmentInfo != null) {
            record.setTreatmentInfo(objectMapper.writeValueAsString(treatmentInfo));
        }
        
        return record;
    }
} 