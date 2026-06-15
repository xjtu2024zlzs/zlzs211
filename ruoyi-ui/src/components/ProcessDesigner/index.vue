<template>
  <div class="process-design" :style="'display: flex; height:' + height">
    <my-process-designer
      v-model="xmlString"
      v-bind="controlForm"
      keyboard
      ref="processDesigner"
      :events="[
        'element.click',
        'connection.added',
        'connection.removed',
        'connection.changed'
      ]"
      @element-click="elementClick"
      @init-finished="initModeler"
      @element-contextmenu="elementContextmenu"
      @save="onSaveProcess"
      @update:modelValue="onModelValueChange"
    />
    <my-process-penal :bpmn-modeler="modeler" :prefix="controlForm.prefix" class="process-panel" />

    <!-- demo config -->
<!--    <div class="demo-control-bar">-->
<!--      <div class="open-model-button" @click="controlDrawerVisible = true"><el-icon><setting /></el-icon></div>-->
<!--    </div>-->
<!--    <el-drawer v-model="controlDrawerVisible" size="400px" title="鍋忓ソ璁剧疆" append-to-body destroy-on-close>-->
<!--      <el-form :model="controlForm" size="small" label-width="100px" class="control-form" @submit.prevent>-->
<!--        <el-form-item label="娴佺▼ID">-->
<!--          <el-input v-model="controlForm.processId" @change="reloadProcessDesigner(true)" />-->
<!--        </el-form-item>-->
<!--        <el-form-item label="娴佺▼鍚嶇О">-->
<!--          <el-input v-model="controlForm.processName" @change="reloadProcessDesigner(true)" />-->
<!--        </el-form-item>-->
<!--        <el-form-item label="娴佽浆妯℃嫙">-->
<!--          <el-switch v-model="controlForm.simulation" inactive-text="鍋滅敤" active-text="鍚敤" @change="reloadProcessDesigner()" />-->
<!--        </el-form-item>-->
<!--        <el-form-item label="绂佺敤鍙屽嚮">-->
<!--          <el-switch v-model="controlForm.labelEditing" inactive-text="鍋滅敤" active-text="鍚敤" @change="changeLabelEditingStatus" />-->
<!--        </el-form-item>-->
<!--        <el-form-item label="鑷畾涔夋覆鏌?>-->
<!--          <el-switch v-model="controlForm.labelVisible" inactive-text="鍋滅敤" active-text="鍚敤" @change="changeLabelVisibleStatus" />-->
<!--        </el-form-item>-->
<!--        <el-form-item label="娴佺▼寮曟搸">-->
<!--          <el-radio-group v-model="controlForm.prefix" @change="reloadProcessDesigner()">-->
<!--            <el-radio label="camunda">camunda</el-radio>-->
<!--            <el-radio label="flowable">flowable</el-radio>-->
<!--            <el-radio label="activiti">activiti</el-radio>-->
<!--          </el-radio-group>-->
<!--        </el-form-item>-->
<!--        <el-form-item label="宸ュ叿鏍?>-->
<!--          <el-radio-group v-model="controlForm.headerButtonSize">-->
<!--            <el-radio label="small">small</el-radio>-->
<!--            <el-radio label="default">default</el-radio>-->
<!--            <el-radio label="large">large</el-radio>-->
<!--          </el-radio-group>-->
<!--        </el-form-item>-->
<!--        <el-switch v-model="pageMode" active-text="dark" inactive-text="light" @change="changePageMode"></el-switch>-->
<!--      </el-form>-->
<!--    </el-drawer>-->
  </div>
</template>

<script>
import MyProcessDesigner from '@/package/designer';
// import MyProcessPalette from '@/package/palette';
import MyProcessPenal from '@/package/penal';
import CustomRenderer from '@/modules/custom-renderer';
import CustomContentPadProvider from '@/package/designer/plugins/content-pad';
import CustomPaletteProvider from '@/package/designer/plugins/palette';

import '@/package/theme/index.scss';
import 'bpmn-js/dist/assets/diagram-js.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-codes.css';

