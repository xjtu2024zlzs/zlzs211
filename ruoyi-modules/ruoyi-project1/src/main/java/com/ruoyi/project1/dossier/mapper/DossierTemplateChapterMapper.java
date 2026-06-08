package com.ruoyi.project1.dossier.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.project1.dossier.domain.DossierTemplateChapter;

public interface DossierTemplateChapterMapper
{
    public List<DossierTemplateChapter> selectByTemplateId(String templateId);

    public int batchInsert(@Param("list") List<DossierTemplateChapter> list);

    public int deleteByTemplateIds(@Param("templateIds") String[] templateIds);
}
