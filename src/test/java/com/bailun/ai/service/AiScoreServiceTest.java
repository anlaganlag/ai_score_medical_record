package com.bailun.ai.service;

import com.bailun.ai.dto.AiScoreRequest;
import com.bailun.ai.dto.AiScoreResponse;
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
        testRequest.setPatientId(225L);

        // 模拟患者信息
        testPatientInfo = new PatientInfoDTO();
        testPatientInfo.setId(225L);
        testPatientInfo.setName("叶金铃");
        testPatientInfo.setAllergy("诉地塞米松及阿莫西林过敏");
        testPatientInfo.setTumor("无");
        testPatientInfo.setSmoking("无");
        testPatientInfo.setTipple("无");
        testPatientInfo.setDialyzeAge("7年零10月");
        testPatientInfo.setFirstReceiveTime("2017-08-18");

        // 模拟诊疗信息
        testTreatmentInfo = new TreatmentInfoDTO();
        testTreatmentInfo.setPatientId(225L);
        testTreatmentInfo.setMainSuit("维持血透5年余。");
        testTreatmentInfo.setInitDiagnosis("维持性血透状态,慢性肾小球肾炎,慢性肾功能不全（CKD-5期）");
        testTreatmentInfo.setHealingProject("低盐低钾低脂、高蛋白饮食，纠正贫血，调整血压改善肾功能对症治疗");
    }

    @Test
    void testGenerateAiScore_Success() {
        // Given
        when(hmsApiService.getPatientInfo(225L)).thenReturn(testPatientInfo);
        when(hmsApiService.getTreatmentInfo(225L)).thenReturn(testTreatmentInfo);
        
        AiScoreResponse expectedAiResponse = new AiScoreResponse();
        expectedAiResponse.setTotalScore(70);
        expectedAiResponse.setLevel("乙级");
        when(deepSeekApiService.evaluateMedicalRecord(any())).thenReturn(expectedAiResponse);

        AiScoreMedicalRecord savedRecord = new AiScoreMedicalRecord();
        savedRecord.setId(1L);
        savedRecord.setPatientId(225L);
        savedRecord.setScoreTotal(70);
        when(repository.save(any(AiScoreMedicalRecord.class))).thenReturn(savedRecord);

        // When
        AiScoreMedicalRecord result = aiScoreService.generateAiScore(testRequest);

        // Then
        assertNotNull(result);
        assertEquals(225L, result.getPatientId());
        assertEquals(70, result.getScoreTotal());
        verify(hmsApiService).getPatientInfo(225L);
        verify(hmsApiService).getTreatmentInfo(225L);
        verify(deepSeekApiService).evaluateMedicalRecord(any());
        verify(repository).save(any(AiScoreMedicalRecord.class));
    }

    @Test
    void testGenerateAiScore_PatientNotFound() {
        // Given
        when(hmsApiService.getPatientInfo(225L)).thenReturn(null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            aiScoreService.generateAiScore(testRequest);
        });
        
        verify(hmsApiService).getPatientInfo(225L);
        verify(hmsApiService, never()).getTreatmentInfo(anyLong());
        verify(deepSeekApiService, never()).evaluateMedicalRecord(any());
        verify(repository, never()).save(any());
    }

    @Test
    void testGetAiScoreReport_Success() {
        // Given
        AiScoreMedicalRecord existingRecord = new AiScoreMedicalRecord();
        existingRecord.setId(1L);
        existingRecord.setPatientId(225L);
        existingRecord.setPatientName("叶金铃");
        existingRecord.setScoreTotal(70);
        existingRecord.setScoreLevel("乙级");
        existingRecord.setExpertComment("专家点评内容");
        
        when(repository.findByPatientIdAndDeletedFalse(225L)).thenReturn(Optional.of(existingRecord));

        // When
        AiScoreMedicalRecord result = aiScoreService.getAiScoreReport(225L);

        // Then
        assertNotNull(result);
        assertEquals(225L, result.getPatientId());
        assertEquals("叶金铃", result.getPatientName());
        assertEquals(70, result.getScoreTotal());
        assertEquals("乙级", result.getScoreLevel());
        assertEquals("专家点评内容", result.getExpertComment());
        verify(repository).findByPatientIdAndDeletedFalse(225L);
    }

    @Test
    void testGetAiScoreReport_NotFound() {
        // Given
        when(repository.findByPatientIdAndDeletedFalse(225L)).thenReturn(Optional.empty());

        // When
        AiScoreMedicalRecord result = aiScoreService.getAiScoreReport(225L);

        // Then
        assertNull(result);
        verify(repository).findByPatientIdAndDeletedFalse(225L);
    }

    @Test
    void testSaveExpertComment_Success() {
        // Given
        AiScoreMedicalRecord existingRecord = new AiScoreMedicalRecord();
        existingRecord.setId(1L);
        existingRecord.setPatientId(225L);
        existingRecord.setExpertComment("原有点评");
        
        when(repository.findByPatientIdAndDeletedFalse(225L)).thenReturn(Optional.of(existingRecord));
        when(repository.save(any(AiScoreMedicalRecord.class))).thenReturn(existingRecord);

        // When
        boolean result = aiScoreService.saveExpertComment(225L, "新的专家点评");

        // Then
        assertTrue(result);
        assertEquals("新的专家点评", existingRecord.getExpertComment());
        verify(repository).findByPatientIdAndDeletedFalse(225L);
        verify(repository).save(existingRecord);
    }

    @Test
    void testSaveExpertComment_RecordNotFound() {
        // Given
        when(repository.findByPatientIdAndDeletedFalse(225L)).thenReturn(Optional.empty());

        // When
        boolean result = aiScoreService.saveExpertComment(225L, "新的专家点评");

        // Then
        assertFalse(result);
        verify(repository).findByPatientIdAndDeletedFalse(225L);
        verify(repository, never()).save(any());
    }

    @Test
    void testSaveExpertComment_ExceedsLimit() {
        // Given
        String longComment = "这是一个超过100字限制的专家点评内容".repeat(10); // 生成超长文本
        
        AiScoreMedicalRecord existingRecord = new AiScoreMedicalRecord();
        existingRecord.setId(1L);
        existingRecord.setPatientId(225L);
        
        when(repository.findByPatientIdAndDeletedFalse(225L)).thenReturn(Optional.of(existingRecord));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            aiScoreService.saveExpertComment(225L, longComment);
        });
        
        verify(repository).findByPatientIdAndDeletedFalse(225L);
        verify(repository, never()).save(any());
    }

    @Test
    void testGenerateAiScore_WithIncompleteData() {
        // Given - 模拟数据不完整的情况
        PatientInfoDTO incompletePatientInfo = new PatientInfoDTO();
        incompletePatientInfo.setId(225L);
        incompletePatientInfo.setName("叶金铃");
        // 其他字段为空

        TreatmentInfoDTO incompleteTreatmentInfo = new TreatmentInfoDTO();
        incompleteTreatmentInfo.setPatientId(225L);
        // 其他字段为空

        when(hmsApiService.getPatientInfo(225L)).thenReturn(incompletePatientInfo);
        when(hmsApiService.getTreatmentInfo(225L)).thenReturn(incompleteTreatmentInfo);
        
        AiScoreResponse expectedAiResponse = new AiScoreResponse();
        expectedAiResponse.setTotalScore(30); // 数据不完整，分数较低
        expectedAiResponse.setLevel("丙级");
        when(deepSeekApiService.evaluateMedicalRecord(any())).thenReturn(expectedAiResponse);

        AiScoreMedicalRecord savedRecord = new AiScoreMedicalRecord();
        savedRecord.setId(1L);
        savedRecord.setPatientId(225L);
        savedRecord.setScoreTotal(30);
        when(repository.save(any(AiScoreMedicalRecord.class))).thenReturn(savedRecord);

        // When
        AiScoreMedicalRecord result = aiScoreService.generateAiScore(testRequest);

        // Then
        assertNotNull(result);
        assertEquals(225L, result.getPatientId());
        assertEquals(30, result.getScoreTotal());
        verify(deepSeekApiService).evaluateMedicalRecord(any());
    }

    @Test
    void testGenerateAiScore_DeepSeekApiFailure() {
        // Given
        when(hmsApiService.getPatientInfo(225L)).thenReturn(testPatientInfo);
        when(hmsApiService.getTreatmentInfo(225L)).thenReturn(testTreatmentInfo);
        when(deepSeekApiService.evaluateMedicalRecord(any())).thenThrow(new RuntimeException("API调用失败"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            aiScoreService.generateAiScore(testRequest);
        });
        
        verify(hmsApiService).getPatientInfo(225L);
        verify(hmsApiService).getTreatmentInfo(225L);
        verify(deepSeekApiService).evaluateMedicalRecord(any());
        verify(repository, never()).save(any());
    }
} 