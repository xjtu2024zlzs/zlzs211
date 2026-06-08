<template>
  <div class="app-container quality-page" @click="closeContextMenu">
    <el-card shadow="never" class="quality-hero">
      <template #header>
        <span class="quality-title">生命周期质量统一监测</span>
      </template>
      <div class="quality-desc">涵盖全生命周期的质量监测系统，统一管理各阶段数据文件</div>
    </el-card>

    <el-card shadow="never" class="quality-card quality-mb16">
      <template #header>
        <span class="quality-section-title">数据导入与预处理</span>
      </template>
      <div class="data-import-row">
        <div class="data-import-card">
          <el-button type="primary" class="import-btn" :loading="numImporting" :disabled="numImporting" @click.stop="importNumData">导入数值型数据</el-button>
          <input ref="numFileInput" type="file" accept=".csv,.txt,text/csv,text/plain" multiple class="hidden-file-input" @change="onNumFileChange" />
          <div class="import-desc">先选择数据用途和归属对象，再选择 CSV/TXT 文件。</div>
          <div v-if="numProg.visible" class="num-import-progress">
            <div class="num-progress-text">{{ numProg.text }}</div>
            <el-progress :percentage="numProg.percentage" :stroke-width="8" />
          </div>
          <div v-if="numUploadRows.length" class="num-upload-list">
            <div class="num-upload-summary">
              <span>总数 {{ numUploadRows.length }}</span>
              <span>成功 {{ numUploadStat.success }}</span>
              <span>失败 {{ numUploadStat.failed }}</span>
              <span>上传中 {{ numUploadStat.uploading }}</span>
            </div>
            <el-table :data="numUploadRows" size="small" max-height="240" border>
              <el-table-column prop="name" label="文件名" min-width="180" show-overflow-tooltip />
              <el-table-column label="大小" width="90"><template #default="{ row }">{{ fileSizeText(row.size) }}</template></el-table-column>
              <el-table-column label="状态" width="110"><template #default="{ row }">{{ uploadStatusText(row.status) }}</template></el-table-column>
              <el-table-column label="进度" width="150"><template #default="{ row }"><el-progress :percentage="row.progress" :stroke-width="6" /></template></el-table-column>
              <el-table-column prop="message" label="说明" min-width="160" show-overflow-tooltip />
              <el-table-column label="操作" width="80"><template #default="{ row }"><el-button v-if="row.status === 'FAILED'" type="primary" link @click="retryNumUpload(row)">重试</el-button></template></el-table-column>
            </el-table>
          </div>
        </div>
        <div class="data-import-card">
          <el-button type="primary" class="import-btn" :loading="numImporting" :disabled="numImporting" @click.stop="importSeqData">导入时序数据</el-button>
          <div class="import-desc">先选择数据用途和归属对象，再选择 CSV/TXT 文件。</div>
        </div>
        <div class="data-import-card">
          <el-button type="primary" class="import-btn" :loading="textImporting" :disabled="textImporting" @click.stop="importTextData">导入文本数据</el-button>
          <input ref="textFileInput" type="file" accept=".xlsx,.xls,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-excel" class="hidden-file-input" @change="onTextFileChange" />
          <div class="import-desc">负责导入层级对象、质量信息等数据</div>
        </div>
        <div class="data-import-card">
          <el-button type="primary" plain class="import-btn" @click.stop="openDataFileDialog">查看全部数据文件</el-button>
          <div class="import-desc">查看各部分已导入的数据文件，并支持删除。</div>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="numObjDlg.visible" :title="numObjDlgTitle" width="1080px" top="4vh" class="numeric-object-dialog" :close-on-click-modal="false">
      <div class="numeric-object-layout">
        <aside class="numeric-task-panel">
          <div class="numeric-panel-title">任务类型</div>
          <div class="numeric-task-list">
            <div
              v-for="item in numPurposeOpts"
              :key="item.value"
              class="numeric-task-card"
              :class="{ active: numObjDlg.form.purpose === item.value }"
              @click="selectNumPurpose(item.value)"
            >
              <div class="numeric-task-main">
                <div class="numeric-task-name">{{ item.label }}</div>
                <div class="numeric-task-desc">{{ item.desc }}</div>
              </div>
              <span class="numeric-radio" :class="{ checked: numObjDlg.form.purpose === item.value }"></span>
            </div>
          </div>
          <div class="numeric-switch-tip">
            <div class="numeric-switch-title"><el-icon><Warning /></el-icon><span>切换任务类型</span></div>
            <div>已选择任务对象会被清空，避免任务类型与对象层级不匹配。</div>
          </div>
          <div class="numeric-switch-tip">
            <div class="numeric-switch-title"><span>导入方式</span></div>
            <el-radio-group v-model="numObjDlg.form.importMode" class="numeric-import-mode">
              <el-radio-button label="file">文件导入</el-radio-button>
              <el-radio-button label="api">API导入</el-radio-button>
            </el-radio-group>
          </div>
        </aside>

        <section class="numeric-object-main">
          <div class="numeric-object-header">
            <div>
              <div class="numeric-panel-title">任务对象</div>
              <div class="numeric-current-task">当前任务：{{ curNumPurposeLabel }}，请选择 <span>{{ numObjReqLevelText }}</span> 作为任务对象。</div>
            </div>
          </div>
          <el-input v-model="numObjDlg.keyword" class="numeric-object-search" placeholder="搜索对象名称" clearable :prefix-icon="Search" />
          <el-form v-if="numObjDlg.form.importMode === 'api'" label-width="96px" class="numeric-api-form">
            <el-form-item label="API地址">
              <el-input v-model="numObjDlg.form.apiUrl" placeholder="请输入返回CSV/TXT数据的HTTP/HTTPS地址" />
            </el-form-item>
            <el-form-item label="请求方法">
              <el-select v-model="numObjDlg.form.apiMethod" style="width: 140px">
                <el-option label="GET" value="GET" />
                <el-option label="POST" value="POST" />
              </el-select>
            </el-form-item>
            <el-form-item label="请求头">
              <el-input v-model="numObjDlg.form.apiHeaders" type="textarea" :rows="3" placeholder='可选，JSON格式，例如 {"Authorization":"Bearer token"}' />
            </el-form-item>
            <el-form-item v-if="numObjDlg.form.apiMethod === 'POST'" label="请求体">
              <el-input v-model="numObjDlg.form.apiBody" type="textarea" :rows="4" placeholder="可选，JSON或文本请求体" />
            </el-form-item>
            <el-form-item label="保存文件名">
              <el-input v-model="numObjDlg.form.apiFileName" placeholder="可选，例如 api_data.csv；为空则自动生成" />
            </el-form-item>
          </el-form>
          <div class="numeric-object-content">
            <div class="numeric-tree-wrap" v-loading="numObjDlg.loading">
              <el-tree
                ref="numTreeRef"
                class="numeric-object-tree"
                :data="treeData"
                node-key="id"
                :expand-on-click-node="false"
                :filter-node-method="filterNumTree"
                :props="{ label: 'name', children: 'children' }"
                @node-click="onNumNodeClick"
              >
                <template #default="{ data }">
                  <div
                    class="numeric-tree-node"
                    :class="{ selected: isNumNodeSelected(data), disabled: !isNumNodeSelectable(data) }"
                  >
                    <div class="numeric-node-left">
                      <span class="numeric-node-name">{{ data.name }}</span>
                    </div>
                    <div class="numeric-node-right">
                      <span class="numeric-node-status" :class="{ disabled: !isNumNodeSelectable(data) }">{{ isNumNodeSelectable(data) ? '可选' : '不可选' }}</span>
                      <el-icon v-if="isNumNodeSelected(data)" class="numeric-node-check"><Select /></el-icon>
                    </div>
                  </div>
                </template>
              </el-tree>
              <el-empty v-if="!numObjDlg.loading && !treeData.length" description="暂无对象层级数据" />
            </div>

            <aside class="numeric-side-panel">
              <div class="numeric-info-card">
                <div class="numeric-info-title">已选择对象</div>
                <div v-if="numSelObj" class="numeric-selected-card">
                  <div class="numeric-selected-name">{{ numSelObj.name }}</div>
                  <div class="numeric-selected-type">对象类型：{{ numSelObj.typeText }}</div>
                  <div v-if="numSelObj.routeName || numSelObj.processCount !== null" class="numeric-selected-process">
                    <div class="numeric-process-row">
                      <span>工序路线</span>
                      <strong>{{ numSelObj.routeName || '-' }}</strong>
                    </div>
                    <div class="numeric-process-row">
                      <span>工序数量</span>
                      <strong>{{ displayCount(numSelObj.processCount) }}</strong>
                    </div>
                  </div>
                  <div class="numeric-selected-path">
                    <div v-for="item in numSelPath" :key="item.id" class="numeric-path-row">
                      <span>{{ item.typeText }}</span>
                      <strong>{{ item.name }}</strong>
                    </div>
                  </div>
                </div>
                <div v-else class="numeric-empty-selected">暂未选择对象</div>
              </div>
            </aside>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="numObjDlg.visible = false">取消</el-button>
        <el-button type="primary" :loading="numObjDlg.loading || numImporting" :disabled="numObjDlg.loading || numImporting" @click="conNumObj">导入数据</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="textObjDlg.visible" title="选择文本数据对象" width="1080px" top="4vh" class="numeric-object-dialog" :close-on-click-modal="false">
      <div class="numeric-object-layout">
        <aside class="numeric-task-panel">
          <div class="numeric-panel-title">任务类型</div>
          <div class="numeric-task-list">
            <div
              v-for="item in textTaskOpts"
              :key="item.value"
              class="numeric-task-card"
              :class="{ active: textObjDlg.form.taskType === item.value }"
              @click="selectTextTask(item.value)"
            >
              <div class="numeric-task-main">
                <div class="numeric-task-name">{{ item.label }}</div>
                <div class="numeric-task-desc">{{ item.desc }}</div>
              </div>
              <span class="numeric-radio" :class="{ checked: textObjDlg.form.taskType === item.value }"></span>
            </div>
          </div>
          <div class="numeric-switch-tip">
            <div class="numeric-switch-title"><el-icon><Warning /></el-icon><span>导入规则</span></div>
            <div>{{ curTextTaskRule }}</div>
          </div>
          <div class="numeric-switch-tip">
            <div class="numeric-switch-title"><span>导入方式</span></div>
            <el-radio-group v-model="textObjDlg.form.importMode" class="numeric-import-mode">
              <el-radio-button label="file">文件导入</el-radio-button>
              <el-radio-button label="api">API导入</el-radio-button>
            </el-radio-group>
          </div>
        </aside>

        <section class="numeric-object-main">
          <div class="numeric-object-header">
            <div>
              <div class="numeric-panel-title">任务对象</div>
              <div v-if="textTaskRequiresObject" class="numeric-current-task">当前任务：{{ curTextTaskLabel }}，请选择 <span>{{ textObjReqLevelText }}</span> 作为任务对象。</div>
              <div v-else class="numeric-current-task">当前任务：{{ curTextTaskLabel }}，无需选择任务对象。</div>
            </div>
            <el-button v-if="hasTextImportTemplate" plain type="primary" @click="downloadTextImportTemplate">下载导入模板</el-button>
          </div>
          <el-form v-if="textObjDlg.form.importMode === 'api'" label-width="96px" class="numeric-api-form">
            <el-form-item label="API地址">
              <el-input v-model="textObjDlg.form.apiUrl" placeholder="请输入返回Excel文件内容的HTTP/HTTPS地址" />
            </el-form-item>
            <el-form-item label="请求方法">
              <el-select v-model="textObjDlg.form.apiMethod" style="width: 140px">
                <el-option label="GET" value="GET" />
                <el-option label="POST" value="POST" />
              </el-select>
            </el-form-item>
            <el-form-item label="请求头">
              <el-input v-model="textObjDlg.form.apiHeaders" type="textarea" :rows="3" placeholder='可选，JSON格式，例如 {"Authorization":"Bearer token"}' />
            </el-form-item>
            <el-form-item v-if="textObjDlg.form.apiMethod === 'POST'" label="请求体">
              <el-input v-model="textObjDlg.form.apiBody" type="textarea" :rows="4" placeholder="可选，JSON或文本请求体" />
            </el-form-item>
            <el-form-item label="文件名">
              <el-input v-model="textObjDlg.form.apiFileName" placeholder="可选，例如 import.xlsx；为空则自动生成" />
            </el-form-item>
          </el-form>
          <el-input v-model="textObjDlg.keyword" class="numeric-object-search" placeholder="搜索对象名称" clearable :prefix-icon="Search" />
          <div class="numeric-object-content">
            <div class="numeric-tree-wrap" v-loading="textObjDlg.loading">
              <el-tree
                ref="textTreeRef"
                class="numeric-object-tree"
                :data="treeData"
                node-key="id"
                :expand-on-click-node="false"
                :filter-node-method="filterTextTree"
                :props="{ label: 'name', children: 'children' }"
                @node-click="onTextNodeClick"
              >
                <template #default="{ data }">
                  <div
                    class="numeric-tree-node"
                    :class="{ selected: isTextNodeSelected(data), disabled: !isTextNodeSelectable(data) }"
                  >
                    <div class="numeric-node-left">
                      <span class="numeric-node-name">{{ data.name }}</span>
                    </div>
                    <div class="numeric-node-right">
                      <span class="numeric-node-status" :class="{ disabled: !isTextNodeSelectable(data) }">{{ isTextNodeSelectable(data) ? '可选' : '不可选' }}</span>
                    </div>
                  </div>
                </template>
              </el-tree>
              <el-empty v-if="!textObjDlg.loading && !treeData.length" description="暂无对象层级数据" />
            </div>

            <aside class="numeric-side-panel">
              <div class="numeric-info-card">
                <div class="numeric-info-title">已选择对象</div>
                <div v-if="textSelObj" class="numeric-selected-card">
                  <div class="numeric-selected-name">{{ textSelObj.name }}</div>
                  <div class="numeric-selected-type">对象类型：{{ textSelObj.typeText }}</div>
                  <div v-if="textSelObj.routeName || textSelObj.processCount !== null" class="numeric-selected-process">
                    <div class="numeric-process-row">
                      <span>工序路线</span>
                      <strong>{{ textSelObj.routeName || '-' }}</strong>
                    </div>
                    <div class="numeric-process-row">
                      <span>工序数量</span>
                      <strong>{{ displayCount(textSelObj.processCount) }}</strong>
                    </div>
                  </div>
                  <div class="numeric-selected-path">
                    <div v-for="item in textSelPath" :key="item.id" class="numeric-path-row">
                      <span>{{ item.typeText }}</span>
                      <strong>{{ item.name }}</strong>
                    </div>
                  </div>
                </div>
                <div v-else class="numeric-empty-selected">暂未选择对象</div>
              </div>
            </aside>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="textObjDlg.visible = false">取消</el-button>
        <el-button type="primary" :loading="textObjDlg.loading || textImporting" :disabled="textObjDlg.loading || textImporting" @click="conTextObj">{{ textConfirmText }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="dataFileDialog.visible" title="全部数据文件" width="1100px" :close-on-click-modal="false">
      <div class="data-file-toolbar">
        <el-select v-model="dataFileQuery.dataUsage" placeholder="全部用途" clearable style="width: 160px" @change="searchDataFiles">
          <el-option label="全部用途" value="ALL" />
          <el-option label="服役周期" value="FAULT_IDENTIFY" />
          <el-option label="制造周期通用" value="MANUFACTURE_COMMON" />
          <el-option label="工序异常检测" value="PROCESS_ANOMALY" />
          <el-option label="关键质量特性挖掘" value="KQC_MINING" />
          <el-option label="关键工序识别" value="KEY_PROCESS" />
          <el-option label="通用" value="COMMON" />
        </el-select>
        <el-input v-model="dataFileQuery.keyword" placeholder="搜索文件名/路径/对象" clearable style="width: 260px" @keyup.enter="searchDataFiles" @clear="searchDataFiles" />
        <el-input v-model="dataFileQuery.uploadBatchId" placeholder="上传批次" clearable style="width: 180px" @keyup.enter="searchDataFiles" @clear="searchDataFiles" />
        <el-button type="primary" @click="searchDataFiles">查询</el-button>
        <el-button :loading="dataFileDialog.selectingAll" :disabled="!dataFileDialog.total || dataFileDialog.loading || dataFileDialog.selectingAll" @click="selectAllDataFiles">全选</el-button>
        <el-button :disabled="!dataFileSelectedCount" @click="clearSelectedDataFiles">清空选择</el-button>
        <el-button type="danger" :loading="dataFileDialog.deleting" :disabled="!dataFileSelectedCount || dataFileDialog.deleting" @click="removeSelectedDataFiles">
          删除选中 {{ dataFileSelectedCount ? `(${dataFileSelectedCount})` : '' }}
        </el-button>
      </div>
      <el-table ref="dataFileTableRef" v-loading="dataFileDialog.loading" :data="dataFileDialog.rows" height="460" border row-key="id" @selection-change="handleDataFileSelectionChange">
        <el-table-column type="selection" width="48" reserve-selection />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="fileName" label="文件名" min-width="180" show-overflow-tooltip />
        <el-table-column label="数据用途" width="180">
          <template #default="{ row }">
            <el-select v-if="isManufacturingDataUsage(row.dataUsage)" :model-value="row.dataUsage" size="small" :loading="row.usageUpdating" @change="value => changeDataFileUsage(row, value)">
              <el-option label="制造周期通用" value="MANUFACTURE_COMMON" />
              <el-option label="工序异常检测" value="PROCESS_ANOMALY" />
              <el-option label="关键质量特性挖掘" value="KQC_MINING" />
              <el-option label="关键工序识别" value="KEY_PROCESS" />
            </el-select>
            <span v-else>{{ dataUsageText(row.dataUsage) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="aircraftId" label="飞机" width="90" />
        <el-table-column prop="subsystemId" label="分系统" width="90" />
        <el-table-column prop="equipmentId" label="设备" width="90" />
        <el-table-column prop="componentId" label="组件" width="90" />
        <el-table-column label="文件大小" width="110"><template #default="{ row }">{{ fileSizeText(row.fileSize) }}</template></el-table-column>
        <el-table-column prop="createTime" label="导入时间" width="170" />
        <el-table-column prop="sourceFile" label="存储路径" min-width="260" show-overflow-tooltip />
        <el-table-column label="操作" width="90" fixed="right"><template #default="{ row }"><el-button type="danger" link @click="removeDataFile(row)">删除</el-button></template></el-table-column>
      </el-table>
      <pagination v-show="dataFileDialog.total > 0" :total="dataFileDialog.total" v-model:page="dataFileQuery.pageNum" v-model:limit="dataFileQuery.pageSize" @pagination="loadDataFiles" />
    </el-dialog>

    <el-row :gutter="16">
      <el-col :span="6">
        <el-card shadow="never" class="quality-card left-tree-card">
          <template #header><span class="quality-section-title">层级菜单</span></template>
          <div class="structure-path">结构路径：{{ currentPathText || '暂无数据' }}</div>
          <el-tree ref="moduleTreeRef" v-loading="tree_loading" class="module-tree" :data="treeData" node-key="id" :current-node-key="currentTreeNodeId" highlight-current :expand-on-click-node="false" :props="{ label: 'name', children: 'children' }" @node-click="handleTreeClick" @node-contextmenu="handleTreeContextMenu">
            <template #default="{ data }"><div class="tree-node-content"><span class="tree-node-name">{{ data.name }}</span></div></template>
          </el-tree>
        </el-card>
      </el-col>
      <el-col :span="18">
        <div class="content-topbar">
          <div class="current-path">当前路径：{{ currentPathText || '暂无数据' }}</div>
          <el-button @click="goParent" :disabled="!canGoParent">返回上级</el-button>
        </div>
        <template v-if="!isTerminalNode">
          <div v-loading="node_loading" class="module-card-grid">
            <div v-for="item in currentChildren" :key="item.id" class="module-node-card" @click.stop="enterNode(item)" @contextmenu.prevent.stop="openContextMenu($event, item)">
              <div class="module-node-title">{{ item.name }}</div>
              <div class="module-node-desc">左键进入模块，右键打开菜单。{{ formatChildCountText(item) }}{{ formatChildCountText(item) ? '，' : '' }}零件：{{ displayCount(item.part_count) }}</div>
            </div>
            <el-empty v-if="!node_loading && currentChildren.length === 0" description="暂无子模块" />
          </div>
        </template>
        <template v-else>
          <el-card shadow="never" class="quality-card">
            <template #header><span class="quality-section-title">{{ current_node?.name || '' }} - 零件列表</span></template>
            <el-form :inline="true" class="quality-mb16">
              <el-form-item><el-input v-model="partQuery.keyword" placeholder="零件编号 / 名称 / 批次" clearable /></el-form-item>
              <el-form-item><el-select v-model="partQuery.material" placeholder="全部材料" clearable style="width: 160px"><el-option v-for="item in partMaterialOptions" :key="`material-${item}`" :label="item" :value="item" /></el-select></el-form-item>
              <el-form-item><el-select v-model="partQuery.status" placeholder="全部状态" clearable style="width: 160px"><el-option v-for="item in partStatusOptions" :key="`status-${item}`" :label="item" :value="item" /></el-select></el-form-item>
              <el-form-item><el-button type="primary" @click="applyPartFilters">筛选</el-button><el-button @click="resetPartFilters">重置</el-button><el-button type="success" @click="openCreatePartInstanceDialog">新增零件实例</el-button></el-form-item>
            </el-form>
            <el-table v-loading="part_loading" :data="part_list" border>
              <el-table-column prop="part_instance_id" label="零件实例ID" min-width="160" />
              <el-table-column prop="serial_number" label="序列号" min-width="140" />
              <el-table-column prop="batch_number" label="批次号" min-width="140" />
              <el-table-column prop="manufacturer" label="制造商" min-width="140" />
              <el-table-column prop="current_status" label="当前状态" min-width="120" />
              <el-table-column label="操作" width="270" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="openPartQualityDialog(row)">质量详情</el-button><el-button link type="primary" @click="openEditPartInstanceDialog(row)">修改</el-button><el-button link type="danger" @click="submitDeletePartInstance(row)">删除</el-button></template></el-table-column>
            </el-table>
          </el-card>
        </template>
      </el-col>
    </el-row>

    <div v-show="context_menu.visible" class="module-context-menu" :style="{ left: context_menu.x + 'px', top: context_menu.y + 'px' }">
      <div class="context-menu-item" @click.stop="handleAddEntry">{{ addEntryText }}</div>
      <div class="context-menu-item" @click.stop="handleViewNodeDetail">查看详情</div>
      <div class="context-menu-item" @click.stop="handleQualityMining">关键质量特性挖掘</div>
      <div class="context-menu-item" @click.stop="handleProcessAnomalyDetect">工序异常检测</div>
      <div class="context-menu-item" @click.stop="handleKeyProcessIdentify">关键工序识别</div>
      <div class="context-menu-item danger" @click.stop="handleDeleteModule">{{ deleteEntryText }}</div>
    </div>

    <el-dialog v-model="nodeDetailDialog.visible" width="620px" :title="`对象详情 - ${nodeDetailDialog.node?.name || ''}`" :close-on-click-modal="false">
      <el-form v-if="nodeDetailDialog.type === 'part_template'" v-loading="nodeDetailDialog.loading" label-width="110px">
        <el-form-item label="零件模板ID">
          <el-input v-model="nodeDetailDialog.partForm.part_template_id" disabled />
        </el-form-item>
        <el-form-item label="零件编号">
          <el-input v-model="nodeDetailDialog.partForm.part_number" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="零件名称">
          <el-input v-model="nodeDetailDialog.partForm.part_name" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="组件ID">
          <el-input v-model="nodeDetailDialog.partForm.component_id" disabled />
        </el-form-item>
        <el-form-item label="材料">
          <el-input v-model="nodeDetailDialog.partForm.material" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="规格型号">
          <el-input v-model="nodeDetailDialog.partForm.specification" maxlength="100" show-word-limit />
        </el-form-item>
      </el-form>
      <el-form v-else label-width="90px">
        <el-form-item label="对象ID">
          <el-input :model-value="nodeDetailDialog.node?.id || ''" disabled />
        </el-form-item>
        <el-form-item label="对象名称">
          <el-input v-model="nodeDetailDialog.form.name" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="父级ID">
          <el-input :model-value="nodeDetailDialog.node?.parent_id || '-'" disabled />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="nodeDetailDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="nodeDetailDialog.loading" @click="submitNodeDetail">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="partQualityDialog.visible" width="900px" :title="`零件质量详情 - ${partQualityDialog.part_name || partQualityDialog.part_code || ''}`">
      <div v-loading="partQualityDialog.loading" class="quality-detail-wrap">
        <div v-for="table in partQualityDialog.tables" :key="table.table_name" class="quality-stage-block">
          <div class="quality-stage-title">{{ table.table_label }}</div>
          <el-table :data="table.rows || []" border size="small" class="quality-dynamic-table" empty-text="暂无数据">
            <el-table-column v-for="column in table.columns || []" :key="`${table.table_name}-${column.prop}`" :prop="column.prop" :label="column.label" min-width="140" show-overflow-tooltip />
          </el-table>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="addModuleDialog.visible" title="新增模块" width="460px" :close-on-click-modal="false">
      <el-form label-width="84px">
        <el-form-item label="当前路径"><div>{{ currentPathText || '暂无数据' }}</div></el-form-item>
        <el-form-item label="模块名称"><el-input v-model="addModuleDialog.module_name" placeholder="请输入模块名称" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="addModuleDialog.visible = false">取消</el-button><el-button type="primary" :loading="addModuleDialog.loading" @click="submitCreateModule">确定</el-button></template>
    </el-dialog>

    <el-dialog v-model="addPartInstanceDialog.visible" title="新增零件实例" width="620px" :close-on-click-modal="false">
      <el-form label-width="120px">
        <el-form-item label="零件编号"><el-input v-model="addPartInstanceDialog.form.part_code" disabled /></el-form-item>
        <el-form-item label="零件名称"><el-input v-model="addPartInstanceDialog.form.part_name" disabled /></el-form-item>
        <el-form-item label="材料"><el-input v-model="addPartInstanceDialog.form.material" disabled /></el-form-item>
        <el-form-item label="规格型号"><el-input v-model="addPartInstanceDialog.form.spec_model" disabled /></el-form-item>
        <el-form-item label="序列号"><el-input v-model="addPartInstanceDialog.form.serial_number" placeholder="请输入序列号" /></el-form-item>
        <el-form-item label="批次号"><el-input v-model="addPartInstanceDialog.form.batch_number" placeholder="请输入批次号" /></el-form-item>
        <el-form-item label="制造商"><el-input v-model="addPartInstanceDialog.form.manufacturer" /></el-form-item>
        <el-form-item label="生产日期"><el-date-picker v-model="addPartInstanceDialog.form.production_date" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
        <el-form-item label="状态"><el-input v-model="addPartInstanceDialog.form.status" /></el-form-item>
        <el-form-item label="质量等级"><el-input v-model="addPartInstanceDialog.form.quality_level" /></el-form-item>
        <el-form-item label="关键程度"><el-input v-model="addPartInstanceDialog.form.key_degree" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="addPartInstanceDialog.visible = false">取消</el-button><el-button type="primary" :loading="addPartInstanceDialog.loading" @click="submitCreatePartInstance">确定</el-button></template>
    </el-dialog>

    <el-dialog v-model="editPartInstanceDialog.visible" title="修改零件实例" width="620px" :close-on-click-modal="false">
      <el-form label-width="120px">
        <el-form-item label="零件编号"><el-input v-model="editPartInstanceDialog.form.part_code" disabled /></el-form-item>
        <el-form-item label="零件名称"><el-input v-model="editPartInstanceDialog.form.part_name" disabled /></el-form-item>
        <el-form-item label="材料"><el-input v-model="editPartInstanceDialog.form.material" disabled /></el-form-item>
        <el-form-item label="规格型号"><el-input v-model="editPartInstanceDialog.form.spec_model" disabled /></el-form-item>
        <el-form-item label="序列号"><el-input v-model="editPartInstanceDialog.form.serial_number" placeholder="请输入序列号" /></el-form-item>
        <el-form-item label="批次号"><el-input v-model="editPartInstanceDialog.form.batch_number" placeholder="请输入批次号" /></el-form-item>
        <el-form-item label="制造商"><el-input v-model="editPartInstanceDialog.form.manufacturer" /></el-form-item>
        <el-form-item label="生产日期"><el-date-picker v-model="editPartInstanceDialog.form.production_date" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
        <el-form-item label="状态"><el-input v-model="editPartInstanceDialog.form.status" /></el-form-item>
        <el-form-item label="质量等级"><el-input v-model="editPartInstanceDialog.form.quality_level" /></el-form-item>
        <el-form-item label="关键程度"><el-input v-model="editPartInstanceDialog.form.key_degree" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="editPartInstanceDialog.visible = false">取消</el-button><el-button type="primary" :loading="editPartInstanceDialog.loading" @click="submitUpdatePartInstance">确定</el-button></template>
    </el-dialog>

    <el-dialog v-model="addPartTemplateDialog.visible" title="新增零件模板" width="520px" :close-on-click-modal="false">
      <el-form label-width="110px">
        <el-form-item label="零件编号"><el-input v-model="addPartTemplateDialog.form.part_code" placeholder="请输入零件编号" /></el-form-item>
        <el-form-item label="零件名称"><el-input v-model="addPartTemplateDialog.form.part_name" placeholder="请输入零件名称" /></el-form-item>
        <el-form-item label="材料"><el-input v-model="addPartTemplateDialog.form.material" /></el-form-item>
        <el-form-item label="规格型号"><el-input v-model="addPartTemplateDialog.form.spec_model" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="addPartTemplateDialog.visible = false">取消</el-button><el-button type="primary" :loading="addPartTemplateDialog.loading" @click="submitCreatePartTemplate">确定</el-button></template>
    </el-dialog>
    <el-card shadow="never" class="quality-card quality-mb16">
      <template #header>
        <div class="quality-card-header">
          <span class="quality-section-title">关键质量特性挖掘历史记录</span>
          <el-button :loading="kqcHistory.loading" @click="loadKqcHistory">刷新</el-button>
        </div>
      </template>
      <div class="kqc-history-toolbar">
        <el-input
          v-model="kqcHistory.query.keyword"
          placeholder="输入任务ID或结果关键字"
          clearable
          @keyup.enter="searchKqcHistory"
          @clear="searchKqcHistory"
        />
        <el-select v-model="kqcHistory.query.status" placeholder="状态" clearable>
          <el-option label="成功" value="SUCCESS" />
          <el-option label="运行中" value="RUNNING" />
          <el-option label="等待中" value="PENDING" />
          <el-option label="失败" value="FAILED" />
          <el-option label="已取消" value="CANCELED" />
        </el-select>
        <el-button type="primary" @click="searchKqcHistory">查询</el-button>
        <el-button @click="resetKqcHistory">重置</el-button>
      </div>
      <el-table v-loading="kqcHistory.loading" :data="kqcHistory.rows" border height="300" empty-text="暂无挖掘历史记录">
        <el-table-column prop="taskId" label="任务ID" width="190" show-overflow-tooltip />
        <el-table-column prop="targetName" label="执行对象" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.targetName || '--' }}</template>
        </el-table-column>
        <el-table-column prop="kqcCount" label="特性数量" width="100" />
        <el-table-column prop="topFeature" label="Top质量特性" min-width="170" show-overflow-tooltip />
        <el-table-column prop="score" label="分数/频率" width="120" />
        <el-table-column prop="outputDir" label="输出目录" min-width="220" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewKqcHistory(row)">查看</el-button>
            <el-button link type="danger" @click="deleteKqcHistory(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="kqcHistory.total > 0"
        v-model:page="kqcHistory.query.page_num"
        v-model:limit="kqcHistory.query.page_size"
        :total="kqcHistory.total"
        @pagination="loadKqcHistory"
      />
    </el-card>
    <el-dialog v-model="kqcDialog.visible" title="关键质量特性挖掘" width="1180px" :close-on-click-modal="false" @closed="closeKqcDialog">
      <el-form :model="kqcDialog.form" label-width="130px">
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="最大行数">
              <el-input-number v-model="kqcDialog.form.maxRows" :min="1000" :step="10000" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="最大特征数">
              <el-input-number v-model="kqcDialog.form.maxFeatures" :min="5" :max="300" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="每工序特征">
              <el-input-number v-model="kqcDialog.form.perStation" :min="1" :max="10" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="稳定选择次数">
              <el-input-number v-model="kqcDialog.form.ssRuns" :min="1" :max="100" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div class="dialog-action-row">
        <div class="dialog-action-hint">配置参数并选择数据文件后提交任务</div>
        <div class="dialog-action-buttons">
          <el-button @click="kqcDialog.visible = false">关闭</el-button>
          <el-button v-if="kqcTaskActive" :loading="kqcDialog.canceling" @click="cancelKqcMining">取消任务</el-button>
          <el-button type="primary" :loading="kqcSubmitLoading" :disabled="kqcSubmitDisabled" @click="submitKqcMining">开始挖掘</el-button>
        </div>
      </div>
      <div class="detect-data-section">
        <div class="detect-data-header">
          <span class="detect-data-title">选择关键质量特性挖掘数据</span>
          <span class="detect-data-count">已选择 {{ kqcSelectedSampleIds.length }} 个文件</span>
        </div>
        <div class="file-toolbar">
          <el-input v-model="kqcFileQuery.keyword" placeholder="搜索文件名" clearable style="width: 240px" @keyup.enter="loadKqcSamples" @clear="loadKqcSamples" />
          <el-button @click="loadKqcSamples">查询</el-button>
          <el-button @click="clearKqcSamples">清空选择</el-button>
        </div>
        <el-table ref="kqcSampleTableRef" v-loading="kqcFileQuery.loading" :data="kqcFileQuery.samples" row-key="id" height="260" border @selection-change="onKqcSampleSelection">
          <el-table-column type="selection" width="48" reserve-selection />
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="fileName" label="文件名" min-width="180" show-overflow-tooltip />
          <el-table-column label="数据用途" width="150"><template #default="{ row }">{{ dataUsageText(row.dataUsage) }}</template></el-table-column>
          <el-table-column prop="conditionLabel" label="对象类型" width="130" />
          <el-table-column prop="bearingCode" label="对象编号" width="120" />
          <el-table-column prop="fileSize" label="文件大小" width="120">
            <template #default="{ row }">{{ fileSizeText(row.fileSize) }}</template>
          </el-table-column>
          <el-table-column prop="createTime" label="导入时间" width="170" />
        </el-table>
        <pagination v-show="kqcFileQuery.total > 0" v-model:page="kqcFileQuery.pageNum" v-model:limit="kqcFileQuery.pageSize" :total="kqcFileQuery.total" @pagination="loadKqcSamples" />
      </div>
      <el-alert
        class="quality-mb16"
        type="warning"
        :closable="false"
        show-icon
        title="提示：关键质量特性挖掘需要 train_numeric.csv 包含响应结果列；如果没有响应结果列，请先准备已完成的 KQC输出目录，目录中需要包含 bosch_adj_matrix_selected.csv 和 bosch_edge_frequency.csv，然后用于关键工序识别。"
      />
      <div v-if="kqcDialog.taskId" class="quality-mb16">
        任务ID：{{ kqcDialog.taskId }}　状态：{{ kqcDialog.status || '--' }}
        <span v-if="kqcDialog.error">　失败原因：{{ kqcDialog.error }}</span>
      </div>
      <div class="kqc-task-lookup">
        <el-input v-model="kqcDialog.lookupTaskId" placeholder="输入任务ID查看挖掘结果" clearable @keyup.enter="lookupKqcTaskResult" />
        <el-button type="primary" :loading="kqcDialog.lookupLoading" @click="lookupKqcTaskResult">查看结果</el-button>
        <el-button :disabled="!kqcGraphData.nodes.length" @click="openKqcGraphDialog">关系详情</el-button>
      </div>
      <div v-if="kqcDialog.result.outputDir" class="quality-mb16">输出目录：{{ kqcDialog.result.outputDir }}</div>
      <div class="kqc-result-layout">
        <div class="kqc-chart-panel">
          <div class="kqc-panel-title">Top 关键质量特性</div>
          <div ref="kqcRankingChartRef" class="kqc-ranking-chart"></div>
        </div>
        <div class="kqc-table-panel">
          <div class="kqc-panel-title">关键质量特性排名</div>
          <el-table :data="kqcRankedRows" border height="320" empty-text="暂无挖掘结果">
            <el-table-column prop="rank" label="排名" width="70" align="center" />
            <el-table-column prop="source_variable" label="质量特性" min-width="160" show-overflow-tooltip />
            <el-table-column prop="station" label="工序" width="110" />
            <el-table-column label="影响方向" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.weight >= 0 ? 'danger' : 'primary'" size="small">
                  {{ row.weight >= 0 ? '正向' : '负向' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="影响权重" width="120" align="right">
              <template #default="{ row }">{{ formatKqcMetric(row.weight, 6) }}</template>
            </el-table-column>
            <el-table-column label="绝对权重" width="120" align="right">
              <template #default="{ row }">{{ formatKqcMetric(row.absWeight, 6) }}</template>
            </el-table-column>
            <el-table-column label="稳定频率" width="110" align="right">
              <template #default="{ row }">{{ formatKqcMetric(row.frequency, 3) }}</template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>
    <el-dialog v-model="kqcGraphDialog.visible" title="关键质量特性关系详情" width="1000px" append-to-body @opened="scheduleKqcGraphRender">
      <div class="kqc-graph-panel">
        <div class="kqc-graph-title">关键质量特性影响关系图</div>
        <div ref="kqcGraphRef" class="kqc-graph"></div>
      </div>
    </el-dialog>
    <el-dialog v-model="detectDialog.visible" title="工序异常检测" width="1080px" :close-on-click-modal="false" @close="closeDetectDialog">
      <div class="detect-summary">
        <span>检测对象：{{ detectTargetText }}</span>
        <span>已选择 {{ detectSelectedSampleIds.length }} 个文件</span>
      </div>
      <div class="dialog-action-row">
        <div class="dialog-action-hint">选择检测数据并提交异常检测任务</div>
        <div class="dialog-action-buttons">
          <el-button @click="closeDetectDialog">关闭</el-button>
          <el-button v-if="detectTaskActive" type="warning" :loading="detectDialog.canceling" @click="cancelDetectTask">取消任务</el-button>
          <el-button type="primary" :loading="detectSubmitLoading" :disabled="detectSubmitDisabled" @click="submitProcessAnomaly">开始异常检测</el-button>
        </div>
      </div>
      <div class="task-lookup-row">
        <el-input v-model="detectDialog.lookupTaskId" placeholder="输入任务ID查看结果" clearable @keyup.enter="lookupDetectTaskResult" />
        <el-button type="primary" :loading="detectDialog.lookupLoading" @click="lookupDetectTaskResult">查看结果</el-button>
      </div>

      <div class="detect-data-section bosch-param-section">
        <div class="detect-data-header">
          <span class="detect-data-title">Bosch 工序异常检测参数</span>
          <el-switch v-model="detectDialog.form.enabled" active-text="Bosch算法" inactive-text="原文件模式" />
        </div>
        <el-form v-if="detectDialog.form.enabled" :model="detectDialog.form" label-width="130px" class="bosch-param-form">
          <el-row :gutter="12">
            <el-col :span="6">
              <el-form-item label="工序">
                <el-input v-model="detectDialog.form.station" placeholder="例如 L3_S36" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="特征列">
                <el-input v-model="detectDialog.form.featureColName" placeholder="可留空自动选择" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="最大行数">
                <el-input-number v-model="detectDialog.form.maxRows" :min="1000" :step="10000" controls-position="right" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="训练轮数">
                <el-input-number v-model="detectDialog.form.epochs" :min="1" :max="300" controls-position="right" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="选择策略">
                <el-select v-model="detectDialog.form.selectionMode" style="width: 100%">
                  <el-option label="按失效关联度" value="response_assoc" />
                  <el-option label="按数据覆盖率" value="coverage" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="告警比例">
                <el-input-number v-model="detectDialog.form.alphaSample" :min="0.001" :max="0.2" :step="0.001" :precision="3" controls-position="right" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        <div v-if="detectDialog.form.enabled" class="detect-data-section">
          <div class="detect-data-header">
            <span class="detect-data-title">选择工序异常检测数据</span>
            <span class="detect-data-count">已选择 {{ detectSelectedSamples.length }} 个文件</span>
          </div>
          <div class="file-toolbar">
            <el-input v-model="detectFileQuery.keyword" placeholder="搜索文件名" clearable style="width: 240px" @keyup.enter="loadDetectSamples" @clear="loadDetectSamples" />
            <el-button @click="loadDetectSamples">查询</el-button>
            <el-button @click="clearDetectSamples">清空选择</el-button>
          </div>
          <el-table ref="detectSampleTableRef" v-loading="detectFileQuery.loading" :data="detectFileQuery.samples" row-key="id" height="260" border @selection-change="onDetectSampleSelection">
            <el-table-column type="selection" width="48" reserve-selection />
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="fileName" label="文件名" min-width="180" show-overflow-tooltip />
            <el-table-column label="数据用途" width="150"><template #default="{ row }">{{ dataUsageText(row.dataUsage) }}</template></el-table-column>
            <el-table-column prop="conditionLabel" label="对象类型" width="130" />
            <el-table-column prop="bearingCode" label="对象编号" width="120" />
            <el-table-column prop="fileSize" label="文件大小" width="120">
              <template #default="{ row }">{{ fileSizeText(row.fileSize) }}</template>
            </el-table-column>
            <el-table-column prop="createTime" label="导入时间" width="170" />
          </el-table>
          <pagination v-show="detectFileQuery.total > 0" v-model:page="detectFileQuery.pageNum" v-model:limit="detectFileQuery.pageSize" :total="detectFileQuery.total" @pagination="loadDetectSamples" />
        </div>
      </div>

      <template v-if="!detectDialog.form.enabled">
        <div class="detect-data-section">
          <div class="detect-data-header">
            <span class="detect-data-title">1、选择训练和测试数据</span>
            <span class="detect-data-count">已选择 {{ detectSelectedSamples.length }} 个文件</span>
          </div>
          <div class="file-toolbar">
            <el-input v-model="detectFileQuery.keyword" placeholder="搜索文件名" clearable style="width: 240px" @keyup.enter="loadDetectSamples" @clear="loadDetectSamples" />
            <el-button @click="loadDetectSamples">查询</el-button>
            <el-button @click="clearDetectSamples">清空选择</el-button>
          </div>
          <el-table ref="detectSampleTableRef" v-loading="detectFileQuery.loading" :data="detectFileQuery.samples" row-key="id" height="260" border @selection-change="onDetectSampleSelection">
            <el-table-column type="selection" width="48" reserve-selection />
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="fileName" label="文件名" min-width="180" show-overflow-tooltip />
            <el-table-column label="数据用途" width="150"><template #default="{ row }">{{ dataUsageText(row.dataUsage) }}</template></el-table-column>
            <el-table-column prop="conditionLabel" label="对象类型" width="130" />
            <el-table-column prop="bearingCode" label="对象编号" width="120" />
            <el-table-column prop="fileSize" label="文件大小" width="120">
              <template #default="{ row }">{{ fileSizeText(row.fileSize) }}</template>
            </el-table-column>
            <el-table-column prop="createTime" label="导入时间" width="170" />
          </el-table>
          <pagination v-show="detectFileQuery.total > 0" v-model:page="detectFileQuery.pageNum" v-model:limit="detectFileQuery.pageSize" :total="detectFileQuery.total" @pagination="loadDetectSamples" />
        </div>

        <div class="detect-data-section">
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
            <el-table-column prop="featureCol" label="使用列" width="100" />
            <el-table-column prop="uploadStatus" label="状态" width="110" />
          </el-table>
        </div>
      </template>

      <div class="detect-result" v-if="detectDialog.status || detectDialog.error || Object.keys(detectDialog.result).length">
        <div v-if="detectTaskProgress || detectTaskMessage" class="task-progress-row">
          <el-progress :percentage="detectTaskProgress || 0" :status="detectDialog.status === 'FAILED' ? 'exception' : (detectDialog.status === 'SUCCESS' ? 'success' : undefined)" />
          <div class="task-progress-message">{{ detectTaskMessage }}</div>
        </div>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="任务ID">{{ detectDialog.taskId || '--' }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detectDialog.status || '--' }}</el-descriptions-item>
          <el-descriptions-item label="是否异常">{{ detectAbnormalText }}</el-descriptions-item>
          <el-descriptions-item label="异常分数">{{ val(detectDialog.result.abnormalScore ?? detectDialog.result.abnormal_score) }}</el-descriptions-item>
          <el-descriptions-item label="异常时间">{{ val(detectDialog.result.abnormalTime ?? detectDialog.result.abnormal_time) }}</el-descriptions-item>
          <el-descriptions-item label="建议" :span="3">{{ val(translateDetectText(detectDialog.result.suggestion)) }}</el-descriptions-item>
          <el-descriptions-item v-if="detectDialog.error" label="失败原因" :span="3">{{ detectDialog.error }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="detectCurveRows.length" class="anomaly-chart-panel quality-mt16">
          <div class="anomaly-chart-head">
            <div>
              <div class="anomaly-chart-title">异常分数趋势</div>
              <div class="anomaly-chart-subtitle">横轴为样本点，纵轴为异常分数</div>
            </div>
            <div class="anomaly-chart-stats">
              <span>点数 {{ detectCurveRows.length }}</span>
              <span>峰值 {{ formatChartValue(detectCurveMaxValue) }}</span>
              <span v-if="detectCurveThreshold !== null">阈值 {{ formatChartValue(detectCurveThreshold) }}</span>
            </div>
          </div>
          <div ref="anomalyChartRef" class="anomaly-chart"></div>
        </div>
        <el-table v-if="detectCurveRows.length" :data="detectCurveRows" border height="160" class="quality-mt16">
          <el-table-column prop="x" label="x" />
          <el-table-column prop="value" label="value" />
        </el-table>
      </div>

    </el-dialog>
    <el-dialog v-model="keyDialog.visible" title="关键工序识别" width="1080px" :close-on-click-modal="false" @close="closeKeyDialog">
      <div class="detect-summary">
        <span>检测对象：{{ keyTargetText }}</span>
        <span>已选择 {{ keySelectedSampleIds.length }} 个文件</span>
      </div>
      <div class="task-lookup-row">
        <el-input v-model="keyDialog.lookupTaskId" placeholder="输入任务ID查看结果" clearable @keyup.enter="lookupKeyTaskResult" />
        <el-button type="primary" :loading="keyDialog.lookupLoading" @click="lookupKeyTaskResult">查看结果</el-button>
      </div>

      <div class="detect-data-section bosch-param-section">
        <div class="detect-data-header">
          <span class="detect-data-title">Bosch 关键工序识别参数</span>
          <el-switch v-model="keyDialog.form.enabled" active-text="Bosch算法" inactive-text="原文件模式" />
        </div>
        <el-form v-if="keyDialog.form.enabled" :model="keyDialog.form" label-width="130px" class="bosch-param-form">
          <el-row :gutter="12">
            <el-col :span="12">
              <el-form-item label="KQC任务ID">
                <el-input v-model="keyDialog.form.kqcTaskId" placeholder="输入已完成的KQC任务ID" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="KQC输出目录">
                <el-input v-model="keyDialog.form.kqcOutputDir" placeholder="也可直接填写已完成KQC输出目录" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="Top N">
                <el-input-number v-model="keyDialog.form.topN" :min="1" :max="50" controls-position="right" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>
      <div v-if="!keyDialog.form.enabled" class="file-toolbar">
        <el-input v-model="keyFileQuery.keyword" placeholder="搜索文件名" clearable style="width: 240px" @keyup.enter="loadKeySamples" @clear="loadKeySamples" />
        <el-button @click="loadKeySamples">查询</el-button>
        <el-button @click="clearKeySamples">清空选择</el-button>
      </div>
      <el-table v-if="!keyDialog.form.enabled" ref="keySampleTableRef" v-loading="keyFileQuery.loading" :data="keyFileQuery.samples" row-key="id" height="360" border @selection-change="onKeySampleSelection">
        <el-table-column type="selection" width="48" reserve-selection />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="fileName" label="文件名" min-width="180" show-overflow-tooltip />
        <el-table-column label="数据用途" width="150"><template #default="{ row }">{{ dataUsageText(row.dataUsage) }}</template></el-table-column>
        <el-table-column prop="conditionLabel" label="对象类型" width="130" />
        <el-table-column prop="bearingCode" label="对象编号" width="120" />
        <el-table-column prop="fileSize" label="文件大小" width="120">
          <template #default="{ row }">{{ fileSizeText(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="导入时间" width="170" />
      </el-table>
      <pagination v-if="!keyDialog.form.enabled" v-show="keyFileQuery.total > 0" v-model:page="keyFileQuery.pageNum" v-model:limit="keyFileQuery.pageSize" :total="keyFileQuery.total" @pagination="loadKeySamples" />

      <div class="detect-result" v-if="keyDialog.status || keyDialog.error || Object.keys(keyDialog.result).length">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="任务ID">{{ keyDialog.taskId || '--' }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ keyDialog.status || '--' }}</el-descriptions-item>
          <el-descriptions-item label="置信度">{{ val(keyField('confidence')) }}</el-descriptions-item>
          <el-descriptions-item label="关键工序">{{ val(keyField('name')) }}</el-descriptions-item>
          <el-descriptions-item label="工序编码">{{ val(keyField('code')) }}</el-descriptions-item>
          <el-descriptions-item label="分数">{{ val(keyField('score')) }}</el-descriptions-item>
          <el-descriptions-item label="原因" :span="3">{{ val(keyText('reason')) }}</el-descriptions-item>
          <el-descriptions-item label="建议" :span="3">{{ val(keyText('suggestion')) }}</el-descriptions-item>
          <el-descriptions-item v-if="keyDialog.error" label="失败原因" :span="3">{{ keyDialog.error }}</el-descriptions-item>
        </el-descriptions>
        <el-table v-if="keyProcessRows.length" :data="keyProcessRows" border height="180" class="quality-mt16">
          <el-table-column prop="rank" label="排名" width="80" />
          <el-table-column prop="processCode" label="工序编码" />
          <el-table-column prop="processName" label="工序名称" />
          <el-table-column prop="score" label="分数" width="120" />
        </el-table>
      </div>

      <template #footer>
        <el-button @click="closeKeyDialog">关闭</el-button>
        <el-button v-if="keyTaskActive" type="warning" :loading="keyDialog.canceling" @click="cancelKeyProcess">取消任务</el-button>
        <el-button type="primary" :loading="keySubmitLoading" :disabled="keySubmitDisabled" @click="submitKeyProcess">开始关键工序识别</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Select, Warning } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import request from '@/utils/request'
import { saveAs } from 'file-saver'
import '@/views/project_3/common.css'


defineOptions({
  name: 'QualityMonitor'
})

function fetchMonitorTree() {
  return request({
    url: '/monitor/tree',
    method: 'get'
  })
}
function fetchPartInstances(query) {
  return request({
    url: '/monitor/parts',
    method: 'get',
    params: query
  })
}



function fetchPartQualityTables(part_instance_id) {
  return request({
    url: `/monitor/parts/${part_instance_id}/quality`,
    method: 'get'
  })
}

function createModule(data) {
  return request({
    url: '/monitor/module',
    method: 'post',
    data
  })
}

function deleteModule(node_id) {
  return request({
    url: `/monitor/module/${encodeURIComponent(node_id)}`,
    method: 'delete'
  })
}

function updateModuleName(node_id, data) {
  return request({
    url: `/monitor/module/${encodeURIComponent(node_id)}/name`,
    method: 'put',
    data
  })
}
function createPartInstance(data) {
  return request({
    url: '/monitor/parts',
    method: 'post',
    data
  })
}

function updatePartInstance(part_instance_id, data) {
  return request({
    url: `/monitor/parts/${encodeURIComponent(part_instance_id)}`,
    method: 'put',
    data
  })
}

function createPartTemplate(data) {
  return request({
    url: '/monitor/part-template',
    method: 'post',
    data
  })
}

function fetchPartTemplateDetail(part_template_id) {
  return request({
    url: `/monitor/part-template/${encodeURIComponent(part_template_id)}`,
    method: 'get'
  })
}

function updatePartTemplateDetail(part_template_id, data) {
  return request({
    url: `/monitor/part-template/${encodeURIComponent(part_template_id)}`,
    method: 'put',
    data
  })
}


function deletePartInstance(part_instance_id) {
  return request({
    url: `/monitor/parts/${encodeURIComponent(part_instance_id)}`,
    method: 'delete'
  })
}

function uploadNumericFile(data, onUploadProgress) {
  return request({
    url: '/quality/fault-iden/catalog/upload-numeric-file',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data',
      repeatSubmit: false
    },
    timeout: 300000,
    onUploadProgress
  })
}

