<template>
  <div class="design-platform-view solve-industrial-view">
    <div class="design-platform-shell">
      <section class="platform-topbar">
        <div>
          <h1 class="platform-title">{{ hasTask ? taskTitle : '模型解耦求解' }}</h1>
        </div>
        <div class="topbar-meta">
          <div class="meta-chip">
            <span class="meta-dot meta-dot--green"></span>
            <span>{{ hasTask ? accessLabel : tabLabel }}</span>
          </div>
          <div class="meta-chip">
            <span class="meta-dot meta-dot--orange"></span>
            <span>解耦与求解</span>
          </div>
        </div>
      </section>

      <section v-if="!hasTask" class="section-block">
        <div class="section-header">
          <div>
            <h2 class="section-title">我的模型解耦求解任务</h2>
          </div>
          <el-button plain icon="Refresh" :loading="inboxLoading" @click="loadInbox">刷新</el-button>
        </div>

        <el-tabs v-model="activeTab" class="mt-12" @tab-change="changeTab">
          <el-tab-pane label="待处理" name="pending" />
          <el-tab-pane label="已处理" name="handled" />
          <el-tab-pane label="相关任务" name="related" />
        </el-tabs>

        <div class="table-shell">
          <el-table v-loading="inboxLoading" :data="visibleInboxTasks" stripe class="platform-table">
            <el-table-column label="任务名称" prop="taskName" min-width="220" show-overflow-tooltip />
            <el-table-column label="当前节点" prop="currentNodeName" min-width="180" />
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="actionType(row.action?.mode)">{{ row.action?.label || (activeTab === 'pending' ? '等待' : '查看') }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="备注" min-width="180">
              <template #default="{ row }">{{ row.action?.reason || row.handledReason || '-' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.action?.mode !== 'wait' || activeTab !== 'pending'"
                  link
                  :type="row.action?.mode === 'enter' ? 'primary' : 'info'"
                  @click="openTask(row)"
                >
                  {{ activeTab === 'pending' ? row.action?.label || '查看' : '查看' }}
                </el-button>
                <span v-else class="wait-action">等待</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </section>

      <template v-else>
        <el-alert
          v-if="access.mode === 'wait'"
          class="mb-16"
          title="当前任务未流转到你，暂不能执行校验、解耦或求解。"
          type="info"
          :closable="false"
          show-icon
        />

        <section class="section-block">
          <div class="section-header">
            <div>
              <h2 class="section-title">任务输入与基准参数</h2>
            </div>
            <el-tag v-if="faultPipeParameters.setCode">{{ faultPipeParameters.setCode }}</el-tag>
          </div>

          <div v-if="faultPipeSummaryItems.length" class="fixed-input-meta">
            <div v-for="item in faultPipeSummaryItems" :key="item.label" class="fixed-input-meta__item">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
            </div>
          </div>

          <el-alert
            v-else
            class="mt-12"
            title="未读取到任务绑定的管段参数。"
            type="info"
            :closable="false"
            show-icon
          />

          <el-collapse v-if="faultPipeParameterGroups.length" class="mt-12">
            <el-collapse-item v-for="group in faultPipeParameterGroups" :key="group.groupCode" :title="group.groupName">
              <div class="table-shell">
                <el-table :data="group.items || []" stripe class="platform-table">
                  <el-table-column label="参数" prop="paramName" min-width="170" show-overflow-tooltip />
                  <el-table-column label="值 / 表达式" min-width="320" show-overflow-tooltip>
                    <template #default="{ row }">{{ row.formulaText || row.paramValue || '-' }}</template>
                  </el-table-column>
                  <el-table-column label="单位" prop="paramUnit" width="110" />
                  <el-table-column label="说明" prop="description" min-width="220" show-overflow-tooltip />
                </el-table>
              </div>
            </el-collapse-item>
          </el-collapse>
        </section>

        <div class="content-grid solve-command-grid">
          <section class="section-block objective-workbench">
            <div class="section-header">
              <div>
                <h2 class="section-title">目标与约束归口确认</h2>
              </div>
              <div class="action-row">
                <el-tag type="success">{{ objectiveCount }} 个目标</el-tag>
                <el-tag type="warning">{{ constraintCount }} 个约束</el-tag>
                <el-tag type="info">优先级 0-10</el-tag>
              </div>
            </div>

            <div class="objective-workbench-toolbar">
              <span>目标优先级用于表达各优化目标的重要程度，不要求总和固定。</span>
              <div>
                <el-button plain icon="Back" class="btn-soft-blue" @click="backToInbox">返回任务列表</el-button>
                <el-button type="primary" icon="Check" class="btn-strong-blue" :disabled="!canEditObjectiveWeights || !objectiveWeightRows.length" :loading="objectiveWeightSaving" @click="saveObjectiveWeightSettings">
                  保存目标优先级
                </el-button>
              </div>
            </div>

            <div class="objective-constraint-columns">
              <div class="oc-panel oc-panel--objective">
                <div class="oc-panel__head">
                  <strong><span class="panel-icon panel-icon--objective">O</span>优化目标池</strong>
                  <span class="panel-badge panel-badge--objective">{{ objectiveCount }} 个目标</span>
                </div>
                <div class="oc-card-list">
                  <div v-for="row in objectiveWeightRows" :key="`${row.discipline}-${row.itemCode}`" class="oc-item oc-item--objective">
                    <div class="oc-item__top">
                      <div>
                        <div class="oc-item__title">{{ row.itemName }}</div>
                        <div class="oc-item__meta">
                          <span :class="['discipline-pill', disciplinePillClass(row.discipline)]">{{ row.disciplineName }}</span>
                          <span>优化类型：{{ row.direction || '-' }}</span>
                        </div>
                      </div>
                      <span :class="['priority-badge', objectivePriorityClass(row.weight)]">{{ objectivePriorityLabel(row.weight) }}</span>
                    </div>
                    <p v-if="row.description || row.remark" class="oc-item__desc">{{ row.description || row.remark }}</p>
                    <div class="oc-item__footer">
                      <span class="selection-badge">已选择</span>
                      <div class="weight-editor weight-editor--card">
                        <span>优先级</span>
                        <el-slider v-model="row.weight" :min="0" :max="10" :disabled="!canEditObjectiveWeights" />
                        <el-input-number v-model="row.weight" :min="0" :max="10" :precision="0" :disabled="!canEditObjectiveWeights" controls-position="right" />
                      </div>
                    </div>
                  </div>
                  <el-empty v-if="!objectiveWeightRows.length" description="暂无已选目标" :image-size="70" />
                </div>
              </div>

              <div class="oc-panel oc-panel--constraint">
                <div class="oc-panel__head">
                  <strong><span class="panel-icon panel-icon--constraint">C</span>设计约束池</strong>
                  <span class="panel-badge panel-badge--constraint">{{ constraintCount }} 个约束</span>
                </div>
                <div class="oc-card-list">
                  <div v-for="row in constraintRows" :key="`${row.discipline}-${row.itemCode}`" class="oc-item oc-item--constraint">
                    <div class="oc-item__top">
                      <div>
                        <div class="oc-item__title">{{ row.itemName }}</div>
                        <div class="oc-item__meta">
                          <span :class="['discipline-pill', disciplinePillClass(row.discipline)]">{{ row.disciplineName }}</span>
                          <span>约束关系：{{ row.direction || '-' }}</span>
                        </div>
                      </div>
                      <span class="constraint-value">{{ row.limitValue || '-' }} {{ row.unit || '' }}</span>
                    </div>
                    <p v-if="row.description || row.remark" class="oc-item__desc">{{ row.description || row.remark }}</p>
                    <div class="oc-item__footer">
                      <span class="selection-badge selection-badge--constraint">已选择</span>
                      <span class="compatibility-note">与前置节点约束兼容</span>
                    </div>
                  </div>
                  <el-empty v-if="!constraintRows.length" description="暂无已选约束" :image-size="70" />
                </div>
              </div>
            </div>
          </section>

          <section class="section-block conflict-panel">
            <div class="section-header">
              <div>
                <h2 class="section-title">目标约束冲突校验</h2>
              </div>
              <div class="action-row">
                <el-button icon="Warning" class="btn-soft-amber" :disabled="!canCheckConflict" @click="check(false)">模拟不通过</el-button>
                <el-button type="success" icon="CircleCheck" :disabled="!canCheckConflict" @click="check(true)">执行校验</el-button>
              </div>
            </div>

            <el-result
              v-if="!conflict.checked"
              icon="info"
              title="待执行目标约束校验"
            />
            <el-result
              v-else-if="conflict.passed"
              icon="success"
              title="目标约束校验通过"
            />
            <div v-else class="mt-12">
              <el-alert type="warning" title="发现目标 / 约束冲突" show-icon :closable="false" />
              <div class="conflict-return-row">
                <el-button type="warning" icon="RefreshLeft" :disabled="!canReturnConflictPlan" :loading="conflictReturnSubmitting" @click="returnConflictPlan">
                  {{ conflictReturnButtonText }}
                </el-button>
              </div>
              <div class="table-shell">
                <el-table :data="conflict.conflicts || []" stripe class="platform-table">
                  <el-table-column label="冲突项" prop="title" min-width="170" />
                  <el-table-column label="影响学科" prop="disciplines" width="140" />
                  <el-table-column label="调整建议" prop="suggestion" min-width="220" />
                </el-table>
              </div>
            </div>
          </section>
        </div>

        <section class="section-block">
          <div class="section-header">
            <div>
              <h2 class="section-title">解耦子任务</h2>
            </div>
            <div class="action-row">
              <el-button icon="Connection" class="btn-soft-green" :disabled="readonlyMode || decomposed" @click="decompose">{{ decomposed ? '已解耦' : '任务解耦' }}</el-button>
            </div>
          </div>

          <el-empty
            v-if="!decomposed"
            class="subtask-empty-state"
            description="点击任务解耦后生成子任务"
          />
          <div v-else class="content-grid content-grid--balanced mt-12">
            <div
              v-for="subtask in subtaskWorkspaces"
              :key="subtask.subtaskCode"
              :class="['soft-panel subtask-entry-card', { 'is-active': activeSubtaskCode === subtask.subtaskCode }]"
              @click="openSubtaskWorkspace(subtask.subtaskCode)"
            >
              <div class="card-head">
                <h3 class="section-title">{{ subtask.subtaskName }}</h3>
                <el-tag :type="activeSubtaskCode === subtask.subtaskCode ? 'primary' : 'info'">
                  {{ activeSubtaskCode === subtask.subtaskCode ? '当前工作区' : '点击进入' }}
                </el-tag>
              </div>
              <div class="subtask-group">
                <p class="subtask-group__title">归类目标</p>
                <el-tag
                  v-for="item in subtaskItems(subtask, 'objective')"
                  :key="`${subtask.subtaskCode}-${item.itemCode}-objective`"
                  class="tag-item"
                  type="success"
                >
                  {{ item.itemName }}
                </el-tag>
                <span v-if="!subtaskItems(subtask, 'objective').length" class="muted-text">暂无目标</span>
              </div>
              <div class="subtask-group">
                <p class="subtask-group__title">归类约束</p>
                <el-tag
                  v-for="item in subtaskItems(subtask, 'constraint')"
                  :key="`${subtask.subtaskCode}-${item.itemCode}-constraint`"
                  class="tag-item"
                  :type="item.shared ? 'danger' : 'warning'"
                >
                  {{ item.itemName }}{{ item.shared ? ' / 同时约束多个子任务' : '' }}
                </el-tag>
                <span v-if="!subtaskItems(subtask, 'constraint').length" class="muted-text">暂无约束</span>
              </div>
              <div class="subtask-group">
                <p class="subtask-group__title">关联设计变量</p>
                <el-tag
                  v-for="item in subtaskVariables(subtask)"
                  :key="`${subtask.subtaskCode}-${item.variableCode}`"
                  class="tag-item"
                  :type="item.shared ? 'danger' : 'info'"
                >
                  {{ item.variableName }}{{ item.shared ? ' / 共享变量' : '' }}
                </el-tag>
                <span v-if="!subtaskVariables(subtask).length" class="muted-text">解耦后在下方选择设计变量</span>
              </div>
              <div class="subtask-entry-card__foot">
                <el-button size="small" type="primary" plain @click.stop="openSubtaskWorkspace(subtask.subtaskCode)">进入处理页面</el-button>
              </div>
            </div>
          </div>
        </section>

        <section v-if="showVariableSection" class="section-block">
          <div class="section-header">
            <div>
              <h2 class="section-title">设计变量统一选择</h2>
            </div>
            <div class="action-row">
              <el-button plain icon="Refresh" class="btn-soft-blue" :disabled="readonlyMode" :loading="variableLoading" @click="loadVariableCatalogs">刷新变量</el-button>
              <el-button type="primary" icon="Check" class="btn-strong-blue" :disabled="readonlyMode || !selectedVariableCount" :loading="variableSaving" @click="saveVariables">
                保存设计变量
              </el-button>
            </div>
          </div>

          <el-alert
            v-if="readonlyMode"
            class="mb-16"
            :title="readonlyReason"
            type="info"
            :closable="false"
            show-icon
          />
          <el-alert
            v-else-if="!selectedVariableCount"
            class="mb-16"
            title="请至少选择一个设计变量，保存后才能进行模型求解。"
            type="warning"
            :closable="false"
            show-icon
          />

          <el-collapse class="mt-12">
            <el-collapse-item v-for="group in variablesBySubtask" :key="group.subtaskCode" :title="group.subtaskName">
              <div class="table-shell">
                <el-table :data="group.items" stripe class="platform-table">
                  <el-table-column v-if="!readonlyMode" width="60">
                    <template #default="{ row }">
                      <el-checkbox v-model="row.checked" />
                    </template>
                  </el-table-column>
                  <el-table-column label="学科" prop="disciplineName" width="100" />
                  <el-table-column label="变量名称" prop="variableName" min-width="180" show-overflow-tooltip />
                  <el-table-column label="类型" width="100">
                    <template #default="{ row }">{{ variableTypeLabel(row.variableType) }}</template>
                  </el-table-column>
                  <el-table-column label="初始值" width="130">
                    <template #default="{ row }">
                      <el-input v-model="row.initialValue" :disabled="readonlyMode" />
                    </template>
                  </el-table-column>
                  <el-table-column label="下限" width="120">
                    <template #default="{ row }">
                      <el-input v-model="row.lowerBound" :disabled="readonlyMode" />
                    </template>
                  </el-table-column>
                  <el-table-column label="上限" width="120">
                    <template #default="{ row }">
                      <el-input v-model="row.upperBound" :disabled="readonlyMode" />
                    </template>
                  </el-table-column>
                  <el-table-column label="步长" width="110">
                    <template #default="{ row }">
                      <el-input v-model="row.stepValue" :disabled="readonlyMode" />
                    </template>
                  </el-table-column>
                  <el-table-column label="单位" prop="unit" width="90" />
                </el-table>
              </div>
            </el-collapse-item>
          </el-collapse>
        </section>

        <section v-if="showHydraulicWorkspace" class="section-block">
          <div class="section-header">
            <div>
              <h2 class="section-title">液压弯管抗冲击性能优化</h2>
              <p class="section-subtitle">调用代理模型生成候选方案，并对候选方案进行 SolidWorks 建模和 ANSYS 仿真验证。</p>
            </div>
            <div class="action-row">
              <el-tag :type="surrogateStatusType">{{ surrogateSolve.statusLabel || '未提交' }}</el-tag>
              <el-button plain icon="Refresh" class="btn-soft-blue" :loading="surrogateRefreshing" @click="refreshSurrogateSolve">刷新状态</el-button>
              <el-button type="primary" icon="CaretRight" :disabled="readonlyMode || !canSolve" :loading="surrogateSubmitting" @click="solve">
                {{ hasSurrogateResult ? '重新优化' : '启动代理模型优化' }}
              </el-button>
              <el-button type="success" icon="Select" :disabled="readonlyMode || !canConfirmSurrogate" :loading="surrogateConfirming" @click="confirmSurrogate">
                确认最优方案
              </el-button>
            </div>
          </div>

          <el-alert
            v-if="!canSolve"
            class="mb-16"
            :title="solveHint"
            type="info"
            :closable="false"
            show-icon
          />
          <el-alert
            v-else-if="surrogateSolve.status === 'FAILED'"
            class="mb-16"
            :title="surrogateSolve.errorMessage || '代理模型求解失败'"
            type="error"
            :closable="false"
            show-icon
          />

          <div class="surrogate-grid mt-12">
            <div class="soft-panel surrogate-model-panel">
              <div class="card-head">
                <h3 class="section-title">代理模型基础信息</h3>
                <el-tag>{{ surrogateSolve.objectiveUnit || 'MPa' }}</el-tag>
              </div>
              <div class="model-selector-row">
                <span>代理模型</span>
                <el-select
                  v-model="selectedSurrogateModel"
                  :disabled="readonlyMode || surrogateSubmitting || surrogateModelLoading"
                  :loading="surrogateModelLoading"
                  placeholder="请选择代理模型"
                  filterable
                >
                  <el-option
                    v-for="model in surrogateModelOptions"
                    :key="model.modelName"
                    :label="model.displayName || model.modelName"
                    :value="model.modelName"
                    :disabled="model.exists === false"
                  >
                    <div class="model-option">
                      <strong>{{ model.displayName || model.modelName }}</strong>
                      <span>{{ model.modelName }}</span>
                    </div>
                  </el-option>
                </el-select>
              </div>
              <div class="model-basic-list">
                <div v-for="item in surrogateInfoItems" :key="item.label" :title="item.description || item.value">
                  <span>{{ item.label }}</span>
                  <strong>{{ item.value }}</strong>
                  <em v-if="item.unit">{{ item.unit }}</em>
                </div>
              </div>
            </div>

            <div class="soft-panel best-solution-panel">
              <div class="card-head">
                <h3 class="section-title">当前最优方案</h3>
                <el-tag :type="surrogateSolve.confirmed ? 'success' : 'info'">{{ surrogateSolve.confirmed ? '已确认' : '待确认' }}</el-tag>
              </div>
              <div v-if="bestSolutionReady" class="best-solution">
                <div><span>L1</span><strong>{{ valueOrDash(best.L1) }}</strong><em>mm</em></div>
                <div><span>L2</span><strong>{{ valueOrDash(best.L2) }}</strong><em>mm</em></div>
                <div><span>θ1</span><strong>{{ valueOrDash(best.theta1) }}</strong><em>°</em></div>
                <div><span>θ2</span><strong>{{ valueOrDash(best.theta2) }}</strong><em>°</em></div>
                <div><span>R</span><strong>{{ valueOrDash(best.R) }}</strong><em>mm</em></div>
                <div class="stress"><span>预测应力</span><strong>{{ valueOrDash(best.predictedStress) }}</strong><em>MPa</em></div>
              </div>
              <el-empty v-else description="暂无优化结果" :image-size="80" />
            </div>
          </div>

          <div class="content-grid content-grid--balanced mt-12">
            <div class="soft-panel">
              <div class="card-head">
                <h3 class="section-title">Top 候选方案</h3>
                <el-tag>{{ (surrogateSolve.candidates || []).length }} 项</el-tag>
              </div>
              <div class="table-shell">
                <el-table :data="surrogateSolve.candidates || []" stripe class="platform-table">
                  <el-table-column label="排序" prop="rank" width="70" />
                  <el-table-column label="L1" width="90">
                    <template #default="{ row }">{{ valueOrDash(row.L1) }}</template>
                  </el-table-column>
                  <el-table-column label="L2" width="90">
                    <template #default="{ row }">{{ valueOrDash(row.L2) }}</template>
                  </el-table-column>
                  <el-table-column label="弯曲角1" width="90">
                    <template #default="{ row }">{{ valueOrDash(row.theta1) }}</template>
                  </el-table-column>
                  <el-table-column label="弯曲角2" width="90">
                    <template #default="{ row }">{{ valueOrDash(row.theta2) }}</template>
                  </el-table-column>
                  <el-table-column label="R" width="80">
                    <template #default="{ row }">{{ valueOrDash(row.R) }}</template>
                  </el-table-column>
                  <el-table-column label="预测应力 / MPa" min-width="140">
                    <template #default="{ row }">{{ valueOrDash(row.predictedStress) }}</template>
                  </el-table-column>
                  <el-table-column label="操作" width="110" fixed="right">
                    <template #default="{ row }">
                      <el-button link type="primary" @click="addCandidateToCompare(row, true)">加入对比</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>

            <div class="soft-panel">
              <div class="card-head">
                <h3 class="section-title">收敛过程</h3>
                <el-tag>{{ (surrogateSolve.history || []).length }} 次记录</el-tag>
              </div>
              <div v-if="convergenceSeries.length" class="convergence-chart-panel">
                <div class="convergence-stats">
                  <div v-for="item in convergenceStatItems" :key="item.label">
                    <span>{{ item.label }}</span>
                    <strong>{{ item.value }}</strong>
                  </div>
                </div>
                <div ref="convergenceChartRef" class="convergence-chart"></div>
              </div>
              <el-empty v-else description="暂无收敛过程数据" :image-size="80" />
            </div>
          </div>

          <div class="soft-panel scheme-compare-panel mt-12">
            <div class="card-head">
              <h3 class="section-title">方案对比工作台</h3>
              <div class="comparison-actions">
                <el-tag>{{ comparisonSchemes.length }} / {{ comparisonLimit }} 个方案</el-tag>
                <el-button size="small" plain icon="Plus" @click="addTopCandidatesToCompare">加入推荐方案</el-button>
                <el-button size="small" plain icon="Delete" :disabled="!comparisonSchemes.length" @click="clearComparisonSchemes">清空对比</el-button>
              </div>
            </div>
            <div class="table-shell">
              <el-table :data="comparisonSchemes" stripe class="platform-table">
                <el-table-column label="方案" min-width="130">
                  <template #default="{ row }">
                    <div class="scheme-name-cell">
                      <strong>{{ row.schemeName }}</strong>
                      <el-tag v-if="row.schemeKey === activeComparisonSchemeKey" size="small" type="primary">当前建模</el-tag>
                      <el-tag v-if="row.schemeKey === finalComparisonSchemeKey" size="small" type="success">最终选用</el-tag>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="L1/mm" prop="L1" width="90" />
                <el-table-column label="L2/mm" prop="L2" width="90" />
                <el-table-column label="R/mm" prop="R" width="80" />
                <el-table-column label="弯曲角1/°" prop="theta1" width="105" />
                <el-table-column label="弯曲角2/°" prop="theta2" width="105" />
                <el-table-column label="预测应力/MPa" prop="predictedStress" width="130" />
                <el-table-column label="建模结果" min-width="150">
                  <template #default="{ row }">
                    <div class="scheme-result-cell">
                      <el-tag :type="cadStatusType(row.cadStatus)">{{ row.cadStatusLabel || '未建模' }}</el-tag>
                      <span>{{ row.cadResultLabel || '未生成参数模型' }}</span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="仿真结果" min-width="170">
                  <template #default="{ row }">
                    <div class="scheme-result-cell">
                      <el-tag :type="ansysStatusType(row.ansysStatus)">{{ row.ansysStatusLabel || '未仿真' }}</el-tag>
                      <span>{{ row.ansysResultLabel || '未生成仿真结果' }}</span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="验证结论" min-width="140">
                  <template #default="{ row }">
                    <span :class="['scheme-judgement', row.judgementClass || '']">{{ row.judgementLabel || '待验证' }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="140" fixed="right">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="setActiveComparisonScheme(row)">查看方案</el-button>
                    <el-button link type="danger" @click="removeComparisonScheme(row)">移除</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-if="!comparisonSchemes.length" description="请从 Top 候选方案中加入需要对比的方案" :image-size="80" />
            </div>
            <div class="ansys-parameter-panel mt-12">
              <div class="ansys-parameter-panel__head">
                <div>
                  <h4>ANSYS 工况参数</h4>
                  <span>{{ activeComparisonSchemeName }}</span>
                </div>
                <div class="ansys-parameter-actions">
                  <el-tag type="info">静力结构</el-tag>
                  <el-button size="small" plain icon="RefreshLeft" :disabled="!canEditAnsysParameters" @click="resetAnsysParametersFromFaultPipe">
                    载入原始参数
                  </el-button>
                </div>
              </div>
              <el-form :model="ansysParameterForm" label-position="top" class="ansys-parameter-form">
                <el-form-item label="初始压力 / Pa">
                  <el-input-number v-model="ansysParameterForm.pressure.initialPressurePa" :min="0" :controls="false" :disabled="!canEditAnsysParameters" />
                </el-form-item>
                <el-form-item label="峰值压力 / Pa">
                  <el-input-number v-model="ansysParameterForm.pressure.peakPressurePa" :min="0" :controls="false" :disabled="!canEditAnsysParameters" />
                </el-form-item>
                <el-form-item label="升压时间 / s">
                  <el-input-number v-model="ansysParameterForm.pressure.riseTimeS" :min="0.000001" :step="0.001" :precision="6" :controls="false" :disabled="!canEditAnsysParameters" />
                </el-form-item>
                <el-form-item label="网格尺寸 / mm">
                  <el-input-number v-model="ansysParameterForm.mesh.globalSizeMm" :min="0.1" :step="0.5" :precision="2" :controls="false" :disabled="!canEditAnsysParameters" />
                </el-form-item>
                <el-form-item label="材料名称">
                  <el-input v-model="ansysParameterForm.material.materialName" :disabled="!canEditAnsysParameters" />
                </el-form-item>
                <el-form-item label="弹性模量 / Pa">
                  <el-input-number v-model="ansysParameterForm.material.youngModulusPa" :min="0" :controls="false" :disabled="!canEditAnsysParameters" />
                </el-form-item>
                <el-form-item label="泊松比">
                  <el-input-number v-model="ansysParameterForm.material.poissonRatio" :min="0" :max="0.5" :step="0.01" :precision="3" :controls="false" :disabled="!canEditAnsysParameters" />
                </el-form-item>
                <el-form-item label="屈服强度 / Pa">
                  <el-input-number v-model="ansysParameterForm.material.tensileYieldStrengthPa" :min="0" :controls="false" :disabled="!canEditAnsysParameters" />
                </el-form-item>
                <el-form-item label="抗拉强度 / Pa">
                  <el-input-number v-model="ansysParameterForm.material.tensileUltimateStrengthPa" :min="0" :controls="false" :disabled="!canEditAnsysParameters" />
                </el-form-item>
                <el-form-item label="固定约束">
                  <el-select v-model="ansysParameterForm.boundary.fixedSupportMode" :disabled="!canEditAnsysParameters">
                    <el-option label="两端固定" value="both_ends" />
                    <el-option label="单端固定" value="single_end" />
                  </el-select>
                </el-form-item>
                <el-form-item label="压力加载面">
                  <div class="ansys-locked-value">
                    <el-tag type="info">管道内壁自动识别</el-tag>
                  </div>
                </el-form-item>
                <el-form-item label="载荷表达式" class="ansys-parameter-form__wide">
                  <el-input :model-value="ansysPressureExpressionPreview" disabled />
                </el-form-item>
              </el-form>
            </div>
            <div v-if="comparisonSchemes.length" class="scheme-model-compare">
              <div
                v-for="row in comparisonSchemes"
                :key="row.schemeKey"
                :class="['scheme-model-card', { 'is-active': row.schemeKey === activeComparisonSchemeKey, 'is-final': row.schemeKey === finalComparisonSchemeKey }]"
              >
                <div class="scheme-model-card__head">
                  <div>
                    <strong>{{ row.schemeName }}</strong>
                    <span>{{ row.cadStatus === 'SUCCESS' ? '已生成模型' : '等待建模' }}</span>
                  </div>
                </div>
                <cad-stl-viewer
                  :model-data="row.stlData || null"
                  :empty-text="row.cadStatus === 'SUCCESS' ? '模型文件未缓存，请切换后刷新或重新生成' : '生成参数模型后显示三维预览'"
                  height="260px"
                />
                <div class="scheme-model-card__actions">
                  <el-button size="small" type="primary" plain :disabled="!canEditVerification" :loading="row.schemeKey === activeComparisonSchemeKey && cadSubmitting" @click="generateSchemeModel(row)">
                    {{ row.cadStatus === 'SUCCESS' ? '重新建模' : '生成模型' }}
                  </el-button>
                  <el-button size="small" type="success" plain :disabled="!canEditVerification || row.cadStatus !== 'SUCCESS'" :loading="row.schemeKey === activeComparisonSchemeKey && ansysSubmitting" @click="runSchemeSimulation(row)">
                    打开 ANSYS
                  </el-button>
                  <el-button size="small" type="warning" plain :disabled="!canEditVerification" :loading="row.schemeKey === activeComparisonSchemeKey && ansysResultImporting" @click="importSchemeSimulationResult(row)">
                    导入结果
                  </el-button>
                  <el-button size="small" plain type="success" @click="setFinalComparisonScheme(row)">最终选用</el-button>
                </div>
                <div class="scheme-parameter-grid">
                  <div v-for="item in schemeParameterRows(row)" :key="item.label">
                    <span>{{ item.label }}</span>
                    <strong>{{ item.value }}</strong>
                    <em>{{ item.unit }}</em>
                  </div>
                </div>
                <div class="scheme-metric-list">
                  <div v-for="item in schemeMetricRows(row)" :key="item.name">
                    <span>{{ item.name }}</span>
                    <strong>{{ item.value }}</strong>
                    <em>{{ item.unit }}</em>
                  </div>
                </div>
                <div class="scheme-ansys-image">
                  <div class="scheme-ansys-image__head">
                    <strong>ANSYS 仿真效果图</strong>
                    <el-button v-if="row.ansysImageUrl" link type="primary" @click="previewSchemeAnsysImage(row)">放大查看</el-button>
                  </div>
                  <div class="scheme-ansys-image__body">
                    <img v-if="row.ansysImageUrl" :src="row.ansysImageUrl" alt="ANSYS 仿真效果图" />
                    <span v-else>{{ schemeAnsysImageEmptyText(row) }}</span>
                  </div>
                </div>
                <div class="scheme-model-card__foot">
                  <span>{{ row.cadResultLabel || '模型未生成' }}</span>
                  <span>{{ row.ansysResultLabel || '仿真未完成' }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="soft-panel report-panel mt-12">
            <div class="card-head report-head">
              <div>
                <h3 class="section-title">设计方案确认与报告提交</h3>
                <p>{{ reportStatusText }}</p>
              </div>
              <div class="report-actions">
                <el-button plain icon="Document" class="btn-soft-blue" :disabled="!canGenerateReport" @click="generateDesignReport">生成报告</el-button>
                <el-button plain icon="View" :disabled="!reportGeneratedAt" @click="reportDialogVisible = true">预览报告</el-button>
                <el-button plain icon="Download" :disabled="!reportGeneratedAt" @click="downloadDesignReport">下载报告</el-button>
                <el-button type="warning" plain icon="RefreshLeft" :disabled="!canReturnSimulationFailure" :loading="simulationReturnSubmitting" @click="returnSimulationForRework">
                  验证不通过并退回
                </el-button>
                <el-button type="success" icon="Upload" :disabled="!canSubmitDesignReport" :loading="reportSubmitting" @click="submitDesignReport">
                  提交设计方案
                </el-button>
              </div>
            </div>

            <div class="report-summary">
              <div v-for="item in reportSummaryItems" :key="item.label">
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
              </div>
            </div>

            <div class="report-compact">
              <el-alert
                class="mb-16"
                type="info"
                :closable="false"
                show-icon
                title="报告正文将在弹窗中预览，页面下方仅保留设计方案提交信息。"
              />
              <section v-if="showReportDecision">
                <el-form :model="reportDecision" label-width="96px" class="report-decision-form">
                  <el-form-item label="验证结论">
                    <el-radio-group v-model="reportDecision.passed" :disabled="!canEditReportDecision">
                      <el-radio :value="true">验证通过，提交当前设计方案</el-radio>
                      <el-radio :value="false">验证不通过，退回重新优化</el-radio>
                    </el-radio-group>
                  </el-form-item>
                  <el-form-item label="提交说明">
                    <el-input v-model="reportDecision.comment" type="textarea" :rows="3" :disabled="!canEditReportDecision" />
                  </el-form-item>
                </el-form>
              </section>
            </div>
          </div>
        </section>

        <section v-if="showCableLayoutWorkspace" class="section-block">
          <div class="section-header">
            <div>
              <h2 class="section-title">线缆管路布局优化</h2>
              <p class="section-subtitle">该子任务页面已预留，后续可接入线缆管路联合布局算法、包络校核、间隙校核和布线方案对比。</p>
            </div>
            <div class="action-row">
              <el-tag type="info">待设计</el-tag>
            </div>
          </div>
          <div class="subtask-empty-workspace">
            <el-empty description="线缆管路布局优化处理页面待配置" :image-size="110" />
          </div>
        </section>
      </template>
    </div>
    <el-dialog
      v-model="ansysImagePreviewVisible"
      title="应力云图"
      width="92vw"
      append-to-body
      class="ansys-image-dialog"
    >
      <div class="ansys-preview-scroll">
        <img v-if="ansysStressImage" :src="ansysStressImage" alt="应力云图预览" />
      </div>
    </el-dialog>
    <el-dialog
      v-model="reportDialogVisible"
      title="设计方案及验证报告"
      width="88vw"
      append-to-body
      class="design-report-dialog"
    >
      <div class="report-doc report-preview--dialog">
        <header class="report-cover">
          <div>
            <span>设计优化任务提交件</span>
            <h2>{{ taskTitle || '设计方案及验证报告' }}</h2>
            <p>报告编号：{{ reportCode }}　生成时间：{{ formatDateTime(reportGeneratedAt || '-') }}</p>
          </div>
          <el-tag :type="reportStatusTagType" size="large">{{ reportSubmitStatusLabel }}</el-tag>
        </header>

        <section class="report-section report-conclusion-section">
          <h4>一、审批结论摘要</h4>
          <table class="report-form-table">
            <tbody>
              <tr>
                <th>审批建议</th>
                <td colspan="3" class="report-decision-cell">{{ reportApprovalDecision }}</td>
              </tr>
              <tr>
                <th>结论说明</th>
                <td colspan="3">{{ reportConclusionText }}</td>
              </tr>
              <tr v-for="row in reportApprovalSummaryTableRows" :key="row[0].label">
                <template v-for="item in row" :key="item.label">
                  <th>{{ item.label }}</th>
                  <td>{{ item.value }}</td>
                </template>
              </tr>
            </tbody>
          </table>
        </section>

        <section class="report-section">
          <h4>二、任务与优化问题概述</h4>
          <p class="report-narrative">{{ reportProblemNarrative }}</p>
          <table class="report-form-table">
            <tbody>
              <tr v-for="row in reportProblemMetaTableRows" :key="row[0].label">
                <template v-for="item in row" :key="item.label">
                  <th>{{ item.label }}</th>
                  <td>{{ item.value }}</td>
                </template>
              </tr>
              <tr v-for="item in reportOptimizationSummaryItems" :key="item.label">
                <th>{{ item.label }}</th>
                <td colspan="3">{{ item.value }}</td>
              </tr>
            </tbody>
          </table>
        </section>

        <section class="report-section">
          <h4>三、最终选用方案</h4>
          <div class="report-visual-grid">
            <div class="report-visual-card report-visual-card--cad">
              <div class="report-visual-title">
                <strong>SolidWorks 参数化模型</strong>
                <span>{{ reportSelectedScheme?.schemeName || '当前方案' }}</span>
              </div>
              <cad-stl-viewer
                ref="reportCadViewerRef"
                :model-data="reportSelectedScheme?.stlData || cadStlData"
                empty-text="模型文件未生成或未缓存"
                height="320px"
              />
            </div>
            <div class="report-visual-card report-visual-card--ansys">
              <div class="report-visual-title">
                <strong>ANSYS 仿真效果图</strong>
                <span>{{ reportSelectedScheme?.ansysStatusLabel || ansysSimulation.statusLabel || '未仿真' }}</span>
              </div>
              <div class="report-ansys-figure">
                <img v-if="reportAnsysPreviewImage" :src="reportAnsysPreviewImage" alt="ANSYS 仿真效果图" />
                <span v-else>仿真效果图未生成或未缓存</span>
              </div>
            </div>
          </div>
          <table class="report-form-table">
            <tbody>
              <tr v-for="row in reportSelectedParameterTableRows" :key="row[0].label">
                <template v-for="item in row" :key="item.label">
                  <th>{{ item.label }}</th>
                  <td>{{ item.value }} {{ item.unit }}</td>
                </template>
              </tr>
            </tbody>
          </table>
        </section>

        <section class="report-section">
          <h4>四、候选方案对比与选用依据</h4>
          <p class="report-narrative">{{ reportSchemeComparisonNarrative }}</p>
          <table class="report-list-table">
            <thead>
              <tr>
                <th>方案</th>
                <th>预测应力</th>
                <th>建模结果</th>
                <th>仿真结果</th>
                <th>验证结论</th>
                <th>选用状态</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in reportSchemeComparisonRows" :key="item.schemeName">
                <td>{{ item.schemeName }}</td>
                <td>{{ item.predictedStress }}</td>
                <td>{{ item.cadStatusLabel }}</td>
                <td>{{ item.ansysResultLabel }}</td>
                <td>{{ item.judgementLabel }}</td>
                <td>{{ item.selectionLabel }}</td>
              </tr>
            </tbody>
          </table>
        </section>

        <section class="report-section">
          <h4>五、目标与约束满足性总结</h4>
          <p class="report-narrative">{{ reportConstraintNarrative }}</p>
          <table class="report-list-table">
            <thead>
              <tr>
                <th>类别</th>
                <th>结论</th>
                <th>说明</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in reportConstraintSummaryRows" :key="item.category">
                <td>{{ item.category }}</td>
                <td>{{ item.conclusion }}</td>
                <td>{{ item.detail }}</td>
              </tr>
            </tbody>
          </table>
        </section>

        <section class="report-section">
          <h4>六、仿真验证结果与工程风险</h4>
          <table class="report-list-table">
            <thead>
              <tr>
                <th>证据项</th>
                <th>状态/结果</th>
                <th>说明</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in reportEvidenceItems" :key="item.label">
                <td>{{ item.label }}</td>
                <td>{{ item.value }}</td>
                <td>{{ item.description }}</td>
              </tr>
            </tbody>
          </table>
          <table class="report-list-table mt-12">
            <thead>
              <tr>
                <th>类别</th>
                <th>工程说明</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in reportRiskRows" :key="item.label">
                <td>{{ item.label }}</td>
                <td>{{ item.value }}</td>
              </tr>
            </tbody>
          </table>
        </section>

        <section class="report-section">
          <h4>七、附录：设计变量与指标明细</h4>
          <table class="report-list-table">
            <thead>
              <tr>
                <th>学科</th>
                <th>变量名称</th>
                <th>初始值</th>
                <th>下限</th>
                <th>上限</th>
                <th>单位</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in reportVariableRows" :key="item.variableCode || item.variableName">
                <td>{{ item.disciplineName }}</td>
                <td>{{ item.variableName }}</td>
                <td>{{ item.initialValue }}</td>
                <td>{{ item.lowerBound }}</td>
                <td>{{ item.upperBound }}</td>
                <td>{{ item.unit }}</td>
              </tr>
            </tbody>
          </table>
          <table class="report-list-table mt-12">
            <thead>
              <tr>
                <th>指标</th>
                <th>数值</th>
                <th>单位</th>
                <th>来源</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in reportSimulationRows" :key="item.name">
                <td>{{ item.name }}</td>
                <td>{{ item.value }}</td>
                <td>{{ item.unit }}</td>
                <td>{{ item.source }}</td>
              </tr>
            </tbody>
          </table>
        </section>
      </div>
      <template #footer>
        <el-button @click="reportDialogVisible = false">关闭</el-button>
        <el-button plain icon="Download" :disabled="!reportGeneratedAt" @click="downloadDesignReport">下载报告</el-button>
        <el-button type="primary" @click="reportDialogVisible = false">确认报告内容</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { saveAs } from 'file-saver'
import * as echarts from 'echarts'
import {
  decomposeTask,
  getAnsysSimulationImage,
  getAnsysSimulationTask,
  getCadModelFile,
  getCadModelTask,
  getDashboard,
  getTaskAttachmentFile,
  getDesignTask,
  getSurrogateModels,
  getDesignVariableCatalog,
  getSurrogateSolveTask,
  confirmSurrogateSolveTask,
  importAnsysResultFile,
  openAnsysSimulationTask,
  runConflictCheck,
  saveObjectiveWeights,
  saveDesignVariables,
  submitCadModelTask,
  runSimulation,
  submitDesignReportTask,
  submitSurrogateSolveTask
} from '@/api/designtask/optimization'
import { updateTask as updateQualityTask } from '@/api/quality/task'
import { addLog as addQualityLog } from '@/api/quality/log'
import CadStlViewer from '../simulation/CadStlViewer.vue'
import ansysStressPlaceholder from '@/assets/designtask/ansys-stress-placeholder.png'

const route = useRoute()
const router = useRouter()
const taskId = ref(route.query.taskId ? Number(route.query.taskId) : null)
const taskTitle = ref('模型解耦求解')
const detail = ref({})
const access = ref({ mode: 'wait', label: '等待' })
const conflict = ref({ checked: false, passed: false, conflicts: [] })
const conflictReturnSubmitting = ref(false)
const subtasks = ref([])
const faultPipeParameters = ref({ groups: [] })
const simulation = ref({ verified: false, passed: null, metrics: [], conclusion: '' })
const surrogateSolve = ref({
  status: 'NOT_SUBMITTED',
  statusLabel: '未提交',
  modelName: 'aero_pipe_kriging.pkl',
  modelType: 'Kriging / Gaussian Process',
  objectiveName: 'predictedStress',
  objectiveUnit: 'MPa',
  bestSolution: {},
  candidates: [],
  history: [],
  iterations: 0,
  errorMessage: '',
  confirmed: false
})
const surrogateSubmitting = ref(false)
const surrogateRefreshing = ref(false)
const surrogateConfirming = ref(false)
const surrogateModelLoading = ref(false)
const surrogateModelOptions = ref([])
const selectedSurrogateModel = ref('aero_pipe_kriging.pkl')
let surrogatePollTimer = null
let cadPollTimer = null
let ansysPollTimer = null
let convergenceChart = null
const decomposed = ref(false)
const designVariables = ref([])
const variablesSaved = ref(false)
const variableLoading = ref(false)
const variableSaving = ref(false)
const objectiveWeightRows = ref([])
const objectiveWeightSaving = ref(false)
const inboxLoading = ref(false)
const pendingTasks = ref([])
const handledTasks = ref([])
const relatedTasks = ref([])
const activeTab = ref(['handled', 'related'].includes(route.query.tab) ? route.query.tab : 'pending')
const activeSubtaskCode = ref(route.query.subtask || 'hydraulic_impact')
const cadForm = ref({ L1: 280, L2: 150, R: 20, theta1: 110, theta2: 120, pipeDiameter: 9.53, pipeInnerDiameter: 7.73 })
const cadModel = ref({ status: 'NOT_SUBMITTED', statusLabel: '未提交', params: {}, files: {} })
const cadStlData = ref(null)
const cadPreviewImageUrl = ref('')
const cadPreviewImageKey = ref('')
const cadSubmitting = ref(false)
const cadRefreshing = ref(false)
const ansysSimulation = ref({ status: 'NOT_SUBMITTED', statusLabel: '未提交', metrics: [], placeholder: true })
const ansysParameterForm = ref(defaultAnsysParameterForm())
const ansysSubmitting = ref(false)
const ansysResultImporting = ref(false)
const ansysRefreshing = ref(false)
const ansysStressObjectUrl = ref('')
const ansysStressImageKey = ref('')
const ansysImagePreviewVisible = ref(false)
const selectedVerificationCandidate = ref(null)
const comparisonSchemes = ref([])
const activeComparisonSchemeKey = ref('')
const finalComparisonSchemeKey = ref('')
const convergenceChartRef = ref(null)
const reportCadViewerRef = ref(null)
const reportGeneratedAt = ref('')
const reportDialogVisible = ref(false)
const reportCadDocImageUrl = ref('')
const reportAnsysDocImageUrl = ref('')
const reportSubmitting = ref(false)
const simulationReturnSubmitting = ref(false)
const reportSubmission = ref({ submitted: false })
const reportDecision = ref({ passed: true, comment: '模型建模和仿真验证结果满足当前设计任务要求，提交当前设计方案。' })
const ANSYS_MODE_DEMO = 'DEMO_SIMULATION_MODEL'
const comparisonLimit = 5
const objectiveSelectNodeKeys = ['structure_select', 'layout_select', 'aero_select', 'hydraulic_select', 'manufacturing_select']
const SURROGATE_MODEL_DISPLAY_NAME = '液压弯管抗冲击代理模型02'

const hasTask = computed(() => !!taskId.value)
const readonlyMode = computed(() => access.value.mode !== 'enter' || route.query.mode === 'view')
const currentNodeKey = computed(() => detail.value.nodeKey || detail.value.task?.currentNodeKey || '')
const canCheckConflict = computed(() => !readonlyMode.value && currentNodeKey.value === 'conflict_check')
const conflictHasFailed = computed(() => conflict.value.checked && conflict.value.passed === false)
const conflictAlreadyReturned = computed(() => objectiveSelectNodeKeys.includes(currentNodeKey.value))
const canReturnConflictPlan = computed(() => hasTask.value && conflictHasFailed.value && (canCheckConflict.value || conflictAlreadyReturned.value))
const conflictReturnButtonText = computed(() => conflictAlreadyReturned.value ? '进入方案调整' : '退回方案调整')
const canEditObjectiveWeights = computed(() => !readonlyMode.value && ['conflict_check', 'model_decompose_solve'].includes(currentNodeKey.value))
const accessLabel = computed(() => access.value.label || (access.value.mode === 'enter' ? '可处理' : '查看'))
const readonlyReason = computed(() => {
  if (route.query.mode === 'view') return '当前以查看方式打开任务，设计变量只能查看，不能编辑。'
  return access.value.reason || '当前任务不在可处理状态，设计变量只能查看，不能编辑。'
})
const tabLabel = computed(() => ({ pending: '待处理', handled: '已处理', related: '相关任务' }[activeTab.value] || '我的任务'))
const visibleInboxTasks = computed(() => {
  if (activeTab.value === 'pending') return pendingTasks.value
  if (activeTab.value === 'handled') return handledTasks.value
  return relatedTasks.value
})
const selectedVariableCount = computed(() => designVariables.value.filter(item => item.checked).length)
const canSolve = computed(() => decomposed.value && variablesSaved.value && selectedVariableCount.value > 0)
const showVariableSection = computed(() => hasTask.value && decomposed.value)
const hasSolutions = computed(() => subtasks.value.some(item => (item.solutions || []).length > 0))
const hasSurrogateResult = computed(() => surrogateSolve.value.status && surrogateSolve.value.status !== 'NOT_SUBMITTED')
const subtaskWorkspaces = computed(() => {
  if (!decomposed.value) return []
  const byCode = new Map((subtasks.value || []).map(item => [item.subtaskCode, item]))
  return [
    {
      subtaskCode: 'hydraulic_impact',
      subtaskName: byCode.get('hydraulic_impact')?.subtaskName || '液压弯管抗冲击性能优化',
      recommended: byCode.get('hydraulic_impact')?.recommended,
      items: byCode.get('hydraulic_impact')?.items || []
    },
    {
      subtaskCode: 'cable_pipe_layout',
      subtaskName: byCode.get('cable_pipe_layout')?.subtaskName || '线缆管路布局优化',
      recommended: byCode.get('cable_pipe_layout')?.recommended,
      items: byCode.get('cable_pipe_layout')?.items || []
    }
  ]
})
const showHydraulicWorkspace = computed(() => hasTask.value && decomposed.value && activeSubtaskCode.value === 'hydraulic_impact')
const showCableLayoutWorkspace = computed(() => hasTask.value && decomposed.value && activeSubtaskCode.value === 'cable_pipe_layout')
const best = computed(() => surrogateSolve.value.bestSolution || {})
const bestSolutionReady = computed(() => Object.keys(best.value).length > 0)
const canConfirmSurrogate = computed(() => surrogateSolve.value.status === 'SUCCESS' && bestSolutionReady.value && !surrogateSolve.value.confirmed)
const convergenceSeries = computed(() => {
  return (surrogateSolve.value.history || [])
    .map((item, index) => {
      const value = Number(item.bestStress ?? item.objectiveValue ?? item.bestValue)
      return {
        iteration: Number(item.iteration ?? index + 1),
        value: Number.isFinite(value) ? value : null
      }
    })
    .filter(item => item.value !== null)
})
const convergenceStatItems = computed(() => {
  const rows = convergenceSeries.value
  if (!rows.length) return []
  const first = rows[0].value
  const last = rows[rows.length - 1].value
  const improvement = Number.isFinite(first) && first !== 0 ? ((first - last) / Math.abs(first)) * 100 : 0
  const tail = rows.slice(-5).map(item => item.value)
  const tailRange = tail.length ? Math.max(...tail) - Math.min(...tail) : 0
  return [
    { label: '初始目标值', value: formatChartValue(first) },
    { label: '当前最优值', value: formatChartValue(last) },
    { label: '累计改进', value: `${formatChartValue(improvement)}%` },
    { label: '最近波动', value: formatChartValue(tailRange) }
  ]
})
const canEditVerification = computed(() => !readonlyMode.value && ['model_decompose_solve', 'simulation_confirm'].includes(currentNodeKey.value))
const cadFormComplete = computed(() => ['L1', 'L2', 'R', 'theta1', 'theta2', 'pipeDiameter', 'pipeInnerDiameter'].every(key => cadForm.value[key] !== null && cadForm.value[key] !== undefined && cadForm.value[key] !== ''))
const canSubmitCadModel = computed(() => canEditVerification.value && hasSurrogateResult.value && cadFormComplete.value)
const canRunAnsysSimulation = computed(() => canEditVerification.value && cadModel.value.status === 'SUCCESS')
const canEditAnsysParameters = computed(() => canEditVerification.value)
const activeComparisonScheme = computed(() => {
  return comparisonSchemes.value.find(item => item.schemeKey === activeComparisonSchemeKey.value) || comparisonSchemes.value[0] || null
})
const activeComparisonSchemeName = computed(() => activeComparisonScheme.value?.schemeName || '当前方案')
const ansysPressureExpressionPreview = computed(() => {
  const pressure = ansysParameterForm.value.pressure || {}
  return pressureExpression(
    numberValue(pressure.initialPressurePa, 101325),
    numberValue(pressure.peakPressurePa, 30000000),
    numberValue(pressure.riseTimeS, 0.001)
  )
})
const canGenerateReport = computed(() => hasTask.value && bestSolutionReady.value)
const simulationBlocksReportSubmit = computed(() => simulation.value.verified && simulation.value.passed !== false)
const canEditReportDecision = computed(() => canEditVerification.value && !reportSubmission.value.submitted && !simulationBlocksReportSubmit.value)
const canReturnSimulationFailure = computed(() => {
  return hasTask.value &&
    canEditVerification.value &&
    ['model_decompose_solve', 'simulation_confirm'].includes(currentNodeKey.value) &&
    !reportSubmission.value.submitted &&
    !simulationBlocksReportSubmit.value
})
const canSubmitDesignReport = computed(() => {
  return canGenerateReport.value &&
    ['model_decompose_solve', 'simulation_confirm'].includes(currentNodeKey.value) &&
    canEditVerification.value &&
    reportGeneratedAt.value &&
    isReportCadComplete() &&
    isReportAnsysComplete() &&
    !reportSubmission.value.submitted &&
    !simulationBlocksReportSubmit.value
})
const showReportDecision = computed(() => ['model_decompose_solve', 'simulation_confirm', 'leader_approve'].includes(currentNodeKey.value) || simulation.value.verified || reportSubmission.value.submitted)
const reportStatusText = computed(() => {
  if (reportSubmission.value.submitted) return `设计方案报告已提交：${formatDateTime(reportSubmission.value.submitTime || reportGeneratedAt.value || '-')}`
  if (simulation.value.verified) return simulation.value.conclusion || '设计方案报告已提交，等待审批或归档。'
  if (!reportGeneratedAt.value) return '完成参数建模和仿真验证后，先生成报告，再提交当前设计方案。'
  return `报告已生成：${formatDateTime(reportGeneratedAt.value)}`
})
const reportCode = computed(() => {
  const task = detail.value.task || {}
  return `${task.taskNo || `DT-${taskId.value || '-'}`}-RPT`
})
const reportSubmitStatusLabel = computed(() => {
  if (reportSubmission.value.submitted) return '已提交审批'
  if (simulation.value.verified) return simulation.value.passed === false ? '已退回优化' : '已提交审批'
  if (reportGeneratedAt.value) return '待提交'
  return '待生成'
})
const reportStatusTagType = computed(() => {
  if (reportSubmission.value.submitted) return reportSubmission.value.passed === false ? 'warning' : 'success'
  if (simulation.value.verified) return simulation.value.passed === false ? 'warning' : 'success'
  return reportGeneratedAt.value ? 'primary' : 'info'
})
const reportSummaryItems = computed(() => [
  { label: '报告状态', value: reportSubmission.value.submitted ? '已提交' : (simulation.value.verified && simulation.value.passed === false ? '已退回' : (simulation.value.verified ? '已提交' : (reportGeneratedAt.value ? '已生成' : '待生成'))) },
  { label: '参数模型', value: cadModel.value.statusLabel || cadModel.value.status || '未提交' },
  { label: '仿真验证', value: ansysSimulation.value.statusLabel || ansysSimulation.value.status || '未提交' },
  { label: '当前节点', value: detail.value.task?.currentNodeName || currentNodeKey.value || '-' }
])
const reportProblemItems = computed(() => {
  const task = detail.value.task || {}
  return [
    { label: '任务名称', value: task.taskName || taskTitle.value || '-' },
    { label: '任务编号', value: task.taskNo || task.taskCode || '-' },
    { label: '优化对象', value: faultPipeParameters.value.faultSegmentName || '液压弯管/线缆管路布局对象' },
    { label: '材料', value: faultPipeParameters.value.materialName || '-' },
    { label: '任务阶段', value: task.currentNodeName || currentNodeKey.value || '-' },
    { label: '问题描述', value: task.remark || task.description || '针对当前航空装备管路与线缆布局任务，完成目标约束归口、设计变量统一选择、代理模型优化、参数化建模和仿真验证。' }
  ]
})
const reportProblemMetaItems = computed(() => reportProblemItems.value.filter(item => item.label !== '问题描述'))
const reportProblemDescription = computed(() => {
  return reportProblemItems.value.find(item => item.label === '问题描述')?.value || '-'
})
const reportConclusionText = computed(() => {
  if (simulation.value.verified) {
    return simulation.value.conclusion || (simulation.value.passed ? '当前设计方案已通过工程师验证并提交审批。' : '当前设计方案未通过验证，建议退回优化。')
  }
  if (cadModel.value.status === 'SUCCESS' && ansysSimulation.value.status === 'SUCCESS') {
    return '当前候选设计方案已完成参数化建模和 Ansys 仿真验证，可作为本轮设计优化任务的提交方案。审批前应重点核对目标约束、设计变量边界、最终参数值和仿真指标是否与工程要求一致。'
  }
  return '当前报告为阶段性预览，参数化建模或仿真验证尚未全部完成，暂不建议提交审批。'
})
const reportKeyMetricItems = computed(() => [
  { label: '优化目标数量', value: `${objectiveCount.value} 个` },
  { label: '设计约束数量', value: `${constraintCount.value} 个` },
  { label: '统一设计变量', value: `${selectedVariableCount.value} 个` },
  { label: '预测应力', value: `${valueOrDash(best.value.predictedStress)} ${surrogateSolve.value.objectiveUnit || 'MPa'}` },
  { label: '参数模型状态', value: reportSelectedScheme.value?.cadStatusLabel || cadModel.value.statusLabel || cadModel.value.status || '未提交' },
  { label: '仿真验证状态', value: reportSelectedScheme.value?.ansysStatusLabel || ansysSimulation.value.statusLabel || ansysSimulation.value.status || '未提交' }
])
const reportSelectedScheme = computed(() => {
  const schemes = comparisonSchemes.value || []
  return schemes.find(item => item.schemeKey === finalComparisonSchemeKey.value)
    || schemes.find(item => item.schemeKey === activeComparisonSchemeKey.value)
    || schemes.find(item => item.cadStatus === 'SUCCESS' || item.ansysStatus === 'SUCCESS')
    || schemes[0]
    || null
})
const reportSelectedAnsysImage = computed(() => {
  return reportSelectedScheme.value?.ansysImageUrl || ansysStressImage.value || ''
})
const reportSelectedCadImage = computed(() => {
  return reportSelectedScheme.value?.cadPreviewImageUrl || cadPreviewImageUrl.value || ''
})
const reportSelectedCadImageKey = computed(() => {
  return reportSelectedScheme.value?.cadPreviewImageKey || cadPreviewImageKey.value || ''
})
const reportAnsysPreviewImage = computed(() => {
  return reportAnsysDocImageUrl.value || reportSelectedAnsysImage.value
})

function isReportCadComplete() {
  return reportSelectedScheme.value?.cadStatus === 'SUCCESS' || cadModel.value.status === 'SUCCESS'
}

function isReportAnsysComplete() {
  return true
}

const reportApprovalDecision = computed(() => {
  if (simulation.value.verified && simulation.value.passed === false) return '建议退回优化'
  if (isReportCadComplete() && isReportAnsysComplete()) return '建议通过'
  return '需补充验证'
})
const reportApprovalSummaryItems = computed(() => [
  { label: '最终选用方案', value: reportSelectedScheme.value?.schemeName || '未标记' },
  { label: '代理模型预测', value: `${valueOrDash(reportSelectedScheme.value?.predictedStress ?? best.value.predictedStress)} ${surrogateSolve.value.objectiveUnit || 'MPa'}` },
  { label: '参数模型', value: reportSelectedScheme.value?.cadStatusLabel || cadModel.value.statusLabel || cadModel.value.status || '未提交' },
  { label: '仿真验证', value: reportSelectedScheme.value?.ansysStatusLabel || ansysSimulation.value.statusLabel || ansysSimulation.value.status || '未提交' },
  { label: '关键约束状态', value: reportConstraintStatusLabel.value },
  { label: '报告状态', value: reportSubmitStatusLabel.value }
])
const reportApprovalSummaryTableRows = computed(() => chunkReportRows(reportApprovalSummaryItems.value, 2))
const reportProblemNarrative = computed(() => {
  return `本报告用于审批液压弯管抗冲击性能优化设计方案。任务围绕降低冲击载荷下的管段应力响应展开，在满足几何、接口、禁布区、制造和维护相关约束的前提下，通过代理模型搜索候选方案，并对推荐方案完成参数化建模和 ANSYS 仿真验证。${reportProblemDescription.value ? `任务说明：${reportProblemDescription.value}` : ''}`
})
const reportProblemMetaTableRows = computed(() => chunkReportRows(reportProblemMetaItems.value, 2))
const reportOptimizationSummaryItems = computed(() => [
  { label: '优化目标', value: objectiveReportSummaryText.value },
  { label: '主要约束', value: constraintSummaryText.value },
  { label: '设计变量', value: reportDesignParameterRows.value.map(item => item.label).join('、') || '-' },
  { label: '验证方式', value: '代理模型预测 + SolidWorks 参数化建模 + ANSYS 仿真验证' }
])
const objectiveSummaryText = computed(() => {
  const names = reportObjectiveConstraintRows.value.filter(item => item.typeLabel === '目标').map(item => item.itemName)
  return names.length ? names.slice(0, 4).join('、') : '冲击应力最小'
})
const objectiveReportSummaryText = computed(() => {
  const names = reportObjectiveConstraintRows.value
    .filter(item => item.typeLabel === '目标')
    .map(item => item.itemName)
    .filter(Boolean)
  if (!names.length) {
    return '以冲击应力最小为主要优化方向；目标明细见后台目标约束数据。'
  }
  const visibleNames = names.slice(0, 4).join('、')
  const moreText = names.length > 4 ? `等 ${names.length} 项` : `共 ${names.length} 项`
  return `${visibleNames}，${moreText}；目标明细见后台目标约束数据。`
})
const constraintSummaryText = computed(() => {
  const names = reportObjectiveConstraintRows.value.filter(item => item.typeLabel === '约束').map(item => item.itemName)
  return names.length ? names.slice(0, 6).join('、') : '几何边界、接口位置、禁布区和制造可达性'
})
const reportSelectedSchemeParameters = computed(() => {
  return schemeParameterRows(reportSelectedScheme.value || {
    L1: cadForm.value.L1,
    L2: cadForm.value.L2,
    R: cadForm.value.R,
    theta1: cadForm.value.theta1,
    theta2: cadForm.value.theta2,
    pipeDiameter: cadForm.value.pipeDiameter,
    pipeInnerDiameter: cadForm.value.pipeInnerDiameter,
    predictedStress: best.value.predictedStress
  })
})
const reportSelectedParameterTableRows = computed(() => chunkReportRows(reportSelectedSchemeParameters.value, 2))
const reportSchemeComparisonRows = computed(() => {
  const schemes = comparisonSchemes.value.length ? comparisonSchemes.value : [reportSelectedScheme.value].filter(Boolean)
  return schemes.map(item => ({
    schemeName: item.schemeName || '-',
    predictedStress: `${valueOrDash(item.predictedStress)} ${surrogateSolve.value.objectiveUnit || 'MPa'}`,
    cadStatusLabel: item.cadStatusLabel || '未建模',
    ansysResultLabel: item.ansysResultLabel || item.ansysStatusLabel || '未仿真',
    judgementLabel: item.judgementLabel || '待验证',
    selectionLabel: item.schemeKey === finalComparisonSchemeKey.value ? '最终选用' : '备选'
  }))
})
const reportSchemeComparisonNarrative = computed(() => {
  const selected = reportSelectedScheme.value?.schemeName || '当前推荐方案'
  const stress = valueOrDash(reportSelectedScheme.value?.predictedStress ?? best.value.predictedStress)
  return `平台已对候选方案进行代理模型预测、参数化建模和仿真验证归集。综合预测应力、建模状态、仿真结果和约束满足情况，${selected} 作为当前推荐提交方案${stress !== '-' ? `，代理模型预测应力为 ${stress} ${surrogateSolve.value.objectiveUnit || 'MPa'}` : ''}。`
})
const reportConstraintStatusLabel = computed(() => {
  if (conflict.value.checked && conflict.value.passed === false) return '存在冲突'
  if (reportConstraintManualReviewRows.value.length) return '含人工复核项'
  return '总体满足'
})
const reportConstraintManualReviewRows = computed(() => {
  return reportObjectiveConstraintRows.value.filter(item => /人工|复核|维护|可达|装配|通过\/不通过/.test(`${item.itemName}${item.setting}${item.description}`))
})
const reportConstraintSummaryRows = computed(() => [
  {
    category: '优化目标',
    conclusion: objectiveSummaryText.value,
    detail: '当前报告不逐条堆叠目标明细，而是将目标作为优化方向进行归纳，详细条目保留在后台数据中用于追溯。'
  },
  {
    category: '硬约束',
    conclusion: conflict.value.checked && conflict.value.passed === false ? '存在待处理冲突' : '未发现阻断项',
    detail: constraintSummaryText.value
  },
  {
    category: '人工复核项',
    conclusion: reportConstraintManualReviewRows.value.length ? `${reportConstraintManualReviewRows.value.length} 项需复核` : '暂无显著人工复核项',
    detail: reportConstraintManualReviewRows.value.slice(0, 3).map(item => item.itemName).join('、') || '后续详细设计阶段仍建议结合装配空间和维护可达性复核。'
  },
  {
    category: '验证状态',
    conclusion: cadModel.value.status === 'SUCCESS' && ansysSimulation.value.status === 'SUCCESS' ? '建模与仿真已完成' : '验证未完全完成',
    detail: `参数模型：${cadModel.value.statusLabel || cadModel.value.status || '未提交'}；仿真验证：${ansysSimulation.value.statusLabel || ansysSimulation.value.status || '未提交'}。`
  }
])
const reportConstraintNarrative = computed(() => {
  return `本次目标与约束由各学科工程师归口后进入模型求解。报告按审批阅读习惯对目标和约束进行分组总结，重点呈现是否存在阻断性冲突、是否需要人工复核，以及当前方案是否具备提交审批条件。`
})
const reportRiskRows = computed(() => [
  {
    label: '主要优势',
    value: `${reportSelectedScheme.value?.schemeName || '当前方案'} 已形成参数化模型和仿真验证证据，便于审批人直接核对模型形态、应力云图和关键指标。`
  },
  {
    label: '潜在风险',
    value: reportConstraintManualReviewRows.value.length
      ? `存在 ${reportConstraintManualReviewRows.value.length} 项约束建议人工复核，重点关注维护可达性、装配空间或工程判据类约束。`
      : '未发现明显阻断性风险，但详细设计阶段仍建议结合整机布局模型进行复核。'
  },
  {
    label: '审批建议',
    value: reportApprovalDecision.value === '建议通过'
      ? '建议审批通过当前设计方案，并将模型文件、仿真结果和约束归口记录作为后续详细设计依据。'
      : '建议暂缓通过，待补充建模、仿真或约束复核后再提交审批。'
  }
])
const reportObjectiveConstraintRows = computed(() => {
  return objectiveSummaryGroups.value.flatMap(group => {
    const disciplineName = group.disciplineName || disciplineLabel(group.discipline)
    return (group.items || []).map(item => ({
      ...item,
      disciplineName,
      typeLabel: item.itemType === 'objective' ? '目标' : '约束',
      setting: item.itemType === 'objective'
        ? objectivePriorityLabel(normalizeDisplayWeight(item.weight))
        : [item.limitValue, item.unit].filter(Boolean).join(' ') || '-',
      description: item.description || item.remark || '-'
    }))
  })
})
const reportVariableRows = computed(() => designVariables.value
  .filter(item => item.checked)
  .map(item => ({
    ...item,
    initialValue: valueOrDash(item.initialValue),
    lowerBound: valueOrDash(item.lowerBound),
    upperBound: valueOrDash(item.upperBound),
    stepValue: valueOrDash(item.stepValue)
  })))
const reportDesignParameterRows = computed(() => [
  { label: 'L1', value: valueOrDash(cadForm.value.L1), unit: 'mm' },
  { label: 'L2', value: valueOrDash(cadForm.value.L2), unit: 'mm' },
  { label: 'R', value: valueOrDash(cadForm.value.R), unit: 'mm' },
  { label: '弯曲角1', value: valueOrDash(cadForm.value.theta1), unit: '°' },
  { label: '弯曲角2', value: valueOrDash(cadForm.value.theta2), unit: '°' },
  { label: '外径', value: valueOrDash(cadForm.value.pipeDiameter), unit: 'mm' },
  { label: '内径', value: valueOrDash(cadForm.value.pipeInnerDiameter), unit: 'mm' }
])
const reportSimulationRows = computed(() => {
  const ansysRows = (ansysSimulation.value.metrics || []).map(item => ({
    name: item.name || item.metricName || '-',
    value: valueOrDash(item.value ?? item.result ?? item.max),
    unit: item.unit || '-',
    source: item.source || ansysSimulation.value.simulationModelName || 'Ansys 仿真'
  }))
  if (ansysRows.length) return ansysRows
  return (simulation.value.metrics || []).map(item => ({
    name: item.name,
    value: valueOrDash(item.after),
    unit: item.unit || '-',
    source: '优化前后指标对比'
  }))
})
const reportEvidenceItems = computed(() => [
  {
    label: '参数模型',
    value: cadModel.value.statusLabel || cadModel.value.status || '未提交',
    description: `模型文件：${cadModel.value.files?.sldprt?.fileName || cadModel.value.files?.stl?.fileName || '未生成'}`
  },
  {
    label: '几何闭合',
    value: cadModel.value.closureStatus || '-',
    description: `L3：${valueOrDash(cadModel.value.l3)} mm；起始方向角：${valueOrDash(cadModel.value.initialAngle)}°`
  },
  {
    label: '仿真模型',
    value: ansysSimulation.value.simulationModelName || 'Ansys 仿真',
    description: ansysSimulation.value.simulationType || '对参数化模型进行结构/应力响应校核。'
  },
  {
    label: '应力云图',
    value: ansysStressImage.value ? '已生成' : '未生成',
    description: ansysSimulation.value.stressImageUrl || '仿真成功后生成应力云图并可在页面预览。'
  }
])
const verificationSelectionLabel = computed(() => {
  if (!selectedVerificationCandidate.value) return '先从 Top 候选方案中选用一条方案，系统会把参数带入下方输入框。'
  const rank = selectedVerificationCandidate.value.rank === '最优'
    ? '当前最优方案'
    : (selectedVerificationCandidate.value.rank ? `第 ${selectedVerificationCandidate.value.rank} 名候选方案` : selectedVerificationCandidate.value.label)
  const stressValue = valueOrDash(selectedVerificationCandidate.value.predictedStress)
  const stress = stressValue !== '-' ? `，代理模型预测应力 ${stressValue} MPa` : ''
  const finalLabel = finalComparisonSchemeKey.value === activeComparisonSchemeKey.value ? '，已标记为最终方案' : ''
  return `${rank || '已选候选方案'}${stress}${finalLabel}`
})
const ansysStressImage = computed(() => {
  if (ansysStressObjectUrl.value) return ansysStressObjectUrl.value
  if (ansysSimulation.value.placeholder && ansysSimulation.value.status === 'SUCCESS') return ansysStressPlaceholder
  return ''
})
const ansysImageEmptyText = computed(() => {
  if (ansysSimulation.value.status === 'FAILED') return '仿真失败，暂无应力云图'
  if (['QUEUED', 'RUNNING'].includes(ansysSimulation.value.status)) return '仿真处理中'
  return '生成参数模型并执行仿真后显示应力云图'
})
const cadEmptyText = computed(() => {
  if (cadModel.value.status === 'FAILED') return cadModel.value.errorMessage || '参数模型生成失败'
  if (['QUEUED', 'RUNNING'].includes(cadModel.value.status)) return '参数模型生成中'
  return '选用候选方案后生成参数模型'
})
const surrogateStatusType = computed(() => {
  return {
    QUEUED: 'info',
    RUNNING: 'warning',
    SUCCESS: 'success',
    CONFIRMED: 'success',
    FAILED: 'danger'
  }[surrogateSolve.value.status] || 'info'
})
const solveHint = computed(() => {
  if (!decomposed.value) return '请先执行任务解耦，再进行模型求解。'
  if (!variablesSaved.value || selectedVariableCount.value === 0) return '请先按解耦子任务选择并保存设计变量，再进行模型求解。'
  return ''
})
const subtaskLabelFallback = {
  hydraulic_impact: '液压弯管抗冲击性能优化',
  cable_pipe_layout: '线缆管路布局设计'
}
const variablesBySubtask = computed(() => {
  const subtaskNameMap = new Map(subtasks.value.map(item => [item.subtaskCode, item.subtaskName]))
  const groups = new Map()
  designVariables.value.forEach(item => {
    variableSubtaskCodes(item).forEach(subtaskCode => {
      if (!groups.has(subtaskCode)) {
        groups.set(subtaskCode, {
          subtaskCode,
          subtaskName: subtaskNameMap.get(subtaskCode) || subtaskLabelFallback[subtaskCode] || '未归类变量',
          items: []
        })
      }
      item.shared = variableSubtaskCodes(item).length > 1
      groups.get(subtaskCode).items.push(item)
    })
  })
  return Array.from(groups.values())
})
const faultPipeParameterGroups = computed(() => {
  return (faultPipeParameters.value.groups || []).filter(group => {
    return !['design_variable_baseline', 'pressure_load'].includes(group.groupCode)
      && !['管道设计变量基准', '入口压强载荷'].includes(group.groupName)
  })
})
const pressureLoadGroup = computed(() => {
  return (faultPipeParameters.value.groups || []).find(group => {
    return group.groupCode === 'pressure_load' || group.groupName === '入口压强载荷'
  })
})
const pressureLoadItems = computed(() => pressureLoadGroup.value?.items || [])
const selectedSurrogateModelInfo = computed(() => {
  return surrogateModelOptions.value.find(item => item.modelName === selectedSurrogateModel.value) || {
    modelName: selectedSurrogateModel.value || surrogateSolve.value.modelName || 'aero_pipe_kriging.pkl',
    displayName: selectedSurrogateModel.value || surrogateSolve.value.modelName || 'aero_pipe_kriging.pkl',
    modelType: surrogateSolve.value.modelType || 'Kriging / Gaussian Process',
    outputName: surrogateSolve.value.objectiveName || 'predictedStress',
    outputUnit: surrogateSolve.value.objectiveUnit || 'MPa'
  }
})
const surrogateInfoItems = computed(() => {
  const baseItems = [
    { label: '模型文件', value: selectedSurrogateModelInfo.value.modelName || surrogateSolve.value.modelName || 'aero_pipe_kriging.pkl' },
    { label: '模型类型', value: selectedSurrogateModelInfo.value.modelType || surrogateSolve.value.modelType || 'Kriging / Gaussian Process' },
    { label: '优化目标', value: '冲击应力最小' }
  ]
  const loadItems = pressureLoadItems.value.map(item => ({
    label: item.paramName,
    value: item.formulaText ? formatDecimalText(item.formulaText) : valueOrDash(item.paramValue),
    unit: item.paramUnit,
    description: item.description
  }))
  return baseItems.concat(loadItems)
})
const faultPipeSummaryItems = computed(() => {
  return [
    { label: '参数集', value: faultPipeParameters.value.setName },
    { label: '管段编号', value: faultPipeParameters.value.faultSegmentName },
    { label: '材料', value: faultPipeParameters.value.materialName }
  ].filter(item => item.value)
})
const objectiveSummaryGroups = computed(() => {
  return (detail.value.objectiveConstraints || []).map(group => {
    const items = group.items || []
    return {
      ...group,
      objectives: items.filter(item => item.itemType === 'objective'),
      constraints: items.filter(item => item.itemType === 'constraint')
    }
  })
})
const objectiveCount = computed(() => objectiveSummaryGroups.value.reduce((sum, group) => sum + group.objectives.length, 0))
const constraintCount = computed(() => objectiveSummaryGroups.value.reduce((sum, group) => sum + group.constraints.length, 0))
const constraintRows = computed(() => {
  return objectiveSummaryGroups.value.flatMap(group => {
    const disciplineName = group.disciplineName || disciplineLabel(group.discipline)
    return group.constraints.map(item => ({
      ...item,
      discipline: group.discipline,
      disciplineName
    }))
  })
})

const disciplines = [
  { value: 'structure', label: '结构' },
  { value: 'layout', label: '布局' },
  { value: 'aero', label: '气动' },
  { value: 'hydraulic', label: '液压' },
  { value: 'manufacturing', label: '制造' }
]

function loadInbox() {
  inboxLoading.value = true
  if (activeTab.value === 'pending') {
    getDashboard({ scope: 'related' }).then(res => {
      const rows = res.data?.tasks || []
      pendingTasks.value = rows.filter(row => ['conflict_check', 'model_decompose_solve', 'simulation_confirm', 'leader_approve'].includes(row.currentNodeKey))
    }).finally(() => {
      inboxLoading.value = false
    })
    return
  }

  if (activeTab.value === 'related') {
    getDashboard({ scope: 'related' }).then(res => {
      const rows = res.data?.tasks || []
      relatedTasks.value = rows
        .filter(row => ['conflict_check', 'model_decompose_solve', 'simulation_confirm', 'leader_approve', 'end'].includes(row.currentNodeKey))
        .map(row => ({
          ...row,
          action: { mode: 'view', label: '查看', reason: relatedReason(row.currentNodeKey) }
        }))
    }).finally(() => {
      inboxLoading.value = false
    })
    return
  }

  getDashboard({ scope: 'related' }).then(async res => {
    const rows = res.data?.tasks || []
    const handled = []
    for (const row of rows) {
      const detailData = await getDesignTask(row.taskId).then(result => result.data || {}).catch(() => null)
      if (isSolvedByCurrentUser(detailData)) {
        handled.push({
          ...row,
          action: { mode: 'view', label: '查看', reason: '已完成模型解耦求解' },
          handledReason: '已完成模型解耦求解'
        })
      }
    }
    handledTasks.value = handled
  }).finally(() => {
    inboxLoading.value = false
  })
}

function relatedReason(nodeKey) {
  if (nodeKey === 'leader_approve') {
    return '可查看设计方案报告并完成审批'
  }
  if (['simulation_confirm', 'end'].includes(nodeKey)) {
    return '可查看已生成的解耦求解结果'
  }
  if (nodeKey === 'model_decompose_solve') {
    return '解耦求解阶段，可查看当前归类结果'
  }
  return '等待解耦求解结果'
}

function isSolvedByCurrentUser(detailData) {
  if (!detailData?.task) return false
  const task = detailData.task
  const currentUserId = Number(detailData.currentUserId)
  const afterSolveNodes = ['simulation_confirm', 'leader_approve', 'end']
  return Number(task.ownerUserId) === currentUserId && afterSolveNodes.includes(task.currentNodeKey)
}

function changeTab() {
  router.replace({ path: '/designtask/solve', query: { tab: activeTab.value } })
  loadInbox()
}

function openTask(row) {
  const viewOnly = activeTab.value !== 'pending'
  router.push({
    path: '/designtask/solve',
    query: { taskId: row.taskId, mode: viewOnly ? 'view' : row.action?.mode || 'view', tab: activeTab.value }
  })
}

function backToInbox() {
  router.push({ path: '/designtask/solve', query: { tab: activeTab.value } })
}

function actionType(mode) {
  return { enter: 'primary', view: 'info', wait: 'info' }[mode] || 'info'
}

function openSubtaskWorkspace(subtaskCode) {
  if (!decomposed.value) return
  activeSubtaskCode.value = subtaskCode || 'hydraulic_impact'
  router.replace({
    path: '/designtask/solve',
    query: {
      ...route.query,
      taskId: taskId.value,
      subtask: activeSubtaskCode.value
    }
  })
}

function syncActiveSubtask() {
  const validCodes = subtaskWorkspaces.value.map(item => item.subtaskCode)
  if (!validCodes.length) {
    activeSubtaskCode.value = ''
    return
  }
  if (!validCodes.includes(activeSubtaskCode.value)) {
    activeSubtaskCode.value = validCodes[0] || 'hydraulic_impact'
  }
}

function loadDetail() {
  if (!taskId.value) {
    loadInbox()
    return
  }
  getDesignTask(taskId.value).then(res => {
    detail.value = res.data || {}
    taskTitle.value = detail.value.task?.taskName || '模型解耦求解'
    access.value = detail.value.access || { mode: 'wait', label: '等待' }
    conflict.value = detail.value.conflictCheck || conflict.value
    subtasks.value = detail.value.subtasks || []
    decomposed.value = Boolean(detail.value.decomposed)
    syncActiveSubtask()
    faultPipeParameters.value = detail.value.faultPipeParameters || { groups: [] }
    simulation.value = detail.value.simulation || simulation.value
    reportSubmission.value = detail.value.reportSubmission || { submitted: false }
    if (reportSubmission.value.submitted) {
      reportGeneratedAt.value = formatDateTime(reportSubmission.value.report?.generatedAt || reportSubmission.value.submitTime || reportGeneratedAt.value)
      reportDecision.value = {
        passed: reportSubmission.value.passed !== false,
        comment: reportSubmission.value.submitComment || reportDecision.value.comment
      }
    }
    surrogateSolve.value = detail.value.surrogateSolve || surrogateSolve.value
    if (surrogateSolve.value.modelName) {
      selectedSurrogateModel.value = surrogateSolve.value.modelName
    }
    syncVerificationCandidateFromBest()
    syncDefaultComparisonSchemes()
    applyCadModel(detail.value.cadModel)
    applyAnsysSimulation(detail.value.ansysSimulation)
    loadObjectiveWeights(detail.value)
    syncSurrogatePolling()
    loadSelectedVariables(detail.value)
    if (!readonlyMode.value && decomposed.value) {
      loadVariableCatalogs()
    }
  })
}

function loadSurrogateModels() {
  surrogateModelLoading.value = true
  getSurrogateModels().then(res => {
    const data = res.data || {}
    const models = Array.isArray(data.models) ? data.models : []
    surrogateModelOptions.value = models.length ? models : [{
      modelName: 'aero_pipe_kriging.pkl',
      displayName: SURROGATE_MODEL_DISPLAY_NAME,
      modelType: 'Kriging / Gaussian Process',
      outputName: 'predictedStress',
      outputUnit: 'MPa',
      exists: true,
      active: true
    }]
    const current = surrogateSolve.value.modelName || selectedSurrogateModel.value
    const active = data.activeModel || surrogateModelOptions.value.find(item => item.active)?.modelName
    selectedSurrogateModel.value = surrogateModelOptions.value.some(item => item.modelName === current)
      ? current
      : active || surrogateModelOptions.value[0]?.modelName || 'aero_pipe_kriging.pkl'
    if (data.errorMessage) {
      ElMessage.warning(data.errorMessage)
    }
  }).catch(() => {
    surrogateModelOptions.value = [{
      modelName: 'aero_pipe_kriging.pkl',
      displayName: SURROGATE_MODEL_DISPLAY_NAME,
      modelType: 'Kriging / Gaussian Process',
      outputName: 'predictedStress',
      outputUnit: 'MPa',
      exists: true,
      active: true
    }]
    selectedSurrogateModel.value = surrogateSolve.value.modelName || 'aero_pipe_kriging.pkl'
  }).finally(() => {
    surrogateModelLoading.value = false
  })
}

function check(passed) {
  if (!canCheckConflict.value) {
    ElMessage.warning('当前节点不可执行目标约束校验。')
    return
  }
  runConflictCheck(taskId.value, { passed, complete: passed }).then(res => {
    conflict.value = res.data || {}
    ElMessage[passed ? 'success' : 'warning'](passed ? '冲突校验通过' : '已模拟冲突校验不通过，请确认是否退回方案调整')
    loadDetail()
  })
}

function returnConflictPlan() {
  if (!canReturnConflictPlan.value) {
    ElMessage.warning('当前任务不能退回方案调整。')
    return
  }
  if (conflictAlreadyReturned.value) {
    router.push({ path: '/designtask/objective', query: { taskId: taskId.value } })
    return
  }
  conflictReturnSubmitting.value = true
  runConflictCheck(taskId.value, { passed: false, complete: true }).then(res => {
    conflict.value = res.data || conflict.value
    ElMessage.warning('已退回目标约束选择，请根据冲突建议调整方案。')
    loadDetail()
    router.push({ path: '/designtask/objective', query: { taskId: taskId.value } })
  }).finally(() => {
    conflictReturnSubmitting.value = false
  })
}

function decompose() {
  decomposeTask(taskId.value).then(res => {
    subtasks.value = res.data.subtasks || []
    decomposed.value = true
    syncActiveSubtask()
    loadVariableCatalogs()
    ElMessage.success('已解耦为两个子任务，请继续选择设计变量。')
  })
}

function loadObjectiveWeights(data) {
  objectiveWeightRows.value = (data.objectiveConstraints || []).flatMap(group => {
    return (group.items || [])
      .filter(item => item.itemType === 'objective')
      .map(item => ({
        ...item,
        discipline: group.discipline,
        disciplineName: group.disciplineName || disciplineLabel(group.discipline),
        weight: normalizeDisplayWeight(item.weight)
      }))
  })
}

function saveObjectiveWeightSettings() {
  if (!canEditObjectiveWeights.value) {
    ElMessage.warning('当前节点不可归口目标权重。')
    return
  }
  objectiveWeightSaving.value = true
  saveObjectiveWeights(taskId.value, {
    items: objectiveWeightRows.value.map(item => ({
      discipline: item.discipline,
      itemCode: item.itemCode,
      weight: normalizeDisplayWeight(item.weight)
    }))
  }).then(res => {
    detail.value = res.data || detail.value
    loadObjectiveWeights(detail.value)
    ElMessage.success('目标权重已统一保存。')
  }).finally(() => {
    objectiveWeightSaving.value = false
  })
}

function normalizeDisplayWeight(value) {
  const number = Number(value)
  if (!Number.isFinite(number)) return 5
  if (number > 10) return Math.max(0, Math.min(10, Math.round(number / 10)))
  return Math.max(0, Math.min(10, Math.round(number)))
}

function objectivePriorityLabel(weight) {
  const value = Number(weight)
  if (value >= 8) return '高优先级'
  if (value >= 5) return '中优先级'
  return '低优先级'
}

function objectivePriorityClass(weight) {
  const value = Number(weight)
  if (value >= 8) return 'priority-badge--high'
  if (value >= 5) return 'priority-badge--medium'
  return 'priority-badge--low'
}

function solve() {
  if (!canSolve.value) {
    ElMessage.warning(solveHint.value || '请先完成设计变量选择和任务解耦。')
    return
  }
  surrogateSubmitting.value = true
  submitSurrogateSolveTask(taskId.value, {
    modelName: selectedSurrogateModel.value,
    maxIterations: 80,
    populationSize: 15,
    seed: 42
  }).then(res => {
    surrogateSolve.value = res.data || surrogateSolve.value
    syncVerificationCandidateFromBest(true)
    syncDefaultComparisonSchemes()
    ElMessage.success('代理模型优化任务已提交')
    syncSurrogatePolling()
  }).finally(() => {
    surrogateSubmitting.value = false
  })
}

function refreshSurrogateSolve() {
  if (!taskId.value) return
  surrogateRefreshing.value = true
  getSurrogateSolveTask(taskId.value).then(res => {
    surrogateSolve.value = res.data || surrogateSolve.value
    syncVerificationCandidateFromBest()
    syncDefaultComparisonSchemes()
    syncSurrogatePolling()
  }).finally(() => {
    surrogateRefreshing.value = false
  })
}

function confirmSurrogate() {
  if (!canConfirmSurrogate.value) {
    ElMessage.warning('请先完成代理模型优化求解。')
    return
  }
  surrogateConfirming.value = true
  confirmSurrogateSolveTask(taskId.value).then(res => {
    surrogateSolve.value = res.data || surrogateSolve.value
    syncDefaultComparisonSchemes()
    ElMessage.success('已确认最优方案，流程进入仿真验证确认')
    stopSurrogatePolling()
    loadDetail()
  }).finally(() => {
    surrogateConfirming.value = false
  })
}

function syncSurrogatePolling() {
  if (['QUEUED', 'RUNNING'].includes(surrogateSolve.value.status)) {
    startSurrogatePolling()
  } else {
    stopSurrogatePolling()
  }
}

function startSurrogatePolling() {
  if (surrogatePollTimer || !taskId.value) return
  surrogatePollTimer = window.setInterval(() => {
    getSurrogateSolveTask(taskId.value).then(res => {
      surrogateSolve.value = res.data || surrogateSolve.value
      syncVerificationCandidateFromBest()
      syncDefaultComparisonSchemes()
      if (!['QUEUED', 'RUNNING'].includes(surrogateSolve.value.status)) {
        stopSurrogatePolling()
      }
    }).catch(() => {
      stopSurrogatePolling()
    })
  }, 2500)
}

function stopSurrogatePolling() {
  if (surrogatePollTimer) {
    window.clearInterval(surrogatePollTimer)
    surrogatePollTimer = null
  }
}

function renderConvergenceChart() {
  nextTick(() => {
    if (!convergenceChartRef.value || !convergenceSeries.value.length) {
      disposeConvergenceChart()
      return
    }
    if (!convergenceChart) {
      convergenceChart = echarts.init(convergenceChartRef.value)
    }
    const unit = surrogateSolve.value.objectiveUnit || 'MPa'
    const data = convergenceSeries.value.map(item => [item.iteration, item.value])
    convergenceChart.setOption({
      color: ['#176db6'],
      grid: { top: 28, right: 18, bottom: 36, left: 56 },
      tooltip: {
        trigger: 'axis',
        valueFormatter: value => `${formatChartValue(value)} ${unit}`
      },
      xAxis: {
        type: 'value',
        name: '迭代次数',
        minInterval: 1,
        axisLine: { lineStyle: { color: '#cbd5e1' } },
        axisLabel: { color: '#64748b' },
        splitLine: { show: false }
      },
      yAxis: {
        type: 'value',
        name: `目标值 / ${unit}`,
        scale: true,
        axisLabel: {
          color: '#64748b',
          formatter: value => formatChartValue(value)
        },
        splitLine: { lineStyle: { color: '#eef2f7' } }
      },
      series: [
        {
          name: '当前最优目标值',
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          data,
          lineStyle: { width: 3 },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(23, 109, 182, 0.22)' },
              { offset: 1, color: 'rgba(23, 109, 182, 0.02)' }
            ])
          },
          markPoint: {
            symbolSize: 46,
            label: {
              formatter: params => formatChartValue(params.value)
            },
            data: [{ type: 'min', name: '最优值' }]
          }
        }
      ]
    }, true)
  })
}

