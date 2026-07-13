<template>
  <div class="project4-page">
    <div class="hero-card">
      <div>
        <div class="hero-subtitle">航空液压管路 · 数据预处理</div>
        <div class="hero-title">航空设备液压管路信号预处理</div>
        <div class="hero-desc">
          从航空设备液压管路故障样本库中选择具体样本，完成滑窗切片、去噪处理与归一化处理，为数据增强、特征融合和故障诊断提供标准化输入。
        </div>
      </div>
      <div class="hero-status">
        <el-tag effect="light" type="success">系统在线</el-tag>
        <el-tag effect="light" type="primary">实时采集</el-tag>
        <el-tag effect="light" type="info">预处理链路</el-tag>
      </div>
    </div>

    <!-- 当前质量问题显示区域：只显示最新一次分派任务，不判断课题五报告 -->
    <el-card
      class="quality-task-card"
      shadow="hover"
      v-loading="qualityTaskLoading"
    >
      <template #header>
        <div class="quality-card-header">
          <div>
            <div class="current-quality-title">当前质量问题</div>
            <div class="current-quality-subtitle">
              接收质量问题管理中心最新分派至{{ moduleName }}的处理任务
            </div>
          </div>

          <div class="current-quality-header-actions">
            <el-tag type="warning">
              质量中心分派
            </el-tag>

            <el-button
              size="small"
              type="primary"
              plain
              @click="loadCurrentQualityTask"
            >
              刷新任务
            </el-button>
          </div>
        </div>
      </template>

      <el-empty
        v-if="!currentQualityTask"
        description="暂无质量问题管理中心分派给本模块的处理中任务"
        :image-size="90"
      />

      <template v-else>
        <el-descriptions
          :column="2"
          border
          class="quality-descriptions"
        >
          <el-descriptions-item label="问题编号">
            {{ currentQualityTask.problemCode || '-' }}
          </el-descriptions-item>

          <el-descriptions-item label="问题标题">
            {{ currentQualityTask.problemTitle || '-' }}
          </el-descriptions-item>

          <el-descriptions-item label="产品型号">
            {{ currentQualityTask.productModel || '-' }}
          </el-descriptions-item>

          <el-descriptions-item label="涉及系统">
            {{ currentQualityTask.involvedSystem || '-' }}
          </el-descriptions-item>

          <el-descriptions-item label="发生部件">
            {{ currentQualityTask.occurPart || '-' }}
          </el-descriptions-item>

          <el-descriptions-item label="部件编号">
            {{ currentQualityTask.componentCode || '-' }}
          </el-descriptions-item>

          <el-descriptions-item label="问题描述" :span="2">
            <div class="description-in-table">
              {{ currentQualityTask.description || '暂无问题描述' }}
            </div>
          </el-descriptions-item>
        </el-descriptions>

        <div class="quality-task-actions">
          <el-button
            type="success"
            :loading="finishQualityTaskLoading"
            :disabled="!currentQualityTask"
            @click="finishCurrentQualityTask"
          >
            完成任务并回填结果
          </el-button>
        </div>
      </template>
    </el-card>

    <div class="metric-grid">
      <div class="metric-card">
        <div class="metric-label">当前管路样本</div>
        <div class="metric-value">{{ form.keyNum || '-' }}</div>
        <div class="metric-foot">样本编号</div>
      </div>
      <div class="metric-card">
        <div class="metric-label">滑窗长度</div>
        <div class="metric-value">{{ form.winLength }}</div>
        <div class="metric-foot">winLength</div>
      </div>
      <div class="metric-card">
        <div class="metric-label">去噪状态</div>
        <div class="metric-value">{{ form.denoise ? '开启' : '关闭' }}</div>
        <div class="metric-foot">{{ form.denoiseMode }}</div>
      </div>
      <div class="metric-card">
        <div class="metric-label">归一化状态</div>
        <div class="metric-value">{{ form.normalize ? '开启' : '关闭' }}</div>
        <div class="metric-foot">{{ form.normalizeMode }}</div>
      </div>
    </div>

    <div class="content-grid">
      <el-card class="panel-card" shadow="never">
        <template #header>
          <div class="card-header">
            <div>
              <div class="card-kicker">参数配置</div>
              <div class="card-title">预处理任务设置</div>
            </div>
            <el-tag type="primary" effect="plain">Step 1</el-tag>
          </div>
        </template>

        <el-form ref="form" :model="form" label-width="140px" class="nice-form">
          <el-form-item label="从数字卷宗选取样本">
            <el-select
              v-model="form.keyNum"
              placeholder="请选择液压管路样本"
              filterable
              clearable
              :loading="fileOptionsLoading"
              style="width: 100%"
              @change="handleKeyNumChange"
            >
              <el-option
                v-for="item in fileOptions"
                :key="item.keyNum"
                :label="item.label"
                :value="item.keyNum"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="滑窗长度">
            <el-input-number v-model="form.winLength" :step="256" />
          </el-form-item>
          <el-form-item label="开启去噪">
            <el-switch v-model="form.denoise" active-text="开启" inactive-text="关闭" />
          </el-form-item>
          <el-form-item label="去噪模式">
            <el-select v-model="form.denoiseMode" style="width: 100%">
              <el-option label="高斯 gaussian" value="gaussian" />
            </el-select>
          </el-form-item>
          <el-form-item label="开启归一化">
            <el-switch v-model="form.normalize" active-text="开启" inactive-text="关闭" />
          </el-form-item>
          <el-form-item label="归一化模式">
            <el-select v-model="form.normalizeMode" style="width: 100%">
              <el-option label="zscore" value="zscore" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button
              class="main-action"
              type="primary"
              @click="submit"
              :loading="loading"
            >
              执行预处理
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card class="panel-card" shadow="never">
        <template #header>
          <div class="card-header">
            <div>
              <div class="card-kicker">任务链路</div>
              <div class="card-title">预处理执行流程</div>
            </div>
            <el-tag :type="result ? 'success' : 'info'" effect="plain">
              {{ result ? '已完成' : '待执行' }}
            </el-tag>
          </div>
        </template>
        <div class="flow-list">
          <div class="flow-item active">
            <div class="flow-index">1</div>
            <div><div class="flow-title">选择航空液压管路样本</div><div class="flow-desc">从数据库样本列表中选择具体液压管路样本编号。</div></div>
          </div>
          <div class="flow-item">
            <div class="flow-index">2</div>
            <div><div class="flow-title">后端读取样本数据</div><div class="flow-desc">Java 根据样本编号获取样本数据路径，并交由 Python 读取 .mat 信号文件。</div></div>
          </div>
          <div class="flow-item">
            <div class="flow-index">3</div>
            <div><div class="flow-title">信号预处理</div><div class="flow-desc">完成滑窗、去噪与归一化，生成两路标准化管路监测信号。</div></div>
          </div>
          <div class="flow-item">
            <div class="flow-index">4</div>
            <div><div class="flow-title">结果入库与缓存</div><div class="flow-desc">结果保存后自动供数据增强、特征融合和故障诊断页面调用。</div></div>
          </div>
        </div>
      </el-card>
    </div>

    <div v-if="result" class="result-grid">
      <el-card class="panel-card" shadow="never">
        <template #header>
          <div class="card-header">
            <div>
              <div class="card-kicker">执行结果</div>
              <div class="card-title">预处理摘要</div>
            </div>
            <el-tag type="success" effect="plain">{{ result.status }}</el-tag>
          </div>
        </template>
        <div class="summary-grid">
          <div class="summary-item"><span>结果ID</span><strong>{{ result.id }}</strong></div>
          <div class="summary-item"><span>源样本ID</span><strong>{{ result.sourceId }}</strong></div>
          <div class="summary-item"><span>管路信号A</span><strong>{{ result.hasNormDe ? '已生成' : '未生成' }}</strong></div>
          <div class="summary-item"><span>管路信号B</span><strong>{{ result.hasNormFe ? '已生成' : '未生成' }}</strong></div>
        </div>

        <el-input
          class="result-textarea"
          :model-value="JSON.stringify(result, null, 2)"
          type="textarea"
          rows="8"
          readonly
        />
      </el-card>
    </div>
  </div>
