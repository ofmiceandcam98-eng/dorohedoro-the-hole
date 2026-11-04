# Dorohedoro: The Hole: complete handoff after Pass 71

Written 2026-11-04 by Claude, for whoever continues the project (ChatGPT or a new Claude session). Read this
first. It is self-contained; the older documents in `docs/` are history and reference.

---

## 1. Where things stand

- **Release:** `Dorohedoro The Hole-BETA0.2.jar` (mod version `0.2.0-beta`). It is built, tested in the real client
  and on a dedicated server, and ready to upload.
- **Source of truth:** GitHub `ofmiceandcam98-eng/dorohedoro-the-hole`, branch **`pass-70-neoforge-build`**.
  PR #1 into `gate-0-1-hole-world` is open and mergeable. The owner decides when to merge it.
- **CurseForge:** project **1777423** (`dorohedoro-the-hole`, owner `noremacxxi`) is approved and public, with one
  file, `dorohedoro-0.1.0.jar`. **BETA 0.2 is not uploaded yet.** The owner is uploading it by hand with the texts
  in `curseforge/`. The account has **no modpack project**; that question is still open with the owner.
- **Last verification:**
  - **Client run:** `./gradlew runQaClient`, with 0 errors, 0 exceptions and 51 screenshots.
  - **Dedicated-server QA:** seed 8675309. All stair, spawn and walkability checks pass. The only failure is
    "no sugar cane near this snowy start", which is the climate, not a bug.
  - **Unit-level:** `runData` and `build` are green, and GitHub CI is green.

## 2. Project facts

| | |
|---|---|
| Minecraft | Java 1.21.1 |
| Loader | NeoForge **21.1.265** (ModDevGradle, `disable_recompilation=true`) |
| Java | 21 |
| Gradle | 9.2.1, wrapper committed (`gradle/wrapper/gradle-wrapper.jar`) |
| Mod id / package | `dorohedoro` / `com.dorohedoro.hole` |
| Owner's machine | Windows 11, repo at `C:\dev\dorohedoro-the-hole` |
| Owner's game | Minecraft Launcher profile **"Dorohedoro"**, game dir `C:\Users\Cam\Dorohedoro-Minecraft`. Keep exactly one Dorohedoro jar in its `mods`; old jars go to `old-jars\` |
| World version | `world_detail_version` **53** for new worlds (`data/minecraft/worldgen/world_preset/normal.json`) |

### Release file naming (owner's rule)

Name release jars like the owner's other CurseForge projects: **`Dorohedoro The Hole-BETA0.2.jar`**. This comes from
`gradle.properties`:

```
mod_version=0.2.0-beta
release_title=Dorohedoro The Hole
release_channel=BETA
release_number=0.2
```

Bump `release_number` (and `mod_version`) for every upload. The channel is **BETA**, not alpha.

## 3. Build, run and test

```bash
./gradlew runData        # regenerate src/generated (models, loot, recipes, tags, worldgen JSON). Run after datagen edits.
./gradlew build          # jar -> build/libs/"Dorohedoro The Hole-BETA0.2.jar"
./gradlew runClient      # normal dev client
bash tools/qa-world.sh <seed> [radius]    # headless dedicated-server QA -> runs/qa/<seed>/forward/qa/report-forward.json
QA_STRUCTURES=true bash tools/qa-world.sh 12345 6   # also locates and inspects vanilla structures (slow: see §8)
./gradlew runQaClient    # unattended REAL client test, screenshots in runs/qaclient/screenshots/
```

- **Never run two Gradle processes in the same tree at once.** Concurrent compiles corrupted class files before.
- **`runQaClient`** (`client/qa/ClientQa.java`, active only with `-Ddorohedoro.clientqa=true`) runs in a
  1600×900 window and covers, in order:
  - The world: a fresh world on seed 8675309, the real origin screen (it picks "fate"), and a city tour.
  - Magic: all 8 smoke types rendered in one frame.
  - Interiors: two furnished rooms and a stairwell.
  - UI: the survival HUD and every character-record tab.
  - Art: an inventory of mod items and a floating wall of mod blocks and ores.
  - Mobs: every mob from 4 angles, plus the Glow Moth at night.

  Then it quits. Read the log for `CLIENTQA` lines and count `/ERROR]` lines.
