import argparse
import csv
import json
import os
import random
import sys
from collections import defaultdict
from pathlib import Path


CURRENT_DIR = Path(__file__).resolve().parent
PROJECT_ROOT = CURRENT_DIR.parents[2]

if str(PROJECT_ROOT) not in sys.path:
    sys.path.append(str(PROJECT_ROOT))
if str(CURRENT_DIR) not in sys.path:
    sys.path.append(str(CURRENT_DIR))


def set_seed(seed):
    os.environ["PYTHONHASHSEED"] = str(seed)
    random.seed(seed)

    try:
        import numpy as np

        np.random.seed(seed)
    except ImportError:
        pass

    try:
        import torch

        torch.manual_seed(seed)
        if torch.cuda.is_available():
            torch.cuda.manual_seed_all(seed)
    except ImportError:
        pass


def split_indices_by_label(labels, val_ratio=0.2, seed=42):
    if not 0 <= val_ratio < 1:
        raise ValueError("--val_ratio must be in the range [0, 1)")

    label_to_indices = defaultdict(list)
    for index, label in enumerate(labels):
        label_to_indices[int(label)].append(index)

    rng = random.Random(seed)
    train_indices = []
    val_indices = []

    for label in sorted(label_to_indices):
        indices = list(label_to_indices[label])
        rng.shuffle(indices)

        if val_ratio == 0 or len(indices) < 4:
            val_count = 0
        else:
            val_count = max(2, int(round(len(indices) * val_ratio)))
            val_count = min(val_count, len(indices) - 2)

        val_indices.extend(indices[:val_count])
        train_indices.extend(indices[val_count:])

    rng.shuffle(train_indices)
    rng.shuffle(val_indices)
    return train_indices, val_indices


def _collate_text_label(batch):
    return [item[0] for item in batch], [item[1] for item in batch]


