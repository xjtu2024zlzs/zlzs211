package com.ruoyi.project1.domain;

/**
 * Field match review request.
 */
public class MatchReviewRequest
{
    private Long[] reviewIds;

    private String targetTable;

    private String targetColumn;

    private String remark;

    public Long[] getReviewIds()
    {
        return reviewIds;
    }

    public void setReviewIds(Long[] reviewIds)
    {
        this.reviewIds = reviewIds;
    }

    public String getTargetTable()
    {
        return targetTable;
    }

    public void setTargetTable(String targetTable)
    {
        this.targetTable = targetTable;
    }

    public String getTargetColumn()
    {
        return targetColumn;
    }

    public void setTargetColumn(String targetColumn)
    {
        this.targetColumn = targetColumn;
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
