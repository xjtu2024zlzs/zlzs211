import json
import sys
import numpy as np

# ===================== 纯增强算法 =====================
def augment_signal(signal, aug_type):
    length = len(signal)
    if aug_type == "noise":
        return signal + np.random.normal(0, 0.02, length)
    elif aug_type == "scale":
        return signal * np.random.uniform(0.8, 1.2)
    elif aug_type == "flip":
        return np.flip(signal)
    elif aug_type == "shift":
        shift = np.random.randint(10, length//4)
        return np.roll(signal, shift)
    elif aug_type == "mask":
        mask = np.random.choice(length, int(length*0.1), replace=False)
        sig = signal.copy()
        sig[mask] = 0
        return sig
    return signal

# ===================== 批量增强 =====================
def batch_augment(de_samples, fe_samples, aug_num, aug_type):
    de_aug_list = [de_samples]
    fe_aug_list = [fe_samples]
    for _ in range(aug_num):
        de_aug = [augment_signal(d, aug_type) for d in de_samples]
        fe_aug = [augment_signal(f, aug_type) for f in fe_samples]
        de_aug_list.append(de_aug)
        fe_aug_list.append(fe_aug)
    return np.vstack(de_aug_list), np.vstack(fe_aug_list)

# ===================== 入口 =====================
def main():
    params = json.loads(sys.argv[1])
    try:
        de = np.array(params["deProcessed"])
        fe = np.array(params["feProcessed"])
        aug_type = params["augAlgorithm"]
        aug_mult = params["augMultiple"]

        de_aug, fe_aug = batch_augment([de], [fe], aug_mult, aug_type)

        print(json.dumps({
            "code": 200,
            "msg": "增强完成",
            "data": {
                "deAugmented": de_aug.tolist(),
                "feAugmented": fe_aug.tolist(),
                "originalLen": len(de),
                "augmentedLen": len(de_aug)
            }
        }, ensure_ascii=False))
    except Exception as e:
        print(json.dumps({"code":500,"msg":str(e)}, ensure_ascii=False))

if __name__ == "__main__":
    main()
