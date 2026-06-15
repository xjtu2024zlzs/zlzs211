package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.FdDataFile;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
@Mapper
/**
 * 故障诊断-原始数据文件Mapper接口
 * 
 * @author ruoyi
 * @date 2026-05-29
 */
public interface FdDataFileMapper 
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
     * 删除故障诊断-原始数据文件
     * 
     * @param fileId 故障诊断-原始数据文件主键
     * @return 结果
     */
    public int deleteFdDataFileByFileId(Long fileId);

    /**
     * 批量删除故障诊断-原始数据文件
     * 
     * @param fileIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteFdDataFileByFileIds(Long[] fileIds);
}
