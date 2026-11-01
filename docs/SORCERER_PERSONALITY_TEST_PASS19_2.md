# Pass 19.2 — Sorcerer personality quiz (source implementation)

**Minecraft:** 1.21.1 / NeoForge 21.1.265 / Java 21

## Flow

1. On first server login when a character has not been selected, the server requests an origin selection screen.
2. The player chooses Human (direct confirmation) or Sorcerer (personality test). Devil is displayed but locked as a future ascension path.
3. Sorcerer answers 12 questions, each with 4 answers, and can revisit earlier answers.
4. The client previews one recommendation based on +3 primary / +1 secondary scoring, stable tie order.
5. The player chooses **Accept Recommendation**, **Choose My Own Magic**, or **Trust Fate** (weighted roll).
6. The server independently validates answers, recalculates the result, and saves immutable character origin and specialty via a player attachment that persists across death.
7. The server replies with accepted identity; first-join selection does not repeat for completed profiles.

## Distinct systems: implemented vs deferred

**Implemented in source (not compiled/run-tested):** first-login server event, GUI, scoring, 8 magic entries, result modes, server-side validation and save attachment.
**Deferred:** functional spells, smoke meter, skill trees, mastery, human talents, mysterious diagnosis quest, devil transformation, server policy configuration and future source migration of player profiles.
**Important:** the current result UI reveals the selected type; an “Unidentified Smoke” gameplay reveal is a planned system and **not implemented**. A `smokeIdentified` field is reserved, currently false, for future use.

## Implementation files

- `src/main/java/com/dorohedoro/hole/character/MagicType.java`
- `src/main/java/com/dorohedoro/hole/character/PersonalityQuiz.java`
- `src/main/java/com/dorohedoro/hole/character/CharacterProfile.java`
- `src/main/java/com/dorohedoro/hole/character/CharacterAttachments.java`
- `src/main/java/com/dorohedoro/hole/character/CharacterNetwork.java`
- `src/main/java/com/dorohedoro/hole/character/CharacterPlayerEvents.java`
- `src/main/java/com/dorohedoro/hole/client/character/CharacterCreationScreen.java`
- modified `src/main/java/com/dorohedoro/hole/Dorohedoro.java`
- this markdown file, machine-readable map JSON and standalone HTML preview.

## Full question-to-magic mapping

### Q01 — A stranger collapses outside your door. You...

A. Treat their injuries before asking questions. — **HEALING +3**, PROTECTION +1
B. Stand guard so nobody can hurt them. — **PROTECTION +3**, HEALING +1
C. Track the attacker and expose their weakness. — **DISMEMBERMENT +3**, CURSE +1
D. Make the attacker regret returning. — **CURSE +3**, DESTRUCTION +1

### Q02 — A sealed passage blocks your way. You...

A. Grow something through the cracks. — **MUSHROOM +3**, CURSE +1
B. Find a shape that can squeeze inside. — **REPTILE +3**, DISMEMBERMENT +1
C. Break down the obstruction. — **DESTRUCTION +3**, MUSHROOM +1
D. Study how it might have opened before. — **TIME +3**, PROTECTION +1

### Q03 — A creature lunges at your friend. You...

A. Create a barrier between them. — **PROTECTION +3**, HEALING +1
B. Punish it with a lasting affliction. — **CURSE +3**, TIME +1
C. Strike hard to end the threat quickly. — **DESTRUCTION +3**, DISMEMBERMENT +1
D. Disable its limbs with precision. — **DISMEMBERMENT +3**, PROTECTION +1

### Q04 — You could undo one terrible event. You...

A. Heal those who still suffer. — **HEALING +3**, PROTECTION +1
B. Turn back the critical moment. — **TIME +3**, CURSE +1
C. Protect others from a repeat. — **PROTECTION +3**, TIME +1
D. Let new life reclaim what was lost. — **MUSHROOM +3**, HEALING +1

### Q05 — You are trapped underground. You...

A. Adapt your body to find an escape route. — **REPTILE +3**, PROTECTION +1
B. Cultivate something that lights your path. — **MUSHROOM +3**, HEALING +1
C. Force open a new exit. — **DESTRUCTION +3**, REPTILE +1
D. Wait and study the rhythm of the tunnels. — **TIME +3**, DISMEMBERMENT +1

### Q06 — In a dangerous fight, you trust...

