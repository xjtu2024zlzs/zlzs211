<template>
  <div v-loading="loading" class="app-container dossier-detail-page">
    <div class="page-head">
      <div>
        <h2>{{ pageTitle }}</h2>
        <div class="subline">
          <span>{{ context.templateCode || '-' }} / {{ context.templateVersion || '-' }}</span>
          <span>{{ context.versionLabel || '-' }}</span>
          <span>{{ context.jobCode || '-' }}</span>
          <span>{{ context.createdAt || '-' }}</span>
        </div>
      </div>
      <div class="actions">
        <el-select v-model="selectedVersionId" placeholder="版本" class="version-select" @change="handleVersionChange">
          <el-option v-for="item in versions" :key="item.versionId" :label="versionLabel(item)" :value="item.versionId" />
        </el-select>
        <el-button icon="Refresh" :loading="loading" @click="loadDetail">刷新</el-button>
        <el-button icon="DocumentChecked">版本对比</el-button>
        <el-button type="primary" icon="Download" :loading="exporting" :disabled="!context.instanceId" @click="openExportDialog">导出</el-button>
      </div>
    </div>

    <el-dialog v-model="exportDialogVisible" title="选择导出格式" width="520px" append-to-body>
      <div class="export-format-list">
        <button
          v-for="item in exportFormats"
          :key="item.type"
          type="button"
          :class="['export-format-item', { active: selectedExportFormat === item.type, disabled: !item.enabled }]"
          :disabled="!item.enabled"
          @click="selectExportFormat(item)"
        >
          <div>
            <strong>{{ item.label }}</strong>
            <span>{{ item.description }}</span>
          </div>
          <el-tag size="small" :type="item.enabled ? 'success' : 'info'">{{ item.enabled ? '可导出' : '暂未开放' }}</el-tag>
        </button>
      </div>
      <template #footer>
        <el-button @click="exportDialogVisible = false">取消</el-button>
        <el-button type="primary" icon="Download" :loading="exporting" :disabled="!selectedExportFormat" @click="handleExportConfirm">导出</el-button>
      </template>
    </el-dialog>

    <div class="summary-strip">
      <div v-for="item in metricCards" :key="item.label" class="metric-cell">
        <strong>{{ item.value }}</strong>
        <span>{{ item.label }}</span>
      </div>
    </div>

    <div class="workspace">
      <aside class="left-stack">
        <section class="panel">
          <div class="panel-head">
            <span>{{ directory.title || '卷宗目录' }}</span>
            <el-tag size="small">{{ directory.count || directoryRows.length }}</el-tag>
          </div>
          <div class="panel-body">
            <el-input v-model="directoryKeyword" placeholder="搜索目录" clearable />
            <div class="directory-list">
              <button
                v-for="item in directoryRows"
                :key="item.key"
                :class="['directory-row', { active: activeDirectoryKey === item.key }]"
                type="button"
                @click="selectDirectory(item)"
              >
                <span>{{ item.orderNo }}</span>
                <strong>{{ item.label }}</strong>
                <el-tag v-if="isCompositionRow(item)" size="small" type="primary">{{ item.key === 'bom' ? 'BOM' : '组成' }}</el-tag>
              </button>
            </div>
          </div>
        </section>

        <section class="panel">
          <div class="panel-head">
            <span>当前对象</span>
            <el-tag size="small" type="success">{{ currentNode.levelName || '-' }}</el-tag>
          </div>
          <div class="panel-body current-object">
            <strong>{{ currentNode.partName || '-' }}</strong>
            <span>{{ currentNode.partNumber || '-' }}</span>
            <span>{{ currentNode.serialNumber || currentNode.positionCode || '-' }}</span>
            <div class="meta-action-list">
              <button v-for="item in metaActions" :key="item.key" type="button" class="meta-action" @click="openMetaPanel(item.key)">
                <span>{{ item.label }}</span>
                <el-tag size="small" :type="item.type">{{ item.value }}</el-tag>
              </button>
            </div>
          </div>
        </section>
      </aside>

      <main class="center-stack">
        <div class="path-toolbar">
          <div class="path-main">
            <el-tag size="small">{{ currentNode.levelName || '节点' }}</el-tag>
            <div class="path-line">
              <button
                v-for="node in bomPath"
                :key="node.nodeId"
                type="button"
                :class="{ active: node.nodeId === currentNode.nodeId }"
                @click="selectBomNode(node)"
              >
                {{ node.partName }}
              </button>
            </div>
          </div>
          <div v-if="isCompositionDirectory" class="bom-search compact">
            <el-input
              v-model="bomKeyword"
              placeholder="名称 / 编号 / ATA / 序列号 / 位置"
              clearable
              @keyup.enter="handleBomSearch"
            />
            <el-button icon="Search" @click="handleBomSearch">搜索</el-button>
            <el-button icon="Aim" @click="resetToRoot">整机</el-button>
          </div>
          <el-button :disabled="!canBackParent" icon="Back" @click="selectParentNode">返回上级</el-button>
        </div>

        <div v-if="isCompositionDirectory && searchResults.length" class="search-results compact-results">
          <button v-for="item in searchResults" :key="item.nodeId" type="button" @click="selectBomNode(item)">
            <strong>{{ item.partName }}</strong>
            <span>{{ item.partNumber }} / {{ item.levelName }}</span>
          </button>
        </div>

        <section v-if="isCompositionDirectory" class="panel bom-browser">
          <div class="panel-head">
            <span>BOM树</span>
            <el-tag size="small" type="primary">按父节点加载</el-tag>
          </div>
          <div class="panel-body bom-grid">
            <el-tree
              v-if="currentNode.nodeId"
              ref="bomTreeRef"
              :key="bomTreeKey"
              node-key="nodeId"
              lazy
              highlight-current
              :load="loadBomTree"
              :props="treeProps"
              @node-click="selectBomNode"
            >
              <template #default="{ data }">
                <div class="tree-node">
                  <span>{{ data.partName }}</span>
                  <small>{{ data.partNumber }}</small>
                  <el-tag size="small">{{ data.levelName }}</el-tag>
                </div>
              </template>
            </el-tree>
            <div class="node-snapshot">
              <div class="snapshot-title">
                <strong>{{ currentNode.partName }}</strong>
                <el-tag size="small" type="success">{{ currentNode.status }}</el-tag>
              </div>
              <div class="field-grid">
                <div v-for="item in detail.basicFields || []" :key="item.label" class="field-item">
                  <label>{{ item.label }}</label>
                  <span>{{ item.value }}</span>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section v-if="showDetailPanel" class="panel detail-panel">
          <div class="panel-head">
            <div>
              <span>{{ directoryView.title }}</span>
              <small>{{ directoryView.subtitle }}</small>
            </div>
            <el-tag size="small" :type="directoryView.statusType">{{ directoryView.status }}</el-tag>
          </div>
          <div class="panel-body">
            <div v-if="directoryCards.length" class="param-grid">
              <div v-for="item in directoryCards" :key="item.label" class="param-card">
                <label>{{ item.label }}</label>
                <strong>{{ item.value }}</strong>
              </div>
            </div>

            <div v-if="directoryTables.length" class="table-stack">
              <div v-for="table in directoryTables" :key="table.title" class="data-block">
                <div class="block-title">{{ table.title }}</div>
                <el-table :data="table.rows" border>
                  <el-table-column prop="name" label="项目" min-width="150" />
                  <el-table-column prop="value" label="内容" min-width="260" show-overflow-tooltip />
                  <el-table-column prop="status" label="状态" width="100" align="center">
                    <template #default="{ row }">
                      <el-tag size="small" :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>

            <div v-if="showContentList" class="data-block">
              <div class="block-title">内容明细</div>
              <el-table :data="categoryContentItems" border>
                <el-table-column prop="itemName" label="内容名称" min-width="220" show-overflow-tooltip />
                <el-table-column prop="lifecycleStage" label="阶段" width="130" />
                <el-table-column prop="sourceSystem" label="来源系统" width="110" />
                <el-table-column prop="sourceRecordKey" label="追溯键" min-width="180" show-overflow-tooltip />
                <el-table-column prop="completenessStatus" label="完整性" width="100" align="center">
                  <template #default="{ row }">
                    <el-tag size="small" :type="statusType(row.completenessStatus)">{{ statusLabel(row.completenessStatus) }}</el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <div v-if="showDocumentList" class="data-block">
              <div class="block-title">附件材料</div>
              <el-table :data="displayDocuments" border>
                <el-table-column prop="fileType" label="类型" width="80" align="center" />
                <el-table-column prop="title" label="文件名称" min-width="200" show-overflow-tooltip />
                <el-table-column prop="sourceSystem" label="来源" width="100" />
                <el-table-column prop="sourceRecordKey" label="追溯键" min-width="220" show-overflow-tooltip />
                <el-table-column prop="documentStatus" label="状态" width="90" align="center">
                  <template #default="{ row }">
                    <el-tag size="small" :type="statusType(row.documentStatus)">{{ statusLabel(row.documentStatus) }}</el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </section>
      </main>

    </div>

    <el-dialog v-model="metaDialogVisible" :title="metaDialogTitle" width="720px" append-to-body>
      <div v-if="activeMetaPanel === 'version'" class="version-info dialog-info">
        <div><label>卷宗版本</label><span>{{ context.versionLabel || '-' }}</span></div>
        <div><label>生成任务</label><span>{{ context.jobCode || '-' }}</span></div>
        <div><label>数据快照</label><span>{{ context.snapshotCode || '-' }}</span></div>
        <div><label>版本原因</label><span>{{ versionReasonLabel(context.versionReason) }}</span></div>
        <div><label>生成时间</label><span>{{ context.createdAt || '-' }}</span></div>
      </div>

      <div v-else-if="activeMetaPanel === 'files'" class="file-list dialog-list">
        <div v-for="item in filePackages" :key="item.name" class="file-row">
          <span>{{ item.type }}</span>
          <strong>{{ item.name }}</strong>
          <small>{{ item.summary }} / {{ item.size }}</small>
        </div>
      </div>

      <el-table v-else-if="activeMetaPanel === 'sources'" :data="dataSources" border>
        <el-table-column prop="sourceSystem" label="来源系统" width="120" />
        <el-table-column prop="sourceTable" label="来源表" min-width="220" show-overflow-tooltip />
        <el-table-column prop="recordCount" label="记录数" width="100" align="right" />
      </el-table>

      <el-table v-else-if="activeMetaPanel === 'logs'" :data="operationLogs" border>
        <el-table-column prop="operatedAt" label="时间" width="170" />
        <el-table-column prop="operationName" label="操作" min-width="150" />
        <el-table-column prop="operatorName" label="操作人" width="120" />
        <el-table-column prop="operationStatus" label="状态" width="110" align="center" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup name="DossierDetail">
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { saveAs } from 'file-saver'
import {
  getCurrentDossierDetail,
  getDossierNodeDetail,
  listBomChildren,
  searchBomNodes
} from '@/api/project1/dossier/detail'
import { exportDossier } from '@/api/project1/dossier/instance'

