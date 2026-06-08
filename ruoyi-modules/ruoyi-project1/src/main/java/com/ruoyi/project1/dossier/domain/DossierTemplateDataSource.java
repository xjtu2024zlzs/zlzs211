package com.ruoyi.project1.dossier.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DossierTemplateDataSource extends DossierEntity
{
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "templateId is required")
    private String templateId;

    @NotBlank(message = "chapterId is required")
    private String chapterId;

    @NotBlank(message = "sourceCode is required")
    @Size(max = 100, message = "sourceCode length must not exceed 100")
    private String sourceCode;

    private String sourceSystem;

    @NotBlank(message = "sourceTable is required")
    @Size(max = 200, message = "sourceTable length must not exceed 200")
    private String sourceTable;

    @NotBlank(message = "sourceName is required")
    @Size(max = 300, message = "sourceName length must not exceed 300")
    private String sourceName;

    private String sourceDesc;
    private String lifecycleStage;
    private String sourceRecordType;
    private String joinConditionJson;
    private String filterConditionJson;
    private String applyObjectType;
    private String supplyModeScope;
    private String keyPartScope;
    private Integer requiredFlag;
    private Integer enabledFlag;
    private Integer sortOrder;
    private String attrsJson;

    public String getTemplateId()
    {
        return templateId;
    }

    public void setTemplateId(String templateId)
    {
        this.templateId = templateId;
    }

    public String getChapterId()
    {
        return chapterId;
    }

    public void setChapterId(String chapterId)
    {
        this.chapterId = chapterId;
    }

    public String getSourceCode()
    {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode)
    {
        this.sourceCode = sourceCode;
    }

    public String getSourceSystem()
    {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem)
    {
        this.sourceSystem = sourceSystem;
    }

    public String getSourceTable()
    {
        return sourceTable;
    }

    public void setSourceTable(String sourceTable)
    {
        this.sourceTable = sourceTable;
    }

    public String getSourceName()
    {
        return sourceName;
    }

    public void setSourceName(String sourceName)
    {
        this.sourceName = sourceName;
    }

    public String getSourceDesc()
    {
        return sourceDesc;
    }

    public void setSourceDesc(String sourceDesc)
    {
        this.sourceDesc = sourceDesc;
    }

    public String getLifecycleStage()
    {
        return lifecycleStage;
    }

    public void setLifecycleStage(String lifecycleStage)
    {
        this.lifecycleStage = lifecycleStage;
    }

    public String getSourceRecordType()
    {
        return sourceRecordType;
    }

    public void setSourceRecordType(String sourceRecordType)
    {
        this.sourceRecordType = sourceRecordType;
    }

    public String getJoinConditionJson()
    {
        return joinConditionJson;
    }

    public void setJoinConditionJson(String joinConditionJson)
    {
        this.joinConditionJson = joinConditionJson;
    }

    public String getFilterConditionJson()
    {
        return filterConditionJson;
    }

    public void setFilterConditionJson(String filterConditionJson)
    {
        this.filterConditionJson = filterConditionJson;
    }

    public String getApplyObjectType()
    {
        return applyObjectType;
    }

    public void setApplyObjectType(String applyObjectType)
    {
        this.applyObjectType = applyObjectType;
    }

    public String getSupplyModeScope()
    {
        return supplyModeScope;
    }

    public void setSupplyModeScope(String supplyModeScope)
    {
        this.supplyModeScope = supplyModeScope;
    }

    public String getKeyPartScope()
    {
        return keyPartScope;
    }

    public void setKeyPartScope(String keyPartScope)
    {
        this.keyPartScope = keyPartScope;
    }

    public Integer getRequiredFlag()
    {
        return requiredFlag;
    }

    public void setRequiredFlag(Integer requiredFlag)
    {
        this.requiredFlag = requiredFlag;
    }

    public Integer getEnabledFlag()
    {
        return enabledFlag;
    }

    public void setEnabledFlag(Integer enabledFlag)
    {
        this.enabledFlag = enabledFlag;
    }

    public Integer getSortOrder()
    {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder)
    {
        this.sortOrder = sortOrder;
    }

    public String getAttrsJson()
    {
        return attrsJson;
    }

    public void setAttrsJson(String attrsJson)
    {
        this.attrsJson = attrsJson;
    }
}
