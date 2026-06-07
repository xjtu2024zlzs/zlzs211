package com.ruoyi.project1.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * Match task version object p1p_match_task_version.
 *
 * versionId is a global primary key. versionNo is the per-task display version
 * number and should be shown as V1/V2/V3 on the page.
 */
public class MatchTaskVersion extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** Version primary key. */
    private Long versionId;

    /** Owner task id. */
    @Excel(name = "任务ID")
    private Long taskId;

    /** Per-task version number. */
    @Excel(name = "版本号")
    private Long versionNo;

    /** Source datasource id. */
    @Excel(name = "来源数据源ID")
    private Long sourceDatasourceId;

    /** Datasource schema snapshot id used by this version. */
    @Excel(name = "模式快照ID")
    private Long schemaSnapshotId;

    /** Target system key. */
    @Excel(name = "目标系统")
    private String targetKey;

    /** Encoding mode key. */
    @Excel(name = "编码模式")
    private String encodingModeKey;

    /** Embedding model key. */
    @Excel(name = "嵌入模型")
    private String embeddingModelKey;

    /** Evaluation mode key. */
    @Excel(name = "评估模式")
    private String evalModeKey;

    /** LLM config id. */
    @Excel(name = "LLM配置ID")
    private Long llmConfigId;

    /** Auto approve threshold. */
    @Excel(name = "自动通过阈值")
    private BigDecimal autoApproveThreshold;

    /** Algorithm parameters JSON. */
    @Excel(name = "算法参数JSON")
    private String algorithmParamsJson;

    /** Version change summary. */
    @Excel(name = "变更说明")
    private String changeSummary;

    /** LLM provider, transient page field. */
    private String llmProvider;

    /** LLM model, transient page field. */
    private String llmModel;

    /** Whether this is the current version, transient page field. */
    private Boolean current;

    public void setVersionId(Long versionId)
    {
        this.versionId = versionId;
    }

    public Long getVersionId()
    {
        return versionId;
    }

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
    }

    public Long getTaskId()
    {
        return taskId;
    }

    public void setVersionNo(Long versionNo)
    {
        this.versionNo = versionNo;
    }

    public Long getVersionNo()
    {
        return versionNo;
    }

    public void setSourceDatasourceId(Long sourceDatasourceId)
    {
        this.sourceDatasourceId = sourceDatasourceId;
    }

    public Long getSourceDatasourceId()
    {
        return sourceDatasourceId;
    }

    public void setSchemaSnapshotId(Long schemaSnapshotId)
    {
        this.schemaSnapshotId = schemaSnapshotId;
    }

    public Long getSchemaSnapshotId()
    {
        return schemaSnapshotId;
    }

    public void setTargetKey(String targetKey)
    {
        this.targetKey = targetKey;
    }

    public String getTargetKey()
    {
        return targetKey;
    }

    public void setEncodingModeKey(String encodingModeKey)
    {
        this.encodingModeKey = encodingModeKey;
    }

    public String getEncodingModeKey()
    {
        return encodingModeKey;
    }

    public void setEmbeddingModelKey(String embeddingModelKey)
    {
        this.embeddingModelKey = embeddingModelKey;
    }

    public String getEmbeddingModelKey()
    {
        return embeddingModelKey;
    }

    public void setEvalModeKey(String evalModeKey)
    {
        this.evalModeKey = evalModeKey;
    }

    public String getEvalModeKey()
    {
        return evalModeKey;
    }

    public void setLlmConfigId(Long llmConfigId)
    {
        this.llmConfigId = llmConfigId;
    }

    public Long getLlmConfigId()
    {
        return llmConfigId;
    }

    public void setAutoApproveThreshold(BigDecimal autoApproveThreshold)
    {
        this.autoApproveThreshold = autoApproveThreshold;
    }

    public BigDecimal getAutoApproveThreshold()
    {
        return autoApproveThreshold;
    }

    public void setAlgorithmParamsJson(String algorithmParamsJson)
    {
        this.algorithmParamsJson = algorithmParamsJson;
    }

    public String getAlgorithmParamsJson()
    {
        return algorithmParamsJson;
    }

    public void setChangeSummary(String changeSummary)
    {
        this.changeSummary = changeSummary;
    }

    public String getChangeSummary()
    {
        return changeSummary;
    }

    public void setLlmProvider(String llmProvider)
    {
        this.llmProvider = llmProvider;
    }

    public String getLlmProvider()
    {
        return llmProvider;
    }

    public void setLlmModel(String llmModel)
    {
        this.llmModel = llmModel;
    }

    public String getLlmModel()
    {
        return llmModel;
    }

    public void setCurrent(Boolean current)
    {
        this.current = current;
    }

    public Boolean getCurrent()
    {
        return current;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("versionId", getVersionId())
            .append("taskId", getTaskId())
            .append("versionNo", getVersionNo())
            .append("sourceDatasourceId", getSourceDatasourceId())
            .append("schemaSnapshotId", getSchemaSnapshotId())
            .append("targetKey", getTargetKey())
            .append("encodingModeKey", getEncodingModeKey())
            .append("embeddingModelKey", getEmbeddingModelKey())
            .append("evalModeKey", getEvalModeKey())
            .append("llmConfigId", getLlmConfigId())
            .append("autoApproveThreshold", getAutoApproveThreshold())
            .append("algorithmParamsJson", getAlgorithmParamsJson())
            .append("changeSummary", getChangeSummary())
            .append("llmProvider", getLlmProvider())
            .append("llmModel", getLlmModel())
            .append("current", getCurrent())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
