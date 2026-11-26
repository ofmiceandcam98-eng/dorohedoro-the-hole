**SUPERSEDED by Pass 06**: the dense teeth and old moth animation were rejected. See `MOTH_LEECH_REFINEMENT_PASS06.md`.

# Creature model refinement — Pass 05 (Sewer Leech and Glow Moth)

## Requested visual changes
The user approved the four-creature concept sheet as the style target, then requested a **larger Sewer Leech with more teeth** and a **Glow Moth with real depth and a more moth-like shape**. Changes below modify the actual `docs/model_geometry/*.json` source of truth, generated Java `LayerDefinition` classes and original PNG texture atlases. No cosmetic-only substitution of generated concept art as a game asset.

## Sewer Leech
- Whole model enlarged in 3 axes, including its eight articulated body segments.
- 249 Java model cuboids (previous 99). Modeled length is approximately 38.4 model pixels versus 21.7 pixels in the preceding source. This is **model geometry length, not collision-box length**.
- Widened, layered suction mouth with concentric rings, a dark throat and multiple rows of deliberately projecting fang geometry; 112 tooth/fang-related **cuboid pieces** (not 112 separate anatomical teeth).
- Changed entity collision bounds from `0.56 x 0.30` blocks to `0.88 x 0.62` blocks; health increased from 8 to 12 to suit the larger mob. Movement, drops and interaction logic otherwise preserved.
- Important gameplay caveat: vanilla Minecraft has an axis-aligned *width × width × height* collision box. A long creature may visually extend past the square horizontal hitbox; must test narrow sewer corridors, attack reach and collision against players before approval.

## Glow Moth
- 265 model cuboids (previous 125); importantly removed obsolete flat wing sheets as sculpted forewing tiles replaced them, to limit rendering cost.
- New thick, stepped, arched forewings with tapered rounded outlines, a secondary lower hindwing on each side, drooping scale fringe, raised veins and pale patterns.
- Distinct thorax/abdomen, furry collar, facial palps, compound eyes and plume antennae. Existing wing groups retain animation pivots and bat-based flying behavior.
- The light-colored texture motifs are **not proof of true emissive light**: Minecraft does not automatically give dynamic illumination from the model. Test material appearance against approved art in game; distinct glow would require a separate renderer/emissive implementation.

## Preserve survival gameplay
- Both species retain their registrations and useful mob drops, loot tables, material recipes, and biomes/spawns. No alterations to normal Minecraft mining, vanilla survival, cave generation or sewer frequency.
- These are original designs for the user's Minecraft adaptation; named Dorohedoro characters remain separate entities.

## Checks completed here (offline; no live Minecraft confirmation)
- Regeneration of Java models and PNG atlases from geometry JSON using `python tools/regenerate-fidelity-models.py`.
- `python tools/qa-mob-fidelity.py` checks source-to-Java parity, UV overlap/bounds, 3D wing profiles, tooth-detail parts, larger leech dimensions and collision box specification.
- Additional static QA for other creatures and previously implemented materials, plus Java 21 syntax parsing.
- Static before/after image `docs/previews/moth_leech_pass05_comparison.png` uses model data and texture pixels. It is NOT an in-game screenshot.

## Required Minecraft acceptance tests
1. Launch the NeoForge 1.21.1 development client and load the model layers. Confirm no model bake, `ModelPart`, UV, or texture errors.
2. Spawn a Leech in a 1-block and 2-block wide sewer, then in deep water. Confirm head, rear, hitbox, reach, jump/float and pathfinding behave acceptably (the model can be ~2.4 blocks long with a narrower entity box).
3. Observe Leech idle and attack motion from front, side, above and underwater; verify the throat is dark, fangs have readable spacing and do not clip through the outer jaw.
4. Spawn a Glow Moth in both open caverns and narrow tunnels. Test flapping, resting/hanging, wing articulation, wing overlap, model origin, occlusion, and framerate with 10/25/50 moths present.
5. Confirm moth wings have visibly stepped vertical depth at different camera angles; distinguish forewings from hindwings; compare directly with `docs/reference/approved_creatures.png`.
6. Check moth shearing cooldown persists after save/restart and drops can actually craft Night Vision tonics, lamps and string.
7. Confirm Leech combat drops and regeneration/adhesive uses in Survival; test Fortune/Looting balance and difficultly.
8. Full `gradlew.bat runData`, `build`, `runClient`, `runServer` + existing GameTests; regenerate assets only where needed; verify dedicated server does not classload client-only model classes.

**Status:** model-code and asset-integrated, offline structural checks passed; **full NeoForge compile and live Minecraft appearance unverified** in this environment.
