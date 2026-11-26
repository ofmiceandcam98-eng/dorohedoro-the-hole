# Pass 20.2 — Physical Magic and Destructive PvP — SOURCE ONLY

**Minecraft Java 1.21.1 / NeoForge. NOT COMPILED; NOT TESTED IN MINECRAFT.**

Baseline: complete Pass 20.1 source (which itself preserves the user's Pass 18 Fixed Source plus subsequent work). Source-only delta. Gradle was not attempted, as requested.

## Rules

- Eight innate magic types; eight techniques per type (64 total), 3 active ability slots, 9 presets. Quiz-assigned innate magic remains permanent.
- Mushroom: physical small and giant mushroom blocks, with vanilla collision and harvesting, persistence governed by mastery.
- Protection: physical dark smoke barriers with collision, staged placement, saved expiry, permanent versions at mastery 900. Not world-erasing.
- Reptile: server-driven wall climb and body size effect (not yet custom mesh or skeleton).
- Dismemberment: actual movement/weakness impairment; PvP opponents eligible if the server allows it. No actual amputated visual limbs.
- Curse: temporary persistent nonlethal affliction, including PvP opponents where PvP is permitted.
- Healing: stronger targetable ally healing; healing other players remains supported.
- Time: sustained local slowing field that affects eligible enemies. **Not canon time travel**.
- Destruction: normal vanilla block explosions credited to the caster, **enabled by default** in the world/server config. NO protection or immunity for the caster's own home. Buildings constructed by other players can also be destroyed. Explosion resistance still matters.
- Terrain destruction is independent of `/gamerule mobGriefing`; disabling mobGriefing will not protect houses from player Destruction blasts. Server owners can disable it with `destruction.terrainDamage=false` in the mod's SERVER config (`dorohedoro-server.toml`).
- The PvP target filters use `ServerPlayer.canHarmPlayer` to respect game/team PvP eligibility. Vanilla explosion events permit other mods to manage explosion impacts; external land-claim compatibility is NOT verified.

### Destruction mastery thresholds (per technique, not player rank)

| Technique | Starts breaking blocks at mastery | Base vanilla explosion power |
|---|---:|---:|
| Detonation | 150 | 1.35 |
| Concussive Wave | 300 | 1.75 |
| Breach Bolt | 350 | 1.10 |
| Shockwave | 450 | 2.35 |
| Annihilation Stream | 650 | 3.00 |

Explosion power scales further with mastery and focused cast; capped at 4.2. Other Destruction spells remain damaging without structure demolition. These numbers are experimental balance settings; power is **not** a literal crater radius in blocks.

## IMPORTANT LIMITATIONS

- This source is **not compiled or gameplay tested**. Confirm API compatibility, mod loading, Server configuration registration, PvP eligibility, actual drops, and multiplayer explosions before release.
- Explosions damage terrain and can damage the caster or nearby players. Use backed-up test saves until gameplay verified.
- The Reptile, Curse, Time, and Dismemberment implementations are gameplay approximations, not final canon-quality visuals.
- Unloaded chunks are not force-loaded, to avoid uncontrolled world generation.
- No source or asset files from Tensura addons were copied.
- The portal landing fix in `PortalService.Frame.stand()` is preserved.
