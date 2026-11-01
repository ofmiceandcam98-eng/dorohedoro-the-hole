# Pass 19 — Lore-grounded architecture and original materials

## Status
Implemented in source, **not compiled or verified in Minecraft**. This is a complete-source handoff, not a release JAR. Based on Dorohedoro manga/anime environmental direction and generic real-world dense urban architecture. No original manga panels, official background textures, branded decals, or third-party game assets were imported.

## Changes in this source checkpoint
- New preset uses `world_detail_version: 8`. The `CitySettings` codec accepts 0–8. Existing worlds with saved 0–7 retain prior generation code paths and familiar block IDs. Do **not** change saved generator settings or `level.dat` manually.
- Six distinct original, survival-craftable construction blocks: smoke-stained concrete, water-damaged plaster, soot-blackened brick, patchwork masonry, rusted industrial sheet, seepage brick. Each has its own PNG texture, blockstate, model, name, crafting recipe, loot/self-drop and pickaxe tag.
- Each building selects ONE coherent material palette deterministically per lot, chosen by district rather than independently randomized block-by-block. This eliminates a major source of screenshot checkerboard facades in the new preset.
- Concrete/plaster tenements dominate Residential; old shopfronts and stained brick dominate Market; soot-brick and rust sheet dominate Industrial; wet masonry and wood support Waterfront; patched ruins dominate Derelict; dark old stone Graveyard; patched brick/plaster Outskirts.
- Roads, sidewalks and yard surfaces are more coherent, with infrequent damage clusters and drains rather than uniform block-level random mixes. Tram rail states remain at `GROUND_Y` and are not intentionally replaced by surfacing at `STREET_Y`.
- New-world buildings gain deliberately sparse and safe external utility fixtures, wall risers, rooftop tanks and aerials, small balconies and one muted shop canopy. These preserve the main stairwell and door.
- Cavern Lamp now uses the built-in lantern mechanics with original standing / hanging 3-D model files instead of a cube, and existing crafting/light behavior is retained.
- Prior Pass 18 portal landing height correction is preserved. **The old portal screenshot is NOT evidence of a current portal bug**; no new portal rewrite is attempted here.

## Important nonclaims
- Full recipe/item utility overhaul, all creature remodels, custom district-specific functional interiors, anime-perfect architecture, NPC/faction systems, Ash-Snow/Sludge-Ice, Cross-Eye camps, and live weather effects are NOT implemented in Pass 19.
- We have not run NeoForge's Gradle build, `runData`, game/client, or multiplayer. The Gradle wrapper needs a distribution download, which is blocked by the current environment's DNS/network access. Static checks cannot prove the runtime behavior.
- Copyright permission/asset provenance and CurseForge moderation eligibility require separate review before public release.

## Test matrix for Claude or the developer
1. Open a backed-up newly created world with the `normal` Hole preset; verify the saved generator uses world detail version 8, and districts visually differ at street level.
2. Render tenement, apartment, shop, clinic, warehouse, factory, landmark, market, waterfront and derelict streets from both near and far; check for floating/occluding trim, inaccessible doors, blocked stairs and cut-through walls.
3. Check neighborhoods in all seven districts; specifically verify no brightly striped old awning over the new muted awning and no uniform turquoise/maroon checkerboard facades.
4. Verify tram tracks, drains, sidewalks, sewer stairwells, canals, bridge entries, and all generated landmarks remain functional.
5. In Creative and Survival, place and break each new block, craft all six building materials, and confirm drops, harvesting tool and textures. Inspect model/item inventory icons.
6. Place Cavern Lamp on top of a block and hang it below a solid block; confirm geometry, waterlogging, rotation, light level 13, collision and drop item on breaking.
7. Confirm prior worlds using old generation version still load and have their existing layout; **backup first**. Versioned code does not prevent changing block behavior in old chunks when the block itself changed (Cavern Lamp).
8. Test Artificial and Infernal portal round trips with the actual **newly compiled** JAR (older screenshot is out of date).
9. Run `./gradlew runData` to regenerate all models/loot/tags/recipes, `./gradlew build`, inspect output JAR, test NeoForge 21.1.265 on client and dedicated server.
10. For public upload: verify licenses and original assets, mark untested builds as development snapshots only.

## Future depth (not yet implemented)
The companion document `DEEP_WORLD_BLUEPRINT.md` covers density, purpose, discoverability, gameplay loops, detailed location types, faction behavior, magic, and survival systems. The user's acceptance order must be followed.
