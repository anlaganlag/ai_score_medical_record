-- AI病历评分记录表
CREATE TABLE ai_score_medical_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    patient_name VARCHAR(100) COMMENT '患者姓名',
    score_result JSON COMMENT '评分结果JSON',
    expert_comment TEXT COMMENT '专家点评',
    score_total INT COMMENT '总分',
    score_level VARCHAR(20) COMMENT '评分等级(甲级/乙级/丙级)',
    ai_model VARCHAR(100) COMMENT 'AI模型名称',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    version INT DEFAULT 1 COMMENT '版本号(预留扩展)',
    INDEX idx_patient_id (patient_id),
    INDEX idx_created_time (created_time)
) COMMENT='AI病历评分记录表'; 