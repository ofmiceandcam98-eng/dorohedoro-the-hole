# Pass 28 — Mixed lighting and lived-in housing (source checkpoint)

User choice: **Cavern Lantern, ordinary vanilla lanterns and torches are all permitted**. Do not globally replace vanilla lighting. The Cavern Lantern remains a distinctive signature, not a compulsory replacement everywhere.

## Implemented

- New `world_detail_version: 16` preset with backwards-compatible version decoding; older saved profiles retain their original generator rules.
- A seed-/coordinate-based light mixture for new interiors: roughly 60% Cavern Lantern and 40% vanilla lantern choices at suitable ceiling positions. The existing block ID and lamp models are untouched.
- Wall torch placement for v16 uses the real exterior support wall on the correct side, at the solid y+1 strip below windows; earlier v15 behavior remains unchanged.
- Additional compact-house daily-life furnishing archetypes (crafting, storage, kitchens, desks and reading corners), using air-only placement against room edges and avoiding the central circulation aisle.
- Small varied window lintels along the facade, added only where the exterior air cell is free.
- All procedural decisions derive from the persisted world seed, building hash, storey and absolute coordinates. No new fixed maps or pasted structures.

## Explicit limits

- Source-only integration; no Minecraft/NeoForge compile or game-client test performed.
- Distribution ratios are hash choices, not measured in-game light coverage. Lamp attachment, light propagation, small house visual quality and collision need in-game validation.
- World lighting, existing river/sewer generation, and deferred creature/villager/faction/ash-snow development are unchanged.
