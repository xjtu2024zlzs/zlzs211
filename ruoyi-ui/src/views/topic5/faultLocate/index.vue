<template>
  <div class="app-container fault-locate-page">
    <!-- 页面标题 -->
    <el-card class="mb16" shadow="never">
      <template #header>
        <div class="card-header">
          <span>故障表征与故障零件定位</span>
        </div>
      </template>

      <el-alert
        title="本模块面向系统故障表征数据，融合数据驱动特征与机理先验信息，基于时空图神经网络实现故障零部件的智能检测与精准定位。"
        type="info"
        show-icon
        :closable="false"
      />
    </el-card>

    <!-- 第一区域：故障表征输入 -->
    <el-card class="mb16" shadow="never">
      <template #header>
        <div class="card-header">
          <span>一、系统故障表征输入</span>
        </div>
      </template>

      <el-form
        ref="caseFormRef"
        :model="caseForm"
        :rules="caseRules"
        label-width="130px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="案例名称" prop="caseName">
              <el-input
                v-model="caseForm.caseName"
                placeholder="请输入追溯案例名称"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="案例编号" prop="caseNo">
              <el-input
                v-model="caseForm.caseNo"
                placeholder="系统可自动生成，也可手动输入"
              />
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="所属系统" prop="systemName">
              <el-input
                v-model="caseForm.systemName"
                placeholder="如：起落架系统"
              />
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="所属子系统" prop="subsystemName">
              <el-input
                v-model="caseForm.subsystemName"
                placeholder="如：液压执行子系统"
              />
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="产品型号" prop="productModel">
              <el-input
                v-model="caseForm.productModel"
                placeholder="如：Aircraft-A"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="故障标题" prop="faultTitle">
              <el-input
                v-model="caseForm.faultTitle"
                placeholder="请输入故障标题"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="故障模式" prop="faultMode">
              <el-input
                v-model="caseForm.faultMode"
                placeholder="如：压力异常、裂纹、卡滞、间隙异常"
              />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="故障现象" prop="faultPhenomenon">
              <el-input
                v-model="caseForm.faultPhenomenon"
                type="textarea"
                :rows="3"
                placeholder="请输入系统出现的故障现象"
              />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="故障详细描述" prop="faultDesc">
              <el-input
                v-model="caseForm.faultDesc"
                type="textarea"
                :rows="4"
                placeholder="请输入故障发生过程、表现、影响范围等信息"
              />
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="故障发生位置" prop="faultLocation">
              <el-input
                v-model="caseForm.faultLocation"
                placeholder="如：主起落架左侧"
              />
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="异常参数" prop="abnormalParam">
              <el-input
                v-model="caseForm.abnormalParam"
                placeholder="如：液压压力、振动幅值、温度"
              />
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="异常参数值" prop="abnormalValue">
              <el-input
                v-model="caseForm.abnormalValue"
                placeholder="如：12MPa、0.8mm、85℃"
              />
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="正常范围" prop="normalRange">
              <el-input
                v-model="caseForm.normalRange"
                placeholder="如：15~18MPa"
              />
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="发现方式" prop="detectMethod">
              <el-input
                v-model="caseForm.detectMethod"
                placeholder="如：人工检查、传感器报警、地面试验"
              />
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="案例状态">
              <el-tag :type="caseStatusType(caseForm.caseStatus)">
                {{ caseStatusLabel(caseForm.caseStatus) }}
              </el-tag>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 第二区域：算法选择与运行 -->
    <el-card class="mb16" shadow="never">
      <template #header>
        <div class="card-header">
          <span>二、算法选择与故障零件定位</span>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form label-width="130px">
            <el-form-item label="定位算法">
              <el-select
                v-model="caseForm.locateAlgorithmId"
                placeholder="请选择故障零件定位算法"
                style="width: 100%"
                @change="handleAlgorithmChange"
              >
                <el-option
                  v-for="item in algorithmOptions"
                  :key="item.algorithmId"
                  :label="item.algorithmName"
                  :value="item.algorithmId"
                >
                  <span>{{ item.algorithmName }}</span>
                  <span class="algorithm-version">
                    {{ item.algorithmVersion }}
                  </span>
                </el-option>
              </el-select>
            </el-form-item>

            <el-form-item label="算法说明">
              <el-input
                v-model="selectedAlgorithmDesc"
                type="textarea"
                :rows="3"
                disabled
                placeholder="选择算法后显示算法说明"
              />
            </el-form-item>
          </el-form>
        </el-col>

        <el-col :span="12">
          <div class="operate-panel">
            <el-button
              type="primary"
              icon="Check"
              :loading="saveLoading"
              @click="handleSaveCase"
            >
              保存故障表征
            </el-button>

            <el-button
              type="success"
              icon="Cpu"
              :loading="locateLoading"
              :disabled="!caseForm.caseId"
              @click="handleRunLocate"
            >
              运行故障零件定位
            </el-button>

            <el-button
              icon="Refresh"
              @click="handleReset"
            >
              重置页面
            </el-button>
          </div>

          <el-divider />

          <el-descriptions
            title="当前定位结果"
            :column="1"
            border
          >
            <el-descriptions-item label="追溯案例ID">
              {{ caseForm.caseId || '尚未保存' }}
            </el-descriptions-item>

            <el-descriptions-item label="最终故障零件ID">
              <el-tag v-if="caseForm.finalPartId" type="danger">
                {{ caseForm.finalPartId }}
              </el-tag>
              <span v-else>尚未定位</span>
            </el-descriptions-item>

            <el-descriptions-item label="最终故障零件编号">
              {{ caseForm.finalPartNo || '尚未定位' }}
            </el-descriptions-item>

            <el-descriptions-item label="最终故障零件名称">
              {{ caseForm.finalPartName || '尚未定位' }}
            </el-descriptions-item>

            <el-descriptions-item label="定位置信度">
              <el-progress
                v-if="caseForm.locateConfidence !== null && caseForm.locateConfidence !== undefined"
                :percentage="toPercent(caseForm.locateConfidence)"
                :stroke-width="12"
              />
              <span v-else>尚未定位</span>
            </el-descriptions-item>
          </el-descriptions>
        </el-col>
      </el-row>
    </el-card>

    <!-- 第三区域：候选零件定位结果 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>三、候选故障零件定位结果</span>
          <el-button
            type="primary"
            link
            :disabled="!caseForm.caseId"
            @click="loadLocateResults"
          >
            刷新结果
          </el-button>
        </div>
      </template>

      <el-table
        v-loading="resultLoading"
        :data="locateResultList"
        border
      >
        <el-table-column
          label="排序"
          prop="rankNo"
          width="80"
          align="center"
        />

        <el-table-column
          label="零件ID"
          prop="partId"
          width="120"
          align="center"
        />

        <el-table-column
          label="零件编号"
          prop="partNo"
          min-width="140"
          align="center"
        />

        <el-table-column
          label="零件名称"
          prop="partName"
          min-width="160"
          align="center"
        />

        <el-table-column
          label="匹配分数"
          prop="matchScore"
          width="120"
          align="center"
        >
          <template #default="scope">
            {{ formatScore(scope.row.matchScore) }}
          </template>
        </el-table-column>

        <el-table-column
          label="置信度"
          prop="confidence"
          width="160"
          align="center"
        >
          <template #default="scope">
            <el-progress
              :percentage="toPercent(scope.row.confidence)"
              :stroke-width="10"
            />
          </template>
        </el-table-column>

        <el-table-column
          label="是否最终零件"
          prop="isFinal"
          width="120"
          align="center"
        >
          <template #default="scope">
            <el-tag
              v-if="scope.row.isFinal === '1'"
              type="danger"
            >
              最终定位
            </el-tag>
            <el-tag
              v-else
              type="info"
            >
              候选
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          label="定位原因"
          prop="locateReason"
          min-width="240"
          show-overflow-tooltip
        />

        <el-table-column
          label="证据摘要"
          prop="evidenceSummary"
          min-width="240"
          show-overflow-tooltip
        />
      </el-table>

      <el-empty
        v-if="!resultLoading && locateResultList.length === 0"
        description="暂无定位结果，请先保存故障表征并运行定位算法"
      />
    </el-card>
  </div>