const route = useRoute()
const loading = ref(false)
const exporting = ref(false)
const exportDialogVisible = ref(false)
const selectedExportFormat = ref('pdf')
const directoryKeyword = ref('')
const bomKeyword = ref('')
const activeDirectoryKey = ref('bom')
const activeTab = ref('data')
const selectedInstanceId = ref(route.query.instanceId || '')
const selectedVersionId = ref(route.query.versionId || '')
const detailData = ref({})
const searchResults = ref([])
const bomTreeRef = ref(null)
const bomTreeKey = ref(0)
const metaDialogVisible = ref(false)
const activeMetaPanel = ref('')

const exportFormats = [
  {
    type: 'pdf',
    label: 'PDF 文档',
    description: '适合直接查看、打印和归档。',
    extension: 'pdf',
    fileKey: 'fileName',
    mimeType: 'application/pdf',
    enabled: true
  },
  {
    type: 'zip',
    label: 'ZIP 完整包',
    description: '包含 PDF、数据快照和导出说明。',
    extension: 'zip',
    fileKey: 'packageName',
    mimeType: 'application/zip',
    enabled: true
  },
  {
    type: 'docx',
    label: 'Word 文档',
    description: '适合后续编辑和流转。',
    extension: 'docx',
    enabled: false
  },
  {
    type: 'xlsx',
    label: 'Excel 清单',
    description: '适合导出目录、数据项和附件清单。',
    extension: 'xlsx',
    enabled: false
  },
  {
    type: 'html',
    label: 'HTML 网页',
    description: '适合离线浏览和轻量分享。',
    extension: 'html',
    enabled: false
  }
]

