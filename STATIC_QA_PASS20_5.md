# Pass 20.5 Static QA — NOT COMPILED

- Source consistency tests: **70/70 passed**
- Parsed JSON files: **387**
- ZIP integrity and byte-for-byte preservation: **passed**
- No `build`, `.gradle`, compiled classes or caches: **verified**
- 64 skills and 64 cast-pattern mappings: **verified**
- All four particle type declarations / providers / atlas descriptors / 28 PNGs: **verified**
- `PortalService.Frame.stand()` file byte-identical to Pass 20.4: **verified**
- `SmokeEngine.java` and `SmokeState.java` byte-identical to Pass 20.4: **verified**
- NeoForge compilation: **NOT ATTEMPTED** (per user instruction)
- In-game particle rendering, FPS, Minecraft PvP, third-person view: **NOT TESTED**

## Reproducible offline checks

Run `python tools/qa-smoke-animation-pass20-5.py`. Uses standard library only.