</template>

<script setup name="FaultLocate">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

/**
 * 说明：
 * 这里直接在页面内部写 request，避免你的 api 文件函数名和代码生成器生成结果不一致。
 * 后续稳定后，也可以把这些接口封装到 src/api/topic5/faultLocate.js 中。
 */

const caseFormRef = ref(null)

const saveLoading = ref(false)
const locateLoading = ref(false)
const resultLoading = ref(false)

const algorithmOptions = ref([])
const locateResultList = ref([])
const selectedAlgorithmDesc = ref('')

const caseForm = reactive({
  caseId: null,
  caseNo: '',
  caseName: '',

  systemName: '',
  subsystemName: '',
  productModel: '',

  faultTitle: '',
  faultPhenomenon: '',
  faultDesc: '',
  faultLocation: '',
  faultMode: '',

  abnormalParam: '',
  abnormalValue: '',
  normalRange: '',
  detectMethod: '',

  locateAlgorithmId: null,
  locateAlgorithmName: '',

  finalPartId: null,
  finalPartNo: '',
  finalPartName: '',
  locateConfidence: null,

  graphId: null,
  rootResultId: null,
  caseStatus: '0'
})

const caseRules = {
  caseName: [
    { required: true, message: '请输入案例名称', trigger: 'blur' }
  ],
  faultTitle: [
    { required: true, message: '请输入故障标题', trigger: 'blur' }
  ],
  faultPhenomenon: [
    { required: true, message: '请输入故障现象', trigger: 'blur' }
  ],
  faultDesc: [
    { required: true, message: '请输入故障详细描述', trigger: 'blur' }
  ],
  locateAlgorithmId: [
    { required: true, message: '请选择定位算法', trigger: 'change' }
  ]
}

