# The Hole — Interior Architecture Pass 15 (Design-review checkpoint)

**Status:** source-integrated, deterministic offline checks passed. Minecraft runtime build and gameplay not yet verified. **Do not advance to the encyclopedic Dorohedoro entity/magic content phase until the user approves the current city architecture.**

## Changes in new worlds

- Residential: distinct shared lobbies, dual flats off a protected circulation aisle, communal sleeping rooms, and live-work studios. More than one layout selected by stable per-building coordinates and floor. Real placed door blocks at interior partitions; survival-usable beds, barrels, crafting stations and lighting.
- Shops: varying groceries, repair, food-counter and book/salvage fixtures. Larger properties have separated storerooms with doors.
- Clinics: ward, exam, laboratory and triage treatments; larger rooms separated by glass partitions with door openings; beds, brewing and supplies.
- Warehouses/factories: side-wall work alcoves, overhead light chains and optional rear supply bays.
- Versioned `world_detail_version: 4`; older world codecs still accept versions 0–3. The v3 irregular city plan, diagonal roads, differentiated façades, ruin states and preserved mining remain in effect.

## Quality gate — user review before more systems

1. Confirm a **fresh world** shows non-square neighborhoods, diagonal routes, variable building footprints, damaged streets and ruin neighborhoods rather than repeated grids.
2. Enter apartments on at least five blocks: check both doors, all floor-to-floor stairs, headroom, actual room depth and reachable beds/storage; look for duplicate layouts at a glance.
3. Inspect stores and clinics: verify functioning interaction with furnishing and that backrooms are actually accessible.
4. Walk through large industrial sites: inspect workplace density and collision, service yards, roofs, exterior detail and no blocked escape routes.
5. Verify foliage, smog, nighttime lighting, underground sewers, conventional ore/caves, spawn plaza, crops and world performance.
6. Verify world-generation **chunk-order independence**, roads, diagonal reservation, roof/floor seams and old world compatibility.

When the user reviews screenshots and identifies quality gaps, fix **those specific gaps** first. No new lore factions, high-tier entities, mushroom magic or event mechanics until this environment phase meets their standard.

## Offline tests

- `javac -d test-out src/main/java/com/dorohedoro/hole/worldgen/city/{CityHash,Rect,InteriorPlan15}.java tools/VerifyInteriorPlan15.java`
- `java -cp test-out VerifyInteriorPlan15`
- `python tools/qa-interiors-pass15.py`
- `python tools/qa-irregular-city-pass14.py`
- `python tools/qa-architecture-pass13.py`
- `python tools/qa-world-detail-pass12.py`
- `python tools/qa-derelict-buildings-pass12.py`

**These are not a substitute for** NeoForge `runData`, `build`, `runClient`, dedicated server, seed QA or in-game assessment.
