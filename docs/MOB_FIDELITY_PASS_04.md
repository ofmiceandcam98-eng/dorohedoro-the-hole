# Mob Model Fidelity Pass 04 — 2026-10-31

## Reference and deliverables

The **user-approved visual target** is `docs/reference/approved_creatures.png` (stylized concept art). This update modifies actual NeoForge Java `HierarchicalModel`/`LayerDefinition` source, unique UV atlases and JSON geometry data. It is **not a replacement of the Minecraft renderer with a concept image**. Previews are rendered directly from current JSON cuboids **with colors sampled from the actual texture atlas** at `docs/reference/fidelity_existing_mobs_04.png` and `docs/reference/fidelity_new_mobs_04.png`. Images are **static source renders**, not in-game screenshots.

### Model improvements

| Creature | Current cuboids | Source model improvements |
|---|---:|---|
| Sewer Cockroach | 138 | Overlapping tapered exoskeleton segments, layered and veined elytra, physically separated shield/head, more eye facets, articulated spiny legs, curved segmented antennae and mandibles |
| Scrap Rat | 124 | Tapered rump/shoulders, mottled layered pelt, ear folds, eye highlights, nose, cheek tufts, whiskers, multiple toes, segmented curled tail |
| Sewer Leech | 99 | Uneven tapered wet rings, lateral folds, mottled dorsal highlights, black recessed mouth, tooth rows, side teeth, slime drips |
| Glow Moth | 125 | Stepped fringed wing membranes, separate hindwings, luminous spot rows and veins, feathery antennae, cream collar and thorax fibers |
| Sludge Crawler | 42 | Organic stepped sludge silhouette and stratified drips, viscous-arm detail, toothed maw |
| Rust-Eater Beetle | 54 | Overlapping metallic scutes and cuticle chassis, segmented legs, antennae and rust color accents |
| Cavern Burrower | 62 | Sloping furred shoulders, rough coat tufts, asymmetrical brows, digging claws and strong paws |
| Spore Crawler | 53 | Multi-tier fungal caps, glowing spore nodules, gill detail, small clustered leg growths |

Each model now uses an original procedural **pixel-textured UV atlas**. The texture painter intentionally introduces pixel-scale mottling, light/dark face variation, and separate palettes for eyes, fur, chitin, slime, fungus and light markings. It does **not** yet implement true normal maps, transparency, emissive shaders, procedural animation rigs, or sophisticated physically-based materials.

## Quality acceptance checks: before calling the visuals finished

1. `./gradlew runData`, `./gradlew build`, `./gradlew runClient`, `./gradlew runServer` with network-enabled Java 21 / NeoForge 21.1.265; verify all eight registered layers render, with no `ModelPart` missing, no atlas overflow, no purple/black textures and no registry errors.
2. Take same-angle screenshots from *live Minecraft* for each mob in daylight-equivalent lighting and in realistic sewer/cavern lighting; compare **silhouette, scale, face, texture palette, limb positions, animation** directly with `docs/reference/approved_creatures.png`, not only with the offline renders.
3. Inspect shoulder and toe positions and render all views (front/side/top) while moving and stationary. Ensure legs don't clip world geometry, antennae don't protrude strangely, and mouth articulation remains visible.
4. Confirm all four pre-existing mob AI behaviors, spawn conditions, loot, shearing/brushing routes, processing recipes and effects still work after changed model geometry; none were intentionally changed here.
5. Confirm new four mobs' combat AI, pathfinding and spawn rates, drops and crafting utility. Run dedicated multiplayer test with several clients and unload/reload chunks. Prevent droppable duplication from repeated entity interactions.
6. Compare spawn collision boxes against the expanded model silhouette. Adjust `ModEntities` sized widths only after confirming actual gameplay and collision behavior.
7. Prioritize final polish where current models still differ: concept cockroach curved thin appendages and mandibles, organic rat fur shaping, rounded leech sucker and body rings, moth wing silhouette and genuinely luminous wing/eye rendering. More texture-authored art / advanced rendered effects may be needed.

## Offline verification (not Minecraft runtime testing)

- Source geometry arrays have valid positive dimensions, UV islands within the atlas, model source cuboid parity and matching PNG dimensions.
- Java syntax parsing and all existing structural QA must pass.
- Static previews sample original texture pixels and use deterministic projection, not Minecraft's renderer.
- Full NeoForge build remains blocked in this isolated environment because Gradle 9.2.1 is not locally cached and network access is unavailable. Do not call these models visually or gameplay release-ready until the live build tests pass.

## Scope and compatibility

- No change in mob AI, spawning, named-character canon, loot rates, crafting recipes, items, ore generation, saved worldgen, mining, city terrain, dimension keys, or portals.
- `docs/model_geometry/*.json` is the authored geometry source used to generate the matching Java layer data. Keep it synchronized if you manually edit models later.
- Maintain the user's hard requirement: each *future* creature needs proper original 3D/voxel artwork, full functional drops, a real survival use, and dedicated tests; passive creatures should offer nonlethal harvesting where sensible.