const treeProps = {
  label: 'label',
  isLeaf: 'leaf'
}

const context = computed(() => detailData.value.context || {})
const metrics = computed(() => detailData.value.metrics || {})
const versions = computed(() => detailData.value.versions || [])
const directory = computed(() => detailData.value.directory || {})
const currentNode = computed(() => detailData.value.currentNode || {})
const bomPath = computed(() => detailData.value.bomPath || [])
const currentChildren = computed(() => detailData.value.bomChildren || [])
const detail = computed(() => detailData.value.detail || {})
const contentItems = computed(() => detailData.value.contentItems || [])
const documents = computed(() => detailData.value.documents || [])
const displayDocuments = computed(() => {
  const rows = [...documents.value]
  contentItems.value
    .filter(item => isDocumentContent(item))
    .forEach(item => {
      rows.push({
        documentEntryId: item.contentItemId,
        fileType: fileType(item.fileStorageKey),
        title: item.itemName,
        sourceSystem: item.sourceSystem,
        sourceRecordKey: item.sourceRecordKey,
        documentStatus: item.itemStatus || 'active',
        completenessStatus: item.completenessStatus,
        fileStorageKey: item.fileStorageKey
      })
    })
  return rows
})
const dataSources = computed(() => detailData.value.dataSources || [])
const operationLogs = computed(() => detailData.value.operationLogs || [])
const filePackages = computed(() => detailData.value.filePackages || [])

