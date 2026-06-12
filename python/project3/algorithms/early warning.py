import numpy as np
import pywt
from scipy.signal import stft
from scipy.io import loadmat
from scipy.ndimage import zoom
import os
import glob
import warnings
import torch
import torch.nn as nn
import torch.nn.functional as F
import torch.optim as optim
from torch.utils.data import Dataset, DataLoader
from torch.optim.lr_scheduler import CosineAnnealingLR
from sklearn.preprocessing import LabelEncoder, StandardScaler
from sklearn.model_selection import train_test_split
from sklearn.metrics import confusion_matrix, classification_report, roc_curve, auc, f1_score, precision_score, \
    recall_score
from sklearn.preprocessing import label_binarize
import pandas as pd
import matplotlib

matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.gridspec as gridspec
from matplotlib.patches import FancyBboxPatch
import seaborn as sns
import time
import math
import matplotlib.pyplot as plt
import numpy as np
from sklearn.metrics import confusion_matrix, f1_score
warnings.filterwarnings('ignore')


# ──────────────────────────────────────────
# 1. 小波包去噪
# ──────────────────────────────────────────
def wavelet_packet_denoise(signal, wavelet='db4', level=4):
    """
    自适应小波包变换去噪
    基于能量熵准则自适应选择阈值

    Args:
        signal: 原始振动信号 (1D array)
        wavelet: 小波基函数，默认 db4
        level: 分解层数
    Returns:
        denoised: 去噪后信号
    """
    # 小波包分解
    wp = pywt.WaveletPacket(data=signal, wavelet=wavelet, mode='symmetric', maxlevel=level)

    # 获取最终层所有节点
    nodes = [node.path for node in wp.get_level(level, 'freq')]

    # 自适应阈值去噪：基于能量熵准则
    for node_path in nodes:
        node = wp[node_path]
        coeffs = node.data

        # 基于MAD的软阈值估计（对高斯噪声鲁棒）
        sigma = np.median(np.abs(coeffs)) / 0.6745
        threshold = sigma * np.sqrt(2 * np.log(len(coeffs) + 1e-8))

        # 软阈值处理
        node.data = pywt.threshold(coeffs, threshold, mode='soft')

    # 逆变换重构去噪信号
    denoised = wp.reconstruct(update=False)

    # 保证长度一致
    denoised = denoised[:len(signal)]
    return denoised


# ──────────────────────────────────────────
# 2. STFT 时频图生成
# ──────────────────────────────────────────
def compute_stft_spectrogram(signal, fs=51200, nperseg=128, noverlap=96, img_size=64):
    """
    短时傅里叶变换，生成归一化时频图

    Args:
        signal: 去噪后信号
        fs: 采样频率
        nperseg: 每段样本数
        noverlap: 重叠样本数
        img_size: 输出图像大小
    Returns:
        spectrogram: (img_size, img_size) 归一化幅值谱
    """
    f, t, Zxx = stft(signal, fs=fs, nperseg=nperseg, noverlap=noverlap)
    magnitude = np.abs(Zxx)

    # 对数压缩，避免动态范围过大
    magnitude = np.log1p(magnitude)

    # 双线性插值到固定尺寸
    h, w = magnitude.shape
    zoom_h = img_size / h
    zoom_w = img_size / w
    spectrogram = zoom(magnitude, (zoom_h, zoom_w), order=1)

    # Min-Max归一化到[0,1]
    s_min, s_max = spectrogram.min(), spectrogram.max()
    if s_max > s_min:
        spectrogram = (spectrogram - s_min) / (s_max - s_min)

    return spectrogram.astype(np.float32)


