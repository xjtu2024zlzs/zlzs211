package com.ruoyi.project1.dossier.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.security.annotation.RequiresLogin;
import com.ruoyi.project1.dossier.service.IDossierOpenApiService;

@RestController
@RequestMapping("/dossier/openapi")
public class DossierOpenApiController extends BaseController
{
    @Autowired
    private IDossierOpenApiService openApiService;

    @RequiresLogin
    @GetMapping("/dossiers")
    public AjaxResult dossiers(@RequestParam Map<String, Object> query)
    {
        return success(openApiService.selectDossierPage(query));
    }

    @RequiresLogin
    @GetMapping("/product-dossiers")
    public AjaxResult productDossiers(@RequestParam Map<String, Object> query)
    {
        return success(openApiService.selectDossierPage(query));
    }

    @RequiresLogin
    @GetMapping("/dossiers/{instanceId}")
    public AjaxResult dossierDetail(@PathVariable String instanceId)
    {
        return success(openApiService.selectDossierDetail(instanceId));
    }

    @RequiresLogin
    @GetMapping("/product-dossiers/{instanceId}")
    public AjaxResult productDossierDetail(@PathVariable String instanceId)
    {
        return success(openApiService.selectDossierDetail(instanceId));
    }

    @RequiresLogin
    @GetMapping("/dossiers/{instanceId}/versions")
    public AjaxResult dossierVersions(@PathVariable String instanceId)
    {
        return success(openApiService.selectDossierVersions(instanceId));
    }

    @RequiresLogin
    @GetMapping("/dossiers/{instanceId}/summary")
    public AjaxResult dossierSummary(@PathVariable String instanceId)
    {
        return success(openApiService.selectDossierSummary(instanceId));
    }

    @RequiresLogin
    @GetMapping("/products/{productId}/structure")
    public AjaxResult productStructure(@PathVariable String productId, @RequestParam Map<String, Object> query)
    {
        return success(openApiService.selectProductStructure(productId, query));
    }

    @RequiresLogin
    @GetMapping("/products/{productId}/bom")
    public AjaxResult productBom(@PathVariable String productId, @RequestParam Map<String, Object> query)
    {
        return success(openApiService.selectProductStructure(productId, query));
    }

    @RequiresLogin
    @GetMapping("/aircraft/{aircraftId}/bom/children")
    public AjaxResult bomChildren(@PathVariable String aircraftId,
            @RequestParam(required = false) String parentId)
    {
        return success(openApiService.selectBomChildren(aircraftId, parentId));
    }

    @RequiresLogin
    @GetMapping("/aircraft/{aircraftId}/bom/search")
    public AjaxResult bomSearch(@PathVariable String aircraftId, @RequestParam String keyword)
    {
        return success(openApiService.searchBomNodes(aircraftId, keyword));
    }

    @RequiresLogin
    @GetMapping("/bom/nodes/{nodeId}")
    public AjaxResult bomNode(@PathVariable String nodeId)
    {
        return success(openApiService.selectBomNode(nodeId));
    }

    @RequiresLogin
    @GetMapping("/bom/nodes/{nodeId}/path")
    public AjaxResult bomPath(@PathVariable String nodeId)
    {
        return success(openApiService.selectBomPath(nodeId));
    }

    @RequiresLogin
    @GetMapping("/bom/nodes/{nodeId}/relations")
    public AjaxResult bomRelations(@PathVariable String nodeId)
    {
        return success(openApiService.selectBomRelations(nodeId));
    }

    @RequiresLogin
    @GetMapping("/dictionaries")
    public AjaxResult dictionaries()
    {
        return success(openApiService.selectDictionaries());
    }

    @RequiresLogin
    @GetMapping("/quality-characteristics")
    public AjaxResult qualityCharacteristics()
    {
        return success(openApiService.selectQualityCharacteristics());
    }

    @RequiresLogin
    @GetMapping("/bom/nodes/{nodeId}/quality-characteristics")
    public AjaxResult nodeQualityCharacteristics(@PathVariable String nodeId)
    {
        return success(openApiService.selectNodeQualityCharacteristics(nodeId));
    }

    @RequiresLogin
    @GetMapping("/products/{productId}/quality-features")
    public AjaxResult productQualityFeatures(@PathVariable String productId, @RequestParam Map<String, Object> query)
    {
        return success(openApiService.selectProductQualityFeatures(productId, query));
    }

