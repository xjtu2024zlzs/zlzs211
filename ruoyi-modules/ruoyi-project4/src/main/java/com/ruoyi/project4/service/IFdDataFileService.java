package com.ruoyi.project4.service;

import com.ruoyi.project4.domain.FdDataFile;

import java.util.List;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.project4.domain.dto.PreprocessRunDto;
/**
 * 鏁呴殰璇婃柇-鍘熷鏁版嵁鏂囦欢Service鎺ュ彛
 * 
 * @author ruoyi
 * @date 2026-05-29
 */
public interface IFdDataFileService 
{
    /**
     * 鏌ヨ鏁呴殰璇婃柇-鍘熷鏁版嵁鏂囦欢
     * 
     * @param fileId 鏁呴殰璇婃柇-鍘熷鏁版嵁鏂囦欢涓婚敭
     * @return 鏁呴殰璇婃柇-鍘熷鏁版嵁鏂囦欢
     */
    public FdDataFile selectFdDataFileByFileId(Long fileId);

    /**
     * 鏌ヨ鏁呴殰璇婃柇-鍘熷鏁版嵁鏂囦欢鍒楄〃
     * 
     * @param fdDataFile 鏁呴殰璇婃柇-鍘熷鏁版嵁鏂囦欢
     * @return 鏁呴殰璇婃柇-鍘熷鏁版嵁鏂囦欢闆嗗悎
     */
    public List<FdDataFile> selectFdDataFileList(FdDataFile fdDataFile);

    /**
     * 鏂板鏁呴殰璇婃柇-鍘熷鏁版嵁鏂囦欢
     * 
     * @param fdDataFile 鏁呴殰璇婃柇-鍘熷鏁版嵁鏂囦欢
     * @return 缁撴灉
     */
    public int insertFdDataFile(FdDataFile fdDataFile);

    /**
     * 淇敼鏁呴殰璇婃柇-鍘熷鏁版嵁鏂囦欢
     * 
     * @param fdDataFile 鏁呴殰璇婃柇-鍘熷鏁版嵁鏂囦欢
     * @return 缁撴灉
     */
    public int updateFdDataFile(FdDataFile fdDataFile);

    /**
     * 鎵归噺鍒犻櫎鏁呴殰璇婃柇-鍘熷鏁版嵁鏂囦欢
     * 
     * @param fileIds 闇€瑕佸垹闄ょ殑鏁呴殰璇婃柇-鍘熷鏁版嵁鏂囦欢涓婚敭闆嗗悎
     * @return 缁撴灉
     */
    public int deleteFdDataFileByFileIds(Long[] fileIds);

    /**
     * 鍒犻櫎鏁呴殰璇婃柇-鍘熷鏁版嵁鏂囦欢淇℃伅
     * 
     * @param fileId 鏁呴殰璇婃柇-鍘熷鏁版嵁鏂囦欢涓婚敭
     * @return 缁撴灉
     */
    public int deleteFdDataFileByFileId(Long fileId);

    AjaxResult runPreprocess(PreprocessRunDto dto);
    AjaxResult listRawSamples();
}

