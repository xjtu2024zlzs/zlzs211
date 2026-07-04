<template>
  <div class="project4-page">
    <div class="hero-card">
      <div>
        <div class="hero-subtitle">轴承算法 · 特征融合</div>
        <div class="hero-title">双模态特征融合</div>
        <div class="hero-desc">
          面向 DE 与 FE 双通道振动信息，设置融合权重并执行联合特征构建，提升故障表征的稳定性和可解释性。
        </div>
      </div>
      <div class="hero-status"><el-tag effect="light" type="success">融合链路</el-tag><el-tag effect="light" type="primary">DE + FE</el-tag><el-tag effect="light" type="info">特征图谱</el-tag></div>
    </div>

    <div class="metric-grid">
      <div class="metric-card"><div class="metric-label">上游预处理ID</div><div class="metric-value">{{ preprocessId || '-' }}</div><div class="metric-foot">preprocessId</div></div>
      <div class="metric-card"><div class="metric-label">DE 权重</div><div class="metric-value">{{ form.wX1 }}</div><div class="metric-foot">wX1</div></div>
      <div class="metric-card"><div class="metric-label">FE 权重</div><div class="metric-value">{{ form.wX2 }}</div><div class="metric-foot">wX2</div></div>
      <div class="metric-card"><div class="metric-label">图像结果</div><div class="metric-value">{{ Object.keys(images || {}).length }}</div><div class="metric-foot">images</div></div>
    </div>

    <div class="content-grid">
      <el-card class="panel-card" shadow="never">
        <template #header><div class="card-header"><div><div class="card-kicker">参数配置</div><div class="card-title">融合任务设置</div></div><el-tag type="primary" effect="plain">Step 3</el-tag></div></template>
        <el-form ref="form" :model="form" label-width="140px" class="nice-form">
          <el-form-item label="上游预处理ID"><el-input-number v-model="preprocessId" :min="1" /></el-form-item>
          <el-form-item label="DE权重 wX1"><el-input-number v-model="form.wX1" :step="0.1" :min="0" :max="1" /></el-form-item>
          <el-form-item label="FE权重 wX2"><el-input-number v-model="form.wX2" :step="0.1" :min="0" :max="1" /></el-form-item>
          <el-form-item><el-button class="main-action" type="primary" @click="submit" :loading="loading">执行特征融合</el-button></el-form-item>
        </el-form>
      </el-card>

      <el-card class="panel-card" shadow="never">
        <template #header><div class="card-header"><div><div class="card-kicker">运行状态</div><div class="card-title">融合执行流程</div></div><el-tag :type="result ? 'success' : 'info'" effect="plain">{{ result ? '已完成' : '待执行' }}</el-tag></div></template>
        <div class="flow-list">
          <div class="flow-item active"><div class="flow-index">1</div><div><div class="flow-title">读取预处理结果</div><div class="flow-desc">从缓存和数据库链路中获取 norm_de 与 norm_fe。</div></div></div>
          <div class="flow-item"><div class="flow-index">2</div><div><div class="flow-title">配置融合权重</div><div class="flow-desc">通过 wX1 与 wX2 控制 DE/FE 特征贡献。</div></div></div>
          <div class="flow-item"><div class="flow-index">3</div><div><div class="flow-title">生成融合特征</div><div class="flow-desc">输出散点图、热力图和相关性图。</div></div></div>
          <div class="flow-item"><div class="flow-index">4</div><div><div class="flow-title">保存融合结果</div><div class="flow-desc">结果入库，为诊断与根因分析提供依据。</div></div></div>
        </div>
      </el-card>
    </div>

    <div v-if="result" class="result-grid">
      <el-card class="panel-card" shadow="never">
        <template #header><div class="card-header"><div><div class="card-kicker">执行结果</div><div class="card-title">融合摘要</div></div><el-tag type="success" effect="plain">{{ result.status }}</el-tag></div></template>
        <div class="summary-grid">
          <div class="summary-item"><span>结果ID</span><strong>{{ result.id }}</strong></div>
          <div class="summary-item"><span>源数据ID</span><strong>{{ result.sourceId }}</strong></div>
          <div class="summary-item"><span>DE权重</span><strong>{{ form.wX1 }}</strong></div>
          <div class="summary-item"><span>FE权重</span><strong>{{ form.wX2 }}</strong></div>
        </div>
        <el-input class="result-textarea" :model-value="JSON.stringify(result, null, 2)" type="textarea" rows="8" readonly />
      </el-card>
    </div>

    <div v-if="images && Object.keys(images).length > 0" class="result-grid">
      <el-card class="panel-card" shadow="never">
        <template #header><div class="card-header"><div><div class="card-kicker">图像结果</div><div class="card-title">特征融合可视化</div></div><el-tag type="primary" effect="plain">{{ Object.keys(images).length }} 张</el-tag></div></template>
        <div class="image-grid">
          <div v-if="images.feat1_dot" class="image-card"><div class="image-title">DE特征散点图</div><img :src="formatImg(images.feat1_dot)" /></div>
          <div v-if="images.feat2_dot" class="image-card"><div class="image-title">FE特征散点图</div><img :src="formatImg(images.feat2_dot)" /></div>
          <div v-if="images.feat1_heatmap" class="image-card"><div class="image-title">DE特征热力图</div><img :src="formatImg(images.feat1_heatmap)" /></div>
          <div v-if="images.feat2_heatmap" class="image-card"><div class="image-title">FE特征热力图</div><img :src="formatImg(images.feat2_heatmap)" /></div>
          <div v-if="images.feature_correlation" class="image-card"><div class="image-title">特征相关性图</div><img :src="formatImg(images.feature_correlation)" /></div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { runFusion } from '@/api/project4/fusion'

