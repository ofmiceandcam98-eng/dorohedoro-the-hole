# CLAUDE HANDOFF — Pass 19 FULL SOURCE — architecture and world depth

**Read this and the two docs** under `docs/` before making modifications. The ZIP delivered to the user contains the **entire project source**, not an isolated diff. This checkpoint includes the prior Pass 18 portal landing correction. The old failure screenshot is obsolete; do not assume a currently broken portal from it.

## User direction
- Make the entire Minecraft Overworld resemble the dense decaying city shown in Dorohedoro manga/anime, as closely as the modding medium and original/permissioned assets allow.
- The world should be deep and playable, not a visual shell; distinctive districts, purposeful interiors, survival, utilities, creatures, crafting and exploration. Prioritize world/detail architecture FIRST. Later hazards and Cross-Eye camps only when accepted; expanded villagers/factions still later.
- Always provide a **complete source ZIP** for every Claude handoff, plus the exact version notes; no patch-only handoffs.
- Preserve existing worlds. Do not silently rename registered IDs (display name choice is still undecided).

## What's already changed in this full-source checkpoint
- Versioned new world architecture `world_detail_version` 8 in normal preset; saved 0–7 generators follow their historical path.
- Six original textures+blocks+crafting+loot+models+tags for construction.
- Coherent per-lot construction palettes; district-specific architecture; realistic roads/drains instead of single-block random patchwork.
- Sparse external vents, risers, rooftop details, balconies, and one muted awning for shops.
- Cavern Lamp is now a modelled hanging or standing LanternBlock with unique item/block models and original PNG textures.
- Existing Pass 18 portal landing correction preserved; no portal rework added.

## Validation and caveats
- `tools/qa-lore-architecture-pass19.py` performs offline source/resource validations. **This is not a Gradle/Java compile or Minecraft gameplay test.** Gradle distribution fetch was blocked by `UnknownHostException` for `services.gradle.org`.
- Need build/runData, client and dedicated server validation before public CurseForge upload, with a rights/license audit. Screenshots for all seven new worlds and a side-by-side before/after are still required.
- **Not fixed yet:** Every item texture, all 8 creature models, deep functional room grammar, all recipe balance issues. Do not claim full completion.
- Do not overwrite newer Claude-only work blindly; review source changes and merge if its version is ahead.

## Permanent quality benchmark — Tensura: Reincarnated
The user explicitly requires the systemic depth of Tensura: Reincarnated (https://tensura.wiki.gg/). Read `docs/TENSURA_REINCARNATED_DEPTH_STANDARD.md` and `docs/TENSURA_DEPTH_FEATURE_MATRIX.csv`, `docs/FIRST_JOIN_ORIGIN_SCREEN_SPEC.md`, and `docs/PASS19_VALIDATION_AND_LIMITATIONS.md`. Required future features include a first-join Human/Sorcerer origin screen, persistent power/leveling and mastery, major milestones, skills, UI, equipment growth, balanced survival and later faction reputation. **These are future design requirements, NOT implemented in Pass19.** Preserve the architectural-first acceptance order and do not use Tensura source or art without authorization.

## Next development pass
Follow `docs/DEEP_WORLD_BLUEPRINT.md`, especially Phase A visual acceptance. Make purposeful building interiors and traversal robust before hazards, factions or villagers. Complete source always delivered on next handoff.

## Pass 19.1 source addendum — race/magic canon
This full source checkpoint now contains `docs/DOROHEDORO_SPECIES_MAGIC_DEVILHOOD_PASS19_1.md`, an expanded reference from the public Dorohedoro Wiki, and updated first-join/Tensura design specs. **No gameplay Java code was changed as part of 19.1; these character systems are NOT implemented.** Three intelligent classifications do not imply a normal Devil start. Human/Sorcerer first-join; Devil later examination/ascension. Maintain one unique innate Sorcerer magic type. The existing architecture-first order remains. Do not overwrite newer independently developed files without comparison.
