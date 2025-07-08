package com.bailun.ai.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * AI病历评分记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "ai_score_medical_record")
public class AiScoreMedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "patient_name", length = 100)
    private String patientName;

    @Column(name = "score_result", columnDefinition = "JSON")
    private String scoreResult;

    @Column(name = "expert_comment", columnDefinition = "TEXT")
    private String expertComment;

    @Column(name = "score_total")
    private Integer scoreTotal;

    @Column(name = "score_level", length = 20)
    private String scoreLevel;

    @Column(name = "ai_model", length = 100)
    private String aiModel;

    @CreationTimestamp
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean deleted = false;

    @Column(name = "version", nullable = false, columnDefinition = "INT DEFAULT 1")
    private Integer version = 1;

    @Column(name = "ai_prompt", columnDefinition = "TEXT")
    private String aiPrompt;

    @Column(name = "ai_request_json", columnDefinition = "JSON")
    private String aiRequestJson;

    @Column(name = "ai_response_json", columnDefinition = "JSON")
    private String aiResponseJson;

    @Column(name = "patient_basic_info", columnDefinition = "JSON")
    private String patientBasicInfo;

    @Column(name = "treatment_info", columnDefinition = "JSON")
    private String treatmentInfo;
} 