package com.bailun.ai.service.impl;

import com.bailun.ai.dto.AiScoreResponse;
import com.bailun.ai.service.DeepSeekApiService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek AI API服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeepSeekApiServiceImpl implements DeepSeekApiService {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${ai.deepseek.api-url}")
    private String deepSeekApiUrl;

    @Value("${ai.deepseek.api-key}")
    private String deepSeekApiKey;

    @Value("${ai.deepseek.model}")
    private String deepSeekModel;

    @Value("${ai.deepseek.temperature:0.1}")
    private Double temperature;

    @Value("${ai.deepseek.max-tokens:4000}")
    private Integer maxTokens;

    @Value("${ai.deepseek.timeout:30000}")
    private Integer timeoutMs;

    @Value("${ai.deepseek.retry-count:1}")
    private Integer retryCount;

    @Override
    public AiScoreResponse evaluateMedicalRecord(String medicalRecordJson) {
        log.info("调用DeepSeek AI评估病历记录");
        
        for (int attempt = 1; attempt <= retryCount + 1; attempt++) {
            try {
                return callDeepSeekApi(medicalRecordJson);
            } catch (Exception e) {
                log.error("DeepSeek AI调用失败，第{}次尝试", attempt, e);
                if (attempt > retryCount) {
                    throw new RuntimeException("DeepSeek AI调用失败，已重试" + retryCount + "次", e);
                }
                // 等待一秒后重试
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试被中断", ie);
                }
            }
        }
        
        throw new RuntimeException("DeepSeek AI调用失败");
    }

    /**
     * 调用DeepSeek AI API
     */
    private AiScoreResponse callDeepSeekApi(String medicalRecordJson) {
        WebClient webClient = webClientBuilder
                .baseUrl(deepSeekApiUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + deepSeekApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        Map<String, Object> requestBody = buildRequestBody(medicalRecordJson);
        
        Map<String, Object> response = webClient.post()
                .uri("")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofMillis(timeoutMs))
                .block();

        return parseAiResponse(response);
    }

    /**
     * 构建请求体
     */
    private Map<String, Object> buildRequestBody(String medicalRecordJson) {
        List<Map<String, String>> messages = new ArrayList<>();
        
        // 系统提示词
        messages.add(Map.of(
                "role", "system",
                "content", buildSystemPrompt()
        ));
        
        // 用户输入
        messages.add(Map.of(
                "role", "user",
                "content", "请对以下病历进行评分：\n" + medicalRecordJson
        ));

        return Map.of(
                "model", deepSeekModel,
                "messages", messages,
                "temperature", temperature,
                "max_tokens", maxTokens
        );
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt() {
        return """
                你是一个专业的医疗质控专家，需要根据病历信息进行评分。请按照以下8个维度进行评分：
                
                1. 主诉 (10分)：评估主诉的完整性和规范性
                2. 病史 (20分)：包括现病史、既往史、过敏史等
                3. 体格检查 (15分)：评估体格检查记录的完整性
                4. 辅助检查 (5分)：评估检验检查结果的完整性
                5. 诊断 (15分)：评估诊断的准确性和完整性
                6. 处理 (20分)：评估治疗方案的合理性
                7. 总体评价 (5分)：评估病历的整体质量
                8. 其他 (10分)：评估其他方面如签名、规范性等
                
                请严格按照以下JSON格式返回评分结果，不要包含任何其他文字：
                {
                  "totalScore": 总分数字,
                  "level": "甲级/乙级/丙级",
                  "details": [
                    {
                      "item": "主诉",
                      "fullScore": 10,
                      "score": 实际得分,
                      "deduction": "扣分说明"
                    },
                    {
                      "item": "病史",
                      "fullScore": 20,
                      "score": 实际得分,
                      "deduction": "扣分说明"
                    },
                    {
                      "item": "体格检查",
                      "fullScore": 15,
                      "score": 实际得分,
                      "deduction": "扣分说明"
                    },
                    {
                      "item": "辅助检查",
                      "fullScore": 5,
                      "score": 实际得分,
                      "deduction": "扣分说明"
                    },
                    {
                      "item": "诊断",
                      "fullScore": 15,
                      "score": 实际得分,
                      "deduction": "扣分说明"
                    },
                    {
                      "item": "处理",
                      "fullScore": 20,
                      "score": 实际得分,
                      "deduction": "扣分说明"
                    },
                    {
                      "item": "总体评价",
                      "fullScore": 5,
                      "score": 实际得分,
                      "deduction": "扣分说明"
                    },
                    {
                      "item": "其他",
                      "fullScore": 10,
                      "score": 实际得分,
                      "deduction": "扣分说明"
                    }
                  ]
                }
                
                评分标准：
                - 甲级：90-100分
                - 乙级：75-89分
                - 丙级：60-74分
                - 不合格：60分以下
                """;
    }

    /**
     * 解析AI响应
     */
    private AiScoreResponse parseAiResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("AI响应格式错误：没有choices");
            }

            Map<String, Object> choice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) choice.get("message");
            String content = (String) message.get("content");

            log.debug("AI响应内容: {}", content);

            // 提取JSON部分
            String jsonContent = extractJsonFromContent(content);
            
            // 解析JSON
            JsonNode jsonNode = objectMapper.readTree(jsonContent);
            
            AiScoreResponse aiResponse = new AiScoreResponse();
            aiResponse.setTotalScore(jsonNode.get("totalScore").asInt());
            aiResponse.setLevel(jsonNode.get("level").asText());
            
            // 解析详细评分
            List<AiScoreResponse.ScoreDetail> details = new ArrayList<>();
            JsonNode detailsNode = jsonNode.get("details");
            if (detailsNode.isArray()) {
                for (JsonNode detailNode : detailsNode) {
                    AiScoreResponse.ScoreDetail detail = new AiScoreResponse.ScoreDetail();
                    detail.setItem(detailNode.get("item").asText());
                    detail.setFullScore(detailNode.get("fullScore").asInt());
                    detail.setScore(detailNode.get("score").asInt());
                    detail.setDeduction(detailNode.get("deduction").asText());
                    details.add(detail);
                }
            }
            aiResponse.setDetails(details);
            
            log.info("AI评分完成，总分: {}, 等级: {}", aiResponse.getTotalScore(), aiResponse.getLevel());
            return aiResponse;
            
        } catch (JsonProcessingException e) {
            log.error("解析AI响应JSON失败", e);
            throw new RuntimeException("解析AI响应失败", e);
        } catch (Exception e) {
            log.error("处理AI响应失败", e);
            throw new RuntimeException("处理AI响应失败", e);
        }
    }

    /**
     * 从内容中提取JSON部分
     */
    private String extractJsonFromContent(String content) {
        // 去除可能的markdown代码块标记
        content = content.trim();
        if (content.startsWith("```json")) {
            content = content.substring(7);
        }
        if (content.startsWith("```")) {
            content = content.substring(3);
        }
        if (content.endsWith("```")) {
            content = content.substring(0, content.length() - 3);
        }
        
        // 查找JSON开始和结束位置
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        
        if (start == -1 || end == -1 || start >= end) {
            throw new RuntimeException("无法从AI响应中提取有效的JSON: " + content);
        }
        
        return content.substring(start, end + 1).trim();
    }
} 