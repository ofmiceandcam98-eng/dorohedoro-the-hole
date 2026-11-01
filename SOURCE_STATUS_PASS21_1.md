# Dorohedoro — Pass 21.1: Reattachment and cooked limbs

**STATUS: NOT COMPILED / NOT TESTED IN MINECRAFT.**

## Medical use of collectible limbs

1. Advanced Dismemberment can remove a player's left/right arm or leg and drop the named physical limb item. The item keeps owner UUID, human-readable username, the exact limb and a unique injury token.
2. A medic picks it up and approaches the original owner. The patient **crouches** to signal consent.
3. The medic right-clicks the crouching player with the **original uncooked limb** in hand or a **Surgical Reattachment Kit** in hand while carrying the matching limb in inventory.
4. The server checks proximity (4 blocks), target's current missing-limb bit, original owner UUID, origin, limb type and injury token. **Item display names are never trusted**.
5. The medic must either (a) be a Healing Sorcerer who has learned `Mend Ally` and has 25 Smoke, or (b) carry one crafted Surgical Reattachment Kit. Success consumes the correct physical limb and the corresponding resource in Survival mode.
6. Reattachment restores only the specified limb, updates movement/attack modifiers, syncs other clients and plays a reconstruction effect. Regeneration by high-level Healing or Reptile continues to work without the physical item.
7. Injuries healed before this pass and old limb trophies with no injury token are deliberately not eligible for this owner-verification path. The token protects against using old copies for later unrelated injuries.

## Cooking and eating

- Raw severed limb items remain medical objects, not food.
- Each limb can be cooked in a **furnace, smoker or campfire** using 12 vanilla-standard cooking recipes.
- Cooking creates the corresponding `Cooked Left/Right Arm/Leg` food (6 nutrition and 0.65 saturation modifier). This works as ordinary Minecraft food and uses original recolored 3D limb models.
- **Cooking irreversibly removes reattachment utility**, and regular vanilla cooking recipes do **not** preserve the original limb's custom owner/name/token in the cooked output. This is deliberate for now: generic cooked food cannot be mistaken for the original limb. An optional future custom cooker could retain the name if wanted.
- No new magic types, weapons, quests, world-generation changes or copied assets were introduced.

## Existing rules preserved

- 64 skill definitions, 8 innate families, three ability slots and nine loadouts.
- Real spell damage, PvP, mastery, injury effects, Healing regeneration, Reptile regrowth and player-linked severing animation.
- Existing city world generation and the `PortalService.Frame.stand()` landing calculation.
- The user-deferred weapons in screenshots remain untouched.

## Necessary in-game QA before publishing

- Compile with Java 21 and supported NeoForge dependencies on the user's PC; run datagen (`runData`) and then `build`.
- In two-player multiplayer, sever each limb, drop/pick up, test inventories, chests, item copying and owner renaming; verify only the current original limb can be attached.
- Test kit treatment with Human and non-Healing Sorcerer; test Healing `Mend Ally` path (sufficient rank, learned skill and Smoke), success/failure resource costs, range, crouch consent and offline/reconnect.
- Test kit and limb use in both hands, including when held stack is consumed; confirm inventory sync.
- Cook raw limbs in furnace, smoker and campfire; verify nutrition, textures, item pickup and that cooked limbs cannot repair injuries.
- Test Healing/Reptile regrowth after limb has been lost or cooked, and ensure already restored limbs cannot be reattached twice.
- Confirm network compatibility and packet timing alongside other players' combat.
