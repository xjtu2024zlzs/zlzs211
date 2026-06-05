# -*- coding: utf-8 -*-
"""
Screen Bosch numeric features by univariate association with Response.

This helper is used to choose better variables for the anomaly detector.
It is data-related only and does not replace the MemAE algorithm.
"""

from __future__ import annotations

import argparse
import re
from pathlib import Path

import numpy as np
import pandas as pd


FEATURE_RE = re.compile(r"^L(?P<line>\d+)_S(?P<station>\d+)_F(?P<feature>\d+)$")


def station_name(col: str) -> str:
    m = FEATURE_RE.match(col)
    return f"L{m.group('line')}_S{m.group('station')}" if m else ""


def main():
    parser = argparse.ArgumentParser(description="Screen Bosch features by Response association")
    parser.add_argument("--train_numeric_csv", required=True)
    parser.add_argument("--max_rows", type=int, default=200000)
    parser.add_argument("--out_dir", default="bosch_feature_screening")
    parser.add_argument("--min_failed_observed", type=int, default=2)
    parser.add_argument("--min_normal_observed", type=int, default=50)
    args = parser.parse_args()

    out_dir = Path(args.out_dir)
    out_dir.mkdir(parents=True, exist_ok=True)
    df = pd.read_csv(args.train_numeric_csv, nrows=args.max_rows if args.max_rows > 0 else None)
    y = pd.to_numeric(df["Response"], errors="coerce").fillna(0).astype(int)
    rows = []
    for col in [c for c in df.columns if FEATURE_RE.match(c)]:
        x = pd.to_numeric(df[col], errors="coerce")
        mask0 = (y == 0) & x.notna()
        mask1 = (y == 1) & x.notna()
        if mask0.sum() < args.min_normal_observed or mask1.sum() < args.min_failed_observed:
            continue
        std = float(x[mask0 | mask1].std())
        if not np.isfinite(std) or std <= 1e-12:
            continue
        mean0 = float(x[mask0].mean())
        mean1 = float(x[mask1].mean())
        effect = abs(mean1 - mean0) / std
        rows.append(
            {
                "feature_col": col,
                "station": station_name(col),
                "observed_count": int(x.notna().sum()),
                "failed_observed_count": int(mask1.sum()),
                "normal_mean": mean0,
                "failed_mean": mean1,
                "effect_size": effect,
            }
        )
    ranking = pd.DataFrame(rows).sort_values("effect_size", ascending=False)
    ranking.to_csv(out_dir / "bosch_response_feature_ranking.csv", index=False, encoding="utf-8-sig")
    station_best = ranking.groupby("station", as_index=False).head(1).sort_values("effect_size", ascending=False)
    station_best.to_csv(out_dir / "bosch_station_best_response_features.csv", index=False, encoding="utf-8-sig")
    print(ranking.head(20).to_string(index=False))


if __name__ == "__main__":
    main()
