package com.ruoyi.project3.domain.partquality;

import java.util.ArrayList;
import java.util.List;

public class PartQualityImportResult
{
    private boolean success;
    private int designCount;
    private int manufacturingCount;
    private int serviceCount;
    private int insertCount;
    private int updateCount;
    private List<PartQualityImportError> errors = new ArrayList<>();

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public int getDesignCount() { return designCount; }
    public void setDesignCount(int designCount) { this.designCount = designCount; }
    public int getManufacturingCount() { return manufacturingCount; }
    public void setManufacturingCount(int manufacturingCount) { this.manufacturingCount = manufacturingCount; }
    public int getServiceCount() { return serviceCount; }
    public void setServiceCount(int serviceCount) { this.serviceCount = serviceCount; }
    public int getInsertCount() { return insertCount; }
    public void setInsertCount(int insertCount) { this.insertCount = insertCount; }
    public int getUpdateCount() { return updateCount; }
    public void setUpdateCount(int updateCount) { this.updateCount = updateCount; }
    public List<PartQualityImportError> getErrors() { return errors; }
    public void setErrors(List<PartQualityImportError> errors) { this.errors = errors; }
}
