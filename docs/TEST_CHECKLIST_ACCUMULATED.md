## Pass 18 — DISTRICT IDENTITY; REQUIRED BEFORE USER REVIEW (not yet in-game verified)
- [ ] Compile full NeoForge on Java 21 (`runData`, `build`); launch client and dedicated server.
- [ ] Generate at least 5 fresh v7 seeds; verify distinct residential, market, industrial, waterfront, derelict, graveyard, outskirts palettes in screenshots from street and rooftop.
- [ ] Find and inspect every new landmark: Communal Court, Bazaar, Machine Yard, Canal Depot, Ruin Court, Burial Ground, Fringe Yard. Ensure varied frequency and correct district placement.
- [ ] Walk every new lot's entrance/aisles; check fences/gates, accessible supply blocks, no water flooding, no floating props, no broken crop placement, and no missing NPC behavior.
- [ ] Traverse irregular streets, diagonal boulevards, canals, trams, alleyways and sewer shafts; confirm unchanged navigation and no decoration in traffic paths.
- [ ] Check reverse chunk generation order, negative coordinates, long distance city layout, save/reload, multiplayer TPS/FPS and old versions 0–6 unchanged.
- [ ] Check all prior mining, trees, shelter, Hungry Bug, hospitals, clinics, shops, mobs, loot, portals and dimensions.
- [ ] Obtain USER approval for district architecture before enabling deferred Ash-Snow / Sludge-Ice / toxicity / Cross-Eye camps / villager overhaul.

## Pass 14 — irregular city and 3D building forms (pending actual Minecraft tests)
- [ ] New v3 world on at least five seeds; map 2–5km, confirm irregular intervals, road bends, offset alleys, diagonal through-boulevards and preserved playable city density.
- [ ] Cross boulevard rights-of-way on foot and by vehicle; ensure roads are continuous at chunk borders, bridges clear canal water, and diagonal roads do not pass through buildings or expose floating decorative props.
- [ ] Confirm no player spawning inside roads; plaza, Hungry Bug, market, nursery, shelter, landmark buildings and sewer shafts remain functional.
- [ ] Test L-shaped, inner-courtyard and terraced multi-storey buildings: safe entrance, stair/roof paths, accessible rooms, windows, no floating roofs/furniture, roof crops, and safe exterior railings.
- [ ] Test detached industrial outbuildings and roof offices: ladder, doors, loot, accessible workstations; never trapped inside or no access from main street.
- [ ] Test environmental variety from street level and above, abandoned-building variation, roadside junkyards/markets, derelict density and 20+ neighborhoods; look for excessive empty parcels.
- [ ] Cross-seed and reverse-chunk-order generation, negative coordinates, city relief, old v0/v1/v2 save behavior, server performance, client FPS/TPS, dimension portals and other systems after worldgen change.
- [ ] Before release: run actual NeoForge `runData`, `build`, `runClient`, `runServer`, and multiplayer QA; offline syntax/static tests are not a substitute.

## Pass 13 — architectural variety and physical access (pending in-game verification)
- [ ] Four fresh v2 seeds; compare 50+ nearby building fronts and roofs from street/roof viewpoints, count visible repeats.
- [ ] Check six neighborhood themes across industrial, market, residential, derelict, waterfront and fringe; visually recognizable, not copy-pasted.
- [ ] Open and climb buildings from ground-floor door to roof, ensure façades and penthouse masses do not trap players/NPCs or block stairs.
- [ ] Test storefront signs, shutters, porches, fire exits/balconies, roofs, crops, water tanks, lanterns and functional block entities.
- [ ] Check streets, crosswalks, tram tracks, canals, shafts, sidewalks, bridges, utility props and clutter remain traversable.
- [ ] Test derelict facade collapse/loot/interiors; cross-chunk seams, negative coordinates, world save compatibility v0/v1, in-game FPS/TPS.
- [ ] Run NeoForge build, runData, client/server and deterministic seed/chunk-order QA. Current environment could not compile.

# Dorohedoro: The Hole — Accumulated test checklist

**Status:** A test plan, **not** a set of completed results. User requested this checklist be retained for later. Flag each item once run, capture seed/log/screenshots.

