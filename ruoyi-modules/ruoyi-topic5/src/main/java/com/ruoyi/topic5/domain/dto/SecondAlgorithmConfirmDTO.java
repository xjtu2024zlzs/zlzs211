package com.ruoyi.topic5.domain.dto;

public class SecondAlgorithmConfirmDTO
{
    /** 是否保存：true保存，false驳回 */
    private Boolean saveFlag;

    /** 人工判定备注 */
    private String remark;

    public Boolean getSaveFlag()
    {
        return saveFlag;
    }

    public void setSaveFlag(Boolean saveFlag)
    {
        this.saveFlag = saveFlag;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }
}