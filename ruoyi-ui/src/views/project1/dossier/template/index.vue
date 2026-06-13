<template>
  <div class="app-container dossier-template-page">
    <div class="page-head">
      <div>
        <h2>卷宗模板管理</h2>
        <div class="subline">当前：卷宗模板管理</div>
      </div>
      <div class="actions">
        <el-button icon="Refresh" @click="getList">刷新</el-button>
        <el-button icon="Back" @click="goDossierInstance">返回卷宗实例管理</el-button>
        <el-button icon="CircleCheck" @click="handleCheck">检查配置</el-button>
      </div>
    </div>

    <div class="overview">
      <div class="overview-main">
        <div class="overview-title">
          <span>{{ detail.name || '单台份飞机综合卷宗模板' }}</span>
          <el-tag :type="statusType(detail.status)" size="small">{{ statusLabel(detail.status) }}</el-tag>
          <el-tag type="primary" size="small">{{ detail.templateVersion || 'V1.0' }}</el-tag>
        </div>
        <div class="overview-text">
          模板统一管理整机目录、BOM 节点下钻目录、各级目录展示方式、章节数据来源、检查规则和默认生成设置。
        </div>
      </div>
      <div class="metric"><strong>{{ aircraftChapterCount }}</strong><span>整机章节</span></div>
      <div class="metric"><strong>{{ drilldownLevelCount }}</strong><span>节点目录</span></div>
      <div class="metric"><strong>{{ displayConfigCount }}</strong><span>展示规则</span></div>
      <div class="metric"><strong>{{ detail.rules.length }}</strong><span>检查规则</span></div>
    </div>

    <div class="workspace">
      <section class="template-panel">
        <div class="panel-head">
          <span>模板列表</span>
          <el-button type="primary" plain icon="Plus" @click="handleAddTemplate">新建</el-button>
        </div>
        <div class="panel-body">
          <el-form :model="queryParams" label-width="0" @submit.prevent>
            <el-form-item>
              <el-input
                v-model="queryParams.name"
                placeholder="请输入模板名称"
                clearable
                @keyup.enter="handleQuery"
              >
                <template #append>
                  <el-button icon="Search" @click="handleQuery" />
                </template>
              </el-input>
            </el-form-item>
          </el-form>

          <div v-loading="loading" class="template-list">
            <div
              v-for="item in templateList"
              :key="item.id"
              class="template-card"
              :class="{ active: item.id === currentId }"
              @click="selectTemplate(item.id)"
            >
              <div class="template-card-head">
                <div class="template-title">
                  <strong>{{ item.name }}</strong>
                  <span>{{ item.templateCode }} / {{ item.templateVersion }}</span>
                </div>
                <el-tag :type="statusType(item.status)" size="small">{{ statusLabel(item.status) }}</el-tag>
              </div>
              <div class="template-tags">
                <el-tag type="primary" size="small">{{ objectTypeLabel(item.applicableObjectType) }}</el-tag>
                <el-tag type="primary" size="small">{{ templateTypeLabel(item.templateType) }}</el-tag>
                <el-tag v-if="item.isDefault === 1" type="warning" size="small">默认</el-tag>
              </div>
              <div class="template-meta">更新时间：{{ parseTime(item.updatedAt || item.createdAt) }}</div>
              <div class="card-actions" @click.stop>
                <el-button link type="primary" icon="Edit" @click="selectTemplate(item.id)">编辑</el-button>
                <el-button link type="primary" icon="CopyDocument" @click="handleCopy(item)">复制</el-button>
                <el-button link type="primary" icon="DocumentAdd" @click="handleNewVersion(item)">新版本</el-button>
                <el-button
                  link
                  type="primary"
                  icon="SwitchButton"
                  @click="handleStatus(item)"
                >{{ item.status === 'active' ? '停用' : '启用' }}</el-button>
              </div>
            </div>
            <el-empty v-if="!loading && templateList.length === 0" description="暂无模板" />
          </div>
        </div>
      </section>

      <section class="config-panel">
        <div class="panel-head">
          <span>模板配置台</span>
          <div class="actions">
            <el-tag type="success" size="small">{{ detail.status === 'archived' ? '只读版本' : '当前可编辑' }}</el-tag>
            <el-tag type="primary" size="small">整机综合卷宗</el-tag>
          </div>
        </div>

        <el-tabs v-model="activeTab" class="config-tabs">
          <el-tab-pane label="基本信息" name="basic">
            <el-form ref="templateRef" :model="detail" :rules="rules" label-width="96px">
              <el-row :gutter="14">
                <el-col :xs="24" :md="12">
                  <el-form-item label="模板编码" prop="templateCode">
                    <el-input v-model="detail.templateCode" maxlength="100" />
                  </el-form-item>
                </el-col>
                <el-col :xs="24" :md="12">
                  <el-form-item label="模板版本" prop="templateVersion">
                    <el-input v-model="detail.templateVersion" maxlength="64" />
                  </el-form-item>
                </el-col>
                <el-col :xs="24" :md="12">
                  <el-form-item label="模板名称" prop="name">
                    <el-input v-model="detail.name" />
                  </el-form-item>
                </el-col>
                <el-col :xs="24" :md="12">
                  <el-form-item label="模板状态" prop="status">
                    <el-select v-model="detail.status" style="width: 100%">
                      <el-option label="启用" value="active" />
                      <el-option label="停用" value="inactive" />
                      <el-option label="草稿" value="draft" />
                      <el-option label="历史" value="archived" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :xs="24" :md="12">
                  <el-form-item label="卷宗类型" prop="templateType">
                    <el-select v-model="detail.templateType" style="width: 100%">
                      <el-option label="综合卷宗" value="general" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :xs="24" :md="12">
                  <el-form-item label="适用对象" prop="applicableObjectType">
                    <el-select v-model="detail.applicableObjectType" style="width: 100%">
                      <el-option label="整机" value="aircraft" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :xs="24" :md="12">
                  <el-form-item label="默认模板">
                    <el-switch v-model="detail.isDefault" :active-value="1" :inactive-value="0" />
                  </el-form-item>
                </el-col>
                <el-col :xs="24" :md="12">
                  <el-form-item label="生效时间">
                    <el-date-picker
                      v-model="effectiveRange"
                      value-format="YYYY-MM-DD HH:mm:ss"
                      type="datetimerange"
                      range-separator="-"
                      start-placeholder="生效时间"
                      end-placeholder="失效时间"
                      style="width: 100%"
                      @change="syncEffectiveRange"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="模板说明">
                    <el-input v-model="detail.description" type="textarea" :rows="3" />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
            <div class="tab-actions">
              <el-button icon="CopyDocument" @click="handleCopy(detail)">复制模板</el-button>
              <el-button type="warning" plain icon="DocumentAdd" @click="handleNewVersion(detail)">生成新版本</el-button>
              <el-button type="danger" plain icon="Delete" @click="handleDeleteTemplate(detail)">删除草稿</el-button>
              <el-button type="primary" icon="Star" @click="handleSetDefault">设为默认</el-button>
              <el-button type="primary" icon="Check" @click="handleSave">保存模板</el-button>
            </div>
          </el-tab-pane>

          <el-tab-pane label="目录结构" name="chapters">
            <div class="tab-actions">
              <el-button type="success" plain icon="Plus" @click="appendSiblingChapter">新增章节</el-button>
              <el-button icon="FolderAdd" @click="appendChapter(selectedChapter)">新增子章节</el-button>
              <el-button icon="Top" @click="moveChapter(-1)" />
              <el-button icon="Bottom" @click="moveChapter(1)" />
              <el-button type="danger" plain icon="Delete" @click="removeChapter">删除章节</el-button>
            </div>
            <el-row :gutter="14">
              <el-col :xs="24" :lg="10">
                <div class="tree-box">
                  <div class="tree-tags">
                    <el-tag type="primary" size="small">卷宗目录</el-tag>
                  </div>
                  <el-tree
                    :data="chapterTree"
                    node-key="id"
                    default-expand-all
                    :props="{ label: 'chapterName', children: 'children' }"
                    highlight-current
                    @node-click="handleNodeClick"
                  >
                    <template #default="{ data }">
                      <span class="tree-node-label">
                        <span>{{ data.chapterName }}</span>
                        <el-tag v-if="data.attrs && data.attrs.objectLevel" size="small">
                          {{ objectLevelLabel(data.attrs.objectLevel) }}
                        </el-tag>
                      </span>
                    </template>
                  </el-tree>
                </div>
              </el-col>
              <el-col :xs="24" :lg="14">
                <el-form v-if="selectedChapter" label-width="96px" class="chapter-form">
                  <el-row :gutter="12">
                    <el-col :xs="24" :md="12">
                      <el-form-item label="目录名称">
                        <el-input v-model="selectedChapter.chapterName" />
                      </el-form-item>
                    </el-col>
                    <el-col :xs="24" :md="12">
                      <el-form-item label="目录编码">
                        <el-input v-model="selectedChapter.chapterCode" />
                      </el-form-item>
                    </el-col>
                    <el-col :xs="24" :md="12">
                      <el-form-item label="对象层级">
                        <el-select v-model="selectedChapter.attrs.objectLevel" style="width: 100%">
                          <el-option label="整机" value="aircraft" />
                          <el-option label="系统" value="system" />
                          <el-option label="子系统" value="subsystem" />
                          <el-option label="设备" value="equipment" />
                          <el-option label="组件" value="component" />
                          <el-option label="零件" value="part" />
                          <el-option label="目录分组" value="template_group" />
                        </el-select>
                      </el-form-item>
                    </el-col>
                    <el-col :xs="24" :md="12">
                      <el-form-item label="目录类型">
                        <el-select v-model="selectedChapter.nodeKind" style="width: 100%">
                          <el-option label="章节" value="chapter" />
                          <el-option label="分组" value="group" />
                          <el-option label="条目" value="item" />
                        </el-select>
                      </el-form-item>
                    </el-col>
                    <el-col :xs="24" :md="12">
                      <el-form-item label="是否必填">
                        <el-switch v-model="selectedChapter.requiredFlag" :active-value="1" :inactive-value="0" />
                      </el-form-item>
                    </el-col>
                    <el-col :xs="24" :md="12">
                      <el-form-item label="默认展开">
                        <el-switch v-model="selectedChapter.defaultExpand" :active-value="1" :inactive-value="0" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="24">
                      <el-form-item label="章节说明">
                        <el-input v-model="selectedChapter.chapterDesc" type="textarea" :rows="3" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="24">
                      <div class="chapter-source-panel">
                        <div class="box-title">
                          <span>数据来源</span>
                          <div class="actions">
                            <el-tag type="primary" size="small">{{ selectedChapterSources.length }} 条</el-tag>
                            <el-button type="primary" plain icon="Plus" @click="openSource()">添加来源</el-button>
                          </div>
                        </div>
                        <el-table v-if="selectedChapterSources.length > 0" :data="selectedChapterSources" border size="small">
                          <el-table-column prop="sourceName" label="来源内容" min-width="150" show-overflow-tooltip />
                          <el-table-column label="业务域" width="95">
                            <template #default="scope">{{ sourceDomainLabel(scope.row.sourceSystem) }}</template>
                          </el-table-column>
                          <el-table-column prop="sourceTable" label="数据库表" min-width="170" show-overflow-tooltip />
                          <el-table-column label="选择字段" min-width="170" show-overflow-tooltip>
                            <template #default="scope">{{ sourceSelectedFieldsLabel(scope.row) }}</template>
                          </el-table-column>
                          <el-table-column label="关联条件" min-width="170" show-overflow-tooltip>
                            <template #default="scope">{{ conditionLabel(scope.row.joinConditionJson) }}</template>
                          </el-table-column>
                          <el-table-column label="过滤条件" min-width="150" show-overflow-tooltip>
                            <template #default="scope">{{ conditionLabel(scope.row.filterConditionJson) }}</template>
                          </el-table-column>
                          <el-table-column label="必选" width="70" align="center">
                            <template #default="scope">
                              <el-tag :type="scope.row.requiredFlag === 1 ? 'success' : 'info'" size="small">
                                {{ scope.row.requiredFlag === 1 ? '是' : '否' }}
                              </el-tag>
                            </template>
                          </el-table-column>
                          <el-table-column label="启用" width="70" align="center">
                            <template #default="scope">
                              <el-switch v-model="scope.row.enabledFlag" :active-value="1" :inactive-value="0" />
                            </template>
                          </el-table-column>
                          <el-table-column label="操作" width="130" align="center">
                            <template #default="scope">
                              <el-button link type="primary" icon="Edit" @click="openSource(scope.row)">编辑</el-button>
                              <el-button link type="primary" icon="Delete" @click="removeSource(scope.row)">删除</el-button>
                            </template>
                          </el-table-column>
                        </el-table>
                        <el-empty
                          v-else
                          description="当前章节暂无数据来源"
                          :image-size="72"
                        />
                      </div>
                    </el-col>
                  </el-row>
                </el-form>
              </el-col>
            </el-row>
          </el-tab-pane>

          <el-tab-pane label="展示配置" name="display">
            <el-row :gutter="14">
              <el-col :xs="24" :lg="12">
                <div class="edit-box" v-if="selectedChapter">
                  <div class="box-title">
                    <span>当前配置</span>
                    <el-tag type="primary" size="small">{{ objectLevelLabel(selectedChapter.attrs.objectLevel) }} / {{ selectedChapter.chapterName }}</el-tag>
                  </div>
                  <el-form label-width="96px">
                    <el-form-item label="展示形式">
                      <el-select v-model="selectedChapter.attrs.displayType" style="width: 100%">
                        <el-option label="概要卡片 + 明细表" value="summary_table" />
                        <el-option label="结构树 + 明细表" value="tree_table" />
                        <el-option label="时间线 + 附件清单" value="timeline_files" />
                        <el-option label="件号卡片 + 参数表" value="part_card_param_table" />
                        <el-option label="文件清单" value="file_list" />
                      </el-select>
                    </el-form-item>
                    <el-form-item label="默认排序">
                      <el-select v-model="selectedChapter.attrs.sortMode" style="width: 100%">
                        <el-option label="业务顺序" value="business_order" />
                        <el-option label="结构顺序" value="structure_order" />
                        <el-option label="时间顺序" value="time_asc" />
                        <el-option label="时间倒序" value="time_desc" />
                        <el-option label="重要性优先" value="priority" />
                      </el-select>
                    </el-form-item>
                    <el-form-item label="关键字段">
                      <el-select
                        v-model="selectedChapter.attrs.primaryFields"
                        multiple
                        filterable
                        allow-create
                        default-first-option
                        style="width: 100%"
                      >
                        <el-option v-for="field in fieldOptions" :key="field" :label="field" :value="field" />
                      </el-select>
                    </el-form-item>
                    <el-form-item label="展示块">
                      <el-checkbox-group v-model="selectedChapter.attrs.blocks">
                        <el-checkbox label="summary">概要</el-checkbox>
                        <el-checkbox label="relation">关系</el-checkbox>
                        <el-checkbox label="details">明细</el-checkbox>
                        <el-checkbox label="params">参数</el-checkbox>
                        <el-checkbox label="timeline">时间线</el-checkbox>
                        <el-checkbox label="documents">文件</el-checkbox>
                        <el-checkbox label="issues">异常提示</el-checkbox>
                      </el-checkbox-group>
                    </el-form-item>
                    <el-form-item label="缺失提示">
                      <el-switch v-model="selectedChapter.attrs.showMissingTips" />
                    </el-form-item>
                  </el-form>
                </div>
              </el-col>
              <el-col :xs="24" :lg="12">
                <div class="edit-box">
                  <div class="box-title">
                    <span>展示块顺序</span>
                    <el-tag type="warning" size="small">按业务阅读顺序</el-tag>
                  </div>
                  <ol class="order-list">
                    <li>概要卡片：当前对象身份、状态和关键指标。</li>
                    <li>关系说明：整机结构中的位置和上下级关系。</li>
                    <li>核心明细表：主要业务记录和关键字段。</li>
                    <li>文件清单：图纸、报告、证明和附件。</li>
                    <li>异常提示：缺失、未关闭或不一致的数据。</li>
                  </ol>
                </div>
              </el-col>
            </el-row>

            <el-table :data="displayRows" border class="mt12">
              <el-table-column label="对象层级" width="100">
                <template #default="scope">{{ objectLevelLabel(scope.row.attrs.objectLevel) }}</template>
              </el-table-column>
              <el-table-column prop="chapterName" label="目录" width="150" show-overflow-tooltip />
              <el-table-column label="展示形式" width="180">
                <template #default="scope">{{ displayTypeLabel(scope.row.attrs.displayType) }}</template>
              </el-table-column>
              <el-table-column label="展示重点" show-overflow-tooltip>
                <template #default="scope">{{ (scope.row.attrs.primaryFields || []).join('、') }}</template>
              </el-table-column>
              <el-table-column label="默认排序" width="120">
                <template #default="scope">{{ sortModeLabel(scope.row.attrs.sortMode) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="90" align="center">
                <template #default="scope">
                  <el-button link type="primary" icon="Edit" @click="selectChapter(scope.row.id)">编辑</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="检查规则" name="rules">
            <el-form :model="ruleQuery" :inline="true">
              <el-form-item label="对象层级">
                <el-select v-model="ruleQuery.applyObjectType" clearable placeholder="全部" style="width: 130px">
                  <el-option label="整机" value="aircraft" />
                  <el-option label="系统" value="system" />
                  <el-option label="子系统" value="subsystem" />
                  <el-option label="设备" value="equipment" />
                  <el-option label="组件" value="component" />
                  <el-option label="零件" value="part" />
                </el-select>
              </el-form-item>
              <el-form-item label="规则类型">
                <el-select v-model="ruleQuery.ruleType" clearable placeholder="全部" style="width: 130px">
                  <el-option label="必填检查" value="required" />
                  <el-option label="关联检查" value="relation" />
                  <el-option label="状态检查" value="expression" />
                  <el-option label="文件检查" value="business" />
                </el-select>
              </el-form-item>
              <el-form-item label="处理方式">
                <el-select v-model="ruleQuery.severity" clearable placeholder="全部" style="width: 130px">
                  <el-option label="阻止生成" value="blocker" />
                  <el-option label="错误" value="error" />
                  <el-option label="提醒风险" value="warning" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" icon="Plus" @click="openRule()">新增规则</el-button>
                <el-button icon="DocumentAdd" @click="supplementRule">补充规则</el-button>
              </el-form-item>
            </el-form>

            <el-table :data="filteredRules" border>
              <el-table-column prop="ruleName" label="规则名称" width="160" show-overflow-tooltip />
              <el-table-column label="对象层级" width="90">
                <template #default="scope">{{ objectLevelLabel(scope.row.applyObjectType) }}</template>
              </el-table-column>
              <el-table-column label="所属目录" width="130" show-overflow-tooltip>
                <template #default="scope">{{ chapterLabel(scope.row.chapterId) || '模板级' }}</template>
              </el-table-column>
              <el-table-column label="规则类型" width="100">
                <template #default="scope">{{ ruleTypeLabel(scope.row.ruleType) }}</template>
              </el-table-column>
              <el-table-column prop="errorMessage" label="检查内容" min-width="220" show-overflow-tooltip />
              <el-table-column label="处理方式" width="100" align="center">
                <template #default="scope">
                  <el-tag :type="severityType(scope.row.severity)" size="small">{{ severityLabel(scope.row.severity) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="80" align="center">
                <template #default="scope">
                  <el-switch v-model="scope.row.enabledFlag" :active-value="1" :inactive-value="0" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="170" align="center">
                <template #default="scope">
                  <el-button link type="primary" icon="View" @click="viewRule(scope.row)">查看</el-button>
                  <el-button link type="primary" icon="Edit" @click="openRule(scope.row)">编辑</el-button>
                  <el-button link type="primary" icon="Delete" @click="removeRule(scope.row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="生成设置" name="params">
            <div class="tab-actions">
              <el-button type="primary" icon="Plus" @click="addParam">新增参数</el-button>
              <el-button icon="RefreshLeft" @click="restoreParams">恢复默认</el-button>
            </div>
            <el-table :data="detail.params" border>
              <el-table-column label="参数名称" min-width="170">
                <template #default="scope">
                  <el-input v-model="scope.row.paramName" />
                </template>
              </el-table-column>
              <el-table-column label="参数编码" min-width="170">
                <template #default="scope">
                  <el-input v-model="scope.row.paramCode" />
                </template>
              </el-table-column>
              <el-table-column label="类型" width="120">
                <template #default="scope">
                  <el-select v-model="scope.row.paramType">
                    <el-option label="文本" value="string" />
                    <el-option label="数字" value="number" />
                    <el-option label="开关" value="boolean" />
                    <el-option label="枚举" value="enum" />
                    <el-option label="JSON" value="json" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="默认值" min-width="220">
                <template #default="scope">
                  <el-input v-model="scope.row.defaultValue" />
                </template>
              </el-table-column>
              <el-table-column label="可编辑" width="90" align="center">
                <template #default="scope">
                  <el-switch v-model="scope.row.editableFlag" :active-value="1" :inactive-value="0" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80" align="center">
                <template #default="scope">
                  <el-button link type="primary" icon="Delete" @click="removeParam(scope.row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </section>
    </div>

    <el-dialog :title="sourceTitle" v-model="sourceOpen" width="980px" append-to-body>
      <el-form :model="sourceForm" label-width="98px">
        <el-row :gutter="12">
          <el-col :xs="24" :md="12">
            <el-form-item label="所属目录">
              <el-select v-model="sourceForm.chapterId" filterable style="width: 100%" @change="handleSourceChapterChange">
                <el-option
                  v-for="chapter in detail.chapters"
                  :key="chapter.id"
                  :label="chapter.chapterName"
                  :value="chapter.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="业务域">
              <el-input
                :model-value="sourceDomainLabel(sourceForm.sourceSystem)"
                disabled
                placeholder="选择数据库表后自动带出"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="来源编码">
              <el-input v-model="sourceForm.sourceCode" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="来源名称">
              <el-input v-model="sourceForm.sourceName" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="数据库表">
              <el-select
                v-model="sourceForm.sourceTable"
                filterable
                clearable
                style="width: 100%"
                @change="handleSourceTableChange"
              >
                <el-option-group
                  v-for="group in sourceTableGroups"
                  :key="group.label"
                  :label="group.label"
                >
                  <el-option
                    v-for="table in group.options"
                    :key="table.tableName"
                    :label="sourceTableOptionLabel(table)"
                    :value="table.tableName"
                  />
                </el-option-group>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="生命周期">
              <el-select
                v-model="sourceForm.lifecycleStage"
                filterable
                allow-create
                default-first-option
                style="width: 100%"
              >
                <el-option v-for="stage in lifecycleStageOptions" :key="stage.value" :label="stage.label" :value="stage.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="适用对象">
              <el-select v-model="sourceForm.applyObjectType" style="width: 100%">
                <el-option label="整机" value="aircraft" />
                <el-option label="系统" value="system" />
                <el-option label="子系统" value="subsystem" />
                <el-option label="设备" value="equipment" />
                <el-option label="组件" value="component" />
                <el-option label="零件" value="part" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="记录类型">
              <el-input v-model="sourceForm.sourceRecordType" placeholder="如 profile / event / file" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="必选来源">
              <el-switch v-model="sourceForm.requiredFlag" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="启用状态">
              <el-switch v-model="sourceForm.enabledFlag" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="选择字段">
              <el-select
                v-model="sourceSelectedFields"
                multiple
                filterable
                allow-create
                default-first-option
                collapse-tags
                collapse-tags-tooltip
                :loading="sourceColumnLoading"
                style="width: 100%"
                placeholder="选择该来源在章节中重点展示或追溯的字段"
              >
                <el-option
                  v-for="column in sourceColumnOptions"
                  :key="column.columnName"
                  :label="columnOptionLabel(column)"
                  :value="column.columnName"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <div class="condition-builder">
              <div class="condition-head">
                <span>关联条件</span>
                <el-button link type="primary" icon="Plus" @click="addJoinCondition">添加关联</el-button>
              </div>
              <el-table :data="sourceJoinRows" border size="small">
                <el-table-column label="来源字段" min-width="220">
                  <template #default="scope">
                    <el-select
                      v-model="scope.row.field"
                      filterable
                      allow-create
                      default-first-option
                      style="width: 100%"
                    >
                      <el-option
                        v-for="column in sourceColumnOptions"
                        :key="column.columnName"
                        :label="columnOptionLabel(column)"
                        :value="column.columnName"
                      />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="匹配上下文" min-width="220">
                  <template #default="scope">
                    <el-select
                      v-model="scope.row.value"
                      filterable
                      allow-create
                      default-first-option
                      style="width: 100%"
                    >
                      <el-option
                        v-for="field in contextFieldOptions"
                        :key="field.value"
                        :label="`${field.label} ${field.value}`"
                        :value="field.value"
                      />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="说明" min-width="180">
                  <template #default="scope">
                    <el-input v-model="scope.row.remark" placeholder="可选" />
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="80" align="center">
                  <template #default="scope">
                    <el-button link type="primary" icon="Delete" @click="removeJoinCondition(scope.$index)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-col>
          <el-col :span="24">
            <div class="condition-builder">
              <div class="condition-head">
                <span>过滤条件</span>
                <el-button link type="primary" icon="Plus" @click="addFilterCondition">添加过滤</el-button>
              </div>
              <el-table :data="sourceFilterRows" border size="small">
                <el-table-column label="来源字段" min-width="220">
                  <template #default="scope">
                    <el-select
                      v-model="scope.row.field"
                      filterable
                      allow-create
                      default-first-option
                      style="width: 100%"
                    >
                      <el-option
                        v-for="column in sourceColumnOptions"
                        :key="column.columnName"
                        :label="columnOptionLabel(column)"
                        :value="column.columnName"
                      />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="等于" min-width="260">
                  <template #default="scope">
                    <el-input v-model="scope.row.value" placeholder="如 1、active、${partNumber}" />
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="80" align="center">
                  <template #default="scope">
                    <el-button link type="primary" icon="Delete" @click="removeFilterCondition(scope.$index)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-col>
          <el-col :span="24">
            <el-form-item label="来源说明">
              <el-input v-model="sourceForm.sourceDesc" type="textarea" :rows="3" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="sourceOpen = false">取 消</el-button>
          <el-button type="primary" @click="submitSource">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog :title="ruleTitle" v-model="ruleOpen" width="780px" append-to-body>
      <el-form :model="ruleForm" label-width="98px">
        <el-row :gutter="12">
          <el-col :xs="24" :md="12">
            <el-form-item label="规则名称">
              <el-input v-model="ruleForm.ruleName" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="规则编码">
              <el-input v-model="ruleForm.ruleCode" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="所属目录">
              <el-select v-model="ruleForm.chapterId" clearable filterable style="width: 100%">
                <el-option
                  v-for="chapter in detail.chapters"
                  :key="chapter.id"
                  :label="chapter.chapterName"
                  :value="chapter.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="对象层级">
              <el-select v-model="ruleForm.applyObjectType" style="width: 100%">
                <el-option label="整机" value="aircraft" />
                <el-option label="系统" value="system" />
                <el-option label="子系统" value="subsystem" />
                <el-option label="设备" value="equipment" />
                <el-option label="组件" value="component" />
                <el-option label="零件" value="part" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="规则类型">
              <el-select v-model="ruleForm.ruleType" style="width: 100%">
                <el-option label="必填检查" value="required" />
                <el-option label="关联检查" value="relation" />
                <el-option label="表达式检查" value="expression" />
                <el-option label="枚举检查" value="enum" />
                <el-option label="业务检查" value="business" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="处理方式">
              <el-select v-model="ruleForm.severity" style="width: 100%">
                <el-option label="阻止生成" value="blocker" />
                <el-option label="错误" value="error" />
                <el-option label="提醒风险" value="warning" />
                <el-option label="提示" value="info" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="目标表">
              <el-input v-model="ruleForm.targetTable" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="目标字段">
              <el-input v-model="ruleForm.targetField" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="检查表达式">
              <el-input v-model="ruleForm.ruleExpression" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="错误提示">
              <el-input v-model="ruleForm.errorMessage" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="整改建议">
              <el-input v-model="ruleForm.remediationHint" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="ruleOpen = false">取 消</el-button>
          <el-button type="primary" @click="submitRule">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="模板配置检查" v-model="checkOpen" width="760px" append-to-body>
      <div v-if="checkResult" class="check-summary">
        <el-alert
          :title="`错误 ${checkResult.errorCount || 0} 项，风险 ${checkResult.warningCount || 0} 项`"
          :type="checkResult.errorCount > 0 ? 'error' : 'success'"
          show-icon
          :closable="false"
        />
        <el-table :data="checkResult.issues || []" border class="mt12">
          <el-table-column label="级别" width="90">
            <template #default="scope">
              <el-tag :type="scope.row.severity === 'error' ? 'danger' : 'warning'" size="small">
                {{ scope.row.severity === 'error' ? '错误' : '风险' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="subject" label="对象" width="140" show-overflow-tooltip />
          <el-table-column prop="message" label="问题" min-width="220" show-overflow-tooltip />
          <el-table-column prop="suggestion" label="建议" min-width="220" show-overflow-tooltip />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="DossierTemplate">
import { useRouter } from 'vue-router'
import {
  addTemplate,
  checkTemplate,
  copyTemplate,
  createTemplateVersion,
  delTemplate,
  getSourceMetadata,
  getTemplate,
  listTemplate,
  setDefaultTemplate,
  updateTemplate,
  updateTemplateStatus
} from '@/api/project1/dossier/template'

const router = useRouter()
const { proxy } = getCurrentInstance()

const loading = ref(false)
const templateList = ref([])
const currentId = ref(undefined)
const activeTab = ref('basic')
const effectiveRange = ref([])
const detail = ref(emptyTemplate())
const selectedChapterId = ref(undefined)
const checkOpen = ref(false)
const checkResult = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 50,
  name: undefined
})

const ruleQuery = reactive({
  applyObjectType: undefined,
  ruleType: undefined,
  severity: undefined
})

const rules = {
  templateCode: [{ required: true, message: '模板编码不能为空', trigger: 'blur' }],
  templateVersion: [{ required: true, message: '模板版本不能为空', trigger: 'blur' }],
  name: [{ required: true, message: '模板名称不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '模板状态不能为空', trigger: 'change' }],
  templateType: [{ required: true, message: '卷宗类型不能为空', trigger: 'change' }],
  applicableObjectType: [{ required: true, message: '适用对象不能为空', trigger: 'change' }]
}

function goDossierInstance() {
  router.push('/dossier/manage/instance')
}

const sourceOpen = ref(false)
const sourceTitle = ref('')
const sourceForm = ref({})
const sourceTables = ref([])
const sourceColumns = ref([])
const contextFieldOptions = ref([])
const sourceColumnLoading = ref(false)
const sourceSelectedFields = ref([])
const sourceJoinRows = ref([])
const sourceFilterRows = ref([])
const ruleOpen = ref(false)
const ruleTitle = ref('')
const ruleForm = ref({})

const lifecycleStageOptions = [
  { label: '卷宗', value: 'DOSSIER' },
  { label: '基础信息', value: 'PROFILE' },
  { label: '设计', value: 'DESIGN' },
  { label: '构型', value: 'CONFIGURATION' },
  { label: '制造', value: 'MANUFACTURING' },
  { label: '检验', value: 'INSPECTION' },
  { label: '装机/拆换', value: 'INSTALLATION' },
  { label: '服役', value: 'SERVICE' },
  { label: '维修', value: 'MAINTENANCE' },
  { label: '故障', value: 'FAULT' },
  { label: '技术状态', value: 'TECHNICAL_STATUS' },
  { label: '接口', value: 'INTERFACE' }
]

const fieldOptions = [
  'aircraft_type',
  'tail_number',
  'msn',
  'part_number',
  'part_name',
  'serial_number',
  'material_spec',
  'is_critical',
  'design_pressure_mpa',
  'proof_pressure_mpa',
  'route_code',
  'shop_order',
  'inspection_result',
  'install_date',
  'fault_status',
  'file_storage_key'
]

const aircraftChapterCount = computed(() =>
  detail.value.chapters.filter(item => item.attrs?.objectLevel === 'aircraft' && item.parentId).length
)
const drilldownLevelCount = computed(() => {
  const levels = new Set(detail.value.chapters.map(item => item.attrs?.objectLevel).filter(item => ['system', 'subsystem', 'equipment', 'component', 'part'].includes(item)))
  return levels.size
})
const displayConfigCount = computed(() => displayRows.value.length)
const chapterTree = computed(() => buildTree(detail.value.chapters))
const selectedChapter = computed(() => detail.value.chapters.find(item => item.id === selectedChapterId.value))
const displayRows = computed(() => detail.value.chapters.filter(item => item.attrs?.displayType))
const selectedChapterSources = computed(() =>
  detail.value.dataSources
    .filter(item => item.chapterId === selectedChapterId.value)
    .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
)
const activeSourceChapter = computed(() =>
  detail.value.chapters.find(item => item.id === sourceForm.value.chapterId) || selectedChapter.value
)
const sourceTableGroups = computed(() => buildSourceTableGroups(sourceTables.value, activeSourceChapter.value))
const sourceColumnOptions = computed(() => sourceColumns.value)
const filteredRules = computed(() => {
  return detail.value.rules.filter(item => {
    const objectMatch = !ruleQuery.applyObjectType || item.applyObjectType === ruleQuery.applyObjectType
    const typeMatch = !ruleQuery.ruleType || item.ruleType === ruleQuery.ruleType
    const severityMatch = !ruleQuery.severity || item.severity === ruleQuery.severity
    return objectMatch && typeMatch && severityMatch
  })
})

function emptyTemplate() {
  const id = uuid()
  return {
    id: id,
    templateCode: 'TPL-AIRCRAFT-COMPOSITE',
    templateVersion: 'V1.0',
    name: '单台份飞机综合卷宗模板',
    description: '用于生成单架飞机的综合卷宗。系统、子系统、设备、组件、零件不单独生成卷宗，作为整机卷宗内的下钻目录展示。',
    templateType: 'general',
    applicableObjectType: 'aircraft',
    chapterTreeJson: '{}',
    validationRulesJson: '{}',
    defaultGeneratorParamsJson: '{}',
    status: 'draft',
    isDefault: 0,
    effectiveFrom: undefined,
    effectiveTo: undefined,
    chapters: [],
    dataSources: [],
    rules: [],
    params: defaultParams(id)
  }
}

function getList() {
  loading.value = true
  listTemplate(queryParams).then(response => {
    templateList.value = response.rows || []
    loading.value = false
    if (!currentId.value && templateList.value.length > 0) {
      selectTemplate(templateList.value[0].id)
    }
  }).catch(() => {
    loading.value = false
  })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function selectTemplate(id) {
  currentId.value = id
  getTemplate(id).then(response => {
    detail.value = normalizeTemplate(response.data)
    selectedChapterId.value = detail.value.chapters[0]?.id
    effectiveRange.value = detail.value.effectiveFrom || detail.value.effectiveTo
      ? [detail.value.effectiveFrom, detail.value.effectiveTo]
      : []
  })
}

function handleAddTemplate() {
  detail.value = emptyTemplate()
  currentId.value = detail.value.id
  selectedChapterId.value = undefined
  activeTab.value = 'basic'
}

function handleSave() {
  proxy.$refs.templateRef?.validate(valid => {
    if (!valid) {
      activeTab.value = 'basic'
      return
    }
    const payload = buildPayload()
    const request = templateList.value.some(item => item.id === payload.id) ? updateTemplate : addTemplate
    request(payload).then(() => {
      proxy.$modal.msgSuccess('保存成功')
      getList()
      selectTemplate(payload.id)
    })
  })
}

function handleCopy(row) {
  if (!row.id) {
    proxy.$modal.msgWarning('请先保存模板')
    return
  }
  copyTemplate(row.id).then(response => {
    proxy.$modal.msgSuccess('复制成功')
    getList()
    selectTemplate(response.data.id)
  })
}

function handleNewVersion(row) {
  if (!row.id) {
    proxy.$modal.msgWarning('请先保存模板')
    return
  }
  createTemplateVersion(row.id).then(response => {
    proxy.$modal.msgSuccess('新版本已生成')
    getList()
    selectTemplate(response.data.id)
  })
}

function handleSetDefault() {
  if (!detail.value.id) {
    proxy.$modal.msgWarning('请先保存模板')
    return
  }
  setDefaultTemplate(detail.value.id).then(() => {
    detail.value.isDefault = 1
    proxy.$modal.msgSuccess('已设置默认模板')
    getList()
  })
}

function handleStatus(row) {
  const nextStatus = row.status === 'active' ? 'inactive' : 'active'
  updateTemplateStatus(row.id, nextStatus).then(() => {
    proxy.$modal.msgSuccess('状态已更新')
    getList()
    if (row.id === detail.value.id) {
      detail.value.status = nextStatus
    }
  })
}

function handleDeleteTemplate(row) {
  if (!row.id) {
    return
  }
  proxy.$modal.confirm('确认删除该模板？').then(() => {
    return delTemplate(row.id)
  }).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    currentId.value = undefined
    detail.value = emptyTemplate()
    getList()
  })
}

function handleCheck() {
  if (!detail.value.id) {
    proxy.$modal.msgWarning('请先保存模板')
    return
  }
  checkTemplate(detail.value.id).then(response => {
    checkResult.value = response.data
    checkOpen.value = true
  })
}

function loadSourceMetadata(tableName) {
  sourceColumnLoading.value = !!tableName
  return getSourceMetadata(tableName).then(response => {
    const data = response.data || {}
    sourceTables.value = data.tables || sourceTables.value
    contextFieldOptions.value = data.contextFields || contextFieldOptions.value
    if (tableName) {
      sourceColumns.value = data.columns || []
    }
  }).catch(() => {
    if (tableName) {
      sourceColumns.value = []
    }
  }).finally(() => {
    sourceColumnLoading.value = false
  })
}

function loadSourceColumns(tableName) {
  if (!tableName) {
    sourceColumns.value = []
    return Promise.resolve()
  }
  return loadSourceMetadata(tableName)
}

function handleNodeClick(data) {
  selectedChapterId.value = data.id
}

function selectChapter(id) {
  selectedChapterId.value = id
  activeTab.value = 'display'
}

function appendSiblingChapter() {
  const current = selectedChapter.value
  if (!current) {
    proxy.$modal.msgWarning('请先选择一个章节')
    return
  }
  const parent = detail.value.chapters.find(item => item.id === current.parentId)
  const siblings = siblingChapters(current.parentId)
  const index = siblings.findIndex(item => item.id === current.id)
  const chapterName = '新章节'
  const chapter = createChapter({
    parent,
    parentId: current.parentId || null,
    chapterName,
    chapterLevel: current.chapterLevel || (parent ? (parent.chapterLevel || 1) + 1 : 1),
    sortOrder: sortOrderAfter(siblings, index),
    objectLevel: current.attrs?.objectLevel || parent?.attrs?.objectLevel || 'aircraft'
  })
  detail.value.chapters.push(chapter)
  selectedChapterId.value = chapter.id
}

function appendChapter(parent) {
  const baseLevel = parent?.attrs?.objectLevel || 'aircraft'
  const chapterName = parent ? '新子章节' : '新章节'
  const chapter = createChapter({
    parent,
    parentId: parent?.id || null,
    chapterName,
    chapterLevel: parent ? (parent.chapterLevel || 1) + 1 : 1,
    sortOrder: nextSortOrder(siblingChapters(parent?.id)),
    objectLevel: baseLevel
  })
  detail.value.chapters.push(chapter)
  selectedChapterId.value = chapter.id
}

function createChapter({ parent, parentId, chapterName, chapterLevel, sortOrder, objectLevel }) {
  return {
    id: uuid(),
    templateId: detail.value.id,
    parentId,
    chapterCode: 'CH-' + Date.now(),
    chapterName,
    chapterLevel,
    chapterPath: buildChapterPath(parent, chapterName),
    nodeKind: 'chapter',
    sortOrder,
    requiredFlag: 0,
    enabledFlag: 1,
    defaultExpand: 0,
    completenessRequirement: 'normal',
    chapterDesc: undefined,
    attrs: {
      objectLevel,
      displayType: 'summary_table',
      sortMode: 'business_order',
      primaryFields: [],
      blocks: ['summary', 'details', 'documents'],
      showMissingTips: true
    }
  }
}

function removeChapter() {
  if (!selectedChapter.value) {
    return
  }
  proxy.$modal.confirm('确认删除当前章节及其子章节？').then(() => {
    const ids = collectChildIds(selectedChapter.value.id)
    detail.value.chapters = detail.value.chapters.filter(item => !ids.includes(item.id))
    detail.value.dataSources = detail.value.dataSources.filter(item => !ids.includes(item.chapterId))
    detail.value.rules = detail.value.rules.filter(item => !ids.includes(item.chapterId))
    detail.value.params = detail.value.params.filter(item => !ids.includes(item.chapterId))
    selectedChapterId.value = detail.value.chapters[0]?.id
  })
}

function moveChapter(step) {
  const chapter = selectedChapter.value
  if (!chapter) {
    return
  }
  const siblings = siblingChapters(chapter.parentId)
  const index = siblings.findIndex(item => item.id === chapter.id)
  const targetIndex = index + step
  if (targetIndex < 0 || targetIndex >= siblings.length) {
    return
  }
  const currentSort = siblings[index].sortOrder
  siblings[index].sortOrder = siblings[targetIndex].sortOrder
  siblings[targetIndex].sortOrder = currentSort
}

function openSource(row) {
  sourceTitle.value = row ? '编辑数据来源' : '添加数据来源'
  sourceForm.value = row ? { ...row } : {
    id: uuid(),
    templateId: detail.value.id,
    chapterId: selectedChapterId.value,
    sourceCode: 'SRC-' + Date.now(),
    sourceSystem: 'DOSSIER',
    sourceTable: '',
    sourceName: '',
    sourceDesc: '',
    lifecycleStage: 'DOSSIER',
    applyObjectType: selectedChapter.value?.attrs?.objectLevel || 'aircraft',
    supplyModeScope: 'all',
    keyPartScope: 'all',
    requiredFlag: 0,
    enabledFlag: 1,
    sortOrder: selectedChapterSources.value.length + 1,
    joinConditionJson: '{}',
    filterConditionJson: '{}',
    attrsJson: '{}'
  }
  const attrs = parseJson(sourceForm.value.attrsJson, {})
  sourceSelectedFields.value = Array.isArray(attrs.selectedFields) ? [...attrs.selectedFields] : []
  sourceJoinRows.value = conditionRowsFromJson(sourceForm.value.joinConditionJson, attrs.joinRemarks || {})
  sourceFilterRows.value = conditionRowsFromJson(sourceForm.value.filterConditionJson, attrs.filterRemarks || {})
  sourceColumns.value = []
  loadSourceColumns(sourceForm.value.sourceTable)
  sourceOpen.value = true
}

function handleSourceChapterChange(chapterId) {
  const chapter = detail.value.chapters.find(item => item.id === chapterId)
  const objectLevel = chapter?.attrs?.objectLevel
  if (objectLevel && objectLevel !== 'template_group') {
    sourceForm.value.applyObjectType = objectLevel
  }
}

function handleSourceTableChange(tableName) {
  const table = sourceTables.value.find(item => item.tableName === tableName)
  if (table) {
    sourceForm.value.sourceSystem = table.sourceSystem || sourceForm.value.sourceSystem
    if (!sourceForm.value.sourceName) {
      sourceForm.value.sourceName = table.tableComment || table.tableName
    }
  }
  loadSourceColumns(tableName)
}

function submitSource() {
  if (!sourceForm.value.chapterId) {
    proxy.$modal.msgWarning('请选择所属章节')
    return
  }
  if (!sourceForm.value.sourceTable) {
    proxy.$modal.msgWarning('请选择数据库表')
    return
  }
  if (!sourceForm.value.sourceName) {
    sourceForm.value.sourceName = sourceForm.value.sourceTable
  }
  const attrs = parseJson(sourceForm.value.attrsJson, {})
  attrs.selectedFields = [...sourceSelectedFields.value]
  attrs.joinRemarks = remarksFromRows(sourceJoinRows.value)
  attrs.filterRemarks = remarksFromRows(sourceFilterRows.value)
  const next = {
    ...sourceForm.value,
    joinConditionJson: stringifyJson(conditionObjectFromRows(sourceJoinRows.value)),
    filterConditionJson: stringifyJson(conditionObjectFromRows(sourceFilterRows.value)),
    attrsJson: stringifyJson(attrs)
  }
  const index = detail.value.dataSources.findIndex(item => item.id === sourceForm.value.id)
  if (index > -1) {
    detail.value.dataSources.splice(index, 1, next)
  } else {
    detail.value.dataSources.push(next)
  }
  sourceOpen.value = false
}

function removeSource(row) {
  detail.value.dataSources = detail.value.dataSources.filter(item => item.id !== row.id)
}

function addJoinCondition() {
  sourceJoinRows.value.push({ field: '', value: '', remark: '' })
}

function removeJoinCondition(index) {
  sourceJoinRows.value.splice(index, 1)
}

function addFilterCondition() {
  sourceFilterRows.value.push({ field: '', value: '', remark: '' })
}

function removeFilterCondition(index) {
  sourceFilterRows.value.splice(index, 1)
}

function openRule(row) {
  ruleTitle.value = row ? '编辑检查规则' : '新增检查规则'
  ruleForm.value = row ? { ...row } : {
    id: uuid(),
    templateId: detail.value.id,
    chapterId: selectedChapterId.value,
    ruleCode: 'RULE-' + Date.now(),
    ruleName: '',
    ruleType: 'required',
    targetTable: '',
    targetField: '',
    targetPath: '',
    ruleExpression: '',
    ruleExpressionJson: '{}',
    severity: 'warning',
    errorMessage: '',
    remediationHint: '',
    applyObjectType: selectedChapter.value?.attrs?.objectLevel || 'aircraft',
    supplyModeScope: 'all',
    keyPartScope: 'all',
    requiredFlag: 1,
    enabledFlag: 1,
    sortOrder: detail.value.rules.length + 1
  }
  ruleOpen.value = true
}

function submitRule() {
  const index = detail.value.rules.findIndex(item => item.id === ruleForm.value.id)
  if (index > -1) {
    detail.value.rules.splice(index, 1, { ...ruleForm.value })
  } else {
    detail.value.rules.push({ ...ruleForm.value })
  }
  ruleOpen.value = false
}

function supplementRule() {
  if (filteredRules.value.length > 0) {
    const source = filteredRules.value[0]
    const rule = {
      ...source,
      id: uuid(),
      ruleCode: source.ruleCode + '-SUP',
      ruleName: source.ruleName + '补充',
      sortOrder: detail.value.rules.length + 1
    }
    detail.value.rules.push(rule)
    openRule(rule)
  } else {
    openRule()
  }
}

function viewRule(row) {
  proxy.$modal.alert(`${row.ruleName}\n\n${row.errorMessage || ''}\n\n${row.remediationHint || ''}`)
}

function removeRule(row) {
  detail.value.rules = detail.value.rules.filter(item => item.id !== row.id)
}

function addParam() {
  detail.value.params.push({
    id: uuid(),
    templateId: detail.value.id,
    chapterId: undefined,
    paramCode: 'param_' + Date.now(),
    paramName: '新参数',
    paramType: 'string',
    paramValue: '',
    defaultValue: '',
    optionJson: '[]',
    requiredFlag: 0,
    editableFlag: 1,
    enabledFlag: 1,
    sortOrder: detail.value.params.length + 1,
    paramDesc: ''
  })
}

function removeParam(row) {
  detail.value.params = detail.value.params.filter(item => item.id !== row.id)
}

function restoreParams() {
  detail.value.params = defaultParams(detail.value.id)
}

function normalizeTemplate(data) {
  const next = {
    ...emptyTemplate(),
    ...(data || {})
  }
  next.chapters = (next.chapters || []).map(item => ({
    ...item,
    chapterName: normalizeChapterName(item.chapterName),
    chapterPath: normalizeChapterPath(item.chapterPath),
    attrs: parseJson(item.attrsJson, {})
  }))
  next.dataSources = next.dataSources || []
  next.rules = next.rules || []
  next.params = next.params || []
  ensureDossierDirectoryLayout(next)
  return next
}

function ensureDossierDirectoryLayout(template) {
  const chapters = template.chapters || []
  const dossierRoot = chapters.find(item => item.chapterCode === 'NODE_TEMPLATE_ROOT' || item.chapterName === '卷宗目录')
  if (!dossierRoot) {
    return
  }
  const topRoot = chapters.find(item => item.chapterCode === 'AIRCRAFT_ROOT')
  dossierRoot.chapterName = '卷宗目录'
  dossierRoot.parentId = null
  dossierRoot.chapterLevel = 1
  dossierRoot.chapterPath = '卷宗目录'
  dossierRoot.sortOrder = dossierRoot.sortOrder || 1

  let aircraftDirectory = chapters.find(item => item.chapterCode === 'AIRCRAFT_DIRECTORY_ROOT')
    || chapters.find(item => item.parentId === dossierRoot.id && item.chapterName === '整机目录')
  if (!aircraftDirectory) {
    aircraftDirectory = {
      id: uuid(),
      templateId: template.id,
      parentId: dossierRoot.id,
      chapterCode: 'AIRCRAFT_DIRECTORY_ROOT',
      chapterName: '整机目录',
      chapterLevel: 2,
      chapterPath: '卷宗目录/整机目录',
      nodeKind: 'group',
      sortOrder: 90,
      requiredFlag: 1,
      enabledFlag: 1,
      defaultExpand: 1,
      completenessRequirement: 'strict',
      chapterDesc: '整机层目录，和系统、子系统、设备、组件、零件目录同属于卷宗目录。',
      attrs: {
        objectLevel: 'aircraft',
        displayType: 'tree_table',
        sortMode: 'business_order',
        primaryFields: ['tail_number', 'msn', 'aircraft_type', 'status'],
        blocks: ['summary', 'relation', 'details', 'documents', 'issues'],
        showMissingTips: true
      }
    }
    chapters.push(aircraftDirectory)
  } else {
    aircraftDirectory.parentId = dossierRoot.id
    aircraftDirectory.chapterName = '整机目录'
    aircraftDirectory.chapterLevel = 2
    aircraftDirectory.chapterPath = '卷宗目录/整机目录'
    aircraftDirectory.nodeKind = aircraftDirectory.nodeKind || 'group'
    aircraftDirectory.sortOrder = aircraftDirectory.sortOrder || 90
    aircraftDirectory.attrs = {
      ...(aircraftDirectory.attrs || {}),
      objectLevel: 'aircraft',
      displayType: 'tree_table',
      sortMode: 'business_order',
      primaryFields: ['tail_number', 'msn', 'aircraft_type', 'status'],
      blocks: ['summary', 'relation', 'details', 'documents', 'issues'],
      showMissingTips: true
    }
  }

  const directDirectoryCodes = ['SYSTEM_ROOT', 'SUBSYSTEM_ROOT', 'EQUIPMENT_ROOT', 'COMPONENT_ROOT', 'PART_ROOT']
  chapters.forEach(item => {
    if (directDirectoryCodes.includes(item.chapterCode)) {
      item.parentId = dossierRoot.id
      item.chapterLevel = 2
      item.chapterPath = `卷宗目录/${item.chapterName}`
    }
  })

  const aircraftParentId = topRoot?.id
  chapters.forEach(item => {
    if ([topRoot?.id, dossierRoot.id, aircraftDirectory.id].includes(item.id)) {
      return
    }
    const isAircraftChapter = item.attrs?.objectLevel === 'aircraft'
      && item.chapterCode !== 'AIRCRAFT_ROOT'
      && item.chapterCode !== 'AIRCRAFT_DIRECTORY_ROOT'
      && (!aircraftParentId || item.parentId === aircraftParentId || item.parentId === dossierRoot.id)
    if (isAircraftChapter) {
      item.parentId = aircraftDirectory.id
      item.chapterLevel = 3
      item.chapterPath = `卷宗目录/整机目录/${item.chapterName}`
    }
  })

  const childByParent = chapters.reduce((acc, item) => {
    if (item.parentId) {
      acc[item.parentId] = acc[item.parentId] || []
      acc[item.parentId].push(item)
    }
    return acc
  }, {})
  const refreshDescendants = (parent) => {
    ;(childByParent[parent.id] || []).forEach(child => {
      child.chapterLevel = (parent.chapterLevel || 1) + 1
      child.chapterPath = `${parent.chapterPath}/${child.chapterName}`
      refreshDescendants(child)
    })
  }
  refreshDescendants(dossierRoot)

  if (topRoot) {
    const redirectRootRef = row => row.chapterId === topRoot.id ? { ...row, chapterId: dossierRoot.id } : row
    template.dataSources = (template.dataSources || []).map(redirectRootRef)
    template.rules = (template.rules || []).map(redirectRootRef)
    template.params = (template.params || []).map(redirectRootRef)
    template.chapters = chapters.filter(item => item.id !== topRoot.id)
  }
}

function normalizeChapterName(value) {
  return value === '节点目录模板' ? '卷宗目录' : value
}

function normalizeChapterPath(value) {
  return typeof value === 'string' ? value.replace(/节点目录模板/g, '卷宗目录') : value
}

function buildPayload() {
  const payload = JSON.parse(JSON.stringify(detail.value))
  payload.chapterTreeJson = stringifyJson({
    scopeMode: 'aircraft_composite',
    nodeDrilldown: true,
    levels: ['aircraft', 'system', 'subsystem', 'equipment', 'component', 'part'],
    chapterCount: payload.chapters.length
  })
  payload.validationRulesJson = stringifyJson({
    ruleCount: payload.rules.length,
    blockerCount: payload.rules.filter(item => item.severity === 'blocker').length
  })
  payload.defaultGeneratorParamsJson = stringifyJson(payload.params.reduce((acc, item) => {
    acc[item.paramCode] = item.defaultValue
    return acc
  }, {}))
  payload.chapters = payload.chapters.map(item => {
    const attrsJson = stringifyJson(item.attrs || parseJson(item.attrsJson, {}))
    delete item.attrs
    delete item.children
    item.attrsJson = attrsJson
    return item
  })
  payload.dataSources = payload.dataSources.map(item => ({
    ...item,
    joinConditionJson: normalizeJsonString(item.joinConditionJson, {}),
    filterConditionJson: normalizeJsonString(item.filterConditionJson, {}),
    attrsJson: normalizeJsonString(item.attrsJson, {})
  }))
  payload.rules = payload.rules.map(item => ({
    ...item,
    ruleExpressionJson: normalizeJsonString(item.ruleExpressionJson, {})
  }))
  payload.params = payload.params.map(item => ({
    ...item,
    optionJson: normalizeJsonString(item.optionJson, [])
  }))
  return payload
}

function buildTree(list) {
  const map = {}
  const roots = []
  list.forEach(item => {
    map[item.id] = { ...item, children: [] }
  })
  list.forEach(item => {
    const node = map[item.id]
    if (item.parentId && map[item.parentId]) {
      map[item.parentId].children.push(node)
    } else {
      roots.push(node)
    }
  })
  Object.values(map).forEach(item => item.children.sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0)))
  roots.sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
  return roots
}

function collectChildIds(id) {
  const ids = [id]
  detail.value.chapters
    .filter(item => item.parentId === id)
    .forEach(item => ids.push(...collectChildIds(item.id)))
  return ids
}

function siblingChapters(parentId) {
  return detail.value.chapters
    .filter(item => (item.parentId || '') === (parentId || ''))
    .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
}

function nextSortOrder(siblings) {
  return siblings.length ? Math.max(...siblings.map(item => Number(item.sortOrder) || 0)) + 10 : 10
}

function sortOrderAfter(siblings, index) {
  const insertIndex = index > -1 ? index + 1 : siblings.length
  siblings.forEach((item, itemIndex) => {
    item.sortOrder = itemIndex < insertIndex ? (itemIndex + 1) * 10 : (itemIndex + 2) * 10
  })
  return (insertIndex + 1) * 10
}

function buildChapterPath(parent, chapterName) {
  return parent?.chapterPath ? `${parent.chapterPath}/${chapterName}` : chapterName
}

function syncEffectiveRange(value) {
  detail.value.effectiveFrom = value?.[0]
  detail.value.effectiveTo = value?.[1]
}

function parseJson(value, defaultValue) {
  if (!value) {
    return defaultValue
  }
  if (typeof value === 'object') {
    return value
  }
  try {
    return JSON.parse(value)
  } catch (e) {
    return defaultValue
  }
}

function stringifyJson(value) {
  return JSON.stringify(value ?? {})
}

function normalizeJsonString(value, defaultValue) {
  return stringifyJson(parseJson(value, defaultValue))
}

function conditionRowsFromJson(value, remarks = {}) {
  const data = parseJson(value, {})
  if (!data || typeof data !== 'object' || Array.isArray(data)) {
    return []
  }
  return Object.entries(data).map(([field, fieldValue]) => ({
    field,
    value: fieldValue == null ? '' : String(fieldValue),
    remark: remarks?.[field] || ''
  }))
}

function conditionObjectFromRows(rows) {
  return rows.reduce((acc, row) => {
    if (row.field) {
      acc[row.field] = row.value == null ? '' : row.value
    }
    return acc
  }, {})
}

function remarksFromRows(rows) {
  return rows.reduce((acc, row) => {
    if (row.field && row.remark) {
      acc[row.field] = row.remark
    }
    return acc
  }, {})
}

function conditionLabel(value) {
  const data = parseJson(value, {})
  const entries = Object.entries(data || {})
  if (entries.length === 0) {
    return '-'
  }
  return entries.map(([field, fieldValue]) => `${field} = ${fieldValue}`).join('；')
}

function sourceSelectedFieldsLabel(row) {
  const attrs = parseJson(row.attrsJson, {})
  const fields = Array.isArray(attrs.selectedFields) ? attrs.selectedFields : []
  return fields.length ? fields.join('、') : '-'
}

function sourceTableOptionLabel(table) {
  const comment = table.tableComment ? ` / ${table.tableComment}` : ''
  return `${table.tableName}${comment} / ${sourceDomainLabel(table.sourceSystem)}`
}

function columnOptionLabel(column) {
  const comment = column.columnComment ? ` - ${column.columnComment}` : ''
  return `${column.columnName}${comment}`
}

function buildSourceTableGroups(tables, chapter) {
  const sourceTables = tables || []
  const recommendedNames = recommendedSourceTableNames(chapter)
  const recommended = sourceTables.filter(table => recommendedNames.has(table.tableName))
  const recommendedSet = new Set(recommended.map(table => table.tableName))
  const groups = []

  if (recommended.length > 0) {
    groups.push({
      label: `当前章节推荐${chapter?.chapterName ? '：' + chapter.chapterName : ''}`,
      options: recommended
    })
  }

  const domainOrder = ['卷宗域', '物理域', '构型域', '设计域', '制造域', '质量域', '运维域', '运行域']
  const domainMap = sourceTables
    .filter(table => !recommendedSet.has(table.tableName))
    .reduce((acc, table) => {
      const domain = sourceDomainLabel(table.sourceSystem)
      acc[domain] = acc[domain] || []
      acc[domain].push(table)
      return acc
    }, {})

  domainOrder.forEach(domain => {
    if (domainMap[domain]?.length) {
      groups.push({ label: domain, options: domainMap[domain] })
      delete domainMap[domain]
    }
  })
  Object.keys(domainMap).sort().forEach(domain => {
    groups.push({ label: domain, options: domainMap[domain] })
  })
  return groups
}

function recommendedSourceTableNames(chapter) {
  const names = new Set()
  const objectLevel = chapter?.attrs?.objectLevel
  const text = `${chapter?.chapterCode || ''} ${chapter?.chapterName || ''} ${chapter?.chapterPath || ''} ${chapter?.chapterDesc || ''}`.toLowerCase()

  addTables(names, baseTablesForObjectLevel(objectLevel))
  if (hasAny(text, ['basic', 'profile', 'summary', 'overview', '基本', '概况', '概要', '身份', '主数据'])) {
    addTables(names, profileTablesForObjectLevel(objectLevel))
  }
  if (hasAny(text, ['bom', 'structure', 'composition', 'assembly_rel', '构型', '组成', '结构', '装配关系'])) {
    addTables(names, ['aircraft_bom_node', 'assembly_record', 'object_interface'])
  }
  if (hasAny(text, ['design', 'mbd', 'model', 'parameter', 'hydraulic', 'impact', '设计', '模型', '参数', '液压', '弯管'])) {
    addTables(names, [
      'part_master',
      'part_parameter_value',
      'part_hydraulic_tube_impact_model',
      'impact_tube_segment',
      'impact_tube_support',
      'impact_tube_fluid',
      'impact_tube_boundary_condition',
      'file_relation'
    ])
  }
  if (hasAny(text, ['manufacturing', 'process', 'production', 'shop', 'material', '制造', '工艺', '生产', '批次', '追溯'])) {
    addTables(names, [
      'shop_order',
      'shop_order_task',
      'process_route',
      'production_operation_record',
      'material_lot_trace'
    ])
  }
  if (hasAny(text, ['inspection', 'quality', 'qa', '检验', '检测', '质量'])) {
    addTables(names, ['inspection_record', 'inspection_measurement', 'quality_text_record'])
  }
  if (hasAny(text, ['install', 'removal', 'assembly', '装机', '拆换', '安装', '装配'])) {
    addTables(names, ['install_removal', 'assembly_record'])
  }
  if (hasAny(text, ['service', 'maintenance', 'life', 'usage', 'mro', '服役', '维修', '寿命', '使用'])) {
    addTables(names, ['work_order', 'work_order_task', 'life_usage_record', 'install_removal'])
  }
  if (hasAny(text, ['fault', '故障', '异常'])) {
    addTables(names, ['fault_event', 'fault_action', 'work_order'])
  }
  if (hasAny(text, ['technical_status', 'status', 'lifecycle', '状态', '履历', '生命周期'])) {
    addTables(names, ['object_technical_status', 'object_status_history', 'object_lifecycle_record'])
  }
  if (hasAny(text, ['interface', '接口'])) {
    addTables(names, ['object_interface'])
  }
  if (hasAny(text, ['file', 'attachment', 'document', 'cert', '附件', '文件', '证明', '证书'])) {
    addTables(names, ['file_relation', 'file_asset', 'file_category'])
  }
  if (hasAny(text, ['flight', 'ops', '运行', '航段', '飞行'])) {
    addTables(names, ['event_flight_leg'])
  }
  return names
}

function baseTablesForObjectLevel(objectLevel) {
  const map = {
    aircraft: ['v_aircraft_profile_detail', 'physical_aircraft', 'aircraft_bom_node', 'file_relation'],
    system: ['v_system_profile_detail', 'aircraft_bom_node', 'object_lifecycle_record', 'object_technical_status', 'file_relation'],
    subsystem: ['v_subsystem_profile_detail', 'aircraft_bom_node', 'object_lifecycle_record', 'object_technical_status', 'file_relation'],
    equipment: ['v_equipment_profile_detail', 'aircraft_bom_node', 'equipment_object_master', 'equipment_object_instance', 'install_removal', 'file_relation'],
    component: ['v_component_profile_detail', 'aircraft_bom_node', 'component_object_master', 'component_object_instance', 'assembly_record', 'file_relation'],
    part: ['v_part_profile_detail', 'part_master', 'part_instance', 'part_parameter_value', 'file_relation']
  }
  return map[objectLevel] || ['file_relation']
}

function profileTablesForObjectLevel(objectLevel) {
  const map = {
    aircraft: ['v_aircraft_profile_detail', 'physical_aircraft'],
    system: ['v_system_profile_detail', 'aircraft_bom_node'],
    subsystem: ['v_subsystem_profile_detail', 'aircraft_bom_node'],
    equipment: ['v_equipment_profile_detail', 'equipment_object_master', 'equipment_object_instance'],
    component: ['v_component_profile_detail', 'component_object_master', 'component_object_instance'],
    part: ['v_part_profile_detail', 'part_master', 'part_instance']
  }
  return map[objectLevel] || []
}

function addTables(target, tables) {
  ;(tables || []).forEach(table => target.add(table))
}

function hasAny(text, keywords) {
  return keywords.some(keyword => text.includes(keyword.toLowerCase()))
}

function sourceDomainLabel(value) {
  const map = {
    DOSSIER: '卷宗域',
    PHYSICAL: '物理域',
    CONFIG: '构型域',
    DESIGN: '设计域',
    PLM: '设计域',
    MES: '制造域',
    QA: '质量域',
    MRO: '运维域',
    OPS: '运行域'
  }
  return map[value] || value || '-'
}

function defaultParams(templateId) {
  return [
    param(templateId, 'keep_empty_chapter', '空目录处理', 'boolean', 'true', '保留空目录并标记无数据'),
    param(templateId, 'regenerate_strategy', '重新生成策略', 'enum', 'new_version', '生成新版本'),
    param(templateId, 'snapshot_enabled', '数据快照', 'boolean', 'true', '生成数据快照'),
    param(templateId, 'missing_data_policy', '缺失数据处理', 'enum', 'block_key_warn_normal', '关键缺失阻止生成，普通缺失提醒'),
    param(templateId, 'default_export_format', '默认导出格式', 'enum', 'PDF+ZIP', 'PDF + 附件 ZIP'),
    param(templateId, 'auto_attach_files', '文件自动挂接', 'boolean', 'true', '自动挂接图纸、报告和证明文件')
  ]
}

function param(templateId, code, name, type, value, desc) {
  return {
    id: uuid(),
    templateId: templateId,
    chapterId: undefined,
    paramCode: code,
    paramName: name,
    paramType: type,
    paramValue: value,
    defaultValue: value,
    optionJson: '[]',
    requiredFlag: 0,
    editableFlag: 1,
    enabledFlag: 1,
    sortOrder: 0,
    paramDesc: desc
  }
}

function uuid() {
  if (window.crypto && window.crypto.randomUUID) {
    return window.crypto.randomUUID()
  }
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
    const r = Math.random() * 16 | 0
    const v = c === 'x' ? r : (r & 0x3 | 0x8)
    return v.toString(16)
  })
}

