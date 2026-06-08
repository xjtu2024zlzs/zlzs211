package com.ruoyi.project1.dossier.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DossierTemplateParam extends DossierEntity
{
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "templateId is required")
    private String templateId;

    private String chapterId;

    @NotBlank(message = "paramCode is required")
    @Size(max = 100, message = "paramCode length must not exceed 100")
    private String paramCode;

    @NotBlank(message = "paramName is required")
    @Size(max = 200, message = "paramName length must not exceed 200")
    private String paramName;

    private String paramType;
    private String paramValue;
    private String defaultValue;
    private String optionJson;
    private Integer requiredFlag;
    private Integer editableFlag;
    private Integer enabledFlag;
    private Integer sortOrder;
    private String paramDesc;

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

    public String getParamCode()
    {
        return paramCode;
    }

    public void setParamCode(String paramCode)
    {
        this.paramCode = paramCode;
    }

    public String getParamName()
    {
        return paramName;
    }

    public void setParamName(String paramName)
    {
        this.paramName = paramName;
    }

    public String getParamType()
    {
        return paramType;
    }

    public void setParamType(String paramType)
    {
        this.paramType = paramType;
    }

    public String getParamValue()
    {
        return paramValue;
    }

    public void setParamValue(String paramValue)
    {
        this.paramValue = paramValue;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    public String getOptionJson()
    {
        return optionJson;
    }

    public void setOptionJson(String optionJson)
    {
        this.optionJson = optionJson;
    }

    public Integer getRequiredFlag()
    {
        return requiredFlag;
    }

    public void setRequiredFlag(Integer requiredFlag)
    {
        this.requiredFlag = requiredFlag;
    }

    public Integer getEditableFlag()
    {
        return editableFlag;
    }

    public void setEditableFlag(Integer editableFlag)
    {
        this.editableFlag = editableFlag;
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

    public String getParamDesc()
    {
        return paramDesc;
    }

    public void setParamDesc(String paramDesc)
    {
        this.paramDesc = paramDesc;
    }
}