</template>

<script>
import { runPreprocess, listCwruFiles } from '@/api/project4/preprocess'
import { listTask, updateTask } from '@/api/quality/task'
import { getProblem, updateProblem } from '@/api/quality/problem'
import { addLog } from '@/api/quality/log'

const MODULE_CODE = 'PROJECT_4'
const MODULE_NAME = '智能故障诊断与根源性分析技术'

const QMS_TASK_EVENT_NAME = 'qms-current-task-change'
const QMS_TASK_EVENT_KEY = 'qms_current_task_change'

const QMS_FLOW_EVENT_NAME = 'qms-flow-change'
const QMS_FLOW_EVENT_KEY = 'qms_flow_change'

export default {
  name: "Preprocess",
  data() {
    return {
      moduleName: MODULE_NAME,

      loading: false,
      fileOptionsLoading: false,
      fileOptions: [],
      result: null,

      faultInfo: null,

      currentQualityTask: null,
      qualityTaskLoading: false,
      finishQualityTaskLoading: false,

      form: {
        keyNum: Number(localStorage.getItem('project4_key_num')) || null,
        winLength: 1024,
        denoise: true,
        denoiseMode: "gaussian",
        normalize: true,
        normalizeMode: "zscore"
      }
    }
  },

  mounted() {
    this.loadFileOptions()
    this.loadCurrentQualityTask()

    window.addEventListener(QMS_TASK_EVENT_NAME, this.handleQmsTaskChange)
    window.addEventListener('storage', this.handleStorageChange)
    window.addEventListener('focus', this.loadCurrentQualityTask)
  },

  beforeUnmount() {
    window.removeEventListener(QMS_TASK_EVENT_NAME, this.handleQmsTaskChange)
    window.removeEventListener('storage', this.handleStorageChange)
    window.removeEventListener('focus', this.loadCurrentQualityTask)
  },

  methods: {
            async loadFileOptions() {
              this.fileOptionsLoading = true

              try {
                const res = await listCwruFiles()

                if (res.code === 200) {
                  const list = res.data || []

                  this.fileOptions = list
                    .map(item => {
                      const keyNum = Number(
                        item.fileName ||
                        item.file_name ||
                        item.keyNum ||
                        item.key_num
                      )

                      return {
                        keyNum,
                        // 下拉框只显示最前面的样本编号
                        label: String(keyNum)
                      }
                    })
                    .filter(item => item.keyNum)

                  if (!this.form.keyNum && this.fileOptions.length > 0) {
                    this.form.keyNum = this.fileOptions[0].keyNum
                  }
                } else {
                  this.$modal.msgError(res.msg || "液压管路样本列表加载失败")
                }
              } catch (err) {
                console.error(err)
                this.$modal.msgError("液压管路样本列表加载失败：" + err.message)
              } finally {
                this.fileOptionsLoading = false
              }
            },
    handleKeyNumChange(value) {
      if (value) {
        localStorage.setItem('project4_key_num', String(value))
      }
    },
    async submit() {
      if (!this.form.keyNum || this.form.keyNum <= 0) {
        return this.$modal.msgError("请选择液压管路样本")
      }

      if (!this.form.keyNum || this.form.keyNum <= 0) {
        return this.$modal.msgError("液压管路样本编号必须大于0，例如108")
      }

      this.loading = true

      try {
        const payload = {
          key_num: this.form.keyNum,
          win_length: this.form.winLength,
          denoise: this.form.denoise,
          denoise_mode: this.form.denoiseMode,
          normalize: this.form.normalize,
          normalize_mode: this.form.normalizeMode
        }

        console.log("preprocess payload =", payload)

        const res = await runPreprocess(payload)

        if (res.code === 200) {
          this.$modal.msgSuccess("执行成功")
          localStorage.setItem('project4_key_num', String(this.form.keyNum))
          localStorage.setItem('project4_preprocess_id', String(res.data.id))

          let preprocessResult = null

          try {
            // Java 返回的 res.data 是数据库记录，真正的 Python 结果在 bizResult 里
            const pythonResponse = typeof res.data.bizResult === 'string'
              ? JSON.parse(res.data.bizResult)
              : res.data.bizResult

            // Python 统一返回格式一般是 { code: 200, msg: "操作成功", data: {...} }
            preprocessResult = pythonResponse && pythonResponse.data
              ? pythonResponse.data
              : pythonResponse

            const faultInfo =
              preprocessResult?.fault_info ||
              preprocessResult?.faultInfo ||
              pythonResponse?.fault_info ||
              pythonResponse?.faultInfo ||
              res.data?.fault_info ||
              res.data?.faultInfo ||
              null

            this.faultInfo = faultInfo

            if (faultInfo) {
              localStorage.setItem(
                'project4_fault_info',
                typeof faultInfo === 'string'
                  ? faultInfo
                  : JSON.stringify(faultInfo, null, 2)
              )
            } else {
              localStorage.removeItem('project4_fault_info')
              console.warn('FastAPI 返回结果中未检测到 fault_info 字段，当前返回内容：', pythonResponse)
            }

            if (!preprocessResult || !preprocessResult.norm_de || !preprocessResult.norm_fe) {
              console.error("预处理结果解析失败，当前内容：", preprocessResult)
              this.$modal.msgError("预处理成功，但结果中缺少标准化管路信号A或管路信号B")
              return
            }

            // 保存给数据增强、特征融合页面使用
            localStorage.setItem('project4_preprocess_id', String(res.data.id))
            localStorage.setItem('project4_preprocess_result', JSON.stringify(preprocessResult, null, 2))

            // 页面只显示摘要，不直接展示超大数组，避免卡死
            this.result = {
              id: res.data.id,
              sourceId: res.data.sourceId,
              status: res.data.status,
              createTime: res.data.createTime,
              updateTime: res.data.updateTime,
              message: "预处理已完成，完整结果已保存，可用于数据增强、特征融合和故障诊断",
              hasNormDe: !!preprocessResult.norm_de,
              hasNormFe: !!preprocessResult.norm_fe,

              faultInfo: faultInfo
            }
          } catch (e) {
            console.error("解析预处理结果失败：", e)
            this.$modal.msgError("预处理成功，但解析 bizResult 失败")
          }
        } else {
          this.$modal.msgError(res.msg)
        }
      } catch (err) {
        console.error(err)
        this.$modal.msgError("接口调用失败：" + err.message)
      } finally {
        this.loading = false
      }
    },

    getNowTime() {
      const now = new Date()
      const y = now.getFullYear()
      const m = String(now.getMonth() + 1).padStart(2, '0')
      const d = String(now.getDate()).padStart(2, '0')
      const h = String(now.getHours()).padStart(2, '0')
      const min = String(now.getMinutes()).padStart(2, '0')
      const s = String(now.getSeconds()).padStart(2, '0')

      return `${y}-${m}-${d} ${h}:${min}:${s}`
    },

    getTimeValue(row) {
      const time =
        row.dispatchTime ||
        row.createTime ||
        row.submitTime ||
        row.updateTime ||
        row.confirmTime

      return time ? new Date(time).getTime() : 0
    },

    getLatestRow(rows) {
      if (!Array.isArray(rows) || rows.length === 0) {
        return null
      }

      return rows
        .slice()
        .sort((a, b) => {
          const timeCompare = this.getTimeValue(b) - this.getTimeValue(a)

          if (timeCompare !== 0) {
            return timeCompare
          }

          return Number(b.taskId || 0) - Number(a.taskId || 0)
        })[0]
    },

    normalizeQualityTask(item) {
      return {
        ...item,
        taskId: item.taskId,
        problemId: item.problemId,
        problemCode: item.problemCode || '',
        moduleCode: item.moduleCode || MODULE_CODE,
        moduleName: item.moduleName || MODULE_NAME,
        taskStatus: item.taskStatus || '',
        dispatchOpinion: item.dispatchOpinion || '',
        processResult: item.processResult || '',
        processFile: item.processFile || '',
        dispatchTime: item.dispatchTime || item.createTime || '',
        createTime: item.createTime || '',
        dispatchUserName: item.dispatchUserName || '',
        submitTime: item.submitTime || '',

        problemTitle: '',
        productModel: '',
        involvedSystem: '',
        occurPart: '',
        componentCode: '',
        description: ''
      }
    },

    async loadQualityProblemInfo(task) {
      if (!task || !task.problemId) {
        return task
      }

      try {
        const problemRes = await getProblem(task.problemId)
        const problem = problemRes?.data || {}

        return {
          ...task,
          problemTitle: problem.problemTitle || problem.title || '',
          productModel: problem.productModel || '',
          involvedSystem: problem.involvedSystem || '',
          occurPart: problem.occurPart || '',
          componentCode: problem.componentCode || '',
          description: problem.description || ''
        }
      } catch (error) {
        console.error('加载质量问题填报信息失败：', error)
        return task
      }
    },

    async loadCurrentQualityTask() {
      this.qualityTaskLoading = true

      try {
        const res = await listTask({
          moduleCode: MODULE_CODE,
          taskStatus: 'PROCESSING'
        })

        const rows = Array.isArray(res?.rows)
          ? res.rows
          : Array.isArray(res?.data)
            ? res.data
            : []

        const latestTask = this.getLatestRow(rows.map((item) => this.normalizeQualityTask(item)))

        if (!latestTask) {
          this.currentQualityTask = null
          return
        }

        this.currentQualityTask = await this.loadQualityProblemInfo(latestTask)
      } catch (error) {
        console.error('加载智能故障诊断与根源性分析当前质量问题失败：', error)
        this.currentQualityTask = null
      } finally {
        this.qualityTaskLoading = false
      }
    },

    buildProject4ReturnResult() {
      const diagnosisInfoText = localStorage.getItem('project4_diagnosis_info') || ''

      if (!diagnosisInfoText) {
        throw new Error('请先进入故障诊断页面并执行故障诊断算法')
      }

      const diagnosis = JSON.parse(diagnosisInfoText)

      return `故障诊断结果：${diagnosis.fault_full_name || '-'}；故障简称：${diagnosis.fault_abbr || '-'}；故障标签：${diagnosis.label ?? '-'}；故障尺寸：${diagnosis.fault_size_inch ?? '-'} inch。`
    },

    buildTaskPayload(task, override = {}) {
      const merged = {
        ...task,
        ...override
      }

      return {
        taskId: merged.taskId,
        problemId: merged.problemId,
        problemCode: merged.problemCode,
        moduleCode: merged.moduleCode || MODULE_CODE,
        moduleName: merged.moduleName || MODULE_NAME,
        taskStatus: merged.taskStatus,
        dispatchOpinion: merged.dispatchOpinion || '',
        processResult: merged.processResult || '',
        processFile: merged.processFile || '',
        dispatchUserId: merged.dispatchUserId,
        dispatchUserName: merged.dispatchUserName,
        dispatchTime: merged.dispatchTime,
        submitUserId: merged.submitUserId,
        submitUserName: merged.submitUserName,
        submitTime: merged.submitTime,
        confirmUserId: merged.confirmUserId,
        confirmUserName: merged.confirmUserName,
        confirmOpinion: merged.confirmOpinion,
        confirmTime: merged.confirmTime,
        createBy: merged.createBy,
        createTime: merged.createTime,
        updateBy: merged.updateBy,
        updateTime: merged.updateTime,
        delFlag: merged.delFlag || '0'
      }
    },

    notifyQmsFlowChanged(payload = {}) {
      const eventData = {
        moduleCode: MODULE_CODE,
        problemId: payload.problemId || '',
        problemCode: payload.problemCode || '',
        taskId: payload.taskId || '',
        action: payload.action || 'SUBMIT',
        time: Date.now()
      }

      window.dispatchEvent(
        new CustomEvent(QMS_FLOW_EVENT_NAME, {
          detail: eventData
        })
      )

      localStorage.setItem(QMS_FLOW_EVENT_KEY, JSON.stringify(eventData))
    },

    async finishCurrentQualityTask() {
      if (!this.currentQualityTask) {
        this.$modal.msgWarning('当前没有需要处理的质量问题')
        return
      }

      this.finishQualityTaskLoading = true

      const now = this.getNowTime()
      const task = this.currentQualityTask

      try {
        const returnResult = this.buildProject4ReturnResult()

        await updateTask(
          this.buildTaskPayload(task, {
            taskStatus: 'SUBMITTED',
            processResult: returnResult,
            submitTime: now
          })
        )

        localStorage.removeItem('project4_diagnosis_info')

        if (this.form && this.form.keyNum) {
          localStorage.removeItem(`project4_diagnosis_info_key_${this.form.keyNum}`)
        }

        await updateProblem({
          problemId: task.problemId,
          problemCode: task.problemCode,
          status: 'WAIT_CONFIRM',
          currentModuleCode: '',
          currentModuleName: ''
        })

        await addLog({
          problemId: task.problemId,
          problemCode: task.problemCode,
          taskId: task.taskId,
          actionType: 'SUBMIT',
          actionName: '故障诊断与根源性分析任务完成',
          operatorName: MODULE_NAME,
          fromStatus: 'PROCESSING',
          toStatus: 'WAIT_CONFIRM',
          actionContent: `${MODULE_NAME}已完成结果回填。fault_info：${returnResult}`,
          createTime: now
        })

        this.notifyQmsFlowChanged({
          problemId: task.problemId,
          problemCode: task.problemCode,
          taskId: task.taskId,
          action: 'SUBMIT'
        })

        await this.loadCurrentQualityTask()

        this.$modal.msgSuccess('已回填至质量问题管理中心')
      } catch (error) {
        console.error('课题四 fault_info 回填失败：', error)

        const realMsg =
          error?.response?.data?.msg ||
          error?.data?.msg ||
          error?.msg ||
          error?.message ||
          String(error)

        this.$modal.msgError(`结果回填失败：${realMsg}`)
      } finally {
        this.finishQualityTaskLoading = false
      }
    },

    handleQmsTaskChange(event) {
      const data = event.detail || {}

      if (data.moduleCode === MODULE_CODE) {
        this.loadCurrentQualityTask()
      }
    },

    handleStorageChange(event) {
      if (event.key !== QMS_TASK_EVENT_KEY || !event.newValue) {
        return
      }

      try {
        const data = JSON.parse(event.newValue)

        if (data.moduleCode === MODULE_CODE) {
          this.loadCurrentQualityTask()
        }
      } catch (error) {
        console.error('解析质量问题分派事件失败：', error)
      }
    }
  }
}
</script>

