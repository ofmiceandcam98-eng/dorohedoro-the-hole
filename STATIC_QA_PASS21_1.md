# Pass 21.1 — Static QA report

**NOT COMPILED / NOT TESTED IN MINECRAFT. No Gradle, Minecraft or NeoForge dependency download attempted.**

Checks completed using the local source tree:

- **129 Java files** parsed by JDK 21 `JavacTask.parse()` with **0 syntax errors** (parser only: no symbols or API validation).
- **408 JSON files** parsed successfully; all 12 new cooking recipes validated for input, output and recipe type.
- Four cooked texture PNGs opened and validated with Pillow; four corresponding 3D item models parsed.
- Server-side owner UUID / current injury token / limb identifier / proximity / patient crouch checks are present.
- Healing `Mend Ally` + 25 Smoke and surgical-kit paths are present; consumption follows successful reattachment.
- All **64** innate technique definitions retained; **9 presets with 3 slots** retained.
- `PortalService.Frame.stand()` verified as `axis == Direction.Axis.X ? core.offset(0, 0, sign) : core.offset(sign, 0, 0);`
- No `/build`, `/.gradle`, compiled `.class` or Python cache files included.

**Limitations and mandatory validation:** Source checks do not prove API correctness, server/client interaction routing, cooking result behavior, persistent injury correctness, recipe datagen, entity rendering, or multiplayer operation. These all require actual NeoForge compilation and in-game tests on the owner's computer.
