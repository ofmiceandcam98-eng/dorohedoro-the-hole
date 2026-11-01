# Mob animations and living Glow Moth lighting — source-integrated, not runtime verified

## Design target
The approved high-quality creature concept sheet remains authoritative for visual appearance. The eight creature models use actual Java 1.21.1 `HierarchicalModel` / `ModelPart` parts, regenerable from `docs/model_geometry/*.json`. Each anatomical part now has a local articulated pivot. The preview GIFs under `docs/previews/animation_pass07/` are rendered from the same authored cuboids and UV texture pixels with ported rotation curves. **They are not Minecraft footage and cannot guarantee that in-game rotations look exactly like the previews.**

## Distinctive movements
| Mob | Walk/fly | Idle | Hit / attack |
| --- | --- | --- | --- |
| Sewer Cockroach | Alternating tripod scuttle with lifted feet | Independent exploratory antenna sweeps | Head dip, antenna recoil, faster leg twitch |
| Scrap Rat | Alternating footfalls, tail inertia, body bounce | Sniffing and head turns, breathing, tail swish | Head jerk, hunch and recoil |
| Sewer Leech | Traveling body-segment undulation, stronger in water | Gentle pulsing, gullet breathing | Forward sucker lunge, body ripples |
| Glow Moth | Separate forewing/hindwing shoulder hinges, delayed hindwing cycle | Folded wings and still abdomen | Mild flinch during hurt state (not hostile) |
| Sludge Crawler | Alternating tentacles with sloshing body | Slow wobble and breathing | Tentacle slap and head lunge |
| Rust-Eater Beetle | Tripod gait and bobbing body | Scanning head / mouth motion | Feeding/bite gesture |
| Cavern Burrower | Opposed quadruped footfalls | Head-bob and sniff | Foreclaw dig/slash and lowered head |
| Spore Crawler | Alternating insect legs | Spore-body breathing / swaying | Spore body swelling and head thrust |

`entity.swinging` powers attack motion when the AI executes a swing. `entity.hurtTime` controls flinch/recoil. Dedicated bite, dig and spore-release action states are still future work: current attacks and specials are **animation foundations**, not timed projectile / particle events.

## Glow Moth — actual moving environment light
Vanilla Minecraft does not normally provide colored/dynamic entity lights. An always-bright renderer alone would NOT illuminate walls. This mod now registers `dorohedoro:glow_moth_light`, a no-item invisible block that emits **block light 12** and is **placed by the moth on the server** in naturally empty air every 10 ticks. Light falloff and solid-block occlusion are provided by the regular Minecraft light engine: at most about 12 blocks in unobstructed air (not a perfect sphere and not blue/green colored light). A scheduled 14-tick expiry checks for a living moth within a short distance; if none is present the light clears. A nearby moth causes the block to reschedule expiry. The block is noncolliding, replaceable, has no loot and is not intended for survival acquisition. It does not overwrite non-air blocks, water, redstone, doors or constructions. Entity model lighting uses `getBlockLightLevel >= 12` to keep the moth itself visibly bright.

**IMPORTANT**: This is a vanilla-light-engine workaround, not proof of flawless dynamic lighting. Confirm no stuck lights after death, teleport, unloading, crash/restart, block placement, rapid flying, claims/mod conflicts, and mass spawns. Existing unloaded scheduled ticks should generally survive saves, but abrupt crashes need validation. The 10-tick light update may look stepped and has server lighting cost. If unacceptable, swap to an optional dedicated dynamic lighting engine, not a permanent light-block spam system.

## Mandatory player verification
1. `gradlew.bat --no-daemon build` and `gradlew.bat runData`; ensure no Java or missing asset errors.
2. `gradlew.bat runClient`; create fresh world; `/summon dorohedoro:sewer_roach`, `scrap_rat`, `sewer_leech`, `glow_moth`, `sludge_crawler`, `rust_eater_beetle`, `cavern_burrower`, `spore_crawler`.
3. Watch each creature **idle**, **walking/flying**, **attacking (hostile)**, **hurt**. Check pivot articulation, jitter, texture stretching, clipping, size/collision, real leg contact, and foot sliding.
4. Spawn Glow Moth in a **sealed dark cavern**; use F3 targeted light or adjacent wall brightness to confirm actual BLOCK light; compare at 0, 3, 6, 10, 12+ blocks with obstructions and water. Confirm a radius (not merely full-bright entity texture).
5. Fly moth through moving tunnels, kill it, unload/reload chunks, restart server, place blocks where light used to be; confirm no leftover lights, block destruction, stuck bright spots, dupes or unusual mob spawn suppression.
6. Spawn 20/50/100 moths on a dedicated server and check MSPT, light engine/chunk update load, tick queues, block update bandwidth and fairness. Consider light throttling or optional dynamic-light mod support if expensive.
7. Dedicated server (`gradlew.bat runServer`) must not class-load client rendering; test multiple users; disable optional mods and compare; ensure ordinary Minecraft worlds have no lighting corruption.

The full Minecraft NeoForge build and the above gameplay validations are pending in this environment.