def _build_balanced_loader(dataset, indices, labels, batch_size):
    import numpy as np
    from torch.utils.data import DataLoader, Subset
    from train_utils import BalancedBatchSampler

    subset = Subset(dataset, indices)
    subset_labels = [labels[index] for index in indices]
    n_classes = len(np.unique(subset_labels))
    if n_classes == 0:
        raise ValueError("Training split is empty.")

    effective_batch_size = min(batch_size, n_classes * 2)
    effective_batch_size = max(2, (effective_batch_size // 2) * 2)
    if effective_batch_size != batch_size:
        print(
            f"Adjusted training batch size from {batch_size} to "
            f"{effective_batch_size} for {n_classes} classes."
        )

    sampler = BalancedBatchSampler(
        subset_labels,
        batch_size=effective_batch_size,
        n_samples_per_class=2,
    )
    return DataLoader(subset, batch_sampler=sampler, collate_fn=_collate_text_label)


def _build_eval_loader(dataset, indices, batch_size):
    from torch.utils.data import DataLoader, Subset

    subset = Subset(dataset, indices)
    return DataLoader(
        subset,
        batch_size=batch_size,
        shuffle=False,
        collate_fn=_collate_text_label,
    )


def _write_metrics_header(log_path):
    log_path.parent.mkdir(parents=True, exist_ok=True)
    with log_path.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(
            handle,
            fieldnames=[
                "epoch",
                "train_loss",
                "val_accuracy",
                "val_recall_at_gt",
                "val_mrr",
                "val_score",
                "is_best",
            ],
        )
        writer.writeheader()


def _append_metrics(log_path, row):
    with log_path.open("a", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(
            handle,
            fieldnames=[
                "epoch",
                "train_loss",
                "val_accuracy",
                "val_recall_at_gt",
                "val_mrr",
                "val_score",
                "is_best",
            ],
        )
        writer.writerow(row)


def train_model(
    model,
    num_classes,
    train_loader,
    optimizer,
    model_path,
    loss_type="triplet",
    margin=1,
    epochs=100,
    val_loader=None,
    patience=5,
    metrics_path=None,
):
    import torch
    from sentence_transformers import losses
    from tqdm import tqdm

    from eval import evaluate_metrics
    from train_utils import SimCLRLoss

    if val_loader is None:
        val_loader = train_loader
    if metrics_path is None:
        metrics_path = Path(model_path).with_suffix(".metrics.csv")

    model_path = Path(model_path)
    metrics_path = Path(metrics_path)
    model_path.parent.mkdir(parents=True, exist_ok=True)
    _write_metrics_header(metrics_path)

    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    model.to(device)

    if loss_type == "triplet":
        loss_fn = losses.BatchHardTripletLoss(
            model=model,
            margin=margin,
            distance_metric=losses.BatchHardTripletLossDistanceFunction.cosine_distance,
        )
    elif loss_type == "simclr":
        loss_fn = SimCLRLoss(model=model, temperature=0.5)
    else:
        raise ValueError(f"Unsupported loss_type: {loss_type}")

    best_score = float("-inf")
    epochs_without_improvement = 0

    for epoch in range(1, epochs + 1):
        model.train()
        total_loss = 0.0
        batch_count = 0

        for texts, labels in tqdm(
            train_loader, desc=f"Epoch {epoch}/{epochs}", unit="batch"
        ):
            labels = torch.tensor(labels, dtype=torch.long, device=device)

            optimizer.zero_grad()
            sentence_features = model.tokenize(texts)
            sentence_features = [
                {key: value.to(device) for key, value in sentence_features.items()}
            ]
            loss = loss_fn(sentence_features, labels)
            loss.backward()
            optimizer.step()

            total_loss += float(loss.item())
            batch_count += 1

        avg_loss = total_loss / batch_count if batch_count else 0.0
        val_accuracy, val_recall_at_gt, val_mrr = evaluate_metrics(
            model, val_loader, device, fixed_k=1
        )
        val_score = (val_recall_at_gt + val_mrr) / 2
        is_best = val_score > best_score

        if is_best:
            best_score = val_score
            epochs_without_improvement = 0
            torch.save(model.state_dict(), model_path)
        else:
            epochs_without_improvement += 1

        _append_metrics(
            metrics_path,
            {
                "epoch": epoch,
                "train_loss": f"{avg_loss:.8f}",
                "val_accuracy": f"{val_accuracy:.8f}",
                "val_recall_at_gt": f"{val_recall_at_gt:.8f}",
                "val_mrr": f"{val_mrr:.8f}",
                "val_score": f"{val_score:.8f}",
                "is_best": int(is_best),
            },
        )

        print(
            f"Epoch {epoch}: loss={avg_loss:.6f}, "
            f"val_accuracy={val_accuracy:.4f}, "
            f"val_recall_at_gt={val_recall_at_gt:.4f}, "
            f"val_mrr={val_mrr:.4f}, val_score={val_score:.4f}"
        )

        if patience is not None and epochs_without_improvement >= patience:
            print(f"Early stopping after {patience} epochs without improvement.")
            break

    print(f"Training complete. Best validation score: {best_score:.4f}")
    print(f"Best model saved to: {model_path}")
    print(f"Training metrics saved to: {metrics_path}")
    return best_score


def _resolve_dataset_path(dataset):
    file_path = PROJECT_ROOT / "data" / "synthetic" / f"{dataset}_synthetic_matches.json"
    if file_path.exists():
        return file_path

    legacy_path = Path("data") / "synthetic" / f"{dataset}_synthetic_matches.json"
    if legacy_path.exists():
        return legacy_path

    raise FileNotFoundError(f"Synthetic training data not found: {file_path}")


def _resolve_output_dir(args):
    if args.output_dir:
        output_dir = Path(args.output_dir)
        if not output_dir.is_absolute():
            output_dir = PROJECT_ROOT / output_dir
        return output_dir

    run_name = f"{args.dataset}_{args.model_type}_{args.serialization}"
    return PROJECT_ROOT / "results" / "finetune" / run_name


def main():
    parser = argparse.ArgumentParser(
        description="Fine-tune a SentenceTransformer model for schema matching."
    )
    parser.add_argument(
        "--dataset",
        default="gdc",
        help="Dataset name, used to load data/synthetic/{dataset}_synthetic_matches.json.",
    )
    parser.add_argument(
        "--model_type",
        default="mpnet",
        help="Base embedding model key, for example roberta or mpnet.",
    )
    parser.add_argument(
        "--serialization",
        default="header_values_repeat",
        help=(
            "Column serialization mode, for example header_values_default, "
            "header_values_repeat, header_values_verbose, or "
            "header_values_verbose_with_table."
        ),
    )
    parser.add_argument(
        "--augmentation",
        default="exact_semantic",
        help="Augmentation type: exact, semantic, or exact_semantic.",
    )
    parser.add_argument("--epochs", type=int, default=30, help="Training epochs.")
    parser.add_argument("--batch_size", type=int, default=64, help="Training batch size.")
    parser.add_argument(
        "--loss_type",
        default="triplet",
        choices=["triplet", "simclr"],
        help="Loss function.",
    )
    parser.add_argument(
        "--margin",
        type=float,
        default=0.5,
        help="Triplet loss margin.",
    )
    parser.add_argument("--seed", type=int, default=42, help="Random seed.")
    parser.add_argument(
        "--patience",
        type=int,
        default=5,
        help="Early-stopping patience in epochs.",
    )
    parser.add_argument(
        "--val_ratio",
        type=float,
        default=0.2,
        help="Validation ratio split by label.",
    )
    parser.add_argument(
        "--output_dir",
        default=None,
        help="Directory for train_metrics.csv. Relative paths are resolved from project root.",
    )
    args = parser.parse_args()

    set_seed(args.seed)

    import numpy as np
    import torch
    from sentence_transformers import SentenceTransformer

    from dataset import CustomDataset
    from train_utils import sentence_transformer_map

    file_path = _resolve_dataset_path(args.dataset)
    print(f"Loading training data from: {file_path}")
    with file_path.open("r", encoding="utf-8") as handle:
        data = json.load(handle)

    dataset = CustomDataset(
        data,
        model_type=args.model_type,
        serialization=args.serialization,
        augmentation=args.augmentation,
    )

    labels = dataset.labels
    n_classes = len(np.unique(labels))
    train_indices, val_indices = split_indices_by_label(
        labels, val_ratio=args.val_ratio, seed=args.seed
    )
    if not val_indices:
        print("Warning: validation split is empty; evaluating on training data.")
        val_indices = list(train_indices)

    train_loader = _build_balanced_loader(
        dataset, train_indices, labels, batch_size=args.batch_size
    )
    val_loader = _build_eval_loader(dataset, val_indices, batch_size=args.batch_size)

    model = SentenceTransformer(sentence_transformer_map[args.model_type])
    optimizer = torch.optim.AdamW(model.parameters(), lr=1e-5, weight_decay=0.5)

    models_dir = PROJECT_ROOT / "models"
    models_dir.mkdir(parents=True, exist_ok=True)
    if args.loss_type == "triplet":
        model_path = (
            models_dir
            / (
                f"{args.model_type}-{args.dataset}-{args.serialization}-"
                f"{args.augmentation}-{args.batch_size}-{args.margin}.pth"
            )
        )
    else:
        model_path = (
            models_dir
            / (
                f"{args.model_type}-{args.dataset}-{args.serialization}-"
                f"{args.augmentation}-simclr.pth"
            )
        )

    output_dir = _resolve_output_dir(args)
    metrics_path = output_dir / "train_metrics.csv"

    print(f"Training model using {args.model_type} on {args.dataset}")
    print(f"Classes: {n_classes}")
    print(f"Train examples: {len(train_indices)}, validation examples: {len(val_indices)}")
    print(f"Epochs: {args.epochs}, batch size: {args.batch_size}")
    print(f"Model output: {model_path}")
    print(f"Metrics output: {metrics_path}")

    train_model(
        model,
        n_classes,
        train_loader,
        optimizer,
        model_path,
        loss_type=args.loss_type,
        margin=args.margin,
        epochs=args.epochs,
        val_loader=val_loader,
        patience=args.patience,
        metrics_path=metrics_path,
    )


if __name__ == "__main__":
    main()
