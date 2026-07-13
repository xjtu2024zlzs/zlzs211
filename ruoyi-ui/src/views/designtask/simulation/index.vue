<template>
  <div class="design-platform-view">
    <div class="design-platform-shell">
      <section class="platform-topbar">
        <div>
          <h1 class="platform-title">{{ hasTask ? taskTitle : '仿真验证确认' }}</h1>
        </div>
        <div class="topbar-meta">
          <el-button v-if="hasTask" plain icon="Back" class="topbar-return" @click="backToInbox">返回任务列表</el-button>
          <div class="meta-chip">
            <span class="meta-dot meta-dot--cyan"></span>
            <span>{{ hasTask ? accessLabel : tabLabel }}</span>
          </div>
          <div class="meta-chip">
            <span class="meta-dot meta-dot--green"></span>
            <span>仿真与审批</span>
          </div>
        </div>
      </section>

      <section v-if="!hasTask" class="section-block">
        <div class="section-header">
          <div>
            <h2 class="section-title">我的仿真验证确认任务</h2>
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
          title="当前任务未流转到你，暂不能执行仿真确认或审批。"
          type="info"
          :closable="false"
          show-icon
        />

        <section class="section-block">
          <div class="section-header">
            <div>
              <h2 class="section-title">CAD 参数建模验证</h2>
            </div>
            <div class="action-row">
              <el-tag :type="cadStatusType(cadModel.status)" effect="light">{{ cadModel.statusLabel || '未提交' }}</el-tag>
              <el-button plain icon="Refresh" :loading="cadRefreshing" @click="refreshCadModel">刷新状态</el-button>
              <el-button type="primary" icon="Box" :loading="cadSubmitting" :disabled="!canEditCad" @click="submitCadModel">
                {{ cadModel.status === 'SUCCESS' ? '重新生成' : '生成 CAD 模型' }}
              </el-button>
            </div>
          </div>

          <div class="cad-model-grid">
            <div class="cad-form-panel">
              <div class="cad-form-scroll">
                <el-form :model="cadForm" label-width="120px" class="cad-param-form">
                  <el-form-item label="L1 / mm">
                    <el-input-number v-model="cadForm.L1" :min="1" :precision="2" :step="10" :disabled="!canEditCad" controls-position="right" />
                  </el-form-item>
                  <el-form-item label="L2 / mm">
                    <el-input-number v-model="cadForm.L2" :min="1" :precision="2" :step="10" :disabled="!canEditCad" controls-position="right" />
                  </el-form-item>
                  <el-form-item label="R / mm">
                    <el-input-number v-model="cadForm.R" :min="1" :precision="2" :step="1" :disabled="!canEditCad" controls-position="right" />
                  </el-form-item>
                  <el-form-item label="θ1 / °">
                    <el-input-number v-model="cadForm.theta1" :precision="2" :step="5" :disabled="!canEditCad" controls-position="right" />
                  </el-form-item>
                  <el-form-item label="θ2 / °">
                    <el-input-number v-model="cadForm.theta2" :precision="2" :step="5" :disabled="!canEditCad" controls-position="right" />
                  </el-form-item>
                  <el-form-item label="外径 / mm">
                    <el-input-number v-model="cadForm.pipeDiameter" :min="0.1" :precision="2" :step="0.1" :disabled="!canEditCad" controls-position="right" />
                  </el-form-item>
                  <el-form-item label="内径 / mm">
                    <el-input-number v-model="cadForm.pipeInnerDiameter" :min="0.1" :max="cadForm.pipeDiameter - 0.1" :precision="2" :step="0.1" :disabled="!canEditCad" controls-position="right" />
                  </el-form-item>
                </el-form>

                <div class="cad-constraint-list">
                  <div><span>水平总长</span><strong>600 mm</strong></div>
                  <div><span>竖直总高</span><strong>300 mm</strong></div>
                  <div><span>管道外径</span><strong>{{ cadPipeDiameter }} mm</strong></div>
                  <div><span>管道内径</span><strong>{{ cadPipeInnerDiameter }} mm</strong></div>
                  <div><span>管道壁厚</span><strong>{{ cadWallThickness }} mm</strong></div>
                </div>
              </div>
            </div>

            <div class="cad-result-panel">
              <div class="cad-result-scroll">
                <div class="cad-result-grid">
                  <div>
                    <span>L3</span>
                    <strong>{{ valueOrDash(cadModel.l3, ' mm') }}</strong>
                  </div>
                  <div>
                    <span>起始方向角</span>
                    <strong>{{ valueOrDash(cadModel.initialAngle, ' °') }}</strong>
                  </div>
                  <div>
                    <span>几何闭合状态</span>
                    <strong>{{ cadModel.closureStatus || '-' }}</strong>
                  </div>
                </div>

                <el-alert
                  v-if="cadModel.status === 'FAILED'"
                  class="mt-12"
                  :title="cadModel.errorMessage || 'CAD 建模失败'"
                  type="error"
                  show-icon
                  :closable="false"
                />

                <div class="cad-file-actions">
                  <el-button icon="Download" :disabled="!cadModel.files?.sldprt" @click="downloadCadFile('sldprt')">下载 SLDPRT</el-button>
                  <el-button icon="Download" :disabled="!cadModel.files?.stl" @click="downloadCadFile('stl')">下载 STL</el-button>
                </div>

                <cad-stl-viewer :model-data="cadStlData" :empty-text="cadEmptyText" />
              </div>
            </div>
          </div>
        </section>

        <section class="section-block">
          <div class="section-header">
            <div>
              <h2 class="section-title">ANSYS 应力仿真结果</h2>
            </div>
            <div class="action-row">
              <el-tag :type="ansysStatusType(ansysSimulation.status)">{{ ansysSimulation.statusLabel || '未提交' }}</el-tag>
              <el-button plain icon="Refresh" :loading="ansysRefreshing" @click="refreshAnsysSimulation">刷新状态</el-button>
              <el-button type="primary" plain icon="Check" :loading="ansysSaving" :disabled="!canSaveAnsysParams" @click="saveAnsysParameters">
                保存参数
              </el-button>
              <el-button type="success" icon="Monitor" :loading="ansysOpening" :disabled="!canOpenAnsys" @click="prepareAnsysSimulation">
                打开 ANSYS
              </el-button>
              <el-button type="warning" plain icon="Download" :loading="ansysImporting" :disabled="!canImportAnsysResult" @click="readAnsysResult">
                读取结果
              </el-button>
            </div>
          </div>

          <div class="ansys-model-selector">
            <el-radio-group v-model="selectedAnsysSimulationMode" :disabled="ansysSaving || ansysOpening || ansysImporting || ansysRefreshing">
              <el-radio-button
                v-for="mode in ansysSimulationModes"
                :key="mode.value"
                :label="mode.value"
              >
                {{ mode.label }}
              </el-radio-button>
            </el-radio-group>
          </div>

          <el-alert
            v-if="canSimulate && cadModel.status !== 'SUCCESS'"
            class="mb-16"
            title="CAD 模型成功后才能打开 ANSYS。"
            type="warning"
            :closable="false"
            show-icon
          />

          <el-alert
            v-if="selectedAnsysSimulationMode !== ANSYS_MODE_DEMO"
            class="mb-16"
            title="参数化打开 ANSYS 先用于演示仿真模型。双向流固耦合模型后续再接入同样的人工确认流程。"
            type="info"
            :closable="false"
            show-icon
          />

          <el-alert
            v-if="ansysSimulation.status === 'FAILED'"
            class="mb-16"
            :title="ansysSimulation.errorMessage || 'ANSYS 仿真失败'"
            type="error"
            :closable="false"
            show-icon
          />

          <div class="ansys-parameter-panel">
            <el-form :model="ansysParameterForm" label-position="top" class="ansys-parameter-form">
              <div class="ansys-param-group">
                <div class="ansys-param-group__head">
                  <strong>压力载荷</strong>
                  <el-button size="small" plain icon="RefreshLeft" :disabled="!canSaveAnsysParams" @click="resetAnsysParametersFromFaultPipe">
                    载入原始参数
                  </el-button>
                </div>
                <div class="ansys-param-grid ansys-param-grid--four">
                  <el-form-item label="初始压强 / Pa">
                    <el-input-number v-model="ansysParameterForm.pressure.initialPressurePa" :min="0" :controls="false" :disabled="!canSaveAnsysParams" />
                  </el-form-item>
                  <el-form-item label="峰值压强 / Pa">
                    <el-input-number v-model="ansysParameterForm.pressure.peakPressurePa" :min="0" :controls="false" :disabled="!canSaveAnsysParams" />
                  </el-form-item>
                  <el-form-item label="上升时间 / s">
                    <el-input-number v-model="ansysParameterForm.pressure.riseTimeS" :min="0.000001" :step="0.001" :precision="6" :controls="false" :disabled="!canSaveAnsysParams" />
                  </el-form-item>
                  <el-form-item label="网格尺寸 / mm">
                    <el-input-number v-model="ansysParameterForm.mesh.globalSizeMm" :min="0.1" :step="0.5" :precision="2" :controls="false" :disabled="!canSaveAnsysParams" />
                  </el-form-item>
                </div>
                <el-form-item label="入口压强表达式">
                  <el-input v-model="ansysParameterForm.pressure.expression" :disabled="!canSaveAnsysParams" />
                </el-form-item>
              </div>

              <div class="ansys-param-group">
                <div class="ansys-param-group__head">
                  <strong>材料与边界</strong>
                  <el-tag size="small" type="info">{{ ansysParameterForm.boundary.pressureFaceMode === 'inner_wall' ? '内壁加载' : '自定义加载' }}</el-tag>
                </div>
                <div class="ansys-param-grid ansys-param-grid--four">
                  <el-form-item label="材料">
                    <el-input v-model="ansysParameterForm.material.materialName" :disabled="!canSaveAnsysParams" />
                  </el-form-item>
                  <el-form-item label="杨氏模量 / Pa">
                    <el-input-number v-model="ansysParameterForm.material.youngModulusPa" :min="0" :controls="false" :disabled="!canSaveAnsysParams" />
                  </el-form-item>
                  <el-form-item label="泊松比">
                    <el-input-number v-model="ansysParameterForm.material.poissonRatio" :min="0" :max="0.5" :step="0.01" :precision="3" :controls="false" :disabled="!canSaveAnsysParams" />
                  </el-form-item>
                  <el-form-item label="屈服强度 / Pa">
                    <el-input-number v-model="ansysParameterForm.material.tensileYieldStrengthPa" :min="0" :controls="false" :disabled="!canSaveAnsysParams" />
                  </el-form-item>
                  <el-form-item label="极限强度 / Pa">
                    <el-input-number v-model="ansysParameterForm.material.tensileUltimateStrengthPa" :min="0" :controls="false" :disabled="!canSaveAnsysParams" />
                  </el-form-item>
                  <el-form-item label="固定约束">
                    <el-select v-model="ansysParameterForm.boundary.fixedSupportMode" :disabled="!canSaveAnsysParams">
                      <el-option label="两端固定" value="both_ends" />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="压力加载面">
                    <el-select v-model="ansysParameterForm.boundary.pressureFaceMode" :disabled="!canSaveAnsysParams">
                      <el-option label="管道内壁" value="inner_wall" />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="结果输出">
                    <div class="ansys-result-switches">
                      <el-switch v-model="ansysParameterForm.result.equivalentStress" active-text="应力" :disabled="!canSaveAnsysParams" />
                      <el-switch v-model="ansysParameterForm.result.totalDeformation" active-text="变形" :disabled="!canSaveAnsysParams" />
                      <el-switch v-model="ansysParameterForm.result.exportStressImage" active-text="云图" :disabled="!canSaveAnsysParams" />
                    </div>
                  </el-form-item>
                </div>
              </div>
            </el-form>
          </div>

          <div class="ansys-result-grid">
            <div class="table-shell ansys-table-panel">
              <el-table :data="ansysSimulation.metrics || []" stripe class="platform-table">
                <el-table-column label="指标" prop="name" min-width="160" />
                <el-table-column label="数值" prop="value" width="130" />
                <el-table-column label="单位" prop="unit" width="90" />
                <el-table-column label="来源" prop="source" min-width="160" show-overflow-tooltip />
              </el-table>
            </div>
            <div class="ansys-image-panel">
              <template v-if="ansysStressImage">
                <div class="ansys-image-actions">
                  <el-button size="small" plain icon="View" @click="ansysImagePreviewVisible = true">查看原图</el-button>
                </div>
                <img
                  :src="ansysStressImage"
                  alt="ANSYS stress contour"
                />
              </template>
              <span v-else>{{ ansysImageEmptyText }}</span>
            </div>
          </div>
        </section>

        <div class="content-grid content-grid--balanced simulation-summary-grid">
          <section class="section-block">
            <div class="section-header">
              <div>
                <h2 class="section-title">管段原始设计参数</h2>
              </div>
              <el-tag v-if="faultPipeParameters.setCode">{{ faultPipeParameters.setCode }}</el-tag>
            </div>

            <div v-if="faultPipeSummaryItems.length" class="fixed-input-meta">
              <div v-for="item in faultPipeSummaryItems" :key="item.label" class="fixed-input-meta__item">
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
              </div>
            </div>

            <el-collapse v-if="faultPipeParameterGroups.length" class="mt-12">
              <el-collapse-item v-for="group in faultPipeParameterGroups" :key="group.groupCode" :title="group.groupName">
                <div class="table-shell">
                  <el-table :data="group.items || []" stripe class="platform-table">
                    <el-table-column label="参数" prop="paramName" min-width="160" show-overflow-tooltip />
                    <el-table-column label="值 / 表达式" min-width="300" show-overflow-tooltip>
                      <template #default="{ row }">{{ row.formulaText || row.paramValue || '-' }}</template>
                    </el-table-column>
                    <el-table-column label="单位" prop="paramUnit" width="100" />
                  </el-table>
                </div>
              </el-collapse-item>
            </el-collapse>
          </section>

          <section class="section-block">
            <div class="section-header">
              <div>
                <h2 class="section-title">优化前后指标对比</h2>
              </div>
              <div class="action-row">
                <el-button type="primary" icon="Refresh" :disabled="!hasTask" @click="refreshComparison">刷新对比</el-button>
              </div>
            </div>

            <div class="table-shell">
              <el-table :data="simulation.metrics || []" stripe class="platform-table">
                <el-table-column label="指标" prop="name" min-width="160" />
                <el-table-column label="优化前" prop="before" />
                <el-table-column label="优化后" prop="after" />
                <el-table-column label="单位" prop="unit" />
                <el-table-column label="趋势" width="120">
                  <template #default="{ row }">
                    <el-tag :type="metricTrendType(row.trend)">{{ metricTrendLabel(row.trend) }}</el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </section>
        </div>

        <section class="section-block conclusion-section">
          <div class="section-header">
            <div>
              <h2 class="section-title">验证结论</h2>
            </div>
          </div>
          <div class="conclusion-body">
            <el-result
              v-if="simulation.verified"
              :icon="simulation.passed ? 'success' : 'warning'"
              :title="simulation.passed ? '仿真验证通过' : '仿真验证未通过'"
              :sub-title="simulation.conclusion"
            />
            <el-form v-else :model="simulationDecision" label-width="90px" class="conclusion-form">
              <el-form-item label="验证结论">
                <el-radio-group v-model="simulationDecision.passed" :disabled="!canConfirmSimulationDecision">
                  <el-radio :value="true">仿真验证通过</el-radio>
                  <el-radio :value="false">仿真验证不通过</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-alert
                v-if="simulation.conclusion"
                class="mb-16"
                type="info"
                :title="simulation.conclusion"
                :closable="false"
                show-icon
              />
              <div class="conclusion-actions">
                <el-button type="primary" icon="Check" :disabled="!canSubmitSimulationDecision" @click="submitSimulationDecision">提交验证结论</el-button>
              </div>
            </el-form>
          </div>
        </section>

        <section class="section-block">
          <div class="section-header">
            <div>
              <h2 class="section-title">优化方案审批</h2>
            </div>
          </div>

          <el-form :model="approval" label-width="90px" class="mt-12">
            <el-form-item label="审批结论">
              <el-radio-group v-model="approval.approved" :disabled="!canApprove">
                <el-radio :value="true">通过</el-radio>
                <el-radio :value="false">退回模型解耦</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="审批意见">
              <el-input v-model="approval.comment" type="textarea" :rows="4" :disabled="!canApprove" />
            </el-form-item>
            <el-button type="primary" icon="Select" :disabled="!canApprove" @click="submitApproval">提交审批</el-button>
          </el-form>
        </section>
      </template>
    </div>
    <el-dialog
      v-model="ansysImagePreviewVisible"
      title="ANSYS 应力云图"
      width="92vw"
      append-to-body
      class="ansys-image-dialog"
    >
      <div class="ansys-preview-scroll">
        <img v-if="ansysStressImage" :src="ansysStressImage" alt="ANSYS stress contour preview" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { saveAs } from 'file-saver'