- **CI:** `.github/workflows/neoforge-build.yml` builds on every push and PR to `pass-*` branches and uploads
  `build/libs/*.jar`. **If your sandbox cannot download Gradle, NeoForge or the Foojay toolchain plugin (ChatGPT's
  usually cannot), push to a branch and let CI compile**, or give the owner exact files to drop in and have them run
  `gradlew build`.

## 4. Architecture map

### World generation (`worldgen/`, mostly `worldgen/city/`)

- **Pure coordinate-based generation.** `CityPlan` decides streets, lots and districts from the seed. `Canvas` writes
  blocks for exactly one chunk at a time, clipped. Every decision must depend only on absolute coordinates and that
  chunk's own blocks:
  - `Canvas.get` returns void air outside the chunk.
  - A structure split across chunks must come out identical from both sides.
- **Version gating (critical):** existing saves keep generating with the version stored when they were created.
  Every change to what generates must be gated behind a **new** version. To add one:
  1. Bump the range in `CitySettings`, both the record check and the Codec `intRange`.
  2. Bump `world_detail_version` in `normal.json`.
  3. Add a `Canvas.hasXxx()` gate.
  4. Document it in the `CitySettings` javadoc.

  Never change old-version output, or players get seams in old worlds. Pass 71 added **v53**.
- **Key classes:**

  | Class | Role |
  |---|---|
  | `LotPainter` | Dispatches lot types |
  | `Tower` | Multi-storey building skeleton with the stair flights |
  | `RoomDressing71` | v53 room furnishing |
  | `CityTerrain40` | Relief (height offset) |
  | `Mix` | Weighted block palettes; v53 lays soil palettes in patches through `Canvas.patchHash` |
  | `Landmarks` | Starter landing and squares |
  | `DistrictIdentity18` | District materials |
  | `OceanRegions65` / `CoastalShelf66` | Oceans and coasts |
  | `DeepDarkBuffer63` / `VanillaStructureBuffer64` | Keep city writes off vanilla structures |
  | `Sewer*` | The sewer underworld |
- `WorldGuard.safeSpawn` finds a standable spawn point.
- **Ores:** `datagen/HoleMineralFeatures` replaces stone with the stone ore and deepslate with the deepslate twin.

### Magic and character

- `character/`: origins (Human or Sorcerer), the personality quiz, fate and ties. The screen is
  `client/character/CharacterCreationScreen`.
- `magic/`:
  - `SmokeSkill`: all techniques, with rank, cost and cooldown.
  - `SmokeEngine`: server authority.
  - `SmokeNetwork`: syncs a pipe-separated status line to the client.
  - `OtherMagicMechanics`: injuries and dismemberment; `injuryStatus()` builds the string shown in the Body tab.
  - `LimbReattachmentService`.
- `client/magic/`:
  - `SmokeClient`: keybinds Z/X/C, R to cycle, B for the ledger; it also hides the vanilla HUD layers.
  - `SmokeMeshClient`: magic visuals. Buffers must be fetched at draw time; see §7.
  - `SmokeLedgerScreen`: the B menu.

### Client UI (all original art)

- `client/ui/HoleHud`: the status plate (portrait and framed numeric bars for health, hunger, smoke and air), chips,
  the Z/X/C sockets and the XP gauge. It replaces the vanilla health, food, armor, air and XP layers in survival and
  adventure only.
- `client/ui/HoleUiTheme`: nine-sliced sprites for the window, panels, title plate, tabs, buttons and dividers, plus
  the gauge helpers. `HoleMenuButton` is the sprite button.
