from __future__ import annotations

import argparse
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path


class SpaHandler(SimpleHTTPRequestHandler):
    dist_dir: Path

    def translate_path(self, path: str) -> str:
        route = path.split("?", 1)[0].split("#", 1)[0].lstrip("/")
        candidate = (self.dist_dir / route).resolve()
        try:
            candidate.relative_to(self.dist_dir)
        except ValueError:
            return str(self.dist_dir / "index.html")
        if route and candidate.exists():
            return str(candidate)
        return str(self.dist_dir / "index.html")


def main() -> None:
    parser = argparse.ArgumentParser(description="Serve built frontend assets with SPA fallback.")
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=5173)
    parser.add_argument(
        "--dist",
        default=str(Path(__file__).resolve().parents[1] / "frontend" / "dist"),
        help="Path to frontend dist directory.",
    )
    args = parser.parse_args()

    dist_dir = Path(args.dist).resolve()
    if not (dist_dir / "index.html").exists():
        raise SystemExit(f"frontend dist index.html not found: {dist_dir}")

    SpaHandler.dist_dir = dist_dir
    server = ThreadingHTTPServer((args.host, args.port), SpaHandler)
    print(f"Serving {dist_dir} at http://{args.host}:{args.port}", flush=True)
    server.serve_forever()


if __name__ == "__main__":
    main()
