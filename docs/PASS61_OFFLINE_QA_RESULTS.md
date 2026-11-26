# Pass 61 — Offline QA evidence

All new source checks and the compiled deterministic policy checks passed, as did the historical offline Pass 41–60 suites in a single runner invocation.

- `python3 tools/qa-pass61-source.py`: **25/25** integration assertions.
- `bash tools/qa-pass61-sewers.sh`: **491,048** Java checks on the actual hash/selection policy; 153,600 sewer intersections with 46,282 cave branches, 27,627 inspection rooms, and 6,942 optional infested rooms. The selected nest fraction is ~25.1% of inspection rooms and ~4.52% of all sampled intersections.
- Light schedule: 7,560 deterministic corridor sections in the fixture; 3,017 unpowered and 4,543 powered sections. Legacy sample 22,680 lamps; fresh-world sample 13,629. This only measures generator placements, **not actual rendered block light or live spawning**.
- `bash tools/qa-pass61-all.sh`: Pass61 tests followed by previous Pass60->Pass41 offline regression chain; final output reports success.
- Source files and ZIPs require true NeoForge `runData`, `build`, client/server and in-world checks on Wednesday. No Minecraft gameplay was executed here.
