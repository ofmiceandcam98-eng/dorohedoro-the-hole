# Pass 70 — Connected storm drains, cave transitions, and sewer architecture

**Target:** Java 1.21.1 / NeoForge 21.1.265 · **Status:** SOURCE ONLY / UNCOMPILED AGAINST NEOFORGE · **Date:** 2026-11-03

## Intent

Expand the playable municipal sewer network without making Dorohedoro techniques mandatory, damaging vanilla Minecraft progression, or placing more free loot, lights, or incidental enemy spawners. Preserve every historical world-detail version and gate new geometry behind **`world_detail_version: 52`**.

## Implementation

1. **`StormDrainBypasses70.java`** creates sparse, 29-block-long, 12-block-deep parallel stormwater passages with **two separate three-wide openings** into the existing main sewer. Three masonry profiles vary by deterministic seed/segment. The center runoff channel occupies a recessed Y=48 strip, with dry walkable lanes alongside and a covered ceiling. This is a practical alternate walking route, not a stand-alone room. It never writes within two blocks of the municipal trunk center, and rejects canal trunks, nearby crossings/shafts, coastline/ocean plots, and overlapping Pass67 repair/filter bays or Pass68/69 pumping stations. The new writes remain clipped by Canvas and its vanilla structure/deep-dark protection.
2. **`SewerCaveTransitions70.java`** widens the **outside margins** of the selected lower cave descent from radius 2 to 5 across steps 20..55. Rough cobbled deepslate, cracked masonry, and tuff replace straight pipe-like wall surfaces. It **does not write to the central three-wide existing staircase**, does not add water or traps, and preserves the existing cave-branch selection ratio of 30%. The original procedural end cavern remains intact. Importantly: this pass does *not* prove or create a connection to a pre-existing vanilla noise cave; that is a distinct future runtime-validation requirement.
3. **`SewerVaultVariants70.java`** textures the *outer shoulder* of ordinary seven-wide sewer trunks with three older/newer construction families (stone, tuff, deepslate). Only wall blocks at horizontal offset 3 and Y50–52 change. It avoids intersections and retains dry walkways, center wastewater, access shafts, lighting policy, and other systems.
4. **`CityPainter.java`** invokes the vault detailing after the existing Pass24 tunnel detail, then Pass67 maintenance bays, Pass68/69 pump halls, and Pass70 bypasses. Existing cave approach painting invokes Pass70 natural rock margins only in world-detail v52 and later.
5. **`CitySettings.java` / `normal.json`** increase the supported maximum and new-world preset from 51 to 52. Absent version still decodes to 0; previously written version 51 worlds stay unchanged in new chunks. Historical source QA scripts were adjusted to accept the correct latest upper bound (the feature gates themselves remain unchanged).

## Offline test evidence

- `bash tools/qa-pass70-sewer-architecture.sh`: **171,461 passing compiled Java fixture assertions** against real new Pass70 source classes using minimal fake Minecraft API classes. Covers 70 seeded cases, 28 seeded worlds with at least one bypass in sampled areas, 738 runoff-water block checks, negative-coordinate placements, 54 full-vs-chunk seam comparison scenarios, both axes, both sides, all 3 masonry modes, both doorways, dry walking lanes and headroom, old-world version isolation, the 30% cave selector, and cave transition width/headroom preservation.
- `python3 tools/qa-pass70-source.py`: **30/30** static source/data/JSON integration checks passing.
- `bash tools/qa-pass70-all.sh` reached and passed new tests and previous Pass69 down to **Pass54**, but hit the environment execution time limit. `bash tools/qa-pass53-all.sh` was then executed separately and **passed Pass53 down through Pass41**. Together, these runs cover the historical offline regressions, but the chained all-in-one command did not finish.
- **Not verified:** NeoForge compilation, Minecraft server/client, fluid update simulation, natural cave intersection, actual spawn and enemy difficulty, vanilla structure location or placement, render appearance, exploration performance, or cross-version upgrades of an already generated chunk. The `gradle-wrapper.jar` is not present; Gradle/NeoForge network build remains pending.

## Wednesday Claude handoff: mandatory priority order

1. Restore the official Gradle wrapper JAR using its independently verified published distribution artifact; use **Java 21**, Gradle 9.2.1, NeoForge 21.1.265. Verify wrappers and dependencies via official sources. Do **not** treat stub Java fixtures as a NeoForge build.
2. Run Gradle resource processing and compilation, then a real development Minecraft server/client. Correct any version/API mapping issues in code, *not* by quietly deleting features. Record concrete build logs and crash traces.
3. Make new version-52 worlds with several seeds. Visit multiple sewer bypass sites on both axes. Walk through **both separate entrances**, confirm the recess does not flood either dry lane, and test that Pass67 repair rooms and Pass68/69 pump stations remain intact.
4. Descend selected sewer-to-cavern branches in **iron armor, food, torches and normal mining tools, with no techniques**. Verify no unwalkable 2+ block jumps or embedded blocks, and investigate whether branch termini genuinely intersect independent natural caves. Do not claim natural-cave connectivity from a synthetic end-cavern alone.
5. Inspect protected vanilla ancient cities/deep-dark/Warden shriekers, strongholds/End portals, mineshafts, and trial chambers, plus ocean structures and the Nether/End progression path. Confirm structure placement and portal completion in actual games, not merely biome tags or `/summon` commands.
6. Capture real world screenshots for review of architecture, water boundaries, street and sewer lighting, navigation, and technique-free challenge. Adjust gameplay based on observed survival and performance, not a hypothetical render.

## Non-negotiable preserved design

- Keep survival crafting, ores, caves, all vanilla structures and vanilla beatability, 30% *selection target* for cave-leading sewer branches, 7-wide municipal corridors, usable return routes, and realistic night/underground hostile spawning.
- No healthy forest overgrowth, cosmetic/filler loot, over-lit sewer walls, forced magic traversal, poison feature, Ash-Snow/Sludge-Ice implementation, or broad villagers/factions rework before visuals are approved.
- Do not overwrite prior saved-world geometry or the original user screenshots with illustrative/generated art.
