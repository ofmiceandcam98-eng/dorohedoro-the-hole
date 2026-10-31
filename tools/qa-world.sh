#!/usr/bin/env bash
# Runs the headless world check on a real dedicated server for one seed.
#   tools/qa-world.sh <seed> [radius_chunks] [forward|reverse]
# Output lands in runs/qa/<seed>/<order>/qa/. The order is a separate world folder so the two runs can be compared.
set -u
seed="${1:?seed required}"
radius="${2:-16}"
order="${3:-forward}"
dir="runs/qa/$seed/$order"
mkdir -p "$dir"
rm -rf "$dir/world" "$dir/qa"
echo "eula=true" > "$dir/eula.txt"
cat > "$dir/server.properties" <<EOF
level-seed=$seed
online-mode=false
spawn-protection=0
view-distance=6
simulation-distance=4
EOF
./gradlew --no-daemon runQaServer -Pqa_dir="$dir" -Pqa_radius="$radius" -Pqa_order="$order" -Pqa_dump="${QA_DUMP:-}" > "$dir/gradle.log" 2>&1
rc=$?
report="$dir/qa/report-$order.json"
if [ ! -f "$report" ]; then
  echo "seed=$seed order=$order: NO REPORT (gradle exit $rc). See $dir/gradle.log"
  exit 1
fi
node -e "
const r=JSON.parse(require('fs').readFileSync('$report','utf8'));
console.log('seed='+r.seed+' order='+r.order+' failures='+r.failures+' chunks='+r.chunks+' ms/chunk='+(r.ms_per_chunk||0).toFixed(1));
for (const c of r.checks) if(!c.passed) console.log('  FAIL '+c.name+': '+c.detail);
"