- [ ] Gradle/NeoForge 1.21.1 `runData`, `build`, `runClient`, `runServer`; no compile errors, no missing registries or datapack warnings
- [ ] Fresh-world and reloaded-save spawns at Hole plaza; city roads, bridges, terrain relief, height pads, chunk-order determinism, cross-chunk continuity
- [ ] Hole smog completely obscures sun/moon/sky/clouds while retaining day/night gameplay lighting, weather, and local water fog; Sorcerers' World and Hell not polluted by this visual effect
- [ ] Varied city districts; real furnished usable interiors, chest loot, furnace/smoker/sign block entities, doors, resources and civilian NPCs
- [ ] Unrestricted vanilla survival: crafting, trees, crop farming, vanilla ore veins/diamonds, deep caves and caverns, ravines, strongholds/End, Nether where intended, redstone, trading, mining, multiplayer and inventory persistence
- [ ] Sewer network 30% cave-leading junctions, 18% inspection branches, remaining ordinary sewer corridors; gradual masonry/damaged/rock transition, no instant caves, safe graded stair landings, ~1-in-6 wide caverns, real traversable deep caves and working vanilla ore distribution
- [ ] Sorcerers' World and Hell dimensions register/load; terrain, biomes, atmosphere, ores, native trees, creatures and return portals; repeat travel and restart
- [ ] Artificial and Devil Gate survival recipes, portal frames, activation and paired safe return, permissions, persistence, crash safety, no duplicated drops from auto-generated destinations; Smoke and En Doors as they are implemented
- [ ] All materials and mobs: unique texture/models, UV checks, animation, spawn biome and spawn cap, vanilla-plus performance, loot with Looting, recipes, effects, balance and Creative accessibility
- [ ] Batch 03 all eight mob models: compare in-game texture detail, silhouettes and scale with `docs/reference/approved_creatures.png`; inspect moving legs, antennae, wings, tail, body segments; animation pivots and bounding boxes
- [ ] Sludge Crawler and Spore Crawler: spawn rates, chase/attack AI, slowing and Poison effects, guaranteed gland/sac and rare tar/membrane, recipes and antidote removing Poison
- [ ] Rust-Eater Beetle: passive underground AI, brush interaction and durability, 4,800-tick cooldown across reloads, renewable carapace, corrosive gland loot and recycling balance
- [ ] Cavern Burrower: 24 HP difficulty, deep cave spawning, claws and hide, mining/crafting recipes, threat/loot balance against normal iron/diamond progression
- [ ] New cockroach + rat: correct attack/flee, drops, food cooking, armor components, probe detection/cooldown and wool recipe
- [ ] New leech: near-water spawn, brief Slowness, leech drops, Medical Salve Regeneration, sealant, slime-ball recycling
- [ ] New moth: bat-like flight, glowing textures, silk/scales, shears nonlethal harvest only every 6000 ticks, NBT restart, Night Vision tonic, cavern lamp brightness
- [ ] Zinc/Oil Shale: stone and deepslate spawn, Fortune/Silk Touch, blast furnace/normal furnace, brass + galvanized building blocks, bitumen + alternate slime sealant recipe
- [ ] Later magic: one innate individual sorcerer magic type, smoke pool/output quality, bottles/masks/black powder, grants, smoke doors, server validation and multiplayer synchronization
- [ ] Later events and NPCs: Living Dead Day, trading, named lore-friendly NPC interaction and nonlethal alternatives where possible
- [ ] Dedicated multiplayer with two players, client texture/animation sync, inventory and block replication, TPS, crash/restart/save recovery and abuse/dupe tests
- [ ] Optional FTB Quests stays deferred; if introduced, tracking only, no forced story gates

## Creature Fidelity Pass 04

- [ ] Verify eight rebuilt textured models render in the actual NeoForge client without UV artifacts, invalid part names or crash.
- [ ] Review live front/side/top screenshots against `docs/reference/approved_creatures.png`; inspect curved antennae, face details, body taper, wings and bioluminescent patterns.
- [ ] Check all eight animations and hitbox alignment; check water/flying/ground movement, live model and entity scaling, night and day lighting in the Hole.
- [ ] Confirm mob drops and survival crafting unchanged; glow moth shear cooldown and rust beetle brushing remain safe and persisted.
- [ ] Repeat runtime multiplayer/entity/chunk reload and performance checks with eight mob species enabled.

## Added: Creature visual pass 05
Verify larger Sewer Leech hitbox, watery pathfinding, visible inner/outer mouth teeth and bite reach; Glow Moth dimensional forewing/hindwing articulation, fuzzy chest, flying/resting, texture bake and nonlethal shearing; collision and performance with many entities. See `docs/MOTH_LEECH_REFINEMENT_PASS05.md`.

