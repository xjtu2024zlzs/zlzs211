<template>
 <div class="app-container">
      <div class="search" v-show="showSearch">
        <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="70">
          <el-form-item label="模型标识" prop="modelKey">
            <el-input v-model="queryParams.modelKey" placeholder="请输入模型标识" clearable style="width: 200px" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="模型名称" prop="modelName">
            <el-input v-model="queryParams.modelName" placeholder="请输入模型名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="流程分类" prop="category">
            <el-select v-model="queryParams.category" clearable placeholder="请选择" @change="handleQuery"  style="width: 240px">
              <el-option
                v-for="item in categoryOptions"
                :key="item.categoryId"
                :label="item.categoryName"
                :value="item.code">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    <el-card shadow="never">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['workflow:model:add']">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['workflow:model:remove']">删除</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['workflow:model:export']">导出</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" :data="modelList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="模型标识" align="center" prop="modelKey" :show-overflow-tooltip="true" />
        <el-table-column label="模型名称" align="center" :show-overflow-tooltip="true">
          <template #default="scope">
            <a class="link-type" @click="handleProcessView(scope.row)">
              <span>{{ scope.row.modelName }}</span>
            </a>
          </template>
        </el-table-column>
        <el-table-column label="流程分类" align="center" prop="categoryName" :formatter="categoryFormat" />
        <el-table-column label="模型版本" align="center">
          <template #default="scope">
            <el-tag size="small">v{{ scope.row.version }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="描述" align="center" prop="description" :show-overflow-tooltip="true" />
        <el-table-column label="创建时间" align="center" prop="createTime" width="180" >
          <template #default="scope">
              {{  parseTime(scope.row.createTime, '{y}-{m}-{d}  {h}:{i}:{s}')}}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['workflow:model:edit']"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['workflow:model:remove']"></el-button>
            </el-tooltip>
            <el-tooltip content="设计" placement="top">
              <el-button link type="primary" icon="Brush" @click="handleDesigner(scope.row)" v-hasPermi="['workflow:model:designer']"></el-button>
            </el-tooltip>
            <el-tooltip content="部署" placement="top">
              <el-button link type="primary" icon="Promotion" @click="handleDeploy(scope.row)" v-hasPermi="['workflow:model:deploy']"></el-button>
            </el-tooltip>
            <el-tooltip content="历史" placement="top">
              <el-button link type="primary" icon="Discount" @click="handleHistory(scope.row)" v-hasPermi="['workflow:model:list']"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>

    <!-- 添加或修改模型信息对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="500px" append-to-body>
      <el-form ref="modelFormRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="模型标识" prop="modelKey">
          <el-input v-model="form.modelKey" clearable disabled placeholder="请输入模型标识" />
        </el-form-item>
        <el-form-item label="模型名称" prop="modelName">
          <el-input v-model="form.modelName" clearable :disabled="form.modelId !== undefined" placeholder="请输入模型名称" />
        </el-form-item>
        <el-form-item label="流程分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择" clearable style="width:100%">
            <el-option v-for="item in categoryOptions" :key="item.categoryId" :label="item.categoryName" :value="item.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入内容" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确定</el-button>
          <el-button @click="cancel">取消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog :title="designer.title" v-model="designer.visible" append-to-body fullscreen class="designer-dialog">
      <template #header>
        <div class="designer-toolbar">
          <span class="designer-toolbar__title">{{ designer.title }}</span>
          <div class="designer-toolbar__actions">
            <el-button @click="closeDesigner">关闭</el-button>
            <el-button type="primary" :loading="designerLoading" @click="onSaveDesigner">保存模型</el-button>
          </div>
        </div>
      </template>
      <ProcessDesigner
        :key="`designer-${reloadIndex}`"
        ref="modelDesignerRef"
        v-loading="designerLoading"
        :designer-form="designerForm"
        :bpmn-xml="bpmnXml"
        @update:bpmnXml="bpmnXml = $event"
      />
      <template #footer>
        <div class="dialog-footer designer-footer">
          <el-button @click="closeDesigner">关闭</el-button>
          <el-button type="primary" :loading="designerLoading" @click="onSaveDesigner">保存模型</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog :title="history.title" v-model="history.visible" append-to-body>
      <el-table v-loading="historyLoading" :data="historyList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="模型标识" align="center" prop="modelKey" :show-overflow-tooltip="true" />
        <el-table-column label="模型名称" align="center" prop="modelName" :show-overflow-tooltip="true" />
        <el-table-column label="流程分类" align="center" prop="categoryName" :formatter="categoryFormat" />
        <el-table-column label="模型版本" align="center">
          <template #default="scope">
            <el-tag>v{{ scope.row.version }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="描述" align="center" prop="description" :show-overflow-tooltip="true" />
        <el-table-column label="创建时间" align="center" prop="createTime" width="180" />
        <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="部署" placement="top">
              <el-button link type="primary" icon="Promotion" @click="handleDeploy(scope.row)" v-hasPermi="['workflow:model:deploy']"></el-button>
            </el-tooltip>
            <el-tooltip content="设为最新" placement="top">
              <el-button link type="primary" icon="Star" @click="handleLatest(scope.row)" v-hasPermi="['workflow:model:save']"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-dialog>

    <!-- 流程图 -->
    <el-dialog :title="processDialog.title" v-model="processDialog.visible" width="70%">
      <ProcessViewer :key="`designer-${reloadIndex}`" :xml="processXml" :style="{height: '650px'}" />
    </el-dialog>
  </div>
</template>

<script setup name="Model" lang="js">
import { getBpmnXml, listModel, historyModel, latestModel, addModel, updateModel, saveModel, delModel, deployModel, getModel } from "@/api/workflow/model";
import { listAllCategory } from "@/api/workflow/category";
import ProcessDesigner from "@/components/ProcessDesigner";
import ProcessViewer from "@/components/ProcessViewer";
import DefaultEmptyXML from "@/package/designer/plugins/defaultEmpty";

const { proxy } = getCurrentInstance() ;

const modelList = ref([]);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const categoryOptions = ref([]);
const designerLoading = ref(true);
const bpmnXml = ref('');
const reloadIndex = ref(0);
const processXml = ref("");

const historyList = ref([]);
const historyLoading = ref(true);
const historyTotal = ref(0);

const modelFormRef = ref();
const queryFormRef = ref();
const modelDesignerRef = ref(null)

const dialog = reactive({
  visible: false,
  title: ''
});

const processDialog = reactive({
  visible: false,
  title: '流程图'
});

const designer = reactive({
  visible: false,
  title: ''
});

const history = reactive({
  visible: false,
  title: ''
});

const resetDesignerForm = () => {
  designerForm.modelId = '';
  designerForm.processName = '';
  designerForm.processKey = '';
};

const closeDesigner = () => {
  designer.visible = false;
  designerLoading.value = true;
  bpmnXml.value = '';
  resetDesignerForm();
};

const getDefaultBpmnXml = (modelId, modelName) => DefaultEmptyXML(modelId, modelName, 'flowable');

const generateModelKey = () => `Process_${Date.now()}`;
const generateModelName = () => `业务流程_${Date.now()}`;

const initFormData={
  modelId: undefined,
  modelKey: '',
  modelName: '',
  category: '',
  description: '',
  formType: undefined,
  formId: undefined,
  bpmnXml: '',
  newVersion: false
}

const data = reactive({
  form: {...initFormData},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    modelKey: '',
    modelName: '',
    category: ''
  },
  rules: {
    modelKey: [{ required: true, message: "模型标识不能为空", trigger: "blur" }],
    modelName: [{ required: true, message: "模型名称不能为空", trigger: "blur" }],
    category: [{ required: true, message: "流程分类不能为空", trigger: "change" }]
  }
});

const designerForm = reactive({
  modelId: '',
  processName: '',
  processKey: ''
});

const { queryParams, form, rules } = toRefs(data);

const router = useRouter();

/** 查询模型列表 */
const getList = async () => {
  loading.value = true;
  const res = await listModel(queryParams.value);
  modelList.value = res.rows;
  total.value = res.total;
  loading.value = false;
}
/** 取消按钮 */
const cancel = () => {
  reset();
  dialog.visible = false;
}
/** 表单重置 */
const reset = () => {
  form.value = {
    ...initFormData,
    modelKey: generateModelKey(),
    modelName: generateModelName()
  };
  modelFormRef.value?.resetFields();
}
/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
}
/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields();
  handleQuery();
}
/** 多选框选中数据 */
const handleSelectionChange = (selection) => {
  ids.value = selection.map(item => item.modelId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}
/** 新增按钮操作 */
const handleAdd = () => {
  dialog.visible = true;
  dialog.title = "添加模型";
  nextTick(() => {
    reset();
  })
}
/** 修改按钮操作 */
const handleUpdate = (row) => {
  dialog.visible = true;
  dialog.title = "修改模型";
  nextTick(async () => {
    reset();
    const modelId = row.modelId || ids.value[0];
    const res = await getModel(modelId);
    form.value = res.data;
  });
};
/** 删除按钮操作 */
const handleDelete = async (row) => {
  const modelIds = row.modelId || ids.value;
  await proxy?.$modal.confirm(`是否确认删除模型编号为 "${modelIds}" 的数据项？`);
  await delModel(modelIds);
  getList();
  proxy?.$modal.msgSuccess("删除成功");
}
/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download("flowable/model/export", {
    ...queryParams.value
  }, `model_${new Date().getTime()}.xlsx`);
};
/** 查看流程图 */
const handleProcessView = async (row) => {
  reloadIndex.value++;
  const res = await getBpmnXml(row.modelId);
  const xml = res.data;
  processXml.value = (xml && xml.trim() && xml !== 'null') ? xml : getDefaultBpmnXml(row.modelKey || generateModelKey(), row.modelName || generateModelName());
  processDialog.visible = true;
}
/** 设计按钮操作 */
const handleDesigner = async (row) => {
  reloadIndex.value++;
  designerForm.modelId = row.modelId;
  designerForm.processKey = row.modelKey || '';
  designerForm.processName = row.modelName || '';
  const res = await getBpmnXml(row.modelId);
  const xml = res.data;
  if (xml && xml.trim() && xml !== 'null') {
    bpmnXml.value = xml;
  } else {
    bpmnXml.value = getDefaultBpmnXml(row.modelKey || generateModelKey(), row.modelName || generateModelName());
  }
  designerLoading.value = false;
  designer.title = "流程设计 - " + row.modelName;
  designer.visible = true;
}
const handleDeploy = (row) => {
  loading.value = true;
  nextTick(async () => {
    await deployModel({ modelId: row?.modelId });
    proxy.$modal.msgSuccess("操作成功");
    router.push({
      name: 'Deploy',
      path: '/workflow/deploy'
    });
    loading.value = false;
  });
}
const handleLatest = async (row) => {
  await proxy.$modal.confirm("是否将此模型保存为新版本？");
  historyLoading.value = true;
  await latestModel({modelId: row.modelId});
  history.visible = false;
  getList();
  proxy?.$modal.msgSuccess("操作成功");
  historyLoading.value = false;
}
/** 查询历史列表 */
const getHistoryList = async () => {
  historyLoading.value = true;
  const res = await historyModel(queryParams.value);
  historyList.value = res.rows;
  historyTotal.value = res.total;
  historyLoading.value = false;
}
const handleHistory = (row) => {
  history.visible = true;
  history.title = "模型历史";
  queryParams.value.modelKey = row?.modelKey;
  getHistoryList();
}
/** 提交表单操作 */
const submitForm = () => {
  modelFormRef.value.validate(async (valid) => {
    if (valid) {
      form.value.modelId ? await updateModel(form.value) : await addModel(form.value);
      proxy.$modal.msgSuccess("操作成功");
      dialog.visible = false;
      getList();
    }
  })
}
/** 查询流程分类列表 */
const getCategoryList = async () => {
  const res = await listAllCategory();
  categoryOptions.value = res.data;
}

