package com.bailun.ai.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

/**
 * AI评分请求DTO
 */
@Data
public class AiScoreRequest {
    
    @NotNull(message = "患者ID不能为空")
    private Long patientId;
} 