export default {
  name: 'ProcessDesigner',
  emits: ['save', 'update:bpmnXml'],
  props: {
    bpmnXml: {
      type: String,
      required: true
    },
    designerForm: {
      type: Object,
      required: true
    }
  },
  components: {
    MyProcessDesigner,
    MyProcessPenal
  },
  data () {
    return {
      height: `calc(100vh - 170px)`,
      xmlString: this.bpmnXml,
      modeler: null,
      controlDrawerVisible: false,
      infoTipVisible: false,
      pageMode: false,
      controlForm: {
        processId: this.designerForm.processKey || '',
        processName: this.designerForm.processName || '',
        simulation: true,
        labelEditing: false,
        labelVisible: false,
        prefix: 'flowable',
        headerButtonSize: 'default',
        additionalModel: [CustomContentPadProvider, CustomPaletteProvider]
      },
      addis: {
        CustomContentPadProvider,
        CustomPaletteProvider
      }
    }
  },
  methods: {
    async getXmlString() {
      const processDesigner = this.$refs.processDesigner;
      if (!processDesigner || typeof processDesigner.getXmlString !== 'function') {
        if (this.xmlString && this.xmlString.includes('<')) {
          return this.xmlString;
        }
        throw new Error('设计器未就绪，无法获取 BPMN XML');
      }

      return await processDesigner.getXmlString();
    },
    reloadProcessDesigner(notDeep) {
      this.controlForm.additionalModel = [];
      for (const key in this.addis) {
        if (this.addis[key]) {
          this.controlForm.additionalModel.push(this.addis[key]);
        }
      }
      !notDeep && (this.xmlString = undefined);
      this.reloadIndex += 1;
      this.modeler = null; // 閬垮厤 panel 寮傚父
    },
    changeLabelEditingStatus(status) {
      this.addis.labelEditing = status ? { labelEditingProvider: ['value', ''] } : false;
      this.reloadProcessDesigner();
    },
    changeLabelVisibleStatus(status) {
      this.addis.customRenderer = status ? CustomRenderer : false;
      this.reloadProcessDesigner();
    },
    elementClick(element) {
      this.element = element;
    },
    initModeler(modeler) {
      setTimeout(() => {
        this.modeler = modeler;
      }, 10);
    },
    elementContextmenu(element) {
    },
    onModelValueChange(xml) {
      this.xmlString = xml;
      this.$emit('update:bpmnXml', xml);
    },
    onSaveProcess(saveData) {
      this.$emit('save', saveData);
    }
  }
}
</script>

<style lang="scss">
body {
  overflow: hidden;
  margin: 0;
  box-sizing: border-box;
}
body,
body * {
  /* 婊氬姩鏉?*/
  &::-webkit-scrollbar-track-piece {
    background-color: #fff; /*婊氬姩鏉＄殑鑳屾櫙棰滆壊*/
    -webkit-border-radius: 0; /*婊氬姩鏉＄殑鍦嗚瀹藉害*/
  }

  &::-webkit-scrollbar {
    width: 10px; /*婊氬姩鏉＄殑瀹藉害*/
    height: 8px; /*婊氬姩鏉＄殑楂樺害*/
  }

  &::-webkit-scrollbar-thumb:vertical {
    /*鍨傜洿婊氬姩鏉＄殑鏍峰紡*/
    height: 50px;
    background-color: rgba(153, 153, 153, 0.5);
    -webkit-border-radius: 4px;
    outline: 2px solid #fff;
    outline-offset: -2px;
    border: 2px solid #fff;
  }

  &::-webkit-scrollbar-thumb {
    /*婊氬姩鏉＄殑hover鏍峰紡*/
    background-color: rgba(159, 159, 159, 0.3);
    -webkit-border-radius: 4px;
  }

  &::-webkit-scrollbar-thumb:hover {
    /*婊氬姩鏉＄殑hover鏍峰紡*/
    background-color: rgba(159, 159, 159, 0.5);
    -webkit-border-radius: 4px;
  }
}
.demo-control-bar {
  position: fixed;
  right: 8px;
  bottom: 48px;
  z-index: 1;
}
.open-model-button {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  font-size: 32px;
  background: rgba(64, 158, 255, 1);
  color: #ffffff;
  cursor: pointer;
}
</style>
