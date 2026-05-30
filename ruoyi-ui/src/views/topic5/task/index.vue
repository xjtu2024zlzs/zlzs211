<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="追溯任务编号" prop="taskNo">
        <el-input
          v-model="queryParams.taskNo"
          placeholder="请输入追溯任务编号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="追溯任务名称" prop="taskName">
        <el-input
          v-model="queryParams.taskName"
          placeholder="请输入追溯任务名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="关联故障表征ID" prop="symptomId">
        <el-input
          v-model="queryParams.symptomId"
          placeholder="请输入关联故障表征ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="优先级：1低，2中，3高" prop="priorityLevel">
        <el-input
          v-model="queryParams.priorityLevel"
          placeholder="请输入优先级：1低，2中，3高"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="最终定位零部件ID" prop="finalPartId">
        <el-input
          v-model="queryParams.finalPartId"
          placeholder="请输入最终定位零部件ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="最终定位零件编号" prop="finalPartNo">
        <el-input
          v-model="queryParams.finalPartNo"
          placeholder="请输入最终定位零件编号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="最终定位零件名称" prop="finalPartName">
        <el-input
          v-model="queryParams.finalPartName"
          placeholder="请输入最终定位零件名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="最终故障阶段编码" prop="finalStageCode">
        <el-input
          v-model="queryParams.finalStageCode"
          placeholder="请输入最终故障阶段编码"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="最终故障阶段名称" prop="finalStageName">
        <el-input
          v-model="queryParams.finalStageName"
          placeholder="请输入最终故障阶段名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="最终置信度" prop="finalConfidence">
        <el-input
          v-model="queryParams.finalConfidence"
          placeholder="请输入最终置信度"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="任务开始时间" prop="startTime">
        <el-date-picker clearable
          v-model="queryParams.startTime"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择任务开始时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="任务结束时间" prop="endTime">
        <el-date-picker clearable
          v-model="queryParams.endTime"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择任务结束时间">
        </el-date-picker>
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
          v-hasPermi="['topic5:task:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['topic5:task:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['topic5:task:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['topic5:task:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="taskList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="追溯任务ID" align="center" prop="taskId" />
      <el-table-column label="追溯任务编号" align="center" prop="taskNo" />
      <el-table-column label="追溯任务名称" align="center" prop="taskName" />
      <el-table-column label="关联故障表征ID" align="center" prop="symptomId" />
      <el-table-column label="任务状态：0待分析，1分析中，2已完成，3失败" align="center" prop="taskStatus" />
      <el-table-column label="优先级：1低，2中，3高" align="center" prop="priorityLevel" />
      <el-table-column label="追溯目标" align="center" prop="traceGoal" />
      <el-table-column label="输入信息摘要" align="center" prop="inputSummary" />
      <el-table-column label="最终定位零部件ID" align="center" prop="finalPartId" />
      <el-table-column label="最终定位零件编号" align="center" prop="finalPartNo" />
      <el-table-column label="最终定位零件名称" align="center" prop="finalPartName" />
      <el-table-column label="最终故障阶段编码" align="center" prop="finalStageCode" />
      <el-table-column label="最终故障阶段名称" align="center" prop="finalStageName" />
      <el-table-column label="最终置信度" align="center" prop="finalConfidence" />
      <el-table-column label="追溯结果摘要" align="center" prop="resultSummary" />
      <el-table-column label="任务开始时间" align="center" prop="startTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.startTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="任务结束时间" align="center" prop="endTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.endTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['topic5:task:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['topic5:task:remove']">删除</el-button>
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

    <!-- 添加或修改课题五-追溯任务主对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="taskRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="追溯任务编号" prop="taskNo">
              <el-input v-model="form.taskNo" placeholder="请输入追溯任务编号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="追溯任务名称" prop="taskName">
              <el-input v-model="form.taskName" placeholder="请输入追溯任务名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="关联故障表征ID" prop="symptomId">
              <el-input v-model="form.symptomId" placeholder="请输入关联故障表征ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="优先级：1低，2中，3高" prop="priorityLevel">
              <el-input v-model="form.priorityLevel" placeholder="请输入优先级：1低，2中，3高" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="追溯目标" prop="traceGoal">
              <el-input v-model="form.traceGoal" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="输入信息摘要" prop="inputSummary">
              <el-input v-model="form.inputSummary" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="最终定位零部件ID" prop="finalPartId">
              <el-input v-model="form.finalPartId" placeholder="请输入最终定位零部件ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="最终定位零件编号" prop="finalPartNo">
              <el-input v-model="form.finalPartNo" placeholder="请输入最终定位零件编号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="最终定位零件名称" prop="finalPartName">
              <el-input v-model="form.finalPartName" placeholder="请输入最终定位零件名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="最终故障阶段编码" prop="finalStageCode">
              <el-input v-model="form.finalStageCode" placeholder="请输入最终故障阶段编码" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="最终故障阶段名称" prop="finalStageName">
              <el-input v-model="form.finalStageName" placeholder="请输入最终故障阶段名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="最终置信度" prop="finalConfidence">
              <el-input v-model="form.finalConfidence" placeholder="请输入最终置信度" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="追溯结果摘要" prop="resultSummary">
              <el-input v-model="form.resultSummary" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="任务开始时间" prop="startTime">
              <el-date-picker clearable
                v-model="form.startTime"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择任务开始时间">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="任务结束时间" prop="endTime">
              <el-date-picker clearable
                v-model="form.endTime"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择任务结束时间">
              </el-date-picker>
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

<script setup name="Task">
import { listTask, getTask, delTask, addTask, updateTask } from "@/api/topic5/task"

const { proxy } = getCurrentInstance()

const taskList = ref([])
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
    taskNo: undefined,
    taskName: undefined,
    symptomId: undefined,
    taskStatus: undefined,
    priorityLevel: undefined,
    traceGoal: undefined,
    inputSummary: undefined,
    finalPartId: undefined,
    finalPartNo: undefined,
    finalPartName: undefined,
    finalStageCode: undefined,
    finalStageName: undefined,
    finalConfidence: undefined,
    resultSummary: undefined,
    startTime: undefined,
    endTime: undefined,
  },
  rules: {
    taskNo: [
      { required: true, message: "追溯任务编号不能为空", trigger: "blur" }
    ],
    taskName: [
      { required: true, message: "追溯任务名称不能为空", trigger: "blur" }
    ],
    symptomId: [
      { required: true, message: "关联故障表征ID不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询课题五-追溯任务主列表 */
function getList() {
  loading.value = true
  listTask(queryParams.value).then(response => {
    taskList.value = response.rows
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
    taskId: null,
    taskNo: null,
    taskName: null,
    symptomId: null,
    taskStatus: null,
    priorityLevel: null,
    traceGoal: null,
    inputSummary: null,
    finalPartId: null,
    finalPartNo: null,
    finalPartName: null,
    finalStageCode: null,
    finalStageName: null,
    finalConfidence: null,
    resultSummary: null,
    startTime: null,
    endTime: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  }
  proxy.resetForm("taskRef")
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
  ids.value = selection.map(item => item.taskId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加课题五-追溯任务主"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _taskId = row.taskId || ids.value
  getTask(_taskId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改课题五-追溯任务主"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["taskRef"].validate(valid => {
    if (valid) {
      if (form.value.taskId != null) {
        updateTask(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addTask(form.value).then(() => {
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
  const _taskIds = row.taskId || ids.value
  proxy.$modal.confirm('是否确认删除课题五-追溯任务主编号为"' + _taskIds + '"的数据项？').then(function() {
    return delTask(_taskIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('topic5/task/export', {
    ...queryParams.value
  }, `task_${new Date().getTime()}.xlsx`)
}

getList()
</script>
