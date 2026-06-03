package com.ruoyi.project3.domain.algorithm;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.List;
import java.util.Map;

public class FaultIdentifyStartRequest
{
    @JsonAlias({"business_object_id", "businessObjectId"})
    private String bizId;


    @JsonAlias({"import_record_id", "importRecordId"})
    private String importId;
    @JsonAlias({"objectId"})
    private String object_id;
    @JsonAlias({"algorithm_params", "algorithmParams"})
    private Map<String, Object> algoParams;

    @JsonAlias({"feature_params", "featureParams"})
    private Map<String, Object> featureParams;

    private Map<String, Object> params;

    @JsonAlias({"selected_object", "selectedObject"})
    private Map<String, Object> selected;

    private String dataSelectionMode;
    private List<Long> sampleIds;

    public String getBizId()
    {
        return bizId;
    }



    public void setBizId(String bizId)
    {
        this.bizId = bizId;
    }

    public String getImportId()
    {
        return importId;
    }
    public void setImportId(String importId)
    {
        this.importId = importId;
    }

    public String getObject_id()
    {
        return object_id;
    }

    public void setObject_id(String object_id)
    {
        this.object_id = object_id;
    }

    public Map<String, Object> getAlgoParams()
    {
        return algoParams;
    }

    public void setAlgoParams(Map<String, Object> algoParams)
    {
        this.algoParams = algoParams;
    }

    public Map<String, Object> getFeatureParams()
    {
        return featureParams;
    }

    public void setFeatureParams(Map<String, Object> featureParams)
    {
        this.featureParams = featureParams;
    }

    public Map<String, Object> getParams()
    {
        return params;
    }

    public void setParams(Map<String, Object> params)
    {
        this.params = params;
    }
    public Map<String, Object> getSelected()
    {
        return selected;
    }

    public void setSelected(Map<String, Object> selected)
    {
        this.selected = selected;
    }

    public String getDataSelectionMode()
    {
        return dataSelectionMode;
    }

    public void setDataSelectionMode(String dataSelectionMode)
    {
        this.dataSelectionMode = dataSelectionMode;
    }

    public List<Long> getSampleIds()
    {
        return sampleIds;
    }

    public void setSampleIds(List<Long> sampleIds)
    {
        this.sampleIds = sampleIds;
    }
}

