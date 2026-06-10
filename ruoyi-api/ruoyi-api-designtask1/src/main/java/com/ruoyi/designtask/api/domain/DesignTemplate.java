package com.ruoyi.designtask.api.domain;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

public class DesignTemplate implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long templateId;
    private String templateName;
    private String templateDesc;
    private String templateConfig;
    private String isDefault;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    public String getTemplateDesc() { return templateDesc; }
    public void setTemplateDesc(String templateDesc) { this.templateDesc = templateDesc; }
    public String getTemplateConfig() { return templateConfig; }
    public void setTemplateConfig(String templateConfig) { this.templateConfig = templateConfig; }
    public String getIsDefault() { return isDefault; }
    public void setIsDefault(String isDefault) { this.isDefault = isDefault; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}