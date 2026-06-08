package com.ruoyi.project1.dossier.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.project1.dossier.domain.DossierTemplateParam;

public interface DossierTemplateParamMapper
{
    public List<DossierTemplateParam> selectByTemplateId(String templateId);

    public int batchInsert(@Param("list") List<DossierTemplateParam> list);

    public int deleteByTemplateIds(@Param("templateIds") String[] templateIds);
}
