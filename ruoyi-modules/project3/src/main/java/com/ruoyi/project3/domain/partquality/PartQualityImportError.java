package com.ruoyi.project3.domain.partquality;

public class PartQualityImportError
{
    private String sheetName;
    private Integer rowIndex;
    private String fieldName;
    private String message;

    public PartQualityImportError() {}

    public PartQualityImportError(String sheetName, Integer rowIndex, String fieldName, String message)
    {
        this.sheetName = sheetName;
        this.rowIndex = rowIndex;
        this.fieldName = fieldName;
        this.message = message;
    }

    public String getSheetName() { return sheetName; }
    public void setSheetName(String sheetName) { this.sheetName = sheetName; }
    public Integer getRowIndex() { return rowIndex; }
    public void setRowIndex(Integer rowIndex) { this.rowIndex = rowIndex; }
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
