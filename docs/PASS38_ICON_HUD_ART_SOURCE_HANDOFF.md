# Pass 38 — Technique icons, minimalist HUD and texture refinements

**State: SOURCE-ONLY / UNCOMPILED / NOT RELEASE-READY.** Derived from the Pass 37 cumulative local source. Nothing was installed, pushed to Codespaces/GitHub, or published to CurseForge.

## New work this pass

1. Added 64 original 24×24 transparent PNG icons at `src/main/resources/assets/dorohedoro/textures/gui/techniques/<skill-id>.png`. All eight innate magic families have eight individual, recognizably distinct designs. The images are real raster textures referenced by `SmokeSkill.id()`, not concept art or labels. These are first-pass art; several similarly shaped family icons deserve more visual refinement at tiny UI scales.
2. Added `SmokeSkillIcon.java` to render the icons using a 1.21.1-compatible `GuiGraphics.blit(ResourceLocation,...)` overload.
3. Replaced the Pass 33 boxed, permanently labeled HUD with **no-panels, icon-led** top-left Health/Hunger/Smoke meters, plus three left-side icon slots (Z/X/C). Names, costs and mastery remain inside the pop-up. Cooldown numbers only appear while cooldown is active. Standard Minecraft armor, air, XP and hotbar remain enabled. In Survival, the two original native heart and food layers are cancelled to avoid duplicate Health/Hunger displays; Creative and Spectator do not lose their native behavior. **Requires actual client testing, especially with other HUD mods**.
4. Updated the technique and loadout menu to display technique icons and use fewer skills per page on small GUI heights. Narrower widths get compact tab and footer controls. The underlying server-owned three-slot, nine-preset, smoke-cost and cooldown systems were NOT rewritten.
5. Refined *actual existing* 32×32 PNGs, retaining their original silhouettes/models/material context. Items: scrap metal, smoke residue, zinc and brass ingots, rock salt, raw sulfur, medical salve and leech mucus. Blocks: smoke-stained concrete, seepage brick, patchwork masonry, rusted industrial sheet. These are restrained pixel-level material and wear refinements, NOT the final full art overhaul.

## Preserved from earlier passes

- Pass 35: occupied and purpose-furnished rooms using existing Minecraft workstations; fewer open-air chests; darker streets allowing nighttime hostile spawns.
- Pass 36: larger river bends, wall/floor/ceiling Cavern Lamp states and physically smaller lamp models, façade details.
- Pass 37: version-gated vacant-lot hills, additional functional interior decoration and village-compatible furniture (not villager AI/jobs/economy).
- Pass 33 character-creation panels, full pop-up, nine loadouts.

## Offline checks performed

- `javac` parser: 152/152 Java files parsed without syntax errors.
- Compiled actual `MagicType` and `SmokeSkill` JDK-only (without the Minecraft engine); runtime test: 8 families × 8 skills = 64, 8 starter skills, stable IDs and positive smoke costs/cooldowns.
- `python tools/qa-pass38.py`: icon uniqueness, exact registered-skill coverage, PNG transparency and dimensions, menu/HUD integration source checks, 4 existing block and 8 existing item PNGs.
- Visual review sheets produced at `/mnt/data/Dorohedoro_Pass38_All64_Technique_Icons_Review.png`, `/mnt/data/Dorohedoro_Pass38_Item_PNG_Refinement_Before_After.png` and `/mnt/data/Dorohedoro_Pass38_Block_Texture_Refinement_Before_After.png` in the creation environment. They are NOT live Minecraft screenshots.

## Unresolved or unverified (release blockers)

1. **Technique crash**: no player `crash-reports/*.txt` or `logs/latest.log` has been provided. The root cause is unknown, and this pass does not claim a fix.
2. Full NeoForge 21.1.265 Minecraft 1.21.1 Gradle compilation, runData, headless QA and client play test not performed for Pass 33–38 combined.
3. Confirm GUI sprites resolve in the Minecraft resource loader, correct Survival hearts/food replacement and armor/air coexistence, multiple GUI scales, crafting/inventory compatibility and hotbar visibility.
4. Inspect all original and refined PNGs at 16px, 24px and 32px in actual Minecraft; make further manual improvements if visual style is below vanilla-grade.
5. Larger hills and valleys remain limited to suitable empty lots; rivers have widened bends but true organic watershed topography and citywide contour-aware foundations are still incomplete.
6. Verify all interior doors, stairs, workstation access, beds, lighting and mob spawns in new in-game worlds. Existing world profiles are not regenerated automatically.
7. Keep character-villager AI/jobs/trading, Ash-Snow, Sludge-Ice and other deferred faction systems blocked until the city/buildings meet the owner's visual standards.

**Installation:** Do not deploy this source archive as a `.jar`. Keep existing local and CurseForge builds until compilation and in-game tests pass. Use a disposable new-world test when eventually validating world-generation changes.
