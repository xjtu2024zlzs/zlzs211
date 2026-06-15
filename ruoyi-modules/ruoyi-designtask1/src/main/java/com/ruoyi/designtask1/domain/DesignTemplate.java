package com.ruoyi.designtask1.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.web.domain.BaseEntity;

public class DesignTemplate extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long templateId;
    private String templateName;
    private String templateDesc;
    private String templateConfig;
    private String isDefault;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

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
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("templateId", getTemplateId())
            .append("templateName", getTemplateName())
            .append("templateDesc", getTemplateDesc())
            .append("isDefault", getIsDefault())
            .append("status", getStatus())
            .toString();
    }
}