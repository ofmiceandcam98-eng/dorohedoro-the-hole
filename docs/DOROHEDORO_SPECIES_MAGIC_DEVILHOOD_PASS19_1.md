# Dorohedoro Species, Magic, and Devilhood — Pass 19.1 Canon-Aware Design Reference

**Design and source-reference document ONLY, not implemented gameplay.** Reviewed 2026-11-01. This document is an implementation contract for future phases. No race selection UI, stat system, or smoke abilities have been created or tested by this addendum.

## Sources and epistemic caution
The Dorohedoro Wiki is a community-maintained reference and may include spoilers or errors. Use the manga/anime as the final creative reference and validate details before shipping. Do not copy manga panels, anime shots, character likenesses, wiki text, source code, or mod assets into the project without appropriate rights.
- https://dorohedoro.fandom.com/wiki/Types_of_Magic — named Magic Users and their reported types.
- https://dorohedoro.fandom.com/wiki/Magic — smoke biology; one innate specialty; smoke storage/residue.
- https://dorohedoro.fandom.com/wiki/Magic_Users — biology, amount/type of smoke, masks, partners, Blue Night, mixed parentage.
- https://dorohedoro.fandom.com/wiki/Devils — Devil nature and relative power.
- https://dorohedoro.fandom.com/wiki/Devil_Exam — voluntary, difficult path to Devilhood.
- https://dorohedoro.fandom.com/wiki/Category:Human — natives of The Hole.
- https://dorohedoro.fandom.com/wiki/Hole — unique entity rather than ordinary playable biological race.

## Three intelligent classifications, but not three equivalent starting races
| Classification | Lore identity | First-join rules | Advancement |
| --- | --- | --- | --- |
| Human | Native life of Hole, lacks biological smoke magic | Playable starting origin; ordinary survival and equipment professions | Combat, scavenging, medicine, anti-magic protection, mechanical skill, exploration; endgame viability through systems and tactics rather than comparable supernatural raw power |
| Magic User / Sorcerer | Distinct biological species; devil tumor, smoke veins/ducts and innate specialty | Playable starting origin; one unique type of smoke magic, not a universal spell list | Improve output, smoke quality, control, delivery, technique within that one type; masks/equipment/partnerships |
| Devil | Unusually powerful beings of Hell; most were transformed from Sorcerers; Chidaruma is a special exception | Visible on selection UI as LOCKED/LATE-GAME ASCENSION; NOT a normal fresh-world origin | Sorcerer-only Devil Exam: approval, tutor, armor, difficult multi-part trials, abstinence trial, transformation, consequences, configurable balance |

**Special cases:** Half-human/Sorcerer offspring, curses, magic-transformed bodies, Caiman-like anomalies, and the singular entity Hole do NOT justify generic race-card spam. Reserve special origins, transformations, and exceptionally unusual people for separately tested story/config options. They are not ordinary species picks.

## Required initial-screen user experience (Tensura-level quality, original implementation)
- First login, once per player per world/server after saved identity query, show an original industrial selection screen (not Tensura visual/code copy).
- Three information cards: HUMAN (selectable), SORCERER (selectable), DEVIL (locked, preview its lore-appropriate late-game unlock requirements).
- For each show starting location, resources, weaknesses, progression paths, combat role and long-term milestones. Full keyboard navigation, accessibility and UI scaling.
- Human selected -> start in appropriate area of Hole; no innate smoke production. Sorcerer selected -> lore-coherent arrival in Hole or optional Sorcerers' World starting rule; do not override user's original requirement that the world starts in Hole. The default world remains The Hole for everyone.
- On selecting Sorcerer, resolve one innate magic specialty by a separate selection/seeded-discovery step with documented difficulty and rarity. Choice must not be silently rerollable to farm rare powers. Rare exceptions need progression gating and/or explicit server policy.
- Confirmation transaction is server-authoritative and atomic; persist ID/variant/version/first-grant, rank, learned techniques, cooldowns and smoke resources; guard reconnect, death, disconnection and old-player migration.
- Cross-Eyes, En Family and other affiliations are factions, NOT races. Do not implement the postponed faction/villager phase here.

## Smoke is NOT a generic mana pool
- A Sorcerer's power consists of smoke capacity/output/purity (rarity/status) AND the innate type's action effects; this distinction affects social rank, trade, combat and counterplay.
- One type per ordinary Sorcerer. Within that type, train a branching skill tree of *techniques*, not unrelated elements/other characters' specialties.
- Effects require actual emitted/spent smoke, range, targets, cooldowns, visualization and server-verified resource cost. Some smoke can be harvested/stored in jars/sprays, with source provenance and suitable restrictions.
- Masks can improve efficiency/strength, and lower-tier or custom-made gear must be obtainable in survival. Smoke duct limitations, smoke injury and surgery can become carefully balanced systems later.
- Unusual cases (e.g. suppressed, dormant, no usable smoke, mixed parentage or artificial interference) are separate scenario mechanics, NOT ways to grant all abilities.
- Human players should interact with smoke-derived equipment if lore and balance allow, but never naturally generate Sorcerer smoke without an exceptional, explicit, tested mechanic.