<style scoped>
.project4-page {
  min-height: calc(100vh - 84px);
  padding: 18px;
  background: #f4f8ff;
}

.hero-card {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  padding: 26px 28px;
  margin-bottom: 18px;
  border: 1px solid #e4eefc;
  border-radius: 18px;
  background: linear-gradient(135deg, #ffffff 0%, #eef6ff 100%);
  box-shadow: 0 8px 24px rgba(50, 102, 180, 0.08);
}

.hero-subtitle {
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 700;
  color: #3678d8;
}

.hero-title {
  font-size: 26px;
  font-weight: 800;
  color: #172b4d;
}

.hero-desc {
  max-width: 900px;
  margin-top: 10px;
  line-height: 1.7;
  font-size: 14px;
  color: #5f6f89;
}

.hero-status {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.quality-task-card {
  margin-bottom: 18px;
  border-left: 4px solid #e6a23c;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(50, 102, 180, 0.06);
}

.quality-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.current-quality-title {
  font-size: 16px;
  font-weight: 800;
  color: #24364f;
}

.current-quality-subtitle {
  margin-top: 4px;
  font-size: 13px;
  color: #909399;
}

.current-quality-header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.quality-descriptions {
  margin-bottom: 14px;
}

.description-in-table {
  line-height: 1.8;
  white-space: pre-wrap;
  color: #303133;
}

.quality-task-actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 18px;
}

.metric-card {
  padding: 18px 20px;
  border: 1px solid #e4eefc;
  border-radius: 14px;
  background: #ffffff;
  box-shadow: 0 6px 18px rgba(50, 102, 180, 0.06);
}

.metric-label {
  font-size: 13px;
  color: #6b7c93;
}

.metric-value {
  margin-top: 12px;
  font-size: 26px;
  font-weight: 800;
  color: #102a54;
  word-break: break-all;
}

.metric-foot {
  margin-top: 8px;
  font-size: 12px;
  color: #7d8da6;
}

.content-grid {
  display: grid;
  grid-template-columns: 1.35fr 0.9fr;
  gap: 18px;
}

.result-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 18px;
  margin-top: 18px;
}