const pageTitle = computed(() => {
  const tailNumber = context.value.tailNumber || 'B-1234'
  return `${tailNumber} 单台份飞机综合卷宗`
})

const directoryMetricCount = computed(() => {
  return metrics.value.directoryCount || metrics.value.chapterCount || directory.value.count || (directory.value.rows || []).length || 0
})

const metricCards = computed(() => [
  { label: '目录', value: directoryMetricCount.value },
  { label: 'BOM节点', value: metrics.value.bomNodeCount || 0 },
  { label: '数据记录', value: metrics.value.sourceRecordCount || 0 },
  { label: '文件', value: metrics.value.fileCount || 0 },
  { label: '阻断项', value: metrics.value.blockingCount || 0 }
])

const metaActions = computed(() => [
  { key: 'version', label: '版本信息', value: context.value.versionLabel || '-', type: 'primary' },
  { key: 'files', label: '文件包', value: metrics.value.fileCount || filePackages.value.length || 0, type: 'success' },
  { key: 'sources', label: '数据来源', value: dataSources.value.length || 0, type: '' },
  { key: 'logs', label: '操作记录', value: operationLogs.value.length || 0, type: '' }
])

const metaDialogTitle = computed(() => {
  return (metaActions.value.find(item => item.key === activeMetaPanel.value) || {}).label || '卷宗信息'
})

const directoryRows = computed(() => {
  const rows = directory.value.rows || []
  if (!directoryKeyword.value) {
    return rows
  }
  return rows.filter(item => item.label.includes(directoryKeyword.value))
})

const canBackParent = computed(() => bomPath.value.length > 1)

const activeDirectoryItem = computed(() => {
  return (directory.value.rows || []).find(item => item.key === activeDirectoryKey.value) || {}
})

const activeDirectoryLabel = computed(() => {
  return activeDirectoryItem.value.label || ''
})

const isCompositionDirectory = computed(() => isCompositionRow(activeDirectoryItem.value))

const activeDirectoryCategory = computed(() => directoryCategory(activeDirectoryItem.value))

const directoryView = computed(() => {
  const label = activeDirectoryLabel.value || '目录数据'
  return {
    title: label,
    subtitle: `${currentNode.value.levelName || '-'} / ${currentNode.value.partName || '-'}`,
    status: isCompositionDirectory.value ? '按层级加载' : (detail.value.status || '有效'),
    statusType: isCompositionDirectory.value ? 'primary' : 'success'
  }
})

const directoryCards = computed(() => buildDirectoryCards(activeDirectoryCategory.value))

const directoryTables = computed(() => buildDirectoryTables(activeDirectoryCategory.value))

const categoryContentItems = computed(() => filterContentItemsByCategory(activeDirectoryCategory.value))

const showDocumentList = computed(() => activeDirectoryCategory.value === 'documents' && displayDocuments.value.length > 0)

const showContentList = computed(() => {
  return !['composition', 'documents'].includes(activeDirectoryCategory.value) && categoryContentItems.value.length > 0
})

const showDetailPanel = computed(() => !isCompositionDirectory.value)

function selectDirectory(item) {
  activeDirectoryKey.value = item.key
  if (isCompositionRow(item)) {
    bomTreeKey.value += 1
    setCurrentTreeNode()
  } else {
    searchResults.value = []
  }
}

function openMetaPanel(key) {
  activeMetaPanel.value = key
  metaDialogVisible.value = true
}

async function loadDetail() {
  loading.value = true
  try {
    const res = await getCurrentDossierDetail({
      instanceId: selectedInstanceId.value,
      versionId: selectedVersionId.value
    })
    detailData.value = res.data || {}
    selectedInstanceId.value = context.value.instanceId || selectedInstanceId.value || ''
    selectedVersionId.value = context.value.versionId || ''
    activeDirectoryKey.value = 'bom'
    activeTab.value = 'data'
    searchResults.value = []
    bomTreeKey.value += 1
    await setCurrentTreeNode()
  } finally {
    loading.value = false
  }
}

async function handleVersionChange() {
  await loadDetail()
}

function openExportDialog() {
  const firstEnabled = exportFormats.find(item => item.enabled)
  selectedExportFormat.value = firstEnabled?.type || ''
  exportDialogVisible.value = true
}

function selectExportFormat(item) {
  if (!item.enabled) {
    ElMessage.info(`${item.label}暂未开放`)
    return
  }
  selectedExportFormat.value = item.type
}

async function handleExportConfirm() {
  const option = exportFormats.find(item => item.type === selectedExportFormat.value)
  if (!option || !option.enabled) {
    ElMessage.warning('请选择可导出的格式')
    return
  }
  await handleExport(option)
}

