# Dorohedoro takeover checkpoint — 2026-10-31

This is a **source patch for the latest `dorohedoro-the-hole-source(2).zip`** supplied by the owner.
It was made after the headless QA handoff. The original auto-consolidated root `HANDOFF.md`
is a **prior code snapshot** and must not be treated as newer source than these files.

## User-observed problems

- Live Minecraft screenshot: entire Hole street plane appears unnaturally flat.
- Live Minecraft screenshot and report: building interiors are very sparse.
- Live Minecraft observation: the sky is still visibly rendered; the owner wants no discernible sky, sun, moon or stars while preserving gloomy lighting.

## Changed in this takeover

1. `CityElevation.java` (new): deterministic, broad, gradual relief (0–11 blocks); flat spawn quarter; no per-chunk RNG.
2. `CitySettings.java` and `world_preset/normal.json`: `terrain_relief` is stored in generator settings.
   New worlds from the normal Dorohedoro preset explicitly enable it. The codec's missing-field
   default is **false**, preserving the old flat generator for pre-update worlds.
3. `Canvas.java`: translates placed block/loot/sign Y coordinates using a per-column cached relief;
   uses a constant sampled elevation per lot so stairwells and interior floors remain level.
4. `CityPainter.java`: fills graded road foundations; clears residual vanilla terrain around raised
   street surfaces; matches tram rail ascent states to the new grades; adds some
   inverted-daylight-sensor redstone street lamps, while retaining always-on lanterns.
5. `CityPopulator.java`: places generated residents at elevation-aware coordinates when enabled.
6. `LotPainter.java`: richer apartments and lobbies, bedsides, rugs, workstations, shop counters,
   shelving, clinic examination stations, warehouses, and factory equipment. Keep clear aisles.
7. `HoleBiomeData.java` **and all eight generated biome JSON files**: smoky biome color adjustments.
8. `client/HoleAtmosphere.java` (new) plus `Dorohedoro.java`: client-only dimension effects hide the drawn vanilla sky, sun, moon, stars and white clouds **when the camera is in a Hole biome**. Opaque grey-green fog provides the visible atmospheric backdrop. Rain, time, skylight, gameplay and farming are intentionally left intact. Vanilla worlds and the other dimensions retain their sky rendering. This is a visual smog canopy, NOT a solid ceiling at build height.

## Verification performed in this environment

- Standalone `javac` compile of `CityHash.java` and `CityElevation.java`: **passed**.
- Deterministic elevation sampled on five seeds (1, 42, 12345, 8675309, -987654321):
  spawn offset 0, sampled values 0–11, no adjacent sampled columns differing by over one block.
- JDK compiler's Java syntax parser across 34 active `.java` files: **0 syntax errors**.
- New world preset JSON and all generated biome JSON: parse successfully.
- After atmosphere integration, 35 active Java files parsed with Java 21 JavacTask: **zero syntax errors**.
- NeoForge 1.21.1 API signatures for sky/cloud hooks and fog events checked against published version-specific javadocs, **not** against the installed project classpath.
- Attempted `./gradlew --offline --no-daemon compileJava`: **blocked**, because Gradle 9.2.1
  distribution was not cached and external DNS/network access is unavailable in this environment.
- **Not verified**: NeoForge compile, sky/cloud effects registration or rendering in the Minecraft client, world generation on a real server, terrain walkability,
  tram/rail continuity on grades, portals, actual daylight differences, powered lamp behavior,
  functioning furnished block entities, visual appearance, and full survival gameplay.

## Highest-priority next steps for Claude / the owner

1. Unzip this update into a **separate project folder**. Do not blindly overwrite work Claude
   created after the ZIP export; compare source changes first.
2. Run `./gradlew --no-daemon runData` (rebuild JSON) and `./gradlew --no-daemon build` on owner's
   machine, where Gradle/NeoForge were available. Fix any API/compilation errors.
3. Use a **brand-new world** created from the default Dorohedoro preset, since the old save is
   deliberately retained as flat. Travel 400–800 blocks away to see the broad relief;
   the initial spawn quarter is deliberately level and may still look flat locally.
4. Use `/time set noon`, `/time set midnight`, `/weather clear`, and `/weather rain` to verify the Hole has no visible sun, moon, stars, white clouds or recognizable blue sky. Check grey-green smog remains visible, ambient lighting is playable, rain still falls, and inverted daylight-sensor lamps still change at night. Check a vanilla world preset still has the normal sky, and that the Sorcerers' World keeps its own sky.
5. Walk stairs and entrances; measure differences at lot-to-road edges. Test tram rails across
   vertical grade transitions and intersections; ascending shapes are now generated on grades
   but have NOT been evaluated in the real Minecraft rail network.
6. Compare multiple chunk orders with the existing 10-seed QA harness. Run interior survival
   and door/bed/chest/brewing stand/furnace tests, and inspect saves after restart.
7. Verify NPC spawning at lot centers; complete location-based spawn QA around elevated lots.
8. Improve terraced sidewalk transitions and large landmark ground work where the chosen
   elevation results in 1–2-block step discontinuities.
9. Reduce overuse of green oxidized copper and repeated chimneys, but only after terrain
   and furnished interiors are confirmed in the client.

## Constraints preserved

- Survival sandbox, no mandatory story quests.
- FTB Quests remains deferred pending explicit owner request.
- Magic/portal systems remain planned; this patch does **not** pretend to implement them.
- Original layout/hash-based deterministic planning is unchanged.
- Do not introduce retroactive modifications to old player-built chunks.

## Build commands on the owner's machine

```bash
./gradlew --no-daemon runData
./gradlew --no-daemon build
./gradlew --no-daemon runClient
bash tools/qa-world.sh 12345 12 forward
bash tools/qa-world.sh 12345 12 reverse
```

## Status labels

- Terrain relief: implemented, **source-tested only**; needs build/client/server validation.
- Interior enrichment: implemented, **syntax-tested only**; needs gameplay validation.
- Day/night streetlights: implemented, **syntax/JSON-tested only**; needs visual validation.
- Hole-only sky occlusion and smog fog: implemented, **JDK syntax checked only**; needs NeoForge build, client visual test and fallback validation.
- All earlier verified QA statuses apply only to the **pre-takeover** code.

## Why the sky is hidden

The Hole canonically has an overcast polluted sky. The owner's stronger design preference is
that players should not perceive a normal sky dome. Rendering an opaque smoke backdrop
instead of generating a physical roof preserves crop growth, rain, weather calculations,
open-sky access, mobs and vanilla survival. The implementation deliberately affects only
Hole biomes inside the actual Overworld.

The new client class registers dimension effects using
`RegisterDimensionSpecialEffectsEvent`, and uses the NeoForge
`IDimensionSpecialEffectsExtension.renderSky/renderClouds` hooks to suppress celestial
rendering. `ViewportEvent.ComputeFogColor/RenderFog` gives the smog color and distance.
The full rendering path has **not** been tested in the actual Minecraft client; a
wrong key, API method mismatch or different renderer may require adjustment.

Implementation references: 1.21.1 API documentation at
`https://lexxie.dev/neoforge/1.21.1/net/neoforged/neoforge/client/extensions/IDimensionSpecialEffectsExtension.html`
and `https://lexxie.dev/neoforge/1.21.1/net/neoforged/neoforge/client/event/RegisterDimensionSpecialEffectsEvent.html`.