- `SmokeLedgerScreen` tabs. **Each tab must state its purpose**, which is the owner's rule:

  | Tab | Purpose |
  |---|---|
  | Status | Origin, smoke, rank and condition |
  | Techniques | Practice and equip |
  | Loadouts | 9 sets of 3 |
  | Body | Diagnosis and accurate treatments |
- `client/ui/ItemUseTooltips`: a gray "what it's for" line from the `item.dorohedoro.<id>.use` lang keys.
- **Art generators:** run from the repo root with Node; there are no dependencies.
  - `node tools/ui/gen-ui.mjs [previewDir]` writes every HUD and menu sprite into
    `assets/dorohedoro/textures/gui/sprites/{hud,menu}` and nine-slice `.mcmeta` files.
  - `node tools/ui/gen-ores.mjs` writes the ore mineral overlays to `textures/block/ore/`.
  - `node tools/ui/to-vanilla.mjs <src> <out> [--item]` converts 32px art to vanilla 16px by keeping each 2×2
    block's dominant color.
  - `tools/ui/png.mjs` and `tools/ui/pngread.mjs` are a minimal PNG writer and reader, including palette and 4-bit
    images.

### Content

- `registry/`: `ModBlocks`, `ModItems` (the creative tab lists every item automatically), `ModEntities`,
  `ModBlockEntities`, `ModWorldgen`, `ModSmokeParticles`.
- `entity/`: 8 mobs.

  | Mob | Base class | Behavior and sounds |
  |---|---|---|
  | SewerRoach | Monster | Pounces; silverfish sounds |
  | ScrapRat | PathfinderMob | Flees players |
  | SewerLeech | Monster | Water dweller; slows on bite |
  | GlowMoth | Bat | Places temporary light; shearable scales; full-bright glow layer |
  | SludgeCrawler | Monster | Slows on hit |
  | RustEaterBeetle | PathfinderMob | Tempted by scrap metal; brush for carapace |
  | CavernBurrower | Monster | Hoglin sounds |
  | SporeCrawler | Monster | Poisons; spore burst when hit |

  All hostiles alert others when hurt. Models and renderers are in `client/entity/`.
- `item/`: flasks, the reattachment kit, the poultice, and `HoleBrewing`. Its six brews start from Awkward:

  | Ingredient | Potion |
  |---|---|
  | Spore Sac | Poison |
  | Sludge Gland | Slowness |
  | Corrosive Gland | Harming |
  | Burrower Claw | Strength |
  | Fungal Membrane | Slow Falling |
  | Metallic Carapace | Turtle Master |
- `portal/`: door frames, cores, linking keys and the ritual tool. The two realms are the Sorcerer's World and Hell
  (`PHASE3`/`PHASE4` docs).
- `datagen/`: `ModModels` (ores use the `models/block/ore_overlay.json` parent: a vanilla stone or deepslate base plus
  a cutout overlay), `ModLoot`, `ModRecipes`, `ModDataGen` (tags), `HoleBiomeData`, `HoleMineralFeatures`.
  Fuel lives in `data/neoforge/data_maps/item/furnace_fuels.json`.

### QA

- `qa/HoleQa`: the headless server report. Checks spawn standability, street reachability, lot entry, **stair flights
  walkable** (each step needs 3 clear blocks above it), fluids, rock in the city, sewers, realms and chunk hashes.
  `qa/StructureQa` locates and inspects vanilla structures.
- `tools/qa-compare.mjs` compares chunk hashes between two reports. Use it to prove a change does not alter worlds.

## 5. The owner's rules and preferences (follow these)

1. **Quality over everything.** *Tensura Neo Otherworld* (a CurseForge pack the owner plays) is the polish bar for UI
   and menus. Use it as **reference only**: all UI must be **original and Dorohedoro-themed**, with no copied assets
   from any other mod.
2. **Never fake results.** Never claim something works until it has been tested in game. Show real screenshots.
   Don't publish broken files and don't create duplicate CurseForge projects or files.
