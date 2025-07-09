package com.bailun.ai.service.impl;

import com.bailun.ai.dto.AiScoreResponse;
import com.bailun.ai.dto.AiScoreDebugResponse;
import com.bailun.ai.dto.TreatmentInfoDTO;
import com.bailun.ai.service.DeepSeekApiService;
import com.bailun.ai.service.PromptTemplateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Mock DeepSeek AI API服务实现类 - 用于开发和测试
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.deepseek.mock.enabled", havingValue = "true")
public class MockDeepSeekApiServiceImpl implements DeepSeekApiService {
    
    private final ObjectMapper objectMapper;
    private final PromptTemplateService promptTemplateService;
    private final Random random = new Random();

    @Override
    public AiScoreResponse evaluateMedicalRecord(String medicalRecordJson) {
        log.info("Mock AI评估病历记录");
        return generateMockResponse();
    }

    @Override
    public AiScoreDebugResponse evaluateMedicalRecordWithDebug(String medicalRecordJson) {
        log.info("Mock AI评估病历记录（包含调试信息）");
        
        AiScoreResponse response = generateMockResponse();
        AiScoreDebugResponse debugResponse = new AiScoreDebugResponse();
        debugResponse.setScoreResponse(response);
        debugResponse.setAiPrompt("Mock System Prompt\n\nMock User Prompt");
        
        try {
            debugResponse.setAiRequestJson(objectMapper.writeValueAsString(Map.of(
                "model", "Qwen/Qwen3-8B",
                "messages", List.of(
                    Map.of("role", "system", "content", "Mock system prompt"),
                    Map.of("role", "user", "content", "Mock user prompt")
                )
            )));
            debugResponse.setAiResponseJson(objectMapper.writeValueAsString(Map.of(
                "choices", List.of(Map.of("message", Map.of("content", "Mock AI response")))
            )));
        } catch (JsonProcessingException e) {
            log.error("Mock响应序列化失败", e);
        }
        
        return debugResponse;
    }

    @Override
    public AiScoreResponse evaluateMedicalRecord(Map<String, Object> patientData, TreatmentInfoDTO treatmentInfo) {
        log.info("Mock AI评估病历记录（结构化数据）");
        
        // 模拟处理时间
        try {
            Thread.sleep(500 + random.nextInt(1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return generateMockResponse();
    }

    @Override
    public AiScoreDebugResponse evaluateMedicalRecordWithDebug(Map<String, Object> patientData, TreatmentInfoDTO treatmentInfo) {
        log.info("Mock AI评估病历记录（结构化数据，包含调试信息）");
        
        // 模拟处理时间
        try {
            Thread.sleep(500 + random.nextInt(1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        AiScoreResponse response = generateMockResponse();
        AiScoreDebugResponse debugResponse = new AiScoreDebugResponse();
        debugResponse.setScoreResponse(response);
        
        // 使用真实的提示词模板
        String systemPrompt = promptTemplateService.buildSystemPrompt();
        String userPrompt = promptTemplateService.buildUserPrompt(patientData, treatmentInfo);
        debugResponse.setAiPrompt(systemPrompt + "\n\n" + userPrompt);
        
        try {
            debugResponse.setAiRequestJson(objectMapper.writeValueAsString(Map.of(
                "model", "Qwen/Qwen3-8B",
                "messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.1,
                "max_tokens", 10000
            )));
            
            debugResponse.setAiResponseJson(objectMapper.writeValueAsString(Map.of(
                "id", "mock-response-" + System.currentTimeMillis(),
                "object", "chat.completion",
                "created", System.currentTimeMillis() / 1000,
                "model", "Qwen/Qwen3-8B",
                "choices", List.of(Map.of(
                    "index", 0,
                    "message", Map.of(
                        "role", "assistant",
                        "content", generateMockJsonResponse(response)
                    ),
                    "finish_reason", "stop"
                )),
                "usage", Map.of(
                    "prompt_tokens", 1000,
                    "completion_tokens", 500,
                    "total_tokens", 1500
                )
            )));
        } catch (JsonProcessingException e) {
            log.error("Mock响应序列化失败", e);
        }
        
        return debugResponse;
    }
    
    /**
     * 生成模拟评分响应
     */
    private AiScoreResponse generateMockResponse() {
        AiScoreResponse response = new AiScoreResponse();
        
        // 随机生成总分 75-95
        int totalScore = 75 + random.nextInt(21);
        response.setTotalScore(totalScore);
        
        // 根据总分确定等级
        if (totalScore >= 90) {
            response.setLevel("甲级");
        } else if (totalScore >= 75) {
            response.setLevel("乙级");
        } else {
            response.setLevel("丙级");
        }
        
        // 生成详细评分
        List<AiScoreResponse.ScoreDetail> details = new ArrayList<>();
        
        String[] items = {"主诉", "病史", "体格检查", "辅助检查", "诊断", "处理", "总体评价", "其他"};
        int[] fullScores = {10, 20, 15, 5, 15, 20, 5, 10};
        String[] deductions = {
            "记录完整，无扣分",
            "现病史记录较详细，但既往史略显简单，扣2分",
            "体格检查记录基本完整，扣1分",
            "检查结果记录完整，无扣分",
            "诊断基本准确，扣1分",
            "治疗方案合理，但用药说明不够详细，扣3分",
            "整体质量良好，扣1分",
            "签名和格式规范，无扣分"
        };
        
        int remainingScore = totalScore;
        for (int i = 0; i < items.length; i++) {
            AiScoreResponse.ScoreDetail detail = new AiScoreResponse.ScoreDetail();
            detail.setItem(items[i]);
            detail.setFullScore(fullScores[i]);
            
            // 为最后一项分配剩余分数，否则随机扣分
            if (i == items.length - 1) {
                detail.setScore(Math.min(remainingScore, fullScores[i]));
            } else {
                int deduction = random.nextInt(Math.min(fullScores[i] / 2 + 1, remainingScore - (items.length - i - 1) * 3));
                detail.setScore(fullScores[i] - deduction);
                remainingScore -= detail.getScore();
            }
            
            if (detail.getScore() == detail.getFullScore()) {
                detail.setDeduction("记录完整，无扣分");
            } else {
                detail.setDeduction(deductions[i]);
            }
            
            details.add(detail);
        }
        
        response.setDetails(details);
        
        log.info("Mock AI评分生成完成，总分: {}, 等级: {}", totalScore, response.getLevel());
        return response;
    }
    
    /**
     * 生成模拟的JSON响应内容
     */
    private String generateMockJsonResponse(AiScoreResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            return "{\"totalScore\": " + response.getTotalScore() + ", \"level\": \"" + response.getLevel() + "\"}";
        }
    }
} 