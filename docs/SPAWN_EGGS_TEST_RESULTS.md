# Pass 11 spawn egg checks — source-only

- Confirmed 8 native `DeferredSpawnEggItem` registrations referencing the corresponding 8 `ModEntities` deferred suppliers.
- Confirmed 8 item model JSON files inherit Minecraft's tintable `item/template_spawn_egg`, and datagen emits matching models.
- Confirmed 8 localized English names, separate Creative tab, Hole tab, and injection into vanilla Spawn Eggs tab (with duplicate guard).
- Confirmed eggs are testing items only: no recipe added to Survival.
- Ran `tools/qa-spawn-eggs-pass11.py`: **44 checks passed**.
- Ran Java 21 syntax parser: **78 Java source files, 0 syntax errors**.
- Ran existing `tools/qa-highquality-mobs.py`: **1,007 source/resource checks passed**.
- Ran existing `tools/qa-cavern-motion10.py`: **364 checks passed**.
- Validated JSON resources: **271 JSON files in src**, no JSON parsing errors.
- Tried `./gradlew --offline build`: fails before compilation because Gradle 9.2.1 is not cached and fetching from services.gradle.org fails with `UnknownHostException`.

All Minecraft in-game functionality, spawn egg registration at runtime, client rendering, server behavior, Glow Moth illumination, and multiplayer remain **untested**.
