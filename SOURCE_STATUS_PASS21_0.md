# Pass 21.0 — Spell Mesh Polish and Collectible Limb Transition

**Status: NOT COMPILED, NOT TESTED IN MINECRAFT.** No Gradle commands or dependency downloads were attempted. Java parsing and file-level checks are not evidence of successful compilation, game startup, or correct rendering.

## Baseline and boundaries
- Based on the complete Pass 20.9 source archive, retaining its top-level project structure.
- Preserves eight innate magic types / **64 techniques** (eight each), three equipped slots per loadout and nine presets.
- Does not modify combat damage, spell costs, permanent biological injuries, mushroom block placement/collision, Protection expiry, or Time field duration.
- Keeps Healing of other players and mastery-gated missing-limb regeneration.
- Preserves corrected `PortalService.Frame.stand()` landing offset.
- No weapons, firearm systems, faction features, or architecture changes in this pass; the weapons from the user's screenshots are explicitly deferred until techniques are done.

## Severed-limb pickup transition
1. When a successful severing strike removes a new arm or leg, the server spawns exactly one **real** named, nonstackable ItemEntity, with victim UUID, username and limb saved in item data.
2. The server sends the ItemEntity's network entity ID along with the sever/regrowth visual cue (new `dropEntityId` field). The client can now follow the actual drop's position for the short, skin-mapped detached-limb mesh.
3. The cosmetic detached mesh displays at its full size briefly, then shrinks over **18 ticks** so the physical dropped item remains visible and collectible. The pickup delay is 30 ticks. If its entity spawn packet has not arrived, the client uses a bounded approximate ballistic trajectory for the transition.
4. The named item remains available after the animation. Picking it up has no healing effect. Regrowth events send `dropEntityId=-1` and do **not** create another item.
5. Changed the cosmetic network protocol registrar version to **`smoke_mesh_2`** because `LimbEffect`'s binary schema changed. Update the client and server together; do not mix 20.9 and 21.0 instances.

## Newly distinct 3D cast visuals
- **Mushroom** — spreading mycelial veins/spore nodes at ground level, around the existing actual mushroom blocks. The rendering mesh itself does not cause collisions.
- **Curse** — eight constricting dark tendrils and an angular mark around the recipient, scaling with technique. The actual injury/curse effects remain server-controlled.
- **Protection** — ribbed and banded soot shell that assembles, then disperses. Real temporary solid smoke barriers remain separate server-side blocks and still always expire.
- **Time** — concentric mismatched clockwork rings with raised hands and an offset rotation, without inventing permanent effects or claiming literal time travel.
- Existing **Dismemberment**, **Healing**, **Destruction** meshes and **Reptile** body layer retained.
- Four original 128×128 RGBA textured atlases created locally. They are original assets, not imports from Tensura or other mods.
- Effects are still early voxel model animations. They need game-time render QA and art refinement to approach the anime's fidelity.

## Required in-game QA after a normal online build on the user's PC
1. Confirm singleplayer startup and a dedicated server with two matching Pass 21.0 clients; verify no packet version mismatch or render errors.
2. Dismember one player: inspect actual limb loss, arm/leg model visibility, spawned name, item pickup after delay, dropped limb after moving/chunk loading, and chest/restart persistence.
3. Check sever animation transition under client/server latency, and when the victim runs, jumps, or dies. Confirm no duplicate *item entities* are spawned (two simultaneous cosmetic visuals are possible early in the transition and may need art refinement).
4. Verify slim-arm and standard skins; test with armor and first-person view. These are known fidelity/compatibility risks.
5. Regrow limbs through advanced Healing or Reptile magic and verify missing-limb data, model, recovery, and **no** new item drop.
6. Cast all eight innate families, validate visual targets (caster/ally/enemy/terrain), mesh sorting, performance with several players, and camera culling.
7. Confirm existing nonvisual mechanics: real mushrooms, temporary Protection blocks, temporary Time spells, player-healing, PvP damage, and world destruction.
8. Verify `PortalService.Frame.stand()` portal transfer height in a new throwaway world; back up any existing save.

## Not accomplished or verified
- No NeoForge compilation, datagen, rendered in-game screenshots, game launch or multiplayer tests.
- No custom full-body ragdoll physics, bone-level skin deformation, per-frame client/server interpolation, armor masking beyond the existing source, or guaranteed perfectly seamless appearance on high latency.
- No changes to the deferred weapons from the reference screenshots, Devilhood, Black Powder, tumor grafts, or world-building priorities.
