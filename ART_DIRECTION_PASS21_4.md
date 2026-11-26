# Pass 21.4 — Minecraft-native art benchmark

Scope: ART AND MODELS ONLY. No Java source, item IDs, recipes, lore, world generation, or spell definitions changed.

## Benchmarks reviewed (reference, not redistributed)

- Minecraft Java 1.21 Tricky Trials copper/tuff families — coherent ramps, natural clustering, differentiated variants: https://feedback.minecraft.net/hc/en-us/articles/27547857163917-Minecraft-Java-Edition-1-21-Tricky-Trials
- Minecraft vanilla 1.21.1 asset categories, reviewed for texture density and item silhouette: https://mcasset.cloud/1.21.1/assets
- Create — framed machinery, deliberate material boundaries and specular highlights: https://www.curseforge.com/minecraft/mc-mods/create
- Supplementaries — vanilla-readability of jars, containers, fittings: https://www.curseforge.com/minecraft/mc-mods/supplementaries
- Farmer's Delight — simple but memorable food/material item silhouettes: https://www.curseforge.com/minecraft/mc-mods/farmers-delight

NO third-party textures, models, code, or icons were copied into this package. Assets are generated/original.

## House style

- **At inventory size:** identify the object primarily by its silhouette; 16-pixel nearest-neighbor downsample is a mandatory *preview check* even for 32-pixel original item PNGs. Do not judge solely at 8x zoom.
- **Materials:** tuff/copper-style hierarchy: mortar/seam -> material face -> shallow bevel -> restrained material weathering. Glass, cloth, metal, leather, bone, and stone must not all share the same edge highlight.
- **Hole aesthetic:** soot, acid seepage, weathered plaster, restrained industrial brass, tarnished zinc, limited warm emergency light. Art must remain readable against Minecraft inventory and world lighting.
- **Block tiling:** always preview 3x3 adjacent blocks. Remove oversized repeated cracks or repeated obvious specks. Avoid random pixel static where constructed edges should exist.
- **3D limb drops:** anatomical implication remains stylized / non-graphic; no realistic gore. Owner names and treatment logic are in the Java/item components and remain unchanged. Eight models use five segments with individual UVs and left/right geometry.
- **Transparent surfaces:** preserve alpha in portal and condensed smoke barrier textures.

## Iterations within Pass 21.4

1. Rebuilt 90 assets to design-led palettes and shapes, not noise overlays.
2. Reviewed at 100px, 32px and 16px; found black resources low contrast; strengthened rim highlights.
3. Repeated source block art into 3x3 walls; found repeated long mineral fractures; reduced them to short irregular cracks.
4. Reviewed eight model JSONs, replaced 3-cube straight segments with 5 cuboids including hand/thumb or forward boot elements and side-dependent bends.

## Outstanding gates before release quality can be claimed

- Actual in-game GUI/in-hand/third-person/world drop screenshots; depth sorting and face UV correctness cannot be established from PNG + JSON checks.
- Evaluate texture tiling under default and popular shader lighting, at normal and low graphics settings.
- Review with a 16x default vanilla inventory background; visually inspect at regular UI scale.
- Ensure geometries of detached items fit GUI and dropped item rendering, don't clip into ground, and align with severing animations.
- QA against colorblind/high-contrast settings and biome lighting.
- Compile and multiplayer test on Windows Java 21 + 1.21.1 NeoForge.

**Do not advertise as verified Minecraft/CurseForge release quality yet.**
