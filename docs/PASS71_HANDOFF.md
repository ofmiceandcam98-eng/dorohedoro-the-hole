# Pass 71 handoff (2026-11-04)

Branch `pass-70-neoforge-build` on GitHub, PR #1 into `gate-0-1-hole-world`. Everything below compiles, passed
`runData` and `build`, and was checked in the real client (`./gradlew runQaClient`) and on a dedicated server
(`bash tools/qa-world.sh 8675309 6`). The last client run had 0 errors and 0 exceptions and produced 51 screenshots.

## Release

- Jar: `build/libs/Dorohedoro The Hole-BETA0.2.jar`. Naming comes from `release_title`, `release_channel` and
  `release_number` in `gradle.properties`. `mod_version` is `0.2.0-beta`.
- Upload kit: `C:\dev\curseforge-upload`, holding the jar, `CHANGELOG-BETA0.2.md`, gallery shots and a one-click
  installer for the owner's launcher profile.
- **CurseForge is not uploaded yet.** The owner is signed in to authors.curseforge.com in their own browser, but the
  Claude in Chrome extension was not connected. Upload to project 1777423 as **Beta**: game version 1.21.1, NeoForge,
  Java 21.

## What changed in Pass 71

- **Stairs:** `Tower.keepStairsWalkable` runs after all decoration. The QA check `stair_flights_walkable` went from
  14 of 14 blocked to 0 of 14.
- **Wall lamps:** `CavernLampBlock` hitboxes, blockstate rotations and the placement fallback are fixed.
- **v53 worldgen, new worlds only:**
  - `CityTerrain40` has no regional escarpments and caps relief at 24.
  - `Mix` soil palettes go through `Canvas.patchHash`.
  - `RoomDressing71` furnishes one feature wall plus tables per storey.
  - The `Landmarks.regionalLanding` spawn keeps only its supply barrel.
- **UI:**
  - `client/ui/HoleHud` is the HUD.
  - `HoleUiTheme` and `HoleMenuButton` are sprite-based.
  - `SmokeLedgerScreen` has 4 tabs (Status, Techniques, Loadouts, Body), and each one states its purpose.
  - All sprites come from `node tools/ui/gen-ui.mjs`.
- **Art:**
  - `tools/ui/to-vanilla.mjs` converted items and building blocks to 16px. The magic portal and door blocks stay 32px.
  - Ores use `models/block/ore_overlay.json`, which puts vanilla stone or deepslate under an overlay from
    `tools/ui/gen-ores.mjs`.
  - There are 4 new deepslate ore blocks.
- **Items:**
  - `item/HoleBrewing` adds 6 brews.
  - New recipes: antidote poultice, empty smoke flask, tar torches, hide to leather, and cooked limbs.
  - Tar is fuel, via `data/neoforge/data_maps/item/furnace_fuels.json`.
  - `client/ui/ItemUseTooltips` shows a gray "what it's for" line from the `item.dorohedoro.<id>.use` lang keys.
- **Mobs:** fitting vanilla sounds, `HurtByTargetGoal`, pouncing roaches and spore crawlers, a fleeing rat, a beetle
  tempted by scrap, spore bursts, and a glow layer on the moth.
- **QA:**
  - `HoleQa` checks stair flights and treats thin snow as walkable.
  - `ClientQa` covers the origin screen, city tour, rooms, HUD, all menu tabs, items, the block wall and every mob
    from 4 angles.

## Owner preferences learned this pass

- Quality over everything; Tensura Neo Otherworld is the bar for UI polish. The UI must be original and
  Dorohedoro-themed.
- Use American spelling in player-facing text.
- Name release files like `Dorohedoro The Hole-BETA0.2.jar`, and use BETA, not alpha.
- No loose clutter: rooms and spawn areas should look deliberately arranged.
- Every item needs a real use.

## Open

1. The ocean monument search is slow (more than 10 minutes). It has a proposed task card.
2. Upload to CurseForge. The modpack question is still unanswered: the account has no modpack project.
3. The owner should play-test BETA 0.2 rooms, HUD and mobs, then iterate on the art. Mob textures are original but
   could get a vanilla-style repaint pass. The Cavern Burrower is the plainest model.
4. The older interior passes (24–39) still place some furniture of their own under `RoomDressing71`.