function uploadNumericChunk(data, onUploadProgress) {
  return request({
    url: '/quality/fault-iden/catalog/upload-numeric-chunk',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data',
      repeatSubmit: false
    },
    timeout: 300000,
    onUploadProgress
  })
}

function uploadNumericApi(data) {
  return request({
    url: '/quality/fault-iden/catalog/upload-numeric-api',
    method: 'post',
    data,
    timeout: 300000
  })
}

function mergeNumericChunks(data) {
  return request({
    url: '/quality/fault-iden/catalog/merge-numeric-chunks',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data',
      repeatSubmit: false
    },
    timeout: 300000
  })
}

function uploadProcessTextData(data) {
  return request({
    url: '/monitor/text/process/import',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data',
      repeatSubmit: false
    },
    timeout: 300000
  })
}

function uploadProcessTextApiData(data) {
  return request({
    url: '/monitor/text/api/import',
    method: 'post',
    data,
    timeout: 300000
  })
}

function uploadPartQualityData(data) {
  return request({
    url: '/quality/part-quality/import',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data',
      repeatSubmit: false
    },
    timeout: 300000
  })
}

function uploadPartQualityApiData(data) {
  return request({
    url: '/quality/part-quality/api/import',
    method: 'post',
    data,
    timeout: 300000
  })
}

