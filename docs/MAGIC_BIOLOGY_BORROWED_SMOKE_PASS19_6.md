# Pass 19.6 — Biological magic and exceptional access (Source ONLY)

Status: **NOT COMPILED / NOT TESTED IN MINECRAFT**. This package was produced without Gradle or game execution. Source and asset syntax checks are not runtime verification.

## Immutable first principle
An ordinary Sorcerer is born with **one** innate magic type, produced by the Devil Tumor and smoke veins. `CharacterProfile.magic` is the only natural type. XP, rank, practice, mastery and loadouts NEVER change it. Human characters have none. Personality test or Fate roll determines the type at creation; ties allow choosing only among tied results.

## Smoke Flask MVP — IMPLEMENTED IN SOURCE (NOT COMPILED)
* Craft two `empty_smoke_flask` items from a glass bottle, a smoke condenser and an iron nugget.
* A Sorcerer right-clicks an empty flask to spend 25 current smoke and capture a **fixed** sample of their innate type. A Human cannot produce/capture smoke.
* A player of any origin can right-click the filled flask to release its single pre-recorded **rank-one** effect; if the effect requires a visible hostile entity and there is no valid target, it does not consume a charge.
* Releasing a flask consumes one filled item, returns one empty flask, and gives the used flask type a 100-tick item cooldown.
* Stored smoke **never** calls the normal learned/equip pathway, changes innate type, grants mastery, trains another type, or draws from the user's smoke pool. It uses an independent `releaseBottledSmoke` entry point.
* It currently reuses existing rank-one prototype effects for eight specialties, not all canonical behavior. For example the current Curse rank-one effect is an illustrative temporary debuff, **not** Risu's canonical death-triggered Curse.
* This is an initial prototype. Source-level wiring and JSON validation are done; gameplay has **not** been verified.

## Lore exception system — DESIGNED, NOT IMPLEMENTED
1. **Black Powder:** extracted smoke essence from Devil Tumors. Scarce Cross-Eyes economy, powerful but temporary output multiplier, dependency risk/withdrawal and other drawbacks. Some users otherwise unable to cast can manifest a transient effect. Never a permanent second innate type. Do NOT implement via generic crafting or create infinite supply.
2. **Experimental tumor grafts:** dangerous surgery, extremely rare endgame quest path and faction consequences. Separate list of grafted access from natural innate type; limiting graft slots is a GAME BALANCE choice, **not** a canonical nine-tumor ceiling. Consent/permission and configurable PvP/world server rules required.
3. **Devil Exam:** an earned long-term milestone involving tutor approval, heavy armor training, dangerous trials, smoke abstinence, transformation and extraordinarily strong Devil abilities. Devils may transcend normal specialty restrictions, but this is not a routine multi-class unlock. Build only after core magic and survival are functional.
4. **Artificial Human-to-Sorcerer surgery:** possible as exceptional lore-based medical transformation, not a new character-creation shortcut; distinct from smoke bottles and grafts.

## Guardrails and sequencing
- Cast, equip and study original Smoke Skills only when `SmokeSkill.magic == innate(player)`.
- Any borrowed effect must carry its explicit source (flask, powder or graft) and never update natural character identity.
- Never allow a client packet to change biological magic.
- Smoke skill cap goal **12–20 active techniques per type** plus meaningful passives, progression and noncombat applications. Currently only 3/type are implemented.
- Preserve the existing first-join quiz/fate and world generation, and preserve the corrected portal offset in `PortalService.Frame.stand`.
- Priority: complete believable, reliable magic, then return to improving The Hole's architecture before the deferred faction/villager/hazard features.

## Lore references
- https://dorohedoro.fandom.com/wiki/Magic
- https://dorohedoro.fandom.com/wiki/Kai
- https://dorohedoro.fandom.com/wiki/Smoke_Flask
- https://dorohedoro.fandom.com/wiki/Black_Powder
- https://dorohedoro.fandom.com/wiki/Devil_Exam
- https://dorohedoro.fandom.com/wiki/Devils

Original pixel art included for flask icons; no proprietary wiki/anime images imported.
