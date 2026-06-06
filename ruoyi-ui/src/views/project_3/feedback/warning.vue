<template>
  <div class="app-container quality-page warning-page">
    <el-card shadow="never" class="quality-hero feedback-hero">
      <template #header>
        <div class="hero-header-row">
          <span class="quality-title">全域制造过程反馈监管</span>
          <el-button type="primary" plain @click="openManufacturingDataDialog">查看制造周期数据</el-button>
        </div>
      </template>
      <div class="quality-desc">查看零件、工序和设备异常状态，并发起异常检测算法分析。</div>
    </el-card>

    <el-card shadow="never" class="quality-card warning-section">
      <template #header>
        <span class="quality-section-title">工序序列关键异常信号预警</span>
      </template>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-card shadow="never" class="inner-card">
            <template #header>
              <span class="inner-title">零件模板列表</span>
            </template>
            <el-form :inline="true" class="quality-mb16">
              <el-form-item>
                <el-input v-model="partQuery.keyword" placeholder="零件编号 / 名称" clearable />
              </el-form-item>
              <el-form-item>
                <el-select v-model="partQuery.sort_by" placeholder="排序方式" style="width: 160px" @change="queryPart">
                  <el-option label="按编号排序" value="part_code" />
                  <el-option label="按零件名称排序" value="part_name" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-select v-model="partQuery.sort_order" placeholder="排序方向" style="width: 120px" @change="queryPart">
                  <el-option label="升序" value="asc" />
                  <el-option label="降序" value="desc" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="queryPart">查询</el-button>
                <el-button @click="resetPart">重置</el-button>
              </el-form-item>
            </el-form>

            <el-table v-loading="partLoading" :data="partList" border highlight-current-row @row-click="pickPartTemplate">
              <el-table-column prop="part_code" label="编号" min-width="120" />
              <el-table-column prop="part_name" label="零件名称" min-width="130" show-overflow-tooltip />
              <el-table-column prop="material" label="材料" min-width="100" show-overflow-tooltip />
              <el-table-column prop="spec_model" label="规格型号" min-width="110" show-overflow-tooltip />
              <el-table-column prop="instance_count" label="实例数" width="90" />
              <el-table-column label="操作" width="90" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click.stop="showTemplateDetail(row)">详情</el-button>
                </template>
              </el-table-column>
            </el-table>
            <pagination
              v-show="partTotal > 0"
              v-model:page="partQuery.page_num"
              v-model:limit="partQuery.page_size"
              :total="partTotal"
              @pagination="loadParts"
            />

            <div class="instance-panel">
              <div class="instance-header">
                <span class="inner-title sub-title">零件实例子列表</span>
                <span class="instance-meta">{{ selectedTemplate?.part_code || '未选择模板' }}</span>
              </div>
              <el-table
                v-loading="partInstanceLoading"
                :data="partInstanceList"
                border
                highlight-current-row
                max-height="280"
                @row-click="pickPart"
              >
                <el-table-column prop="part_instance_id" label="实例ID" min-width="120" show-overflow-tooltip />
                <el-table-column prop="serial_number" label="序列号" min-width="110" show-overflow-tooltip />
                <el-table-column prop="batch_number" label="批次号" min-width="100" show-overflow-tooltip />
                <el-table-column prop="status" label="状态" width="90" />
                <el-table-column label="操作" width="220" fixed="right">
                  <template #default="{ row }">
                    <el-button link type="danger" @click.stop="keyProc(row)">关键工序识别</el-button>
                    <el-button link type="info" @click.stop="showExecutionDetail(row)">详情</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <pagination
                v-show="partInstanceTotal > 0"
                v-model:page="partInstanceQuery.page_num"
                v-model:limit="partInstanceQuery.page_size"
                :total="partInstanceTotal"
                @pagination="loadPartInstances(selectedTemplate)"
              />
            </div>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card shadow="never" class="inner-card">
            <template #header>
              <span class="inner-title">对应制造工序</span>
            </template>
            <div class="selected-info">
              <div class="selected-path">来源：{{ selectedPart?.path_text || '暂无数据' }}</div>
              <div class="info-box quality-mt16">
                <div class="info-label">零件实例</div>
                <div class="info-value">{{ selectedPart?.part_code || '暂无数据' }}</div>
                <div class="info-desc">{{ selectedPart?.part_instance_id || selectedPart?.serial_number || '暂无数据' }}</div>
              </div>
            </div>

            <div v-loading="processLoading" class="process-list">
              <div v-for="item in processList" :key="item.process_exec_id" class="process-card">
                <div class="process-main">
                  <div class="process-title">{{ item.process_order }}. {{ item.process_name }}</div>
                  <div class="process-desc">{{ item.process_desc || '暂无异常描述' }}</div>
                  <div class="process-device">加工设备：{{ item.device_name || item.device_id || '暂无数据' }}</div>
                </div>
                <div class="process-actions">
                  <el-tag size="small" type="success">{{ item.status || '未知' }}</el-tag>
                  <el-tag size="small" :type="riskTag(item.risk_level)">{{ item.risk_level || '无' }}</el-tag>
                  <el-button plain round @click.stop="procDetect(item)">工序异常检测</el-button>
                </div>
              </div>
              <el-empty v-if="!processLoading && processList.length === 0" description="暂无工序数据" />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never" class="quality-card warning-section">
      <template #header>
        <span class="quality-section-title">设备异常信号预警</span>
      </template>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-card shadow="never" class="inner-card">
            <template #header>
              <span class="inner-title">设备列表</span>
            </template>
            <el-form :inline="true" class="quality-mb16">
              <el-form-item>
                <el-input v-model="deviceQuery.keyword" placeholder="设备编号 / 名称" clearable />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="queryDev">查询</el-button>
                <el-button @click="resetDev">重置</el-button>
              </el-form-item>
            </el-form>

            <el-table v-loading="deviceLoading" :data="deviceList" border highlight-current-row @row-click="pickDev">
              <el-table-column prop="device_code" label="编号" min-width="120" />
              <el-table-column prop="device_name" label="设备名称" min-width="160" show-overflow-tooltip />
              <el-table-column prop="status" label="状态" min-width="120" />
            </el-table>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card shadow="never" class="inner-card">
            <template #header>
              <span class="inner-title">设备加工零件列表</span>
            </template>
            <div class="info-box quality-mb16">
              <div class="info-label">设备编号</div>
              <div class="info-value">{{ selectedDevice?.device_code || '暂无数据' }}</div>
              <div class="info-desc">{{ selectedDevice?.device_name || '暂无数据' }}</div>
            </div>

            <el-table v-loading="devicePartLoading" :data="devicePartList" border max-height="320">
              <el-table-column prop="part_code" label="编号" min-width="120" />
              <el-table-column prop="part_name" label="零件名称" min-width="100" show-overflow-tooltip />
              <el-table-column label="加工工序" min-width="180" show-overflow-tooltip>
                <template #default="{ row }">
                  {{ formatProcess(row) }}
                </template>
              </el-table-column>
            </el-table>
            <pagination
              v-show="devicePartTotal > 0"
              v-model:page="devicePartQuery.page_num"
              v-model:limit="devicePartQuery.page_size"
              :total="devicePartTotal"
              @pagination="loadDevPart(selectedDevice)"
            />
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never" class="quality-card warning-section">
      <template #header>
        <div class="card-header-row">
          <span class="quality-section-title">历史异常检测结果</span>
          <el-button @click="loadDetectResults">刷新</el-button>
        </div>
      </template>
      <el-table v-loading="resultLoading" :data="resultRows" border>
        <el-table-column prop="taskId" label="任务ID" width="190" show-overflow-tooltip />
        <el-table-column prop="targetName" label="检测对象" min-width="140" show-overflow-tooltip />
        <el-table-column label="数据文件" min-width="170" show-overflow-tooltip>
          <template #default="{ row }">
            <el-link v-if="row.dataFileUrl" type="primary" :underline="false" @click.stop="downloadDataFile(row)">
              {{ row.dataFile || '--' }}
            </el-link>
            <span v-else>{{ row.dataFile || '--' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="abnormalScore" label="异常分数" width="110" />
        <el-table-column prop="abnormalTime" label="异常时间" width="170" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewHistoryResult(row, 'detect')">查看</el-button>
            <el-button link type="danger" @click="deleteDetectResult(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="resultTotal > 0"
        v-model:page="resultQuery.page_num"
        v-model:limit="resultQuery.page_size"
        :total="resultTotal"
        @pagination="loadDetectResults"
      />
    </el-card>

    <el-card shadow="never" class="quality-card warning-section">
      <template #header>
        <div class="card-header-row">
          <span class="quality-section-title">历史关键工序识别结果</span>
          <el-button @click="loadKeyProcessResults">刷新</el-button>
        </div>
      </template>
      <el-table v-loading="keyResultLoading" :data="keyResultRows" border>
        <el-table-column prop="taskId" label="任务ID" width="190" show-overflow-tooltip />
        <el-table-column prop="targetName" label="识别对象" min-width="150" show-overflow-tooltip />
        <el-table-column prop="keyProcessName" label="关键工序" min-width="150" show-overflow-tooltip />
        <el-table-column prop="keyProcessCode" label="工序编码" width="130" show-overflow-tooltip />
        <el-table-column prop="score" label="分数" width="110" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewHistoryResult(row, 'key')">查看</el-button>
            <el-button link type="danger" @click="deleteKeyProcessResult(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="keyResultTotal > 0"
        v-model:page="keyResultQuery.page_num"
        v-model:limit="keyResultQuery.page_size"
        :total="keyResultTotal"
        @pagination="loadKeyProcessResults"
      />
    </el-card>

    <el-dialog v-model="detectDialog.visible" :title="detectDialogTitle" width="1080px" :close-on-click-modal="false" @close="closeDetectDialog">
      <div class="detect-summary">
        <span>检测对象：{{ detectTargetText }}</span>
        <span>已选择 {{ selectedSampleIds.length }} 个文件</span>
      </div>
      <div class="task-lookup-row">
        <el-input v-model="lookupTaskId" placeholder="输入任务ID查看结果" clearable @keyup.enter="lookupDialogTaskResult" />
        <el-button type="primary" :loading="lookupLoading" @click="lookupDialogTaskResult">查看结果</el-button>
      </div>

      <template v-if="isKeyMode">
        <div class="detect-data-section bosch-param-section">
          <div class="detect-data-header">
            <span class="detect-data-title">Bosch 关键工序识别参数</span>
            <el-switch v-model="boschKeyForm.enabled" active-text="Bosch算法" inactive-text="原文件模式" />
          </div>
          <el-form v-if="boschKeyForm.enabled" :model="boschKeyForm" label-width="130px" class="bosch-param-form">
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="KQC任务ID">
                  <el-input v-model="boschKeyForm.kqcTaskId" placeholder="输入已完成的KQC任务ID" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="KQC输出目录">
                  <el-input v-model="boschKeyForm.kqcOutputDir" placeholder="也可直接填写已完成KQC输出目录" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="Top N">
                  <el-input-number v-model="boschKeyForm.topN" :min="1" :max="50" controls-position="right" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>
        <div v-if="!boschKeyForm.enabled" class="file-toolbar">
          <el-input v-model="fileQuery.keyword" placeholder="搜索文件名" clearable style="width: 240px" @keyup.enter="loadSamples" @clear="loadSamples" />
          <el-button @click="loadSamples">查询</el-button>
          <el-button @click="clearSamples">清空选择</el-button>
        </div>
        <el-table v-if="!boschKeyForm.enabled" ref="sampleTableRef" v-loading="fileQuery.loading" :data="fileQuery.samples" row-key="id" height="360" border @selection-change="onSampleSelection">
          <el-table-column type="selection" width="48" reserve-selection />
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="fileName" label="文件名" min-width="180" show-overflow-tooltip />
          <el-table-column prop="conditionLabel" label="对象类型" width="130" />
          <el-table-column prop="bearingCode" label="对象编号" width="120" />
          <el-table-column prop="fileSize" label="文件大小" width="120">
            <template #default="{ row }">{{ fileSizeText(row.fileSize) }}</template>
          </el-table-column>
          <el-table-column prop="createTime" label="导入时间" width="170" />
        </el-table>
        <pagination v-if="!boschKeyForm.enabled" v-show="fileQuery.total > 0" v-model:page="fileQuery.pageNum" v-model:limit="fileQuery.pageSize" :total="fileQuery.total" @pagination="loadSamples" />
      </template>

      <template v-else>
        <div class="detect-data-section bosch-param-section">
          <div class="detect-data-header">
            <span class="detect-data-title">Bosch 工序异常检测参数</span>
            <el-switch v-model="boschProcessForm.enabled" active-text="Bosch算法" inactive-text="原文件模式" />
          </div>
          <el-form v-if="boschProcessForm.enabled" :model="boschProcessForm" label-width="130px" class="bosch-param-form">
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="选择数据文件" required>
                  <el-input v-model="boschProcessForm.trainNumericPath" placeholder="例如 F:/python/三算法/test_numeric.csv" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="工序">
                  <el-input v-model="boschProcessForm.station" placeholder="例如 L3_S36" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="特征列">
                  <el-input v-model="boschProcessForm.featureColName" placeholder="可留空自动选择" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="最大行数">
                  <el-input-number v-model="boschProcessForm.maxRows" :min="1000" :step="10000" controls-position="right" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="训练轮数">
                  <el-input-number v-model="boschProcessForm.epochs" :min="1" :max="300" controls-position="right" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="选择策略">
                  <el-select v-model="boschProcessForm.selectionMode" style="width: 100%">
                    <el-option label="按失效关联度" value="response_assoc" />
                    <el-option label="按数据覆盖率" value="coverage" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="告警比例">
                  <el-input-number v-model="boschProcessForm.alphaSample" :min="0.001" :max="0.2" :step="0.001" :precision="3" controls-position="right" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>

        <div v-if="!boschProcessForm.enabled" class="detect-data-section">
          <div class="detect-data-header">
            <span class="detect-data-title">1、选择训练和测试数据</span>
            <span class="detect-data-count">已选择 {{ selectedSamples.length }} 个文件</span>
          </div>
          <div class="file-toolbar">
            <el-input v-model="fileQuery.keyword" placeholder="搜索文件名" clearable style="width: 240px" @keyup.enter="loadSamples" @clear="loadSamples" />
            <el-button @click="loadSamples">查询</el-button>
            <el-button @click="clearSamples">清空选择</el-button>
          </div>
          <el-table ref="sampleTableRef" v-loading="fileQuery.loading" :data="fileQuery.samples" row-key="id" height="260" border @selection-change="onSampleSelection">
            <el-table-column type="selection" width="48" reserve-selection />
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="fileName" label="文件名" min-width="180" show-overflow-tooltip />
            <el-table-column prop="conditionLabel" label="对象类型" width="130" />
            <el-table-column prop="bearingCode" label="对象编号" width="120" />
            <el-table-column prop="fileSize" label="文件大小" width="120">
              <template #default="{ row }">{{ fileSizeText(row.fileSize) }}</template>
            </el-table-column>
            <el-table-column prop="createTime" label="导入时间" width="170" />
          </el-table>
          <pagination v-show="fileQuery.total > 0" v-model:page="fileQuery.pageNum" v-model:limit="fileQuery.pageSize" :total="fileQuery.total" @pagination="loadSamples" />
        </div>

        <div v-if="!boschProcessForm.enabled" class="detect-data-section">
          <div class="detect-data-header">
            <span class="detect-data-title">2、选择检测数据</span>
            <span class="detect-data-count">已上传 {{ detectUploadRows.length }} 个文件</span>
          </div>
          <div class="file-toolbar">
            <el-upload accept=".csv,.txt" :auto-upload="false" :show-file-list="false" :on-change="onDetectFileChange">
              <el-button type="primary" :loading="detectUpload.loading">上传数据文件</el-button>
            </el-upload>
            <el-button :disabled="!detectUploadRows.length || detectUpload.loading" @click="clearDetectUploads">清空上传</el-button>
          </div>
          <el-table :data="detectUploadRows" border height="180" empty-text="暂无检测数据文件">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="fileName" label="文件名" min-width="220" show-overflow-tooltip />
            <el-table-column prop="fileSize" label="文件大小" width="120">
              <template #default="{ row }">{{ fileSizeText(row.fileSize) }}</template>
            </el-table-column>
            <el-table-column prop="columnCount" label="列数" width="80" />
            <el-table-column label="使用列" width="100">
              <template #default="{ row }">
                {{ row.featureCol || autoFeatureColText(row) }}
              </template>
            </el-table-column>
            <el-table-column prop="uploadStatus" label="状态" width="110" />
          </el-table>
        </div>
      </template>

      <div class="detect-result" v-if="status || error || Object.keys(result).length">
        <el-descriptions v-if="isKeyMode" :column="3" border>
          <el-descriptions-item label="任务ID">{{ taskId || '--' }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ status || '--' }}</el-descriptions-item>
          <el-descriptions-item label="置信度">{{ val(keyField('confidence')) }}</el-descriptions-item>
          <el-descriptions-item label="关键工序">{{ val(keyField('name')) }}</el-descriptions-item>
          <el-descriptions-item label="工序编码">{{ val(keyField('code')) }}</el-descriptions-item>
          <el-descriptions-item label="分数">{{ val(keyField('score')) }}</el-descriptions-item>
          <el-descriptions-item label="原因" :span="3">{{ val(keyText('reason')) }}</el-descriptions-item>
          <el-descriptions-item label="建议" :span="3">{{ val(keyText('suggestion')) }}</el-descriptions-item>
          <el-descriptions-item v-if="error" label="失败原因" :span="3">{{ error }}</el-descriptions-item>
        </el-descriptions>
        <el-table v-if="isKeyMode && keyProcessRows.length" :data="keyProcessRows" border height="180" class="quality-mt16">
          <el-table-column prop="rank" label="排名" width="80" />
          <el-table-column prop="processCode" label="工序编码" />
          <el-table-column prop="processName" label="工序名称" />
          <el-table-column prop="score" label="分数" width="120" />
        </el-table>
        <div v-if="!isKeyMode && (taskProgress || taskMessage)" class="task-progress-row">
          <el-progress :percentage="taskProgress || 0" :status="status === 'FAILED' ? 'exception' : (status === 'SUCCESS' ? 'success' : undefined)" />
          <div class="task-progress-message">{{ taskMessage }}</div>
        </div>
        <el-descriptions v-if="!isKeyMode" :column="3" border>
          <el-descriptions-item label="任务ID">{{ taskId || '--' }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ status || '--' }}</el-descriptions-item>
          <el-descriptions-item label="是否异常">{{ abnormalText }}</el-descriptions-item>
          <el-descriptions-item label="异常分数">{{ val(result.abnormalScore) }}</el-descriptions-item>
          <el-descriptions-item label="异常时间">{{ val(result.abnormalTime) }}</el-descriptions-item>
          <el-descriptions-item label="建议" :span="3">{{ val(result.suggestion) }}</el-descriptions-item>
          <el-descriptions-item v-if="error" label="失败原因" :span="3">{{ error }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="!isKeyMode && curveRows.length" class="anomaly-chart-panel quality-mt16">
          <div class="anomaly-chart-head">
            <div>
              <div class="anomaly-chart-title">异常分数趋势</div>
              <div class="anomaly-chart-subtitle">横轴为样本点，纵轴为异常分数</div>
            </div>
            <div class="anomaly-chart-stats">
              <span>点数 {{ curveRows.length }}</span>
              <span>峰值 {{ formatChartValue(curveMaxValue) }}</span>
              <span v-if="curveThreshold !== null">阈值 {{ formatChartValue(curveThreshold) }}</span>
            </div>
          </div>
          <div ref="anomalyChartRef" class="anomaly-chart"></div>
        </div>
        <el-table v-if="!isKeyMode && curveRows.length" :data="curveRows" border height="160" class="quality-mt16">
          <el-table-column prop="x" label="x" />
          <el-table-column prop="value" label="value" />
        </el-table>
      </div>

      <template #footer>
        <el-button @click="closeDetectDialog">关闭</el-button>
        <el-button v-if="canCancelDetect" type="warning" :loading="cancelLoading" @click="cancelDetectTask">Cancel</el-button>
        <el-button type="primary" :loading="detectSubmitLoading" :disabled="detectSubmitDisabled" @click="submitDetectDialog">
          {{ detectDialogSubmitText }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="featureColumnDialog.visible" title="选择检测数据列" width="420px" :close-on-click-modal="false">
      <el-form label-width="110px">
        <el-form-item label="工序 number">
          <el-input :model-value="featureColumnDialog.processNumber || '--'" disabled />
        </el-form-item>
        <el-form-item label="文件列数">
          <el-input :model-value="featureColumnDialog.columnCount || '--'" disabled />
        </el-form-item>
        <el-form-item label="使用列" required>
          <el-select v-model="featureColumnDialog.selected" style="width: 100%">
            <el-option v-for="item in featureColumnOptions" :key="item" :label="`第 ${item} 列`" :value="item" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="featureColumnDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="confirmFeatureColumn">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="templateDetailDialog.visible" title="零件模板详细信息" width="760px">
      <el-table :data="templateDetailRows" border>
        <el-table-column prop="label" label="字段" width="160" />
        <el-table-column prop="value" label="内容" min-width="420" show-overflow-tooltip />
      </el-table>
    </el-dialog>

    <el-dialog v-model="executionDetailDialog.visible" title="工序执行详情" width="1120px">
      <div class="execution-detail-head">
        <div class="info-box">
          <div class="info-label">零件实例</div>
          <div class="info-value">{{ executionDetailDialog.part?.part_instance_id || '--' }}</div>
          <div class="info-desc">{{ executionDetailDialog.part?.part_name || executionDetailDialog.part?.part_code || '--' }}</div>
        </div>
        <div class="selected-path">{{ executionDetailDialog.part?.path_text || '--' }}</div>
      </div>
      <div class="execution-detail-toolbar">
        <el-button type="primary" @click="openExecutionForm()">新增</el-button>
      </div>
      <el-table v-loading="executionDetailDialog.loading" :data="executionDetailRows" border max-height="420" empty-text="暂无工序执行数据">
        <el-table-column prop="process_exec_id" label="执行ID" min-width="150" show-overflow-tooltip />
        <el-table-column prop="work_order_id" label="工单ID" min-width="130" show-overflow-tooltip />
        <el-table-column prop="process_def_id" label="工序定义ID" min-width="130" show-overflow-tooltip />
        <el-table-column prop="process_order" label="工序顺序" width="90" />
        <el-table-column prop="process_name" label="工序名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="device_id" label="设备ID" min-width="120" show-overflow-tooltip />
        <el-table-column prop="device_name" label="设备名称" min-width="130" show-overflow-tooltip />
        <el-table-column prop="start_time" label="开始时间" min-width="160" show-overflow-tooltip />
        <el-table-column prop="end_time" label="结束时间" min-width="160" show-overflow-tooltip />
        <el-table-column prop="operator" label="操作员" min-width="100" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openExecutionForm(row)">编辑</el-button>
            <el-button link type="danger" @click="removeExecution(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="executionFormDialog.visible" :title="executionFormTitle" width="640px" :close-on-click-modal="false">
      <el-form :model="executionForm" label-width="110px">
        <el-form-item label="执行ID">
          <el-input v-model="executionForm.process_exec_id" :disabled="executionFormDialog.mode === 'edit'" placeholder="新增时可为空，由系统生成" />
        </el-form-item>
        <el-form-item label="工单ID" required>
          <el-input v-model="executionForm.work_order_id" placeholder="请输入工单ID" />
        </el-form-item>
        <el-form-item label="工序定义ID" required>
          <el-input v-model="executionForm.process_def_id" placeholder="请输入工序定义ID" />
        </el-form-item>
        <el-form-item label="设备ID">
          <el-input v-model="executionForm.device_id" placeholder="请输入设备ID" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="executionForm.start_time" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择开始时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="executionForm.end_time" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择结束时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="操作员">
          <el-input v-model="executionForm.operator" placeholder="请输入操作员" />
        </el-form-item>
        <el-form-item label="状态">
          <el-input v-model="executionForm.status" placeholder="请输入状态" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="executionFormDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="executionFormDialog.loading" @click="submitExecutionForm">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="partDetailDialog.visible" title="零件详细信息" width="920px" class="part-detail-dialog">
      <div class="part-detail-layout">
        <div class="part-image-panel">
          <div class="part-image-title">零件图片</div>
          <el-upload class="part-image-uploader" accept="image/*" :auto-upload="false" :show-file-list="false" :on-change="onPartImageChange">
            <div v-loading="partDetailUploading" class="part-image-box">
              <img v-if="partDetailImage" :src="partDetailImage" alt="零件图片" />
              <div v-else class="part-image-empty">
                <span class="part-image-plus">+</span>
                <span>点击插入图片</span>
                <small>支持 JPG / PNG / BMP</small>
              </div>
            </div>
          </el-upload>
          <el-button v-if="partDetailImage" size="small" plain type="danger" :loading="partDetailUploading" @click="removePartImage">
            移除图片
          </el-button>
        </div>
        <div class="part-info-panel">
          <el-table :data="partDetailRows" border>
            <el-table-column prop="label" label="字段" width="150" />
            <el-table-column prop="value" label="内容" min-width="360" show-overflow-tooltip />
          </el-table>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="manufacturingDataDialog.visible" title="制造周期数据" width="1100px" :close-on-click-modal="false">
      <div class="data-file-toolbar">
        <el-input v-model="manufacturingDataQuery.keyword" placeholder="搜索文件名 / 对象编号 / 来源文件" clearable style="width: 280px" @keyup.enter="searchManufacturingData" @clear="searchManufacturingData" />
        <el-input v-model="manufacturingDataQuery.uploadBatchId" placeholder="上传批次" clearable style="width: 180px" @keyup.enter="searchManufacturingData" @clear="searchManufacturingData" />
        <el-button type="primary" @click="searchManufacturingData">查询</el-button>
        <el-button @click="resetManufacturingData">重置</el-button>
      </div>
      <el-table v-loading="manufacturingDataDialog.loading" :data="manufacturingDataDialog.rows" height="460" border row-key="id">
        <el-table-column prop="fileName" label="文件名" min-width="180" show-overflow-tooltip />
        <el-table-column label="数据用途" width="180">
          <template #default="{ row }">
            <el-select :model-value="row.dataUsage" size="small" :loading="row.usageUpdating" @change="value => changeManufacturingDataUsage(row, value)">
              <el-option label="制造通用数据" value="MANUFACTURE_COMMON" />
              <el-option label="工序异常检测" value="PROCESS_ANOMALY" />
              <el-option label="关键工序识别" value="KEY_PROCESS" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column prop="aircraftId" label="飞机" width="90" />
        <el-table-column prop="subsystemId" label="分系统" width="90" />
        <el-table-column prop="equipmentId" label="设备" width="90" />
        <el-table-column prop="componentId" label="组件" width="90" />
        <el-table-column label="文件大小" width="110">
          <template #default="{ row }">{{ fileSizeText(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="导入时间" width="170" />
        <el-table-column prop="sourceFile" label="来源文件" min-width="260" show-overflow-tooltip />
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" link :loading="row.deleting" @click="deleteManufacturingData(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="manufacturingDataDialog.total > 0"
        v-model:page="manufacturingDataQuery.pageNum"
        v-model:limit="manufacturingDataQuery.pageSize"
        :total="manufacturingDataDialog.total"
        @pagination="loadManufacturingData"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import {
  createWarningProcessExecution,
  deleteWarningProcessExecution,
  deleteWarningPartImage,
  deleteWarningAlgorithmResult,
  cancelWarningDetectTask,
  cancelWarningKeyProcessTask,
  getWarningDetectTask,
  getWarningKeyProcessTask,
  listWarningDetectResults,
  listWarningDeviceParts,
  listWarningDevices,
  listWarningPartInstances,
  listWarningPartProcesses,
  listWarningParts,
  startWarningDetectTask,
  startWarningKeyProcessTask,
  updateWarningProcessExecution,
  uploadWarningDetectFile,
  uploadWarningPartImage
} from '@/api/project_3/feedback'
import { deleteFaultIdentifyResult, deleteFaultIdenSample, listFaultIdenSamples, listKeyProcessResults, updateFaultIdenSampleDataUsage } from '@/api/project_3/service'
import { getApiPayload, getErrorMessage, useQualityAlgorithmTask } from '@/utils/useQualityAlgorithmTask'
import '@/views/project_3/common.css'

defineOptions({ name: 'FeedbackWarning' })

const partLoading = ref(false)
const partInstanceLoading = ref(false)
const processLoading = ref(false)
const deviceLoading = ref(false)
const devicePartLoading = ref(false)
const resultLoading = ref(false)
const keyResultLoading = ref(false)
const partDetailUploading = ref(false)
const partList = ref([])
const partInstanceList = ref([])
const processList = ref([])
const selectedTemplate = ref(null)
const selectedPart = ref(null)
const partTotal = ref(0)
const partInstanceTotal = ref(0)
const deviceList = ref([])
const devicePartList = ref([])
const selectedDevice = ref(null)
const devicePartTotal = ref(0)
const resultRows = ref([])
const resultTotal = ref(0)
const keyResultRows = ref([])
const keyResultTotal = ref(0)
const selectedSamples = ref([])
const sampleTableRef = ref(null)
const taskId = ref('')
const lookupTaskId = ref('')
const status = ref('')
const error = ref('')
const result = ref({})
const lookupLoading = ref(false)
const cancelLoading = ref(false)
const detectSubmitting = ref(false)
const detectRunning = ref(false)
const keySubmitting = ref(false)
const keyRunning = ref(false)
const anomalyChartRef = ref(null)
let anomalyChart = null
const fileQuery = reactive({ keyword: '', samples: [], pageNum: 1, pageSize: 20, total: 0, loading: false })
const detectUpload = reactive({ loading: false })
const detectUploadRows = ref([])
const detectDialog = reactive({ visible: false, loading: false, mode: 'detect', part: null, process: null })
const featureColumnDialog = reactive({ visible: false, row: null, columnCount: 0, processNumber: 0, selected: 1 })
const boschProcessForm = reactive({
  enabled: true,
  trainNumericPath: 'F:/python/三算法/test_numeric.csv',
  station: 'L3_S36',
  featureColName: '',
  maxRows: 200000,
  epochs: 60,
  selectionMode: 'response_assoc',
  alphaSample: 0.01,
  ewmaLambda: 0.3,
  minFailedObserved: 10,
  minObserved: 1000,
  seed: 0,
  device: 'auto'
})
const boschKeyForm = reactive({
  enabled: true,
  kqcTaskId: '',
  kqcOutputDir: '',
  keepFreq: 0.6,
  weightThresh: 0.0001,
  topN: 10
})
const partDetailDialog = reactive({ visible: false, row: null })
const templateDetailDialog = reactive({ visible: false, row: null })
const executionDetailDialog = reactive({ visible: false, loading: false, part: null })
const executionDetailRows = ref([])
const executionFormDialog = reactive({ visible: false, loading: false, mode: 'create' })
const executionForm = reactive({
  process_exec_id: '',
  work_order_id: '',
  process_def_id: '',
  device_id: '',
  start_time: '',
  end_time: '',
  operator: '',
  status: ''
})
const manufacturingDataDialog = reactive({ visible: false, loading: false, rows: [], total: 0 })
const manufacturingDataQuery = reactive({ keyword: '', uploadBatchId: '', pageNum: 1, pageSize: 20 })

const partQuery = reactive({ keyword: '', sort_by: 'part_code', sort_order: 'asc', page_num: 1, page_size: 6 })
const partInstanceQuery = reactive({ page_num: 1, page_size: 5 })
const deviceQuery = reactive({ keyword: '', risk_level: '', page_num: 1, page_size: 10 })
const devicePartQuery = reactive({ page_num: 1, page_size: 6 })
const resultQuery = reactive({ page_num: 1, page_size: 10 })
const keyResultQuery = reactive({ page_num: 1, page_size: 10 })

const MANUFACTURING_DATA_USAGES = ['MANUFACTURE_COMMON', 'PROCESS_ANOMALY', 'KEY_PROCESS']
const DETECT_DATA_USAGES = {
  detect: ['PROCESS_ANOMALY'],
  key: ['KEY_PROCESS', 'MANUFACTURE_COMMON']
}

const task = useQualityAlgorithmTask({
  taskId,
  status,
  error,
  result,
  query: (id, context) => context.mode === 'key' ? getWarningKeyProcessTask(id) : getWarningDetectTask(id),
  onDone: (taskStatus, data, context) => {
    if (context.mode === 'detect') {
      detectSubmitting.value = false
      detectDialog.loading = false
      detectRunning.value = false
      if (taskStatus === 'SUCCESS') loadDetectResults()
    } else if (context.mode === 'key') {
      keySubmitting.value = false
      keyRunning.value = false
      detectDialog.loading = false
    }
  },
  onError: (msg, context) => {
    if (context?.mode === 'detect') {
      detectSubmitting.value = false
      detectDialog.loading = false
      detectRunning.value = false
    } else if (context?.mode === 'key') {
      keySubmitting.value = false
      keyRunning.value = false
      detectDialog.loading = false
    }
    ElMessage.error(msg)
  },
  onMissingTaskId: () => ElMessage.warning('任务已提交，但未返回 taskId')
})

const trainSampleIds = computed(() => selectedSamples.value.map(item => item.id))
const detectSampleIds = computed(() => detectUploadRows.value.map(item => item.id).filter(Boolean))
const selectedSampleIds = computed(() => {
  if (isKeyMode.value && boschKeyForm.enabled) return []
  if (!isKeyMode.value && boschProcessForm.enabled) return []
  return isKeyMode.value ? trainSampleIds.value : [...trainSampleIds.value, ...detectSampleIds.value]
})
const featureColumnOptions = computed(() => Array.from({ length: featureColumnDialog.columnCount || 0 }, (_, index) => index + 1))
const isKeyMode = computed(() => detectDialog.mode === 'key')
const executionFormTitle = computed(() => executionFormDialog.mode === 'edit' ? '编辑工序执行' : '新增工序执行')
const detectDialogTitle = computed(() => isKeyMode.value ? '关键工序识别' : '工序异常检测')
const detectDialogSubmitText = computed(() => isKeyMode.value ? '开始关键工序识别' : '开始异常检测')
const taskProgress = computed(() => {
  const value = Number(result.value?.progress || 0)
  return Number.isFinite(value) ? Math.max(0, Math.min(100, value)) : 0
})
const taskMessage = computed(() => result.value?.message || result.value?.errorMessage || '')
const canCancelDetect = computed(() => taskId.value && ['PENDING', 'RUNNING'].includes(status.value))
const detectTaskActive = computed(() => !isKeyMode.value && detectRunning.value && taskId.value && isActiveTaskStatus(status.value))
const keyTaskActive = computed(() => isKeyMode.value && keyRunning.value && taskId.value && isActiveTaskStatus(status.value))
const detectSubmitLoading = computed(() => isKeyMode.value ? keySubmitting.value : detectSubmitting.value)
const detectSubmitDisabled = computed(() => isKeyMode.value ? (keySubmitting.value || keyTaskActive.value) : (detectSubmitting.value || detectTaskActive.value))
const detectTargetText = computed(() => {
  const part = detectDialog.part
  const proc = detectDialog.process
  if (!part) return '--'
  return proc ? `${part.part_name || part.part_code} / ${proc.process_name || proc.process_id}` : (part.part_name || part.part_code || part.part_id)
})
const abnormalText = computed(() => {
  const value = result.value?.isAbnormal ?? result.value?.is_abnormal
  if (value === true || value === 'true') return '是'
  if (value === false || value === 'false') return '否'
  return '--'
})
const curveRows = computed(() => {
  const rows = result.value?.curveData || result.value?.curve_data || []
  return Array.isArray(rows) ? rows.map(item => ({ x: item.x, value: item.value ?? item.y ?? item.riskScore })) : []
})
const curveValues = computed(() => curveRows.value.map(item => Number(item.value)).filter(Number.isFinite))
const curveMaxValue = computed(() => {
  return curveValues.value.length ? Math.max(...curveValues.value) : null
})
const curveThreshold = computed(() => {
  const value = result.value?.threshold ?? result.value?.ucl ?? result.value?.UCL_sample ?? result.value?.summary?.UCL_sample ?? result.value?.summary?.ucl
  const num = Number(value)
  return Number.isFinite(num) ? num : null
})
const keyProcessRows = computed(() => {
  const rows = result.value?.candidateProcesses || result.value?.candidate_processes || []
  return Array.isArray(rows)
    ? rows.map(item => ({
      ...item,
      rank: item.rank ?? item.order,
      processCode: item.processCode ?? item.process_code ?? item.keyProcessCode ?? item.key_process_code,
      processName: item.processName ?? item.process_name ?? item.keyProcessName ?? item.key_process_name,
      score: item.score ?? item.confidence
    }))
    : []
})
const templateDetailRows = computed(() => {
  const row = templateDetailDialog.row || {}
  return [
    { label: '零件模板ID', value: val(row.part_template_id) },
    { label: '零件编号', value: val(row.part_code) },
    { label: '零件名称', value: val(row.part_name) },
    { label: '材料', value: val(row.material) },
    { label: '规格型号', value: val(row.spec_model) },
    { label: '设计版本', value: val(row.design_version) },
    { label: '实例数量', value: val(row.instance_count) },
    { label: '飞机ID', value: val(row.aircraft_id) },
    { label: '分系统ID', value: val(row.subsystem_id) },
    { label: '设备ID', value: val(row.equipment_id) },
    { label: '组件ID', value: val(row.component_id) },
    { label: '结构路径', value: val(row.path_text) }
  ]
})
const partDetailRows = computed(() => {
  const row = partDetailDialog.row || {}
  return [
    { label: '零件实例ID', value: val(row.part_id) },
    { label: '零件模板ID', value: val(row.part_template_id) },
    { label: '零件编号', value: val(row.part_code) },
    { label: '零件名称', value: val(row.part_name) },
    { label: '序列号', value: val(row.serial_number) },
    { label: '批次号', value: val(row.batch_number) },
    { label: '制造商', value: val(row.manufacturer) },
    { label: '生产日期', value: val(row.production_date) },
    { label: '材料', value: val(row.material) },
    { label: '规格型号', value: val(row.spec_model) },
    { label: '当前状态', value: val(row.status) },
    { label: '当前工序', value: val(row.current_process) },
    { label: '风险等级', value: val(row.risk_level) },
    { label: '结构路径', value: val(row.path_text) }
  ]
})
const partDetailImage = computed(() => imageSrc(partDetailDialog.row?.image_url || partDetailDialog.row?.imageUrl))

function riskTag(level) {
  if (level === '高' || level === 'HIGH') return 'danger'
  if (level === '中' || level === 'MEDIUM') return 'warning'
  if (level === '低' || level === 'LOW') return 'success'
  return 'info'
}

async function loadParts() {
  partLoading.value = true
  try {
    const res = await listWarningParts(partQuery)
    const payload = res?.data || {}
    const rows = Array.isArray(payload.rows) ? payload.rows : []
    partList.value = rows
    partTotal.value = Number(payload.total || 0)
    const currentTemplateId = selectedTemplate.value?.part_template_id
    const currentInPage = rows.some(item => item.part_template_id === currentTemplateId)
    if (rows.length && !currentInPage) await pickPartTemplate(rows[0])
    if (!rows.length) {
      selectedTemplate.value = null
      selectedPart.value = null
      partInstanceList.value = []
      partInstanceTotal.value = 0
      processList.value = []
    }
  } finally {
    partLoading.value = false
  }
}

async function pickPartTemplate(row) {
  selectedTemplate.value = row || null
  selectedPart.value = null
  processList.value = []
  partInstanceQuery.page_num = 1
  await loadPartInstances(row)
}

async function loadPartInstances(row) {
  const templateId = row?.part_template_id
  if (!templateId) {
    partInstanceList.value = []
    partInstanceTotal.value = 0
    selectedPart.value = null
    processList.value = []
    return
  }
  partInstanceLoading.value = true
  try {
    const res = await listWarningPartInstances(templateId, partInstanceQuery)
    const payload = res?.data || {}
    const rows = Array.isArray(payload.rows) ? payload.rows : []
    partInstanceList.value = rows
    partInstanceTotal.value = Number(payload.total || 0)
    if (rows.length) {
      await pickPart(rows[0])
    } else {
      selectedPart.value = null
      processList.value = []
    }
  } finally {
    partInstanceLoading.value = false
  }
}

async function pickPart(row) {
  selectedPart.value = row || null
  await loadPartProc(row)
}

async function loadPartProc(row) {
  if (!row?.part_id) {
    processList.value = []
    return
  }
  processLoading.value = true
  try {
    const res = await listWarningPartProcesses(row.part_id)
    processList.value = Array.isArray(res?.data) ? res.data : []
  } finally {
    processLoading.value = false
  }
}

function queryPart() {
  partQuery.page_num = 1
  selectedTemplate.value = null
  selectedPart.value = null
  partInstanceQuery.page_num = 1
  loadParts()
}

function resetPart() {
  partQuery.keyword = ''
  partQuery.sort_by = 'part_code'
  partQuery.sort_order = 'asc'
  partQuery.page_num = 1
  partInstanceQuery.page_num = 1
  selectedTemplate.value = null
  selectedPart.value = null
  loadParts()
}

function showTemplateDetail(row) {
  templateDetailDialog.row = row || null
  templateDetailDialog.visible = true
}

async function showExecutionDetail(row) {
  executionDetailDialog.part = row || null
  executionDetailRows.value = []
  executionDetailDialog.visible = true
  await reloadExecutionDetailRows()
}

async function reloadExecutionDetailRows() {
  const row = executionDetailDialog.part
  if (!row?.part_id) {
    executionDetailRows.value = []
    return
  }
  executionDetailDialog.loading = true
  try {
    const res = await listWarningPartProcesses(row.part_id)
    const rows = Array.isArray(res?.data) ? res.data : []
    executionDetailRows.value = rows.filter(item => item.process_exec_id)
  } finally {
    executionDetailDialog.loading = false
  }
}

function resetExecutionForm(row = {}) {
  executionForm.process_exec_id = row.process_exec_id || ''
  executionForm.work_order_id = row.work_order_id || ''
  executionForm.process_def_id = row.process_def_id || row.process_id || ''
  executionForm.device_id = row.device_id || ''
  executionForm.start_time = row.start_time || ''
  executionForm.end_time = row.end_time || ''
  executionForm.operator = row.operator || ''
  executionForm.status = row.status || ''
}

function openExecutionForm(row) {
  executionFormDialog.mode = row?.process_exec_id ? 'edit' : 'create'
  const defaults = row || executionDetailRows.value[0] || {}
  resetExecutionForm(defaults)
  if (!row) {
    executionForm.process_exec_id = ''
    executionForm.start_time = ''
    executionForm.end_time = ''
    executionForm.operator = ''
    executionForm.status = ''
  }
  executionFormDialog.visible = true
}

async function submitExecutionForm() {
  if (!executionForm.work_order_id) {
    ElMessage.warning('请输入工单ID')
    return
  }
  if (!executionForm.process_def_id) {
    ElMessage.warning('请输入工序定义ID')
    return
  }
  executionFormDialog.loading = true
  try {
    const data = { ...executionForm }
    if (executionFormDialog.mode === 'edit') {
      await updateWarningProcessExecution(executionForm.process_exec_id, data)
      ElMessage.success('工序执行已更新')
    } else {
      await createWarningProcessExecution(data)
      ElMessage.success('工序执行已新增')
    }
    executionFormDialog.visible = false
    await reloadExecutionDetailRows()
    if (selectedPart.value?.part_id === executionDetailDialog.part?.part_id) {
      await loadPartProc(selectedPart.value)
    }
  } finally {
    executionFormDialog.loading = false
  }
}

async function removeExecution(row) {
  if (!row?.process_exec_id) return
  try {
    await ElMessageBox.confirm('确认删除该工序执行记录吗？相关异常和质量数据也会同步删除。', '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch (_) {
    return
  }
  await deleteWarningProcessExecution(row.process_exec_id)
  ElMessage.success('工序执行已删除')
  await reloadExecutionDetailRows()
  if (selectedPart.value?.part_id === executionDetailDialog.part?.part_id) {
    await loadPartProc(selectedPart.value)
  }
}

function showPartDetail(row) {
  partDetailDialog.row = row || null
  partDetailDialog.visible = true
}

async function onPartImageChange(file) {
  const raw = file?.raw
  const id = partDetailDialog.row?.part_id
  if (!id || !raw) return
  if (!raw.type?.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  const data = new FormData()
  data.append('file', raw)
  partDetailUploading.value = true
  try {
    const res = await uploadWarningPartImage(id, data)
    const imageUrl = res?.image_url || res?.imageUrl || res?.data?.image_url || res?.data?.imageUrl
    updatePartImage(id, imageUrl)
    ElMessage.success('图片上传成功')
  } finally {
    partDetailUploading.value = false
  }
}

async function removePartImage() {
  const id = partDetailDialog.row?.part_id
  if (!id) return
  partDetailUploading.value = true
  try {
    await deleteWarningPartImage(id)
    updatePartImage(id, '')
    ElMessage.success('图片已移除')
  } finally {
    partDetailUploading.value = false
  }
}

function updatePartImage(partId, imageUrl) {
  if (partDetailDialog.row?.part_id === partId) {
    partDetailDialog.row.image_url = imageUrl
  }
  const row = partInstanceList.value.find(item => item.part_id === partId)
  if (row) row.image_url = imageUrl
  if (selectedPart.value?.part_id === partId) {
    selectedPart.value.image_url = imageUrl
  }
}

function imageSrc(url) {
  if (!url) return ''
  return /^https?:\/\//i.test(url) ? url : `${import.meta.env.VITE_APP_BASE_API}${url}`
}

function procDetect(row) {
  if (!selectedPart.value) {
    ElMessage.warning('请先选择零件')
    return
  }
  openDetect(selectedPart.value, row)
}

function keyProc(row) {
  openKeyProcess(row)
}

async function openKeyProcess(part) {
  if (!part?.part_id) {
    ElMessage.warning('识别对象不能为空')
    return
  }
  task.clearPoll()
  detectSubmitting.value = false
  detectRunning.value = false
  keySubmitting.value = false
  keyRunning.value = false
  detectDialog.loading = false
  detectDialog.mode = 'key'
  detectDialog.part = part
  detectDialog.process = null
  detectDialog.visible = true
  taskId.value = ''
  status.value = ''
  error.value = ''
  result.value = {}
  clearSamples()
  clearDetectUploads()
  fileQuery.pageNum = 1
  await loadSamples()
}

async function openDetect(part, process) {
  if (!part?.part_id) {
    ElMessage.warning('检测对象不能为空')
    return
  }
  if (!(process?.process_exec_id || process?.process_id)) {
    ElMessage.warning('请先选择工序')
    return
  }
  task.clearPoll()
  detectSubmitting.value = false
  detectRunning.value = false
  keySubmitting.value = false
  keyRunning.value = false
  detectDialog.loading = false
  detectDialog.mode = 'detect'
  detectDialog.part = part
  detectDialog.process = process
  detectDialog.visible = true
  taskId.value = ''
  status.value = ''
  error.value = ''
  result.value = {}
  clearSamples()
  clearDetectUploads()
  fileQuery.pageNum = 1
  await loadSamples()
}

async function loadSamples() {
  fileQuery.loading = true
  try {
    const usages = DETECT_DATA_USAGES[detectDialog.mode] || DETECT_DATA_USAGES.detect
    const objectQuery = sampleObjectQuery()
    if (objectQuery === null) {
      fileQuery.samples = []
      fileQuery.total = 0
      return
    }
    const results = await Promise.all(usages.map(dataUsage => listFaultIdenSamples({
      dataUsage,
      keyword: fileQuery.keyword,
      ...objectQuery,
      pageNum: 1,
      pageSize: 5000
    })))
    const rows = results.flatMap(res => Array.isArray(res?.rows) ? res.rows : [])
    const start = (fileQuery.pageNum - 1) * fileQuery.pageSize
    fileQuery.samples = rows.slice(start, start + fileQuery.pageSize)
    fileQuery.total = rows.length
  } finally {
    fileQuery.loading = false
  }
}

function sampleObjectQuery() {
  if (detectDialog.mode !== 'detect') return {}
  const part = detectDialog.part || {}
  const partId = part.part_template_id || part.partTemplateId
  if (!partId || !part.component_id) return null
  return {
    aircraftId: part.aircraft_id,
    subsystemId: part.subsystem_id,
    equipmentId: part.equipment_id,
    componentId: part.component_id,
    partId
  }
}

function onSampleSelection(rows) {
  const map = new Map(selectedSamples.value.map(item => [item.id, item]))
  fileQuery.samples.forEach(item => map.delete(item.id))
  rows.forEach(item => map.set(item.id, item))
  selectedSamples.value = Array.from(map.values())
}

function clearSamples() {
  selectedSamples.value = []
  sampleTableRef.value?.clearSelection?.()
}

function clearDetectUploads() {
  detectUploadRows.value = []
  featureColumnDialog.visible = false
  featureColumnDialog.row = null
}

async function onDetectFileChange(file) {
  const raw = file?.raw
  if (!raw) return
  const name = raw.name || ''
  if (!/\.(csv|txt)$/i.test(name)) {
    ElMessage.warning('请选择 CSV/TXT 数据文件')
    return
  }
  const part = detectDialog.part || {}
  const partTemplateId = part.part_template_id || part.partTemplateId
  if (!partTemplateId) {
    ElMessage.warning('检测对象缺少零件模板ID')
    return
  }
  const columnCount = await detectFileColumnCount(raw)
  const payload = new FormData()
  payload.append('purpose', 'processAnomaly')
  payload.append('executionObject', 'bearing')
  payload.append('aircraftId', part.aircraft_id || '')
  payload.append('subsystemId', part.subsystem_id || '')
  payload.append('equipmentId', part.equipment_id || '')
  payload.append('componentId', part.component_id || '')
  payload.append('partId', partTemplateId)
  payload.append('uploadBatchId', `WARN_DETECT_${Date.now()}`)
  payload.append('fileIndex', '1')
  payload.append('totalFiles', '1')
  payload.append('file', raw)
  detectUpload.loading = true
  try {
    const res = await uploadWarningDetectFile(payload)
    const data = res?.data || res || {}
    const row = {
      id: data.id || data.sampleId || data.sample_id,
      fileName: data.fileName || data.file_name || name,
      fileSize: data.fileSize || data.file_size || raw.size,
      uploadStatus: data.uploadStatus || data.upload_status || 'SUCCESS',
      dataUsage: data.dataUsage || 'MANUFACTURE_COMMON',
      columnCount,
      featureCol: autoFeatureCol(columnCount)
    }
    if (!row.id) throw new Error('上传成功但未返回文件ID')
    detectUploadRows.value = [row]
    ElMessage.success('检测数据文件上传成功')
    if (needsFeatureColumnChoice(row)) {
      openFeatureColumnDialog(row)
    }
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '检测数据文件上传失败'))
  } finally {
    detectUpload.loading = false
  }
}

async function detectFileColumnCount(raw) {
  const text = await raw.slice(0, 65536).text()
  const line = text.split(/\r?\n/).find(item => item && item.trim())
  return line ? splitDataLine(line, detectDelimiter(line)).length : 0
}

function detectDelimiter(line) {
  if (line.includes('\t') && !line.includes(',')) return '\t'
  if (!line.includes(',') && line.trim().includes(' ')) return ' '
  return ','
}

function splitDataLine(line, delimiter) {
  if (delimiter === ' ') return line.trim().split(/\s+/)
  const values = []
  let current = ''
  let quoted = false
  for (let i = 0; i < line.length; i++) {
    const ch = line[i]
    if (ch === '"') {
      if (quoted && line[i + 1] === '"') {
        current += '"'
        i += 1
      } else {
        quoted = !quoted
      }
    } else if (ch === delimiter && !quoted) {
      values.push(current)
      current = ''
    } else {
      current += ch
    }
  }
  values.push(current)
  return values
}

function processNumber() {
  const proc = detectDialog.process || {}
  const value = Number(proc.process_number || proc.processNumber || proc.process_order || proc.processOrder || 0)
  return Number.isFinite(value) && value > 0 ? value : 0
}

function autoFeatureCol(columnCount) {
  const count = Number(columnCount || 0)
  const number = processNumber()
  if (count <= 1) return count === 1 ? 1 : null
  if (number && number <= count) return number
  return null
}

function autoFeatureColText(row) {
  const value = autoFeatureCol(row?.columnCount)
  return value || '--'
}

function needsFeatureColumnChoice(row) {
  const count = Number(row?.columnCount || 0)
  return count > 1 && !autoFeatureCol(count) && !row?.featureCol
}

function openFeatureColumnDialog(row) {
  featureColumnDialog.row = row
  featureColumnDialog.columnCount = Number(row?.columnCount || 0)
  featureColumnDialog.processNumber = processNumber()
  featureColumnDialog.selected = row?.featureCol || 1
  featureColumnDialog.visible = true
}

function confirmFeatureColumn() {
  if (!featureColumnDialog.selected) {
    ElMessage.warning('请选择需要使用的数据列')
    return
  }
  if (featureColumnDialog.row) {
    featureColumnDialog.row.featureCol = featureColumnDialog.selected
  }
  featureColumnDialog.visible = false
}

function isActiveTaskStatus(value) {
  return ['PENDING', 'RUNNING'].includes(value)
}

function continueDetectPolling() {
  if (taskId.value && isActiveTaskStatus(status.value)) {
    task.startPoll(taskId.value, { mode: isKeyMode.value ? 'key' : 'detect' })
  }
}

function submitDetectDialog() {
  if ((isKeyMode.value && keyTaskActive.value) || (!isKeyMode.value && (detectSubmitting.value || detectTaskActive.value))) {
    continueDetectPolling()
    return
  }
  if (isKeyMode.value) {
    startKeyProcess()
    return
  }
  startDetect()
}

function closeDetectDialog() {
  task.clearPoll()
  detectSubmitting.value = false
  detectRunning.value = false
  keySubmitting.value = false
  keyRunning.value = false
  detectDialog.loading = false
  detectDialog.visible = false
  clearDetectUploads()
}

async function startKeyProcess() {
  if (keySubmitting.value || keyTaskActive.value) {
    continueDetectPolling()
    return
  }
  if (!detectDialog.part?.part_id) {
    ElMessage.warning('识别对象不能为空')
    return
  }
  if (boschKeyForm.enabled && !boschKeyForm.kqcTaskId && !boschKeyForm.kqcOutputDir) {
    ElMessage.warning('请填写 KQC任务ID或KQC输出目录')
    return
  }
  if (!boschKeyForm.enabled && !trainSampleIds.value.length) {
    ElMessage.warning('请至少选择一个 CSV/TXT 文件')
    return
  }
  task.clearPoll()
  keySubmitting.value = true
  keyRunning.value = true
  detectDialog.loading = true
  status.value = 'PENDING'
  error.value = ''
  result.value = {}
  try {
    const part = detectDialog.part
    const partTplId = part.part_template_id || part.partTemplateId
    if (!partTplId) throw new Error('识别对象缺少零件模板ID')
    const targetNodeId = `part_template:${partTplId}`
    const selectedObject = {
      id: targetNodeId,
      level: 'part',
      objectLevel: 'part',
      name: part.part_name || part.part_code || part.part_id,
      path: part.path_text
    }
    const params = {
      dataSelectionMode: 'SELECTED_FILES',
      dataUsage: 'KEY_PROCESS',
      sampleIds: trainSampleIds.value,
      selected_object: selectedObject,
      targetNodeId,
      targetType: 'part',
      targetLevel: 'part',
      targetId: partTplId,
      aircraftId: part.aircraft_id,
      subsystemId: part.subsystem_id,
      equipmentId: part.equipment_id,
      componentId: part.component_id,
      partId: partTplId,
      partInstanceId: part.part_id,
      partCode: part.part_code,
      partName: part.part_name,
      pathText: part.path_text,
      topN: 3,
      threshold: 0.7
    }
    if (boschKeyForm.enabled) {
      const keyKqcTaskId = cleanLocalPath(boschKeyForm.kqcTaskId)
      const keyKqcOutputDir = cleanLocalPath(boschKeyForm.kqcOutputDir)
      boschKeyForm.kqcTaskId = keyKqcTaskId
      boschKeyForm.kqcOutputDir = keyKqcOutputDir
      Object.assign(params, {
        dataSelectionMode: 'BOSCH',
        sampleIds: [],
        kqcTaskId: keyKqcTaskId,
        kqcOutputDir: keyKqcOutputDir,
        keepFreq: boschKeyForm.keepFreq,
        weightThresh: boschKeyForm.weightThresh,
        topN: boschKeyForm.topN
      })
    }
    const res = await startWarningKeyProcessTask({
      import_record_id: targetNodeId,
      dataSelectionMode: boschKeyForm.enabled ? 'BOSCH' : 'SELECTED_FILES',
      sampleIds: boschKeyForm.enabled ? [] : trainSampleIds.value,
      params
    })
    if (!detectDialog.visible) return
    task.acceptTask(res, { mode: 'key' })
    ElMessage.success('关键工序识别任务已提交')
  } catch (e) {
    if (!detectDialog.visible) return
    const msg = getErrorMessage(e, '关键工序识别任务启动失败')
    status.value = 'FAILED'
    error.value = msg
    keyRunning.value = false
    ElMessage.error(msg)
  } finally {
    keySubmitting.value = false
    detectDialog.loading = false
  }
}

function cleanLocalPath(path) {
  return String(path || '').replace(/[\u200e\u200f\u202a-\u202e\u2066-\u2069\ufeff]/g, '').trim()
}

async function startDetect() {
  if (detectSubmitting.value || detectTaskActive.value) {
    continueDetectPolling()
    return
  }
  if (!detectDialog.part?.part_id) {
    ElMessage.warning('检测对象不能为空')
    return
  }
  if (!(detectDialog.process?.process_exec_id || detectDialog.process?.process_id)) {
    ElMessage.warning('请先选择工序')
    return
  }
  const processTrainNumericPath = cleanLocalPath(boschProcessForm.trainNumericPath)
  boschProcessForm.trainNumericPath = processTrainNumericPath
  if (boschProcessForm.enabled && !processTrainNumericPath) {
    ElMessage.warning('请填写 Bosch train_numeric.csv 路径')
    return
  }
  if (!boschProcessForm.enabled && !trainSampleIds.value.length) {
    ElMessage.warning('请至少选择一个训练和测试数据文件')
    return
  }
  if (!boschProcessForm.enabled && !detectSampleIds.value.length) {
    ElMessage.warning('请上传一个检测数据文件')
    return
  }
  const detectRow = detectUploadRows.value[0]
  if (!boschProcessForm.enabled && needsFeatureColumnChoice(detectRow)) {
    openFeatureColumnDialog(detectRow)
    return
  }
  const featureCol = detectRow?.featureCol || autoFeatureCol(detectRow?.columnCount) || processNumber() || 1
  task.clearPoll()
  detectSubmitting.value = true
  detectRunning.value = true
  detectDialog.loading = true
  status.value = 'PENDING'
  error.value = ''
  result.value = {}
  try {
    const part = detectDialog.part
    const proc = detectDialog.process || {}
    const payload = {
      partId: part.part_id,
      partCode: part.part_code,
      partName: part.part_name,
      pathText: part.path_text,
      processId: proc.process_exec_id || proc.process_id,
      processName: proc.process_name,
      processOrder: proc.process_order,
      deviceId: proc.device_id,
      deviceName: proc.device_name,
      dataUsage: boschProcessForm.enabled ? 'BOSCH_PROCESS_ANOMALY' : 'PROCESS_ANOMALY',
      featureCol
    }
    if (boschProcessForm.enabled) {
      Object.assign(payload, {
        boschTrainNumericPath: processTrainNumericPath,
        station: boschProcessForm.station,
        featureColName: boschProcessForm.featureColName,
        maxRows: boschProcessForm.maxRows,
        epochs: boschProcessForm.epochs,
        selectionMode: boschProcessForm.selectionMode,
        alphaSample: boschProcessForm.alphaSample,
        ewmaLambda: boschProcessForm.ewmaLambda,
        minFailedObserved: boschProcessForm.minFailedObserved,
        minObserved: boschProcessForm.minObserved,
        seed: boschProcessForm.seed,
        device: boschProcessForm.device
      })
    } else {
      Object.assign(payload, {
        trainSampleIds: trainSampleIds.value,
        detectSampleIds: detectSampleIds.value,
        sampleIds: selectedSampleIds.value
      })
    }
    const res = await startWarningDetectTask(payload)
    if (!detectDialog.visible) return
    task.acceptTask(res, { mode: 'detect' })
    ElMessage.success('异常检测任务已提交')
    loadDetectResults()
  } catch (e) {
    if (!detectDialog.visible) return
    const msg = getErrorMessage(e, '异常检测任务启动失败')
    status.value = 'FAILED'
    error.value = msg
    detectRunning.value = false
    ElMessage.error(msg)
  } finally {
    detectSubmitting.value = false
    detectDialog.loading = false
  }
}

async function cancelDetectTask() {
  if (!taskId.value || cancelLoading.value) return
  cancelLoading.value = true
  task.clearPoll()
  detectSubmitting.value = false
  detectRunning.value = false
  keySubmitting.value = false
  keyRunning.value = false
  detectDialog.loading = false
  try {
    const api = isKeyMode.value ? cancelWarningKeyProcessTask : cancelWarningDetectTask
    const res = await api(taskId.value)
    const data = getApiPayload(res)
    status.value = data.status || 'CANCELED'
    result.value = data.result || data || {}
    error.value = ''
    ElMessage.success('任务已取消')
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '任务取消失败'))
  } finally {
    detectSubmitting.value = false
    detectRunning.value = false
    keySubmitting.value = false
    keyRunning.value = false
    detectDialog.loading = false
    cancelLoading.value = false
  }
}

function keyField(type) {
  const keys = {
    code: ['keyProcessCode', 'key_process_code', 'processCode', 'process_code'],
    name: ['keyProcessName', 'key_process_name', 'processName', 'process_name'],
    confidence: ['confidence', 'score'],
    score: ['score', 'confidence'],
    reason: ['reason'],
    suggestion: ['suggestion']
  }
  return pickValue(result.value, keys[type] || [type])
}

function keyText(type) {
  return translateKeyProcessText(keyField(type))
}

function translateKeyProcessText(value) {
  const text = String(value || '').trim()
  const map = {
    'Ranked by KQC-weighted direct response impact, path response impact and station PageRank.': '根据KQC加权的直接响应影响、路径响应影响和工序PageRank综合排序。',
    'Prioritize process review and quality control checks for the top ranked station.': '建议优先对排名最高的工序进行工艺复核和质量控制检查。',
    'mock reason': '模拟原因',
    'mock suggestion': '模拟建议'
  }
  return map[text] || value
}

function pickValue(source, keys) {
  if (!source || typeof source !== 'object') return undefined
  for (const key of keys) {
    if (source[key] !== undefined && source[key] !== null && source[key] !== '') return source[key]
  }
  return undefined
}

async function loadDetectResults() {
  resultLoading.value = true
  try {
    const res = await listWarningDetectResults(resultQuery)
    const data = getApiPayload(res)
    resultRows.value = Array.isArray(data.rows) ? data.rows : []
    resultTotal.value = Number(data.total || 0)
  } finally {
    resultLoading.value = false
  }
}

async function loadKeyProcessResults() {
  keyResultLoading.value = true
  try {
    const res = await listKeyProcessResults(keyResultQuery)
    const data = getApiPayload(res)
    keyResultRows.value = Array.isArray(data.rows) ? data.rows : []
    keyResultTotal.value = Number(data.total || 0)
  } finally {
    keyResultLoading.value = false
  }
}

async function lookupDialogTaskResult() {
  const id = cleanLocalPath(lookupTaskId.value)
  if (!id) {
    ElMessage.warning('请输入任务ID')
    return
  }
  lookupTaskId.value = id
  lookupLoading.value = true
  task.clearPoll()
  try {
    const api = isKeyMode.value ? getWarningKeyProcessTask : getWarningDetectTask
    const res = await api(id)
    const data = getApiPayload(res)
    taskId.value = data.taskId || data.task_id || id
    status.value = data.status || ''
    error.value = data.errorMessage || data.error_message || ''
    result.value = data.result && typeof data.result === 'object' ? data.result : data
    if (taskId.value && isActiveTaskStatus(status.value)) {
      task.startPoll(taskId.value, { mode: isKeyMode.value ? 'key' : 'detect' })
    }
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '查询任务结果失败'))
  } finally {
    lookupLoading.value = false
  }
}