# ──────────────────────────────────────────
# 3. 数据集加载（PHM 2019 格式）
# ──────────────────────────────────────────
def load_phm2019_data(data_dir, segment_len=2048, overlap=0.8, fs=51200):

    signals_list = []
    spectrograms_list = []
    labels_list = []

    step = int(segment_len * (1 - overlap))

    # ── 尝试加载 .csv 文件 ──
    csv_files = glob.glob(os.path.join(data_dir, '**', '*.csv'), recursive=True)
    if csv_files:
        print(f"  找到 {len(csv_files)} 个 .csv 文件")
        for fpath in csv_files:
            try:
                df = pd.read_csv(fpath, header=0)
                arr = df.iloc[:, 1].values.astype(np.float64)

                if len(arr) < segment_len:
                    continue

                # 正确获取标签（按文件夹 T1~T8）
                fname = os.path.basename(fpath)

                parts = fpath.replace("\\", "/").split("/")

                condition = parts[-3]  # T1/T2/T3/T4...

                label = _infer_label_from_filename(
                    fname,
                    condition
                )

                # 滑动窗口分段（overlap=0.9 会切出超多片段）
                for start in range(0, len(arr) - segment_len + 1, step):
                    seg = arr[start: start + segment_len]
                    seg_denoised = wavelet_packet_denoise(seg)
                    spec = compute_stft_spectrogram(seg_denoised, fs=fs)
                    signals_list.append(seg_denoised.astype(np.float32))
                    spectrograms_list.append(spec)
                    labels_list.append(label)
            except Exception as e:
                print(f"  跳过 {fpath}: {e}")

    return signals_list, spectrograms_list, labels_list

def _infer_label_from_filename(fname, folder_name=""):

    if "T1" in folder_name:
        return 0      # 健康

    elif any(t in folder_name for t in
             ["T2","T3","T4","T5","T6","T7","T8"]):
        return 1      # 故障

    else:
        raise ValueError(
            f"无法识别标签: {fname} {folder_name}"
        )


# ──────────────────────────────────────────
# PyTorch 数据集封装
# ──────────────────────────────────────────
class CrackDataset(Dataset):
    """
    双路输入数据集：
      - signals: (N, L) 一维时域信号
      - spectrograms: (N, 1, H, W) 二维时频图
      - labels: (N,) 整数标签
    """

    def __init__(self, signals, spectrograms, labels,
                 scaler=None, fit_scaler=False):
        """
        Args:
            signals: list/array of 1D denoised signals
            spectrograms: list/array of 2D spectrograms
            labels: list/array of int labels
            scaler: StandardScaler 实例（测试集传入训练集的scaler）
            fit_scaler: 是否在当前数据上拟合scaler
        """
        # 信号 → (N, L)
        self.signals = np.array(signals, dtype=np.float32)

        # 时频图 → (N, 1, H, W)
        specs = np.array(spectrograms, dtype=np.float32)
        if specs.ndim == 3:
            specs = specs[:, np.newaxis, :, :]  # 添加通道维
        self.spectrograms = specs

        self.labels = np.array(labels, dtype=np.int64)

        # 标准化一维信号
        N, L = self.signals.shape
        flat = self.signals.reshape(N, -1)
        if scaler is None:
            self.scaler = StandardScaler()
            if fit_scaler:
                flat = self.scaler.fit_transform(flat)
            else:
                flat = self.scaler.fit_transform(flat)
        else:
            self.scaler = scaler
            flat = self.scaler.transform(flat)
        self.signals = flat.reshape(N, L).astype(np.float32)

    def __len__(self):
        return len(self.labels)

    def __getitem__(self, idx):
        signal = torch.from_numpy(self.signals[idx]).unsqueeze(0)  # (1, L)
        spectrogram = torch.from_numpy(self.spectrograms[idx])  # (1, H, W)
        label = torch.tensor(self.labels[idx], dtype=torch.long)
        return signal, spectrogram, label


