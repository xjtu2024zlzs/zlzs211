# Project22 Frame Beam Crack Life Prediction Service

This FastAPI service is intentionally separated from `python/project2`.

- Default port: `9822`
- Health check: `GET /health`
- Prediction API: `POST /api/frame-beam-crack/growth-predict`

## Start

```bash
cd python/project22
pip install -r requirements.txt
uvicorn app.main:app --host 127.0.0.1 --port 9822
```

The current implementation is a deterministic placeholder surrogate. Replace
`app/services/crack_growth_predictor.py` with the real model loader when the
trained frame-beam model is available.