async function handleExport(option) {
  if (!context.value.instanceId || !context.value.versionId) {
    ElMessage.warning('当前卷宗还没有可导出的版本')
    return
  }
  exporting.value = true
  try {
    const params = { versionId: context.value.versionId }
    const output = context.value.output || {}
    const filename = output[option.fileKey] || `${context.value.tailNumber || 'dossier'}_${context.value.versionLabel || 'current'}.${option.extension}`
    const data = await exportDossier(context.value.instanceId, option.type, params)
    saveAs(new Blob([data], { type: option.mimeType }), filename)
    exportDialogVisible.value = false
    ElMessage.success(`${option.label}导出完成`)
  } finally {
    exporting.value = false
  }
}

async function selectBomNode(node) {
  if (!node || !node.nodeId) {
    return
  }
  loading.value = true
  try {
    const res = await getDossierNodeDetail({
      instanceId: context.value.instanceId,
      versionId: context.value.versionId,
      bomNodeId: node.nodeId
    })
    const payload = res.data || {}
    detailData.value = {
      ...detailData.value,
      ...payload
    }
    activeDirectoryKey.value = payload.currentNode && payload.currentNode.objectLevel === 'aircraft' ? 'bom' : 'basic'
    activeTab.value = 'data'
    searchResults.value = []
    bomTreeKey.value += 1
    await setCurrentTreeNode()
  } finally {
    loading.value = false
  }
}

async function loadBomTree(node, resolve) {
  try {
    if (node.level === 0) {
      resolve(currentNode.value.nodeId ? [currentNode.value] : [])
      return
    }
    const res = await listBomChildren({
      aircraftId: context.value.aircraftId,
      instanceId: context.value.instanceId,
      versionId: context.value.versionId,
      parentId: node.data.nodeId
    })
    resolve(res.data || [])
  } catch (e) {
    resolve([])
  }
}

async function handleBomSearch() {
  if (!bomKeyword.value) {
    searchResults.value = []
    return
  }
  const res = await searchBomNodes({
    aircraftId: context.value.aircraftId,
    instanceId: context.value.instanceId,
    versionId: context.value.versionId,
    keyword: bomKeyword.value
  })
  searchResults.value = res.data || []
  if (!searchResults.value.length) {
    ElMessage.info('没有匹配的BOM节点')
  }
}

function selectParentNode() {
  if (!canBackParent.value) {
    return
  }
  selectBomNode(bomPath.value[bomPath.value.length - 2])
}

function resetToRoot() {
  if (bomPath.value.length) {
    selectBomNode(bomPath.value[0])
  }
}

function isCompositionRow(item) {
  const label = item && item.label ? item.label : ''
  const key = item && item.key ? item.key : ''
  return ['bom', 'composition'].includes(key) || label.includes('BOM') || label.includes('结构') || label.includes('组成')
}

function directoryCategory(item) {
  const label = item && item.label ? item.label : ''
  if (isCompositionRow(item)) return 'composition'
  if (label.includes('附件') || label.includes('证明')) return 'documents'
  if (label.includes('故障')) return 'fault'
  if (label.includes('检验')) return 'inspection'
  if (label.includes('制造') || label.includes('追溯') || label.includes('装配')) return 'manufacturing'
  if (label.includes('装机') || label.includes('服役') || label.includes('使用') || label.includes('履历')) return 'service'
  if (label.includes('维修')) return 'fault'
  if (label.includes('技术')) return 'status'
  if (label.includes('接口')) return 'interface'
  if (label.includes('设计')) return 'design'
  if (label.includes('基本') || label.includes('概况')) return 'basic'
  return 'content'
}

function buildDirectoryCards(category) {
  if (category === 'basic') {
    return detail.value.basicFields || []
  }
  if (category === 'composition') {
    return []
  }
  if (category === 'documents') {
    return [
      fieldItem('文件数量', displayDocuments.value.length),
      fieldItem('目录对象', currentNode.value.partName || '-'),
      fieldItem('数据来源', 'document_entry'),
      fieldItem('存储状态', displayDocuments.value.length ? '已挂接' : '待补充')
    ]
  }
  return [
    fieldItem('目录对象', activeDirectoryLabel.value || '-'),
    fieldItem('当前节点', currentNode.value.partName || '-'),
    fieldItem('数据来源', sourceByCategory(category)),
    fieldItem('展示方式', displayModeByCategory(category))
  ]
}

function buildDirectoryTables(category) {
  if (category === 'documents') {
    return []
  }
  if (category === 'composition') {
    return []
  }
  const tables = detail.value.tables || []
  const exact = tables.filter(table => tableMatchesLabel(table, activeDirectoryLabel.value))
  if (exact.length) {
    return exact
  }
  const matched = tables.filter(table => tableMatchesCategory(table, category, activeDirectoryLabel.value))
  if (matched.length) {
    return matched
  }
  return [buildFallbackTable(category)]
}

