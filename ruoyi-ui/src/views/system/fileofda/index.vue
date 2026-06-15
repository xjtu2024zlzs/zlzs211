<template>
  <div class="project4-page">
    <!-- 页面标题卡片 -->
    <section class="module-hero">
      <div>
        <div class="module-eyebrow">课题四 · 航空装备质量追溯</div>
        <h2>数据文件管理</h2>
        <p>
          管理原始数据文件、文件解析状态、样本数量与数据归档信息，为后续样本增强、特征融合与故障诊断提供数据基础。
        </p>
      </div>

      <div class="module-status">
        <span>数据已接入</span>
        <span>演示模式</span>
      </div>
    </section>

    <!-- 指标卡片 -->
    <section class="metric-strip">
      <div class="metric-mini">
        <span>文件总数</span>
        <strong>{{ total }}</strong>
        <em>当前数据集</em>
      </div>

      <div class="metric-mini">
        <span>当前页解析成功</span>
        <strong>{{ parseSuccessCount }}</strong>
        <em>parseStatus = 成功</em>
      </div>

      <div class="metric-mini">
        <span>当前页导入成功</span>
        <strong>{{ importSuccessCount }}</strong>
        <em>importStatus = 成功</em>
      </div>

      <div class="metric-mini">
        <span>当前页样本数量</span>
        <strong>{{ sampleTotal }}</strong>
        <em>sampleCount 汇总</em>
      </div>
    </section>

    <!-- 数据集导入与选择 -->
    <section class="dataset-card">
      <div class="card-header">
        <div>
          <div class="module-eyebrow">数据集管理</div>
          <h3>导入数据集与选择数据集</h3>
          <p class="section-desc">
            支持导入原始轴承振动数据集文件夹或单个数据文件，并选择当前用于预处理、样本分帧和后续诊断分析的数据集。
          </p>
        </div>

        <el-tag type="primary" effect="plain">
          当前数据集：{{ currentDataset?.datasetName || "未选择" }}
        </el-tag>
      </div>

      <div class="dataset-grid">
        <div class="dataset-panel">
          <div class="panel-title">导入数据集文件夹</div>

          <div class="folder-upload-box">
            <div class="folder-upload-icon">
              <el-icon><FolderOpened /></el-icon>
            </div>

            <div class="folder-upload-title">
              选择 CWRU 数据集文件夹或单个数据文件
            </div>

            <div class="folder-upload-desc">
              推荐选择数据集根目录或子目录，系统会自动识别其中的 MAT、CSV、TXT、XLSX 文件；也可以选择单个文件进行演示导入。
            </div>

            <div class="folder-upload-actions">
              <el-button type="primary" icon="FolderOpened" @click="handleChooseDatasetFolder">
                选择文件夹
              </el-button>
              <el-button plain icon="Document" @click="handleChooseSingleDatasetFile">
                选择单个文件
              </el-button>
            </div>

            <input
                ref="datasetFolderInputRef"
                class="hidden-file-input"
                type="file"
                multiple
                webkitdirectory
                directory
                @change="handleDatasetFolderChange"
            />

            <input
                ref="datasetSingleFileInputRef"
                class="hidden-file-input"
                type="file"
                accept=".mat,.csv,.txt,.xlsx"
                @change="handleDatasetSingleFileChange"
            />

            <div v-if="datasetUploadFiles.length" class="selected-dataset-summary">
              <div>
                <span>{{ datasetUploadMode === "folder" ? "已选择文件夹" : "已选择文件" }}</span>
                <strong>{{ datasetUploadName }}</strong>
              </div>
              <el-tag type="success" effect="plain">有效文件 {{ datasetUploadFiles.length }} 个</el-tag>
            </div>
          </div>

          <div class="upload-actions">
            <el-button type="primary" icon="Upload" @click="handleImportDataset">
              导入数据集
            </el-button>
            <el-button icon="Refresh" @click="handleResetDatasetImport">
              清空
            </el-button>
          </div>
        </div>

        <div class="dataset-panel">
          <div class="panel-title">选择数据集</div>

          <el-form :model="datasetForm" label-width="110px">
            <el-form-item label="当前数据集">
              <el-select
                  v-model="datasetForm.datasetId"
                  placeholder="请选择数据集"
                  style="width: 100%"
                  @change="handleDatasetChange"
              >
                <el-option
                    v-for="item in datasetOptions"
                    :key="item.datasetId"
                    :label="item.datasetName"
                    :value="item.datasetId"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="数据集类型">
              <el-input v-model="datasetForm.datasetType" disabled />
            </el-form-item>

            <el-form-item label="文件数量">
              <el-input v-model="datasetForm.fileCount" disabled />
            </el-form-item>

            <el-form-item label="样本总量">
              <el-input v-model="datasetForm.sampleCount" disabled />
            </el-form-item>
          </el-form>
        </div>
      </div>
    </section>

    <!-- 数据表格 -->
    <section class="table-card">
      <div class="card-header table-card-header">
        <div>
          <div class="module-eyebrow">数据列表</div>
          <h3>原始数据文件</h3>
        </div>

        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
      </div>

      <el-collapse-transition>
        <div v-show="showSearch" class="raw-query-box">
          <div class="inline-section-title">数据文件查询</div>
          <el-form
              :model="queryParams"
              ref="queryRef"
              :inline="true"
              class="raw-query-form"
              label-width="92px"
          >
            <el-form-item label="数据集ID" prop="datasetId">
              <el-input-number
                  v-model="queryParams.datasetId"
                  :controls="false"
                  :min="0"
                  placeholder="请输入数据集ID"
                  clearable
                  style="width: 170px"
                  @keyup.enter="handleQuery"
              />
            </el-form-item>

            <el-form-item label="文件编号" prop="fileCode">
              <el-input
                  v-model="queryParams.fileCode"
                  placeholder="请输入文件编号"
                  clearable
                  @keyup.enter="handleQuery"
              />
            </el-form-item>

            <el-form-item label="原始文件名" prop="originalFileName">
              <el-input
                  v-model="queryParams.originalFileName"
                  placeholder="请输入原始文件名"
                  clearable
                  @keyup.enter="handleQuery"
              />
            </el-form-item>

            <el-form-item label="文件后缀" prop="fileSuffix">
              <el-input
                  v-model="queryParams.fileSuffix"
                  placeholder="请输入文件后缀"
                  clearable
                  @keyup.enter="handleQuery"
              />
            </el-form-item>

            <el-form-item label="解析状态" prop="parseStatus">
              <el-select
                  v-model="queryParams.parseStatus"
                  placeholder="请选择解析状态"
                  clearable
                  style="width: 170px"
              >
                <el-option label="成功" value="成功" />
                <el-option label="失败" value="失败" />
                <el-option label="未解析" value="未解析" />
              </el-select>
            </el-form-item>

            <el-form-item class="query-actions">
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-collapse-transition>

      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button
              type="primary"
              plain
              icon="Plus"
              @click="handleAdd"
              v-hasPermi="['system:fileofda:add']"
          >
            新增
          </el-button>
        </el-col>

        <el-col :span="1.5">
          <el-button
              type="success"
              plain
              icon="Edit"
              :disabled="single"
              @click="handleUpdate"
              v-hasPermi="['system:fileofda:edit']"
          >
            修改
          </el-button>
        </el-col>

        <el-col :span="1.5">
          <el-button
              type="danger"
              plain
              icon="Delete"
              :disabled="multiple"
              @click="handleDelete"
              v-hasPermi="['system:fileofda:remove']"
          >
            删除
          </el-button>
        </el-col>

        <el-col :span="1.5">
          <el-button
              type="warning"
              plain
              icon="Download"
              @click="handleExport"
              v-hasPermi="['system:fileofda:export']"
          >
            导出
          </el-button>
        </el-col>
      </el-row>

      <el-table
          v-loading="loading"
          :data="fileofdaList"
          border
          stripe
          @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />

        <el-table-column label="文件ID" align="center" prop="fileId" width="90" />
        <el-table-column label="数据集ID" align="center" prop="datasetId" width="100" />
        <el-table-column label="文件编号" align="center" prop="fileCode" width="150" show-overflow-tooltip />
        <el-table-column label="原始文件名" align="center" prop="originalFileName" width="180" show-overflow-tooltip />
        <el-table-column label="后缀" align="center" prop="fileSuffix" width="80" />
        <el-table-column label="类型" align="center" prop="fileType" width="90" />

        <el-table-column label="文件大小" align="center" prop="fileSize" width="110">
          <template #default="scope">
            {{ formatFileSize(scope.row.fileSize) }}
          </template>
        </el-table-column>

        <el-table-column label="存储路径" align="center" prop="storagePath" width="220" show-overflow-tooltip />
        <el-table-column label="文件MD5" align="center" prop="fileMd5" width="180" show-overflow-tooltip />

        <el-table-column label="数据来源" align="center" prop="sourceType" width="110">
          <template #default="scope">
            <el-tag type="info" effect="plain">{{ scope.row.sourceType || "-" }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="导入状态" align="center" prop="importStatus" width="110">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.importStatus)" effect="plain">
              {{ scope.row.importStatus || "-" }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="解析状态" align="center" prop="parseStatus" width="110">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.parseStatus)" effect="plain">
              {{ scope.row.parseStatus || "-" }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="样本数量" align="center" prop="sampleCount" width="110" />
        <el-table-column label="错误信息" align="center" prop="errorMsg" width="200" show-overflow-tooltip />
        <el-table-column label="备注" align="center" prop="remark" width="220" show-overflow-tooltip />

        <el-table-column label="操作" align="center" width="150" fixed="right">
          <template #default="scope">
            <el-button
                link
                type="primary"
                icon="Edit"
                @click="handleUpdate(scope.row)"
                v-hasPermi="['system:fileofda:edit']"
            >
              修改
            </el-button>

            <el-button
                link
                type="primary"
                icon="Delete"
                @click="handleDelete(scope.row)"
                v-hasPermi="['system:fileofda:remove']"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
          v-show="total > 0"
          :total="total"
          v-model:page="queryParams.pageNum"
          v-model:limit="queryParams.pageSize"
          @pagination="getList"
      />
    </section>


    <!-- 数据预处理 -->
    <section class="preprocess-card">
      <div class="card-header">
        <div>
          <div class="module-eyebrow">数据预处理</div>
          <h3>数据预处理配置</h3>
          <p class="section-desc">
            对当前数据集执行滤波去噪、归一化与数据分帧，其中数据分帧为必选步骤；处理完成后生成可用于样本增强、特征融合和故障诊断的样本窗口。
          </p>
        </div>

        <el-tag :type="preprocessStatus === '已完成' ? 'success' : 'warning'" effect="plain">
          处理状态：{{ preprocessStatus }}
        </el-tag>
      </div>

      <el-form :model="preprocessForm" label-width="130px">
        <div class="preprocess-layout">
          <div class="preprocess-main">
            <div class="preprocess-panel preprocess-panel-compact">
              <div class="panel-title">预处理操作</div>

              <el-form-item label="操作选择">
                <el-checkbox-group v-model="preprocessForm.steps">
                  <el-checkbox label="denoise">滤波去噪</el-checkbox>
                  <el-checkbox label="normalize">归一化</el-checkbox>
                  <el-checkbox label="frame" disabled>数据分帧（必选）</el-checkbox>
                </el-checkbox-group>
              </el-form-item>

              <el-form-item
                  v-if="preprocessForm.steps.includes('denoise')"
                  label="去噪方法"
              >
                <el-select
                    v-model="preprocessForm.denoiseMethod"
                    placeholder="请选择去噪方法"
                    style="width: 100%"
                >
                  <el-option label="小波去噪" value="wavelet" />
                  <el-option label="巴特沃斯带通滤波" value="butterworth" />
                  <el-option label="希尔伯特黄变换" value="hht" />
                </el-select>
              </el-form-item>

              <el-form-item
                  v-if="preprocessForm.steps.includes('normalize')"
                  label="归一化方法"
              >
                <el-select
                    v-model="preprocessForm.normalizeMethod"
                    placeholder="请选择归一化方法"
                    style="width: 100%"
                >
                  <el-option label="Z-score 标准化" value="zscore" />
                  <el-option label="Min-Max 归一化" value="minmax" />
                </el-select>
              </el-form-item>
            </div>

            <div class="preprocess-panel preprocess-panel-compact">
              <div class="panel-title">数据分帧参数</div>

              <div class="frame-param-grid">
                <el-form-item label="时间窗长度">
                  <el-input-number
                      v-model="preprocessForm.windowSize"
                      :min="128"
                      :step="128"
                      style="width: 100%"
                  />
                </el-form-item>

                <el-form-item label="步长设定">
                  <el-input-number
                      v-model="preprocessForm.stride"
                      :min="1"
                      :step="64"
                      style="width: 100%"
                  />
                </el-form-item>
              </div>

              <el-form-item label="重叠率">
                <el-slider
                    v-model="preprocessForm.overlapRate"
                    :min="0"
                    :max="90"
                    :step="5"
                    show-input
                />
              </el-form-item>
            </div>
          </div>

          <div class="preprocess-panel preprocess-summary">
            <div class="panel-title">处理摘要</div>

            <div class="summary-item">
              <span>当前数据集</span>
              <strong>{{ currentDataset?.datasetName || "-" }}</strong>
            </div>

            <div class="summary-item">
              <span>处理流程</span>
              <strong>{{ buildPreprocessFlowText() }}</strong>
            </div>

            <div class="summary-item">
              <span>预计生成样本数</span>
              <strong>{{ estimatedFrameCount }}</strong>
            </div>

            <el-button
                type="primary"
                icon="Operation"
                style="width: 100%; margin-top: 10px"
                @click="handleRunPreprocess"
            >
              执行数据预处理
            </el-button>
          </div>
        </div>
      </el-form>
    </section>

    <!-- 预处理后样本列表 -->
    <section class="table-card">
      <div class="card-header table-card-header">
        <div>
          <div class="module-eyebrow">处理结果</div>
          <h3>预处理后数据样本列表</h3>
          <p class="section-desc">
            展示完成去噪、归一化和数据分帧后的样本窗口，用于后续样本增强、特征融合和故障诊断。
          </p>
        </div>

        <div class="processed-table-actions">
          <el-button
              class="processed-action-btn"
              type="success"
              plain
              icon="Right"
              :disabled="!canGoAugment"
              @click="handleGoAugment"
          >
            进入样本增强
          </el-button>

          <el-button
              class="processed-action-btn"
              type="primary"
              plain
              icon="Download"
              @click="handleExportProcessedSamples"
          >
            导出样本列表
          </el-button>
        </div>
      </div>

      <el-table
          :data="processedSampleList"
          border
          stripe
          empty-text="请先选择数据集并执行数据预处理"
      >
        <el-table-column label="样本ID" align="center" prop="sampleId" width="90" />
        <el-table-column label="样本编号" align="center" prop="sampleCode" width="150" />
        <el-table-column label="来源文件" align="center" prop="sourceFileName" width="220" show-overflow-tooltip />
        <el-table-column label="时间窗长度" align="center" prop="windowSize" width="120" />
        <el-table-column label="步长" align="center" prop="stride" width="90" />
        <el-table-column label="重叠率" align="center" prop="overlapRate" width="100">
          <template #default="scope">
            {{ scope.row.overlapRate }}%
          </template>
        </el-table-column>
        <el-table-column label="去噪方法" align="center" prop="denoiseMethod" width="170" />
        <el-table-column label="归一化方法" align="center" prop="normalizeMethod" width="170" />
        <el-table-column label="样本维度" align="center" prop="sampleShape" width="120" />
        <el-table-column label="处理状态" align="center" prop="status" width="110">
          <template #default="scope">
            <el-tag type="success" effect="plain">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="生成时间" align="center" prop="createTime" width="170" />
      </el-table>
    </section>

    <!-- 新增 / 修改弹窗 -->
    <el-dialog :title="title" v-model="open" width="760px" append-to-body>
      <el-form ref="fileofdaRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="数据集ID" prop="datasetId">
              <el-input-number
                  v-model="form.datasetId"
                  :controls="false"
                  :min="0"
                  placeholder="请输入数据集ID"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="文件编号" prop="fileCode">
              <el-input v-model="form.fileCode" placeholder="请输入文件编号" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="原始文件名" prop="originalFileName">
              <el-input v-model="form.originalFileName" placeholder="请输入原始文件名" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="文件后缀" prop="fileSuffix">
              <el-input v-model="form.fileSuffix" placeholder="请输入文件后缀" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="文件类型" prop="fileType">
              <el-input v-model="form.fileType" placeholder="例如 mat / csv / txt" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="文件大小" prop="fileSize">
              <el-input-number
                  v-model="form.fileSize"
                  :controls="false"
                  :min="0"
                  placeholder="请输入文件大小，单位字节"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="数据来源" prop="sourceType">
              <el-select v-model="form.sourceType" placeholder="请选择数据来源" style="width: 100%">
                <el-option label="CWRU" value="CWRU" />
                <el-option label="文件上传" value="文件上传" />
                <el-option label="传感器上传" value="传感器上传" />
                <el-option label="维修记录" value="维修记录" />
                <el-option label="飞行日志" value="飞行日志" />
                <el-option label="手工录入" value="手工录入" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="样本数量" prop="sampleCount">
              <el-input-number
                  v-model="form.sampleCount"
                  :controls="false"
                  :min="0"
                  placeholder="请输入样本数量"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="导入状态" prop="importStatus">
              <el-select v-model="form.importStatus" placeholder="请选择导入状态" style="width: 100%">
                <el-option label="成功" value="成功" />
                <el-option label="失败" value="失败" />
                <el-option label="未导入" value="未导入" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="解析状态" prop="parseStatus">
              <el-select v-model="form.parseStatus" placeholder="请选择解析状态" style="width: 100%">
                <el-option label="成功" value="成功" />
                <el-option label="失败" value="失败" />
                <el-option label="未解析" value="未解析" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="文件存储路径" prop="storagePath">
              <el-input v-model="form.storagePath" placeholder="请输入文件存储路径" />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="文件MD5" prop="fileMd5">
              <el-input v-model="form.fileMd5" placeholder="请输入文件MD5" />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="错误信息" prop="errorMsg">
              <el-input v-model="form.errorMsg" type="textarea" placeholder="请输入错误信息" />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Fileofda">
import { computed, getCurrentInstance, reactive, ref, toRefs } from "vue"
import { FolderOpened } from "@element-plus/icons-vue"
import {
  createTopic4Pipeline,
  updateTopic4Pipeline
} from "@/utils/topic4Pipeline"
import {
  listFileofda,
  getFileofda,
  delFileofda,
  addFileofda,
  updateFileofda,
  executePreprocess,
  listRawSamples
} from "@/api/system/fileofda"

const { proxy } = getCurrentInstance()

const fileofdaList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const datasetUploadFiles = ref([])
const datasetUploadMode = ref("")
const datasetUploadName = ref("")
const datasetFolderInputRef = ref(null)
const datasetSingleFileInputRef = ref(null)
const preprocessStatus = ref("待处理")
const processedSampleList = ref([])
const currentPipelineId = ref(null)
const canGoAugment = ref(false)

const datasetOptions = ref([
  {
    datasetId: 1,
    datasetName: "CWRU 轴承故障数据集",
    datasetType: "轴承振动信号",
    fileCount: 6,
    sampleCount: 12
  },
  {
    datasetId: 2,
    datasetName: "航空装备轴承试验数据集",
    datasetType: "多传感器时序数据",
    fileCount: 8,
    sampleCount: 16
  }
])

const datasetForm = reactive({
  datasetId: 1,
  datasetName: "CWRU 轴承故障数据集",
  datasetType: "轴承振动信号",
  fileCount: 6,
  sampleCount: 12
})

const preprocessForm = reactive({
  steps: ["frame"],
  denoiseMethod: "wavelet",
  normalizeMethod: "zscore",
  windowSize: 1024,
  stride: 512,
  overlapRate: 50
})

const currentDataset = computed(() => {
  return datasetOptions.value.find(item => item.datasetId === datasetForm.datasetId)
})

const estimatedFrameCount = computed(() => {
  const dataset = currentDataset.value

  if (!dataset) {
    return 0
  }

  const baseCount = Number(dataset.sampleCount || 0)
  const overlapFactor = 1 + Number(preprocessForm.overlapRate || 0) / 100

  return Math.round(baseCount * overlapFactor * 8)
})


const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    datasetId: null,
    fileCode: null,
    originalFileName: null,
    fileSuffix: null,
    fileMd5: null,
    parseStatus: null
  },
  rules: {
    datasetId: [
      { required: true, message: "数据集ID不能为空", trigger: "blur" }
    ],
    fileCode: [
      { required: true, message: "文件编号不能为空", trigger: "blur" }
    ],
    originalFileName: [
      { required: true, message: "原始文件名不能为空", trigger: "blur" }
    ]
  }
})

