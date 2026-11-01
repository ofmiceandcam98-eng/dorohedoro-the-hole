# Phase 4 — portals and first original item set (October 31, 2026)

**Status: source integrated and static checked, not compile or game verified.** This snapshot begins from the previously published Phase 3 integrated source, preserves Hole terrain/interior/smog changes, and introduces first-version Artificial Doors and Devil Gates.

## Source work

- Shared `PortalService`, `PortalKind`, `PortalCoreBlock`, `PortalSurfaceBlock`, `PortalCoreBlockEntity`; `ModBlockEntities` registration.
- Craftable 4×5 Artificial Door and 5×5 Devil Gate frames/cores and functional tool interactions, paired generated return structures (no loot), and a server-controlled landing/linking path.
- Namespaced items, blockstate/model data, original 16×16 textures, tool/ingredient recipe JSON and source datagen definitions.
- NEW: Smoke-Soaked Filter scavenging added to Industrial Debris and three chest tables, and smoke residue smelting. Devil Sigil now has an expensive, pre-Hell survival recipe in addition to rare loot.
- See `docs/PHASE4_PORTAL_MATERIALS.md` for every item, source, exact craft and usage. Use `node tools/make-textures.mjs` to regenerate art.

## Offline QA (not an in-game pass)

- `python tools/qa-phase4-materials.py`: **86/86 passed** (IDs, assets, recipes, loot, ingredient registration, pre-Hell path and distinct images).
- `python tools/qa-phase3-static.py`: **81/81 passed** (previous realm structural check).
- JDK 21 compiler-parser: **45 Java source files, zero syntax errors** (parsing only, not symbol resolution).
- `runData`, `build`, server/client travel tests **NOT RUN** successfully. Gradle 9.2.1 wrapper download unavailable in offline environment. The mod jar from this snapshot is not compile-verified.

## Next mandatory work (do not skip)

1. Run `./gradlew --no-daemon runData` **once**, followed by `./gradlew --no-daemon build` and correct NeoForge 21.1.265 compile/API errors.
2. Launch a new client world and check visual rendering, missing models, inventory names, recipes, portal-surface render type/geometry and Hole atmosphere/terrain.
3. Test actual survival resource routes, especially Smoke-Soaked Filter → smelt → Smoke Residue and recipe-driven Devil Sigil without Hell materials.
4. Test building both door frames in each horizontal axis, activating, safe linked return, persistence through reload/restart, obstructed destination, chunk boundaries, and safe failure when a dimension is absent.
5. Add permissions/claim hooks and robust GameTests before advertising multiplayer ready; this prototype has neither full claims integration nor an independently verified persistent portal registry.
6. Only after runtime passes should Phase 4 be marked `client_verified` / `server_verified`. Then implement smoke-producing sorcerer biology and the Smoke Door using this backend, with ownership and access control. En Family Door is also still to be built.
7. FTB Quests deferred per owner. Keep `HANDOFF.md`, status, tests and this file updated as new changes land.

## Important architectural and economy constraints

- Never allow generated return frames/cores to drop freely craftable items; no portal-induced iron/bone dupes.
- Physical door plane, not Nether portal purple texture; must verify rendering on actual client.
- Do not silently upgrade an existing world generation. Back up saves and test fresh worlds for elevation/worldgen changes.
- The Sorcerers' World and Hell were only source-integrated, not yet verified through a full game launch.
- No mandatory quests or story restrictions. Humans must be capable of crafting the Artificial Door.