## Reference catalog: named examples from the community wiki (NOT an implementation checklist)
These are magic themes for ORIGINAL gameplay designs, and should not automatically grant proprietary character identities or imply all powers are suitable for players.

| Sorcerer example | Reported innate magic | Design implication |
| --- | --- | --- |
| Fujita | Destruction | Projectile/impact with low starting output as an example of common magic |
| En | Mushroom | Growth and transformation, environment interaction and counterplay |
| Ebisu | Reptile | Transformation with form and animation requirements |
| Shin | Dismemberment | Surgical separation; avoid instant-kill PvP mechanics |
| Noi | Healing | Repair/restoration, meaningful cost and target validations |
| Risu | Curse | Exceptionally conditional/dormant supernatural effect |
| Nikaido | Time | Very rare, world-safe bounded intervention, no arbitrary server rollback |
| Chota | Dispelling | Anti-magic/negation with scope and resistance |
| Kikurage | Revival | Exceptionally rare, death/save/exploit-sensitive and tightly controlled |
| Kawajiri / Asu | Teleportation | Distinguish from constructed Door travel |
| Shou | Invisibility | Concealment with counterplay and mob AI awareness |
| Baku | Explosive | Area effects, configurable block griefing |
| Natsuki | Protection | Shielding/barriers and defensive mastery |
| Fuu | Rot | Decay/status damage, immune targets and reversal |
| Yakumo | Flight | Movement with server authority and vertical safety |
| Turkey | Puppet / Cooking | Manipulation/constructs rather than universal conjuration |
| Aitake / Maitake | Metamorphosis | Entity form changes with ownership/duration limits |
| Fukuyama | Meat-pie transformation | Idiosyncratic specialization reinforces varied magic |
| Shaitan | Healing (tentative) | Flag uncertain community claims for verification |
| Acupuncturist | Face transformation | Narrow cosmetic/body alteration; consent for multiplayer effects |
| Red Brake's partner | Muscle augmentation | Physical enhancement specialty |
| En building crew | Enlargement | Environmental interaction and bounded scaling |
| Additional names | Gravity, Liquid Manipulation, Telepathy, Barrier, Precognition, Flight, Absorption, Explosion, Augmentation, Reduction | Expand only when individual techniques and counterplay are designed |

**Do not replace one personal magic with a huge open spellbook.** A master of one unusual specialty is consistent; everyone eventually learning En + Noi + Nikaido is not.

## Devil Exam: the long-form progression (proposed implementation, lore-based inspiration)
1. Sorcerer achieves deep mastery and specific non-level achievements; obtain an invitation/eligibility and approval from the relevant Devil institution (future systems).
2. Begin tutored multi-part examination: physically taxing training armor, timed stamina/strength goals, survival chores, dangerous but fair creature and traversal trials. No mandatory extreme or inappropriate depiction.
3. A long smoke abstinence phase prevents normal casting, requiring alternate weapon/equipment survival and careful protection from exploits or offline timer farming.
4. Resolve final transformation/failed trials with player-facing warnings, choices and irreversible/reversible server configuration. Devils must remain playable under explicit multiplayer/PvP balance rather than literal invulnerability or unrestricted reality-warping.
5. The result is not a simple "level 100 = Devil" unlock; multiple systems and decisions matter. Human-origin characters do NOT naturally transform by this route.
6. Chidaruma is a unique canonical exception; do not offer his lore-level power to ordinary player starters.

## Design depth, balance and multiplayer
- Human can match player *usefulness* at endgame with specialized resistance, materials, surgeries, medicine, tactics, gadgets, vehicles/transport (if eventually supported), traversal and team roles; do not falsely describe humans as supernatural equals.
- Sorcerers have meaningful low-output starts and high-output aspirational builds. Tradeoffs include costly magic use, rain exposure (future hazards), specific innate weaknesses and scarcity.
- Resource, timer, cooldown, rank, character-choice and talent ownership must be server-owned, saved and migrated across Minecraft updates. Do not copy Tensura's EP formulas or race assets.
- Establish distinct difficulty/rarity for magic specialty families; all require unique particles, effects, tools, inventory visuals, action controls, progression rewards, tooltips and test cases.
- Critically: Existing visual/worldgen acceptance comes FIRST. Do not prematurely implement hazards, Cross-Eyes camps, deep villagers/factions or all magic powers until current city architecture passes user review.

## Acceptance gates (all currently not implemented for these new systems)
1. First-join three-card UI renders, two choices functional, Devil visibly gated, persistence works for two clients and dedicated server.
2. Human produces zero smoke and has useful non-magic skill path; Sorcerer is assigned exactly one persistent specialty.
3. Smoke output, capacity, cost, cooldown and mastery vary independently and survive all reload/death/reconnect flows.
4. Normal Sorcerer cannot use unrelated specialties; rare world-warping powers are constrained by configuration and server safety.
5. Devil Ascension progression is a real multi-stage quest/trial with consequences and exploits tested, not a checkbox or free starting choice.
6. No duplicate starter items, no client stat inflation, no incompatible server/common class loading, and no loss of old world IDs.
7. Never claim these systems are in the shipped JAR until implemented and tested.