const { queryParams, form, rules } = toRefs(data)

const parseSuccessCount = computed(() => {
  return fileofdaList.value.filter(item => item.parseStatus === "成功").length
})

const importSuccessCount = computed(() => {
  return fileofdaList.value.filter(item => item.importStatus === "成功").length
})

const sampleTotal = computed(() => {
  return fileofdaList.value.reduce((sum, item) => {
    return sum + Number(item.sampleCount || 0)
  }, 0)
})

function getList() {
  loading.value = true
  listFileofda(queryParams.value).then(response => {
    fileofdaList.value = response.rows || []
    total.value = response.total || 0
    loading.value = false
  }).catch(error => {
    console.error("数据文件查询失败：", error)
    fileofdaList.value = []
    total.value = 0
    loading.value = false
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    fileId: null,
    datasetId: null,
    fileCode: null,
    originalFileName: null,
    fileSuffix: null,
    fileType: null,
    fileSize: 0,
    storagePath: null,
    fileMd5: null,
    sourceType: "CWRU",
    importStatus: "成功",
    parseStatus: "成功",
    sampleCount: 0,
    errorMsg: null,
    delFlag: "0",
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  }

  proxy.resetForm("fileofdaRef")
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

const datasetFilePattern = /\.(mat|csv|txt|xlsx)$/i

function handleChooseDatasetFolder() {
  datasetFolderInputRef.value?.click?.()
}

function handleChooseSingleDatasetFile() {
  datasetSingleFileInputRef.value?.click?.()
}

function getImportFileName(file) {
  const relativePath = file?.webkitRelativePath || ""
  const fileName = file?.name || ""

  if (relativePath) {
    return relativePath.split("/").pop() || fileName
  }

  return fileName
}

function getImportFileRelativePath(file) {
  return file?.webkitRelativePath || file?.name || ""
}

function getFolderNameFromFiles(files) {
  const firstPath = getImportFileRelativePath(files[0])

  if (firstPath.includes("/")) {
    return firstPath.split("/")[0]
  }

  return firstPath.replace(datasetFilePattern, "") || "导入数据集"
}

function filterSupportedDatasetFiles(files) {
  return Array.from(files || []).filter(file => {
    const name = getImportFileName(file)
    return datasetFilePattern.test(name)
  })
}

function handleDatasetFolderChange(event) {
  const files = filterSupportedDatasetFiles(event.target.files)

  if (!files.length) {
    datasetUploadFiles.value = []
    datasetUploadMode.value = ""
    datasetUploadName.value = ""
    proxy.$modal.msgWarning("所选文件夹中未识别到 MAT、CSV、TXT、XLSX 数据文件")
    return
  }

  datasetUploadFiles.value = files
  datasetUploadMode.value = "folder"
  datasetUploadName.value = getFolderNameFromFiles(files)

  if (datasetSingleFileInputRef.value) {
    datasetSingleFileInputRef.value.value = ""
  }

  proxy.$modal.msgSuccess(`已选择文件夹：${datasetUploadName.value}，识别到 ${files.length} 个有效数据文件`)
}

function handleDatasetSingleFileChange(event) {
  const files = filterSupportedDatasetFiles(event.target.files)

  if (!files.length) {
    datasetUploadFiles.value = []
    datasetUploadMode.value = ""
    datasetUploadName.value = ""
    proxy.$modal.msgWarning("仅支持 MAT、CSV、TXT、XLSX 数据文件")
    return
  }

  const file = files[0]
  datasetUploadFiles.value = [file]
  datasetUploadMode.value = "file"
  datasetUploadName.value = getImportFileName(file)

  if (datasetFolderInputRef.value) {
    datasetFolderInputRef.value.value = ""
  }
}

function handleImportDataset() {
  if (!datasetUploadFiles.value.length) {
    proxy.$modal.msgWarning("请先选择需要导入的数据集文件夹或单个数据文件")
    return
  }

  const datasetId = Date.now()
  const datasetName = datasetUploadMode.value === "folder"
      ? datasetUploadName.value
      : datasetUploadName.value.replace(datasetFilePattern, "")

  const newDataset = {
    datasetId,
    datasetName,
    datasetType: datasetUploadMode.value === "folder" ? "文件夹数据集" : "单文件数据集",
    fileCount: datasetUploadFiles.value.length,
    sampleCount: datasetUploadFiles.value.length * 2
  }

  datasetOptions.value.unshift(newDataset)
  Object.assign(datasetForm, newDataset)
  appendImportedRawFiles(datasetUploadFiles.value, datasetId)

  preprocessStatus.value = "待处理"
  processedSampleList.value = []
  currentPipelineId.value = null
  canGoAugment.value = false

  proxy.$modal.msgSuccess("数据集导入成功，原始数据文件已加入列表，请继续配置数据预处理参数")
}

function appendImportedRawFiles(files, datasetId) {
  const now = Date.now()
  const rows = files.map((file, index) => {
    const fileName = getImportFileName(file)
    const suffixMatch = fileName.match(/\.([^.]+)$/)
    const suffix = suffixMatch ? suffixMatch[1].toLowerCase() : ""
    const relativePath = getImportFileRelativePath(file)

    return {
      fileId: now + index,
      datasetId,
      fileCode: `FILE-IMP-${String(index + 1).padStart(3, "0")}`,
      originalFileName: fileName,
      fileSuffix: suffix,
      fileType: suffix,
      fileSize: file.size || 0,
      storagePath: relativePath ? `/data/import/${relativePath}` : `/data/import/${fileName}`,
      fileMd5: "-",
      sourceType: datasetUploadMode.value === "folder" ? "文件夹导入" : "文件上传",
      importStatus: "成功",
      parseStatus: "成功",
      sampleCount: 2,
      errorMsg: null,
      delFlag: "0",
      remark: datasetUploadMode.value === "folder" ? `来自文件夹：${datasetUploadName.value}` : "单文件导入"
    }
  })

  fileofdaList.value = [...rows, ...fileofdaList.value]
  total.value = Number(total.value || 0) + rows.length
}

function handleResetDatasetImport() {
  datasetUploadFiles.value = []
  datasetUploadMode.value = ""
  datasetUploadName.value = ""

  if (datasetFolderInputRef.value) {
    datasetFolderInputRef.value.value = ""
  }

  if (datasetSingleFileInputRef.value) {
    datasetSingleFileInputRef.value.value = ""
  }
}

function handleDatasetChange(datasetId) {
  const dataset = datasetOptions.value.find(item => item.datasetId === datasetId)

  if (!dataset) {
    return
  }

  Object.assign(datasetForm, dataset)
  preprocessStatus.value = "待处理"
  processedSampleList.value = []
  currentPipelineId.value = null
  canGoAugment.value = false
}

function buildPreprocessFlowText() {
  const flow = []

  if (preprocessForm.steps.includes("denoise")) {
    flow.push(getDenoiseMethodName(preprocessForm.denoiseMethod))
  }

  if (preprocessForm.steps.includes("normalize")) {
    flow.push(getNormalizeMethodName(preprocessForm.normalizeMethod))
  }

  flow.push("数据分帧")

  return flow.join(" → ")
}

function getDenoiseMethodName(value) {
  const map = {
    wavelet: "小波去噪",
    butterworth: "巴特沃斯带通滤波",
    hht: "希尔伯特黄变换"
  }

  return map[value] || "-"
}

function getNormalizeMethodName(value) {
  const map = {
    zscore: "Z-score 标准化",
    minmax: "Min-Max 归一化"
  }

  return map[value] || "-"
}

async function handleRunPreprocess() {
  if (!currentDataset.value) {
    proxy.$modal.msgWarning("请先选择数据集")
    return
  }

  if (!preprocessForm.steps.includes("frame")) {
    preprocessForm.steps.push("frame")
  }

  const selectedFileIds = ids.value && ids.value.length
      ? ids.value
      : fileofdaList.value.map(item => item.fileId).filter(Boolean)

  if (!selectedFileIds.length) {
    proxy.$modal.msgWarning("请先勾选需要预处理的数据文件")
    return
  }

  preprocessStatus.value = "处理中..."

  const pipeline = createTopic4Pipeline({
    datasetId: datasetForm.datasetId,
    datasetName: datasetForm.datasetName
  })

  const data = {
    pipelineId: pipeline.pipelineId,
    datasetId: datasetForm.datasetId,
    datasetCode: datasetForm.datasetCode || "CWRU",
    fileIds: selectedFileIds,
    windowSize: Number(preprocessForm.windowSize || 1024),
    stride: Number(preprocessForm.stride || 512),
    overlapRate: Number(preprocessForm.overlapRate || 50),
    denoise: preprocessForm.steps.includes("denoise"),
    normalize: preprocessForm.steps.includes("normalize"),
    frame: true,
    denoiseMethod: preprocessForm.denoiseMethod,
    normalizeMethod: preprocessForm.normalizeMethod,
    methodName: buildPreprocessFlowText()
  }

  try {
    const res = await executePreprocess(data)

    if (res.code !== 200) {
      preprocessStatus.value = "失败"
      proxy.$modal.msgError(res.msg || "数据预处理失败")
      return
    }

    const samples = await loadProcessedSamples()
    preprocessStatus.value = "已完成"

    updateTopic4Pipeline(pipeline.pipelineId, {
      currentStage: "PREPROCESSED",
      processedSamples: samples
    })

    currentPipelineId.value = pipeline.pipelineId
    canGoAugment.value = true

    await getList()
    proxy.$modal.msgSuccess("数据预处理完成，可点击进入样本增强继续下一步")
  } catch (error) {
    console.error("数据预处理接口调用失败：", error)
    preprocessStatus.value = "失败"
    proxy.$modal.msgError("数据预处理接口调用失败，请检查后端 /system/fileofda/preprocess 是否可用")
  }
}

async function loadProcessedSamples() {
  try {
    const res = await listRawSamples()
    const rows = res.rows || res.data || []
    const samples = rows.map(item => ({
      sampleId: item.sampleId,
      sampleCode: item.sampleCode,
      sourceFile: item.sourceFile || item.originalFileName || item.fileName || item.filePath || "-",
      windowSize: item.windowSize || preprocessForm.windowSize,
      stride: item.stride || preprocessForm.stride,
      overlapRate: item.overlapRate || `${preprocessForm.overlapRate}%`,
      denoiseMethod: preprocessForm.steps.includes("denoise") ? getDenoiseMethodName(preprocessForm.denoiseMethod) : "未启用",
      normalizeMethod: preprocessForm.steps.includes("normalize") ? getNormalizeMethodName(preprocessForm.normalizeMethod) : "未启用",
      sampleShape: item.sampleShape || `${item.windowSize || preprocessForm.windowSize}×1`,
      processStatus: item.preprocessStatus || item.sampleStatus || "已生成",
      createTime: item.createTime || formatDateTime(new Date()),
      sampleFilePath: item.sampleFilePath || item.samplePath || item.remark || ""
    }))

    processedSampleList.value = samples
    return samples
  } catch (error) {
    console.error("预处理样本查询失败：", error)
    processedSampleList.value = []
    return []
  }
}

function handleGoAugment() {
  if (!currentPipelineId.value) {
    proxy.$modal.msgWarning("请先执行数据预处理")
    return
  }

  goTopic4PageByTitle("样本增强", {
    pipelineId: currentPipelineId.value
  })
}

function goTopic4PageByTitle(title, query = {}) {
  const routes = proxy.$router.getRoutes()

  const targetRoute = routes.find(route => {
    return route.meta && route.meta.title === title
  })

  if (!targetRoute) {
    proxy.$modal.msgError(`没有找到菜单路由：${title}，请检查左侧菜单名称是否一致`)
    console.table(
        routes
            .filter(route => route.meta && route.meta.title)
            .map(route => ({
              title: route.meta.title,
              path: route.path,
              name: route.name
            }))
    )
    return
  }

  proxy.$router.push({
    path: targetRoute.path,
    query
  })
}

function buildProcessedSampleRows() {
  const denoiseName = preprocessForm.steps.includes("denoise")
      ? getDenoiseMethodName(preprocessForm.denoiseMethod)
      : "未启用"

  const normalizeName = preprocessForm.steps.includes("normalize")
      ? getNormalizeMethodName(preprocessForm.normalizeMethod)
      : "未启用"

  const sourceFiles = getPreprocessSourceFiles()

  return sourceFiles.flatMap((file, fileIndex) => {
    const frameCount = Math.max(1, Number(file.sampleCount || 2))

    return Array.from({ length: frameCount }).map((_, frameIndex) => {
      const id = fileIndex * 10 + frameIndex + 1

      return {
        sampleId: id,
        sampleCode: `SAMPLE-${String(id).padStart(3, "0")}`,
        datasetId: datasetForm.datasetId,
        sourceFileName: file.sourceFileName,
        faultType: file.faultType,
        faultLocation: file.faultLocation,
        windowSize: preprocessForm.windowSize,
        stride: preprocessForm.stride,
        overlapRate: preprocessForm.overlapRate,
        denoiseMethod: denoiseName,
        normalizeMethod: normalizeName,
        sampleShape: `${preprocessForm.windowSize}×1`,
        status: "已生成",
        createTime: formatDateTime(new Date())
      }
    })
  })
}

function getPreprocessSourceFiles() {
  const rows = (fileofdaList.value || []).filter(item => {
    if (!currentDataset.value) {
      return true
    }

    return Number(item.datasetId || currentDataset.value.datasetId) === Number(currentDataset.value.datasetId)
  })

  if (rows.length > 0) {
    return rows.map(item => {
      const fileName = item.originalFileName || item.fileCode || "unknown.mat"
      const faultInfo = inferFaultInfo(fileName)

      return {
        sourceFileName: fileName,
        sampleCount: Number(item.sampleCount || 2),
        faultType: faultInfo.faultType,
        faultLocation: faultInfo.faultLocation
      }
    })
  }

  return [
    {
      sourceFileName: "normal_0hp.mat",
      faultType: "正常",
      faultLocation: "无",
      sampleCount: 2
    },
    {
      sourceFileName: "inner_race_0hp_007.mat",
      faultType: "内圈故障",
      faultLocation: "轴承内圈",
      sampleCount: 2
    },
    {
      sourceFileName: "ball_0hp_007.mat",
      faultType: "滚动体故障",
      faultLocation: "轴承滚动体",
      sampleCount: 2
    },
    {
      sourceFileName: "outer_race_0hp_007.mat",
      faultType: "外圈故障",
      faultLocation: "轴承外圈",
      sampleCount: 2
    },
    {
      sourceFileName: "inner_race_1hp_014.mat",
      faultType: "内圈故障",
      faultLocation: "轴承内圈",
      sampleCount: 2
    },
    {
      sourceFileName: "outer_race_2hp_021.mat",
      faultType: "外圈故障",
      faultLocation: "轴承外圈",
      sampleCount: 2
    }
  ]
}

function inferFaultInfo(fileName) {
  const name = String(fileName || "").toLowerCase()

  if (name.includes("normal")) {
    return {
      faultType: "正常",
      faultLocation: "无"
    }
  }

  if (name.includes("inner")) {
    return {
      faultType: "内圈故障",
      faultLocation: "轴承内圈"
    }
  }

  if (name.includes("outer")) {
    return {
      faultType: "外圈故障",
      faultLocation: "轴承外圈"
    }
  }

  if (name.includes("ball")) {
    return {
      faultType: "滚动体故障",
      faultLocation: "轴承滚动体"
    }
  }

  return {
    faultType: "未知",
    faultLocation: "未知"
  }
}

function handleExportProcessedSamples() {
  if (!processedSampleList.value.length) {
    proxy.$modal.msgWarning("暂无可导出的预处理样本")
    return
  }

  proxy.$modal.msgSuccess("预处理样本列表已导出")
}

function formatDateTime(date) {
  const pad = value => String(value).padStart(2, "0")

  const year = date.getFullYear()
  const month = pad(date.getMonth() + 1)
  const day = pad(date.getDate())
  const hour = pad(date.getHours())
  const minute = pad(date.getMinutes())
  const second = pad(date.getSeconds())

  return `${year}-${month}-${day} ${hour}:${minute}:${second}`
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.fileId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  form.value.datasetId = 1
  form.value.fileCode = "FILE-CWRU-" + String(new Date().getTime()).slice(-3)
  form.value.fileSuffix = "mat"
  form.value.fileType = "mat"
  form.value.fileSize = 0
  form.value.sourceType = "CWRU"
  form.value.importStatus = "成功"
  form.value.parseStatus = "成功"
  form.value.sampleCount = 2
  open.value = true
  title.value = "添加数据文件"
}

function handleUpdate(row) {
  reset()
  const fileId = row.fileId || ids.value
  getFileofda(fileId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改数据文件"
  })
}

function submitForm() {
  proxy.$refs["fileofdaRef"].validate(valid => {
    if (!valid) {
      return
    }

    if (form.value.fileId != null) {
      updateFileofda(form.value).then(() => {
        proxy.$modal.msgSuccess("修改成功")
        open.value = false
        getList()
      })
    } else {
      addFileofda(form.value).then(() => {
        proxy.$modal.msgSuccess("新增成功")
        open.value = false
        getList()
      })
    }
  })
}

function handleDelete(row) {
  const fileIds = row.fileId || ids.value
  proxy.$modal.confirm('是否确认删除数据文件编号为 "' + fileIds + '" 的数据项？').then(() => {
    return delFileofda(fileIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download("system/fileofda/export", {
    ...queryParams.value
  }, `fileofda_${new Date().getTime()}.xlsx`)
}

function statusTagType(value) {
  if (value === "成功") {
    return "success"
  }

  if (value === "失败") {
    return "danger"
  }

  if (value === "未解析" || value === "未导入") {
    return "warning"
  }

  return "info"
}

function formatFileSize(size) {
  const value = Number(size || 0)

  if (value <= 0) {
    return "0 B"
  }

  if (value < 1024) {
    return value + " B"
  }

  if (value < 1024 * 1024) {
    return (value / 1024).toFixed(1) + " KB"
  }

  return (value / 1024 / 1024).toFixed(1) + " MB"
}

getList()
</script>

<style scoped lang="scss">
.project4-page {
  padding: 18px;
  min-height: calc(100vh - 84px);
  background: #eef5fb;
  color: #12213a;
}

.module-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
  padding: 24px 28px;
  border: 1px solid #cfe2f5;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #f7fbff 100%);
  box-shadow: 0 10px 24px rgba(38, 92, 145, 0.08);

  h2 {
    margin: 4px 0 8px;
    font-size: 26px;
    font-weight: 800;
    color: #0c2b52;
  }

  p {
    margin: 0;
    color: #4b688c;
    font-size: 14px;
    line-height: 1.8;
  }
}

.module-eyebrow {
  color: #1d7ed0;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.module-status {
  display: flex;
  gap: 10px;
  flex-shrink: 0;

  span {
    padding: 7px 14px;
    border: 1px solid #c9def3;
    border-radius: 999px;
    background: #ffffff;
    color: #2f5f91;
    font-size: 13px;
    font-weight: 600;
  }
}

.metric-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 18px;
}

.metric-mini {
  padding: 18px 20px;
  border: 1px solid #cfe2f5;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #f7fbff 100%);
  box-shadow: 0 10px 24px rgba(38, 92, 145, 0.06);

  span {
    display: block;
    color: #6b7f99;
    font-size: 13px;
    font-weight: 700;
  }

  strong {
    display: block;
    margin: 10px 0 4px;
    color: #0d1b2f;
    font-size: 28px;
    font-weight: 800;
  }

  em {
    color: #1d7ed0;
    font-size: 12px;
    font-style: normal;
  }
}

.dataset-card,
.preprocess-card,
.table-card {
  margin-bottom: 18px;
  padding: 18px 20px;
  border: 1px solid #cfe2f5;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #f7fbff 100%);
  box-shadow: 0 10px 24px rgba(38, 92, 145, 0.06);
}

.dataset-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.preprocess-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 420px;
  gap: 14px;
  align-items: stretch;
}

.preprocess-main {
  display: grid;
  grid-template-columns: 0.95fr 1.05fr;
  gap: 14px;
}

.frame-param-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.dataset-panel,
.preprocess-panel {
  padding: 14px 16px;
  border: 1px solid #d6e7f7;
  border-radius: 14px;
  background: #ffffff;
}

.panel-title {
  margin-bottom: 10px;
  color: #0c2b52;
  font-size: 16px;
  font-weight: 800;
}

.section-desc {
  margin: 6px 0 0;
  color: #5d728c;
  font-size: 13px;
  line-height: 1.7;
}

.folder-upload-box {
  padding: 22px 18px;
  border: 1px dashed #b8d5f0;
  border-radius: 16px;
  background: linear-gradient(180deg, #f7fbff 0%, #ffffff 100%);
  text-align: center;
}

.folder-upload-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  margin-bottom: 10px;
  border-radius: 16px;
  background: #e8f4ff;
  color: #2f8be6;
  font-size: 26px;
}

.folder-upload-title {
  margin-bottom: 8px;
  color: #0c2b52;
  font-size: 16px;
  font-weight: 800;
}

.folder-upload-desc {
  max-width: 560px;
  margin: 0 auto 16px;
  color: #6b7f99;
  font-size: 13px;
  line-height: 1.7;
}

.folder-upload-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 14px;
}

