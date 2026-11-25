# FTB integration: test results

All runs used the real client (`./gradlew runQaClientFtb` through `tools/ftb-client.sh`) on a fresh world, seed 8675309.
FTB's own jars came from the owner's CurseForge instances. Evidence is in `docs/evidence/pass78/`, with the full
console output of each run in `ftb-run-*.txt`.

## Run A: the owner's listed versions (2026-11-25)

FTB Library 2101.1.33, Teams 2101.1.10, Quests 2101.1.27, Chunks 2101.1.20, Ultimine 2101.1.15, XMod Compat 21.1.9,
Essentials 2101.1.10, Filter System 21.1.4, PlayerNBT Quests 1.0.1.1, Architectury 13.0.8.

| Check | Result |
|---|---|
| All 10 mods load with the mod | PASS |
| `/dorohedoro quests install` writes all 7 book files | PASS |
| Book opens in FTB's own screen (`ftbquests open_book`) | PASS |
| Ultimine, creative: 18-block zinc vein, stone over deepslate, mined in one go | PASS (0 of 18 left) |
| Ultimine, control: same vein without the key mines 1 block | PASS (17 of 18 left) |
| Ultimine, survival: 3-block mixed column started on deepslate | PASS (0 of 3 left, 3 raw zinc dropped) |
| A rival team's claim blocks the mod's own block placement | PASS |
| The same check outside any claim allows placement | PASS |
| A player's explosion (Destruction magic path) breaks nothing in the claim | PASS (9 of 9 survived) |
| A door's far end is refused inside a rival claim in the Sorcerers' World | PASS |
| A door's far end opens inside the player's own team's claim | PASS |
| Travel through the door to the Sorcerers' World, then back | PASS |
| Quests completed from ordinary play | PASS: 10 of 10 expected (see below) |
| Quests that should not complete (Hydra Forest, Safe Return) | PASS: not completed |
| `/ERROR]` lines in the log | 0 |

## Run B: the Official Dorohedoro Modpack's versions (2026-11-25)

FTB Library 2101.1.40, Teams 2101.1.12, Quests 2101.1.39, Chunks 2101.1.24, Architectury 13.0.12. The pack does not
include Ultimine, XMod Compat, Essentials, Filter System or PlayerNBT Quests, so the Ultimine checks were skipped.

Every other check in the table above: **PASS**. 0 errors, 0 exceptions. A position trace confirmed the player stays
where they land at the far end.

## Quest completion

Completion was read from FTB's own saved team progress (`<world>/ftbquests/<team>.snbt`) after the game closed, by
`tools/ftbquests/check-progress.mjs`.

- Origin choice, "Scrap Is Currency" and "Salt and Sulfur" completed from items and the origin advancement.
- The whole "Doors Between Worlds" chapter up to "The Sorcerers' World" completed. The door was deliberately opened and
  used before the earlier door quests were done, and those two quests completed by themselves once their prerequisites
  were finished. That shows credit for organic play under FTB's flexible progression.

## Problems found and fixed during testing

- **Door far ends often had nowhere to go:** "No safe unoccupied location found" in forests and over water. Fixed in
  `PortalService` (ring search, natural cover cleared, fallback platform).
- **Claims in another dimension were ignored:** claim mods judge by the dimension the player stands in. Fixed in
  `BlockProtection` by asking through a fake player with the same identity standing in the target dimension.
- **8 FTB Chunks errors about missing minimap icon settings** for the mod's mobs. Fixed with icon settings that are off
  by default.

## Known limits

- Kill tasks only count kills made while the book is loaded (FTB has no kill history). One quest uses one.
- Reward claiming and team sharing are FTB's own features and were not exercised beyond completion: the test is
  singleplayer.
- Survival Ultimine skips blocks harder than the first one mined. Starting on a stone ore leaves the deepslate part of
  a mixed vein, exactly as with vanilla ores.
