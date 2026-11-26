# Pass 45 — Working urban courts and inhabited low-rise infill

**2026-11-02 — source-only checkpoint — not compiled, not installed, and not verified in Minecraft**

## What this pass fixes

The player's original November 1 Minecraft screenshots show a plausible polluted sky and some industrial architecture, but city blocks still contain too many generic empty / rubble parcels and repeated uninterrupted paving. The previous passes improved markets, canals, street weathering, facades and roof silhouettes. This pass adds **limited grounded low-rise urban infill** where people have a reason to work and live, rather than scattering extra no-purpose clutter.

`InhabitedCourts45.java` can convert a minority of eligible 19×19-or-larger **central-city** `VACANT` and `RUBBLE` lots into an enclosed two-bay municipal service building with an open yard. The bay toward the west has a shop-use station (crafting, food processing or smithing) and a second complementary workstation. The east bay is a separate wash / repair room with a usable cauldron and furnace / stonecutter. Each bay has its **own real door**, not a fake doorway; outside paths from each door continue south to the public lane. Selected courts have one modest indoor barrel, **no outdoor loot chests**, and **no new lights**. Roof height is uneven on some intact courts, providing genuine low-rise massing alongside existing taller tenements.

Workstation choices follow the surrounding district: residential, market, industrial/waterfront. Some damaged plots use cracked materials while remaining serviceable. Intact ruins, wild lots and sapling/wood sources largely remain unchanged. Graveyards, outskirts, smaller parcels, all five new outer climate biomes, hospitals, Hungry Bug, named landmarks, houses, underground accesses, transit, and original roads are explicitly outside this conversion.

## Saved-world and generator integration

* `CitySettings` accepts world detail 0–29. Optional `world_detail_version` is **still 0** for old saved generator settings; **only new default worlds** opt into `29`. Do not edit an existing saved world's generator version to force this update. No retroactive world edits occur.
* `Canvas.hasInhabitedCourts45()` and `Canvas.isHoleCity(...)` guard the feature; city-only conditional uses the existing exact Pass42 region selector.
* `OpenLots.vacant` and `OpenLots.rubble` branch **before** open-lot trees and rubble heaps are placed, preventing accidental collision between a later shed and pre-existing lot vegetation. Other open lots run their original code.
* Derelict `RUBBLE` lots skip the old structural ruin remnant only when selected for the new working court. This prevents two buildings from occupying the same lot.
* Parcel-seeded deterministic structure writes are locally clipped by Canvas. No RNG state, cross-chunk reads or unbounded freestanding utility corridors.

## Offline tests executed

* **18/18** Pass45 source integration / registry / biome / default save profile / JSON / no-new-lights checks.
* Actual Java `InhabitedCourts45.java` compiled on JDK 21 **with stubbed vanilla Minecraft APIs**; **3,780** seeded district / width / depth parcel cases evaluated; **858** eligible finished courts; **1,195,956** synthetic block writes. Wide painting and tiled 16×16 painting identical. Each rendered court remained inside its parcel, had exactly two front doors, and each door's outside path remained empty up to the lot boundary at ground and two blocks above. Test also confirms opt-out for older versions and non-city frontiers.
* Pass44 16/16 source and 4,320-building Java fixture; Pass43 16/16 source and placement fixture; Pass42 26/26 + Java region selector; Pass41 images/JSON/icon audits and geometry fixtures; Pass35 dark night checks — all carried forward and passed after updating stale test expectations to permit version 29.
* **Actual Gradle attempt** (`bash gradlew --version`) failed because `gradle/wrapper/gradle-wrapper.jar` is **absent**. JDK 21 is installed. No full project compile/runData/real-world loading happened here.
* Both release ZIPs CRC-tested after creation. Not game artifacts.

## Still unverified / specific quality gates

1. On a development PC, restore the **official** Gradle wrapper JAR matching the repository's pinned Gradle 9.2.1 wrapper configuration; do not copy arbitrary jar downloads. Run `./gradlew runData`, `./gradlew build`, `./gradlew runClient`, dedicated server and GameTests. Fix errors before installing.
2. Create **fresh v29 world seeds**. Compare the five original November 1 street/market/canal/skyline views with in-game new-world screenshots. Check added courts enhance density without looking uniform, have reasonable shell/roof thickness and real daylight/weather behavior, and appear in appropriate neighborhoods at believable frequency.
3. Walk into **both** rooms; use workstations and cauldron; check no bay is sealed, that path clearance survives foundation grading and adjacent irregular streets, and roof massing does not collide with adjoining parcels or existing structures.
4. Verify the source-only Pass44 facades and older rooftop structures do not cover windows, ladders, stair entrances or streetlamps. Test performance across chunk boundaries with multiple world seeds.
5. Retest all five frontier biomes, generated districts, roads, river bends, sewer shafts, 30% cave branches, ores, sky occlusion, night hostile spawning, creature/magic progression, portal persistence and original crash reports. Do not publish without an actual client/server build.

**Deferred by user:** Ash-Snow, Sludge-Ice, toxic snowballs, poison, Cross-Eye camps, and larger villager/faction overhaul. Emeralds and ordinary vanilla survival remain intact. This implementation does not introduce new biome hazards, items, textures or NPC professions.
