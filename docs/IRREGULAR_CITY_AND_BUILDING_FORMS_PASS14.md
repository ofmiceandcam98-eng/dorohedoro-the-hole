# World Detail Pass 14 — Irregular City, Streets, and Buildings

Status: **source-integrated, offline geometry-tested; NOT compiled or verified in Minecraft.**

## Why this changed

The player rejected the aerial view of many repeated square city blocks. Higher-resolution façades alone did not solve it. The city must vary at macro road-network scale, at local streets/blocks, and at actual 3D building volumes.

## New world generation (`world_detail_version: 3`)

- Preserve the exact old v0/v1/v2 street generator for existing saves. The new preset opts into v3; create a new world to see it. Never flip a generated saved world's codec setting live.
- Macro grid: *different X/Z arterial intervals* (`144 × 104` nominal, with seeded ±32 jitter away from the starting quarter) rather than square 120×120. Deterministically bending AVENUE stretches ±4 blocks between unchanged intersections; tram rails and canals stay straight, as do sewer galleries. Main road crossings and protected starter plaza remain connected.
- Reserve six additional blocks at arterial edges for avenue bends so roads cannot bisect building footprints.
- Interior blocks: 87.9% of sampled blocks select staggered, offset, stepped-diagonal local streets with 5-block transverse alleys. Layout consists of nonoverlapping rectangular buildable parcels enclosing local road passages; this is more irregular than a simple binary subdivision, but many small lots remain rectangular to fit Minecraft stairs, beds and furniture.
- **Genuine diagonal boulevards**: two analytic 45-degree families of wide multi-block corridors cross the city. Their seeded intercepts vary by ±50 blocks; the spacing is roughly 660 blocks. These routes cross multiple arterials, not just one individual city block. Lots touching the road are repurposed as open roadside market areas, junkyards, rubble, or vacant lots, with road right-of-way paved after decorative lot generation.
- Spawn plaza and nearby food/shelter remain protected within a 380-block radius. Landmark superblocks are also protected (a boulevard can locally stop and rejoin the connected existing road system). No worldgen change beneath street base: existing vanilla ores, caves, sewers, shaft entrances, aquifers and 30%-target cave connections remain as before. For canals, deck at worldgen Y=62-64 preserves the water below at Y≤61.
- Street pavement and roadside props follow the offset avenues. Portal and mob implementation remains unchanged.

## Building structural volume variation

All original `Tower` interiors, doors, stair flights, roof gardens and functional equipment are generated first. In **v3 only**, a postprocessing pass picks a deterministic structural profile based on each building seed and dimensions:

1. **L-shaped block**: northeast recess becomes open air and a small ground court; new enclosing brick and glass walls seal exposed rooms. The existing northwest stair flight and south-centered ground entrance are untouched.
2. **Courtyard tenement**: a lightwell is cut through the center, with new courtyard walls/windows, roof ring railings, and a ground-floor passage back into the building. Only available on larger buildings.
3. **Upper-floor setback / terrace**: a southern upper corner becomes an open terrace with railing and lower-floor deck. Existing roof gardens are excluded from this shape.
4. **Intact volume**: a fraction of buildings remain complete rectangular shells, reflecting real varied neighborhood age rather than an endless procession of recesses.

Industrial properties with suitable plot size can also reserve a side yard and create a **detached small workshop** with doors, furniture, salvage and a gravel approach, plus a separate rooftop factory office with ladder access and workstations. Otherwise they keep the prior hall without an inaccessible miniature structure.

## Offline verification performed

- Irregular layout: 24,010 seed/site decisions, 21,102 irregular sites, 141,880 non-overlapping parcels, every sampled street cell flood-filled to an arterial edge; smooth avenue bend ≤1 block per world block, correct intersection centering.
- Building form: 171,360 seeded building profiles checked, all four silhouettes selected, cuts inside bounds; principal stair/ground entrance/roof-garden regions protected by pure geometry checks.
- Diagonal roads: 25,350 lots checked across six seeds, 4,857 edge parcels reserved, 1,056,819 road cells all matched reservations (no unscheduled road through a generated building); spawn core protected.
- 54/54 Pass 14 code/resource assertions. Existing passes: architecture 35/35, detail 25/25, ruins 24/24, Phase3 81/81, Phase4 86/86, sewer 51/51, spawn egg 44, eight mobs 1007, animation 364. Java parser: 88 files 0 errors. Main resources JSON: 62 files, 0 errors.
- `./gradlew --offline compileJava` still attempts to download Gradle 9.2.1; external network unreachable here. **All actual NeoForge runtime and mod-world tests are pending.** Static assertion success is not a guarantee these paths build or render correctly.

## Mandatory in-game QA (consolidated later; do not call this release-ready)

- Generate fresh v3 worlds on 5+ seeds. Fly across 2–5km at varied altitudes. Compare actual street plan against prior repetitive square grid; count diagonal through-routes and verify each reconnects to conventional roads. Watch for empty/scarred plots and road density that becomes too low.
- Walk streets and sidewalks after they bend, cross all 45-degree roads and tram/canal intersections, test pavement heights and bridge clearance. Ensure no pavement blocks, props or rubble obstructing player navigation; roads must be continuous across chunk borders and when chunks generate in reverse order.
- Enter/exit 20+ examples of L-shaped apartments, courtyard buildings, terraced upper floors, industrial separated sheds and raised offices. Check stairhead clearance, portal entrances, open courtyard passage, guard rails, safe descents, container/bed tile entities, lighting and floor stability. Check floating roof equipment after post-carving.
- Test protected starter plaza, Hungry Bug, nursery, market and first bed; verify no diagonals through landmark superblocks or protected sewer shaft kiosks.
- Test negative coordinates, city slope changes, district boundaries, older v0/v1/v2 saved worlds, villages/shops/NPC spawns and farm access, and dedicated multiplayer performance at high view distances. Confirm no excess chunk-write CPU from repeated hash/road checks. Compare FPS/TPS before and after.
- Check underwater canals still flow below diagonal bridge decks, and cave/sewer systems remain fully intact.

## Known limitations

This is not a hand-built city or wholly freeform street simulator. Buildings still use Minecraft-aligned rooms and the backbone remains a connected pair of arterial families, but their intervals are unequal/variable, portions meander, local streets are staggered, and diagonal boulevards interrupt the former square rhythm. Real visual quality and performance must be judged in the client. Any missing/invalid NeoForge API use is a build blocker to fix before calling the mod playable.
