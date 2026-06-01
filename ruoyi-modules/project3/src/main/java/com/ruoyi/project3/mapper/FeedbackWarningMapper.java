package com.ruoyi.project3.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface FeedbackWarningMapper {

    List<Map<String, Object>> partList(
            @Param("keyword") String keyword,
            @Param("risk_level") String riskLevel,
            @Param("sort_by") String sortBy,
            @Param("sort_order") String sortOrder,
            @Param("page_num") Integer pageNum,
            @Param("page_size") Integer pageSize
    );

    Long partCount(
            @Param("keyword") String keyword,
            @Param("risk_level") String riskLevel
    );

    List<Map<String, Object>> partInstanceList(
            @Param("part_template_id") String partTemplateId,
            @Param("page_num") Integer pageNum,
            @Param("page_size") Integer pageSize
    );

    Long partInstanceCount(@Param("part_template_id") String partTemplateId);

    List<Map<String, Object>> partProc(@Param("part_id") String partId);

    String partImage(@Param("part_id") String partId);

    int updPartImage(
            @Param("part_id") String partId,
            @Param("image_url") String imageUrl
    );

    List<Map<String, Object>> devList(
            @Param("keyword") String keyword,
            @Param("risk_level") String riskLevel,
            @Param("page_num") Integer pageNum,
            @Param("page_size") Integer pageSize
    );

    Long devCount(
            @Param("keyword") String keyword,
            @Param("risk_level") String riskLevel
    );

    List<Map<String, Object>> devPart(@Param("device_id") String deviceId);
}
