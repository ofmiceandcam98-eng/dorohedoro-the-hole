# Dorohedoro: The Hole — Pass 55 (2026-11-02)

**Status:** Source-only checkpoint. The `gradle/wrapper/gradle-wrapper.jar` is still missing in the source baseline; no actual NeoForge compilation, runData, runClient, runServer, GameTests or Minecraft screenshots are verified for Pass 55. The Java geometry harness uses test doubles for Minecraft blocks and a clipped Canvas.

## User-approved goal

Improve the actual massing and lived-in nature of The Hole's buildings, using Q Hayashida / the anime's cramped, layered, improvised urban architecture. Not every house should be a rectangular tower with decorative roof clutter, and new rooms must do something. Keep Minecraft survival, uneven climates and variable urban density intact. Preserve the past pass's new crooked minor lanes, main arterial links, canals, underground sewers (30% cave passages), vanilla resources, dim nights and hostiles, and usable indoor floorplans.

## Implemented

- `src/main/java/com/dorohedoro/hole/worldgen/city/AttachedWings55.java`: seed-selected side-biased attached rear service wing, **within the existing footprint** of some eligible larger homes, apartment blocks, and multi-storey shops. The main shell recedes six blocks at the rear while keeping the full south entrance, frontal street wall and north-west stair route unchanged. Chosen on approximately one sixth of qualifying lots; ordinary unextended buildings remain common.
- Each extension includes a usable *connected* room and an interior door from the main building. Ground-floor role chosen deterministically: shared laundry/wash, real repair shop, or food prep/dry storage. Upper room on some larger houses includes a bed and workbench, and sits beneath a separate roof silhouette. No open-air chest spam or new lamps.
- Every wing's lower roof is an actual accessible maintenance deck. It includes two functional vanilla blocks (cauldron and stonecutter), railings on exposed sides, and a door from the next full-height main-storey floor. The wing's doorway is far east of the house's internal north-west staircase; the near doorway approach is carved out of furniture on both sides. It won't attach if the original structural-massing algorithm would carve away the relevant north-east wall.
- `LotPainter` hooks this into `TENEMENT`, `APARTMENT`, and `SHOP`; landmarks, clinics, hospitals, factories, market squares, and their interiors are unchanged. Existing interior furnishing passes, dormitories, jobs, doors, and loot tables are retained.
- `Canvas.hasAttachedServiceWings55()` opts in only from fresh `world_detail_version >= 38`. `CitySettings` constructor, world-setting Codec, and `normal.json` new-world preset accept/select v38. Older existing saved profiles keep historical layout, since the codec still defaults omitted versions to zero. Do **not** manually change world_detail_version for an old save.

## Verified offline

Run `bash tools/qa-pass55-all.sh` from the project root. This performs source checks, compilation of the actual AttachedWings55 algorithm against minimal Java/Minecraft stand-ins, per-block mock placement, doorway/approach checks, plot containment, 16x16-equivalent chunk slicing, and the Pass 41–54 offline chain.

Sample source and geometry results are in `/mnt/data/Pass55_OFFLINE_QA.log`. Tests are strong evidence of seeded geometry/fixture consistency but not evidence of actual Minecraft block states, renderer visuals, in-game pathfinding, chunk seams, lighting, loot, performance, or gameplay.

## Minecraft acceptance checklist (must still be done)

1. Restore official verified Gradle wrapper JAR on a network-enabled Windows machine (see Pass48 build prep). Run `gradlew.bat runData`, `gradlew.bat build`, `gradlew.bat runClient`, dedicated server and the GameTests.
2. Create fresh v38 worlds across multiple seeds and climate origins (snow, desert, badlands, dead taiga, mountains, regular city). Ensure each can have dense, occupied, derelict or open areas independent of biome.
3. Inspect 100+ generated mixed-size buildings. Verify wings don't clip housing windows, flatten existing roofs, erase bedrooms, or protrude into roads, canals, tram corridors or neighboring lots.
4. Verify every service-wing and roof-deck door opens and is reached from the primary street entrance via real stairs; there are no unreachable upstairs rooms or blocked bed/workstation interactions. Check from interior and exterior at night.
5. Compare exact in-game screenshots to actual Dorohedoro manga/anime references: roofline profiles, attached additions, facade weathering, street widths, small improvised workshops, narrow back alleys, purposeful activity, lack of excess lights and conspicuous public loot containers.
6. Confirm old v37 worlds are unchanged across chunk borders, and that Minecraft's natural mining, vegetation, caves, crafting, exploration and survival remain intact.

## Deferred work

Ash-Snow, Sludge-Ice, toxic snowballs, environmental poisoning, Cross-Eye camps, and citizen/villager/faction overhaul remain deferred until the world and districts meet the user's visual quality standards. No new entity/texture asset changes in Pass55.
