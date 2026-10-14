#!/usr/bin/env python3
"""Export the complete agent documentation package without application/build files."""
import argparse
import shutil
from pathlib import Path

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("destination", type=Path)
args = parser.parse_args()
root = Path(__file__).resolve().parents[1]
destination = args.destination.expanduser().resolve()
if destination == root:
    parser.error("Choose a destination outside the source repository root.")
for relative in ("AGENTS.md", "README.md", "docs/ARCHITECTURE.md", "docs/PROJECT_CONTEXT.md", "docs/DECISIONS.md"):
    target = destination / relative
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(root / relative, target)
print(f"Exported five documentation files to {destination}")
