package com.bailun.ai.controller;

import com.bailun.ai.dto.AiScoreRequest;
import com.bailun.ai.entity.AiScoreMedicalRecord;
import com.bailun.ai.service.AiScoreService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(AiScoreController.class)
class AiScoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AiScoreService aiScoreService;
    
    // 我们不需要mock repository，因为service层已经被mock了

    @Test
    void testGenerateScore_Success() throws Exception {
        // Given
        AiScoreRequest request = new AiScoreRequest();
        request.setPatientId(3835L);

        AiScoreMedicalRecord mockRecord = new AiScoreMedicalRecord();
        mockRecord.setId(1L);
        mockRecord.setPatientId(3835L);
        mockRecord.setPatientName("测试患者");
        mockRecord.setScoreTotal(85);
        mockRecord.setScoreLevel("乙级");

        when(aiScoreService.generateAiScore(any(AiScoreRequest.class))).thenReturn(mockRecord);
        
        ObjectMapper objectMapper = new ObjectMapper();
        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/admin-api/business/ai-score/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.patientId").value(3835));
    }
} 