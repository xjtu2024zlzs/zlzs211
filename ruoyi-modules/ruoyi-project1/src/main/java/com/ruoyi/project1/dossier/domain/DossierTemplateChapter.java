package com.ruoyi.project1.dossier.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DossierTemplateChapter extends DossierEntity
{
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "templateId is required")
    private String templateId;

    private String parentId;

    @NotBlank(message = "chapterCode is required")
    @Size(max = 100, message = "chapterCode length must not exceed 100")
    private String chapterCode;

    @NotBlank(message = "chapterName is required")
    @Size(max = 300, message = "chapterName length must not exceed 300")
    private String chapterName;

    @NotNull(message = "chapterLevel is required")
    private Integer chapterLevel;

    private String chapterPath;
    private String nodeKind;
    private Integer sortOrder;
    private Integer requiredFlag;
    private Integer enabledFlag;
    private Integer defaultExpand;
    private String completenessRequirement;
    private String chapterDesc;
    private String attrsJson;

    public String getTemplateId()
    {
        return templateId;
    }

    public void setTemplateId(String templateId)
    {
        this.templateId = templateId;
    }

    public String getParentId()
    {
        return parentId;
    }

    public void setParentId(String parentId)
    {
        this.parentId = parentId;
    }

    public String getChapterCode()
    {
        return chapterCode;
    }

    public void setChapterCode(String chapterCode)
    {
        this.chapterCode = chapterCode;
    }

    public String getChapterName()
    {
        return chapterName;
    }

    public void setChapterName(String chapterName)
    {
        this.chapterName = chapterName;
    }

    public Integer getChapterLevel()
    {
        return chapterLevel;
    }

    public void setChapterLevel(Integer chapterLevel)
    {
        this.chapterLevel = chapterLevel;
    }

    public String getChapterPath()
    {
        return chapterPath;
    }

    public void setChapterPath(String chapterPath)
    {
        this.chapterPath = chapterPath;
    }

    public String getNodeKind()
    {
        return nodeKind;
    }

    public void setNodeKind(String nodeKind)
    {
        this.nodeKind = nodeKind;
    }

    public Integer getSortOrder()
    {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder)
    {
        this.sortOrder = sortOrder;
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

    public Integer getDefaultExpand()
    {
        return defaultExpand;
    }

    public void setDefaultExpand(Integer defaultExpand)
    {
        this.defaultExpand = defaultExpand;
    }

    public String getCompletenessRequirement()
    {
        return completenessRequirement;
    }

    public void setCompletenessRequirement(String completenessRequirement)
    {
        this.completenessRequirement = completenessRequirement;
    }

    public String getChapterDesc()
    {
        return chapterDesc;
    }

    public void setChapterDesc(String chapterDesc)
    {
        this.chapterDesc = chapterDesc;
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
