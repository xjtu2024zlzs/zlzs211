package com.ruoyi.project1.dossier.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.project1.dossier.domain.DossierTemplate;
import com.ruoyi.project1.dossier.domain.DossierTemplateDetail;

public interface IDossierTemplateService
{
    public List<DossierTemplate> selectDossierTemplateList(DossierTemplate template);

    public DossierTemplateDetail selectDossierTemplateById(String id);

    public int insertDossierTemplate(DossierTemplateDetail template);

    public int updateDossierTemplate(DossierTemplateDetail template);

    public int deleteDossierTemplateByIds(String[] templateIds);

    public DossierTemplateDetail copyDossierTemplate(String id);

    public DossierTemplateDetail createDossierTemplateVersion(String id);

    public Map<String, Object> checkDossierTemplate(String id);

    public Map<String, Object> selectDataSourceMetadata(String tableName);

    public int setDefaultTemplate(String id);

    public int updateTemplateStatus(String id, String status);
}
