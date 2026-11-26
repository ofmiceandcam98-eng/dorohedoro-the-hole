# Pass 23 — Riverbank / canal sewer entrances

**Date:** 2026-11-01  
**Status:** New source integrated and offline structural checks passed. **NOT NeoForge-compiled or in-game tested.**

## The Hole's waterways now give two additional routes into the SAME sewer dungeon network

- **Quay maintenance hatch:** flush, manually openable trapdoor in the canal-side walking bank, visible municipal metalwork and lantern, dry ladder descending from street Y64 to sewer floor Y49. Dry lined corridor joins the existing 7-wide municipal sewer through its side walkway (not through the flowing wastewater center). Maintenance loot.
- **Underwater culvert:** visible masonry/metal drainage mouth under the canal surface, short flooded inlet, water-level swim-up pocket (surface Y61), air passage OVER the watertight divider, then dry ladder and lined corridor into the SAME municipal sewers. Filtration salvage loot. The vertical shaft is sealed at surface and is not secretly a second street hatch.
- Both entries are paired but 48 blocks apart along the canal, on opposite banks; about one in three eligible canal/cross-street intersections is selected deterministically from world seed. Existing surface kiosks, dungeon rooms, caves, and mob spawns remain.

## Scope and important limitation

This procedural Hole creates **engineered canals**, modeled by `Line.Kind.CANAL`. These cover the city's currently implemented river-like waterways. This pass does **not** dynamically attach to arbitrary vanilla river biome coastlines, and it does not add new overworld river terrain. Actual water in worldgen is at Y58–61 over a Y57 bed. The intake's wet pocket ends at Y61 while the air crossover is at Y62–63, over a masonry divider separating water from the Y50–63 dry ladder and municipal sewer floor. This is designed to prevent dungeon flooding; **real fluid update behavior requires in-game testing.**

## Save compatibility

The normal world preset now selects **`sewer_cavern_version: 3`**. The saved generator codec accepts 0–3, defaulting to 0 for old worlds. Pass 22 dungeon remains active from version 2. Waterway access is ONLY generated with version >= 3; old 0/1/2 saves retain their generator profile and do not get a partial shoreline in newly generated chunks. Do not manually change existing level.dat generator versions.

## Validation

- `python tools/qa-pass23-waterway-entrances.py`: 26/26 source and schematic tests pass (includes version gating, cross-axis geometry, airlock routing, paired access, surface and submerged cases).
- `python tools/qa-pass22-dungeon-ecology.py`: 117/117 existing structural checks pass; one assertion updated to allow the new version >=2.
- Java 21 javac parser (ParsePass22): 132 active Java sources, zero *syntax* errors.
- Needs actual `gradlew.bat --no-daemon runData` then `gradlew.bat --no-daemon build` on Windows with pinned dev dependencies, then client/server/worldgen QA.

## In-game acceptance checklist

1. Fresh creative test world with version 3. Locate several `CANAL` + roadway crossings; some should have one quay hatch at +24 along the water and one submerged intake at -24, opposite banks.
2. Open dry hatch from quay, descend all the way to sewer floor and walk into the existing runnel-side walkway; emerge back out of hatch without mining.
3. Swim into the underwater drainage mouth at Y59–61, swim UP into the air pocket at Y62, cross the bulkhead, grab the ladder at r=8, descend to sewer, and return the same way.
4. Wait for water/fluid ticks, save/reload chunks, then confirm no flooded dry shaft or unexpected riverwater in the main dungeon.
5. Traverse entrances crossing chunk borders with the terrain relief enabled, and test with river/canal crossings near factories, apartments, and frozen districts.
6. Confirm the eight existing mobs still spawn in sewer and appropriate surface biome habitats; no duplicate dungeon or entity changes were introduced.
7. Compare actual rendered masonry, pipe/metal, water, surface trapdoor and lighting to high quality Minecraft worldgen mods. Improve based on screenshots; static tests do not establish visual parity.

Deferred Ash-Snow/Sludge-Ice/environmental poisoning, Cross-Eyes camps, villager/faction overhaul, and unrelated screenshot-dependent magic work remain deferred.
