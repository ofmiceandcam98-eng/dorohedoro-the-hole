# Claude handoff — Dorohedoro Pass 21.0

**Give Claude this entire source ZIP, not a patch.** Baseline is the complete Pass 20.9 project. Do not drop prior city/world code or replace the corrected `PortalService.Frame.stand()` logic.

## Implemented this pass (source only)
- Severed-limb animated world model now tracks the *actual server-spawned ItemEntity ID* for a short handoff to its collectible named item. Collectible provenance, pickup behavior, and independent injury permanence remain as in Pass 20.9.
- Cosmetic `LimbEffect` networking now contains `dropEntityId`; payload registrar advanced to `smoke_mesh_2`. Client and server must run matching versions.
- Added original **Mushroom**, **Curse**, **Protection**, and **Time** 3D mesh definitions, render events, and four RGBA texture atlases; prior Reptile, Dismemberment, Healing and Destruction visuals remain intact.
- No changes to the 64 skill definitions, attack damage, 3×9 loadouts, mastery, cooldown, portal, player world, weapons, or prior healing/limb regeneration.
- All visual meshes are ephemeral; solid mushrooms and barriers remain server-owned blocks.

## Critical next actions on the owner's Windows PC
1. Keep the prior working JAR and world-save backup. Replace the source tree with this full ZIP (same project root structure).
2. Run the existing `./gradlew runData` then `./gradlew build` under **Java 21, Gradle 9.2.1**, with Minecraft/NeoForge dependencies available online. Do **not** interpret `STATIC_QA_PASS21_0.md` as a successful compilation.
3. Investigate *real* NeoForge API compiler errors, if any; do not invent green build results.
4. Start a fresh test world and a dedicated multiplayer server with matching mod versions. Test named limb drops, packet compatibility, animations, Reptile & Healing regrowth and all eight magic families.
5. Review differences from the user-provided manga/anime reference imagery. Improve models, low-latency interpolation, armor and slim skin handling before public release.

## Constraints still binding
- One biological innate type per normal Sorcerer. Keep all **64** learnable techniques (8 each), **3** active slots per loadout and **9** loadouts.
- Offensive spells must inflict health damage; Dismemberment can remove player limbs. Advanced Healing and Reptile can regenerate missing limbs. Support Time/Protection effects are never permanent; mastery-based Mushroom growth may be permanent.
- Do not implement the weapons in the screenshot or drift into factions/world generation yet. After magic passes user acceptance, resume lore-accurate architecture/district design, then environmental hazards, then factions/villagers.
- Preserve Minecraft survival, recipes, existing IDs, and portal functionality.

**THIS SOURCE IS NOT COMPILED AND NOT TESTED IN MINECRAFT.**
