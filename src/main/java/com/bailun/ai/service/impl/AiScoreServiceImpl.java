package com.bailun.ai.service.impl;

import com.bailun.ai.dto.AiScoreRequest;
import com.bailun.ai.dto.AiScoreResponse;
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
    private static final String AI_MODEL = "deepseek-ai/DeepSeek-R1-0528-Qwen3-8B";

    @Override
    @Transactional
    public AiScoreMedicalRecord generateAiScore(AiScoreRequest request) {
        log.info("开始生成AI评分，患者ID: {}", request.getPatientId());
        
        // 1. 获取患者基本信息
        PatientInfoDTO patientInfo = hmsApiService.getPatientInfo(request.getPatientId());
        if (patientInfo == null) {
            throw new IllegalArgumentException("患者信息不存在，患者ID: " + request.getPatientId());
        }
        
        // 2. 获取诊疗信息
        TreatmentInfoDTO treatmentInfo = hmsApiService.getTreatmentInfo(request.getPatientId());
        
        // 3. 整合数据
        Map<String, Object> medicalData = buildMedicalData(patientInfo, treatmentInfo);
        
        try {
            String medicalDataJson = objectMapper.writeValueAsString(medicalData);
            
            // 4. 调用AI评分
            AiScoreResponse aiResponse = deepSeekApiService.evaluateMedicalRecord(medicalDataJson);
            
            // 5. 保存评分结果
            AiScoreMedicalRecord record = buildScoreRecord(request.getPatientId(), patientInfo.getName(), aiResponse);
            AiScoreMedicalRecord savedRecord = repository.save(record);
            
            log.info("AI评分生成完成，患者ID: {}, 总分: {}", request.getPatientId(), aiResponse.getTotalScore());
            return savedRecord;
            
        } catch (JsonProcessingException e) {
            log.error("序列化病历数据失败", e);
            throw new RuntimeException("数据处理失败", e);
        }
    }

    @Override
    public AiScoreMedicalRecord getAiScoreReport(Long patientId) {
        log.info("获取AI评分报告，患者ID: {}", patientId);
        return repository.findByPatientIdAndDeletedFalse(patientId).orElse(null);
    }

    @Override
    @Transactional
    public boolean saveExpertComment(Long patientId, String expertComment) {
        log.info("保存专家点评，患者ID: {}", patientId);
        
        // 验证点评长度
        if (expertComment != null && expertComment.length() > MAX_COMMENT_LENGTH) {
            throw new IllegalArgumentException("专家点评不能超过" + MAX_COMMENT_LENGTH + "字");
        }
        
        return repository.findByPatientIdAndDeletedFalse(patientId)
                .map(record -> {
                    record.setExpertComment(expertComment);
                    repository.save(record);
                    log.info("专家点评保存成功，患者ID: {}", patientId);
                    return true;
                })
                .orElse(false);
    }
    
    /**
     * 构建病历数据
     */
    private Map<String, Object> buildMedicalData(PatientInfoDTO patientInfo, TreatmentInfoDTO treatmentInfo) {
        Map<String, Object> data = new HashMap<>();
        
        // 基本信息
        data.put("patientId", patientInfo.getId());
        data.put("patientName", patientInfo.getName());
        
        // 主诉 (10分)
        if (treatmentInfo != null) {
            data.put("mainSuit", treatmentInfo.getMainSuit());
        }
        
        // 病史 (20分)
        data.put("allergy", patientInfo.getAllergy());
        data.put("tumor", patientInfo.getTumor());
        data.put("smoking", patientInfo.getSmoking());
        data.put("tipple", patientInfo.getTipple());
        data.put("dialyzeAge", patientInfo.getDialyzeAge());
        data.put("firstReceiveTime", patientInfo.getFirstReceiveTime());
        if (treatmentInfo != null) {
            data.put("nowDiseaseHistory", treatmentInfo.getNowDiseaseHistory());
            data.put("oldDiseaseHistory", treatmentInfo.getOldDiseaseHistory());
        }
        
        // 体格检查 (15分)
        data.put("stature", patientInfo.getStature());
        data.put("initWeight", patientInfo.getInitWeight());
        data.put("eyesight", patientInfo.getEyesight());
        if (treatmentInfo != null) {
            data.put("weightPre", treatmentInfo.getWeightPre());
            data.put("pressurePre", treatmentInfo.getPressurePre());
            data.put("bodyInspect", treatmentInfo.getBodyInspect());
        }
        
        // 辅助检查 (5分)
        data.put("bloodType", patientInfo.getBloodType());
        data.put("rh", patientInfo.getRh());
        data.put("erythrocyte", patientInfo.getErythrocyte());
        if (treatmentInfo != null) {
            data.put("assayInspect", treatmentInfo.getAssayInspect());
        }
        
        // 诊断 (15分)
        if (treatmentInfo != null) {
            data.put("diagnosis", treatmentInfo.getDiagnosis());
            data.put("diseaseReasonNames", treatmentInfo.getDiseaseReasonNames());
            data.put("initDiagnosis", treatmentInfo.getInitDiagnosis());
        }
        
        // 处理 (20分)
        if (treatmentInfo != null) {
            data.put("dialysisPlan", treatmentInfo.getDialysisPlan());
            data.put("healingProject", treatmentInfo.getHealingProject());
            data.put("medic", treatmentInfo.getMedic());
            data.put("nurse", treatmentInfo.getNurse());
            data.put("facilityName", treatmentInfo.getFacilityName());
            data.put("startDialyzeTime", treatmentInfo.getStartDialyzeTime());
            data.put("endDialyzeTime", treatmentInfo.getEndDialyzeTime());
        }
        
        // 总体评价 (5分)
        data.put("state", patientInfo.getState());
        data.put("signature", patientInfo.getSignature());
        
        return data;
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
} 