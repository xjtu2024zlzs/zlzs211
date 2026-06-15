package com.ruoyi.system.service;

import com.ruoyi.system.domain.FdDataFile;

import java.util.List;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.system.domain.dto.PreprocessRunDto;
/**
 * 故障诊断-原始数据文件Service接口
 * 
 * @author ruoyi
 * @date 2026-05-29
 */
public interface IFdDataFileService 
{
    /**
     * 查询故障诊断-原始数据文件
     * 
     * @param fileId 故障诊断-原始数据文件主键
     * @return 故障诊断-原始数据文件
     */
    public FdDataFile selectFdDataFileByFileId(Long fileId);

    /**
     * 查询故障诊断-原始数据文件列表
     * 
     * @param fdDataFile 故障诊断-原始数据文件
     * @return 故障诊断-原始数据文件集合
     */
    public List<FdDataFile> selectFdDataFileList(FdDataFile fdDataFile);

    /**
     * 新增故障诊断-原始数据文件
     * 
     * @param fdDataFile 故障诊断-原始数据文件
     * @return 结果
     */
    public int insertFdDataFile(FdDataFile fdDataFile);

    /**
     * 修改故障诊断-原始数据文件
     * 
     * @param fdDataFile 故障诊断-原始数据文件
     * @return 结果
     */
    public int updateFdDataFile(FdDataFile fdDataFile);

    /**
     * 批量删除故障诊断-原始数据文件
     * 
     * @param fileIds 需要删除的故障诊断-原始数据文件主键集合
     * @return 结果
     */
    public int deleteFdDataFileByFileIds(Long[] fileIds);

    /**
     * 删除故障诊断-原始数据文件信息
     * 
     * @param fileId 故障诊断-原始数据文件主键
     * @return 结果
     */
    public int deleteFdDataFileByFileId(Long fileId);

    AjaxResult runPreprocess(PreprocessRunDto dto);
    AjaxResult listRawSamples();
}
