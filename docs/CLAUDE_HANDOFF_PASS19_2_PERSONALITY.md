# Claude Handoff — Pass 19.2 Sorcerer Aptitude

**This archive is a FULL source tree from the available Pass 19.1 checkpoint**, augmented with the origin-selection UI and personality-test Java code. It is **NOT** claimed to be based on the user's unlocated `Dorohedoro_The_Hole_Pass18_Fixed_Source.zip`, nor is it claimed to compile with NeoForge. Do not replace the owner's already working version without merging and verification.

## User decisions (authoritative)

- Tensura: Reincarnated sets the standard for depth and first-join character selection.
- Human or Sorcerer as origins. Sorcerer goes to a 12-question Pokémon Mystery Dungeon-like personality exam.
- Devil is shown as an eventual ascension, not a starting option.
- Eight initial innate smoke types: Mushroom, Reptile, Dismemberment, Healing, Curse, Protection, Time, Destruction; more later.
- Every answer has a scored primary (+3) and secondary (+1) magic type.
- Result: Accept Recommendation / Choose Freely / Trust Fate (rarity- and answer-weighted).
- Server must enforce and persist chosen identity; players cannot give themselves multiple innate types.
- Future systems: Smoke discovery quest, combat leveling, mastery, spells, human progression and Devils; those are NOT implemented here.
- The world's architecture quality is still the top priority in parallel; do not advance NPC/villager overhaul or environment hazards prematurely.
- Preserve `dorohedoro` registry IDs for existing save compatibility even if the CurseForge project changes name.

## What this handoff contains

1. The **entire** Pass 19.2 source tree; not a patch-only archive.
2. Character networking, profile attachment, first-join event and screen Java code.
3. Complete question/answer scoring map at `docs/PERSONALITY_TEST_QUESTION_MAP_PASS19_2.json` and `docs/SORCERER_PERSONALITY_TEST_PASS19_2.md`.
4. Standalone preview at `docs/PERSONALITY_TEST_INTERACTIVE_PREVIEW.html`.

## Verification

- Java 21 `javac` and `java` verified the pure quiz algorithm for all eight specialties; every type is reachable via valid quiz answers.
- Structural checks verify 12 questions / 4 answers / +3 +1 scoring.
- Gradle 9.2.1 is available locally, but `gradle --offline help` fails before source compilation because `org.gradle.toolchains.foojay-resolver-convention:1.0.0` is unavailable offline.
- No full NeoForge compilation or in-game multiplayer/GUI validation has been performed.
- The user identifies `Dorohedoro_The_Hole_Pass18_Fixed_Source.zip` as the **only known-compiling baseline**; obtain that exact ZIP and merge the 19.2 changes into it before declaring an installable build. Don't substitute earlier broken Pass 18 source ZIPs.

## Required merge / review before release

- Acquire the user's exact Fixed Source ZIP; compare against the included full source; resolve changed classes/resources with care.
- Confirm `CharacterNetwork` payload registration, side isolation, ByteBuf stream codecs, event lifecycle and attachment serialization against actual NeoForge 21.1.265.
- Compile, run a headless server, and verify first-login character flow, second-login persistence, death, dimension transitions, malformed payloads and multiple players.
- Review screen with different GUI scales, including long answer wrapping and controller/keyboard use.
- Run all preexisting worldgen tests to ensure the city has not regressed.
- Maintain compatibility with prior player saves and original `dorohedoro` registry ID.

**This is a source implementation handoff, not a validated CurseForge release.**