onMounted(() => {
  initCaseNo()
  loadAlgorithmOptions()
})

/**
 * 初始化案例编号
 */
function initCaseNo() {
  if (!caseForm.caseNo) {
    const now = new Date()
    const y = now.getFullYear()
    const m = String(now.getMonth() + 1).padStart(2, '0')
    const d = String(now.getDate()).padStart(2, '0')
    const h = String(now.getHours()).padStart(2, '0')
    const mi = String(now.getMinutes()).padStart(2, '0')
    const s = String(now.getSeconds()).padStart(2, '0')
    caseForm.caseNo = `TC${y}${m}${d}${h}${mi}${s}`
  }
}

/**
 * 加载算法下拉框
 */
function loadAlgorithmOptions() {
  request({
    url: '/topic5/algorithmConfig/list',
    method: 'get',
    params: {
      algorithmType: 'locate',
      status: '0',
      pageNum: 1,
      pageSize: 100
    }
  }).then(res => {
    algorithmOptions.value = res.rows || []

    const defaultAlgorithm = algorithmOptions.value.find(item => item.defaultFlag === '1')
    if (defaultAlgorithm && !caseForm.locateAlgorithmId) {
      caseForm.locateAlgorithmId = defaultAlgorithm.algorithmId
      caseForm.locateAlgorithmName = defaultAlgorithm.algorithmName
      selectedAlgorithmDesc.value = defaultAlgorithm.algorithmDesc || ''
    }
  })
}

/**
 * 选择算法
 */
function handleAlgorithmChange(algorithmId) {
  const item = algorithmOptions.value.find(a => a.algorithmId === algorithmId)
  if (item) {
    caseForm.locateAlgorithmName = item.algorithmName
    selectedAlgorithmDesc.value = item.algorithmDesc || ''
  }
}

/**
 * 保存故障表征案例
 */
function handleSaveCase() {
  caseFormRef.value.validate(valid => {
    if (!valid) {
      return
    }

    saveLoading.value = true

    const data = {
      ...caseForm
    }

    const req = caseForm.caseId
      ? request({
          url: '/topic5/traceCase',
          method: 'put',
          data
        })
      : request({
          url: '/topic5/traceCase',
          method: 'post',
          data
        })

    req.then(res => {
      ElMessage.success(caseForm.caseId ? '故障表征更新成功' : '故障表征保存成功')

      /**
       * 若依新增接口通常只返回 AjaxResult，不一定返回新增ID。
       * 如果后端 add 方法没有返回 caseId，这里会拿不到 ID。
       * 建议后端新增接口返回新建对象或新建ID。
       */
      if (res.data && res.data.caseId) {
        Object.assign(caseForm, res.data)
      } else if (res.data && typeof res.data === 'number') {
        caseForm.caseId = res.data
      } else if (!caseForm.caseId) {
        ElMessage.warning('案例已保存，但前端未获取到 caseId。建议修改后端新增接口返回 caseId。')
      }
    }).finally(() => {
      saveLoading.value = false
    })
  })
}

