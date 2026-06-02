package com.ruoyi.topic5.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 追溯案例对象 t5_part_instance
 * 
 * @author ruoyi
 * @date 2026-05-31
 */
public class T5PartInstance extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 零部件实例ID */
    private Long partId;

    /** 零件编号 */
    @Excel(name = "零件编号")
    private String partNo;

    /** 零件名称 */
    @Excel(name = "零件名称")
    private String partName;

    /** 零件序列号 */
    @Excel(name = "零件序列号")
    private String serialNo;

    /** 批次号 */
    @Excel(name = "批次号")
    private String batchNo;

    /** 产品型号 */
    @Excel(name = "产品型号")
    private String productModel;

    /** 所属系统 */
    @Excel(name = "所属系统")
    private String systemName;

    /** 所属子系统 */
    @Excel(name = "所属子系统")
    private String subsystemName;

    /** 材料牌号/材料规格 */
    @Excel(name = "材料牌号/材料规格")
    private String materialSpec;

    /** 供应商名称 */
    @Excel(name = "供应商名称")
    private String supplierName;

    /** 安装位置 */
    @Excel(name = "安装位置")
    private String installPosition;

    /** 父级部件ID */
    @Excel(name = "父级部件ID")
    private Long parentPartId;

    /** 生命周期状态：0制造中，1已装配，2服役中，3维修中，4退役 */
    @Excel(name = "生命周期状态：0制造中，1已装配，2服役中，3维修中，4退役")
    private String lifecycleStatus;

    public void setPartId(Long partId) 
    {
        this.partId = partId;
    }

    public Long getPartId() 
    {
        return partId;
    }

    public void setPartNo(String partNo) 
    {
        this.partNo = partNo;
    }

    public String getPartNo() 
    {
        return partNo;
    }

    public void setPartName(String partName) 
    {
        this.partName = partName;
    }

    public String getPartName() 
    {
        return partName;
    }

    public void setSerialNo(String serialNo) 
    {
        this.serialNo = serialNo;
    }

    public String getSerialNo() 
    {
        return serialNo;
    }

    public void setBatchNo(String batchNo) 
    {
        this.batchNo = batchNo;
    }

    public String getBatchNo() 
    {
        return batchNo;
    }

    public void setProductModel(String productModel) 
    {
        this.productModel = productModel;
    }

    public String getProductModel() 
    {
        return productModel;
    }

    public void setSystemName(String systemName) 
    {
        this.systemName = systemName;
    }

    public String getSystemName() 
    {
        return systemName;
    }

    public void setSubsystemName(String subsystemName) 
    {
        this.subsystemName = subsystemName;
    }

    public String getSubsystemName() 
    {
        return subsystemName;
    }

    public void setMaterialSpec(String materialSpec) 
    {
        this.materialSpec = materialSpec;
    }

    public String getMaterialSpec() 
    {
        return materialSpec;
    }

    public void setSupplierName(String supplierName) 
    {
        this.supplierName = supplierName;
    }

    public String getSupplierName() 
    {
        return supplierName;
    }

    public void setInstallPosition(String installPosition) 
    {
        this.installPosition = installPosition;
    }

    public String getInstallPosition() 
    {
        return installPosition;
    }

    public void setParentPartId(Long parentPartId) 
    {
        this.parentPartId = parentPartId;
    }

    public Long getParentPartId() 
    {
        return parentPartId;
    }

    public void setLifecycleStatus(String lifecycleStatus) 
    {
        this.lifecycleStatus = lifecycleStatus;
    }

    public String getLifecycleStatus() 
    {
        return lifecycleStatus;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("partId", getPartId())
            .append("partNo", getPartNo())
            .append("partName", getPartName())
            .append("serialNo", getSerialNo())
            .append("batchNo", getBatchNo())
            .append("productModel", getProductModel())
            .append("systemName", getSystemName())
            .append("subsystemName", getSubsystemName())
            .append("materialSpec", getMaterialSpec())
            .append("supplierName", getSupplierName())
            .append("installPosition", getInstallPosition())
            .append("parentPartId", getParentPartId())
            .append("lifecycleStatus", getLifecycleStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
