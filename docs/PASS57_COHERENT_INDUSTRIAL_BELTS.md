# Pass 57 — Industrial land use survives biome-independent city generation

**Date:** 2026-11-02. **Status: source-only, not NeoForge compiled, not visually verified in Minecraft.**

## Problem and rationale

The user wants Dorohedoro's Hole to support dense and sparse urbanization in any climate, while retaining Minecraft survival. Pass 52 achieved climate-independent fabric, but it also routed every non-dense parcel through `UrbanPattern52.choose(region, fabric, roll)` regardless of whether its original district was INDUSTRIAL. An inhabited industrial quarter could therefore turn into a climate-themed village of ordinary houses and fields, even when neighboring buildings were factories. The industrial complex lost its coherent working character simply because it was snowy, dusty, or located in badlands.

## Changes

- New `IndustrialBelts57.java`, invoked only by `LotChooser` when `world_detail_version >= 40`, `district == INDUSTRIAL` and `fabric != OPEN_COUNTRY`.
- Reuses the existing deterministic 320-block `CityArchitecture.neighborhood` cells to group industry into **manufacturing, freight/depot, reclamation/salvage, and worker-services** neighborhoods. Rather than uniformly weighting all production, two of six construction palettes emphasize salvage and worker support, so the city isn't wall-to-wall smokestacks.
- Every working belt contains a **mixed ecosystem** of real factories, warehouses, furnished repair shops, junkyards, tenements and vacant/ruined sites, with clinics and communal courts where appropriate. Building identity changes based on both its subdistrict and urban fabric.
- Dense fabric has a greater concentration of active workshops, freight and industry; inhabited fabric supports working streets and housing; derelict fabric favors salvage, vacant land and closed depots. **OPEN_COUNTRY keeps the previous biome-specific decision path without alteration**, protecting exploration, mining and rural survival play.
- Small plots cannot get a `FACTORY` from this new chooser unless at least 20×20; undersized factory lots become `SHOP` with existing furnished repair/service interiors. `WAREHOUSE` requires at least 13×13; otherwise it becomes `SHOP`. This does not replace prior industrial building code or its already-functional internal rooms.
- `CitySettings` codec/constructor now accept 0..40, fresh-world preset uses 40. Historical saves retain their stored detail versions, with their previous land-use algorithm intact.
- No new blocks, lights, exposed chests, extra mobs, structures, climate assignments, sidewalks, sewer mutations or registries were introduced. Previously-built Pass 55 wings and Pass 56 linked industrial yards remain intact.

## Offline verification

`bash tools/qa-pass57-all.sh`:

- 18/18 integration checks, verifying v40 opt-in, v0 historical default, new industrial routing, climate independence, and preserved legacy systems.
- Compiled **the actual `IndustrialBelts57.java`** (plus production `CityArchitecture.java`, `CityHash.java`, `Rect.java`, `LotType.java`) with a minimal Fabric enum stub. Exercised **864,000 seeded parcel selections**, all four belt types, min viable factory/warehouse footprint constraints, deterministic repeat selections, and **6,400 same-neighborhood cohesion checks**. Open countryside intentionally raises on direct policy invocation because it must use the original path.
- Complete historical Pass41–56 offline chain passes.
- Current online/IDE Gradle dependencies cannot be fetched from this isolated runtime; official `gradle-wrapper.jar` is absent. A **full NeoForge build has not been performed**. Java unit fixtures with a stub do not prove compatibility with Minecraft/NeoForge or visuals.

## Actual in-game QA still required

1. On a network-enabled JDK 21 host recover official verified Gradle wrapper, run `gradlew.bat runData`, `gradlew.bat build`, `gradlew.bat runClient`, `gradlew.bat runServer`, and GameTests.
2. In six climate types inspect 4+ INDUSTRIAL neighborhoods in DENSE/INHABITED/DERELICT/OPEN_COUNTRY (at least 72 district/fabric/climate combinations), checking active factory/workshop/warehouse/salvage ratios, sheltered loading corridors, door usability, usable interior rooms, flat/walkable entrances, and landmark right-of-way.
3. Ensure the number of derelict/open areas maintains realistic access to ores, caves, dead-tree logs and normal Minecraft hostile mob spawning at night. Verify *no additional* lighting in this pass.
4. Compare actual render screenshots with Q Hayashida / Shinji Kimura anime/manga references and user's Nov 1 screenshots, specifically density, skyline and nonuniform factory/workshop streets. This pass organizes meaningful land use; it **does not** implement physical shared party walls between neighboring parcels.
5. Validate new-world v40 generated chunks at multiple negative and positive coordinates and ensure v39 saves preserve their earlier lot selection and v39 building geometry in new chunks.

## Deferred features

Do not start Ash-Snow, Sludge-Ice, toxic snowballs, environmental poisoning, Cross-Eye camps, or the citizen/villager/faction overhaul until city visual quality is approved. No extra unrequested cosmetic-only materials. Next geometry priority: carefully implement real shared-wall rowhouses or inter-building industrial courts **inside one parcel or by an explicit ownership-safe multi-parcel plan**, without blocking the min-3-wide alleys or causing chunk seams. A real NeoForge build and screenshots are a higher-priority blocker than more unverified layers.