function downloadPartQualityTemplateApi() {
  return request({
    url: '/quality/part-quality/template',
    method: 'get',
    responseType: 'blob',
    timeout: 300000
  })
}

function downloadPartProcessTemplateApi(params) {
  return request({
    url: '/monitor/text/process/template',
    method: 'get',
    params,
    responseType: 'blob',
    timeout: 300000
  })
}

function downloadHierarchyTemplateApi() {
  return request({
    url: '/monitor/text/hierarchy/template',
    method: 'get',
    responseType: 'blob',
    timeout: 300000
  })
}

function uploadHierarchyData(data) {
  return request({
    url: '/monitor/text/hierarchy/import',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data',
      repeatSubmit: false
    },
    timeout: 300000
  })
}

function listAllDataFiles(query) {
  return request({
    url: '/quality/fault-iden/samples',
    method: 'get',
    params: query
  })
}

function deleteDataFile(id) {
  return request({
    url: `/quality/fault-iden/samples/${id}`,
    method: 'delete'
  })
}

function updateDataFileUsage(id, dataUsage) {
  return request({
    url: `/quality/fault-iden/samples/${id}/data-usage`,
    method: 'put',
    data: { dataUsage }
  })
}

function uploadWarningDetectFile(data, onUploadProgress) {
  return request({
    url: '/quality/fault-iden/catalog/upload-numeric-file',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data',
      repeatSubmit: false
    },
    timeout: 300000,
    onUploadProgress
  })
}

function startWarningDetectTask(data) {
  return request({
    url: '/feedback/warning/detect/tasks',
    method: 'post',
    data
  })
}

function getWarningDetectTask(taskId) {
  return request({
    url: `/feedback/warning/detect/tasks/${encodeURIComponent(taskId)}/status`,
    method: 'get'
  })
}

function cancelWarningDetectTask(taskId) {
  return request({
    url: `/feedback/warning/detect/tasks/${encodeURIComponent(taskId)}/cancel`,
    method: 'post'
  })
}

function startKeyProcessTask(data) {
  return request({
    url: '/service/identify/key_process_tasks',
    method: 'post',
    data
  })
}

function getKeyProcessTask(taskId) {
  return request({
    url: `/service/identify/key_process_tasks/${encodeURIComponent(taskId)}/status`,
    method: 'get'
  })
}

function cancelKeyProcessTask(taskId) {
  return request({
    url: `/service/identify/key_process_tasks/${encodeURIComponent(taskId)}/cancel`,
    method: 'post'
  })
}

const tree_loading = ref(false)
const node_loading = ref(false)
const part_loading = ref(false)


const treeData = ref([])
const moduleTreeRef = ref(null)
const numTreeRef = ref(null)
const numFileInput = ref(null)
const textTreeRef = ref(null)
const textFileInput = ref(null)
const currentTreeNodeId = ref('')
const current_node = ref(null)
const breadcrumbList = ref([])
const currentChildren = ref([])
const part_list = ref([])
const partMaterialOptions = ref([])
const partStatusOptions = ref([])
const numImporting = ref(false)
const textImporting = ref(false)
const numProg = reactive({
  visible: false,
  percentage: 0,
  text: ''
})
let numProgTimer = null
const NUM_MB = 1024 * 1024
const NUM_KB = 1024
const NUM_NORMAL_UPLOAD_LIMIT = 5*1024 * NUM_KB
const NUM_IN_FLIGHT_SIZE = 400 * NUM_MB
const NUM_CHUNK_SIZE = 5*1024 * NUM_KB
const NUM_UPLOAD_WAITING = 'WAITING'
const NUM_UPLOAD_UPLOADING = 'UPLOADING'
const NUM_UPLOAD_SUCCESS = 'SUCCESS'
const NUM_UPLOAD_FAILED = 'FAILED'
const NUM_UPLOAD_CHUNK_UPLOADING = 'CHUNK_UPLOADING'
const NUM_UPLOAD_MERGING = 'MERGING'
const numUploadRows = ref([])
let numUploadBatchId = ''
const numPurposeOpts = [
  { label: '服役周期', value: 'faultIdentify', desc: '支持选择分系统、设备、组件' },
  { label: '制造周期', value: 'processAnomaly', desc: '支持选择任意层级对象' },
]
const textTaskOpts = [
  {
    label: '零件标准制作过程',
    value: 'partProcess',
    desc: '导入零件及制造工序',
    objectLevelText: '无需选择',
    rule: '无需选择任务对象，Excel需包含零件模板、工序路线、详细工序三个Sheet。'
  },
  {
    label: '零件实际制作过程',
    value: 'PART_ACTUAL_MANUFACTURING_PROCESS',
    desc: '导入零件实例、生产工单和工序执行记录',
    objectLevelText: '无需选择',
    rule: 'Excel需包含零件实例、生产工单、工序执行记录三个Sheet。'
  },
  {
    label: '零件质量信息',
    value: 'partQuality',
    desc: '导入零件设计、制造、服役质量信息',
    objectLevelText: '无需选择',
    rule: '无需选择任务对象，导入时按Excel中的零件模板ID、零件实例ID等字段校验关联关系。'
  },
  {
    label: '层级对象',
    value: 'hierarchy',
    desc: '导入飞机、分系统、设备、组件、零件模板和零件实例',
    objectLevelText: '无需选择',
    rule: 'Excel需包含飞机、分系统、设备、组件、零件、零件信息六个Sheet。'
  }
]
const numObjDlg = reactive({
  visible: false,
  loading: false,
  importType: 'numeric',
  keyword: '',
  form: {
    purpose: '',
    aircraftId: '',
    subsystemId: '',
    equipmentId: '',
    componentId: '',
    partId: '',
    importMode: 'file',
    apiUrl: '',
    apiMethod: 'GET',
    apiHeaders: '',
    apiBody: '',
    apiFileName: ''
  }
})
const numObjDlgTitle = computed(() => numObjDlg.importType === 'sequence' ? '选择时序数据对象' : '选择数值型数据对象')
const textObjDlg = reactive({
  visible: false,
  loading: false,
  keyword: '',
  form: {
    taskType: 'partProcess',
    componentId: '',
    partId: '',
    importMode: 'file',
    apiUrl: '',
    apiMethod: 'GET',
    apiHeaders: '',
    apiBody: '',
    apiFileName: ''
  }
})
const dataFileDialog = reactive({
  visible: false,
  loading: false,
  deleting: false,
  selectingAll: false,
  rows: [],
  total: 0,
  selectedRows: []
})
const dataFileTableRef = ref(null)
const dataFileSelectedCount = computed(() => dataFileDialog.selectedRows.length)
const dataFileQuery = reactive({
  dataUsage: 'ALL',
  keyword: '',
  uploadBatchId: '',
  pageNum: 1,
  pageSize: 20
})

const partQuery = reactive({
  module_id: '',
  keyword: '',
  material: '',
  status: '',
  page_num: 1,
  page_size: 10
})



const context_menu = reactive({
  visible: false,
  x: 0,
  y: 0,
  node: null
})
const kqcDialog = reactive({
  visible: false,
  loading: false,
  canceling: false,
  lookupTaskId: '',
  lookupLoading: false,
  taskId: '',
  status: '',
  error: '',
  node: null,
  form: {
    maxRows: 200000,
    maxFeatures: 80,
    perStation: 2,
    ssRuns: 5
  },
  result: {}
})
const kqcHistory = reactive({
  loading: false,
  rows: [],
  total: 0,
  query: {
    keyword: '',
    status: '',
    page_num: 1,
    page_size: 10
  }
})
const KQC_DONE_STATUSES = ['SUCCESS', 'FAILED', 'CANCELED']
let kqcPollTimer = null
let kqcPollToken = 0
let kqcPollCount = 0
const kqcSubmitting = ref(false)
const kqcRunning = ref(false)
const kqcTaskActive = computed(() => kqcRunning.value && kqcDialog.taskId && ['PENDING', 'RUNNING'].includes(kqcDialog.status))
const kqcSubmitLoading = computed(() => kqcSubmitting.value)
const kqcSubmitDisabled = computed(() => kqcSubmitting.value || kqcTaskActive.value)
const kqcInfluenceRows = computed(() => {
  const rows = kqcDialog.result?.topInfluences || kqcDialog.result?.top_influences || []
  return Array.isArray(rows) ? rows : []
})
const kqcRankedRows = computed(() => kqcInfluenceRows.value
  .map(row => ({
    ...row,
    weight: Number(row.weight || 0),
    frequency: Number(row.frequency || 0),
    absWeight: Math.abs(Number(row.weight || 0))
  }))
  .sort((a, b) => b.absWeight - a.absWeight)
  .map((row, index) => ({ ...row, rank: index + 1 })))
