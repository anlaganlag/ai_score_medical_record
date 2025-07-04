package com.bailun.ai.repository;

import com.bailun.ai.entity.AiScoreMedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * AI评分记录Repository
 */
@Repository
public interface AiScoreMedicalRecordRepository extends JpaRepository<AiScoreMedicalRecord, Long> {
    
    /**
     * 根据患者ID查找未删除的评分记录
     */
    Optional<AiScoreMedicalRecord> findByPatientIdAndDeletedFalse(Long patientId);
} 