import json
import sys
import os
import numpy as np
import scipy.io as scio
import torch
import torch.nn as nn


# ================== 模型定义 ==================
class Net(nn.Module):
    def __init__(self):
        super().__init__()
        self.layer1 = nn.Sequential(nn.Conv1d(1, 16, 64, 16, 24), nn.BatchNorm1d(16), nn.ReLU(), nn.MaxPool1d(2))
        self.layer2 = nn.Sequential(nn.Conv1d(16, 32, 3, padding=1), nn.BatchNorm1d(32), nn.ReLU(), nn.MaxPool1d(2))
        self.layer3 = nn.Sequential(nn.Conv1d(32, 64, 3, padding=1), nn.BatchNorm1d(64), nn.ReLU(), nn.MaxPool1d(2))
        self.layer4 = nn.Sequential(nn.Conv1d(64, 64, 3, padding=1), nn.BatchNorm1d(64), nn.ReLU(), nn.MaxPool1d(2))
        self.layer5 = nn.Sequential(nn.Conv1d(64, 64, 3), nn.BatchNorm1d(64), nn.ReLU(), nn.MaxPool1d(2))
        self.extract = nn.Sequential(self.layer1, self.layer2, self.layer3, self.layer4, self.layer5)

    def forward(self, x):
        return self.extract(x)


# ================== 工具函数 ==================
def open_data(file_path, key_num):
    path = os.path.join(file_path, f"{key_num}.mat")
    str1 = f"X{key_num:03d}_DE_time"
    str2 = f"X{key_num:03d}_FE_time"
    data = scio.loadmat(path)
    return data[str1].ravel(), data[str2].ravel()


# ================== 特征提取 ==================
def run_extract(params):
    try:
        file_path = params["filePath"]
        key_num = params["keyNum"]
        win_len = params.get("winLength", 1024)
        model_path = "model.pt"

        de, fe = open_data(file_path, key_num)
        de = de[:win_len]
        fe = fe[:win_len]

        model = Net()
        model.load_state_dict(torch.load(model_path, map_location="cpu"))
        model.eval()

        with torch.no_grad():
            feat_de = model(torch.tensor(de, dtype=torch.float32).reshape(1, 1, -1)).numpy().tolist()
            feat_fe = model(torch.tensor(fe, dtype=torch.float32).reshape(1, 1, -1)).numpy().tolist()

        return {
            "code": 200,
            "msg": "特征提取成功",
            "data": {"deFeature": feat_de, "feFeature": feat_fe}
        }
    except Exception as e:
        return {"code": 500, "msg": f"失败：{str(e)}", "data": None}


# ================== 入口 ==================
def main():
    params = json.loads(sys.argv[1])
    res = run_extract(params)
    print(json.dumps(res, ensure_ascii=False))


if __name__ == '__main__':
    main()