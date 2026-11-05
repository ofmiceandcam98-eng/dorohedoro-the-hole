# Pass 20.4 — Magic combat injury + real damage (SOURCE ONLY)

Baseline: `Dorohedoro_Pass20_3_Full_Source_Temporary_Healing_Time_Protection_NOT_COMPILED.zip` extracted into the original project layout. Full project included, not a patch.

## Completed in source

- Preserved 64 techniques: eight per innate magic type, 3 equipable slots and 9 saved presets.
- Added real direct health damage to all 33 offensive spells. Mushroom, Curse, Dismemberment and offensive Time spells no longer depend on status effects alone. Existing Reptile and Destruction damage is normalized through the same server-owned handler.
- Damage scales with rank/mastery/focused casting and uses an attack-attributed magic source. Eligible player targets are filtered through server PvP permissions. Destruction explosions and world damage remain intact.
- Curse periodic damage now remembers the casting Sorcerer, attributes damage to them and checks PvP rules; recurring attrition only occurs while the caster is online in the same dimension.
- Dedicated arm, leg, and body Dismemberment injuries use saved entity data and named transient attributes. Strongest wounds can persist until Healing. Healing other players repairs those injuries without stripping unrelated potion effects on new wounds; old-save wound migration retains its legacy-cleanup exception.
- Technique descriptions clearly distinguish damaging attacks from support magic. Healing, Protection and all Time-field effects are nonpermanent except Healing's repair of injuries.
- No change to physical Mushroom growth, 3-slot loadouts, city generation, high-mastery home destruction, or corrected PortalService.Frame.stand() landing.

## Verification and limitations

- Static QA only; **NOT COMPILED — NOT TESTED IN MINECRAFT**.
- Gradle and Minecraft/NeoForge dependency downloads/builds intentionally not attempted.
- No engine-level verification of balancing, hit registration, armor, friendly fire, world explosions, cross-world persistence or visuals.
- Visible injury markers are particles and attribute impairments, not severed limb geometry.
- Character death or dimension transfer may require separate integration tests for injury data; do not claim production readiness.

## Handoff / owner validation

Use the supplied full source as the new source checkpoint; keep prior working JAR and world backups. Run your local `runData` and `build` with cached/downloadable NeoForge dependencies. In a new disposable world, test same-family damage at beginner/high mastery; one-target vs area spells; PvP enabled/disabled; armor/resistance; damage attribution on lethal hits; injury/healing; Curse caster logout; Protection/Time expiry; and Destruction's own-home damage. Check logs for any API differences. Never deploy this uncompiled source as a functioning JAR.
