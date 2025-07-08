package com.bailun.ai.repository;

import com.bailun.ai.entity.AiScoreMedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * AI评分记录Repository
 */
@Repository
public interface AiScoreMedicalRecordRepository extends JpaRepository<AiScoreMedicalRecord, Long> {
    
    /**
     * 根据患者ID查找未删除的最新评分记录（按创建时间倒序，取第一条）
     */
    Optional<AiScoreMedicalRecord> findFirstByPatientIdAndDeletedFalseOrderByCreatedTimeDesc(Long patientId);
    
    /**
     * 根据患者ID查找所有未删除的评分记录（按创建时间倒序）
     */
    List<AiScoreMedicalRecord> findAllByPatientIdAndDeletedFalseOrderByCreatedTimeDesc(Long patientId);
} 