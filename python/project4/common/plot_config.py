import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

def set_matplotlib_global():
    plt.rcParams['font.sans-serif'] = ['SimSun']
    plt.rcParams['font.family'] = ['Times New Roman', 'SimSun']
    plt.rcParams['font.weight'] = 'bold'
    plt.rcParams['font.size'] = 18
    plt.rcParams["axes.unicode_minus"] = False