export default {
  name: "FeatureFusion",
  data() {
    return {
      loading: false,
      preprocessId: 1,
      preprocessResultJson: '{}',
      result: null,
      images: {},
      form: {
        wX1: 0.5,
        wX2: 0.5,
        preprocessResult: {}
      }
    }
  },

  mounted() {
    this.loadPreprocessFromCache()
  },

  activated() {
    this.loadPreprocessFromCache()
  },

  methods: {
    loadPreprocessFromCache() {
      const cacheId = localStorage.getItem('project4_preprocess_id')
      const cacheResult = localStorage.getItem('project4_preprocess_result')

      if (cacheId) {
        this.preprocessId = Number(cacheId)
      }

      if (cacheResult) {
        this.preprocessResultJson = cacheResult
      }
    },

    normalizePreprocessResult(obj) {
      if (!obj) {
        return null
      }

      // 如果粘贴的是 Java 外层记录，例如 { id, sourceId, bizResult, status }
      if (obj.bizResult) {
        let pythonResponse = obj.bizResult
        if (typeof pythonResponse === 'string') {
          pythonResponse = JSON.parse(pythonResponse)
        }

        if (pythonResponse && pythonResponse.data) {
          return pythonResponse.data
        }

        return pythonResponse
      }

      // 如果粘贴的是统一返回体，例如 { code, msg, data }
      if (obj.code && obj.data) {
        return obj.data
      }

      // 如果粘贴的是业务结构，例如 { params, data }
      if (obj.data && obj.data.norm_de && obj.data.norm_fe) {
        return obj.data
      }

      // 如果本身就是 { norm_de, norm_fe }
      return obj
    },
    formatImg(img) {
      if (!img) return ''
      if (img.startsWith('data:image')) return img
      return 'data:image/png;base64,' + img
    },

    async submit() {
      if (!this.preprocessId) {
        return this.$modal.msgError("请填写预处理ID")
      }

      let parsed = null

      try {
        parsed = JSON.parse(this.preprocessResultJson)
      } catch (e) {
        return this.$modal.msgError("预处理结果JSON格式错误")
      }

      const preprocessResult = this.normalizePreprocessResult(parsed)

      if (!preprocessResult || !preprocessResult.norm_de || !preprocessResult.norm_fe) {
        console.error("当前融合页面拿到的预处理结果：", preprocessResult)
        return this.$modal.msgError("预处理结果中缺少 norm_de 或 norm_fe，请回到数据预处理页面重新执行")
      }

      this.form.preprocessResult = preprocessResult

      this.loading = true

      try {
        const payload = {
          w_x1: this.form.wX1,
          w_x2: this.form.wX2
        }

        console.log("fusion payload =", payload)

        const res = await runFusion(this.preprocessId, payload)

        if (res.code === 200) {
          this.$modal.msgSuccess("执行成功")

          const pythonResponse = typeof res.data.bizResult === 'string'
            ? JSON.parse(res.data.bizResult)
            : res.data.bizResult

          const bizResult = pythonResponse.data || pythonResponse

          this.images = bizResult.images || {}

          this.result = {
            id: res.data.id,
            sourceId: res.data.sourceId,
            status: res.data.status,
            createTime: res.data.createTime,
            updateTime: res.data.updateTime,
            message: "特征融合已完成，图片已解析"
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
