# The Hole — District Identity Pass 18

**Status:** source-integrated only. Static/source review complete in this environment; full NeoForge compilation and in-game verification still pending.

## Intent

This pass continues the user-approved priority: make each district visually distinctive before adding Ash-Snow, Sludge-Ice, toxic snowballs, environmental poisoning, Cross-Eye camps, or the larger faction/villager overhaul.

## What changed in new worlds

- `world_detail_version: 7` for the default normal preset.
- Added **district-signature landmark lots** on a subset of landmark regions while preserving the existing citywide hospital/department-store/research-manor/refuse rotation elsewhere.
- New district landmark lot types:
  - `COMMUNAL_COURT` (residential)
  - `BAZAAR` (market)
  - `MACHINE_YARD` (industrial)
  - `CANAL_DEPOT` (waterfront)
  - `RUIN_COURT` (derelict)
  - `BURIAL_GROUND` (graveyard)
  - `FRINGE_YARD` (outskirts)
- Stronger district ground palettes for lots.
- Stronger district street materials and edge fixtures so roads/squares read differently between neighborhoods.
- `BAZAAR` also gets a deterministic villager population hook like other civic/market lots.

## Design intent per district

- **Residential:** shared communal courts and softer mixed paving.
- **Market:** denser bazaar courts with awnings, packed mud/brick paving.
- **Industrial:** machine yards, gantries, darker service paving and heavier edge furniture.
- **Waterfront:** canal depot blocks with dock timbers, water basins and cargo storage.
- **Derelict:** ruin courts and rougher broken surfaces.
- **Graveyard:** burial grounds and more stone/funerary street cues.
- **Outskirts:** fringe compounds mixing subsistence farming, animal space and salvage.

## Required acceptance checks

1. Generate fresh worlds and travel beyond spawn: confirm districts are recognizable from **street materials and landmark blocks**, not just building type mix.
2. Specifically locate at least one each of: Bazaar, Machine Yard, Canal Depot, Ruin Court, Burial Ground, Fringe Yard, Communal Court.
3. Verify these signature lots remain navigable, chunk-order independent, and do not block arteries/canals/tram corridors.
4. Confirm older worlds at detail versions 0–6 keep their previous layouts.
5. Re-run the existing city/interior/sewer checks plus new in-game screenshot review before claiming completion.