def build_dataloaders(signals, spectrograms, labels,
                      test_size=0.2, val_size=0.1,
                      batch_size=32, random_state=42):
    """
    划分训练/验证/测试集，返回三个DataLoader

    Returns:
        train_loader, val_loader, test_loader, n_classes, scaler
    """
    labels_arr = np.array(labels)
    n_classes = len(np.unique(labels_arr))

    # 先分测试集
    idx = np.arange(len(labels_arr))
    idx_trainval, idx_test = train_test_split(
        idx, test_size=test_size, random_state=random_state,
        stratify=labels_arr
    )
    # 再从trainval分验证集
    rel_val = val_size / (1 - test_size)
    idx_train, idx_val = train_test_split(
        idx_trainval, test_size=rel_val, random_state=random_state,
        stratify=labels_arr[idx_trainval]
    )

    sigs = np.array(signals, dtype=np.float32)
    specs = np.array(spectrograms, dtype=np.float32)

    train_ds = CrackDataset(
        sigs[idx_train], specs[idx_train], labels_arr[idx_train],
        fit_scaler=True
    )
    val_ds = CrackDataset(
        sigs[idx_val], specs[idx_val], labels_arr[idx_val],
        scaler=train_ds.scaler
    )
    test_ds = CrackDataset(
        sigs[idx_test], specs[idx_test], labels_arr[idx_test],
        scaler=train_ds.scaler
    )

    train_loader = DataLoader(train_ds, batch_size=batch_size,
                              shuffle=True, num_workers=0, pin_memory=False)
    val_loader = DataLoader(val_ds, batch_size=batch_size,
                            shuffle=False, num_workers=0)
    test_loader = DataLoader(test_ds, batch_size=batch_size,
                             shuffle=False, num_workers=0)

    print(f"  训练集: {len(train_ds)} | 验证集: {len(val_ds)} | 测试集: {len(test_ds)}")
    print(f"  类别数: {n_classes}")

    return train_loader, val_loader, test_loader, n_classes, train_ds.scaler


# ──────────────────────────────────────────
# 核心模型定义
# ──────────────────────────────────────────
# 子模块 1：2D-CNN（时频特征提取）
class STFT2DCNN(nn.Module):
    """
    轻量级 2D-CNN，输入 STFT 时频图 (B, 1, H, W)
    输出时频特征向量 fstft (B, feature_dim)
    """

    def __init__(self, img_size=64, feature_dim=128):
        super().__init__()
        self.conv_block = nn.Sequential(
            # Block 1
            nn.Conv2d(1, 32, kernel_size=3, padding=1),
            nn.BatchNorm2d(32),
            nn.ReLU(inplace=True),
            nn.MaxPool2d(2, 2),  # → H/2, W/2

            # Block 2
            nn.Conv2d(32, 64, kernel_size=3, padding=1),
            nn.BatchNorm2d(64),
            nn.ReLU(inplace=True),
            nn.MaxPool2d(2, 2),  # → H/4, W/4

            # Block 3
            nn.Conv2d(64, 128, kernel_size=3, padding=1),
            nn.BatchNorm2d(128),
            nn.ReLU(inplace=True),
            nn.AdaptiveAvgPool2d((4, 4))  # → 4×4 固定输出
        )
        self.fc = nn.Sequential(
            nn.Flatten(),
            nn.Linear(128 * 4 * 4, feature_dim),
            nn.ReLU(inplace=True),
            nn.Dropout(0.3)
        )

    def forward(self, x):
        """x: (B, 1, H, W)"""
        x = self.conv_block(x)
        x = self.fc(x)
        return x  # (B, feature_dim)


# 子模块 2：1D-CNN（时域序列特征提取）
class Signal1DCNN(nn.Module):
    """
    1D-CNN，直接从去噪信号提取时序特征
    输入: (B, 1, L)
    输出: fsignal (B, feature_dim)
    """

    def __init__(self, signal_len=2048, feature_dim=128):
        super().__init__()
        self.conv_block = nn.Sequential(
            # Block 1：大感受野捕捉低频模式
            nn.Conv1d(1, 32, kernel_size=64, stride=4, padding=30),
            nn.BatchNorm1d(32),
            nn.ReLU(inplace=True),
            nn.MaxPool1d(4),

            # Block 2
            nn.Conv1d(32, 64, kernel_size=32, stride=2, padding=15),
            nn.BatchNorm1d(64),
            nn.ReLU(inplace=True),
            nn.MaxPool1d(4),

            # Block 3：细粒度特征
            nn.Conv1d(64, 128, kernel_size=16, stride=1, padding=8),
            nn.BatchNorm1d(128),
            nn.ReLU(inplace=True),
            nn.AdaptiveAvgPool1d(8)
        )
        self.fc = nn.Sequential(
            nn.Flatten(),
            nn.Linear(128 * 8, feature_dim),
            nn.ReLU(inplace=True),
            nn.Dropout(0.3)
        )

    def forward(self, x):
        """x: (B, 1, L)"""
        x = self.conv_block(x)
        x = self.fc(x)
        return x  # (B, feature_dim)


