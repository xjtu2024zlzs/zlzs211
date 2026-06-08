package com.ruoyi.project1.dossier.domain;

import java.util.List;

public class DossierTemplateDetail extends DossierTemplate
{
    private static final long serialVersionUID = 1L;

    private List<DossierTemplateChapter> chapters;
    private List<DossierTemplateDataSource> dataSources;
    private List<DossierTemplateRule> rules;
    private List<DossierTemplateParam> params;

    public List<DossierTemplateChapter> getChapters()
    {
        return chapters;
    }

    public void setChapters(List<DossierTemplateChapter> chapters)
    {
        this.chapters = chapters;
    }

    public List<DossierTemplateDataSource> getDataSources()
    {
        return dataSources;
    }

    public void setDataSources(List<DossierTemplateDataSource> dataSources)
    {
        this.dataSources = dataSources;
    }

    public List<DossierTemplateRule> getRules()
    {
        return rules;
    }

    public void setRules(List<DossierTemplateRule> rules)
    {
        this.rules = rules;
    }

    public List<DossierTemplateParam> getParams()
    {
        return params;
    }

    public void setParams(List<DossierTemplateParam> params)
    {
        this.params = params;
    }
}
