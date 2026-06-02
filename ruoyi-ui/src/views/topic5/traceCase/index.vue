<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
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
      <el-form-item label="产品型号" prop="productModel">
        <el-input
          v-model="queryParams.productModel"
          placeholder="请输入产品型号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="所属系统" prop="systemName">
        <el-input
          v-model="queryParams.systemName"
          placeholder="请输入所属系统"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="所属子系统" prop="subsystemName">
        <el-input
          v-model="queryParams.subsystemName"
          placeholder="请输入所属子系统"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="材料牌号/材料规格" prop="materialSpec">
        <el-input
          v-model="queryParams.materialSpec"
          placeholder="请输入材料牌号/材料规格"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="供应商名称" prop="supplierName">
        <el-input
          v-model="queryParams.supplierName"
          placeholder="请输入供应商名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="安装位置" prop="installPosition">
        <el-input
          v-model="queryParams.installPosition"
          placeholder="请输入安装位置"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="父级部件ID" prop="parentPartId">
        <el-input
          v-model="queryParams.parentPartId"
          placeholder="请输入父级部件ID"
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
          v-hasPermi="['topic5:traceCase:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['topic5:traceCase:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['topic5:traceCase:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['topic5:traceCase:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="traceCaseList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="零部件实例ID" align="center" prop="partId" />
      <el-table-column label="零件编号" align="center" prop="partNo" />
      <el-table-column label="零件名称" align="center" prop="partName" />
      <el-table-column label="零件序列号" align="center" prop="serialNo" />
      <el-table-column label="批次号" align="center" prop="batchNo" />
      <el-table-column label="产品型号" align="center" prop="productModel" />
      <el-table-column label="所属系统" align="center" prop="systemName" />
      <el-table-column label="所属子系统" align="center" prop="subsystemName" />
      <el-table-column label="材料牌号/材料规格" align="center" prop="materialSpec" />
      <el-table-column label="供应商名称" align="center" prop="supplierName" />
      <el-table-column label="安装位置" align="center" prop="installPosition" />
      <el-table-column label="父级部件ID" align="center" prop="parentPartId" />
      <el-table-column label="生命周期状态：0制造中，1已装配，2服役中，3维修中，4退役" align="center" prop="lifecycleStatus" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['topic5:traceCase:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['topic5:traceCase:remove']">删除</el-button>
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

    <!-- 添加或修改追溯案例对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="traceCaseRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
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
            <el-form-item label="产品型号" prop="productModel">
              <el-input v-model="form.productModel" placeholder="请输入产品型号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="所属系统" prop="systemName">
              <el-input v-model="form.systemName" placeholder="请输入所属系统" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="所属子系统" prop="subsystemName">
              <el-input v-model="form.subsystemName" placeholder="请输入所属子系统" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="材料牌号/材料规格" prop="materialSpec">
              <el-input v-model="form.materialSpec" placeholder="请输入材料牌号/材料规格" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="供应商名称" prop="supplierName">
              <el-input v-model="form.supplierName" placeholder="请输入供应商名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="安装位置" prop="installPosition">
              <el-input v-model="form.installPosition" placeholder="请输入安装位置" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="父级部件ID" prop="parentPartId">
              <el-input v-model="form.parentPartId" placeholder="请输入父级部件ID" />
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

<script setup name="TraceCase">
import { listTraceCase, getTraceCase, delTraceCase, addTraceCase, updateTraceCase } from "@/api/topic5/traceCase"

const { proxy } = getCurrentInstance()

const traceCaseList = ref([])
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
    partNo: undefined,
    partName: undefined,
    serialNo: undefined,
    batchNo: undefined,
    productModel: undefined,
    systemName: undefined,
    subsystemName: undefined,
    materialSpec: undefined,
    supplierName: undefined,
    installPosition: undefined,
    parentPartId: undefined,
    lifecycleStatus: undefined,
  },
  rules: {
    partNo: [
      { required: true, message: "零件编号不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询追溯案例列表 */
function getList() {
  loading.value = true
  listTraceCase(queryParams.value).then(response => {
    traceCaseList.value = response.rows
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
    partId: null,
    partNo: null,
    partName: null,
    serialNo: null,
    batchNo: null,
    productModel: null,
    systemName: null,
    subsystemName: null,
    materialSpec: null,
    supplierName: null,
    installPosition: null,
    parentPartId: null,
    lifecycleStatus: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  }
  proxy.resetForm("traceCaseRef")
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
  ids.value = selection.map(item => item.partId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加追溯案例"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _partId = row.partId || ids.value
  getTraceCase(_partId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改追溯案例"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["traceCaseRef"].validate(valid => {
    if (valid) {
      if (form.value.partId != null) {
        updateTraceCase(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addTraceCase(form.value).then(() => {
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
  const _partIds = row.partId || ids.value
  proxy.$modal.confirm('是否确认删除追溯案例编号为"' + _partIds + '"的数据项？').then(function() {
    return delTraceCase(_partIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('topic5/traceCase/export', {
    ...queryParams.value
  }, `traceCase_${new Date().getTime()}.xlsx`)
}

getList()
</script>
