package com.ruoyi.project1.service.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DossierWriteServiceTest
{
    @Test
    void derivesRequiredDemoFieldsForKnownDossierTargets()
    {
        Map<String, Object> position = new LinkedHashMap<>();
        position.put("position_code", "POS-LG-02");

        DossierWriteService.deriveRequiredDemoFields("p1p_dossier_installed_position", position);

        assertEquals("POS-LG-02 Position", position.get("position_name"));

        Map<String, Object> qualityEvent = new LinkedHashMap<>();
        qualityEvent.put("trigger_source_no", "FB-CF-0001");

        DossierWriteService.deriveRequiredDemoFields("p1p_dossier_quality_event", qualityEvent);

        assertEquals("QE-FB-CF-0001", qualityEvent.get("event_no"));
        assertEquals("MRO", qualityEvent.get("event_source"));
        assertEquals("MRO", qualityEvent.get("event_type"));
    }
}
