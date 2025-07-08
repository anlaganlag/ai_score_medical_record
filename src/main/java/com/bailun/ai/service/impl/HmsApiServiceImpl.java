package com.bailun.ai.service.impl;

import com.bailun.ai.dto.PatientInfoDTO;
import com.bailun.ai.dto.TreatmentInfoDTO;
import com.bailun.ai.service.HmsApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.HashMap;

/**
 * HMS API服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HmsApiServiceImpl implements HmsApiService {

    private final WebClient.Builder webClientBuilder;

    @Value("${ai.hms.api-url}")
    private String hmsApiUrl;

    @Value("${ai.hms.token}")
    private String hmsToken;

    @Value("${ai.hms.dept-id}")
    private String hmsDeptId;

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @Override
    public PatientInfoDTO getPatientInfo(Long patientId) {
        log.info("调用HMS API获取患者信息，患者ID: {}", patientId);
        
        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(hmsApiUrl)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + hmsToken)
                    .defaultHeader("systemdeptid", hmsDeptId)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();

            Map<String, Object> response = webClient.get()
                    .uri("/admin-api/business/outpatient-blood/get?patientId={patientId}", patientId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(TIMEOUT)
                    .block();

            if (response != null && response.get("code").equals(0)) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                return mapToPatientInfoDTO(data);
            } else {
                log.error("HMS API返回错误: {}", response);
                return null;
            }
            
        } catch (Exception e) {
            log.error("调用HMS API获取患者信息失败，患者ID: {}", patientId, e);
            throw new RuntimeException("获取患者信息失败", e);
        }
    }

    @Override
    public Map<String, Object> getPatientInfoRaw(Long patientId) {
        log.info("调用HMS API获取患者原始信息，患者ID: {}", patientId);
        
        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(hmsApiUrl)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + hmsToken)
                    .defaultHeader("systemdeptid", hmsDeptId)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();

            Map<String, Object> response = webClient.get()
                    .uri("/admin-api/business/outpatient-blood/get?patientId={patientId}", patientId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(TIMEOUT)
                    .block();

            if (response != null && response.get("code").equals(0)) {
                return (Map<String, Object>) response.get("data");
            } else {
                log.error("HMS API返回错误: {}", response);
                return new HashMap<>();
            }
            
        } catch (Exception e) {
            log.error("调用HMS API获取患者原始信息失败，患者ID: {}", patientId, e);
            throw new RuntimeException("获取患者原始信息失败", e);
        }
    }

    @Override
    public TreatmentInfoDTO getTreatmentInfo(Long patientId) {
        log.info("调用HMS API获取诊疗信息，患者ID: {}", patientId);
        
        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(hmsApiUrl)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + hmsToken)
                    .defaultHeader("systemdeptid", hmsDeptId)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();

            Map<String, Object> requestBody = Map.of("patientId", patientId);
            
            Map<String, Object> response = webClient.post()
                    .uri("/admin-api/business/first-course-record/get")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(TIMEOUT)
                    .block();

            if (response != null && response.get("code").equals(0)) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                return mapToTreatmentInfoDTO(data);
            } else {
                log.error("HMS API返回错误: {}", response);
                return null;
            }
            
        } catch (Exception e) {
            log.error("调用HMS API获取诊疗信息失败，患者ID: {}", patientId, e);
            throw new RuntimeException("获取诊疗信息失败", e);
        }
    }

    /**
     * 映射患者信息数据
     */
    private PatientInfoDTO mapToPatientInfoDTO(Map<String, Object> data) {
        if (data == null) {
            return null;
        }
        
        PatientInfoDTO dto = new PatientInfoDTO();
        dto.setId(getLongValue(data, "id"));
        dto.setName(getStringValue(data, "name"));
        dto.setAllergy(getStringValue(data, "allergy"));
        dto.setTumor(getStringValue(data, "tumor"));
        dto.setSmoking(getStringValue(data, "smoking"));
        dto.setTipple(getStringValue(data, "tipple"));
        dto.setDialyzeAge(getStringValue(data, "dialyzeAge"));
        dto.setFirstReceiveTime(getStringValue(data, "firstReceiveTime"));
        dto.setStature(getStringValue(data, "stature"));
        dto.setInitWeight(getStringValue(data, "initWeight"));
        dto.setEyesight(getStringValue(data, "eyesight"));
        dto.setBloodType(getStringValue(data, "bloodType"));
        dto.setRh(getStringValue(data, "rh"));
        dto.setErythrocyte(getStringValue(data, "erythrocyte"));
        dto.setState(getStringValue(data, "state"));
        dto.setSignature(getStringValue(data, "signature"));
        
        return dto;
    }

    /**
     * 映射诊疗信息数据
     */
    private TreatmentInfoDTO mapToTreatmentInfoDTO(Map<String, Object> data) {
        if (data == null) {
            return null;
        }
        
        TreatmentInfoDTO dto = new TreatmentInfoDTO();
        dto.setPatientId(getLongValue(data, "patientId"));
        dto.setMainSuit(getStringValue(data, "mainSuit"));
        dto.setInitDiagnosis(getStringValue(data, "initDiagnosis"));
        dto.setHealingProject(getStringValue(data, "healingProject"));
        dto.setNowDiseaseHistory(getStringValue(data, "nowDiseaseHistory"));
        dto.setOldDiseaseHistory(getStringValue(data, "oldDiseaseHistory"));
        dto.setWeightPre(getStringValue(data, "weightPre"));
        dto.setPressurePre(getStringValue(data, "pressurePre"));
        dto.setBodyInspect(getStringValue(data, "bodyInspect"));
        dto.setAssayInspect(getStringValue(data, "assayInspect"));
        dto.setDiagnosis(getStringValue(data, "diagnosis"));
        dto.setDiseaseReasonNames(getStringValue(data, "diseaseReasonNames"));
        dto.setDialysisPlan(getStringValue(data, "dialysisPlan"));
        dto.setMedic(getStringValue(data, "medic"));
        dto.setNurse(getStringValue(data, "nurse"));
        dto.setFacilityName(getStringValue(data, "facilityName"));
        dto.setStartDialyzeTime(getStringValue(data, "startDialyzeTime"));
        dto.setEndDialyzeTime(getStringValue(data, "endDialyzeTime"));
        
        return dto;
    }

    /**
     * 安全获取字符串值
     */
    private String getStringValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 安全获取Long值
     */
    private Long getLongValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            log.warn("无法将值转换为Long: key={}, value={}", key, value);
            return null;
        }
    }
} 