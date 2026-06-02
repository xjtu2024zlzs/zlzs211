<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="追溯案例编号" prop="caseNo">
        <el-input
          v-model="queryParams.caseNo"
          placeholder="请输入追溯案例编号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="案例名称" prop="caseName">
        <el-input
          v-model="queryParams.caseName"
          placeholder="请输入案例名称"
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
      <el-form-item label="产品型号/装备型号" prop="productModel">
        <el-input
          v-model="queryParams.productModel"
          placeholder="请输入产品型号/装备型号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="故障标题" prop="faultTitle">
        <el-input
          v-model="queryParams.faultTitle"
          placeholder="请输入故障标题"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="故障发生位置/区域" prop="faultLocation">
        <el-input
          v-model="queryParams.faultLocation"
          placeholder="请输入故障发生位置/区域"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="初步故障模式" prop="faultMode">
        <el-input
          v-model="queryParams.faultMode"
          placeholder="请输入初步故障模式"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="异常参数值" prop="abnormalValue">
        <el-input
          v-model="queryParams.abnormalValue"
          placeholder="请输入异常参数值"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="正常范围" prop="normalRange">
        <el-input
          v-model="queryParams.normalRange"
          placeholder="请输入正常范围"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="发现方式/检测方法" prop="detectMethod">
        <el-input
          v-model="queryParams.detectMethod"
          placeholder="请输入发现方式/检测方法"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="零件定位算法ID" prop="locateAlgorithmId">
        <el-input
          v-model="queryParams.locateAlgorithmId"
          placeholder="请输入零件定位算法ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="零件定位算法名称" prop="locateAlgorithmName">
        <el-input
          v-model="queryParams.locateAlgorithmName"
          placeholder="请输入零件定位算法名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="最终定位故障零件ID" prop="finalPartId">
        <el-input
          v-model="queryParams.finalPartId"
          placeholder="请输入最终定位故障零件ID"
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
      <el-form-item label="零件定位置信度" prop="locateConfidence">
        <el-input
          v-model="queryParams.locateConfidence"
          placeholder="请输入零件定位置信度"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="关联知识图谱ID" prop="graphId">
        <el-input
          v-model="queryParams.graphId"
          placeholder="请输入关联知识图谱ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="关联根因分析结果ID" prop="rootResultId">
        <el-input
          v-model="queryParams.rootResultId"
          placeholder="请输入关联根因分析结果ID"
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
          v-hasPermi="['topic5:partInstance:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['topic5:partInstance:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['topic5:partInstance:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['topic5:partInstance:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="partInstanceList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="追溯案例ID" align="center" prop="caseId" />
      <el-table-column label="追溯案例编号" align="center" prop="caseNo" />
      <el-table-column label="案例名称" align="center" prop="caseName" />
      <el-table-column label="所属系统" align="center" prop="systemName" />
      <el-table-column label="所属子系统" align="center" prop="subsystemName" />
      <el-table-column label="产品型号/装备型号" align="center" prop="productModel" />
      <el-table-column label="故障标题" align="center" prop="faultTitle" />
      <el-table-column label="故障现象" align="center" prop="faultPhenomenon" />
      <el-table-column label="故障详细描述" align="center" prop="faultDesc" />
      <el-table-column label="故障发生位置/区域" align="center" prop="faultLocation" />
      <el-table-column label="初步故障模式" align="center" prop="faultMode" />
      <el-table-column label="异常参数描述" align="center" prop="abnormalParam" />
      <el-table-column label="异常参数值" align="center" prop="abnormalValue" />
      <el-table-column label="正常范围" align="center" prop="normalRange" />
      <el-table-column label="发现方式/检测方法" align="center" prop="detectMethod" />
      <el-table-column label="零件定位算法ID" align="center" prop="locateAlgorithmId" />
      <el-table-column label="零件定位算法名称" align="center" prop="locateAlgorithmName" />
      <el-table-column label="最终定位故障零件ID" align="center" prop="finalPartId" />
      <el-table-column label="最终定位零件编号" align="center" prop="finalPartNo" />
      <el-table-column label="最终定位零件名称" align="center" prop="finalPartName" />
      <el-table-column label="零件定位置信度" align="center" prop="locateConfidence" />
      <el-table-column label="关联知识图谱ID" align="center" prop="graphId" />
      <el-table-column label="关联根因分析结果ID" align="center" prop="rootResultId" />
      <el-table-column label="案例状态：0待定位，1已定位零件，2已构建图谱，3已完成根因分析，4失败" align="center" prop="caseStatus" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['topic5:partInstance:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['topic5:partInstance:remove']">删除</el-button>
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

    <!-- 添加或修改零部件实例对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="partInstanceRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="追溯案例编号" prop="caseNo">
              <el-input v-model="form.caseNo" placeholder="请输入追溯案例编号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="案例名称" prop="caseName">
              <el-input v-model="form.caseName" placeholder="请输入案例名称" />
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
            <el-form-item label="产品型号/装备型号" prop="productModel">
              <el-input v-model="form.productModel" placeholder="请输入产品型号/装备型号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="故障标题" prop="faultTitle">
              <el-input v-model="form.faultTitle" placeholder="请输入故障标题" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="故障现象" prop="faultPhenomenon">
              <el-input v-model="form.faultPhenomenon" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="故障详细描述" prop="faultDesc">
              <el-input v-model="form.faultDesc" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="故障发生位置/区域" prop="faultLocation">
              <el-input v-model="form.faultLocation" placeholder="请输入故障发生位置/区域" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="初步故障模式" prop="faultMode">
              <el-input v-model="form.faultMode" placeholder="请输入初步故障模式" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="异常参数描述" prop="abnormalParam">
              <el-input v-model="form.abnormalParam" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="异常参数值" prop="abnormalValue">
              <el-input v-model="form.abnormalValue" placeholder="请输入异常参数值" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="正常范围" prop="normalRange">
              <el-input v-model="form.normalRange" placeholder="请输入正常范围" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="发现方式/检测方法" prop="detectMethod">
              <el-input v-model="form.detectMethod" placeholder="请输入发现方式/检测方法" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="零件定位算法ID" prop="locateAlgorithmId">
              <el-input v-model="form.locateAlgorithmId" placeholder="请输入零件定位算法ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="零件定位算法名称" prop="locateAlgorithmName">
              <el-input v-model="form.locateAlgorithmName" placeholder="请输入零件定位算法名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="最终定位故障零件ID" prop="finalPartId">
              <el-input v-model="form.finalPartId" placeholder="请输入最终定位故障零件ID" />
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
            <el-form-item label="零件定位置信度" prop="locateConfidence">
              <el-input v-model="form.locateConfidence" placeholder="请输入零件定位置信度" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="关联知识图谱ID" prop="graphId">
              <el-input v-model="form.graphId" placeholder="请输入关联知识图谱ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="关联根因分析结果ID" prop="rootResultId">
              <el-input v-model="form.rootResultId" placeholder="请输入关联根因分析结果ID" />
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

<script setup name="PartInstance">
import { listPartInstance, getPartInstance, delPartInstance, addPartInstance, updatePartInstance } from "@/api/topic5/partInstance"

const { proxy } = getCurrentInstance()

const partInstanceList = ref([])
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
    caseNo: undefined,
    caseName: undefined,
    systemName: undefined,
    subsystemName: undefined,
    productModel: undefined,
    faultTitle: undefined,
    faultPhenomenon: undefined,
    faultDesc: undefined,
    faultLocation: undefined,
    faultMode: undefined,
    abnormalParam: undefined,
    abnormalValue: undefined,
    normalRange: undefined,
    detectMethod: undefined,
    locateAlgorithmId: undefined,
    locateAlgorithmName: undefined,
    finalPartId: undefined,
    finalPartNo: undefined,
    finalPartName: undefined,
    locateConfidence: undefined,
    graphId: undefined,
    rootResultId: undefined,
    caseStatus: undefined,
  },
  rules: {
    caseNo: [
      { required: true, message: "追溯案例编号不能为空", trigger: "blur" }
    ],
    caseName: [
      { required: true, message: "案例名称不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询零部件实例列表 */
function getList() {
  loading.value = true
  listPartInstance(queryParams.value).then(response => {
    partInstanceList.value = response.rows
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
    caseId: null,
    caseNo: null,
    caseName: null,
    systemName: null,
    subsystemName: null,
    productModel: null,
    faultTitle: null,
    faultPhenomenon: null,
    faultDesc: null,
    faultLocation: null,
    faultMode: null,
    abnormalParam: null,
    abnormalValue: null,
    normalRange: null,
    detectMethod: null,
    locateAlgorithmId: null,
    locateAlgorithmName: null,
    finalPartId: null,
    finalPartNo: null,
    finalPartName: null,
    locateConfidence: null,
    graphId: null,
    rootResultId: null,
    caseStatus: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  }
  proxy.resetForm("partInstanceRef")
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
  ids.value = selection.map(item => item.caseId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加零部件实例"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _caseId = row.caseId || ids.value
  getPartInstance(_caseId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改零部件实例"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["partInstanceRef"].validate(valid => {
    if (valid) {
      if (form.value.caseId != null) {
        updatePartInstance(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addPartInstance(form.value).then(() => {
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
  const _caseIds = row.caseId || ids.value
  proxy.$modal.confirm('是否确认删除零部件实例编号为"' + _caseIds + '"的数据项？').then(function() {
    return delPartInstance(_caseIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('topic5/partInstance/export', {
    ...queryParams.value
  }, `partInstance_${new Date().getTime()}.xlsx`)
}

getList()
</script>
