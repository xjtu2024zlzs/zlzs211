from fastapi import FastAPI

from app.schemas.crack_growth_schema import CrackGrowthRequest, CrackGrowthResponse
from app.services.crack_growth_predictor import predict_growth

app = FastAPI(title="Project22 Frame Beam Crack Life Prediction Service", version="1.0.0")


@app.get("/health")
def health():
    return {"status": "UP", "service": "project22-frame-beam-crack-life", "port": 9822}


@app.post("/api/frame-beam-crack/growth-predict", response_model=CrackGrowthResponse)
def growth_predict(request: CrackGrowthRequest):
    return predict_growth(request)
