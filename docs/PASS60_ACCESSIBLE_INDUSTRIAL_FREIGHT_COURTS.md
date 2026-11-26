# Pass 60 — Clear Industrial Freight Entrances and Linked Service Courts

**Date:** November 2, 2026  
**Status:** SOURCE-ONLY; **NOT** compiled against NeoForge or tested in Minecraft.  
**Fresh-world setting:** `world_detail_version: 43` (prior: v42).

## Why this exists

Passes 56–59 created detached workrooms, three-block workshop–hall connections,
low attached industrial bays and south-facing side-workshop service courts.
The main factory/warehouse still had a large south-facing, 3-block-wide cargo
entrance, but later yard detail passes could obstruct the **external** approach
from that cargo entrance to the public lane. Fixing actual access is more useful
than adding yet another warehouse or more roof props.

## Production changes

1. New `FreightApproaches60.java`, called **after** world and urban yard
   decoration and after Pass59's court: the loader's south door receives a
   3-wide, 4-block-high clear stone-paved approach out to the lot frontage.
   One inner threshold row clears cargo clutter while preserving the door
   blocks themselves.
2. On industrial properties with a genuine 3-block southern setback,
   join the workshop's existing paved service route to the main hall's
   loading apron with another three-block-wide east–west route. If the
   frontage is narrow, skip this optional connector rather than carve through
   the factory wall or create a one-block token passage.
3. No changes to lot choice, rectangular parcel limits, the main city road
   network, tram tracks, canals, sewers, caves, snow/desert/mountain climates,
   lantern count, world spawn, loot generation or items.
4. `Canvas.hasFreightApproaches60()` gates *all* new writes to v43+; old
   worlds saved as v42 and earlier remain on their original code path.
   `CitySettings` accepts versions 0–43, the codec still defaults absent detail
   to 0, and only the new-world preset moves to 43.

## Verified in this environment

- `python3 tools/qa-pass60-source.py`: 27/27 integration assertions.
- `bash tools/qa-pass60-freight.sh`: Compiled the actual Pass60 and Pass59
  geometry classes with **Java 21 + deliberately minimal Minecraft stubs**;
  **8,200 lots**, **8,200 loading aprons**, **742 optional linked courts**,
  **31,758 checked loading floor cells**, **222 chunk-split comparisons**,
  **1,172 unchanged v42 scenarios**, **4,100 negative-coordinate examples**,
  **291 combined Pass59/60 yard checks** (including retained workstations),
  and **600 rejected undersized lots**.
- `bash tools/qa-pass60-all.sh` reached and passed the Pass59→Pass44 offline
  regression sequence, but the monolithic chain **timed out** while entering
  the older Pass43 placement test. Both remaining tails were explicitly run:
  `bash tools/qa-pass43-placement.sh` and `bash tools/qa-pass42-region.sh`,
  each passed. Logs in the delivery accompany this report when available.
- The newer tests and the older source/Java geometry suites assert deterministic
  parcel ownership and chunk-clipped placement, **not** actual world loading,
  pathfinding, mob spawning, block-state validity or visual appearance.

## Wednesday, November 4 — Claude handoff

**User decided:** Claude handles official Gradle wrapper restoration,
`runData`, full NeoForge compile, and client/server testing after its tokens
refresh. Do not claim these jobs have already been completed.

1. Start from `Dorohedoro_Pass60_CONSOLIDATED_Source_UNCOMPILED.zip`.
2. Restore the *official checksum-verified* Gradle wrapper JAR. Then run
   `./gradlew runData`, `./gradlew build`, `./gradlew runClient`,
   `./gradlew runServer` and GameTests where available. Fix actual API and
   registry failures; **do not** merely adjust a source checker to hide them.
3. Test fresh v43 worlds in all seed-variable biomes: snowy core, dust city,
   iron badlands, dead taiga, mountain and mixed-climate Hole. Confirm natural
   vanilla mining, cave access, trees/logs, and survival gameplay are intact.
4. Walk a **factory loading apron** and the **side workshop courtyard** in
   both narrow-frontage and deep-frontage industrial lots; verify the doors,
   accessible entrances, workshop stations, cargo aisle, and roof ladders.
5. Verify mob spawning at night, sparse streetlights, finite performance,
   old save compatibility (especially v42), and screenshot the city against
   actual Dorohedoro architecture, not invented visual mocks.
6. Prioritize genuine architecture, streets, irregular skyline, meaningful
   interiors and recognizable districts. No deferred Ash-Snow, Sludge-Ice,
   toxic snowballs, Cross-Eye camps, or villager/faction overhaul until user
   approves world visual quality.

## Not yet verified

- Missing `gradle/wrapper/gradle-wrapper.jar` still blocks a full Gradle build
  in this environment. No installable mod JAR is provided by this pass.
- The test fixture models block writes and version gates using lightweight
  stand-ins; it does not simulate real Minecraft terrain physics or renderer.
- **Adjacent-parcel actual shared-wall construction** remains a separate,
  higher-risk issue. Pass60 improves linked circulation *within one lot*, not
  property-spanning bridges. The current design preserves the 3-wide public
  alleys rather than crossing into another owner's lot.