const kqcGraphDialog = reactive({ visible: false })
const kqcRankingChartRef = ref(null)
const kqcGraphRef = ref(null)
let kqcRankingChart = null
let kqcGraphChart = null
const kqcGraphData = computed(() => {
  const responseGraph = kqcDialog.result?.responseGraphData || kqcDialog.result?.response_graph_data
  const fullGraph = kqcDialog.result?.graphData || kqcDialog.result?.graph_data
  return responseGraph?.nodes?.length ? responseGraph : (fullGraph?.nodes?.length ? fullGraph : { nodes: [], links: [], categories: [] })
})
const KQC_MISSING_RESPONSE_TIP = '当前 train_numeric.csv 缺少响应结果列，无法直接进行关键质量特性挖掘。请使用包含响应结果列的训练数据；如果没有响应结果列，请提供已完成的 KQC输出目录用于关键工序识别。该目录需要包含 bosch_adj_matrix_selected.csv 和 bosch_edge_frequency.csv。'
const detectDialog = reactive({
  visible: false,
  loading: false,
  canceling: false,
  lookupTaskId: '',
  lookupLoading: false,
  taskId: '',
  status: '',
  error: '',
  node: null,
  result: {},
  form: {
    enabled: true,
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
  }
})
const detectFileQuery = reactive({ keyword: '', samples: [], pageNum: 1, pageSize: 20, total: 0, loading: false })
const detectSelectedSamples = ref([])
const detectSampleTableRef = ref(null)
const kqcFileQuery = reactive({ keyword: '', samples: [], pageNum: 1, pageSize: 20, total: 0, loading: false })
const kqcSelectedSamples = ref([])
const kqcSampleTableRef = ref(null)
const detectUpload = reactive({ loading: false })
const detectUploadRows = ref([])
const DETECT_DONE_STATUSES = ['SUCCESS', 'FAILED', 'CANCELED']
let detectPollTimer = null
let detectPollToken = 0
let detectPollCount = 0
const detectSubmitting = ref(false)
const detectRunning = ref(false)
const anomalyChartRef = ref(null)
let anomalyChart = null
const detectTaskActive = computed(() => detectRunning.value && detectDialog.taskId && ['PENDING', 'RUNNING'].includes(detectDialog.status))
const detectSubmitLoading = computed(() => detectSubmitting.value)
const detectSubmitDisabled = computed(() => detectSubmitting.value || detectTaskActive.value)
const detectTrainSampleIds = computed(() => detectSelectedSamples.value.map(item => item.id))
const detectUploadSampleIds = computed(() => detectUploadRows.value.map(item => item.id).filter(Boolean))
const detectSelectedSampleIds = computed(() => detectDialog.form.enabled ? detectTrainSampleIds.value : [...detectTrainSampleIds.value, ...detectUploadSampleIds.value])
const kqcSelectedSampleIds = computed(() => kqcSelectedSamples.value.map(item => item.id))
const detectTargetText = computed(() => detectDialog.node ? `${detectDialog.node.name || detectDialog.node.id || '--'}` : '--')
const detectTaskProgress = computed(() => {
  const value = Number(detectDialog.result?.progress || 0)
  return Number.isFinite(value) ? Math.max(0, Math.min(100, value)) : 0
})
const detectTaskMessage = computed(() => detectDialog.result?.message || detectDialog.result?.errorMessage || '')
const detectAbnormalText = computed(() => {
  const value = detectDialog.result?.isAbnormal ?? detectDialog.result?.is_abnormal
  if (value === true || value === 'true') return '是'
  if (value === false || value === 'false') return '否'
  return '--'
})
const detectCurveRows = computed(() => {
  const rows = detectDialog.result?.curveData || detectDialog.result?.curve_data || []
  return Array.isArray(rows) ? rows.map(item => ({ x: item.x, value: item.value ?? item.y ?? item.riskScore })) : []
})
const detectCurveValues = computed(() => detectCurveRows.value.map(item => Number(item.value)).filter(Number.isFinite))
const detectCurveMaxValue = computed(() => detectCurveValues.value.length ? Math.max(...detectCurveValues.value) : null)
const detectCurveThreshold = computed(() => {
  const value = detectDialog.result?.threshold ?? detectDialog.result?.ucl ?? detectDialog.result?.UCL_sample ?? detectDialog.result?.summary?.UCL_sample ?? detectDialog.result?.summary?.ucl
  const num = Number(value)
  return Number.isFinite(num) ? num : null
})
const keyDialog = reactive({
  visible: false,
  loading: false,
  canceling: false,
  lookupTaskId: '',
  lookupLoading: false,
  taskId: '',
  status: '',
  error: '',
  node: null,
  result: {},
  form: {
    enabled: true,
    kqcTaskId: '',
    kqcOutputDir: '',
    keepFreq: 0.6,
    weightThresh: 0.0001,
    topN: 10
  }
})
const keyFileQuery = reactive({ keyword: '', samples: [], pageNum: 1, pageSize: 20, total: 0, loading: false })
const keySelectedSamples = ref([])
const keySampleTableRef = ref(null)
const KEY_DONE_STATUSES = ['SUCCESS', 'FAILED', 'CANCELED']
const MANUFACTURING_DATA_USAGES = ['MANUFACTURE_COMMON', 'PROCESS_ANOMALY', 'KQC_MINING', 'KEY_PROCESS']
const DETECT_FILE_DATA_USAGES = ['PROCESS_ANOMALY', 'MANUFACTURE_COMMON']
const KQC_FILE_DATA_USAGES = ['KQC_MINING']
const KEY_FILE_DATA_USAGES = ['KEY_PROCESS', 'MANUFACTURE_COMMON']
let keyPollTimer = null
let keyPollToken = 0
let keyPollCount = 0
const keySubmitting = ref(false)
const keyRunning = ref(false)
const keyTaskActive = computed(() => keyRunning.value && keyDialog.taskId && ['PENDING', 'RUNNING'].includes(keyDialog.status))
const keySubmitLoading = computed(() => keySubmitting.value)
const keySubmitDisabled = computed(() => keySubmitting.value || keyTaskActive.value)
const keySelectedSampleIds = computed(() => keySelectedSamples.value.map(item => item.id))
const keyTargetText = computed(() => keyDialog.node ? `${keyDialog.node.name || keyDialog.node.id || '--'}` : '--')
const keyProcessRows = computed(() => {
  const rows = keyDialog.result?.candidateProcesses || keyDialog.result?.candidate_processes || []
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
const nodeDetailDialog = reactive({
  visible: false,
  loading: false,
  type: '',
  node: null,
  form: {
    name: ''
  },
  partForm: {
    part_template_id: '',
    part_number: '',
    part_name: '',
    component_id: '',
    material: '',
    specification: ''
  }
})
const partQualityDialog = reactive({
  visible: false,
  loading: false,
  part_name: '',
  part_code: '',
  tables: []
})

const partQualityColumnLabels = {
  design_quality_id: '设计质量ID',
  manufacturing_quality_id: '制造质量ID',
  service_quality_id: '服役质量ID',
  part_template_id: '零件模板ID',
  part_instance_id: '零件实例ID',
  quality_level: '质量等级',
  inspection_item: '检验项目',
  inspection_result: '检验结果',
  inspection_time: '检验时间',
  inspection_person: '检验人员',
  test_item: '试验项目',
  test_result: '试验结果',
  test_time: '试验时间',
  inspector: '检验员',
  defect_type: '缺陷类型',
  defect_desc: '缺陷描述',
  fault_type: '故障类型',
  fault_desc: '故障描述',
  service_time: '服役时间',
  create_time: '创建时间',
  update_time: '更新时间',
  remark: '备注',
  remarks: '备注'
}

const addModuleDialog = reactive({
  visible: false,
  loading: false,
  current_node_id: '',
  current_node_name: '',
  module_name: ''
})


const addPartInstanceDialog = reactive({
  visible: false,
  loading: false,
  form: {
    part_code: '',
    part_name: '',
    material: '',
    spec_model: '',
    serial_number: '',
    batch_number: '',
    manufacturer: '',
    production_date: '',
    status: '',
    quality_level: '',
    key_degree: ''
  }
})

const editPartInstanceDialog = reactive({
  visible: false,
  loading: false,
  form: {
    part_instance_id: '',
    part_code: '',
    part_name: '',
    material: '',
    spec_model: '',
    serial_number: '',
    batch_number: '',
    manufacturer: '',
    production_date: '',
    status: '',
    quality_level: '',
    key_degree: ''
  }
})

const addPartTemplateDialog = reactive({
  visible: false,
  loading: false,
  form: {
    part_code: '',
    part_name: '',
    material: '',
    spec_model: ''
  }
})

const isTerminalNode = computed(() => !!current_node.value && current_node.value.terminal === true)
const numUploadStat = computed(() => {
  const rows = numUploadRows.value
  return {
    success: rows.filter(item => item.status === NUM_UPLOAD_SUCCESS).length,
    failed: rows.filter(item => item.status === NUM_UPLOAD_FAILED).length,
    uploading: rows.filter(item => [NUM_UPLOAD_UPLOADING, NUM_UPLOAD_CHUNK_UPLOADING, NUM_UPLOAD_MERGING].includes(item.status)).length
  }
})
const addEntryText = computed(() => {
  const nodeId = context_menu.node?.id || ''
  return String(nodeId).startsWith('part_template:') ? '新增零件' : '新增模块'
})



const deleteEntryText = computed(() => {
  const nodeId = context_menu.node?.id || ''
  return String(nodeId).startsWith('part_template:') ? '删除零件' : '删除模块'
})
const currentPathText = computed(() => breadcrumbList.value.map(item => item.name).join(' / '))

const canGoParent = computed(() => !!current_node.value?.id)

const isProcAnom = computed(() => numObjDlg.form.purpose === 'processAnomaly')
const curNumPurposeLabel = computed(() => {
  return numPurposeOpts.find(item => item.value === numObjDlg.form.purpose)?.label || '未选择'
})
const numObjReqLevelText = computed(() =>
  isProcAnom.value ? '任意层级对象' : '分系统、设备、组件'
)
const numObjReqText = computed(() =>
  isProcAnom.value ? '请选择任务对象' : '请选择分系统、设备或组件'
)
const numSelNodeId = computed(() => {
  const form = numObjDlg.form
  if (form.partId) return prefixedNodeId('part_template', form.partId)
  if (form.componentId) return prefixedNodeId('component', form.componentId)
  if (form.equipmentId) return prefixedNodeId('equipment', form.equipmentId)
  if (form.subsystemId) return prefixedNodeId('subsystem', form.subsystemId)
  if (form.aircraftId) return prefixedNodeId('aircraft', form.aircraftId)
  return ''
})
const numSelObj = computed(() => {
  const node = findTreeNode(treeData.value, numSelNodeId.value)
  if (!node) return null
  return {
    name: node.name,
    typeText: numNodeTypeText(node),
    routeName: node.route_name || '',
    processCount: numNodeType(node) === 'part_template' ? Number(node.process_count || 0) : null
  }
})
const numSelPath = computed(() =>
  findNodePath(treeData.value, numSelNodeId.value).map(node => ({
    id: node.id,
    name: node.name,
    typeText: numNodeTypeText(node)
  }))
)
const curTextTask = computed(() =>
  textTaskOpts.find(item => item.value === textObjDlg.form.taskType) || textTaskOpts[0]
)
const curTextTaskLabel = computed(() => curTextTask.value.label)
const curTextTaskRule = computed(() => curTextTask.value.rule)
const textObjReqLevelText = computed(() => curTextTask.value.objectLevelText)
const isTextPartQuality = computed(() => textObjDlg.form.taskType === 'partQuality')
const isTextHierarchy = computed(() => textObjDlg.form.taskType === 'hierarchy')
const isTextActualProcess = computed(() => textObjDlg.form.taskType === 'PART_ACTUAL_MANUFACTURING_PROCESS')
const textTaskRequiresObject = computed(() => !['partProcess', 'partQuality', 'hierarchy', 'PART_ACTUAL_MANUFACTURING_PROCESS'].includes(textObjDlg.form.taskType))
const hasTextImportTemplate = computed(() => ['partProcess', 'partQuality', 'hierarchy', 'PART_ACTUAL_MANUFACTURING_PROCESS'].includes(textObjDlg.form.taskType))
const textConfirmText = computed(() => '导入数据')
const textSelNodeId = computed(() => {
  if (!textTaskRequiresObject.value) return ''
  if (isTextPartQuality.value) return prefixedNodeId('part_template', textObjDlg.form.partId)
  return prefixedNodeId('component', textObjDlg.form.componentId)
})
const textSelObj = computed(() => {
  const node = findTreeNode(treeData.value, textSelNodeId.value)
  if (!node) return null
  return {
    name: node.name,
    typeText: numNodeTypeText(node),
    routeName: node.route_name || '',
    processCount: numNodeType(node) === 'part_template' ? Number(node.process_count || 0) : null
  }
})
const textSelPath = computed(() =>
  findNodePath(treeData.value, textSelNodeId.value).map(node => ({
    id: node.id,
    name: node.name,
    typeText: numNodeTypeText(node)
  }))
)

function displayCount(value) {
  return value === null || value === undefined ? '暂无数据' : value
}
function formatChildCountText(item) {
  const countText = displayCount(item?.child_count)
  const level = Number(item?.level || 0)
  if (level === 1) return `分系统：${countText}`
  if (level === 2) return `设备：${countText}`
  if (level === 3) return `组件：${countText}`
  if (level === 4) return ''
  if (level === 5) return ''
  return `子级：${countText}`
}



function fetchNodeView(nodeId) {
  return request({
    url: `/monitor/node/${nodeId}/view`,
    method: 'get'
  })
}


function applyRootView(tree) {
  currentTreeNodeId.value = ''
  current_node.value = null
  breadcrumbList.value = []
  currentChildren.value = Array.isArray(tree) ? tree : []
  partQuery.module_id = ''
  part_list.value = []
}

function findTreeNode(nodes, nodeId) {
  if (!Array.isArray(nodes) || !nodeId) return null
  for (const n of nodes) {
    if (n.id === nodeId) return n
    const found = findTreeNode(n.children || [], nodeId)
    if (found) return found
  }
  return null
}

function rawNodeId(nodeId) {
  const text = String(nodeId || '')
  const index = text.indexOf(':')
  return index >= 0 ? text.slice(index + 1) : text
}

function prefixedNodeId(type, rawId) {
  return rawId ? `${type}:${rawId}` : ''
}

function numNodeType(data) {
  return String(data?.id || '').split(':')[0] || ''
}

function numNodeTypeText(data) {
  const map = {
    aircraft: '飞机',
    subsystem: '分系统',
    equipment: '设备',
    component: '组件',
    part_template: '零件'
  }
  return map[numNodeType(data)] || '对象'
}

function isNumNodeSelectable(data) {
  const type = numNodeType(data)
  if (isProcAnom.value) return !!type
  return ['subsystem', 'equipment', 'component'].includes(type)
}

function isNumNodeSelected(data) {
  return data?.id === numSelNodeId.value
}

function findNodePath(nodes, nodeId, path = []) {
  if (!Array.isArray(nodes) || !nodeId) return []
  for (const node of nodes) {
    const nextPath = path.concat(node)
    if (node.id === nodeId) return nextPath
    const found = findNodePath(node.children || [], nodeId, nextPath)
    if (found.length) return found
  }
  return []
}

function applyNumNodeSel(data) {
  const path = findNodePath(treeData.value, data?.id)
  const form = numObjDlg.form
  form.aircraftId = ''
  form.subsystemId = ''
  form.equipmentId = ''
  form.componentId = ''
  form.partId = ''

  path.forEach(node => {
    const rawId = rawNodeId(node.id)
    const type = numNodeType(node)
    if (type === 'aircraft') form.aircraftId = rawId
    if (type === 'subsystem') form.subsystemId = rawId
    if (type === 'equipment') form.equipmentId = rawId
    if (type === 'component') form.componentId = rawId
    if (type === 'part_template') form.partId = rawId
  })
}

function onNumNodeClick(data) {
  if (!isNumNodeSelectable(data)) return
  applyNumNodeSel(data)
}

function filterNumTree(value, data) {
  if (!value) return true
  const keyword = String(value).toLowerCase()
  if (String(data?.name || '').toLowerCase().includes(keyword)) return true
  return (data?.children || []).some(child => filterNumTree(value, child))
}

function isTextNodeSelectable(data) {
  if (!textTaskRequiresObject.value) return false
  return numNodeType(data) === (isTextPartQuality.value ? 'part_template' : 'component')
}

function isTextNodeSelected(data) {
  return data?.id === textSelNodeId.value
}

function onTextNodeClick(data) {
  if (!isTextNodeSelectable(data)) return
  if (isTextPartQuality.value) {
    textObjDlg.form.partId = rawNodeId(data.id)
    return
  }
  textObjDlg.form.componentId = rawNodeId(data.id)
}

function filterTextTree(value, data) {
  if (!value) return true
  const keyword = String(value).toLowerCase()
  if (String(data?.name || '').toLowerCase().includes(keyword)) return true
  return (data?.children || []).some(child => filterTextTree(value, child))
}

watch(
  () => numObjDlg.keyword,
  value => {
    numTreeRef.value?.filter(value)
  }
)

watch(
  () => textObjDlg.keyword,
  value => {
    textTreeRef.value?.filter(value)
  }
)

async function reloadTreeView(targetNodeId, fallbackParentId = '') {
  const res = await fetchMonitorTree()
  const tree = Array.isArray(res.data) ? res.data : []
  treeData.value = tree

  if (!targetNodeId) {
    applyRootView(tree)
    return
  }
  const targetNode = findTreeNode(tree, targetNodeId)
  if (targetNode) {
    enterNode(targetNode)
    return
  }

  if (fallbackParentId) {
    const parentNode = findTreeNode(tree, fallbackParentId)
    if (parentNode) {
      enterNode(parentNode)
      return
    }
  }

  applyRootView(tree)
}

function enterNode(node) {
  if (!node || !node.id) return
  syncTreeFocus(node.id)
  current_node.value = node
  loadNodeView(node.id)
}

function syncTreeFocus(nodeId) {
  if (!nodeId) return
  currentTreeNodeId.value = nodeId
  nextTick(() => {
    if (!moduleTreeRef.value) return
    moduleTreeRef.value.setCurrentKey(nodeId)
    const currentTreeNode = moduleTreeRef.value.getNode(nodeId)
    if (!currentTreeNode) return
    let parent = currentTreeNode.parent
    while (parent) {
      if (parent.level > 0) {
        parent.expanded = true
      }
      parent = parent.parent
    }
  })
}



function loadPartInstances() {
  if (!partQuery.module_id) {
    part_list.value = []
    partMaterialOptions.value = []
    partStatusOptions.value = []
    return
  }

  const keyword = (partQuery.keyword || '').trim()
  const material = (partQuery.material || '').trim()
  const status = (partQuery.status || '').trim()
  
  const query = {
    ...partQuery,
    keyword,
    material,
    status
  }

  part_loading.value = true

  fetchPartInstances(query)
    .then(res => {
      const data = res.data || {}
      const rows = Array.isArray(data.rows) ? data.rows : []
      part_list.value = rows
      refreshPartFilterOptions(rows)
    })

    .catch(() => {
      part_list.value = []
      partMaterialOptions.value = []
      partStatusOptions.value = []
    })
    .finally(() => {
      part_loading.value = false
    })

}

function applyPartFilters() {
  partQuery.page_num = 1
  loadPartInstances()
}

function resetPartFilters() {
  partQuery.keyword = ''
  partQuery.material = ''
  partQuery.status = ''
  partQuery.page_num = 1
  loadPartInstances()
}

function refreshPartFilterOptions(rows) {
  const materialSet = new Set(partMaterialOptions.value)
  const statusSet = new Set(partStatusOptions.value)
  for (const row of rows || []) {
    const material = String(row?.material || '').trim()
    const status = String(row?.status || '').trim()
    if (material) materialSet.add(material)
    if (status) statusSet.add(status)
  }
  partMaterialOptions.value = Array.from(materialSet)
  partStatusOptions.value = Array.from(statusSet)
}

function handleTreeClick(data) {
  enterNode(data)
}

function handleTreeContextMenu(event, data) {
  event.preventDefault()
  openContextMenu(event, data, true)
}

function goParent() {
  if (!canGoParent.value) return
  const parentId = current_node.value?.parent_id
  if (!parentId) {
    applyRootView(treeData.value)
    return
  }
  const parentNode = findTreeNode(treeData.value, parentId)
  if (parentNode) {
    enterNode(parentNode)
    return
  }
  applyRootView(treeData.value)
}

function openContextMenu(event, node, select_first = false) {
  if (select_first && node && node.id) {
    enterNode(node)
  }
  context_menu.x = event.clientX
  context_menu.y = event.clientY
  context_menu.node = node
  context_menu.visible = true
}

function closeContextMenu() {
  context_menu.visible = false
  context_menu.node = null
}

function openCreateModuleDialog() {
  const node = context_menu.node
  closeContextMenu()

  if (!node || !node.id) {
    ElMessage.warning('请先选择模块')
    return
  }
  addModuleDialog.visible = true
  addModuleDialog.current_node_id = node.id
  addModuleDialog.current_node_name = node.name || ''
  addModuleDialog.module_name = ''
}
function handleAddEntry() {
  const nodeId = context_menu.node?.id || ''
  if (String(nodeId).startsWith('part_template:')) {
    openCreatePartTemplateDialog()
    return
  }
  openCreateModuleDialog()
}
async function handleViewNodeDetail() {
  const node = context_menu.node
  closeContextMenu()
  if (!node) {
    ElMessage.warning('请先选择对象')
    return
  }
  nodeDetailDialog.node = { ...node }
  nodeDetailDialog.type = String(node.id || '').startsWith('part_template:') ? 'part_template' : 'module'
  nodeDetailDialog.form.name = node.name || ''
  nodeDetailDialog.visible = true
  if (nodeDetailDialog.type === 'part_template') {
    await loadPartTemplateDetail(node.id)
  }
}

function rawPartTemplateId(nodeId) {
  const value = String(nodeId || '')
  return value.startsWith('part_template:') ? value.slice('part_template:'.length) : value
}

function resetPartTemplateDetailForm(data = {}) {
  nodeDetailDialog.partForm.part_template_id = data.part_template_id || ''
  nodeDetailDialog.partForm.part_number = data.part_number || ''
  nodeDetailDialog.partForm.part_name = data.part_name || ''
  nodeDetailDialog.partForm.component_id = data.component_id || ''
  nodeDetailDialog.partForm.material = data.material || ''
  nodeDetailDialog.partForm.specification = data.specification || ''
}

async function loadPartTemplateDetail(nodeId) {
  const partTemplateId = rawPartTemplateId(nodeId)
  resetPartTemplateDetailForm({ part_template_id: partTemplateId })
  if (!partTemplateId) return
  nodeDetailDialog.loading = true
  try {
    const res = await fetchPartTemplateDetail(partTemplateId)
    resetPartTemplateDetailForm(res?.data || {})
  } catch {
    ElMessage.error('获取零件模板详情失败')
  } finally {
    nodeDetailDialog.loading = false
  }
}

async function submitNodeDetail() {
  if (nodeDetailDialog.type === 'part_template') {
    await submitPartTemplateDetail()
    return
  }
  await submitNodeDetailName()
}

async function submitNodeDetailName() {
  const node = nodeDetailDialog.node
  const name = (nodeDetailDialog.form.name || '').trim()
  if (!node || !node.id) {
    ElMessage.warning('请选择对象')
    return
  }
  if (!name) {
    ElMessage.warning('请输入对象名称')
    return
  }

  nodeDetailDialog.loading = true
  try {
    const keepNodeId = current_node.value?.id || node.id
    await updateModuleName(node.id, { module_name: name })
    ElMessage.success('修改成功')
    nodeDetailDialog.visible = false
    await reloadTreeView(keepNodeId)
  } catch {
    ElMessage.error('修改对象名称失败')
  } finally {
    nodeDetailDialog.loading = false
  }
}

async function submitPartTemplateDetail() {
  const node = nodeDetailDialog.node
  const form = nodeDetailDialog.partForm
  const partTemplateId = rawPartTemplateId(node?.id || form.part_template_id)
  const partNumber = (form.part_number || '').trim()
  const partName = (form.part_name || '').trim()
  if (!partTemplateId) {
    ElMessage.warning('请选择零件模板')
    return
  }
  if (!partNumber) {
    ElMessage.warning('请输入零件编号')
    return
  }
  if (!partName) {
    ElMessage.warning('请输入零件名称')
    return
  }

  nodeDetailDialog.loading = true
  try {
    const keepNodeId = current_node.value?.id || node?.id || ''
    await updatePartTemplateDetail(partTemplateId, {
      part_number: partNumber,
      part_name: partName,
      material: (form.material || '').trim(),
      specification: (form.specification || '').trim()
    })
    ElMessage.success('修改成功')
    nodeDetailDialog.visible = false
    await reloadTreeView(keepNodeId)
  } catch {
    ElMessage.error('修改零件模板详情失败')
  } finally {
    nodeDetailDialog.loading = false
  }
}

async function submitCreateModule() {
  if (!addModuleDialog.module_name || !addModuleDialog.module_name.trim()) {
    ElMessage.warning('请输入模块名称')
    return

  }

  addModuleDialog.loading = true
  try {
    const keepNodeId = current_node.value?.id || ''
    await createModule({
      current_node_id: addModuleDialog.current_node_id,
      module_name: addModuleDialog.module_name.trim()
    })
    ElMessage.success('新增成功')

    addModuleDialog.visible = false
    await reloadTreeView(keepNodeId)
  } catch {
  } finally {
    addModuleDialog.loading = false
  }
}

async function handleQualityMining() {
  const node = context_menu.node
  closeContextMenu()


  if (!node || !node.id) {
    ElMessage.warning('请先选择模块')
    return
  }

  kqcDialog.node = node
  clearKqcPolling()
  kqcSubmitting.value = false
  kqcRunning.value = false
  kqcDialog.loading = false
  kqcDialog.canceling = false
  kqcDialog.lookupTaskId = ''
  kqcDialog.lookupLoading = false
  kqcDialog.result = {}
  kqcDialog.taskId = ''
  kqcDialog.status = ''
  kqcDialog.error = ''
  kqcGraphDialog.visible = false
  kqcDialog.visible = true
  clearKqcSamples()
  kqcFileQuery.pageNum = 1
  await loadKqcSamples()
}

async function handleProcessAnomalyDetect() {
  const node = context_menu.node
  closeContextMenu()

  if (!node || !node.id) {
    ElMessage.warning('请先选择模块')
    return
  }
  clearDetectPolling()
  detectSubmitting.value = false
  detectRunning.value = false
  detectDialog.loading = false
  detectDialog.canceling = false
  detectDialog.lookupTaskId = ''
  detectDialog.lookupLoading = false
  detectDialog.result = {}
  detectDialog.taskId = ''
  detectDialog.status = ''
  detectDialog.error = ''
  detectDialog.node = node
  detectDialog.visible = true
  clearDetectSamples()
  clearDetectUploads()
  detectFileQuery.pageNum = 1
  await loadDetectSamples()
}

async function handleKeyProcessIdentify() {
  const node = context_menu.node
  closeContextMenu()

  if (!node || !node.id) {
    ElMessage.warning('请先选择模块')
    return
  }
  clearKeyPolling()
  keySubmitting.value = false
  keyRunning.value = false
  keyDialog.loading = false
  keyDialog.canceling = false
  keyDialog.lookupTaskId = ''
  keyDialog.lookupLoading = false
  keyDialog.result = {}
  keyDialog.taskId = ''
  keyDialog.status = ''
  keyDialog.error = ''
  keyDialog.node = node
  keyDialog.visible = true
  clearKeySamples()
  keyFileQuery.pageNum = 1
  if (!keyDialog.form.enabled) {
    await loadKeySamples()
  }
}

async function submitKqcMining() {
  if (kqcSubmitting.value || kqcTaskActive.value) {
    startKqcPolling(kqcDialog.taskId)
    return
  }
  if (!kqcSelectedSampleIds.value.length) {
    ElMessage.warning('请选择关键质量特性挖掘数据文件')
    return
  }

  clearKqcPolling()
  kqcSubmitting.value = true
  kqcRunning.value = true
  kqcDialog.loading = true
  kqcDialog.status = 'PENDING'
  kqcDialog.error = ''
  kqcDialog.result = {}
  try {
    const node = kqcDialog.node
    const hierarchyIds = sampleObjectQuery(node) || {}
    const json = await request({
      url: '/feedback/warning/kqc-mining',
      method: 'post',
      data: {
        dataSelectionMode: 'SELECTED_FILES',
        dataUsage: 'KQC_MINING',
        sampleIds: kqcSelectedSampleIds.value,
        targetType: numNodeType(node),
        targetId: rawNodeId(node.id),
        targetName: node.name || rawNodeId(node.id),
        ...hierarchyIds,
        maxRows: kqcDialog.form.maxRows,
        maxFeatures: kqcDialog.form.maxFeatures,
        perStation: kqcDialog.form.perStation,
        selectionMode: 'response_assoc',
        ssRuns: kqcDialog.form.ssRuns
      },
      timeout: 30000
    })
    const payload = getKqcPayload(json)
    if (payload?.status === 'FAILED' || json?.success === false || json?.code === 500) {
      throw new Error(payload?.message || json?.message || '关键质量特性挖掘任务提交失败')
    }
    acceptKqcTask(payload)
    ElMessage.success('关键质量特性挖掘任务已提交')
  } catch (e) {
    kqcRunning.value = false
    kqcDialog.status = 'FAILED'
    kqcDialog.error = formatKqcError(e?.message || '关键质量特性挖掘任务提交失败')
    ElMessage.error(kqcDialog.error)
  } finally {
    kqcSubmitting.value = false
    kqcDialog.loading = false
  }
}

function cleanLocalPath(path) {
  return String(path || '').replace(/[\u200e\u200f\u202a-\u202e\u2066-\u2069\ufeff]/g, '').trim()
}

function getKqcPayload(response) {
  return response?.data || response?.payload || response || {}
}

function getKqcResult(data) {
  const result = data?.result && typeof data.result === 'object' ? data.result : data
  return result && typeof result === 'object' ? result : {}
}

function formatKqcError(message) {
  const text = String(message || '').trim()
  if (!text) return ''
  if (text === "'Response'" || text === 'Response' || text.includes('KeyError') || text.includes('响应结果列')) {
    return KQC_MISSING_RESPONSE_TIP
  }
  return text
}

function acceptKqcTask(payload) {
  const data = getKqcPayload(payload)
  kqcDialog.taskId = data.taskId || data.task_id || ''
  kqcDialog.status = data.status || kqcDialog.status || 'RUNNING'
  kqcDialog.error = formatKqcError(data.errorMessage || data.error_message || '')
  const result = getKqcResult(data.result)
  if (Object.keys(result).length) {
    kqcDialog.result = result
  }
  kqcRunning.value = !!(kqcDialog.taskId && !KQC_DONE_STATUSES.includes(kqcDialog.status))
  if (kqcDialog.taskId && !KQC_DONE_STATUSES.includes(kqcDialog.status)) {
    startKqcPolling(kqcDialog.taskId)
  }
  scheduleKqcRankingRender()
}

async function submitProcessAnomaly() {
  if (detectSubmitting.value || detectTaskActive.value) {
    startDetectPolling(detectDialog.taskId)
    return
  }
  const node = detectDialog.node
  if (!node?.id) {
    ElMessage.warning('检测对象不能为空')
    return
  }
  if (detectDialog.form.enabled && !detectTrainSampleIds.value.length) {
    ElMessage.warning('请选择工序异常检测数据文件')
    return
  }
  if (!detectDialog.form.enabled && !detectTrainSampleIds.value.length) {
    ElMessage.warning('请至少选择一个训练和测试数据文件')
    return
  }
  if (!detectDialog.form.enabled && !detectUploadSampleIds.value.length) {
    ElMessage.warning('请上传一个检测数据文件')
    return
  }

  clearDetectPolling()
  detectSubmitting.value = true
  detectRunning.value = true
  detectDialog.loading = true
  detectDialog.status = 'PENDING'
  detectDialog.error = ''
  detectDialog.result = {}
  try {
    const partId = rawNodeId(node.id)
    const hierarchyIds = sampleObjectQuery(node) || {}
    const station = cleanLocalPath(detectDialog.form.station)
    detectDialog.form.station = station
    const processId = station || `process:${partId}`
    const payload = {
      partId,
      partCode: node.part_number || node.code || partId,
      partName: node.name || node.part_name || partId,
      ...hierarchyIds,
      pathText: currentPathText.value,
      processId,
      processName: station || '工序异常检测',
      processOrder: 1,
      dataUsage: detectDialog.form.enabled ? 'BOSCH_PROCESS_ANOMALY' : 'PROCESS_ANOMALY',
      featureCol: detectUploadRows.value[0]?.featureCol || 1
    }
    if (detectDialog.form.enabled) {
      Object.assign(payload, {
        trainSampleIds: detectTrainSampleIds.value,
        sampleIds: detectTrainSampleIds.value,
        station,
        featureColName: detectDialog.form.featureColName,
        maxRows: detectDialog.form.maxRows,
        epochs: detectDialog.form.epochs,
        selectionMode: detectDialog.form.selectionMode,
        alphaSample: detectDialog.form.alphaSample,
        ewmaLambda: detectDialog.form.ewmaLambda,
        minFailedObserved: detectDialog.form.minFailedObserved,
        minObserved: detectDialog.form.minObserved,
        seed: detectDialog.form.seed,
        device: detectDialog.form.device
      })
    } else {
      Object.assign(payload, {
        trainSampleIds: detectTrainSampleIds.value,
        detectSampleIds: detectUploadSampleIds.value,
        sampleIds: detectSelectedSampleIds.value
      })
    }
    const res = await startWarningDetectTask(payload)
    acceptDetectTask(getKqcPayload(res))
    ElMessage.success('异常检测任务已提交')
  } catch (e) {
    detectRunning.value = false
    detectDialog.status = 'FAILED'
    detectDialog.error = getErrorMessage(e, '异常检测任务启动失败')
    ElMessage.error(detectDialog.error)
  } finally {
    detectSubmitting.value = false
    detectDialog.loading = false
  }
}

function acceptDetectTask(payload, shouldPoll = true) {
  const data = getKqcPayload(payload)
  detectDialog.taskId = data.taskId || data.task_id || detectDialog.taskId || ''
  detectDialog.status = data.status || detectDialog.status || 'RUNNING'
  detectDialog.error = data.errorMessage || data.error_message || data.error || ''
  const result = data.result && typeof data.result === 'object' ? data.result : data
  if (result && typeof result === 'object' && Object.keys(result).length) {
    detectDialog.result = result
  }
  detectRunning.value = !!(detectDialog.taskId && !DETECT_DONE_STATUSES.includes(detectDialog.status))
  if (shouldPoll && detectDialog.taskId && !DETECT_DONE_STATUSES.includes(detectDialog.status)) {
    startDetectPolling(detectDialog.taskId)
  }
  scheduleAnomalyChartRender()
}

async function lookupDetectTaskResult() {
  const id = cleanLocalPath(detectDialog.lookupTaskId)
  if (!id) {
    ElMessage.warning('请输入任务ID')
    return
  }
  clearDetectPolling()
  detectDialog.lookupTaskId = id
  detectDialog.lookupLoading = true
  try {
    const res = await getWarningDetectTask(id)
    acceptDetectTask(getKqcPayload(res))
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '查询任务结果失败'))
  } finally {
    detectDialog.lookupLoading = false
  }
}

