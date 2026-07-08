package com.ruoyi.project1.dossier.service.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class DossierDetailMapperSqlTest
{
    @Test
    void selectVersionOrdersOnlyNarrowVersionIdsBeforeReadingSnapshotJson() throws Exception
    {
        String mapperXml;
        try (InputStream input = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("mapper/project1/dossier/DossierDetailMapper.xml"))
        {
            mapperXml = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }

        String selectVersion = mapperXml.substring(mapperXml.indexOf("<select id=\"selectVersion\""),
                mapperXml.indexOf("<select id=\"selectVersionList\""));

        assertTrue(Pattern.compile("inner\\s+join\\s*\\(\\s*select\\s+id\\s+from\\s+t1_dossier_version",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(selectVersion).find());
    }
}
