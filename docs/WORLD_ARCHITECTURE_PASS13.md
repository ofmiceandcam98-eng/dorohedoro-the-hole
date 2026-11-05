# World Detail Pass 13 — Multiscale Architectural Variation

**Project:** Dorohedoro: The Hole — Minecraft Java 1.21.1 / NeoForge 21.1.265 / Java 21

**Status:** Source integration and deterministic offline tests complete; NeoForge compilation and actual in-game visual appearance **NOT verified**. This is a structured procedural detail improvement, not a claim that buildings equal the handcrafted quality of Greenfield or World of Keralis.

## Architectural goals

The previous city pass added weathering, props, varying materials, and derelict buildings but left too many rectangular structures with the same roof and frontage rhythm. Pass 13 provides variance at four scales:

1. **Neighborhood (320-block cells):** six seeded architectural identities affect material palettes, lot mix, street wear, and rooftop trim. District identities (industrial, derelict, residential, markets, etc.) still matter. A neighborhood has coherent cues, not identical buildings.
2. **Street/block:** residential pockets lean toward old tenements, apartment clusters, shops, or greenhouses; industrial areas develop repair strips, warehouse belts, factory clusters, or scrapyards; markets/ruins/outskirts get their own variety. Local roads and sidewalks have restrained wear and masonry detailing; highways, canals, tram rails, and crossings remain navigable.
3. **Building:** eight footprint setback profiles (when lots are large enough), contextual style selection, six architectural facade profiles, and five rooftop masses: penthouses, chimney pairs, stepped crowns, gantries, rooftop vents. Industrial halls have a separate three-way treatment for sawtooth crests, gantries, and plenums.
4. **Facade/prop:** narrow projecting window bays, deep entry lintels with supporting piers, vertical ribs, external balconies, industrial/workshop shutters and bands, layered storefront canopies. These are *block geometry* placed into the generated city, not mere concept art.

### Important limitations

- **World generation is deterministic**, but the Minecraft client/server must still verify that all protrusions render properly, roof structures do not become navigation hazards, no doors/stairs are blocked, no drops float and no accidental cross-plot collisions occur.
- There are still relatively uniform base Tower interiors and broadly orthogonal roads; this pass *improves variation* but does not yet implement wholly bespoke multistory L/U-shaped floorplans, curved roads, building-façade decals or landmark city-scale silhouettes. Those require further design and in-game reviews.
- The change *never intentionally touches* natural deep caves, ores, survival recipes, Nether, End, current mobs or portal systems.
- **Legacy saves stay at their saved version**: v0 = original, v1 = Pass 12, v2 = Pass 13. `world_detail_version=2` is explicitly selected only in `src/main/resources/data/minecraft/worldgen/world_preset/normal.json` for **new worlds**. Never manually change the value in a partially explored save: worldgen borders could mismatch.
- The starting quarter around (0,0) remains seeded as before. For unbiased evaluation, travel several hundred blocks and compare the same district across multiple neighborhoods.

## Files modified

- `CitySettings.java`: codec accepts v0/v1/v2, with missing field default 0.
- `Canvas.java`: retains boolean constructor but stores the integer version and has `hasArchitectureVariation()`.
- `CityPainter.java`: passes saved version into Canvas and overlays version-gated street surface treatment.
- `CityArchitecture.java` (new): pure seeded neighborhood, facade, rooftop, setback, and style selection. Eight margins tested against valid lot sizes.
- `ArchitecturePass13.java` (new): facade, rooftop, shopfront and industrial hall geometry plus safe street-surface details.
- `WorldDetail.java`, `Tower.java`, `LotPainter.java`, `LotChooser.java`: version-gated integration.
- `tools/VerifyCityArchitecture.java` (new): deterministic Java regression that does not need Minecraft.
- `tools/qa-architecture-pass13.py` (new): integration/static guards.

## Offline tests performed

- **123,210** independent building-site selections across ten world seeds; all facade, roof, and setback profiles represented.
- **980** neighborhood coherence checks, including negative coordinates.
- Adjacent facades differ in **83.44%** of sample pairs. This is a *profile-selection statistic*, not a visual-quality rating or real gameplay measurement.
- **8** setback profiles checked over multiple building sizes to preserve minimal width and depth.
- **35/35** Pass 13 integration/static checks passed.
- Legacy structural tests for Pass 12, ruins, the sewer/cave system, extra dimensions, portals, spawn eggs and cavern animation passed.
- Java files parse correctly and data JSON files load as JSON. **No full NeoForge compilation**; Gradle wrapper cannot download Gradle 9.2.1 in this environment.

## Mandatory in-game acceptance tests before claiming this is release ready

- In *fresh* v2 worlds, walk through **at least four seeds**, at ground level and on rooftops, at the plaza, >600 blocks from spawn, and through industrial, residential, market, derelict, waterfront and fringe areas.
- Compare a sample of **50+ adjacent building faces** for distinctive storefronts, roofs, projections, setbacks, scale, and neighboring theme coherence; count duplicates visually rather than relying only on hashes.
- Walk each storey to the roof: door is accessible, windows have valid states, ladders/stair route remains safe, rooftop farms remain harvestable, no roof props block access.
- Verify storefront signs and shutter orientation, covered walkways, paved roads, bridges, canals, tram alignment, lamps, sewers/shaft entrances, and NPC routes.
- Confirm derelict sites remain broken and abandoned while interiors stay explorable, shopkeepers don't spawn in abandoned shops, and valuables/loot still work.
- Check chunk borders at positive and **negative** coordinates, chunk-order independence on server, low-end FPS/TPS and memory consumption, world save/reload and crash restart.
- Verify existing v0/v1 saves do not change architecture if this mod version is installed. Do not switch a live save to v2.
- Run Gradle `runData`, `build`, `runClient`, `runServer`, full worldgen QA, plus gameplay testing of both new dimensions, portals, new ores, all eight spawn eggs, mob loot/animations and Glow Moth dynamic light cleanup.

## Next milestones

1. **Client screenshots / real game verification** (currently blocked here by missing Gradle distribution) and targeted corrections from screenshots.
2. More building templates with bespoke floor plans: stepped multiwing tenements, L-shaped courtyard buildings with usable circulation, gabled repair shops and larger real factories.
3. Deeper street grammar: alleys with varied widths/terraces, public squares and authentic landmark blocks, all preserving vanilla survival.
