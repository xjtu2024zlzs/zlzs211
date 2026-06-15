import json
import sys
import numpy as np

# ===================== 去噪 =====================
def denoise(signal, mode="gaussian", win_size=3):
    signal = np.array(signal).ravel()
    n = len(signal)
    half = win_size // 2
    pad = np.pad(signal, half, mode="edge")
    res = np.zeros_like(signal)

    if mode == "moving_avg":
        for i in range(n):
            res[i] = np.mean(pad[i:i+win_size])
    elif mode == "median":
        for i in range(n):
            res[i] = np.median(pad[i:i+win_size])
    elif mode == "gaussian":
        sigma = win_size / 3
        x = np.arange(-half, half+1)
        kernel = np.exp(-(x**2)/(2*sigma**2))
        kernel /= np.sum(kernel)
        for i in range(n):
            res[i] = np.sum(pad[i:i+win_size] * kernel)
    return res

# ===================== 归一化 =====================
def normalize(signal, mode="zscore"):
    signal = np.array(signal)
    if mode == "minmax":
        s_min, s_max = np.min(signal), np.max(signal)
        return (signal - s_min) / (s_max - s_min) if s_max != s_min else signal
    elif mode == "zscore":
        s_mean, s_std = np.mean(signal), np.std(signal)
        return (signal - s_mean) / s_std if s_std != 0 else signal
    return signal

# ===================== 入口 =====================
def main():
    params = json.loads(sys.argv[1])
    try:
        de = np.array(params["deSignal"])
        fe = np.array(params["feSignal"])
        denoise_flag = params.get("denoise", True)
        denoise_mode = params.get("denoiseMode", "gaussian")
        norm_flag = params.get("normalize", True)
        norm_mode = params.get("normMode", "zscore")

        if denoise_flag:
            de = denoise(de, denoise_mode)
            fe = denoise(fe, denoise_mode)
        if norm_flag:
            de = normalize(de, norm_mode)
            fe = normalize(fe, norm_mode)

        print(json.dumps({
            "code": 200,
            "msg": "预处理完成",
            "data": {
                "deProcessed": de.tolist(),
                "feProcessed": fe.tolist()
            }
        }, ensure_ascii=False))
    except Exception as e:
        print(json.dumps({"code":500,"msg":str(e)}, ensure_ascii=False))

if __name__ == "__main__":
    main()
