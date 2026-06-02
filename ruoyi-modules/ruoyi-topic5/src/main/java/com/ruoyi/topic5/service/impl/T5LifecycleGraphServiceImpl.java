package com.ruoyi.topic5.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.topic5.mapper.T5LifecycleGraphMapper;
import com.ruoyi.topic5.domain.T5LifecycleGraph;
import com.ruoyi.topic5.service.IT5LifecycleGraphService;
import java.util.Map;
import java.util.HashMap;
import com.ruoyi.common.core.web.domain.AjaxResult;




/**测试图网络代码
 *
 */
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.web.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.topic5.domain.T5TraceCase;
import com.ruoyi.topic5.domain.T5DossierSampleRecord;
import com.ruoyi.topic5.domain.T5LifecycleGraph;
import com.ruoyi.topic5.domain.T5LifecycleNode;
import com.ruoyi.topic5.domain.T5LifecycleEdge;
import com.ruoyi.topic5.mapper.T5TraceCaseMapper;
import com.ruoyi.topic5.mapper.T5DossierSampleRecordMapper;
import com.ruoyi.topic5.mapper.T5LifecycleGraphMapper;
import com.ruoyi.topic5.mapper.T5LifecycleNodeMapper;
import com.ruoyi.topic5.mapper.T5LifecycleEdgeMapper;







/**
 * 全生命周期关联模型Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
@Service
public class T5LifecycleGraphServiceImpl implements IT5LifecycleGraphService 
{
    @Autowired
    private T5LifecycleGraphMapper t5LifecycleGraphMapper;

    /**
     * 查询全生命周期关联模型
     * 
     * @param graphId 全生命周期关联模型主键
     * @return 全生命周期关联模型
     */
    @Override
    public T5LifecycleGraph selectT5LifecycleGraphByGraphId(Long graphId)
    {
        return t5LifecycleGraphMapper.selectT5LifecycleGraphByGraphId(graphId);
    }

    /**
     * 查询全生命周期关联模型列表
     * 
     * @param t5LifecycleGraph 全生命周期关联模型
     * @return 全生命周期关联模型
     */
    @Override
    public List<T5LifecycleGraph> selectT5LifecycleGraphList(T5LifecycleGraph t5LifecycleGraph)
    {
        return t5LifecycleGraphMapper.selectT5LifecycleGraphList(t5LifecycleGraph);
    }

    /**
     * 新增全生命周期关联模型
     * 
     * @param t5LifecycleGraph 全生命周期关联模型
     * @return 结果
     */
    @Override
    public int insertT5LifecycleGraph(T5LifecycleGraph t5LifecycleGraph)
    {
        t5LifecycleGraph.setCreateTime(DateUtils.getNowDate());
        return t5LifecycleGraphMapper.insertT5LifecycleGraph(t5LifecycleGraph);
    }

    /**
     * 修改全生命周期关联模型
     * 
     * @param t5LifecycleGraph 全生命周期关联模型
     * @return 结果
     */
    @Override
    public int updateT5LifecycleGraph(T5LifecycleGraph t5LifecycleGraph)
    {
        t5LifecycleGraph.setUpdateTime(DateUtils.getNowDate());
        return t5LifecycleGraphMapper.updateT5LifecycleGraph(t5LifecycleGraph);
    }

    /**
     * 批量删除全生命周期关联模型
     * 
     * @param graphIds 需要删除的全生命周期关联模型主键
     * @return 结果
     */
    @Override
    public int deleteT5LifecycleGraphByGraphIds(Long[] graphIds)
    {
        return t5LifecycleGraphMapper.deleteT5LifecycleGraphByGraphIds(graphIds);
    }

    /**
     * 删除全生命周期关联模型信息
     * 
     * @param graphId 全生命周期关联模型主键
     * @return 结果
     */
    @Override
    public int deleteT5LifecycleGraphByGraphId(Long graphId)
    {
        return t5LifecycleGraphMapper.deleteT5LifecycleGraphByGraphId(graphId);
    }

    /**
     * 构建全生命周期关联图模型
     */
