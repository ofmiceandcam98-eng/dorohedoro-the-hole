# Pass 19.9 — Lore-oriented Smoke VFX, complete-source handoff

**Status: NOT COMPILED / NOT RUN IN MINECRAFT / NOT MULTIPLAYER-TESTED.**

Based on the entire Pass 19.8 source tree, itself descended from the user's Pass 18 Fixed Source baseline. The source layout is unchanged at ZIP root. It contains no build, `.gradle`, compiled classes or game saves.

## Changed source files (relative to Pass 19.8)

### Modified (2)
- `src/main/java/com/dorohedoro/hole/magic/SmokeEngine.java`
- `src/main/java/com/dorohedoro/hole/magic/SmokeVisuals.java`

### Added (2)
- `docs/PASS19_9_LORE_MAGIC_VISUAL_DESIGN.md`
- `docs/SMOKE_MAGIC_PASS19_9_VISUAL_PREVIEW.html`

### Removed (0)
- None

## Existing functionality retained

- 64 `SmokeSkill` definitions, eight per innate magic type, unchanged byte-for-byte.
- Nine presets, three equipped techniques per preset (Z / X / C), R cycle, B Smoke Ledger.
- One innate magic, quiz/fate assignment, learning and mastery, cooldowns, flask borrowed magic.
- PortalService.Frame.stand() correct stand height: `core.offset(0, 0, sign)` / `core.offset(sign, 0, 0)`.
- World generation and lore architecture unchanged by this visual pass.

## Pass 19.9 implementation

- Replaced eight brightly tinted smoke families with the lore-grounded **black smoke** emitted from the player's hand.
- Added scheduled, server-broadcast effect phases (14 ticks, every other tick): launch, moving smoke, material change and fade.
- Added distinctive family-specific visual geometry (mushrooms, reptile bands, cutting seams, healing threads, curse tendrils, protection cocoon, clock dial, pressure-wave impacts).
- Added visual traces for team members affected by group healing and protection skills; existing targetable ally healing remains.
- Vanilla Minecraft particles and sounds only. No copyrighted assets, no extra dependencies.
- Added an offline visual concept preview and an original lore/implementation reference file.

## Known limitations

These are still particle geometry prototypes; 64 entirely unique bespoke animated meshes, skin mutations, reversible limb dismemberment, flesh shaders, smoke volumetrics and true time travel are **NOT** implemented. Compilation and gameplay verification are required externally with Java 21 / NeoForge 1.21.1. These files must not be published as a proven working JAR.
