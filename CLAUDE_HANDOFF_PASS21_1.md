# Claude handoff — Pass 21.1 (complete source)

This ZIP is the **entire** Minecraft 1.21.1 NeoForge source project, derived from the Pass 21.0 full source. It is **NOT COMPILED** and must not be described as gameplay-tested.

## Scope

- Complete owner-and-injury-token-verified physical limb reattachment through Healing `Mend Ally` or a consumable surgical kit.
- Client/server entity-interaction hooks on raw severed limbs and surgical kits.
- Four cooked limb food items with 12 standard furnace/smoker/campfire recipes and original recolored voxel item textures.
- Full change manifest and QA plan in `CHANGED_FILES_PASS21_1.md` / `SOURCE_STATUS_PASS21_1.md`.

## Integration rules

- Do not remove the prior 64 techniques, three-slot nine-preset loadout system, direct combat damage, multiplayer Healing, Reptile recovery, collection/animations, portal fix or world files.
- Do not touch the weapons screenshots until the user authorizes them.
- The previous Pass 21.0 archive is the comparison baseline; changed and added files are precisely listed.
- Source-only Java syntax and recipe checks do not establish NeoForge API correctness. Resolve errors using the owner's locally cached/downloaded NeoForge dependencies and actual Minecraft tests.
- Use the owner's established procedure: Java 21, `./gradlew runData`, then `./gradlew build` on their local system. Don't attempt an offline workaround or claim a successful build here.

## Important gameplay details

- Surgery requires the **same original living player**, the **same limb** and **the active random injury token**, not a modified display name.
- Patient must crouch; medic must be within 4 blocks; first successful medical use consumes the original limb.
- Physical reattachment is independent from rare high-mastery magical creation of a missing limb.
- Vanilla cooking produces generic named cooked food with **no owner provenance**. Cooked limbs do not reattach.