function startDetectPolling(taskId) {
  if (!taskId || DETECT_DONE_STATUSES.includes(detectDialog.status)) return
  clearDetectPolling()
  const token = detectPollToken
  detectPollCount = 0
  const poll = () => queryDetectStatus(taskId, token)
  detectPollTimer = setInterval(() => {
    detectPollCount += 1
    if (detectPollCount > 120 || DETECT_DONE_STATUSES.includes(detectDialog.status)) {
      clearDetectPolling()
      return
    }
    poll()
  }, 3000)
  poll()
}

async function queryDetectStatus(taskId, token = detectPollToken) {
  if (token !== detectPollToken || detectDialog.taskId !== taskId) return
  try {
    const res = await getWarningDetectTask(taskId)
    if (token !== detectPollToken || detectDialog.taskId !== taskId) return
    acceptDetectTask(getKqcPayload(res), false)
    if (DETECT_DONE_STATUSES.includes(detectDialog.status)) {
      clearDetectPolling()
      detectSubmitting.value = false
      detectRunning.value = false
      detectDialog.loading = false
      if (detectDialog.status === 'SUCCESS') ElMessage.success('工序异常检测完成')
      if (detectDialog.status === 'FAILED' && detectDialog.error) ElMessage.error(detectDialog.error)
    }
  } catch (e) {
    if (token !== detectPollToken || detectDialog.taskId !== taskId) return
    clearDetectPolling()
    detectSubmitting.value = false
    detectRunning.value = false
    detectDialog.loading = false
    detectDialog.status = 'FAILED'
    detectDialog.error = getErrorMessage(e, '工序异常检测状态查询失败')
    ElMessage.error(detectDialog.error)
  }
}

async function cancelDetectTask() {
  if (!detectDialog.taskId || detectDialog.canceling) return
  detectDialog.canceling = true
  try {
    const res = await cancelWarningDetectTask(detectDialog.taskId)
    const data = getKqcPayload(res)
    detectDialog.status = data.status || 'CANCELED'
    detectDialog.error = data.errorMessage || data.error_message || ''
    detectDialog.result = data.result || data || detectDialog.result
    clearDetectPolling()
    detectSubmitting.value = false
    detectRunning.value = false
    detectDialog.loading = false
    ElMessage.success('任务已取消')
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '任务取消失败'))
  } finally {
    detectDialog.canceling = false
  }
}

function clearDetectPolling() {
  detectPollToken += 1
  if (detectPollTimer) {
    clearInterval(detectPollTimer)
    detectPollTimer = null
  }
}

function closeDetectDialog() {
  clearDetectPolling()
  detectSubmitting.value = false
  detectRunning.value = false
  detectDialog.loading = false
  detectDialog.visible = false
  clearDetectUploads()
}

async function loadDetectSamples() {
  detectFileQuery.loading = true
  try {
    const rows = await loadObjectSamples(detectDialog.node, DETECT_FILE_DATA_USAGES, detectFileQuery.keyword)
    const start = (detectFileQuery.pageNum - 1) * detectFileQuery.pageSize
    detectFileQuery.samples = rows.slice(start, start + detectFileQuery.pageSize)
    detectFileQuery.total = rows.length
  } catch (e) {
    detectFileQuery.samples = []
    detectFileQuery.total = 0
    ElMessage.error(getErrorMessage(e, '数据文件加载失败'))
  } finally {
    detectFileQuery.loading = false
  }
}

async function loadObjectSamples(node, dataUsages, keyword) {
  const objectQuery = sampleObjectQuery(node)
  if (objectQuery === null) return []
  const results = await Promise.all(dataUsages.map(dataUsage => listAllDataFiles({
    dataUsage,
    keyword,
    ...objectQuery,
    pageNum: 1,
    pageSize: 5000
  })))
  const uniqueRows = new Map()
  results.flatMap(res => Array.isArray(res?.rows) ? res.rows : []).forEach(row => {
    if (row && row.id !== undefined && row.id !== null) {
      uniqueRows.set(row.id, row)
    }
  })
  return Array.from(uniqueRows.values())
}

function sampleObjectQuery(node) {
  if (!node?.id) return null
  const path = findNodePath(treeData.value, node.id)
  if (!path.length) return null
  const query = { aircraftId: '', subsystemId: '', equipmentId: '', componentId: '', partId: '' }
  path.forEach(item => {
    const rawId = rawNodeId(item.id)
    const type = numNodeType(item)
    if (type === 'aircraft') query.aircraftId = rawId
    if (type === 'subsystem') query.subsystemId = rawId
    if (type === 'equipment') query.equipmentId = rawId
    if (type === 'component') query.componentId = rawId
    if (type === 'part_template') query.partId = rawId
  })
  return query
}

async function loadKqcSamples() {
  kqcFileQuery.loading = true
  try {
    const rows = await loadObjectSamples(kqcDialog.node, KQC_FILE_DATA_USAGES, kqcFileQuery.keyword)
    const start = (kqcFileQuery.pageNum - 1) * kqcFileQuery.pageSize
    kqcFileQuery.samples = rows.slice(start, start + kqcFileQuery.pageSize)
    kqcFileQuery.total = rows.length
  } catch (e) {
    kqcFileQuery.samples = []
    kqcFileQuery.total = 0
    ElMessage.error(getErrorMessage(e, '数据文件加载失败'))
  } finally {
    kqcFileQuery.loading = false
  }
}

function onDetectSampleSelection(rows) {
  detectSelectedSamples.value = Array.isArray(rows) ? rows : []
}

function onKqcSampleSelection(rows) {
  kqcSelectedSamples.value = Array.isArray(rows) ? rows : []
}

function clearDetectSamples() {
  detectSelectedSamples.value = []
  detectSampleTableRef.value?.clearSelection?.()
}

function clearKqcSamples() {
  kqcSelectedSamples.value = []
  kqcSampleTableRef.value?.clearSelection?.()
}

async function onDetectFileChange(file) {
  const raw = file?.raw
  if (!raw) return
  detectUpload.loading = true
  try {
    const columnCount = await detectFileColumnCount(raw)
    const payload = new FormData()
    payload.append('file', raw)
    payload.append('dataUsage', 'PROCESS_ANOMALY')
    payload.append('conditionLabel', '工序异常检测')
    const res = await uploadWarningDetectFile(payload)
    const data = getKqcPayload(res)
    const row = {
      id: data.id,
      fileName: data.fileName || data.file_name || raw.name,
      fileSize: data.fileSize || data.file_size || raw.size,
      uploadStatus: data.uploadStatus || data.upload_status || 'SUCCESS',
      dataUsage: data.dataUsage || 'PROCESS_ANOMALY',
      columnCount,
      featureCol: autoDetectFeatureCol(columnCount) || 1
    }
    if (!row.id) throw new Error('上传成功但未返回文件ID')
    detectUploadRows.value = [row]
    ElMessage.success('检测数据文件上传成功')
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '检测数据文件上传失败'))
  } finally {
    detectUpload.loading = false
  }
}

function clearDetectUploads() {
  detectUploadRows.value = []
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

function autoDetectFeatureCol(columnCount) {
  return Number(columnCount || 0) > 0 ? 1 : null
}

function translateDetectText(value) {
  const text = String(value || '').trim()
  const map = {
    'Review alarm points with the largest EWMA excess and trace the selected station feature.': '建议复核EWMA超限最大的告警点，并追溯所选工序特征。',
    'mock suggestion': '模拟建议'
  }
  return map[text] || value
}

function scheduleAnomalyChartRender() {
  nextTick(() => renderAnomalyChart())
}

function renderAnomalyChart() {
  if (!detectDialog.visible || !anomalyChartRef.value || !detectCurveRows.value.length) return
  if (anomalyChart && anomalyChart.getDom() !== anomalyChartRef.value) {
    anomalyChart.dispose()
    anomalyChart = null
  }
  if (!anomalyChart) {
    anomalyChart = echarts.init(anomalyChartRef.value)
  }
  const rows = detectCurveRows.value
  const threshold = detectCurveThreshold.value
  anomalyChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 48, right: 24, top: 28, bottom: 36 },
    xAxis: { type: 'category', data: rows.map(item => item.x) },
    yAxis: { type: 'value' },
    series: [{
      type: 'line',
      name: '异常分数',
      data: rows.map(item => item.value),
      smooth: true,
      showSymbol: false,
      markLine: threshold === null ? undefined : {
        symbol: 'none',
        data: [{ yAxis: threshold, name: '阈值' }],
        lineStyle: { color: '#f56c6c', type: 'dashed' },
        label: { formatter: '阈值' }
      }
    }]
  }, true)
  anomalyChart.resize()
}

function resizeAnomalyChart() {
  anomalyChart?.resize()
}

function formatChartValue(value) {
  const num = Number(value)
  if (!Number.isFinite(num)) return '--'
  return Math.abs(num) >= 100 ? num.toFixed(1) : num.toFixed(4)
}

async function submitKeyProcess() {
  if (keySubmitting.value || keyTaskActive.value) {
    startKeyPolling(keyDialog.taskId)
    return
  }
  const node = keyDialog.node
  if (!node?.id) {
    ElMessage.warning('识别对象不能为空')
    return
  }
  if (keyDialog.form.enabled && !keyDialog.form.kqcTaskId && !keyDialog.form.kqcOutputDir) {
    ElMessage.warning('请填写 KQC任务ID或KQC输出目录')
    return
  }
  if (!keyDialog.form.enabled && !keySelectedSampleIds.value.length) {
    ElMessage.warning('请至少选择一个 CSV/TXT 文件')
    return
  }

  clearKeyPolling()
  keySubmitting.value = true
  keyRunning.value = true
  keyDialog.loading = true
  keyDialog.status = 'PENDING'
  keyDialog.error = ''
  keyDialog.result = {}
  try {
    const targetType = numNodeType(node)
    const targetId = String(node.id).split(':').slice(1).join(':')
    const params = {
      dataSelectionMode: keyDialog.form.enabled ? 'BOSCH' : 'SELECTED_FILES',
      dataUsage: 'KEY_PROCESS',
      sampleIds: keyDialog.form.enabled ? [] : keySelectedSampleIds.value,
      selected_object: {
        id: node.id,
        level: targetType,
        objectLevel: targetType,
        name: node.name || node.id,
        path: currentPathText.value
      },
      targetNodeId: node.id,
      targetType,
      targetLevel: targetType,
      targetId,
      topN: keyDialog.form.topN,
      threshold: 0.7
    }
    if (keyDialog.form.enabled) {
      const kqcTaskId = cleanLocalPath(keyDialog.form.kqcTaskId)
      const kqcOutputDir = cleanLocalPath(keyDialog.form.kqcOutputDir)
      keyDialog.form.kqcTaskId = kqcTaskId
      keyDialog.form.kqcOutputDir = kqcOutputDir
      Object.assign(params, {
        kqcTaskId,
        kqcOutputDir,
        keepFreq: keyDialog.form.keepFreq,
        weightThresh: keyDialog.form.weightThresh
      })
    }
    const res = await startKeyProcessTask({
      import_record_id: node.id,
      dataSelectionMode: keyDialog.form.enabled ? 'BOSCH' : 'SELECTED_FILES',
      sampleIds: keyDialog.form.enabled ? [] : keySelectedSampleIds.value,
      params
    })
    acceptKeyTask(getKqcPayload(res))
    ElMessage.success('关键工序识别任务已提交')
  } catch (e) {
    keyRunning.value = false
    keyDialog.status = 'FAILED'
    keyDialog.error = getErrorMessage(e, '关键工序识别任务启动失败')
    ElMessage.error(keyDialog.error)
  } finally {
    keySubmitting.value = false
    keyDialog.loading = false
  }
}

function acceptKeyTask(payload, shouldPoll = true) {
  const data = getKqcPayload(payload)
  keyDialog.taskId = data.taskId || data.task_id || keyDialog.taskId || ''
  keyDialog.status = data.status || keyDialog.status || 'RUNNING'
  keyDialog.error = data.errorMessage || data.error_message || data.error || ''
  const result = data.result && typeof data.result === 'object' ? data.result : data
  if (result && typeof result === 'object' && Object.keys(result).length) {
    keyDialog.result = result
  }
  keyRunning.value = !!(keyDialog.taskId && !KEY_DONE_STATUSES.includes(keyDialog.status))
  if (shouldPoll && keyDialog.taskId && !KEY_DONE_STATUSES.includes(keyDialog.status)) {
    startKeyPolling(keyDialog.taskId)
  }
}

async function lookupKeyTaskResult() {
  const id = cleanLocalPath(keyDialog.lookupTaskId)
  if (!id) {
    ElMessage.warning('请输入任务ID')
    return
  }
  clearKeyPolling()
  keyDialog.lookupTaskId = id
  keyDialog.lookupLoading = true
  try {
    const res = await getKeyProcessTask(id)
    acceptKeyTask(getKqcPayload(res))
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '查询任务结果失败'))
  } finally {
    keyDialog.lookupLoading = false
  }
}

function startKeyPolling(taskId) {
  if (!taskId || KEY_DONE_STATUSES.includes(keyDialog.status)) return
  clearKeyPolling()
  const token = keyPollToken
  keyPollCount = 0
  const poll = () => queryKeyStatus(taskId, token)
  keyPollTimer = setInterval(() => {
    keyPollCount += 1
    if (keyPollCount > 120 || KEY_DONE_STATUSES.includes(keyDialog.status)) {
      clearKeyPolling()
      return
    }
    poll()
  }, 3000)
  poll()
}

async function queryKeyStatus(taskId, token = keyPollToken) {
  if (token !== keyPollToken || keyDialog.taskId !== taskId) return
  try {
    const res = await getKeyProcessTask(taskId)
    if (token !== keyPollToken || keyDialog.taskId !== taskId) return
    acceptKeyTask(getKqcPayload(res), false)
    if (KEY_DONE_STATUSES.includes(keyDialog.status)) {
      clearKeyPolling()
      keySubmitting.value = false
      keyRunning.value = false
      keyDialog.loading = false
      if (keyDialog.status === 'SUCCESS') ElMessage.success('关键工序识别完成')
      if (keyDialog.status === 'FAILED' && keyDialog.error) ElMessage.error(keyDialog.error)
    }
  } catch (e) {
    if (token !== keyPollToken || keyDialog.taskId !== taskId) return
    clearKeyPolling()
    keySubmitting.value = false
    keyRunning.value = false
    keyDialog.loading = false
    keyDialog.status = 'FAILED'
    keyDialog.error = getErrorMessage(e, '关键工序识别状态查询失败')
    ElMessage.error(keyDialog.error)
  }
}

