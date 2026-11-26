# Phase 3 integration — Sorcerers' World and Hell (2026-10-31)

## Goal and state

The project is still a Minecraft **survival sandbox**, not a story campaign. All players start in the Hole (the existing Overworld). Two additional persistent dimensions have now been **integrated at source/data level**. This is a *build/runtime verification checkpoint*, NOT a completed, proven phase.

**Baseline:** the user's earlier `Dorohedoro_The_Hole_Takeover_Smog_2026-10-31.zip`, which itself is based on the user's `dorohedoro-the-hole-source(2).zip`. Preserve the earlier terrain, interior, streetlight, and smog changes.

**Work completed in this checkout:**

- Moved the existing Phase 3 draft biome sources and datagen from `docs/handoff/phase3-draft/` into active Java sources.
- Registered `dorohedoro:sorcerer_lands` and `dorohedoro:hell_realms` biome source codecs.
- Registered eight Sorcerers' World biomes and four Hell biomes (12 new biome keys).
- Registered six Hell ore configured/placed features and mineable custom `hell_rock` / `ash_soil` blocks, including loot and block tags.
- Added generated PNGs, item/block models, lang entries, and dimension definitions to the source archive.
- Added two noise-settings JSONs, five noise parameter JSONs, and a portable generator. The packaged noise files are **offline-bootstrap estimates** and have not been validated by Minecraft. On the owner's PC **regenerate from the pinned Minecraft 1.21.1 client jar** using `tools/make-realm-settings.ps1`, then run `runData`.
- Added provisional generated biome/feature resource JSONs for source continuity. **NeoForge Java `runData` is authoritative; overwrite these files with its output before using the mod.**
- Extended the existing QA dedicated-server runner to verify dimension registration, noise chunk generators, five loaded surface columns and biome namespace in both realms. This runtime QA is **not run** in this environment.
- Added `tools/qa-phase3-static.py` (81 structural checks) and `tools/bootstrap-realm-generated.py` for source-only offline package construction.

## Actual tests in this environment

| Check | Result |
|---|---|
| Phase 3 offline structural checks | 81 / 81 passed |
| Java 21 compiler syntax parser (all active classes) | 39 Java files, 0 parse errors |
| JSON syntax (main and generated resources) | 93 JSON files parsed |
| Original procedural block textures | Generated locally as PNGs |
| `./gradlew --offline --no-daemon help` | BLOCKED: Gradle 9.2.1 is not cached, and `services.gradle.org` cannot be resolved here |
| NeoForge compile, `runData`, headless server, dimension loading, rendering, gameplay | NOT VERIFIED |

**Static checks do not verify Minecraft registry schema, Biome feature ordering, NeoForge method signatures, gameplay, performance or client visuals.**

## First required actions on the owner's Windows computer

1. Unpack this source into **a separate directory**. Keep the last working ZIP/JAR and world saves as backups. Do not overwrite Claude's newer edits without comparing them.
2. In Git Bash / PowerShell, make sure Java 21, Gradle 9.2.1 (via wrapper) and NeoForge 21.1.265 dependencies are available.
3. `powershell.exe -ExecutionPolicy Bypass -File tools/make-realm-settings.ps1` using the actual location of `minecraft_1.21.1_client.jar` if the default jar path differs. The PowerShell generator uses the vanilla, exact 1.21.1 final density. The portable Python `bootstrap-realm-settings.py` is only a fallback for environments without the vanilla jar.
4. `./gradlew --no-daemon runData` — look for `BUILD SUCCESSFUL`. This **must replace provisional generated biome/ore files**.
5. `./gradlew --no-daemon build`. Correct any actual NeoForge API errors and datagen cycle failures; source-file parse checking is not enough.
6. Create a **new throwaway world** with the Dorohedoro normal preset. Do not load a valuable old save before backup.
7. In a test world with cheats enabled, travel to the Sorcerers' World and Hell using vanilla commands as a **temporary developer QA method**, since Phase 4 portals are not implemented. For safety, use Creative/Spectator and inspect a safe landing height rather than blindly teleporting into blocks.
8. Confirm Sorcerers' World biomes, forest growth, no precipitation, caves, resource generation, and dry weather. Confirm Hell's stone/ash terrain, cavern regions, horn formations, no natural forests, and mining. Both dimensions must remain buildable.
9. Run `bash tools/qa-world.sh 12345 12 forward`, inspect the report, repeat in reverse order, compare hashes; repeat for several seeds and confirm new `realm_*` checks.
10. Test saving/reloading, terrain determinism, and the Hole's smog only rendering in the Hole. If a vanilla world or Sorcerers' World loses its normal sky, investigate `HoleAtmosphere` before shipping.

## Known unfinished work / risks

- Worldgen code has not been compiled or run here, so all new registry and noise features remain **provisional**.
- Forests in Hydra currently use vanilla dark-forest vegetation **as a placeholder**. Custom trees, Haru's cabin and rural structures remain later art/worldgen work.
- City biomes in the Sorcerers' World are reserved terrain regions, but **actual cities and En's Mansion are not generated** yet. They belong to a later structures milestone.
- Hell's mob spawning is intentionally empty. Devils and their behaviors are future content, not recolored zombies.
- Sorcerer magic, masks, FTB Quests and portal blocks are **not implemented** by this work. FTB remains deferred per the current directive.
- Packaged offline Sorcerers' World noise uses `minecraft:overworld/sloped_cheese` for natural terrain because the vanilla client jar wasn't available here; the owner's pinned-jar PowerShell generator should replace it. Terrain & biome alignment are unverified until the generator runs.
- Unverified earlier smog/interiors/terrain code from the previous patch remains present. The client hook/Gradle build requires verification before classifying Phase 2 as complete.
- The root `HANDOFF.md` in the original upload was an auto-consolidated *earlier* snapshot, not the latest implementation. See **`docs/HANDOFF.md`** and this file for the newest changes.

## Entrance to Phase 4 — Portal implementation

After the Phase 3 build, server QA and client render tests succeed, begin the **persistent server-side portal registry and safe travel backend**. First implement a permanent Artificial Door and a paired exit. Then the Devil Gate, En Family Door, and smoke-dependent temporary door. All required first-gate materials must be accessible before the destination dimension, humans cannot be locked out, player inventories must remain safe, and portals must survive restarts without duplicating records.

Do not call Phase 3 verified, or tell the owner all three dimensions are playable, until real tests have been performed.
