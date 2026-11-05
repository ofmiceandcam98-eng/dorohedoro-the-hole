# Pass 19 validation notes and limitations

- `tools/qa-lore-architecture-pass19.py`: **59/59 passed**, static checks only. JSON parsing, new block assets, recipes, loot tables, version gate, existing portal check existence and project benchmark docs verified.
- `tools/qa-portal-landing-pass18.py`: **12/12 passed**, static source assertions. An older screenshot of portal-placement failure was explicitly identified by user as OUTDATED; do not list it as an open current problem without reproduction.
- Older Pass 12/14/15 source QA scripts count 24/25, 52/54, 22/23 because those tests were written for older preset-version upper bounds and do not accept the new Pass19 `worldDetailVersion=8`. The failures do not independently prove generator regression, but those older scripts should be updated and rerun during proper QA rather than ignored.
- Gradle build could NOT run: `UnknownHostException: services.gradle.org`, attempting to download Gradle 9.2.1. Therefore no compile, data generation, in-game worldgen, lighting, recipes, client/server or multiplayer tests have passed in this environment. Do not upload or label as tested release.
- The first-join origin UI, leveling, mastery, combat trees, mastery-based gear growth, full creature remodelling, advanced interiors and factions are **designed only**. See `TENSURA_REINCARNATED_DEPTH_STANDARD.md` and `TENSURA_DEPTH_FEATURE_MATRIX.csv`.
- The current source introduces coherent building materials, six new craftable blocks, road/palette changes, and LanternBlock models for Cavern Lamp; source compile/runtime results remain unknown.
- New world generation uses world detail version 8; existing saved worlds keep their previous version, but new materials in newly generated areas may not appear in already-explored terrain. Back up every save first.
- Public CurseForge publishing also requires licensing/rights review for all assets, models, texts and franchise IP.
