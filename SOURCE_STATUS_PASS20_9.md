# Pass 20.9 — Collectible Named Severed Limbs

**NOT COMPILED. NOT TESTED IN MINECRAFT.** No Gradle or NeoForge download, runData, compilation, or game runtime attempted.

## New gameplay

- A successful high-mastery dismemberment roll that actually removes a player's arm or leg now spawns **exactly one real, pickupable Minecraft ItemEntity** near the sever point. Invalid hits, duplicate missing limbs and purely cosmetic cutting cues do not spawn extra items.
- Ground items have stable, readable names such as **Alex's Left Arm** or **Alex's Right Leg**. Their ItemStack stores original victim username, UUID, limb type, dimension and server world tick as custom item data. It also includes readable inventory tooltip lore.
- Four non-stackable item IDs (`severed_left_arm`, `severed_right_arm`, `severed_left_leg`, `severed_right_leg`) with original handcrafted 3D voxel item models and textures. No crafting recipes or unrelated weapon content added.
- Players may walk over and pick up the item once the initial 30-tick pickup delay passes, then store, trade or carry it under vanilla inventory rules. Vanilla dropped item destruction/despawn rules still apply; an item is not a Healing resource and collecting it does not reconnect a missing limb.
- The original detached player-skin 3D cosmetic animation remains. The item entity itself is the persistent collectible, not a client packet or a duplicate spawned by viewers.
- Limb reconstruction now elongates outward from the shoulder/hip with 3D skin mesh and a brief spiral of original Healing Fiber particles. Client particles are emitted once per client tick, not on each rendered frame.

## Preserved systems

- Exactly **64 existing techniques**, 8 each for the 8 innate magic types.
- 3 active ability slots per loadout and 9 saved presets.
- Permanent injury mask, chance and mastery thresholds, actual PvP damage, Healing and Reptile regrowth, multiplayer syncing, smoke flasks, physical mushrooms, structural damage, and the portal landing fix.
- World generation, district architecture, crafting and unrelated weapons were not changed, including the weapons pictured by the user.

## Known constraints / QA on developer machine

- This source archive does not guarantee successful compilation. Compile with Java 21 + NeoForge 1.21.1 dependencies on the owner's PC.
- Inspect custom 3D item model UV alignment, floating name tags and real dropped-item physics in game. In the first ~30 ticks the detached player-skin animation and pickup entity can both be visible; refine blending after actual screenshots.
- Check limb item survival through chest storage/restarts/trading and custom item metadata display. Test two clients; only one drop should exist.
- Cross-mod armor skins and slim-arm models remain imperfect. Static Java parsing does not prove NeoForge symbol/API compatibility.
- Reattaching a collected limb is intentionally NOT implemented. Restoring anatomy remains part of Healing and supernatural Reptile regrowth.
