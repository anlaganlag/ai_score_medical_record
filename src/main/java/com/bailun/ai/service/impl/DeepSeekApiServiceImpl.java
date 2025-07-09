package com.bailun.ai.service.impl;

import com.bailun.ai.dto.AiScoreResponse;
import com.bailun.ai.dto.AiScoreDebugResponse;
import com.bailun.ai.dto.TreatmentInfoDTO;
import com.bailun.ai.service.DeepSeekApiService;
import com.bailun.ai.service.PromptTemplateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(name = "ai.deepseek.mock.enabled", havingValue = "false", matchIfMissing = true)
public class DeepSeekApiServiceImpl implements DeepSeekApiService {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private final PromptTemplateService promptTemplateService;

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

    @Override
    public AiScoreDebugResponse evaluateMedicalRecordWithDebug(String medicalRecordJson) {
        log.info("调用DeepSeek AI评估病历记录（包含调试信息）");
        
        for (int attempt = 1; attempt <= retryCount + 1; attempt++) {
            try {
                return callDeepSeekApiWithDebug(medicalRecordJson);
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
        
        // 添加请求日志
        try {
            String requestJson = objectMapper.writeValueAsString(requestBody);
            log.info("AI请求体: {}", requestJson);
        } catch (JsonProcessingException e) {
            log.warn("序列化请求体用于日志记录失败", e);
        }
        
        Map<String, Object> response = webClient.post()
                .uri("/v1/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), clientResponse -> {
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(errorBody -> {
                                log.error("DeepSeek API错误响应 [{}]: {}", clientResponse.statusCode(), errorBody);
                                return Mono.error(new RuntimeException("API调用失败: " + clientResponse.statusCode() + " - " + errorBody));
                            });
                })
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
                "content", promptTemplateService.buildSystemPrompt()
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

    /**
     * 调用DeepSeek AI API（包含调试信息）
     */
    private AiScoreDebugResponse callDeepSeekApiWithDebug(String medicalRecordJson) {
        WebClient webClient = webClientBuilder
                .baseUrl(deepSeekApiUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + deepSeekApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        // 构建提示词
        String systemPrompt = promptTemplateService.buildSystemPrompt();
        String userPrompt = "请对以下病历进行评分：\n" + medicalRecordJson;
        
        Map<String, Object> requestBody = buildRequestBody(medicalRecordJson);
        
        // 转换请求体为JSON字符串
        String requestJson;
        try {
            requestJson = objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化请求体失败", e);
        }
        
        Map<String, Object> response = webClient.post()
                .uri("/v1/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), clientResponse -> {
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(errorBody -> {
                                log.error("DeepSeek API错误响应 [{}]: {}", clientResponse.statusCode(), errorBody);
                                return Mono.error(new RuntimeException("API调用失败: " + clientResponse.statusCode() + " - " + errorBody));
                            });
                })
                .bodyToMono(Map.class)
                .timeout(Duration.ofMillis(timeoutMs))
                .block();

        // 转换响应为JSON字符串
        String responseJson;
        try {
            responseJson = objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化响应失败", e);
        }

        // 解析AI响应
        AiScoreResponse aiResponse = parseAiResponse(response);

        // 构建调试响应
        AiScoreDebugResponse debugResponse = new AiScoreDebugResponse();
        debugResponse.setScoreResponse(aiResponse);
        debugResponse.setAiPrompt(systemPrompt + "\n\n" + userPrompt);
        debugResponse.setAiRequestJson(requestJson);
        debugResponse.setAiResponseJson(responseJson);

        return debugResponse;
    }

    @Override
    public AiScoreResponse evaluateMedicalRecord(Map<String, Object> patientData, TreatmentInfoDTO treatmentInfo) {
        log.info("调用DeepSeek AI评估病历记录（结构化数据）");
        
        for (int attempt = 1; attempt <= retryCount + 1; attempt++) {
            try {
                return callDeepSeekApiWithStructuredData(patientData, treatmentInfo);
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

    @Override
    public AiScoreDebugResponse evaluateMedicalRecordWithDebug(Map<String, Object> patientData, TreatmentInfoDTO treatmentInfo) {
        log.info("调用DeepSeek AI评估病历记录（结构化数据，包含调试信息）");
        
        for (int attempt = 1; attempt <= retryCount + 1; attempt++) {
            try {
                return callDeepSeekApiWithStructuredDataAndDebug(patientData, treatmentInfo);
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
     * 调用DeepSeek AI API（结构化数据）
     */
    private AiScoreResponse callDeepSeekApiWithStructuredData(Map<String, Object> patientData, TreatmentInfoDTO treatmentInfo) {
        WebClient webClient = webClientBuilder
                .baseUrl(deepSeekApiUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + deepSeekApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        Map<String, Object> requestBody = buildRequestBodyWithStructuredData(patientData, treatmentInfo);
        
        Map<String, Object> response = webClient.post()
                .uri("/v1/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), clientResponse -> {
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(errorBody -> {
                                log.error("DeepSeek API错误响应 [{}]: {}", clientResponse.statusCode(), errorBody);
                                return Mono.error(new RuntimeException("API调用失败: " + clientResponse.statusCode() + " - " + errorBody));
                            });
                })
                .bodyToMono(Map.class)
                .timeout(Duration.ofMillis(timeoutMs))
                .block();

        return parseAiResponse(response);
    }

    /**
     * 调用DeepSeek AI API（结构化数据，包含调试信息）
     */
    private AiScoreDebugResponse callDeepSeekApiWithStructuredDataAndDebug(Map<String, Object> patientData, TreatmentInfoDTO treatmentInfo) {
        WebClient webClient = webClientBuilder
                .baseUrl(deepSeekApiUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + deepSeekApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        // 构建提示词
        String systemPrompt = promptTemplateService.buildSystemPrompt();
        String userPrompt = promptTemplateService.buildUserPrompt(patientData, treatmentInfo);
        
        Map<String, Object> requestBody = buildRequestBodyWithStructuredData(patientData, treatmentInfo);
        
        // 转换请求体为JSON字符串
        String requestJson;
        try {
            requestJson = objectMapper.writeValueAsString(requestBody);
            log.info("AI请求体: {}", requestJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化请求体失败", e);
        }
        
        Map<String, Object> response = webClient.post()
                .uri("/v1/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), clientResponse -> {
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(errorBody -> {
                                log.error("DeepSeek API错误响应 [{}]: {}", clientResponse.statusCode(), errorBody);
                                return Mono.error(new RuntimeException("API调用失败: " + clientResponse.statusCode() + " - " + errorBody));
                            });
                })
                .bodyToMono(Map.class)
                .timeout(Duration.ofMillis(timeoutMs))
                .block();

        // 转换响应为JSON字符串
        String responseJson;
        try {
            responseJson = objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化响应失败", e);
        }

        // 解析AI响应
        AiScoreResponse aiResponse = parseAiResponse(response);

        // 构建调试响应
        AiScoreDebugResponse debugResponse = new AiScoreDebugResponse();
        debugResponse.setScoreResponse(aiResponse);
        debugResponse.setAiPrompt(systemPrompt + "\n\n" + userPrompt);
        debugResponse.setAiRequestJson(requestJson);
        debugResponse.setAiResponseJson(responseJson);

        return debugResponse;
    }

    /**
     * 构建请求体（结构化数据）
     */
    private Map<String, Object> buildRequestBodyWithStructuredData(Map<String, Object> patientData, TreatmentInfoDTO treatmentInfo) {
        List<Map<String, String>> messages = new ArrayList<>();
        
        // 系统提示词
        messages.add(Map.of(
                "role", "system",
                "content", promptTemplateService.buildSystemPrompt()
        ));
        
        // 用户输入 - 使用结构化的提示词模板
        messages.add(Map.of(
                "role", "user",
                "content", promptTemplateService.buildUserPrompt(patientData, treatmentInfo)
        ));

        return Map.of(
                "model", deepSeekModel,
                "messages", messages,
                "temperature", temperature,
                "max_tokens", maxTokens
        );
    }
} 