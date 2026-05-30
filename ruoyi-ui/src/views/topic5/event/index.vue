<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="零部件实例ID" prop="partId">
        <el-input
          v-model="queryParams.partId"
          placeholder="请输入零部件实例ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="生命周期阶段编码" prop="stageCode">
        <el-input
          v-model="queryParams.stageCode"
          placeholder="请输入生命周期阶段编码"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="生命周期阶段名称" prop="stageName">
        <el-input
          v-model="queryParams.stageName"
          placeholder="请输入生命周期阶段名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="事件名称" prop="eventName">
        <el-input
          v-model="queryParams.eventName"
          placeholder="请输入事件名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="事件发生时间" prop="eventTime">
        <el-date-picker clearable
          v-model="queryParams.eventTime"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择事件发生时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="工序编号" prop="processNo">
        <el-input
          v-model="queryParams.processNo"
          placeholder="请输入工序编号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="工序名称" prop="processName">
        <el-input
          v-model="queryParams.processName"
          placeholder="请输入工序名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="设备编号" prop="equipmentNo">
        <el-input
          v-model="queryParams.equipmentNo"
          placeholder="请输入设备编号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="操作人员" prop="operatorName">
        <el-input
          v-model="queryParams.operatorName"
          placeholder="请输入操作人员"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="材料批次号" prop="materialBatchNo">
        <el-input
          v-model="queryParams.materialBatchNo"
          placeholder="请输入材料批次号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="检测结果" prop="inspectionResult">
        <el-input
          v-model="queryParams.inspectionResult"
          placeholder="请输入检测结果"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="是否异常：0否，1是" prop="abnormalFlag">
        <el-input
          v-model="queryParams.abnormalFlag"
          placeholder="请输入是否异常：0否，1是"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="质量评分" prop="qualityScore">
        <el-input
          v-model="queryParams.qualityScore"
          placeholder="请输入质量评分"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="来源系统" prop="sourceSystem">
        <el-input
          v-model="queryParams.sourceSystem"
          placeholder="请输入来源系统"
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
          v-hasPermi="['topic5:event:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['topic5:event:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['topic5:event:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['topic5:event:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="eventList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="生命周期事件ID" align="center" prop="eventId" />
      <el-table-column label="零部件实例ID" align="center" prop="partId" />
      <el-table-column label="生命周期阶段编码" align="center" prop="stageCode" />
      <el-table-column label="生命周期阶段名称" align="center" prop="stageName" />
      <el-table-column label="事件类型" align="center" prop="eventType" />
      <el-table-column label="事件名称" align="center" prop="eventName" />
      <el-table-column label="事件描述" align="center" prop="eventDesc" />
      <el-table-column label="事件发生时间" align="center" prop="eventTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.eventTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="工序编号" align="center" prop="processNo" />
      <el-table-column label="工序名称" align="center" prop="processName" />
      <el-table-column label="设备编号" align="center" prop="equipmentNo" />
      <el-table-column label="操作人员" align="center" prop="operatorName" />
      <el-table-column label="材料批次号" align="center" prop="materialBatchNo" />
      <el-table-column label="检测结果" align="center" prop="inspectionResult" />
      <el-table-column label="事件参数JSON" align="center" prop="parameterJson" />
      <el-table-column label="是否异常：0否，1是" align="center" prop="abnormalFlag" />
      <el-table-column label="质量评分" align="center" prop="qualityScore" />
      <el-table-column label="来源系统" align="center" prop="sourceSystem" />
      <el-table-column label="来源表" align="center" prop="sourceTable" />
      <el-table-column label="来源记录ID" align="center" prop="sourceRecordId" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['topic5:event:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['topic5:event:remove']">删除</el-button>
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

    <!-- 添加或修改课题五-零部件生命周期事件对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="eventRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="零部件实例ID" prop="partId">
              <el-input v-model="form.partId" placeholder="请输入零部件实例ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="生命周期阶段编码" prop="stageCode">
              <el-input v-model="form.stageCode" placeholder="请输入生命周期阶段编码" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="生命周期阶段名称" prop="stageName">
              <el-input v-model="form.stageName" placeholder="请输入生命周期阶段名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="事件名称" prop="eventName">
              <el-input v-model="form.eventName" placeholder="请输入事件名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="事件描述" prop="eventDesc">
              <el-input v-model="form.eventDesc" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="事件发生时间" prop="eventTime">
              <el-date-picker clearable
                v-model="form.eventTime"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择事件发生时间">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="工序编号" prop="processNo">
              <el-input v-model="form.processNo" placeholder="请输入工序编号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="工序名称" prop="processName">
              <el-input v-model="form.processName" placeholder="请输入工序名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="设备编号" prop="equipmentNo">
              <el-input v-model="form.equipmentNo" placeholder="请输入设备编号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="操作人员" prop="operatorName">
              <el-input v-model="form.operatorName" placeholder="请输入操作人员" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="材料批次号" prop="materialBatchNo">
              <el-input v-model="form.materialBatchNo" placeholder="请输入材料批次号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="检测结果" prop="inspectionResult">
              <el-input v-model="form.inspectionResult" placeholder="请输入检测结果" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="事件参数JSON" prop="parameterJson">
              <el-input v-model="form.parameterJson" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="是否异常：0否，1是" prop="abnormalFlag">
              <el-input v-model="form.abnormalFlag" placeholder="请输入是否异常：0否，1是" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="质量评分" prop="qualityScore">
              <el-input v-model="form.qualityScore" placeholder="请输入质量评分" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="来源系统" prop="sourceSystem">
              <el-input v-model="form.sourceSystem" placeholder="请输入来源系统" />
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

<script setup name="Event">
import { listEvent, getEvent, delEvent, addEvent, updateEvent } from "@/api/topic5/event"

const { proxy } = getCurrentInstance()

const eventList = ref([])
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
    partId: undefined,
    stageCode: undefined,
    stageName: undefined,
    eventType: undefined,
    eventName: undefined,
    eventDesc: undefined,
    eventTime: undefined,
    processNo: undefined,
    processName: undefined,
    equipmentNo: undefined,
    operatorName: undefined,
    materialBatchNo: undefined,
    inspectionResult: undefined,
    parameterJson: undefined,
    abnormalFlag: undefined,
    qualityScore: undefined,
    sourceSystem: undefined,
    sourceTable: undefined,
    sourceRecordId: undefined,
  },
  rules: {
    partId: [
      { required: true, message: "零部件实例ID不能为空", trigger: "blur" }
    ],
    stageCode: [
      { required: true, message: "生命周期阶段编码不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询课题五-零部件生命周期事件列表 */
function getList() {
  loading.value = true
  listEvent(queryParams.value).then(response => {
    eventList.value = response.rows
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
    eventId: null,
    partId: null,
    stageCode: null,
    stageName: null,
    eventType: null,
    eventName: null,
    eventDesc: null,
    eventTime: null,
    processNo: null,
    processName: null,
    equipmentNo: null,
    operatorName: null,
    materialBatchNo: null,
    inspectionResult: null,
    parameterJson: null,
    abnormalFlag: null,
    qualityScore: null,
    sourceSystem: null,
    sourceTable: null,
    sourceRecordId: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  }
  proxy.resetForm("eventRef")
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
  ids.value = selection.map(item => item.eventId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加课题五-零部件生命周期事件"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _eventId = row.eventId || ids.value
  getEvent(_eventId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改课题五-零部件生命周期事件"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["eventRef"].validate(valid => {
    if (valid) {
      if (form.value.eventId != null) {
        updateEvent(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addEvent(form.value).then(() => {
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
  const _eventIds = row.eventId || ids.value
  proxy.$modal.confirm('是否确认删除课题五-零部件生命周期事件编号为"' + _eventIds + '"的数据项？').then(function() {
    return delEvent(_eventIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('topic5/event/export', {
    ...queryParams.value
  }, `event_${new Date().getTime()}.xlsx`)
}

getList()
</script>
