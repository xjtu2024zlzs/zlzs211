package com.ruoyi.designtask1.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.designtask1.mapper.DesignTaskNodeMapper;
import com.ruoyi.designtask1.domain.DesignTaskNode;
import com.ruoyi.designtask1.service.IDesignTaskNodeService;

@Service
public class DesignTaskNodeServiceImpl implements IDesignTaskNodeService {

    @Autowired
    private DesignTaskNodeMapper taskNodeMapper;

    @Override
    public List<DesignTaskNode> selectTaskNodeList(DesignTaskNode taskNode) {
        return taskNodeMapper.selectTaskNodeList(taskNode);
    }

    @Override
    public DesignTaskNode selectTaskNodeById(Long id) {
        return taskNodeMapper.selectTaskNodeById(id);
    }

    @Override
    public List<DesignTaskNode> selectNodesByTaskId(Long taskId) {
        return taskNodeMapper.selectNodesByTaskId(taskId);
    }

    @Override
    @Transactional
    public int insertTaskNode(DesignTaskNode taskNode) {
        return taskNodeMapper.insertTaskNode(taskNode);
    }

    @Override
    @Transactional
    public int insertTaskNodes(List<DesignTaskNode> taskNodes) {
        return taskNodeMapper.insertTaskNodes(taskNodes);
    }

    @Override
    @Transactional
    public int updateTaskNode(DesignTaskNode taskNode) {
        return taskNodeMapper.updateTaskNode(taskNode);
    }

    @Override
    @Transactional
    public int deleteTaskNodeById(Long id) {
        return taskNodeMapper.deleteTaskNodeById(id);
    }

    @Override
    @Transactional
    public int deleteTaskNodesByTaskId(Long taskId) {
        return taskNodeMapper.deleteTaskNodesByTaskId(taskId);
    }
}