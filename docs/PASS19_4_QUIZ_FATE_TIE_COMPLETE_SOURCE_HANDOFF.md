# Pass 19.4 — Sorcerer Origin, Quiz / Fate, Score Tie Choice

## Source lineage
Canonical baseline: user-uploaded `Dorohedoro_The_Hole_Pass18_Fixed_Source.zip`. Preserved fixed Gradle/portal files and selectively integrated later Pass 19 architecture/lantern and Pass 19.2 character code. Pass 19.4 changes the personality flow in source, including a browser-only preview. The original project source, worldgen, assets, and build scripts are all included.

## Player flow
1. On first join, choose Human or Sorcerer; Devil is an earned late-game status, not available at spawn.
2. After Sorcerer, choose either `Take Personality Test` or `Trust Fate`.
3. Quiz: server selects 8 distinct randomly ordered questions from 12, client displays four answers per question. Each answer scores +3 for one innate magic affinity and +1 for a secondary; scoring stays hidden to players.
4. Quiz unique highest scorer: server commits that type immediately; no accept/choose override.
5. Quiz tied highest scorers (2+): client displays only the tied high-score types, and player selects one. Server checks the choice is truly in the tied maxima before committing it.
6. Fate: directly server-roll among all 8, weighted by weights in MagicType: 4 Healing, 4 Protection, 4 Destruction, 3 Mushroom, 3 Reptile, 3 Dismemberment, 2 Curse, 1 Time (total 24). No option to reject/reroll the outcome.
7. Human skips magic; character profile and choice mode persist on the server. Existing characters don't redo creation.
8. Neither quiz nor Fate implements working smoke spells yet—this is the entry/progression foundation.

## Safety / regressions
- Server, not client, chooses question order, computes scores, tracks tie options, rolls Fate, and persists the result.
- Client packet carries only origin action, answer digits, or a proposed tied type. Forged untied magic choice is rejected.
- Future expansion: more questions and magic types requires save-format and balance review.
- No gender choices. No direct type selection. No regular reroll.
- Do not overwrite the current JAR or world saves before Minecraft runtime verification; Gradle and resource checks are not equivalent to gameplay tests.
- Legacy modid and worldgen registry IDs remain unchanged for world/save compatibility.
- Development order still prioritizes city visual/lore improvements before deferred environmental hazards and NPC/faction overhauls.
