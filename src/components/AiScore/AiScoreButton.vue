<template>
  <div class="ai-score-button">
    <el-button
      v-hasPermi="['ai:score:generate']"
      type="primary"
      icon="el-icon-cpu"
      :loading="generating"
      :disabled="!patientId || generating"
      @click="handleGenerateScore"
    >
      {{ generating ? '生成中...' : '生成AI评分' }}
    </el-button>
    
    <el-button
      v-if="hasExistingScores"
      type="success"
      icon="el-icon-view"
      @click="handleViewScores"
    >
      查看评分报告
    </el-button>
  </div>
</template>

<script>
import { generateAiScore, getAiScoreList } from '@/api/aiScore'

export default {
  name: 'AiScoreButton',
  props: {
    patientId: {
      type: [String, Number],
      required: true
    },
    patientInfo: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      generating: false,
      hasExistingScores: false,
      existingScores: []
    }
  },
  mounted() {
    this.checkExistingScores()
  },
  watch: {
    patientId: {
      handler(newVal) {
        if (newVal) {
          this.checkExistingScores()
        }
      },
      immediate: true
    }
  },
  methods: {
    // 检查是否有已存在的评分
    async checkExistingScores() {
      if (!this.patientId) return
      
      try {
        const response = await getAiScoreList(this.patientId)
        if (response.code === 200) {
          this.existingScores = response.data || []
          this.hasExistingScores = this.existingScores.length > 0
        }
      } catch (error) {
        console.error('获取评分列表失败:', error)
      }
    },

    // 生成AI评分
    async handleGenerateScore() {
      if (!this.patientId) {
        this.$modal.msgError('患者信息无效，无法生成评分')
        return
      }

      // 检查患者信息完整性
      if (!this.validatePatientInfo()) {
        return
      }

      this.generating = true
      
      try {
        const requestData = {
          patientId: this.patientId,
          recordType: 'comprehensive', // 综合评分
          patientInfo: this.patientInfo
        }
        
        const response = await generateAiScore(requestData)
        
        if (response.code === 200) {
          this.$modal.msgSuccess('AI评分生成成功')
          
          // 刷新评分列表
          await this.checkExistingScores()
          
          // 触发父组件事件，打开评分报告弹窗
          this.$emit('score-generated', {
            scoreData: response.data,
            patientId: this.patientId
          })
        } else {
          this.$modal.msgError(response.msg || 'AI评分生成失败')
        }
      } catch (error) {
        console.error('生成AI评分失败:', error)
        
        if (error.response && error.response.status === 422) {
          this.$modal.msgError('患者病历信息不完整，无法生成评分')
        } else if (error.response && error.response.status === 403) {
          this.$modal.msgError('您没有权限执行此操作')
        } else {
          this.$modal.msgError('网络异常，请稍后重试')
        }
      } finally {
        this.generating = false
      }
    },

    // 查看已有评分
    handleViewScores() {
      this.$emit('view-scores', {
        scores: this.existingScores,
        patientId: this.patientId
      })
    },

    // 验证患者信息完整性
    validatePatientInfo() {
      if (!this.patientInfo || Object.keys(this.patientInfo).length === 0) {
        this.$modal.msgError('患者信息不完整，无法生成评分')
        return false
      }

      // 检查必要字段
      const requiredFields = ['name', 'gender', 'age']
      const missingFields = []
      
      requiredFields.forEach(field => {
        if (!this.patientInfo[field]) {
          missingFields.push(this.getFieldLabel(field))
        }
      })

      if (missingFields.length > 0) {
        this.$modal.msgError(`患者信息不完整，缺少：${missingFields.join('、')}`)
        return false
      }

      return true
    },

    // 获取字段标签
    getFieldLabel(field) {
      const fieldLabels = {
        name: '姓名',
        gender: '性别',
        age: '年龄',
        hospitalId: '住院号',
        diagnosis: '诊断'
      }
      return fieldLabels[field] || field
    }
  }
}
</script>

<style lang="scss" scoped>
.ai-score-button {
  .el-button {
    margin-right: 10px;
    
    &:last-child {
      margin-right: 0;
    }
  }
  
  .el-button--primary {
    background-color: #1890ff;
    border-color: #1890ff;
    
    &:hover {
      background-color: #40a9ff;
      border-color: #40a9ff;
    }
    
    &:active {
      background-color: #096dd9;
      border-color: #096dd9;
    }
  }
}
</style> 