/**
 * 运行故障零件定位算法
 */
function handleRunLocate() {
  if (!caseForm.caseId) {
    ElMessage.warning('请先保存故障表征，获取追溯案例ID')
    return
  }

  if (!caseForm.locateAlgorithmId) {
    ElMessage.warning('请选择故障零件定位算法')
    return
  }

  locateLoading.value = true

  request({
    url: `/topic5/traceCase/locatePart/${caseForm.caseId}`,
    method: 'post',
    params: {
      algorithmId: caseForm.locateAlgorithmId
    }
  }).then(res => {
    ElMessage.success('故障零件定位完成')

    /**
     * 建议后端 locatePart 接口返回更新后的 t5_trace_case。
     * 如果没有返回，则通过 getTraceCase 再查一次。
     */
    if (res.data && res.data.caseId) {
      Object.assign(caseForm, res.data)
      loadLocateResults()
    } else {
      loadCaseInfo()
      loadLocateResults()
    }
  }).finally(() => {
    locateLoading.value = false
  })
}

/**
 * 查询案例详情
 */
function loadCaseInfo() {
  if (!caseForm.caseId) {
    return
  }

  request({
    url: `/topic5/traceCase/${caseForm.caseId}`,
    method: 'get'
  }).then(res => {
    if (res.data) {
      Object.assign(caseForm, res.data)
    }
  })
}

/**
 * 查询候选零件定位结果
 */
function loadLocateResults() {
  if (!caseForm.caseId) {
    return
  }

  resultLoading.value = true

  request({
    url: '/topic5/partLocateResult/list',
    method: 'get',
    params: {
      caseId: caseForm.caseId,
      pageNum: 1,
      pageSize: 100
    }
  }).then(res => {
    locateResultList.value = res.rows || []
  }).finally(() => {
    resultLoading.value = false
  })
}

/**
 * 重置页面
 */
function handleReset() {
  Object.assign(caseForm, {
    caseId: null,
    caseNo: '',
    caseName: '',

    systemName: '',
    subsystemName: '',
    productModel: '',

    faultTitle: '',
    faultPhenomenon: '',
    faultDesc: '',
    faultLocation: '',
    faultMode: '',

    abnormalParam: '',
    abnormalValue: '',
    normalRange: '',
    detectMethod: '',

    locateAlgorithmId: null,
    locateAlgorithmName: '',

    finalPartId: null,
    finalPartNo: '',
    finalPartName: '',
    locateConfidence: null,

    graphId: null,
    rootResultId: null,
    caseStatus: '0'
  })

  locateResultList.value = []
  selectedAlgorithmDesc.value = ''
  initCaseNo()

  const defaultAlgorithm = algorithmOptions.value.find(item => item.defaultFlag === '1')
  if (defaultAlgorithm) {
    caseForm.locateAlgorithmId = defaultAlgorithm.algorithmId
    caseForm.locateAlgorithmName = defaultAlgorithm.algorithmName
    selectedAlgorithmDesc.value = defaultAlgorithm.algorithmDesc || ''
  }
}

/**
 * 状态标签
 */
function caseStatusLabel(status) {
  const map = {
    '0': '待定位',
    '1': '已定位零件',
    '2': '已构建图谱',
    '3': '已完成根因分析',
    '4': '失败'
  }
  return map[status] || '未知'
}

function caseStatusType(status) {
  const map = {
    '0': 'info',
    '1': 'success',
    '2': 'warning',
    '3': 'danger',
    '4': 'danger'
  }
  return map[status] || 'info'
}

/**
 * 分数显示
 */
function toPercent(value) {
  if (value === null || value === undefined || value === '') {
    return 0
  }

  const num = Number(value)
  if (Number.isNaN(num)) {
    return 0
  }

  if (num <= 1) {
    return Math.round(num * 100)
  }

  return Math.round(num)
}

function formatScore(value) {
  if (value === null || value === undefined || value === '') {
    return '-'
  }
  const num = Number(value)
  if (Number.isNaN(num)) {
    return value
  }
  return num.toFixed(4)
}
</script>

<style scoped>
.fault-locate-page {
  padding: 16px;
}

.mb16 {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}

.operate-panel {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.algorithm-version {
  float: right;
  color: #8492a6;
  font-size: 13px;
}
</style>