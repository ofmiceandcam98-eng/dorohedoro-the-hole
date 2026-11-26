# Pass 65 — True ocean basins for vanilla Minecraft compatibility

Status: **source only, not NeoForge-compiled or Minecraft-playtested**. Applies only to newly created `world_detail_version: 47` worlds with `ocean_regions: true`. Earlier saved biome-source records decode the missing ocean flag to `false`; earlier city generator records retain their previous version.

## Why this pass exists

Pass 64 preserves registered underground structures and the Nether/End but The Hole had no actual Minecraft ocean biomes. Canals are not ocean habitats: a canal cannot substitute for the vanilla **deep-ocean biome eligibility** used by ocean monuments or the marine ecology required by shipwreck/ocean-ruin exploration.

## Implementation

- `OceanRegions65`: broad, irregular 2,048-block provincial cells, seeded through Minecraft's climate sampler, with 760–1,039 block basin radii, 1–30 blocks of underwater depth, shallow coast ramps and deep-water interiors. Avoids all potential randomized starting neighborhoods with a 5,200-block safety envelope. Oceans remain uncommon enough to preserve extensive land exploration.
- `HoleBiomeSource`: new optional `ocean_regions` flag (legacy default `false`); assigns vanilla `minecraft:ocean`, `minecraft:deep_ocean`, `minecraft:cold_ocean`, `minecraft:deep_cold_ocean` where appropriate. Top-level codec field count stays within DFU limits by extending the existing bundled frontier biome record. Old biome-source records do not acquire additional possible-ocean entries.
- `HoleChunkGenerator`: after painting existing city/sewer features, invokes `OceanBathymetry65` on v47 worlds. Ocean block writes are chunk-clipped and use absolute world Y instead of graded street-level Y.
- `OceanBathymetry65`: gravel/clay shallow sea bed, real water through Y=62, clear water surface and air above to Y=176. Keeps deep noise/caves below the shallow bed cap. Land columns are left alone and whole land chunks return immediately.
- `Canvas`: precomputes v47 ocean columns so ordinary city/block-entity writers do NOT place housing, containers, spawners or signs inside open ocean; its absolute-Y sea painter still honors Pass 63/64 structure protection. `CityPopulator` no longer injects surface residents above submerged lots.
- `CitySettings` and fresh Overworld preset: v47; all absent save fields remain backwards compatible.

## Offline verification evidence

- `bash tools/qa-pass65-oceans.sh` compiles the **production `OceanRegions65` source** against minimal climate stubs and evaluates 510,860 coordinate/seed cases, including 69,300 safety-envelope probes and 6,230 neighbor-depth comparisons. Wet/dry and deep ocean results passed.
- `bash tools/qa-pass65-bathymetry.sh` compiles the **production `OceanBathymetry65` source** with minimal terrain/ChunkAccess/Canvas stubs, verifies real water at sea level, sea floor, upper air clearance, unchanged land columns and complete no-op on v46 worlds.
- `python tools/qa-pass65-source.py` checks source/preset integration, optional codec defaults, virgin worlds, same policy between biome and terrain, vanilla structure protection, ocean-prevented block-entity writes, Nether/End, and saved-world compatibility.
- `bash tools/qa-pass64-all.sh` and `bash tools/qa-pass60-all.sh` run the previous offline regression families (nested back to Pass 41).

**These tests are NOT equivalent to** a NeoForge compilation, actual biome registry deserialization, true climate sampler, ocean monument placement, survival balance or a rendered world. They use controlled Java climate/Canvas fixtures.

## Wednesday acceptance in Minecraft 1.21.1 — Claude

1. Restore the checksum-verified official Gradle wrapper and compile with `gradlew.bat build`, `runData`, a Minecraft client and a dedicated server. Fix all Java/codec/registry errors first; don't quietly remove vanilla ocean holders to bypass them.
2. Start a new `normal` world and test several seeds. Validate randomized land spawn and actual `/locate biome minecraft:deep_ocean` results far from the start, and use F3 to confirm true vanilla ocean IDs.
3. Travel to a sea by boat; verify substantial depth and coherent coastline, no paved ocean floor, no city towers in the water, no ghost loot containers, and no chunk seams. Check performance and map loading.
4. `/locate structure minecraft:ocean_monument`; confirm an intact underwater monument, guardians/elder guardians, treasure and usable sponge acquisition. Likewise test `minecraft:shipwreck` and `minecraft:ocean_ruin_cold`, their legitimate underwater loot and maps; check actual generated structures, not command-only success.
5. Confirm sea creatures naturally spawn and resupply (fish, drowned etc.) and ocean ecology does not accidentally dominate all wilderness.
6. Check underwater cave/aquifer continuity and avoid ocean painter collisions with Pass64 protected strongholds/Ancient Cities/mineshafts/trial chambers. Examine shoreline cut-off lots and sea-floor transition.
7. Play survival with no techniques: obtain food, wood, iron, portal materials, and reach the Nether and End normally. Test Ender Dragon victory. Test Warden habitat at a genuine Ancient City.

## Open gaps — don't claim solved

- **Warm oceans, coral reefs, beaches/turtle nesting, full underwater ecology and all vanilla water structures** are not yet fully implemented or verified. This pass introduces native temperate/cold/deep ocean habitat only.
- Ocean coastlines may cut old city parcel shapes awkwardly. The painter suppresses writes to submerged columns, not a sophisticated waterfront/quay building-placement strategy. A later pass should reserve shore plots and build accessible waterfront transitions.
- Cities below sea level may meet shore walls 2–4 blocks high; actual erosion/survival balance must be judged from Minecraft, not static tests.
- Marine structure starts must be **physically verified**. Vanilla biome ID eligibility is a prerequisite, not proof of structure generation.
- World generation remains uncompiled in this environment. The included Gradle wrapper JAR still must be installed from an official verified source on Wednesday, November 4, 2026.
- Existing worlds should be backed up before any mod update or datapack change. World preset v47 governs freshly created worlds only.

**User-approved design rules remain:** The Hole spans randomized climates and dense or sparse neighborhoods independent of biome, vanilla Minecraft survival remains possible without techniques, sewers are dangerous but fair, no separate Forgotten City (user meant vanilla Ancient City), and environmental poisoning/Ash-Snow/Sludge-Ice/faction-villager overhaul stay deferred until visual world quality is approved.
