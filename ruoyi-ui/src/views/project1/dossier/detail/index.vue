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
        <el-button icon="Refresh" :loading="loading" @click="loadDetail">刷新</el-button>
        <el-button icon="Back" @click="goDossierInstance">返回卷宗实例管理</el-button>
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
                <el-tag v-else-if="item.displayType" size="small">{{ displayTypeLabel(item.displayType) }}</el-tag>
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

            <div v-if="showTimeline" class="data-block timeline-block">
              <div class="block-title">时间线</div>
              <el-timeline>
                <el-timeline-item
                  v-for="item in activeTimelineItems"
                  :key="`${item.key}-${item.title}-${item.time}`"
                  :timestamp="item.time"
                  placement="top"
                >
                  <strong>{{ item.title }}</strong>
                  <p>{{ item.detail }}</p>
                  <el-tag size="small" :type="statusType(item.status)">{{ item.stage || statusLabel(item.status) }}</el-tag>
                </el-timeline-item>
              </el-timeline>
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
              <div class="block-title block-title-row">
                <span>附件材料</span>
                <el-button
                  size="small"
                  type="primary"
                  icon="Download"
                  :loading="fileExporting"
                  :disabled="!directoryDocuments.length"
                  @click="exportAllFiles"
                >
                  导出全部
                </el-button>
              </div>
              <el-table :data="directoryDocuments" border>
                <el-table-column prop="fileType" label="类型" width="80" align="center" />
                <el-table-column prop="title" label="文件名称" min-width="220" show-overflow-tooltip />
                <el-table-column label="文件编号" min-width="180" show-overflow-tooltip>
                  <template #default="{ row }">{{ documentCode(row) }}</template>
                </el-table-column>
                <el-table-column label="版本/修订" width="100" show-overflow-tooltip>
                  <template #default="{ row }">{{ documentRevision(row) }}</template>
                </el-table-column>
                <el-table-column label="与节点关系" width="130" show-overflow-tooltip>
                  <template #default="{ row }">{{ documentRelationLabel(row) }}</template>
                </el-table-column>
                <el-table-column label="业务阶段" width="110" show-overflow-tooltip>
                  <template #default="{ row }">{{ documentStageLabel(row) }}</template>
                </el-table-column>
                <el-table-column prop="documentStatus" label="状态" width="90" align="center">
                  <template #default="{ row }">
                    <el-tag size="small" :type="statusType(row.documentStatus)">{{ statusLabel(row.documentStatus) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="日期" width="120" show-overflow-tooltip>
                  <template #default="{ row }">{{ documentDisplayDate(row) }}</template>
                </el-table-column>
                <el-table-column label="操作" width="128" align="center" fixed="right">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="viewFile(row)">查看</el-button>
                    <el-button link type="primary" @click="downloadFile(row)">下载</el-button>
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

    <el-dialog v-model="fileDetailVisible" title="文件详情" width="760px" append-to-body>
      <div class="file-detail-summary">
        <el-tag size="small" type="primary">{{ activeFile.fileType || fileType(activeFile.fileStorageKey || activeFile.fileExt || activeFile.mimeType) }}</el-tag>
        <div>
          <strong>{{ activeFile.title || activeFile.displayName || '-' }}</strong>
          <span>{{ firstPresent(activeFile.docNo, activeFile.fileCode, activeFile.businessNo) || '-' }}</span>
        </div>
      </div>

      <div class="version-info dialog-info file-detail-grid">
        <div v-for="item in fileDetailRows" :key="item.label">
          <label>{{ item.label }}</label>
          <span>{{ item.value }}</span>
        </div>
      </div>

      <div v-if="fileDetailAttrs.length" class="file-detail-section">
        <div class="block-title">业务属性</div>
        <div class="version-info dialog-info file-detail-grid">
          <div v-for="item in fileDetailAttrs" :key="item.label">
            <label>{{ item.label }}</label>
            <span>{{ item.value }}</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="DossierDetail">
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { saveAs } from 'file-saver'
import {
  exportDossierFiles,
  getCurrentDossierDetail,
  getDossierNodeDetail,
  listBomChildren,
  previewDossierFile,
  searchBomNodes
} from '@/api/project1/dossier/detail'
import { exportDossier } from '@/api/project1/dossier/instance'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const exporting = ref(false)
const fileExporting = ref(false)
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
const fileDetailVisible = ref(false)
const activeFile = ref({})

const exportFormats = [
  {
    type: 'pdf',
    label: 'PDF 文档',
    description: '导出可阅读归档的卷宗主文档。',
    extension: 'pdf',
    fileKey: 'fileName',
    mimeType: 'application/pdf',
    enabled: true
  },
  {
    type: 'zip',
    label: 'ZIP 完整包',
    description: '包含主文档、Excel清单、原始附件和机器可读数据。',
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

function goDossierInstance() {
  router.push('/project1/dossier/manage/instance')
}

const context = computed(() => detailData.value.context || {})
const metrics = computed(() => detailData.value.metrics || {})
const directory = computed(() => detailData.value.directory || {})
const currentNode = computed(() => detailData.value.currentNode || {})
const bomPath = computed(() => detailData.value.bomPath || [])
const currentChildren = computed(() => detailData.value.bomChildren || [])
const detail = computed(() => detailData.value.detail || {})
const contentItems = computed(() => detailData.value.contentItems || [])
const documents = computed(() => detailData.value.documents || [])
const displayDocuments = computed(() => {
  const rows = documents.value.map(item => ({
    ...item,
    fileType: item.fileType || fileType(item.fileStorageKey),
    title: item.title || item.docNo || item.fileStorageKey || '-'
  }))
  contentItems.value
    .filter(item => isDocumentContent(item))
    .forEach(item => {
      rows.push({
        documentEntryId: item.contentItemId,
        fileType: fileType(item.fileStorageKey),
        title: item.itemName,
        docNo: firstPresent(item.itemCode, item.sourceRecordKey),
        fileCode: item.itemCode,
        revision: firstPresent(item.revision, item.attrs?.revision, item.attrs?.version),
        relationType: contentItemRelationType(item),
        sourceSystem: item.sourceSystem,
        sourceTable: item.sourceTable,
        lifecycleStage: item.lifecycleStage,
        sourceRecordKey: item.sourceRecordKey,
        documentStatus: item.itemStatus || 'active',
        completenessStatus: item.completenessStatus,
        issueDate: item.issueDate,
        effectiveDate: item.effectiveDate,
        createdAt: item.createdAt,
        fileStorageKey: item.fileStorageKey
      })
    })
  return rows
})
const directoryDocuments = computed(() => filterDocumentsForDirectory(activeDirectoryItem.value))
const dataSources = computed(() => detailData.value.dataSources || [])
const operationLogs = computed(() => detailData.value.operationLogs || [])
const filePackages = computed(() => detailData.value.filePackages || [])
const fileDetailRows = computed(() => {
  const file = activeFile.value || {}
  return [
    fieldItem('文件编号', firstPresent(file.docNo, file.fileCode, file.businessNo)),
    fieldItem('显示名称', firstPresent(file.displayName, file.title)),
    fieldItem('原始文件名', file.originalFileName),
    fieldItem('版本/修订', file.revision),
    fieldItem('文件类型', firstPresent(file.fileExt, file.mimeType, file.fileType)),
    fieldItem('资产类型', file.assetKind),
    fieldItem('文件状态', statusLabel(firstPresent(file.fileStatus, file.documentStatus))),
    fieldItem('存储类型', storageTypeLabel(file.storageType)),
    fieldItem('存储桶', file.storageBucket),
    fieldItem('存储Key', file.storageKey),
    fieldItem('存储路径', file.storagePath),
    fieldItem('访问URL', file.accessUrl),
    fieldItem('预览Key', file.previewStorageKey),
    fieldItem('合并路径', file.fileStorageKey),
    fieldItem('文件大小', formatFileSize(file.fileSize)),
    fieldItem('哈希算法', file.hashAlgorithm),
    fieldItem('文件哈希', file.fileHash),
    fieldItem('挂靠类型', file.relationType),
    fieldItem('挂靠对象', formatRelationTarget(file)),
    fieldItem('对象层级', file.objectLevel),
    fieldItem('生命周期', file.lifecycleStage),
    fieldItem('业务域', file.businessDomain),
    fieldItem('是否必需', flagLabel(file.requiredFlag)),
    fieldItem('是否纳入卷宗', flagLabel(file.includedFlag)),
    fieldItem('是否主文件', flagLabel(file.isPrimary)),
    fieldItem('是否当前关系', flagLabel(file.isCurrent)),
    fieldItem('是否最新文件', flagLabel(file.isLatest)),
    fieldItem('来源系统', file.sourceSystem),
    fieldItem('来源表', file.sourceTable),
    fieldItem('来源记录', firstPresent(file.sourceRecordKey, file.sourceRecordId)),
    fieldItem('签发方', file.issuedBy),
    fieldItem('签发日期', file.issueDate),
    fieldItem('生效日期', file.effectiveDate),
    fieldItem('失效日期', file.expiryDate),
    fieldItem('安全级别', file.securityLevel),
    fieldItem('关系ID', file.documentEntryId),
    fieldItem('文件资产ID', file.fileAssetId)
  ].filter(item => hasPresentValue(item.value))
})
const fileDetailAttrs = computed(() => {
  const attrs = activeFile.value?.attrs || {}
  return Object.keys(attrs)
    .map(key => fieldItem(key, formatDetailValue(attrs[key])))
    .filter(item => hasPresentValue(item.value))
    .slice(0, 16)
})

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

const activeDirectoryDisplayType = computed(() => {
  return activeDirectoryItem.value.displayType || activeDirectoryItem.value.attrs?.displayType || ''
})

const activeDirectoryBlocks = computed(() => {
  return getStringList(activeDirectoryItem.value.blocks || activeDirectoryItem.value.attrs?.blocks)
})

const activeTimelineItems = computed(() => {
  return (detail.value.timeline || []).filter(item => item.key === activeDirectoryKey.value)
})

const showTimeline = computed(() => {
  return activeTimelineItems.value.length > 0
    && (activeDirectoryDisplayType.value === 'timeline_files' || activeDirectoryBlocks.value.includes('timeline'))
})

const directoryView = computed(() => {
  const label = activeDirectoryLabel.value || '目录数据'
  return {
    title: label,
    subtitle: `${currentNode.value.levelName || '-'} / ${currentNode.value.partName || '-'}`,
    status: isCompositionDirectory.value ? '按层级加载' : (detail.value.status || '有效'),
    statusType: isCompositionDirectory.value ? 'primary' : 'success'
  }
})

const directoryCards = computed(() => buildDirectoryCards(activeDirectoryItem.value))

const directoryTables = computed(() => buildDirectoryTables(activeDirectoryItem.value))

const categoryContentItems = computed(() => filterContentItemsForDirectory(activeDirectoryItem.value))

const showDocumentList = computed(() => {
  return isDocumentDirectory(activeDirectoryItem.value) && directoryDocuments.value.length > 0
})

const showContentList = computed(() => {
  return !['composition', 'documents'].includes(activeDirectoryCategory.value)
    && categoryContentItems.value.length > 0
    && (!activeDirectoryBlocks.value.length || activeDirectoryBlocks.value.includes('details'))
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

function openFileDetail(row) {
  activeFile.value = row || {}
  fileDetailVisible.value = true
}

async function viewFile(row) {
  if (!row) {
    return
  }
  const externalUrl = firstPresent(row.accessUrl, '')
  if (isHttpUrl(externalUrl)) {
    window.open(externalUrl, '_blank')
    return
  }
  if (!row.documentEntryId) {
    ElMessage.warning('当前文件没有可查看的挂靠记录')
    return
  }

  const previewWindow = browserPreviewable(row) ? window.open('', '_blank') : null
  try {
    const data = await previewDossierFile(row.documentEntryId)
    const blob = data instanceof Blob ? data : new Blob([data])
    const filename = fileDownloadName(row)
    if (browserPreviewable(row, blob.type)) {
      const url = URL.createObjectURL(blob)
      if (previewWindow) {
        previewWindow.location.href = url
      } else {
        window.open(url, '_blank')
      }
      setTimeout(() => URL.revokeObjectURL(url), 5 * 60 * 1000)
      return
    }
    if (previewWindow) {
      previewWindow.close()
    }
    saveAs(blob, filename)
  } catch (error) {
    if (previewWindow) {
      previewWindow.close()
    }
    ElMessage.error('文件不存在或尚未落盘，暂时无法直接查看')
  }
}

async function downloadFile(row) {
  if (!row) {
    return
  }
  const externalUrl = firstPresent(row.accessUrl, '')
  if (isHttpUrl(externalUrl)) {
    window.open(externalUrl, '_blank')
    return
  }
  if (!row.documentEntryId) {
    ElMessage.warning('当前文件没有可下载的挂靠记录')
    return
  }

  try {
    const data = await previewDossierFile(row.documentEntryId)
    const blob = data instanceof Blob ? data : new Blob([data])
    saveAs(blob, fileDownloadName(row))
    ElMessage.success('文件下载完成')
  } catch (error) {
    ElMessage.error('文件不存在或尚未落盘，暂时无法下载')
  }
}

async function exportAllFiles() {
  const documentEntryIds = [...new Set(directoryDocuments.value.map(row => row.documentEntryId).filter(Boolean))]
  if (!documentEntryIds.length) {
    ElMessage.warning('当前附件目录没有可导出的文件')
    return
  }

  fileExporting.value = true
  try {
    const data = await exportDossierFiles(documentEntryIds)
    saveAs(new Blob([data], { type: 'application/zip' }), exportAllFileName())
    ElMessage.success('附件导出完成')
  } catch (error) {
    ElMessage.error('附件文件不存在或尚未落盘，暂时无法导出')
  } finally {
    fileExporting.value = false
  }
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
    activeDirectoryKey.value = pickDefaultDirectoryKey(detailData.value, 'composition')
    activeTab.value = 'data'
    searchResults.value = []
    bomTreeKey.value += 1
    await setCurrentTreeNode()
  } finally {
    loading.value = false
  }
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
    activeDirectoryKey.value = pickDefaultDirectoryKey(
      payload,
      payload.currentNode && payload.currentNode.objectLevel === 'aircraft' ? 'composition' : 'basic'
    )
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
  const key = item && item.key ? item.key : ''
  return item?.compositionTree === true
    || item?.category === 'composition'
    || ['bom', 'composition'].includes(key)
}

function directoryCategory(item) {
  if (item?.category) return item.category
  if (item?.displayType === 'file_list') return 'documents'
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

function isDocumentDirectory(item) {
  return directoryCategory(item) === 'documents'
}

function buildDirectoryCards(item) {
  const category = directoryCategory(item)
  const primaryFields = getStringList(item?.primaryFields || item?.attrs?.primaryFields)
  const configuredCards = primaryFields
    .map(field => fieldItem(fieldLabel(field), lookupFieldValue(field, item)))
    .filter(field => field.value !== undefined && field.value !== null && field.value !== '')

  if (configuredCards.length) {
    return configuredCards.slice(0, 8)
  }
  if (category === 'basic') {
    return detail.value.basicFields || []
  }
  if (category === 'composition') {
    return []
  }
  if (category === 'documents') {
    return [
      fieldItem('文件数量', directoryDocuments.value.length),
      fieldItem('目录对象', currentNode.value.partName || '-'),
      fieldItem('数据来源', sourceLabelForDirectory(item, 'file_relation')),
      fieldItem('存储状态', directoryDocuments.value.length ? '已挂接' : '待补充')
    ]
  }
  return [
    fieldItem('目录对象', activeDirectoryLabel.value || '-'),
    fieldItem('当前节点', currentNode.value.partName || '-'),
    fieldItem('数据来源', sourceLabelForDirectory(item, sourceByCategory(category))),
    fieldItem('展示方式', displayModeForDirectory(item, category))
  ]
}

function buildDirectoryTables(item) {
  const category = directoryCategory(item)
  if (category === 'documents') {
    return []
  }
  if (category === 'composition') {
    return []
  }
  const tables = detail.value.tables || []
  const byKey = tables.filter(table => table.key === item?.key)
  if (byKey.length) {
    return byKey
  }
  const exact = tables.filter(table => tableMatchesLabel(table, activeDirectoryLabel.value))
  if (exact.length) {
    return exact
  }
  const matched = tables.filter(table => tableMatchesCategory(table, category, activeDirectoryLabel.value))
  if (matched.length) {
    return matched
  }
  return [buildFallbackTable(item, category)]
}

function buildFallbackTable(item, category) {
  return {
    title: activeDirectoryLabel.value || '目录数据',
    rows: [
      rowItem('目录对象', `${currentNode.value.partName || '-'} / ${currentNode.value.partNumber || '-'}`, '有效'),
      rowItem('数据来源', sourceLabelForDirectory(item, sourceByCategory(category)), '有效'),
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
    basic: 'v_*_profile_detail / physical_aircraft / part_instance',
    composition: 'aircraft_bom_node',
    design: 'part_master / file_relation / impact_tube_*',
    manufacturing: 'shop_order / process_route / production_operation_record',
    inspection: 'inspection_record / inspection_measurement',
    service: 'life_usage_record / install_removal / work_order',
    fault: 'fault_event / work_order',
    status: 'object_technical_status / object_status_history',
    interface: 'object_interface',
    documents: 'file_relation / file_asset'
  }
  return map[category] || 'dossier_content_item'
}

function displayModeForDirectory(item, category) {
  if (item?.displayType) {
    return displayTypeLabel(item.displayType)
  }
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

function displayTypeLabel(displayType) {
  const map = {
    tree_table: '树形结构',
    summary_table: '概要表',
    timeline_files: '时间线',
    part_card_param_table: '参数卡',
    file_list: '文件清单'
  }
  return map[displayType] || displayType || '-'
}

function filterContentItemsForDirectory(directoryItem) {
  const category = directoryCategory(directoryItem)
  if (category === 'composition' || category === 'documents') {
    return []
  }
  const sourceTables = lowerStringSet(directoryItem?.sourceTables)
  const lifecycleStages = upperStringSet(directoryItem?.lifecycleStages)
  const chapterId = String(directoryItem?.chapterId || '')
  return contentItems.value.filter(item => {
    const itemChapterId = String(item?.attrs?.chapterId || item?.chapterId || '')
    if (chapterId && itemChapterId) {
      return chapterId === itemChapterId
    }
    if (sourceTables.size && sourceTables.has(String(item.sourceTable || '').toLowerCase())) {
      return true
    }
    if (lifecycleStages.size && lifecycleStages.has(String(item.lifecycleStage || '').toUpperCase())) {
      return true
    }
    if (item.itemType === 'key_node_summary') {
      return category === 'basic' || category === 'content'
    }
    return matchesContentCategory(item, category)
  })
}

function filterDocumentsForDirectory(directoryItem) {
  if (!isDocumentDirectory(directoryItem)) {
    return []
  }
  const sourceTables = lowerStringSet(directoryItem?.sourceTables)
  const lifecycleStages = upperStringSet(directoryItem?.lifecycleStages)
  return displayDocuments.value.filter(row => {
    if (sourceTables.size && sourceTables.has(String(row.sourceTable || '').toLowerCase())) {
      return true
    }
    if (lifecycleStages.size && lifecycleStages.has(String(row.lifecycleStage || '').toUpperCase())) {
      return true
    }
    return !sourceTables.size && !lifecycleStages.size
  })
}

function matchesContentCategory(item, category) {
  const stage = String(item.lifecycleStage || '').toUpperCase()
  const itemType = String(item.itemType || '').toLowerCase()
  const sourceTable = String(item.sourceTable || '').toLowerCase()
  if (category === 'basic') {
    return itemType === 'profile_detail'
      || sourceTable.includes('profile')
      || ['physical_aircraft', 'part_instance', 'part_master'].includes(sourceTable)
      || itemType === 'key_node_summary'
  }
  if (category === 'design') return ['DESIGN', 'INTERFACE', 'TECHNICAL_STATUS'].includes(stage)
  if (category === 'manufacturing') return ['MANUFACTURING', 'INSTALLATION'].includes(stage) || sourceTable.includes('shop_order')
  if (category === 'inspection') return stage === 'INSPECTION' || sourceTable.includes('inspection')
  if (category === 'service') return stage === 'SERVICE' || itemType.includes('work_order') || sourceTable.includes('life_usage')
  if (category === 'fault') return stage === 'FAULT' || itemType.includes('fault') || sourceTable.includes('fault')
  if (category === 'status') return stage === 'TECHNICAL_STATUS' || itemType.includes('status')
  if (category === 'interface') return stage === 'INTERFACE' || itemType.includes('interface') || sourceTable.includes('interface')
  return true
}

function pickDefaultDirectoryKey(payload, preference) {
  const rows = payload?.directory?.rows || directory.value.rows || []
  if (!rows.length) {
    return activeDirectoryKey.value || 'bom'
  }
  const composition = rows.find(item => isCompositionRow(item))
  if (preference === 'composition' && composition) {
    return composition.key
  }
  const basic = rows.find(item => directoryCategory(item) === 'basic')
  if (preference === 'basic' && basic) {
    return basic.key
  }
  const current = rows.find(item => item.key === activeDirectoryKey.value)
  if (current) {
    return current.key
  }
  const content = rows.find(item => !isCompositionRow(item))
  return (content || composition || rows[0]).key
}

function lookupFieldValue(field, directoryItem) {
  const fieldMap = detail.value.fieldMap || {}
  const normalized = String(field || '').trim()
  if (!normalized) {
    return '-'
  }
  if (fieldMap[normalized] !== undefined) {
    return fieldMap[normalized]
  }
  const fieldMapKey = Object.keys(fieldMap).find(key => key.toLowerCase() === normalized.toLowerCase())
  if (fieldMapKey) {
    return fieldMap[fieldMapKey]
  }
  if (currentNode.value[normalized] !== undefined) {
    return currentNode.value[normalized]
  }
  if (context.value[normalized] !== undefined) {
    return context.value[normalized]
  }
  const row = filterContentItemsForDirectory(directoryItem).find(item => {
    const attrs = item.attrs || {}
    return attrs[normalized] !== undefined || item[normalized] !== undefined
  })
  if (!row) {
    return '-'
  }
  return row.attrs?.[normalized] ?? row[normalized] ?? '-'
}

function fieldLabel(field) {
  const labelMap = {
    partNumber: '件号',
    partName: '名称',
    serialNumber: '序列号',
    positionCode: '位置',
    ataChapter: 'ATA',
    installDate: '装机日期',
    tsnFh: 'TSN FH',
    tsnFc: 'TSN FC',
    contentCount: '内容记录',
    documentCount: '附件材料'
  }
  return labelMap[field] || field
}

function sourceLabelForDirectory(item, fallback) {
  const sourceTables = getStringList(item?.sourceTables)
  return sourceTables.length ? sourceTables.join(' / ') : fallback
}

function getStringList(value) {
  if (!value) return []
  if (Array.isArray(value)) {
    return value.filter(Boolean).map(item => String(item))
  }
  return String(value)
    .split(/[,\n;，；]/)
    .map(item => item.trim())
    .filter(Boolean)
}

function lowerStringSet(value) {
  return new Set(getStringList(value).map(item => item.toLowerCase()))
}

function upperStringSet(value) {
  return new Set(getStringList(value).map(item => item.toUpperCase()))
}

function isDocumentContent(item) {
  return hasFileLikeStorageKey(item.fileStorageKey)
}

function hasFileLikeStorageKey(value) {
  const key = String(value || '').trim().split(/[?#]/)[0]
  if (!key || key.endsWith('/') || key.endsWith('\\')) {
    return false
  }
  return !!fileExtension(key)
}

function fileType(fileStorageKey) {
  const key = String(fileStorageKey || '').toLowerCase()
  if (key.endsWith('.pdf') || key === 'pdf' || key.includes('application/pdf')) return 'PDF'
  if (key.endsWith('.zip') || key === 'zip' || key.includes('application/zip')) return 'ZIP'
  if (key.endsWith('.jpg') || key.endsWith('.jpeg') || key.endsWith('.png') || ['jpg', 'jpeg', 'png'].includes(key) || key.startsWith('image/')) return 'IMG'
  return 'DOC'
}

function browserPreviewable(row, mimeType = '') {
  const type = String(mimeType || row?.mimeType || '').toLowerCase()
  const name = String(firstPresent(row?.fileStorageKey, row?.storagePath, row?.storageKey, row?.originalFileName, row?.title, row?.fileExt) || '').toLowerCase()
  return type.includes('application/pdf')
    || type.startsWith('image/')
    || type.startsWith('text/plain')
    || name.endsWith('.pdf')
    || name.endsWith('.png')
    || name.endsWith('.jpg')
    || name.endsWith('.jpeg')
    || name.endsWith('.txt')
}

function fileDownloadName(row) {
  const name = firstPresent(row?.originalFileName, row?.displayName, row?.title, row?.docNo, 'dossier-file')
  const extension = fileExtension(firstPresent(row?.fileStorageKey, row?.storagePath, row?.storageKey, row?.originalFileName, row?.fileExt))
  if (extension && !String(name).toLowerCase().endsWith(`.${extension}`)) {
    return `${name}.${extension}`
  }
  return name
}

function documentCode(row) {
  return firstPresent(row?.docNo, row?.businessNo, row?.fileCode, row?.targetCode, row?.sourceRecordKey, row?.itemCode, '-')
}

function documentRevision(row) {
  return firstPresent(row?.revision, row?.version, row?.versionLabel, row?.attrs?.revision, row?.attrs?.version, '-')
}

function documentRelationLabel(row) {
  const relationType = String(row?.relationType || '').toUpperCase()
  const sourceTable = String(row?.sourceTable || '').toLowerCase()
  const stage = String(row?.lifecycleStage || '').toUpperCase()
  const title = String(firstPresent(row?.title, row?.displayName, row?.originalFileName, '') || '')
  const map = {
    PRIMARY: '主文件',
    DOSSIER_ATTACHMENT: '证明附件',
    REFERENCE: '参考资料',
    CONTENT_FILE: '节点附件',
    CERTIFICATE: '证书文件',
    DELIVERABLE: '交付文件'
  }
  if (map[relationType]) return map[relationType]
  if (sourceTable.includes('certificate')) return '证书文件'
  if (sourceTable.includes('part_document')) return '技术/参考文件'
  if (stage === 'DESIGN') return '设计依据'
  if (stage === 'MANUFACTURING') return '制造记录'
  if (stage === 'INSPECTION') return '检验记录'
  if (stage === 'INSTALLATION') return '装机证据'
  if (stage === 'SERVICE') return '服役记录'
  if (title.includes('证') || title.includes('合格') || title.includes('符合') || title.includes('检验') || title.includes('试验')) {
    return '证明附件'
  }
  return firstPresent(row?.relationType, row?.sourceTable, '-')
}

function documentStageLabel(row) {
  const stage = String(row?.lifecycleStage || '').toUpperCase()
  const domain = String(row?.businessDomain || '').toUpperCase()
  const title = String(firstPresent(row?.title, row?.displayName, row?.originalFileName, '') || '')
  const map = {
    DOCUMENT: '文档',
    DESIGN: '设计',
    MANUFACTURING: '制造',
    INSPECTION: '检验',
    INSTALLATION: '装机',
    SERVICE: '服役',
    MAINTENANCE: '维修',
    FAULT: '故障',
    TECHNICAL_STATUS: '技术状态',
    INTERFACE: '接口',
    FULL_LIFECYCLE: '全生命周期'
  }
  if (map[stage]) return map[stage]
  if (map[domain]) return map[domain]
  if (title.includes('随工') || title.includes('制造')) return '制造'
  if (title.includes('检验') || title.includes('试验') || title.includes('终检')) return '检验'
  if (title.includes('装机')) return '装机'
  if (title.includes('维修')) return '维修'
  if (title.includes('设计') || title.includes('规范')) return '设计'
  if (title.includes('证') || title.includes('合格') || title.includes('符合')) return '证明'
  return '-'
}

function documentDisplayDate(row) {
  return firstPresent(
    formatDateOnly(row?.issueDate),
    formatDateOnly(row?.effectiveDate),
    formatDateOnly(row?.createdAt),
    formatDateOnly(row?.updatedAt),
    formatDateOnly(row?.expiryDate),
    '-'
  )
}

function contentItemRelationType(item) {
  const sourceTable = String(item?.sourceTable || '').toLowerCase()
  if (sourceTable.includes('certificate')) return 'CERTIFICATE'
  if (sourceTable.includes('part_document')) return 'REFERENCE'
  return 'CONTENT_FILE'
}

function formatDateOnly(value) {
  if (!hasPresentValue(value)) {
    return ''
  }
  return String(value).slice(0, 10)
}

function exportAllFileName() {
  const nodeName = firstPresent(currentNode.value.partNumber, currentNode.value.partName, context.value.tailNumber, 'dossier')
  return `${safeFileSegment(nodeName)}_附件材料.zip`
}

function safeFileSegment(value) {
  const text = String(value || '').trim().replace(/[\\/:*?"<>|]/g, '_')
  return text || 'dossier'
}

function fileExtension(value) {
  const text = String(value || '').trim()
  if (!text) {
    return ''
  }
  if (!text.includes('.') && /^[a-z0-9]+$/i.test(text)) {
    return text.toLowerCase()
  }
  const clean = text.split(/[?#]/)[0]
  const index = clean.lastIndexOf('.')
  return index >= 0 && index < clean.length - 1 ? clean.slice(index + 1).toLowerCase() : ''
}

function isHttpUrl(value) {
  const text = String(value || '').toLowerCase()
  return text.startsWith('http://') || text.startsWith('https://')
}

function firstPresent(...values) {
  return values.find(value => hasPresentValue(value))
}

function hasPresentValue(value) {
  return value !== undefined && value !== null && String(value).trim() !== ''
}

function flagLabel(value) {
  if (!hasPresentValue(value)) {
    return ''
  }
  return value === true || Number(value) === 1 ? '是' : '否'
}

function storageTypeLabel(value) {
  const map = {
    LOCAL: '本地存储',
    OSS: '对象存储',
    URL: '外部URL',
    MINIO: 'MinIO',
    S3: 'S3'
  }
  return map[String(value || '').toUpperCase()] || value || ''
}

function formatFileSize(value) {
  if (!hasPresentValue(value)) {
    return ''
  }
  const size = Number(value)
  if (!Number.isFinite(size) || size < 0) {
    return value
  }
  if (size === 0) {
    return '0 B'
  }
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const index = Math.min(Math.floor(Math.log(size) / Math.log(1024)), units.length - 1)
  return `${(size / Math.pow(1024, index)).toFixed(index === 0 ? 0 : 2)} ${units[index]}`
}

function formatRelationTarget(file) {
  const parts = [
    file.targetType,
    firstPresent(file.targetName, file.targetCode, file.targetId),
    file.structureNodeId ? `节点 ${file.structureNodeId}` : '',
    file.bomNodeId ? `BOM ${file.bomNodeId}` : '',
    file.partNumber ? `件号 ${file.partNumber}` : ''
  ].filter(hasPresentValue)
  return parts.join(' / ')
}

function formatDetailValue(value) {
  if (!hasPresentValue(value)) {
    return ''
  }
  if (typeof value === 'object') {
    return JSON.stringify(value)
  }
  return value
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
.data-block + .table-stack,
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

.block-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.timeline-block {
  padding: 10px 12px 0;
  border: 1px solid #edf0f5;
  border-radius: 6px;
  background: #ffffff;

  p {
    margin: 4px 0 8px;
    color: #6b7280;
    line-height: 1.5;
  }
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

.file-title-button {
  max-width: 100%;
  padding: 0;

  :deep(span) {
    min-width: 0;
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.file-detail-summary {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid #edf0f5;

  strong,
  span {
    display: block;
    min-width: 0;
    overflow-wrap: anywhere;
  }

  strong {
    color: #111827;
    font-size: 16px;
    font-weight: 650;
  }

  span {
    margin-top: 4px;
    color: #6b7280;
    font-size: 12px;
  }
}

.file-detail-grid {
  div {
    grid-template-columns: 96px minmax(0, 1fr);
  }
}

.file-detail-section {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #edf0f5;
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