import {
  approveTask,
  getAnsysSimulationImage,
  getAnsysSimulationTask,
  getCadModelFile,
  getCadModelTask,
  getDashboard,
  getDesignTask,
  importAnsysSimulationResult,
  openAnsysSimulationTask,
  runSimulation,
  saveAnsysSimulationParams,
  submitCadModelTask
} from '@/api/designtask/optimization'
import CadStlViewer from './CadStlViewer.vue'
import ansysStressPlaceholder from '@/assets/designtask/ansys-stress-placeholder.png'

const route = useRoute()
const router = useRouter()
const taskId = ref(route.query.taskId ? Number(route.query.taskId) : null)
const taskTitle = ref('仿真验证确认')
const currentNodeKey = ref('')
const access = ref({ mode: 'wait', label: '等待' })
const simulation = ref({ verified: false, passed: null, metrics: [], conclusion: '' })
const simulationDecision = ref({ passed: null })
const canConfirmSimulation = ref(false)
const ansysSimulation = ref({ status: 'NOT_SUBMITTED', statusLabel: '未提交', metrics: [], placeholder: true })
const ansysStressObjectUrl = ref('')
const ansysStressImageKey = ref('')
const ansysImagePreviewVisible = ref(false)
const faultPipeParameters = ref({ groups: [] })
const ansysParameterForm = ref(defaultAnsysParameterForm())
const approval = ref({ approved: true, comment: '仿真验证结果满足设计要求，同意通过。' })
const cadForm = ref({ L1: 280, L2: 150, R: 20, theta1: 110, theta2: 120, pipeDiameter: 9.53, pipeInnerDiameter: 7.73 })
const cadPipeDiameter = computed(() => Number(cadForm.value.pipeDiameter || 9.53).toFixed(2))
const cadPipeInnerDiameter = computed(() => Number(cadForm.value.pipeInnerDiameter || 7.73).toFixed(2))
const cadWallThickness = computed(() => {
  const outer = Number(cadForm.value.pipeDiameter || 0)
  const inner = Number(cadForm.value.pipeInnerDiameter || 0)
  return Math.max((outer - inner) / 2, 0).toFixed(2)
})
const cadModel = ref({ status: 'NOT_SUBMITTED', statusLabel: '未提交', params: {}, files: {} })
const cadStlData = ref(null)
const cadSubmitting = ref(false)
const cadRefreshing = ref(false)
const ansysRefreshing = ref(false)
const ansysSaving = ref(false)
const ansysOpening = ref(false)
const ansysImporting = ref(false)
const ANSYS_MODE_DEMO = 'DEMO_SIMULATION_MODEL'
const ANSYS_MODE_FSI = 'BIDIRECTIONAL_FSI_MODEL'
const ansysSimulationModes = [
  {
    value: ANSYS_MODE_DEMO,
    label: '演示仿真模型'
  },
  {
    value: ANSYS_MODE_FSI,
    label: '双向流固耦合仿真模型'
  }
]
const selectedAnsysSimulationMode = ref(
  [ANSYS_MODE_DEMO, ANSYS_MODE_FSI].includes(route.query.ansysMode) ? route.query.ansysMode : ANSYS_MODE_DEMO
)
let cadPollTimer = null
let ansysPollTimer = null
const inboxLoading = ref(false)
const pendingTasks = ref([])
const handledTasks = ref([])
const relatedTasks = ref([])
const activeTab = ref(['handled', 'related'].includes(route.query.tab) ? route.query.tab : 'pending')

