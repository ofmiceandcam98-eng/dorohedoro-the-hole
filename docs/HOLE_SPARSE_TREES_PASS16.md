# The Hole — sparse trees and canopy realism (Pass 16)

**Status:** Implemented in source for new worlds; offline checks only. No NeoForge compile or visual Minecraft test yet.

## Canon and aesthetics

The Hole is a surface city under oppressive industrial pollution and residual sorcerer smoke, not a subterranean city. Outdoor trees should generally be dead, skeletal, or severely stunted. Never introduce a forest, green boulevard, or bright healthy park in the Hole. The Sorcerers’ World has its own distinct forests. The dense client smog remains a rendering treatment and should not literally roof over the world or remove vanilla rain/day-night mechanics.

## Generation

- **Fresh worlds** use `world_detail_version = 5` and receive rare dead trunks in abandoned parks/lots and rarer crooked/stunted survivors, with small sparse crowns. **One tree at most per park lot**. No outdoor trees on paved streets.
- Rough intended selection for eligible *park lots*: 0.8–12% barely living, 26–54% dead, remainder empty. These are per eligible lot, not percentages of the city. Vacant sites have much lower frequency.
- Empty parks are gravel/podzol/coarse-dirt fields with occasional dead scrub, not green lawns.
- **Nurseries:** supply protected saplings behind glass, but no rows of full-grown healthy open-air trees in new worlds; scavenged timber in junkyards and existing city structures remains available for standard survival crafting. A roof shelter is only over the nursery plants, not the city.
- **Legacy saves:** `world_detail_version` 0–4 have their previous vegetation rules, avoiding mismatched chunk edges. Do not upgrade a live world’s worldgen version.
- Trunks, leaves, saplings use standard Minecraft blocks to support axes, wood crafting, farming and tree propagation.
- Caves, ore distributions, sewers and natural deep mining are unchanged.

## Tests to do in a real Minecraft new world

1. Fly ~1 km through market, industrial, derelict and outskirts: no rows of green trees; only isolated dead or struggling specimens.
2. Inspect parks/vacant/rubble: no floating trunks, leaves replacing the central trunk, or branches on roads or through ruins.
3. Check sapling shelter and nursery chest; break existing logs, obtain saplings, grow vanilla trees by player choice, craft planks and basic survival tools.
4. Verify nursery glass canopy, saplings and end of aisle remain accessible; no hostile mob spawn bypass through roof.
5. Verify rain, smog, sky suppression, survival daylight and natural crop growth all behave as intended; no claim of canonical absolute darkness.
6. Check cross-chunk boundaries and fresh-vs-old world compatibility, generated tree placement vs street/courtyard roads and performance.
7. Confirm the Sorcerers’ World remains forested as independently intended; this scarcity applies to the Hole alone.

## Offline tests

- `python tools/qa-hole-trees-pass16.py`
- `javac` + `java` on `HoleTreePolicy` with `CityHash` and test enum stand-ins, with deterministic seeded counts.
- All earlier structural QA and Java syntax parse.

**Not complete until full NeoForge build, new-world city rendering, wood/sapling access and survival test pass.**