# 子模块 3：注意力融合模块
class AttentionFusion(nn.Module):
    """
    可学习注意力权重，自适应融合两路特征
    alpha_stft + alpha_signal = 1（Softmax归一化）
    输出: fused (B, feature_dim)
    """

    def __init__(self, feature_dim=128):
        super().__init__()
        # 学习每路特征的重要性得分
        self.score_net = nn.Sequential(
            nn.Linear(feature_dim * 2, 64),
            nn.ReLU(inplace=True),
            nn.Linear(64, 2)  # 输出两个标量得分
        )

    def forward(self, f_stft, f_signal):
        """
        f_stft: (B, D)
        f_signal: (B, D)
        """
        concat = torch.cat([f_stft, f_signal], dim=1)  # (B, 2D)
        scores = self.score_net(concat)  # (B, 2)
        weights = F.softmax(scores, dim=1)  # (B, 2) 归一化权重

        alpha_stft = weights[:, 0:1]  # (B, 1)
        alpha_signal = weights[:, 1:2]  # (B, 1)

        fused = alpha_stft * f_stft + alpha_signal * f_signal  # (B, D)
        return fused, weights


# 子模块 4：时序注意力（Temporal Attention）forward
class TemporalAttention(nn.Module):
    """
    对 BiLSTM 输出的所有时间步加权聚合
    输入: (B, T, H)  输出: (B, H)
    """

    def __init__(self, hidden_dim):
        super().__init__()
        self.attn_fc = nn.Linear(hidden_dim, 1)

    def forward(self, lstm_out):
        """lstm_out: (B, T, H)"""
        scores = self.attn_fc(lstm_out)  # (B, T, 1)
        weights = F.softmax(scores, dim=1)  # (B, T, 1)
        context = (weights * lstm_out).sum(dim=1)  # (B, H)
        return context, weights.squeeze(-1)  # (B, H), (B, T)


