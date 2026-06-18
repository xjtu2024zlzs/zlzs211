package com.ruoyi.project3.scheduler;

import com.ruoyi.project3.config.Project3TaskSyncProperties;
import com.ruoyi.project3.service.PreventiveMaintenanceService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Project3TaskSyncScheduler
{
    private static final Logger log = LoggerFactory.getLogger(Project3TaskSyncScheduler.class);

    @Resource
    private Project3TaskSyncProperties properties;

    @Resource
    private PreventiveMaintenanceService preventiveMaintenanceService;

    @Scheduled(fixedDelayString = "${project3.task-sync.fixed-delay-ms:60000}")
    public void reconcilePreventiveMaintenanceTasks()
    {
        if (!properties.isEnabled())
        {
            return;
        }
        try
        {
            int count = preventiveMaintenanceService.reconcileActiveTasks(
                    properties.getBatchSize(),
                    properties.getActiveTimeoutMinutes(),
                    properties.getMaxRetryCount()
            );
            if (count > 0)
            {
                log.info("project3 preventive maintenance task sync reconciled {} records", count);
            }
        }
        catch (Exception e)
        {
            log.warn("project3 preventive maintenance task sync failed: {}", e.getMessage());
        }
    }
}
