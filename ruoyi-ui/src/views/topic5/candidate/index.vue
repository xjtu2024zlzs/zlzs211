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
      <el-form-item label="算法运行ID" prop="runId">
        <el-input
          v-model="queryParams.runId"
          placeholder="请输入算法运行ID"
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
      <el-form-item label="零件编号" prop="partNo">
        <el-input
          v-model="queryParams.partNo"
          placeholder="请输入零件编号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="零件名称" prop="partName">
        <el-input
          v-model="queryParams.partName"
          placeholder="请输入零件名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="零件序列号" prop="serialNo">
        <el-input
          v-model="queryParams.serialNo"
          placeholder="请输入零件序列号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="批次号" prop="batchNo">
        <el-input
          v-model="queryParams.batchNo"
          placeholder="请输入批次号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="定位方式：rule/kg/transe/gnn/hybrid" prop="locateMethod">
        <el-input
          v-model="queryParams.locateMethod"
          placeholder="请输入定位方式：rule/kg/transe/gnn/hybrid"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="匹配分数" prop="matchScore">
        <el-input
          v-model="queryParams.matchScore"
          placeholder="请输入匹配分数"
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
      <el-form-item label="是否选定为目标零件：0否，1是" prop="isSelected">
        <el-input
          v-model="queryParams.isSelected"
          placeholder="请输入是否选定为目标零件：0否，1是"
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
          v-hasPermi="['topic5:candidate:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['topic5:candidate:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['topic5:candidate:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['topic5:candidate:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="candidateList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="候选结果ID" align="center" prop="candidateId" />
      <el-table-column label="追溯任务ID" align="center" prop="taskId" />
      <el-table-column label="算法运行ID" align="center" prop="runId" />
      <el-table-column label="零部件实例ID" align="center" prop="partId" />
      <el-table-column label="零件编号" align="center" prop="partNo" />
      <el-table-column label="零件名称" align="center" prop="partName" />
      <el-table-column label="零件序列号" align="center" prop="serialNo" />
      <el-table-column label="批次号" align="center" prop="batchNo" />
      <el-table-column label="定位方式：rule/kg/transe/gnn/hybrid" align="center" prop="locateMethod" />
      <el-table-column label="匹配分数" align="center" prop="matchScore" />
      <el-table-column label="置信度" align="center" prop="confidence" />
      <el-table-column label="排序号" align="center" prop="rankNo" />
      <el-table-column label="是否选定为目标零件：0否，1是" align="center" prop="isSelected" />
      <el-table-column label="定位原因" align="center" prop="locateReason" />
      <el-table-column label="关联路径/知识图谱路径" align="center" prop="relationPath" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['topic5:candidate:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['topic5:candidate:remove']">删除</el-button>
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

    <!-- 添加或修改课题五-候选零部件定位结果对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="candidateRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="追溯任务ID" prop="taskId">
              <el-input v-model="form.taskId" placeholder="请输入追溯任务ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="算法运行ID" prop="runId">
              <el-input v-model="form.runId" placeholder="请输入算法运行ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="零部件实例ID" prop="partId">
              <el-input v-model="form.partId" placeholder="请输入零部件实例ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="零件编号" prop="partNo">
              <el-input v-model="form.partNo" placeholder="请输入零件编号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="零件名称" prop="partName">
              <el-input v-model="form.partName" placeholder="请输入零件名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="零件序列号" prop="serialNo">
              <el-input v-model="form.serialNo" placeholder="请输入零件序列号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="批次号" prop="batchNo">
              <el-input v-model="form.batchNo" placeholder="请输入批次号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="定位方式：rule/kg/transe/gnn/hybrid" prop="locateMethod">
              <el-input v-model="form.locateMethod" placeholder="请输入定位方式：rule/kg/transe/gnn/hybrid" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="匹配分数" prop="matchScore">
              <el-input v-model="form.matchScore" placeholder="请输入匹配分数" />
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
            <el-form-item label="是否选定为目标零件：0否，1是" prop="isSelected">
              <el-input v-model="form.isSelected" placeholder="请输入是否选定为目标零件：0否，1是" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="定位原因" prop="locateReason">
              <el-input v-model="form.locateReason" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="关联路径/知识图谱路径" prop="relationPath">
              <el-input v-model="form.relationPath" type="textarea" placeholder="请输入内容" />
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

<script setup name="Candidate">
import { listCandidate, getCandidate, delCandidate, addCandidate, updateCandidate } from "@/api/topic5/candidate"

const { proxy } = getCurrentInstance()

const candidateList = ref([])
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
    runId: undefined,
    partId: undefined,
    partNo: undefined,
    partName: undefined,
    serialNo: undefined,
    batchNo: undefined,
    locateMethod: undefined,
    matchScore: undefined,
    confidence: undefined,
    rankNo: undefined,
    isSelected: undefined,
    locateReason: undefined,
    relationPath: undefined,
  },
  rules: {
    taskId: [
      { required: true, message: "追溯任务ID不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询课题五-候选零部件定位结果列表 */
function getList() {
  loading.value = true
  listCandidate(queryParams.value).then(response => {
    candidateList.value = response.rows
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
    candidateId: null,
    taskId: null,
    runId: null,
    partId: null,
    partNo: null,
    partName: null,
    serialNo: null,
    batchNo: null,
    locateMethod: null,
    matchScore: null,
    confidence: null,
    rankNo: null,
    isSelected: null,
    locateReason: null,
    relationPath: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  }
  proxy.resetForm("candidateRef")
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
  ids.value = selection.map(item => item.candidateId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加课题五-候选零部件定位结果"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _candidateId = row.candidateId || ids.value
  getCandidate(_candidateId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改课题五-候选零部件定位结果"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["candidateRef"].validate(valid => {
    if (valid) {
      if (form.value.candidateId != null) {
        updateCandidate(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addCandidate(form.value).then(() => {
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
  const _candidateIds = row.candidateId || ids.value
  proxy.$modal.confirm('是否确认删除课题五-候选零部件定位结果编号为"' + _candidateIds + '"的数据项？').then(function() {
    return delCandidate(_candidateIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('topic5/candidate/export', {
    ...queryParams.value
  }, `candidate_${new Date().getTime()}.xlsx`)
}

getList()
</script>