function disposeConvergenceChart() {
  if (!convergenceChart) return
  convergenceChart.dispose()
  convergenceChart = null
}

function resizeConvergenceChart() {
  convergenceChart?.resize()
}

function formatChartValue(value) {
  return valueOrDash(value)
}

function selectVerificationCandidate(row, silent = false) {
  if (!row) return
  reportGeneratedAt.value = ''
  selectedVerificationCandidate.value = {
    schemeKey: row.schemeKey || comparisonSchemeKey(row),
    rank: row.rank,
    label: row.label,
    predictedStress: row.predictedStress
  }
  activeComparisonSchemeKey.value = row.schemeKey || comparisonSchemeKey(row)
  cadForm.value = {
    L1: numberOrFallback(row.L1, cadForm.value.L1),
    L2: numberOrFallback(row.L2, cadForm.value.L2),
    R: numberOrFallback(row.R, cadForm.value.R),
    theta1: numberOrFallback(row.theta1, cadForm.value.theta1),
    theta2: numberOrFallback(row.theta2, cadForm.value.theta2),
    pipeDiameter: numberOrFallback(cadForm.value.pipeDiameter, 9.53),
    pipeInnerDiameter: numberOrFallback(cadForm.value.pipeInnerDiameter, 7.73)
  }
  if (!silent) {
    ElMessage.success('已将候选方案参数带入建模输入框。')
  }
}

