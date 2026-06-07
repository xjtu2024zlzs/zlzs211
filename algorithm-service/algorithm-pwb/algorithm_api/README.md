# CF Schema Matching Algorithm API

This FastAPI service wraps the CF schema matching algorithm for the RuoYi backend.

## Scope

- Reads source schemas and samples from the five CF API Pull adapters.
- Reads dossier target schema and samples from RuoYi MySQL `p1p_dossier_*` tables.
- Reads CF ground truth from `data/magneto_cf/ground-truth`.
- Returns matching rows, evaluation metrics, and chart data as JSON.
- Does not write to RuoYi MySQL or any external CF source database.

## Start

```powershell
conda activate py310-magneto
cd E:\Desktop\zlzs211\algorithm-service\algorithm-pwb
pip install -r algorithm_api\requirements.txt
uvicorn algorithm_api.main:app --host 127.0.0.1 --port 9204
```

Or use the bundled script:

```powershell
.\start_algorithm_api.ps1
```

Default RuoYi MySQL connection:

```text
mysql+pymysql://root:password@127.0.0.1:3306/ry-cloud?charset=utf8mb4
```

Override it with:

```powershell
$env:RUOYI_MYSQL_URL = "mysql+pymysql://root:password@127.0.0.1:3306/ry-cloud?charset=utf8mb4"
```

## Endpoints

- `GET /api/health`
- `GET /api/v1/capabilities`
- `POST /api/v1/match/run`
- `POST /api/v1/match/run-all`

Stop the local service with:

```powershell
.\stop_algorithm_api.ps1
```

## Minimal Request

```json
{
  "requestId": "record-1",
  "source": {
    "systemKey": "plm",
    "baseUrl": "http://127.0.0.1:9101",
    "limitPerTable": 300
  },
  "target": {
    "mode": "mysql",
    "tablePrefix": "p1p_dossier_",
    "limitPerTable": 300
  },
  "groundTruth": {
    "enabled": true
  },
  "algorithm": {
    "method": "Magneto",
    "embeddingModel": "mpnet",
    "encodingMode": "header_values_verbose_with_table",
    "topk": 20
  }
}
```
