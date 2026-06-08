package com.ruoyi.project1.dossier.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.project1.dossier.domain.DossierTemplateRule;

public interface DossierTemplateRuleMapper
{
    public List<DossierTemplateRule> selectByTemplateId(String templateId);

    public int batchInsert(@Param("list") List<DossierTemplateRule> list);

    public int deleteByTemplateIds(@Param("templateIds") String[] templateIds);
}
