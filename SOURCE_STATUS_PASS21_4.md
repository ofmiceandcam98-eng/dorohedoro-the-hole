# Dorohedoro: The Hole — Pass 21.4 status

**NOT COMPILED — NOT TESTED IN MINECRAFT**. Source-only visual and structural changes.

- Baseline: Pass 21.3 full source.
- 31 block PNGs revised, 59 item PNGs revised, 8 item model JSONs revised.
- All textures retain the original resource IDs and paths. No Java source files changed.
- All 64 existing techniques, three equipped slots, nine presets, portal landing correction, persistent limb identity and reattachment, cooking recipes, existing world generation remain untouched.
- Four reproducible art tools added under tools/art.
- Original PNGs are 32x32 except eight limb UV atlases which remain 64x64.
- The new models use 5 block-model cuboids per item; they are not dynamically rigged.
- No third-party image art copied or included.

Windows test: from Java 21 development directory run ./gradlew runData then ./gradlew build, install resulting JAR on matching client/server and check a fresh world. Back up saves and old JAR; do not upload a release build before successful testing.
