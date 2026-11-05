# Fauna Batch 03 — approved visual standard applied to native Minecraft voxel models

**Status:** source-integrated, offline-validated; no complete NeoForge runtime build or in-game quality approval yet.

## Research and visual requirements

- Basis: user-approved 2×2 creature concept sheet (Cockroach, Rat, Leech, Glow Moth). These *source model previews* are generated from current Java cuboids and are **not** Minecraft screenshots or exact reproductions of the approved sheet.
- Styling reference: [Alex's Mobs on Modrinth](https://modrinth.com/mod/alexs-mobs), especially naturalistic/creature-specific silhouettes, useful drops and behavior (reference for quality, **do not copy assets**). The Graveyard is another useful benchmark for coherent dark-themed entity models.
- Engine: Minecraft Java 1.21.1 + NeoForge 21.1.x. Each species has `HierarchicalModel`, `LayerDefinition`, client renderer, one dedicated 512×256 painted UV atlas and a species-specific set of animated model parts. Source geometry is in `docs/model_geometry/*.json` (includes cuboid coordinates, UVs, group names, and animation expressions).
- Regenerated art uses unique UV allocations; quality is still dependent on in-game screenshot review of colors, scaling and animations. Model-layer group pivot placement needs further validation under Minecraft's rendering coordinate system.

## Existing models upgraded

| Species | Cuboids | Major improvement | Drop/use maintained |
|---|---:|---|---|
| Sewer Cockroach | 54 | layered armored shell; mandibles; 6 individually articulated legs; twitching feelers | chitin armor, antenna detection equipment |
| Scrap Rat | 35 | raised back, layered fur, snout and whiskers, four animated paws, four animated tail segments | food, fur insulation |
| Sewer Leech | 33 | 8 undulating body rings, sucker with teeth, underside secretions | adhesive, salves |
| Glow Moth | 46 | broad layered wings, pale wing spots, abdomen ridges, feathery antenna, wing animation | moth silk, lighting and Night Vision |

## Newly added native-entity models and gameplay

| Species | Cuboids | Behavior | Primary (guaranteed) | Rare secondary | Survival use |
|---|---:|---|---|---|---|
| Sludge Crawler | 21 | hostile; slows enemies briefly; deep sewers | Sludge Gland (1–2) | Coagulated Tar (40%) | gland+clay -> slime; tar+bitumen+clay -> sewer sealant |
| Rust-Eater Beetle | 23 | neutral; underground scavenger; brush nonlethally every 4m | Corrosive Gland (1–2) | Metallic Carapace (40%) | corroded scrap processing into iron nuggets; carapace boosts chitin yields |
| Cavern Burrower | 32 | hostile deep cavern monster | Burrower Claw (1–2) | Dense Hide (40%) | craft iron pickaxe with less iron; hide -> leather |
| Spore Crawler | 28 | hostile; applies Poison briefly | Spore Sac (1–2) | Fungal Membrane (40%) | fertilizer; paper; antidote poultice that cures Poison |

The four new species are **original Minecraft interpretations**, not canon named Dorohedoro characters. All four have their own entity types, renderer registrations, biome spawner entries, spawn placement checks, loot tables, translations, item icons/models, and crafting recipes.

## Important survival / engine considerations

1. New mobs only live in the existing Hole undercity biome, rather than replacing vanilla cave mobs or overwriting normal cave mining and ore generation.
2. Real gameplay must confirm underground spawn distributions and brightness restrictions: biome counts and placement rules are not a substitute for in-game tests.
3. Food/potion/crafting outputs are not gated behind killing named nonhostile characters. The Rust-Eater Beetle has a renewable nonlethal brush drop.
4. Some material conversions deliberately use vanilla outputs (iron nuggets, slime balls, leather, paper, bone meal, pickaxe). These are useful alternatives, not replacements for vanilla recipes.
5. Recipes and custom item models are in `src/main/resources`, separate from `src/generated/resources` so a later `runData` does not erase hand-authored files. The generator code and pre-generated undercity biome both include new spawn entries.
6. The custom antidote uses `Item.finishUsingItem` and removes the Poison effect on the server; verify the ability is functioning during gameplay.

## Required run/debug on a real development PC

From the mod root with JDK 21 and Gradle online/cache ready:

```bash
./gradlew runData
./gradlew clean build
./gradlew runClient
./gradlew runServer
python tools/qa-highquality-mobs.py
```

- Inspect all eight mobs at `/summon dorohedoro:<mob>`: textures, proportion, pivot, leg articulation, wings while resting/flying, movement and entity bounding boxes.
- Verify in a **new Hole world**: spawning underground without excess spawn counts or unexpected surface spawns; no despawn/AI glitches; save and dedicated server works.
- Kill each creature, verify primary and rare drops, verify output recipes in a survival crafting table, confirm alternate vanilla progress remains viable.
- On a surviving Rust-Eater Beetle: brush with a vanilla Brush; ensure nonlethal collection, no unlimited spam, brush durability decreases, cooldown saves on world reload.
- Test Spore Crawler poison and antidote poultice before and after consuming. Test Sludge Crawler slowing.
- Take actual in-game screenshots and compare them against the *approved design sheet*; further shape/pixel texture detailing is still needed for perfect visual parity.
- All earlier mod integration tests remain mandatory (city, smog, interiors, sewer-to-cavern transitions, dimensions, doors, recipes, all vanilla systems, multiplayer).

**No claims that this batch compiles or renders correctly in a running game until those steps pass.**

Offline validator latest result: **406/406 checks passed**, Java 21 parser **77 files / 0 syntax errors**, JSON **261 files valid**. Gradle wrapper attempted, blocked by network access to Gradle distribution.