### Pass 06 model-specific gameplay checks
- [ ] Leech has exactly six conspicuous teeth in client view, no overcrowded gum spikes; inspect from front, side and during attack
- [ ] Glow Moth flaps four hinged wing assemblies (forewing/hindwing × left/right) at thorax pivots, not rotating through body center; inspect full stroke and wing overlap
- [ ] When hanging/resting, moth wings fold naturally and stop fluttering; check smooth transition back to flight
- [ ] Moth hindwings slightly lag forewings; check they do not clip body, terrain, other entities or each other; flight sounds and body motion remain believable

## Pass 07 — 8 mob animation and dynamic light checks
Verify each mob idle/walk/attack/hurt animation, local pivot articulation, clipping, multiplayer sync; Glow Moth real nearby block lighting across radius/falloff, wall occlusion, movement, 100-moth performance, death/unload/restart cleanup; compare against approved design; do not mark runtime verified without screenshots.


## Mob movement pass 08 — five-second animation review
- Build, run and record **at least 5 uninterrupted seconds** of moving each of the eight mobs (not only isolated preview loops). Check walk/run/idle transitions, foot lifting and foot sliding, contact on stairs/slabs, collision and block obstacles, Leech movement on land versus water, Glow Moth wing coherence and temporary block-light radius/cleanup, multiplayer timing, performance in packs. Compare against `docs/reference/approved_creatures.png`.
- Offline five-second previews use actual cuboids and texture pixels but are NOT Minecraft gameplay. They do not verify pathfinding, animations rendered by Minecraft, or dynamic lighting.

## Cavern Motion Pass 09: newly required live tests

- [ ] Check Rust-Eater Beetles actually spawn naturally on industrial streets, derelict lots and Hole outskirts, by day and at night, without spawning in other dimensions or vanilla biomes; inspect distribution vs underground.
- [ ] Verify surface Beetles obey fluid, floor and height constraints, avoid roofs and protected buildings, and still drop/brush useful Metallic Carapace consistently.
- [ ] Compare old/new Sludge, Beetle, Burrower and Spore movement on flat ground, stairs, slabs, sewers, rock slopes and in combat; inspect foot contact, clipping, gait speed, server/client synchronization and AI pathfinding.
- [ ] Capture 5-second in-game videos and compare against offline previews; confirm changes are real rather than GIF-only playback acceleration.
- [ ] Re-run `runData` and verify surface spawn weights Industrial 9 / Derelict 6 / Outskirts 3 / Undercity 18 in the final game data, plus verify loaded existing save data.

## Pass 10 — Cavern mob articulation and gait checks
- [ ] Record and compare 5+ seconds of Burrower, Beetle, Sludge, and Spore moving over level floors and uneven terrain; compare *in-game* footage to new source-rendered 5-second GIFs. Check independent body weight/leg stepping, meaningful animation speed, and absence of sliding.
- [ ] Confirm Beetle mandibles and antennae pivot at the head while feeding/turning; Sludge drips and tentacles do not detach; Spore sacs stay on back and wobble without clipping; Burrower foreclaws do not phase through rock.
- [ ] Trigger attack and hurt states with actual player combat, then idle after stopping; check transitions, performance with packs, cross-chunk movement, dedicated server and animation stability following world reload.
- [ ] Verify Rust-Eater Beetle still spawns on surface in correct biomes and remains brushable; all mob loot/recipes unchanged. Recheck Glow Moth light block cleanup under high population as part of wider runtime certification.


## Spawn Egg Testing — Pass 11 (source-integrated, requires live build)
- [ ] `gradlew.bat runData`, `gradlew.bat build`, `gradlew.bat runClient`, and `gradlew.bat runServer` succeed on Minecraft 1.21.1 / NeoForge 21.1.265.
- [ ] In Creative, `The Hole: Creatures` and vanilla `Spawn Eggs` both list exactly the eight intended Dorohedoro eggs; existing `The Hole` tab still lists them.
- [ ] All egg names and colors render (no pink/black missing-texture squares); Creative Search finds each.
- [ ] Right-click with every egg in an open flat area; exactly the intended mob spawns, with no crash or missing model.
- [ ] Spawn each in a permitted test enclosure below ground and on the surface; egg-spawn should work even when its natural spawn biome or height predicate would not allow a random spawn.
- [ ] Spawn Glow Moth in a dark enclosed cavern and check moving level-12 light, radius, cleanup after motion/death/world reload, FPS impact and multiplayer behavior.
- [ ] Check leech collision/size, motion and animations for all eight entities in-game.
- [ ] Kill / shear / brush as appropriate to validate the existing drops and the survival recipe paths, no duplicate item farming exploit.
- [ ] `/give @s dorohedoro:sewer_roach_spawn_egg` and `/summon dorohedoro:sewer_roach` work as emergency fallbacks; repeat for all eight mobs.
- [ ] Spawner/dispenser egg behavior and restart persistence; test in a dedicated server with multiple players.
- [ ] Ensure eggs remain Creative-only and do not accidentally become obtainable in ordinary Survival.

