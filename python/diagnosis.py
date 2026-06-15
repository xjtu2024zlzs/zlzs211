import json
import sys
import os
import numpy as np
import scipy.io as scio
import torch
import torch.nn as nn


# ================== 模型定义 ==================
class Net(nn.Module):
    def __init__(self, out_channel=10):
        super().__init__()
        self.layer1 = nn.Sequential(nn.Conv1d(1, 16, 64, 16, 24), nn.BatchNorm1d(16), nn.ReLU(), nn.MaxPool1d(2))
        self.layer2 = nn.Sequential(nn.Conv1d(16, 32, 3, padding=1), nn.BatchNorm1d(32), nn.ReLU(), nn.MaxPool1d(2))
        self.layer3 = nn.Sequential(nn.Conv1d(32, 64, 3, padding=1), nn.BatchNorm1d(64), nn.ReLU(), nn.MaxPool1d(2))
        self.layer4 = nn.Sequential(nn.Conv1d(64, 64, 3, padding=1), nn.BatchNorm1d(64), nn.ReLU(), nn.MaxPool1d(2))
        self.layer5 = nn.Sequential(nn.Conv1d(64, 64, 3), nn.BatchNorm1d(64), nn.ReLU(), nn.MaxPool1d(2))
        self.extract = nn.Sequential(self.layer1, self.layer2, self.layer3, self.layer4, self.layer5)
        self.linear = nn.Linear(64, out_channel)

    def forward(self, x, y, wx1=0.5, wx2=0.5):
        f1 = self.extract(x).permute(0, 2, 1)
        f2 = self.extract(y).permute(0, 2, 1)
        f = torch.cat([wx1 * f1, wx2 * f2], dim=1)
        return self.linear(f[:, 0, :]) + self.linear(f[:, 1, :])


# ================== 故障标签 ==================
FAULT_MAP = {
    0: "正常", 1: "B007", 2: "B014", 3: "B021",
    4: "IR007", 5: "IR014", 6: "IR021",
    7: "OR007", 8: "OR014", 9: "OR021"
}


# ================== 工具函数 ==================
def open_data(file_path, key_num):
    path = os.path.join(file_path, f"{key_num}.mat")
    str1 = f"X{key_num:03d}_DE_time"
    str2 = f"X{key_num:03d}_FE_time"
    data = scio.loadmat(path)
    return data[str1].ravel(), data[str2].ravel()


# ================== 诊断函数 ==================
def run_diagnosis(params):
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
            x1 = torch.tensor(de, dtype=torch.float32).reshape(1, 1, -1)
            x2 = torch.tensor(fe, dtype=torch.float32).reshape(1, 1, -1)
            out = model(x1, x2)
            pred = torch.argmax(out, dim=1).item()

        return {
            "code": 200,
            "msg": "故障诊断成功",
            "data": {
                "predLabel": pred,
                "faultType": FAULT_MAP[pred]
            }
        }
    except Exception as e:
        return {"code": 500, "msg": f"失败：{str(e)}", "data": None}


# ================== 入口 ==================
def main():
    params = json.loads(sys.argv[1])
    res = run_diagnosis(params)
    print(json.dumps(res, ensure_ascii=False))


if __name__ == '__main__':
    main()