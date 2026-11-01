# Pass 29 — changed files relative to the Pass 28 full source

- `src/main/java/com/dorohedoro/hole/worldgen/city/HousingPlan29.java` — NEW pure seeded house planner.
- `src/main/java/com/dorohedoro/hole/worldgen/city/HousingDetail29.java` — NEW actual worldgen façade/roof/interior painter.
- `src/main/java/com/dorohedoro/hole/worldgen/city/LotPainter.java` — integrated housing detail for homes, apartments and upstairs shop residences.
- `src/main/java/com/dorohedoro/hole/worldgen/city/Canvas.java` — feature gate `hasHousing29`.
- `src/main/java/com/dorohedoro/hole/worldgen/city/CitySettings.java` — persisted version 17 supported; older profile defaults unchanged.
- `src/main/resources/data/minecraft/worldgen/world_preset/normal.json` — new worlds use detail version 17.
- `tools/qa-pass29-housing.py` — NEW Java stub compilation, determinism, clearance and chunk split checks.
- `tools/qa-pass28-mixed-lighting.py` — historical Pass28 test accepts later new-world versions >=16.
- `tools/qa-pass24-canal-ecology.py` — historical Pass24 check accepts later detail-version upper bounds.
- `PASS29_WORLD_HOUSING.md`, `CHANGED_FILES_PASS29.md` — implementation and QA notes.
- `README.md` — latest-checkpoint notice.

No other registered creature, magic, server-owned game-mechanic, existing texture PNG or loot-table file was intentionally modified.
