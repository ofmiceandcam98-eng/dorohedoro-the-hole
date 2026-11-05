# Sorcerer personality-test QA — Pass 19.2

- 12 questions, each 4 options; 48 distinct responses with 3 primary + 1 secondary points.
- All eight types are selectable by the `choose` mode and have valid 12-answer paths that make them the highest-scoring recommendation.
- Java 21 `javac` compiled `MagicType.java`, `PersonalityQuiz.java` and a standalone `QuizTest` harness successfully.
- Java test output: all eight paths valid, every completed quiz totals 48 points, fate always returns a magic type, malformed 3-character answers rejected.
- UI source contains Origin / Quiz / Recommended / Choose / Fate / Completion stages and an Esc-close guard.
- Network source validates 12 answer digits and recalculates selection on the server, refusing already-selected profiles.
- Player attachment stores origin and chosen magic with persistence and copy-on-death configured.
- HTML preview script validated using `node --check` and is linked separately.

**Limitations:** This is not a NeoForge build, GUI runtime test, dedicated-server test, or real-world save verification. `gradle --offline help` failed while resolving uncached `org.gradle.toolchains.foojay-resolver-convention:1.0.0`, despite the user-supplied Gradle 9.2.1 binary being available. The owner's requested `Dorohedoro_The_Hole_Pass18_Fixed_Source.zip` was not available in our current files, so the full source is built from the previously available Pass 19.1 checkpoint and must be merged into the known-compiling baseline before release.
