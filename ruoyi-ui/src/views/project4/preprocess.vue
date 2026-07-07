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
            <el-button class="main-action" type="primary" @click="submit" :loading="loading">执行预处理</el-button>
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
            <el-tag :type="result ? 'success' : 'info'" effect="plain">{{ result ? '已完成' : '待执行' }}</el-tag>
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
        <el-input class="result-textarea" :model-value="JSON.stringify(result, null, 2)" type="textarea" rows="8" readonly />
      </el-card>
    </div>
  </div>
</template>

<script>
import { runPreprocess, listCwruFiles } from '@/api/project4/preprocess'
export default {
  name: "Preprocess",
  data() {
    return {
      loading: false,
      fileOptionsLoading: false,
      fileOptions: [],
      result: null,
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
  },
  methods: {
    async loadFileOptions() {
      this.fileOptionsLoading = true
      try {
        const res = await listCwruFiles()
        if (res.code === 200) {
          const list = res.data || []

          this.fileOptions = list.map(item => {
            const keyNum = Number(item.fileName || item.file_name || item.keyNum || item.key_num)
            return {
              keyNum,
              label: `${keyNum}`
            }
          }).filter(item => item.keyNum)

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
              hasNormFe: !!preprocessResult.norm_fe
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
