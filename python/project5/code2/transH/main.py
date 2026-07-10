from __future__ import annotations

import argparse
import subprocess
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parent
PYTHON = sys.executable


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Unified command entry for dual-domain lifecycle traceability.")
    parser.add_argument(
        "--mode",
        choices=["generate_data", "build_triples", "train", "reason", "export_graph", "report", "demo", "full"],
        default="demo",
    )
    parser.add_argument("--domain", choices=["bearing", "hydraulic", "all"], default="all")
    parser.add_argument("--case_id", default="latest")
    parser.add_argument("--epochs", type=int, default=100)
    parser.add_argument("--embedding_dim", type=int, default=64)
    parser.add_argument("--quick", action="store_true", help="Use epochs=5 and embedding_dim=32 for quick training tests.")
    return parser.parse_args()


def command(script: str, *args: str) -> list[str]:
    return [PYTHON, str(ROOT / script), *args]


def run_steps(steps: list[tuple[str, list[str]]]) -> None:
    total = len(steps)
    for index, (title, cmd) in enumerate(steps, start=1):
        print(f"\n[{index}/{total}] {title}", flush=True)
        print("Command:", " ".join(cmd), flush=True)
        result = subprocess.run(cmd, cwd=ROOT)
        if result.returncode != 0:
            print("\nFAILED")
            print("失败命令:", " ".join(cmd))
            print("返回码:", result.returncode)
            print("请检查对应脚本:", cmd[1] if len(cmd) > 1 else cmd[0])
            raise SystemExit(result.returncode)
        print("OK")


def domains_for(domain: str) -> list[str]:
    return ["bearing", "hydraulic"] if domain == "all" else [domain]


def generate_data_steps(domain: str) -> list[tuple[str, list[str]]]:
    steps: list[tuple[str, list[str]]] = []
    if domain in {"bearing", "all"}:
        steps.append(("Running data generation for bearing...", command("scripts/generate_bearing_data.py")))
    if domain in {"hydraulic", "all"}:
        steps.append(("Running data generation for hydraulic...", command("scripts/generate_hydraulic_data.py")))
    return steps


def build_triples_steps(domain: str) -> list[tuple[str, list[str]]]:
    if domain == "all":
        return [("Running triple build for all domains...", command("scripts/build_triples.py", "--domain", "all"))]
    return [(f"Running triple build for {domain}...", command("scripts/build_triples.py", "--domain", domain))]


def train_steps(domain: str, epochs: int, embedding_dim: int, quick: bool) -> list[tuple[str, list[str]]]:
    if quick:
        epochs = 5
        embedding_dim = 32
    steps = []
    for item in domains_for(domain):
        steps.append(
            (
                f"Running TransH training for {item}...",
                command(
                    "scripts/train_transh.py",
                    "--domain",
                    item,
                    "--epochs",
                    str(epochs),
                    "--embedding_dim",
                    str(embedding_dim),
                ),
            )
        )
    return steps


def reason_steps(domain: str, case_id: str) -> list[tuple[str, list[str]]]:
    if domain == "all":
        return [
            ("Running reasoning for bearing...", command("scripts/run_reasoning.py", "--domain", "bearing", "--case_id", "random")),
            ("Running reasoning for hydraulic...", command("scripts/run_reasoning.py", "--domain", "hydraulic", "--case_id", "random")),
        ]
    return [(f"Running reasoning for {domain}...", command("scripts/run_reasoning.py", "--domain", domain, "--case_id", case_id))]


def export_graph_steps(domain: str, case_id: str) -> list[tuple[str, list[str]]]:
    if domain == "all":
        return [
            ("Running graph export for bearing...", command("scripts/export_graph.py", "--domain", "bearing", "--case_id", "latest")),
            ("Running graph export for hydraulic...", command("scripts/export_graph.py", "--domain", "hydraulic", "--case_id", "latest")),
        ]
    return [(f"Running graph export for {domain}...", command("scripts/export_graph.py", "--domain", domain, "--case_id", case_id))]


def report_steps(domain: str, case_id: str) -> list[tuple[str, list[str]]]:
    if domain == "all":
        return [
            ("Running report generation for bearing...", command("scripts/generate_report.py", "--domain", "bearing", "--case_id", "latest")),
            ("Running report generation for hydraulic...", command("scripts/generate_report.py", "--domain", "hydraulic", "--case_id", "latest")),
        ]
    return [(f"Running report generation for {domain}...", command("scripts/generate_report.py", "--domain", domain, "--case_id", case_id))]


def demo_steps(domain: str) -> list[tuple[str, list[str]]]:
    steps: list[tuple[str, list[str]]] = []
    for item in domains_for(domain):
        steps.extend(
            [
                (f"Running reasoning demo for {item}...", command("scripts/run_reasoning.py", "--domain", item, "--case_id", "random")),
                (f"Running graph export demo for {item}...", command("scripts/export_graph.py", "--domain", item, "--case_id", "latest")),
                (f"Running report generation demo for {item}...", command("scripts/generate_report.py", "--domain", item, "--case_id", "latest")),
            ]
        )
    return steps


def full_steps(domain: str, epochs: int, embedding_dim: int, quick: bool) -> list[tuple[str, list[str]]]:
    steps: list[tuple[str, list[str]]] = []
    steps.extend(generate_data_steps(domain))
    steps.extend(build_triples_steps(domain))
    steps.extend(train_steps(domain, epochs, embedding_dim, quick))
    steps.extend(reason_steps(domain, "random"))
    steps.extend(export_graph_steps(domain, "latest"))
    steps.extend(report_steps(domain, "latest"))
    return steps


def steps_for(args: argparse.Namespace) -> list[tuple[str, list[str]]]:
    if args.mode == "generate_data":
        return generate_data_steps(args.domain)
    if args.mode == "build_triples":
        return build_triples_steps(args.domain)
    if args.mode == "train":
        return train_steps(args.domain, args.epochs, args.embedding_dim, args.quick)
    if args.mode == "reason":
        return reason_steps(args.domain, args.case_id)
    if args.mode == "export_graph":
        return export_graph_steps(args.domain, args.case_id)
    if args.mode == "report":
        return report_steps(args.domain, args.case_id)
    if args.mode == "demo":
        return demo_steps(args.domain)
    if args.mode == "full":
        return full_steps(args.domain, args.epochs, args.embedding_dim, args.quick)
    raise ValueError(f"Unsupported mode: {args.mode}")


def print_key_directories(domain: str) -> None:
    print("\nKey output directories:")
    selected = domains_for(domain)
    for item in selected:
        print(ROOT / "outputs" / item / "reports")
        print(ROOT / "outputs" / item / "graphs")


def main() -> None:
    args = parse_args()
    steps = steps_for(args)
    if not steps:
        print("No steps to run.")
        return
    run_steps(steps)
    print_key_directories(args.domain)
    print("\nAll requested steps completed.")


if __name__ == "__main__":
    main()
