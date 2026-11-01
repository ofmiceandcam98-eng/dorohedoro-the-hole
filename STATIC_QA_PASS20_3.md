# Pass 20.3 — Static QA report

**NO COMPILATION, NO GRADLE, NO MINECRAFT TESTING.**

Source-level checks: 13/13 passed by `tools/qa-magic-temporality-pass20-3.py`.

- All new Protection barriers tracked with finite expiry, including at highest mastery.
- Finite Time fields remain governed by expiry checks.
- Explicit server-side Dismemberment injury cure removes persistent NBT state.
- Ordinary cleanse does not remove a still-active Dismemberment injury.
- Group and targeted Healing may cure other players.
- `Purge Barrier` no longer cleanses injuries.
- 64 technique IDs and 3-slot × 9-preset system preserved.
- `PortalService.Frame.stand()` corrected Y offset preserved.
- 383 JSON resources parsed.
- No `build` or `.gradle` directories in the project.

## Known limitations

- Untracked smoke barriers left behind by Pass 20.2 cannot be automatically identified as magic-created (they are plain blocks). Remove any old ones manually; new casts expire.
- Curing Dismemberment clears vanilla Slowness/Weakness even if an unrelated effect overlaps. Source-level provenance will need a dedicated status effect in a later pass.
- Dismemberment injury itself still expires after the original limited duration unless healed sooner; no persistent anatomical injury model is currently implemented.
- Functional and dedicated-server checks remain pending.