A. Clean, exact strikes. — **DISMEMBERMENT +3**, REPTILE +1
B. Overwhelming pressure. — **DESTRUCTION +3**, CURSE +1
C. A consequence your enemy cannot ignore. — **CURSE +3**, MUSHROOM +1
D. Staying safe until a chance appears. — **PROTECTION +3**, TIME +1

### Q07 — An unfamiliar sickness spreads. You...

A. Care for the sick and search for treatment. — **HEALING +3**, MUSHROOM +1
B. Study what is growing inside the victims. — **MUSHROOM +3**, HEALING +1
C. Trace the earliest cases and their timing. — **TIME +3**, DISMEMBERMENT +1
D. Find the source and seal it away. — **CURSE +3**, PROTECTION +1

### Q08 — You are offered a new body. You...

A. Accept a stronger, stranger form. — **REPTILE +3**, DESTRUCTION +1
B. Choose precise control over every movement. — **DISMEMBERMENT +3**, REPTILE +1
C. Keep your body and gain a protective shell. — **PROTECTION +3**, HEALING +1
D. Restore your body to its healthiest state. — **HEALING +3**, TIME +1

### Q09 — Black smoke gathers around your hands. You...

A. Shape it into something that can spread. — **MUSHROOM +3**, CURSE +1
B. Let it reshape you. — **REPTILE +3**, HEALING +1
C. Launch it forward as a weapon. — **DESTRUCTION +3**, DISMEMBERMENT +1
D. Try to suspend one moment within it. — **TIME +3**, PROTECTION +1

### Q10 — Someone threatens the people you love. You...

A. Shield them, even if you must stand alone. — **PROTECTION +3**, REPTILE +1
B. Restore anyone who is hurt. — **HEALING +3**, MUSHROOM +1
C. Make the threat carry a terrible mark. — **CURSE +3**, DESTRUCTION +1
D. End the threat with exact force. — **DISMEMBERMENT +3**, TIME +1

### Q11 — In an abandoned factory, you notice...

A. Fungi growing in the damp machinery. — **MUSHROOM +3**, HEALING +1
B. A machine that could change your body. — **REPTILE +3**, DISMEMBERMENT +1
C. The energy still trapped in its engines. — **DESTRUCTION +3**, PROTECTION +1
D. Clocks frozen at the same instant. — **TIME +3**, CURSE +1

### Q12 — What kind of mastery appeals to you?

A. Understanding where to separate and repair. — **DISMEMBERMENT +3**, HEALING +1
B. The ability to rewrite a lasting consequence. — **CURSE +3**, TIME +1
C. The discipline to protect without retreat. — **PROTECTION +3**, DESTRUCTION +1
D. The power to heal what others cannot. — **HEALING +3**, MUSHROOM +1

## Fate formula

For each magic type: `weight = (2 + aptitude_score) * base_magic_weight`.
Weights: MUSHROOM=3, REPTILE=3, DISMEMBERMENT=3, HEALING=4, CURSE=2, PROTECTION=4, TIME=1, DESTRUCTION=4.
The server performs the random draw. These are game-balance weights, not canonical birth rates.

## Verification and limitations

- All 12 questions/48 answers and primary + secondary associations structurally verified.
- Pure Java scoring classes compiled and executed using Java 21. All eight types reachable as top result via valid answer paths.
- Complete NeoForge build **blocked** because Gradle plugin `org.gradle.toolchains.foojay-resolver-convention:1.0.0` is not available in this environment offline. User-supplied Gradle 9.2.1 binary is present, but does not itself include this plugin.
- The requested `Dorohedoro_The_Hole_Pass18_Fixed_Source.zip` was **not available** in files or mounted runtime; this full source is based on the existing Pass 19.1 checkpoint and has **not** been merged into a known-compiling fixed baseline. Do not release it as a verified JAR.
- No in-game client/server, death persistence, login-loop, or GUI scale test has been performed.
- Keep mod ID `dorohedoro` unchanged to preserve registry compatibility even if the public project title changes.

## Required manual game tests

1. Fresh local world and dedicated-server login: first-join UI appears and both paths operate.
2. Complete all 12 questions and see correct top magic recommendation.
3. Test all three outcome modes and each of eight magic choices.
4. Attempt incomplete/invalid/spoofed packets; server should refuse them.
5. Quit, rejoin, die, respawn, and cross dimensions; selection does not repeat.
6. Test narrow GUI scales, multiplayer simultaneous selections, disconnect during quiz, and plugin loading on a headless server.
7. Recheck compatibility against the user’s working Fixed Source checkpoint before deployment.
