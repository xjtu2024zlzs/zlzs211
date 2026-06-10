package com.ruoyi.project1.service.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class PresetScenarioDataFactoryTest
{
    @Test
    void loadsCompleteGroundTruthBySourceSystem()
    {
        PresetScenarioDataFactory factory = PresetScenarioDataFactory.loadDefault();

        assertEquals(88, factory.mappingsFor("PLM").size());
        assertEquals(63, factory.mappingsFor("ERP").size());
        assertEquals(119, factory.mappingsFor("MES").size());
        assertEquals(121, factory.mappingsFor("QMS").size());
        assertEquals(62, factory.mappingsFor("MRO").size());
    }

    @Test
    void visibleCandidateProfilesHaveOrderedDistinctMetricsAndReviewMix()
    {
        PresetScenarioDataFactory factory = PresetScenarioDataFactory.loadDefault();
        List<BigDecimal> f1Scores = new ArrayList<>();

        for (PresetScenarioDataFactory.CandidateProfile profile : PresetScenarioDataFactory.candidateProfiles())
        {
            PresetScenarioDataFactory.GeneratedResult result = factory.generateCandidate("ERP", profile);
            f1Scores.add(result.metrics().f1Score());

            assertTrue(result.rows().size() >= factory.mappingsFor("ERP").size());
            assertEquals(6, result.metrics().f1Score().scale());
            assertFalse(result.metrics().f1Score().toPlainString().endsWith("0"));
        }

        for (int i = 1; i < f1Scores.size(); i++)
        {
            assertTrue(f1Scores.get(i).compareTo(f1Scores.get(i - 1)) > 0);
        }

        PresetScenarioDataFactory.GeneratedResult weakest =
                factory.generateCandidate("ERP", PresetScenarioDataFactory.candidateProfiles().get(0));
        Set<String> statuses = weakest.rows().stream()
                .map(PresetScenarioDataFactory.GeneratedRow::reviewStatus)
                .collect(Collectors.toSet());
        assertTrue(statuses.contains("auto_approved"));
        assertTrue(statuses.contains("approved"));
        assertTrue(statuses.contains("rejected"));
        assertTrue(statuses.contains("pending"));
    }
}
