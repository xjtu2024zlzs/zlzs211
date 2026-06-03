package com.ruoyi.project3.domain.partquality;

import java.util.ArrayList;
import java.util.List;

public class PartQualityImportData
{
    private List<DesignQuality> designRows = new ArrayList<>();
    private List<ManufacturingQuality> manufacturingRows = new ArrayList<>();
    private List<ServiceQuality> serviceRows = new ArrayList<>();
    private List<PartQualityImportError> errors = new ArrayList<>();

    public List<DesignQuality> getDesignRows() { return designRows; }
    public void setDesignRows(List<DesignQuality> designRows) { this.designRows = designRows; }
    public List<ManufacturingQuality> getManufacturingRows() { return manufacturingRows; }
    public void setManufacturingRows(List<ManufacturingQuality> manufacturingRows) { this.manufacturingRows = manufacturingRows; }
    public List<ServiceQuality> getServiceRows() { return serviceRows; }
    public void setServiceRows(List<ServiceQuality> serviceRows) { this.serviceRows = serviceRows; }
    public List<PartQualityImportError> getErrors() { return errors; }
    public void setErrors(List<PartQualityImportError> errors) { this.errors = errors; }
}
