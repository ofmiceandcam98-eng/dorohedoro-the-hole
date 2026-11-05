# Pass 20.1 — Full changed-file manifest

**Baseline:** Pass 19.9 full source (latest packaged release before the Pass 20 work). **NOT COMPILED.**

## Modified (4)
- `src/main/java/com/dorohedoro/hole/client/magic/SmokeLedgerScreen.java`
- `src/main/java/com/dorohedoro/hole/magic/SmokeEngine.java`
- `src/main/java/com/dorohedoro/hole/magic/SmokeSkill.java`
- `src/main/java/com/dorohedoro/hole/magic/SmokeVisuals.java`

## Added (4, including this manifest)
- `SOURCE_STATUS_PASS20_1.md`
- `src/main/java/com/dorohedoro/hole/magic/MushroomGrowthSavedData.java`
- `src/main/java/com/dorohedoro/hole/magic/MushroomSpellVisuals.java`
- `CHANGED_FILES_PASS20_1.md`

## Removed (0)
- None

## Implementation summary
- Replaces visual-only mushroom block displays with real red/brown mushroom and giant cap/stem blocks, with vanilla collision and drops.
- Mastery controls longevity and giant size; intermediate growth expires, advanced growth is permanent.
- Added dimension-scoped SavedData to persist temporary growth expiry over server restarts without force-loading chunks.
- Connected mastery from SmokeEngine -> SmokeVisuals -> MushroomSpellVisuals and registered server tick/harvest handlers.
- Added a Smoke Ledger mushroom permanence hint and updated Mushroom skill descriptions.
- Preserved 64 techniques, innate-type restrictions, 3 slots × 9 presets, existing world generation, and the portal landing source fix.

**Note:** Static checks do not establish compilation, API compatibility or in-game operation. See `SOURCE_STATUS_PASS20_1.md`.