function addCandidateToCompare(row, makeActive = false) {
  if (!row) return
  const scheme = normalizeComparisonScheme(row)
  const existed = comparisonSchemes.value.some(item => item.schemeKey === scheme.schemeKey)
  if (!existed) {
    if (comparisonSchemes.value.length >= comparisonLimit) {
      ElMessage.warning(`最多同时对比 ${comparisonLimit} 个方案。`)
      return
    }
    comparisonSchemes.value.push(scheme)
  }
  if (makeActive || !activeComparisonSchemeKey.value) {
    setActiveComparisonScheme(scheme)
  } else if (!existed) {
    ElMessage.success('已加入方案对比。')
  }
}

function addTopCandidatesToCompare() {
  const candidates = surrogateSolve.value.candidates || []
  if (!candidates.length) {
    ElMessage.warning('暂无候选方案可加入对比。')
    return
  }
  candidates.slice(0, Math.min(3, comparisonLimit)).forEach(row => addCandidateToCompare(row, false))
  if (!activeComparisonSchemeKey.value && comparisonSchemes.value.length) {
    setActiveComparisonScheme(comparisonSchemes.value[0])
  }
}

function clearComparisonSchemes() {
  resetComparisonState()
}

function resetComparisonState() {
  comparisonSchemes.value = []
  activeComparisonSchemeKey.value = ''
  finalComparisonSchemeKey.value = ''
  selectedVerificationCandidate.value = null
  clearCadPreviewImage()
  reportGeneratedAt.value = ''
}

