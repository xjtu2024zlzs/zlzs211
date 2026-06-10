package com.ruoyi.flowable.workflow.service.impl;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.flowable.common.constant.ProcessConstants;
import com.ruoyi.flowable.common.enums.FormType;
import com.ruoyi.flowable.core.domain.model.PageQuery;
import com.ruoyi.flowable.core.page.TableDataInfo;
import com.ruoyi.flowable.factory.FlowServiceFactory;
import com.ruoyi.flowable.utils.JsonUtils;
import com.ruoyi.flowable.utils.ModelUtils;
import com.ruoyi.flowable.workflow.domain.bo.WfModelBo;
import com.ruoyi.flowable.workflow.domain.dto.WfMetaInfoDto;
import com.ruoyi.flowable.workflow.domain.vo.WfFormVo;
import com.ruoyi.flowable.workflow.domain.vo.WfModelVo;
import com.ruoyi.flowable.workflow.service.IWfDeployFormService;
import com.ruoyi.flowable.workflow.service.IWfFormService;
import com.ruoyi.flowable.workflow.service.IWfModelService;
import com.ruoyi.system.api.model.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ModelQuery;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author KonBAI
 * @createTime 2022/6/21 9:11
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class WfModelServiceImpl extends FlowServiceFactory implements IWfModelService {

    private final IWfFormService formService;
    private final IWfDeployFormService deployFormService;

    @Override
    public TableDataInfo<WfModelVo> list(WfModelBo modelBo, PageQuery pageQuery) {
        ModelQuery modelQuery = repositoryService.createModelQuery().latestVersion().orderByCreateTime().desc();
        // 鏋勫缓鏌ヨ鏉′欢
        if (StringUtils.isNotBlank(modelBo.getModelKey())) {
            modelQuery.modelKey(modelBo.getModelKey());
        }
        if (StringUtils.isNotBlank(modelBo.getModelName())) {
            modelQuery.modelNameLike("%" + modelBo.getModelName() + "%");
        }
        if (StringUtils.isNotBlank(modelBo.getCategory())) {
            modelQuery.modelCategory(modelBo.getCategory());
        }
        // 鎵ц鏌ヨ
        long pageTotal = modelQuery.count();
        if (pageTotal <= 0) {
            return TableDataInfo.build();
        }
        int offset = pageQuery.getPageSize() * (pageQuery.getPageNum() - 1);
        List<Model> modelList = modelQuery.listPage(offset, pageQuery.getPageSize());
        List<WfModelVo> modelVoList = new ArrayList<>(modelList.size());
        modelList.forEach(model -> {
            WfModelVo modelVo = new WfModelVo();
            modelVo.setModelId(model.getId());
            modelVo.setModelName(model.getName());
            modelVo.setModelKey(model.getKey());
            modelVo.setCategory(model.getCategory());
            modelVo.setCreateTime(model.getCreateTime());
            modelVo.setVersion(model.getVersion());
            WfMetaInfoDto metaInfo = JsonUtils.parseObject(model.getMetaInfo(), WfMetaInfoDto.class);
            if (metaInfo != null) {
                modelVo.setDescription(metaInfo.getDescription());
                modelVo.setFormType(metaInfo.getFormType());
                modelVo.setFormId(metaInfo.getFormId());
            }
            modelVoList.add(modelVo);
        });
        Page<WfModelVo> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize(), pageTotal);
        page.addOrder(OrderItem.desc("createTime"));
        page.setRecords(modelVoList);
        return TableDataInfo.build(page);
    }

    @Override
    public List<WfModelVo> list(WfModelBo modelBo) {
        ModelQuery modelQuery = repositoryService.createModelQuery().latestVersion().orderByCreateTime().desc();
        // 鏋勫缓鏌ヨ鏉′欢
        if (StringUtils.isNotBlank(modelBo.getModelKey())) {
            modelQuery.modelKey(modelBo.getModelKey());
        }
        if (StringUtils.isNotBlank(modelBo.getModelName())) {
            modelQuery.modelNameLike("%" + modelBo.getModelName() + "%");
        }
        if (StringUtils.isNotBlank(modelBo.getCategory())) {
            modelQuery.modelCategory(modelBo.getCategory());
        }
        List<Model> modelList = modelQuery.list();
        List<WfModelVo> modelVoList = new ArrayList<>(modelList.size());
        modelList.forEach(model -> {
            WfModelVo modelVo = new WfModelVo();
            modelVo.setModelId(model.getId());
            modelVo.setModelName(model.getName());
            modelVo.setModelKey(model.getKey());
            modelVo.setCategory(model.getCategory());
            modelVo.setCreateTime(model.getCreateTime());
            modelVo.setVersion(model.getVersion());
            WfMetaInfoDto metaInfo = JsonUtils.parseObject(model.getMetaInfo(), WfMetaInfoDto.class);
            if (metaInfo != null) {
                modelVo.setDescription(metaInfo.getDescription());
                modelVo.setFormType(metaInfo.getFormType());
                modelVo.setFormId(metaInfo.getFormId());
            }
            modelVoList.add(modelVo);
        });
        return modelVoList;
    }

    @Override
    public TableDataInfo<WfModelVo> historyList(WfModelBo modelBo, PageQuery pageQuery) {
        ModelQuery modelQuery = repositoryService.createModelQuery()
            .modelKey(modelBo.getModelKey())
            .orderByModelVersion()
            .desc();
        // 执行查询（不显示最新版本，-1）
        long pageTotal = modelQuery.count() - 1;
        if (pageTotal <= 0) {
            return TableDataInfo.build();
        }
        // offset + 1，跳过最新版本
        int offset = 1 + pageQuery.getPageSize() * (pageQuery.getPageNum() - 1);
        List<Model> modelList = modelQuery.listPage(offset, pageQuery.getPageSize());
        List<WfModelVo> modelVoList = new ArrayList<>(modelList.size());
        modelList.forEach(model -> {
            WfModelVo modelVo = new WfModelVo();
            modelVo.setModelId(model.getId());
            modelVo.setModelName(model.getName());
            modelVo.setModelKey(model.getKey());
            modelVo.setCategory(model.getCategory());
            modelVo.setCreateTime(model.getCreateTime());
            modelVo.setVersion(model.getVersion());
            WfMetaInfoDto metaInfo = JsonUtils.parseObject(model.getMetaInfo(), WfMetaInfoDto.class);
            if (metaInfo != null) {
                modelVo.setDescription(metaInfo.getDescription());
                modelVo.setFormType(metaInfo.getFormType());
                modelVo.setFormId(metaInfo.getFormId());
            }
            modelVoList.add(modelVo);
        });
        Page<WfModelVo> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize(), pageTotal);
        page.addOrder(OrderItem.desc("createTime"));
        page.setRecords(modelVoList);
        return TableDataInfo.build(page);
    }

    @Override
    public WfModelVo getModel(String modelId) {
        // 获取流程模型
        Model model = repositoryService.getModel(modelId);
        if (ObjectUtil.isNull(model)) {
            throw new RuntimeException("流程模型不存在！");
        }
        // 获取流程图 XML
        String bpmnXml = queryBpmnXmlById(modelId);
        WfModelVo modelVo = new WfModelVo();
        modelVo.setModelId(model.getId());
        modelVo.setModelName(model.getName());
        modelVo.setModelKey(model.getKey());
        modelVo.setCategory(model.getCategory());
        modelVo.setCreateTime(model.getCreateTime());
        modelVo.setVersion(model.getVersion());
        modelVo.setBpmnXml(bpmnXml);
        WfMetaInfoDto metaInfo = JsonUtils.parseObject(model.getMetaInfo(), WfMetaInfoDto.class);
        if (metaInfo != null) {
            modelVo.setDescription(metaInfo.getDescription());
            modelVo.setFormType(metaInfo.getFormType());
            modelVo.setFormId(metaInfo.getFormId());
            if (FormType.PROCESS.getType().equals(metaInfo.getFormType())) {
                WfFormVo wfFormVo = formService.queryById(metaInfo.getFormId());
                if (wfFormVo != null) {
                    modelVo.setContent(wfFormVo.getContent());
                }
            }
        }
        return modelVo;
    }

    @Override
    public String queryBpmnXmlById(String modelId) {
        byte[] bpmnBytes = repositoryService.getModelEditorSource(modelId);
        if (bpmnBytes == null || bpmnBytes.length == 0) {
            return "";
        }
        return StrUtil.utf8Str(bpmnBytes);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertModel(WfModelBo modelBo) {
        Model model = repositoryService.newModel();
        model.setName(modelBo.getModelName());
        model.setKey(modelBo.getModelKey());
        model.setCategory(modelBo.getCategory());
        String metaInfo = buildMetaInfo(new WfMetaInfoDto(), modelBo.getDescription());
        model.setMetaInfo(metaInfo);
        repositoryService.saveModel(model);
        repositoryService.addModelEditorSource(
            model.getId(),
            StringUtils.getBytes(defaultBpmnXml(model.getKey(), model.getName()), StandardCharsets.UTF_8)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateModel(WfModelBo modelBo) {
        // 鏍规嵁妯″瀷Key鏌ヨ妯″瀷淇℃伅
        Model model = repositoryService.getModel(modelBo.getModelId());
        if (ObjectUtil.isNull(model)) {
            throw new RuntimeException("娴佺▼妯″瀷涓嶅瓨鍦紒");
        }
        model.setCategory(modelBo.getCategory());
        WfMetaInfoDto metaInfoDto = JsonUtils.parseObject(model.getMetaInfo(), WfMetaInfoDto.class);
        String metaInfo = buildMetaInfo(metaInfoDto, modelBo.getDescription());
        model.setMetaInfo(metaInfo);
        // 淇濆瓨娴佺▼妯″瀷
        repositoryService.saveModel(model);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveModel(WfModelBo modelBo) {
        Model model = getOrCreateModel(modelBo);
        if (StrUtil.isBlank(modelBo.getBpmnXml())) {
            modelBo.setBpmnXml(queryBpmnXmlById(model.getId()));
        }
        if (StrUtil.isBlank(modelBo.getBpmnXml())) {
            modelBo.setBpmnXml(defaultBpmnXml(model.getKey(), model.getName()));
        }
        String rawBpmnXml = modelBo.getBpmnXml();
        String bpmnXml = ModelUtils.cleanBpmnXml(rawBpmnXml);
        if (log.isInfoEnabled()) {
            log.info("saveModel received xml: modelId={}, rawLength={}, cleanLength={}, firstLtRaw={}, firstLtClean={}, rawPreview={}, cleanPreview={}",
                modelBo.getModelId(),
                rawBpmnXml.length(),
                bpmnXml.length(),
                rawBpmnXml.indexOf('<'),
                bpmnXml.indexOf('<'),
                previewXml(rawBpmnXml),
                previewXml(bpmnXml));
        }
        if (!ModelUtils.looksLikeBpmnXml(bpmnXml)) {
            throw new RuntimeException("流程 XML 内容无效，请确认设计器提交的是标准 BPMN XML。");
        }
        BpmnModel bpmnModel;
        try {
            bpmnModel = ModelUtils.getBpmnModel(bpmnXml);
        } catch (Exception e) {
            log.error("saveModel parse xml failed: modelId={}, rawPreview={}, cleanPreview={}",
                modelBo.getModelId(), previewXml(rawBpmnXml), previewXml(bpmnXml), e);
            throw new RuntimeException("流程 XML 解析失败，请检查模型设计是否完整。", e);
        }
        if (ObjectUtil.isEmpty(bpmnModel)) {
            throw new RuntimeException("获取模型设计失败，请检查流程图是否完整。");
        }
        if (ObjectUtil.isNull(bpmnModel.getMainProcess())) {
            throw new RuntimeException("流程 XML 中缺少流程定义，请检查模型设计是否完整。");
        }
        String processName = bpmnModel.getMainProcess().getName();
        if (StrUtil.isBlank(processName)) {
            processName = model.getName();
        }
        StartEvent startEvent = ModelUtils.getStartEvent(bpmnModel);
        if (ObjectUtil.isNull(startEvent)) {
            throw new RuntimeException("开始节点不存在，请检查流程设计是否完整。");
        }
        Model newModel;
        if (Boolean.TRUE.equals(modelBo.getNewVersion())) {
            newModel = repositoryService.newModel();
            newModel.setName(processName);
            newModel.setKey(model.getKey());
            newModel.setCategory(model.getCategory());
            newModel.setMetaInfo(model.getMetaInfo());
            newModel.setVersion(model.getVersion() + 1);
        } else {
            newModel = model;
            newModel.setName(processName);
        }
        repositoryService.saveModel(newModel);
        byte[] bpmnXmlBytes = StringUtils.getBytes(bpmnXml, StandardCharsets.UTF_8);
        repositoryService.addModelEditorSource(newModel.getId(), bpmnXmlBytes);
        return newModel.getId();
    }

    private String previewXml(String xml) {
        if (xml == null) {
            return "null";
        }
        String normalized = xml
            .replace("\r", "\\r")
            .replace("\n", "\\n")
            .replace("\t", "\\t");
        return StrUtil.sub(normalized, 0, 200);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void latestModel(String modelId) {
        // 鑾峰彇娴佺▼妯″瀷
        Model model = repositoryService.getModel(modelId);
        if (ObjectUtil.isNull(model)) {
            throw new RuntimeException("娴佺▼妯″瀷涓嶅瓨鍦紒");
        }
        Integer latestVersion = repositoryService.createModelQuery()
            .modelKey(model.getKey())
            .latestVersion()
            .singleResult()
            .getVersion();
        if (model.getVersion().equals(latestVersion)) {
            throw new RuntimeException("当前版本已经是最新版本。");
        }
        // 鑾峰彇 BPMN XML
        byte[] bpmnBytes = repositoryService.getModelEditorSource(modelId);
        if (ArrayUtil.isEmpty(bpmnBytes)) {
            bpmnBytes = StringUtils.getBytes(defaultBpmnXml(model.getKey(), model.getName()), StandardCharsets.UTF_8);
        }
        Model newModel = repositoryService.newModel();
        newModel.setName(model.getName());
        newModel.setKey(model.getKey());
        newModel.setCategory(model.getCategory());
        newModel.setMetaInfo(model.getMetaInfo());
        newModel.setVersion(latestVersion + 1);
        // 淇濆瓨娴佺▼妯″瀷
        repositoryService.saveModel(newModel);
        // 淇濆瓨 BPMN XML
        repositoryService.addModelEditorSource(newModel.getId(), bpmnBytes);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(Collection<String> ids) {
        ids.forEach(id -> {
            Model model = repositoryService.getModel(id);
            if (ObjectUtil.isNull(model)) {
                throw new RuntimeException("娴佺▼妯″瀷涓嶅瓨鍦紒");
            }
            repositoryService.deleteModel(id);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deployModel(String modelId) {
        // 鑾峰彇娴佺▼妯″瀷
        Model model = repositoryService.getModel(modelId);
        if (ObjectUtil.isNull(model)) {
            throw new RuntimeException("娴佺▼妯″瀷涓嶅瓨鍦紒");
        }
        byte[] bpmnBytes = repositoryService.getModelEditorSource(modelId);
        if (ArrayUtil.isEmpty(bpmnBytes)) {
            bpmnBytes = StringUtils.getBytes(defaultBpmnXml(model.getKey(), model.getName()), StandardCharsets.UTF_8);
            repositoryService.addModelEditorSource(modelId, bpmnBytes);
        }
        String bpmnXml = StringUtils.toEncodedString(bpmnBytes, StandardCharsets.UTF_8);
        bpmnXml = ModelUtils.cleanBpmnXml(bpmnXml);
        BpmnModel bpmnModel = ModelUtils.getBpmnModel(bpmnXml);
        validateBpmnModel(bpmnModel);
        byte[] deployBytes = StringUtils.getBytes(bpmnXml, StandardCharsets.UTF_8);
        String processName = processResourceName(model);
        // 閮ㄧ讲娴佺▼
        Deployment deployment = repositoryService.createDeployment()
            .name(model.getName())
            .key(model.getKey())
            .category(model.getCategory())
            .addBytes(processName, deployBytes)
            .deploy();
        ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery()
            .deploymentId(deployment.getId())
            .singleResult();
        // 淇敼娴佺▼瀹氫箟鐨勫垎绫伙紝渚夸簬鎼滅储娴佺▼
        repositoryService.setProcessDefinitionCategory(procDef.getId(), model.getCategory());
        // 淇濆瓨閮ㄧ讲琛ㄥ崟
        return deployFormService.saveInternalDeployForm(deployment.getId(), bpmnModel);
    }

    private Model getOrCreateModel(WfModelBo modelBo) {
        if (StrUtil.isNotBlank(modelBo.getModelId())) {
            Model model = repositoryService.getModel(modelBo.getModelId());
            if (ObjectUtil.isNotNull(model)) {
                return model;
            }
        }
        if (StrUtil.isBlank(modelBo.getModelKey())) {
            throw new RuntimeException("模型标识不能为空。");
        }
        if (StrUtil.isBlank(modelBo.getModelName())) {
            throw new RuntimeException("模型名称不能为空。");
        }
        Model model = repositoryService.newModel();
        model.setName(modelBo.getModelName());
        model.setKey(modelBo.getModelKey());
        model.setCategory(modelBo.getCategory());
        model.setMetaInfo(buildMetaInfo(new WfMetaInfoDto(), modelBo.getDescription()));
        repositoryService.saveModel(model);
        modelBo.setModelId(model.getId());
        return model;
    }

    private void validateBpmnModel(BpmnModel bpmnModel) {
        if (ObjectUtil.isEmpty(bpmnModel) || ObjectUtil.isNull(bpmnModel.getMainProcess())) {
            throw new RuntimeException("流程模型为空，请先在设计器中保存有效的 BPMN 流程。");
        }
        if (StrUtil.isBlank(bpmnModel.getMainProcess().getId())) {
            throw new RuntimeException("流程定义 Key 为空，请检查流程属性。");
        }
        StartEvent startEvent = ModelUtils.getStartEvent(bpmnModel);
        if (ObjectUtil.isNull(startEvent)) {
            throw new RuntimeException("开始节点不存在，请检查流程设计是否完整。");
        }
    }

    private String processResourceName(Model model) {
        String key = StrUtil.blankToDefault(model.getKey(), "process");
        if (key.endsWith(ProcessConstants.SUFFIX)) {
            return key;
        }
        return key + ProcessConstants.SUFFIX;
    }

    private String defaultBpmnXml(String key, String name) {
        String processKey = sanitizeProcessKey(StrUtil.blankToDefault(key, "Process_" + System.currentTimeMillis()));
        String processName = escapeXml(StrUtil.blankToDefault(name, processKey));
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <bpmn2:definitions
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL"
              xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
              xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
              xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
              id="diagram_%s"
              targetNamespace="http://flowable.org/bpmn">
              <bpmn2:process id="%s" name="%s" isExecutable="true">
                <bpmn2:startEvent id="StartEvent_1" name="开始" />
                <bpmn2:endEvent id="EndEvent_1" name="结束" />
                <bpmn2:sequenceFlow id="Flow_1" sourceRef="StartEvent_1" targetRef="EndEvent_1" />
              </bpmn2:process>
              <bpmndi:BPMNDiagram id="BPMNDiagram_1">
                <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="%s">
                  <bpmndi:BPMNShape id="StartEvent_1_di" bpmnElement="StartEvent_1">
                    <dc:Bounds x="150" y="120" width="36" height="36" />
                  </bpmndi:BPMNShape>
                  <bpmndi:BPMNShape id="EndEvent_1_di" bpmnElement="EndEvent_1">
                    <dc:Bounds x="300" y="120" width="36" height="36" />
                  </bpmndi:BPMNShape>
                  <bpmndi:BPMNEdge id="Flow_1_di" bpmnElement="Flow_1">
                    <di:waypoint x="186" y="138" />
                    <di:waypoint x="300" y="138" />
                  </bpmndi:BPMNEdge>
                </bpmndi:BPMNPlane>
              </bpmndi:BPMNDiagram>
            </bpmn2:definitions>
            """.formatted(processKey, processKey, processName, processKey);
    }

    private String sanitizeProcessKey(String key) {
        String sanitized = key.replaceAll("[^A-Za-z0-9_]", "_");
        if (!sanitized.matches("[A-Za-z_].*")) {
            sanitized = "Process_" + sanitized;
        }
        return sanitized;
    }

    private String escapeXml(String value) {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;");
    }

    /**
     * 鏋勫缓妯″瀷鎵╁睍淇℃伅
     * @return
     */
    private String buildMetaInfo(WfMetaInfoDto metaInfo, String description) {
        if (StringUtils.isNotEmpty(description)) {
            metaInfo.setDescription(description);
        }
        if (StringUtils.isNotEmpty(metaInfo.getCreateUser())) {
            LoginUser loginUser = SecurityUtils.getLoginUser();
            if (StringUtils.isNotNull(loginUser)){
                metaInfo.setCreateUser(loginUser.getUsername());
            }
        }
        return JsonUtils.toJsonString(metaInfo);
    }
}
