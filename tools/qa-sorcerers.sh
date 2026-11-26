#!/usr/bin/env bash
# Sorcerers' World settlement QA on a real dedicated server, in both chunk orders, then a determinism comparison.
#   bash tools/qa-sorcerers.sh <seed> [hole_radius]
# The settlement checks run inside the normal headless QA (HoleQa -> SorcererTownQa). The Hole part uses a small
# radius here to save time; the Hole checks it prints are not about settlements.
set -u
seed="${1:?seed required}"
radius="${2:-4}"
for order in forward reverse; do bash tools/qa-world.sh "$seed" "$radius" "$order"; done
node -e "
const fs=require('fs');
const read=o=>JSON.parse(fs.readFileSync('runs/qa/$seed/'+o+'/qa/report-'+o+'.json','utf8'));
const f=read('forward'), r=read('reverse');
const sf=f.sorcerer_settlements||{}, sr=r.sorcerer_settlements||{};
console.log('survey:', JSON.stringify(sf.survey));
console.log('biome % at y80:', JSON.stringify(sf.biome_percent_at_y80));
for (const t of (sf.settlements||[])) console.log(t.name+': '+t.buildings+' buildings, '+t.rooms+' rooms, '+t.villagers+' villagers ('+t.villagers_with_trade+' with a trade), '+t.loot_containers+' loot containers, '+t.shop_signs+' signs, '+t.ms_per_chunk.toFixed(1)+' ms/chunk');
for (const c of f.checks) if ((c.name.startsWith('sw_')||c.name.includes('@'))) console.log((c.passed?'  PASS ':'  FAIL ')+c.name+': '+c.detail);
const keys=Object.keys(sf.chunk_hashes||{}); let same=0; const diff=[];
for (const k of keys) { if ((sr.chunk_hashes||{})[k]===sf.chunk_hashes[k]) same++; else diff.push(k); }
console.log('determinism: '+same+'/'+keys.length+' settlement chunks identical forward vs reverse'+(diff.length?' MISMATCH '+diff.join(' '):''));
process.exit(diff.length?1:0);
"