function removeComparisonScheme(row) {
  const key = row.schemeKey || comparisonSchemeKey(row)
  comparisonSchemes.value = comparisonSchemes.value.filter(item => item.schemeKey !== key)
  if (activeComparisonSchemeKey.value === key) {
    const next = comparisonSchemes.value[0]
    activeComparisonSchemeKey.value = ''
    selectedVerificationCandidate.value = null
    if (next) setActiveComparisonScheme(next)
  }
  if (finalComparisonSchemeKey.value === key) {
    finalComparisonSchemeKey.value = ''
  }
  reportGeneratedAt.value = ''
}

function setActiveComparisonScheme(row, silent = false) {
  if (!row) return
  const scheme = normalizeComparisonScheme(row)
  if (!comparisonSchemes.value.some(item => item.schemeKey === scheme.schemeKey)) {
    if (comparisonSchemes.value.length >= comparisonLimit) {
      ElMessage.warning(`最多同时对比 ${comparisonLimit} 个方案。`)
      return
    }
    comparisonSchemes.value.push(scheme)
  }
  selectVerificationCandidate(scheme, true)
  cadModel.value = scheme.cadSnapshot || emptyCadModel()
  ansysSimulation.value = scheme.ansysSnapshot || emptyAnsysSimulation()
  if (scheme.stlData) {
    cadStlData.value = scheme.stlData
  } else if (cadModel.value.status === 'SUCCESS' && cadModel.value.files?.stl) {
    loadCadStl()
  } else {
    cadStlData.value = null
  }
  if (scheme.cadPreviewImageUrl) {
    cadPreviewImageUrl.value = scheme.cadPreviewImageUrl
    cadPreviewImageKey.value = scheme.cadPreviewImageKey || ''
  } else if (cadModel.value.status === 'SUCCESS' && cadModel.value.files?.previewPng) {
    loadCadPreviewImage()
  } else {
    clearCadPreviewImage()
  }
  if (ansysSimulation.value.status === 'SUCCESS') {
    if (scheme.ansysImageUrl) {
      ansysStressObjectUrl.value = scheme.ansysImageUrl
      ansysStressImageKey.value = scheme.ansysImageKey || ''
    } else {
      loadAnsysStressImage()
    }
  } else {
    clearAnsysStressImage()
  }
  if (!silent) {
    ElMessage.success('已切换当前建模方案。')
  }
}

