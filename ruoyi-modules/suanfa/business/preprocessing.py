import numpy as np
import scipy.io as scio
from sklearn import preprocessing
from common.plot_config import set_matplotlib_global
from common.utils import set_random_seed
import pymysql
import scipy.io
import io
from urllib.request import urlopen
from urllib.error import HTTPError, URLError

set_matplotlib_global()
set_random_seed(0)

MYSQL_CONFIG = {
    "host": "127.0.0.1",
    "port": 3306,
    "user": "root",
    "password": "root@20260525",
    "database": "t4_sql",
    "charset": "utf8mb4"
}


def get_mat_url_by_key(key_num):
    """
    根据 key_num 从数据库中查询 mat_content 字段。
    现在 mat_content 不再是文件二进制内容，而是 mat 文件 URL 地址。
    """
    conn = pymysql.connect(**MYSQL_CONFIG)
    cur = conn.cursor()

    cur.execute(
        "SELECT mat_content FROM cwru_file_meta WHERE file_name=%s",
        (str(key_num),)
    )

    row = cur.fetchone()

    cur.close()
    conn.close()

    if row is None:
        raise FileNotFoundError(f"数据库中未找到 file_name={key_num} 的记录")

    mat_url = row[0]

    # 如果数据库字段之前是 LONGBLOB，可能会以 bytes 形式返回
    if isinstance(mat_url, bytes):
        mat_url = mat_url.decode("utf-8")

    if not mat_url:
        raise ValueError(f"file_name={key_num} 对应的 mat_content URL 为空")

    return mat_url


def load_mat_from_url(mat_url, timeout=30):
    """
    通过 URL 读取 mat 文件内容，再转为 scipy 可识别的内存文件。
    """
    try:
        with urlopen(mat_url, timeout=timeout) as response:
            mat_bytes = response.read()

    except HTTPError as e:
        raise RuntimeError(f"请求 mat 文件失败，HTTP状态码={e.code}，URL={mat_url}") from e

    except URLError as e:
        raise RuntimeError(f"无法访问 mat 文件 URL：{mat_url}，错误原因：{e.reason}") from e

    except Exception as e:
        raise RuntimeError(f"读取 mat 文件 URL 失败：{mat_url}，错误信息：{e}") from e

    if not mat_bytes:
        raise ValueError(f"URL 返回的 mat 文件内容为空：{mat_url}")

    buf = io.BytesIO(mat_bytes)
    mat_data = scipy.io.loadmat(buf)

    return mat_data


def test_read_mat(key_num):
    """
    原逻辑：
        key_num -> 数据库 -> mat_content二进制 -> loadmat

    新逻辑：
        key_num -> 数据库 -> mat_content中的URL -> 通过URL读取mat文件 -> loadmat
    """
    mat_url = get_mat_url_by_key(key_num)

    mat_data = load_mat_from_url(mat_url)

    print(f"编号{key_num}读取成功")
    print(f"mat文件URL：{mat_url}")
    print("内部信号字段：")
    print(list(mat_data.keys()))

    return mat_data


# 底层工具函数
def open_data(key_num):
    str1 = "X" + "%03d" % key_num + "_DE_time"
    str2 = "X" + "%03d" % key_num + "_FE_time"

    data = test_read_mat(str(key_num))

    data1 = data[str1]
    data2 = data[str2]

    return data1, data2


def deal_data(data1, data2, length, label):
    data1 = np.reshape(data1, (-1))
    num = len(data1) // length
    data1 = data1[0:num * length]
    data1 = np.reshape(data1, (num, length))

    min_max_scaler = preprocessing.MinMaxScaler()
    data1 = min_max_scaler.fit_transform(np.transpose(data1, [1, 0]))
    data1 = np.transpose(data1, [1, 0])

    data2 = np.reshape(data2, (-1))
    num = len(data2) // length
    data2 = data2[0:num * length]
    data2 = np.reshape(data2, (num, length))

    min_max_scaler = preprocessing.MinMaxScaler()
    data2 = min_max_scaler.fit_transform(np.transpose(data2, [1, 0]))
    data2 = np.transpose(data2, [1, 0])

    label = np.ones((num, 1)) * label

    return np.column_stack((data1, data2, label))


