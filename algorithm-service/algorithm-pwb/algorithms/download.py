import os
import sys

import requests
from tqdm.autonotebook import tqdm

"""
该模块提供统一的模型下载与缓存逻辑，帮助算法在本地复用预训练权重，
避免重复联网开销并保持与论文实验环境一致。
"""

default_os_cache_dir = os.getenv(
    "XDG_CACHE_HOME", os.path.join(os.path.expanduser("~"), ".cache")
)
BDIKIT_CACHE_DIR = os.getenv(
    "BDIKIT_CACHE", os.path.join(default_os_cache_dir, "bdikit")
)
BDIKIT_MODELS_CACHE_DIR = os.path.join(BDIKIT_CACHE_DIR, "models")

BUILTIN_MODELS_BOX_URL = {
    "cl-reducer-v0.1": "https://nyu.box.com/shared/static/hc4qxzbuxz0uoynfwy4pe2yxo5ch6xgm.pt",
    "bdi-cl-v0.2": "https://nyu.box.com/shared/static/1vdc28kzbpoj6ey95bksaww541p9gj31.pt",
}


def download_file_url(url: str, destination: str):
    # 启动流式下载，便于处理大文件
    response = requests.get(url, stream=True)
    # 从响应头中读取文件大小，用于展示进度
    total_size = int(response.headers.get("content-length", 0))
    # 分块写入磁盘，避免一次性占用大量内存
    block_size = 1024
    with tqdm(total=total_size, unit="B", unit_scale=True) as progress_bar:
        with open(destination, "wb") as file:
            for data in response.iter_content(block_size):
                progress_bar.update(len(data))
                file.write(data)
    # 下载结束后核对文件大小，确保传输完整
    if total_size != 0 and progress_bar.n != total_size:
        raise RuntimeError(
            f"Failed to download file (expected {total_size} bytes, got {progress_bar.n} bytes)"
        )


def get_cache_file_path(model_name: str):
    if not os.path.exists(BDIKIT_MODELS_CACHE_DIR):
        print(
            f"Cache directory does not exist, creating it at: {BDIKIT_MODELS_CACHE_DIR}"
        )
        os.makedirs(BDIKIT_MODELS_CACHE_DIR)
    return os.path.join(BDIKIT_MODELS_CACHE_DIR, model_name)


def get_cached_model_or_download(model_name: str):
    model_path = get_cache_file_path(model_name)
    if not os.path.exists(model_path):
        if model_name in BUILTIN_MODELS_BOX_URL:
            print(f"Downloading model {model_name} from Box to {model_path}")
            download_file_url(BUILTIN_MODELS_BOX_URL[model_name], model_path)
        else:
            raise ValueError(f"Model {model_name} not found in builtin models")
    return model_path


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Please provide a model_id as a command line argument.")
        sys.exit(1)

    model_id = sys.argv[1]
    model_path = get_cached_model_or_download(model_id)
    print(f"Downloaded model: {model_path}")
