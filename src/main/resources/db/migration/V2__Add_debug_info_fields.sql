-- 添加患者基本信息和诊疗信息字段
ALTER TABLE ai_score_medical_record 
ADD COLUMN patient_basic_info JSON COMMENT '患者基本信息(接口1返回数据)',
ADD COLUMN treatment_info JSON COMMENT '初始诊疗信息(接口2返回数据)'; 