3. **American spelling** in everything players read: practice, color, armor.
4. **Release naming** as in §2, on the BETA channel.
5. **No clutter.** Rooms and spawn areas must look deliberately arranged, not randomly filled. The owner rejected the
   first version of room furnishing ("this looks terrible") and the loose workstation row at spawn ("random shit").
6. **Every item must have a real use**: weapons, armor, potions, crafting, food or fuel. `ItemUseTooltips` lines must
   describe it accurately.
7. **Every menu tab needs a reason**, stated on the tab.
8. **Vanilla-quality art:** 16×16 items and blocks; ores look like vanilla stone with the mineral showing.
9. **Preserve vanilla progression** (strongholds, the End, Ancient Cities, trial chambers, the Nether) and **keep
   emeralds useful**.
10. **Deferred; do not implement unless asked:** Ash-Snow, Sludge-Ice, environmental poisoning, Cross-Eye camps, and
    the villager or faction overhaul. The roadmap (FTB Quests, the full magic system) is in `docs/ROADMAP.md`.
11. **Handoff rule:** when nearly out of usage, deliver an updated source ZIP and handoff. Refresh both after every
    milestone.
12. The owner tests by playing; take their screenshots seriously and fix what they point at first.

## 6. What changed in Passes 70 and 71

**Pass 70 takeover:**
- Fixed 3 compile errors.
- Fixed the "Not building!" smoke crash, which was the owner's Nov 1 crashes. A before-and-after client test proved
  it.
- Fixed barrel block entities going missing.
- Added `safeSpawn` and a QA re-center on spawn.
- Cached `OceanRegions65`; worlds are identical, 441 of 441 hashes.
- CI now runs on every push.

**Pass 71, from the owner's play-test** (commit `b54ee11` and later):
- **Stairs:** every flight had the floor slab over its first step, so players were blocked going up and down.
  `Tower.keepStairsWalkable` runs after all decoration. QA went from 14 of 14 blocked to 0 of 14.
- **Wall lamps:** the `CavernLampBlock` wall model was rotated 180° and the hitbox mirrored. Its placement fallback
  used look directions, not their opposites. All fixed.
- **Limb item icons** overflowed their slots; the GUI scale went from 1.5 to 0.95.
- **v53 worldgen:**
  - No regional escarpments: giant terraced stone pyramids came from up to 66 blocks of mountain-rim relief stepped in
    1-block terraces. Relief is now capped at 24.
  - Soil palettes are laid in patches.
  - `RoomDressing71`: one feature wall per room with a deliberate run of furniture, at most one or two table settings,
    and the rest of the floor open.
  - The spawn landing keeps only its supply barrel.
- **UI:** new HUD, menus, origin screen and XP gauge, all from generated sprites.
- **Art:** 50 items and 21 building blocks converted to 16px. The magic door and portal blocks stay at 32px on
  purpose, because conversion destroyed their thin glowing lines. Ores were rebuilt as overlays with 4 new deepslate
  variants and full loot, tags, lang and worldgen.
- **Items:**
  - Brewing for six creature parts.
  - New recipes: Antidote Poultice (it had none), Empty Smoke Flask (it had none, so the flask system was
    unreachable), tar torches, and hide to leather.
  - Cooking for severed limbs into cooked limbs (they were food with no source).
  - Tar is fuel, and items have use tooltips.
- **Mobs:** proper sounds, behaviors and the moth glow, as in the table above.
- **QA:** stair-flight check, snow-aware walkability, and much wider client coverage.

Evidence screenshots are in `docs/evidence/pass70/` and `docs/evidence/pass71/`. The running log is in
`docs/PASS70_TAKEOVER_LOG.md` and `docs/OVERNIGHT_DEBRIEF_2026-11-04.md`.

## 7. Hard-won technical lessons (avoid re-learning these)

**Rendering and UI**
- **1.21 render buffers:** `MultiBufferSource.BufferSource.getBuffer(newType)` ends the previous batch. Never hold a
  `VertexConsumer` across another `getBuffer` call; fetch it right before drawing. Doing otherwise caused the
  "Not building!" crash.