//    @Override
//    public AjaxResult buildLifecycleGraph(Long caseId, Long algorithmId)
//    {
//        return AjaxResult.error("全生命周期关联图模型构建逻辑尚未实现");
//    }
    /**
     * 构建全生命周期关联图模型
     *
     * 当前版本为模拟构图算法：
     * 1. 根据追溯案例ID读取故障零件ID；
     * 2. 根据零件ID读取模拟数字卷宗；
     * 3. 构建零件节点、生命周期阶段节点、卷宗记录节点和参数节点；
     * 4. 写入图模型主表、节点表和关系边表；
     * 5. 返回 graphId，供前端进行图网络展示。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult buildLifecycleGraph(Long caseId, Long algorithmId)
    {
        // 1. 查询追溯案例
        T5TraceCase traceCase = t5TraceCaseMapper.selectT5TraceCaseByCaseId(caseId);
        if (traceCase == null)
        {
            return AjaxResult.error("追溯案例不存在");
        }

        if (traceCase.getFinalPartId() == null)
        {
            return AjaxResult.error("当前案例尚未完成故障零件定位，请先完成第一个模块");
        }

        // 2. 查询该零件对应的模拟数字卷宗数据
        T5DossierSampleRecord recordQuery = new T5DossierSampleRecord();
        recordQuery.setPartId(traceCase.getFinalPartId());

        List<T5DossierSampleRecord> recordList =
                t5DossierSampleRecordMapper.selectT5DossierSampleRecordList(recordQuery);

        if (recordList == null || recordList.isEmpty())
        {
            return AjaxResult.error("当前故障零件没有对应的模拟数字卷宗数据，请先插入 t5_dossier_sample_record 样例数据");
        }

        // 3. 新建图模型主记录
        T5LifecycleGraph graph = new T5LifecycleGraph();
        graph.setGraphNo("LCG-" + System.currentTimeMillis());
        graph.setGraphName(traceCase.getFinalPartName() + "全生命周期关联模型");
        graph.setCaseId(caseId);
        graph.setPartId(traceCase.getFinalPartId());
        graph.setPartNo(traceCase.getFinalPartNo());
        graph.setPartName(traceCase.getFinalPartName());
        graph.setBuildAlgorithmId(algorithmId);
        graph.setBuildAlgorithmName("全生命周期关联建模模拟算法");
        graph.setGraphType("lifecycle");
        graph.setBuildStatus("1");
        graph.setNodeCount(0l);
        graph.setEdgeCount(0l);
        graph.setCreateTime(DateUtils.getNowDate());

        t5LifecycleGraphMapper.insertT5LifecycleGraph(graph);

        Long graphId = graph.getGraphId();
        if (graphId == null)
        {
            return AjaxResult.error("图模型主表已插入，但 graphId 未回填，请检查 T5LifecycleGraphMapper.xml 的 useGeneratedKeys 配置");
        }

        int nodeCount = 0;
        int edgeCount = 0;

        // 4. 创建零件中心节点
        T5LifecycleNode partNode = new T5LifecycleNode();
        partNode.setGraphId(graphId);
        partNode.setNodeCode("PART-" + traceCase.getFinalPartId());
        partNode.setNodeName(traceCase.getFinalPartName());
        partNode.setNodeType("part");
        partNode.setLifecycleStage("PART");
        partNode.setAbnormalFlag("0");
        partNode.setNodeWeight(new BigDecimal("1.0000"));
        partNode.setRiskScore(new BigDecimal("0.5000"));
        partNode.setDisplayCategory("part");
        partNode.setNodeProperties("{\"partNo\":\"" + safe(traceCase.getFinalPartNo()) + "\"}");
        partNode.setCreateTime(DateUtils.getNowDate());

        t5LifecycleNodeMapper.insertT5LifecycleNode(partNode);

        Long partNodeId = partNode.getNodeId();
        if (partNodeId == null)
        {
            return AjaxResult.error("零件节点已插入，但 nodeId 未回填，请检查 T5LifecycleNodeMapper.xml 的 useGeneratedKeys 配置");
        }

        nodeCount++;

        // 每个生命周期阶段只创建一个阶段节点
        Map<String, Long> stageNodeMap = new LinkedHashMap<>();

        // 5. 根据数字卷宗记录生成图节点和关系边
        for (T5DossierSampleRecord record : recordList)
        {
            String stage = record.getLifecycleStage();

            // 5.1 创建阶段节点
            Long stageNodeId = stageNodeMap.get(stage);
            if (stageNodeId == null)
            {
                T5LifecycleNode stageNode = new T5LifecycleNode();
                stageNode.setGraphId(graphId);
                stageNode.setNodeCode("STAGE-" + stage);
                stageNode.setNodeName(stageLabel(stage));
                stageNode.setNodeType("stage");
                stageNode.setLifecycleStage(stage);
                stageNode.setAbnormalFlag("0");
                stageNode.setNodeWeight(new BigDecimal("0.8000"));
                stageNode.setRiskScore(new BigDecimal("0.3000"));
                stageNode.setDisplayCategory("stage");
                stageNode.setCreateTime(DateUtils.getNowDate());

                t5LifecycleNodeMapper.insertT5LifecycleNode(stageNode);

                stageNodeId = stageNode.getNodeId();
                if (stageNodeId == null)
                {
                    return AjaxResult.error("阶段节点已插入，但 nodeId 未回填，请检查 T5LifecycleNodeMapper.xml 的 useGeneratedKeys 配置");
                }

                stageNodeMap.put(stage, stageNodeId);
                nodeCount++;

                // 零件节点 -> 阶段节点
                T5LifecycleEdge partStageEdge = buildEdge(
                        graphId,
                        partNodeId,
                        stageNodeId,
                        "HAS_STAGE",
                        "经历",
                        "has_stage",
                        null
                );
                t5LifecycleEdgeMapper.insertT5LifecycleEdge(partStageEdge);
                edgeCount++;
            }

            // 5.2 创建卷宗记录节点
            T5LifecycleNode recordNode = new T5LifecycleNode();
            recordNode.setGraphId(graphId);
            recordNode.setNodeCode("RECORD-" + record.getRecordId());
            recordNode.setNodeName(record.getRecordName());
            recordNode.setNodeType("record");
            recordNode.setLifecycleStage(stage);
            recordNode.setSourceRecordId(record.getRecordId());
            recordNode.setSourceTable(record.getSourceTable());
            recordNode.setAbnormalFlag(normalStatus(record.getResultStatus()));
            recordNode.setNodeWeight(new BigDecimal("0.7000"));
            recordNode.setRiskScore(riskScore(record.getRiskLevel(), record.getResultStatus()));
            recordNode.setDisplayCategory("record");
            recordNode.setNodeProperties("{\"recordType\":\"" + safe(record.getRecordType()) + "\"}");
            recordNode.setCreateTime(DateUtils.getNowDate());

            t5LifecycleNodeMapper.insertT5LifecycleNode(recordNode);

            Long recordNodeId = recordNode.getNodeId();
            if (recordNodeId == null)
            {
                return AjaxResult.error("卷宗记录节点已插入，但 nodeId 未回填，请检查 T5LifecycleNodeMapper.xml 的 useGeneratedKeys 配置");
            }

            nodeCount++;

            // 阶段节点 -> 记录节点
            T5LifecycleEdge stageRecordEdge = buildEdge(
                    graphId,
                    stageNodeId,
                    recordNodeId,
                    "HAS_RECORD",
                    "包含",
                    "has_record",
                    record.getRecordId()
            );
            t5LifecycleEdgeMapper.insertT5LifecycleEdge(stageRecordEdge);
            edgeCount++;

            // 5.3 创建参数节点
            if (hasText(record.getParameterName()))
            {
                T5LifecycleNode paramNode = new T5LifecycleNode();
                paramNode.setGraphId(graphId);
                paramNode.setNodeCode("PARAM-" + record.getRecordId());
                paramNode.setNodeName(record.getParameterName() + "：" + safe(record.getParameterValue()));
                paramNode.setNodeType("parameter");
                paramNode.setLifecycleStage(stage);
                paramNode.setSourceRecordId(record.getRecordId());
                paramNode.setSourceTable(record.getSourceTable());
                paramNode.setAbnormalFlag(normalStatus(record.getResultStatus()));
                paramNode.setNodeWeight(new BigDecimal("0.6000"));
                paramNode.setRiskScore(riskScore(record.getRiskLevel(), record.getResultStatus()));
                paramNode.setDisplayCategory("parameter");
                paramNode.setNodeProperties("{\"standardValue\":\"" + safe(record.getStandardValue()) + "\"}");
                paramNode.setCreateTime(DateUtils.getNowDate());

                t5LifecycleNodeMapper.insertT5LifecycleNode(paramNode);

                Long paramNodeId = paramNode.getNodeId();
                if (paramNodeId == null)
                {
                    return AjaxResult.error("参数节点已插入，但 nodeId 未回填，请检查 T5LifecycleNodeMapper.xml 的 useGeneratedKeys 配置");
                }

                nodeCount++;

                // 记录节点 -> 参数节点
                T5LifecycleEdge recordParamEdge = buildEdge(
                        graphId,
                        recordNodeId,
                        paramNodeId,
                        "HAS_PARAMETER",
                        "关联参数",
                        "has_parameter",
                        record.getRecordId()
                );
                t5LifecycleEdgeMapper.insertT5LifecycleEdge(recordParamEdge);
                edgeCount++;
            }
        }

        // 6. 更新图模型统计信息
        graph.setNodeCount(Long.valueOf(nodeCount));
        graph.setEdgeCount(Long.valueOf(edgeCount));
        graph.setBuildStatus("2");
        graph.setBuildTime(DateUtils.getNowDate());
        graph.setGraphSummary("围绕故障零件“" + traceCase.getFinalPartName()
                + "”构建全生命周期关联图模型，共生成 "
                + nodeCount + " 个节点和 " + edgeCount + " 条关系边。");
        graph.setUpdateTime(DateUtils.getNowDate());

        t5LifecycleGraphMapper.updateT5LifecycleGraph(graph);

        // 7. 回写追溯案例
        traceCase.setGraphId(graphId);
        //traceCase.setGraphName(graph.getGraphName());
        traceCase.setCaseStatus("2");
        traceCase.setUpdateTime(DateUtils.getNowDate());

        t5TraceCaseMapper.updateT5TraceCase(traceCase);

        return AjaxResult.success(graph);
    }

    /**
     * 根据追溯案例查询最新图模型
     */
    @Override
    public T5LifecycleGraph selectLatestGraphByCaseId(Long caseId)
    {
        return null;
    }

    /**
     * 获取前端图网络展示数据
     */
