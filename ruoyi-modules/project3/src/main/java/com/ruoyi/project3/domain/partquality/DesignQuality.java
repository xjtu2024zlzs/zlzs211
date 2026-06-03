package com.ruoyi.project3.domain.partquality;

public class DesignQuality
{
    private String designQualityId;
    private String partTemplateId;
    private String designVersion;
    private String drawingVersion;
    private String designRequirements;
    private String functionalRequirements;
    private String toleranceRequirements;
    private String keyQualityCharacteristics;
    private String designReviewResult;
    private String verificationResult;

    public String getDesignQualityId() { return designQualityId; }
    public void setDesignQualityId(String designQualityId) { this.designQualityId = designQualityId; }
    public String getPartTemplateId() { return partTemplateId; }
    public void setPartTemplateId(String partTemplateId) { this.partTemplateId = partTemplateId; }
    public String getDesignVersion() { return designVersion; }
    public void setDesignVersion(String designVersion) { this.designVersion = designVersion; }
    public String getDrawingVersion() { return drawingVersion; }
    public void setDrawingVersion(String drawingVersion) { this.drawingVersion = drawingVersion; }
    public String getDesignRequirements() { return designRequirements; }
    public void setDesignRequirements(String designRequirements) { this.designRequirements = designRequirements; }
    public String getFunctionalRequirements() { return functionalRequirements; }
    public void setFunctionalRequirements(String functionalRequirements) { this.functionalRequirements = functionalRequirements; }
    public String getToleranceRequirements() { return toleranceRequirements; }
    public void setToleranceRequirements(String toleranceRequirements) { this.toleranceRequirements = toleranceRequirements; }
    public String getKeyQualityCharacteristics() { return keyQualityCharacteristics; }
    public void setKeyQualityCharacteristics(String keyQualityCharacteristics) { this.keyQualityCharacteristics = keyQualityCharacteristics; }
    public String getDesignReviewResult() { return designReviewResult; }
    public void setDesignReviewResult(String designReviewResult) { this.designReviewResult = designReviewResult; }
    public String getVerificationResult() { return verificationResult; }
    public void setVerificationResult(String verificationResult) { this.verificationResult = verificationResult; }
}