async function cancelKeyProcess() {
  if (!keyDialog.taskId || keyDialog.canceling) return
  keyDialog.canceling = true
  try {
    const res = await cancelKeyProcessTask(keyDialog.taskId)
    const data = getKqcPayload(res)
    keyDialog.status = data.status || 'CANCELED'
    keyDialog.error = data.errorMessage || data.error_message || ''
    keyDialog.result = data.result || data || keyDialog.result
    clearKeyPolling()
    keySubmitting.value = false
    keyRunning.value = false
    keyDialog.loading = false
    ElMessage.success('任务已取消')
  } catch (e) {
    ElMessage.error(getErrorMessage(e, '任务取消失败'))
  } finally {
    keyDialog.canceling = false
  }
}

function clearKeyPolling() {
  keyPollToken += 1
  if (keyPollTimer) {
    clearInterval(keyPollTimer)
    keyPollTimer = null
  }
}

function closeKeyDialog() {
  clearKeyPolling()
  keySubmitting.value = false
  keyRunning.value = false
  keyDialog.loading = false
  keyDialog.visible = false
}

async function loadKeySamples() {
  keyFileQuery.loading = true
  try {
    const rows = await loadObjectSamples(keyDialog.node, KEY_FILE_DATA_USAGES, keyFileQuery.keyword)
    const start = (keyFileQuery.pageNum - 1) * keyFileQuery.pageSize
    keyFileQuery.samples = rows.slice(start, start + keyFileQuery.pageSize)
    keyFileQuery.total = rows.length
  } catch (e) {
    keyFileQuery.samples = []
    keyFileQuery.total = 0
    ElMessage.error(getErrorMessage(e, '数据文件加载失败'))
  } finally {
    keyFileQuery.loading = false
  }
}

function onKeySampleSelection(rows) {
  keySelectedSamples.value = Array.isArray(rows) ? rows : []
}

function clearKeySamples() {
  keySelectedSamples.value = []
  keySampleTableRef.value?.clearSelection?.()
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
  return pickValue(keyDialog.result, keys[type] || [type])
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

function getErrorMessage(error, fallback) {
  return error?.response?.data?.msg || error?.response?.data?.message || error?.message || fallback
}

function scheduleKqcGraphRender() {
  nextTick(() => renderKqcGraph())
}

function scheduleKqcRankingRender() {
  nextTick(() => renderKqcRankingChart())
}

function formatKqcMetric(value, precision) {
  const number = Number(value)
  return Number.isFinite(number) ? number.toFixed(precision) : '--'
}

function renderKqcRankingChart() {
  if (!kqcDialog.visible || !kqcRankingChartRef.value) return
  if (kqcRankingChart && kqcRankingChart.getDom() !== kqcRankingChartRef.value) {
    kqcRankingChart.dispose()
    kqcRankingChart = null
  }
  if (!kqcRankingChart) {
    kqcRankingChart = echarts.init(kqcRankingChartRef.value)
  }
  const rows = kqcRankedRows.value.slice(0, 15).reverse()
  if (!rows.length) {
    kqcRankingChart.setOption({
      title: { text: '暂无排名数据', left: 'center', top: 'center', textStyle: { color: '#8a97a8', fontSize: 14, fontWeight: 400 } },
      xAxis: { show: false },
      yAxis: { show: false },
      series: []
    }, true)
    return
  }
  kqcRankingChart.setOption({
    grid: { left: 110, right: 28, top: 24, bottom: 42 },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter(params) {
        const point = params?.[0]
        const row = rows[point?.dataIndex]
        if (!row) return ''
        return `${row.source_variable}<br/>工序：${row.station || '--'}<br/>影响权重：${formatKqcMetric(row.weight, 6)}<br/>稳定频率：${formatKqcMetric(row.frequency, 3)}`
      }
    },
    xAxis: {
      type: 'value',
      name: '影响权重',
      nameLocation: 'middle',
      nameGap: 28,
      axisLabel: { formatter: value => Number(value).toExponential(1) },
      splitLine: { lineStyle: { color: '#edf1f5' } }
    },
    yAxis: {
      type: 'category',
      data: rows.map(row => row.source_variable),
      axisLabel: { width: 96, overflow: 'truncate' }
    },
    series: [{
      type: 'bar',
      barMaxWidth: 22,
      data: rows.map(row => ({
        value: row.weight,
        itemStyle: { color: row.weight >= 0 ? '#e56a67' : '#4c8fdc' }
      })),
      label: {
        show: true,
        position: 'right',
        formatter: params => Number(params.value).toExponential(2),
        color: '#526273',
        fontSize: 10
      }
    }]
  }, true)
  kqcRankingChart.resize()
}

function openKqcGraphDialog() {
  if (!kqcGraphData.value.nodes?.length) {
    ElMessage.warning('暂无关系图数据')
    return
  }
  kqcGraphDialog.visible = true
}

function closeKqcDialog() {
  clearKqcPolling()
  kqcGraphDialog.visible = false
}

function renderKqcGraph() {
  if (!kqcGraphDialog.visible || !kqcGraphRef.value) return
  if (kqcGraphChart && kqcGraphChart.getDom() !== kqcGraphRef.value) {
    kqcGraphChart.dispose()
    kqcGraphChart = null
  }
  if (!kqcGraphChart) {
    kqcGraphChart = echarts.init(kqcGraphRef.value)
  }
  const data = kqcGraphData.value
  const nodes = Array.isArray(data.nodes) ? data.nodes : []
  const links = Array.isArray(data.links) ? data.links : []
  if (!nodes.length || !links.length) {
    kqcGraphChart.setOption({
      title: { text: '暂无关系图数据', left: 'center', top: 'center', textStyle: { color: '#8a97a8', fontSize: 14, fontWeight: 400 } },
      xAxis: { show: false },
      yAxis: { show: false },
      series: []
    }, true)
    return
  }
  kqcGraphChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter(params) {
        if (params.dataType === 'edge') {
          const d = params.data || {}
          const frequency = d.frequency === null || d.frequency === undefined ? '--' : Number(d.frequency).toFixed(3)
          const weight = d.weight === null || d.weight === undefined ? '--' : Number(d.weight).toFixed(6)
          return `${d.source} -> ${d.target}<br/>权重：${weight}<br/>稳定频率：${frequency}`
        }
        const d = params.data || {}
        return `${d.name || d.id}<br/>工序：${d.station || '--'}`
      }
    },
    legend: [{ data: (data.categories || []).map(item => item.name), bottom: 0 }],
    series: [{
      type: 'graph',
      layout: 'force',
      roam: true,
      draggable: true,
      categories: data.categories || [],
      data: nodes,
      links,
      edgeSymbol: ['none', 'arrow'],
      edgeSymbolSize: 8,
      label: { show: true, position: 'right', fontSize: 11 },
      force: { repulsion: 420, edgeLength: 130 },
      lineStyle: { curveness: 0.12, opacity: 0.8 },
      emphasis: { focus: 'adjacency' }
    }]
  }, true)
  kqcGraphChart.resize()
}

function resizeKqcGraph() {
  kqcRankingChart?.resize()
  kqcGraphChart?.resize()
}

async function lookupKqcTaskResult() {
  const id = cleanLocalPath(kqcDialog.lookupTaskId)
  if (!id) {
    ElMessage.warning('请输入任务ID')
    return
  }
  clearKqcPolling()
  kqcDialog.lookupTaskId = id
  kqcDialog.lookupLoading = true
  try {
    const json = await request({
      url: `/feedback/warning/kqc-mining/tasks/${encodeURIComponent(id)}/status`,
      method: 'get'
    })
    acceptKqcTask(getKqcPayload(json))
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || e?.message || '查询挖掘结果失败')
  } finally {
    kqcDialog.lookupLoading = false
  }
}

async function loadKqcHistory() {
  kqcHistory.loading = true
  try {
    const json = await request({
      url: '/feedback/warning/kqc-mining/results',
      method: 'get',
      params: kqcHistory.query
    })
    const data = getKqcPayload(json)
    kqcHistory.rows = Array.isArray(data.rows) ? data.rows : []
    kqcHistory.total = Number(data.total || 0)
  } finally {
    kqcHistory.loading = false
  }
}

function searchKqcHistory() {
  kqcHistory.query.page_num = 1
  loadKqcHistory()
}

function resetKqcHistory() {
  kqcHistory.query.keyword = ''
  kqcHistory.query.status = ''
  kqcHistory.query.page_num = 1
  loadKqcHistory()
}

function viewKqcHistory(row) {
  if (!row?.taskId && !row?.task_id) return
  clearKqcPolling()
  kqcSubmitting.value = false
  kqcRunning.value = false
  kqcDialog.loading = false
  kqcDialog.canceling = false
  kqcDialog.node = null
  kqcDialog.visible = true
  kqcDialog.lookupTaskId = row.taskId || row.task_id
  kqcDialog.taskId = row.taskId || row.task_id
  kqcDialog.status = row.status || ''
  kqcDialog.error = formatKqcError(row.errorMessage || row.error_message || '')
  kqcDialog.result = row.result && typeof row.result === 'object' ? row.result : {}
  kqcGraphDialog.visible = false
  scheduleKqcRankingRender()
}