//    @Override
//    public Map<String, Object> getGraphData(Long graphId)
//    {
//        Map<String, Object> result = new HashMap<>();
//        result.put("nodes", new java.util.ArrayList<>());
//        result.put("links", new java.util.ArrayList<>());
//        result.put("categories", new java.util.ArrayList<>());
//        return result;
//    }
    @Override
    public Map<String, Object> getGraphData(Long graphId)
    {
        T5LifecycleNode nodeQuery = new T5LifecycleNode();
        nodeQuery.setGraphId(graphId);
        List<T5LifecycleNode> nodeList = t5LifecycleNodeMapper.selectT5LifecycleNodeList(nodeQuery);

        T5LifecycleEdge edgeQuery = new T5LifecycleEdge();
        edgeQuery.setGraphId(graphId);
        List<T5LifecycleEdge> edgeList = t5LifecycleEdgeMapper.selectT5LifecycleEdgeList(edgeQuery);

        List<Map<String, Object>> nodes = new ArrayList<>();
        for (T5LifecycleNode node : nodeList)
        {
            Map<String, Object> item = new HashMap<>();
            item.put("id", String.valueOf(node.getNodeId()));
            item.put("name", node.getNodeName());
            item.put("category", node.getDisplayCategory() == null ? node.getNodeType() : node.getDisplayCategory());
            item.put("symbolSize", nodeSize(node));
            item.put("value", node.getRiskScore());
            item.put("raw", node);
            nodes.add(item);
        }

        List<Map<String, Object>> links = new ArrayList<>();
        for (T5LifecycleEdge edge : edgeList)
        {
            Map<String, Object> item = new HashMap<>();
            item.put("source", String.valueOf(edge.getSourceNodeId()));
            item.put("target", String.valueOf(edge.getTargetNodeId()));
            item.put("name", edge.getRelationName());
            item.put("value", edge.getEdgeWeight());
            item.put("raw", edge);
            links.add(item);
        }

        List<Map<String, Object>> categories = new ArrayList<>();
        categories.add(category("part"));
        categories.add(category("stage"));
        categories.add(category("record"));
        categories.add(category("parameter"));

        Map<String, Object> result = new HashMap<>();
        result.put("nodes", nodes);
        result.put("links", links);
        result.put("categories", categories);
        result.put("nodeList", nodeList);
        result.put("edgeList", edgeList);

        return result;
    }

    private Integer nodeSize(T5LifecycleNode node)
    {
        if ("part".equals(node.getNodeType())) return 70;
        if ("stage".equals(node.getNodeType())) return 55;
        if ("parameter".equals(node.getNodeType())) return 40;
        return 45;
    }

    private Map<String, Object> category(String name)
    {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        return map;
    }










    /**测试图网络代码
     *
     */
    @Autowired
    private T5TraceCaseMapper t5TraceCaseMapper;

    @Autowired
    private T5DossierSampleRecordMapper t5DossierSampleRecordMapper;

    @Autowired
    private T5LifecycleNodeMapper t5LifecycleNodeMapper;

    @Autowired
    private T5LifecycleEdgeMapper t5LifecycleEdgeMapper;
    /**
     * 构造图关系边
     */
    private T5LifecycleEdge buildEdge(Long graphId,
                                      Long sourceNodeId,
                                      Long targetNodeId,
                                      String relationCode,
                                      String relationName,
                                      String relationType,
                                      Long sourceRecordId)
    {
        T5LifecycleEdge edge = new T5LifecycleEdge();
        edge.setGraphId(graphId);
        edge.setSourceNodeId(sourceNodeId);
        edge.setTargetNodeId(targetNodeId);
        edge.setRelationCode(relationCode);
        edge.setRelationName(relationName);
        edge.setRelationType(relationType);
        edge.setEdgeWeight(new BigDecimal("0.8000"));
        edge.setConfidence(new BigDecimal("0.9000"));
        edge.setSourceRecordId(sourceRecordId);
        edge.setCreateTime(DateUtils.getNowDate());
        return edge;
    }

    /**
     * 生命周期阶段中文名
     */
    private String stageLabel(String stage)
    {
        if ("DESIGN".equals(stage)) return "设计阶段";
        if ("MATERIAL".equals(stage)) return "材料阶段";
        if ("MANUFACTURING".equals(stage)) return "制造阶段";
        if ("INSPECTION".equals(stage)) return "检测阶段";
        if ("ASSEMBLY".equals(stage)) return "装配阶段";
        if ("OPERATION".equals(stage)) return "服役阶段";
        if ("MAINTENANCE".equals(stage)) return "维修阶段";
        if ("PART".equals(stage)) return "故障零件";
        return stage;
    }

    /**
     * 根据风险等级和异常状态生成风险分数
     */
    private BigDecimal riskScore(String riskLevel, String resultStatus)
    {
        if ("1".equals(resultStatus))
        {
            if ("high".equalsIgnoreCase(riskLevel)) return new BigDecimal("0.9000");
            if ("medium".equalsIgnoreCase(riskLevel)) return new BigDecimal("0.7000");
            return new BigDecimal("0.6000");
        }
        return new BigDecimal("0.3000");
    }

    /**
     * 结果状态空值兜底
     */
    private String normalStatus(String resultStatus)
    {
        return resultStatus == null ? "0" : resultStatus;
    }

    /**
     * 字符串非空判断
     */
    private boolean hasText(String value)
    {
        return value != null && value.trim().length() > 0;
    }

    /**
     * JSON 字符串安全处理
     */
    private String safe(String value)
    {
        return value == null ? "" : value.replace("\"", "\\\"");
    }




}
