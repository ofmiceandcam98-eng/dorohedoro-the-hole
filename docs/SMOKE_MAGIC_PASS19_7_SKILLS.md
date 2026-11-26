# Magic Skill Reference — Pass 19.7

This is a design and source implementation reference. **NOT COMPILED / NOT GAME-TESTED.**

Each ordinary Sorcerer has exactly one innate magic family. Eight techniques per family (64 total). The starter is granted on character creation; all later techniques require rank and study.

| Magic family | Skill | Required rank | Smoke cost | Cooldown | Source behavior |
|---|---|---:|---:|---:|---|
| Mushroom | Spore Puff | 1 | 9 | 1.8s | Poison a target with a puff of spores |
| Mushroom | Spore Cloud | 3 | 18 | 5.0s | Poison hostile creatures in a cloud |
| Mushroom | Fungal Bind | 6 | 24 | 6.8s | Root an enemy in spreading mycelium |
| Reptile | Scale Skin | 1 | 9 | 4.0s | Reptilian armor protects your body |
| Reptile | Predator Sprint | 3 | 15 | 5.0s | Increase speed and jumping ability |
| Reptile | Reptile Rage | 6 | 24 | 9.0s | Combine reptilian strength and resistance |
| Dismemberment | Severing Touch | 1 | 11 | 2.2s | Precise close-range smoke strike |
| Dismemberment | Clean Cut | 3 | 18 | 4.5s | A longer-range disabling strike |
| Dismemberment | Dismember Strike | 6 | 29 | 8.0s | A devastating precision attack |
| Healing | Mend Flesh | 1 | 10 | 3.5s | Restore your own health |
| Healing | Purify Blood | 3 | 18 | 5.0s | Remove poison and harmful effects |
| Healing | Rapid Regeneration | 6 | 30 | 9.5s | Powerful sustained regeneration |
| Curse | Curse Mark | 1 | 12 | 3.2s | Slow and weaken a hostile target |
| Curse | Hex Burden | 3 | 19 | 5.5s | Inflict weakness and withering |
| Curse | Wither Seal | 6 | 29 | 9.0s | Severe lingering curse against a target |
| Protection | Smoke Ward | 1 | 10 | 4.0s | Grant brief damage resistance |
| Protection | Guardian Veil | 3 | 18 | 5.8s | Create an absorbing protective layer |
| Protection | Sanctuary | 6 | 29 | 9.0s | Protect nearby players and yourself |
| Time | Fleeting Step | 1 | 12 | 5.0s | Brief accelerated movement |
| Time | Dilated Moment | 3 | 23 | 7.5s | Slow a hostile target locally |
| Time | Temporal Window | 6 | 36 | 13.0s | Short localized acceleration and resistance |
| Destruction | Smoke Shot | 1 | 10 | 2.0s | Direct damage from concentrated smoke |
| Destruction | Detonation | 3 | 20 | 5.0s | Short-range burst against nearby enemies |
| Destruction | Shockwave | 6 | 31 | 8.5s | Heavy area damage without breaking blocks |
| Mushroom | Mycelial Sense | 2 | 12 | 5.0s | Detect movement in dark fungal surroundings |
| Mushroom | Spore Purge | 5 | 20 | 6.5s | Use regenerative spores to cleanse yourself |
| Mushroom | Fungal Bloom | 7 | 25 | 7.0s | Slow and poison enemies in a growing bloom |
| Mushroom | Mycelium Guard | 10 | 28 | 8.5s | Coat your body in protective mycelium |
| Mushroom | Spore Dominion | 18 | 48 | 14.0s | A wide-area wave of spores and fungal decay |
| Reptile | Shed Skin | 2 | 13 | 6.0s | Shed damaged skin and remove poison |
| Reptile | Wall Crawl | 5 | 18 | 7.5s | Leap higher and descend safely |
| Reptile | Tail Lash | 7 | 22 | 5.0s | Strike and push a nearby opponent |
| Reptile | Chameleon Hunt | 11 | 26 | 9.5s | Brief camouflage and hunting speed |
| Reptile | Apex Form | 19 | 51 | 16.0s | Assume a formidable empowered reptilian form |
| Dismemberment | Precision Nerve | 2 | 14 | 4.5s | Attack the nerves and weaken a target |
| Dismemberment | Limb Lock | 5 | 22 | 6.5s | Immobilize a target with precise smoke cuts |
| Dismemberment | Disarming Smoke | 8 | 27 | 8.0s | Cripple the target's attacking strength |
| Dismemberment | Severing Wave | 12 | 35 | 10.0s | Wide precision attack against nearby foes |
| Dismemberment | Anatomy Rend | 20 | 56 | 16.0s | Devastating high-mastery targeted strike |
| Healing | Mend Ally | 2 | 13 | 3.5s | Restore a nearby ally or yourself |
| Healing | Cleanse Ally | 5 | 19 | 6.0s | Remove harmful conditions from an ally |
| Healing | Restorative Field | 8 | 31 | 8.5s | Heal players standing nearby |
| Healing | Vital Shell | 12 | 28 | 9.5s | Grant an ally or yourself temporary absorption |
| Healing | Lifeweave | 20 | 55 | 16.0s | Major group healing and regeneration |
| Curse | Mark of Dread | 2 | 14 | 5.0s | Mark an enemy and weaken it |
| Curse | Persistent Hex | 5 | 24 | 7.5s | Make a target suffer a lingering hex |
| Curse | Misfortune | 8 | 27 | 7.5s | Impair the senses of a marked foe |
| Curse | Curse Pulse | 12 | 36 | 11.0s | Spread withering curses through an area |
| Curse | Ruinous Curse | 20 | 54 | 16.0s | Long-lasting powerful curse against one foe |
| Protection | Guard Ally | 2 | 14 | 4.0s | Shield an ally or yourself |
| Protection | Hardening Shell | 5 | 19 | 6.8s | Gain absorption and resistance |
| Protection | Smoke Bulwark | 8 | 30 | 9.5s | Protect all nearby allies with smoke |
| Protection | Purge Barrier | 11 | 25 | 8.5s | Cleanse toxins while strengthening defenses |
| Protection | Last Stand | 19 | 52 | 16.0s | Major regenerative defense for your team |
| Time | Temporal Awareness | 2 | 12 | 6.0s | Heightened perception and speed |
| Time | Hastened Hands | 5 | 19 | 8.5s | Accelerate movement and mining for a short time |
| Time | Slowed Moment | 8 | 28 | 9.2s | Create a localized sluggish moment around a target |
| Time | Time Shelter | 12 | 32 | 11.5s | Temporary defensive temporal pocket |
| Time | Chrono Burst | 20 | 56 | 18.0s | Accelerate yourself while slowing surrounding threats |
| Destruction | Breach Bolt | 2 | 14 | 3.8s | Piercing long-range smoke strike |
| Destruction | Rupture Armor | 5 | 22 | 6.5s | Break the momentum of a protected opponent |
| Destruction | Concussive Wave | 8 | 31 | 8.0s | Damage and repel nearby enemies |
| Destruction | Shrapnel Smoke | 12 | 39 | 9.0s | A precision strike with splash damage |
| Destruction | Annihilation Stream | 20 | 58 | 17.5s | Very powerful concentrated destructive smoke |