function buildFallbackTable(category) {
  return {
    title: activeDirectoryLabel.value || '目录数据',
    rows: [
      rowItem('目录对象', `${currentNode.value.partName || '-'} / ${currentNode.value.partNumber || '-'}`, '有效'),
      rowItem('数据来源', sourceByCategory(category), '有效'),
      rowItem('生成版本', context.value.versionLabel || '-', '已固化'),
      rowItem('快照状态', context.value.snapshotCode || '已随卷宗版本固化', '有效')
    ]
  }
}

function tableMatchesCategory(table, category, label) {
  const title = table.title || ''
  if (category === 'basic') return title.includes('基础')
  if (category === 'design') return title.includes('设计')
  if (category === 'manufacturing') return title.includes('制造') || title.includes('装配')
  if (category === 'inspection') return title.includes('检验') || title.includes('试验')
  if (category === 'service') return title.includes('服役') || title.includes('装机') || title.includes('履历') || title.includes('使用')
  if (category === 'fault') return title.includes('故障') || title.includes('维修') || title.includes('服役与故障')
  if (category === 'status') return title.includes('技术') || title.includes('状态')
  if (category === 'interface') return title.includes('接口') || label.includes('接口')
  return false
}

function tableMatchesLabel(table, label) {
  const title = table.title || ''
  if (!label) return false
  if (title === label) return true
  if (label.includes('基本') && title.includes('基础')) return true
  if (label.includes('附件') && title.includes('附件')) return true
  if (label.includes('证明') && title.includes('证明')) return true
  return false
}

function sourceByCategory(category) {
  const map = {
    basic: 'aircraft_bom_node / physical_aircraft',
    composition: 'aircraft_bom_node',
    design: 'part_parameter_value / design_document',
    manufacturing: 'manufacturing_record',
    inspection: 'inspection_record',
    service: 'service_record / install_record',
    fault: 'fault_event / maintenance_record',
    status: 'configuration_change',
    interface: 'interface_control_document',
    documents: 'document_entry'
  }
  return map[category] || 'dossier_content_item'
}

function displayModeByCategory(category) {
  const map = {
    design: '参数卡片 + 明细表',
    manufacturing: '工序记录 + 追溯表',
    inspection: '检验结论 + 记录表',
    service: '履历时间线 + 明细表',
    fault: '事件记录 + 闭环状态',
    status: '状态清单 + 变更记录',
    interface: '接口清单 + 关联关系'
  }
  return map[category] || '概要卡片 + 明细表'
}

function filterContentItemsByCategory(category) {
  if (category === 'composition' || category === 'documents') {
    return []
  }
  return contentItems.value.filter(item => {
    if (item.itemType === 'key_node_summary') {
      return category === 'basic' || category === 'content'
    }
    const stage = String(item.lifecycleStage || '').toUpperCase()
    const itemType = String(item.itemType || '').toLowerCase()
    const sourceTable = String(item.sourceTable || '').toLowerCase()
    if (category === 'basic') return true
    if (category === 'design') return ['DESIGN', 'INTERFACE', 'TECHNICAL_STATUS'].includes(stage)
    if (category === 'manufacturing') return ['MANUFACTURING', 'INSTALLATION'].includes(stage) || sourceTable.includes('shop_order')
    if (category === 'inspection') return stage === 'INSPECTION' || sourceTable.includes('inspection')
    if (category === 'service') return stage === 'SERVICE' || itemType.includes('work_order') || sourceTable.includes('life_usage')
    if (category === 'fault') return stage === 'FAULT' || itemType.includes('fault') || sourceTable.includes('fault')
    if (category === 'status') return stage === 'TECHNICAL_STATUS' || itemType.includes('status')
    if (category === 'interface') return stage === 'INTERFACE' || itemType.includes('interface') || sourceTable.includes('interface')
    return true
  })
}

function isDocumentContent(item) {
  const stage = String(item.lifecycleStage || '').toUpperCase()
  const itemType = String(item.itemType || '').toLowerCase()
  const sourceTable = String(item.sourceTable || '').toLowerCase()
  return stage === 'DOCUMENT' || itemType.includes('document') || sourceTable.includes('document') || !!item.fileStorageKey
}

function fileType(fileStorageKey) {
  const key = String(fileStorageKey || '').toLowerCase()
  if (key.endsWith('.pdf')) return 'PDF'
  if (key.endsWith('.zip')) return 'ZIP'
  if (key.endsWith('.jpg') || key.endsWith('.jpeg') || key.endsWith('.png')) return 'IMG'
  return 'DOC'
}

function fieldItem(label, value) {
  return { label, value }
}

function rowItem(name, value, status) {
  return { name, value, status }
}

