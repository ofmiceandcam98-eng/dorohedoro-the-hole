# Pass 19.8 — 3 ability slots and visible smoke signatures

**NOT COMPILED. NOT TESTED IN MINECRAFT.** Full project based on verified Pass 19.7 complete source, retaining Fixed Source ancestry, portal correction and all world systems. No Gradle/build/cache folders.

## Confirmed design decision

- A Sorcerer keeps **one innate magic family**. The **64 technique definitions** remain unchanged (8 per family).
- Players can learn all eight techniques belonging to their innate family, but equip only **three at once**.
- Nine persistent presets, each with **three** distinct active slots; 27 equipped entries total.
- **Z / X / C:** cast equipped slots 1–3. **B:** Smoke Ledger. **R:** next preset. **Shift+R:** previous preset. **Alt+Z/X/C:** switch that slot between standard and focused. Keybinds are rebindable via Minecraft Controls.
- Server remains authoritative for preset changes, slots, smoke costs, ability ownership, mastery, and cooldowns.
- On load, old 9×8 equipped slot lists migrate to new 9×3, **retaining the first three slots of every old preset**. Other equipped assignments are dropped; **learned techniques, study, mastery and cooldowns are preserved**, so nothing is unlearned. Make a save backup before upgrading.

## First-pass visible spell design

These are Minecraft vanilla-particle **functional visual prototypes** broadcast from server to nearby players, not finished 3D transformation meshes, custom sprites, or fully animated spell entities.

| Innate family | Palette | Visible casting identity |
|---|---|---|
| Mushroom | olive and amber | spore cap rings, drifting clouds |
| Reptile | green and lime | three green claw trails, glittering scale debris |
| Dismemberment | silver and crimson | crossing razor-straight silver/red cuts |
| Healing | mint and ivory | ascending mint rings and heart glints |
| Curse | violet and black | pentagonal violet glyph / dark core |
| Protection | cyan and pearl | two barrier orbits and an arching dome |
| Time | lilac and pale blue | 12-mark clock ring and rising spiral |
| Destruction | red-orange and yellow | orange shock rings with ember spray |

Every completed cast produces an initial smoke plume, appropriate line/tether when targeted, family signature, and one of eight per-technique accent motifs (thread, expansion, binding cut, orbit, charged rise, spokes, layered wave, mastery crown). Higher rank adds brighter/wider bursts and focused mode increases intensity. A distinctive sound cue is played server-side. Geometry and emissions are intentionally bounded; terrain is never modified by VFX.

## Full technique roster retained

- **Mushroom**: Spore Puff, Spore Cloud, Fungal Bind, Mycelial Sense, Spore Purge, Fungal Bloom, Mycelium Guard, Spore Dominion
- **Reptile**: Scale Skin, Predator Sprint, Reptile Rage, Shed Skin, Wall Crawl, Tail Lash, Chameleon Hunt, Apex Form
- **Dismemberment**: Severing Touch, Clean Cut, Dismember Strike, Precision Nerve, Limb Lock, Disarming Smoke, Severing Wave, Anatomy Rend
- **Healing**: Mend Flesh, Purify Blood, Rapid Regeneration, Mend Ally, Cleanse Ally, Restorative Field, Vital Shell, Lifeweave
- **Curse**: Curse Mark, Hex Burden, Wither Seal, Mark of Dread, Persistent Hex, Misfortune, Curse Pulse, Ruinous Curse
- **Protection**: Smoke Ward, Guardian Veil, Sanctuary, Guard Ally, Hardening Shell, Smoke Bulwark, Purge Barrier, Last Stand
- **Time**: Fleeting Step, Dilated Moment, Temporal Window, Temporal Awareness, Hastened Hands, Slowed Moment, Time Shelter, Chrono Burst
- **Destruction**: Smoke Shot, Detonation, Shockwave, Breach Bolt, Rupture Armor, Concussive Wave, Shrapnel Smoke, Annihilation Stream

## Next steps after compilation in a normal connected Minecraft development environment

1. Back up the world. Build with Minecraft/NeoForge dependencies available.
2. Test each skill family and each of the 64 skills in singleplayer and on a dedicated server; check visible particle/sound syncing, cooldowns and status changes.
3. Verify old eight-slot player saves migrate correctly and that each preset remains separate.
4. Observe VFX at night, underground, and in cramped interiors; tune particle limits and accessibility settings (reduced motion / particles).
5. Replace prototype particles with original animated meshes/sprites on a later art pass.
