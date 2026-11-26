# Pass 21.4 — Static QA (source-only)

- 59 item textures read successfully; all RGBA. Eight limb atlases remain 64×64; other item sprites remain 32×32.
- 31 block textures read successfully; all 32×32 RGBA. Portal and condensed smoke barrier retain translucency.
- Eight 3D item model JSONs now have five model cuboids each; geometry and face UV coordinates lie in Minecraft's 0–16 coordinate range, rotation angles are valid.
- All 110 resource JSON files parsed; local `dorohedoro:` model texture references resolve to actual PNGs.
- The portal landing correction remains in `PortalService.Frame.stand`.
- All Java files are byte-identical to Pass 21.3.
- Before/after inventory QA includes an actual 16×16 downscale, a 32×32 view and enlarged original pixels for selected items.
- Before/after world-wall QA repeats the actual 32×32 block texture in a 3×3 pattern for nine representative materials. Repeated crack motifs were reduced after review.
- Complete ZIP integrity and absence of build artifacts checked at packaging time.

NOT COMPILED / NOT TESTED IN MINECRAFT. Structural QA does not guarantee visual parity with high-quality vanilla/mod assets or correct gameplay rendering.
