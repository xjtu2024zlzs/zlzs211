<template>
  <div class="project4-page">
    <div class="hero-card">
      <div>
        <div class="hero-subtitle">航空液压管路 · 数据增强</div>
        <div class="hero-title">液压管路样本数据增强</div>
        <div class="hero-desc">
          基于上游预处理结果，对标准化管路监测信号进行样本扩增，提高样本多样性和后续诊断模型的鲁棒性。
        </div>
      </div>
      <div class="hero-status">
        <el-tag effect="light" type="success">增强链路</el-tag>
        <el-tag effect="light" type="primary">scale</el-tag>
        <el-tag effect="light" type="info">图像解析</el-tag>
      </div>
    </div>

    <div class="metric-grid">
      <div class="metric-card"><div class="metric-label">上游预处理ID</div><div class="metric-value">{{ preprocessId || '-' }}</div><div class="metric-foot">preprocessId</div></div>
      <div class="metric-card"><div class="metric-label">增强模型</div><div class="metric-value">{{ form.augModel }}</div><div class="metric-foot">augModel</div></div>
      <div class="metric-card"><div class="metric-label">增强倍数</div><div class="metric-value">{{ form.augScale }}</div><div class="metric-foot">augScale</div></div>
      <div class="metric-card"><div class="metric-label">图像结果</div><div class="metric-value">{{ Object.keys(images || {}).length }}</div><div class="metric-foot">images</div></div>
    </div>

    <div class="content-grid">
      <el-card class="panel-card" shadow="never">
        <template #header><div class="card-header"><div><div class="card-kicker">参数配置</div><div class="card-title">增强任务设置</div></div><el-tag type="primary" effect="plain">Step 2</el-tag></div></template>
        <el-form ref="form" :model="form" label-width="140px" class="nice-form">
          <el-form-item label="上游预处理ID"><el-input-number v-model="preprocessId" :min="1" /></el-form-item>
          <el-form-item label="增强模型"><el-select v-model="form.augModel" style="width: 100%"><el-option label="scale 缩放" value="scale" /></el-select></el-form-item>
          <el-form-item label="增强倍数"><el-input-number v-model="form.augScale" :min="1" /></el-form-item>
          <el-form-item><el-button class="main-action" type="primary" @click="submit" :loading="loading">执行增强</el-button></el-form-item>
        </el-form>
      </el-card>

      <el-card class="panel-card" shadow="never">
        <template #header><div class="card-header"><div><div class="card-kicker">运行状态</div><div class="card-title">增强执行流程</div></div><el-tag :type="result ? 'success' : 'info'" effect="plain">{{ result ? '已完成' : '待执行' }}</el-tag></div></template>
        <div class="flow-list">
          <div class="flow-item active"><div class="flow-index">1</div><div><div class="flow-title">读取预处理结果</div><div class="flow-desc">根据 preprocessId 读取上游模态1与模态2标准化信号。</div></div></div>
          <div class="flow-item"><div class="flow-index">2</div><div><div class="flow-title">执行样本扩增</div><div class="flow-desc">按增强模型和倍数生成扩增样本。</div></div></div>
          <div class="flow-item"><div class="flow-index">3</div><div><div class="flow-title">生成对比图像</div><div class="flow-desc">解析 Python 返回图片并在页面展示。</div></div></div>
          <div class="flow-item"><div class="flow-index">4</div><div><div class="flow-title">结果保存</div><div class="flow-desc">增强结果写入数据库，供后续诊断链路追踪。</div></div></div>
        </div>
      </el-card>
    </div>

    <div v-if="result" class="result-grid">
      <el-card class="panel-card" shadow="never">
        <template #header><div class="card-header"><div><div class="card-kicker">执行结果</div><div class="card-title">增强摘要</div></div><el-tag type="success" effect="plain">{{ result.status }}</el-tag></div></template>
        <div class="summary-grid">
          <div class="summary-item"><span>结果ID</span><strong>{{ result.id }}</strong></div>
          <div class="summary-item"><span>源数据ID</span><strong>{{ result.sourceId }}</strong></div>
          <div class="summary-item"><span>图片数量</span><strong>{{ result.imageKeys ? result.imageKeys.length : 0 }}</strong></div>
          <div class="summary-item"><span>状态</span><strong>{{ result.status }}</strong></div>
        </div>
        <el-input class="result-textarea" :model-value="JSON.stringify(result, null, 2)" type="textarea" rows="8" readonly />
      </el-card>
    </div>

    <div v-if="images && Object.keys(images).length > 0" class="result-grid">
      <el-card class="panel-card" shadow="never">
        <template #header><div class="card-header"><div><div class="card-kicker">图像结果</div><div class="card-title">数据增强可视化</div></div><el-tag type="primary" effect="plain">{{ Object.keys(images).length }} 张</el-tag></div></template>
        <div class="image-grid">
          <div v-for="(img, name) in images" :key="name" class="image-card">
            <div class="image-title">{{ name }}</div>
            <img :src="formatImg(img)" />
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { runAugment } from '@/api/project4/augment'
export default {
  name: "Augment",
  data() {
    return {
      loading: false,
      preprocessId: Number(localStorage.getItem('project4_preprocess_id')) || 1,

      result: null,
      images: {},
      form: {
        augModel: "scale",
        augScale: 2,
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
    if (!obj) return null

    if (obj.bizResult) {
      let pythonResponse = obj.bizResult
      if (typeof pythonResponse === 'string') {
        pythonResponse = JSON.parse(pythonResponse)
      }
      return pythonResponse.data || pythonResponse
    }

    if (obj.code && obj.data) {
      return obj.data
    }

    if (obj.data && obj.data.norm_de && obj.data.norm_fe) {
      return obj.data
    }

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

      this.loading = true

      try {
        const payload = {
          aug_model: this.form.augModel,
          aug_scale: this.form.augScale
        }

        console.log("augment payload =", payload)

        const res = await runAugment(this.preprocessId, payload)

        if (res.code === 200) {
          this.$modal.msgSuccess("执行成功")

          const pythonResponse = typeof res.data.bizResult === 'string'
            ? JSON.parse(res.data.bizResult)
            : res.data.bizResult

          this.images = pythonResponse.images ||
            (pythonResponse.data && pythonResponse.data.images) ||
            {}

          console.log("增强返回 pythonResponse =", pythonResponse)
          console.log("增强图片 images =", this.images)

          this.result = {
            id: res.data.id,
            sourceId: res.data.sourceId,
            status: res.data.status,
            createTime: res.data.createTime,
            updateTime: res.data.updateTime,
            message: "数据增强已完成，图片已解析",
            imageKeys: Object.keys(this.images)
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
