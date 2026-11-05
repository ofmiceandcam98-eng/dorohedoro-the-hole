# Pass 08 — readable five-second movement previews and foot contact

## Changes to real mod source

All eight mobs continue using the Java 1.21.1 `HierarchicalModel` animation classes generated from `docs/model_geometry`. The approved voxel body geometry, textures, loot tables, spawning, light and recipes were not replaced by these motion refinements.

- **Cockroach / Rust-Eater Beetle / Spore Crawler:** the walking tripod pairs now lift during their swing phase. Horizontal leg rotations are preserved. The part's `y` is reset to its stored default pivot plus a small displacement each frame, preventing offset accumulation.
- **Scrap Rat / Cavern Burrower:** alternating paws now have separate upward lifts during the swing of a footfall. This is a visual approximation; collision, ground contact and pathfinding remain controlled by Minecraft.
- **Sewer Leech:** the eight body segments now animate vertical and longitudinal position (small contraction / lift waves) in addition to rotating. No extra teeth or geometry changes were made.
- **Glow Moth / Sludge Crawler:** approved wing-hinge and tentacle motions remain as previously implemented.

The animation generator in `tools/regenerate-fidelity-models.py` consumes all `.y` and `.z` part expressions from the model geometry JSON; do not manually edit generated Java without updating those JSON source files.

## Preview deliverables

`docs/previews/animation_pass08/` contains eight **five-second, 40-frame, 8 FPS** continuous movement previews plus two group reels. These are generated from actual JSON cuboids, source texture pixels and ported Java rotation / lift curves using `tools/render-movement-pass08.py` (requires Pillow). They sample a continuous five-second timeline; they are not simply a 1.3-second clip copied repeatedly.

**Important limitation:** These are offline model-part previews. They do not depict entities traversing Minecraft blocks, server movement or collision, and cannot prove limb contact, animation quality, dynamic light radius or correct rendering in the actual game. The preview renderer uses a simplified three-face projection; it is not Minecraft's renderer.

## Validation

Run `python tools/qa-anim-motion08.py`, `python tools/qa-anim-light-pass07.py`, and `python tools/qa-highquality-mobs.py`; then compile and run the mod in the intended Minecraft 1.21.1 NeoForge environment. A Java syntax parser is not a substitute for compiling against NeoForge.

In `runClient`, summon every mob and watch **five or more uninterrupted seconds** of natural moving behavior at 1x and reduced game speed where possible. Verify leg lifting and ground contact, tail following, no clipping or sudden pops, leech body coherence, and stable wing animation. Test idle→walk→idle transitions, faster movement, water versus land and foot contact on stairs/slabs. Record in-game videos/screenshots and compare directly with the approved art. Confirm Glow Moth light source actually illuminates nearby blocks, respects walls, and expires correctly on death, unload and chunk reload. Test movement in multiplayer and dedicated server. The original 30% sewer-to-cave and all vanilla mining checks remain required.
