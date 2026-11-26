# Dorohedoro Pass 20.5 — Technique Animation Source

**Starting point:** exact full project from `Dorohedoro_Pass20_4_Full_Source_Damage_Injuries_NOT_COMPILED.zip`.

**STATUS: NOT COMPILED. NOT TESTED IN MINECRAFT.** No Gradle command was run and no Minecraft or NeoForge dependencies were downloaded.

## Implemented this pass

1. **Four new original animated sprite particle registrations** with 28 hand-authored procedural PNG frames: soot plume (8), smoke filament (8), healing fiber (6), fungal spore (6). Files are under `assets/dorohedoro/textures/particle`; matching JSON definitions are under `assets/dorohedoro/particles`.
2. **New client particle providers**, client-side alpha fade, diffusion, rotation, age-dependent sprite frames, and translucency. Client providers are only registered on the physical client.
3. **Rebuilt multi-stage cast presentation:** hand smoke ejection, drifting curved streamer for travel/contact spells, target bloom, technique-specific follow-through and fade. Effects play over approximately 1.3 seconds, independently from server-controlled damage timing.
4. **All 64 techniques explicitly categorized** into seven animation silhouette patterns: Bolt, Touch, Area, Body, Shell, Construct, Rupture. The eight innate types retain distinctive material effects as well.
5. **Directional Dismemberment slashes and Time dials** now orient relative to the caster/target instead of being stuck along one world axis.
6. **Separate, shared sound cue** at spell impact. VFX broadcasts come from the logical server for nearby clients.
7. **Visual packet budget**: no more than 150 individual `sendParticles` calls per cast-frame to reduce multiplayer spikes. This is a source-level safeguard, not a measured performance result.
8. **Physical mushrooms continue to grow** exactly as in Pass 20.4. Their growth scheduler still runs at impact phase 2, so the new smoke animation does not turn real mushrooms into cosmetic particles.

## Unchanged

- All 64 techniques and their damage, healing, mastery and smoke costs.
- One innate magic type per normal Sorcerer, optional fate roll and ranked quiz.
- Three equipped skills per preset, nine presets, and cycle key.
- Human and Sorcerer healing, Dismemberment injury and cure mechanics.
- Destructible PvP, mastery-based real mushroom growth, temporary Time/Protection.
- Existing district worldgen and full portal code including `PortalService.Frame.stand()` landing correction.
- No new runtime dependency beyond the existing Minecraft/NeoForge environment.

## Important limitations / next animation passes

The new sprites are 2D camera-facing particles with mathematical geometry. They are **not** full 3D spell models, shader volumetrics, custom player animation rigs, reptilian morph meshes, detached limbs, or working literal time travel. Actual target motion and visual rendering must be tested in client multiplayer sessions. Physical world changes and damage are handled by the existing server gameplay code, not by any particle effect.

**After the user compiles on Windows:** verify four particle descriptions load; cast every specialty; confirm 64 assignment shapes; verify teammate healing visuals, mushroom placement, and multiplayer 2-player visibility; measure packet cost and FPS on low graphics; compare third-person camera alignment and cramped interiors. If any missing method error appears in `SmokeParticleClient`, check NeoForge 21.1.x client API signatures before publication.

## Technical references

- NeoForge 1.21.1 particle docs: https://docs.neoforged.net/docs/1.21.1/resources/client/particles/
- Ars Nouveau mod (reference for compositional spell feel, not copied source): https://www.curseforge.com/minecraft/mc-mods/ars-nouveau
- Spell Engine particle animation architecture: https://modrinth.com/mod/spell-engine

All art generated for this project is original. No addon code, textures, sounds, or Minecraft shader mods were copied.
