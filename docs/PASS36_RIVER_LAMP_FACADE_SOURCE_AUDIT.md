# Pass 36 — Wider meanders, compact wall-mountable Cavern Lamp, façade depth

**Status:** SOURCE ONLY. The current Pass 36 code has **not** been compiled with NeoForge or inspected in Minecraft. Do NOT publish or merge into CurseForge until runtime validation succeeds. This source is intended to be layered over the prior Pass 32E + Pass 33 + Pass 35 checkpoints. No current Codespace or Minecraft installation was modified when preparing it.

## Grounded in user feedback

The user found: canals/river reaches are visibly straight, buildings look like flat boxes, the Cavern Lamp is oversized and cannot attach to walls, streets and exterior lights are excessive, outdoor chests too visible, rooms need a real functional purpose, and texture/items need native Minecraft-level art. The existing Pass 35 source already handles inhabitable, purposeful rooms, reduced visible storage, and less street illumination; those changes are retained here.

## Implemented in Pass 36

1. **River reservation** (`CityPlan`, `RiverChannels24`): New `world_detail_version=21` worlds use a 17-block half-width municipal waterway reserve instead of 9. The existing river painter's lateral centerline now has seed-dependent two-scale bends up to ten blocks off the old axis instead of three. Width continues to vary, with two blocks reserved on each side for banks, quays and routes. The surrounding lots are constructed outside the reservation; this is not a river overlay that slices into houses. Old world profiles (0–20) retain their original canal width and offset formula. This remains urban canal-river morphology rather than a hydrologically accurate multi-branch watershed. If the latest default world changes after publishing, new worlds will use the widened waterway automatically; previously saved worlds preserve their stored world detail version.
2. **Compact Cavern Lamp** (`CavernLampBlock`, registry, 3D models, blockstates): Handcrafted smaller voxel lantern models for standing, ceiling-hanging, and four-sided wall mounting. The block supports normal right-click placement on supported faces, waterlogging, break-on-support-loss, proper orientation and a matching voxel hitbox. Existing `hanging` and `waterlogged` properties remain present for stored block states, defaulting to floor mount when the new `wall` state is absent. There are no new item/recipe IDs. Existing lamp light output is changed from 13 to 12 globally; material uses stay the same. Runtime save migration/rendering still needs testing.
3. **Purposeful façade geometry** (`FacadeDetail36`, `LotPainter`): New-profile homes gain selectively placed window lintels, long vertical drainage lines and occasional roof ventilation; shops gain deliberately imperfect weathered-copper awnings. No random visible chests or extra street lamps are introduced. Existing door geometry and main paths are left intact, and all added blocks use air-only placement. Features are tied to the new world detail profile, preserving older structure generation.
4. **Default new-world profile** changes from 20 to **21** in the normal world preset. The codec accepts 0–21. Other gameplay, villager/faction economy and Ash-Snow features remain deferred.

## Real checks performed locally (not a NeoForge compile)

- `python tools/qa-pass36-rivers-lamp-static.py` passed **18/18** source/schema checks.
- `python tools/qa-pass36-curve-samples.py` sampled four seeds (`12345`, `8675309`, `-987654321`, `42`) over 2,049 consecutive river blocks each in old/new geometry, checked max neighbor-step, minimum channel width and bank reservation; passed.
- The two pure Java utility classes `CityHash.java` and `Line.java` compile with Java 21. This **does not** demonstrate that `CavernLampBlock.java`, NeoForge or the entire mod compiles.
- The 3 lamp models and 32 blockstate variants are valid JSON; bounds of every model element lie within [0,16]. These checks do **not** prove wall placement or that the block item is visually correct in-game.

## Mandatory integration tests before another player install

1. Run Java 21 and `bash tools/verify-neoforge-build.sh`, and fix any compiler/data-generation errors.
2. Execute `bash tools/qa-world.sh 12345 12 forward` and reverse, run comparator, then repeat with 8675309 and two additional seeds; require 0 QA failures and 625/625 normalized city hashes. Old v20 loads and old river geometry must be spot-checked.
3. In the actual Minecraft 1.21.1 NeoForge client, test lamp placement on floor, ceiling, and each of four wall directions using solid support, waterlogged placement, breaking support and save/reload; confirm no missing model textures, correct hitbox and right-click item behavior.
4. Inspect multiple river bends from above and at water level, then verify contiguous water surface, bridges/canal crossings, bank access, no water spills and fish. Terrain-relief interactions are particularly important: the current river-water Y uses column grading, and stronger hills should not be enabled until flow is validated.
5. Walk every newly decorated building type at night: doors open, three-block passage headroom, no pipe/awning blocks player movement, no unexpected excessive light, no furniture replacement or staircase obstruction.
6. Test actual dark-level hostile mob spawning in survival at night and verify lights remain functional indoors.
7. Review PNG item/block art against vanilla Minecraft pixel readability and the user's reference screenshots. Art-quality overhaul and bespoke technique icons remain unfinished; no claim of final visual fidelity.
8. Diagnose original Curse Mark cast crash from a real `crash-reports/*.txt` (or latest.log). This remains a **hard blocker** for any public update. The first Pass 33 UI source is also uncompiled, and the minimum-HUD changes in the HTML review need to be applied to real UI code.

## Deliberately not included in Pass 36

- Full hill/valley terrain refactor: needs an end-to-end slope, building access and water-surface solution; simply raising `CityElevation` now can flood canals, break tracks or strand buildings.
- Full vanilla-grade item/texture repaint, individual icons for every technique, and full final menu revision: these need the user's visual approval and actual assets rather than placeholders.
- Any unverified fix for the magic crash, changes to old save worldgen, or deferred faction/villager overhaul.
