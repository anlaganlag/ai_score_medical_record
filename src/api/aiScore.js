import request from '@/utils/request'

// 生成AI评分
export function generateAiScore(data) {
  return request({
    url: '/ai-score/generate',
    method: 'post',
    data
  })
}

// 获取患者AI评分列表
export function getAiScoreList(patientId) {
  return request({
    url: '/ai-score/list',
    method: 'get',
    params: {
      patientId
    }
  })
}

// 获取AI评分详情
export function getAiScoreDetail(scoreId) {
  return request({
    url: '/ai-score/detail',
    method: 'get',
    params: {
      scoreId
    }
  })
}

// 保存专家点评
export function saveExpertComment(data) {
  return request({
    url: '/ai-score/expert-comment',
    method: 'post',
    data
  })
}

// 删除AI评分记录
export function deleteAiScore(scoreId) {
  return request({
    url: '/ai-score/delete',
    method: 'delete',
    params: {
      scoreId
    }
  })
}

// 获取AI评分生成状态
export function getGenerateStatus(taskId) {
  return request({
    url: '/ai-score/status',
    method: 'get',
    params: {
      taskId
    }
  })
}

// 获取门诊血液患者信息
export function getOutpatientBloodInfo(patientId) {
  return request({
    url: '/admin-api/business/outpatient-blood/get',
    method: 'get',
    params: { patientId }
  })
} 