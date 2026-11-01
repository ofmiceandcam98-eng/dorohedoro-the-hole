# Pass 20.9 — Static QA only

**NOT COMPILED / NOT TESTED IN MINECRAFT**

- Java 21 source syntax parse: 126 files, 0 syntax errors (parser only; no symbols resolved).
- JSON files parsed: 391
- Source integrity tests: 23/23 passed.
- Changed baseline files: 5 modified, 12 added (including manifests).
- Removed baseline files: 0
- API signatures checked against published NeoForge 1.21.1 Javadoc; **not compiled**.

| Check | Result |
|---|---|
| exactly one item drop on new limb loss | PASS |
| four limb variants registered | PASS |
| non-stackable item variants | PASS |
| owner name and limb are present in display name | PASS |
| item provenance persists victim UUID and name | PASS |
| name visible in world | PASS |
| vanilla pickup applies, no owner restriction | PASS |
| drops created server-side only | PASS |
| item does not heal by picking it up | PASS |
| no item creation on blocked invalid casts | PASS |
| physical severing visuals remain | PASS |
| regrowth draws animated tissue threads | PASS |
| all 64 techniques | PASS |
| eight per each innate type | PASS |
| three slots and nine presets | PASS |
| portal landing fix unchanged | PASS |
| no generated/build caches | PASS |
| JSON resources parse | PASS |
| severed_left_arm 3D model and texture | PASS |
| severed_left_leg 3D model and texture | PASS |
| severed_right_arm 3D model and texture | PASS |
| severed_right_leg 3D model and texture | PASS |
| no prior project files removed | PASS |

## Runtime tests still required

- PvP chance and one dropped limb per actual amputation; no drops for blocked/duplicate hits.
- Player-name text, loot pickup and NBT survival through inventory, chest, restart and other player pickup.
- 3D item asset display, collision, severing + regrowth visual timing, and multiplayer rendering.
- Confirm baseline portal stand, world generation, all prior mechanics in a test world.
