package com.dorohedoro.hole.magic;

import com.dorohedoro.hole.character.MagicType;
import com.dorohedoro.hole.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;


/**
 * Server-authoritative restoration of an ORIGINAL recovered body part.
 * A renamed item or an old trophy cannot heal a new injury. No new innate magic
 * is granted by the procedure; Humans may perform it with surgical equipment.
 */
public final class LimbReattachmentService {
    private LimbReattachmentService() {}

    /** Item-based interactions require no global event listener; kept for initializer compatibility. */
    public static void register() { }

    /** Right-click a consenting, crouching patient with an original limb or a kit. */
    public static boolean attempt(ServerPlayer operator, ServerPlayer patient, ItemStack held,
                                  InteractionHand hand) {
        if (operator.level() != patient.level() || operator.distanceToSqr(patient) > 16.0D) {
            inform(operator, "Move closer to the patient.");
            return false;
        }
        if (!patient.isShiftKeyDown()) {
            inform(operator, "The patient must crouch to consent to reattachment.");
            return false;
        }
        int missing = OtherMagicMechanics.missingLimbs(patient);
        if (missing == 0) {
            inform(operator, "This patient has no missing limbs.");
            return false;
        }
        ItemStack original = matchingLimb(held, patient);
        if (original.isEmpty()) original = findLimb(operator, patient);
        if (original.isEmpty()) {
            inform(operator, "You need this patient's current, uncooked severed limb.");
            return false;
        }
        int limb = ((com.dorohedoro.hole.item.SeveredLimbItem) original.getItem()).limb();
        if (!matches(original, patient)) {
            inform(operator, "Limb provenance does not match the current injury.");
            return false;
        }
        boolean healer = SmokeEngine.innate(operator) == MagicType.HEALING;
        SmokeState smoke = healer ? SmokeEngine.state(operator) : null;
        boolean mayHeal = healer && smoke.rank() >= SmokeSkill.MEND_ALLY.rankRequired()
            && smoke.learned(SmokeSkill.MEND_ALLY) && smoke.smoke() >= 25;
        ItemStack kit = operator.getItemInHand(hand).is(ModItems.SURGICAL_REATTACHMENT_KIT.get())
            ? operator.getItemInHand(hand) : findKit(operator);
        if (!mayHeal && kit.isEmpty()) {
            inform(operator, "Requires a Surgical Reattachment Kit or a Healing Sorcerer with Mend Ally and 25 Smoke.");
            return false;
        }
        // The item and patient data can change between interactions. Re-check
        // immediately before the authoritative injury mutation.
        if (!matches(original, patient) || !OtherMagicMechanics.reattachRecoveredLimb(patient, limb)) {
            inform(operator, "That limb can no longer be attached to this injury.");
            return false;
        }
        // Only after a successful restoration: consume the original and the
        // chosen resource. Creative mode is not charged but still needs valid data.
        if (!operator.getAbilities().instabuild) {
            original.shrink(1);
            if (mayHeal) {
                operator.setData(SmokeAttachments.STATE, smoke.withSmoke(smoke.smoke() - 25));
                SmokeNetwork.sync(operator);
            } else {
                kit.shrink(1);
            }
        }
        operator.sendSystemMessage(Component.literal("Reattachment completed for " +
            patient.getGameProfile().getName() + ".").withStyle(ChatFormatting.GREEN));
        return true;
    }

    /** The stored token is different for every severing event, and is erased
     * on regeneration or reattachment. A cooked limb has no token or raw item ID. */
    public static boolean matches(ItemStack item, ServerPlayer patient) {
        if (!(item.getItem() instanceof com.dorohedoro.hole.item.SeveredLimbItem raw)) return false;
        int limb = raw.limb();
        if ((OtherMagicMechanics.missingLimbs(patient) & limb) == 0) return false;
        CustomData stored = item.get(DataComponents.CUSTOM_DATA);
        if (stored == null) return false;
        CompoundTag data = stored.copyTag();
        if (!data.hasUUID("victim_uuid") || !data.getUUID("victim_uuid").equals(patient.getUUID())) return false;
        if (!"dismemberment_magic".equals(data.getString("origin"))) return false;
        if (!partName(limb).equals(data.getString("limb"))) return false;
        String token = data.getString("injury_token");
        return !token.isBlank() && token.equals(OtherMagicMechanics.activeLimbToken(patient, limb));
    }

    private static String partName(int limb) {
        return switch (limb) {
            case OtherMagicMechanics.LEFT_ARM -> "Left Arm";
            case OtherMagicMechanics.RIGHT_ARM -> "Right Arm";
            case OtherMagicMechanics.LEFT_LEG -> "Left Leg";
            case OtherMagicMechanics.RIGHT_LEG -> "Right Leg";
            default -> "";
        };
    }

    private static ItemStack matchingLimb(ItemStack item, ServerPlayer patient) {
        return matches(item, patient) ? item : ItemStack.EMPTY;
    }

    private static ItemStack findLimb(ServerPlayer operator, ServerPlayer patient) {
        for (ItemStack item : operator.getInventory().items) {
            if (matches(item, patient)) return item;
        }
        for (ItemStack item : operator.getInventory().offhand) {
            if (matches(item, patient)) return item;
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack findKit(ServerPlayer operator) {
        for (ItemStack item : operator.getInventory().items) {
            if (item.is(ModItems.SURGICAL_REATTACHMENT_KIT.get())) return item;
        }
        for (ItemStack item : operator.getInventory().offhand) {
            if (item.is(ModItems.SURGICAL_REATTACHMENT_KIT.get())) return item;
        }
        return ItemStack.EMPTY;
    }

    private static void inform(ServerPlayer player, String message) {
        player.displayClientMessage(Component.literal(message).withStyle(ChatFormatting.GRAY), true);
    }
}
