<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="追溯任务ID" prop="taskId">
        <el-input
          v-model="queryParams.taskId"
          placeholder="请输入追溯任务ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="零部件实例ID" prop="partId">
        <el-input
          v-model="queryParams.partId"
          placeholder="请输入零部件实例ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="算法运行ID" prop="runId">
        <el-input
          v-model="queryParams.runId"
          placeholder="请输入算法运行ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="故障阶段编码" prop="stageCode">
        <el-input
          v-model="queryParams.stageCode"
          placeholder="请输入故障阶段编码"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="故障阶段名称" prop="stageName">
        <el-input
          v-model="queryParams.stageName"
          placeholder="请输入故障阶段名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="风险分数" prop="riskScore">
        <el-input
          v-model="queryParams.riskScore"
          placeholder="请输入风险分数"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="置信度" prop="confidence">
        <el-input
          v-model="queryParams.confidence"
          placeholder="请输入置信度"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="排序号" prop="rankNo">
        <el-input
          v-model="queryParams.rankNo"
          placeholder="请输入排序号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="是否最终结论：0否，1是" prop="isFinal">
        <el-input
          v-model="queryParams.isFinal"
          placeholder="请输入是否最终结论：0否，1是"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="故障模式" prop="faultMode">
        <el-input
          v-model="queryParams.faultMode"
          placeholder="请输入故障模式"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="算法名称" prop="algorithmName">
        <el-input
          v-model="queryParams.algorithmName"
          placeholder="请输入算法名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="模型版本" prop="modelVersion">
        <el-input
          v-model="queryParams.modelVersion"
          placeholder="请输入模型版本"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['topic5:diagnosis:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['topic5:diagnosis:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['topic5:diagnosis:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['topic5:diagnosis:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="diagnosisList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="诊断结果ID" align="center" prop="diagnosisId" />
      <el-table-column label="追溯任务ID" align="center" prop="taskId" />
      <el-table-column label="零部件实例ID" align="center" prop="partId" />
      <el-table-column label="算法运行ID" align="center" prop="runId" />
      <el-table-column label="故障阶段编码" align="center" prop="stageCode" />
      <el-table-column label="故障阶段名称" align="center" prop="stageName" />
      <el-table-column label="风险分数" align="center" prop="riskScore" />
      <el-table-column label="置信度" align="center" prop="confidence" />
      <el-table-column label="排序号" align="center" prop="rankNo" />
      <el-table-column label="是否最终结论：0否，1是" align="center" prop="isFinal" />
      <el-table-column label="故障模式" align="center" prop="faultMode" />
      <el-table-column label="原因描述" align="center" prop="causeDesc" />
      <el-table-column label="诊断结论" align="center" prop="diagnosisResult" />
      <el-table-column label="算法名称" align="center" prop="algorithmName" />
      <el-table-column label="模型版本" align="center" prop="modelVersion" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['topic5:diagnosis:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['topic5:diagnosis:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改课题五-故障阶段诊断结果对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="diagnosisRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="追溯任务ID" prop="taskId">
              <el-input v-model="form.taskId" placeholder="请输入追溯任务ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="零部件实例ID" prop="partId">
              <el-input v-model="form.partId" placeholder="请输入零部件实例ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="算法运行ID" prop="runId">
              <el-input v-model="form.runId" placeholder="请输入算法运行ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="故障阶段编码" prop="stageCode">
              <el-input v-model="form.stageCode" placeholder="请输入故障阶段编码" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="故障阶段名称" prop="stageName">
              <el-input v-model="form.stageName" placeholder="请输入故障阶段名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="风险分数" prop="riskScore">
              <el-input v-model="form.riskScore" placeholder="请输入风险分数" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="置信度" prop="confidence">
              <el-input v-model="form.confidence" placeholder="请输入置信度" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="排序号" prop="rankNo">
              <el-input v-model="form.rankNo" placeholder="请输入排序号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="是否最终结论：0否，1是" prop="isFinal">
              <el-input v-model="form.isFinal" placeholder="请输入是否最终结论：0否，1是" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="故障模式" prop="faultMode">
              <el-input v-model="form.faultMode" placeholder="请输入故障模式" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="原因描述" prop="causeDesc">
              <el-input v-model="form.causeDesc" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="诊断结论" prop="diagnosisResult">
              <el-input v-model="form.diagnosisResult" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="算法名称" prop="algorithmName">
              <el-input v-model="form.algorithmName" placeholder="请输入算法名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="模型版本" prop="modelVersion">
              <el-input v-model="form.modelVersion" placeholder="请输入模型版本" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
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

<script setup name="Diagnosis">
import { listDiagnosis, getDiagnosis, delDiagnosis, addDiagnosis, updateDiagnosis } from "@/api/topic5/diagnosis"

const { proxy } = getCurrentInstance()

const diagnosisList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    taskId: undefined,
    partId: undefined,
    runId: undefined,
    stageCode: undefined,
    stageName: undefined,
    riskScore: undefined,
    confidence: undefined,
    rankNo: undefined,
    isFinal: undefined,
    faultMode: undefined,
    causeDesc: undefined,
    diagnosisResult: undefined,
    algorithmName: undefined,
    modelVersion: undefined,
  },
  rules: {
    taskId: [
      { required: true, message: "追溯任务ID不能为空", trigger: "blur" }
    ],
    stageCode: [
      { required: true, message: "故障阶段编码不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询课题五-故障阶段诊断结果列表 */
function getList() {
  loading.value = true
  listDiagnosis(queryParams.value).then(response => {
    diagnosisList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    diagnosisId: null,
    taskId: null,
    partId: null,
    runId: null,
    stageCode: null,
    stageName: null,
    riskScore: null,
    confidence: null,
    rankNo: null,
    isFinal: null,
    faultMode: null,
    causeDesc: null,
    diagnosisResult: null,
    algorithmName: null,
    modelVersion: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  }
  proxy.resetForm("diagnosisRef")
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.diagnosisId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加课题五-故障阶段诊断结果"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _diagnosisId = row.diagnosisId || ids.value
  getDiagnosis(_diagnosisId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改课题五-故障阶段诊断结果"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["diagnosisRef"].validate(valid => {
    if (valid) {
      if (form.value.diagnosisId != null) {
        updateDiagnosis(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addDiagnosis(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _diagnosisIds = row.diagnosisId || ids.value
  proxy.$modal.confirm('是否确认删除课题五-故障阶段诊断结果编号为"' + _diagnosisIds + '"的数据项？').then(function() {
    return delDiagnosis(_diagnosisIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('topic5/diagnosis/export', {
    ...queryParams.value
  }, `diagnosis_${new Date().getTime()}.xlsx`)
}

getList()
</script>
