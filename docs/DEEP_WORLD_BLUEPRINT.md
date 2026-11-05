# Deep World Vision — Dorohedoro-inspired Minecraft survival

This is a **design specification** and acceptance sequence, not a list of features already shipped.

## Guiding rule
Every district should be **visually distinct, socially legible, mechanically useful and fully explorable**. A dense district is not just more boxes or materials. Every structure needs a reason to exist, purpose-built rooms, readable entrances and survival rewards. The tone comes from real, believable wear: stained concrete, old brick, grafted repairs, rusted sheet metal, wet plaster, signage, shop shutters, pipe risers, crowds and cramped back alleys. Avoid random surface noise, gratuitous neon, endless abandoned wasteland, and anachronistic futuristic towers.

## Phase A — World and architectural depth: FIRST, approval required
1. **Urban morphology:** multi-width irregular streets, back-lanes, pocket courtyards, corner lots, passages through connected buildings, varied heights and age of construction. Preserve chunk determinism, travel and loot routes.
2. **Physical construction:** foundation/base/wall/floor/roof must make structural sense; windows and staircases align by storey; doors usable; balconies enterable if present; roofs shed rain; utility access/boiler rooms; no disconnected floating trim or windows into retaining walls.
3. **Seven visual identities:**
   - Residential: soot tenements, subdivided apartments, communal laundries, rooftop tanks and improvised kitchens.
   - Market: compact shop rows, eateries, shuttered stalls, covered walkways, repair counters and supply caches.
   - Industrial: massive mills, boiler halls, loading bays, gantries, narrow service tracks, vats, power conduits.
   - Waterfront: canal embankments, pump rooms, locks, piers, cold storage, flood staining and bridge crossings.
   - Derelict: collapse with actual structural logic, remnants of old floor plans, blocked entrances with alternate paths.
   - Graveyard: aged stonework, memorial paths, crypts and mortuary services without copy-pasted suburban houses.
   - Outskirts: improvised low-rises, crumbling infrastructure, scrapyards, greenhouses and sparse scrap settlements.
4. **Interiors:** a procedural grammar for shop, clinic, apartment, workshop, warehouse, hospital, diner, bathroom and office, with rooms, lighting, furniture and loot that match purpose; no chests embedded in walls or disconnected stair flights.
5. **Environmental detail:** overhead power/telephone cables, weathered signage, patched plaster, drains, steam vents, stairwell service boxes, real fire-escape routes, service ducts, food stalls, bins, crates and rain gutters. Make detail sparse enough for game readability and FPS.
6. **Named landmarks:** food/medical/commerce/transit/sewer entrances with consistent discoverability and navigation. Look distinct from ordinary generated towers.
7. **Survival parity:** food, wood, iron, farms, cave access, resource tiers, ores, portals and exploration do not depend on Creative. Preserve the existing world-save version and avoid new exploitable duplication loops.
8. **Visual QA gate:** paired screenshots from the same seed across all seven districts, street / rooftop / interior / night, at least 20 building footprints checked, with precise failure notes and performance stats. This gate is not satisfied solely by source-level tests.

## Phase B — Climate and districts: AFTER Phase A approved
- Ash-Snow, Sludge-Ice, toxic snowballs, gradual poison and remedies, visual + gameplay status indicators, safe refuge and proper biomes. Tune so survival remains fair. Cross-Eye camps and faction environments follow only after streets/buildings meet standards.

## Phase C — People, factions and economy: AFTER Phase B / explicit approval
- Distinct citizen classes, workplaces, routines, schedules, social consequences, trade and licensing; no removal of emerald usefulness. Sorcerers, victims, Cross-Eyes, organizations and Devil systems need careful progression and lore framing. Define clear rules for friendly/hostile behaviors and exploit-resistant trade. Avoid importing unlicensed franchise characters/art.

## Phase D — Magic and supernatural depth
- Door keys and links, smoke resources, spell identity, counterplay, distinct Sorcerers' World vs The Hole vs Hell, crafted progression, visual feedback and reliable return paths. Network and dedicated-server correctness before multiplayer claims.

## Phase E — Creatures, combat, world storytelling
- Species silhouette tests, animation timing, ecological niches, creature drop economy, nighttime behavior, ambient sounds, purpose-driven loot, journal/lore discovery. Fix rat anatomy, leech tapering, insect limb shapes and moth wing silhouette before claiming fidelity.

## Release criteria
- Full source plus generated resources, license/asset provenance ledger, reproducible compile on Java 21 + NeoForge 21.1.265, runtime + dedicated server verification, meaningful patch notes, save backups/migrations and no untested claims on CurseForge.
- Don't change the stable `dorohedoro` registry IDs just to rename the public project display title.
