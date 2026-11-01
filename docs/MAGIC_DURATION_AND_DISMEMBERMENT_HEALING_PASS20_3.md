# Pass 20.3 — Temporary Healing / Time / Protection; Dismemberment treatment

**Source only; not compiled and not tested inside Minecraft.**

## Governing rules

1. **Healing:** restored hit points and the removal of an actual Dismemberment injury are lasting outcomes. Regeneration, absorption, and other magical boosts remain strictly time-limited. Healing does **not** create permanent auras or blocks.
2. **Protection:** armor/resistance/absorption buffs expire. Generated collidable condensed-smoke barriers expire even at 900–1000 mastery, rather than becoming permanent blocks. Mastery increases their size and lifetime only; maximum barrier lifetime is 1,800 ticks (90 seconds). All **new** barrier blocks are recorded with an expiry in saved world data before they can persist beyond a session.
3. **Time:** the existing temporal auras and slow fields remain temporary. A field expires automatically; field-applied vanilla effects linger briefly after expiry but are not permanent. No persistent time travel or world edits.
4. **Dismemberment treatment:** the real server-side impairment lives in the entity's saved `dorohedoro_sever_until` / `dorohedoro_sever_power` markers and movement mechanics. Healing treatment explicitly removes both markers, plus the associated Slowness and Weakness, immediately. Ordinary detoxifying and defensive spells cannot cure the saved wound. The existing Dismemberment injury itself remains finite-duration in this version; long-term anatomical injuries are not yet simulated.

## Which healing techniques repair the injury?

| Spell | Condition |
|---|---|
| Mend Flesh | Self, technique mastery >= 600 |
| Purify Blood | Self, technique mastery >= 400 |
| Rapid Regen | Self, technique mastery >= 600 |
| Mend Ally | Selected player (or self if no ally), technique mastery >= 350 |
| Cleanse Ally | Selected player (or self), any mastery |
| Restorative Field | Players in radius, technique mastery >= 500 |
| Vital Shell | No injury repair; absorption only |
| Lifeweave | Players in radius, any mastery |

These mastery values are gameplay design choices, not canonical statistics.

## Important compatibility notes

- All 64 techniques, three active slots per preset, nine presets, innate affinity assignment, Smoke Flask behavior, destructive PvP and physical Mushroom magic are preserved.
- Existing world saves made under Pass 20.2 might contain **untracked permanent smoke-barrier blocks** placed at >=900 mastery. Their origin cannot safely be distinguished from other blocks with the same registry ID; this patch does **not** silently scan or erase old builds. Remove any inherited older barrier blocks manually/admin-side. Newly generated barriers are all temporary.
- As this first implementation uses vanilla Slowness and Weakness for the Dismemberment effect, healing an injury can also clear coincidentally overlapping Slowness/Weakness from another source. A dedicated custom-injury MobEffect is a later refinement.
- There is **no Gradle build, Minecraft runtime verification or NeoForge API compilation** in this checkpoint.
