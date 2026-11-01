# Pass 19.7 — Smoke Magic Source Completion

**NOT COMPILED. NOT RUNTIME TESTED.** This is a complete source checkpoint, not a functioning JAR.

## Implemented in source

- 64 registered spell techniques / 8 for each of 8 innate types, with an execution branch for each.
- Each technique has rank, smoke cost and cooldown; 8 equipped slots × 9 saved loadouts.
- Cooldowns stored in the persistent SmokeState (an optional codec field for older saved profiles).
- Server checks character origin, innate family, learned state, smoke, cooldown, target and line of sight before casting.
- Per-slot Focused casting (Alt + same slot key) with higher smoke costs and modified effects.
- Smoke Ledger paginates techniques 3 at a time; HUD cooldowns decrease and are refreshed by server snapshots.
- Healing and protection support nearby cooperative players; offensive effects exclude other players by design.
- Family-specific vanilla visual particles, mastery increases duration and damage/healing scaling.
- The prior personality quiz and Smoke Flask production/release remain in the full archive.
- Existing world generation, district, portal and Cavern Lamp files remain unchanged.

## What remains unfinished

- NeoForge compilation, client startup, dedicated server startup, source/API compatibility validation, and all in-game casting acceptance tests.
- Custom spell animations, model transformations, world-altering mushroom growth, canonical time travel, specialized curses and new magic families.
- Fully tuned PvP, economy and progression balancing, item sound effects and new art.
- Rarity exceptions (Black Powder, graft surgery, Devilhood) remain intentionally excluded.

## Portal regression invariant
`PortalService.Frame.stand(int sign)` returns `core.offset(0, 0, sign)` for X-axis frames or `core.offset(sign, 0, 0)` for Z-axis frames, with no +1 vertical offset.

## Next on Windows
Unpack to a separate development directory, preserve your working JAR and worlds, perform a normal NeoForge 1.21.1 / Java 21 build with internet available, fix any errors, and test on a disposable local world before installing publicly. Then return to lore-accurate city architecture as planned.
