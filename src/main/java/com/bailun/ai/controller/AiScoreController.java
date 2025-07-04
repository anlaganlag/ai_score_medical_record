package com.bailun.ai.controller;

import com.bailun.ai.dto.AiScoreRequest;
import com.bailun.ai.entity.AiScoreMedicalRecord;
import com.bailun.ai.service.AiScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * AI评分控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin-api/business/ai-score")
@RequiredArgsConstructor
@Validated
public class AiScoreController {

    private final AiScoreService aiScoreService;

    /**
     * 生成/更新AI评分
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateAiScore(
            @Valid @RequestBody AiScoreRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "systemdeptid", required = false) String systemDeptId) {
        
        log.info("收到AI评分生成请求，患者ID: {}", request.getPatientId());
        
        try {
            AiScoreMedicalRecord record = aiScoreService.generateAiScore(request);
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", record.getId());
            data.put("patientId", record.getPatientId());
            data.put("status", "completed");
            
            return ResponseEntity.ok(buildSuccessResponse(data, "评分生成成功"));
            
        } catch (IllegalArgumentException e) {
            log.error("参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest().body(buildErrorResponse(400, e.getMessage()));
        } catch (Exception e) {
            log.error("生成AI评分失败", e);
            return ResponseEntity.internalServerError().body(buildErrorResponse(500, "评分生成失败：" + e.getMessage()));
        }
    }

    /**
     * 查看AI评分报告
     */
    @GetMapping("/report/{patientId}")
    public ResponseEntity<Map<String, Object>> getAiScoreReport(
            @PathVariable @NotNull Long patientId,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "systemdeptid", required = false) String systemDeptId) {
        
        log.info("查看AI评分报告，患者ID: {}", patientId);
        
        try {
            AiScoreMedicalRecord record = aiScoreService.getAiScoreReport(patientId);
            
            if (record == null) {
                return ResponseEntity.ok(buildErrorResponse(404, "未找到该患者的评分报告"));
            }
            
            Map<String, Object> data = buildReportData(record);
            return ResponseEntity.ok(buildSuccessResponse(data, ""));
            
        } catch (Exception e) {
            log.error("获取AI评分报告失败", e);
            return ResponseEntity.internalServerError().body(buildErrorResponse(500, "获取报告失败：" + e.getMessage()));
        }
    }

    /**
     * 保存专家点评
     */
    @PostMapping("/expert-comment")
    public ResponseEntity<Map<String, Object>> saveExpertComment(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "systemdeptid", required = false) String systemDeptId) {
        
        Long patientId = Long.valueOf(request.get("patientId").toString());
        String expertComment = (String) request.get("expertComment");
        
        log.info("保存专家点评，患者ID: {}", patientId);
        
        try {
            boolean success = aiScoreService.saveExpertComment(patientId, expertComment);
            
            if (success) {
                return ResponseEntity.ok(buildSuccessResponse(true, "专家点评保存成功"));
            } else {
                return ResponseEntity.ok(buildErrorResponse(404, "未找到该患者的评分记录"));
            }
            
        } catch (IllegalArgumentException e) {
            log.error("参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest().body(buildErrorResponse(400, e.getMessage()));
        } catch (Exception e) {
            log.error("保存专家点评失败", e);
            return ResponseEntity.internalServerError().body(buildErrorResponse(500, "保存失败：" + e.getMessage()));
        }
    }

    /**
     * 构建报告数据
     */
    private Map<String, Object> buildReportData(AiScoreMedicalRecord record) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", record.getId());
        data.put("patientId", record.getPatientId());
        data.put("patientName", record.getPatientName());
        data.put("expertComment", record.getExpertComment());
        data.put("createdTime", record.getCreatedTime());
        data.put("updatedTime", record.getUpdatedTime());
        
        // 解析评分结果JSON
        try {
            if (record.getScoreResult() != null) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map<String, Object> scoreResult = mapper.readValue(record.getScoreResult(), Map.class);
                data.put("scoreResult", scoreResult);
            }
        } catch (Exception e) {
            log.error("解析评分结果JSON失败", e);
            // 如果解析失败，提供基本信息
            Map<String, Object> basicScore = new HashMap<>();
            basicScore.put("totalScore", record.getScoreTotal());
            basicScore.put("level", record.getScoreLevel());
            data.put("scoreResult", basicScore);
        }
        
        return data;
    }

    /**
     * 构建成功响应
     */
    private Map<String, Object> buildSuccessResponse(Object data, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("data", data);
        response.put("msg", message);
        return response;
    }

    /**
     * 构建错误响应
     */
    private Map<String, Object> buildErrorResponse(int code, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", code);
        response.put("data", null);
        response.put("msg", message);
        return response;
    }
} 