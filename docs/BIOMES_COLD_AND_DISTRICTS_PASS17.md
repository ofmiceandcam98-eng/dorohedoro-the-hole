# Pass 17 — Rare cold districts and dry frostbound highlands

## Lore audit (read BEFORE implementing environmental effects)

- **Snow in the Hole is canon**: Dorohedoro Chapter 13 shows snowfall outside Kasukabe's house. The Hole is a gloomy polluted **surface** city.
- **Permanent winter industrial suburbs are NOT established canon**. `hole_frostbitten_industrial` is an optional rare Minecraft adaptation to give the vast city climatic diversity; it is not a canonical named district.
- **Sorcerers' World is normally precipitation-free**; Chapter 90's rain was an extraordinary story event. `sw_frostbound_highlands` is an ORIGINAL geographic extension, and intentionally sets `has_precipitation=false`. It uses a separate ground-level snow-patch feature instead of pretending that ordinary blizzards are canonical.
- The screenshot's claims about permanently frozen Cross-Eyes strongholds and guaranteed Black Powder laboratory caches were **not verified**. They are future design proposals, NOT implemented features.
- Any environmental poison, freezing damage, or equipment protection must be tested and balanced before enabling. **No new debuffs or armor requirements added in this pass.** Humans must retain survivability; the Hole's rain primarily debilitates sorcerers in canon.

## Implemented source

- New Hole biome `dorohedoro:hole_frostbitten_industrial`, modeled after industrial air/spawns/minerals with temperature 0 for snow; its appearance includes cold fog, gray-green water, ash motes, ordinary survival ores and craft resources.
- Its selector is `ColdDistricts17.isCold`, based on the existing city climate noise with 128-block coherent neighborhoods. It is restricted to Industrial/Outskirts beyond nine cells from spawn. The same selector is used in the biome source and city-plan snow decorations, to avoid conflicts.
- The new Overworld preset sets biome-source `winter_districts=true` and saved city `world_detail_version=6`. Existing saves without the flag continue with their previous biome selection and street detail.
- A little non-solid, one-layer normal snow collects on selected paving edges. No road barriers, no swamp replacement, no ice converting all drinking water, no universal damage.
- New Sorcerers biome `dorohedoro:sw_frostbound_highlands` appears only in remote higher-continentalness / cold / high-weirdness landscape when `frostbound_regions=true`. Prior biomes remain unchanged. A custom placed simple-block feature spreads light snow cover on solid terrain while keeping the sky dry. The normal ore/cave tables remain in place.
- Repaired accidental duplicate Java method definitions in CityPlan and CityPainter (two copies of avenue-offset/street-band methods). This was a preexisting full-compilation blocker missed by syntax-only QA.
- Generated biome and placed/configured feature JSON are included as an offline snapshot. **Run `gradlew.bat runData` in your NeoForge 1.21.1 dev environment** to regenerate and compare them; the full build is unverified here.

## Not yet implemented — do not mistake these for working content

- Special original `Ash-Snow` and `Sludge-Ice` blocks, mineable items, armor or toxic snowballs. These require their own art, registries, loot tables, recipes, terrain generation, gameplay effects, and tests. Vanilla snow is used in this checkpoint instead of adding placeholders.
- Distinct hospital-only zone, reserved Lake of Refuse district, scripted Living Dead event, Cross-Eyes camp structures and Black Powder caches. Existing eight Hole biomes, existing Lake landmark, existing hospital/clinic generation and undercity are preserved.
- The snowy ground distribution, winter biome visibility, player travel, feature sorting and snowfall in both dimensions still require actual Minecraft client/server worldgen testing.

## Test cases to preserve

1. Generate fresh Hole world. Confirm startup quarter snow-free; find a rare frostbitten industrial/outskirts patch and verify a coherent neighborhood not one-block checkerboard.
2. Confirm real snow on cold roads occurs sporadically, is walkable and never blocks sewer entrances, staircases, rail lines or market access.
3. In an existing save from version 5 or earlier, confirm no change in terrain layout or biome routing where new chunks meet saved chunks.
4. Generate fresh Sorcerers' World. Verify frostbound highlands snow patches without falling rain or snow; woodland, Hydra forest, cities and caves remain.
5. Mine vanilla ores in each realm; ensure nether/end portals and all earlier dimensions, sewer cave systems, eight mob spawn eggs, portals, crafting and Glow Moth light still behave correctly.
6. Test 10+ seeds, continuous vs shuffled chunk order, multiplayer and render performance, natural weather, and all asset/texture registration.