.panel-card {
  border: 1px solid #e4eefc;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(50, 102, 180, 0.06);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.card-kicker {
  margin-bottom: 4px;
  font-size: 12px;
  font-weight: 700;
  color: #4a83dc;
}

.card-title {
  font-size: 17px;
  font-weight: 800;
  color: #24364f;
}

.nice-form {
  padding-top: 8px;
}

.main-action {
  min-width: 130px;
  height: 38px;
  border-radius: 10px;
}

.flow-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.flow-item {
  display: flex;
  gap: 14px;
  padding: 14px;
  border: 1px solid #edf2fb;
  border-radius: 14px;
  background: #f9fbff;
}

.flow-item.active {
  border-color: #9fc4ff;
  background: #eef6ff;
}

.flow-index {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-weight: 800;
  color: #ffffff;
  background: #4d8df7;
}

.flow-title {
  font-size: 14px;
  font-weight: 800;
  color: #273b59;
}

.flow-desc {
  margin-top: 4px;
  line-height: 1.6;
  font-size: 13px;
  color: #708199;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.summary-item {
  padding: 14px;
  border-radius: 12px;
  background: #f6f9ff;
  border: 1px solid #e7effc;
}

.summary-item span {
  display: block;
  font-size: 12px;
  color: #75859b;
}

.summary-item strong {
  display: block;
  margin-top: 8px;
  font-size: 18px;
  color: #172b4d;
  word-break: break-all;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.image-card {
  padding: 14px;
  border: 1px solid #e7effc;
  border-radius: 14px;
  background: #fbfdff;
}

.image-title {
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 800;
  color: #273b59;
}

.image-card img {
  display: block;
  width: 100%;
  max-width: 100%;
  border: 1px solid #d9e6fb;
  border-radius: 12px;
  background: #ffffff;
}

.result-textarea :deep(.el-textarea__inner) {
  border-radius: 12px;
  background: #fbfdff;
  font-family: Consolas, Monaco, monospace;
}

@media (max-width: 1200px) {
  .metric-grid,
  .summary-grid,
  .image-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .content-grid {
    grid-template-columns: 1fr;
  }

  .hero-card {
    flex-direction: column;
  }

  .hero-status {
    justify-content: flex-start;
  }
}

@media (max-width: 768px) {
  .project4-page {
    padding: 12px;
  }

  .metric-grid,
  .summary-grid,
  .image-grid {
    grid-template-columns: 1fr;
  }

  .hero-title {
    font-size: 22px;
  }
}
</style>