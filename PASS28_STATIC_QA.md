# Pass 28 static QA — 2026-11-01

- 141/141 active Java files passed JDK 21 syntax parsing; this is not typechecking against NeoForge.
- 412/412 JSON resource files parsed successfully.
- Java stub test: 5,000 deterministic fixture choices (2,997 Cavern Lantern and 2,003 vanilla lantern); 300 deterministic furnished room layouts. Center aisle remains empty. One supported torch correctly placed, unsupported torch and seam torch correctly rejected.
- Existing dungeon structural tests: 117/117 passed.
- Existing waterfront-access structural tests: 26/26 passed.
- Existing density/urban-fabric regression harnesses passed, though tests from pre-Pass-28 that hard-code older world-detail-version maxima still need updating and should not be treated as genuine regression failures.
- No Gradle/NeoForge compile, data generation, client render, game spawn, lighting, or multiplayer runtime checks performed.

**Known concern:** There are still no authoritative in-game nighttime screenshots, so visual quality is NOT signed off. Pass 28 changes only new generator worlds (`world_detail_version=16`); older saved generator profiles remain at their persisted version.
