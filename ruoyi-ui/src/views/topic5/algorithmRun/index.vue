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
      <el-form-item label="开始时间" prop="startTime">
        <el-date-picker clearable
          v-model="queryParams.startTime"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择开始时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="结束时间" prop="endTime">
        <el-date-picker clearable
          v-model="queryParams.endTime"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择结束时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="耗时ms" prop="costTimeMs">
        <el-input
          v-model="queryParams.costTimeMs"
          placeholder="请输入耗时ms"
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
          v-hasPermi="['topic5:algorithmRun:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['topic5:algorithmRun:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['topic5:algorithmRun:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['topic5:algorithmRun:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="algorithmRunList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="算法运行ID" align="center" prop="runId" />
      <el-table-column label="追溯任务ID" align="center" prop="taskId" />
      <el-table-column label="算法类型：rule/kg/transe/gnn/hybrid" align="center" prop="algorithmType" />
      <el-table-column label="算法名称" align="center" prop="algorithmName" />
      <el-table-column label="模型版本" align="center" prop="modelVersion" />
      <el-table-column label="算法输入数据JSON" align="center" prop="inputData" />
      <el-table-column label="算法输出数据JSON" align="center" prop="outputData" />
      <el-table-column label="运行状态：0待运行，1运行中，2成功，3失败" align="center" prop="runStatus" />
      <el-table-column label="错误信息" align="center" prop="errorMessage" />
      <el-table-column label="开始时间" align="center" prop="startTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.startTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="结束时间" align="center" prop="endTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.endTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="耗时ms" align="center" prop="costTimeMs" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['topic5:algorithmRun:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['topic5:algorithmRun:remove']">删除</el-button>
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

    <!-- 添加或修改课题五-算法运行记录对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="algorithmRunRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="追溯任务ID" prop="taskId">
              <el-input v-model="form.taskId" placeholder="请输入追溯任务ID" />
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
            <el-form-item label="算法输入数据JSON" prop="inputData">
              <el-input v-model="form.inputData" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="算法输出数据JSON" prop="outputData">
              <el-input v-model="form.outputData" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="错误信息" prop="errorMessage">
              <el-input v-model="form.errorMessage" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker clearable
                v-model="form.startTime"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择开始时间">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker clearable
                v-model="form.endTime"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择结束时间">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="耗时ms" prop="costTimeMs">
              <el-input v-model="form.costTimeMs" placeholder="请输入耗时ms" />
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

<script setup name="AlgorithmRun">
import { listAlgorithmRun, getAlgorithmRun, delAlgorithmRun, addAlgorithmRun, updateAlgorithmRun } from "@/api/topic5/algorithmRun"

const { proxy } = getCurrentInstance()

const algorithmRunList = ref([])
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
    algorithmType: undefined,
    algorithmName: undefined,
    modelVersion: undefined,
    inputData: undefined,
    outputData: undefined,
    runStatus: undefined,
    errorMessage: undefined,
    startTime: undefined,
    endTime: undefined,
    costTimeMs: undefined,
  },
  rules: {
    taskId: [
      { required: true, message: "追溯任务ID不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询课题五-算法运行记录列表 */
function getList() {
  loading.value = true
  listAlgorithmRun(queryParams.value).then(response => {
    algorithmRunList.value = response.rows
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
    runId: null,
    taskId: null,
    algorithmType: null,
    algorithmName: null,
    modelVersion: null,
    inputData: null,
    outputData: null,
    runStatus: null,
    errorMessage: null,
    startTime: null,
    endTime: null,
    costTimeMs: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  }
  proxy.resetForm("algorithmRunRef")
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
  ids.value = selection.map(item => item.runId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加课题五-算法运行记录"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _runId = row.runId || ids.value
  getAlgorithmRun(_runId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改课题五-算法运行记录"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["algorithmRunRef"].validate(valid => {
    if (valid) {
      if (form.value.runId != null) {
        updateAlgorithmRun(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addAlgorithmRun(form.value).then(() => {
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
  const _runIds = row.runId || ids.value
  proxy.$modal.confirm('是否确认删除课题五-算法运行记录编号为"' + _runIds + '"的数据项？').then(function() {
    return delAlgorithmRun(_runIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('topic5/algorithmRun/export', {
    ...queryParams.value
  }, `algorithmRun_${new Date().getTime()}.xlsx`)
}

getList()
</script>