async function setCurrentTreeNode() {
  await nextTick()
  if (bomTreeRef.value && currentNode.value.nodeId) {
    bomTreeRef.value.setCurrentKey(currentNode.value.nodeId)
  }
}

function versionLabel(item) {
  const current = Number(item.isCurrent) === 1 ? ' 当前' : ''
  return `${item.versionLabel} / ${item.templateVersion}${current}`
}

function statusType(status) {
  if (['complete', 'active', '有效', '通过', '完成', '齐套', 'succeeded'].includes(status)) return 'success'
  if (['warning', '提醒'].includes(status)) return 'warning'
  if (['missing', 'error', 'failed'].includes(status)) return 'danger'
  return 'info'
}

function statusLabel(status) {
  const map = {
    complete: '齐套',
    active: '有效',
    warning: '提醒',
    missing: '缺失',
    error: '异常',
    succeeded: '完成'
  }
  return map[status] || status || '-'
}

function versionReasonLabel(value) {
  const map = {
    initial: '首次生成',
    data_update: '数据更新',
    template_change: '模板变化'
  }
  return map[value] || value || '-'
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped lang="scss">
.dossier-detail-page {
  min-height: calc(100vh - 84px);
  background: #f5f7fb;
}

.page-head,
.summary-strip,
.workspace {
  max-width: 1540px;
  margin-left: auto;
  margin-right: auto;
}

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;

  h2 {
    margin: 0;
    color: #111827;
    font-size: 22px;
    font-weight: 650;
  }

  .subline {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
    margin-top: 6px;
    color: #6b7280;
    font-size: 13px;
  }
}

.actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.version-select {
  width: 190px;
}

.export-format-list {
  display: grid;
  gap: 10px;
}

.export-format-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  background: #ffffff;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;

  strong,
  span {
    display: block;
  }

  strong {
    color: #1f2937;
    font-size: 14px;
    font-weight: 650;
  }

  span {
    margin-top: 4px;
    color: #6b7280;
    font-size: 12px;
    line-height: 1.5;
  }

  &.active {
    border-color: #409eff;
    background: #ecf5ff;
  }

  &.disabled {
    cursor: not-allowed;
    opacity: 0.62;
  }
}

.summary-strip {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 12px;
}

.metric-cell,
.panel {
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #ffffff;
}

.metric-cell {
  min-height: 64px;
  padding: 12px;

  strong {
    display: block;
    color: #111827;
    font-size: 24px;
    line-height: 1;
  }

  span {
    display: block;
    margin-top: 8px;
    color: #6b7280;
    font-size: 13px;
  }
}

.workspace {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 12px;
}

.left-stack,
.center-stack {
  min-width: 0;
}

.left-stack {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.center-stack {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  align-content: start;
  gap: 12px;
}

.panel-head {
  min-height: 46px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 0 14px;
  border-bottom: 1px solid #edf0f5;
  color: #1f2937;
  font-weight: 650;

  small {
    display: block;
    margin-top: 2px;
    color: #6b7280;
    font-size: 12px;
    font-weight: 400;
  }
}

.panel-body {
  padding: 12px;
}

.directory-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: 10px;
}

.directory-row {
  width: 100%;
  min-height: 38px;
  display: grid;
  grid-template-columns: 30px minmax(0, 1fr) auto;
  align-items: center;
  gap: 8px;
  border: 1px solid transparent;
  border-radius: 4px;
  background: transparent;
  color: #4b5563;
  text-align: left;
  cursor: pointer;

  span {
    color: #909399;
    text-align: center;
  }

  strong {
    overflow: hidden;
    color: #374151;
    font-weight: 500;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &.active {
    border-color: #c6e2ff;
    background: #ecf5ff;

    strong,
    span {
      color: #409eff;
    }
  }
}

.current-object {
  display: flex;
  flex-direction: column;
  gap: 6px;

  strong {
    color: #111827;
    font-size: 16px;
  }

  span {
    color: #6b7280;
  }
}

.meta-action-list {
  display: grid;
  gap: 6px;
  margin-top: 8px;
}

.meta-action {
  width: 100%;
  min-height: 34px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 0 8px;
  border: 1px solid #edf0f5;
  border-radius: 4px;
  background: #fafbfc;
  color: #374151;
  cursor: pointer;

  span {
    color: #374151;
  }

  &:hover {
    border-color: #c6e2ff;
    background: #ecf5ff;
  }
}

.path-toolbar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(280px, 420px) auto;
  align-items: center;
  align-self: start;
  gap: 10px;
  min-height: 40px;
  padding: 6px 8px;
  border: 1px solid #edf0f5;
  border-radius: 6px;
  background: #ffffff;
}

.path-main {
  display: flex;
  align-items: center;
  min-width: 0;
  gap: 8px;
}

