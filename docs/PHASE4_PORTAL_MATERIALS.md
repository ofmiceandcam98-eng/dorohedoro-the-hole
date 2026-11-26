# Phase 4 — portal components, actual recipes, and acquisition

**Status:** source-implemented, offline/static checked only. No NeoForge compile, datagen run, client test, survival craft test, portal-travel test, or restart test has passed in this environment. Do not describe Phase 4 as runtime-complete.

The recipes, materials and art below are **original Minecraft gameplay adaptations**, not canonical Dorohedoro manufacturing instructions. All items use `dorohedoro:` namespaced IDs and their own original 16×16 textures, regenerable with `node tools/make-textures.mjs`. Source-of-truth recipes are in `ModRecipes.java`; matching generated JSON has been included for standalone pack loading before regeneration.

## Material and item catalog

| ID (`dorohedoro:`) | Purpose | Obtain or craft | Original texture/model |
|---|---|---|---|
| `smoke_soaked_filter` | Scavenged item retaining traces of old magical smoke | 16% chance as an additional drop when mining Industrial Debris; also in apartment, factory and laboratory loot | Distinct dirty mesh/filter sprite |
| `smoke_residue` | Magical condensate required for door apparatus | Smelt one Smoke-Soaked Filter for 200 ticks (1 unit); also found in selected loot | Smoke particles and soot sprite |
| `smoke_condenser` | Key machinery for Artificial Door Core | 3×3 below | Copper-framed condenser with green gauge |
| `smoke_conduit` | Transfers captured smoke; used in preserved conduit | 3×3 below produces 2 | Copper bent-pipe sprite |
| `preserved_smoke_conduit` | Stable connection used in both cores | Shapeless Smoke Conduit + Smoke Residue + Bone Meal | Luminous treated pipe sprite |
| `reinforced_door_frame` | Construction of artificial 4×5 frame | 3×3 below produces 4 | Riveted steel/iron block |
| `artificial_door_core` | Artificial Door origin core | 3×3 below | Machinery block with exposed smoke core |
| `door_activation_tool` | Activates Artificial Door frame | 3×3 below | Wrench-like tool |
| `door_linking_key` | Inspect linked target | 3×3 below | Metallic key |
| `etched_gate_stone` | Construction of 5×5 Devil Gate frame | 3×3 below produces 4 | Dark ritual stone with marks |
| `devil_sigil` | Expensive Devil Gate attunement material | Rarely found in laboratories/crypts, or crafted using costly renewable/survival-accessible materials | Scarlet glyph on aged paper |
| `infernal_door_core` | Hell gate origin core | 3×3 below | Dark ritualized core block |
| `ritual_tool` | Activates Devil Gate | 3×3 below | Bone-and-stone ritual tool |

**Not craftable / not in creative crafting progression:** `generated_door_frame`, `generated_gate_stone`, `generated_artificial_core`, `generated_infernal_core` and `portal_surface`. These are generated paired-return components. They do **not** drop the corresponding crafted frame or core when broken, to prevent infinite material generation.

## 3×3 recipes

Spaces are empty slots. The IDs and counts in this table match `ModRecipes` and `src/generated/resources/data/dorohedoro/recipe`.

### Smoke Condenser ×1
```
I R I
C S C
I R I
```
I = Iron Ingot; R = Redstone Dust; C = Copper Ingot; S = Smoke Residue.

### Smoke Conduit ×2
```
C G C
I R I
C G C
```
C = Copper Ingot; G = Glass; I = Iron Ingot; R = Redstone Dust.

### Preserved Smoke Conduit ×1
Shapeless: Smoke Conduit + Smoke Residue + Bone Meal.

### Reinforced Door Frame ×4
```
I R I
R S R
I R I
```
I = Iron Ingot; R = Rusted Plating; S = Stone Bricks. Rusted Plating is craftable from 2×2 Scrap Metal (2 plating per recipe).

### Artificial Door Core ×1
```
B C B
I M I
B R B
```
B = Bone Block; C = Smoke Condenser; I = Iron Ingot; M = Preserved Smoke Conduit; R = Redstone Block.

### Door Activation Tool ×1
```
  R I
  S R
S
```
I = Iron Ingot; R = Redstone Dust; S = Stick. (Rows use fixed 3-slot alignment in the actual recipe JSON.)

### Door Linking Key ×1
```
  I I
  R I
S
```
I = Iron Ingot; R = Redstone Dust; S = Stick.

### Etched Gate Stone ×4
```
B S B
S R S
B S B
```
B = Bone Block; S = Stone Bricks; R = Redstone Block.

### Devil Sigil ×1 (noncanonical survival adaptation)
```
O G O
S B S
O E O
```
O = Obsidian; G = Gold Ingot; S = Smoke Residue; B = Bone Block; E = Emerald.

Requires four Obsidian, two Residue, one Bone Block, one Gold Ingot, and one Emerald. None are Hell-exclusive. Players can still find rare preexisting sigils in exploration loot.

### Infernal Door Core ×1
```
B S B
R C R
B S B
```
B = Bone Block; S = Devil Sigil; R = Etched Gate Stone; C = Preserved Smoke Conduit.

### Ritual Tool ×1
```
  R S
  B R
B
```
R = Redstone Dust; S = Gold Ingot; B = Bone.

### Smoke Residue via smelting
1× Smoke-Soaked Filter in a furnace → 1× Smoke Residue, 200 ticks, 0.35 XP. Filter sources are the finite smoke-contaminated industrial remains scattered through an **infinite** generated Hole. This is not a claim that mundane coal makes magical smoke.

## Build instructions

**Artificial Door:** 4 blocks wide and 5 high. Bottom core at column 2, counting from left, with a 2×3 clear interior. Requires 13 Reinforced Door Frames + 1 Artificial Door Core. Four frame crafts yield 16 frames, leaving 3 spares. Right-click the core using Door Activation Tool. The linked end is a generated no-drop return frame. Check dimension registration and safe position before any material-consuming actions.

**Devil Gate:** 5 blocks wide and 5 high. Bottom core at the center with a 3×3 clear interior. Requires 15 Etched Gate Stone + 1 Infernal Door Core. Four frame crafts yield 16 stones, leaving 1 spare. Right-click the core using Ritual Tool. All first-gate ingredients obtainable before Hell.

**Shared backend:** Artificial Door joins Hole ↔ Sorcerers' World; Devil Gate joins Hole ↔ Hell. Both use portal core block entities with persisted UUID links and safe landing checks; gameplay function is currently **unverified**. Smoke Doors and En Family Doors are not implemented in this phase.

## Release-quality acceptance rule for every future item

Every new item, material, block, machine, food, mask, weapon and spell component must include **all** of: a name and stable registry ID; an original PNG or appropriate 3D model; generated model and/or blockstate JSON; item/block registration; localization; an actual obtainable source; survival crafting, processing, trading or loot as appropriate; integration with creative/recipe UI; correct drops; and tests that a real player can obtain and use it. Do not register inert placeholder items as if finished. For magic-exclusive components, provide a non-magic alternative when required to keep human survival viable. FTB Quests is intentionally deferred.

## Still needs real Minecraft verification

Run `./gradlew --no-daemon runData`, `./gradlew --no-daemon build`, `./gradlew --no-daemon runClient`, and dedicated-server and recipe/crafting tests. Confirm model loading, transparent portal rendering, interactions, source and return doors, save/restart, possible chunk seams, and protections. An offline script cannot prove behavior or API compatibility.
