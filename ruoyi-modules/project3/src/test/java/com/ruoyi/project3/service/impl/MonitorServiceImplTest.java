package com.ruoyi.project3.service.impl;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.project3.domain.ModuleNode;
import com.ruoyi.project3.domain.PageRows;
import com.ruoyi.project3.mapper.MonitorMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonitorServiceImplTest
{
    @Mock
    private MonitorMapper monitorMapper;

    @InjectMocks
    private MonitorServiceImpl service;

    @Test
    void getTreeBuildsHierarchyAndKeepsOrphanAsRoot()
    {
        when(monitorMapper.sel_all_nodes()).thenReturn(List.of(
                node("aircraft:A1", null, "Aircraft"),
                node("subsystem:S1", "aircraft:A1", "Subsystem"),
                node("equipment:E1", "missing:X", "Orphan")
        ));

        List<ModuleNode> roots = service.getTree();

        assertEquals(2, roots.size());
        assertEquals("subsystem:S1", roots.get(0).get_children().get(0).get_id());
        assertEquals("equipment:E1", roots.get(1).get_id());
    }

    @Test
    void getPartInstancesNormalizesPagination()
    {
        when(monitorMapper.sel_part_list("part_template:PT1", null, null, null, 1, 200))
                .thenReturn(List.of(Map.of("part_instance_id", "PI1")));
        when(monitorMapper.cnt_part_list("part_template:PT1", null, null, null)).thenReturn(1L);

        PageRows result = service.getPartInstances("part_template:PT1", null, null, null, 0, 500);

        assertEquals(1L, result.get_tot());
        assertEquals(1, result.get_rows().size());
    }

    @Test
    void createModuleRejectsInvalidNodeBeforeDatabaseCall()
    {
        ServiceException error = assertThrows(
                ServiceException.class,
                () -> service.createModule("invalid", "name")
        );

        assertTrue(error.getMessage().contains("当前节点不合法"));
        verify(monitorMapper, never()).sel_node_by_id(any());
    }

    @Test
    void deleteAircraftWithChildrenIsRejected()
    {
        when(monitorMapper.cnt_subs_by_air_id("A1")).thenReturn(1L);

        assertThrows(ServiceException.class, () -> service.deleteModule("aircraft:A1"));

        verify(monitorMapper, never()).del_aircraft(eq("A1"));
    }

    @Test
    void hierarchyImportRejectsEmptyAndUnsupportedFiles()
    {
        MockMultipartFile empty = new MockMultipartFile(
                "file", "hierarchy.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[0]
        );
        MockMultipartFile wrongType = new MockMultipartFile(
                "file", "hierarchy.csv", "text/csv", "a,b".getBytes()
        );

        assertThrows(ServiceException.class, () -> service.importHierarchy(empty));
        assertThrows(ServiceException.class, () -> service.importHierarchy(wrongType));
    }

    private Map<String, Object> node(String id, String parentId, String name)
    {
        return Map.of(
                "id", id,
                "parent_id", parentId == null ? "" : parentId,
                "name", name,
                "level", 1,
                "terminal", false,
                "child_count", 0,
                "part_count", 0
        );
    }
}
