<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="算法编码" prop="algorithmCode">
        <el-input
          v-model="queryParams.algorithmCode"
          placeholder="请输入算法编码"
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
      <el-form-item label="算法版本" prop="algorithmVersion">
        <el-input
          v-model="queryParams.algorithmVersion"
          placeholder="请输入算法版本"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="是否默认算法：0否，1是" prop="defaultFlag">
        <el-input
          v-model="queryParams.defaultFlag"
          placeholder="请输入是否默认算法：0否，1是"
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
          v-hasPermi="['topic5:partLocateResult:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['topic5:partLocateResult:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['topic5:partLocateResult:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['topic5:partLocateResult:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="partLocateResultList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="算法ID" align="center" prop="algorithmId" />
      <el-table-column label="算法编码" align="center" prop="algorithmCode" />
      <el-table-column label="算法名称" align="center" prop="algorithmName" />
      <el-table-column label="算法类型：locate/kg_build/root_cause" align="center" prop="algorithmType" />
      <el-table-column label="算法版本" align="center" prop="algorithmVersion" />
      <el-table-column label="算法描述" align="center" prop="algorithmDesc" />
      <el-table-column label="输入说明" align="center" prop="inputDesc" />
      <el-table-column label="输出说明" align="center" prop="outputDesc" />
      <el-table-column label="是否默认算法：0否，1是" align="center" prop="defaultFlag" />
      <el-table-column label="状态：0启用，1停用" align="center" prop="status" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['topic5:partLocateResult:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['topic5:partLocateResult:remove']">删除</el-button>
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

    <!-- 添加或修改零件定位结果对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="partLocateResultRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="算法编码" prop="algorithmCode">
              <el-input v-model="form.algorithmCode" placeholder="请输入算法编码" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="算法名称" prop="algorithmName">
              <el-input v-model="form.algorithmName" placeholder="请输入算法名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="算法版本" prop="algorithmVersion">
              <el-input v-model="form.algorithmVersion" placeholder="请输入算法版本" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="算法描述" prop="algorithmDesc">
              <el-input v-model="form.algorithmDesc" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="输入说明" prop="inputDesc">
              <el-input v-model="form.inputDesc" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="输出说明" prop="outputDesc">
              <el-input v-model="form.outputDesc" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="是否默认算法：0否，1是" prop="defaultFlag">
              <el-input v-model="form.defaultFlag" placeholder="请输入是否默认算法：0否，1是" />
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

<script setup name="PartLocateResult">
import { listPartLocateResult, getPartLocateResult, delPartLocateResult, addPartLocateResult, updatePartLocateResult } from "@/api/topic5/partLocateResult"

const { proxy } = getCurrentInstance()

const partLocateResultList = ref([])
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
    algorithmCode: undefined,
    algorithmName: undefined,
    algorithmType: undefined,
    algorithmVersion: undefined,
    algorithmDesc: undefined,
    inputDesc: undefined,
    outputDesc: undefined,
    defaultFlag: undefined,
    status: undefined,
  },
  rules: {
    algorithmCode: [
      { required: true, message: "算法编码不能为空", trigger: "blur" }
    ],
    algorithmName: [
      { required: true, message: "算法名称不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询零件定位结果列表 */
function getList() {
  loading.value = true
  listPartLocateResult(queryParams.value).then(response => {
    partLocateResultList.value = response.rows
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
    algorithmId: null,
    algorithmCode: null,
    algorithmName: null,
    algorithmType: null,
    algorithmVersion: null,
    algorithmDesc: null,
    inputDesc: null,
    outputDesc: null,
    defaultFlag: null,
    status: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  }
  proxy.resetForm("partLocateResultRef")
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
  ids.value = selection.map(item => item.algorithmId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加零件定位结果"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _algorithmId = row.algorithmId || ids.value
  getPartLocateResult(_algorithmId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改零件定位结果"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["partLocateResultRef"].validate(valid => {
    if (valid) {
      if (form.value.algorithmId != null) {
        updatePartLocateResult(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addPartLocateResult(form.value).then(() => {
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
  const _algorithmIds = row.algorithmId || ids.value
  proxy.$modal.confirm('是否确认删除零件定位结果编号为"' + _algorithmIds + '"的数据项？').then(function() {
    return delPartLocateResult(_algorithmIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('topic5/partLocateResult/export', {
    ...queryParams.value
  }, `partLocateResult_${new Date().getTime()}.xlsx`)
}

getList()
</script>