## Controls

- Choose **Sorcerer** on first join; complete eight hidden-affinity questions or use **Trust Fate**. Ties allow choosing only between tied types.
- Press **B** to open the Smoke Ledger. **Z X C V G H J K** cast eight individual slots.
- Hold **Alt** with one of those slot keys to toggle **Focused** for that particular slot. Focus costs 1.5× Smoke and boosts applicable effects.
- In the **TECHNIQUES** tab, navigate three-skill pages; once the required rank is reached, practice an unlearned skill until Study reaches 100. Each successful practice costs 5 Smoke; server enforces a 40-tick practice interval.
- In **LOADOUT**, pick a slot and equip learned abilities, or change among nine persistent presets.
- Smoke naturally regenerates on the server; experience from casts increases rank/capacity and per-skill mastery.
- **Smoke Flasks** are one-use borrowed smoke effects and never change innate magic.

## Important limits

- Techniques implemented as server-side prototype effects based primarily on Minecraft status effects, health changes, damage, knockback, and vanilla particles. Not yet custom animated transformations, model morphs, complex time travel, or environmental transmutation.
- Hostile targeting intentionally excludes players; cooperative Healing and Protection can target nearby players. PvP magic should be separately configured and implemented deliberately.
- Source changes are not validated by NeoForge compilation or gameplay testing.
- No Gradle/build attempts performed. This full-source ZIP is NOT a playable mod JAR.
- Earlier Pass 19.6 smoke-flask effects remain starter-effect prototypes.
- Black Powder, grafting, and Devilhood are future systems, not implemented.
