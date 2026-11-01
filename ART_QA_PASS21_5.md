# Dorohedoro Pass 21.5 — Actual source-art verification (November 1, 2026)

STATUS: ART/SOURCE ASSET PATCH ONLY. NOT COMPILED OR IN-GAME TESTED.

- Baseline: Pass 21.4 full source ZIP, no replacement project.
- Updated existing PNGs: **40 item textures, 9 block textures**. All other 41 item/block PNGs preserved byte-for-byte.
- All item paths, names, resolutions, model references, recipes and original resource links preserved.
- Eight 64x64 severed/cooked limb atlas PNGs preserved. All existing limb 3D JSON model files preserved.
- Original Java files, existing JSON, Java APIs, gameplay mechanics, tech definitions, worldgen, dimension structure, docs are unchanged.
- Structural asset QA: 59 item PNGs / 31 block PNGs, RGBA, non-empty; item sizes 32x32 except eight 64x64 limb atlases; all 31 block textures 32x32.
- Existing Pass 21.4 QA script: tested separately. This checks JSON parsing, model textures and portal landing adjustment; it **does not** run Minecraft.
- Two real-source before/after preview sheets generated at 16 and 32 pixels; a 3x3 world block tiling preview generated; full 59 sprite inventory chart generated at 16 pixels.
- Original reference textures are not redistributed; new artwork is original hand-directed pixel art based on the mod's existing assets.
- Not verified: actual in-game rendering/UV, item handling, shader/lighting performance, multiplayer client sync, build/compile or NeoForge startup.

### New art direction adjustments

- High contrast and deliberate silhouette for dark tar/smoke drops.
- Zinc now reads as metallic lumpy ore; rock salt as cuboid crystalline shards; sulfur as craggy yellow ore.
- Carapace, chitin, hide, scrap, fungus, and medicinal supplies have different silhouettes and material cues.
- Distinct gland organs, tied silk material, fuzzy fur, key/tool silhouettes, viscous mucus drop, food ingredients.
- Existing smoke flask content glyphs preserved, with small color-coded stoppers; do not confuse colored contents with separate magical schools.
- City blocks received small purposeful corrosion, mortar stains and damp streaks instead of random visual noise.

### Deferred gates

Real vanilla 16x slot background client comparison; in-game screenshots with actual shader/default lighting; real block material placement; GUI/in-hand/ground placement of 3D limb item models. Gameplay implementation intentionally NOT changed.