    @RequiresLogin
    @GetMapping("/data-standards")
    public AjaxResult dataStandards()
    {
        return success(openApiService.selectDataStandards());
    }

    @RequiresLogin
    @GetMapping("/bom/nodes/{nodeId}/design-data")
    public AjaxResult designData(@PathVariable String nodeId)
    {
        return success(openApiService.selectNodeLifecycleData(nodeId, "design"));
    }

    @RequiresLogin
    @GetMapping("/bom/nodes/{nodeId}/manufacturing-data")
    public AjaxResult manufacturingData(@PathVariable String nodeId)
    {
        return success(openApiService.selectNodeLifecycleData(nodeId, "manufacturing"));
    }

    @RequiresLogin
    @GetMapping("/manufacturing/process-data")
    public AjaxResult manufacturingProcessData(@RequestParam Map<String, Object> query)
    {
        return success(openApiService.selectManufacturingProcessData(query));
    }

    @RequiresLogin
    @GetMapping("/products/{productId}/manufacturing-process-data")
    public AjaxResult productManufacturingProcessData(@PathVariable String productId,
            @RequestParam Map<String, Object> query)
    {
        query.put("productId", productId);
        return success(openApiService.selectManufacturingProcessData(query));
    }

    @RequiresLogin
    @GetMapping("/bom/nodes/{nodeId}/inspection-records")
    public AjaxResult inspectionRecords(@PathVariable String nodeId)
    {
        return success(openApiService.selectNodeLifecycleData(nodeId, "inspection"));
    }

    @RequiresLogin
    @GetMapping("/bom/nodes/{nodeId}/installation-records")
    public AjaxResult installationRecords(@PathVariable String nodeId)
    {
        return success(openApiService.selectNodeLifecycleData(nodeId, "installation"));
    }

    @RequiresLogin
    @GetMapping("/bom/nodes/{nodeId}/service-records")
    public AjaxResult serviceRecords(@PathVariable String nodeId)
    {
        return success(openApiService.selectNodeLifecycleData(nodeId, "service"));
    }

    @RequiresLogin
    @GetMapping("/operation-maintenance/data")
    public AjaxResult operationMaintenanceData(@RequestParam Map<String, Object> query)
    {
        return success(openApiService.selectOperationMaintenanceData(query));
    }

    @RequiresLogin
    @GetMapping("/products/{productId}/operation-maintenance-data")
    public AjaxResult productOperationMaintenanceData(@PathVariable String productId,
            @RequestParam Map<String, Object> query)
    {
        query.put("productId", productId);
        return success(openApiService.selectOperationMaintenanceData(query));
    }

    @RequiresLogin
    @GetMapping("/bom/nodes/{nodeId}/fault-records")
    public AjaxResult faultRecords(@PathVariable String nodeId)
    {
        return success(openApiService.selectNodeLifecycleData(nodeId, "fault"));
    }

    @RequiresLogin
    @GetMapping("/bom/nodes/{nodeId}/files")
    public AjaxResult nodeFiles(@PathVariable String nodeId)
    {
        return success(openApiService.selectNodeFiles(nodeId));
    }

    @RequiresLogin
    @GetMapping("/files-models")
    public AjaxResult filesAndModels(@RequestParam Map<String, Object> query)
    {
        return success(openApiService.selectFilesAndModels(query));
    }

    @RequiresLogin
    @GetMapping("/products/{productId}/files-models")
    public AjaxResult productFilesAndModels(@PathVariable String productId, @RequestParam Map<String, Object> query)
    {
        query.put("productId", productId);
        return success(openApiService.selectFilesAndModels(query));
    }

    @RequiresLogin
    @PostMapping("/writeback/optimization-results")
    public AjaxResult optimizationWriteback(@RequestBody Map<String, Object> request)
    {
        return success(openApiService.writebackResult("optimization", request));
    }

    @RequiresLogin
    @PostMapping("/writeback/design-optimization")
    public AjaxResult designOptimizationWriteback(@RequestBody Map<String, Object> request)
    {
        return success(openApiService.writebackResult("design_optimization", request));
    }

