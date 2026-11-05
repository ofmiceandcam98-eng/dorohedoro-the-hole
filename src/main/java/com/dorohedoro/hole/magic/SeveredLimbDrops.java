package com.dorohedoro.hole.magic;

import com.dorohedoro.hole.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** The ONLY server path that spawns collectable severed-limb trophies.
 * Each item retains the victim's name, UUID and specific limb after pickup,
 * trade, chest storage and restart. The same physical limb can be reattached by a healer or surgeon.
 * The model animation is cosmetic; this ItemEntity is the real loot. */
public final class SeveredLimbDrops {
    private SeveredLimbDrops() {}

    public static ItemStack create(ServerPlayer victim, int limb) {
        String person = victim.getGameProfile().getName();
        String part = switch (limb) {
            case OtherMagicMechanics.LEFT_ARM -> "Left Arm";
            case OtherMagicMechanics.RIGHT_ARM -> "Right Arm";
            case OtherMagicMechanics.LEFT_LEG -> "Left Leg";
            case OtherMagicMechanics.RIGHT_LEG -> "Right Leg";
            default -> throw new IllegalArgumentException("Invalid missing limb");
        };
        ItemStack stack = new ItemStack(ModItems.severedLimb(limb));
        String name = person + "'s " + part;
        // ITEM_NAME gives the drop a stable descriptive name, while its original
        // identity is stored separately in CUSTOM_DATA, not inferred from a label.
        stack.set(DataComponents.ITEM_NAME, Component.literal(name).withStyle(ChatFormatting.DARK_RED));
        CompoundTag provenance = new CompoundTag();
        provenance.putString("victim_name", person);
        provenance.putUUID("victim_uuid", victim.getUUID());
        provenance.putString("limb", part);
        provenance.putString("origin", "dismemberment_magic");
        provenance.putString("injury_token", OtherMagicMechanics.activeLimbToken(victim, limb));
        provenance.putString("dimension", victim.level().dimension().location().toString());
        provenance.putLong("world_tick", victim.level().getGameTime());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(provenance));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
            Component.literal("Severed from " + person).withStyle(ChatFormatting.GRAY),
            Component.literal("Limb: " + part).withStyle(ChatFormatting.DARK_GRAY),
            Component.literal("Can be reattached to its original owner.").withStyle(ChatFormatting.DARK_GREEN),
            Component.literal("Right-click a crouching patient; Healing or surgical kit required.").withStyle(ChatFormatting.GRAY),
            Component.literal("Cooking makes this limb edible but impossible to reattach.").withStyle(ChatFormatting.DARK_GRAY)
        )));
        return stack;
    }

    /** Spawn a real inventory item before announcing the visual effect.
     * The returned network entity ID lets every client transition its skin-
     * mapped flying limb onto the same loot item instead of drawing a second
     * untracked limb somewhere else. -1 means the spawn was rejected. */
    public static int spawn(ServerPlayer victim, int limb) {
        if (!(victim.level() instanceof ServerLevel level)) return -1;
        boolean arm = limb == OtherMagicMechanics.LEFT_ARM || limb == OtherMagicMechanics.RIGHT_ARM;
        boolean left = limb == OtherMagicMechanics.LEFT_ARM || limb == OtherMagicMechanics.LEFT_LEG;
        double radians = Math.toRadians(victim.getYRot());
        // Keep the drop close to the original attachment point, but let vanilla
        // gravity and item physics handle its motion and later item pickup.
        Vec3 outward = new Vec3(Math.cos(radians), 0, Math.sin(radians)).scale(left ? -.35 : .35);
        Vec3 pos = victim.position().add(outward).add(0,
            victim.getBbHeight() * (arm ? .70 : .28), 0);
        ItemEntity drop = new ItemEntity(level, pos.x, pos.y, pos.z, create(victim, limb));
        drop.setPickUpDelay(30); // Give the 3D severing effect a moment to play.
        drop.setDeltaMovement(outward.scale(.60).add(0, .22, 0));
        drop.setCustomName(drop.getItem().getHoverName());
        drop.setCustomNameVisible(true); // Nearby players can read the name on the ground.
        return level.addFreshEntity(drop) ? drop.getId() : -1;
    }
}
