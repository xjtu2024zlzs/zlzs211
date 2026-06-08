package com.ruoyi.project1.dossier.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.project1.dossier.domain.DossierEntity;
import com.ruoyi.project1.dossier.domain.DossierTemplate;
import com.ruoyi.project1.dossier.domain.DossierTemplateChapter;
import com.ruoyi.project1.dossier.domain.DossierTemplateDataSource;
import com.ruoyi.project1.dossier.domain.DossierTemplateDetail;
import com.ruoyi.project1.dossier.domain.DossierTemplateParam;
import com.ruoyi.project1.dossier.domain.DossierTemplateRule;
import com.ruoyi.project1.dossier.mapper.DossierTemplateChapterMapper;
import com.ruoyi.project1.dossier.mapper.DossierTemplateDataSourceMapper;
import com.ruoyi.project1.dossier.mapper.DossierTemplateMapper;
import com.ruoyi.project1.dossier.mapper.DossierTemplateParamMapper;
import com.ruoyi.project1.dossier.mapper.DossierTemplateRuleMapper;
import com.ruoyi.project1.dossier.service.IDossierTemplateService;

@Service
public class DossierTemplateServiceImpl implements IDossierTemplateService
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Pattern VERSION_PATTERN = Pattern.compile("^V?(\\d+)(?:\\.(\\d+))?$", Pattern.CASE_INSENSITIVE);

    @Autowired
    private DossierTemplateMapper templateMapper;

    @Autowired
    private DossierTemplateChapterMapper chapterMapper;

    @Autowired
    private DossierTemplateDataSourceMapper dataSourceMapper;

    @Autowired
    private DossierTemplateRuleMapper ruleMapper;

    @Autowired
    private DossierTemplateParamMapper paramMapper;

    @Override
    public List<DossierTemplate> selectDossierTemplateList(DossierTemplate template)
    {
        return templateMapper.selectDossierTemplateList(template);
    }

    @Override
    public DossierTemplateDetail selectDossierTemplateById(String id)
    {
        DossierTemplate template = templateMapper.selectDossierTemplateById(id);
        if (template == null)
        {
            return null;
        }
        DossierTemplateDetail detail = new DossierTemplateDetail();
        BeanUtils.copyProperties(template, detail);
        detail.setChapters(chapterMapper.selectByTemplateId(id));
        detail.setDataSources(dataSourceMapper.selectByTemplateId(id));
        detail.setRules(ruleMapper.selectByTemplateId(id));
        detail.setParams(paramMapper.selectByTemplateId(id));
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertDossierTemplate(DossierTemplateDetail template)
    {
        String userName = currentUser();
        fillTemplateDefaults(template);
        ensureId(template);
        template.setCreatedBy(userName);
        template.setUpdatedBy(userName);
        checkTemplateCodeUnique(template);
        int rows = templateMapper.insertDossierTemplate(template);
        insertChildren(template, userName);
        if (Integer.valueOf(1).equals(template.getIsDefault()))
        {
            setDefaultTemplate(template.getId());
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDossierTemplate(DossierTemplateDetail template)
    {
        if (!hasText(template.getId()))
        {
            throw new ServiceException("template id is required");
        }
        DossierTemplate old = templateMapper.selectDossierTemplateById(template.getId());
        if (old == null)
        {
            throw new ServiceException("template does not exist");
        }
        String userName = currentUser();
        fillTemplateDefaults(template);
        template.setUpdatedBy(userName);
        checkTemplateCodeUnique(template);
        int rows = templateMapper.updateDossierTemplate(template);
        String[] templateIds = new String[] { template.getId() };
        dataSourceMapper.deleteByTemplateIds(templateIds);
        ruleMapper.deleteByTemplateIds(templateIds);
        paramMapper.deleteByTemplateIds(templateIds);
        chapterMapper.deleteByTemplateIds(templateIds);
        insertChildren(template, userName);
        if (Integer.valueOf(1).equals(template.getIsDefault()))
        {
            setDefaultTemplate(template.getId());
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDossierTemplateByIds(String[] templateIds)
    {
        dataSourceMapper.deleteByTemplateIds(templateIds);
        ruleMapper.deleteByTemplateIds(templateIds);
        paramMapper.deleteByTemplateIds(templateIds);
        chapterMapper.deleteByTemplateIds(templateIds);
        return templateMapper.deleteDossierTemplateByIds(templateIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DossierTemplateDetail copyDossierTemplate(String id)
    {
        DossierTemplateDetail source = selectExistingTemplate(id);
        String templateCode = nextCopyTemplateCode(source.getTemplateCode(), source.getTemplateVersion());
        DossierTemplateDetail target = cloneTemplateGraph(source, templateCode, source.getTemplateVersion(),
                source.getName() + "副本", "draft");
        insertDossierTemplate(target);
        return selectDossierTemplateById(target.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DossierTemplateDetail createDossierTemplateVersion(String id)
    {
        DossierTemplateDetail source = selectExistingTemplate(id);
        String nextVersion = nextTemplateVersion(source.getTemplateVersion());
        while (templateMapper.selectDossierTemplateByCodeAndVersion(source.getTemplateCode(), nextVersion) != null)
        {
            nextVersion = nextTemplateVersion(nextVersion);
        }
        DossierTemplateDetail target = cloneTemplateGraph(source, source.getTemplateCode(), nextVersion, source.getName(), "draft");
        insertDossierTemplate(target);
        return selectDossierTemplateById(target.getId());
    }

    @Override
    public Map<String, Object> checkDossierTemplate(String id)
    {
        DossierTemplateDetail detail = selectExistingTemplate(id);
        List<Map<String, Object>> issues = new ArrayList<>();
        List<DossierTemplateChapter> chapters = defaultList(detail.getChapters());
        List<DossierTemplateDataSource> dataSources = defaultList(detail.getDataSources());
        List<DossierTemplateRule> rules = defaultList(detail.getRules());
        List<DossierTemplateParam> params = defaultList(detail.getParams());

        if (!hasText(detail.getTemplateCode()) || !hasText(detail.getTemplateVersion()) || !hasText(detail.getName()))
        {
            issues.add(issue("error", "基本信息", "模板编码、版本和名称必须完整。", "补齐模板基本信息后再启用模板。"));
        }
        if (!"aircraft".equals(detail.getApplicableObjectType()))
        {
            issues.add(issue("warning", "适用对象", "当前阶段只建设整机综合卷宗模板。", "适用对象建议保持为 aircraft。"));
        }
        if (chapters.isEmpty())
        {
            issues.add(issue("error", "目录结构", "模板尚未配置目录。", "初始化整机目录和系统、子系统、设备、组件、零件下钻目录。"));
        }

        Set<String> chapterIds = new HashSet<>();
        Map<String, String> chapterObjectLevels = new HashMap<>();
        Set<String> objectLevels = new HashSet<>();
        for (DossierTemplateChapter chapter : chapters)
        {
            chapterIds.add(chapter.getId());
            String objectLevel = readJsonText(chapter.getAttrsJson(), "objectLevel");
            chapterObjectLevels.put(chapter.getId(), objectLevel);
            if (hasText(objectLevel))
            {
                objectLevels.add(objectLevel);
            }
            checkChapterDisplay(chapter, issues);
        }
        requireLevel(objectLevels, issues, "aircraft", "整机目录");
        requireLevel(objectLevels, issues, "system", "系统下钻目录");
        requireLevel(objectLevels, issues, "subsystem", "子系统下钻目录");
        requireLevel(objectLevels, issues, "equipment", "设备下钻目录");
        requireLevel(objectLevels, issues, "component", "组件下钻目录");
        requireLevel(objectLevels, issues, "part", "零件下钻目录");

        Set<String> sourceChapterIds = new HashSet<>();
        for (DossierTemplateDataSource dataSource : dataSources)
        {
            if (isEnabled(dataSource.getEnabledFlag()))
            {
                sourceChapterIds.add(dataSource.getChapterId());
            }
            if (!chapterIds.contains(dataSource.getChapterId()))
            {
                issues.add(issue("warning", "数据来源", dataSource.getSourceName() + " 未绑定有效目录。",
                        "重新选择所属目录，避免生成控制台无法定位数据。"));
            }
            if (chapterIds.contains(dataSource.getChapterId()) && isEnabled(dataSource.getEnabledFlag()))
            {
                String chapterObjectLevel = chapterObjectLevels.get(dataSource.getChapterId());
                if (hasText(chapterObjectLevel) && hasText(dataSource.getApplyObjectType())
                        && !"template_group".equals(chapterObjectLevel)
                        && !chapterObjectLevel.equals(dataSource.getApplyObjectType()))
                {
                    issues.add(issue("warning", dataSource.getSourceName(), "数据来源适用对象与所属目录层级不一致。",
                            "将 applyObjectType 调整为 " + chapterObjectLevel + "，或确认该来源确需跨层级聚合。"));
                }
                if (!hasText(dataSource.getSourceTable()))
                {
                    issues.add(issue("error", dataSource.getSourceName(), "数据来源缺少业务表名。",
                            "补齐 sourceTable，生成服务才能按来源配置读取业务数据。"));
                }
            }
        }

        Set<String> ruleChapterIds = new HashSet<>();
        for (DossierTemplateRule rule : rules)
        {
            if (isEnabled(rule.getEnabledFlag()) && hasText(rule.getChapterId()))
            {
                ruleChapterIds.add(rule.getChapterId());
            }
            if (hasText(rule.getChapterId()) && !chapterIds.contains(rule.getChapterId()))
            {
                issues.add(issue("warning", "检查规则", rule.getRuleName() + " 未绑定有效目录。",
                        "重新选择所属目录或改为模板级规则。"));
            }
        }

        for (DossierTemplateChapter chapter : chapters)
        {
            if (isContentChapter(chapter) && isEnabled(chapter.getEnabledFlag()) && !sourceChapterIds.contains(chapter.getId()))
            {
                if (isRequired(chapter.getRequiredFlag()))
                {
                    issues.add(issue("error", chapter.getChapterName(), "必填目录缺少启用的数据来源。",
                            "至少配置一条可用的数据来源。"));
                }
                else
                {
                    issues.add(issue("warning", chapter.getChapterName(), "启用目录缺少数据来源。",
                            "如果该目录需要呈现全生命周期数据，应配置业务来源；如果只是占位目录，请在章节说明中明确。"));
                }
            }
            if (isContentChapter(chapter) && isEnabled(chapter.getEnabledFlag()) && isRequired(chapter.getRequiredFlag())
                    && !ruleChapterIds.contains(chapter.getId()))
            {
                issues.add(issue("warning", chapter.getChapterName(), "必填目录尚未配置检查规则。",
                        "为关键目录补充必填、关联或状态检查。"));
            }
        }

        requireParam(params, issues, "keep_empty_chapter", "空目录处理");
        requireParam(params, issues, "regenerate_strategy", "重新生成策略");
        requireParam(params, issues, "snapshot_enabled", "数据快照");
        requireParam(params, issues, "missing_data_policy", "缺失数据处理");
        requireParam(params, issues, "default_export_format", "默认导出格式");
        requireParam(params, issues, "auto_attach_files", "文件自动挂接");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("templateId", detail.getId());
        result.put("templateCode", detail.getTemplateCode());
        result.put("templateVersion", detail.getTemplateVersion());
        result.put("chapterCount", chapters.size());
        result.put("dataSourceCount", dataSources.size());
        result.put("ruleCount", rules.size());
        result.put("paramCount", params.size());
        result.put("errorCount", countIssues(issues, "error"));
        result.put("warningCount", countIssues(issues, "warning"));
        result.put("issues", issues);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int setDefaultTemplate(String id)
    {
        DossierTemplate template = templateMapper.selectDossierTemplateById(id);
        if (template == null)
        {
            throw new ServiceException("template does not exist");
        }
        String userName = currentUser();
        template.setUpdatedBy(userName);
        templateMapper.clearDefaultTemplate(template);
        return templateMapper.updateTemplateDefault(id, 1, userName);
    }

    @Override
    public int updateTemplateStatus(String id, String status)
    {
        if (!hasText(status))
        {
            throw new ServiceException("status is required");
        }
        return templateMapper.updateTemplateStatus(id, status, currentUser());
    }

    private void insertChildren(DossierTemplateDetail template, String userName)
    {
        prepareChapters(template.getChapters(), template.getId(), userName);
        prepareDataSources(template.getDataSources(), template.getId(), userName);
        prepareRules(template.getRules(), template.getId(), userName);
        prepareParams(template.getParams(), template.getId(), userName);
        if (template.getChapters() != null && !template.getChapters().isEmpty())
        {
            chapterMapper.batchInsert(template.getChapters());
        }
        if (template.getDataSources() != null && !template.getDataSources().isEmpty())
        {
            dataSourceMapper.batchInsert(template.getDataSources());
        }
        if (template.getRules() != null && !template.getRules().isEmpty())
        {
            ruleMapper.batchInsert(template.getRules());
        }
        if (template.getParams() != null && !template.getParams().isEmpty())
        {
            paramMapper.batchInsert(template.getParams());
        }
    }

    private void prepareChapters(List<DossierTemplateChapter> list, String templateId, String userName)
    {
        if (list == null)
        {
            return;
        }
        for (DossierTemplateChapter item : list)
        {
            ensureId(item);
            item.setTemplateId(templateId);
            item.setCreatedBy(defaultString(item.getCreatedBy(), userName));
            item.setUpdatedBy(defaultString(item.getUpdatedBy(), userName));
            item.setChapterLevel(defaultInteger(item.getChapterLevel(), 1));
            item.setNodeKind(defaultString(item.getNodeKind(), "chapter"));
            item.setSortOrder(defaultInteger(item.getSortOrder(), 0));
            item.setRequiredFlag(defaultInteger(item.getRequiredFlag(), 0));
            item.setEnabledFlag(defaultInteger(item.getEnabledFlag(), 1));
            item.setDefaultExpand(defaultInteger(item.getDefaultExpand(), 0));
            item.setCompletenessRequirement(defaultString(item.getCompletenessRequirement(), "normal"));
            item.setAttrsJson(defaultJsonObject(item.getAttrsJson()));
        }
    }

    private void prepareDataSources(List<DossierTemplateDataSource> list, String templateId, String userName)
    {
        if (list == null)
        {
            return;
        }
        for (DossierTemplateDataSource item : list)
        {
            ensureId(item);
            item.setTemplateId(templateId);
            item.setCreatedBy(defaultString(item.getCreatedBy(), userName));
            item.setUpdatedBy(defaultString(item.getUpdatedBy(), userName));
            item.setJoinConditionJson(defaultJsonObject(item.getJoinConditionJson()));
            item.setFilterConditionJson(defaultJsonObject(item.getFilterConditionJson()));
            item.setSupplyModeScope(defaultString(item.getSupplyModeScope(), "all"));
            item.setKeyPartScope(defaultString(item.getKeyPartScope(), "all"));
            item.setRequiredFlag(defaultInteger(item.getRequiredFlag(), 0));
            item.setEnabledFlag(defaultInteger(item.getEnabledFlag(), 1));
            item.setSortOrder(defaultInteger(item.getSortOrder(), 0));
            item.setAttrsJson(defaultJsonObject(item.getAttrsJson()));
        }
    }

    private void prepareRules(List<DossierTemplateRule> list, String templateId, String userName)
    {
        if (list == null)
        {
            return;
        }
        for (DossierTemplateRule item : list)
        {
            ensureId(item);
            item.setTemplateId(templateId);
            item.setCreatedBy(defaultString(item.getCreatedBy(), userName));
            item.setUpdatedBy(defaultString(item.getUpdatedBy(), userName));
            item.setRuleExpressionJson(defaultJsonObject(item.getRuleExpressionJson()));
            item.setSeverity(defaultString(item.getSeverity(), "warning"));
            item.setSupplyModeScope(defaultString(item.getSupplyModeScope(), "all"));
            item.setKeyPartScope(defaultString(item.getKeyPartScope(), "all"));
            item.setRequiredFlag(defaultInteger(item.getRequiredFlag(), 1));
            item.setEnabledFlag(defaultInteger(item.getEnabledFlag(), 1));
            item.setSortOrder(defaultInteger(item.getSortOrder(), 0));
        }
    }

    private void prepareParams(List<DossierTemplateParam> list, String templateId, String userName)
    {
        if (list == null)
        {
            return;
        }
        for (DossierTemplateParam item : list)
        {
            ensureId(item);
            item.setTemplateId(templateId);
            item.setCreatedBy(defaultString(item.getCreatedBy(), userName));
            item.setUpdatedBy(defaultString(item.getUpdatedBy(), userName));
            item.setParamType(defaultString(item.getParamType(), "string"));
            item.setOptionJson(defaultJsonArray(item.getOptionJson()));
            item.setRequiredFlag(defaultInteger(item.getRequiredFlag(), 0));
            item.setEditableFlag(defaultInteger(item.getEditableFlag(), 1));
            item.setEnabledFlag(defaultInteger(item.getEnabledFlag(), 1));
            item.setSortOrder(defaultInteger(item.getSortOrder(), 0));
        }
    }

    private void fillTemplateDefaults(DossierTemplate template)
    {
        template.setTemplateVersion(defaultString(template.getTemplateVersion(), "V1.0"));
        template.setTemplateType(defaultString(template.getTemplateType(), "general"));
        template.setApplicableObjectType(defaultString(template.getApplicableObjectType(), "aircraft"));
        template.setChapterTreeJson(defaultJsonObject(template.getChapterTreeJson()));
        template.setValidationRulesJson(defaultJsonObject(template.getValidationRulesJson()));
        template.setDefaultGeneratorParamsJson(defaultJsonObject(template.getDefaultGeneratorParamsJson()));
        template.setStatus(defaultString(template.getStatus(), "active"));
        template.setIsDefault(defaultInteger(template.getIsDefault(), 0));
    }

    private void checkTemplateCodeUnique(DossierTemplate template)
    {
        DossierTemplate exists = templateMapper.selectDossierTemplateByCodeAndVersion(template.getTemplateCode(),
                template.getTemplateVersion());
        if (exists != null && !exists.getId().equals(template.getId()))
        {
            throw new ServiceException("template code and version already exist");
        }
    }

    private DossierTemplateDetail selectExistingTemplate(String id)
    {
        DossierTemplateDetail detail = selectDossierTemplateById(id);
        if (detail == null)
        {
            throw new ServiceException("template does not exist");
        }
        return detail;
    }

    private DossierTemplateDetail cloneTemplateGraph(DossierTemplateDetail source, String templateCode,
            String templateVersion, String name, String status)
    {
        DossierTemplateDetail target = new DossierTemplateDetail();
        BeanUtils.copyProperties(source, target);
        target.setId(newId());
        target.setTemplateCode(templateCode);
        target.setTemplateVersion(templateVersion);
        target.setName(name);
        target.setStatus(status);
        target.setIsDefault(0);
        target.setCreatedAt(null);
        target.setUpdatedAt(null);
        target.setCreatedBy(null);
        target.setUpdatedBy(null);

        Map<String, String> chapterIdMap = new HashMap<>();
        List<DossierTemplateChapter> chapters = new ArrayList<>();
        for (DossierTemplateChapter sourceChapter : defaultList(source.getChapters()))
        {
            DossierTemplateChapter targetChapter = new DossierTemplateChapter();
            BeanUtils.copyProperties(sourceChapter, targetChapter);
            targetChapter.setId(newId());
            targetChapter.setTemplateId(target.getId());
            targetChapter.setCreatedAt(null);
            targetChapter.setUpdatedAt(null);
            targetChapter.setCreatedBy(null);
            targetChapter.setUpdatedBy(null);
            chapterIdMap.put(sourceChapter.getId(), targetChapter.getId());
            chapters.add(targetChapter);
        }
        for (DossierTemplateChapter chapter : chapters)
        {
            if (hasText(chapter.getParentId()))
            {
                chapter.setParentId(chapterIdMap.get(chapter.getParentId()));
            }
        }
        target.setChapters(chapters);

        List<DossierTemplateDataSource> dataSources = new ArrayList<>();
        for (DossierTemplateDataSource sourceDataSource : defaultList(source.getDataSources()))
        {
            DossierTemplateDataSource targetDataSource = new DossierTemplateDataSource();
            BeanUtils.copyProperties(sourceDataSource, targetDataSource);
            targetDataSource.setId(newId());
            targetDataSource.setTemplateId(target.getId());
            targetDataSource.setChapterId(chapterIdMap.get(sourceDataSource.getChapterId()));
            targetDataSource.setCreatedAt(null);
            targetDataSource.setUpdatedAt(null);
            targetDataSource.setCreatedBy(null);
            targetDataSource.setUpdatedBy(null);
            dataSources.add(targetDataSource);
        }
        target.setDataSources(dataSources);

        List<DossierTemplateRule> rules = new ArrayList<>();
        for (DossierTemplateRule sourceRule : defaultList(source.getRules()))
        {
            DossierTemplateRule targetRule = new DossierTemplateRule();
            BeanUtils.copyProperties(sourceRule, targetRule);
            targetRule.setId(newId());
            targetRule.setTemplateId(target.getId());
            if (hasText(sourceRule.getChapterId()))
            {
                targetRule.setChapterId(chapterIdMap.get(sourceRule.getChapterId()));
            }
            targetRule.setCreatedAt(null);
            targetRule.setUpdatedAt(null);
            targetRule.setCreatedBy(null);
            targetRule.setUpdatedBy(null);
            rules.add(targetRule);
        }
        target.setRules(rules);

        List<DossierTemplateParam> params = new ArrayList<>();
        for (DossierTemplateParam sourceParam : defaultList(source.getParams()))
        {
            DossierTemplateParam targetParam = new DossierTemplateParam();
            BeanUtils.copyProperties(sourceParam, targetParam);
            targetParam.setId(newId());
            targetParam.setTemplateId(target.getId());
            if (hasText(sourceParam.getChapterId()))
            {
                targetParam.setChapterId(chapterIdMap.get(sourceParam.getChapterId()));
            }
            targetParam.setCreatedAt(null);
            targetParam.setUpdatedAt(null);
            targetParam.setCreatedBy(null);
            targetParam.setUpdatedBy(null);
            params.add(targetParam);
        }
        target.setParams(params);

        return target;
    }

    private String nextCopyTemplateCode(String templateCode, String templateVersion)
    {
        String baseCode = safeCode(templateCode, 93) + "-COPY";
        String code = baseCode;
        int index = 2;
        while (templateMapper.selectDossierTemplateByCodeAndVersion(code, templateVersion) != null)
        {
            code = safeCode(baseCode, 98) + index;
            index++;
        }
        return code;
    }

    private String nextTemplateVersion(String version)
    {
        Matcher matcher = VERSION_PATTERN.matcher(defaultString(version, "V1.0").trim());
        if (!matcher.matches())
        {
            return "V1.0";
        }
        int major = Integer.parseInt(matcher.group(1));
        String minorText = matcher.group(2);
        if (minorText == null)
        {
            return "V" + (major + 1) + ".0";
        }
        int minor = Integer.parseInt(minorText);
        return "V" + major + "." + (minor + 1);
    }

    private String safeCode(String code, int maxLength)
    {
        String value = defaultString(code, "TPL-DOSSIER");
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }

    private String readJsonText(String json, String field)
    {
        if (!hasText(json))
        {
            return null;
        }
        try
        {
            JsonNode node = OBJECT_MAPPER.readTree(json);
            JsonNode value = node.get(field);
            return value == null || value.isNull() ? null : value.asText();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private void checkChapterDisplay(DossierTemplateChapter chapter, List<Map<String, Object>> issues)
    {
        if (!isContentChapter(chapter) || !isEnabled(chapter.getEnabledFlag()))
        {
            return;
        }
        String displayType = readJsonText(chapter.getAttrsJson(), "displayType");
        if (!hasText(displayType))
        {
            issues.add(issue("warning", chapter.getChapterName(), "目录缺少展示形式配置。",
                    "在展示配置中选择概要卡片、结构树、时间线、件号卡片或文件清单。"));
            return;
        }

        String chapterCode = defaultString(chapter.getChapterCode(), "").toUpperCase();
        if (isCompositionChapter(chapterCode) && !"tree_table".equals(displayType))
        {
            issues.add(issue("warning", chapter.getChapterName(), "组成类目录建议使用结构树 + 明细表。",
                    "构型 / BOM、系统结构、子系统结构、组成设备、组成结构、组成零件和装配关系应展示上下级结构。"));
        }
        if (isFileChapter(chapterCode) && !"file_list".equals(displayType))
        {
            issues.add(issue("warning", chapter.getChapterName(), "附件证明类目录建议使用文件清单。",
                    "附件材料和证明附件应优先展示文件编号、标题、状态、来源和存储地址。"));
        }
        if (isTimelineChapter(chapterCode) && !"timeline_files".equals(displayType))
        {
            issues.add(issue("warning", chapter.getChapterName(), "过程履历类目录建议使用时间线 + 附件清单。",
                    "制造、装配、装机、服役、维修和故障目录应按事件时间组织全生命周期记录。"));
        }
        if (isPartCardChapter(chapterCode) && !"part_card_param_table".equals(displayType))
        {
            issues.add(issue("warning", chapter.getChapterName(), "零件关键目录建议使用件号卡片 + 参数表。",
                    "零件基本信息和零件设计数据需要突出件号、实物序列号、关键参数和来源文件。"));
        }
        if (chapterCode.contains("INTERFACE") && !jsonArrayContains(chapter.getAttrsJson(), "blocks", "relation"))
        {
            issues.add(issue("warning", chapter.getChapterName(), "接口关系目录缺少关系展示块。",
                    "接口关系应至少包含 relation 展示块，便于展示来源节点、目标节点和接口状态。"));
        }
    }

    private boolean isCompositionChapter(String chapterCode)
    {
        return chapterCode.contains("_BOM") || chapterCode.contains("_STRUCTURE")
                || chapterCode.contains("_EQUIPMENT") || chapterCode.contains("_PARTS")
                || chapterCode.contains("_ASSEMBLY_REL");
    }

    private boolean isFileChapter(String chapterCode)
    {
        return chapterCode.contains("ATTACHMENTS") || chapterCode.contains("CERT");
    }

    private boolean isTimelineChapter(String chapterCode)
    {
        return chapterCode.contains("MANUFACTURING") || chapterCode.contains("TRACE")
                || chapterCode.contains("ASSEMBLY_RECORD") || chapterCode.contains("INSTALL")
                || chapterCode.contains("SERVICE") || chapterCode.contains("MAINTENANCE")
                || chapterCode.contains("FAULT");
    }

    private boolean isPartCardChapter(String chapterCode)
    {
        return "PART_BASIC".equals(chapterCode) || "PART_DESIGN".equals(chapterCode);
    }

    private boolean jsonArrayContains(String json, String field, String expected)
    {
        if (!hasText(json))
        {
            return false;
        }
        try
        {
            JsonNode value = OBJECT_MAPPER.readTree(json).get(field);
            if (value == null || !value.isArray())
            {
                return false;
            }
            for (JsonNode item : value)
            {
                if (expected.equals(item.asText()))
                {
                    return true;
                }
            }
        }
        catch (Exception e)
        {
            return false;
        }
        return false;
    }

    private void requireLevel(Set<String> objectLevels, List<Map<String, Object>> issues, String level, String label)
    {
        if (!objectLevels.contains(level))
        {
            issues.add(issue("error", "目录结构", label + "缺失。", "按开发设计说明补齐整机与节点下钻目录。"));
        }
    }

    private void requireParam(List<DossierTemplateParam> params, List<Map<String, Object>> issues, String code,
            String label)
    {
        for (DossierTemplateParam param : params)
        {
            if (code.equals(param.getParamCode()) && isEnabled(param.getEnabledFlag()))
            {
                return;
            }
        }
        issues.add(issue("warning", "生成设置", label + "未配置。", "在生成设置中补齐默认生成参数。"));
    }

    private Map<String, Object> issue(String severity, String subject, String message, String suggestion)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("severity", severity);
        item.put("subject", subject);
        item.put("message", message);
        item.put("suggestion", suggestion);
        return item;
    }

    private int countIssues(List<Map<String, Object>> issues, String severity)
    {
        int count = 0;
        for (Map<String, Object> issue : issues)
        {
            if (severity.equals(issue.get("severity")))
            {
                count++;
            }
        }
        return count;
    }

    private boolean isEnabled(Integer value)
    {
        return value == null || Integer.valueOf(1).equals(value);
    }

    private boolean isRequired(Integer value)
    {
        return Integer.valueOf(1).equals(value);
    }

    private boolean isContentChapter(DossierTemplateChapter chapter)
    {
        return chapter != null && "chapter".equals(chapter.getNodeKind());
    }

    private <T> List<T> defaultList(List<T> list)
    {
        return list == null ? new ArrayList<>() : list;
    }

    private void ensureId(DossierEntity entity)
    {
        if (!hasText(entity.getId()))
        {
            entity.setId(newId());
        }
    }

    private String newId()
    {
        return UUID.randomUUID().toString();
    }

    private String currentUser()
    {
        try
        {
            return SecurityUtils.getUsername();
        }
        catch (Exception e)
        {
            return "system";
        }
    }

    private String defaultString(String value, String defaultValue)
    {
        return hasText(value) ? value : defaultValue;
    }

    private Integer defaultInteger(Integer value, Integer defaultValue)
    {
        return value == null ? defaultValue : value;
    }

    private String defaultJsonObject(String value)
    {
        return defaultString(value, "{}");
    }

    private String defaultJsonArray(String value)
    {
        return defaultString(value, "[]");
    }

    private boolean hasText(String value)
    {
        return value != null && !value.trim().isEmpty();
    }
}