.hidden-file-input {
  display: none;
}

.selected-dataset-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  max-width: 620px;
  margin: 0 auto;
  padding: 10px 12px;
  border: 1px solid #d8ebd2;
  border-radius: 12px;
  background: #f5fff2;
  color: #244568;
  text-align: left;

  span {
    display: block;
    margin-bottom: 4px;
    color: #6b7f99;
    font-size: 12px;
    font-weight: 700;
  }

  strong {
    color: #0c2b52;
    font-size: 14px;
  }
}

.upload-actions {
  display: flex;
  gap: 10px;
  margin-top: 14px;
}

.preprocess-summary {
  display: flex;
  flex-direction: column;
}

.summary-item {
  margin-bottom: 10px;
  padding: 10px 12px;
  border: 1px solid #dbeaf8;
  border-radius: 12px;
  background: #f5f9ff;

  span {
    display: block;
    margin-bottom: 6px;
    color: #6b7f99;
    font-size: 12px;
    font-weight: 700;
  }

  strong {
    color: #0c2b52;
    font-size: 14px;
    font-weight: 800;
    line-height: 1.6;
  }
}

:deep(.el-upload-dragger) {
  border-radius: 14px;
  border-color: #c7dcf2;
  background: #f7fbff;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  h3 {
    margin: 4px 0 0;
    font-size: 20px;
    font-weight: 800;
    color: #0c2b52;
  }
}