.path-line {
  display: flex;
  align-items: center;
  min-width: 0;
  overflow: hidden;
  gap: 4px;

  button {
    position: relative;
    min-width: 0;
    max-width: 150px;
    overflow: hidden;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    background: #ffffff;
    color: #606266;
    cursor: pointer;
    padding: 4px 8px;
    text-overflow: ellipsis;
    white-space: nowrap;

    &.active {
      border-color: #409eff;
      color: #409eff;
      background: #ecf5ff;
    }
  }
}

.bom-search {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
  gap: 8px;

  &.compact {
    margin-bottom: 0;
  }
}

.search-results {
  display: grid;
  gap: 8px;
}

.search-results {
  grid-template-columns: repeat(3, minmax(0, 1fr));

  &.compact-results {
    margin-top: -4px;
  }
}

.search-results button {
  min-width: 0;
  min-height: 58px;
  padding: 9px 10px;
  border: 1px solid #edf0f5;
  border-radius: 6px;
  background: #fafbfc;
  text-align: left;
  cursor: pointer;

  span,
  small,
  strong {
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  span {
    color: #409eff;
    font-size: 12px;
  }

  strong {
    margin: 4px 0;
    color: #111827;
  }

  small {
    color: #6b7280;
  }
}

.bom-grid {
  display: grid;
  grid-template-columns: 380px minmax(0, 1fr);
  gap: 12px;
}

.bom-browser :deep(.el-tree) {
  height: 430px;
  overflow: auto;
  border: 1px solid #edf0f5;
  border-radius: 6px;
  padding: 8px;
}

.tree-node {
  display: flex;
  align-items: center;
  min-width: 0;
  gap: 8px;

  span {
    overflow: hidden;
    color: #1f2937;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  small {
    color: #909399;
  }
}

.node-snapshot {
  min-width: 0;
  border: 1px solid #edf0f5;
  border-radius: 6px;
  padding: 12px;
  background: #fafbfc;
}

.snapshot-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 12px;

  strong {
    color: #111827;
    font-size: 18px;
  }
}

.field-grid,
.param-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.field-item,
.param-card {
  min-height: 64px;
  padding: 10px;
  border: 1px solid #edf0f5;
  border-radius: 6px;
  background: #ffffff;

  label {
    display: block;
    margin-bottom: 8px;
    color: #6b7280;
    font-size: 12px;
  }

  span,
  strong {
    color: #111827;
    font-size: 14px;
  }
}

.param-grid {
  margin-bottom: 12px;
}

.param-grid + .table-stack,
.param-grid + .data-block,
.table-stack + .data-block {
  margin-top: 12px;
}

.detail-tabs {
  margin-top: 8px;
}

.table-stack {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.block-title {
  margin-bottom: 8px;
  color: #1f2937;
  font-weight: 650;
}

.version-info {
  display: flex;
  flex-direction: column;
  gap: 10px;

  div {
    display: grid;
    grid-template-columns: 78px minmax(0, 1fr);
    gap: 8px;
  }

  label {
    color: #909399;
  }

  span {
    min-width: 0;
    color: #374151;
    overflow-wrap: anywhere;
  }
}

.dialog-info {
  gap: 12px;
}

.dialog-list {
  max-height: 460px;
  overflow: auto;
}

.file-list,
.source-list,
.operation-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.file-row {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  gap: 8px;
  padding: 8px;
  border: 1px solid #edf0f5;
  border-radius: 6px;

  span {
    grid-row: span 2;
    align-self: center;
    color: #409eff;
    font-weight: 700;
  }

  strong,
  small {
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  strong {
    color: #111827;
  }

  small {
    color: #6b7280;
  }
}

.source-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f2f5;

  span,
  strong {
    display: block;
  }

  span {
    color: #909399;
    font-size: 12px;
  }

  strong {
    color: #374151;
  }

  em {
    color: #409eff;
    font-style: normal;
    font-weight: 650;
  }
}

.operation-row {
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f2f5;

  span,
  strong,
  small {
    display: block;
  }

  span,
  small {
    color: #909399;
    font-size: 12px;
  }

  strong {
    margin: 3px 0;
    color: #374151;
  }
}

@media (max-width: 1280px) {
  .workspace {
    grid-template-columns: 260px minmax(0, 1fr);
  }

  .summary-strip,
  .field-grid,
  .param-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .path-toolbar {
    grid-template-columns: minmax(0, 1fr) auto;
  }

  .path-toolbar .bom-search {
    grid-column: 1 / -1;
    grid-row: 2;
  }
}

@media (max-width: 860px) {
  .page-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .workspace,
  .bom-grid,
  .path-toolbar,
  .summary-strip,
  .field-grid,
  .param-grid,
  .search-results,
  .bom-search {
    grid-template-columns: 1fr;
  }
}
</style>
