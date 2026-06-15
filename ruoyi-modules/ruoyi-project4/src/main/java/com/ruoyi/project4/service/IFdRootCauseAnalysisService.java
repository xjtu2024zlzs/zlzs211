package com.ruoyi.project4.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.project4.domain.FdRootCauseAnalysis;

/**
 * 鏁呴殰璇婃柇-鏍瑰洜鍒嗘瀽缁撴灉Service鎺ュ彛
 *
 * @author ruoyi
 * @date 2026-05-29
 */
public interface IFdRootCauseAnalysisService
{
    /**
     * 鏌ヨ鏁呴殰璇婃柇-鏍瑰洜鍒嗘瀽缁撴灉
     *
     * @param analysisId 鏍瑰洜鍒嗘瀽ID
     * @return 鏁呴殰璇婃柇-鏍瑰洜鍒嗘瀽缁撴灉
     */
    public FdRootCauseAnalysis selectFdRootCauseAnalysisByAnalysisId(Long analysisId);

    /**
     * 鏌ヨ鏁呴殰璇婃柇-鏍瑰洜鍒嗘瀽缁撴灉鍒楄〃
     *
     * @param fdRootCauseAnalysis 鏁呴殰璇婃柇-鏍瑰洜鍒嗘瀽缁撴灉
     * @return 鏁呴殰璇婃柇-鏍瑰洜鍒嗘瀽缁撴灉闆嗗悎
     */
    public List<FdRootCauseAnalysis> selectFdRootCauseAnalysisList(FdRootCauseAnalysis fdRootCauseAnalysis);
    /**
     * 鏌ヨ鏍瑰洜鍒嗘瀽缃俊搴︾粺璁?
     *
     * @param fdRootCauseAnalysis 鏍瑰洜鍒嗘瀽鏌ヨ鏉′欢
     * @return 鏍瑰洜鍒嗘瀽缃俊搴︾粺璁＄粨鏋?
     */
    public Map<String, Object> selectConfidenceStats(FdRootCauseAnalysis fdRootCauseAnalysis);
    /**
     * 鏂板鏁呴殰璇婃柇-鏍瑰洜鍒嗘瀽缁撴灉
     *
     * @param fdRootCauseAnalysis 鏁呴殰璇婃柇-鏍瑰洜鍒嗘瀽缁撴灉
     * @return 缁撴灉
     */
    public int insertFdRootCauseAnalysis(FdRootCauseAnalysis fdRootCauseAnalysis);

    /**
     * 淇敼鏁呴殰璇婃柇-鏍瑰洜鍒嗘瀽缁撴灉
     *
     * @param fdRootCauseAnalysis 鏁呴殰璇婃柇-鏍瑰洜鍒嗘瀽缁撴灉
     * @return 缁撴灉
     */
    public int updateFdRootCauseAnalysis(FdRootCauseAnalysis fdRootCauseAnalysis);

    /**
     * 鎵归噺鍒犻櫎鏁呴殰璇婃柇-鏍瑰洜鍒嗘瀽缁撴灉
     *
     * @param analysisIds 闇€瑕佸垹闄ょ殑鏍瑰洜鍒嗘瀽ID闆嗗悎
     * @return 缁撴灉
     */
    public int deleteFdRootCauseAnalysisByAnalysisIds(Long[] analysisIds);

    /**
     * 鍒犻櫎鏁呴殰璇婃柇-鏍瑰洜鍒嗘瀽缁撴灉淇℃伅
     *
     * @param analysisId 鏍瑰洜鍒嗘瀽ID
     * @return 缁撴灉
     */
    public int deleteFdRootCauseAnalysisByAnalysisId(Long analysisId);
}