.table-card-header {
  margin-bottom: 12px;
}

.processed-table-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.processed-action-btn {
  width: 132px;
  height: 36px;
  justify-content: center;
}

.raw-query-box {
  margin-bottom: 14px;
  padding: 14px 16px 2px;
  border: 1px solid #d6e7f7;
  border-radius: 14px;
  background: #f8fbff;
}

.inline-section-title {
  margin-bottom: 10px;
  color: #0c2b52;
  font-size: 15px;
  font-weight: 800;
}

.raw-query-form {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
}

.query-actions {
  margin-left: auto;
}

.mb8 {
  margin-bottom: 14px;
}

:deep(.el-form--inline .el-form-item) {
  margin-right: 28px;
  margin-bottom: 16px;
}

:deep(.el-form-item__label) {
  color: #244568;
  font-weight: 700;
}

:deep(.el-input__wrapper),
:deep(.el-select .el-input__wrapper),
:deep(.el-date-editor.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px #d6e4f2 inset;
}

:deep(.el-button) {
  border-radius: 10px;
  font-weight: 600;
}

:deep(.el-table) {
  border-radius: 14px;
  overflow: hidden;
  color: #243b57;
}

:deep(.el-table th.el-table__cell) {
  background: #f3f8fd;
  color: #244568;
  font-weight: 800;
}

:deep(.el-table td.el-table__cell) {
  padding: 13px 0;
}

:deep(.el-table .cell) {
  line-height: 1.6;
}

:deep(.el-table__row:hover > td.el-table__cell) {
  background: #f1f8ff !important;
}

:deep(.pagination-container) {
  margin-top: 18px;
  background: transparent;
}

:deep(.el-dialog) {
  border-radius: 16px;
}

:deep(.el-dialog__header) {
  padding: 20px 24px 10px;
}

:deep(.el-dialog__title) {
  font-weight: 800;
  color: #0c2b52;
}

@media screen and (max-width: 1400px) {
  .metric-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .dataset-grid,
  .preprocess-layout,
  .preprocess-main,
  .frame-param-grid {
    grid-template-columns: 1fr;
  }
}

@media screen and (max-width: 1200px) {
  .module-hero {
    flex-direction: column;
    align-items: flex-start;
    gap: 14px;
  }
}

@media screen and (max-width: 768px) {
  .metric-strip {
    grid-template-columns: 1fr;
  }
}
</style>