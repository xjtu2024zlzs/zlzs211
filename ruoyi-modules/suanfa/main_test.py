from business.preprocessing import run_preprocess_biz
from business.augment import run_augment_biz
# 新增：导入特征融合业务
from business.fusion import run_feature_fusion_biz
from business.diagnose import run_diagnose_biz

if __name__ == '__main__':
    # 全局配置
    key_num = 107

    # ========== 1. 执行业务1：数据预处理 ==========
    pre_res = run_preprocess_biz(
        key_num=key_num,
        win_length=1024,
        denoise=True,
        denoise_mode="gaussian",
        normalize=True,
        normalize_mode="zscore"
    )
    print("【业务1-预处理输出】", pre_res["status"])
    # 软件此处逻辑：将 pre_res 写入数据库

    # ========== 2. 执行业务2：数据增强（读取库中pre_res） ==========
    aug_res = run_augment_biz(
        preprocess_result=pre_res,
        aug_model="scale",
        aug_scale=2
    )
    print("【业务2-数据增强输出】", aug_res["status"])
    # 软件此处逻辑：将 aug_res 写入数据库

    # ========== 3. 执行业务3：双模态特征融合（独立新增业务） ==========
    fusion_res = run_feature_fusion_biz(
        preprocess_result=pre_res,
        w_x1=0.5,
        w_x2=0.5
    )
    print("【业务3-特征融合输出】", fusion_res["status"])
    print("融合特征维度信息已生成，可入库存储特征与可视化图")
    # 软件此处逻辑：将 fusion_res 写入数据库

    # ========== 4. 执行业务4：故障诊断（读取库中pre_res，内部自动调用融合） ==========
    diag_res = run_diagnose_biz(
        preprocess_result=pre_res,
        w_x1=0.5,
        w_x2=0.5
    )
    print("【业务4-故障诊断输出】", diag_res["status"])
    print("预测故障信息：", diag_res["model_output"]["fault_info"])
    # 软件此处逻辑：将 diag_res 写入数据库