function chapterLabel(id) {
  return detail.value.chapters.find(item => item.id === id)?.chapterName || ''
}

function parseTime(value) {
  return value ? proxy.parseTime(value) : '-'
}

function objectTypeLabel(value) {
  return value === 'aircraft' ? '整机' : value || '-'
}

function templateTypeLabel(value) {
  const map = { general: '综合卷宗' }
  return map[value] || value || '-'
}

function statusLabel(value) {
  const map = { active: '启用', inactive: '停用', draft: '草稿', archived: '历史' }
  return map[value] || value || '-'
}

function statusType(value) {
  const map = { active: 'success', inactive: 'info', draft: 'warning', archived: 'info' }
  return map[value] || 'info'
}

function objectLevelLabel(value) {
  const map = {
    aircraft: '整机',
    system: '系统',
    subsystem: '子系统',
    equipment: '设备',
    component: '组件',
    part: '零件',
    template_group: '目录分组'
  }
  return map[value] || value || '-'
}

function displayTypeLabel(value) {
  const map = {
    summary_table: '概要卡片 + 明细表',
    tree_table: '结构树 + 明细表',
    timeline_files: '时间线 + 附件清单',
    part_card_param_table: '件号卡片 + 参数表',
    file_list: '文件清单'
  }
  return map[value] || value || '-'
}