function setFinalComparisonScheme(row) {
  if (!row) return
  const scheme = normalizeComparisonScheme(row)
  finalComparisonSchemeKey.value = scheme.schemeKey
  if (activeComparisonSchemeKey.value !== scheme.schemeKey) {
    setActiveComparisonScheme(scheme, true)
  }
  reportGeneratedAt.value = ''
  ElMessage.success('已标记为最终选用方案。')
}

function syncDefaultComparisonSchemes() {
  if (comparisonSchemes.value.length) return
  const candidates = surrogateSolve.value.candidates || []
  if (candidates.length) {
    candidates.slice(0, Math.min(3, comparisonLimit)).forEach(row => {
      comparisonSchemes.value.push(normalizeComparisonScheme(row))
    })
    setActiveComparisonScheme(comparisonSchemes.value[0], true)
    return
  }
  if (bestSolutionReady.value) {
    const scheme = normalizeComparisonScheme({ ...best.value, rank: '最优', label: '当前最优方案' })
    comparisonSchemes.value.push(scheme)
    setActiveComparisonScheme(scheme, true)
  }
}

function normalizeComparisonScheme(row) {
  const rank = row.rank || row.label || comparisonSchemes.value.length + 1
  const schemeKey = row.schemeKey || comparisonSchemeKey(row)
  return {
    ...row,
    schemeKey,
    schemeName: row.schemeName || (rank === '最优' ? '当前最优方案' : `候选方案 ${rank}`),
    rank,
    L1: valueOrDash(row.L1),
    L2: valueOrDash(row.L2),
    R: valueOrDash(row.R),
    theta1: valueOrDash(row.theta1),
    theta2: valueOrDash(row.theta2),
    pipeDiameter: valueOrDash(row.pipeDiameter || cadForm.value.pipeDiameter),
    pipeInnerDiameter: valueOrDash(row.pipeInnerDiameter || cadForm.value.pipeInnerDiameter),
    predictedStress: valueOrDash(row.predictedStress),
    cadStatus: row.cadStatus || 'NOT_SUBMITTED',
    cadStatusLabel: row.cadStatusLabel || '未建模',
    cadResultLabel: row.cadResultLabel || '',
    cadSnapshot: row.cadSnapshot || null,
    cadPreviewImageUrl: row.cadPreviewImageUrl || '',
    cadPreviewImageKey: row.cadPreviewImageKey || '',
    ansysStatus: row.ansysStatus || 'NOT_SUBMITTED',
    ansysStatusLabel: row.ansysStatusLabel || '未仿真',
    ansysResultLabel: row.ansysResultLabel || '',
    ansysSnapshot: row.ansysSnapshot || null,
    ansysMetrics: row.ansysMetrics || [],
    ansysImageUrl: row.ansysImageUrl || '',
    ansysImageKey: row.ansysImageKey || '',
    stlData: row.stlData || null,
    judgementLabel: row.judgementLabel || '待验证',
    judgementClass: row.judgementClass || ''
  }
}

function comparisonSchemeKey(row) {
  if (row.schemeKey) return row.schemeKey
  if (row.solutionCode) return `solution-${row.solutionCode}`
  if (row.rank) return `rank-${row.rank}`
  return ['scheme', row.L1, row.L2, row.R, row.theta1, row.theta2].join('-')
}

function syncVerificationCandidateFromBest(force = false) {
  if (!bestSolutionReady.value) return
  if (!force && selectedVerificationCandidate.value) return
  if (!force && cadModel.value.params && Object.keys(cadModel.value.params).length) return
  selectVerificationCandidate({ ...best.value, rank: '最优', label: '当前最优方案' }, true)
}

function numberValue(value, fallback) {
  const number = Number(value)
  return Number.isFinite(number) ? number : fallback
}

function faultPipeValue(code, fallback = '') {
  const values = faultPipeParameters.value.values || {}
  if (values[code] !== undefined && values[code] !== null && values[code] !== '') {
    return values[code]
  }
  for (const group of faultPipeParameters.value.groups || []) {
    const item = (group.items || []).find(row => row.paramCode === code)
    if (item && item.paramValue !== undefined && item.paramValue !== null && item.paramValue !== '') {
      return item.paramValue
    }
  }
  return fallback
}

function pressureExpression(initialPressurePa, peakPressurePa, riseTimeS) {
  const safeRiseTimeS = Math.max(numberValue(riseTimeS, 0.001), 0.000001)
  return `IF(t <= ${safeRiseTimeS}, ${initialPressurePa} + (${peakPressurePa} - ${initialPressurePa}) * t / ${safeRiseTimeS}, ${peakPressurePa})`
}

function defaultAnsysParameterForm() {
  const initialPressurePa = numberValue(faultPipeValue('INLET_PRESSURE_INITIAL'), 101325)
  const peakPressurePa = numberValue(faultPipeValue('INLET_PRESSURE_PEAK'), 30000000)
  const riseTimeS = numberValue(faultPipeValue('INLET_PRESSURE_RISE_TIME'), 0.001)
  return {
    analysisType: 'static_structural',
    pressure: {
      initialPressurePa,
      peakPressurePa,
      riseTimeS
    },
    material: {
      materialName: faultPipeValue('MATERIAL_NAME', faultPipeParameters.value.materialName || 'Structural Steel'),
      youngModulusPa: numberValue(faultPipeValue('YOUNG_MODULUS'), 1.93e11),
      poissonRatio: numberValue(faultPipeValue('POISSON_RATIO'), 0.31),
      tensileYieldStrengthPa: numberValue(faultPipeValue('TENSILE_YIELD_STRENGTH'), 2.07e8),
      tensileUltimateStrengthPa: numberValue(faultPipeValue('TENSILE_ULTIMATE_STRENGTH'), 5.86e8)
    },
    mesh: {
      globalSizeMm: 3
    },
    boundary: {
      fixedSupportMode: 'both_ends',
      pressureFaceMode: 'inner_wall'
    },
    result: {
      equivalentStress: true,
      totalDeformation: true,
      exportStressImage: true
    }
  }
}

function applyAnsysParameters(parameters) {
  const fallback = defaultAnsysParameterForm()
  const pressure = parameters?.pressure || {}
  const material = parameters?.material || {}
  const mesh = parameters?.mesh || {}
  const boundary = parameters?.boundary || {}
  const result = parameters?.result || {}
  ansysParameterForm.value = {
    analysisType: parameters?.analysisType || fallback.analysisType,
    pressure: {
      initialPressurePa: numberValue(pressure.initialPressurePa, fallback.pressure.initialPressurePa),
      peakPressurePa: numberValue(pressure.peakPressurePa, fallback.pressure.peakPressurePa),
      riseTimeS: numberValue(pressure.riseTimeS, fallback.pressure.riseTimeS)
    },
    material: {
      materialName: material.materialName || fallback.material.materialName,
      youngModulusPa: numberValue(material.youngModulusPa, fallback.material.youngModulusPa),
      poissonRatio: numberValue(material.poissonRatio, fallback.material.poissonRatio),
      tensileYieldStrengthPa: numberValue(material.tensileYieldStrengthPa, fallback.material.tensileYieldStrengthPa),
      tensileUltimateStrengthPa: numberValue(material.tensileUltimateStrengthPa, fallback.material.tensileUltimateStrengthPa)
    },
    mesh: {
      globalSizeMm: numberValue(mesh.globalSizeMm ?? mesh.meshSizeMm, fallback.mesh.globalSizeMm)
    },
    boundary: {
      fixedSupportMode: boundary.fixedSupportMode || fallback.boundary.fixedSupportMode,
      pressureFaceMode: fallback.boundary.pressureFaceMode
    },
    result: {
      equivalentStress: result.equivalentStress !== false,
      totalDeformation: result.totalDeformation !== false,
      exportStressImage: result.exportStressImage !== false
    }
  }
}

function resetAnsysParametersFromFaultPipe(showMessage = true) {
  applyAnsysParameters(defaultAnsysParameterForm())
  reportGeneratedAt.value = ''
  if (showMessage) {
    ElMessage.success('已载入原始管段参数。')
  }
}

function buildAnsysPayload(extra = {}) {
  const form = ansysParameterForm.value
  const initialPressurePa = numberValue(form.pressure.initialPressurePa, 101325)
  const peakPressurePa = numberValue(form.pressure.peakPressurePa, 30000000)
  const riseTimeS = numberValue(form.pressure.riseTimeS, 0.001)
  return {
    simulationMode: ANSYS_MODE_DEMO,
    params: {
      ...cadForm.value,
      activeSchemeKey: activeComparisonSchemeKey.value,
      activeSchemeName: activeComparisonSchemeName.value
    },
    simulationParameters: {
      analysisType: form.analysisType || 'static_structural',
      pressure: {
        initialPressurePa,
        peakPressurePa,
        riseTimeS,
        expression: ansysPressureExpressionPreview.value
      },
      material: {
        materialName: form.material.materialName || 'Structural Steel',
        youngModulusPa: numberValue(form.material.youngModulusPa, 1.93e11),
        poissonRatio: numberValue(form.material.poissonRatio, 0.31),
        tensileYieldStrengthPa: numberValue(form.material.tensileYieldStrengthPa, 2.07e8),
        tensileUltimateStrengthPa: numberValue(form.material.tensileUltimateStrengthPa, 5.86e8)
      },
      mesh: {
        globalSizeMm: numberValue(form.mesh.globalSizeMm, 3)
      },
      boundary: {
        fixedSupportMode: form.boundary.fixedSupportMode || 'both_ends',
        pressureFaceMode: 'inner_wall'
      },
      result: {
        equivalentStress: form.result.equivalentStress !== false,
        totalDeformation: form.result.totalDeformation !== false,
        exportStressImage: form.result.exportStressImage !== false
      }
    },
    ...extra
  }
}

function numberOrFallback(value, fallback) {
  const number = Number(value)
  return Number.isFinite(number) ? number : fallback
}

function isNumericDisplayValue(value) {
  if (typeof value === 'number') return Number.isFinite(value)
  if (typeof value !== 'string') return false
  const text = value.trim()
  return text !== '' && /^-?(?:\d+|\d*\.\d+)(?:e[+-]?\d+)?$/i.test(text)
}

function formatDisplayNumber(value) {
  const number = Number(value)
  if (!Number.isFinite(number)) return String(value)
  const source = typeof value === 'string' ? value.trim() : ''
  if (Number.isInteger(number) && !source.includes('.')) {
    return String(number)
  }
  return number.toFixed(2)
}

function formatDecimalText(value) {
  if (value === null || value === undefined || value === '') return ''
  return String(value).replace(/-?\d+\.\d+(?:e[+-]?\d+)?/gi, match => {
    const number = Number(match)
    return Number.isFinite(number) ? number.toFixed(2) : match
  })
}

function valueOrDash(value) {
  if (value === null || value === undefined || value === '') return '-'
  if (isNumericDisplayValue(value)) return formatDisplayNumber(value)
  return String(value)
}

function chunkReportRows(items, size) {
  const rows = []
  const normalized = (items || []).map(item => ({
    label: item.label || '-',
    value: item.value ?? '-',
    unit: item.unit || ''
  }))
  for (let index = 0; index < normalized.length; index += size) {
    const row = normalized.slice(index, index + size)
    while (row.length < size) {
      row.push({ label: '', value: '', unit: '' })
    }
    rows.push(row)
  }
  return rows
}

function emptyCadModel() {
  return { status: 'NOT_SUBMITTED', statusLabel: '未提交', params: {}, files: {} }
}

function emptyAnsysSimulation() {
  return { status: 'NOT_SUBMITTED', statusLabel: '未提交', metrics: [], placeholder: true }
}

function updateActiveComparisonCadResult(model) {
  const key = activeComparisonSchemeKey.value
  if (!key) return
  const index = comparisonSchemes.value.findIndex(item => item.schemeKey === key)
  if (index < 0) return
  const status = model?.status || 'NOT_SUBMITTED'
  const fileName = model?.files?.sldprt?.fileName || model?.files?.stl?.fileName || ''
  const resultLabel = status === 'SUCCESS'
    ? (fileName ? `模型已生成：${fileName}` : '参数模型已生成')
    : (model?.errorMessage || model?.statusLabel || '未生成参数模型')
  comparisonSchemes.value[index] = {
    ...comparisonSchemes.value[index],
    cadStatus: status,
    cadStatusLabel: model?.statusLabel || status || '未提交',
    cadResultLabel: resultLabel,
    cadSnapshot: model ? { ...model } : null,
    stlData: status === 'SUCCESS' ? comparisonSchemes.value[index].stlData : null,
    cadPreviewImageUrl: status === 'SUCCESS' ? comparisonSchemes.value[index].cadPreviewImageUrl : '',
    cadPreviewImageKey: status === 'SUCCESS' ? comparisonSchemes.value[index].cadPreviewImageKey : '',
    judgementLabel: status === 'FAILED' ? '建模未通过' : comparisonSchemes.value[index].judgementLabel,
    judgementClass: status === 'FAILED' ? 'is-failed' : comparisonSchemes.value[index].judgementClass
  }
}

function updateActiveComparisonStlData(data) {
  const key = activeComparisonSchemeKey.value
  if (!key) return
  const index = comparisonSchemes.value.findIndex(item => item.schemeKey === key)
  if (index < 0) return
  comparisonSchemes.value[index] = {
    ...comparisonSchemes.value[index],
    stlData: data || null
  }
}

function updateActiveComparisonCadPreviewImage(imageUrl, imageKey) {
  const key = activeComparisonSchemeKey.value
  if (!key) return
  const index = comparisonSchemes.value.findIndex(item => item.schemeKey === key)
  if (index < 0) return
  comparisonSchemes.value[index] = {
    ...comparisonSchemes.value[index],
    cadPreviewImageUrl: imageUrl || '',
    cadPreviewImageKey: imageKey || ''
  }
}

function updateActiveComparisonAnsysResult(model) {
  const key = activeComparisonSchemeKey.value
  if (!key) return
  const index = comparisonSchemes.value.findIndex(item => item.schemeKey === key)
  if (index < 0) return
  const status = model?.status || 'NOT_SUBMITTED'
  const stressValue = getAnsysStressValue(model)
  const resultLabel = status === 'SUCCESS'
    ? (stressValue ? `最大应力 ${stressValue}` : '仿真结果已生成')
    : (model?.errorMessage || model?.statusLabel || '未生成仿真结果')
  comparisonSchemes.value[index] = {
    ...comparisonSchemes.value[index],
    ansysStatus: status,
    ansysStatusLabel: model?.statusLabel || status || '未提交',
    ansysResultLabel: resultLabel,
    ansysSnapshot: model ? { ...model } : null,
    ansysMetrics: model?.metrics || [],
    ansysImageUrl: status === 'SUCCESS' ? comparisonSchemes.value[index].ansysImageUrl : '',
    ansysImageKey: status === 'SUCCESS' ? comparisonSchemes.value[index].ansysImageKey : '',
    judgementLabel: schemeJudgementLabel(status),
    judgementClass: schemeJudgementClass(status)
  }
}

function updateActiveComparisonAnsysImage(imageUrl, imageKey) {
  const key = activeComparisonSchemeKey.value
  if (!key) return
  const index = comparisonSchemes.value.findIndex(item => item.schemeKey === key)
  if (index < 0) return
  comparisonSchemes.value[index] = {
    ...comparisonSchemes.value[index],
    ansysImageUrl: imageUrl || '',
    ansysImageKey: imageKey || ''
  }
}

function getAnsysStressValue(model) {
  const metrics = model?.metrics || []
  const stress = metrics.find(item => /应力|stress/i.test(item.name || item.metricName || '')) || metrics[0]
  if (!stress) return ''
  const value = stress.value ?? stress.result ?? stress.max
  const unit = stress.unit || 'MPa'
  return value === null || value === undefined || value === '' ? '' : `${valueOrDash(value)} ${unit}`
}

function schemeJudgementLabel(status) {
  if (status === 'SUCCESS') return '可作为候选'
  if (status === 'FAILED') return '仿真未通过'
  if (['QUEUED', 'RUNNING'].includes(status)) return '验证中'
  return '待验证'
}

function schemeJudgementClass(status) {
  if (status === 'SUCCESS') return 'is-passed'
  if (status === 'FAILED') return 'is-failed'
  if (['QUEUED', 'RUNNING'].includes(status)) return 'is-running'
  return ''
}

function schemeParameterRows(row) {
  return [
    { label: 'L1', value: valueOrDash(row.L1), unit: 'mm' },
    { label: 'L2', value: valueOrDash(row.L2), unit: 'mm' },
    { label: 'R', value: valueOrDash(row.R), unit: 'mm' },
    { label: '弯曲角1', value: valueOrDash(row.theta1), unit: '°' },
    { label: '弯曲角2', value: valueOrDash(row.theta2), unit: '°' },
    { label: '外径', value: valueOrDash(row.pipeDiameter), unit: 'mm' },
    { label: '内径', value: valueOrDash(row.pipeInnerDiameter), unit: 'mm' },
    { label: '预测应力', value: valueOrDash(row.predictedStress), unit: surrogateSolve.value.objectiveUnit || 'MPa' }
  ]
}

function schemeMetricRows(row) {
  const metrics = row.ansysMetrics || []
  if (metrics.length) {
    return metrics.map(item => ({
      name: item.name || item.metricName || '仿真指标',
      value: valueOrDash(item.value ?? item.result ?? item.max),
      unit: item.unit || '-'
    }))
  }
  return [
    { name: 'ANSYS 最大应力', value: '-', unit: surrogateSolve.value.objectiveUnit || 'MPa' },
    { name: '仿真状态', value: row.ansysStatusLabel || '未仿真', unit: '-' }
  ]
}

function schemeAnsysImageEmptyText(row) {
  if (row.ansysStatus === 'FAILED') return '仿真失败，暂无效果图'
  if (['QUEUED', 'RUNNING'].includes(row.ansysStatus)) return '仿真处理中'
  if (row.ansysStatus === 'SUCCESS') return '仿真完成，效果图未缓存'
  return '完成仿真验证后显示效果图'
}

