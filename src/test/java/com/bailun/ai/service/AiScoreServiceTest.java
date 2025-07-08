package com.bailun.ai.service;

import com.bailun.ai.dto.AiScoreRequest;
import com.bailun.ai.dto.AiScoreResponse;
import com.bailun.ai.dto.AiScoreDebugResponse;
import com.bailun.ai.dto.PatientInfoDTO;
import com.bailun.ai.dto.TreatmentInfoDTO;
import com.bailun.ai.entity.AiScoreMedicalRecord;
import com.bailun.ai.repository.AiScoreMedicalRecordRepository;
import com.bailun.ai.service.impl.AiScoreServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AI评分服务测试类
 * 采用TDD方式开发，先编写测试用例
 */
@ExtendWith(MockitoExtension.class)
class AiScoreServiceTest {

    @Mock
    private AiScoreMedicalRecordRepository repository;

    @Mock
    private HmsApiService hmsApiService;

    @Mock
    private DeepSeekApiService deepSeekApiService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AiScoreServiceImpl aiScoreService;

    private AiScoreRequest testRequest;
    private PatientInfoDTO testPatientInfo;
    private TreatmentInfoDTO testTreatmentInfo;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        testRequest = new AiScoreRequest();
        testRequest.setPatientId(3835L);

        // 模拟患者信息
        testPatientInfo = new PatientInfoDTO();
        testPatientInfo.setId(3835L);
        testPatientInfo.setName("胡安秀");
        testPatientInfo.setAllergy(null); // Not clearly available in the new JSON
        testPatientInfo.setTumor(null);
        testPatientInfo.setSmoking(null);
        testPatientInfo.setTipple(null);
        testPatientInfo.setDialyzeAge(null); // "dialyzeAge": null in JSON
        testPatientInfo.setFirstReceiveTime(null);

