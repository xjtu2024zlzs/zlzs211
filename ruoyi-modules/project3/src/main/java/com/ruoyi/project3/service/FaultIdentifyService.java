package com.ruoyi.project3.service;

import com.ruoyi.project3.domain.PageRows;
import com.ruoyi.project3.domain.algorithm.DegradationTaskRequest;
import com.ruoyi.project3.domain.algorithm.FaultIdentifyStartRequest;

import java.util.Map;

public interface FaultIdentifyService {

    PageRows get_import_record(String keyword, Integer page_num, Integer page_size);

    PageRows get_identify_result(String keyword, String taskType, String status, Integer page_num, Integer page_size);

    PageRows get_feature_result(String keyword, String status, Integer page_num, Integer page_size);


    PageRows get_degradation_result(String keyword, String status, Integer page_num, Integer page_size);

    PageRows get_key_process_result(String keyword, String status, String targetType, String targetId, Integer page_num, Integer page_size);

    Map<String, Object> start_analysis(FaultIdentifyStartRequest request_data);

    Map<String, Object> get_task(String task_id);

    Map<String, Object> delete_identify_result(String flowTaskId, Map<String, Object> options);

    Map<String, Object> submit_feature(String request_body);

    Map<String, Object> submit_degradation(DegradationTaskRequest request);

    Map<String, Object> submit_key_process(FaultIdentifyStartRequest request);

    Map<String, Object> cancel_key_process(String taskId);
}
