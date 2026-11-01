# Pass 32E — City determinism hash correction (QA only)

## Evidence (seed 12345, forward vs reverse, 625 chunks)

Both worlds passed all 26 QA checks. The Y>=64 hash differed in four chunks,
exactly one block per chunk:

- 8,-12: 128 64 -186 — dirt versus copper ore
- 10,-7: 172 64 -106 — coal ore versus stone
- 12,-8: 201 65 -114 — coal ore versus gravel
- 12,6: 207 64 100 — stone versus coal ore

The Pass 32D material check found no ore or replaceable rock ON/ABOVE the graded
surface. The fixed Y=64 hash was therefore capturing buried natural fill in
raised districts and was not an accurate city/infrastructure fingerprint.

## Changes

- `HoleQa.java`: build expected per-column city ground from CityElevation and
  leveled lot centers BEFORE hashing. Hash Y>=64 with full fidelity for buildings,
  roads, canals, air, water, and every other non-natural block. Only below each
  column's ground level, treat natural ores, overworld stone, dirt, gravel, sand
  and clay as equivalent mineable fill. These blocks are still present in-game.
- Preserve legacy raw Y>=64 hashes in `chunk_hashes_y64_raw` and whole-chunk
  hashes in `chunk_hashes_full` for independent auditing.
- `tools/qa-compare.mjs`: display all three hashes but fail only when the
  structure-aware city hash differs.

**This does not make underground Minecraft ore placement deterministic.**
It correctly distinguishes built city infrastructure from natural buried
geology without masking differences in buildings, city water, air, or streets.

## Codespaces verification

Upload this patch to the repo root, then:

```bash
unzip -o Dorohedoro_Pass32E_Buried_Ore_Determinism_QA_Fix.zip -d . && bash tools/verify-neoforge-build.sh
bash tools/qa-world.sh 12345 12 forward && bash tools/qa-world.sh 12345 12 reverse
node tools/qa-compare.mjs runs/qa/12345/forward/qa/report-forward.json runs/qa/12345/reverse/qa/report-reverse.json
```

Expect `chunk_hashes: 625/625 identical` if the four differences are only
buried natural fill. The raw hash may still show 621/625 and the full hash
may still vary. The actual build and fresh QA result are not yet verified.
