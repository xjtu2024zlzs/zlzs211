# Fatigue Crack Surrogate Worker

Internal FastAPI worker for fatigue crack prediction. RuoYi calls this service from the `ruoyi-designtask1` backend; the frontend must not call it directly.

This standalone worker is now optional. The recommended unified startup is `python/project2` on port `9721`, which exposes both:

- `POST /api/surrogate/optimize`
- `POST /api/surrogate/fatigue/predict`

## Start

Standalone startup, only when you explicitly need a separate fatigue service:

Recommended on Windows:

```bat
cd /d C:\Users\wrr\IdeaProjects\zlzs211-clean\python\project22\fatigue_worker
setup_project22_fatigue_env.bat
start_fatigue_worker.bat
```

Manual commands:

```bat
cd /d C:\Users\wrr\IdeaProjects\zlzs211-clean\python\project22\fatigue_worker
conda create -n project22-fatigue python=3.13 -y
conda activate project22-fatigue
python -m pip install --upgrade -r requirements.txt
python -m uvicorn fatigue_worker:app --host 127.0.0.1 --port 5000
```

## APIs

- `GET /health`
- `POST /api/surrogate/fatigue/predict`
