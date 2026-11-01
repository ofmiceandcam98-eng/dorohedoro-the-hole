# Pass 06 — smaller tooth count, articulated four-wing flight

## User review changes
- **Sewer Leech** retains its larger profile and 0.88 × 0.62 entity hitbox, but now has **only six visible teeth**, each composed of a base and tip: two upper, two lower and two side-hooks. The multiple dense fang rows, rakers and gum papillae are deleted. Gullet and soft layered sucker tissues remain.
- **Cavern Glow Moth** now has separately rotating left/right **forewings and hindwings**; wing geometry is rebased to independent **shoulder hinge ModelPart pivots** rather than rotating around the center of the creature. The broad forewings and shorter rounded hindwings overlap but move separately.
- **Flight stroke**: mirrored up/down wing beats about the longitudinal Z axis, the hindwings lagging by 0.34 radians and using smaller amplitude. Frequency `1.25 radians/tick` ≈ 4 Hz at 20 ticks/second. When resting, wings fold upward (front ≈ 1.04 rad, rear ≈ 0.88 rad) and stop flapping. Small fore/aft sweep prevents rigid hinge appearance.
- Model cubes and PNG texture atlases are regenerated from `docs/model_geometry` through `tools/regenerate-fidelity-models.py`; the compiler now understands `part_pivots` for real client-side hinge animation.
- Other mobs, material drops, crafting, and world generation are unchanged.

## Validation and required real-world tests
- Offline: Java grammar check, asset consistency, six-tooth count, model/UV parity, group symmetry, 4 moving wing parts, hinge positions and resting animation; standalone source-based phase snapshots.
- Still required: Gradle `build`, `runClient`, dedicated server, observe the moth facing multiple directions at slow-motion/normal speed while flying and resting, inspect wing intersection/clipping and light, 20+ moths performance, test larger leech collision/pathfinding and actual bite animation, verify multiplayer renders and pre-existing worlds.
- Offline pose renders are NOT game screenshots. NeoForge client compilation/gameplay cannot be claimed verified here until run on a normal modding installation.
