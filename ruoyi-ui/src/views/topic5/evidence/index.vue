<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="诊断结果ID" prop="diagnosisId">
        <el-input
          v-model="queryParams.diagnosisId"
          placeholder="请输入诊断结果ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
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
      <el-form-item label="生命周期事件ID" prop="eventId">
        <el-input
          v-model="queryParams.eventId"
          placeholder="请输入生命周期事件ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="证据名称" prop="evidenceName">
        <el-input
          v-model="queryParams.evidenceName"
          placeholder="请输入证据名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="证据贡献度" prop="contribution">
        <el-input
          v-model="queryParams.contribution"
          placeholder="请输入证据贡献度"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="证据置信度" prop="confidence">
        <el-input
          v-model="queryParams.confidence"
          placeholder="请输入证据置信度"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="来源表" prop="sourceTable">
        <el-input
          v-model="queryParams.sourceTable"
          placeholder="请输入来源表"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="来源记录ID" prop="sourceRecordId">
        <el-input
          v-model="queryParams.sourceRecordId"
          placeholder="请输入来源记录ID"
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
          v-hasPermi="['topic5:evidence:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['topic5:evidence:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['topic5:evidence:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['topic5:evidence:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="evidenceList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="诊断证据ID" align="center" prop="evidenceId" />
      <el-table-column label="诊断结果ID" align="center" prop="diagnosisId" />
      <el-table-column label="追溯任务ID" align="center" prop="taskId" />
      <el-table-column label="零部件实例ID" align="center" prop="partId" />
      <el-table-column label="生命周期事件ID" align="center" prop="eventId" />
      <el-table-column label="证据类型：lifecycle/kg/rule/model/history" align="center" prop="evidenceType" />
      <el-table-column label="证据名称" align="center" prop="evidenceName" />
      <el-table-column label="证据描述" align="center" prop="evidenceDesc" />
      <el-table-column label="证据取值" align="center" prop="evidenceValue" />
      <el-table-column label="证据贡献度" align="center" prop="contribution" />
      <el-table-column label="证据置信度" align="center" prop="confidence" />
      <el-table-column label="知识图谱路径" align="center" prop="kgPath" />
      <el-table-column label="来源表" align="center" prop="sourceTable" />
      <el-table-column label="来源记录ID" align="center" prop="sourceRecordId" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['topic5:evidence:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['topic5:evidence:remove']">删除</el-button>
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

    <!-- 添加或修改课题五-诊断证据对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="evidenceRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="诊断结果ID" prop="diagnosisId">
              <el-input v-model="form.diagnosisId" placeholder="请输入诊断结果ID" />
            </el-form-item>
          </el-col>
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
            <el-form-item label="生命周期事件ID" prop="eventId">
              <el-input v-model="form.eventId" placeholder="请输入生命周期事件ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="证据名称" prop="evidenceName">
              <el-input v-model="form.evidenceName" placeholder="请输入证据名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="证据描述" prop="evidenceDesc">
              <el-input v-model="form.evidenceDesc" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="证据取值" prop="evidenceValue">
              <el-input v-model="form.evidenceValue" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="证据贡献度" prop="contribution">
              <el-input v-model="form.contribution" placeholder="请输入证据贡献度" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="证据置信度" prop="confidence">
              <el-input v-model="form.confidence" placeholder="请输入证据置信度" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="知识图谱路径" prop="kgPath">
              <el-input v-model="form.kgPath" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="来源表" prop="sourceTable">
              <el-input v-model="form.sourceTable" placeholder="请输入来源表" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="来源记录ID" prop="sourceRecordId">
              <el-input v-model="form.sourceRecordId" placeholder="请输入来源记录ID" />
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

<script setup name="Evidence">
import { listEvidence, getEvidence, delEvidence, addEvidence, updateEvidence } from "@/api/topic5/evidence"

const { proxy } = getCurrentInstance()

const evidenceList = ref([])
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
    diagnosisId: undefined,
    taskId: undefined,
    partId: undefined,
    eventId: undefined,
    evidenceType: undefined,
    evidenceName: undefined,
    evidenceDesc: undefined,
    evidenceValue: undefined,
    contribution: undefined,
    confidence: undefined,
    kgPath: undefined,
    sourceTable: undefined,
    sourceRecordId: undefined,
  },
  rules: {
    diagnosisId: [
      { required: true, message: "诊断结果ID不能为空", trigger: "blur" }
    ],
    taskId: [
      { required: true, message: "追溯任务ID不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询课题五-诊断证据列表 */
function getList() {
  loading.value = true
  listEvidence(queryParams.value).then(response => {
    evidenceList.value = response.rows
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
    evidenceId: null,
    diagnosisId: null,
    taskId: null,
    partId: null,
    eventId: null,
    evidenceType: null,
    evidenceName: null,
    evidenceDesc: null,
    evidenceValue: null,
    contribution: null,
    confidence: null,
    kgPath: null,
    sourceTable: null,
    sourceRecordId: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  }
  proxy.resetForm("evidenceRef")
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
  ids.value = selection.map(item => item.evidenceId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加课题五-诊断证据"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _evidenceId = row.evidenceId || ids.value
  getEvidence(_evidenceId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改课题五-诊断证据"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["evidenceRef"].validate(valid => {
    if (valid) {
      if (form.value.evidenceId != null) {
        updateEvidence(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addEvidence(form.value).then(() => {
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
  const _evidenceIds = row.evidenceId || ids.value
  proxy.$modal.confirm('是否确认删除课题五-诊断证据编号为"' + _evidenceIds + '"的数据项？').then(function() {
    return delEvidence(_evidenceIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('topic5/evidence/export', {
    ...queryParams.value
  }, `evidence_${new Date().getTime()}.xlsx`)
}

getList()
</script>
