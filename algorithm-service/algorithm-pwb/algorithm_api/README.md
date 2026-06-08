# CF Schema Matching Algorithm API

This FastAPI service wraps the CF schema matching algorithm for the RuoYi backend.

## Scope

- Reads source schemas and samples from the five CF API Pull adapters.
- Reads dossier target schema and samples from RuoYi MySQL `p1p_dossier_*` tables.
- Reads CF ground truth from `data/magneto_cf/ground-truth`.
- Returns method/variant result sets, matching rows, evaluation metrics, and chart data as JSON.
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

## LLM API Key

`MagnetoGPT` uses the API key from the Python service environment. Do not enter the key in the RuoYi frontend.

Create a local `.env` file from the example and paste your own key:

```powershell
cd E:\Desktop\zlzs211\algorithm-service\algorithm-pwb
copy .env.example .env
notepad .env
```

Supported variables:

```text
DEEPSEEK_API_KEY=
OPENAI_API_KEY=
```

If `DEEPSEEK_API_KEY` is not configured for `deepseek/deepseek-chat`, the service skips `MagnetoGPT` and returns only four result sets: `Magneto all`, `Magneto one2one`, `MagnetoBoost all`, and `MagnetoBoost one2one`.

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
    "methods": ["Magneto", "MagnetoBoost", "MagnetoGPT"],
    "variants": ["all", "one2one"],
    "embeddingModel": "mpnet",
    "encodingMode": "header_values_verbose_with_table",
    "topk": 20,
    "llmModel": "deepseek/deepseek-chat"
  }
}
```

The response contains `resultSets`, one item per requested `method + variant`.
For the default three methods and two variants, one request returns six result sets:

- `Magneto:all`
- `Magneto:one2one`
- `MagnetoBoost:all`
- `MagnetoBoost:one2one`
- `MagnetoGPT:all`
- `MagnetoGPT:one2one`

Each result set includes `rows`, `metrics`, `metricsSummary`, and `charts`.
Metrics reuse the legacy CF benchmark logic: `mrr` and `Recall@20` come from the old benchmark utility semantics, while precision/recall/F1 come from `valentine.MatcherResults.get_metrics()`.

Legacy requests that only send `method` are still accepted. For example, `"method": "Magneto"` returns the two Magneto result sets: `all` and `one2one`.
