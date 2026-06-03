# Spring Boot 调用版振动信号算法服务

该服务把原 PyQt 程序中的四个算法模块拆成 FastAPI 接口，供 Spring Boot 调用：

1. 数据上传/读取、数据清洗、时域分析、时频分析
2. 特征处理
3. 早期故障识别
4. 故障预防

## 启动

```bash
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000
```

可选环境变量：

```bash
export ALGORITHM_STORAGE_ROOT=/data/algorithm-storage
```

默认存储目录是当前服务目录下的 `./storage`。

---

## 1. upload-analyze：兼容 Spring Boot 当前 JSON 请求

接口：

```http
POST /algorithm/data/upload-analyze
Content-Type: application/json
```

请求示例：

```json
{
  "taskId": "FE20260521152039963",
  "taskType": "FEATURE_ANALYSIS",
  "fileMode": "MULTI_FILE",
  "sampleIds": [9, 11, 13],
  "filePaths": [
    "F:/TotalData/FaultIdentifyData/ObjectNumericData/component/C1/NUM_xxx/1.csv",
    "F:/TotalData/FaultIdentifyData/ObjectNumericData/component/C1/NUM_xxx/2.csv",
    "F:/TotalData/FaultIdentifyData/ObjectNumericData/component/C1/NUM_xxx/3.csv"
  ],
  "fileUrls": [
    "/quality/fault-iden/files/source/9",
    "/quality/fault-iden/files/source/11",
    "/quality/fault-iden/files/source/13"
  ],
  "batchPath": "F:/TotalData/FaultIdentifyData/ObjectNumericData/component/C1/NUM_xxx",
  "samplingFrequency": 25600,
  "columnIndex": 0,
  "windowSize": 1.0,
  "overlapPercent": 50.0,
  "removeOutliers": true,
  "maxSeconds": 5.0
}
```

说明：

- `taskId` 默认作为 `datasetId` 使用。
- `filePaths` 必须是 Python 服务所在机器可以访问的路径。
- `columnIndex` 从 0 开始，`0` 表示第一列。
- `removeOutliers=true` 时使用 IQR 方法剔除异常值。
- `maxSeconds` 控制 STFT 时频分析最多取前多少秒，防止响应过大。

返回核心字段：

```json
{
  "taskId": "FE20260521152039963",
  "datasetId": "FE20260521152039963",
  "combinedDataPath": ".../storage/datasets/FE20260521152039963/data_combined.npy",
  "originalDataDir": ".../storage/datasets/FE20260521152039963/original",
  "samplingFrequency": 25600,
  "columnIndex": 0,
  "totalSamples": 76800,
  "totalTime": 3.0,
  "fileReports": [],
  "timeDomain": {},
  "timeFrequency": {},
  "status": "READY"
}
```

后续接口使用返回的 `datasetId` 和 `combinedDataPath`。

---

## 2. 特征提取

```http
POST /algorithm/features/extract
Content-Type: application/json
```

```json
{
  "datasetId": "FE20260521152039963",
  "combinedDataPath": ".../storage/datasets/FE20260521152039963/data_combined.npy",
  "samplingFrequency": 25600,
  "windowSize": 1.0,
  "overlapPercent": 50.0
}
```

返回 `featureSetId` 和 `featurePath`。

---

## 3. 早期故障识别

```http
POST /algorithm/detection/early-fault
Content-Type: application/json
```

```json
{
  "datasetId": "FE20260521152039963",
  "featureSetId": "fs_xxx",
  "featurePath": ".../features/FE20260521152039963/fs_xxx.json",
  "methods": ["kurtosis_3sigma", "rms_trend"],
  "baselineWindows": 500,
  "rmsSensitivity": 0.2,
  "fusionStrategy": "earliest"
}
```

返回 `detectionId`、`detectionPath`、`earlyDegradationTime`。

---

## 4. 故障预防

```http
POST /algorithm/prevention/analyze
Content-Type: application/json
```

```json
{
  "datasetId": "FE20260521152039963",
  "combinedDataPath": ".../storage/datasets/FE20260521152039963/data_combined.npy",
  "samplingFrequency": 25600,
  "detectionPath": ".../detections/FE20260521152039963/det_xxx.json"
}
```

也可以直接传：

```json
{
  "datasetId": "FE20260521152039963",
  "combinedDataPath": ".../storage/datasets/FE20260521152039963/data_combined.npy",
  "samplingFrequency": 25600,
  "degradationTime": 12.5
}
```

---

## 注意事项

- 如果 Spring Boot 和 Python 不在同一台机器，`F:/...` 这种路径在 Python 机器上可能不可访问。此时应改用共享盘、对象存储，或调用 `/algorithm/data/upload-multipart` 直接上传文件流。
- `data_combined` 保存为 `.npy` 文件，不直接写入数据库。
- Spring Boot 建议把 `datasetId -> combinedDataPath` 写入数据库，后续接口只查路径再传给 Python。
