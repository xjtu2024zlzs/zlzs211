package com.ruoyi.project1.dossier.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DossierTemplate extends DossierEntity
{
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "templateCode is required")
    @Size(max = 100, message = "templateCode length must not exceed 100")
    private String templateCode;

    @NotBlank(message = "templateVersion is required")
    @Size(max = 64, message = "templateVersion length must not exceed 64")
    private String templateVersion;

    @NotBlank(message = "name is required")
    private String name;

    private String description;

    @NotBlank(message = "templateType is required")
    @Size(max = 40, message = "templateType length must not exceed 40")
    private String templateType;

    @NotBlank(message = "applicableObjectType is required")
    @Size(max = 40, message = "applicableObjectType length must not exceed 40")
    private String applicableObjectType;

    private String chapterTreeJson;
    private String validationRulesJson;
    private String defaultGeneratorParamsJson;

    @NotBlank(message = "status is required")
    @Size(max = 24, message = "status length must not exceed 24")
    private String status;

    private Integer isDefault;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date effectiveFrom;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date effectiveTo;

    public String getTemplateCode()
    {
        return templateCode;
    }

    public void setTemplateCode(String templateCode)
    {
        this.templateCode = templateCode;
    }

    public String getTemplateVersion()
    {
        return templateVersion;
    }

    public void setTemplateVersion(String templateVersion)
    {
        this.templateVersion = templateVersion;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getTemplateType()
    {
        return templateType;
    }

    public void setTemplateType(String templateType)
    {
        this.templateType = templateType;
    }

    public String getApplicableObjectType()
    {
        return applicableObjectType;
    }

    public void setApplicableObjectType(String applicableObjectType)
    {
        this.applicableObjectType = applicableObjectType;
    }

    public String getChapterTreeJson()
    {
        return chapterTreeJson;
    }

    public void setChapterTreeJson(String chapterTreeJson)
    {
        this.chapterTreeJson = chapterTreeJson;
    }

    public String getValidationRulesJson()
    {
        return validationRulesJson;
    }

    public void setValidationRulesJson(String validationRulesJson)
    {
        this.validationRulesJson = validationRulesJson;
    }

    public String getDefaultGeneratorParamsJson()
    {
        return defaultGeneratorParamsJson;
    }

    public void setDefaultGeneratorParamsJson(String defaultGeneratorParamsJson)
    {
        this.defaultGeneratorParamsJson = defaultGeneratorParamsJson;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Integer getIsDefault()
    {
        return isDefault;
    }

    public void setIsDefault(Integer isDefault)
    {
        this.isDefault = isDefault;
    }

    public Date getEffectiveFrom()
    {
        return effectiveFrom;
    }

    public void setEffectiveFrom(Date effectiveFrom)
    {
        this.effectiveFrom = effectiveFrom;
    }

    public Date getEffectiveTo()
    {
        return effectiveTo;
    }

    public void setEffectiveTo(Date effectiveTo)
    {
        this.effectiveTo = effectiveTo;
    }
}
