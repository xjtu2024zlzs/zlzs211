package com.ruoyi.project3.domain.partquality;

import java.util.Date;

public class ManufacturingQuality
{
    private String manufacturingQualityId;
    private String partInstanceId;
    private String productionOrderId;
    private String workshopId;
    private String productionLineId;
    private Date startTime;
    private Date endTime;
    private String processStatus;
    private String finalInspectionResult;
    private Integer defectCount;
    private Integer reworkCount;

    public String getManufacturingQualityId() { return manufacturingQualityId; }
    public void setManufacturingQualityId(String manufacturingQualityId) { this.manufacturingQualityId = manufacturingQualityId; }
    public String getPartInstanceId() { return partInstanceId; }
    public void setPartInstanceId(String partInstanceId) { this.partInstanceId = partInstanceId; }
    public String getProductionOrderId() { return productionOrderId; }
    public void setProductionOrderId(String productionOrderId) { this.productionOrderId = productionOrderId; }
    public String getWorkshopId() { return workshopId; }
    public void setWorkshopId(String workshopId) { this.workshopId = workshopId; }
    public String getProductionLineId() { return productionLineId; }
    public void setProductionLineId(String productionLineId) { this.productionLineId = productionLineId; }
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    public String getProcessStatus() { return processStatus; }
    public void setProcessStatus(String processStatus) { this.processStatus = processStatus; }
    public String getFinalInspectionResult() { return finalInspectionResult; }
    public void setFinalInspectionResult(String finalInspectionResult) { this.finalInspectionResult = finalInspectionResult; }
    public Integer getDefectCount() { return defectCount; }
    public void setDefectCount(Integer defectCount) { this.defectCount = defectCount; }
    public Integer getReworkCount() { return reworkCount; }
    public void setReworkCount(Integer reworkCount) { this.reworkCount = reworkCount; }
}