- **GUI draw order:** `blitSprite` draws immediately, while text is batched and drawn later. If you draw a panel
  sprite after a widget, it hides the widget's frame but not its label. Put all panel geometry in
  `renderBackground(...)`, which also replaces vanilla's background blur. Draw text after `super.render`.
- **Nine-slice:** the borders must leave a center. A 4px-tall sprite with a 2px border fails to load: the log shows
  "Nine-sliced texture has no vertical center slice" and the game renders magenta.
- **Glow layers:** use a full-bright layer (an `EyesLayer` with `RenderType.eyes`) with a mask texture of only the
  parts that should glow.

**Worldgen and QA**
- **Collision for walking up stairs:** mid-step, the player is half a block up with their head 2.3 blocks above the
  lower step, while still overlapping that step's column. So each step needs **three** clear blocks above it, not two.
- **QA passability:** "standable" must match the player. Thin snow layers and carpets are walkable; the old literal
  `isAir` check made snowy seeds report 0% reachability.
- **Block entities in worldgen:** `Canvas.set` registers a `DUMMY` block-entity tag for blocks that need one, the way
  vanilla does, and removes stale block entities when a container is replaced.
- **Ores** that span Y<0 need deepslate twins; `OreConfiguration` takes one target per rock type.
- **Vanilla textures:** reference them in models (`minecraft:block/stone`); never copy vanilla PNGs into the mod.
- **Server watchdog:** QA sets `max-tick-time=-1`, because structure searches can stall a tick.

**Tooling on Windows**
- Under Git Bash, Node resolves `/c/...` paths as `C:\c\...`. Pass Windows paths (`C:/...`) to Node scripts.
- `sed` misses lines in CRLF files.
- Complex patches are safer as script files than as quoted shell one-liners.
- Never run two Gradle processes in the same tree at once.

## 8. Known issues and next steps (priority order)

1. **Ocean monument search is extremely slow** (more than 10 minutes, nothing found). `/locate` and cartographer ocean
   explorer maps can freeze a server. The time goes into vanilla `OceanMonumentStructure.findGenerationPoint` biome
   checks and chunk loading against `HoleBiomeSource`. The desert and jungle pyramid searches take about 2 minutes.
   Fix it without changing worlds, using `qa-compare.mjs` hash checks.
2. **CurseForge:** confirm the owner's BETA 0.2 upload went through and the gallery screenshots are in. Answer the
   modpack question (create one, or it lives on another account?).
3. **Iterate with the owner's play-test** on room furnishing, the HUD and menus, the mobs and the ores. Likely
   requests:
   - A vanilla-style repaint of the mob textures. The Cavern Burrower is the plainest model.
   - Real weapons and armor from mob materials. The owner mentioned weapons and armor as acceptable uses.
4. **Old interior passes** (Pass 24–39 furnishers in `Housing25`, `InteriorDetails24`, `ResidentialDetails26`,
   `ResidentialAtmosphere28` and others) still place their own pieces before `RoomDressing71`. If rooms still feel
   random, gate those off for v54+ and let `RoomDressing71` own interiors.
5. **Sewer QA:** 3 checks at the origin's starter crossing fail on some seeds. That area may be deliberately uncarved
   because of the Deep Dark buffer; this is not proven.
6. **Door linking, destination placement and return travel** are still being refined (see the PHASE4 docs).
7. No full survival playthrough has been done: the Ender Dragon, the Nether and natural Warden summons.

## 9. Package contents (the handoff ZIP)

- `HANDOFF.md`: this file.
- `source/`: the complete repository at the handoff commit, including the Gradle wrapper, `src/generated`, tools,
  docs and evidence. It is the same as branch `pass-70-neoforge-build`.
- `release/Dorohedoro The Hole-BETA0.2.jar`: the tested build.
- `curseforge/`: the changelog, project description and summary as written for the owner, plus `gallery/` with 19
  real screenshots.
- `owner-feedback.md`: the owner's requests from this session, in order, so the reasons behind each change are clear.