def signal_denoise(signal: np.ndarray, mode: str = "moving_avg", win_size: int = 3) -> np.ndarray:
    signal = np.asarray(signal, dtype=np.float32).ravel()
    n = len(signal)

    if win_size % 2 == 0:
        raise ValueError("窗口大小 win_size 必须为奇数")

    half_win = win_size // 2
    pad_sig = np.pad(signal, pad_width=half_win, mode="constant")
    res = np.zeros_like(signal)

    if mode == "moving_avg":
        for i in range(n):
            res[i] = np.mean(pad_sig[i:i + win_size])

    elif mode == "median":
        for i in range(n):
            res[i] = np.median(pad_sig[i:i + win_size])

    elif mode == "gaussian":
        x = np.arange(-half_win, half_win + 1)
        sigma = win_size / 3
        gauss_kernel = np.exp(-(x ** 2) / (2 * sigma ** 2))
        gauss_kernel /= np.sum(gauss_kernel)

        for i in range(n):
            res[i] = np.sum(pad_sig[i:i + win_size] * gauss_kernel)

    else:
        raise ValueError("模式仅支持: moving_avg / median / gaussian")

    return res


def signal_normalize(signal: np.ndarray, mode: str = "minmax") -> np.ndarray:
    signal = np.asarray(signal, dtype=np.float32)

    if mode == "minmax":
        s_min = np.min(signal)
        s_max = np.max(signal)

        if s_max == s_min:
            return np.zeros_like(signal)

        return (signal - s_min) / (s_max - s_min)

    elif mode == "zscore":
        s_mean = np.mean(signal)
        s_std = np.std(signal)

        if s_std == 0:
            return np.zeros_like(signal)

        return (signal - s_mean) / s_std

    elif mode == "l1":
        l1 = np.sum(np.abs(signal))
        return signal / l1 if l1 != 0 else signal

    elif mode == "l2":
        l2 = np.linalg.norm(signal)
        return signal / l2 if l2 != 0 else signal

    else:
        raise ValueError("模式仅支持: minmax / zscore / l1 / l2")


# ===================== 业务顶层执行函数【带完整返回值】 =====================
def run_preprocess_biz(
    key_num: int,
    win_length: int = 1024,
    denoise: bool = True,
    denoise_mode: str = "gaussian",
    normalize: bool = True,
    normalize_mode: str = "zscore"
):
    """
    业务1：数据预处理主入口
    return 结构化结果字典，可直接入库
    """

    # 1. 读取原始 mat 数据
    raw_de, raw_fe = open_data(key_num)

    # 截取窗口长度
    raw_de_cut = raw_de[:win_length].copy()
    raw_fe_cut = raw_fe[:win_length].copy()

    # 2. 去噪
    denoise_de = raw_de_cut
    denoise_fe = raw_fe_cut

    if denoise:
        denoise_de = signal_denoise(raw_de_cut, denoise_mode)
        denoise_fe = signal_denoise(raw_fe_cut, denoise_mode)

    # 3. 归一化
    norm_de = denoise_de
    norm_fe = denoise_fe

    if normalize:
        norm_de = signal_normalize(denoise_de, normalize_mode)
        norm_fe = signal_normalize(denoise_fe, normalize_mode)

    # 返回业务输出，全量数据，下游增强/诊断读取
    return {
        "biz_name": "preprocess",
        "status": "success",
        "params": {
            "key_num": key_num,
            "win_length": win_length,
            "denoise": denoise,
            "denoise_mode": denoise_mode,
            "normalize": normalize,
            "normalize_mode": normalize_mode
        },
        "data": {
            "raw_de": raw_de_cut.tolist(),
            "raw_fe": raw_fe_cut.tolist(),
            "denoise_de": denoise_de.tolist(),
            "denoise_fe": denoise_fe.tolist(),
            "norm_de": norm_de.tolist(),
            "norm_fe": norm_fe.tolist()
        }
    }