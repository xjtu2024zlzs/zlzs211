package com.ruoyi.project1.dossier.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.project1.dossier.domain.DossierTemplateDataSource;

public interface DossierTemplateDataSourceMapper
{
    public List<DossierTemplateDataSource> selectByTemplateId(String templateId);

    public int batchInsert(@Param("list") List<DossierTemplateDataSource> list);

    public int deleteByTemplateIds(@Param("templateIds") String[] templateIds);
}
