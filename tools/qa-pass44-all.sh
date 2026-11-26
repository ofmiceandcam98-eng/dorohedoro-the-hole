#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
python tools/qa-pass44-source.py
bash tools/qa-pass44-facades.sh
python tools/qa-pass43-source.py
bash tools/qa-pass43-placement.sh
python tools/qa-pass42-source.py
bash tools/qa-pass42-region.sh
python tools/qa-pass41-static.py
bash tools/qa-frontage41.sh
bash tools/qa-facade41.sh
python tools/qa-pass35-inhabited-night-static.py
printf '\nPass 44 source-only QA completed; NO NeoForge build or Minecraft session was run.\n'
