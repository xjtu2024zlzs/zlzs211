import random
import numpy as np
import io
import base64
from PIL import Image
import torch

def set_random_seed(seed=0):
    random.seed(seed)
    np.random.seed(seed)
    torch.manual_seed(seed)

def fig_to_base64(fig):
    buf = io.BytesIO()
    fig.savefig(buf, format="png", bbox_inches="tight", dpi=120)
    buf.seek(0)
    img_bytes = buf.read()
    b64_str = base64.b64encode(img_bytes).decode("utf-8")
    import matplotlib.pyplot as plt
    plt.close(fig)
    return b64_str