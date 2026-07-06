import uvicorn
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import traceback

# 导入四大业务模块
from business.preprocessing import run_preprocess_biz
from business.augment import run_augment_biz
# 修正：真实特征融合业务导入
from business.fusion import run_feature_fusion_biz
from business.diagnose import run_diagnose_biz

# ===================== 初始化FastAPI =====================
app = FastAPI(
    title="轴承故障诊断算法服务",
    description="数据预处理/数据增强/特征融合/故障诊断四大业务接口，输出结构化数据，可直接存入数据库",
    version="1.0.0"
)

# 跨域配置（适配RuoYi-Vue前端）
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 生产环境替换为前端真实地址
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ===================== 请求参数实体类 =====================
# 1. 数据预处理入参
class PreprocessReq(BaseModel):
    data_root_path: str
    key_num: int
    win_length: int = 1024
    denoise: bool = True
    denoise_mode: str = "gaussian"
    normalize: bool = True
    normalize_mode: str = "zscore"

# 2. 数据增强入参（依赖预处理完整结果）
class AugmentReq(BaseModel):
    preprocess_result: dict
    aug_model: str = "scale"
    aug_scale: int = 2

# 3. 特征融合入参【修正：和run_feature_fusion_biz入参匹配】
class FusionReq(BaseModel):
    preprocess_result: dict
    w_x1: float = 0.5
    w_x2: float = 0.5

# 4. 故障诊断入参
class DiagnoseReq(BaseModel):
    data_root_path: str
    key_num: int
    win_length: int = 1024
    denoise: bool = True
    denoise_mode: str = "gaussian"
    normalize: bool = True
    normalize_mode: str = "zscore"
    w_x1: float = 0.5
    w_x2: float = 0.5

# ===================== 统一返回格式 =====================
def to_builtin(obj):
    try:
        import numpy as np
        if isinstance(obj, np.ndarray):
            return obj.tolist()
        if isinstance(obj, np.integer):
            return int(obj)
        if isinstance(obj, np.floating):
            return float(obj)
    except Exception:
        pass
    if isinstance(obj, dict):
        return {k: to_builtin(v) for k, v in obj.items()}
    if isinstance(obj, list):
        return [to_builtin(v) for v in obj]
    if isinstance(obj, tuple):
        return [to_builtin(v) for v in obj]
    return obj

def success_response(data: dict):
    return {
        "code": 200,
        "msg": "操作成功",
        "data": to_builtin(data)
    }

def fail_response(msg: str):
    return {
        "code": 500,
        "msg": msg,
        "data": None
    }

# ===================== 业务接口 =====================
@app.post("/api/preprocess", summary="业务1：数据预处理")
def api_preprocess(req: PreprocessReq):
    try:
        res = run_preprocess_biz(
            key_num=req.key_num,
            win_length=req.win_length,
            denoise=req.denoise,
            denoise_mode=req.denoise_mode,
            normalize=req.normalize,
            normalize_mode=req.normalize_mode
        )
        return success_response(res)
    except Exception as e:
        err_info = traceback.format_exc()
        print(f"预处理异常：{err_info}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/augment", summary="业务2：数据增强")
def api_augment(req: AugmentReq):
    try:
        res = run_augment_biz(
            preprocess_result=req.preprocess_result,
            aug_model=req.aug_model,
            aug_scale=req.aug_scale
        )
        return success_response(res)
    except Exception as e:
        err_info = traceback.format_exc()
        print(f"数据增强异常：{err_info}")
        raise HTTPException(status_code=500, detail=str(e))

# 【修正后真实特征融合接口】
@app.post("/api/fusion", summary="业务3：双模态CNN特征融合")
def api_fusion(req: FusionReq):
    try:
        # 直接调用真实融合业务，参数完全匹配
        res = run_feature_fusion_biz(
            preprocess_result=req.preprocess_result,
            w_x1=req.w_x1,
            w_x2=req.w_x2
        )
        return success_response(res)
    except Exception as e:
        err_info = traceback.format_exc()
        print(f"特征融合异常：{err_info}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/diagnose", summary="业务4：故障诊断（融合+分类一体化）")
def api_diagnose(req: DiagnoseReq):
    try:
        # 1. 先执行预处理，得到 diagnose 需要的 preprocess_result
        pre_res = run_preprocess_biz(
            key_num=req.key_num,
            win_length=req.win_length,
            denoise=req.denoise,
            denoise_mode=req.denoise_mode,
            normalize=req.normalize,
            normalize_mode=req.normalize_mode
        )
        # 2. 传入预处理结果+权重执行诊断
        res = run_diagnose_biz(
            preprocess_result=pre_res,
            w_x1=req.w_x1,
            w_x2=req.w_x2
        )
        return success_response(res)
    except Exception as e:
        err_info = traceback.format_exc()
        print(f"故障诊断异常：{err_info}")
        raise HTTPException(status_code=500, detail=str(e))

# 流水线一站式接口：预处理→增强→特征融合→诊断（完整全流程）
@app.post("/api/pipeline", summary="一站式全业务流水线")
def api_pipeline(
    req: DiagnoseReq,
    aug_model: str = "scale",
    aug_scale: int = 2
):
    try:
        # 1 执行预处理
        pre_res = run_preprocess_biz(
            key_num=req.key_num,
            win_length=req.win_length,
            denoise=req.denoise,
            denoise_mode=req.denoise_mode,
            normalize=req.normalize,
            normalize_mode=req.normalize_mode
        )
        # 2 执行增强
        aug_res = run_augment_biz(pre_res, aug_model, aug_scale)
        # 3 执行特征融合
        fuse_res = run_feature_fusion_biz(pre_res, w_x1=req.w_x1, w_x2=req.w_x2)
        # 4 执行故障诊断
        diag_res = run_diagnose_biz(pre_res, w_x1=req.w_x1, w_x2=req.w_x2)

        full_pipeline = {
            "preprocess": pre_res,
            "augment": aug_res,
            "feature_fusion": fuse_res,
            "diagnose": diag_res
        }
        return success_response(full_pipeline)
    except Exception as e:
        err_info = traceback.format_exc()
        print(f"流水线执行异常：{err_info}")
        raise HTTPException(status_code=500, detail=str(e))

# 健康检查接口（给若依后台监控用）
@app.get("/health", summary="服务健康检测")
def health_check():
    return success_response({"status": "running"})

# 服务启动入口
if __name__ == "__main__":
    uvicorn.run(
        app="app:app",
        host="0.0.0.0",
        port=8000,
        reload=True,  # 开发环境开启热更新；生产环境关闭
        log_level="info"
    )