const hasTask = computed(() => !!taskId.value)
const accessLabel = computed(() => access.value.label || (access.value.mode === 'enter' ? '可处理' : '查看'))
const tabLabel = computed(() => ({ pending: '待处理', handled: '已处理', related: '相关任务' }[activeTab.value] || '我的任务'))
const visibleInboxTasks = computed(() => {
  if (activeTab.value === 'pending') return pendingTasks.value
  if (activeTab.value === 'handled') return handledTasks.value
  return relatedTasks.value
})
const viewOnly = computed(() => route.query.mode === 'view' && access.value.mode !== 'enter')
const canSimulate = computed(() => !viewOnly.value && access.value.mode === 'enter' && currentNodeKey.value === 'simulation_confirm')
const canSaveAnsysParams = computed(() => canSimulate.value)
const canOpenAnsys = computed(() => canSimulate.value && cadModel.value.status === 'SUCCESS' && selectedAnsysSimulationMode.value === ANSYS_MODE_DEMO)
const canImportAnsysResult = computed(() => canSimulate.value && selectedAnsysSimulationMode.value === ANSYS_MODE_DEMO && !!ansysSimulation.value.resultFilePath)
const canConfirmSimulationDecision = computed(() => !viewOnly.value && canConfirmSimulation.value)
const canSubmitSimulationDecision = computed(() => canConfirmSimulationDecision.value && simulationDecision.value.passed !== null && simulationDecision.value.passed !== undefined)
const canEditCad = computed(() => !viewOnly.value && access.value.mode === 'enter')
const canApprove = computed(() => !viewOnly.value && access.value.mode === 'enter' && currentNodeKey.value === 'leader_approve')
const faultPipeParameterGroups = computed(() => faultPipeParameters.value.groups || [])
const faultPipeSummaryItems = computed(() => {
  return [
    { label: '参数集', value: faultPipeParameters.value.setName },
    { label: '管段编号', value: faultPipeParameters.value.faultSegmentName },
    { label: '材料', value: faultPipeParameters.value.materialName }
  ].filter(item => item.value)
})
const ansysStressImage = computed(() => {
  if (ansysStressObjectUrl.value) return ansysStressObjectUrl.value
  if (ansysSimulation.value.placeholder && ansysSimulation.value.status === 'SUCCESS') return ansysStressPlaceholder
  return ''
})
const ansysImageEmptyText = computed(() => {
  if (ansysSimulation.value.status === 'FAILED') return 'ANSYS 仿真失败，暂无真实云图'
  if (['QUEUED', 'RUNNING'].includes(ansysSimulation.value.status)) return 'ANSYS 仿真处理中'
  return '暂无应力云图'
})
const cadEmptyText = computed(() => {
  if (cadModel.value.status === 'FAILED') return cadModel.value.errorMessage || 'CAD 建模失败'
  if (['QUEUED', 'RUNNING'].includes(cadModel.value.status)) return 'CAD 模型生成中'
  return '请先生成 CAD 模型'
})

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
  return `IF(t <= ${riseTimeS}, ${initialPressurePa} + (${peakPressurePa} - ${initialPressurePa}) * t / ${riseTimeS}, ${peakPressurePa})`
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
      riseTimeS,
      expression: faultPipeValue('INLET_PRESSURE_EXPRESSION') || pressureExpression(initialPressurePa, peakPressurePa, riseTimeS)
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
  const initialPressurePa = numberValue(pressure.initialPressurePa, fallback.pressure.initialPressurePa)
  const peakPressurePa = numberValue(pressure.peakPressurePa, fallback.pressure.peakPressurePa)
  const riseTimeS = numberValue(pressure.riseTimeS, fallback.pressure.riseTimeS)
  ansysParameterForm.value = {
    analysisType: parameters?.analysisType || fallback.analysisType,
    pressure: {
      initialPressurePa,
      peakPressurePa,
      riseTimeS,
      expression: pressure.expression || fallback.pressure.expression || pressureExpression(initialPressurePa, peakPressurePa, riseTimeS)
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
      pressureFaceMode: boundary.pressureFaceMode || fallback.boundary.pressureFaceMode
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
  if (showMessage) {
    ElMessage.success('已载入原始管段参数')
  }
}

function buildAnsysPayload(extra = {}) {
  const form = ansysParameterForm.value
  const initialPressurePa = numberValue(form.pressure.initialPressurePa, 101325)
  const peakPressurePa = numberValue(form.pressure.peakPressurePa, 30000000)
  const riseTimeS = numberValue(form.pressure.riseTimeS, 0.001)
  const expression = form.pressure.expression || pressureExpression(initialPressurePa, peakPressurePa, riseTimeS)
  return {
    simulationMode: selectedAnsysSimulationMode.value,
    simulationParameters: {
      analysisType: form.analysisType || 'static_structural',
      pressure: {
        initialPressurePa,
        peakPressurePa,
        riseTimeS,
        expression
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
        pressureFaceMode: form.boundary.pressureFaceMode || 'inner_wall'
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

function loadInbox() {
  inboxLoading.value = true
  if (activeTab.value === 'pending') {
    getDashboard({ scope: 'related' }).then(res => {
      const rows = res.data?.tasks || []
      pendingTasks.value = rows.filter(row => ['simulation_confirm', 'leader_approve'].includes(row.currentNodeKey))
    }).finally(() => {
      inboxLoading.value = false
    })
    return
  }

  if (activeTab.value === 'related') {
    getDashboard({ scope: 'related' }).then(res => {
      const rows = res.data?.tasks || []
      relatedTasks.value = rows
        .filter(row => ['simulation_confirm', 'leader_approve', 'end'].includes(row.currentNodeKey))
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
      const reason = handledReason(detailData)
      if (reason) {
        handled.push({
          ...row,
          action: { mode: 'view', label: '查看', reason },
          handledReason: reason
        })
      }
    }
    handledTasks.value = handled
  }).finally(() => {
    inboxLoading.value = false
  })
}

function relatedReason(nodeKey) {
  if (nodeKey === 'end') {
    return '可查看最终仿真与审批结果'
  }
  if (nodeKey === 'leader_approve') {
    return '可查看仿真确认结果，等待审批完成'
  }
  return '仿真确认阶段，可查看当前指标结果'
}

function handledReason(detailData) {
  if (!detailData?.task) return ''
  const task = detailData.task
  const currentUserId = Number(detailData.currentUserId)
  if (Number(task.manufacturingUserId) === currentUserId && ['leader_approve', 'end'].includes(task.currentNodeKey)) {
    return '已完成仿真确认'
  }
  if (Number(task.leaderUserId) === currentUserId && task.currentNodeKey === 'end') {
    return '已完成优化方案审批'
  }
  return ''
}

function changeTab() {
  router.replace({ path: '/designtask/simulation', query: { tab: activeTab.value } })
  loadInbox()
}

function openTask(row) {
  const forceView = activeTab.value !== 'pending'
  router.push({
    path: '/designtask/simulation',
    query: { taskId: row.taskId, mode: forceView ? 'view' : row.action?.mode || 'view', tab: activeTab.value }
  })
}

function backToInbox() {
  router.push({ path: '/designtask/simulation', query: { tab: activeTab.value } })
}

function actionType(mode) {
  return { enter: 'primary', view: 'info', wait: 'info' }[mode] || 'info'
}

function loadData() {
  if (!taskId.value) {
    loadInbox()
    return
  }
  getDesignTask(taskId.value).then(res => {
    const data = res.data || {}
    taskTitle.value = data.task?.taskName || '仿真验证确认'
    currentNodeKey.value = data.nodeKey || data.task?.currentNodeKey || ''
    access.value = data.access || { mode: 'wait', label: '等待' }
    simulation.value = data.simulation || simulation.value
    canConfirmSimulation.value = !!data.canConfirmSimulation
    simulationDecision.value.passed = simulation.value.verified ? simulation.value.passed : null
    faultPipeParameters.value = data.faultPipeParameters || { groups: [] }
    applyAnsysSimulation(data.ansysSimulation)
    if (data.ansysSimulation?.simulationMode !== selectedAnsysSimulationMode.value) {
      refreshAnsysSimulation(true)
    }
    applyCadModel(data.cadModel)
    applySurrogateSolution(data.surrogateSolve)
  })
}

function applyCadModel(model) {
  cadModel.value = model || { status: 'NOT_SUBMITTED', statusLabel: '未提交', params: {}, files: {} }
  if (cadModel.value.params && Object.keys(cadModel.value.params).length) {
    cadForm.value = {
      L1: Number(cadModel.value.params.L1 ?? cadForm.value.L1),
      L2: Number(cadModel.value.params.L2 ?? cadForm.value.L2),
      R: Number(cadModel.value.params.R ?? cadForm.value.R),
      theta1: Number(cadModel.value.params.theta1 ?? cadForm.value.theta1),
      theta2: Number(cadModel.value.params.theta2 ?? cadForm.value.theta2),
      pipeDiameter: Number(cadModel.value.params.pipeDiameter ?? cadForm.value.pipeDiameter),
      pipeInnerDiameter: Number(cadModel.value.params.pipeInnerDiameter ?? cadForm.value.pipeInnerDiameter)
    }
  }
  if (cadModel.value.status === 'SUCCESS' && cadModel.value.files?.stl) {
    loadCadStl()
  } else if (cadModel.value.status !== 'SUCCESS') {
    cadStlData.value = null
  }
  if (['QUEUED', 'RUNNING'].includes(cadModel.value.status)) {
    startCadPolling()
  } else {
    stopCadPolling()
  }
}

function applySurrogateSolution(surrogateSolve) {
  const best = surrogateSolve?.bestSolution || {}
  const surrogateConfirmed = surrogateSolve?.confirmed || surrogateSolve?.status === 'CONFIRMED'
  const shouldUseSurrogate = surrogateConfirmed && cadModel.value.status === 'NOT_SUBMITTED' && Object.keys(best).length
  if (!shouldUseSurrogate) return
  cadForm.value = {
    L1: Number(best.L1 ?? cadForm.value.L1),
    L2: Number(best.L2 ?? cadForm.value.L2),
    R: Number(best.R ?? cadForm.value.R),
    theta1: Number(best.theta1 ?? cadForm.value.theta1),
    theta2: Number(best.theta2 ?? cadForm.value.theta2),
    pipeDiameter: Number(cadForm.value.pipeDiameter || 9.53),
    pipeInnerDiameter: Number(cadForm.value.pipeInnerDiameter || 7.73)
  }
}

function applyAnsysSimulation(model) {
  ansysSimulation.value = model || ansysSimulation.value
  applyAnsysParameters(ansysSimulation.value.input?.simulationParameters)
  if (['QUEUED', 'RUNNING'].includes(ansysSimulation.value.status)) {
    clearAnsysStressImage()
    startAnsysPolling()
  } else {
    stopAnsysPolling()
    loadAnsysStressImage()
  }
}

function clearAnsysStressImage() {
  if (ansysStressObjectUrl.value) {
    URL.revokeObjectURL(ansysStressObjectUrl.value)
  }
  ansysStressObjectUrl.value = ''
  ansysStressImageKey.value = ''
}

function loadAnsysStressImage() {
  const imagePath = ansysSimulation.value.stressImageUrl
  const shouldLoad = taskId.value &&
    ['SUCCESS', 'RESULT_IMPORTED'].includes(ansysSimulation.value.status) &&
    !ansysSimulation.value.placeholder &&
    imagePath
  if (!shouldLoad) {
    clearAnsysStressImage()
    return
  }
  const imageKey = `${taskId.value}:${selectedAnsysSimulationMode.value}:${imagePath}:${ansysSimulation.value.updatedAt || ''}`
  if (ansysStressImageKey.value === imageKey && ansysStressObjectUrl.value) return
  getAnsysSimulationImage(taskId.value, { simulationMode: selectedAnsysSimulationMode.value }).then(data => {
    clearAnsysStressImage()
    ansysStressObjectUrl.value = URL.createObjectURL(new Blob([data], { type: 'image/png' }))
    ansysStressImageKey.value = imageKey
  }).catch(() => {
    clearAnsysStressImage()
  })
}

function saveAnsysParameters() {
  if (!taskId.value) return
  ansysSaving.value = true
  saveAnsysSimulationParams(taskId.value, buildAnsysPayload()).then(res => {
    applyAnsysSimulation(res.data)
    ElMessage.success('ANSYS 参数已保存')
  }).finally(() => {
    ansysSaving.value = false
  })
}

function prepareAnsysSimulation() {
  if (!taskId.value) return
  ansysOpening.value = true
  openAnsysSimulationTask(taskId.value, buildAnsysPayload()).then(res => {
    applyAnsysSimulation(res.data)
    ElMessage.success('正在打开 ANSYS')
  }).finally(() => {
    ansysOpening.value = false
  })
}

function readAnsysResult() {
  if (!taskId.value) return
  ansysImporting.value = true
  importAnsysSimulationResult(taskId.value, buildAnsysPayload({
    projectPath: ansysSimulation.value.resultFilePath,
    resultFilePath: ansysSimulation.value.resultFilePath,
    workDir: ansysSimulation.value.result?.workDir || ansysSimulation.value.input?.workDir
  })).then(res => {
    applyAnsysSimulation(res.data)
    ElMessage.success('正在读取 ANSYS 结果')
  }).finally(() => {
    ansysImporting.value = false
  })
}

function refreshAnsysSimulation(silent = false) {
  if (!taskId.value) return
  const silentMode = silent === true
  if (!silentMode) ansysRefreshing.value = true
  getAnsysSimulationTask(taskId.value, { simulationMode: selectedAnsysSimulationMode.value }).then(res => {
    applyAnsysSimulation(res.data)
  }).finally(() => {
    if (!silentMode) ansysRefreshing.value = false
  })
}

function ansysStatusType(status) {
  return {
    NOT_SUBMITTED: 'info',
    PARAM_CONFIRMED: 'primary',
    QUEUED: 'warning',
    RUNNING: 'primary',
    WAITING_ENGINEER_SOLVE: 'warning',
    RESULT_IMPORTED: 'success',
    SUCCESS: 'success',
    FAILED: 'danger'
  }[status] || 'info'
}

function submitCadModel() {
  if (!taskId.value) return
  cadSubmitting.value = true
  const useSurrogateBest = cadModel.value.status === 'SUCCESS' ||
    cadModel.value.resetRequired ||
    cadModel.value.params?.source === 'surrogate_best_solution'
  submitCadModelTask(taskId.value, {
    ...cadForm.value,
    useSurrogateBest
  }).then(res => {
    applyCadModel(res.data)
    ElMessage.success(useSurrogateBest ? '已按模型最优解重新提交 CAD 建模' : 'CAD 建模任务已提交')
  }).finally(() => {
    cadSubmitting.value = false
  })
}

function refreshCadModel() {
  if (!taskId.value) return
  cadRefreshing.value = true
  getCadModelTask(taskId.value).then(res => {
    applyCadModel(res.data)
  }).finally(() => {
    cadRefreshing.value = false
  })
}

function startCadPolling() {
  if (cadPollTimer) return
  cadPollTimer = window.setInterval(refreshCadModel, 3000)
}

function stopCadPolling() {
  if (!cadPollTimer) return
  window.clearInterval(cadPollTimer)
  cadPollTimer = null
}

function startAnsysPolling() {
  if (ansysPollTimer) return
  ansysPollTimer = window.setInterval(() => refreshAnsysSimulation(true), 5000)
}

function stopAnsysPolling() {
  if (!ansysPollTimer) return
  window.clearInterval(ansysPollTimer)
  ansysPollTimer = null
}

function loadCadStl() {
  if (!taskId.value) return
  getCadModelFile(taskId.value, 'stl').then(data => {
    cadStlData.value = data
  }).catch(() => {
    cadStlData.value = null
  })
}

function downloadCadFile(kind) {
  if (!taskId.value) return
  getCadModelFile(taskId.value, kind).then(data => {
    const file = cadModel.value.files?.[kind]
    const filename = file?.fileName || `pipe_model.${kind === 'sldprt' ? 'SLDPRT' : 'stl'}`
    saveAs(new Blob([data]), filename)
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

function valueOrDash(value, suffix = '') {
  if (value === null || value === undefined || value === '') return '-'
  const number = Number(value)
  return Number.isFinite(number) ? `${number.toFixed(3)}${suffix}` : `${value}${suffix}`
}

function refreshComparison() {
  loadData()
  ElMessage.success('指标对比已刷新')
}

function submitSimulationDecision() {
  if (!canSubmitSimulationDecision.value) {
    ElMessage.warning('请先选择仿真验证通过或不通过。')
    return
  }
  runSimulation(taskId.value, { simulationPassed: simulationDecision.value.passed }).then(res => {
    simulation.value = res.data || {}
    ElMessage.success(simulationDecision.value.passed ? '已提交仿真验证通过' : '已提交仿真验证不通过')
    loadData()
  })
}

function metricTrendLabel(trend) {
  if (trend === 'up') return '提升'
  if (trend === 'down') return '降低'
  if (trend === 'change') return '变化'
  return '基准'
}

function metricTrendType(trend) {
  if (trend === 'up') return 'success'
  if (trend === 'down') return 'primary'
  if (trend === 'change') return 'warning'
  return 'info'
}

function submitApproval() {
  approveTask(taskId.value, approval.value).then(() => {
    ElMessage.success(approval.value.approved ? '审批通过，任务完成' : '已退回模型解耦求解')
    loadData()
  })
}

onMounted(loadData)
onBeforeUnmount(() => {
  stopCadPolling()
  stopAnsysPolling()
  clearAnsysStressImage()
})

watch(() => route.query.taskId, value => {
  taskId.value = value ? Number(value) : null
  activeTab.value = ['handled', 'related'].includes(route.query.tab) ? route.query.tab : activeTab.value
  selectedAnsysSimulationMode.value = [ANSYS_MODE_DEMO, ANSYS_MODE_FSI].includes(route.query.ansysMode)
    ? route.query.ansysMode
    : selectedAnsysSimulationMode.value
  loadData()
})

watch(selectedAnsysSimulationMode, mode => {
  if (!taskId.value) return
  router.replace({
    path: '/designtask/simulation',
    query: { ...route.query, ansysMode: mode }
  })
  refreshAnsysSimulation(true)
})
</script>

<style scoped lang="scss">
@use "../platform-theme.scss";

.mb-16 {
  margin-bottom: 16px;
}

.wait-action {
  color: #98a2b3;
  font-size: 13px;
}

.topbar-return {
  height: 34px;
  padding: 0 14px;
  border-radius: 4px;
}

.simulation-summary-grid {
  align-items: stretch;

  > .section-block {
    min-width: 0;
  }
}

.conclusion-section {
  min-height: 220px;
}

.conclusion-body {
  display: flex;
  min-height: 150px;
  align-items: stretch;
  justify-content: flex-start;
}

.conclusion-form {
  width: 100%;
  max-width: none;
}

.conclusion-actions {
  display: flex;
  justify-content: flex-start;
}

.fixed-input-meta {
  display: flex;
  flex-wrap: wrap;
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

.ansys-result-grid {
  display: grid;
  grid-template-columns: minmax(360px, 0.9fr) minmax(520px, 1.1fr);
  gap: 14px;
  align-items: stretch;
  min-height: 420px;
}

.ansys-model-selector {
  display: flex;
  gap: 12px;
  align-items: center;
  margin: 0 0 16px;
  padding: 9px 12px;
  border: 1px solid #e6ebf1;
  border-radius: 6px;
  background: #fbfcfe;
}

.ansys-parameter-panel {
  margin-bottom: 16px;
  padding: 12px;
  border: 1px solid #dde5ee;
  border-radius: 6px;
  background: #ffffff;
}

.ansys-parameter-form {
  display: grid;
  gap: 12px;

  :deep(.el-form-item) {
    margin-bottom: 0;
  }

  :deep(.el-input-number),
  :deep(.el-select) {
    width: 100%;
  }
}

.ansys-param-group {
  display: grid;
  gap: 12px;
  padding: 12px;
  border: 1px solid #e6ebf1;
  border-radius: 6px;
  background: #fbfcfe;
}

.ansys-param-group__head {
  display: flex;
  min-height: 28px;
  align-items: center;
  justify-content: space-between;
  gap: 12px;

  strong {
    color: #233955;
    font-size: 15px;
  }
}

.ansys-param-grid {
  display: grid;
  gap: 12px;
}

.ansys-param-grid--four {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.ansys-result-switches {
  display: flex;
  min-height: 32px;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.ansys-image-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 420px;
  max-height: 560px;
  align-items: center;
  justify-content: center;
  overflow: auto;
  padding: 10px;
  border: 1px solid #e1e7ef;
  border-radius: 6px;
  background: #eef3fb;

  img {
    display: block;
    width: 100%;
    height: auto;
    max-height: 500px;
    object-fit: contain;
  }
}

.ansys-table-panel {
  height: 100%;
  min-height: 420px;
  max-height: 560px;
  overflow: auto;
}

.ansys-image-actions {
  display: flex;
  width: 100%;
  justify-content: flex-end;
  margin-bottom: 8px;
}

.ansys-preview-scroll {
  max-height: 78vh;
  overflow: auto;
  text-align: center;
  background: #eef3fb;

  img {
    display: block;
    max-width: none;
    margin: 0 auto;
  }
}

:global(.ansys-image-dialog .el-dialog__body) {
  padding: 10px;
}

.cad-model-grid {
  display: grid;
  grid-template-columns: 360px minmax(0, 1fr);
  gap: 14px;
  align-items: stretch;
  margin-top: 16px;
  min-height: 560px;
}

.cad-form-panel,
.cad-result-panel {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 560px;
}

.cad-form-scroll,
.cad-result-scroll {
  flex: 1;
  min-height: 0;
  max-height: 620px;
  overflow: auto;
  padding-right: 4px;
}

.cad-param-form {
  padding: 12px 12px 2px;
  border: 1px solid #e1e7ef;
  border-radius: 6px;
  background: #ffffff;

  :deep(.el-input-number) {
    width: 100%;
  }
}

.cad-constraint-list {
  display: grid;
  gap: 10px;
  margin-top: 12px;

  div {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 12px;
    border: 1px solid #e6ebf1;
    border-left: 3px solid #4f8edc;
    border-radius: 6px;
    color: #65788d;
    background: #fbfcfe;
  }

  strong {
    color: #233955;
  }
}

.cad-result-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 12px;

  div {
    min-height: 72px;
    padding: 12px;
    border: 1px solid #e6ebf1;
    border-left: 3px solid #4f8edc;
    border-radius: 6px;
    background: #fbfcfe;
  }

  span {
    display: block;
    color: #718399;
    font-size: 13px;
  }

  strong {
    display: block;
    margin-top: 10px;
    color: #233955;
    font-size: 18px;
  }
}

.cad-file-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin: 12px 0;
}

.cad-result-panel :deep(.cad-viewer) {
  min-height: 0;
  height: 420px;
}

@media (max-width: 1200px) {
  .cad-model-grid {
    grid-template-columns: 1fr;
    min-height: auto;
  }

  .cad-result-grid {
    grid-template-columns: 1fr;
  }

  .cad-form-panel,
  .cad-result-panel {
    min-height: auto;
  }

  .cad-form-scroll,
  .cad-result-scroll {
    max-height: none;
    overflow: visible;
    padding-right: 0;
  }

  .ansys-result-grid {
    grid-template-columns: 1fr;
    min-height: auto;
  }

  .ansys-param-grid--four {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .ansys-table-panel,
  .ansys-image-panel {
    min-height: 320px;
    max-height: none;
  }
}

@media (max-width: 768px) {
  .conclusion-body {
    align-items: stretch;
    justify-content: flex-start;
  }

  .ansys-param-grid--four {
    grid-template-columns: 1fr;
  }

  .ansys-param-group__head {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