function viewHistoryResult(row, mode) {
  if (!row?.taskId && !row?.task_id) return
  task.clearPoll()
  detectSubmitting.value = false
  detectRunning.value = false
  keySubmitting.value = false
  keyRunning.value = false
  detectDialog.loading = false
  detectDialog.mode = mode
  detectDialog.part = {
    part_id: row.targetId || row.target_id || '',
    part_name: row.targetName || row.target_name || row.bizName || ''
  }
  detectDialog.process = mode === 'detect' ? { process_name: row.processName || row.process_name || '' } : null
  detectDialog.visible = true
  lookupTaskId.value = row.taskId || row.task_id
  lookupDialogTaskResult()
}

async function deleteDetectResult(row) {
  await deleteAlgorithmHistory(row, deleteWarningAlgorithmResult, loadDetectResults, '异常检测结果')
}

async function deleteKeyProcessResult(row) {
  await deleteAlgorithmHistory(row, (id) => deleteFaultIdentifyResult(id, { deleteAlgorithmArtifacts: true }), loadKeyProcessResults, '关键工序识别结果')
}

async function deleteAlgorithmHistory(row, api, reload, label) {
  const id = row?.taskId || row?.task_id
  if (!id) return
  try {
    await ElMessageBox.confirm(`确认删除${label} ${id} 吗？本地算法结果和数据库记录都会删除。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch (_) {
    return
  }
  try {
    await api(id)
    ElMessage.success('删除成功')
    await reload()
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '删除失败'))
  }
}

function downloadDataFile(row) {
  const url = row?.dataFileUrl || row?.data_file_url
  if (url) window.open(url, '_blank')
}

async function openManufacturingDataDialog() {
  manufacturingDataDialog.visible = true
  manufacturingDataQuery.pageNum = 1
  await loadManufacturingData()
}

async function searchManufacturingData() {
  manufacturingDataQuery.pageNum = 1
  await loadManufacturingData()
}

async function resetManufacturingData() {
  manufacturingDataQuery.keyword = ''
  manufacturingDataQuery.uploadBatchId = ''
  manufacturingDataQuery.pageNum = 1
  await loadManufacturingData()
}

async function loadManufacturingData() {
  manufacturingDataDialog.loading = true
  try {
    const results = await Promise.all(MANUFACTURING_DATA_USAGES.map(dataUsage => listFaultIdenSamples({
      dataUsage,
      keyword: manufacturingDataQuery.keyword,
      uploadBatchId: manufacturingDataQuery.uploadBatchId,
      pageNum: 1,
      pageSize: 5000
    })))
    const rows = results
      .flatMap(res => Array.isArray(res?.rows) ? res.rows : [])
      .filter(row => MANUFACTURING_DATA_USAGES.includes(row.dataUsage))
    manufacturingDataDialog.total = rows.length
    const start = (manufacturingDataQuery.pageNum - 1) * manufacturingDataQuery.pageSize
    manufacturingDataDialog.rows = rows.slice(start, start + manufacturingDataQuery.pageSize)
  } catch (e) {
    manufacturingDataDialog.rows = []
    manufacturingDataDialog.total = 0
    ElMessage.error('制造周期数据加载失败')
  } finally {
    manufacturingDataDialog.loading = false
  }
}

async function changeManufacturingDataUsage(row, dataUsage) {
  if (!row?.id || row.dataUsage === dataUsage) return
  const oldUsage = row.dataUsage
  row.usageUpdating = true
  try {
    await updateFaultIdenSampleDataUsage(row.id, dataUsage)
    row.dataUsage = dataUsage
    ElMessage.success('数据用途已更新')
  } catch (e) {
    row.dataUsage = oldUsage
    ElMessage.error(e?.response?.data?.msg || e?.message || '数据用途更新失败')
  } finally {
    row.usageUpdating = false
  }
}

async function deleteManufacturingData(row) {
  if (!row?.id || row.deleting) return
  try {
    await ElMessageBox.confirm(`确认删除数据文件 ${row.fileName || row.id} 吗？删除后无法恢复。`, '删除制造周期数据', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch (_) {
    return
  }
  row.deleting = true
  try {
    await deleteFaultIdenSample(row.id)
    selectedSamples.value = selectedSamples.value.filter(item => item.id !== row.id)
    ElMessage.success('数据文件已删除')
    if (manufacturingDataDialog.rows.length <= 1 && manufacturingDataQuery.pageNum > 1) {
      manufacturingDataQuery.pageNum -= 1
    }
    await loadManufacturingData()
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || e?.message || '删除数据文件失败')
  } finally {
    row.deleting = false
  }
}

async function loadDevs() {
  deviceLoading.value = true
  try {
    const res = await listWarningDevices(deviceQuery)
    const payload = res?.data || {}
    const rows = Array.isArray(payload.rows) ? payload.rows : []
    deviceList.value = rows
    if (!selectedDevice.value && rows.length) await pickDev(rows[0])
  } finally {
    deviceLoading.value = false
  }
}

async function loadDevPart(deviceRow) {
  const deviceId = deviceRow?.device_id
  if (!deviceId) {
    devicePartList.value = []
    devicePartTotal.value = 0
    return
  }
  devicePartLoading.value = true
  try {
    const res = await listWarningDeviceParts(deviceId)
    const allRows = Array.isArray(res?.data) ? res.data : []
    devicePartTotal.value = allRows.length
    const start = (devicePartQuery.page_num - 1) * devicePartQuery.page_size
    devicePartList.value = allRows.slice(start, start + devicePartQuery.page_size)
  } finally {
    devicePartLoading.value = false
  }
}

async function pickDev(row) {
  selectedDevice.value = row || null
  devicePartQuery.page_num = 1
  await loadDevPart(row)
}

function queryDev() {
  deviceQuery.page_num = 1
  loadDevs()
}

function resetDev() {
  deviceQuery.keyword = ''
  deviceQuery.risk_level = ''
  deviceQuery.page_num = 1
  loadDevs()
}

function fileSizeText(size) {
  const value = Number(size || 0)
  if (!value) return '--'
  if (value < 1024) return value + ' B'
  if (value < 1024 * 1024) return (value / 1024).toFixed(1) + ' KB'
  return (value / 1024 / 1024).toFixed(1) + ' MB'
}

function val(value) {
  return value === undefined || value === null || value === '' ? '--' : value
}

function formatChartValue(value) {
  const num = Number(value)
  if (!Number.isFinite(num)) return '--'
  return Math.abs(num) >= 100 ? num.toFixed(2) : num.toFixed(4)
}

function renderAnomalyChart() {
  if (!detectDialog.visible || !anomalyChartRef.value || isKeyMode.value || !curveRows.value.length) return
  if (anomalyChart && anomalyChart.getDom() !== anomalyChartRef.value) {
    anomalyChart.dispose()
    anomalyChart = null
  }
  if (!anomalyChart) {
    anomalyChart = echarts.init(anomalyChartRef.value)
  }

  const xData = curveRows.value.map(item => String(item.x ?? ''))
  const yData = curveRows.value.map(item => Number(item.value))
  const threshold = curveThreshold.value
  const lineColor = '#2f80ed'
  const alarmColor = '#e85d5d'
  const markLineData = threshold === null ? [] : [{
    yAxis: threshold,
    name: '阈值',
    lineStyle: { color: alarmColor, type: 'dashed', width: 2 },
    label: {
      formatter: `阈值 ${formatChartValue(threshold)}`,
      color: alarmColor,
      fontWeight: 600
    }
  }]

  anomalyChart.setOption({
    backgroundColor: 'transparent',
    color: [lineColor],
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(17, 24, 39, 0.92)',
      borderWidth: 0,
      textStyle: { color: '#fff' },
      axisPointer: { type: 'line', lineStyle: { color: '#9bb8d8', width: 1 } },
      formatter(params) {
        const point = params?.[0]
        if (!point) return ''
        return `样本点：${point.axisValue}<br/>异常分数：${formatChartValue(point.data)}`
      }
    },
    grid: { left: 46, right: 24, top: 28, bottom: 54 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: xData,
      axisLine: { lineStyle: { color: '#c9d6e2' } },
      axisTick: { show: false },
      axisLabel: { color: '#607081' }
    },
    yAxis: {
      type: 'value',
      min: value => Math.min(0, value.min),
      axisLabel: { color: '#607081', formatter: value => formatChartValue(value) },
      splitLine: { lineStyle: { color: '#e7edf3', type: 'dashed' } }
    },
    dataZoom: [
      { type: 'inside', throttle: 50 },
      { type: 'slider', height: 18, bottom: 14, borderColor: '#dbe4ee', fillerColor: 'rgba(47, 128, 237, 0.18)' }
    ],
    series: [{
      name: '异常分数',
      type: 'line',
      data: yData,
      smooth: true,
      showSymbol: yData.length <= 80,
      symbol: 'circle',
      symbolSize: 5,
      lineStyle: { width: 3, color: lineColor },
      itemStyle: { color: lineColor },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(47, 128, 237, 0.26)' },
          { offset: 1, color: 'rgba(47, 128, 237, 0.03)' }
        ])
      },
      markPoint: {
        symbolSize: 52,
        itemStyle: { color: alarmColor },
        label: { color: '#fff', formatter: '峰值' },
        data: [{ type: 'max', name: '峰值' }]
      },
      markLine: {
        symbol: 'none',
        data: markLineData
      }
    }]
  }, true)
  anomalyChart.resize()
}

function scheduleAnomalyChartRender() {
  nextTick(() => renderAnomalyChart())
}

function resizeAnomalyChart() {
  anomalyChart?.resize()
}

function formatProcess(row) {
  const order = row?.process_order
  const name = row?.process_name
  if (order !== undefined && order !== null && order !== '' && name) return `${order}. ${name}`
  return name || '--'
}

watch([curveRows, curveThreshold, () => detectDialog.visible, isKeyMode], scheduleAnomalyChartRender)

onMounted(() => {
  loadParts()
  loadDevs()
  loadDetectResults()
  loadKeyProcessResults()
  window.addEventListener('resize', resizeAnomalyChart)
})

onBeforeUnmount(() => {
  task.clearPoll()
  detectSubmitting.value = false
  detectRunning.value = false
  keySubmitting.value = false
  keyRunning.value = false
  window.removeEventListener('resize', resizeAnomalyChart)
  anomalyChart?.dispose()
  anomalyChart = null
})
</script>

<style scoped>
.feedback-hero {
  border-top: 4px solid #4c8fdc;
}

.hero-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.warning-section {
  margin-bottom: 18px;
  border-top: 4px solid #7fb0ea;
}

.inner-card {
  min-height: 480px;
  border-radius: 12px;
}

.inner-title {
  position: relative;
  padding-left: 12px;
  font-size: 17px;
  font-weight: 700;
  color: #1f2d3d;
}

.inner-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 3px;
  width: 4px;
  height: 18px;
  background: #4c8fdc;
  border-radius: 4px;
}

.selected-info {
  margin-bottom: 14px;
}

.instance-panel {
  margin-top: 18px;
}

.instance-header,
.card-header-row,
.detect-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.task-lookup-row {
  display: flex;
  gap: 10px;
  margin: 12px 0 16px;
}

.task-lookup-row .el-input {
  max-width: 320px;
}

.sub-title {
  font-size: 15px;
}

.instance-meta,
.selected-path,
.process-desc,
.process-device,
.info-desc,
.detect-data-count {
  color: #607081;
}

.selected-path,
.process-desc,
.process-device,
.info-desc {
  line-height: 1.8;
}

.info-box {
  padding: 14px 16px;
  border: 1px solid #dbe4ee;
  border-radius: 8px;
  background: #fbfdff;
}

.info-label {
  color: #607081;
  font-size: 13px;
  margin-bottom: 6px;
}

.info-value {
  font-size: 16px;
  font-weight: 700;
  color: #1f2d3d;
}

.process-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 455px;
  overflow-y: auto;
  padding-right: 6px;
}

.process-card {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid #dbe4ee;
  border-radius: 8px;
  background: #fff;
}

.process-main {
  min-width: 0;
  flex: 1;
}

.process-title {
  font-weight: 700;
  color: #1f2d3d;
  margin-bottom: 6px;
}

.process-actions,
.file-toolbar,
.data-file-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.process-actions {
  justify-content: flex-end;
}

.file-toolbar,
.data-file-toolbar {
  margin: 12px 0;
}

.detect-result {
  margin-top: 16px;
}

.anomaly-chart-panel {
  padding: 14px 16px 10px;
  border: 1px solid #dbe4ee;
  border-radius: 8px;
  background: linear-gradient(180deg, #fbfdff 0%, #f5f9fd 100%);
}

.anomaly-chart-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 8px;
}

.anomaly-chart-title {
  font-size: 15px;
  font-weight: 700;
  color: #1f2d3d;
}

.anomaly-chart-subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: #607081;
}

.anomaly-chart-stats {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
  color: #3d566e;
  font-size: 12px;
}

.anomaly-chart-stats span {
  padding: 4px 8px;
  border: 1px solid #dbe4ee;
  border-radius: 999px;
  background: #fff;
}

.anomaly-chart {
  width: 100%;
  height: 320px;
}

.task-progress-row {
  margin-bottom: 12px;
}

.task-progress-message {
  margin-top: 6px;
  color: #607081;
  font-size: 13px;
}

.detect-data-section {
  margin-top: 14px;
  padding: 14px;
  border: 1px solid #dbe4ee;
  border-radius: 8px;
  background: #fbfdff;
}

.detect-data-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.detect-data-title {
  font-size: 15px;
  font-weight: 700;
  color: #1f2d3d;
}

.execution-detail-head {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 14px;
  align-items: stretch;
  margin-bottom: 14px;
}

.execution-detail-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 10px;
}

.part-detail-layout {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 18px;
  align-items: start;
}

.part-image-panel {
  padding: 16px;
  border: 1px solid #dbe4ee;
  border-radius: 10px;
  background: #fbfdff;
}

.part-image-title {
  margin-bottom: 12px;
  font-size: 15px;
  font-weight: 700;
  color: #1f2d3d;
}

.part-image-uploader,
.part-image-uploader :deep(.el-upload) {
  width: 100%;
}

.part-image-box {
  width: 100%;
  aspect-ratio: 4 / 3;
  overflow: hidden;
  border: 1px dashed #b8c7d9;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
}

.part-image-box img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
  background: #f6f8fb;
}

.part-image-empty {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #607081;
}

.part-image-plus {
  width: 34px;
  height: 34px;
  line-height: 30px;
  border: 1px solid #9bb8da;
  border-radius: 50%;
  color: #4c8fdc;
  font-size: 26px;
  text-align: center;
}

.part-info-panel {
  min-width: 0;
}

@media (max-width: 900px) {
  .part-detail-layout,
  .execution-detail-head {
    grid-template-columns: 1fr;
  }
}
</style>
