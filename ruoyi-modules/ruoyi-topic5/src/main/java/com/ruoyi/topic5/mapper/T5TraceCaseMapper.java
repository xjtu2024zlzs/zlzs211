package com.ruoyi.topic5.mapper;

import java.util.List;
import com.ruoyi.topic5.domain.T5TraceCase;

/**
 * 零部件实例Mapper接口
 * 
 * @author ruoyi
 * @date 2026-05-31
 */
public interface T5TraceCaseMapper 
{
    /**
     * 查询零部件实例
     * 
     * @param caseId 零部件实例主键
     * @return 零部件实例
     */
    public T5TraceCase selectT5TraceCaseByCaseId(Long caseId);

    /**
     * 查询零部件实例列表
     * 
     * @param t5TraceCase 零部件实例
     * @return 零部件实例集合
     */
    public List<T5TraceCase> selectT5TraceCaseList(T5TraceCase t5TraceCase);

    /**
     * 新增零部件实例
     * 
     * @param t5TraceCase 零部件实例
     * @return 结果
     */
    public int insertT5TraceCase(T5TraceCase t5TraceCase);

    /**
     * 修改零部件实例
     * 
     * @param t5TraceCase 零部件实例
     * @return 结果
     */
    public int updateT5TraceCase(T5TraceCase t5TraceCase);

    /**
     * 删除零部件实例
     * 
     * @param caseId 零部件实例主键
     * @return 结果
     */
    public int deleteT5TraceCaseByCaseId(Long caseId);

    /**
     * 批量删除零部件实例
     * 
     * @param caseIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteT5TraceCaseByCaseIds(Long[] caseIds);
}