## Pass 12: whole-world visual detail
- [ ] Generate 4 new worlds, sample all city districts at street and roof level; compare against Greenfield/World of Keralis *quality expectations*, not exact structures.
- [ ] Check shop signs, window supports, awnings, roof parapets, balconies, industrial vents, rooftop utility structures and walkability.
- [ ] Verify paths, trams, crosswalks, drainage, muddy road patches, sidewalk props, gates, sewer entrances, bridges and canals are navigable.
- [ ] Compare at least 40 buildings and 20 adjacent superblocks for distinct silhouettes and ground-floor use.
- [ ] Confirm new-world-only `world_detail_version` and unchanged legacy world boundaries; chunk order invariance and performance at 8-16 chunk view.

## Pass 12 — Broken-down and abandoned buildings
- [ ] Fresh world at `world_detail_version=1`: walk derelict, outskirts, industrial, residential and active markets; validate distinct frequencies of ruins and intact buildings.
- [ ] Inspect boarded windows, CLOSED/ABANDONED storefront signs, rust repair panels, broken masonry, collapsed upper corners, roof gaps, cobwebs and shell remnants at street and roof level.
- [ ] Enter >20 abandoned buildings: open ground-floor entrances, navigable stairs, no collision traps in doorways or hidden single-block falls in stair routes. Check railings/windows/roof access.
- [ ] Break/salvage ruins in Survival; no illegal blocks, phantom blocks, invalid signs, impossible resource requirements, or duplicated loot.
- [ ] Check the original food/sapling/water/animal buildings and all lore landmarks survive (no ruin overlays).
- [ ] Confirm abandoned shops have no ordinary shopkeeper and active shops do; no NPC suffocation; check mobs can navigate derelict lots.
- [ ] Generate adjacent chunks in opposite orders along a collapsed roof/wall and compare building shape and block-entity NBT for determinism.
- [ ] Verify old worlds with missing version retain original lot distribution, style and population after update. Do not change the setting mid-save.
- [ ] Test heavy rain, day/night, Glow Moth light, hostile spawns, loot and dedicated-server performance in abandoned buildings.

## Interior Architecture Pass 15 — Approval Gate
- [ ] Fresh-world v4 buildings: diverse floor plans, service rooms, apartment divisions, clinics, studios and factory alcoves
- [ ] Stairs reachable on every floor; both apartment doors open to walkable areas; no beds/counters block doors
- [ ] Shop stockrooms and clinic wards reachable; barrels/chests/furnaces/smokers/brewing stands function and persist contents
- [ ] Irregular streets and diagonal boulevards do not repeat square blocks visually; varied ruins, rooflines and connected streets
- [ ] Ensure all new architectural details meet user's approval **before** beginning lore-entity/magic content expansion

### Pass 16 — Polluted-city vegetation
- [ ] Confirm the Hole is a smoke-obscured **surface** city, not a cavern with a literal roof.
- [ ] In new worlds, inspect 100+ outdoor lots across districts: healthy full-sized trees should be exceedingly rare; mostly dead trees, bare lots, and a few struggling specimens.
- [ ] Check dead and stunted tree geometry against chunk boundaries, streets, roofs, sidewalks, and covered alleys; no block obstruction or floating leaves.
- [ ] Harvest nursery saplings and city salvage logs; obtain vanilla planks, grow personal trees, and verify sustainable survival wood access.
- [ ] Check nursery glass shelter, chest and saplings remain usable and protected.
- [ ] Compare saved world v0–v4 to fresh v5 vegetation; no legacy world chunk seams.

## Pass 17 cold districts and frostbound highlands
- [ ] Fresh-world Hole cold biome appears rarely and only far from spawn; older saves unchanged.
- [ ] Cold paving dusting is sparse and does not block transit, building entrances or sewer hatches.
- [ ] Sorcerers frostbound highlands generate with visible ground snow but no ordinary precipitation.
- [ ] Biome feature sorting, mining, nearby biome transitions and multi-seed determinism.
- [ ] Full `runData`, `build`, `runClient`, `runServer` and multiplayer checks.