function previewSchemeAnsysImage(row) {
  if (!row.ansysImageUrl) return
  ansysStressObjectUrl.value = row.ansysImageUrl
  ansysStressImageKey.value = row.ansysImageKey || row.schemeKey || ''
  ansysImagePreviewVisible.value = true
}

function generateSchemeModel(row) {
  setActiveComparisonScheme(row, true)
  submitCadModel()
}

function runSchemeSimulation(row) {
  setActiveComparisonScheme(row, true)
  startAnsysSimulation()
}

function importSchemeSimulationResult(row) {
  setActiveComparisonScheme(row, true)
  importAnsysResult()
}

function submitCadModel() {
  if (!canSubmitCadModel.value) {
    ElMessage.warning('请先选用候选方案并确认建模参数。')
    return
  }
  reportGeneratedAt.value = ''
  cadSubmitting.value = true
  submitCadModelTask(taskId.value, {
    ...cadForm.value,
    useSurrogateBest: false,
    candidateRank: selectedVerificationCandidate.value?.rank
  }).then(res => {
    applyCadModel(res.data)
    ElMessage.success('参数建模任务已提交')
  }).finally(() => {
    cadSubmitting.value = false
  })
}

function refreshVerificationStatus() {
  refreshCadModel()
  refreshAnsysSimulation()
}

function refreshCadModel(silent = false) {
  if (!taskId.value) return
  if (!silent) cadRefreshing.value = true
  getCadModelTask(taskId.value).then(res => {
    applyCadModel(res.data)
  }).finally(() => {
    if (!silent) cadRefreshing.value = false
  })
}

function applyCadModel(model) {
  cadModel.value = model || emptyCadModel()
  updateActiveComparisonCadResult(cadModel.value)
  if (cadModel.value.params && Object.keys(cadModel.value.params).length) {
    cadForm.value = {
      L1: numberOrFallback(cadModel.value.params.L1, cadForm.value.L1),
      L2: numberOrFallback(cadModel.value.params.L2, cadForm.value.L2),
      R: numberOrFallback(cadModel.value.params.R, cadForm.value.R),
      theta1: numberOrFallback(cadModel.value.params.theta1, cadForm.value.theta1),
      theta2: numberOrFallback(cadModel.value.params.theta2, cadForm.value.theta2),
      pipeDiameter: numberOrFallback(cadModel.value.params.pipeDiameter, cadForm.value.pipeDiameter),
      pipeInnerDiameter: numberOrFallback(cadModel.value.params.pipeInnerDiameter, cadForm.value.pipeInnerDiameter)
    }
  }
  if (cadModel.value.status === 'SUCCESS' && cadModel.value.files?.stl) {
    loadCadStl()
  } else if (cadModel.value.status !== 'SUCCESS') {
    cadStlData.value = null
  }
  if (cadModel.value.status === 'SUCCESS' && cadModel.value.files?.previewPng) {
    loadCadPreviewImage()
  } else if (cadModel.value.status !== 'SUCCESS') {
    clearCadPreviewImage()
    updateActiveComparisonCadPreviewImage('', '')
  }
  if (['QUEUED', 'RUNNING'].includes(cadModel.value.status)) {
    startCadPolling()
  } else {
    stopCadPolling()
  }
}

function startCadPolling() {
  if (cadPollTimer || !taskId.value) return
  cadPollTimer = window.setInterval(() => refreshCadModel(true), 3000)
}

function stopCadPolling() {
  if (!cadPollTimer) return
  window.clearInterval(cadPollTimer)
  cadPollTimer = null
}

function loadCadStl() {
  if (!taskId.value) return
  getCadModelFile(taskId.value, 'stl').then(data => {
    cadStlData.value = data
    updateActiveComparisonStlData(data)
  }).catch(() => {
    cadStlData.value = null
    updateActiveComparisonStlData(null)
  })
}

function clearCadPreviewImage() {
  cadPreviewImageUrl.value = ''
  cadPreviewImageKey.value = ''
  reportCadDocImageUrl.value = ''
}

function loadCadPreviewImage() {
  if (!taskId.value) return Promise.resolve('')
  const file = cadModel.value.files?.previewPng
  const shouldLoad = cadModel.value.status === 'SUCCESS' && file
  if (!shouldLoad) {
    clearCadPreviewImage()
    updateActiveComparisonCadPreviewImage('', '')
    return Promise.resolve('')
  }
  const imageKey = `${taskId.value}:${file.fileId || file.filePath || file.fileName || 'preview'}:${cadModel.value.updatedAt || ''}`
  if (cadPreviewImageKey.value === imageKey && cadPreviewImageUrl.value) {
    updateActiveComparisonCadPreviewImage(cadPreviewImageUrl.value, imageKey)
    return Promise.resolve(cadPreviewImageUrl.value)
  }
  return getCadModelFile(taskId.value, 'preview').then(data => {
    return blobToDataUrl(new Blob([data], { type: 'image/png' })).then(imageUrl => {
      cadPreviewImageUrl.value = imageUrl
      cadPreviewImageKey.value = imageKey
      updateActiveComparisonCadPreviewImage(imageUrl, imageKey)
      return imageUrl
    })
  }).catch(() => {
    clearCadPreviewImage()
    updateActiveComparisonCadPreviewImage('', '')
    return ''
  })
}

async function captureReportCadPreviewImage() {
  await nextTick()
  const ready = await waitForReportCadViewerModel()
  if (!ready) return ''
  const imageUrl = reportCadViewerRef.value?.captureImage?.()
  if (!imageUrl || !imageUrl.startsWith('data:image')) return ''
  const imageKey = `capture:${taskId.value || 'task'}:${Date.now()}`
  cadPreviewImageUrl.value = imageUrl
  cadPreviewImageKey.value = imageKey
  updateActiveComparisonCadPreviewImage(imageUrl, imageKey)
  return imageUrl
}

async function waitForReportCadViewerModel(maxFrames = 12) {
  for (let index = 0; index < maxFrames; index += 1) {
    if (reportCadViewerRef.value?.hasModel?.()) return true
    await new Promise(resolve => requestAnimationFrame(resolve))
  }
  return Boolean(reportCadViewerRef.value?.hasModel?.())
}

async function ensureReportCadImage(allowCapture = false) {
  if (cadModel.value.status === 'SUCCESS' && cadModel.value.files?.previewPng) {
    const loadedImage = await loadCadPreviewImage()
    if (loadedImage) return loadedImage
  }

  const selectedImage = reportSelectedCadImage.value
  const selectedImageKey = reportSelectedCadImageKey.value
  if (selectedImage && !selectedImageKey.startsWith('capture:')) return selectedImage

  if (allowCapture) {
    const capturedImage = await captureReportCadPreviewImage()
    if (capturedImage) return capturedImage
  }

  return ''
}

async function normalizeReportImage(source, options = {}) {
  if (!source) return ''
  const width = options.width || 1280
  const height = options.height || 720
  const background = options.background || '#f8fafc'
  const padding = options.padding ?? 30

  return new Promise((resolve) => {
    const image = new Image()
    image.onload = () => {
      try {
        const canvas = document.createElement('canvas')
        canvas.width = width
        canvas.height = height
        const context = canvas.getContext('2d')
        if (!context) {
          resolve(source)
          return
        }

        const crop = options.trimWhitespace ? getImageContentCrop(image, options) : null
        const sourceX = crop?.x ?? 0
        const sourceY = crop?.y ?? 0
        const sourceWidth = crop?.width ?? image.naturalWidth
        const sourceHeight = crop?.height ?? image.naturalHeight

        context.fillStyle = background
        context.fillRect(0, 0, width, height)

        const targetWidth = Math.max(width - padding * 2, 1)
        const targetHeight = Math.max(height - padding * 2, 1)
        const scale = Math.min(targetWidth / sourceWidth, targetHeight / sourceHeight)
        const drawWidth = sourceWidth * scale
        const drawHeight = sourceHeight * scale
        const drawX = (width - drawWidth) / 2
        const drawY = (height - drawHeight) / 2

        context.drawImage(image, sourceX, sourceY, sourceWidth, sourceHeight, drawX, drawY, drawWidth, drawHeight)
        resolve(canvas.toDataURL('image/png'))
      } catch {
        resolve(source)
      }
    }
    image.onerror = () => resolve(source)
    image.src = source
  })
}

function getImageContentCrop(image, options = {}) {
  const sourceWidth = image.naturalWidth
  const sourceHeight = image.naturalHeight
  if (!sourceWidth || !sourceHeight) return null

  const canvas = document.createElement('canvas')
  canvas.width = sourceWidth
  canvas.height = sourceHeight
  const context = canvas.getContext('2d')
  if (!context) return null

  context.drawImage(image, 0, 0)
  const imageData = context.getImageData(0, 0, sourceWidth, sourceHeight).data
  const whiteThreshold = options.whiteThreshold ?? 246
  const scanStep = options.scanStep || 2
  let minX = sourceWidth
  let minY = sourceHeight
  let maxX = 0
  let maxY = 0

  for (let y = 0; y < sourceHeight; y += scanStep) {
    for (let x = 0; x < sourceWidth; x += scanStep) {
      const index = (y * sourceWidth + x) * 4
      const alpha = imageData[index + 3]
      if (alpha < 20) continue

      const red = imageData[index]
      const green = imageData[index + 1]
      const blue = imageData[index + 2]
      const isWhite = red >= whiteThreshold && green >= whiteThreshold && blue >= whiteThreshold
      if (isWhite) continue

      minX = Math.min(minX, x)
      minY = Math.min(minY, y)
      maxX = Math.max(maxX, x)
      maxY = Math.max(maxY, y)
    }
  }

  if (minX >= maxX || minY >= maxY) return null

  const margin = options.trimMargin ?? 18
  const x = Math.max(0, minX - margin)
  const y = Math.max(0, minY - margin)
  const right = Math.min(sourceWidth, maxX + margin)
  const bottom = Math.min(sourceHeight, maxY + margin)

  return {
    x,
    y,
    width: Math.max(1, right - x),
    height: Math.max(1, bottom - y)
  }
}

async function prepareReportImages(allowCapture = false) {
  let cadImage = reportSelectedCadImage.value
  let ansysImage = reportSelectedAnsysImage.value

  if (isReportCadComplete()) {
    cadImage = await ensureReportCadImage(allowCapture)
  }

  if (isReportAnsysComplete()) {
    ansysImage = reportSelectedAnsysImage.value || await loadAnsysStressImage()
  }

  const cadNormalized = await normalizeReportImage(cadImage, { padding: 20 })
  const ansysNormalized = await normalizeReportImage(ansysImage, { padding: 8 })

  reportCadDocImageUrl.value = cadNormalized || cadImage || ''
  reportAnsysDocImageUrl.value = ansysNormalized || ansysImage || ''
}

function downloadCadFile(kind) {
  if (!taskId.value) return
  getCadModelFile(taskId.value, kind).then(data => {
    const file = cadModel.value.files?.[kind]
    const filename = file?.fileName || `pipe_model.${kind === 'sldprt' ? 'SLDPRT' : 'stl'}`
    saveAs(new Blob([data]), filename)
  })
}

function startAnsysSimulation() {
  if (!canRunAnsysSimulation.value) {
    ElMessage.warning('请先生成参数模型，再打开 ANSYS。')
    return
  }
  reportGeneratedAt.value = ''
  ansysSubmitting.value = true
  openAnsysSimulationTask(taskId.value, buildAnsysPayload()).then(res => {
    applyAnsysSimulation(res.data)
    ElMessage.success('正在打开 ANSYS，请在 Mechanical 中确认参数后手动求解')
  }).finally(() => {
    ansysSubmitting.value = false
  })
}

function importAnsysResult() {
  if (!taskId.value) return
  reportGeneratedAt.value = ''
  ansysResultImporting.value = true
  importAnsysResultFile(taskId.value, buildAnsysPayload()).then(res => {
    applyAnsysSimulation(res.data)
    ElMessage.success('结果已导入并传回平台')
  }).finally(() => {
    ansysResultImporting.value = false
  })
}

function refreshAnsysSimulation(silent = false) {
  if (!taskId.value) return
  if (!silent) ansysRefreshing.value = true
  getAnsysSimulationTask(taskId.value, { simulationMode: ANSYS_MODE_DEMO }).then(res => {
    applyAnsysSimulation(res.data)
  }).finally(() => {
    if (!silent) ansysRefreshing.value = false
  })
}

function applyAnsysSimulation(model) {
  ansysSimulation.value = model || emptyAnsysSimulation()
  applyAnsysParameters(ansysSimulation.value.input?.simulationParameters)
  updateActiveComparisonAnsysResult(ansysSimulation.value)
  if (['QUEUED', 'RUNNING'].includes(ansysSimulation.value.status)) {
    clearAnsysStressImage()
    startAnsysPolling()
  } else {
    stopAnsysPolling()
    loadAnsysStressImage()
  }
}

function startAnsysPolling() {
  if (ansysPollTimer || !taskId.value) return
  ansysPollTimer = window.setInterval(() => refreshAnsysSimulation(true), 5000)
}

function stopAnsysPolling() {
  if (!ansysPollTimer) return
  window.clearInterval(ansysPollTimer)
  ansysPollTimer = null
}

function clearAnsysStressImage() {
  if (ansysStressObjectUrl.value && ansysStressObjectUrl.value.startsWith('blob:')) {
    URL.revokeObjectURL(ansysStressObjectUrl.value)
  }
  ansysStressObjectUrl.value = ''
  ansysStressImageKey.value = ''
  reportAnsysDocImageUrl.value = ''
}

function loadAnsysStressImage() {
  const imagePath = ansysSimulation.value.stressImageUrl
  const shouldLoad = taskId.value &&
    ansysSimulation.value.status === 'SUCCESS' &&
    !ansysSimulation.value.placeholder &&
    imagePath
  if (!shouldLoad) {
    clearAnsysStressImage()
    return Promise.resolve('')
  }
  const imageKey = `${taskId.value}:${ANSYS_MODE_DEMO}:${imagePath}:${ansysSimulation.value.updatedAt || ''}`
  if (ansysStressImageKey.value === imageKey && ansysStressObjectUrl.value) return Promise.resolve(ansysStressObjectUrl.value)
  return getAnsysSimulationImage(taskId.value, { simulationMode: ANSYS_MODE_DEMO }).then(data => {
    return blobToDataUrl(new Blob([data], { type: 'image/png' })).then(imageUrl => {
      clearAnsysStressImage()
      ansysStressObjectUrl.value = imageUrl
      ansysStressImageKey.value = imageKey
      updateActiveComparisonAnsysImage(imageUrl, imageKey)
      return imageUrl
    })
  }).catch(() => {
    clearAnsysStressImage()
    updateActiveComparisonAnsysImage('', '')
    return ''
  })
}

function blobToDataUrl(blob) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result)
    reader.onerror = reject
    reader.readAsDataURL(blob)
  })
}

function cadStatusType(status) {
  return {
    NOT_SUBMITTED: 'info',
    QUEUED: 'warning',
    RUNNING: 'primary',
    SUCCESS: 'success',
    FAILED: 'danger'
  }[status] || 'info'
}

function ansysStatusType(status) {
  return {
    NOT_SUBMITTED: 'info',
    QUEUED: 'warning',
    RUNNING: 'primary',
    SUCCESS: 'success',
    FAILED: 'danger'
  }[status] || 'info'
}

function generateDesignReport() {
  if (!canGenerateReport.value) {
    ElMessage.warning('请先完成代理模型求解并形成候选设计方案。')
    return
  }
  reportGeneratedAt.value = formatDateTime(new Date())
  reportDialogVisible.value = true
  nextTick(() => {
    prepareReportImages(true).finally(() => {
      ElMessage.success('设计方案及验证报告已生成，请检查报告内容后提交。')
    })
  })
}

function buildQualityTaskDesignResult(submission = {}) {
  const result = {
    moduleCode: 'PROJECT_2',
    moduleName: '设计制造协同优化平台',
    resultType: 'DESIGN_OPTIMIZATION_REPORT',
    conclusion: reportDecision.value.passed
      ? '设计制造协同优化方案已形成，报告已生成，等待回填至质量问题管理中心。'
      : '设计制造协同优化方案验证未通过，已提交退回处理结论。',
    designTaskId: taskId.value,
    designTaskName: taskTitle.value,
    designReport: {
      submitted: Boolean(submission.submitted || submission.reportFileId),
      taskId: taskId.value,
      reportId: submission.reportId || '',
      reportCode: submission.reportCode || reportCode.value || '',
      reportTitle: submission.reportTitle || taskTitle.value || '设计制造协同优化方案报告',
      reportFileId: submission.reportFileId || '',
      reportFilePath: submission.reportFilePath || '',
      reportFileName: submission.reportFileName || '设计制造协同优化方案报告.doc',
      submitTime: formatDateTime(submission.submitTime || new Date())
    },
    generateTime: formatDateTime(new Date())
  }

  return JSON.stringify(result, null, 2)
}

async function markQualityTaskPendingBackfill(submission = {}) {
  const link = detail.value?.qualityTaskLink || {}
  const qualityTaskId = link.qualityTaskId

  if (!qualityTaskId) {
    return
  }

  try {
    const processResult = buildQualityTaskDesignResult(submission)
    const reportFilePath = submission.reportFilePath || ''

    await updateQualityTask({
      taskId: qualityTaskId,
      problemId: link.qualityProblemId,
      problemCode: link.qualityProblemCode,
      moduleCode: 'PROJECT_2',
      moduleName: '设计制造协同优化平台',
      taskStatus: 'PENDING_BACKFILL',
      processResult,
      processFile: reportFilePath
    })

    await addQualityLog({
      problemId: link.qualityProblemId,
      problemCode: link.qualityProblemCode,
      taskId: qualityTaskId,
      actionType: 'REPORT_SUBMIT',
      actionName: '设计制造协同优化平台提交优化方案',
      operatorName: '设计制造协同优化平台',
      fromStatus: 'PROCESSING',
      toStatus: 'PENDING_BACKFILL',
      actionContent: `设计制造协同优化平台已提交最终设计方案报告：${submission.reportTitle || taskTitle.value || '设计制造协同优化方案报告'}。`,
      createTime: formatDateTime(new Date())
    })
  } catch (error) {
    console.warn('同步质量任务“已完成待回填”状态失败：', error)
    ElMessage.warning('设计报告已提交，但同步质量任务状态失败，可在任务看板重新刷新后回填')
  }
}

function returnSimulationForRework() {
  if (!canReturnSimulationFailure.value) {
    ElMessage.warning('当前任务节点不能退回重新优化。')
    return
  }
  const comment = reportDecision.value.comment || '仿真验证不通过，退回模型解耦求解。'
  reportDecision.value = {
    ...reportDecision.value,
    passed: false,
    comment
  }
  simulationReturnSubmitting.value = true
  runSimulation(taskId.value, {
    simulationPassed: false,
    comment
  }).then(res => {
    simulation.value = res.data || simulation.value
    reportGeneratedAt.value = ''
    reportCadDocImageUrl.value = ''
    reportAnsysDocImageUrl.value = ''
    reportDialogVisible.value = false
    ElMessage.warning('已退回模型解耦求解，请重新调整方案后再验证。')
    loadDetail()
  }).finally(() => {
    simulationReturnSubmitting.value = false
  })
}

function submitDesignReport() {
  if (reportDecision.value.passed === false) {
    returnSimulationForRework()
    return
  }
  if (!canSubmitDesignReport.value) {
    ElMessage.warning(submitDesignReportBlockedReason())
    return
  }
  reportSubmitting.value = true
  prepareReportImages(true).finally(() => {
    const reportHtml = buildReportHtml()
    submitDesignReportTask(taskId.value, {
      simulationPassed: reportDecision.value.passed,
      report: buildDesignReportPayload(),
      reportHtmlBase64: encodeBase64Utf8(reportHtml),
      comment: reportDecision.value.comment
    }).then(async res => {
      simulation.value = res.data || simulation.value
      reportSubmission.value = res.data?.reportSubmission || reportSubmission.value
      if (reportDecision.value.passed) {
        await markQualityTaskPendingBackfill(reportSubmission.value)
      }
      ElMessage.success(reportDecision.value.passed ? '设计方案报告已提交审批。' : '已提交验证不通过结论。')
      loadDetail()
    }).finally(() => {
      reportSubmitting.value = false
    })
  })
}

function encodeBase64Utf8(value) {
  const bytes = new TextEncoder().encode(String(value || ''))
  let binary = ''
  const chunkSize = 0x8000
  for (let i = 0; i < bytes.length; i += chunkSize) {
    binary += String.fromCharCode(...bytes.subarray(i, i + chunkSize))
  }
  return btoa(binary)
}

function submitDesignReportBlockedReason() {
  if (!canGenerateReport.value) return '请先完成代理模型求解并形成候选设计方案。'
  if (!['model_decompose_solve', 'simulation_confirm'].includes(currentNodeKey.value) || !canEditVerification.value) return '当前节点或当前账号没有提交设计方案报告的权限。'
  if (!reportGeneratedAt.value) return '请先点击“生成报告”，确认报告内容后再提交。'
  if (!isReportCadComplete()) return '当前选用方案尚未完成参数化建模，不能提交设计方案。'
  if (!isReportAnsysComplete()) return '当前选用方案尚未完成 ANSYS 仿真验证，不能提交设计方案。'
  if (reportSubmission.value.submitted) return '该设计方案报告已经提交，不能重复提交。'
  if (simulationBlocksReportSubmit.value) return '该设计方案报告已经提交，不能重复提交。'
  return '当前条件未满足，暂不能提交设计方案报告。'
}

function downloadDesignReport() {
  if (reportSubmission.value.submitted && reportSubmission.value.reportFileId) {
    getTaskAttachmentFile(reportSubmission.value.reportFileId).then(data => {
      const filename = reportSubmission.value.reportFileName || `${reportCode.value}.doc`
      saveAs(new Blob([data]), filename)
    })
    return
  }
  if (!reportGeneratedAt.value) {
    reportGeneratedAt.value = formatDateTime(new Date())
  }
  prepareReportImages(true).finally(() => {
    const html = buildReportHtml()
    const task = detail.value.task || {}
    const filename = `${task.taskNo || task.taskName || '设计方案验证报告'}.doc`
    saveAs(new Blob(['\ufeff', html], { type: 'application/msword;charset=utf-8' }), filename)
  })
}

