package com.ruoyi.project1.dossier.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DossierTemplateRule extends DossierEntity
{
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "templateId is required")
    private String templateId;

    private String chapterId;

    @NotBlank(message = "ruleCode is required")
    @Size(max = 100, message = "ruleCode length must not exceed 100")
    private String ruleCode;

    @NotBlank(message = "ruleName is required")
    @Size(max = 200, message = "ruleName length must not exceed 200")
    private String ruleName;

    @NotBlank(message = "ruleType is required")
    private String ruleType;

    private String targetTable;
    private String targetField;
    private String targetPath;
    private String ruleExpression;
    private String ruleExpressionJson;
    private String severity;
    private String errorMessage;
    private String remediationHint;
    private String bindQualityRuleId;
    private String applyObjectType;
    private String supplyModeScope;
    private String keyPartScope;
    private Integer requiredFlag;
    private Integer enabledFlag;
    private Integer sortOrder;

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

    public String getRuleCode()
    {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode)
    {
        this.ruleCode = ruleCode;
    }

    public String getRuleName()
    {
        return ruleName;
    }

    public void setRuleName(String ruleName)
    {
        this.ruleName = ruleName;
    }

    public String getRuleType()
    {
        return ruleType;
    }

    public void setRuleType(String ruleType)
    {
        this.ruleType = ruleType;
    }

    public String getTargetTable()
    {
        return targetTable;
    }

    public void setTargetTable(String targetTable)
    {
        this.targetTable = targetTable;
    }

    public String getTargetField()
    {
        return targetField;
    }

    public void setTargetField(String targetField)
    {
        this.targetField = targetField;
    }

    public String getTargetPath()
    {
        return targetPath;
    }

    public void setTargetPath(String targetPath)
    {
        this.targetPath = targetPath;
    }

    public String getRuleExpression()
    {
        return ruleExpression;
    }

    public void setRuleExpression(String ruleExpression)
    {
        this.ruleExpression = ruleExpression;
    }

    public String getRuleExpressionJson()
    {
        return ruleExpressionJson;
    }

    public void setRuleExpressionJson(String ruleExpressionJson)
    {
        this.ruleExpressionJson = ruleExpressionJson;
    }

    public String getSeverity()
    {
        return severity;
    }

    public void setSeverity(String severity)
    {
        this.severity = severity;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    public String getRemediationHint()
    {
        return remediationHint;
    }

    public void setRemediationHint(String remediationHint)
    {
        this.remediationHint = remediationHint;
    }

    public String getBindQualityRuleId()
    {
        return bindQualityRuleId;
    }

    public void setBindQualityRuleId(String bindQualityRuleId)
    {
        this.bindQualityRuleId = bindQualityRuleId;
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
}
