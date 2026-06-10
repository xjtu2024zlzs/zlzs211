package com.ruoyi.designtask1.mapper;

import java.util.List;
import com.ruoyi.designtask1.domain.DesignTemplateNodeRole;

public interface DesignTemplateNodeRoleMapper {

    List<DesignTemplateNodeRole> selectRoleList(DesignTemplateNodeRole role);

    List<DesignTemplateNodeRole> selectRolesByNodeId(Long nodeId);

    int insertNodeRole(DesignTemplateNodeRole role);

    int deleteRolesByNodeId(Long nodeId);

    int deleteRolesByNodeIds(Long[] nodeIds);
}