function buildDesignReportPayload() {
  return {
    reportCode: reportCode.value,
    reportTitle: taskTitle.value || '设计方案及验证报告',
    generatedAt: reportGeneratedAt.value,
    problem: reportProblemItems.value,
    approvalDecision: reportApprovalDecision.value,
    approvalSummary: reportApprovalSummaryItems.value,
    optimizationSummary: reportOptimizationSummaryItems.value,
    selectedScheme: reportSelectedScheme.value,
    cadPreviewImage: reportCadDocImageUrl.value || reportSelectedCadImage.value,
    schemeComparison: reportSchemeComparisonRows.value,
    constraintSummary: reportConstraintSummaryRows.value,
    riskRows: reportRiskRows.value,
    objectivesAndConstraints: reportObjectiveConstraintRows.value,
    designVariables: reportVariableRows.value,
    designParameters: reportDesignParameterRows.value,
    cadModel: cadModel.value,
    ansysSimulation: ansysSimulation.value,
    simulationMetrics: reportSimulationRows.value,
    conclusion: reportDecision.value
  }
}

function buildReportHtml() {
  const section = (title, body) => `<section class="report-section"><h2 style="margin:0 0 4mm;padding:0 0 2mm;border-bottom:0.75pt solid #1f3b63;color:#172b4d;font-size:11pt;line-height:1.45;font-weight:500;">${escapeHtml(title)}</h2>${body}</section>`
  const formTable = rows => `<table class="report-form-table" cellspacing="0" cellpadding="0"><tbody>${rows.map(row => `<tr>${row.map(item => `<th>${escapeHtml(item.label)}</th><td>${escapeHtml(item.value)}${item.unit ? ` ${escapeHtml(item.unit)}` : ''}</td>`).join('')}</tr>`).join('')}</tbody></table>`
  const summaryTable = rows => `<table class="report-form-table report-summary-table" cellspacing="0" cellpadding="0"><tbody>${rows.map(item => `<tr><th>${escapeHtml(item.label)}</th><td colspan="3">${escapeHtml(item.value)}</td></tr>`).join('')}</tbody></table>`
  const listTable = (headers, rows) => `<table class="report-list-table" cellspacing="0" cellpadding="0"><thead><tr>${headers.map(item => `<th>${escapeHtml(item.label)}</th>`).join('')}</tr></thead><tbody>${rows.map(row => `<tr>${headers.map(item => `<td>${escapeHtml(row[item.prop])}</td>`).join('')}</tr>`).join('')}</tbody></table>`
  const paragraph = text => `<p>${escapeHtml(text)}</p>`
  const cadReportImage = reportCadDocImageUrl.value || reportSelectedCadImage.value
  const ansysReportImage = reportAnsysDocImageUrl.value || reportSelectedAnsysImage.value
  const cadFigure = `<div class="report-figure-panel"><h3>SolidWorks 参数化模型</h3>${cadReportImage
    ? `<div class="report-figure-box report-figure-box--cad"><img src="${escapeHtml(cadReportImage)}" alt="SolidWorks 参数化模型"></div>`
    : `<div class="report-figure-empty">${escapeHtml(reportSelectedScheme.value?.cadStatusLabel || cadModel.value.statusLabel || '模型文件未生成或未缓存')}</div>`}</div>`
  const ansysFigure = `<div class="report-figure-panel"><h3>ANSYS 仿真效果图</h3>${ansysReportImage
    ? `<div class="report-figure-box report-figure-box--ansys"><img src="${escapeHtml(ansysReportImage)}" alt="ANSYS 仿真效果图"></div>`
    : `<div class="report-figure-empty">仿真效果图未生成或未缓存</div>`}</div>`
  return `<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
  <title>液压弯管抗冲击性能优化设计方案验证报告</title>
  <style>
    @page WordSection1 { size: 210mm 297mm; margin: 16mm 16mm 16mm 16mm; }
    div.WordSection1 { page: WordSection1; }
    body { margin: 0; font-family: "Microsoft YaHei", SimSun, Arial, sans-serif; color: #1f2a44; background: #fff; }
    .report-doc-export { width: 178mm; margin: 0 auto; background: #fff; }
    .report-cover { margin-bottom: 7mm; padding-bottom: 5mm; border-bottom: 1.2pt solid #1f3b63; }
    .report-cover span { display: block; color: #52667a; font-size: 8.5pt; font-weight: 700; }
    .report-cover h1 { margin: 3mm 0 2mm; color: #172b4d; font-size: 15.5pt; line-height: 1.35; text-align: center; font-weight: 500; }
    .report-cover p { margin: 0; color: #64748b; font-size: 9pt; line-height: 1.5; text-align: center; }
    .report-section { padding: 6mm 0 0; page-break-inside: avoid; }
    .report-section h2 { margin: 0 0 3mm; padding: 0 0 1.8mm; border-bottom: 0.75pt solid #1f3b63; color: #172b4d; font-size: 11pt; line-height: 1.35; font-weight: 500; }
    p { margin: 0 0 3mm; color: #24324f; font-size: 9.5pt; line-height: 1.65; }
    .report-form-table, .report-list-table { width: 100%; border-collapse: collapse; table-layout: fixed; margin: 2.5mm 0 5mm; color: #1f2a44; font-size: 9pt; mso-table-lspace: 0pt; mso-table-rspace: 0pt; }
    .report-form-table th, .report-form-table td, .report-list-table th, .report-list-table td { border: 0.75pt solid #b9c6d6; padding: 2.8pt 4.5pt; line-height: 1.3; text-align: left; vertical-align: middle; word-break: break-all; mso-line-height-rule: exactly; mso-para-margin: 0; }
    .report-form-table th p, .report-form-table td p, .report-list-table th p, .report-list-table td p { margin: 0; line-height: 1.3; mso-line-height-rule: exactly; }
    .report-form-table th { width: 22mm; color: #24324f; font-weight: 700; background: #eef3f8; }
    .report-list-table { table-layout: auto; }
    .report-list-table th { color: #24324f; font-weight: 700; text-align: center; background: #eef3f8; }
    .report-decision-cell { color: #0f6b3f; font-size: 10pt; font-weight: 700; }
    .report-visual-stack { width: 100%; margin: 3mm 0 6mm; }
    .report-figure-panel { margin: 0 0 5mm; page-break-inside: avoid; }
    .report-figure-panel h3 { margin: 0 0 2mm; color: #19375a; font-size: 9.5pt; font-weight: 700; }
    .report-figure-box, .report-figure-empty { width: 100%; min-height: 94mm; border: 0.75pt solid #e1e7ef; background: #f8fafc; text-align: center; vertical-align: middle; overflow: visible; }
    .report-figure-box img { display: block; width: 164mm; height: auto; max-height: 92mm; margin: 1mm auto; border: 0; }
    .report-figure-empty { color: #7a8da3; font-size: 9pt; line-height: 94mm; }
  </style>
</head>
<body>
  <div class="WordSection1"><div class="report-doc-export">
  <header class="report-cover">
    <span>设计优化任务提交件</span>
    <h1 style="margin:3mm 0 2mm;color:#172b4d;font-size:15.5pt;line-height:1.35;text-align:center;font-weight:500;">${escapeHtml(taskTitle.value || '液压弯管抗冲击性能优化设计方案验证报告')}</h1>
    <p>报告编号：${escapeHtml(reportCode.value)}　生成时间：${escapeHtml(formatDateTime(reportGeneratedAt.value || new Date()))}　状态：${escapeHtml(reportSubmitStatusLabel.value)}</p>
  </header>
  ${section('一、审批结论摘要', `<table class="report-form-table"><tbody><tr><th>审批建议</th><td colspan="3" class="report-decision-cell">${escapeHtml(reportApprovalDecision.value)}</td></tr><tr><th>结论说明</th><td colspan="3">${escapeHtml(reportConclusionText.value)}</td></tr>${reportApprovalSummaryTableRows.value.map(row => `<tr>${row.map(item => `<th>${escapeHtml(item.label)}</th><td>${escapeHtml(item.value)}</td>`).join('')}</tr>`).join('')}</tbody></table>`)}
  ${section('二、任务与优化问题概述', paragraph(reportProblemNarrative.value) + formTable(reportProblemMetaTableRows.value) + summaryTable(reportOptimizationSummaryItems.value))}
  ${section('三、最终选用方案', `<div class="report-visual-stack">${cadFigure}${ansysFigure}</div>` + formTable(reportSelectedParameterTableRows.value))}
  ${section('四、候选方案对比与选用依据', paragraph(reportSchemeComparisonNarrative.value) + listTable([
    { label: '方案', prop: 'schemeName' },
    { label: '预测应力', prop: 'predictedStress' },
    { label: '建模结果', prop: 'cadStatusLabel' },
    { label: '仿真结果', prop: 'ansysResultLabel' },
    { label: '验证结论', prop: 'judgementLabel' },
    { label: '选用状态', prop: 'selectionLabel' }
  ], reportSchemeComparisonRows.value))}
  ${section('五、目标与约束满足性总结', paragraph(reportConstraintNarrative.value) + listTable([
    { label: '类别', prop: 'category' },
    { label: '结论', prop: 'conclusion' },
    { label: '说明', prop: 'detail' }
  ], reportConstraintSummaryRows.value))}
  ${section('六、仿真验证结果与工程风险', listTable([
    { label: '证据项', prop: 'label' },
    { label: '状态/结果', prop: 'value' },
    { label: '说明', prop: 'description' }
  ], reportEvidenceItems.value) + listTable([
    { label: '类别', prop: 'label' },
    { label: '说明', prop: 'value' }
  ], reportRiskRows.value))}
  ${section('七、附录：设计变量与指标明细', listTable([
    { label: '学科', prop: 'disciplineName' },
    { label: '变量名称', prop: 'variableName' },
    { label: '初始值', prop: 'initialValue' },
    { label: '下限', prop: 'lowerBound' },
    { label: '上限', prop: 'upperBound' },
    { label: '单位', prop: 'unit' }
  ], reportVariableRows.value) + listTable([
    { label: '指标', prop: 'name' },
    { label: '数值', prop: 'value' },
    { label: '单位', prop: 'unit' },
    { label: '来源', prop: 'source' }
  ], reportSimulationRows.value))}
  </div></div>
</body>
</html>`
}

function escapeHtml(value) {
  return String(value ?? '-')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

function formatDateTime(value = new Date()) {
  const pad = number => String(number).padStart(2, '0')
  if (!value) return ''
  if (value === '-') return '-'
  if (Array.isArray(value)) {
    const [year, month = 1, day = 1, hour = 0, minute = 0, second = 0] = value
    if (year) return `${year}-${pad(month)}-${pad(day)} ${pad(hour)}:${pad(minute)}:${pad(second)}`
  }
  if (typeof value === 'string') {
    const text = value.trim()
    if (!text) return ''
    const commaParts = text.split(',').map(item => Number(item.trim()))
    if (commaParts.length >= 3 && commaParts.every(Number.isFinite)) {
      return formatDateTime(commaParts)
    }
    const matched = text.replace('T', ' ').match(/^(\d{4})-(\d{1,2})-(\d{1,2})(?:\s+(\d{1,2}):(\d{1,2})(?::(\d{1,2}))?)?/)
    if (matched) {
      return `${matched[1]}-${pad(matched[2])}-${pad(matched[3])} ${pad(matched[4] || 0)}:${pad(matched[5] || 0)}:${pad(matched[6] || 0)}`
    }
  }
  const date = value instanceof Date ? value : new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

function subtaskItems(subtask, itemType) {
  return (subtask.items || []).filter(item => item.itemType === itemType)
}

function subtaskVariables(subtask) {
  return designVariables.value
    .filter(item => item.checked && variableSubtaskCodes(item).includes(subtask.subtaskCode))
    .map(item => ({
      ...item,
      shared: variableSubtaskCodes(item).length > 1
    }))
}

function variableSubtaskCodes(item) {
  if (item.subtaskCode === 'shared') {
    return ['hydraulic_impact', 'cable_pipe_layout']
  }
  return [item.subtaskCode || 'unassigned']
}

function loadSelectedVariables(data) {
  designVariables.value = (data.designVariables || []).flatMap(group => {
    const disciplineName = group.disciplineName || disciplineLabel(group.discipline)
    return (group.items || []).map(item => ({
      ...item,
      discipline: group.discipline,
      disciplineName,
      checked: true,
      initialValue: item.initialValue || item.defaultValue || ''
    }))
  })
  variablesSaved.value = designVariables.value.length > 0
}

function loadVariableCatalogs() {
  variableLoading.value = true
  const selectedMap = new Map(designVariables.value.map(item => [item.variableCode, item]))
  const hadSavedVariables = variablesSaved.value
  Promise.all(disciplines.map(item => getDesignVariableCatalog(item.value).then(res => ({
    discipline: item.value,
    disciplineName: item.label,
    rows: res.data || []
  })))).then(results => {
    designVariables.value = results.flatMap(group => group.rows.map(row => {
      const selected = selectedMap.get(row.variableCode)
      return {
        ...row,
        ...selected,
        discipline: group.discipline,
        disciplineName: group.disciplineName,
        checked: Boolean(selected?.checked)
      }
    }))
    variablesSaved.value = hadSavedVariables
  }).finally(() => {
    variableLoading.value = false
  })
}

function saveVariables() {
  const selected = Array.from(new Map(
    designVariables.value.filter(item => item.checked).map(item => [item.variableCode, item])
  ).values())
  if (!selected.length) {
    ElMessage.warning('请至少选择一个设计变量。')
    return
  }
  variableSaving.value = true
  saveDesignVariables(taskId.value, {
    designVariables: selected
  }).then(res => {
    detail.value = res.data || detail.value
    loadSelectedVariables(detail.value)
    decomposed.value = Boolean(detail.value.decomposed) || decomposed.value
    variablesSaved.value = true
    ElMessage.success('设计变量已保存，可以执行模型求解。')
  }).finally(() => {
    variableSaving.value = false
  })
}

function disciplineLabel(value) {
  return disciplines.find(item => item.value === value)?.label || value
}

function disciplinePillClass(value) {
  return `discipline-pill--${value || 'default'}`
}

function variableTypeLabel(value) {
  return { continuous: '连续型', discrete: '离散型', enum: '枚举型' }[value] || value || '-'
}

onMounted(() => {
  loadSurrogateModels()
  loadDetail()
  window.addEventListener('resize', resizeConvergenceChart)
})

onBeforeUnmount(() => {
  stopSurrogatePolling()
  stopCadPolling()
  stopAnsysPolling()
  clearAnsysStressImage()
  window.removeEventListener('resize', resizeConvergenceChart)
  disposeConvergenceChart()
})

watch(() => route.query.taskId, value => {
  stopSurrogatePolling()
  stopCadPolling()
  stopAnsysPolling()
  clearAnsysStressImage()
  resetComparisonState()
  taskId.value = value ? Number(value) : null
  activeTab.value = ['handled', 'related'].includes(route.query.tab) ? route.query.tab : activeTab.value
  loadDetail()
})

watch(convergenceSeries, () => {
  renderConvergenceChart()
}, { deep: true })
</script>

<style scoped lang="scss">
@use "../platform-theme.scss";

.solve-industrial-view {
  background:
    linear-gradient(90deg, rgba(18, 73, 119, 0.08), transparent 260px),
    linear-gradient(180deg, #f6f8fb 0%, #edf2f7 100%);

  .design-platform-shell {
    gap: 14px;
  }

  .platform-topbar {
    min-height: 104px;
    padding: 20px 24px;
    border: 1px solid #d8e0ea;
    border-radius: 18px;
    background: linear-gradient(135deg, #ffffff 0%, #f7fbff 100%);
    box-shadow: 0 8px 22px rgba(38, 65, 92, 0.08);
  }

  .platform-eyebrow,
  .section-label {
    margin-bottom: 6px;
    color: #176db6;
    letter-spacing: 0.06em;
  }

  .platform-title {
    font-size: 28px;
  }

  .meta-chip {
    min-height: 34px;
    border-radius: 999px;
    background: #f7f9fc;
  }

  .section-block {
    padding: 15px;
    border-color: #d8e0ea;
    border-radius: 8px;
    background: #ffffff;
    box-shadow: 0 4px 14px rgba(49, 76, 108, 0.06);
  }

  .section-header {
    min-height: 38px;
    padding-bottom: 10px;
    border-bottom: 1px solid #e6ebf1;
  }

  .section-title {
    font-size: 18px;
  }

  .table-shell {
    border-color: #e1e7ef;
    border-radius: 6px;
    background: #ffffff;
  }

  .conflict-return-row {
    display: flex;
    justify-content: flex-end;
    margin: 10px 0;
  }

  .soft-panel {
    position: relative;
    overflow: hidden;
    border-color: #dfe6ef;
    border-radius: 6px;
    background: #ffffff;

    &::before {
      position: absolute;
      top: 0;
      right: 0;
      left: 0;
      height: 3px;
      background: #4f8edc;
      content: "";
    }
  }

  :deep(.el-button) {
    border-radius: 4px;
  }

  :deep(.el-tag) {
    border-radius: 4px;
  }

  :deep(.el-alert) {
    border-radius: 6px;
  }

  :deep(.el-collapse) {
    --el-collapse-header-bg-color: #ffffff;
    --el-collapse-content-bg-color: #ffffff;
    border-top: 0;
  }

  :deep(.el-collapse-item__header) {
    padding: 0 10px;
    border-bottom-color: #e6ebf1;
    color: #273d5b;
    font-weight: 600;
  }

  :deep(.el-collapse-item__content) {
    padding: 10px;
  }

  :deep(.el-table__cell) {
    padding: 8px 0;
  }
}

.content-grid--balanced {
  gap: 14px;
}

.card-head {
  min-height: 44px;
  margin: -2px -2px 12px;
  padding: 10px 12px;
  border-bottom: 1px solid #e6ebf1;
  background: #f8fafc;

  .section-title {
    font-size: 15px;
  }
}

.subtask-group {
  margin: 10px 0;
  padding: 0 2px;
}

.subtask-group__title {
  margin: 0 0 7px;
  color: #40556f;
  font-size: 12px;
  font-weight: 700;
}

.subtask-entry-card {
  cursor: pointer;
  transition: border-color 0.16s ease, box-shadow 0.16s ease, transform 0.16s ease;

  &:hover {
    border-color: #8bbcf0;
    box-shadow: 0 8px 20px rgba(24, 70, 116, 0.08);
    transform: translateY(-1px);
  }

  &.is-active {
    border-color: #409eff;
    box-shadow: 0 0 0 1px rgba(64, 158, 255, 0.16);
  }
}

.subtask-entry-card__foot {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid #e6ebf1;
  text-align: right;
}

.subtask-empty-state {
  margin-top: 12px;
  padding: 24px 0 18px;
  border: 1px dashed #cfd8e3;
  border-radius: 6px;
  background: #fbfcfe;
}

.section-subtitle {
  margin: 4px 0 0;
  color: #6b7f95;
  font-size: 13px;
  line-height: 1.5;
}

.subtask-empty-workspace {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 360px;
  border: 1px dashed #cfd8e3;
  border-radius: 6px;
  background: #fbfcfe;
}

.tag-item {
  margin: 0 6px 6px 0;
  border-radius: 4px;
}

.mb-16 {
  margin-bottom: 16px;
}

.wait-action {
  color: #98a2b3;
  font-size: 13px;
}

.solve-command-grid {
  grid-template-columns: minmax(720px, 1.42fr) minmax(360px, 0.58fr);
  align-items: start;
}

.objective-workbench {
  min-width: 0;
}

.objective-workbench-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin: 10px 0 12px;
  padding: 9px 12px;
  border: 1px solid #e2e8f0;
  border-left: 3px solid #4f8edc;
  border-radius: 6px;
  background: #f8fafc;

  span {
    color: #52667a;
    font-size: 13px;
  }

  div {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    justify-content: flex-end;
  }
}

.objective-constraint-columns {
  display: grid;
  grid-template-columns: minmax(0, 1.12fr) minmax(0, 0.88fr);
  gap: 14px;
}

.oc-panel {
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
  border: 1px solid #dfe6ef;
  border-radius: 6px;
  background: #ffffff;
}

.oc-panel--objective {
  border-top: 3px solid #2f80d8;
}

.oc-panel--constraint {
  border-top: 3px solid #d9822b;
}

.oc-panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 12px;
  border-bottom: 1px solid #e6ebf1;
  background: #f8fafc;

  strong {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    color: #19375a;
    font-size: 15px;
  }

  span {
    color: #75869b;
    font-size: 12px;
  }
}

.panel-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 6px;
  color: #ffffff;
  font-size: 12px;
  font-weight: 800;
}

.panel-icon--objective {
  background: #2f80d8;
}

.panel-icon--constraint {
  background: #d9822b;
}

.panel-badge {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 9px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.panel-badge--objective {
  color: #176db6;
  background: #eaf4ff;
}

.panel-badge--constraint {
  color: #9a5a10;
  background: #fff3dd;
}

.oc-card-list {
  display: grid;
  gap: 9px;
  max-height: 368px;
  padding: 10px 12px 12px;
  overflow-y: auto;
  scrollbar-gutter: stable;

  &::-webkit-scrollbar {
    width: 8px;
  }

  &::-webkit-scrollbar-thumb {
    border: 2px solid #f8fafc;
    border-radius: 999px;
    background: #c7d6e8;
  }
}

.oc-item {
  position: relative;
  padding: 10px 12px 10px 14px;
  border: 1px solid #e6ebf1;
  border-left-width: 4px;
  border-radius: 6px;
  background: #fbfcfe;
  color: #32415f;
  transition: border-color 0.2s ease, background-color 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    border-color: #c7d6e8;
    background: #ffffff;
    box-shadow: 0 6px 16px rgba(36, 63, 94, 0.08);
  }
}

.oc-item--objective {
  border-left-color: #2f80d8;
}

.oc-item--constraint {
  border-left-color: #d9822b;
}

.oc-item__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.oc-item__title {
  color: #19375a;
  font-size: 14px;
  font-weight: 700;
}

.oc-item__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-top: 5px;
  color: #64748b;
  font-size: 12px;
}

.oc-item__desc {
  display: -webkit-box;
  margin: 7px 0 0;
  overflow: hidden;
  color: #5e6c7d;
  font-size: 12px;
  line-height: 1.45;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
}

.oc-item__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 8px;
}

