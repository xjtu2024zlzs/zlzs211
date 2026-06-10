# ================= 1. 导入需要的工具 =================
import pandas as pd
import joblib
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler, PolynomialFeatures
from sklearn.gaussian_process import GaussianProcessRegressor
from sklearn.gaussian_process.kernels import Matern, WhiteKernel, ConstantKernel,RBF
from sklearn.pipeline import Pipeline
from sklearn.metrics import r2_score, mean_absolute_error

# ================= 2. 读取你的test.xlsx表格数据 =================
print("正在读取test.csv文件...")
# 这里指定读取你的test.csv文件
df = pd.read_csv("test.csv", encoding="gbk")

# 提取输入特征（5个参数，对应A-E列）
# 列名和你的表格完全一致，不要修改
X = df[["L1(mm)", "L2(mm)", "角度1(°)", "角度2(°)", "R(mm)"]].values
# 提取输出目标（冲击应力，对应G列）
y = df["冲击应力(MPa)"].values

print(f"✅ 成功读取 {len(df)} 组数据！")
print(f"输入参数形状：{X.shape}")
print(f"输出应力形状：{y.shape}")

# 拆分数据：80%用来训练，20%用来测试
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

# ================= 3. 构建「改进版Kriging模型」 =================
# 改进点：
# 1. 用Pipeline打包所有步骤，防止数据泄露
# 2. 添加二次多项式趋势项（Universal Kriging核心）
# 3. 使用Matern核函数（工程数据拟合效果最好）
# 4. 增加优化重启次数，避免局部最优

model_pipeline = Pipeline([
    ('scaler', StandardScaler()),          # 第一步：标准化数据（Kriging必须做）
    ('poly', PolynomialFeatures(degree=2, include_bias=False)), # 第二步：添加二次趋势
    ('gpr', GaussianProcessRegressor(      # 第三步：Kriging模型
        kernel=ConstantKernel(1.0) * RBF(length_scale=1.0, length_scale_bounds=(1e-2, 1e2)) + WhiteKernel(noise_level=1.0),
        n_restarts_optimizer=100,  # 自动优化100次，找全局最优参数
        alpha=1e-10,                       # 这里的 alpha 加上物理层面的微小扰动提高数值稳定性
        random_state=42
    ))
])

# ================= 4. 训练模型 =================
print("\n正在训练Kriging模型...")
model_pipeline.fit(X_train, y_train)

# ================= 5. 测试模型精度 =================
y_pred_train = model_pipeline.predict(X_train)
y_pred_test = model_pipeline.predict(X_test)

print("\n===== 模型训练完成！精度报告 =====")
print(f"训练集 R²分数（越接近1越好）: {r2_score(y_train, y_pred_train):.4f}")
print(f"测试集 R²分数（越接近1越好）: {r2_score(y_test, y_pred_test):.4f}")
print(f"训练集平均误差（MPa）: {mean_absolute_error(y_train, y_pred_train):.2f}")
print(f"测试集平均误差（MPa）: {mean_absolute_error(y_test, y_pred_test):.2f}")

# ================= 6. 保存训练好的模型 =================
joblib.dump(model_pipeline, "trained_kriging_model.pkl")
print("\n✅ 模型已成功保存！")
print("文件名：trained_kriging_model.pkl")
print("现在可以启动后端服务了！")