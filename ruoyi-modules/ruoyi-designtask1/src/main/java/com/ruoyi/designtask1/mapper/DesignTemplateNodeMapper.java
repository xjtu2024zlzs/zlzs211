package com.ruoyi.designtask1.mapper;

import java.util.List;
import com.ruoyi.designtask1.domain.DesignTemplateNode;

public interface DesignTemplateNodeMapper {

    List<DesignTemplateNode> selectNodeList(DesignTemplateNode node);

    DesignTemplateNode selectNodeById(Long nodeId);

    List<DesignTemplateNode> selectNodesByTemplateId(Long templateId);

    int insertNode(DesignTemplateNode node);

    int updateNode(DesignTemplateNode node);

    int deleteNodeById(Long nodeId);

    int deleteNodesByTemplateId(Long templateId);
}