# 完整模型：CrackDetectionModel
class CrackDetectionModel(nn.Module):
    """
    飞机框梁裂纹早期故障识别模型

    架构：
      信号输入 → 1D-CNN → fsignal
      时频图   → 2D-CNN → fstft
                         ↓
                   AttentionFusion → fused
                         ↓
                 序列重塑 → BiLSTM
                         ↓
                TemporalAttention
                         ↓
                  FC → Softmax
    """

    def __init__(self,
                 signal_len=2048,
                 img_size=64,
                 feature_dim=128,
                 lstm_hidden=128,
                 lstm_layers=2,
                 n_classes=4,
                 dropout=0.4):
        super().__init__()

        self.feature_dim = feature_dim

        # 双路特征提取
        self.cnn_2d = STFT2DCNN(img_size=img_size, feature_dim=feature_dim)
        self.cnn_1d = Signal1DCNN(signal_len=signal_len, feature_dim=feature_dim)

        # 注意力融合
        self.fusion = AttentionFusion(feature_dim=feature_dim)

        # 序列建模：将融合特征扩展为时间序列送入BiLSTM
        # 把 feature_dim 维特征展开为 seq_len × input_size 的序列
        self.seq_len = 8
        self.lstm_input_size = feature_dim // self.seq_len  # 16

        self.bilstm = nn.LSTM(
            input_size=self.lstm_input_size,
            hidden_size=lstm_hidden,
            num_layers=lstm_layers,
            batch_first=True,
            bidirectional=True,
            dropout=dropout if lstm_layers > 1 else 0.0
        )

        # 时间步注意力（作用在BiLSTM的双向输出上）
        self.temporal_attn = TemporalAttention(hidden_dim=lstm_hidden * 2)

        # 分类头
        self.classifier = nn.Sequential(
            nn.Linear(lstm_hidden * 2, 64),
            nn.ReLU(inplace=True),
            nn.Dropout(dropout),
            nn.Linear(64, n_classes)
        )

        self._init_weights()

    def _init_weights(self):
        for m in self.modules():
            if isinstance(m, (nn.Conv1d, nn.Conv2d)):
                nn.init.kaiming_normal_(m.weight, mode='fan_out', nonlinearity='relu')
                if m.bias is not None:
                    nn.init.zeros_(m.bias)
            elif isinstance(m, (nn.BatchNorm1d, nn.BatchNorm2d)):
                nn.init.ones_(m.weight)
                nn.init.zeros_(m.bias)
            elif isinstance(m, nn.Linear):
                nn.init.xavier_uniform_(m.weight)
                nn.init.zeros_(m.bias)

    def forward(self, signal, spectrogram):
        """
        Args:
            signal:      (B, 1, L)  一维去噪信号
            spectrogram: (B, 1, H, W) STFT时频图
        Returns:
            logits: (B, n_classes)
            fusion_weights: (B, 2) 两路注意力权重（可视化用）
            temporal_weights: (B, T) 时间步权重（可视化用）
        """
        # 双路特征提取
        f_stft = self.cnn_2d(spectrogram)  # (B, D)
        f_signal = self.cnn_1d(signal)  # (B, D)

        # 注意力融合
        fused, fusion_weights = self.fusion(f_stft, f_signal)  # (B, D)

        # 重塑为序列：(B, D) → (B, seq_len, lstm_input_size)
        B = fused.size(0)
        seq = fused.view(B, self.seq_len, self.lstm_input_size)

        # BiLSTM 序列建模
        lstm_out, _ = self.bilstm(seq)  # (B, T, 2*hidden)

        # 时间步注意力聚合
        context, temporal_weights = self.temporal_attn(lstm_out)  # (B, 2*hidden)

        # 分类
        logits = self.classifier(context)  # (B, n_classes)

        return logits, fusion_weights, temporal_weights


def count_parameters(model):
    """统计模型可训练参数量"""
    total = sum(p.numel() for p in model.parameters())
    trainable = sum(p.numel() for p in model.parameters() if p.requires_grad)
    print(f"  总参数: {total:,}  |  可训练参数: {trainable:,}")
    return trainable


# ──────────────────────────────────────────
# 训练与评估引擎
# ──────────────────────────────────────────

# 早停
class EarlyStopping:
    def __init__(self, patience=25, min_delta=1e-4, mode='max'):
        self.patience = patience
        self.min_delta = min_delta
        self.mode = mode
        self.best = None
        self.counter = 0
        self.should_stop = False

    def step(self, metric):
        if self.best is None:
            self.best = metric
            return False
        if self.mode == 'max':
            improved = metric > self.best + self.min_delta
        else:
            improved = metric < self.best - self.min_delta
        if improved:
            self.best = metric
            self.counter = 0
        else:
            self.counter += 1
            if self.counter >= self.patience:
                self.should_stop = True
        return self.should_stop


# 核心训练/评估函数
def train_one_epoch(model, loader, optimizer, criterion, device, grad_clip=1.0):
    model.train()
    total_loss, correct, total = 0.0, 0, 0

    for signal, spectrogram, labels in loader:
        signal = signal.to(device)
        spectrogram = spectrogram.to(device)
        labels = labels.to(device)

        optimizer.zero_grad()
        logits, _, _ = model(signal, spectrogram)
        loss = criterion(logits, labels)
        loss.backward()

        # 梯度裁剪
        torch.nn.utils.clip_grad_norm_(model.parameters(), grad_clip)
        optimizer.step()

        total_loss += loss.item() * labels.size(0)
        preds = logits.argmax(dim=1)
        correct += (preds == labels).sum().item()
        total += labels.size(0)

    return total_loss / total, correct / total


