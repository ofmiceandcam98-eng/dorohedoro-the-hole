# Pass 18 hotfix: Artificial Door return-door placement — 2026-10-31

## Player-observed symptom

The user built and right-clicked an Artificial Door and received:

> No safe unoccupied location found for the return door.

This message originates in `PortalService.activate()` **after** the source frame is validated and the target dimension exists. The screenshot is therefore evidence that the user did not simply assemble the source frame incorrectly.

## Verified source defect

`findSafeSite()` takes the height from `Heightmap.Types.MOTION_BLOCKING_NO_LEAVES`, which is the **first open block above the surface**, and constructs the generated return frame with that height as its bottom row. The older `Frame.stand()` added +1 Y to the bottom-row height. `validLanding()` then required a sturdy block at the position under the player's feet. That position was the *first air block*, so even perfectly flat unobstructed terrain was rejected.

**Fix:** `Frame.stand()` now places player feet at the same Y as the core/bottom frame row (outside the portal plane), so the block below can be the solid ground returned by the heightmap. Applies to both `Direction.Axis.X` and `Direction.Axis.Z` and to both Artificial and Infernal Doors because the code is shared. No materials, recipes, worldgen versions, structures or dimensions were changed.

## QA done / not done

- `python tools/qa-portal-landing-pass18.py` guards the coordinate contract and preservation of safety checks.
- The original return-door code otherwise remains unaltered. No forced destination clearing, bypassed safety check, or chunk overwrite.
- Full NeoForge build, live destination generation and round-trip portal gameplay are **NOT verified** by this source fix. Claude must run them. Existing live worlds need a new compiled JAR for this code correction; do not change their saved worldgen version.

## Claude: mandatory tests

1. Recompile the updated source and replace ONLY the matching mod JAR in the user's test modpack. Do not duplicate old/new mod JARs. Back up saves first.
2. Build the 4-wide × 5-high Artificial Door with the core at bottom row index 1, press Door Activation Tool. Check same message no longer appears, and a *generated* return frame is built in Sorcerers' World.
3. Walk through the portal, verify safe floor at arrival, ability to enter return doorway without collision/fall damage, and round trip. Check both X and Z alignments and three fresh seeds.
4. Verify Hell 5×5 gates also work; valid and obstructed destinations, and save/reload persistence. Confirm no block overwrite or forced relocation into protected structures.
5. If it still says 'No safe unoccupied location', collect server logs, destination coordinate/heightmap, counts of rejection reasons (non-air, unsupported bottom frame, bad landing), and target terrain. **Do not remove safety validation** to conceal the problem. Improve bounded safe-site selection only with evidence.
6. Update tests/handoff with real results and tell the user precisely what is verified vs pending.
