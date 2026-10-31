# Dorohedoro: The Hole — Claude Code handoff bundle

This bundle is a detailed specification, **not a working Minecraft mod**. Open a Java modding project in Claude Code and provide the full `CLAUDE_MASTER_PROMPT.md` as the task. The CSV files are concrete implementation queues and audit seeds, not proof that assets or spells already exist.

## Bundle files

- `CLAUDE_MASTER_PROMPT.md`: end-to-end mandate, worldgen, magic, vanilla survival, weapons, crafting, creatures, AI, networking, QA, official docs.
- `MAGIC_SPELL_IMPLEMENTATION_MATRIX.csv`: 58 proposed working game techniques across 20 lore-oriented innate-power families. The move names are **game design**, not canonical spell dialogue; lore status varies.
- `VANILLA_MOB_COMPATIBILITY_SEED.csv`: 80 tentative vanilla entity registry IDs and policies. Claude MUST check them against the exact 1.21.1 registry and fill any missing mobs.
- `CRAFTING_AND_ITEMS_SEED.csv`: 25 initial craftable/tool/food/content entries and their functional goals; recipes are proposed gameplay designs, not canonical recipes.
- `ASSET_REFERENCE_AND_MODEL_MANIFEST.csv`: 56 required structure/entity/item/visual references to source or build.

## Primary goal

New world => actual `minecraft:overworld` is an endlessly generated **Hole**, player spawns in it, no visible normal vanilla landscape, and survival has a viable resource/tech tree. Magic, the Sorcerers' World, NPCs and factions then build on that base.

## Recommended implementation baseline

Minecraft 1.21.1 / NeoForge for 1.21.1 / Java 21 **subject to audit and existing repo constraints**. Reference version-specific docs in master prompt. All systems are expected to be independently validated on a running client and dedicated server. No AI prompt can guarantee bug-free code.

## Intellectual property

Dorohedoro is someone else's copyrighted franchise. Keep a private reference folder for screenshots/art; produce original models/textures/audio, record provenance and check distribution permissions before any public release. Do not include art ripped from anime in a redistributed mod.
