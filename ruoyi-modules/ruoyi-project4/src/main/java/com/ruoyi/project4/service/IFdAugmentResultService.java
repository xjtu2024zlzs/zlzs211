package com.ruoyi.project4.service;

import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.project4.domain.FdAugmentResult;
import com.ruoyi.project4.domain.dto.AugmentRunDto;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.project4.domain.dto.AugmentRunDto;
import java.util.List;

/**
 * 鏁呴殰璇婃柇-鏍锋湰澧炲己缁撴灉Service鎺ュ彛
 *
 * @author ruoyi
 * @date 2026-05-29
 */
public interface IFdAugmentResultService
{
    /**
     * 鏌ヨ鏁呴殰璇婃柇-鏍锋湰澧炲己缁撴灉
     *
     * @param augmentId 鏍锋湰澧炲己缁撴灉涓婚敭
     * @return 鏁呴殰璇婃柇-鏍锋湰澧炲己缁撴灉
     */
    FdAugmentResult selectFdAugmentResultByAugmentId(Long augmentId);

    /**
     * 鏌ヨ鏁呴殰璇婃柇-鏍锋湰澧炲己缁撴灉鍒楄〃
     *
     * @param fdAugmentResult 鏁呴殰璇婃柇-鏍锋湰澧炲己缁撴灉
     * @return 鏁呴殰璇婃柇-鏍锋湰澧炲己缁撴灉闆嗗悎
     */
    List<FdAugmentResult> selectFdAugmentResultList(FdAugmentResult fdAugmentResult);

    /**
     * 鏂板鏁呴殰璇婃柇-鏍锋湰澧炲己缁撴灉
     *
     * @param fdAugmentResult 鏁呴殰璇婃柇-鏍锋湰澧炲己缁撴灉
     * @return 缁撴灉
     */
    int insertFdAugmentResult(FdAugmentResult fdAugmentResult);

    /**
     * 淇敼鏁呴殰璇婃柇-鏍锋湰澧炲己缁撴灉
     *
     * @param fdAugmentResult 鏁呴殰璇婃柇-鏍锋湰澧炲己缁撴灉
     * @return 缁撴灉
     */
    int updateFdAugmentResult(FdAugmentResult fdAugmentResult);

    /**
     * 鎵归噺鍒犻櫎鏁呴殰璇婃柇-鏍锋湰澧炲己缁撴灉
     *
     * @param augmentIds 闇€瑕佸垹闄ょ殑鏍锋湰澧炲己缁撴灉涓婚敭闆嗗悎
     * @return 缁撴灉
     */
    int deleteFdAugmentResultByAugmentIds(Long[] augmentIds);

    /**
     * 鍒犻櫎鏁呴殰璇婃柇-鏍锋湰澧炲己缁撴灉淇℃伅
     *
     * @param augmentId 鏍锋湰澧炲己缁撴灉涓婚敭
     * @return 缁撴灉
     */
    int deleteFdAugmentResultByAugmentId(Long augmentId);

    /**
     * 鎵ц鏍锋湰澧炲己绠楁硶
     *
     * @param dto 鏍锋湰澧炲己杩愯鍙傛暟
     * @return 鏍锋湰澧炲己鎵ц缁撴灉
     */
    AjaxResult runAugment(AugmentRunDto dto);

}