function sortModeLabel(value) {
  const map = {
    business_order: '业务顺序',
    structure_order: '结构顺序',
    time_asc: '时间顺序',
    time_desc: '时间倒序',
    priority: '重要性优先'
  }
  return map[value] || value || '-'
}

function ruleTypeLabel(value) {
  const map = { required: '必填检查', relation: '关联检查', expression: '表达式检查', enum: '枚举检查', business: '业务检查' }
  return map[value] || value || '-'
}

function severityLabel(value) {
  const map = { blocker: '阻止生成', error: '错误', warning: '提醒风险', info: '提示' }
  return map[value] || value || '-'
}

function severityType(value) {
  const map = { blocker: 'danger', error: 'danger', warning: 'warning', info: 'info' }
  return map[value] || 'info'
}

loadSourceMetadata()
getList()
</script>

<style scoped lang="scss">
.dossier-template-page {
  background: #f4f6f9;
}

.page-head,
.panel-head,
.template-card-head,
.overview,
.actions,
.tab-actions,
.box-title {
  display: flex;
  align-items: center;
}

.page-head {
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;

  h2 {
    margin: 0;
    font-size: 22px;
    line-height: 30px;
  }
}

.subline,
.overview-text,
.template-meta {
  color: #6b7280;
  font-size: 13px;
}

