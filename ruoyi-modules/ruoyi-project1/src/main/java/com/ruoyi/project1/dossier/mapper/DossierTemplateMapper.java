package com.ruoyi.project1.dossier.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.project1.dossier.domain.DossierTemplate;

public interface DossierTemplateMapper
{
    public List<DossierTemplate> selectDossierTemplateList(DossierTemplate template);

    public DossierTemplate selectDossierTemplateById(String id);

    public DossierTemplate selectDossierTemplateByCode(String templateCode);

    public DossierTemplate selectDossierTemplateByCodeAndVersion(@Param("templateCode") String templateCode,
            @Param("templateVersion") String templateVersion);

    public int insertDossierTemplate(DossierTemplate template);

    public int updateDossierTemplate(DossierTemplate template);

    public int deleteDossierTemplateByIds(@Param("templateIds") String[] templateIds);

    public int clearDefaultTemplate(DossierTemplate template);

    public int updateTemplateDefault(@Param("id") String id, @Param("isDefault") Integer isDefault,
            @Param("updatedBy") String updatedBy);

    public int updateTemplateStatus(@Param("id") String id, @Param("status") String status,
            @Param("updatedBy") String updatedBy);

    public List<Map<String, Object>> selectDataSourceTables(@Param("tableNames") List<String> tableNames);

    public List<Map<String, Object>> selectDataSourceColumns(@Param("tableName") String tableName);
}