.priority-badge,
.constraint-value,
.selection-badge {
  display: inline-flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  min-height: 24px;
  padding: 0 9px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.priority-badge--high {
  color: #c73535;
  background: #fff0f0;
}

.priority-badge--medium {
  color: #a56511;
  background: #fff5dc;
}

.priority-badge--low {
  color: #16814f;
  background: #edf9f1;
}

.constraint-value {
  color: #9a5a10;
  background: #fff3dd;
}

.selection-badge {
  color: #176db6;
  background: #eaf4ff;
}

.selection-badge--constraint {
  color: #9a5a10;
  background: #fff3dd;
}

.compatibility-note {
  color: #76879b;
  font-size: 12px;
}

.discipline-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: fit-content;
  min-width: 52px;
  max-width: 76px;
  padding: 3px 8px;
  overflow: hidden;
  border: 1px solid #cfd8e3;
  border-radius: 999px;
  color: #415166;
  background: #f7f9fc;
  font-size: 12px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.discipline-pill--structure {
  border-color: #bfd8ff;
  color: #176db6;
  background: #eef6ff;
}

.discipline-pill--layout {
  border-color: #b8e3df;
  color: #08766d;
  background: #edfafa;
}

.discipline-pill--aero {
  border-color: #d8ccff;
  color: #6547b8;
  background: #f5f1ff;
}

.discipline-pill--hydraulic {
  border-color: #f3d0a2;
  color: #9a5a10;
  background: #fff5e8;
}

.discipline-pill--manufacturing {
  border-color: #bfe5c8;
  color: #16814f;
  background: #effaf2;
}

.discipline-pill--default {
  border-color: #cfd8e3;
  color: #415166;
  background: #f7f9fc;
}

.btn-soft-blue {
  border-color: #c9ddff;
  color: #176db6;
  background: #f1f7ff;
}

.btn-soft-green {
  border-color: #c8e8d7;
  color: #16814f;
  background: #f0fbf5;
}

.btn-soft-amber {
  border-color: #f4d6a7;
  color: #a56511;
  background: #fff8ed;
}

.btn-soft-purple {
  border-color: #d9ccff;
  color: #6b4cc2;
  background: #f6f2ff;
}

.btn-strong-blue {
  border-color: #176db6;
  background: #176db6;
}

.fixed-input-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
  margin-top: 12px;
  padding: 9px 12px;
  border: 1px solid #e6ebf1;
  border-radius: 6px;
  background: #fbfcfe;
}

.fixed-input-meta__item {
  display: flex;
  align-items: center;
  min-width: 0;
  padding: 0 18px;
  border-right: 1px solid #e2e8f0;

  &:first-child {
    padding-left: 0;
  }

  &:last-child {
    border-right: 0;
  }

  span {
    flex-shrink: 0;
    margin-right: 8px;
    color: #708198;
    font-size: 12px;
  }

  strong {
    min-width: 0;
    overflow: hidden;
    color: #24324f;
    font-size: 13px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.weight-editor {
  display: grid;
  grid-template-columns: auto minmax(100px, 150px) 76px;
  gap: 8px;
  align-items: center;
  min-width: 238px;

  span {
    color: #52667a;
    font-size: 12px;
    font-weight: 700;
  }

  :deep(.el-slider) {
    --el-slider-main-bg-color: #176db6;
  }

  :deep(.el-input-number) {
    width: 76px;
  }
}

.weight-editor--card {
  flex: 1;
  max-width: 310px;
}

.surrogate-grid {
  display: grid;
  grid-template-columns: minmax(520px, 1.2fr) minmax(420px, 0.8fr);
  gap: 14px;
}

.surrogate-model-panel {
  min-width: 0;
}

.model-selector-row {
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr);
  gap: 10px;
  align-items: center;
  margin-bottom: 10px;

  > span {
    color: #52667a;
    font-size: 13px;
    font-weight: 700;
  }
}

.model-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-width: 0;

  strong,
  span {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  strong {
    color: #24324f;
    font-weight: 600;
  }

  span {
    color: #7a8da3;
    font-size: 12px;
  }
}

.model-basic-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  align-content: start;
  gap: 10px;

  div {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    padding: 9px 12px;
    border: 1px solid #e6ebf1;
    border-left: 3px solid #4f8edc;
    border-radius: 6px;
    background: #fbfcfe;
  }

  span {
    color: #708198;
    font-size: 13px;
  }

  strong {
    min-width: 0;
    overflow: hidden;
    color: #24324f;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  em {
    color: #708198;
    font-size: 12px;
    font-style: normal;
  }

  p {
    flex-basis: 100%;
    margin: 0;
    color: #667085;
    font-size: 12px;
    line-height: 1.45;
  }
}

.best-solution {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;

  div {
    min-height: 82px;
    padding: 11px 12px;
    border: 1px solid #e6ebf1;
    border-radius: 6px;
    background: #fbfcfe;
  }

  span,
  em {
    display: block;
    color: #708198;
    font-style: normal;
    font-size: 12px;
  }

  strong {
    display: block;
    margin: 5px 0;
    color: #1f2a44;
    font-size: 22px;
  }

  .stress {
    border-left: 3px solid #4f8edc;
    background: #eef6ff;
  }
}

.convergence-chart-panel {
  display: grid;
  gap: 10px;
}

.convergence-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;

  div {
    min-width: 0;
    padding: 8px 10px;
    border: 1px solid #e6ebf1;
    border-radius: 6px;
    background: #fbfcfe;
  }

  span {
    display: block;
    color: #708198;
    font-size: 12px;
  }

  strong {
    display: block;
    margin-top: 3px;
    overflow: hidden;
    color: #24324f;
    font-size: 14px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.convergence-chart {
  width: 100%;
  height: 300px;
  min-height: 300px;
  border: 1px solid #e6ebf1;
  border-radius: 6px;
  background: #ffffff;
}

.verification-panel {
  padding: 0 12px 12px;
}

.verification-head {
  align-items: flex-start;

  p {
    margin: 5px 0 0;
    color: #64748b;
    font-size: 13px;
    line-height: 1.45;
  }
}

.verification-tags,
.verification-actions,
.verification-subhead {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.verification-toolbar {
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
  align-items: start;
  padding-bottom: 12px;
  border-bottom: 1px solid #e6ebf1;
}

.verification-form {
  display: grid;
  grid-template-columns: repeat(7, minmax(118px, 1fr));
  gap: 8px;

  :deep(.el-form-item) {
    display: block;
    margin: 0;
  }

  :deep(.el-form-item__label) {
    display: block;
    height: auto;
    margin-bottom: 4px;
    color: #52667a;
    font-size: 12px;
    line-height: 1.2;
    text-align: left;
  }

  :deep(.el-input-number) {
    width: 100%;
  }

  :deep(.el-input-number .el-input) {
    width: 100%;
  }

  :deep(.el-input-number.is-controls-right .el-input-number__increase),
  :deep(.el-input-number.is-controls-right .el-input-number__decrease) {
    right: 1px;
  }
}

.verification-actions {
  justify-content: flex-end;
}

.ansys-parameter-panel {
  padding: 12px;
  border: 1px solid #e1e7ef;
  border-radius: 6px;
  background: #fbfcfe;
}

.ansys-parameter-panel__head,
.ansys-parameter-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.ansys-parameter-panel__head {
  margin-bottom: 10px;

  h4 {
    margin: 0;
    color: #19375a;
    font-size: 15px;
  }

  span {
    display: block;
    margin-top: 3px;
    color: #708198;
    font-size: 12px;
  }
}

.ansys-parameter-form {
  display: grid;
  grid-template-columns: repeat(4, minmax(150px, 1fr));
  gap: 10px;

  :deep(.el-form-item) {
    display: block;
    margin: 0;
  }

  :deep(.el-form-item__label) {
    display: block;
    height: auto;
    margin-bottom: 4px;
    color: #52667a;
    font-size: 12px;
    line-height: 1.2;
    text-align: left;
  }

  :deep(.el-input-number),
  :deep(.el-select) {
    width: 100%;
  }

  :deep(.el-input-number .el-input) {
    width: 100%;
  }
}

.ansys-parameter-form__wide {
  grid-column: 1 / -1;
}

.ansys-locked-value {
  display: flex;
  align-items: center;
  min-height: 32px;
}

.scheme-compare-panel {
  padding: 0 12px 12px;
}

.comparison-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
}

.scheme-name-cell {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;

  strong {
    color: #19375a;
  }
}

.scheme-result-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;

  span {
    color: #5f7184;
    font-size: 12px;
    line-height: 1.35;
    white-space: normal;
  }
}

.scheme-judgement {
  color: #7b8794;
  font-weight: 600;

  &.is-passed {
    color: #1f8a4c;
  }

  &.is-failed {
    color: #c23b3b;
  }

  &.is-running {
    color: #b7791f;
  }
}

.scheme-model-compare {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  align-items: stretch;
  gap: 12px;
  margin-top: 12px;
}

.scheme-model-card {
  display: flex;
  flex-direction: column;
  min-width: 0;
  padding: 10px;
  border: 1px solid #e1e7ef;
  border-radius: 6px;
  background: #fff;

  &.is-active {
    border-color: #409eff;
    box-shadow: 0 0 0 1px rgba(64, 158, 255, 0.16);
  }

  &.is-final {
    border-color: #67c23a;
  }

  :deep(.cad-viewer) {
    flex: 0 0 260px;
    height: 260px;
    min-height: 260px;
    border-radius: 4px;
  }
}

.scheme-model-card__head,
.scheme-model-card__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.scheme-model-card__head {
  margin-bottom: 8px;

  div {
    display: grid;
    gap: 2px;
    min-width: 0;
  }

  strong {
    overflow: hidden;
    color: #19375a;
    font-size: 14px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  span {
    color: #7a8da3;
    font-size: 12px;
  }
}

.scheme-model-card__foot {
  flex-wrap: wrap;
  margin-top: 8px;
  color: #52667a;
  font-size: 12px;
}

.scheme-model-card__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.scheme-parameter-grid,
.scheme-metric-list {
  display: grid;
  gap: 6px;
  margin-top: 10px;
}

.scheme-parameter-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.scheme-parameter-grid div,
.scheme-metric-list div {
  display: grid;
  grid-template-columns: minmax(62px, 0.9fr) minmax(52px, 1fr) auto;
  align-items: center;
  gap: 4px;
  min-width: 0;
  padding: 6px 8px;
  border-radius: 4px;
  background: #f7f9fc;
}

.scheme-parameter-grid span,
.scheme-metric-list span {
  overflow: hidden;
  color: #6c7f92;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.scheme-parameter-grid strong,
.scheme-metric-list strong {
  overflow: hidden;
  color: #19375a;
  font-size: 12px;
  text-align: right;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.scheme-parameter-grid em,
.scheme-metric-list em {
  color: #8a9bad;
  font-size: 12px;
  font-style: normal;
}

.scheme-ansys-image {
  margin-top: 10px;
}

.scheme-ansys-image__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 6px;

  strong {
    color: #19375a;
    font-size: 13px;
  }
}

.scheme-ansys-image__body {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 150px;
  overflow: hidden;
  border: 1px solid #e1e7ef;
  border-radius: 4px;
  color: #7a8da3;
  background: #f8fafc;

  img {
    display: block;
    width: 100%;
    height: 150px;
    object-fit: contain;
  }

  span {
    padding: 16px;
    color: #7a8da3;
    font-size: 12px;
    text-align: center;
  }
}

.verification-content {
  display: grid;
  grid-template-columns: minmax(520px, 1.12fr) minmax(420px, 0.88fr);
  gap: 14px;
  padding-top: 12px;
}

.verification-subhead {
  justify-content: space-between;
  min-height: 32px;
  margin-bottom: 8px;

  strong {
    color: #19375a;
    font-size: 14px;
  }
}

.cad-preview-area,
.ansys-result-area {
  min-width: 0;
}

.cad-preview-area {
  :deep(.cad-viewer) {
    min-height: 360px;
  }
}

.ansys-result-area {
  display: grid;
  align-content: start;
  gap: 10px;
}

.ansys-image-strip {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 156px;
  overflow: hidden;
  border: 1px solid #e1e7ef;
  border-radius: 6px;
  color: #7a8da3;
  background: #f8fafc;

  img {
    display: block;
    width: 100%;
    height: 100%;
    max-height: 260px;
    object-fit: contain;
  }

  span {
    padding: 20px;
    color: #7a8da3;
    font-size: 13px;
    text-align: center;
  }
}

.ansys-preview-scroll {
  max-height: 78vh;
  overflow: auto;
  text-align: center;

  img {
    max-width: 100%;
  }
}

.report-panel {
  padding: 0 12px 12px;
}

.report-head {
  align-items: flex-start;

  p {
    margin: 5px 0 0;
    color: #64748b;
    font-size: 13px;
    line-height: 1.45;
  }
}

.report-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-end;
}

.report-summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0;
  margin-bottom: 12px;
  padding: 10px 12px;
  border: 1px solid #e6ebf1;
  border-radius: 6px;
  background: #fbfcfe;

  div {
    min-width: 0;
    padding: 0 14px;
    border-right: 1px solid #e2e8f0;

    &:first-child {
      padding-left: 0;
    }

    &:last-child {
      border-right: 0;
    }
  }

  span {
    display: block;
    color: #708198;
    font-size: 12px;
  }

  strong {
    display: block;
    min-width: 0;
    margin-top: 3px;
    overflow: hidden;
    color: #24324f;
    font-size: 13px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.report-preview {
  display: grid;
  gap: 12px;

  section {
    min-width: 0;
    padding-top: 2px;
  }

  h4 {
    margin: 0 0 8px;
    color: #19375a;
    font-size: 14px;
  }
}

.report-preview--dialog {
  max-height: 68vh;
  padding: 0 8px 0 0;
  overflow-y: auto;
  background: #eef2f7;
}

.report-doc {
  max-width: 1120px;
  margin: 0 auto;
  padding: 30px 34px 36px;
  border: 1px solid #d8e0ea;
  background: #ffffff;
  color: #1f2a44;
  box-shadow: 0 12px 30px rgba(31, 42, 68, 0.08);
}

.report-cover {
  display: block;
  margin-bottom: 18px;
  padding: 0 0 16px;
  border: 0;
  border-bottom: 2px solid #1f3b63;
  border-radius: 0;
  background: transparent;

  span {
    color: #52667a;
    font-size: 12px;
    font-weight: 700;
  }

  h2 {
    margin: 8px 0 10px;
    color: #172b4d;
    font-size: 26px;
    line-height: 1.35;
    text-align: center;
  }

  p {
    margin: 0;
    color: #64748b;
    font-size: 13px;
    text-align: center;
  }
}

.report-section {
  padding: 22px 0 4px;
  border-top: 0;

  h4 {
    margin: 0 0 14px;
    padding: 0 0 7px;
    border-bottom: 1px solid #1f3b63;
    color: #172b4d;
    font-size: 16px;
    line-height: 1.45;
  }
}

.report-section-note {
  margin: -2px 0 10px;
  color: #52667a;
  font-size: 13px;
  line-height: 1.7;
}

.report-conclusion-section {
  padding-top: 6px;
}

.report-conclusion-text {
  margin: 0 0 12px;
  padding: 11px 12px;
  border-left: 3px solid #176db6;
  border-radius: 4px;
  color: #24324f;
  background: #f3f8ff;
  font-size: 14px;
  line-height: 1.8;
}

.approval-summary {
  display: grid;
  grid-template-columns: minmax(280px, 1.5fr) repeat(3, minmax(0, 1fr));
  gap: 8px;

  div {
    min-width: 0;
    padding: 12px;
    border: 1px solid #e6ebf1;
    border-radius: 6px;
    background: #ffffff;

    &:first-child {
      border-left: 4px solid #176db6;
      background: #f3f8ff;
    }
  }

  span {
    display: block;
    color: #708198;
    font-size: 12px;
  }

  strong {
    display: block;
    margin-top: 5px;
    color: #172b4d;
    font-size: 16px;
    line-height: 1.45;
    word-break: break-word;
  }

  p {
    margin: 8px 0 0;
    color: #52667a;
    font-size: 13px;
    line-height: 1.7;
  }
}

.report-narrative {
  margin: 0 0 16px;
  color: #24324f;
  font-size: 14px;
  line-height: 2;
  white-space: pre-wrap;
}

.report-form-table,
.report-list-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
  margin: 10px 0 18px;
  color: #1f2a44;
  font-size: 13px;

  th,
  td {
    border: 1px solid #b9c6d6;
    padding: 9px 10px;
    line-height: 1.75;
    text-align: left;
    vertical-align: top;
    word-break: break-word;
  }

  th {
    width: 15%;
    color: #24324f;
    font-weight: 700;
    background: #eef3f8;
  }

  td {
    background: #ffffff;
  }
}

.report-list-table {
  table-layout: auto;

  thead th {
    text-align: center;
  }
}

.report-decision-cell {
  color: #0f6b3f;
  font-size: 16px;
  font-weight: 700;
}

.report-key-metrics {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 8px;

  div {
    min-width: 0;
    padding: 10px 12px;
    border: 1px solid #e6ebf1;
    border-radius: 6px;
    background: #ffffff;
  }

  span {
    display: block;
    color: #708198;
    font-size: 12px;
  }

  strong {
    display: block;
    margin-top: 4px;
    color: #172b4d;
    font-size: 14px;
    line-height: 1.45;
    white-space: normal;
    word-break: break-word;
  }
}

.report-meta-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 8px;
}

.report-meta-grid div {
  min-width: 0;
  padding: 9px 10px;
  border: 1px solid #e6ebf1;
  border-radius: 6px;
  background: #fbfcfe;

  span {
    display: block;
    color: #708198;
    font-size: 12px;
  }

  strong {
    display: block;
    margin-top: 4px;
    color: #24324f;
    font-size: 14px;
    line-height: 1.55;
    white-space: normal;
    word-break: break-word;
  }
}

.problem-statement {
  margin-top: 8px;
  padding: 11px 12px;
  border: 1px solid #e6ebf1;
  border-radius: 6px;
  background: #ffffff;

  span {
    display: block;
    margin-bottom: 6px;
    color: #708198;
    font-size: 12px;
    font-weight: 700;
  }

  p {
    margin: 0;
    color: #24324f;
    font-size: 14px;
    line-height: 1.8;
    white-space: pre-wrap;
    word-break: break-word;
  }
}

.report-summary-grid,
.constraint-summary-grid,
.risk-summary-list {
  display: grid;
  gap: 8px;
  margin-top: 10px;
}

.report-summary-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.constraint-summary-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.risk-summary-list {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.report-summary-grid div,
.constraint-summary-grid div,
.risk-summary-list div {
  min-width: 0;
  padding: 10px 12px;
  border: 1px solid #e6ebf1;
  border-radius: 6px;
  background: #fbfcfe;

  span {
    display: block;
    color: #708198;
    font-size: 12px;
  }

  strong {
    display: block;
    margin-top: 4px;
    color: #172b4d;
    font-size: 14px;
    line-height: 1.55;
    word-break: break-word;
  }

  p {
    margin: 6px 0 0;
    color: #64748b;
    font-size: 12px;
    line-height: 1.65;
    word-break: break-word;
  }
}

.report-visual-grid {
  display: grid;
  grid-template-columns: minmax(360px, 1fr) minmax(360px, 1fr);
  align-items: start;
  gap: 12px;
  margin-bottom: 12px;
}

.report-visual-card {
  min-width: 0;
}

.report-visual-card--cad {
  :deep(.cad-viewer) {
    height: 300px !important;
    min-height: 300px !important;
    border-radius: 6px;
  }
}

.report-visual-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 8px;

  strong {
    color: #19375a;
    font-size: 14px;
  }

  span {
    color: #708198;
    font-size: 12px;
  }
}

.report-ansys-figure {
  display: flex;
  align-items: center;
  justify-content: center;
  aspect-ratio: 16 / 9;
  width: 100%;
  min-height: 0;
  overflow: hidden;
  border: 1px solid #e1e7ef;
  border-radius: 6px;
  color: #7a8da3;
  background: #f8fafc;

  img {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: contain;
    object-position: center center;
  }

  span {
    padding: 20px;
    color: #7a8da3;
    font-size: 13px;
    text-align: center;
  }
}

.report-info-grid,
.report-param-grid,
.report-result-grid {
  display: grid;
  gap: 8px;
}

.report-info-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.report-param-grid {
  grid-template-columns: repeat(7, minmax(0, 1fr));
}

.report-result-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.report-info-grid div,
.report-param-grid div,
.report-result-grid div {
  min-width: 0;
  padding: 9px 10px;
  border: 1px solid #e6ebf1;
  border-radius: 6px;
  background: #fbfcfe;

  span,
  em {
    display: block;
    overflow: hidden;
    color: #708198;
    font-size: 12px;
    font-style: normal;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  strong {
    display: block;
    min-width: 0;
    margin-top: 4px;
    overflow: hidden;
    color: #24324f;
    font-size: 14px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.report-evidence-list {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;

  div {
    min-width: 0;
    padding: 10px 12px;
    border: 1px solid #e6ebf1;
    border-radius: 6px;
    background: #fbfcfe;
  }

  span {
    display: block;
    color: #708198;
    font-size: 12px;
  }

  strong {
    display: block;
    margin-top: 4px;
    color: #24324f;
    font-size: 14px;
    line-height: 1.45;
  }

  p {
    margin: 6px 0 0;
    color: #64748b;
    font-size: 12px;
    line-height: 1.55;
    word-break: break-word;
  }
}

.design-report-dialog {
  :deep(.el-table .cell) {
    white-space: normal;
    word-break: break-word;
    line-height: 1.5;
  }
}

.report-decision-form {
  max-width: 920px;
  padding: 10px 12px;
  border: 1px solid #e6ebf1;
  border-radius: 6px;
  background: #fbfcfe;
}

@media (max-width: 1100px) {
  .solve-command-grid,
  .objective-constraint-columns {
    grid-template-columns: 1fr;
  }

  .objective-workbench-toolbar {
    align-items: flex-start;
    flex-direction: column;

    div {
      justify-content: flex-start;
    }
  }

  .surrogate-grid {
    grid-template-columns: 1fr;
  }

  .best-solution {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .weight-editor {
    grid-template-columns: 1fr;
  }

  .verification-toolbar,
  .verification-content {
    grid-template-columns: 1fr;
  }

  .verification-form {
    grid-template-columns: repeat(2, minmax(160px, 1fr));
  }

  .ansys-parameter-form {
    grid-template-columns: repeat(2, minmax(160px, 1fr));
  }

  .report-summary,
  .report-key-metrics,
  .report-meta-grid,
  .report-info-grid,
  .report-param-grid,
  .report-result-grid,
  .report-evidence-list {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
