# Pass 24 — Naturalized canals/rivers, fish, sewage infrastructure and interior furnishing

**Checkpoint:** 2026-11-01. Source-integrated, not Minecraft-built / client tested / server tested. Do not claim playable release quality.

## User goals carried into this checkpoint

- Preserve The Hole as a persistent survival sandbox and the connected sewer dungeon with all eight previously created creatures.
- WATERWAYS ARE NOT FLAT BLUE LINES: fish-bearing river-like channels with changing width, bends, shallow margins, deep pockets, muddy beaches, gravelly terraced shores and sections of built-up industrial embankment.
- Preserve the multiple sewer entrances: street access, riverbank maintenance hatches, and underwater culverts into the same dungeon. Keep submerged access hydraulically isolated.
- Add sewer hardware and room variety, with better furnishing of residences, shops, clinics and factories. Do not implement deferred villager/faction or Ash-Snow/Sludge-Ice/Cross-Eye systems.

## Source changes

1. **`RiverChannels24.java`** replaces the straight canal water strip for new profile `sewer_cavern_version >= 5`. 80-block smooth centerline bends, long 144-block + local 40-block smoothed width modulation; water width **7–15 blocks** in tests, kept within existing 19-block city-owned corridor to avoid cutting existing lots. Longitudinal station sampling uses world coordinates and seed, so shape agrees across chunk edges. Narrow reaches, wider pools, shallow outside water (~Y59 bed), deeper inner channel (~Y56/57 bed), confluences, and 3-height stepped shores. Municipal quay surfaces are district-sensitive.
2. **`CanalEcology24.java`** now detects the actual curved river water mask and varying bed height. Placed seagrass, sparse moss/silt, lily pads, and a few fishing/utility stations are water-checked.
3. **`CityPopulator.java`** updates small, deterministic starter salmon/cod schools to sample the new curving centerline and confirm water before spawning. Vanilla water-ambient spawn rules already appear in eight surface city biome resources and remain in place; this is not a new custom fish entity.
4. **`SewerDetails24.java`** adds shaped ceilings, copper pipes and grates, walking banks, municipal lighting and retention of the central wastewater channel. Existing Pass22 dungeon rooms and Pass23 multiple entry points are preserved.
5. **`InteriorDetails24.java`** provides guarded additional furniture to residences, lobbies, shops, clinics and industrial interiors using existing vanilla blocks, avoiding doors, beds, main passages, and inventories.
6. `CityPainter.java`, `CitySettings.java`, `Canvas.java`, normal world preset and inlet code are wired to **fresh-world-only** profile `world_detail_version=11`, `sewer_cavern_version=5`. Older saved profile values remain encoded and accepted.
7. Separate `tools/qa-pass24-river-geometry.py` compiles actual river geometry with lightweight Java stubs (no NeoForge required) and checks 62 seeds/axes over -2048..2048 for smoothness, width variation and reserved-lot boundary. Adds source-derived profile sample CSV.

## Tests actually performed

- RiverChannels24 Java compiled using stubbed Canvas/Blocks only; **62/62** random seed/axis profiles showed changing width; water diameters **7..15** blocks; bank reserve never exceeded; no centerline/width jumps greater than one block per coordinate.
- Java 21 compiler syntax parser: **136/136 Java files**, zero syntax errors (parser does NOT resolve NeoForge API types).
- `python tools/qa-pass24-canal-ecology.py`: **114/114** source assertions.
- `python tools/qa-pass23-waterway-entrances.py`: **26 checks, zero failures**.
- `python tools/qa-pass22-dungeon-ecology.py`: **117/117** source assertions.
- 397 JSON files parsed by the above source QA.
- ZIP entry CRC and extraction integrity checked on packaging.

**Not performed:** official NeoForge `runData`, `build`, actual Minecraft worlds, fluid-flow/waterproof inlet tick updates, procedural lot overlap QA in a client, fish behavior and performance. Do not report a successful gameplay build.

## Risks and acceptance tests

- City relief offsets are applied per column by `Canvas`. Actual water-edge fluid updates and small elevation steps must be checked with real world loading, saved chunk borders, and 5+ canal crossings; corrective bank/canal-level logic may be necessary.
- Test width variation at several seeds and in both X- and Z-oriented canals, comparing shore height transitions, bridge clearances and entrance throat connectivity. Fluid tick for several minutes and reload.
- Confirm salmon/cod persist and replenish but never spawn in air, on streets or in sewer wastewater. Natural city fish spawn only in source-water and above the sewer depth.
- Check full sewer dungeon traversability and all eight existing creature habitats; don't claim new entity spawning performance without in-game evidence.
- Test furniture against every doorway, chest, stair, underground corridor and clearance; confirm no misplaced features when new chunks load beside old save profiles.
- Run `gradlew.bat --no-daemon runData` then `gradlew.bat --no-daemon build` on a Java21 Windows machine with dependencies, serially (never concurrent), correct any actual compilation/datagen errors; then `runServer`/`runClient` and video/screenshot QA.

### Save compatibility

Fresh normal worlds opt into `(sewer_cavern_version=5, world_detail_version=11)`. Older saved worlds keep their original versions; never manually upgrade saved chunk generator versions, as that would cause ugly seams at old/new chunks. Back up saves before testing.

### Known scope limit

The waterways still occupy planned city-owned corridors and junctions; they are naturalized **urban canal-rivers**, not a complete unrestricted erosion/watershed simulation with distant off-grid tributaries. Arbitrary vanilla river biome connections remain future work.
