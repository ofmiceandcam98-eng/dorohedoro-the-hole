# Pass 19.9: Dorohedoro-inspired magic visuals

**Source status: NOT COMPILED; NOT TESTED IN MINECRAFT.**

## Art direction and verified lore

- Ordinary Sorcerer smoke is **black**, with a documented unusual exception for Kikurage. Do not give the eight player specialties eight differently colored smoke trails. Colored particles are sparse accents representing **material or injury** after the smoke takes effect, never a new smoke color.
- Sorcerers may emit smoke from mouths or fingertips. Dark, dense, twisting tendrils should visibly leave the player's casting hand, cross the space, then induce a material effect around the victim or beneficiary.
- En: real mushrooms grow or transform objects and living bodies. Our first pass uses rising fungal stalk and an off-white cap/gill silhouette, not generic green aura. Persistent 3D mushroom assets and actual transformation are a later milestone.
- Noi: black smoke repairs the wound; do not present Healing as a green circle and hearts. Pale sewing/closure lines are a *gameplay visualization*, not a literal manga claim. Human and Sorcerer allies can be healed by aim or nearby field effects.
- Shin: black separation seams and surgical precision, not a fantasy light saber. Actual dismemberment and limb rendering are not implemented.
- Ebisu: smoke-induced reptilian transformation. This pass shows moving scale-bands, claw arcs, and body smoke; real player mesh morphing is not yet implemented.
- Nikaido: represent the ominous mechanical time-magic apparatus rather than interchangeable lavender bolts. The dial visual is artistic shorthand; real time travel is not implemented.
- Fujita: black smoke blasts with pressure-wave impacts. No block griefing.
- Curse/Protection: dark marks and smoke cocoons. Do not incorrectly claim that Risu's exceptional trigger is represented by an ordinary repeating damage curse.

Primary references: https://dorohedoro.fandom.com/wiki/Magic ; https://dorohedoro.fandom.com/wiki/En ; https://dorohedoro.fandom.com/wiki/Noi ; https://dorohedoro.fandom.com/wiki/Shin ; https://dorohedoro.fandom.com/wiki/Types_of_Magic

## Minecraft magic mod *interaction* references (NOT copied)

- Iron's Spells 'n Spellbooks: layered telegraphs, reliable audiovisual feedback, and level-based progression: https://www.iron.wiki/
- Ars Nouveau: separate form/targeting, effect, and augment layers; this informed the emitter -> impact -> follow-through breakdown: https://arsnouveau.org/
- These mods are **references**, not dependencies. Do not copy their asset files, spell implementations, logos, or sounds.

## Implemented in this source

1. **Three-stage casts:** dark hand/mouth smoke launch -> moving black smoke trail -> time-sliced family effect.
2. **Eight family signatures:** sculpted mushroom gills, reptile bands/claws, dismemberment seams, healing stitches, curse tendrils, protection cocoon, time dial and spirals, destruction smoke shockwaves.
3. **Eight visual tiers within each family:** affected shapes, scale, intensity and follow-on elements are varied by the technique index (0 to 7). This is not the same as 64 entirely bespoke animated VFX assets.
4. **Multiplayer visibility:** server sends particle events to watching clients; extra ally-target visualizations for targeted and group support magic.
5. **Bounded duration:** 14 ticks, emitted on alternating ticks, maximum 3 active VFX per caster, and cleaned on logout/dimension change.
6. **Non-griefing:** VFX are purely cosmetic. Underlying effects from Pass 19.8 are unchanged except for adding visible group recipient traces.

## Still required for truly anime-quality presentation

- New custom animated sprite sheets for volumetric black smoke, hand emission and lingering dark haze; particle type/provider registration in NeoForge 1.21.1.
- Model-driven mushrooms growing in stages, true reptile player-model alterations, real controlled cutting effect, local healing skin shader or decal textures, and shaped defensive meshes.
- Correct animation synchronizing fingers/arms/mouth, audio designed for each magic family, and visibility/culling benchmarks across clients.
- Accurate 3D attachments and per-technique screen-space readability. These cannot be claimed completed by vanilla particle points alone.
- In-game testing of exact NeoForge signatures and server packet visibility, FPS, multiplayer latency, range and casting timing.

## Keybindings retained

- B: Smoke Ledger.
- Z/X/C: cast three equipped techniques.
- R: cycle nine loadout presets.
- Each family still has eight learnable techniques and exactly one innate magic type; all 64 `SmokeSkill` definitions are unchanged.
