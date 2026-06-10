package com.ruoyi.designtask1.service;

import java.util.List;
import com.ruoyi.designtask1.domain.DesignTaskNode;

public interface IDesignTaskNodeService {

    List<DesignTaskNode> selectTaskNodeList(DesignTaskNode taskNode);

    DesignTaskNode selectTaskNodeById(Long id);

    List<DesignTaskNode> selectNodesByTaskId(Long taskId);

    int insertTaskNode(DesignTaskNode taskNode);

    int insertTaskNodes(List<DesignTaskNode> taskNodes);

    int updateTaskNode(DesignTaskNode taskNode);

    int deleteTaskNodeById(Long id);

    int deleteTaskNodesByTaskId(Long taskId);
}