async function deleteKqcHistory(row) {
  const id = row?.taskId || row?.task_id
  if (!id) return
  try {
    await ElMessageBox.confirm(`确认删除关键质量特性挖掘结果 ${id} 吗？本地算法结果和数据库记录都会删除。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch (_) {
    return
  }
  try {
    await request({
      url: `/feedback/warning/results/${encodeURIComponent(id)}`,
      method: 'delete'
    })
    ElMessage.success('删除成功')
    await loadKqcHistory()
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || e?.message || '删除失败')
  }
}

function clearKqcPolling() {
  kqcPollToken += 1
  if (kqcPollTimer) {
    clearInterval(kqcPollTimer)
    kqcPollTimer = null
  }
}

function startKqcPolling(taskId) {
  if (!taskId || KQC_DONE_STATUSES.includes(kqcDialog.status)) return
  clearKqcPolling()
  const token = kqcPollToken
  kqcPollCount = 0
  const poll = () => queryKqcStatus(taskId, token)
  kqcPollTimer = setInterval(() => {
    kqcPollCount += 1
    if (kqcPollCount > 120 || KQC_DONE_STATUSES.includes(kqcDialog.status)) {
      clearKqcPolling()
      return
    }
    poll()
  }, 3000)
  poll()
}

async function queryKqcStatus(taskId, token = kqcPollToken) {
  if (token !== kqcPollToken || kqcDialog.taskId !== taskId) return
  try {
    const json = await request({
      url: `/feedback/warning/kqc-mining/tasks/${taskId}/status`,
      method: 'get'
    })
    if (token !== kqcPollToken || kqcDialog.taskId !== taskId) return
    const data = getKqcPayload(json)
    kqcDialog.status = data.status || kqcDialog.status
    kqcDialog.error = formatKqcError(data.errorMessage || data.error_message || '')
    const result = getKqcResult(data.result)
    if (Object.keys(result).length) {
      kqcDialog.result = result
      scheduleKqcRankingRender()
    }
    if (KQC_DONE_STATUSES.includes(kqcDialog.status)) {
      clearKqcPolling()
      kqcSubmitting.value = false
      kqcRunning.value = false
      kqcDialog.loading = false
      if (kqcDialog.status === 'SUCCESS') ElMessage.success('关键质量特性挖掘完成')
      if (kqcDialog.status === 'FAILED' && kqcDialog.error) ElMessage.error(kqcDialog.error)
    }
  } catch (e) {
    if (token !== kqcPollToken || kqcDialog.taskId !== taskId) return
    clearKqcPolling()
    kqcSubmitting.value = false
    kqcRunning.value = false
    kqcDialog.loading = false
    kqcDialog.status = 'FAILED'
    kqcDialog.error = formatKqcError(e?.message || '关键质量特性挖掘状态查询失败')
    ElMessage.error(kqcDialog.error)
  }
}

async function cancelKqcMining() {
  if (!kqcDialog.taskId || kqcDialog.canceling) return
  kqcDialog.canceling = true
  try {
    const json = await request({
      url: `/feedback/warning/kqc-mining/tasks/${kqcDialog.taskId}/cancel`,
      method: 'post'
    })
    const data = getKqcPayload(json)
    if (data.success === false) {
      throw new Error(data.message || data.errorMessage || '取消任务失败')
    }
    kqcDialog.status = data.status || 'CANCELED'
    kqcDialog.error = formatKqcError(data.errorMessage || data.error_message || '')
    clearKqcPolling()
    kqcSubmitting.value = false
    kqcRunning.value = false
    kqcDialog.loading = false
    ElMessage.success('任务已取消')
  } catch (e) {
    ElMessage.error(e?.message || '取消任务失败')
  } finally {
    kqcDialog.canceling = false
  }
}

async function handleDeleteModule() {
  const node = context_menu.node
  closeContextMenu()
  if (!node || !node.id) {
    ElMessage.warning('请先选择模块')
    return
  }

  if (!window.confirm('确认删除模块【' + (node.name || node.id) + '】吗？')) {
    return
  }

  try {
    const keepNodeId = current_node.value?.id || ''
    const parentNodeId = breadcrumbList.value.length > 1 ? breadcrumbList.value[breadcrumbList.value.length - 2].id : ''
    const fallbackParentId = keepNodeId === node.id ? parentNodeId : ''
    await deleteModule(node.id)
    ElMessage.success('删除成功')
    await reloadTreeView(keepNodeId, fallbackParentId)
  } catch {
  }
}

async function openPartQualityDialog(row) {
  if (!row || !row.part_instance_id) {
    ElMessage.warning('未找到零件实例编号')
    return
  }
  partQualityDialog.visible = true
  partQualityDialog.loading = true
  partQualityDialog.part_name = row.part_name || ''
  partQualityDialog.part_code = row.part_code || ''
  partQualityDialog.tables = []

  try {
    const res = await fetchPartQualityTables(row.part_instance_id)
    partQualityDialog.tables = normalizePartQualityTables(res)
  } catch {
    partQualityDialog.tables = []
    ElMessage.error('获取零件质量信息失败')
  } finally {
    partQualityDialog.loading = false
  }

}

function normalizePartQualityTables(response) {
  const data = response?.data && typeof response.data === 'object' ? response.data : response
  const tables = Array.isArray(data?.tables) ? data.tables : []
  return tables.map((table, index) => {
    const rows = Array.isArray(table?.rows) ? table.rows : []
    const rawColumns = Array.isArray(table?.columns) ? table.columns : []
    const columns = rawColumns.length ? rawColumns : buildPartQualityColumns(rows)
    return {
      table_name: table?.table_name || table?.tableName || `quality_${index}`,
      table_label: table?.table_label || table?.tableLabel || table?.label || `质量信息${index + 1}`,
      columns,
      rows
    }
  })
}

function buildPartQualityColumns(rows) {
  const keys = []
  rows.forEach(row => {
    if (!row || typeof row !== 'object') return
    Object.keys(row).forEach(key => {
      if (!keys.includes(key)) keys.push(key)
    })
  })
  return keys.map(key => ({
    prop: key,
    label: partQualityColumnLabels[key] || key
  }))
}

function openCreatePartInstanceDialog() {
  const template_base = Array.isArray(part_list.value) && part_list.value.length > 0 ? part_list.value[0] : {}
  addPartInstanceDialog.visible = true
  addPartInstanceDialog.loading = false
  addPartInstanceDialog.form.part_code = template_base.part_code || ''
  addPartInstanceDialog.form.part_name = template_base.part_name || (current_node.value?.name || '')
  addPartInstanceDialog.form.material = template_base.material || ''
  addPartInstanceDialog.form.spec_model = template_base.spec_model || ''
  addPartInstanceDialog.form.serial_number = ''
  addPartInstanceDialog.form.batch_number = ''
  addPartInstanceDialog.form.manufacturer = ''
  addPartInstanceDialog.form.production_date = ''
  addPartInstanceDialog.form.status = ''
  addPartInstanceDialog.form.quality_level = ''
  addPartInstanceDialog.form.key_degree = ''
}

async function submitCreatePartInstance() {
  if (!addPartInstanceDialog.form.serial_number || !addPartInstanceDialog.form.serial_number.trim()) {
    ElMessage.warning('请输入序列号')

    return
  }
  if (!addPartInstanceDialog.form.batch_number || !addPartInstanceDialog.form.batch_number.trim()) {
    ElMessage.warning('请输入批次号')
    return
  }
  addPartInstanceDialog.loading = true
  try {
    const payload = {
      module_id: partQuery.module_id,
      part_code: addPartInstanceDialog.form.part_code,
      part_name: addPartInstanceDialog.form.part_name,
      material: addPartInstanceDialog.form.material,
      spec_model: addPartInstanceDialog.form.spec_model,
      serial_number: addPartInstanceDialog.form.serial_number,
      batch_number: addPartInstanceDialog.form.batch_number,
      manufacturer: addPartInstanceDialog.form.manufacturer,
      production_date: addPartInstanceDialog.form.production_date,
      status: addPartInstanceDialog.form.status,
      quality_level: addPartInstanceDialog.form.quality_level,
      key_degree: addPartInstanceDialog.form.key_degree
    }
    const res = await createPartInstance(payload)
    const newPartInstanceId = res?.data?.part_instance_id || ''
    const newRow = {
      part_id: newPartInstanceId,
      part_instance_id: newPartInstanceId,
      part_template_id: String(partQuery.module_id || '').replace(/^part_template:/, ''),
      part_code: payload.part_code || '',
      part_name: payload.part_name || '',
      material: payload.material || '',
      spec_model: payload.spec_model || '',
      batch_no: payload.batch_number || '',
      batch_number: payload.batch_number || '',
      serial_number: payload.serial_number || '',
      manufacturer: payload.manufacturer || '',
      production_date: payload.production_date || '',
      status: payload.status || '',
      current_status: payload.status || '',
      quality_level: payload.quality_level || '',
      key_degree: payload.key_degree || ''
    }
    if (matchesPartFilters(newRow)) {
      part_list.value = [newRow, ...(Array.isArray(part_list.value) ? part_list.value : [])]
      refreshPartFilterOptions(part_list.value)
    }
    ElMessage.success('新增零件实例成功')
    addPartInstanceDialog.visible = false
  } catch {
  } finally {
    addPartInstanceDialog.loading = false
  }
}

function openEditPartInstanceDialog(row) {
  if (!row || !row.part_instance_id) {
    ElMessage.warning('未找到零件实例编号')
    return
  }
  editPartInstanceDialog.visible = true
  editPartInstanceDialog.loading = false
  editPartInstanceDialog.form.part_instance_id = row.part_instance_id || ''
  editPartInstanceDialog.form.part_code = row.part_code || ''
  editPartInstanceDialog.form.part_name = row.part_name || ''
  editPartInstanceDialog.form.material = row.material || ''
  editPartInstanceDialog.form.spec_model = row.spec_model || ''
  editPartInstanceDialog.form.serial_number = row.serial_number || ''
  editPartInstanceDialog.form.batch_number = row.batch_number || row.batch_no || ''
  editPartInstanceDialog.form.manufacturer = row.manufacturer || ''
  editPartInstanceDialog.form.production_date = row.production_date || ''
  editPartInstanceDialog.form.status = row.status || ''
  editPartInstanceDialog.form.quality_level = row.quality_level || ''
  editPartInstanceDialog.form.key_degree = row.key_degree || ''
}

async function submitUpdatePartInstance() {
  if (!editPartInstanceDialog.form.part_instance_id) {
    ElMessage.warning('未找到零件实例编号')
    return
  }
  if (!editPartInstanceDialog.form.serial_number || !editPartInstanceDialog.form.serial_number.trim()) {
    ElMessage.warning('请输入序列号')
    return
  }
  if (!editPartInstanceDialog.form.batch_number || !editPartInstanceDialog.form.batch_number.trim()) {
    ElMessage.warning('请输入批次号')
    return
  }

  editPartInstanceDialog.loading = true
  try {
    const payload = {
      serial_number: editPartInstanceDialog.form.serial_number,
      batch_number: editPartInstanceDialog.form.batch_number,
      manufacturer: editPartInstanceDialog.form.manufacturer,
      production_date: editPartInstanceDialog.form.production_date,
      status: editPartInstanceDialog.form.status,
      quality_level: editPartInstanceDialog.form.quality_level,
      key_degree: editPartInstanceDialog.form.key_degree
    }
    await updatePartInstance(editPartInstanceDialog.form.part_instance_id, payload)
    ElMessage.success('修改零件实例成功')
    editPartInstanceDialog.visible = false
    loadPartInstances()
  } catch {
  } finally {
    editPartInstanceDialog.loading = false
  }
}

function matchesPartFilters(row) {
  if (!row) return false
  const keyword = (partQuery.keyword || '').trim().toLowerCase()
  const material = (partQuery.material || '').trim()
  const status = (partQuery.status || '').trim()

  if (material && String(row.material || '') !== material) {
    return false
  }
  if (status && String(row.status || '') !== status) {
    return false
  }
  if (keyword) {
    const haystack = [
      row.part_code,
      row.part_name,
      row.spec_model,
      row.serial_number
    ]
      .map(v => String(v || '').toLowerCase())
      .join(' ')
    if (!haystack.includes(keyword)) {
      return false
    }
  }
  return true

}

async function submitDeletePartInstance(row) {
  if (!row || !row.part_instance_id) {
    ElMessage.warning('未找到零件实例编号')
    return
  }
  if (!window.confirm('确认删除零件实例【' + row.part_instance_id + '】吗？')) {
    return
  }
  try {
    await deletePartInstance(row.part_instance_id)
    ElMessage.success('删除零件实例成功')
    loadPartInstances()
  } catch {
  }
}

function openCreatePartTemplateDialog() {
  addPartTemplateDialog.visible = true
  addPartTemplateDialog.loading = false
  addPartTemplateDialog.form.part_code = ''
  addPartTemplateDialog.form.part_name = ''
  addPartTemplateDialog.form.material = ''
  addPartTemplateDialog.form.spec_model = ''
}

function openCreatePartTemplateFromComponent() {
  const node = current_node.value
  closeContextMenu()
  if (!node || !node.id) {
    ElMessage.warning('请先选择组件')
    return
  }
  if (!String(node.id).startsWith('component:')) {
    ElMessage.warning('请在组件层级新增零件')
    return
  }
  partQuery.module_id = node.id
  openCreatePartTemplateDialog()
}

async function submitCreatePartTemplate() {
  if (!addPartTemplateDialog.form.part_code || !addPartTemplateDialog.form.part_code.trim()) {
    ElMessage.warning('请输入编号')
    return
  }
  if (!addPartTemplateDialog.form.part_name || !addPartTemplateDialog.form.part_name.trim()) {
    ElMessage.warning('请输入零件名称')
    return
  }
  addPartTemplateDialog.loading = true
  try {
    const keepNodeId = current_node.value?.id || ''
    await createPartTemplate({
      module_id: partQuery.module_id,
      part_code: addPartTemplateDialog.form.part_code,
      part_name: addPartTemplateDialog.form.part_name,
      material: addPartTemplateDialog.form.material,
      spec_model: addPartTemplateDialog.form.spec_model
    })

    ElMessage.success('新增零件模板成功')
    addPartTemplateDialog.visible = false
    await reloadTreeView(keepNodeId)
  } catch {
  } finally {
    addPartTemplateDialog.loading = false
  }
}

async function loadModuleTree() {
  tree_loading.value = true

  try {
    const res = await fetchMonitorTree()
    const tree = Array.isArray(res.data) ? res.data : []
    treeData.value = tree
    applyRootView(tree)
  } catch {
    treeData.value = []
    applyRootView([])
  } finally {
    tree_loading.value = false
  }
}

async function loadNodeView(nodeId) {
  if (!nodeId) {
    currentChildren.value = []
    breadcrumbList.value = []
    partQuery.module_id = ''
    part_list.value = []
    return
  }

  node_loading.value = true

  try {
    const res = await fetchNodeView(nodeId)
    const data = res.data || {}
    const nextNode = data.current_node || current_node.value

    current_node.value = nextNode
    breadcrumbList.value = Array.isArray(data.breadcrumb_list) ? data.breadcrumb_list : []
    currentChildren.value = Array.isArray(data.children) ? data.children : []
    if (nextNode && nextNode.terminal === true) {
      partQuery.module_id = nextNode.id
      loadPartInstances()
    } else {
      partQuery.module_id = ''
      part_list.value = []
    }
  } catch {
    currentChildren.value = []
    breadcrumbList.value = []
    partQuery.module_id = ''
    part_list.value = []
  } finally {
    node_loading.value = false
  }
}

function importNumData() {
  openNumObjDlg('numeric')
}

function importSeqData() {
  openNumObjDlg('sequence')
}

function importTextData() {
  openTextObjDlg()
}

async function openTextObjDlg() {
  if (textImporting.value) return
  resetTextObjForm()
  textObjDlg.visible = true
  if (!treeData.value.length) {
    textObjDlg.loading = true
    try {
      const res = await fetchMonitorTree()
      treeData.value = Array.isArray(res.data) ? res.data : []
    } catch {
      treeData.value = []
      ElMessage.error('获取对象层级失败')
    } finally {
      textObjDlg.loading = false
    }
  }
}

function resetTextObjForm() {
  textObjDlg.keyword = ''
  textObjDlg.form.taskType = 'partProcess'
  textObjDlg.form.componentId = ''
  textObjDlg.form.partId = ''
  textObjDlg.form.importMode = 'file'
  textObjDlg.form.apiUrl = ''
  textObjDlg.form.apiMethod = 'GET'
  textObjDlg.form.apiHeaders = ''
  textObjDlg.form.apiBody = ''
  textObjDlg.form.apiFileName = ''
}

async function openNumObjDlg(importType = 'numeric') {
  if (numImporting.value) return
  numObjDlg.importType = importType
  resetNumObjForm()
  numObjDlg.visible = true
  if (!treeData.value.length) {
    numObjDlg.loading = true
    try {
      const res = await fetchMonitorTree()
      treeData.value = Array.isArray(res.data) ? res.data : []
    } catch {
      treeData.value = []
      ElMessage.error('获取对象层级失败')
    } finally {
      numObjDlg.loading = false
    }
  }
}

function resetNumObjForm() {
  numObjDlg.keyword = ''
  numObjDlg.form.purpose = 'faultIdentify'
  numObjDlg.form.aircraftId = ''
  numObjDlg.form.subsystemId = ''
  numObjDlg.form.equipmentId = ''
  numObjDlg.form.componentId = ''
  numObjDlg.form.partId = ''
  numObjDlg.form.importMode = 'file'
  numObjDlg.form.apiUrl = ''
  numObjDlg.form.apiMethod = 'GET'
  numObjDlg.form.apiHeaders = ''
  numObjDlg.form.apiBody = ''
  numObjDlg.form.apiFileName = ''
}

function selectNumPurpose(value) {
  if (numObjDlg.form.purpose === value) return
  numObjDlg.form.purpose = value
  onNumPurposeChange()
}

function selectTextTask(value) {
  if (textObjDlg.form.taskType === value) return
  textObjDlg.form.taskType = value
  textObjDlg.form.componentId = ''
  textObjDlg.form.partId = ''
}

function onNumPurposeChange() {
  numObjDlg.form.aircraftId = ''
  numObjDlg.form.subsystemId = ''
  numObjDlg.form.equipmentId = ''
  numObjDlg.form.componentId = ''
  numObjDlg.form.partId = ''
}

function hasNumObjSel() {
  const form = numObjDlg.form
  if (isProcAnom.value) {
    return !!(form.aircraftId || form.subsystemId || form.equipmentId || form.componentId || form.partId)
  }
  return !!(form.subsystemId || form.equipmentId || form.componentId)
}

function wait(ms) {
  return new Promise(resolve => window.setTimeout(resolve, ms))
}

function stopNumProgTimer() {
  if (numProgTimer) {
    window.clearInterval(numProgTimer)
    numProgTimer = null
  }
}

function startNumProg(text = '准备导入数据...') {
  stopNumProgTimer()
  numProg.visible = true
  numProg.percentage = 6
  numProg.text = text
  numProgTimer = window.setInterval(() => {
    if (numProg.percentage < 88) {
      numProg.percentage += Math.max(1, Math.round((88 - numProg.percentage) / 8))
    }
  }, 220)
}

async function finishNumProg(text = '导入完成') {
  stopNumProgTimer()
  numProg.text = text
  while (numProg.percentage < 96) {
    numProg.percentage += 4
    await wait(60)
  }
  numProg.percentage = 100
  await wait(650)
  numProg.visible = false
}

async function failNumProg(text = '导入失败') {
  stopNumProgTimer()
  numProg.text = text
  numProg.percentage = 100
  await wait(900)
  numProg.visible = false
  numProg.percentage = 0
}

function conNumObj() {
  if (!numObjDlg.form.purpose) {
    ElMessage.warning('请选择数据用途')
    return
  }
  if (!hasNumObjSel()) {
    ElMessage.warning(numObjReqText.value)
    return
  }
  if (numObjDlg.form.importMode === 'api') {
    submitNumApiData()
    return
  }
  numObjDlg.visible = false
  if (numFileInput.value) {
    numFileInput.value.value = ''
    numFileInput.value.click()
  }
}

function conTextObj() {
  if (!textTaskRequiresObject.value) {
    // These imports do not bind to a selected tree node.
  } else if (isTextPartQuality.value) {
    if (!textObjDlg.form.partId) {
      ElMessage.warning('请选择零件作为任务对象')
      return
    }
  } else if (!textObjDlg.form.componentId) {
    ElMessage.warning('请选择组件作为导入对象')
    return
  }
  if (textObjDlg.form.importMode === 'api') {
    submitTextApiData()
    return
  }
  textObjDlg.visible = false
  if (textFileInput.value) {
    textFileInput.value.value = ''
    textFileInput.value.click()
  }
}

async function onNumFileChange(event) {
  const files = Array.from(event?.target?.files || []).filter(file => /\.(csv|txt)$/i.test(file.name))
  if (event?.target) event.target.value = ''
  if (!files.length) {
    ElMessage.warning('请选择 CSV/TXT 文件')
    return
  }
  submitNumData(files)
}

async function onTextFileChange(event) {
  const pattern = (isTextPartQuality.value || isTextHierarchy.value || isTextActualProcess.value) ? /\.xlsx$/i : /\.(xlsx|xls)$/i
  const file = Array.from(event?.target?.files || []).find(item => pattern.test(item.name))
  if (event?.target) event.target.value = ''
  if (!file) {
    ElMessage.warning((isTextPartQuality.value || isTextHierarchy.value || isTextActualProcess.value) ? '请选择 .xlsx Excel 文件' : '请选择 Excel 文件')
    return
  }
  if (isTextHierarchy.value) {
    await submitHierarchyData(file)
    return
  }
  if (isTextPartQuality.value) {
    await submitPartQualityData(file)
    return
  }
  await submitTextData(file)
}

async function submitNumData(files) {
  if (!files.length) {
    ElMessage.warning('请选择 CSV/TXT 文件')
    return
  }

  numImporting.value = true
  numUploadBatchId = createNumUploadBatchId()
  numUploadRows.value = files.map((file, index) => ({
    id: `${numUploadBatchId}_${index}`,
    file,
    name: file.name,
    size: file.size,
    index,
    status: NUM_UPLOAD_WAITING,
    progress: 0,
    message: ''
  }))
  startNumProg('准备上传文件...')
  try {
    await runNumUploadQueue(numUploadRows.value)
    const success = numUploadRows.value.filter(item => item.status === NUM_UPLOAD_SUCCESS).length
    const failed = numUploadRows.value.filter(item => item.status === NUM_UPLOAD_FAILED).length
    await finishNumProg(`导入完成：成功 ${success} 个，失败 ${failed} 个`)
    ElMessage.success(`导入完成：成功 ${success} 个，失败 ${failed} 个`)
    dataFileQuery.uploadBatchId = numUploadBatchId
    if (dataFileDialog.visible) {
      dataFileQuery.pageNum = 1
      await loadDataFiles()
    }
  } catch (error) {
    const msg = error?.response?.data?.msg || error?.message || '未知错误'
    await failNumProg('导入失败')
    ElMessage.error('导入失败：' + msg)
  } finally {
    numImporting.value = false
  }
}

function parseNumApiHeaders() {
  const raw = (numObjDlg.form.apiHeaders || '').trim()
  if (!raw) return {}
  try {
    const parsed = JSON.parse(raw)
    if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed)) {
      throw new Error('headers must be object')
    }
    return parsed
  } catch {
    ElMessage.warning('请求头必须是JSON对象格式')
    return null
  }
}

async function submitNumApiData() {
  const apiUrl = (numObjDlg.form.apiUrl || '').trim()
  if (!apiUrl) {
    ElMessage.warning('请输入API地址')
    return
  }
  const headers = parseNumApiHeaders()
  if (headers === null) return

  numObjDlg.visible = false
  numImporting.value = true
  numUploadBatchId = createNumUploadBatchId()
  numUploadRows.value = [{
    id: `${numUploadBatchId}_api`,
    name: (numObjDlg.form.apiFileName || '').trim() || 'api_numeric_data.csv',
    size: 0,
    index: 0,
    status: NUM_UPLOAD_UPLOADING,
    progress: 25,
    message: 'API拉取中'
  }]
  startNumProg('正在通过API导入数据...')
  try {
    const res = await uploadNumericApi({
      purpose: numObjDlg.form.purpose,
      executionObject: 'bearing',
      aircraftId: numObjDlg.form.aircraftId || '',
      subsystemId: numObjDlg.form.subsystemId || '',
      equipmentId: numObjDlg.form.equipmentId || '',
      componentId: numObjDlg.form.componentId || '',
      partId: numObjDlg.form.partId || '',
      uploadBatchId: numUploadBatchId,
      apiUrl,
      method: numObjDlg.form.apiMethod || 'GET',
      headers,
      body: numObjDlg.form.apiBody || '',
      fileName: (numObjDlg.form.apiFileName || '').trim()
    })
    const data = res?.data || {}
    numUploadRows.value[0].status = data.uploadStatus === 'SKIPPED' ? NUM_UPLOAD_SUCCESS : NUM_UPLOAD_SUCCESS
    numUploadRows.value[0].progress = 100
    numUploadRows.value[0].size = Number(data.fileSize || 0)
    numUploadRows.value[0].name = data.fileName || numUploadRows.value[0].name
    numUploadRows.value[0].message = data.message || '导入成功'
    await finishNumProg('API导入完成')
    ElMessage.success('API导入完成')
    dataFileQuery.uploadBatchId = numUploadBatchId
    if (dataFileDialog.visible) {
      dataFileQuery.pageNum = 1
      await loadDataFiles()
    }
  } catch (error) {
    const msg = error?.response?.data?.msg || error?.message || '未知错误'
    numUploadRows.value[0].status = NUM_UPLOAD_FAILED
    numUploadRows.value[0].progress = 100
    numUploadRows.value[0].message = msg
    await failNumProg('API导入失败')
    ElMessage.error('API导入失败：' + msg)
  } finally {
    numImporting.value = false
  }
}

function createNumUploadBatchId() {
  return `NUM_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`
}

function numUploadForm(row) {
  const payload = new FormData()
  payload.append('purpose', numObjDlg.form.purpose)
  payload.append('executionObject', 'bearing')
  payload.append('aircraftId', numObjDlg.form.aircraftId || '')
  payload.append('subsystemId', numObjDlg.form.subsystemId || '')
  payload.append('equipmentId', numObjDlg.form.equipmentId || '')
  payload.append('componentId', numObjDlg.form.componentId || '')
  payload.append('partId', numObjDlg.form.partId || '')
  payload.append('uploadBatchId', numUploadBatchId)
  payload.append('fileIndex', String(row.index + 1))
  payload.append('totalFiles', String(numUploadRows.value.length))
  return payload
}

function getNumUploadConcurrency(rows) {
  const maxSize = Math.max(...rows.map(item => item.size), 0)
  if (maxSize <= 20 * NUM_MB) return 8
  if (maxSize <= 100 * NUM_MB) return 5
  if (maxSize <= NUM_NORMAL_UPLOAD_LIMIT) return 2
  return 1
}

async function runNumUploadQueue(rows) {
  const maxConcurrency = getNumUploadConcurrency(rows)
  const pending = rows.filter(item => item.status === NUM_UPLOAD_WAITING || item.status === NUM_UPLOAD_FAILED)
  let running = 0
  let runningSize = 0
  let cursor = 0

  return new Promise(resolve => {
    const schedule = () => {
      if (cursor >= pending.length && running === 0) {
        updateNumUploadProgress()
        resolve()
        return
      }
      while (running < maxConcurrency && cursor < pending.length) {
        const row = pending[cursor]
        if (running > 0 && runningSize + row.size > NUM_IN_FLIGHT_SIZE) break
        cursor++
        running++
        runningSize += row.size
        uploadNumRow(row)
          .catch(() => {})
          .finally(() => {
            running--
            runningSize -= row.size
            updateNumUploadProgress()
            schedule()
          })
      }
    }
    schedule()
  })
}

async function uploadNumRow(row) {
  row.progress = 0
  row.message = ''
  try {
    if (row.size > NUM_NORMAL_UPLOAD_LIMIT) {
      await uploadNumRowChunks(row)
    } else {
      const payload = numUploadForm(row)
      payload.append('file', row.file)
      row.status = NUM_UPLOAD_UPLOADING
      await uploadNumericFile(payload, event => {
        if (event.total) row.progress = Math.min(99, Math.round((event.loaded / event.total) * 100))
      })
      row.status = NUM_UPLOAD_SUCCESS
      row.progress = 100
      row.message = '上传成功'
    }
  } catch (error) {
    row.status = NUM_UPLOAD_FAILED
    row.message = error?.response?.data?.msg || error?.message || '上传失败'
  }
}

async function uploadNumRowChunks(row) {
  const uploadId = `${numUploadBatchId}_${row.index}_${Date.now()}`
  const chunkCount = Math.ceil(row.size / NUM_CHUNK_SIZE)
  row.status = NUM_UPLOAD_CHUNK_UPLOADING
  for (let i = 0; i < chunkCount; i++) {
    const start = i * NUM_CHUNK_SIZE
    const end = Math.min(row.size, start + NUM_CHUNK_SIZE)
    const payload = new FormData()
    payload.append('uploadId', uploadId)
    payload.append('chunkIndex', String(i))
    payload.append('chunkCount', String(chunkCount))
    payload.append('fileName', row.name)
    payload.append('chunk', row.file.slice(start, end))
    await uploadNumericChunk(payload)
    row.progress = Math.min(95, Math.round(((i + 1) / chunkCount) * 95))
  }

  row.status = NUM_UPLOAD_MERGING
  row.message = '正在合并'
  const mergePayload = numUploadForm(row)
  mergePayload.append('uploadId', uploadId)
  mergePayload.append('fileName', row.name)
  await mergeNumericChunks(mergePayload)
  row.status = NUM_UPLOAD_SUCCESS
  row.progress = 100
  row.message = '上传成功'
}

function updateNumUploadProgress() {
  const total = numUploadRows.value.length || 1
  const done = numUploadRows.value.filter(item => [NUM_UPLOAD_SUCCESS, NUM_UPLOAD_FAILED].includes(item.status)).length
  const success = numUploadRows.value.filter(item => item.status === NUM_UPLOAD_SUCCESS).length
  const failed = numUploadRows.value.filter(item => item.status === NUM_UPLOAD_FAILED).length
  numProg.visible = true
  numProg.percentage = Math.min(100, Math.round((done / total) * 100))
  numProg.text = `正在上传：已完成 ${done}/${total}，成功 ${success}，失败 ${failed}`
}

async function retryNumUpload(row) {
  if (!row || numImporting.value) return
  numImporting.value = true
  numProg.visible = true
  try {
    row.status = NUM_UPLOAD_WAITING
    row.progress = 0
    row.message = ''
    await runNumUploadQueue([row])
    const failed = numUploadRows.value.filter(item => item.status === NUM_UPLOAD_FAILED).length
    ElMessage.success(failed ? '重试完成，仍有失败文件' : '重试完成')
    if (dataFileDialog.visible) await loadDataFiles()
  } finally {
    numImporting.value = false
  }
}

function uploadStatusText(status) {
  const map = {
    WAITING: '等待上传',
    UPLOADING: '上传中',
    SUCCESS: '成功',
    FAILED: '失败',
    CHUNK_UPLOADING: '分片上传中',
    MERGING: '合并中'
  }
  return map[status] || status || '--'
}

async function submitTextData(file) {
  if (textTaskRequiresObject.value && !textObjDlg.form.componentId) {
    ElMessage.warning('请选择组件作为导入对象')
    return
  }

  const payload = new FormData()
  payload.append('componentId', textObjDlg.form.componentId || '')
  payload.append('taskType', textObjDlg.form.taskType)
  payload.append('file', file)

  textImporting.value = true
  try {
    const res = await uploadProcessTextData(payload)
    const data = res?.data || {}
    if (isTextActualProcess.value) {
      ElMessage.success('导入完成：零件实例 ' + (data.part_instance_count || 0) + ' 条，生产工单 ' + (data.work_order_count || 0) + ' 条，工序执行记录 ' + (data.process_execution_count || 0) + ' 条')
    } else {
      ElMessage.success('导入完成：零件 ' + (data.part_count || 0) + ' 个，新增实例 ' + (data.instance_count || 0) + ' 个，工序 ' + (data.process_count || 0) + ' 条')
    }
    const keepNodeId = current_node.value?.id || ''
    await reloadTreeView(keepNodeId)
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.message || '导入文本数据失败')
  } finally {
    textImporting.value = false
  }
}

async function submitPartQualityData(file) {
  const payload = new FormData()
  payload.append('file', file)

  textImporting.value = true
  try {
    const res = await uploadPartQualityData(payload)
    const data = res?.data || {}
    if (data.success === false) {
      const errors = Array.isArray(data.errors) ? data.errors : []
      const firstError = errors.length ? errors[0] : null
      const detail = firstError ? `${firstError.sheetName || ''} 第${firstError.rowIndex || '-'}行 ${firstError.fieldName || ''}：${firstError.message || ''}` : '请检查Excel数据'
      ElMessage.error(`导入失败：${detail}${errors.length > 1 ? `，共 ${errors.length} 处错误` : ''}`)
      return
    }
    ElMessage.success('导入成功：设计 ' + (data.designCount || 0) + ' 条，制造 ' + (data.manufacturingCount || 0) + ' 条，服役 ' + (data.serviceCount || 0) + ' 条')
    if (partQuery.module_id) loadPartInstances()
  } catch (error) {
    const data = error?.response?.data?.data
    const firstError = Array.isArray(data?.errors) && data.errors.length ? data.errors[0] : null
    const detail = firstError ? `${firstError.sheetName || ''} 第${firstError.rowIndex || '-'}行 ${firstError.fieldName || ''}：${firstError.message || ''}` : ''
    ElMessage.error(detail || error?.response?.data?.msg || error?.message || '导入零件质量信息失败')
  } finally {
    textImporting.value = false
  }
}

async function submitHierarchyData(file) {
  const payload = new FormData()
  payload.append('file', file)

  textImporting.value = true
  try {
    const res = await uploadHierarchyData(payload)
    const data = res?.data || {}
    ElMessage.success('导入完成：层级对象 ' + (data.total_count || 0) + ' 条')
    await reloadTreeView(current_node.value?.id || '')
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.message || '导入层级对象失败')
  } finally {
    textImporting.value = false
  }
}

function parseTextApiHeaders() {
  const raw = (textObjDlg.form.apiHeaders || '').trim()
  if (!raw) return {}
  try {
    const parsed = JSON.parse(raw)
    if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed)) {
      throw new Error('headers must be object')
    }
    return parsed
  } catch {
    ElMessage.warning('请求头必须是JSON对象格式')
    return null
  }
}

async function submitTextApiData() {
  const apiUrl = (textObjDlg.form.apiUrl || '').trim()
  if (!apiUrl) {
    ElMessage.warning('请输入API地址')
    return
  }
  const headers = parseTextApiHeaders()
  if (headers === null) return

  const payload = {
    taskType: textObjDlg.form.taskType,
    componentId: textObjDlg.form.componentId || '',
    partId: textObjDlg.form.partId || '',
    apiUrl,
    method: textObjDlg.form.apiMethod || 'GET',
    headers,
    body: textObjDlg.form.apiBody || '',
    fileName: (textObjDlg.form.apiFileName || '').trim()
  }

  textObjDlg.visible = false
  textImporting.value = true
  try {
    let res
    if (isTextPartQuality.value) {
      res = await uploadPartQualityApiData(payload)
      const data = res?.data || {}
      if (data.success === false) {
        const errors = Array.isArray(data.errors) ? data.errors : []
        const firstError = errors.length ? errors[0] : null
        const detail = firstError ? `${firstError.sheetName || ''} 第${firstError.rowIndex || '-'}行 ${firstError.fieldName || ''}：${firstError.message || ''}` : '请检查Excel数据'
        ElMessage.error(`导入失败：${detail}${errors.length > 1 ? `，共 ${errors.length} 处错误` : ''}`)
        return
      }
      ElMessage.success('API导入成功：设计 ' + (data.designCount || 0) + ' 条，制造 ' + (data.manufacturingCount || 0) + ' 条，服役 ' + (data.serviceCount || 0) + ' 条')
      if (partQuery.module_id) loadPartInstances()
      return
    }

    res = await uploadProcessTextApiData(payload)
    const data = res?.data || {}
    if (isTextHierarchy.value) {
      ElMessage.success('API导入完成：层级对象 ' + (data.total_count || 0) + ' 条')
      await reloadTreeView(current_node.value?.id || '')
    } else if (isTextActualProcess.value) {
      ElMessage.success('API导入完成：零件实例 ' + (data.part_instance_count || 0) + ' 条，生产工单 ' + (data.work_order_count || 0) + ' 条，工序执行记录 ' + (data.process_execution_count || 0) + ' 条')
      await reloadTreeView(current_node.value?.id || '')
    } else {
      ElMessage.success('API导入完成：零件 ' + (data.part_count || 0) + ' 个，新增实例 ' + (data.instance_count || 0) + ' 个，工序 ' + (data.process_count || 0) + ' 条')
      await reloadTreeView(current_node.value?.id || '')
    }
  } catch (error) {
    const data = error?.response?.data?.data
    const firstError = Array.isArray(data?.errors) && data.errors.length ? data.errors[0] : null
    const detail = firstError ? `${firstError.sheetName || ''} 第${firstError.rowIndex || '-'}行 ${firstError.fieldName || ''}：${firstError.message || ''}` : ''
    ElMessage.error(detail || error?.response?.data?.msg || error?.message || 'API导入文本数据失败')
  } finally {
    textImporting.value = false
  }
}

async function downloadPartQualityTemplate() {
  try {
    const data = await downloadPartQualityTemplateApi()
    saveAs(new Blob([data]), '零件质量信息导入模板.xlsx')
  } catch {
    ElMessage.error('下载导入模板失败')
  }
}

async function downloadPartProcessTemplate() {
  try {
    const data = await downloadPartProcessTemplateApi()
    saveAs(new Blob([data]), '零件标准制作过程导入模板.xlsx')
  } catch {
    ElMessage.error('下载导入模板失败')
  }
}

async function generatePartActualManufacturingProcessTemplate() {
  try {
    const data = await downloadPartProcessTemplateApi({ taskType: 'PART_ACTUAL_MANUFACTURING_PROCESS' })
    saveAs(new Blob([data]), '零件实际制作过程导入模板.xlsx')
  } catch {
    ElMessage.error('下载导入模板失败')
  }
}

async function downloadHierarchyTemplate() {
  try {
    const data = await downloadHierarchyTemplateApi()
    saveAs(new Blob([data]), '层级对象导入模板.xlsx')
  } catch {
    ElMessage.error('下载导入模板失败')
  }
}

function downloadTextImportTemplate() {
  if (isTextHierarchy.value) {
    downloadHierarchyTemplate()
    return
  }
  if (isTextPartQuality.value) {
    downloadPartQualityTemplate()
    return
  }
  if (isTextActualProcess.value) {
    generatePartActualManufacturingProcessTemplate()
    return
  }
  downloadPartProcessTemplate()
}

function dataUsageText(value) {
  const map = {
    FAULT_IDENTIFY: '服役周期',
    MANUFACTURE_COMMON: '制造周期通用',
    PROCESS_ANOMALY: '工序异常检测',
    KQC_MINING: '关键质量特性挖掘',
    KEY_PROCESS: '关键工序识别',
    COMMON: '常用'
  }
  return map[value] || value || '-'
}

function isManufacturingDataUsage(value) {
  return MANUFACTURING_DATA_USAGES.includes(value)
}

function fileSizeText(size) {
  const value = Number(size || 0)
  if (!value) return '-'
  if (value < 1024) return value + ' B'
  if (value < 1024 * 1024) return (value / 1024).toFixed(1) + ' KB'
  return (value / 1024 / 1024).toFixed(1) + ' MB'
}

function val(value) {
  return value === undefined || value === null || value === '' ? '--' : value
}

async function changeDataFileUsage(row, dataUsage) {
  if (!row?.id || row.dataUsage === dataUsage) return
  const oldUsage = row.dataUsage
  row.usageUpdating = true
  try {
    await updateDataFileUsage(row.id, dataUsage)
    row.dataUsage = dataUsage
    dataFileDialog.selectedRows.forEach(item => {
      if (item.id === row.id) item.dataUsage = dataUsage
    })
    ElMessage.success('数据用途已更新')
  } catch (e) {
    row.dataUsage = oldUsage
    ElMessage.error(e?.response?.data?.msg || e?.message || '数据用途更新失败')
  } finally {
    row.usageUpdating = false
  }
}

async function openDataFileDialog() {
  dataFileDialog.visible = true
  dataFileQuery.pageNum = 1
  clearSelectedDataFiles()
  await loadDataFiles()
}

async function searchDataFiles() {
  dataFileQuery.pageNum = 1
  clearSelectedDataFiles()
  await loadDataFiles()
}

async function loadDataFiles() {
  dataFileDialog.loading = true
  try {
    const res = await listAllDataFiles({
      dataUsage: dataFileQuery.dataUsage || 'ALL',
      keyword: dataFileQuery.keyword,
      uploadBatchId: dataFileQuery.uploadBatchId,
      pageNum: dataFileQuery.pageNum,
      pageSize: dataFileQuery.pageSize
    })
    dataFileDialog.rows = Array.isArray(res.rows) ? res.rows : []
    dataFileDialog.total = Number(res.total || 0)
    syncDataFilePageSelection()
  } catch (error) {
    dataFileDialog.rows = []
    dataFileDialog.total = 0
    ElMessage.error(error?.response?.data?.msg || error?.message || '获取数据文件失败')
  } finally {
    dataFileDialog.loading = false
  }
}

function handleDataFileSelectionChange(selection) {
  const selectedMap = new Map(dataFileDialog.selectedRows.map(row => [row.id, row]))
  const currentIds = new Set(dataFileDialog.rows.map(row => row.id))
  currentIds.forEach(id => selectedMap.delete(id))
  ;(Array.isArray(selection) ? selection : []).forEach(row => {
    if (row && row.id !== undefined && row.id !== null) {
      selectedMap.set(row.id, row)
    }
  })
  dataFileDialog.selectedRows = Array.from(selectedMap.values())
}

function syncDataFilePageSelection() {
  const selectedIds = new Set(dataFileDialog.selectedRows.map(row => row.id))
  nextTick(() => {
    const table = dataFileTableRef.value
    if (!table || !dataFileDialog.rows.length) return
    table.clearSelection?.()
    dataFileDialog.rows.forEach(row => {
      if (selectedIds.has(row.id)) {
        table.toggleRowSelection?.(row, true)
      }
    })
  })
}

async function selectAllDataFiles() {
  if (!dataFileDialog.total) return
  dataFileDialog.selectingAll = true
  try {
    const pageSize = 500
    const first = await listAllDataFiles({
      dataUsage: dataFileQuery.dataUsage || 'ALL',
      keyword: dataFileQuery.keyword,
      uploadBatchId: dataFileQuery.uploadBatchId,
      pageNum: 1,
      pageSize
    })
    const total = Number(first.total || 0)
    const allRows = Array.isArray(first.rows) ? [...first.rows] : []
    const effectivePageSize = allRows.length || pageSize
    const pageCount = Math.ceil(total / effectivePageSize)
    for (let pageNum = 2; pageNum <= pageCount; pageNum++) {
      const res = await listAllDataFiles({
        dataUsage: dataFileQuery.dataUsage || 'ALL',
        keyword: dataFileQuery.keyword,
        uploadBatchId: dataFileQuery.uploadBatchId,
        pageNum,
        pageSize
      })
      if (Array.isArray(res.rows)) {
        allRows.push(...res.rows)
      }
    }
    const unique = new Map()
    allRows.forEach(row => {
      if (row && row.id !== undefined && row.id !== null) {
        unique.set(row.id, row)
      }
    })
    dataFileDialog.selectedRows = Array.from(unique.values())
    syncDataFilePageSelection()
    ElMessage.success(`已全选 ${dataFileDialog.selectedRows.length} 个数据文件`)
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.message || '全选数据文件失败')
  } finally {
    dataFileDialog.selectingAll = false
  }
}

function clearSelectedDataFiles() {
  dataFileDialog.selectedRows = []
  dataFileTableRef.value?.clearSelection?.()
}

async function removeDataFile(row) {
  try {
    await ElMessageBox.confirm('确认删除数据文件【' + (row.fileName || row.id) + '】吗？', '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    await deleteDataFile(row.id)
    ElMessage.success('删除成功')
    await loadDataFiles()
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error?.response?.data?.msg || error?.message || '删除数据文件失败')
  }
}

async function removeSelectedDataFiles() {
  const rows = dataFileDialog.selectedRows.filter(row => row && row.id !== undefined && row.id !== null)
  if (!rows.length) {
    ElMessage.warning('请先选择要删除的数据文件')
    return
  }
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${rows.length} 个数据文件吗？`, '批量删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    dataFileDialog.deleting = true
    let success = 0
    let failed = 0
    for (const row of rows) {
      try {
        await deleteDataFile(row.id)
        success++
      } catch {
        failed++
      }
    }
    clearSelectedDataFiles()
    await loadDataFiles()
    if (failed) {
      ElMessage.warning(`批量删除完成：成功 ${success} 个，失败 ${failed} 个`)
    } else {
      ElMessage.success(`批量删除成功：${success} 个`)
    }
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error?.response?.data?.msg || error?.message || '批量删除数据文件失败')
  } finally {
    dataFileDialog.deleting = false
  }
}

