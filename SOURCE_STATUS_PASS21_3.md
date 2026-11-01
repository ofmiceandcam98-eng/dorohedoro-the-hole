# Pass 21.3 — Reference-driven Minecraft asset polish

**Status: NOT COMPILED; NOT TESTED IN MINECRAFT.**

**Base:** Pass 21.2 complete source. Full same project structure; no build/cache folders.

## External art references (reference study only; no third-party assets copied)

- Vanilla Minecraft 1.21 Tricky Trials copper and tuff families — consistent material identity, clean mortar, readable stone/metal patterning: https://feedback.minecraft.net/hc/en-us/articles/27547857163917-Minecraft-Java-Edition-1-21-Tricky-Trials
- Vanilla Minecraft 1.21.4 resin/clump art — silhouette and material differentiation (visual benchmark only; the mod remains Minecraft 1.21.1): https://feedback.minecraft.net/hc/en-us/articles/32385811139085-Minecraft-Java-Edition-1-21-4-The-Garden-Awakens
- Create — framed machine casings, believable rivets, recognizable mechanical shapes: https://modrinth.com/mod/create
- Supplementaries — functional jars and decorative utility shapes: https://www.curseforge.com/minecraft/mc-mods/supplementaries
- Farmer's Delight — readable tool and food silhouettes and world-integrated equipment: https://www.curseforge.com/minecraft/mc-mods/farmers-delight

## What changed

- 21 block texture PNGs refined, including deliberate brick coursing, mortar, rivets, rusted sheet metal, ore clusters, smoke-streaked concrete, translucent barrier, and shaped Cavern Lamp atlases.
- All nine (empty + eight innate family) Smoke Flask textures refreshed with legible internal magic motifs and glass highlights.
- Surgical Reattachment Kit and Smoke Condenser item sprite redrawn to improve small-size silhouette and material separation.
- Eight collectible raw/cooked limb atlases redrawn as deliberately mapped cuboid surfaces instead of decorative straight stripes. Non-graphic, stylized voxel art.
- Eight existing 3D limb item JSON models improved with segmented anatomy, differentiated arm/leg forms, and explicit GUI/hand/ground display transforms.
- All existing remaining textures are retained from the prior 21.2 pass; **this is a focused reference-driven refinement, not a claim that all 90 assets were newly redrawn again.**
- No mod ID, Java code, item/recipe IDs, world generation, spell definitions or network code changed.
- User reported the public mod project is approved by CurseForge. This asset update has NOT been built or published to CurseForge.

## Validation

- All changed PNGs decoded, proper 32x32 / 64x64 dimensions and expected RGBA alpha.
- Every project JSON file parsed, changed limb JSON UVs lie within 0-16 and their texture references exist.
- All original Java source file bytes verified unchanged.
- `PortalService.Frame.stand(int sign)` still uses zero Y offset with the original corrected stand location.
- No build folders, `.gradle`, compiled classes, or caches included.
- Complete zip CRC tested.

## Necessary next checks

Compile on the owner's PC, inspect tiling across multi-block walls (especially mortar continuity), test item model visibility in inventory/hand/ground, inspect lantern brightness while hanging/standing, verify transparent barrier in world and all original behaviors. Treat visuals as unverified until screenshots are provided.

## Out of scope

The previously deferred Dorohedoro weapons in screenshots remain untouched. Magic techniques, limb reattachment/cooking, world generation and other gameplay remain intact, not newly tested.
