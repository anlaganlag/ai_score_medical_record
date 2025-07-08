<template>
  <el-dialog
    :title="dialogTitle"
    :visible.sync="visible"
    :width="dialogWidth"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    class="ai-score-dialog"
    @close="handleClose"
  >
    <div class="dialog-content">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
        <div class="loading-text">正在生成AI评分报告...</div>
      </div>

      <!-- 评分报告列表 -->
      <div v-else class="score-reports">
        <!-- 报告标签页 -->
        <el-tabs 
          v-if="scoreReports.length > 1"
          v-model="activeTab"
          type="card"
          class="report-tabs"
        >
          <el-tab-pane
            v-for="(report, index) in scoreReports"
            :key="report.scoreId"
            :label="`报告${index + 1}(${report.score}分)`"
            :name="report.scoreId"
          >
            <score-report-card
              :report="report"
              :patient-info="patientInfo"
              @comment-updated="handleCommentUpdated"
            />
          </el-tab-pane>
        </el-tabs>

        <!-- 单个报告 -->
        <score-report-card
          v-else-if="scoreReports.length === 1"
          :report="scoreReports[0]"
          :patient-info="patientInfo"
          @comment-updated="handleCommentUpdated"
        />

        <!-- 无数据状态 -->
        <el-empty
          v-else
          description="暂无评分报告"
          :image-size="120"
        />
      </div>
    </div>

    <!-- 弹窗底部操作 -->
    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">关闭</el-button>
      <el-button
        v-if="scoreReports.length > 0"
        type="primary"
        icon="el-icon-download"
        @click="handleExport"
      >
        导出报告
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
import { getAiScoreList, getAiScoreDetail } from '@/api/aiScore'
import ScoreReportCard from './ScoreReportCard.vue'

export default {
  name: 'AiScoreDialog',
  components: {
    ScoreReportCard
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    patientId: {
      type: [String, Number],
      required: true
    },
    patientInfo: {
      type: Object,
      default: () => ({})
    },
    initialScoreData: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      loading: false,
      scoreReports: [],
      activeTab: '',
      maxReports: 3 // 最多显示3个报告
    }
  },
  computed: {
    dialogTitle() {
      const patientName = this.patientInfo.name || '患者'
      return `${patientName} - AI质控评分报告`
    },
    dialogWidth() {
      // 根据报告数量调整弹窗宽度
      if (this.scoreReports.length > 1) {
        return '90%'
      }
      return '800px'
    }
  },
  watch: {
    visible: {
      handler(newVal) {
        if (newVal) {
          this.initDialog()
        } else {
          this.resetDialog()
        }
      },
      immediate: true
    },
    initialScoreData: {
      handler(newVal) {
        if (newVal && this.visible) {
          this.addNewScore(newVal)
        }
      },
      immediate: true
    }
  },
  methods: {
    // 初始化弹窗
    async initDialog() {
      if (!this.patientId) return

      // 如果有初始评分数据，直接添加
      if (this.initialScoreData) {
        this.addNewScore(this.initialScoreData)
        return
      }

      // 否则加载已有的评分报告
      await this.loadScoreReports()
    },

    // 加载评分报告列表
    async loadScoreReports() {
      this.loading = true
      
      try {
        const response = await getAiScoreList(this.patientId)
        
        if (response.code === 200) {
          const scores = response.data || []
          
          // 限制最多显示3个最新的报告
          const latestScores = scores
            .sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
            .slice(0, this.maxReports)

          this.scoreReports = latestScores
          
          if (this.scoreReports.length > 0) {
            this.activeTab = this.scoreReports[0].scoreId
          }
        } else {
          this.$modal.msgError(response.msg || '获取评分报告失败')
        }
      } catch (error) {
        console.error('加载评分报告失败:', error)
        this.$modal.msgError('网络异常，请稍后重试')
      } finally {
        this.loading = false
      }
    },

    // 添加新的评分报告
    addNewScore(scoreData) {
      // 检查是否已存在相同的报告
      const existingIndex = this.scoreReports.findIndex(
        report => report.scoreId === scoreData.scoreId
      )

      if (existingIndex !== -1) {
        // 更新已存在的报告
        this.$set(this.scoreReports, existingIndex, scoreData)
      } else {
        // 添加新报告，如果超过最大数量则移除最旧的
        this.scoreReports.unshift(scoreData)
        if (this.scoreReports.length > this.maxReports) {
          this.scoreReports = this.scoreReports.slice(0, this.maxReports)
        }
      }

      // 设置当前标签页为新报告
      this.activeTab = scoreData.scoreId
    },

    // 处理专家点评更新
    handleCommentUpdated(data) {
      const { scoreId, comment } = data
      
      // 更新对应报告的专家点评
      const reportIndex = this.scoreReports.findIndex(
        report => report.scoreId === scoreId
      )
      
      if (reportIndex !== -1) {
        this.$set(this.scoreReports[reportIndex], 'expertComment', comment)
      }

      this.$modal.msgSuccess('专家点评保存成功')
    },

    // 导出报告
    handleExport() {
      // 这里可以实现报告导出功能
      // 可以导出为PDF或Excel格式
      this.$modal.msgInfo('报告导出功能开发中...')
    },

    // 关闭弹窗
    handleClose() {
      this.$emit('update:visible', false)
    },

    // 重置弹窗状态
    resetDialog() {
      this.loading = false
      this.scoreReports = []
      this.activeTab = ''
    }
  }
}
</script>

<style lang="scss" scoped>
.ai-score-dialog {
  .dialog-content {
    min-height: 400px;
    
    .loading-container {
      text-align: center;
      padding: 40px 0;
      
      .loading-text {
        margin-top: 20px;
        color: #666;
        font-size: 14px;
      }
    }
    
    .score-reports {
      .report-tabs {
        ::v-deep .el-tabs__header {
          margin-bottom: 20px;
        }
        
        ::v-deep .el-tabs__item {
          font-weight: 500;
          
          &.is-active {
            color: #1890ff;
          }
        }
      }
    }
  }
  
  .dialog-footer {
    text-align: right;
    padding-top: 20px;
    border-top: 1px solid #e8e8e8;
  }
}

// 调整弹窗样式
::v-deep .ai-score-dialog {
  .el-dialog {
    border-radius: 8px;
  }
  
  .el-dialog__header {
    background-color: #f8f9fa;
    border-bottom: 1px solid #e8e8e8;
    
    .el-dialog__title {
      font-weight: 600;
      color: #2c3e50;
    }
  }
  
  .el-dialog__body {
    padding: 20px 24px;
  }
  
  .el-dialog__footer {
    padding: 20px 24px;
    background-color: #f8f9fa;
    border-top: 1px solid #e8e8e8;
  }
}
</style> 