const onSaveDesigner = async () => {
  if (designerLoading.value) {
    return;
  }

  designerLoading.value = true;
  let xml = '';
  try {
    if (!modelDesignerRef.value || typeof modelDesignerRef.value.getXmlString !== 'function') {
      throw new Error('设计器实例未就绪');
    }
    xml = await modelDesignerRef.value.getXmlString();
  } catch (e) {
    console.error('[onSaveDesigner] getXmlString error:', e);
    proxy?.$modal.msgError('获取流程图 XML 失败：' + (e.message || '未知错误'));
    designerLoading.value = false;
    return;
  }

  if (!xml || !xml.includes('<')) {
    proxy?.$modal.msgError('未获取到有效的 BPMN XML，请稍后重试');
    designerLoading.value = false;
    return;
  }

  bpmnXml.value = xml;
  const dataBody = {
    modelId: designerForm.modelId,
    bpmnXml: xml
  };

  designerLoading.value = false;

  try {
    await proxy.$modal.confirm('是否将此模型保存为新版本？');
    await confirmSave(dataBody, true);
  } catch (action) {
    if (action === 'cancel') {
      await confirmSave(dataBody, false);
    }
  }
}
const confirmSave = async (body, newVersion) => {
  designerLoading.value = true;
  try {
    const res = await saveModel({ ...body, newVersion });
    if (res.code === 200 && res.data) {
      designerForm.modelId = res.data;
    }
    await getList();
    proxy.$modal.msgSuccess('保存成功');
    closeDesigner();
  } catch (error) {
    console.error('[confirmSave] saveModel error:', error);
    proxy?.$modal.msgError('保存失败：' + (error?.message || '未知错误'));
  } finally {
    designerLoading.value = false;
  }
}

const categoryFormat = (row) => {
  var category = categoryOptions.value.find(function(k) {
        return k.code === row.category;
    });
    return category ? category.categoryName : '';
}

onMounted(() => {
  getCategoryList()
  getList();
});
</script>

<style lang="scss" scoped>
:deep(.designer-dialog .el-dialog) {
  overflow: hidden;
}

:deep(.designer-dialog .el-dialog__header) {
  padding: 0;
  margin-right: 0;
}

:deep(.designer-dialog .el-dialog__headerbtn) {
  z-index: 50;
}

:deep(.designer-dialog .el-dialog__body) {
  max-height: calc(100vh - 120px) !important;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 0 0 88px;
}

.designer-toolbar {
  position: sticky;
  top: 0;
  z-index: 40;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.designer-toolbar__title {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.designer-toolbar__actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-right: 48px;
}

:deep(.designer-dialog .el-dialog__footer) {
  position: sticky;
  bottom: 0;
  z-index: 20;
  background: #fff;
  border-top: 1px solid var(--el-border-color-lighter);
}
</style>
