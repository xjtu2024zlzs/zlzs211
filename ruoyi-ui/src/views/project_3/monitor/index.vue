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
        </aside>

        <section class="numeric-object-main">
          <div class="numeric-object-header">
            <div>
              <div class="numeric-panel-title">任务对象</div>
              <div class="numeric-current-task">当前任务：{{ curNumPurposeLabel }}，请选择 <span>{{ numObjReqLevelText }}</span> 作为任务对象。</div>
            </div>
          </div>
          <el-input v-model="numObjDlg.keyword" class="numeric-object-search" placeholder="搜索对象名称" clearable :prefix-icon="Search" />
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
        <el-button type="primary" :loading="numObjDlg.loading" :disabled="numObjDlg.loading" @click="conNumObj">导入数据</el-button>
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
        </aside>

        <section class="numeric-object-main">
          <div class="numeric-object-header">
            <div>
              <div class="numeric-panel-title">任务对象</div>
              <div class="numeric-current-task">当前任务：{{ curTextTaskLabel }}，请选择 <span>{{ textObjReqLevelText }}</span> 作为任务对象。</div>
            </div>
            <el-button v-if="hasTextImportTemplate" plain type="primary" @click="downloadTextImportTemplate">下载导入模板</el-button>
          </div>
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
        <el-select v-model="dataFileQuery.dataUsage" placeholder="全部用途" clearable style="width: 160px" @change="loadDataFiles">
          <el-option label="全部用途" value="ALL" />
          <el-option label="服役周期" value="FAULT_IDENTIFY" />
          <el-option label="制造周期" value="PROCESS_ANOMALY" />
          <el-option label="通用" value="COMMON" />
        </el-select>
        <el-input v-model="dataFileQuery.keyword" placeholder="搜索文件名/路径/对象" clearable style="width: 260px" @keyup.enter="loadDataFiles" @clear="loadDataFiles" />
        <el-input v-model="dataFileQuery.uploadBatchId" placeholder="上传批次" clearable style="width: 180px" @keyup.enter="loadDataFiles" @clear="loadDataFiles" />
        <el-button type="primary" @click="loadDataFiles">查询</el-button>
      </div>
      <el-table v-loading="dataFileDialog.loading" :data="dataFileDialog.rows" height="460" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="fileName" label="文件名" min-width="180" show-overflow-tooltip />
        <el-table-column label="数据用途" width="130"><template #default="{ row }">{{ dataUsageText(row.dataUsage) }}</template></el-table-column>
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
              <el-table-column prop="part_code" label="编号" min-width="120" />
              <el-table-column prop="part_name" label="零件名称" min-width="160" />
              <el-table-column prop="material" label="材料" min-width="140" />
              <el-table-column prop="spec_model" label="规格型号" min-width="140" />
              <el-table-column prop="batch_no" label="批次号" min-width="120" />
              <el-table-column prop="quality_level" label="质量等级" min-width="120" />
              <el-table-column prop="status" label="状态" min-width="100" />
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
      <div class="context-menu-item danger" @click.stop="handleDeleteModule">{{ deleteEntryText }}</div>
    </div>

    <el-dialog v-model="nodeDetailDialog.visible" width="760px" :title="`对象详情 - ${nodeDetailDialog.node?.name || ''}`">
      <el-descriptions :column="2" border>
        <el-descriptions-item v-for="item in nodeDetailItems" :key="item.key" :label="item.label">
          {{ item.value }}
        </el-descriptions-item>
      </el-descriptions>
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
  </div>
</template>
<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Select, Warning } from '@element-plus/icons-vue'
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

function downloadPartQualityTemplateApi() {
  return request({
    url: '/quality/part-quality/template',
    method: 'get',
    responseType: 'blob',
    timeout: 300000
  })
}

