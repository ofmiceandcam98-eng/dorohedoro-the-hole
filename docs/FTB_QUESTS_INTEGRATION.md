# FTB integration

Added in Pass 78 (2026-11-25). The mod runs with or without any FTB mod installed; nothing here is required.

## Supported versions (tested in the real client)

| Mod | Owner's listed versions | Official Dorohedoro Modpack ("das" instance) |
|---|---|---|
| FTB Library | 2101.1.33 | 2101.1.40 |
| FTB Teams | 2101.1.10 | 2101.1.12 |
| FTB Quests | 2101.1.27 | 2101.1.39 |
| FTB Chunks | 2101.1.20 | 2101.1.24 |
| FTB Ultimine | 2101.1.15 | not in the pack |
| FTB XMod Compat | 21.1.9 | not in the pack |
| FTB Essentials | 2101.1.10 | not in the pack |
| FTB Filter System | 21.1.4 | not in the pack |
| PlayerNBT Quests | 1.0.1.1 | not in the pack |
| Architectury | 13.0.8 | 13.0.12 |

Results are in `docs/FTB_QUESTS_TEST_RESULTS.md`. Run the test with `bash tools/ftb-client.sh <folder with the jars>`
(a CurseForge instance's `mods` folder works). FTB jars are copied into `runs/` only and are never committed.

## What the integration does

### FTB Quests: the optional Dorohedoro quest book

- **30 quests in 4 chapters** under one chapter group, "Dorohedoro: The Hole":
  - Welcome to the Hole (7)
  - Under the Streets (8)
  - Doors Between Worlds (8)
  - Hell (7)
- **A guide only.** No quest gates a recipe, block, dimension, door, trade or ability. Two quests are marked optional:
  "First Technique" (sorcerers) and "Back in One Piece" (limb reattachment).
- **Source:** `tools/ftbquests/gen-quests.mjs` generates the SNBT into
  `src/main/resources/dorohedoro_compat/ftbquests/quests/`. Edit the script and run `node tools/ftbquests/gen-quests.mjs`;
  never hand-edit the SNBT.
- **Stable IDs:** every object ID starts with `0D0E0B0E`, then chapter, quest and kind. Never renumber a quest that has
  shipped, or players lose progress.
- **Real IDs only.** Every task and reward uses an item, entity or advancement that exists in this mod or vanilla.
- **Rewards are modest:** food, torches, a few iron or copper ingots, saplings, experience. Nothing makes mining or
  trading pointless, and there are no repeatable loops.

### Credit for things done before opening the book

- **Item tasks** look at the inventory, so a player who already holds the item gets credit.
- **Events only the mod can see** are hidden advancements in `data/dorohedoro/advancement/`, granted from the mod's own
  code at the moment they really happen (`progress/HoleAdvancements`):

  | Advancement | Granted when |
  |---|---|
  | `origin/chosen`, `origin/human`, `origin/sorcerer` | The origin is chosen; also on login for existing characters |
  | `magic/first_technique` | A technique is successfully cast |
  | `medicine/limb_reattached` | A reattachment succeeds (operator and patient) |
  | `doors/artificial_door_opened`, `doors/devil_gate_opened` | A real frame is activated and its far end is built. Holding a core does not count |
  | `doors/traveled_by_door` | The player crosses through any door |
  | `realms/returned_from_hell` | The player walks back through a Devil Gate alive (dying and respawning does not count) |
  | `realms/entered_sorcerers_world`, `realms/entered_hell` | Vanilla `changed_dimension` trigger |
  | `realms/hydra_forest`, `realms/hell_horns`, `hole/undercity` | Vanilla `location` trigger with the biome |

  They have no display, so they never show a toast or an advancement tab. FTB's advancement task re-checks them every
  few seconds.
- **FTB's flexible mode:** the book uses `progression_mode: "flexible"`. A quest whose prerequisites are unfinished still
  records its task progress. The quest completes on its own as soon as the prerequisites do. The test proves this: the
  door is opened and used before the quests leading up to it, and both door quests complete once those are done.
- **Kill tasks** only count kills made while the book is loaded. FTB has no history of past kills. Only one quest uses
  a kill task: "Pest Control", 5 Sewer Roaches.

### Installing the book

FTB Quests reads quests only from `config/ftbquests/quests/`, never from inside a mod jar. There are two ways in:

1. **Modpack (recommended):** copy `src/main/resources/dorohedoro_compat/ftbquests/quests/` into the pack's
   `overrides/config/ftbquests/quests/`. Players get the book with the pack, on clients and servers alike.
2. **In game:** an operator runs `/dorohedoro quests install`. It only works when FTB Quests is installed
   (`ModList.isLoaded("ftbquests")`). It then reloads the book with `/ftbquests reload`.
   - **No book yet:** the whole Dorohedoro book is written, including its `data.snbt`.
   - **A pack already has a book:** only the Dorohedoro chapter group, its four chapters and its translation lines are
     added. The pack's own chapters and settings are untouched. `chapter_groups.snbt` and `lang/en_us.snbt` are backed up
     to `*.before-dorohedoro` first.
   - **Running it again** updates the Dorohedoro chapters in place. IDs never change, so progress is kept.

On a dedicated server, run the command on the server (or put the files in the server's `config/ftbquests/quests/`).
Clients receive the book from the server.

### Teams and rewards

FTB Teams decides who shares progress. Quest progress belongs to the team; `default_reward_team: false` means each
member claims their own rewards once. FTB Quests itself prevents double claims and keeps progress across restarts.
Nothing in this mod changes FTB's team or reward handling.

### PlayerNBT Quests

The addon loads alongside the mod without problems, but the book doesn't use it. The hidden advancements above cover
the same needs more cheaply. A pack author can still use it: character data lives in the player NBT under
`neoforge:attachments` → `dorohedoro:character_profile` (`origin`, `magic`). That key is not one the addon reads cheaply,
so it serializes the whole player on every check; keep `checkInterval` high (200 ticks or more).

### FTB Ultimine

- The ores carry the convention tags `c:ores`, `c:ores/sulfur`, `c:ores/salt`, `c:ores/zinc`, `c:ores/oil_shale`,
  `c:ores_in_ground/stone` and `c:ores_in_ground/deepslate` (blocks and items). Ultimine's default `merge_tags`
  (`c:ores/*`) therefore mines a vein that crosses from stone into deepslate in one go. Every other mod block is mined
  block-for-block like any vanilla block.
- **Ultimine's own rule:** in survival it never breaks a block harder than the first one mined. Deepslate ores are
  harder than stone ores, exactly as with vanilla iron or copper. Starting a mixed vein on a stone ore leaves the
  deepslate part, while starting on a deepslate ore takes the whole vein. This is FTB's design, not a bug in the mod.
- `c:raw_materials/zinc`, `c:ingots/zinc` and `c:ingots/brass` are also tagged, for recipe viewers and other mods.

### FTB Chunks

- Every block the mod places for a player is first offered to claim mods through NeoForge's place event
  (`world/BlockProtection`). That covers Mushroom growth, Protection barriers and the far end of a door. Inside someone
  else's claim nothing is placed.
- Destruction magic uses vanilla explosions, which FTB Chunks already filters (`allow_explosions` is off by default).
- **The far end of a door** is in another dimension. Claim mods judge by the dimension the player stands in, so the
  check uses a NeoForge fake player that carries the player's identity and stands in the target dimension.
  - A door can never open inside another team's claim. The player is told "The far side is protected land."
  - A door into the player's own team's claim still opens, through FTB Chunks' `allow_fake_players_by_id` team setting
    (on by default). A team that turns that setting off can't open doors into its own claimed land.
- **Minimap icons:** FTB Chunks gets icon settings for all 8 mobs
  (`assets/ftblibrary/textures/faces/dorohedoro/*.json`). They use a slice of each mob's existing texture and are off
  by default (`default_enabled: false`), so no new art appears unless a player turns them on in FTB Chunks' entity icon
  settings. This also removes 8 "Missing entity icon settings" errors from the log.

### FTB XMod Compat, Essentials and Filter System

Nothing in the mod needs special handling for these. All three load and run alongside it with no errors.

## Optional compat code

The only code that touches FTB at all is `compat/FtbQuestsInstaller`. It does nothing unless
`ModList.isLoaded("ftbquests")`, and it uses no FTB classes, only files. The mod has no compile or runtime dependency
on any FTB mod, and none is declared in `neoforge.mods.toml`.

## Should this ship as a modpack?

Yes, as an extra, not a replacement. The mod stays standalone on CurseForge. The quest book only reaches players through
a pack's `config` folder (or the install command). The owner's local "Official Dorohedoro Modpack" export is already a
complete pack, so the natural home for the book is that pack's overrides. The owner's CurseForge account has no modpack
project yet; creating one is the owner's decision.
