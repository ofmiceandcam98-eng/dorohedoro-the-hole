# Roadmap

The phase order comes from `docs/spec/SANDBOX_DIRECTIVE.md`. Status values are `planned`, `implemented`, `build_verified`, `gametest_verified`, `client_verified` and `server_verified`. Evidence for anything past `implemented` is in `docs/TEST_RESULTS.md`.

| Phase | Scope | Status |
| --- | --- | --- |
| 1 | Project audit | Done, see `docs/PROJECT_AUDIT.md` |
| 2 | Hole sandbox foundation | In progress |
| 3 | Sorcerers' World and Hell generation | Planned |
| 4 | Portals: Smoke Door, Artificial Door, En Family Door, Devil Gate | Planned |
| 5 | Resources, crafting, wood sources, gyoza | Planned |
| 6 | Structures, NPCs, trading, environment | Planned |
| 7 | Combat, magic, masks, hostile sorcerers | Planned |
| 8 | Optimization and full survival testing | Planned |
| Deferred | FTB Quests integration, see below | **Not started, on hold until the owner asks** |
| Queued | Complete magic system, see below. Expands Phase 7 | **Not started** |

## Queued: complete magic system

Added at the owner's request on 2026-10-31. The full requirements are in `docs/spec/MAGIC_SYSTEM_DIRECTIVE.md`. It expands Phase 7 of this roadmap with its own 12 internal phases: lore audit, sorcerer biology, magic engine, six first types (Mushroom, Healing, Dismemberment, Reptile Transformation, Destruction, Dispelling), world interaction, smoke economy and equipment, mastery, doors, NPCs and environment, rare powers, FTB Quests, verification.

Ordering conflict to settle: that directive puts magical doors at its Phase 8, while the sandbox directive puts portals at Phase 4. The Artificial Door and Devil Gate need no magic, so they stay at Phase 4. The Smoke Door needs smoke, so it should ship with sorcerer biology, on the shared portal backend.

## Deferred: FTB Quests integration

Added at the owner's request on 2026-10-31 with the instruction **not to start yet**. The full requirements are in `docs/spec/FTB_QUESTS_DIRECTIVE.md`.

It is an optional guide and achievement layer on the real FTB Quests mod. It must never gate recipes, dimensions, portals, trading or abilities, so it does not conflict with the no-quests sandbox rule.

Dependencies: it references real registry IDs, so it needs Phases 3 to 5 in place first (dimensions, biomes, portal blocks and items, gyoza, scrap wood). Its custom portal triggers need the Phase 4 events to exist.

First batch when started: audit supported FTB versions for NeoForge 1.21.1, then 20 to 30 tested quests covering survival, farming, industry, smoke materials, Artificial Door construction, dimension travel, forests and Hell. Later batches grow to about 150 to 200 quests across 15 chapters.
