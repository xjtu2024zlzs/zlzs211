package com.ruoyi.project1.dossier.service;

import java.io.File;

public interface IDossierExportService
{
    public File exportPdf(String instanceId, String versionId);

    public File exportZip(String instanceId, String versionId);
}
