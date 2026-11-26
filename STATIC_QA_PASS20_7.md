# Pass 20.7 — Static QA

**NOT COMPILED / NOT TESTED IN MINECRAFT.** Java syntax was parsed separately; no project compilation or Gradle build attempted.

Baseline: Pass 20.6 full source ZIP.
Archive: `Dorohedoro_Pass20_7_Full_Source_Limb_Loss_Regeneration_NOT_COMPILED.zip`
Files: 863
Modified baseline files: 7
Added files including status, manifest, and this QA report: 4
Removed files: 0

## Checks — 31 / 31 PASS

| Check | Result |
|---|---|
| Exactly 64 unique technique enum definitions | PASS |
| Exactly eight technique definitions for each of eight innate types | PASS |
| Three active slots and nine presets preserved | PASS |
| Nine loadout presets preserved | PASS |
| All 4 limbs stored separately in NBT | PASS |
| Limb loss only for eligible survival/adventure players | PASS |
| Server random limb roll and free-limb selection | PASS |
| Limb sync packet clientbound only | PASS |
| Late-tracking state sync | PASS |
| Player-death clone persists limbs | PASS |
| Client renders missing arms and legs | PASS |
| First-person hands hidden | PASS |
| Server accepts no client limb-loss packets | PASS |
| Reptile overlay respects missing arm | PASS |
| Injury effects trigger only if health damage lands | PASS |
| No old double Dismemberment call site | PASS |
| Healing can restore limbs conditionally | PASS |
| One-or-more allies can have limbs restored | PASS |
| No permanent Protection barriers introduced | PASS |
| Portal landing horizontal offset preserved | PASS |
| No generated output/cache directories in project | PASS |
| CLEAN_CUT probability remains 0..1 and monotonic | PASS |
| DISMEMBER_STRIKE probability remains 0..1 and monotonic | PASS |
| SEVERING_WAVE probability remains 0..1 and monotonic | PASS |
| ANATOMY_REND probability remains 0..1 and monotonic | PASS |
| All JSON resources parse | PASS |
| Nothing removed from previous project | PASS |
| Full archive CRC passes | PASS |
| No cache/build folders packaged | PASS |
| Preserved project source structure | PASS |
| All baseline files preserved | PASS |

## Important caveats
- Java parser: 124 source files, 0 syntax errors (no symbol/type resolution or dependencies).
- NeoForge mappings and vanilla render behavior have not been compiled or executed.
- Missing body parts in third person rely on rendering hooks; armor/third-party render models need in-game validation.
- Severed-limb mechanics currently apply to player targets, not mobs.
