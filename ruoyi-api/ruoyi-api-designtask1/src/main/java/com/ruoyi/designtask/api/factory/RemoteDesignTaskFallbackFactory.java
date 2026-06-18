package com.ruoyi.designtask.api.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.designtask.api.RemoteDesignTaskService;
import com.ruoyi.designtask.api.domain.DesignTask;
import com.ruoyi.designtask.api.domain.DesignTemplate;

@Component
public class RemoteDesignTaskFallbackFactory implements FallbackFactory<RemoteDesignTaskService> {

    private static final Logger log = LoggerFactory.getLogger(RemoteDesignTaskFallbackFactory.class);

    @Override
    public RemoteDesignTaskService create(Throwable cause) {
        log.error("设计任务服务调用失败: {}", cause.getMessage());
        return new RemoteDesignTaskService() {
            @Override
            public R<Object> listTasks(String source) {
                return R.fail("获取任务列表失败:" + cause.getMessage());
            }
            @Override
            public R<Object> getTask(Long taskId, String source) {
                return R.fail("获取任务详情失败:" + cause.getMessage());
            }
            @Override
            public R<Object> addTask(DesignTask task, String source) {
                return R.fail("新增任务失败:" + cause.getMessage());
            }
            @Override
            public R<Object> submitTask(Long taskId, String source) {
                return R.fail("提交任务失败:" + cause.getMessage());
            }
            @Override
            public R<Object> listTemplates(String source) {
                return R.fail("获取模板列表失败:" + cause.getMessage());
            }
            @Override
            public R<Object> getTemplate(Long templateId, String source) {
                return R.fail("获取模板详情失败:" + cause.getMessage());
            }
        };
    }
}