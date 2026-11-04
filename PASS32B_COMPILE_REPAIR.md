# Dorohedoro Pass 32B — Java 21 / NeoForge 1.21.1 compilation fixes

**Starting point:** the user-compiled Pass32A Compact Build Source, tested in GitHub Codespaces on Java 21 with `disable_recompilation=true`.

**Evidence:** 12 compiler errors reported on 2026-11-01 after successful Minecraft artifact generation. The errors are confined to four Java files.

**Changes:**
1. `SewerLeechEntity.java`: remove override of final vanilla `canBreatheUnderwater`; implement NeoForge `canDrownInFluidType` to permit underwater survival while using default behavior in other fluids.
2. `LoreArchitecture19.java`: `Mix.pick` returns `BlockState`, not `Block`; use the correct type.
3. `SmokeVisuals.java`: resolve `SoundEvents.GENERIC_EXPLODE` from `Holder.Reference<SoundEvent>` using `.value()` in both sound switches.
4. `SmokeMeshClient.java`: preserve double-precision `age` for animation calculations; cast once to float (`rotationAge`) for the eight `Axis.rotationDegrees` calls.

No textures, world-generation settings, or mechanics were added. The rest of the source is byte-for-byte identical to Pass32A Compact.

## Codespaces use (from the `pass-32a-build-test` branch)
1. Upload `Dorohedoro_Pass32B_Four_File_Compile_Fix.zip` to your Codespace project folder.
2. `unzip -o Dorohedoro_Pass32B_Four_File_Compile_Fix.zip -d .`
3. Ensure Java 21: `java -version`.
4. Ensure `disable_recompilation=true`: `grep '^disable_recompilation=' gradle.properties`.
5. `bash tools/verify-neoforge-build.sh`
6. Show the `:compileJava`/`:runData` output or paste errors from `build/verification/02-runData.log`.

**QA limits:** Exact source-level checks and archive difference checks passed locally; NeoForge compilation must be rerun in the Codespace to confirm. No claim is made that this completes compilation or gameplay verification.
