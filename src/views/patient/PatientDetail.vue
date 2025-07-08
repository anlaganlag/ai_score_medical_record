<template>
  <div class="patient-detail">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/patient' }">患者管理</el-breadcrumb-item>
        <el-breadcrumb-item>患者详情</el-breadcrumb-item>
      </el-breadcrumb>
      
      <div class="header-actions">
        <!-- AI评分按钮组件 -->
        <ai-score-button
          :patient-id="patientInfo.id"
          :patient-info="patientInfo"
          @score-generated="handleScoreGenerated"
          @view-scores="handleViewScores"
        />
        
        <el-button icon="el-icon-edit" @click="editPatient">编辑患者</el-button>
      </div>
    </div>

    <!-- 患者基本信息 -->
    <el-card class="patient-info-card" shadow="hover">
      <div slot="header" class="card-header">
        <span>患者基本信息</span>
        <el-tag :type="getStatusTagType(patientInfo.status)">
          {{ getStatusLabel(patientInfo.status) }}
        </el-tag>
      </div>
      
      <el-row :gutter="24">
        <el-col :span="8">
          <div class="info-item">
            <label>姓名：</label>
            <span>{{ patientInfo.name }}</span>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="info-item">
            <label>性别：</label>
            <span>{{ patientInfo.gender }}</span>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="info-item">
            <label>年龄：</label>
            <span>{{ patientInfo.age }}岁</span>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="info-item">
            <label>住院号：</label>
            <span>{{ patientInfo.hospitalId }}</span>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="info-item">
            <label>入院时间：</label>
            <span>{{ formatDate(patientInfo.admissionDate) }}</span>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="info-item">
            <label>主治医师：</label>
            <span>{{ patientInfo.doctor }}</span>
          </div>
        </el-col>
        <el-col :span="24">
          <div class="info-item">
            <label>主要诊断：</label>
            <span>{{ patientInfo.diagnosis }}</span>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 住院记录 -->
    <el-card class="medical-records-card" shadow="hover">
      <div slot="header" class="card-header">
        <span>住院记录</span>
        <el-button type="text" size="small" @click="refreshRecords">
          <i class="el-icon-refresh"></i> 刷新
        </el-button>
      </div>
      
      <el-table 
        :data="medicalRecords" 
        style="width: 100%"
        v-loading="recordsLoading"
      >
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="type" label="记录类型" width="120">
          <template slot-scope="scope">
            <el-tag size="small">{{ scope.row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="记录内容" show-overflow-tooltip />
        <el-table-column prop="doctor" label="医师" width="100" />
        <el-table-column label="操作" width="120">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="viewRecord(scope.row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- AI评分弹窗 -->
    <ai-score-dialog
      :visible.sync="scoreDialogVisible"
      :patient-id="patientInfo.id"
      :patient-info="patientInfo"
      :initial-score-data="newScoreData"
    />
  </div>
</template>

<script>
import AiScoreButton from '@/components/AiScore/AiScoreButton.vue'
import AiScoreDialog from '@/components/AiScore/AiScoreDialog.vue'
import { getOutpatientBloodInfo } from '@/api/aiScore'

export default {
  name: 'PatientDetail',
  components: {
    AiScoreButton,
    AiScoreDialog
  },
  data() {
    return {
      // 患者信息
      patientInfo: {
        id: 1001,
        name: '张三',
        gender: '男',
        age: 65,
        hospitalId: 'H202400001',
        admissionDate: '2024-01-15',
        doctor: '李医师',
        diagnosis: '慢性肾功能衰竭，血液透析',
        status: 'hospitalized'
      },
      
      // 住院记录
      medicalRecords: [
        {
          id: 1,
          date: '2024-01-15',
          type: '入院记录',
          content: '患者因慢性肾功能衰竭入院，既往有高血压病史...',
          doctor: '李医师'
        },
        {
          id: 2,
          date: '2024-01-16',
          type: '透析记录',
          content: '血液透析治疗，透析时间4小时，超滤量1.5L...',
          doctor: '王医师'
        },
        {
          id: 3,
          date: '2024-01-17',
          type: '检查报告',
          content: '血常规：红细胞计数偏低，血红蛋白85g/L...',
          doctor: '李医师'
        }
      ],
      
      recordsLoading: false,
      scoreDialogVisible: false,
      newScoreData: null
    }
  },
  
  created() {
    this.initPatientData()
  },
  
  methods: {
    // 初始化患者数据
    async initPatientData() {
      const patientId = this.$route.params.id
      if (patientId) {
        const response = await getOutpatientBloodInfo(patientId)
        if (response && response.code === 0 && response.data) {
          this.patientInfo = response.data
        }
      }
    },

    // 处理AI评分生成成功
    handleScoreGenerated(data) {
      this.newScoreData = data.scoreData
      this.scoreDialogVisible = true
    },

    // 处理查看评分报告
    handleViewScores(data) {
      this.newScoreData = null
      this.scoreDialogVisible = true
    },

    // 获取状态标签类型
    getStatusTagType(status) {
      const typeMap = {
        hospitalized: 'primary',
        discharged: 'success',
        emergency: 'danger'
      }
      return typeMap[status] || 'info'
    },

    // 获取状态标签
    getStatusLabel(status) {
      const labelMap = {
        hospitalized: '住院中',
        discharged: '已出院',
        emergency: '急诊'
      }
      return labelMap[status] || status
    },

    // 格式化日期
    formatDate(date) {
      if (!date) return '--'
      return new Date(date).toLocaleDateString('zh-CN')
    },

    // 编辑患者
    editPatient() {
      this.$router.push(`/patient/edit/${this.patientInfo.id}`)
    },

    // 刷新记录
    async refreshRecords() {
      this.recordsLoading = true
      try {
        // 这里可以调用API刷新住院记录
        // const response = await getMedicalRecords(this.patientInfo.id)
        // this.medicalRecords = response.data
        
        // 模拟异步操作
        await new Promise(resolve => setTimeout(resolve, 1000))
        this.$modal.msgSuccess('记录已刷新')
      } catch (error) {
        this.$modal.msgError('刷新失败')
      } finally {
        this.recordsLoading = false
      }
    },

    // 查看记录详情
    viewRecord(record) {
      this.$alert(record.content, `${record.type} - ${record.date}`, {
        confirmButtonText: '确定',
        type: 'info'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.patient-detail {
  padding: 20px;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .header-actions {
      display: flex;
      gap: 10px;
    }
  }

  .patient-info-card,
  .medical-records-card {
    margin-bottom: 20px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-weight: 600;
    }
  }

  .info-item {
    display: flex;
    align-items: center;
    margin-bottom: 12px;
    
    label {
      font-weight: 500;
      color: #666;
      min-width: 80px;
    }
    
    span {
      color: #2c3e50;
    }
  }
}

// 全局样式调整
::v-deep .el-card__header {
  background-color: #f8f9fa;
  border-bottom: 1px solid #e8e8e8;
}

::v-deep .el-breadcrumb {
  font-size: 14px;
  
  .el-breadcrumb__item {
    .el-breadcrumb__inner {
      color: #666;
      
      &:hover {
        color: #1890ff;
      }
    }
    
    &:last-child .el-breadcrumb__inner {
      color: #2c3e50;
      font-weight: 500;
    }
  }
}
</style> 