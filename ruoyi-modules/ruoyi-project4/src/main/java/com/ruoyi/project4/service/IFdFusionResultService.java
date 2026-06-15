package com.ruoyi.project4.service;

import com.ruoyi.project4.domain.FdFusionResult;

import java.util.List;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.project4.domain.dto.FusionRunDto;
/**
 * 鏁呴殰璇婃柇-铻嶅悎寰佺粨鏋淪ervice鎺ュ彛
 * 
 * @author ruoyi
 * @date 2026-05-29
 */
public interface IFdFusionResultService 
{
    /**
     * 鏌ヨ鏁呴殰璇婃柇-铻嶅悎寰佺粨鏋?
     * 
     * @param fusionId 鏁呴殰璇婃柇-铻嶅悎寰佺粨鏋滀富閿?
     * @return 鏁呴殰璇婃柇-铻嶅悎寰佺粨鏋?
     */
    public FdFusionResult selectFdFusionResultByFusionId(Long fusionId);

    /**
     * 鏌ヨ鏁呴殰璇婃柇-铻嶅悎寰佺粨鏋滃垪琛?
     * 
     * @param fdFusionResult 鏁呴殰璇婃柇-铻嶅悎寰佺粨鏋?
     * @return 鏁呴殰璇婃柇-铻嶅悎寰佺粨鏋滈泦鍚?
     */
    public List<FdFusionResult> selectFdFusionResultList(FdFusionResult fdFusionResult);

    /**
     * 鏂板鏁呴殰璇婃柇-铻嶅悎寰佺粨鏋?
     * 
     * @param fdFusionResult 鏁呴殰璇婃柇-铻嶅悎寰佺粨鏋?
     * @return 缁撴灉
     */
    public int insertFdFusionResult(FdFusionResult fdFusionResult);

    /**
     * 淇敼鏁呴殰璇婃柇-铻嶅悎寰佺粨鏋?
     * 
     * @param fdFusionResult 鏁呴殰璇婃柇-铻嶅悎寰佺粨鏋?
     * @return 缁撴灉
     */
    public int updateFdFusionResult(FdFusionResult fdFusionResult);

    /**
     * 鎵归噺鍒犻櫎鏁呴殰璇婃柇-铻嶅悎寰佺粨鏋?
     * 
     * @param fusionIds 闇€瑕佸垹闄ょ殑鏁呴殰璇婃柇-铻嶅悎寰佺粨鏋滀富閿泦鍚?
     * @return 缁撴灉
     */
    public int deleteFdFusionResultByFusionIds(Long[] fusionIds);

    /**
     * 鍒犻櫎鏁呴殰璇婃柇-铻嶅悎寰佺粨鏋滀俊鎭?
     * 
     * @param fusionId 鏁呴殰璇婃柇-铻嶅悎寰佺粨鏋滀富閿?
     * @return 缁撴灉
     */
    public int deleteFdFusionResultByFusionId(Long fusionId);

    /**
     * 鎵ц鐗瑰緛铻嶅悎绠楁硶
     *
     * @param dto 鐗瑰緛铻嶅悎杩愯鍙傛暟
     * @return 铻嶅悎缁撴灉
     */
    AjaxResult runFusion(FusionRunDto dto);
}

