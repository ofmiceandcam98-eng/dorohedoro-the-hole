package com.dorohedoro.hole.hazard;

import com.dorohedoro.hole.Dorohedoro;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

/**
 * Hole Toxin: what the Hole's ash, sludge and tainted weather do to a body. It is applied and refreshed by
 * {@link ToxinExposure} from a player's exposure level, never directly by a mob, and it never kills:
 * <ul>
 *   <li>Level I: hunger drains faster.</li>
 *   <li>Level II: faster still, and blows land a little weaker.</li>
 *   <li>Level III: also a point of damage every four seconds, but never below one heart.</li>
 * </ul>
 * Shelter, boots and a helmet stop it building; an Antidote Poultice clears it; milk halves it.
 */
public final class HoleToxinEffect extends MobEffect {
    public static final ResourceKey<DamageType> DAMAGE =
            ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "hole_toxin"));

    HoleToxinEffect() {
        super(MobEffectCategory.HARMFUL, 0x8FA33A);
        // Scales with the level: -0.5, -1.0, -1.5 attack damage.
        addAttributeModifier(Attributes.ATTACK_DAMAGE,
                ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "effect.hole_toxin"), -0.5,
                AttributeModifier.Operation.ADD_VALUE);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player) {
            player.causeFoodExhaustion(0.004F * (amplifier + 1));
        }
        if (amplifier >= 2 && entity.tickCount % 80 == 0 && entity.getHealth() > 2.0F) {
            entity.hurt(entity.damageSources().source(DAMAGE), 1.0F);
        }
        return true;
    }
}
