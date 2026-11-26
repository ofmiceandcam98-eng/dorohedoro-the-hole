# Pass 19.3 — Automatic Sorcerer innate magic (full source)

## BASELINE
- Extracted and preserved user-provided `Dorohedoro_The_Hole_Pass18_Fixed_Source.zip` as canonical compiler-known baseline.
- Selectively merged Pass 19 lore architecture/lantern/materials and Pass 19.2 character code and docs.
- Kept fixed source `build.gradle`, settings, `PortalCoreBlock` codec and other unrelated source intact.
- Includes the earlier portal landing-height correction while preserving the working portal parser.

## CHARACTER FLOW
1. First join: Human or Sorcerer. Devil is locked ascension.
2. Sorcerer: server creates one session with 8 distinct random questions from 12-question bank, in random order.
3. Four answers per question, hidden affinity scores (+3 primary, +1 secondary). No gender input.
4. At end the server recomputes points and ASSIGNS a single innate magic automatically. Only score-tied maxima are randomly resolved; rare specialties have lighter tie weights.
5. Result is saved as `CharacterProfile`, with question IDs and answers, and cannot be overridden by the client. No direct magic choice, accept/reject, reroll, or optional Fate mode.
6. Human receives no magic; later-game Devil evolution is separate.

## SAFETY
- Network payload sends ONLY origin and answer digits; never a requested magic type.
- Server stores the question order in a UUID-keyed session; validates responses and recomputes the outcome; session removed on completion/logout.
- Existing registry IDs and save codec fields retained for compatibility. Existing selected players are never forced to redo origin selection.

## QA HONESTY
- Standalone Java 21 quiz tests and JSON checks run as separate checks. These do not prove an actual NeoForge client or dedicated server works.
- User-supplied Gradle 9.2.1 binary is locally available; plugins/dependencies may still be uncached in this offline environment.
- Do not publish untested mod code as a working CurseForge release. Back up saves before changing your mod.

## ORDER
Continue visual city quality improvements first; deferred hazards, faction/villager overhaul are not reactivated by this quiz work.
