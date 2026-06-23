from fastapi import FastAPI, Query
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from test import fault_diagnose_api

# 实例化服务
app = FastAPI(title="轴承故障诊断API", version="1.0")

# 跨域配置（若依前端本地开发必开）
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 请求参数模型
class DiagnoseRequest(BaseModel):
    data_root_path: str = r"E:\\程序文件（杜子昂）\\paper\\sensors journal\\baseline(SE）\\Datasets_dir\\cwru\\12k Drive End Bearing Fault Data\\"
    key_num: int
    win_length: int = 1024
    w_x1: float = 0.5
    w_x2: float = 0.5
    denoise: bool = True
    denoise_mode: str = "gaussian"
    normalize: bool = True
    normalize_mode: str = "zscore"
    aug_model: str = "scale"
    aug_scale: int = 2

# 诊断核心接口（给若依axios调用 POST）
@app.post("/api/bearing/diagnose")
def bearing_diagnose(req: DiagnoseRequest):
    try:
        result = fault_diagnose_api(
            data_root_path=req.data_root_path,
            key_num=req.key_num,
            win_length=req.win_length,
            w_x1=req.w_x1,
            w_x2=req.w_x2,
            denoise=req.denoise,
            denoise_mode=req.denoise_mode,
            normalize=req.normalize,
            normalize_mode=req.normalize_mode,
            aug_model=req.aug_model,
            aug_scale=req.aug_scale
        )
        # 若依标准返回格式
        return {
            "code": 200,
            "msg": "诊断成功",
            "data": result
        }
    except Exception as e:
        return {
            "code": 500,
            "msg": f"算法执行失败：{str(e)}",
            "data": None
        }

# 可选：获取支持的滤波/归一化/增强类型下拉接口（给前端下拉框）
@app.get("/api/bearing/config")
def get_config():
    return {
        "code": 200,
        "msg": "ok",
        "data": {
            "denoise_modes": ["moving_avg", "median", "gaussian"],
            "normalize_modes": ["minmax", "zscore", "l1", "l2"],
            "aug_modes": ["noise", "scale", "flip", "shift", "mask"]
        }
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main_api:app", host="0.0.0.0", port=8000)