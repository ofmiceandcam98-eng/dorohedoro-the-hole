# First-Join Origin Screen — Tensura-level Interaction, Dorohedoro Identity

**Design requirement, not implemented in this source checkpoint.** Never present mockup/description as a working UI until implemented and tested.

## When
A player first spawns into a new or existing world and lacks a `dorohedoro:origin_selected` server-side record. Must work in a local world and a dedicated multiplayer server, and must not grant another starter pack on reconnect.

## Proposed screen
- The screen interrupts gameplay with a dark industrial UI. Original artwork only.
- Header **WELCOME TO THE HOLE**; subtitle **Who are you in this city?**
- Three identity cards: **HUMAN** (selectable), **SORCERER** (selectable), and **DEVIL** (visible but locked as a lore-gated Sorcerer ascension path), each displaying a complete side-by-side information panel. A short flavor description, strengths, drawbacks, skills, opening equipment, playstyle and future growth.
- Human card: resourceful urban survivor; scavenging, medicine, weapons, street craft, evasion and special gear. Cannot use inherent smoke magic.
- Sorcerer card: innate smoke production and one personal magic specialty; smoke resource management, control, magical skill mastery, specialization limitations. Sorcerers can still use normal weapons and survival tools.
- Hover/focus preview displays planned progression tracks without giving guaranteed fictional powers to every player.
- Buttons: **CHOOSE HUMAN**, **CHOOSE SORCERER** or **VIEW DEVIL EXAM REQUIREMENTS** (locked; cannot start as a Devil); second confirmation displays expected consequences. A confirmation dialogue disallows accidental irreversible selection.
- Do not copy Tensura's selection-screen exact styling/layout/iconography or its code; match the *quality and flow*.

## Player progression after selection
- After confirmation, unlock a status overlay, keybindable ability/proficiency interface, starting quests and the local map/journal.
- Choice persists; rejoin continues saved origin. Death retains origin by default. Respec/rebirth is only a separate documented late-game/admin flow.
- Optional server rules may relax origin re-selection for private testing.
- If multiple players pick simultaneously, one player's choice cannot alter another's. No race-initialization exploit through death, disconnects or portal travel.

## Engineering contracts (for future implementation)
- NeoForge 1.21.1 Java 21: a typed server player attachment/capability-like persistent record for origin, version, level/rank, selected specialty, skills, progress and first grant flag; client render/data sync only after validated packet.
- Dedicated-server event on first authorized join; client screen shown after player data loaded; server rejects gameplay abilities until origin selected. Keep vanilla singleplayer menus and accessibility intact.
- Avoid keybind collisions, include UI scaling and keyboard use, descriptive text and a readable confirmation screen.

## Acceptance tests
1. New world => first login prompts once.
2. Existing world no mod data => prompts once; no saved world damage.
3. Select Human => no smoke; survival gear path open; data persists after restart and death.
4. Select Sorcerer => innate resource/specialty; persists after restart and death.
5. Disconnect during screen => prompt returns; no double grant.
6. Two players simultaneously => independent saved choices.
7. Client cannot forge race, XP or skills with networking packets.
8. Admin test reset => explicit confirmation and audited update; no duplicate items.
9. Dedicated server starts with no client screen classes loaded.

## Pass 19.1 lore reconciliation (2026-11-01)
See `DOROHEDORO_SPECIES_MAGIC_DEVILHOOD_PASS19_1.md` for authoritative project design: three intelligent classifications do not mean three equally selectable race starts; Humans and Sorcerers are ordinary selectable origins, Devils are generally former Sorcerers who completed exceptional training. Each Sorcerer has ONE persistent innate magic TYPE. Hybrids, curses, transformed humans and the singular Hole entity are special cases, not starter races. Current status: **DESIGN ONLY, no UI implemented**.
