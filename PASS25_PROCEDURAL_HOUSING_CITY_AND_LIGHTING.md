# Dorohedoro: The Hole — Pass 25: procedural city density, housing, furnishings, lighting

Date: 2026-11-01. Status: **source changes, offline verification only. NOT compiled with NeoForge; NOT verified in Minecraft.**

## Priorities carried forward

The Hole should be crowded, varied and readable: narrow secondary streets but navigable main routes, a diverse mix of **small one- and two-storey dwellings**, mixed-use shop-houses, medium tenements and larger apartment blocks, and furnished interiors with working doors, beds, storage, utility equipment and intentional lighting. **All variants are determined from the world seed/parcel coordinates**, not hard-coded fixed city maps or repeated imported schematics. Previous rivers, canal fish, riverbanks, sewer dungeon and waterway entrances are preserved.

No additional villagers, villager trades, Cross-Eye camps, Ash-Snow or Sludge-Ice features were implemented.

## Pass 25 urban foundation

- `DenseCity25.java` and `Superblock.java`: smaller/more numerous residential, market, industrial, waterfront and derelict parcels; tighter secondary streets (minimum three-block walkable gaps), preserving primary arterial/tram/river corridors and landmarks.
- `UrbanFabric25.java` and related integrations: coordinate-seeded pavement deterioration patches, neighborhood service equipment, yard clutter and rooftop equipment. Prior tests compare full-region output with 16x16 clipped chunk output to check stability.
- `CitySettings.java` / `normal.json`: new-world **world_detail_version=14**. Prior saved generators retain their serialized version; `DEFAULT` stays version 0.

## Pass 25 housing priority

- `Housing25.java` is a new source class with six residential facade variations. It uses one of seven district palettes, seeded accent/roof choices, exterior downpipes, optional compact balconies, entrance lighting and low-rise roof silhouettes. No copied external buildings or imported schematics.
- `LotChooser.java` prioritizes real **tenements/homes and small mixed-use shops on compact residential lots**, rather than generating towers wherever the lot type happened to be APARTMENT.
- `LotPainter.java` selects the number of storeys **from the footprint area and aspect ratio**, then the per-lot hash. Small homes have 1–2 floors (some compact apartments reach 3); medium blocks can be 2–6; appropriately large blocks can be taller. Existing per-world max-floor caps remain enforced.
- Small homes have furnishing on the **ground floor**, not only a lobby: usable kitchenette or stove, wash station, storage, a sleeping space in single-storey houses (upstairs bed in two-storey houses), seat/work surface when room allows, textiles and multiple light sources for larger units.
- Existing partitioned multi-unit buildings keep their accessible hallways and furnished apartments. Additional small details and ceiling lamps are placed only into empty supported cells.
- Small shops can generate 2–3 storeys with living quarters upstairs. Their ground-floor work and trading spaces remain.
- Existing foundational stairs and centered street entrances are preserved. Facade additions are positioned above/aside from doorway clearances.

## References and design extraction (no asset copying)

- [Minecraft Village – The Slums, Vilgot Larsson](https://vilgotlarsson.artstation.com/projects/w6dvb6): irregular roof paths, cramped nested small homes and connected sewers.
- [Nova Celes, Minecraft official feature](https://www.minecraft.net/nb-no/article/city-lights): district character, exterior utility equipment and fully furnished interiors.
- [Misosiru City article](https://automaton-media.com/en/news/20221101-16513/): scale-appropriate urban streets, service poles, roadway details and mixed building types.
- [Artisan Apartments, Planet Minecraft](https://www.planetminecraft.com/project/artisan-apartments/): stacked residential masses, balconies and layered facades.

These provide **design principles only**, not directly reused schematics, textures or world files.

## Verification performed

- `tools/qa-housing25.py` compiles the **actual new Housing25.java** with stub Minecraft block APIs and checks **800** sampled configurations. Results: **463** placed beds in compact-unit cases, **900** recorded lantern placements, **9** distinct floor counts; all checks on seeded repeatability, bounds, entry clearance and light/bed provision passed. The sample does **not** include every dimension or every cross-chunk edge case.
- `tools/qa-pass25-density.py` compiles actual lot and superblock math with stubs and reports **161,596** geometry assertions in 350 trials; 14th-version sampling found **1,152 compact parcels**, **749 tenement/home parcels** within them, **2,052 medium** and **145 large** residential parcels (100 sampled seeded blocks).
- `tools/qa-urban-fabric-pass25.py`: stub compilation plus full-region/chunk-split coordinate-matching passed.
- Java syntax parser: **139 / 139** active Java files parsed, no errors.
- Existing selected static regressions: architecture 35/35, interiors 23/23, irregular streets 54/54, lore architecture 59/59, waterways and ecology 114/114.
- **NeoForge build attempt blocked:** `gradle/wrapper/gradle-wrapper.jar` absent from the archived source; a full build or `runData` cannot be claimed. `qa-phase3-static.py` and `qa-phase4-materials.py` are historical assumptions and not a validation of current 32px assets.

## In-game acceptance still required

1. Start a new creative world with the supplied generator profile and teleport among multiple districts/seeds. Photograph narrow houses, medium mixed-use buildings and large blocks, both daytime and at night.
2. Check actual front door accessibility; stair flights to every floor; bed two-block placement; doorway clearance; ceiling lantern attachment and light levels, and whether interiors feel dense rather than cluttered.
3. Test attached exterior lanterns and overhangs for normal neighbor updates. Inspect building appearance at riverfront, sloped hills, sewer hatch locations and alleys.
4. Verify that style accents don't create unsupported floating furniture, doorway obstructions, or ugly repetition at chunk boundaries. Rework with real screenshots before calling visual quality approved.
5. Run `runData`, the full NeoForge build, dedicated server/world generation and gameplay QA on an environment with the pinned wrapper/dependencies. Use new saves; back up old worlds.
