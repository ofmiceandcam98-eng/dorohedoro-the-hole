#!/usr/bin/env bash
# Runs the unattended client test with the real FTB mods installed.
#   tools/ftb-client.sh <folder with the FTB jars>
# The folder is a CurseForge instance's mods folder (or any folder) holding at least Architectury, FTB Library, FTB Teams
# and FTB Quests; FTB Chunks, Ultimine, XMod Compat, Essentials, Filter System and PlayerNBT Quests are used when
# present, and the checks that need a missing mod are skipped. The jars are copied into runs/qaclient-ftb/mods (ignored
# by git; FTB jars are never committed). Screenshots land in runs/qaclient-ftb/screenshots/ and the log in
# runs/qaclient-ftb/client.log.
set -u
src="${1:?folder with FTB jars required}"
dir="runs/qaclient-ftb"
mkdir -p "$dir/mods"
rm -f "$dir/mods/"*.jar
rm -rf "$dir/saves" "$dir/config/ftbquests" "$dir/screenshots"
found=0
for pattern in 'architectury-*neoforge*.jar' 'ftb-library-neoforge-*.jar' 'ftb-teams-neoforge-*.jar' 'ftb-quests-neoforge-*.jar' \
    'ftb-chunks-neoforge-*.jar' 'ftb-ultimine-neoforge-*.jar' 'ftb-xmod-compat-neoforge-*.jar' 'ftb-filter-system-neoforge-*.jar' \
    'ftb-essentials-neoforge-*.jar*' 'playernbtftb-*.jar'; do
  for f in "$src"/$pattern; do
    [ -f "$f" ] || continue
    name=$(basename "$f")
    cp "$f" "$dir/mods/${name%.disabled}"
    echo "installed ${name%.disabled}"
    found=$((found + 1))
  done
done
for need in architectury ftb-library ftb-teams ftb-quests; do
  ls "$dir/mods/$need"*.jar > /dev/null 2>&1 || { echo "missing $need jar in $src"; exit 1; }
done
echo "$found jars installed"
./gradlew --no-daemon runQaClientFtb > "$dir/client.log" 2>&1
rc=$?
echo "gradle exit $rc"
grep -a "CLIENTQA" "$dir/client.log" | sed 's/.*CLIENTQA /  /'
echo "errors: $(grep -ac '/ERROR\]' "$dir/client.log")  exceptions: $(grep -acE 'Exception|Caused by' "$dir/client.log")"
# FTB writes team progress when the world is saved on exit, so quest completion is checked here, after the game closes.
# Expected: origin, scrap, salt and sulfur, the whole door chapter up to the Sorcerers' World (the last two earned before
# their prerequisites, so they must complete once those are done). Not: Hydra Forest (never visited), safe return from Hell.
node tools/ftbquests/check-progress.mjs "$dir/saves/clientqa-8675309" "1:1,1:2,1:4,3:20,3:21,3:22,3:23,3:24,3:25,3:26" "3:27,4:36"