.actions,
.tab-actions {
  gap: 8px;
  flex-wrap: wrap;
}

.overview {
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid #d9e2ec;
  border-radius: 6px;
  background: #fff;
  margin-bottom: 12px;
}

.overview-main {
  flex: 1;
  min-width: 0;
}

.overview-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  margin-bottom: 5px;
}

.metric {
  min-width: 94px;
  padding: 8px 10px;
  border-left: 1px solid #edf2f7;
  display: grid;
  gap: 2px;
  color: #6b7280;

  strong {
    color: #1f2937;
    font-size: 22px;
    line-height: 24px;
  }
}

.workspace {
  display: grid;
  grid-template-columns: minmax(280px, 360px) minmax(0, 1fr);
  gap: 12px;
  align-items: start;
}

.template-panel,
.config-panel,
.edit-box,
.tree-box {
  border: 1px solid #d9e2ec;
  border-radius: 6px;
  background: #fff;
}

.panel-head {
  height: 48px;
  padding: 0 14px;
  border-bottom: 1px solid #edf2f7;
  justify-content: space-between;
  font-weight: 700;
}

.panel-body {
  padding: 12px;
}

.template-list {
  display: grid;
  gap: 10px;
}

.template-card {
  border: 1px solid #d9e2ec;
  border-radius: 6px;
  padding: 12px;
  cursor: pointer;

  &.active {
    border-color: #2563eb;
    background: #eff6ff;
  }
}

