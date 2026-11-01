package com.dorohedoro.hole.entity;

import com.dorohedoro.hole.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/** Neutral metal-scavenging insect; brush its carapace for a sustainable sample. */
public final class RustEaterBeetleEntity extends PathfinderMob {
    private static final int BRUSH_COOLDOWN = 4800;
    private int brushCooldown;
    public RustEaterBeetleEntity(EntityType<? extends RustEaterBeetleEntity> type, Level level) { super(type, level); }
    public static AttributeSupplier.Builder attributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D).add(Attributes.FOLLOW_RANGE, 12.0D);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new PanicGoal(this, 1.4D));
        goalSelector.addGoal(3, new RandomStrollGoal(this, .98D));
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 5.0F));
    }
    @Override public void tick() { super.tick(); if (!level().isClientSide && brushCooldown>0) brushCooldown--; }
    @Override protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held=player.getItemInHand(hand);
        if (!held.is(Items.BRUSH)) return super.mobInteract(player, hand);
        if (brushCooldown > 0) return InteractionResult.PASS;
        if (!level().isClientSide) {
            brushCooldown=BRUSH_COOLDOWN;
            ItemStack result=new ItemStack(ModItems.METALLIC_CARAPACE.get());
            if (!player.addItem(result)) spawnAtLocation(result);
            held.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }
    @Override public void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); tag.putInt("BeetleBrushCooldown",brushCooldown); }
    @Override public void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag); brushCooldown=Math.max(0,Math.min(BRUSH_COOLDOWN,tag.getInt("BeetleBrushCooldown"))); }
}