    @RequiresLogin
    @PostMapping("/writeback/quality-monitoring-results")
    public AjaxResult qualityMonitoringWriteback(@RequestBody Map<String, Object> request)
    {
        return success(openApiService.writebackResult("quality_monitoring", request));
    }

    @RequiresLogin
    @PostMapping("/writeback/quality-supervision")
    public AjaxResult qualitySupervisionWriteback(@RequestBody Map<String, Object> request)
    {
        return success(openApiService.writebackResult("quality_supervision", request));
    }

    @RequiresLogin
    @PostMapping("/writeback/fault-diagnosis-results")
    public AjaxResult faultDiagnosisWriteback(@RequestBody Map<String, Object> request)
    {
        return success(openApiService.writebackResult("fault_diagnosis", request));
    }

    @RequiresLogin
    @PostMapping("/writeback/fault-diagnosis")
    public AjaxResult faultDiagnosisWritebackV2(@RequestBody Map<String, Object> request)
    {
        return success(openApiService.writebackResult("fault_diagnosis", request));
    }

    @RequiresLogin
    @PostMapping("/writeback/traceability-results")
    public AjaxResult traceabilityWriteback(@RequestBody Map<String, Object> request)
    {
        return success(openApiService.writebackResult("traceability", request));
    }

    @RequiresLogin
    @PostMapping("/writeback/traceability")
    public AjaxResult traceabilityWritebackV2(@RequestBody Map<String, Object> request)
    {
        return success(openApiService.writebackResult("traceability", request));
    }

    @RequiresLogin
    @PostMapping("/writeback/data-completion-results")
    public AjaxResult dataCompletionWriteback(@RequestBody Map<String, Object> request)
    {
        return success(openApiService.writebackResult("data_completion", request));
    }

    @RequiresLogin
    @GetMapping("/changes")
    public AjaxResult changes(@RequestParam Map<String, Object> query)
    {
        return success(openApiService.selectChangePage(query));
    }

    @RequiresLogin
    @GetMapping("/data-change/notifications")
    public AjaxResult dataChangeNotifications(@RequestParam Map<String, Object> query)
    {
        return success(openApiService.selectChangePage(query));
    }

    @RequiresLogin
    @GetMapping("/changes/{changeId}")
    public AjaxResult changeDetail(@PathVariable String changeId)
    {
        return success(openApiService.selectChangeDetail(changeId));
    }

    @RequiresLogin
    @PostMapping("/subscriptions")
    public AjaxResult createSubscription(@RequestBody Map<String, Object> request)
    {
        return success(openApiService.createSubscription(request));
    }

    @RequiresLogin
    @PostMapping("/data-change/subscriptions")
    public AjaxResult createDataChangeSubscription(@RequestBody Map<String, Object> request)
    {
        return success(openApiService.createSubscription(request));
    }

    @RequiresLogin
    @DeleteMapping("/subscriptions/{subscriptionId}")
    public AjaxResult deleteSubscription(@PathVariable String subscriptionId)
    {
        return toAjax(openApiService.deleteSubscription(subscriptionId));
    }

    @RequiresLogin
    @GetMapping("/access/current")
    public AjaxResult currentAccess()
    {
        return success(openApiService.selectAccessInfo());
    }

    @RequiresLogin
    @GetMapping("/access/check")
    public AjaxResult accessCheck(@RequestParam Map<String, Object> query)
    {
        return success(openApiService.checkAccess(query));
    }

    @RequiresLogin
    @GetMapping("/operation-logs")
    public AjaxResult operationLogs(@RequestParam Map<String, Object> query)
    {
        return success(openApiService.selectOperationLogPage(query));
    }

    @RequiresLogin
    @GetMapping("/operation-logs/{logId}")
    public AjaxResult operationLogDetail(@PathVariable String logId)
    {
        return success(openApiService.selectOperationLogDetail(logId));
    }

    @RequiresLogin
    @GetMapping("/catalog")
    public AjaxResult catalog()
    {
        return success(openApiService.selectCatalog());
    }

    @RequiresLogin
    @GetMapping("/health")
    public AjaxResult health()
    {
        return success(openApiService.selectHealth());
    }

    @RequiresLogin
    @GetMapping("/version")
    public AjaxResult version()
    {
        return success(openApiService.selectVersion());
    }
}
