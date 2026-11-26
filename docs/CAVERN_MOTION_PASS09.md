# Cavern movement and surface beetle habitat — Pass 09

**Purpose:** Address user review that the four cavern mobs walked/crawled noticeably more slowly than the sewer group, and allow Rust-Eater Beetles on appropriate Hole surface streets as well as underground.

## Implemented in source

| Creature | Base movement-speed attribute | Leg/body animation cycle | Notes |
|---|---|---|---|
| Sludge Crawler | 0.19 → **0.24** | motion wave 0.91 → **1.46** | Remains a heavy, slower-moving pursuer; its tentacles also move more quickly |
| Rust-Eater Beetle | 0.22 → **0.27** | 1.62 → **2.32** | Faster tripod gait, faster ambient stroll |
| Cavern Burrower | 0.17 → **0.23** | 1.08 → **1.65** | Faster deliberate quadruped steps without insect-like scurrying |
| Spore Crawler | 0.25 → **0.30** | 1.35 → **1.98** | More active alternating six-leg crawl |

The attribute figures are **Minecraft attributes**, not measured blocks per second. Attack AI speed, collisions, server tick behavior, and actual perceived speed must be checked in Minecraft.

### Rust-Eater Beetle surface spawning

- Remains in the Undercity: creature spawn weight **18**, group **1–2**, in underground biome.
- **Industrial** surface: weight **9**, group 1–2; **Derelict**: weight **6**, group 1–2; **Outskirts**: weight **3**, solo.
- Surface placement restricted to height 55–82 and the named Hole biomes, on normal ground and out of liquids; avoids other dimensions, ordinary vanilla biomes, residential homes, markets, and graveyards.
- Added both to the Java biome data generator and the checked-in generated biome JSON. Verify after `runData` that the generated output agrees.
- Surface populations should remain **lower than underground populations**. Population balance needs a live server test at several times of day and on multiple seeds.
- Beetle brushing, drops, textures, recipes and model are preserved. Surface spawns should be able to occur without players hunting deep caves.

### Visual review

- `docs/previews/animation_pass09/cavern_movement_5s.gif`: 50 frames, 5.00 sec, 10 FPS.
- Separate 5-second GIFs for all eight mobs and the two grouped animations are also in that folder.
- The camera and creature models are stationary; the limbs are moving **in place**. This is for reviewing gait rhythm, **not** proof of actual walking velocity/pathfinding in Minecraft.
- Review both surface and cavern scenarios in Minecraft for ground contact, pathfinding, turn rates, collision, clipping, mob density and animation synchrony.

### Validation required on a Windows dev PC

```
.\gradlew.bat runData
.\gradlew.bat build
.\gradlew.bat runClient
.\gradlew.bat runServer
```

Then create a new Hole world, test day/night surface beetles near scrapyards, derelict lots and outskirts, test underground cave beetle populations, observe creature motion on slabs, stairs, water edges, tight corridors and multiplayer, restart server and verify no worldgen regression. This environment has **not** verified the NeoForge build or gameplay.
