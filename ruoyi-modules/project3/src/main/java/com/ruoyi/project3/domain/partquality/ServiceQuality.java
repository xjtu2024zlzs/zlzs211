package com.ruoyi.project3.domain.partquality;

import java.math.BigDecimal;
import java.util.Date;

public class ServiceQuality
{
    private String serviceQualityId;
    private String equipmentId;
    private String componentId;
    private String installedAircraftId;
    private String installationPosition;
    private Date installationDate;
    private String serviceStatus;
    private BigDecimal totalRunningHours;
    private Integer cycleCount;
    private BigDecimal healthScore;
    private Date lastInspectionDate;
    private Date nextInspectionDate;

    public String getServiceQualityId() { return serviceQualityId; }
    public void setServiceQualityId(String serviceQualityId) { this.serviceQualityId = serviceQualityId; }
    public String getEquipmentId() { return equipmentId; }
    public void setEquipmentId(String equipmentId) { this.equipmentId = equipmentId; }
    public String getComponentId() { return componentId; }
    public void setComponentId(String componentId) { this.componentId = componentId; }
    public String getInstalledAircraftId() { return installedAircraftId; }
    public void setInstalledAircraftId(String installedAircraftId) { this.installedAircraftId = installedAircraftId; }
    public String getInstallationPosition() { return installationPosition; }
    public void setInstallationPosition(String installationPosition) { this.installationPosition = installationPosition; }
    public Date getInstallationDate() { return installationDate; }
    public void setInstallationDate(Date installationDate) { this.installationDate = installationDate; }
    public String getServiceStatus() { return serviceStatus; }
    public void setServiceStatus(String serviceStatus) { this.serviceStatus = serviceStatus; }
    public BigDecimal getTotalRunningHours() { return totalRunningHours; }
    public void setTotalRunningHours(BigDecimal totalRunningHours) { this.totalRunningHours = totalRunningHours; }
    public Integer getCycleCount() { return cycleCount; }
    public void setCycleCount(Integer cycleCount) { this.cycleCount = cycleCount; }
    public BigDecimal getHealthScore() { return healthScore; }
    public void setHealthScore(BigDecimal healthScore) { this.healthScore = healthScore; }
    public Date getLastInspectionDate() { return lastInspectionDate; }
    public void setLastInspectionDate(Date lastInspectionDate) { this.lastInspectionDate = lastInspectionDate; }
    public Date getNextInspectionDate() { return nextInspectionDate; }
    public void setNextInspectionDate(Date nextInspectionDate) { this.nextInspectionDate = nextInspectionDate; }
}
