# Pass 19.5 — Smoke Magic Core, Smoke Ledger and Tensura addon research

**Status: SOURCE ONLY — NOT COMPILED — NOT TESTED IN MINECRAFT.**

## Source baseline and scope

This complete project is derived from Pass 19.4, whose lineage was the user-provided `Dorohedoro_The_Hole_Pass18_Fixed_Source.zip`. No Minecraft or Gradle build was attempted. Keep the Fixed Source ZIP and the last known working JAR as backups.

This is a **magic-focused pass**. Keep the previous architectural generation and portal landing correction. Do **not** implement environmental poisoning, Ash-Snow/Sludge-Ice, Cross-Eye camps, NPC/factions, or villager systems yet. After the core magic system has been compiled, tested and tuned in-game, resume improving the lore-inspired city and buildings.

## How players access it

- Character creation remains unchanged: Human or Sorcerer; Sorcerers get 8 randomized personality questions with hidden scores or optional weighted Fate. Only tied top results can be selected manually. Devilhood remains an earned path.
- `B` opens **Smoke Ledger** with Status, Techniques, Loadout. Defaults are rebindable in Minecraft controls.
- `Z X C V G H J K` cast the eight ability slots (eight slots in each of nine presets).
- `Alt + skill key` toggles a global Focused mode (1.5× smoke cost, 1.4× base damage/heal scaling, longer relevant effect duration). This is **not** yet Tensura's per-skill multimode system.
- The first basic skill for the chosen innate magic is already equipped. Higher abilities require smoke rank 3 and 6, plus deliberate practice to earn 100 study points. Every practice action can fail (10% chance) and is limited to once per 40 ticks per player. Practice never changes innate magic.
- Successful casts use Smoke, validate server-side cooldowns and skill ownership, increase skill mastery and grant Smoke rank XP. Smoke naturally regenerates on the server. No independent client resource authority.
- Mouse-based equipping and nine loadout presets reside in the Smoke Ledger; slot assignments, Smoke, Rank, XP, learning and mastery are stored in the player's persistent NeoForge attachment (copy on death).
- Most offensive spells target line-of-sight mobs. Area abilities do **not** damage players or break terrain. Defensive `Sanctuary` may buff nearby players.
- Time magic is *localized debuff/buff gameplay*; this first pass does not change the dimension's tick rate, rewind world blocks, or grant unrestricted time control.

## 24 techniques included in the source

| Innate type | Basic (rank 1) | Intermediate (rank 3) | Advanced (rank 6) |
| --- | --- | --- | --- |
| Mushroom | Spore Puff | Spore Cloud | Fungal Bind |
| Reptile | Scale Skin | Predator Sprint | Reptile Rage |
| Dismemberment | Severing Touch | Clean Cut | Dismember Strike |
| Healing | Mend Flesh | Purify Blood | Rapid Regeneration |
| Curse | Curse Mark | Hex Burden | Wither Seal |
| Protection | Smoke Ward | Guardian Veil | Sanctuary |
| Time | Fleeting Step | Dilated Moment | Temporal Window |
| Destruction | Smoke Shot | Detonation | Shockwave |

These are original gameplay interpretations in development, not a claim that the underlying franchise or Tensura canon lists exactly these skills.

## Tensura addon reference audit — useful patterns, not imported source/assets

CurseForge project pages were checked on Nov 1, 2026:

- Tensura: HUD — https://www.curseforge.com/minecraft/mc-mods/tensura-hud — eight bound skills, nine presets, modes, live gauges and draggable HUD. **Pass 19.5 implements** eight slots, nine presets, basic Smoke bar and tick-synced cooldown state. **Not yet** draggable styles, hold/release activations or per-skill mode rings.
- Tensura: GUI — https://www.curseforge.com/minecraft/mc-mods/tensura-gui — Chronicle, skill search, race constellations, UI themes. **Pass 19.5 implements** original Smoke Ledger with status, techniques and loadouts. **Not yet** skill search, race constellation, cinematic identity/history, layout editor.
- Tensura RPG System — https://www.curseforge.com/minecraft/mc-mods/tensura-rpg-system — attributes, skill unlocks and configurable progression. **Pass 19.5 implements** smoke rank, learning progress and mastery counters. **Not yet** talent allocation, full player attributes or rebalance configuration.
- Tensura: Quests — https://www.curseforge.com/minecraft/mc-mods/tensura-quests — branching quest journal and editor. **Later** use for smoke diagnosis, training and The Hole investigations; do not divert from functional magic and world quality.
- Great Sage — https://www.curseforge.com/minecraft/mc-mods/great-sage — spoken skill announcements, analysis and cinematics. **Later** add an original in-world smoke diagnostic / examination system without copying voice recordings or designs.
- Elite Tensura — https://www.curseforge.com/minecraft/mc-mods/elite-tensura-mod — world events, titles, crafting and factions. **Later** adapt meaningful reputations, Devil Exam milestones, quality crafting and bosses.
- Tensura: Status Menus — https://www.curseforge.com/minecraft/mc-mods/tensura-status-menus — floating synchronized workstations. **Later** add optional workshop UI only when the underlying workstations function well.

## Risks / next QA on owner machine

1. Compile with Java 21, NeoForge 21.1.x, and the owner's working source/dependency cache; no compile was possible/attempted here.
2. Resolve any actual API and resource errors. Java / event APIs have been written for the indicated loader version but **not compile verified**.
3. Test first join: Human gets no Smoke; Sorcerer gets exactly their innate type and one basic skill; quiz/Fate/ties unchanged; reconnect/death retains progress.
4. Test all 24 effects (target selection, smoke costs, cooldown, mode toggling, practice unlocks, mastery, rank XP).
5. Test nine preset persistence, all eight key mappings, client HUD and open/close GUI on dedicated multiplayer server.
6. Test mob targeting, exploit prevention, dimension travel and FPS/packet rate. World gen and portal are *not* changed by this phase.
7. Back up existing saves before replacing the working JAR. No source-only QA proves a gameplay feature is finished.

## Future priorities after first working magic build

- Add polished ability icons, distinct particle treatments, animations and clearer visual tells; configure accessibility.
- Tune mastery requirements and combat balance, long-term attribute trees, sorcerer smoke organ capacity, resistance and exposure risk.
- Add smoke diagnosis missions and specialized skill branches per innate magic type rather than cross-learning other innate magic.
- Resume **The Hole world architecture** once smoke casting is stable. District differentiation and lore-accurate roads/buildings remain the next major feature group, prior to deferred hazards and factions.

**Legal/credit note:** All new interface and skill code is original source; Tensura and its addons are design references only. Do not copy their JAR contents, UI art, sounds, models or copyrighted material into this mod.
