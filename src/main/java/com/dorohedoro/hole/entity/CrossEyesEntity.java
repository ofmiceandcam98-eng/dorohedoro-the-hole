package com.dorohedoro.hole.entity;

import com.dorohedoro.hole.faction.FactionStanding;
import com.dorohedoro.hole.registry.ModItems;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;

/**
 * A member of the Cross-Eyes, the sorcerer gang that keeps camps on the Hole's vacant lots. Original adaptation: the
 * gang and its red cross marks come from the series; these members, their camps and their trades are this mod's.
 *
 * <ul>
 *   <li>Hostile on sight while the player's Cross-Eyes standing is below {@link FactionStanding#CROSS_EYES_TOLERATE};
 *       a member who is hurt alerts the rest of the camp.</li>
 *   <li>An emerald handed over raises standing (tribute, a few a day). At the tolerate threshold they stop attacking;
 *       at {@link FactionStanding#CROSS_EYES_TRADE} they trade smoke goods, which restock every day.</li>
 *   <li>Hitting one lowers standing; killing one lowers it more and raises Hole resident standing a little.</li>
 *   <li>They stay near their camp and never despawn.</li>
 * </ul>
 */
public final class CrossEyesEntity extends Monster implements Merchant {
    private static final int HOME_RADIUS = 14;
    /** How many vanilla default skins a member can wear (the renderer maps each to Alex, Ari and the rest; not Steve). */
    public static final int SKIN_COUNT = 8;
    private static final EntityDataAccessor<Integer> SKIN =
            SynchedEntityData.defineId(CrossEyesEntity.class, EntityDataSerializers.INT);
    @Nullable private Player tradingPlayer;
    @Nullable private MerchantOffers offers;
    private long offersDay = -1;
    private long lastStandingHit = -100;

