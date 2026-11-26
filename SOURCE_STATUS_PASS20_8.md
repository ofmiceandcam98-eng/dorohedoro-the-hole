# Pass 20.8 — Detached Limb Animation & Reptile Regrowth

**NOT COMPILED. NOT TESTED IN MINECRAFT.** No Gradle invocation, Minecraft/NeoForge dependency download, or runtime build attempt.

Complete project based on Pass 20.7. All world generation and established systems preserved.

## Mechanics
- Dismemberment can already remove arms or legs after real damage (Pass 20.7). Now each successful server-side limb removal sends a one-shot cosmetic packet, rendering a 3D detached limb using the affected player's own skin. The limb spins, falls, and dissolves after about 1.8 seconds. There are no loot drops or client-owned injury rolls.
- Advanced Healing limb restoration triggers a server-origin regrowth event. In third person a limb scales up during smoke reconstruction; the vanilla limb is briefly concealed so it doesn't pop into view instantly. The underlying persistent limb mask is always server-owned.
- Reptile has innate **biological supernatural regrowth** without unlocking a second magic type. At 850+ mastery in either Shed Skin or Apex Form, regrowth proceeds naturally but very slowly. From Shed Skin mastery 650, casting Shed Skin accelerates regeneration for 120 seconds; sustained Smoke expenditure and time are still required.
- Server regrowth is gradual (one severed limb per cycle), consumes 2 Smoke per active second, pauses without sufficient Smoke, and saves progress across logouts and death. Regenerated limbs permanently restore normal player anatomy and associated combat/movement stats.
- These mechanics are lore-inspired gameplay extensions. Most living reptiles regrow tails, not full limbs; advanced regrowth is a supernatural Reptile magic feature, not a claim about typical animal biology.

## Approximate biological regrowth timing
- Natural when biological mastery >=850: roughly 4.6–7.5 minutes per limb with sustained Smoke.
- Accelerated when Shed Skin mastery >=650 and the ability is cast: roughly 80–120 seconds per limb with sustained Smoke. If the boost ends, progress is saved and may resume with further casting, or at natural mastery >=850.
- Actual recovery time may be longer if interrupted by casting, depleted Smoke, disconnecting or death.

## Visual limitations
- The detached model is a textured 3D arm or leg, but is not yet a physics-simulated entity or a skeletal animation with joints.
- Vanilla player skin's model UVs are used; slim arms are currently shown using 4-pixel-wide geometry, and armor/other mods' custom player models are not fully supported.
- First-person right/left arms hide during short regrowth; detached limbs appear in world coordinates.
- Visual quality and gameplay timing cannot be confirmed until client and multiplayer testing.

## QA targets on owner PC
1. Build with Java 21 + correct NeoForge 1.21.1 dependencies, runData then build.
2. Test severing in PvP at the documented mastery thresholds; confirm no loss after blocked hits.
3. Watch detached arm and leg from two clients, including a late-joining spectator; verify no false replays.
4. Confirm a Human cannot initiate reptilian regrowth and a Sorcerer of another type cannot access it.
5. Test Reptile Shed Skin mastery 649 vs 650, natural mastery 849 vs 850, Smoke depletion and persistence through logout/death.
6. Confirm group/targeted Healing still regrows multiple limbs properly and restores model, HUD and stats.
7. Inspect different skins, slim models, armor and Reptile form compatibility; verify no permanent render-state hiding.
8. Confirm server-side portal stand() landing and structural PvP are untouched.

**No build or .gradle outputs included.**
