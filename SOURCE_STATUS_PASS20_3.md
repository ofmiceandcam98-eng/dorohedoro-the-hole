# Pass 20.3 — Temporary Support Magic and Dismemberment Healing

Based on **Pass 20.2 complete source**, with narrowly scoped corrections to Healing, Protection and Time lifetimes.

- Protection barriers are **always temporary**, including at maximum mastery. Active barrier expiry is saved with the world.
- Healing spells can actually cure Dismemberment's persisted injury state; selected spells have mastery gates.
- Protection's `Purge Barrier` now grants only temporary protection and does not cleanse injuries or poison.
- Time effect fields are retained as short-lived casts, never permanent.
- 64 technique definitions remain. Loadouts still use 3 slots × 9 presets.
- `PortalService.Frame.stand()` landing-height correction is preserved.
- All source provided; no build/Gradle cache artifacts.

**NOT COMPILED. NOT TESTED IN MINECRAFT.** The source checks in `STATIC_QA_PASS20_3.md` do not establish runtime correctness.
