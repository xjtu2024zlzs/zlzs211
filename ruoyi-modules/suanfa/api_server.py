from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import traceback

from business.preprocessing import run_preprocess_biz

app = FastAPI(title="轴承故障诊断算法服务")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


class PreprocessRequest(BaseModel):
    key_num: int
    win_length: int = 1024
    denoise: bool = True
    denoise_mode: str = "gaussian"
    normalize: bool = True
    normalize_mode: str = "zscore"


@app.post("/api/preprocess")
def preprocess(req: PreprocessRequest):
    try:
        result = run_preprocess_biz(
            key_num=req.key_num,
            win_length=req.win_length,
            denoise=req.denoise,
            denoise_mode=req.denoise_mode,
            normalize=req.normalize,
            normalize_mode=req.normalize_mode
        )

        return {
            "code": 200,
            "msg": "预处理执行成功",
            "data": result
        }

    except Exception as e:
        print("预处理执行失败：")
        traceback.print_exc()

        return {
            "code": 500,
            "msg": f"预处理执行失败：{str(e)}",
            "data": None
        }