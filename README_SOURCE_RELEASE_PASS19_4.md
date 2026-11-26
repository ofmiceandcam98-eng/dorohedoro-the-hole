# Dorohedoro Minecraft source — Pass 19.4 (UNCOMPILED)

**NOT COMPILED. NO GRADLE BUILD. NO MINECRAFT / NEOFORGE RUNTIME VERIFICATION.**

Canonical baseline: `Dorohedoro_The_Hole_Pass18_Fixed_Source.zip` as provided by the user. This complete rootless source archive preserves the base Gradle configuration, wrapper, and `PortalCoreBlock.java` byte-for-byte, then integrates the Pass 19 lore architecture, original building materials, modeled lantern, and Pass 19.4 first-join Human/Sorcerer creation flow. The source retains the existing `dorohedoro` registry ID to protect save compatibility. Devilhood remains locked for later progression and is not a playable starting origin.

## Character flow
* First join: Human or Sorcerer.
* Sorcerer: eight random distinct questions selected by server from twelve; answer weights +3 primary / +1 secondary; server chooses highest score.
* Equal highest scores: restricted choice among tied leaders, server validated.
* Optional weighted Fate roll: skip the quiz, permanent server-selected type; no free choice or reroll.
* Persists server-side character profile via NeoForge data attachments. Smoke spells, mastery, progression and devil exam are **NOT IMPLEMENTED** by this pass.

## Portal landing fix — confirmed present
`src/main/java/com/dorohedoro/hole/portal/PortalService.java`: `PortalService.Frame.stand(int sign)` now returns positions on the same Y level as the portal core: `core.offset(0, 0, sign)` or `core.offset(sign, 0, 0)`, instead of the previous erroneous `+1` Y offset. This is a **source-level confirmation**, not an in-game test of portal travel.

## Source packaging
* Same no-top-level-folder ZIP layout as the user's Pass 18 Fixed Source.
* Contains `src/main`, `src/generated`, `tools`, `docs`, Gradle wrappers/configuration.
* Excludes `.gradle/`, `build/`, `out/`, `__pycache__/`, compiled classes and Python bytecode.
* See `CHANGED_FILES_PASS19_4.md` for every source difference from the Fixed Source baseline.
* See `docs/PERSONALITY_TEST_ANSWER_KEY_PASS19_4.md` for all 12 questions and 48 answer affinity links.
* See `docs/SORCERER_PERSONALITY_TEST_PASS19_4_INTERACTIVE_PREVIEW.html` for a browser-only mockup (not an in-game screenshot).

**Do not install this ZIP in CurseForge `mods`. A compatible NeoForge mod JAR has not been built.**
