import torch
import torch.nn as nn

class Net(nn.Module):
    def __init__(self, in_channel=1, out_channel=10):
        super(Net, self).__init__()
        self.layer1 = nn.Sequential(
            nn.Conv1d(in_channel, 16, kernel_size=64, stride=16, padding=24),
            nn.BatchNorm1d(16),
            nn.ReLU(inplace=True),
            nn.MaxPool1d(kernel_size=2, stride=2)
        )
        self.layer2 = nn.Sequential(
            nn.Conv1d(16, 32, kernel_size=3, padding=1),
            nn.BatchNorm1d(32),
            nn.ReLU(inplace=True),
            nn.MaxPool1d(kernel_size=2, stride=2)
        )
        self.layer3 = nn.Sequential(
            nn.Conv1d(32, 64, kernel_size=3, padding=1),
            nn.BatchNorm1d(64),
            nn.ReLU(inplace=True),
            nn.MaxPool1d(kernel_size=2, stride=2)
        )
        self.layer4 = nn.Sequential(
            nn.Conv1d(64, 64, kernel_size=3, padding=1),
            nn.BatchNorm1d(64),
            nn.ReLU(inplace=True),
            nn.MaxPool1d(kernel_size=2, stride=2)
        )
        self.layer5 = nn.Sequential(
            nn.Conv1d(64, 64, kernel_size=3),
            nn.BatchNorm1d(64),
            nn.ReLU(inplace=True),
            nn.MaxPool1d(kernel_size=2, stride=2)
        )
        self.extract = nn.Sequential(self.layer1, self.layer2, self.layer3, self.layer4, self.layer5)
        self.linear = nn.Linear(64, out_channel)


    def forward(self, x, y, wx1, wx2):
        features1 = self.extract(x).permute(0, 2, 1)
        features2 = self.extract(y).permute(0, 2, 1)
        features1 = wx1 * features1
        features2 = wx2 * features2
        fuse_features = torch.cat([features1, features2], dim=1)
        logits = self.linear(fuse_features[:, 0, :]) + self.linear(fuse_features[:, 1, :])

        return logits

    # 仅提取融合特征，返回3个值
    def forward_fusion(self, x, y, wx1, wx2):
        features1 = self.extract(x).permute(0, 2, 1)
        features2 = self.extract(y).permute(0, 2, 1)
        features1 = wx1 * features1
        features2 = wx2 * features2
        fuse_feat = torch.cat([features1, features2], dim=1)
        return fuse_feat, features1, features2

    # 仅分类，输入融合特征输出logits
    def forward_classify(self, fuse_features):
        # print(fuse_features.shape)  # 1,2,64
        output = self.linear(fuse_features[:, 0, :]) + self.linear(fuse_features[:, 1, :])
        #
        return output

MODEL_CACHE = None
MODEL_WEIGHT_PATH = "model.pt"

def load_model_once():
    global MODEL_CACHE
    if MODEL_CACHE is None:
        model = Net(in_channel=1, out_channel=10)
        checkpoint = torch.load(MODEL_WEIGHT_PATH, map_location=torch.device('cpu'))
        model.load_state_dict(checkpoint)
        model.eval()
        MODEL_CACHE = model
    return MODEL_CACHE