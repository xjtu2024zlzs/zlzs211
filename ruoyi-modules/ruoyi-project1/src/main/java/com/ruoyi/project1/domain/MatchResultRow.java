package com.ruoyi.project1.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 模式映射结果明细对象 p1p_match_result_row
 * 
 * @author pwb
 * @date 2026-06-05
 */
public class MatchResultRow extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 结果明细主键 */
    private Long resultRowId;

    /** 结果集 ID */
    @Excel(name = "结果集 ID")
    private Long resultSetId;

    /** 结果行号 */
    @Excel(name = "结果行号")
    private Long rowNo;

    /** 源数据库名 */
    @Excel(name = "源数据库名")
    private String sourceDatabase;

    /** 源表名 */
    @Excel(name = "源表名")
    private String sourceTable;

    /** 源字段名 */
    @Excel(name = "源字段名")
    private String sourceColumn;

    /** 目标表名 */
    @Excel(name = "目标表名")
    private String targetTable;

    /** 目标字段名 */
    @Excel(name = "目标字段名")
    private String targetColumn;

    /** 匹配分数 */
    @Excel(name = "匹配分数")
    private BigDecimal score;

    /** 原始算法结果 JSON */
    @Excel(name = "原始算法结果 JSON")
    private String rawPayload;

    public void setResultRowId(Long resultRowId) 
    {
        this.resultRowId = resultRowId;
    }

    public Long getResultRowId() 
    {
        return resultRowId;
    }

    public void setResultSetId(Long resultSetId) 
    {
        this.resultSetId = resultSetId;
    }

    public Long getResultSetId() 
    {
        return resultSetId;
    }

    public void setRowNo(Long rowNo) 
    {
        this.rowNo = rowNo;
    }

    public Long getRowNo() 
    {
        return rowNo;
    }

    public void setSourceDatabase(String sourceDatabase) 
    {
        this.sourceDatabase = sourceDatabase;
    }

    public String getSourceDatabase() 
    {
        return sourceDatabase;
    }

    public void setSourceTable(String sourceTable) 
    {
        this.sourceTable = sourceTable;
    }

    public String getSourceTable() 
    {
        return sourceTable;
    }

    public void setSourceColumn(String sourceColumn) 
    {
        this.sourceColumn = sourceColumn;
    }

    public String getSourceColumn() 
    {
        return sourceColumn;
    }

    public void setTargetTable(String targetTable) 
    {
        this.targetTable = targetTable;
    }

    public String getTargetTable() 
    {
        return targetTable;
    }

    public void setTargetColumn(String targetColumn) 
    {
        this.targetColumn = targetColumn;
    }

    public String getTargetColumn() 
    {
        return targetColumn;
    }

    public void setScore(BigDecimal score) 
    {
        this.score = score;
    }

    public BigDecimal getScore() 
    {
        return score;
    }

    public void setRawPayload(String rawPayload) 
    {
        this.rawPayload = rawPayload;
    }

    public String getRawPayload() 
    {
        return rawPayload;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("resultRowId", getResultRowId())
            .append("resultSetId", getResultSetId())
            .append("rowNo", getRowNo())
            .append("sourceDatabase", getSourceDatabase())
            .append("sourceTable", getSourceTable())
            .append("sourceColumn", getSourceColumn())
            .append("targetTable", getTargetTable())
            .append("targetColumn", getTargetColumn())
            .append("score", getScore())
            .append("rawPayload", getRawPayload())
            .append("createTime", getCreateTime())
            .append("remark", getRemark())
            .toString();
    }
}
