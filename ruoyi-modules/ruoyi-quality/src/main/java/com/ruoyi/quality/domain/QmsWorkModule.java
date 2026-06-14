package com.ruoyi.quality.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 质量问题工作模块配置对象 qms_work_module
 * 
 * @author ruoyi
 * @date 2026-06-13
 */
public class QmsWorkModule extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 模块ID */
    private Long moduleId;

    /** 模块编码 */
    @Excel(name = "模块编码")
    private String moduleCode;

    /** 模块名称 */
    @Excel(name = "模块名称")
    private String moduleName;

    /** 模块说明 */
    @Excel(name = "模块说明")
    private String moduleDesc;

    /** 前端路由地址 */
    @Excel(name = "前端路由地址")
    private String moduleRoute;

    /** 负责人用户ID */
    @Excel(name = "负责人用户ID")
    private Long ownerUserId;

    /** 负责人姓名 */
    @Excel(name = "负责人姓名")
    private String ownerUserName;

    /** 负责人角色权限字符 */
    @Excel(name = "负责人角色权限字符")
    private String ownerRoleKey;

    /** 排序 */
    @Excel(name = "排序")
    private Long sortOrder;

    /** 状态：0正常 1停用 */
    @Excel(name = "状态：0正常 1停用")
    private String status;

    /** 删除标志：0存在 2删除 */
    private String delFlag;

    public void setModuleId(Long moduleId) 
    {
        this.moduleId = moduleId;
    }

    public Long getModuleId() 
    {
        return moduleId;
    }

    public void setModuleCode(String moduleCode) 
    {
        this.moduleCode = moduleCode;
    }

    public String getModuleCode() 
    {
        return moduleCode;
    }

    public void setModuleName(String moduleName) 
    {
        this.moduleName = moduleName;
    }

    public String getModuleName() 
    {
        return moduleName;
    }

    public void setModuleDesc(String moduleDesc) 
    {
        this.moduleDesc = moduleDesc;
    }

    public String getModuleDesc() 
    {
        return moduleDesc;
    }

    public void setModuleRoute(String moduleRoute) 
    {
        this.moduleRoute = moduleRoute;
    }

    public String getModuleRoute() 
    {
        return moduleRoute;
    }

    public void setOwnerUserId(Long ownerUserId) 
    {
        this.ownerUserId = ownerUserId;
    }

    public Long getOwnerUserId() 
    {
        return ownerUserId;
    }

    public void setOwnerUserName(String ownerUserName) 
    {
        this.ownerUserName = ownerUserName;
    }

    public String getOwnerUserName() 
    {
        return ownerUserName;
    }

    public void setOwnerRoleKey(String ownerRoleKey) 
    {
        this.ownerRoleKey = ownerRoleKey;
    }

    public String getOwnerRoleKey() 
    {
        return ownerRoleKey;
    }

    public void setSortOrder(Long sortOrder) 
    {
        this.sortOrder = sortOrder;
    }

    public Long getSortOrder() 
    {
        return sortOrder;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("moduleId", getModuleId())
            .append("moduleCode", getModuleCode())
            .append("moduleName", getModuleName())
            .append("moduleDesc", getModuleDesc())
            .append("moduleRoute", getModuleRoute())
            .append("ownerUserId", getOwnerUserId())
            .append("ownerUserName", getOwnerUserName())
            .append("ownerRoleKey", getOwnerRoleKey())
            .append("sortOrder", getSortOrder())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