.template-card-head {
  justify-content: space-between;
  gap: 10px;
  align-items: flex-start;
}

.template-title {
  display: grid;
  gap: 4px;
  min-width: 0;

  strong,
  span {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  span {
    color: #64748b;
    font-size: 12px;
  }
}

.template-tags,
.card-actions {
  margin-top: 8px;
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.config-panel {
  min-width: 0;
}

.config-tabs {
  padding: 0 14px 14px;
}

.tab-actions {
  margin-bottom: 12px;
}

.tree-box {
  padding: 12px;
  min-height: 520px;
}

.tree-tags {
  display: flex;
  gap: 6px;
  margin-bottom: 10px;
}

.tree-node-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.chapter-form,
.edit-box {
  padding: 14px;
}

.chapter-source-panel {
  border-top: 1px solid #edf2f7;
  padding-top: 14px;
}

.box-title {
  justify-content: space-between;
  gap: 10px;
  font-weight: 700;
  margin-bottom: 12px;
}

.condition-builder {
  border: 1px solid #edf2f7;
  border-radius: 6px;
  padding: 10px;
  margin-bottom: 12px;
}

.condition-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 8px;
  font-weight: 700;
}

.order-list {
  margin: 0;
  padding-left: 20px;
  line-height: 28px;
  color: #4b5563;
}

.mt12 {
  margin-top: 12px;
}

.check-summary {
  min-height: 160px;
}

@media (max-width: 1200px) {
  .workspace {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .page-head,
  .overview {
    align-items: flex-start;
    flex-direction: column;
  }

  .metric {
    width: 100%;
    border-left: 0;
    border-top: 1px solid #edf2f7;
  }
}
</style>