watch([() => kqcDialog.visible, kqcRankedRows], scheduleKqcRankingRender)
watch([() => kqcGraphDialog.visible, kqcGraphData], () => {
  if (kqcGraphDialog.visible) scheduleKqcGraphRender()
})
watch([() => detectDialog.visible, detectCurveRows], scheduleAnomalyChartRender)

watch(
  () => detectDialog.form.enabled,
  enabled => {
    if (!enabled && detectDialog.visible) {
      detectFileQuery.pageNum = 1
      loadDetectSamples()
    }
  }
)

watch(
  () => keyDialog.form.enabled,
  enabled => {
    if (!enabled && keyDialog.visible) {
      keyFileQuery.pageNum = 1
      loadKeySamples()
    }
  }
)

onMounted(() => {
  loadModuleTree()
  loadKqcHistory()
  window.addEventListener('resize', resizeKqcGraph)
  window.addEventListener('resize', resizeAnomalyChart)
})

onBeforeUnmount(() => {
  stopNumProgTimer()
  clearKqcPolling()
  clearDetectPolling()
  clearKeyPolling()
  kqcSubmitting.value = false
  kqcRunning.value = false
  detectSubmitting.value = false
  detectRunning.value = false
  keySubmitting.value = false
  keyRunning.value = false
  window.removeEventListener('resize', resizeKqcGraph)
  window.removeEventListener('resize', resizeAnomalyChart)
  kqcRankingChart?.dispose()
  kqcRankingChart = null
  kqcGraphChart?.dispose()
  kqcGraphChart = null
  anomalyChart?.dispose()
  anomalyChart = null
})
</script>


<style scoped>
.left-tree-card {
  min-height: 540px;
}

.structure-path {
  margin-bottom: 12px;
  color: #607081;
  line-height: 1.7;
}

.module-tree {
  padding-top: 4px;
}

.tree-node-content {
  display: flex;
  align-items: center;
  min-height: 28px;
}

.tree-node-name {
  color: #355070;
}
.content-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  gap: 12px;
}
.current-path {
  flex: 1;
  min-height: 46px;
  padding: 0 16px;
  border: 1px solid #dbe4ee;
  border-radius: 12px;
  background: #fff;
  display: flex;
  align-items: center;
  color: #4f6075;
}

.data-import-row {
  display: flex;
  gap: 16px;
}

.data-import-card {
  flex: 1;
  padding: 16px;
  border: 1px solid #dbe4ee;
  border-radius: 14px;
  background: #fff;
}

.import-btn {
  width: 100%;
  height: 44px;
  border-radius: 12px;
}

.quality-card-header,
.kqc-history-toolbar,
.kqc-task-lookup {
  display: flex;
  align-items: center;
  gap: 12px;
}

.quality-card-header {
  justify-content: space-between;
}

.kqc-history-toolbar {
  margin-bottom: 12px;
}

.kqc-history-toolbar .el-input {
  max-width: 280px;
}

.kqc-history-toolbar .el-select {
  width: 140px;
}

.kqc-task-lookup {
  margin-bottom: 16px;
}

.kqc-task-lookup .el-input {
  max-width: 320px;
}

.detect-summary,
.task-lookup-row,
.detect-data-header,
.file-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.detect-summary {
  justify-content: space-between;
  margin-bottom: 12px;
  color: #607081;
}

.task-lookup-row {
  margin: 12px 0 16px;
}

.task-lookup-row .el-input {
  max-width: 320px;
}

.dialog-action-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: 12px 0 16px;
  padding: 12px 14px;
  border: 1px solid #dbe4ee;
  border-radius: 8px;
  background: #f7fbff;
}

.dialog-action-hint {
  color: #607081;
  font-size: 13px;
}

.dialog-action-buttons {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.dialog-action-buttons .el-button {
  margin-left: 0;
}

.detect-data-section {
  margin-top: 14px;
  padding: 14px;
  border: 1px solid #dbe4ee;
  border-radius: 8px;
  background: #fbfdff;
}

.detect-data-header {
  justify-content: space-between;
  margin-bottom: 10px;
}

.detect-data-title {
  font-size: 15px;
  font-weight: 700;
  color: #1f2d3d;
}

.file-toolbar {
  margin: 12px 0;
  flex-wrap: wrap;
}

.detect-result {
  margin-top: 16px;
}

.anomaly-chart-panel {
  padding: 14px 16px 10px;
  border: 1px solid #dbe4ee;
  border-radius: 8px;
  background: #fbfdff;
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

.kqc-result-layout {
  display: grid;
  grid-template-columns: minmax(360px, 0.9fr) minmax(560px, 1.4fr);
  gap: 16px;
}

.kqc-chart-panel,
.kqc-table-panel,
.kqc-graph-panel {
  border: 1px solid #dbe4ee;
  border-radius: 8px;
  background: #fbfdff;
  overflow: hidden;
}

.kqc-panel-title,
.kqc-graph-title {
  padding: 10px 12px;
  border-bottom: 1px solid #e3ebf4;
  color: #304156;
  font-weight: 600;
}

.kqc-ranking-chart {
  width: 100%;
  height: 320px;
}

.kqc-graph {
  width: 100%;
  height: 560px;
}

@media (max-width: 1100px) {
  .kqc-result-layout {
    grid-template-columns: 1fr;
  }
}


.import-desc {
  margin-top: 14px;
  color: #607081;
  line-height: 1.8;
}

.num-import-progress {
  margin-top: 12px;
  padding: 10px 12px;
  border: 1px solid #dbe7f5;
  border-radius: 10px;
  background: #f8fbff;
}

.num-progress-text {
  margin-bottom: 8px;
  font-size: 13px;
  color: #4f6075;
}

.num-upload-list {
  margin-top: 12px;
}

.num-upload-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 8px;
  color: #304156;
  font-size: 13px;
}

.form-tip {
  width: 100%;
  margin-top: 6px;
  color: #8a97a8;
  font-size: 12px;
  line-height: 1.5;
}

.hidden-file-input {
  display: none;
}

.object-select {
  width: 100%;
}

:deep(.numeric-object-dialog .el-dialog__header) {
  padding: 16px 22px 12px;
  margin-right: 0;
  border-bottom: 1px solid #dfe7f1;
}

:deep(.numeric-object-dialog .el-dialog__body) {
  padding: 0;
}

:deep(.numeric-object-dialog .el-dialog__footer) {
  padding: 14px 22px;
  border-top: 1px solid #dfe7f1;
  background: #f8fafc;
}

.numeric-object-layout {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  min-height: 560px;
  background: #fff;
}

.numeric-task-panel {
  padding: 20px 22px;
  border-right: 1px solid #dfe7f1;
  background: #f8fafc;
}

.numeric-panel-title {
  margin-bottom: 12px;
  font-size: 16px;
  color: #0f2a4a;
}

.numeric-task-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.numeric-task-card {
  min-height: 78px;
  padding: 16px 16px;
  border: 1px solid #d9e4f0;
  border-radius: 14px;
  background: #fff;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  cursor: pointer;
  transition: all 0.18s ease;
}

.numeric-task-card.active {
  border-color: #83b9ff;
  background: #edf6ff;
  box-shadow: 0 4px 10px rgba(29, 107, 255, 0.14);
}

.numeric-task-main {
  min-width: 0;
}

.numeric-task-name {
  margin-bottom: 10px;
  font-size: 17px;
  font-weight: 700;
  color: #001b44;
}

.numeric-task-desc {
  font-size: 13px;
  color: #506886;
}

.numeric-radio {
  width: 16px;
  height: 16px;
  margin-top: 2px;
  border: 1px solid #c6d6ea;
  border-radius: 50%;
  background: #fff;
  flex: 0 0 auto;
}

.numeric-radio.checked {
  border: 4px solid #2167ff;
}

.numeric-switch-tip {
  margin-top: 22px;
  padding: 14px 16px;
  border: 1px solid #ffd45b;
  border-radius: 14px;
  background: #fff9e8;
  color: #b65f00;
  font-size: 13px;
  line-height: 1.7;
}

.numeric-switch-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
  font-size: 14px;
  color: #b65f00;
}

.numeric-object-main {
  padding: 20px 22px 22px;
  min-width: 0;
}

.numeric-object-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 14px;
}

.numeric-current-task {
  color: #334c70;
  font-size: 14px;
  line-height: 1.6;
}

.numeric-current-task span {
  color: #0057ff;
}

.numeric-object-search {
  margin-bottom: 14px;
}

:deep(.numeric-object-search .el-input__wrapper) {
  min-height: 40px;
  border-radius: 10px;
  box-shadow: 0 0 0 1px #d9e4f0 inset;
}

.numeric-object-content {
  display: grid;
  grid-template-columns: minmax(430px, 1fr) 260px;
  gap: 18px;
}

.numeric-tree-wrap {
  height: 398px;
  padding: 10px;
  border: 1px solid #d9e4f0;
  border-radius: 14px;
  overflow: auto;
}

.numeric-object-tree {
  min-width: 420px;
}

:deep(.numeric-object-tree .el-tree-node__content) {
  height: 42px;
  border-radius: 10px;
}

:deep(.numeric-object-tree .el-tree-node__content:hover) {
  background: transparent;
}

.numeric-tree-node {
  width: 100%;
  min-width: 0;
  height: 42px;
  padding: 0 12px 0 6px;
  border: 1px solid transparent;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  color: #001b44;
}

.numeric-tree-node.selected {
  border-color: #8bc1ff;
  background: #edf6ff;
}

.numeric-tree-node.disabled {
  color: #8ba1bd;
}

.numeric-node-left,
.numeric-node-right {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.numeric-node-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
}

.numeric-node-icon {
  color: #435a75;
}

.numeric-tree-node.disabled .numeric-node-icon {
  color: #c2d0e0;
}

.numeric-node-status {
  padding: 3px 8px;
  border-radius: 12px;
  background: #e9fbf2;
  color: #008d55;
  font-size: 12px;
  line-height: 1;
  white-space: nowrap;
}

.numeric-node-status.disabled {
  background: #f1f5f9;
  color: #8093ad;
}

.numeric-node-check {
  color: #0057ff;
}

.numeric-side-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.numeric-info-card {
  padding: 16px;
  border-radius: 14px;
}

.numeric-info-card {
  border: 1px solid #d9e4f0;
  background: #fff;
}

.numeric-info-title {
  margin-bottom: 12px;
  font-size: 15px;
  color: #0f2a4a;
}

.numeric-selected-card {
  padding: 14px;
  border: 1px solid #c9dfff;
  border-radius: 12px;
  background: #edf6ff;
}

.numeric-selected-name {
  margin-bottom: 6px;
  font-size: 17px;
  font-weight: 700;
  color: #0b3b91;
}

.numeric-selected-type {
  margin-bottom: 12px;
  color: #0057ff;
  font-size: 13px;
}

.numeric-selected-process {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}

.numeric-process-row {
  padding: 8px 10px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.85);
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.numeric-process-row span {
  color: #60728b;
  font-size: 12px;
  white-space: nowrap;
}

.numeric-process-row strong {
  min-width: 0;
  color: #0f2a4a;
  font-size: 13px;
  font-weight: 500;
  text-align: right;
  word-break: break-all;
}

.numeric-selected-path {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.numeric-path-row {
  padding: 8px 10px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.7);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.numeric-path-row span {
  color: #60728b;
  font-size: 12px;
  white-space: nowrap;
}

.numeric-path-row strong {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #0f2a4a;
  font-size: 13px;
  font-weight: 500;
}

.numeric-empty-selected {
  padding: 16px;
  border-radius: 12px;
  background: #f6f8fb;
  color: #8093ad;
}

.data-file-toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 12px;
}

.module-card-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(240px, 1fr));
  gap: 18px;
  align-items: start;
  min-height: 180px;
}

.module-node-card {
  min-height: 132px;
  padding: 22px 20px;
  border: 1px solid #cfe0f2;
  border-radius: 16px;
  background: #fff;
  cursor: pointer;
  transition: all 0.2s ease;
}

.module-node-card:hover {
  border-color: #8eb8e8;
  box-shadow: 0 8px 20px rgba(76, 143, 220, 0.12);
}

.module-node-title {
  margin-bottom: 12px;
  font-size: 18px;
  font-weight: 700;
  color: #17324d;
}


.module-node-desc {
  color: #5f7287;
  line-height: 1.9;
}

.module-context-menu {
  position: fixed;
  z-index: 3000;
  width: 210px;
  padding: 6px;
  border: 1px solid #d8e5f2;
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 16px 32px rgba(31, 45, 61, 0.18);
}

.context-menu-item {
  height: 38px;
  line-height: 38px;
  padding: 0 14px;
  border-radius: 8px;
  font-size: 14px;
  color: #1f2d3d;
  cursor: pointer;
}

.context-menu-item:hover {
  background: #eaf3ff;
  color: #4c8fdc;
}

.context-menu-item.danger:hover {
  background: #fdeeee;
  color: #c95858;
}

.quality-detail-wrap {
  max-height: 560px;
  overflow: auto;
}


.quality-stage-block {
  margin-top: 16px;
}


.quality-stage-title {
  margin-bottom: 10px;
  font-weight: 600;
  color: #17324d;
}

.quality-dynamic-table {
  width: 100%;
}
</style>



