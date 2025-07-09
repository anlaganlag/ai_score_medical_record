package com.bailun.ai.dto;

import lombok.Data;
import java.util.List;

/**
 * AI评分响应DTO
 */
@Data
public class AiScoreResponse {
    
    private Integer totalScore;
    private String level;
    private List<ScoreDetail> details;
    
    @Data
    public static class ScoreDetail {
        private String item;
        private Integer fullScore;
        private Integer score;
        private String deduction;
        private List<FieldDetail> fieldDetails;
    }
    
    @Data
    public static class FieldDetail {
        private String fieldName;
        private String fieldLabel;
        private String fieldValue;
        private String issue;
    }
} 