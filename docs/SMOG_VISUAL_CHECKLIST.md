# The Hole: opaque smog canopy visual checklist

## Visual design
- Permanent smoke and pollutant haze must hide recognizable blue sky, sunrise, sunsets, sun, moon, stars, and ordinary blocky vanilla clouds while inside any Hole biome.
- Do **not** construct a literal block roof: crops, rain, skylight, weather, night-time entity behavior, redstone daylight sensors and building to world height must remain possible.
- Atmospheric appearance should stay muted and overcast across the day/night cycle; night can still be subtly darker at street level. Lamps and glowing signs should remain meaningful.
- Only Hole biome regions of the actual Overworld receive this treatment. Vanilla presets and the Sorcerers' World / Hell retain their own behavior.

## Runtime acceptance tests (not yet performed)
1. Run Gradle build and launch client on the owner's machine.
2. With fresh world created through the Dorohedoro normal preset, look directly up at noon and at midnight. No disc, stars or bright blue sky should appear.
3. Check distant skyline: upper silhouettes blend into smoke instead of terminating under a bright vanilla cloud sheet.
4. Use `/weather rain` and `/weather clear`: rainfall remains visible and works. Sky stays hidden when clear.
5. Check night/day by `/time set noon` / `/time set midnight`: crops receive real skylight, daylight sensor changes state, nighttime enemy spawning follows existing rules.
6. Stand under a roof, underwater, and in lava: weather, interiors and fluid-specific fog do not become artificially colored by open-air smog.
7. Test an ordinary Minecraft world preset without the Hole biomes. Vanilla sun, moon and clouds must still render.
8. Once the Sorcerers' World is implemented, verify it keeps its own sky and ordinary dry weather.
9. Disable/enable any shader packs and re-test; compatibility is unverified.

## Current implementation
`com.dorohedoro.hole.client.HoleAtmosphere` hooks the NeoForge 1.21.1 client render and fog events. `Dorohedoro` registers it on physical clients only. Render callbacks are conditional on Hole biomes.

State: **source-only, syntax-verified; NO full mod compile or visual validation here**. If `RegisterDimensionSpecialEffectsEvent` refuses replacing `minecraft:overworld`, investigate the supported client extension registration path for the exact NeoForge build rather than globally changing vanilla world lighting or substituting a solid ceiling.
