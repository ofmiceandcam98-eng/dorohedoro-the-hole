# Pass 21.2 — Asset-quality overhaul (source only)

**Baseline:** Dorohedoro Pass 21.1 complete source archive.

**Status: NOT COMPILED / NOT TESTED IN MINECRAFT.** No Gradle or game launch was attempted.

## Changes

- Replaced **all 59 item PNG textures** and **all 31 block PNG textures** at their existing paths.
- Redrew items at 32×32 resolution, with the 8 limb texture atlases at 64×64 to preserve existing model UV coordinates while allowing more pixel detail.
- Redrew blocks at 32×32 resolution, preserving translucent portal and smoke barrier textures.
- Upgraded eight existing limb item JSON models from single straight cuboids to three segments (upper limb / lower limb / hand or boot), using the same texture path and original normalized UV scheme.
- Preserved the same item IDs, block IDs, recipe IDs, Java logic, mechanics, network packets and world-generation code.
- Black Powder and Smoke Canister are **not** new items in this archive; they appeared only in earlier illustrations. Existing smoke flasks and condensers were updated.
- Existing Minecraft vanilla mushrooms remain vanilla assets. Their placement and magic code was not changed.

## Art direction

Vanilla-compatible, deliberate pixel-art silhouettes; clear value separation; warm metal highlights and dark outlines; varied mineral structures, masonry joints and industrial weathering; visually distinct smoke-flask payloads; more readable medical and engineering tools. These PNGs are new source assets rather than AI-generated concept boards.

## Validation

- All 90 affected PNGs opened successfully and have expected dimensions.
- All JSON files parse and item model texture paths resolve.
- PortalService.Frame.stand(int sign) still exists and its Java bytes are unchanged versus Pass 21.1; the portal fix is preserved as provided by the baseline.
- 64 skills, 3 equipped abilities per preset, 9 presets, Healing/reattachment, physical Mushroom magic, PvP destruction, and cooked limb recipes are unchanged because gameplay Java, data and item registrations are untouched.
- Full source ZIP is complete and excludes build artifacts and Gradle caches.

## Next verification on your machine

Compile using your regular Windows procedure, then inspect each new sprite in inventory, in hand, as a ground pickup and next to vanilla blocks. Pay particular attention to the new segmented 3D limb item model UVs and translucent smoke barrier. In-game visual quality and model compatibility are unverified.
