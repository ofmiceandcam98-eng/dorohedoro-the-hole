# Sorcerers' World: asset manifest

What is bundled from other builders, what was considered and why it was rejected. Every page was read in a browser,
most recently on 2026-11-26. **A free download is not permission to redistribute:** only builds whose page grants reuse
in other projects are bundled.

## Bundled

| ID | Original | Builder | Licence on the page | Original file | Converted file | Credit shown |
|---|---|---|---|---|---|---|
| `victorian_exchange` | "Victorian Town Building", https://www.planetminecraft.com/project/victorian-town-building/ | **Frit** | The page says to use it however you want, including modifying and rebuilding it and including it in your own cities and projects. No credit is required. | `.schem`, Sponge v2, DataVersion 4671 (a later 1.21.x), 26 x 29 x 20, all vanilla blocks, empty interior. SHA-256 `3f1a6d0ae47aac8f5421adb0d99f89d8adf20a4f61bf308f28c937407489f331` | `data/dorohedoro/structure/sorcerer_city/victorian_exchange.nbt`, 3,382 blocks, 126 states. SHA-256 `81a0470ff0929b06f3e03cefeef47e19eafa3f4e4790e7c76ae5345f8fb950ea` | Credited anyway: in this manifest, in the CurseForge description and in the code comment on `SorcererBuildings.exchange` |

**How it was converted** (`tools/community/convert-builds.mjs`, which checks the original's SHA-256 first):

- **Blocks newer than 1.21.1** (`tools/community/compat-1.21.1.mjs`):
  - shelves (1.21.9) become open trapdoors of the same wood, facing kept; pale oak becomes birch;
  - pale oak signs, fence gates and so on become birch;
  - copper bars become iron bars, and copper chains become chains.
- **Every output block** is checked against vanilla 1.21.1.
- **Street entrance:** the copper railing at x = 10 (y = 0 and 1) beside the porch stair is removed.
- **Fitted out in the mod, not in the file:**
  - an underfloor fill;
  - a staircase between the floors (the shell had none);
  - three market stalls with their keepers, back storage, an office and library, and an apartment;
  - lanterns and loot.
- Decorative signs and shelves lose their text and items; they were trim.

## Considered, not bundled

| Build | Builder | What the page says | Decision |
|---|---|---|---|
| Gothic Manor (createmod.com) | Sadvely | No licence stated. Requires Create and Create: Copycats+; empty interior | Not bundled: no permission, and it would make Create a hard dependency |
| Gothic Castle (createmod.com) | Fallentick2 | No licence stated. Uses `pale_moss_block`, which is not in 1.21.1 | Not bundled: no permission |
| Gothic Townhouse | epic_schnitzel | "You can use this house on your map, but please credit me" (2013, old numeric-ID format) | Not bundled: permission covers maps, not redistribution in a mod. Ask the builder |
| Kenport, The Steampunk City | Carlec_ and TessaBoone | Asks for credit if used; no redistribution grant. 1.14.4 schematic | Not bundled: ask both builders |
| Steampunk"ish" Sky Citadel | Francesco2704 | "feel free to use it" | Not bundled yet: closest to permitted, but not explicit about redistribution. Ask the builder |
| Enchanted Mushroom Mansion | MrLionUS | No licence stated; the site footer reserves creators' copyright | Not bundled |
| Gothic Cathedral | NoobBuilder_ | No licence stated | Not bundled |
| The Steampunk City | Aderlyon build team | Not checked in detail: about 800 x 800, reference only | Not bundled |
| Wizard Tower, Steampunk Castle1, Halloween SteamPunkin | FatPat666 | States CC BY 4.0, but credits Sketchfab 3D models by other artists | Not bundled: voxelized 3D models (licence chain to the Sketchfab originals unverified), glowstone-filled, far off the vanilla art standard |
| Crimson Sanctum, Crimson Crown | TheCroix | "do not redistribute the original" | Not bundled: explicitly forbidden |
| The Triumvirate Conservatory | (page author) | CC BY-NC-SA 4.0 | Not bundled: ShareAlike would bind the mod's licence; NonCommercial conflicts with CurseForge rewards |
| Classic Greenhouse | barrel_maker | "Feel free to use my builds anywhere you want", with credit if shown publicly | Candidate: permission reads as sufficient. Download is a world save, which needs a region-file importer first |
| Victorian Greenhouse/Conservatory | (page author) | "Please credit if used in an official capacity", 1.21.11 | Candidate: confirm with the builder |
| medium Canadian farmhouse | (page author) | "free to use, just give me credit" | Candidate for hamlets; not Gothic |
| First Baptist Dallas | Colton_C3 | "Feel free to use my build in your projects as long as you credit me" | Not used: a replica of a real church |

## Original assets

All block states are vanilla 1.21.1. There are no new textures or models.

| Asset | Path |
|---|---|
| 15 building and lot designs | `src/main/java/com/dorohedoro/hole/worldgen/sorcerer/SorcererBuildings.java` |
| Roads, plaza, fountain, lamps | `src/main/java/com/dorohedoro/hole/worldgen/sorcerer/TownRoadsPiece.java` |
| 11 loot tables | `src/main/resources/data/dorohedoro/loot_table/chests/sorcerer_town/` (generated by `tools/gen-sorcerer-loot.mjs`) |

## Asking for permission

To bundle a "not yet" build, the owner messages the builder through Planet Minecraft or Discord. The message names the
mod (free on CurseForge), the build, and that it will be modified and credited, and asks for permission to redistribute
it inside the mod. Save the reply (a screenshot) under `docs/permissions/` and add the build to `BUILDS` in
`tools/community/convert-builds.mjs`.
