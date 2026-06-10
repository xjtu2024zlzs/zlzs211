<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <span>发起流程</span>
      </template>
      <div class="form-conf" v-if="dialog.visible">
        <v-form-render v-if="hasStartForm" :form-json="formModel" :form-data="formData" ref="vfRenderRef"></v-form-render>
        <el-empty v-else description="当前流程未配置启动表单，可直接发起" />
        <div class="cu-submit">
          <el-button type="primary" @click="submit">提交</el-button>
          <el-button v-if="hasStartForm" @click="reset">重置</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup name="WorkStart" lang="js">
import { getProcessForm, startProcess } from '@/api/workflow/work/process';


const route = useRoute();
const { proxy } = getCurrentInstance() ;

const vfRenderRef = ref(null);

const deployId = ref();
const definitionId = ref();
const formModel = ref({});
const formData = ref({});
const hasStartForm = computed(() => Array.isArray(formModel.value?.widgetList) && formModel.value.widgetList.length > 0);

const dialog = reactive({
  visible: false,
  title: ''
});

const initData = async () => {
  deployId.value = route.params && route.params.deployId;
  definitionId.value = route.query && route.query.definitionId;
  const res = await getProcessForm({ definitionId: definitionId.value, deployId: deployId.value });
  formModel.value = res.data?.formModel || { formConfig: {}, widgetList: [] };
  dialog.visible = true;
  nextTick(async () => {
    if (hasStartForm.value && vfRenderRef.value) {
      vfRenderRef.value.setFormJson(formModel.value);
    }
  });
}

const submit = async () => {
  const data = hasStartForm.value && vfRenderRef.value ? await vfRenderRef.value.getFormData() : {};
  if (definitionId.value) {
    const res = await startProcess(definitionId.value, data);
    proxy.$modal.msgSuccess(res.msg);
    // const obj = { path: "/work/own" };
    // proxy?.$tab.closeOpenPage(obj);
    proxy.$tab.closePage();
    proxy.$router.back();
  }
}

const reset = () => {
  vfRenderRef.value?.resetForm();
}

onMounted(() => {
  initData();
});
</script>

<style lang="scss" scoped>
.form-conf {
  margin: 15px auto;
  width: 80%;
  padding: 15px;
}
</style>
