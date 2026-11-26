# Pass 19.6 Complete Source — NOT COMPILED

Baseline: Pass 19.5 complete source (itself built from user's Pass 18 Fixed Source). This pass preserves the corrected `PortalService.Frame.stand()` from Pass 19.4.

**NO GRADLE / NO MINECRAFT COMPILE / NO IN-GAME TESTING PERFORMED.** Existing compile blockers remain unresolved; do not claim this works in a public JAR. No build, `.gradle`, `.idea` or compiled artifacts are bundled.

### Code changes
- New `EmptySmokeFlaskItem` and `FilledSmokeFlaskItem` classes.
- `ModItems`: register reusable empty flask and eight fixed-type filled smoke flasks.
- `SmokeEngine`: 25-smoke capture and one-use borrowed effect execution without touching innate profile.
- Nine original hand-drawn 16×16 PNG icons, nine item models and nine translated item names.
- New crafting recipe for empty smoke flask.
- Lore / feature staging specification distinguishing fully designed vs currently implemented source.

### Unimplemented (NOT in this pass)
Black Powder drug system, human smoke production after powder, graft surgery, Devil Exam, new magic types, expanded skill trees, extensive new FX / sounds, smoke marketplace, multiplayer PvP tuning, game compile and runtime acceptance tests.