function downloadPartProcessTemplateApi() {
  return request({
    url: '/monitor/text/process/template',
    method: 'get',
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
const NUM_NORMAL_UPLOAD_LIMIT = 500 * NUM_MB
const NUM_IN_FLIGHT_SIZE = 400 * NUM_MB
const NUM_CHUNK_SIZE = 20 * NUM_MB
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
  { label: '制造周期', value: 'processAnomaly', desc: '仅支持选择零件' },
]
const textTaskOpts = [
  {
    label: '零件工序',
    value: 'partProcess',
    desc: '导入零件及制造工序',
    objectLevelText: '组件',
    rule: '请选择组件作为导入对象，Excel需包含零件编号、零件名称、工序序号、工序编号、工序名称。'
  },
  {
    label: '零件质量信息',
    value: 'partQuality',
    desc: '选择零件作为质量信息归属对象',
    objectLevelText: '零件',
    rule: '请选择零件作为任务对象。'
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
    partId: ''
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
    partId: ''
  }
})
const dataFileDialog = reactive({
  visible: false,
  loading: false,
  rows: [],
  total: 0
})
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
const nodeDetailDialog = reactive({
  visible: false,
  node: null
})
const partQualityDialog = reactive({
  visible: false,
  loading: false,
  part_name: '',
  part_code: '',
  tables: []
})

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
const nodeDetailItems = computed(() => {
  const node = nodeDetailDialog.node || {}
  const labelMap = {
    id: '对象ID',
    name: '对象名称',
    level: '层级',
    parent_id: '父级ID',
    terminal: '是否末级',
    child_count: '子级数量',
    part_count: '零件数量'
  }
  const excludeKeys = new Set(['children'])
  return Object.keys(node)
    .filter(key => !excludeKeys.has(key))
    .map(key => ({
      key,
      label: labelMap[key] || key,
      value: formatNodeDetailValue(node[key], key)
    }))
})

const currentPathText = computed(() => breadcrumbList.value.map(item => item.name).join(' / '))

const canGoParent = computed(() => !!current_node.value?.id)

const isProcAnom = computed(() => numObjDlg.form.purpose === 'processAnomaly')
const curNumPurposeLabel = computed(() => {
  return numPurposeOpts.find(item => item.value === numObjDlg.form.purpose)?.label || '未选择'
})
const numObjReqLevelText = computed(() =>
  isProcAnom.value ? '零件' : '分系统、设备、组件'
)
const numObjReqText = computed(() =>
  isProcAnom.value ? '请选择零件' : '请选择分系统、设备或组件'
)
const numSelNodeId = computed(() => {
  const form = numObjDlg.form
  if (form.partId) return prefixedNodeId('part_template', form.partId)
  if (form.componentId) return prefixedNodeId('component', form.componentId)
  if (form.equipmentId) return prefixedNodeId('equipment', form.equipmentId)
  if (form.subsystemId) return prefixedNodeId('subsystem', form.subsystemId)
  return ''
})
const numSelObj = computed(() => {
  const node = findTreeNode(treeData.value, numSelNodeId.value)
  if (!node) return null
  return {
    name: node.name,
    typeText: numNodeTypeText(node)
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
const hasTextImportTemplate = computed(() => ['partProcess', 'partQuality', 'hierarchy'].includes(textObjDlg.form.taskType))
const textConfirmText = computed(() => '导入数据')
const textSelNodeId = computed(() => {
  if (isTextHierarchy.value) return ''
  if (isTextPartQuality.value) return prefixedNodeId('part_template', textObjDlg.form.partId)
  return prefixedNodeId('component', textObjDlg.form.componentId)
})
const textSelObj = computed(() => {
  const node = findTreeNode(treeData.value, textSelNodeId.value)
  if (!node) return null
  return {
    name: node.name,
    typeText: numNodeTypeText(node)
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
  if (isProcAnom.value) return type === 'part_template'
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
  if (isTextHierarchy.value) return false
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
function handleViewNodeDetail() {
  const node = context_menu.node
  closeContextMenu()
  if (!node) {
    ElMessage.warning('请先选择对象')
    return
  }
  nodeDetailDialog.node = node
  nodeDetailDialog.visible = true
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

function handleQualityMining() {
  const node = context_menu.node
  closeContextMenu()


  if (!node || !node.id) {
    ElMessage.warning('请先选择模块')
    return
  }

  ElMessage.warning('关键质量特性挖掘暂未开放')
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
    const data = res.data || {}
    partQualityDialog.tables = Array.isArray(data.tables) ? data.tables : []
  } catch {
    partQualityDialog.tables = []
    ElMessage.error('获取零件质量信息失败')
  } finally {
    partQualityDialog.loading = false
  }

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
      serial_number: payload.serial_number || '',
      manufacturer: payload.manufacturer || '',
      production_date: payload.production_date || '',
      status: payload.status || '',
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
    return !!form.partId
  }
  return !!(form.subsystemId || form.equipmentId || form.componentId)
}

function wait(ms) {
  return new Promise(resolve => window.setTimeout(resolve, ms))
}

function formatNodeDetailValue(value, key = '') {
  if (value === null || value === undefined || value === '') return '暂无数据'
  if (typeof value === 'boolean') return value ? '是' : '否'
  if (Array.isArray(value)) return value.length ? JSON.stringify(value) : '[]'
  if (typeof value === 'object') return JSON.stringify(value)
  if (key === 'terminal') return String(value) === 'true' ? '是' : '否'
  return String(value)
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
  numObjDlg.visible = false
  if (numFileInput.value) {
    numFileInput.value.value = ''
    numFileInput.value.click()
  }
}

function conTextObj() {
  if (isTextHierarchy.value) {
    // hierarchy import does not bind to a selected tree node.
  } else if (isTextPartQuality.value) {
    if (!textObjDlg.form.partId) {
      ElMessage.warning('请选择零件作为任务对象')
      return
    }
  } else if (!textObjDlg.form.componentId) {
    ElMessage.warning('请选择组件作为导入对象')
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
  const pattern = (isTextPartQuality.value || isTextHierarchy.value) ? /\.xlsx$/i : /\.(xlsx|xls)$/i
  const file = Array.from(event?.target?.files || []).find(item => pattern.test(item.name))
  if (event?.target) event.target.value = ''
  if (!file) {
    ElMessage.warning((isTextPartQuality.value || isTextHierarchy.value) ? '请选择 .xlsx Excel 文件' : '请选择 Excel 文件')
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
  if (!textObjDlg.form.componentId) {
    ElMessage.warning('请选择组件作为导入对象')
    return
  }

  const payload = new FormData()
  payload.append('componentId', textObjDlg.form.componentId)
  payload.append('file', file)

  textImporting.value = true
  try {
    const res = await uploadProcessTextData(payload)
    const data = res?.data || {}
    ElMessage.success('导入完成：零件 ' + (data.part_count || 0) + ' 个，新增实例 ' + (data.instance_count || 0) + ' 个，工序 ' + (data.process_count || 0) + ' 条')
    const keepNodeId = current_node.value?.id || ''
    await reloadTreeView(keepNodeId)
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.message || '导入文本数据失败')
  } finally {
    textImporting.value = false
  }
}

async function submitPartQualityData(file) {
  if (!textObjDlg.form.partId) {
    ElMessage.warning('请选择零件作为任务对象')
    return
  }

  const payload = new FormData()
  payload.append('file', file)

  textImporting.value = true
  try {
    const res = await uploadPartQualityData(payload)
    const data = res?.data || {}
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
    saveAs(new Blob([data]), '零件工序导入模板.xlsx')
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
  downloadPartProcessTemplate()
}

function dataUsageText(value) {
  const map = {
    FAULT_IDENTIFY: '服役周期',
    PROCESS_ANOMALY: '制造周期',
    COMMON: '常用'
  }
  return map[value] || value || '-'
}

function fileSizeText(size) {
  const value = Number(size || 0)
  if (!value) return '-'
  if (value < 1024) return value + ' B'
  if (value < 1024 * 1024) return (value / 1024).toFixed(1) + ' KB'
  return (value / 1024 / 1024).toFixed(1) + ' MB'
}

async function openDataFileDialog() {
  dataFileDialog.visible = true
  dataFileQuery.pageNum = 1
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
  } catch (error) {
    dataFileDialog.rows = []
    dataFileDialog.total = 0
    ElMessage.error(error?.response?.data?.msg || error?.message || '获取数据文件失败')
  } finally {
    dataFileDialog.loading = false
  }
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

onMounted(() => {
  loadModuleTree()
})

onBeforeUnmount(() => {
  stopNumProgTimer()
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



