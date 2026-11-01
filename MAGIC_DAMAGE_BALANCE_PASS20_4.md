# Pass 20.4 — Offensive technique damage balance (source-level)

All values are health points (2 health points = 1 heart) **before** rank, mastery, Focused casting, defenses and vanilla hurt/invulnerability handling. They are gameplay tuning values, not canon claims.

**Damage multiplier:** `focused ? 1.4 : 1.0` × `(1 + min(20, rank-1) × 0.025)` × `(1 + min(1000, mastery) × 0.0002)`. The direct-hit damage helper clamps to 1–36 health points.

| Magic | Offensive technique | Base direct hit | Other effects |
|---|---|---:|---|
| Mushroom | Spore Puff | 2.0 | Additional magical effects according to technique. |
| Mushroom | Spore Cloud | 1.5 | Additional magical effects according to technique. |
| Mushroom | Fungal Bind | 2.5 | Additional magical effects according to technique. |
| Mushroom | Fungal Bloom | 3.0 | Additional magical effects according to technique. |
| Mushroom | Spore Dominion | 4.0 | Additional magical effects according to technique. |
| Reptile | Tail Lash | 4.5 | Additional magical effects according to technique. |
| Dismemberment | Severing Touch | 3.5 | Additional magical effects according to technique. |
| Dismemberment | Clean Cut | 4.5 | Additional magical effects according to technique. |
| Dismemberment | Dismember Strike | 7.5 | Additional magical effects according to technique. |
| Dismemberment | Precision Nerve | 3.0 | Additional magical effects according to technique. |
| Dismemberment | Limb Lock | 2.5 | Additional magical effects according to technique. |
| Dismemberment | Disarming Smoke | 2.5 | Additional magical effects according to technique. |
| Dismemberment | Severing Wave | 5.0 | Additional magical effects according to technique. |
| Dismemberment | Anatomy Rend | 10.0 | Additional magical effects according to technique. |
| Curse | Curse Mark | 1.5 | Additional magical effects according to technique. |
| Curse | Hex Burden | 2.0 | Additional magical effects according to technique. |
| Curse | Wither Seal | 2.5 | Additional magical effects according to technique. |
| Curse | Mark of Dread | 1.5 | Additional magical effects according to technique. |
| Curse | Persistent Hex | 2.5 | Additional magical effects according to technique. |
| Curse | Misfortune | 2.0 | Additional magical effects according to technique. |
| Curse | Curse Pulse | 2.5 | Additional magical effects according to technique. |
| Curse | Ruinous Curse | 5.0 | Additional magical effects according to technique. |
| Time | Dilated Moment | 1.5 | Additional magical effects according to technique. |
| Time | Slowed Moment | 2.5 | Additional magical effects according to technique. |
| Time | Chrono Burst | 3.5 | Additional magical effects according to technique. |
| Destruction | Smoke Shot | 3.5 | Extra debuff/injury/knockback where specified in the technique; for Destruction, mastery-gated explosions use vanilla blast damage instead of this fallback hit. |
| Destruction | Detonation | 4.5 | Extra debuff/injury/knockback where specified in the technique; for Destruction, mastery-gated explosions use vanilla blast damage instead of this fallback hit. |
| Destruction | Shockwave | 7.0 | Extra debuff/injury/knockback where specified in the technique; for Destruction, mastery-gated explosions use vanilla blast damage instead of this fallback hit. |
| Destruction | Breach Bolt | 5.5 | Extra debuff/injury/knockback where specified in the technique; for Destruction, mastery-gated explosions use vanilla blast damage instead of this fallback hit. |
| Destruction | Rupture Armor | 3.5 | Extra debuff/injury/knockback where specified in the technique; for Destruction, mastery-gated explosions use vanilla blast damage instead of this fallback hit. |
| Destruction | Concussive Wave | 5.0 | Extra debuff/injury/knockback where specified in the technique; for Destruction, mastery-gated explosions use vanilla blast damage instead of this fallback hit. |
| Destruction | Shrapnel Smoke | 6.0 | Extra debuff/injury/knockback where specified in the technique; for Destruction, mastery-gated explosions use vanilla blast damage instead of this fallback hit. |
| Destruction | Annihilation Stream | 15.0 | Extra debuff/injury/knockback where specified in the technique; for Destruction, mastery-gated explosions use vanilla blast damage instead of this fallback hit. |

The 31 remaining techniques are utility or support, including Healing and Protection, and do not deal direct attack damage. Some status effects (poison/wither/curse attrition) also cause damage over time; they do not replace the direct hits above.

When a Destruction spell creates an explosion, actual entity damage comes from the Minecraft explosion; the listed hit damage is its low-mastery/disabled-terrain fallback. Minecraft armor, resistance, obstruction and invulnerability windows may affect damage.

PvP uses player harm permission checks. Curse attrition is attributed to the original online caster and checked against PvP rules. Anatomical limb removal is not modeled, but arm/leg/body injury penalties are saved and may require Healing.

**Status:** Not compiled. Not tested in Minecraft.