        // 模拟诊疗信息
        testTreatmentInfo = new TreatmentInfoDTO();
        testTreatmentInfo.setPatientId(3835L);
        testTreatmentInfo.setMainSuit("维持性血液透析11年余。");
        testTreatmentInfo.setInitDiagnosis("慢性肾脏病5期,维持性血透状态,肾性贫血,肾性高血压,继发性甲状旁腺功能亢进,高磷酸血症,高尿酸血症,代谢性酸中毒,高钾血症");
        testTreatmentInfo.setHealingProject(null); // Not in new JSON
        testTreatmentInfo.setNowDiseaseHistory("患者于2013年6月因头晕、乏力、全身浮肿、恶心、呕吐、食欲减退在深圳市龙华区人民医院诊断为慢性肾脏病5期，肾性贫血，肾性骨病，开始规律性血液透析治疗，并行左前臂自体动静脉血管造瘘术，同年8月启用内瘘。目前透析方案为3次/周，每次超滤量为2500-3000ml，现干体重为57kg，为继续行规律血液透析收入我中心。患者平素未诉不适，双下肢无水肿，无乏力、胸闷、心悸、气促，无头晕、头痛，胃纳欠佳，精神、睡眠一般，无尿，大便正常。");
        testTreatmentInfo.setOldDiseaseHistory("乙型病毒性肝炎. 2013年左前臂自体动静脉血管造瘘术。");
    }

    @Test
    void testGenerateAiScore_Success() {
        // Given
        when(hmsApiService.getPatientInfo(3835L)).thenReturn(testPatientInfo);
        when(hmsApiService.getTreatmentInfo(3835L)).thenReturn(testTreatmentInfo);
        
        AiScoreResponse expectedAiResponse = new AiScoreResponse();
        expectedAiResponse.setTotalScore(70);
        expectedAiResponse.setLevel("乙级");

        AiScoreDebugResponse debugResponse = new AiScoreDebugResponse();
        debugResponse.setScoreResponse(expectedAiResponse);
        when(deepSeekApiService.evaluateMedicalRecordWithDebug(any())).thenReturn(debugResponse);

        AiScoreMedicalRecord savedRecord = new AiScoreMedicalRecord();
        savedRecord.setId(1L);
        savedRecord.setPatientId(3835L);
        savedRecord.setScoreTotal(70);
        when(repository.save(any(AiScoreMedicalRecord.class))).thenReturn(savedRecord);

        // When
        AiScoreMedicalRecord result = aiScoreService.generateAiScore(testRequest);

        // Then
        assertNotNull(result);
        assertEquals(3835L, result.getPatientId());
        assertEquals(70, result.getScoreTotal());
        verify(hmsApiService).getPatientInfo(3835L);
        verify(hmsApiService).getTreatmentInfo(3835L);
        verify(deepSeekApiService).evaluateMedicalRecordWithDebug(any());
        verify(repository).save(any(AiScoreMedicalRecord.class));
    }

    @Test
    void testGenerateAiScore_PatientNotFound() {
        // Given
        when(hmsApiService.getPatientInfo(3835L)).thenReturn(null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            aiScoreService.generateAiScore(testRequest);
        });
        
        verify(hmsApiService).getPatientInfo(3835L);
        verify(hmsApiService, never()).getTreatmentInfo(anyLong());
        verify(deepSeekApiService, never()).evaluateMedicalRecord(any());
        verify(repository, never()).save(any());
    }

    @Test
    void testGetAiScoreReport_Success() {
        // Given
        AiScoreMedicalRecord existingRecord = new AiScoreMedicalRecord();
        existingRecord.setId(1L);
        existingRecord.setPatientId(3835L);
        existingRecord.setPatientName("胡安秀");
        existingRecord.setScoreTotal(70);
        existingRecord.setScoreLevel("乙级");
        existingRecord.setExpertComment("专家点评内容");
        
        when(repository.findAllByPatientIdAndDeletedFalseOrderByCreatedTimeDesc(3835L)).thenReturn(List.of(existingRecord));

        // When
        AiScoreMedicalRecord result = aiScoreService.getAiScoreReport(3835L);

        // Then
        assertNotNull(result);
        assertEquals(3835L, result.getPatientId());
        assertEquals("胡安秀", result.getPatientName());
        assertEquals(70, result.getScoreTotal());
        assertEquals("乙级", result.getScoreLevel());
        assertEquals("专家点评内容", result.getExpertComment());
        verify(repository).findAllByPatientIdAndDeletedFalseOrderByCreatedTimeDesc(3835L);
    }

    @Test
    void testGetAiScoreReport_NotFound() {
        // Given
        when(repository.findAllByPatientIdAndDeletedFalseOrderByCreatedTimeDesc(3835L)).thenReturn(List.of());

        // When
        AiScoreMedicalRecord result = aiScoreService.getAiScoreReport(3835L);

        // Then
        assertNull(result);
        verify(repository).findAllByPatientIdAndDeletedFalseOrderByCreatedTimeDesc(3835L);
    }

    @Test
    void testSaveExpertComment_Success() {
        // Given
        AiScoreMedicalRecord existingRecord = new AiScoreMedicalRecord();
        existingRecord.setId(1L);
        existingRecord.setPatientId(3835L);
        existingRecord.setExpertComment("原有点评");
        
        when(repository.findAllByPatientIdAndDeletedFalseOrderByCreatedTimeDesc(3835L)).thenReturn(List.of(existingRecord));
        when(repository.save(any(AiScoreMedicalRecord.class))).thenReturn(existingRecord);

        // When
        boolean result = aiScoreService.saveExpertComment(3835L, "新的专家点评");

        // Then
        assertTrue(result);
        assertEquals("新的专家点评", existingRecord.getExpertComment());
        verify(repository).findAllByPatientIdAndDeletedFalseOrderByCreatedTimeDesc(3835L);
        verify(repository).save(existingRecord);
    }

    @Test
    void testSaveExpertComment_RecordNotFound() {
        // Given
        when(repository.findAllByPatientIdAndDeletedFalseOrderByCreatedTimeDesc(3835L)).thenReturn(List.of());

        // When
        boolean result = aiScoreService.saveExpertComment(3835L, "新的专家点评");

        // Then
        assertFalse(result);
        verify(repository).findAllByPatientIdAndDeletedFalseOrderByCreatedTimeDesc(3835L);
        verify(repository, never()).save(any());
    }

    @Test
    void testSaveExpertComment_ExceedsLimit() {
        // Given
        String longComment = "这是一个超过100字限制的专家点评内容".repeat(10); // 生成超长文本
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            aiScoreService.saveExpertComment(3835L, longComment);
        });
        
        verify(repository, never()).save(any());
    }

    @Test
    void testGenerateAiScore_WithIncompleteData() {
        // Given - 模拟数据不完整的情况
        PatientInfoDTO incompletePatientInfo = new PatientInfoDTO();
        incompletePatientInfo.setId(3835L);
        incompletePatientInfo.setName("胡安秀");
        // 其他字段为空

        TreatmentInfoDTO incompleteTreatmentInfo = new TreatmentInfoDTO();
        incompleteTreatmentInfo.setPatientId(3835L);
        // 其他字段为空

        when(hmsApiService.getPatientInfo(3835L)).thenReturn(incompletePatientInfo);
        when(hmsApiService.getTreatmentInfo(3835L)).thenReturn(incompleteTreatmentInfo);
        
        AiScoreResponse expectedAiResponse = new AiScoreResponse();
        expectedAiResponse.setTotalScore(30); // 数据不完整，分数较低
        expectedAiResponse.setLevel("丙级");

        AiScoreDebugResponse debugResponse = new AiScoreDebugResponse();
        debugResponse.setScoreResponse(expectedAiResponse);
        when(deepSeekApiService.evaluateMedicalRecordWithDebug(any())).thenReturn(debugResponse);

        AiScoreMedicalRecord savedRecord = new AiScoreMedicalRecord();
        savedRecord.setId(1L);
        savedRecord.setPatientId(3835L);
        savedRecord.setScoreTotal(30);
        when(repository.save(any(AiScoreMedicalRecord.class))).thenReturn(savedRecord);

        // When
        AiScoreMedicalRecord result = aiScoreService.generateAiScore(testRequest);

        // Then
        assertNotNull(result);
        assertEquals(3835L, result.getPatientId());
        assertEquals(30, result.getScoreTotal());
        verify(deepSeekApiService).evaluateMedicalRecordWithDebug(any());
    }

    @Test
    void testGenerateAiScore_DeepSeekApiFailure() {
        // Given
        when(hmsApiService.getPatientInfo(3835L)).thenReturn(testPatientInfo);
        when(hmsApiService.getTreatmentInfo(3835L)).thenReturn(testTreatmentInfo);
        when(deepSeekApiService.evaluateMedicalRecordWithDebug(any())).thenThrow(new RuntimeException("API调用失败"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            aiScoreService.generateAiScore(testRequest);
        });
        
        verify(hmsApiService).getPatientInfo(3835L);
        verify(hmsApiService).getTreatmentInfo(3835L);
        verify(deepSeekApiService).evaluateMedicalRecordWithDebug(any());
        verify(repository, never()).save(any());
    }
} 