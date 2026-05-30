<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="故障表征编号" prop="symptomNo">
        <el-input
          v-model="queryParams.symptomNo"
          placeholder="请输入故障表征编号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="故障标题" prop="symptomTitle">
        <el-input
          v-model="queryParams.symptomTitle"
          placeholder="请输入故障标题"
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
      <el-form-item label="故障发生时间" prop="occurTime">
        <el-date-picker clearable
          v-model="queryParams.occurTime"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择故障发生时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="严重程度：1低，2中，3高" prop="severityLevel">
        <el-input
          v-model="queryParams.severityLevel"
          placeholder="请输入严重程度：1低，2中，3高"
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
          v-hasPermi="['topic5:symptom:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['topic5:symptom:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['topic5:symptom:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['topic5:symptom:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="symptomList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="故障表征ID" align="center" prop="symptomId" />
      <el-table-column label="故障表征编号" align="center" prop="symptomNo" />
      <el-table-column label="故障标题" align="center" prop="symptomTitle" />
      <el-table-column label="所属系统" align="center" prop="systemName" />
      <el-table-column label="所属子系统" align="center" prop="subsystemName" />
      <el-table-column label="产品型号/装备型号" align="center" prop="productModel" />
      <el-table-column label="故障现象" align="center" prop="faultPhenomenon" />
      <el-table-column label="故障详细描述" align="center" prop="faultDesc" />
      <el-table-column label="故障发生位置/区域" align="center" prop="faultLocation" />
      <el-table-column label="初步故障模式" align="center" prop="faultMode" />
      <el-table-column label="异常参数描述" align="center" prop="abnormalParam" />
      <el-table-column label="异常参数值" align="center" prop="abnormalValue" />
      <el-table-column label="正常范围" align="center" prop="normalRange" />
      <el-table-column label="发现方式/检测方法" align="center" prop="detectMethod" />
      <el-table-column label="故障发生时间" align="center" prop="occurTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.occurTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="严重程度：1低，2中，3高" align="center" prop="severityLevel" />
      <el-table-column label="输入数据类型：text/image/sensor/mixed" align="center" prop="dataType" />
      <el-table-column label="附件ID集合" align="center" prop="attachmentIds" />
      <el-table-column label="状态：0待追溯，1追溯中，2已完成" align="center" prop="symptomStatus" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['topic5:symptom:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['topic5:symptom:remove']">删除</el-button>
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

    <!-- 添加或修改课题五-故障征输入对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="symptomRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="故障表征编号" prop="symptomNo">
              <el-input v-model="form.symptomNo" placeholder="请输入故障表征编号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="故障标题" prop="symptomTitle">
              <el-input v-model="form.symptomTitle" placeholder="请输入故障标题" />
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
            <el-form-item label="故障发生时间" prop="occurTime">
              <el-date-picker clearable
                v-model="form.occurTime"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择故障发生时间">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="严重程度：1低，2中，3高" prop="severityLevel">
              <el-input v-model="form.severityLevel" placeholder="请输入严重程度：1低，2中，3高" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="附件ID集合" prop="attachmentIds">
              <el-input v-model="form.attachmentIds" type="textarea" placeholder="请输入内容" />
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

<script setup name="Symptom">
import { listSymptom, getSymptom, delSymptom, addSymptom, updateSymptom } from "@/api/topic5/symptom"

const { proxy } = getCurrentInstance()

const symptomList = ref([])
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
    symptomNo: undefined,
    symptomTitle: undefined,
    systemName: undefined,
    subsystemName: undefined,
    productModel: undefined,
    faultPhenomenon: undefined,
    faultDesc: undefined,
    faultLocation: undefined,
    faultMode: undefined,
    abnormalParam: undefined,
    abnormalValue: undefined,
    normalRange: undefined,
    detectMethod: undefined,
    occurTime: undefined,
    severityLevel: undefined,
    dataType: undefined,
    attachmentIds: undefined,
    symptomStatus: undefined,
  },
  rules: {
    symptomNo: [
      { required: true, message: "故障表征编号不能为空", trigger: "blur" }
    ],
    symptomTitle: [
      { required: true, message: "故障标题不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询课题五-故障征输入列表 */
function getList() {
  loading.value = true
  listSymptom(queryParams.value).then(response => {
    symptomList.value = response.rows
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
    symptomId: null,
    symptomNo: null,
    symptomTitle: null,
    systemName: null,
    subsystemName: null,
    productModel: null,
    faultPhenomenon: null,
    faultDesc: null,
    faultLocation: null,
    faultMode: null,
    abnormalParam: null,
    abnormalValue: null,
    normalRange: null,
    detectMethod: null,
    occurTime: null,
    severityLevel: null,
    dataType: null,
    attachmentIds: null,
    symptomStatus: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  }
  proxy.resetForm("symptomRef")
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
  ids.value = selection.map(item => item.symptomId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加课题五-故障征输入"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _symptomId = row.symptomId || ids.value
  getSymptom(_symptomId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改课题五-故障征输入"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["symptomRef"].validate(valid => {
    if (valid) {
      if (form.value.symptomId != null) {
        updateSymptom(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addSymptom(form.value).then(() => {
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
  const _symptomIds = row.symptomId || ids.value
  proxy.$modal.confirm('是否确认删除课题五-故障征输入编号为"' + _symptomIds + '"的数据项？').then(function() {
    return delSymptom(_symptomIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('topic5/symptom/export', {
    ...queryParams.value
  }, `symptom_${new Date().getTime()}.xlsx`)
}

getList()
</script>
