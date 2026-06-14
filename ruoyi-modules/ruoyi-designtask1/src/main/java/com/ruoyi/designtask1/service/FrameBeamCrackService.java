package com.ruoyi.designtask1.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.designtask1.domain.DesignTask;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FrameBeamCrackService {

    public static final String TASK_TYPE = "FRAME_BEAM_CRACK_LIFE_PREDICTION";

    private final IDesignTaskService taskService;
    private final JdbcTemplate jdbcTemplate;
    private final FrameBeamPredictionClient predictionClient;
    private final FrameBeamMaintenanceAdviceService adviceService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FrameBeamCrackService(
        IDesignTaskService taskService,
        JdbcTemplate jdbcTemplate,
        FrameBeamPredictionClient predictionClient,
        FrameBeamMaintenanceAdviceService adviceService
    ) {
        this.taskService = taskService;
        this.jdbcTemplate = jdbcTemplate;
        this.predictionClient = predictionClient;
        this.adviceService = adviceService;
    }

    public Map<String, Object> crackInput(Long taskId) {
        assertFrameBeamTask(taskId);
        ensureTables();
        return firstJsonRow("select input_json from t2_frame_beam_crack_input where task_id = ?", taskId);
    }

    @Transactional
    public Map<String, Object> saveCrackInput(Long taskId, Map<String, Object> body) {
        assertFrameBeamTask(taskId);
        ensureTables();
        String json = toJson(body);
        jdbcTemplate.update("""
            insert into t2_frame_beam_crack_input(task_id, input_json, create_by, create_time, update_by, update_time)
            values (?, ?, ?, sysdate(), ?, sysdate())
            on duplicate key update input_json = values(input_json), update_by = values(update_by), update_time = sysdate()
            """, taskId, json, currentUsername(), currentUsername());
        return crackInput(taskId);
    }

    @Transactional
    public Map<String, Object> saveLoadSpectrum(Long taskId, Map<String, Object> body) {
        assertFrameBeamTask(taskId);
        ensureTables();
        String spectrumName = stringValue(body.get("spectrumName"), "frame_beam_load_spectrum");
        String fileName = stringValue(body.get("fileName"), "");
        String filePath = stringValue(body.get("filePath"), "");
        String json = toJson(body);
        jdbcTemplate.update("""
            insert into t2_frame_beam_load_spectrum(task_id, spectrum_name, file_name, file_path, spectrum_json, create_by, create_time, update_by, update_time)
            values (?, ?, ?, ?, ?, ?, sysdate(), ?, sysdate())
            on duplicate key update spectrum_name = values(spectrum_name), file_name = values(file_name),
              file_path = values(file_path), spectrum_json = values(spectrum_json), update_by = values(update_by), update_time = sysdate()
            """, taskId, spectrumName, fileName, filePath, json, currentUsername(), currentUsername());
        return loadSpectrum(taskId);
    }

    public Map<String, Object> loadSpectrum(Long taskId) {
        assertFrameBeamTask(taskId);
        ensureTables();
        return firstJsonRow("select spectrum_json from t2_frame_beam_load_spectrum where task_id = ?", taskId);
    }

    public Map<String, Object> lifePrediction(Long taskId) {
        assertFrameBeamTask(taskId);
        ensureTables();
        Map<String, Object> row = firstMap("select prediction_json from t2_frame_beam_life_prediction where task_id = ?", taskId);
        return row.isEmpty() ? defaultPrediction() : fromJson(stringValue(row.get("predictionJson"), "{}"));
    }

    @Transactional
    public Map<String, Object> runLifePrediction(Long taskId) {
        assertFrameBeamTask(taskId);
        ensureTables();
        Map<String, Object> input = crackInput(taskId);
        Map<String, Object> spectrum = loadSpectrum(taskId);
        if (input.isEmpty()) {
            throw new IllegalStateException("请先保存框梁裂纹信息。");
        }
        if (spectrum.isEmpty()) {
            throw new IllegalStateException("请先保存或上传载荷谱。");
        }

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("taskId", taskId);
        request.put("crackInput", input);
        request.put("loadSpectrum", spectrum);

        Map<String, Object> prediction;
        try {
            prediction = predictionClient.predictGrowth(request);
        } catch (Exception e) {
            prediction = new LinkedHashMap<>();
            prediction.put("status", "FAILED");
            prediction.put("errorMessage", "Frame beam surrogate service unavailable: " + e.getMessage());
        }

        Map<String, Object> advice = adviceService.buildAdvice(prediction);
        prediction.put("maintenanceAdvice", advice);
        upsertPrediction(taskId, prediction);
        upsertAdvice(taskId, advice, "SYSTEM_GENERATED");
        return prediction;
    }

    @Transactional
    public Map<String, Object> confirmAdvice(Long taskId, Map<String, Object> body) {
        assertFrameBeamTask(taskId);
        ensureTables();
        Map<String, Object> advice = new LinkedHashMap<>(body == null ? Collections.emptyMap() : body);
        if (advice.isEmpty()) {
            advice.putAll(adviceService.buildAdvice(lifePrediction(taskId)));
        }
        advice.put("confirmed", true);
        advice.put("confirmedBy", currentUsername());
        advice.put("confirmedTime", now());
        upsertAdvice(taskId, advice, "CONFIRMED");
        return advice(taskId);
    }

    public Map<String, Object> advice(Long taskId) {
        assertFrameBeamTask(taskId);
        ensureTables();
        Map<String, Object> row = firstMap("select advice_json from t2_frame_beam_maintenance_advice where task_id = ?", taskId);
        return row.isEmpty() ? Collections.emptyMap() : fromJson(stringValue(row.get("adviceJson"), "{}"));
    }

    private void upsertPrediction(Long taskId, Map<String, Object> prediction) {
        jdbcTemplate.update("""
            insert into t2_frame_beam_life_prediction(task_id, status, risk_level, prediction_json, create_by, create_time, update_by, update_time)
            values (?, ?, ?, ?, ?, sysdate(), ?, sysdate())
            on duplicate key update status = values(status), risk_level = values(risk_level),
              prediction_json = values(prediction_json), update_by = values(update_by), update_time = sysdate()
            """, taskId, stringValue(prediction.get("status"), "SUCCESS"), stringValue(prediction.get("riskLevel"), ""),
            toJson(prediction), currentUsername(), currentUsername());
    }

    private void upsertAdvice(Long taskId, Map<String, Object> advice, String status) {
        jdbcTemplate.update("""
            insert into t2_frame_beam_maintenance_advice(task_id, advice_status, advice_json, create_by, create_time, update_by, update_time)
            values (?, ?, ?, ?, sysdate(), ?, sysdate())
            on duplicate key update advice_status = values(advice_status), advice_json = values(advice_json),
              update_by = values(update_by), update_time = sysdate()
            """, taskId, status, toJson(advice), currentUsername(), currentUsername());
    }

    private void assertFrameBeamTask(Long taskId) {
        DesignTask task = taskService.selectTaskById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在：" + taskId);
        }
        if (!TASK_TYPE.equals(task.getTaskType())) {
            throw new IllegalStateException("当前接口仅支持框梁裂纹扩展与寿命预测任务。");
        }
    }

    private Map<String, Object> firstJsonRow(String sql, Long taskId) {
        Map<String, Object> row = firstMap(sql, taskId);
        if (row.isEmpty()) return Collections.emptyMap();
        Object value = row.values().iterator().next();
        return fromJson(stringValue(value, "{}"));
    }

    private Map<String, Object> firstMap(String sql, Long taskId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, taskId);
        if (rows.isEmpty()) return Collections.emptyMap();
        return normalizeKeys(rows.get(0));
    }

    private Map<String, Object> normalizeKeys(Map<String, Object> row) {
        Map<String, Object> normalized = new LinkedHashMap<>();
        row.forEach((key, value) -> normalized.put(toCamel(key), value));
        return normalized;
    }

    private String toCamel(String key) {
        String lower = key == null ? "" : key.toLowerCase();
        StringBuilder sb = new StringBuilder();
        boolean upperNext = false;
        for (char ch : lower.toCharArray()) {
            if (ch == '_') {
                upperNext = true;
            } else if (upperNext) {
                sb.append(Character.toUpperCase(ch));
                upperNext = false;
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    private Map<String, Object> defaultPrediction() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "NOT_SUBMITTED");
        result.put("riskLevel", "");
        result.put("growthCurve", Collections.emptyList());
        result.put("maintenanceAdvice", Collections.emptyMap());
        return result;
    }

    private String currentUsername() {
        try {
            return SecurityUtils.getUsername();
        } catch (Exception e) {
            return "system";
        }
    }

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String stringValue(Object value, String defaultValue) {
        return value == null ? defaultValue : String.valueOf(value);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("JSON serialization failed.", e);
        }
    }

    private Map<String, Object> fromJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    private void ensureTables() {
        jdbcTemplate.execute("""
            create table if not exists t2_frame_beam_crack_input (
              id bigint(20) not null auto_increment,
              task_id bigint(20) not null,
              input_json longtext not null,
              create_by varchar(64) default '',
              create_time datetime default null,
              update_by varchar(64) default '',
              update_time datetime default null,
              primary key (id),
              unique key uk_t2_frame_beam_crack_task (task_id)
            ) engine=InnoDB default charset=utf8mb4 comment='Frame beam crack input'
            """);
        jdbcTemplate.execute("""
            create table if not exists t2_frame_beam_load_spectrum (
              id bigint(20) not null auto_increment,
              task_id bigint(20) not null,
              spectrum_name varchar(160) default '',
              file_name varchar(260) default '',
              file_path varchar(800) default '',
              spectrum_json longtext not null,
              create_by varchar(64) default '',
              create_time datetime default null,
              update_by varchar(64) default '',
              update_time datetime default null,
              primary key (id),
              unique key uk_t2_frame_beam_spectrum_task (task_id)
            ) engine=InnoDB default charset=utf8mb4 comment='Frame beam load spectrum'
            """);
        jdbcTemplate.execute("""
            create table if not exists t2_frame_beam_life_prediction (
              id bigint(20) not null auto_increment,
              task_id bigint(20) not null,
              status varchar(30) default 'NOT_SUBMITTED',
              risk_level varchar(30) default '',
              prediction_json longtext not null,
              create_by varchar(64) default '',
              create_time datetime default null,
              update_by varchar(64) default '',
              update_time datetime default null,
              primary key (id),
              unique key uk_t2_frame_beam_prediction_task (task_id)
            ) engine=InnoDB default charset=utf8mb4 comment='Frame beam life prediction'
            """);
        jdbcTemplate.execute("""
            create table if not exists t2_frame_beam_maintenance_advice (
              id bigint(20) not null auto_increment,
              task_id bigint(20) not null,
              advice_status varchar(30) default 'SYSTEM_GENERATED',
              advice_json longtext not null,
              create_by varchar(64) default '',
              create_time datetime default null,
              update_by varchar(64) default '',
              update_time datetime default null,
              primary key (id),
              unique key uk_t2_frame_beam_advice_task (task_id)
            ) engine=InnoDB default charset=utf8mb4 comment='Frame beam maintenance advice'
            """);
    }
}