    public CrossEyesEntity(EntityType<? extends CrossEyesEntity> type, Level level) {
        super(type, level);
        xpReward = 8;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SKIN, 0);
    }

    /** Which vanilla default skin this member wears, 0 to {@link #SKIN_COUNT} - 1; picked at spawn and saved. */
    public int skin() {
        return entityData.get(SKIN);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.29D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ARMOR, 2.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1D, false));
        goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 1.0D));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.7D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                target -> target instanceof ServerPlayer player && hostileTo(player)));
    }

    public static boolean hostileTo(ServerPlayer player) {
        return FactionStanding.of(player).crossEyes() < FactionStanding.CROSS_EYES_TOLERATE;
    }

    /** Keeps the member around its camp. Saved, because vanilla does not save a mob's home. */
    public void setCampHome(BlockPos center) {
        restrictTo(center, HOME_RADIUS);
    }

    @Override
    protected void populateDefaultEquipmentSlots(net.minecraft.util.RandomSource random, DifficultyInstance difficulty) {
        int roll = random.nextInt(10);
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(roll < 5 ? Items.IRON_SWORD : roll < 8 ? Items.IRON_AXE : Items.STONE_SWORD));
        setDropChance(EquipmentSlot.MAINHAND, 0.06F);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            @Nullable SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        entityData.set(SKIN, level.getRandom().nextInt(SKIN_COUNT));
        populateDefaultEquipmentSlots(level.getRandom(), difficulty);
        return result;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    /** A hit from a player lowers their Cross-Eyes standing, once every two seconds at most. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && source.getEntity() instanceof ServerPlayer player && level().getGameTime() - lastStandingHit > 40) {
            lastStandingHit = level().getGameTime();
            FactionStanding.changeCrossEyes(player, -5, "attacked");
        }
        return hurt;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (level().isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer) || !isAlive()) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        if (held.is(Items.EMERALD)) {
            return tribute(serverPlayer, held);
        }
        int standing = FactionStanding.of(serverPlayer).crossEyes();
        if (standing < FactionStanding.CROSS_EYES_TRADE) {
            serverPlayer.displayClientMessage(Component.translatable("message.dorohedoro.cross_eyes.no_deal"), true);
            return InteractionResult.CONSUME;
        }
        if (tradingPlayer == null && getTarget() == null) {
            setTradingPlayer(player);
            openTradingScreen(player, getDisplayName(), 1);
        }
        return InteractionResult.CONSUME;
    }

    private InteractionResult tribute(ServerPlayer player, ItemStack emeralds) {
        if (!FactionStanding.countTribute(player)) {
            player.displayClientMessage(Component.translatable("message.dorohedoro.cross_eyes.enough_today"), true);
            return InteractionResult.CONSUME;
        }
        emeralds.consume(1, player);
        FactionStanding.changeCrossEyes(player, 6, "tribute");
        playSound(SoundEvents.PILLAGER_CELEBRATE, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable("message.dorohedoro.cross_eyes.tribute"), true);
        if (!hostileTo(player)) {
            // Word goes round the camp: everyone who was after this player stands down.
            for (CrossEyesEntity member : level().getEntitiesOfClass(CrossEyesEntity.class, new AABB(blockPosition()).inflate(32))) {
                if (member.getTarget() == player) member.setTarget(null);
                if (member.getLastHurtByMob() == player) member.setLastHurtByMob(null);
            }
        }
        return InteractionResult.CONSUME;
    }

    // ---- Merchant

    @Override public void setTradingPlayer(@Nullable Player player) { tradingPlayer = player; }
    @Override @Nullable public Player getTradingPlayer() { return tradingPlayer; }

    /** The day's stock. Smoke goods for emeralds, and they buy scrap. Restocks with each new day. */
    @Override
    public MerchantOffers getOffers() {
        long day = level().getDayTime() / 24000L;
        if (offers == null || (!level().isClientSide && day != offersDay)) {
            offersDay = day;
            offers = new MerchantOffers();
            offers.add(new MerchantOffer(new ItemCost(ModItems.SCRAP_METAL.get(), 10), new ItemStack(Items.EMERALD), 8, 0, 0.05F));
            offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 2), new ItemStack(ModItems.SMOKE_SOAKED_FILTER.get()), 4, 0, 0.05F));
            offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 3), new ItemStack(ModItems.SMOKE_RESIDUE.get()), 4, 0, 0.05F));
            offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 16), java.util.Optional.of(new ItemCost(Items.GOLD_INGOT, 1)),
                    new ItemStack(ModItems.DEVIL_SIGIL.get()), 1, 0, 0.05F));
        }
        return offers;
    }

    @Override public void overrideOffers(MerchantOffers newOffers) { offers = newOffers; }
    @Override public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();
        ambientSoundTime = -getAmbientSoundInterval();
        playSound(SoundEvents.PILLAGER_AMBIENT, getSoundVolume(), getVoicePitch());
    }
    @Override public void notifyTradeUpdated(ItemStack stack) { }
    @Override public int getVillagerXp() { return 0; }
    @Override public void overrideXp(int xp) { }
    @Override public boolean showProgressBar() { return false; }
    @Override public SoundEvent getNotifyTradeSound() { return SoundEvents.PILLAGER_AMBIENT; }
    @Override public boolean isClientSide() { return level().isClientSide; }

    @Override
    public void die(DamageSource source) {
        if (tradingPlayer != null) tradingPlayer.closeContainer();
        super.die(source);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (tradingPlayer != null) {
            getNavigation().stop();
            getLookControl().setLookAt(tradingPlayer);
        }
    }

    // ---- Save

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (hasRestriction()) tag.put("CampHome", NbtUtils.writeBlockPos(getRestrictCenter()));
        tag.putInt("Skin", skin());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        NbtUtils.readBlockPos(tag, "CampHome").ifPresent(this::setCampHome);
        entityData.set(SKIN, Math.floorMod(tag.getInt("Skin"), SKIN_COUNT));
    }

    // ---- Sounds: a human gang.

    @Override protected SoundEvent getAmbientSound() { return SoundEvents.PILLAGER_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.PILLAGER_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.PILLAGER_DEATH; }

    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level, MobSpawnType reason) {
        return reason != MobSpawnType.NATURAL && super.checkSpawnRules(level, reason);
    }

    public static boolean inCamp(ServerLevel level, BlockPos pos) {
        return !level.getEntitiesOfClass(CrossEyesEntity.class, new AABB(pos).inflate(HOME_RADIUS)).isEmpty();
    }
}
