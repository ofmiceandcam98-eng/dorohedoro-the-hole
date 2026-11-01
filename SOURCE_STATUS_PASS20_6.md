# Source Status — Pass 20.6: Anatomical and Impact Meshes

**NOT COMPILED. NOT RUN IN MINECRAFT. NO GRADLE EXECUTION.**

Baseline: `Dorohedoro_Pass20_5_Full_Source_Animated_Techniques_NOT_COMPILED.zip`.

## Implemented in source

- Server-broadcast, client-only 3D spell rendering. Only a successful server cast emits a cosmetic packet. Existing damage/injuries/terrain changes are unchanged.
- **Reptile:** original 3D player attachment (snout, jaw, ridged scales, claws, segmented oscillating tail). Renders for nearby players without replacing their skin. Mastery and spell type determine an approximate cosmetic form duration; server remains authoritative for real size/movement.
- **Dismemberment:** oriented crossed 3D slicing seams and flying shards, rendered at an opponent's current position when tracked.
- **Healing:** crisscrossing 3D tissue filaments that converge on the recipient, including ally/group target cues. Actual injuries are repaired in the existing server logic.
- **Destruction:** expanding angular 3D blast ribs and a soot core, scaled by technique, mastery and Focused casting. Structural destruction remains configured and enforced server-side.
- Four original 128×128 RGBA art textures; no assets from Tensura, Dorohedoro, or third-party mods are copied.
- Fix: accidentally duplicate smoke particle and sprite provider registration from Pass 20.5 reduced to one each.

## Known limitations / testing requirements

- New geometry is a first-generation original blocky/voxel-style 3D model, **not** a professional rigged anime character or realistic shader.
- Reptile overlays are cosmetic; they do not replace vanilla player mesh and are **not an anatomical limb replacement**. Real hitboxes are managed by the existing server scale code.
- Mesh event positions and intensity are approximate and need live tests for 1.21.1 NeoForge rendering API compatibility, multiplayer tracking, translucency, skin layers, and resource reload.
- VFX animations are locally timed, and on late packet delivery may start a few ticks after server damage. Cosmetic cues are never a source of authority.
- Other four magic families retain Pass 20.5 animated particles/physical blocks, rather than receiving new meshes in this pass.
- Existing home damage/PvP settings and time-limited protection effects are unchanged.
- No Java/NeoForge compilation was run (per project owner instruction); source-level QA is *not* compilation.