@torch.no_grad()
def evaluate(model, loader, criterion, device):
    model.eval()
    total_loss, correct, total = 0.0, 0, 0
    all_preds, all_labels, all_probs = [], [], []

    for signal, spectrogram, labels in loader:
        signal = signal.to(device)
        spectrogram = spectrogram.to(device)
        labels = labels.to(device)

        logits, _, _ = model(signal, spectrogram)
        loss = criterion(logits, labels)

        probs = torch.softmax(logits, dim=1)
        preds = logits.argmax(dim=1)

        total_loss += loss.item() * labels.size(0)
        correct += (preds == labels).sum().item()
        total += labels.size(0)

        all_preds.extend(preds.cpu().numpy())
        all_labels.extend(labels.cpu().numpy())
        all_probs.extend(probs.cpu().numpy())

    return (total_loss / total,
            correct / total,
            np.array(all_preds),
            np.array(all_labels),
            np.array(all_probs))


# 完整训练流程
def train_model(model, train_loader, val_loader,
                n_epochs=80, lr=3e-4, weight_decay=5e-4,  # 👈 改好了！
                device='cpu', save_dir='results'):
    """
    完整训练循环，含：
      - Adam + Cosine退火学习率
      - 标签平滑损失
      - 早停（验证集准确率）
      - 最优模型保存

    Returns:
        history: dict，包含每轮 train_loss, train_acc, val_loss, val_acc
    """
    os.makedirs(save_dir, exist_ok=True)
    best_model_path = os.path.join(save_dir, 'best_model.pth')

    criterion = nn.CrossEntropyLoss()
    optimizer = optim.Adam(model.parameters(), lr=lr, weight_decay=weight_decay)
    scheduler = CosineAnnealingLR(optimizer, T_max=n_epochs, eta_min=lr * 0.01)
    early_stop = EarlyStopping(patience=15, mode='max')

    history = {
        'train_loss': [], 'train_acc': [],
        'val_loss': [], 'val_acc': [],
        'lr': []
    }

    best_val_acc = 0.0
    print(f"\n{'=' * 65}")
    print(f"  开始训练 | 设备: {device} | 最大轮数: {n_epochs}")
    print(f"{'=' * 65}")
    print(f"  {'Epoch':>5} | {'Train Loss':>10} | {'Train Acc':>9} | "
          f"{'Val Loss':>8} | {'Val Acc':>7} | {'LR':>8}")
    print(f"  {'-' * 60}")

    for epoch in range(1, n_epochs + 1):
        t0 = time.time()

        tr_loss, tr_acc = train_one_epoch(
            model, train_loader, optimizer, criterion, device)
        vl_loss, vl_acc, _, _, _ = evaluate(
            model, val_loader, criterion, device)

        scheduler.step()
        current_lr = optimizer.param_groups[0]['lr']

        history['train_loss'].append(tr_loss)
        history['train_acc'].append(tr_acc)
        history['val_loss'].append(vl_loss)
        history['val_acc'].append(vl_acc)
        history['lr'].append(current_lr)

        # 保存最优模型
        if vl_acc > best_val_acc:
            best_val_acc = vl_acc
            torch.save({
                'epoch': epoch,
                'model_state_dict': model.state_dict(),
                'optimizer_state_dict': optimizer.state_dict(),
                'val_acc': vl_acc,
            }, best_model_path)

        elapsed = time.time() - t0
        marker = ' ★' if vl_acc == best_val_acc else ''
        print(f"  {epoch:>5} | {tr_loss:>10.4f} | {tr_acc:>8.2%} | "
              f"{vl_loss:>8.4f} | {vl_acc:>6.2%}{marker} | {current_lr:.2e}  [{elapsed:.1f}s]")

        if early_stop.step(vl_acc):
            print(f"\n  早停触发 (patience={early_stop.patience})，最优验证准确率: {best_val_acc:.2%}")
            break

    print(f"\n  训练完成！最优验证准确率: {best_val_acc:.2%}")
    print(f"  最优模型已保存至: {best_model_path}")

    return history, best_model_path


# ──────────────────────────────────────────
# 评估与可视化
# ──────────────────────────────────────────

