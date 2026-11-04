# Pass 68 — Large municipal stormwater pump galleries

**Status:** real world-generation Java source changed and unit-tested; **not compiled against Minecraft 1.21.1/NeoForge 21.1.265 and not tested in the game**. Fresh worlds use `world_detail_version: 50`. Missing generator version still defaults to 0, and existing saved profiles 0–49 keep their prior content.

## Purpose and design

The upper municipal sewers were relatively uniform seven-wide arched corridors with occasional small Pass67 repair/filter bays. Pass68 introduces a rare, substantial three-dimensional infrastructure destination on straight trunk segments: a **14-deep by 19-long enclosed stormwater pump hall**, with a much higher vault than the ordinary sewer and a walk-in 3-wide entrance. This is not a general city-room decoration or a new technique system; it's an underground workplace and a risky navigation detour.

A representative cross-section, measured outward from the sewer's central drainage trunk:

- Offsets 0–2: **unaltered** existing municipal water course and dry pedestrian lanes.
- Offsets 3–4: intact masonry wall except the 3-wide, 3-high station mouth.
- Offsets 4–15: actual 6-high interior (Y50–55), deepslate floor, roofed masonry shell, repair workstations, an optional operator booth.
- Distance 11–13, along-axis ±2: recessed sump with two rows of contained shallow water flanking a **solid dry spine** that can be crossed with ordinary Minecraft movement. No mandatory deep-swimming or technique-based movement.
- Along-axis offsets +6..+8: partitioned operator booth with an iron door; an adjacent **floor lever** on the public side is intended to power the door. The booth's equipment is optional; the trunk is always open. This tests only blockstate placement, not redstone behavior.
- Along-axis offsets −7: crafting table, furnace, stonecutter. Inside booth: cauldron and grindstone. Workstations are vanilla blocks with actual survival functions.

The room has **no new lanterns, chests, barrels, spawners, scripted poison, or technique locks**. Its wider vault and wet sump make dark sewer exploration more varied and tactical without creating a safe, over-lit shelter network. Pump machinery/flowing water control itself **is not implemented in this pass**. The manual lever operates the iron *access door*, not a fictitious active pump.

## Generation and integration

- `StormwaterPumpHalls68.java` chooses about 1/4 of 192-block trunk sections as **candidate sites**, with a seeded position, side, and distinct X/Z-axis orientation. Many candidate sites are rejected; only non-canal straight segments at least 48 blocks away from crossing arterials are eligible. This protects public shafts, large cisterns, dungeon wings, and cave-exit junctions. The site search uses the *same* far-reaching crossing list in every overlapping chunk.
- Reject any station if **any coordinate in its 14×19 footprint** overlaps an ocean basin or reserved dry beach shelf. Older versions are explicitly excluded.
- `SewerServiceBays67` now conservatively reserves entire potential pump-hall spans so its smaller room cannot compete with a station shell or booth. This rule is enabled **only for version >=50**; versions 0–49 continue their old bay layout.
- `CityPainter` invokes the pump hall after Pass67 masonry/repair bays and before existing cisterns, cave links, and dungeons. No changed worldgen registration, mob, ore, biome, structure tags, or custom dimension files. All writes go through `Canvas`, inheriting Pass63 Deep Dark and Pass64 actual vanilla structure-start protections.
- The feature is designed to stay deterministic for negative coordinates and every chunk generation order.

## Offline validation evidence

1. `bash tools/qa-pass68-pump-halls.sh` compiles **production** `StormwaterPumpHalls68`, `SewerServiceBays67`, `CityHash`, `Line`, `Rect` against minimal stand-in Minecraft types. **100,679 successful assertions**, 2,041 seed-selected station candidates, 895 built in the synthetic city geometry, 952 selected candidates on negative world-coordinate sections, 3 complete multi-chunk stitching comparisons, 35 Pass67 overlap-isolation tests, 4 old-save/sewer-disabled/ocean/beach checks, 20 forced near-crossing rejections. Validates three-wide mouth, floor, wet sump/dry spine, vanilla workstations, both iron-door block halves, and lever placement. These test values are from a controlled synthetic arterial network, **not Minecraft spawn rates or rarity**.
2. `python3 tools/qa-pass68-source.py` checks **34/34** save-version, pipeline, coast, structure, ability-free gameplay, resource JSON, and vanilla-dimension assertions.
3. Historical QA `bash tools/qa-pass68-all.sh` reached Pass51 before 120-second execution limit; it reported no failures through that stage. The remaining `bash tools/qa-pass50-all.sh` was run **separately to completion and passed**, covering Pass50 down through Pass41. The historical Pass67 Java fixture continues to test v49 with a compile-time stub for the *new* reservation API; Pass68 compiles and tests both production classes together at v50.
4. ZIP integrity and CRC will be checked during packaging.

## Wednesday Nov 4 NeoForge checklist (required)

- Use **Java 21**, official **Gradle 9.2.1 checksum-verified wrapper JAR**, **Minecraft 1.21.1**, **NeoForge 21.1.265**. The current source still lacks `gradle/wrapper/gradle-wrapper.jar`; **no runtime JAR has been produced**. See `RESTORE_GRADLE_WRAPPER_WINDOWS.ps1` and `BUILD_AND_TEST_WINDOWS.cmd`.
- Compile the full source and run client/server. Do not assume the isolated Java tests confirm real Minecraft class signatures or block update behavior.
- Generate a **fresh v50 world**. Locate multiple real pump halls, including on negative X/Z sides. Walk through each entrance in Survival, use all workstations, check whether the floor lever reliably powers its iron door, and test any redstone update issues.
- Walk down the entire municipal trunk past each hall using **ordinary armor, food, torches, tools and no techniques**. Confirm center water channel and both dry lanes remain continuous. Test lighting, mob encounters, fishing/fluids, and ability to escape.
- Test halls at chunk borders, near ocean/coast, and near actual junctions; compare new v50 world against a **backup** of v49 save. Check structure-overlap clipping where vanilla starts abut custom halls.
- Verify naturally generated **Ancient Cities/Deep Dark/Wardens**, mineshafts, stronghold portal rooms, trial chambers, Nether and End portals, ocean monuments/shipwrecks/ruins, and a complete technique-free Ender Dragon run. Vanilla playability is not proven by tag presence or `/summon`.

## Known risks and deferred systems

- Rare halls do not yet constitute an overall sewer tunnel-width overhaul. Future work: truly varied storm-drain arteries, valves/floodgate logic, pump operation, and validated natural-cave exits.
- A vanilla structure protection box can clip a room; better to protect vanilla than corrupt a stronghold, but the **room may be incomplete** and requires live testing.
- `Canvas` applies terrain-relative height per column; verify approaches and hall floors do not develop steps in hilly terrain.
- Runtime redstone, iron door halves, fluid flow, natural mob behavior, decoration quality, performance, and vanilla advancement completion remain **unverified**.
- **No separate "Forgotten City"**: that earlier phrase was the user's reference to vanilla **Ancient Cities**, which must remain intact.
