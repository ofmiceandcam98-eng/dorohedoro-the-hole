# Cavern creature movement — Pass 10 (source integrated, runtime unverified)

## Purpose
User reviewed cavern creature movement and found simple limb swinging unconvincing. This pass changes **animation poses in the actual NeoForge Java `HierarchicalModel` classes** and in the canonical `docs/model_geometry/*.json` inputs. All four five-second GIFs are rendered using **those same Java animation expressions**, rather than manually duplicated approximations or speed-up-only playback.

### Changes
- **Sludge Crawler:** traveling forward body push, slow ooze compression, body-height changes, delayed head movement and distinct tentacle and drip joints (six animated drips), so motion feels viscous rather than a rigid walking box.
- **Rust-Eater Beetle:** faster alternating tripod gait, actual stride lifts, abdomen weight bob and roll, individually pivoted antennae and mandibles with search/nibbling motion. No change to its deliberately permitted surface spawning.
- **Cavern Burrower:** asymmetrical heavy front-foot lift and back-foot push, body weight bob/roll, head thrust and distinct attack claw pose. Still a grounded quadruped rather than a bounding rabbit.
- **Spore Crawler:** deliberately asynchronous six-leg phases, uneven sway and head movement, and independent three-spore-sac joints that react to locomotion and attacks.

**No change:** approved Glow Moth four-wing motion and level-12 temporary lighting, six-tooth Sewer Leech, vanilla mining/caves, sewer 30% cave links, mob drops or survival recipes. No new resources are gated.

### Preview / verification
`docs/previews/animation_pass10/cavern_movement_5s.gif` and four individual GIFs: each **50 unique time samples at 10 fps / 5000 ms**, movement-only. The preview loads authored model cuboids, their PNG atlas and actual rotation/position formulas; it is still an offline static-perspective renderer. It does **not** prove Minecraft foot/terrain contact, turn handling, mob movement speeds, idle transitions, hit reactions or dynamic lighting.

Regenerate models/textures: `python tools/regenerate-fidelity-models.py`.
Regenerate video/GIF: `python tools/render-cavern-motion10.py`.
Offline checks: `python tools/qa-cavern-motion10.py`, `python tools/qa-mob-fidelity.py`, `python tools/qa-highquality-mobs.py`, then full Gradle `runData`, `build`, `runClient`, `runServer` on a computer with dependency downloads.

### Required client acceptance tests
- At least five seconds of each mob moving over flat blocks, half-slabs, water margins and slopes. Watch for grounded footfall, sliding, animation tick/joint resets, clipping, pathfinding stalls, attack and hurt-state smoothness.
- For Sludge: inspect whether drips and tentacles appear attached through compression and turning. For Beetle: confirm antenna/mandible joints do not detach and tripod feet alternate cleanly. For Burrower: verify paw contact and no body/terrain penetration. For Spore: verify all three sacs track body movement while rocking independently.
- Spawn and compare 10+ mobs simultaneously on dedicated multiplayer server, cross-chunk movement and reload. Verify Rust-Eater Beetles appear on industrial surface (biome weights 9/6/3), remain underground (18), and are still brush-harvestable.
- Confirm all loot drops and crafting recipes continue to work. Verify Glowing Moth cleanup at chunk unload and death unchanged.

**Important:** No full NeoForge build or in-game verification was available in the working environment. This code is a test candidate, not release-ready.