# 1. 计算核心指标
def compute_metrics(y_true, y_pred):
    acc = (y_true == y_pred).mean()
    f1_weight = f1_score(y_true, y_pred, average='weighted', zero_division=0)

    cm = confusion_matrix(y_true, y_pred)
    per_class_acc = cm.diagonal() / cm.sum(axis=1)
    early_acc = per_class_acc[1]  # 早期故障准确率

    return {
        'accuracy': acc,
        'early_fault_acc': early_acc,
        'f1_weighted': f1_weight,
        'per_class_acc': per_class_acc
    }


# 2. 打印结果（只显示关键信息）
def print_metrics(metrics):
    print("\n" + "=" * 50)
    print("📊 测试集核心评估结果")
    print("=" * 50)
    print(f"  总体准确率        : {metrics['accuracy']:.2%}")
    print(f"  早期裂纹识别准确率 : {metrics['early_fault_acc']:.2%}")
    print(f"  加权F1分数        : {metrics['f1_weighted']:.4f}")
    print("=" * 50)


# 3. 训练曲线（最简）
def plot_training_curves(history, save_path="results/training_curve.png"):
    plt.rcParams['axes.grid'] = True
    epochs = range(1, len(history['train_loss']) + 1)

    plt.rcParams['font.sans-serif'] = ['Microsoft YaHei']
    plt.rcParams['axes.unicode_minus'] = False
    plt.figure(figsize=(12, 4))

    # 损失
    plt.subplot(1, 2, 1)
    plt.plot(epochs, history['train_loss'], label='训练损失')
    plt.plot(epochs, history['val_loss'], label='验证损失')
    plt.title('损失曲线')
    plt.xlabel('Epoch')
    plt.legend()

    # 准确率
    plt.subplot(1, 2, 2)
    plt.plot(epochs, [v * 100 for v in history['train_acc']], label='训练准确率')
    plt.plot(epochs, [v * 100 for v in history['val_acc']], label='验证准确率')
    plt.axhline(90, color='r', linestyle='--', label='90% 目标线')
    plt.title('准确率曲线')
    plt.xlabel('Epoch')
    plt.ylabel('Acc (%)')
    plt.legend()

    plt.tight_layout()
    plt.savefig(save_path, dpi=120)
    plt.close()
    print(f"\n✅ 训练曲线已保存: {save_path}")
# ──────────────────────────────────────────
# 主程序入口
# ──────────────────────────────────────────
if __name__ == '__main__':


    # 1. 参数配置
    DATA_DIR = "./PHMDC2019_Data"
    SEGMENT_LEN = 2048
    BATCH_SIZE = 32
    EPOCHS = 50
    DEVICE = "cuda" if torch.cuda.is_available() else "cpu"

    # 2. 加载数据
    print("[1] 加载并预处理数据...")
    signals, specs, labels = load_phm2019_data(
        data_dir=DATA_DIR, segment_len=SEGMENT_LEN
    )

    # 3. 构建数据集
    print("[2] 构建数据集...")
    train_loader, val_loader, test_loader, n_classes, scaler = build_dataloaders(
        signals, specs, labels, batch_size=BATCH_SIZE
    )

    # 4. 初始化模型
    print("[3] 初始化故障识别模型...")
    model = CrackDetectionModel(
        signal_len=SEGMENT_LEN, n_classes=4
    ).to(DEVICE)
    count_parameters(model)

    # 5. 训练
    print("[4] 开始训练...")
    history, best_path = train_model(
        model, train_loader, val_loader,
        n_epochs=EPOCHS, device=DEVICE
    )

    # 6. 测试集评估
    print("[5] 测试集评估...")
    criterion = nn.CrossEntropyLoss()
    test_loss, test_acc, y_pred, y_true, y_probs = evaluate(
        model, test_loader, criterion, DEVICE
    )

    # 7. 极简评估（只保留核心）
    metrics = compute_metrics(y_true, y_pred)
    print_metrics(metrics)

    os.makedirs("results", exist_ok=True)
    plot_training_curves(history)

    print(f"\n🎉 全部运行完成！最终测试集准确率: {test_acc:.2%}")