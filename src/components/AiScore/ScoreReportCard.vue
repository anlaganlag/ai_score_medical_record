<template>
  <div class="score-report-card">
    <!-- 报告头部 -->
    <div class="report-header">
      <div class="score-summary">
        <div class="main-score">
          <span class="score-value" :class="getScoreClass(report.score)">
            {{ report.score }}
          </span>
          <span class="score-label">分</span>
        </div>
        <div class="score-level">
          <el-tag :type="getScoreTagType(report.score)" size="medium">
            {{ getScoreLevel(report.score) }}
          </el-tag>
        </div>
      </div>
      
      <div class="report-info">
        <p class="report-time">
          <i class="el-icon-time"></i>
          生成时间：{{ formatTime(report.createTime) }}
        </p>
        <p class="report-type">
          <i class="el-icon-document"></i>
          评分类型：{{ getReportTypeLabel(report.recordType) }}
        </p>
      </div>
    </div>

    <!-- 评分详情 -->
    <div class="report-content">
      <el-row :gutter="20">
        <!-- 分项评分 -->
        <el-col :span="16">
          <div class="section-title">
            <i class="el-icon-data-line"></i>
            详细评分
          </div>
          <div class="score-details">
            <div 
              v-for="(item, index) in report.details"
              :key="index"
              class="score-item"
            >
              <div class="item-header">
                <span class="item-label">{{ item.item }}</span>
                <div class="item-score">
                  <el-progress
                    :percentage="getPercentage(item.score, item.fullScore)"
                    :color="getProgressColor(getPercentage(item.score, item.fullScore))"
                    :stroke-width="8"
                    :show-text="false"
                  />
                  <span class="score-text">{{ item.score }}/{{ item.fullScore }}分</span>
                </div>
              </div>
              
              <!-- 扣分说明 -->
              <div v-if="item.deduction" class="deduction-info">
                <span class="deduction-label">扣分原因：</span>
                <span class="deduction-text">{{ item.deduction }}</span>
              </div>
              
              <!-- 字段详情 -->
              <div v-if="item.fieldDetails && item.fieldDetails.length > 0" class="field-details">
                <div class="field-details-title">
                  <i class="el-icon-warning"></i>
                  问题字段详情
                </div>
                <div 
                  v-for="(field, fieldIndex) in item.fieldDetails"
                  :key="fieldIndex"
                  class="field-detail-item"
                >
                  <div class="field-info">
                    <span class="field-name">{{ field.fieldLabel }}</span>
                    <el-tag type="info" size="mini">{{ field.fieldName }}</el-tag>
                  </div>
                  <div class="field-value">
                    <span class="value-label">当前值：</span>
                    <span class="value-content">{{ field.fieldValue || '空值' }}</span>
                  </div>
                  <div class="field-issue">
                    <span class="issue-label">问题：</span>
                    <span class="issue-content">{{ field.issue }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-col>

        <!-- AI建议 -->
        <el-col :span="8">
          <div class="section-title">
            <i class="el-icon-info"></i>
            AI分析建议
          </div>
          <div class="suggestions">
            <div 
              v-for="(suggestion, index) in report.suggestions"
              :key="index"
              class="suggestion-item"
            >
              <el-tag type="info" size="small">{{ index + 1 }}</el-tag>
              <span class="suggestion-text">{{ suggestion }}</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 专家点评区域 -->
    <div class="expert-comment-section">
      <div class="section-title">
        <i class="el-icon-edit-outline"></i>
        专家点评
        <el-button 
          v-if="!isEditing && hasPermission"
          type="text" 
          size="small"
          @click="startEdit"
        >
          {{ report.expertComment ? '编辑' : '添加点评' }}
        </el-button>
      </div>

      <!-- 查看模式 -->
      <div v-if="!isEditing" class="comment-view">
        <div v-if="report.expertComment" class="comment-content">
          {{ report.expertComment }}
        </div>
        <div v-else class="no-comment">
          <el-empty description="暂无专家点评" :image-size="60" />
        </div>
      </div>

      <!-- 编辑模式 -->
      <div v-else class="comment-edit">
        <el-input
          v-model="editComment"
          type="textarea"
          :rows="4"
          :maxlength="500"
          show-word-limit
          placeholder="请输入专家点评..."
          resize="none"
        />
        <div class="edit-actions">
          <el-button size="small" @click="cancelEdit">取消</el-button>
          <el-button 
            type="primary" 
            size="small"
            :loading="saving"
            @click="saveComment"
          >
            保存
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { saveExpertComment } from '@/api/aiScore'
import { mapGetters } from 'vuex'

export default {
  name: 'ScoreReportCard',
  props: {
    report: {
      type: Object,
      required: true
    },
    patientInfo: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      isEditing: false,
      editComment: '',
      saving: false
    }
  },
  computed: {
    ...mapGetters(['permissions']),
    
    hasPermission() {
      // 检查用户是否有专家点评权限
      return this.checkPermi(['ai:score:comment'])
    }
  },
  methods: {
    // 检查权限
    checkPermi(permissionList) {
      if (!permissionList || permissionList.length === 0) return true
      return permissionList.some(permission => this.permissions.includes(permission))
    },

    // 获取评分等级
    getScoreLevel(score) {
      if (score >= 90) return '优秀'
      if (score >= 80) return '良好'
      if (score >= 70) return '合格'
      if (score >= 60) return '基本合格'
      return '不合格'
    },

    // 获取评分标签类型
    getScoreTagType(score) {
      if (score >= 90) return 'success'
      if (score >= 80) return 'primary'
      if (score >= 70) return 'warning'
      return 'danger'
    },

    // 获取评分样式类
    getScoreClass(score) {
      if (score >= 90) return 'excellent'
      if (score >= 80) return 'good'
      if (score >= 70) return 'fair'
      return 'poor'
    },

    // 获取进度条颜色
    getProgressColor(score) {
      if (score >= 90) return '#52c41a'
      if (score >= 80) return '#1890ff'
      if (score >= 70) return '#faad14'
      return '#f5222d'
    },

    // 获取详情标签
    getDetailLabel(key) {
      const labelMap = {
        medication: '用药合理性',
        treatment: '治疗方案',
        monitoring: '监测指标',
        nursing: '护理质量',
        safety: '安全管理',
        documentation: '文档规范'
      }
      return labelMap[key] || key
    },

    // 获取报告类型标签
    getReportTypeLabel(type) {
      const typeMap = {
        comprehensive: '综合评分',
        medication: '用药评分',
        treatment: '治疗评分',
        nursing: '护理评分'
      }
      return typeMap[type] || type
    },

    // 格式化时间
    formatTime(time) {
      if (!time) return '--'
      return new Date(time).toLocaleString('zh-CN')
    },

    // 计算百分比
    getPercentage(score, fullScore) {
      if (fullScore === 0) return 0
      return (score / fullScore) * 100
    },

    // 开始编辑
    startEdit() {
      this.isEditing = true
      this.editComment = this.report.expertComment || ''
    },

    // 取消编辑
    cancelEdit() {
      this.isEditing = false
      this.editComment = ''
    },

    // 保存点评
    async saveComment() {
      if (!this.editComment.trim()) {
        this.$modal.msgWarning('请输入点评内容')
        return
      }

      this.saving = true
      
      try {
        const requestData = {
          scoreId: this.report.scoreId,
          comment: this.editComment.trim(),
          patientId: this.report.patientId
        }
        
        const response = await saveExpertComment(requestData)
        
        if (response.code === 200) {
          // 通知父组件更新数据
          this.$emit('comment-updated', {
            scoreId: this.report.scoreId,
            comment: this.editComment.trim()
          })
          
          this.isEditing = false
        } else {
          this.$modal.msgError(response.msg || '保存失败')
        }
      } catch (error) {
        console.error('保存专家点评失败:', error)
        this.$modal.msgError('保存失败，请重试')
      } finally {
        this.saving = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.score-report-card {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;

  .report-header {
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    padding: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .score-summary {
      display: flex;
      align-items: center;
      gap: 15px;

      .main-score {
        display: flex;
        align-items: baseline;
        
        .score-value {
          font-size: 48px;
          font-weight: 700;
          
          &.excellent { color: #52c41a; }
          &.good { color: #1890ff; }
          &.fair { color: #faad14; }
          &.poor { color: #f5222d; }
        }
        
        .score-label {
          font-size: 24px;
          color: #666;
          margin-left: 4px;
        }
      }
    }

    .report-info {
      text-align: right;
      
      p {
        margin: 0 0 8px 0;
        color: #666;
        font-size: 14px;
        
        i {
          margin-right: 4px;
        }
      }
    }
  }

  .report-content {
    padding: 20px;

    .section-title {
      font-size: 16px;
      font-weight: 600;
      margin-bottom: 15px;
      color: #2c3e50;
      display: flex;
      align-items: center;
      
      i {
        margin-right: 8px;
        color: #1890ff;
      }
    }

    .score-details {
      .score-item {
        border: 1px solid #e8e8e8;
        border-radius: 6px;
        padding: 15px;
        margin-bottom: 15px;
        background-color: #f8f9fa;

        .item-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 10px;

          .item-label {
            font-size: 16px;
            font-weight: 600;
            color: #2c3e50;
          }

          .item-score {
            display: flex;
            align-items: center;
            gap: 10px;

            .el-progress {
              flex: 1;
              margin-right: 10px;
            }
            
            .score-text {
              font-size: 14px;
              color: #666;
              min-width: 60px;
            }
          }
        }

        .deduction-info {
          background-color: #fffbe6;
          border: 1px solid #ffe58f;
          border-radius: 4px;
          padding: 8px 12px;
          margin-top: 10px;
          margin-bottom: 10px;

          .deduction-label {
            font-size: 14px;
            font-weight: 600;
            color: #faad14;
            margin-right: 5px;
          }

          .deduction-text {
            font-size: 14px;
            color: #faad14;
            line-height: 1.5;
          }
        }

        .field-details {
          margin-top: 15px;
          border: 1px solid #e8e8e8;
          border-radius: 4px;
          padding: 10px;
          background-color: #f8f9fa;

          .field-details-title {
            font-size: 14px;
            font-weight: 600;
            color: #2c3e50;
            display: flex;
            align-items: center;
            margin-bottom: 10px;

            i {
              margin-right: 8px;
              color: #1890ff;
            }
          }

          .field-detail-item {
            border-bottom: 1px dashed #e8e8e8;
            padding-bottom: 10px;
            margin-bottom: 10px;

            &:last-child {
              border-bottom: none;
              margin-bottom: 0;
              padding-bottom: 0;
            }

            .field-info {
              display: flex;
              align-items: center;
              margin-bottom: 5px;

              .field-name {
                font-size: 14px;
                font-weight: 600;
                color: #2c3e50;
                margin-right: 10px;
              }

              .el-tag {
                font-size: 12px;
              }
            }

            .field-value {
              font-size: 14px;
              color: #666;
              margin-bottom: 5px;

              .value-label {
                font-weight: 600;
                color: #2c3e50;
              }

              .value-content {
                font-weight: 400;
                color: #2c3e50;
              }
            }

            .field-issue {
              font-size: 14px;
              color: #f5222d;
              font-weight: 600;

              .issue-label {
                font-weight: 600;
                color: #f5222d;
              }

              .issue-content {
                font-weight: 400;
                color: #f5222d;
              }
            }
          }
        }
      }
    }

    .suggestions {
      .suggestion-item {
        display: flex;
        align-items: flex-start;
        margin-bottom: 10px;
        
        .el-tag {
          margin-right: 8px;
          margin-top: 2px;
        }
        
        .suggestion-text {
          flex: 1;
          font-size: 14px;
          line-height: 1.5;
          color: #2c3e50;
        }
      }
    }
  }

  .expert-comment-section {
    border-top: 1px solid #e8e8e8;
    padding: 20px;

    .section-title {
      font-size: 16px;
      font-weight: 600;
      margin-bottom: 15px;
      color: #2c3e50;
      display: flex;
      align-items: center;
      justify-content: space-between;
      
      i {
        margin-right: 8px;
        color: #1890ff;
      }
    }

    .comment-view {
      .comment-content {
        background-color: #f8f9fa;
        padding: 15px;
        border-radius: 6px;
        line-height: 1.6;
        color: #2c3e50;
        border-left: 4px solid #1890ff;
      }
      
      .no-comment {
        text-align: center;
        padding: 20px 0;
      }
    }

    .comment-edit {
      .edit-actions {
        margin-top: 10px;
        text-align: right;
      }
    }
  }
}
</style> 