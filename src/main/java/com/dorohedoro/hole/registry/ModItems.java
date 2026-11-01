package com.dorohedoro.hole.registry;

import com.dorohedoro.hole.Dorohedoro;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import com.dorohedoro.hole.item.SewerEchoProbeItem;
import com.dorohedoro.hole.item.EmptySmokeFlaskItem;
import com.dorohedoro.hole.item.FilledSmokeFlaskItem;
import com.dorohedoro.hole.character.MagicType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Dorohedoro.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Dorohedoro.MOD_ID);

    /** Salvaged metal. Nine craft into an iron ingot, so scavenging alone can reach iron tools. */
    public static final DeferredItem<Item> SCRAP_METAL = ITEMS.registerSimpleItem("scrap_metal");
    public static final DeferredItem<Item> RAW_SULFUR = ITEMS.registerSimpleItem("raw_sulfur");
    public static final DeferredItem<Item> ROCK_SALT = ITEMS.registerSimpleItem("rock_salt");
    public static final DeferredItem<Item> LIME_POWDER = ITEMS.registerSimpleItem("lime_powder");
    public static final DeferredItem<Item> REINFORCED_CHITIN = ITEMS.registerSimpleItem("reinforced_chitin");
    public static final DeferredItem<Item> ROACH_ANTENNA = ITEMS.registerSimpleItem("roach_antenna");
    public static final DeferredItem<Item> RAT_FUR = ITEMS.registerSimpleItem("rat_fur");
    public static final DeferredItem<Item> RAW_RAT_MEAT = ITEMS.registerSimpleItem("raw_rat_meat",
            new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.15F).build()));
    public static final DeferredItem<Item> COOKED_RAT_MEAT = ITEMS.registerSimpleItem("cooked_rat_meat",
            new Item.Properties().food(new FoodProperties.Builder().nutrition(5).saturationModifier(0.55F).build()));
    public static final DeferredItem<Item> SALTED_RAT_RATION = ITEMS.registerSimpleItem("salted_rat_ration",
            new Item.Properties().food(new FoodProperties.Builder().nutrition(7).saturationModifier(0.7F).build()));
    public static final DeferredItem<Item> LEECH_MUCUS = ITEMS.registerSimpleItem("leech_mucus");
    public static final DeferredItem<Item> LEECH_SUCKER = ITEMS.registerSimpleItem("leech_sucker");
    public static final DeferredItem<Item> MEDICAL_SALVE = ITEMS.registerSimpleItem("medical_salve",
            new Item.Properties().food(new FoodProperties.Builder().nutrition(0).saturationModifier(0.0F)
                    .alwaysEdible().effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 100, 0), 1.0F).build()));
    public static final DeferredItem<Item> LUMINOUS_SCALE = ITEMS.registerSimpleItem("luminous_scale");
    public static final DeferredItem<Item> MOTH_SILK = ITEMS.registerSimpleItem("moth_silk");
    public static final DeferredItem<Item> GLOW_TONIC = ITEMS.registerSimpleItem("glow_tonic",
            new Item.Properties().food(new FoodProperties.Builder().nutrition(0).saturationModifier(0.0F)
                    .alwaysEdible().effect(() -> new MobEffectInstance(MobEffects.NIGHT_VISION, 1800, 0), 1.0F).build()));
    public static final DeferredItem<Item> RAW_ZINC = ITEMS.registerSimpleItem("raw_zinc");
    public static final DeferredItem<Item> ZINC_INGOT = ITEMS.registerSimpleItem("zinc_ingot");
    public static final DeferredItem<Item> BRASS_INGOT = ITEMS.registerSimpleItem("brass_ingot");
    public static final DeferredItem<Item> OIL_SHALE_CHUNK = ITEMS.registerSimpleItem("oil_shale_chunk");
    public static final DeferredItem<Item> BITUMEN = ITEMS.registerSimpleItem("bitumen");
    public static final DeferredItem<Item> SLUDGE_GLAND = ITEMS.registerSimpleItem("sludge_gland");
    public static final DeferredItem<Item> COAGULATED_TAR = ITEMS.registerSimpleItem("coagulated_tar");
    public static final DeferredItem<Item> CORROSIVE_GLAND = ITEMS.registerSimpleItem("corrosive_gland");
    public static final DeferredItem<Item> METALLIC_CARAPACE = ITEMS.registerSimpleItem("metallic_carapace");
    public static final DeferredItem<Item> BURROWER_CLAW = ITEMS.registerSimpleItem("burrower_claw");
    public static final DeferredItem<Item> DENSE_HIDE = ITEMS.registerSimpleItem("dense_hide");
    public static final DeferredItem<Item> SPORE_SAC = ITEMS.registerSimpleItem("spore_sac");
    public static final DeferredItem<Item> FUNGAL_MEMBRANE = ITEMS.registerSimpleItem("fungal_membrane");
    public static final DeferredItem<Item> ANTIDOTE_POULTICE = ITEMS.register("antidote_poultice",
            () -> new com.dorohedoro.hole.item.AntidotePoulticeItem(new Item.Properties().stacksTo(16)
                    .food(new FoodProperties.Builder().nutrition(0).saturationModifier(0.0F).alwaysEdible().build())));
    public static final DeferredItem<Item> SEWER_SEALANT = ITEMS.registerSimpleItem("sewer_sealant");
    public static final DeferredItem<SewerEchoProbeItem> SEWER_ECHO_PROBE = ITEMS.register("sewer_echo_probe",
            () -> new SewerEchoProbeItem(new Item.Properties().stacksTo(1).durability(64)));
    /** Recovered from smoke-stained machinery and abandoned laboratories; furnace processing yields usable residue. */
    public static final DeferredItem<Item> SMOKE_SOAKED_FILTER = ITEMS.registerSimpleItem("smoke_soaked_filter");
    public static final DeferredItem<Item> SMOKE_RESIDUE = ITEMS.registerSimpleItem("smoke_residue");
    public static final DeferredItem<Item> SMOKE_CONDENSER = ITEMS.registerSimpleItem("smoke_condenser");
    public static final DeferredItem<Item> SMOKE_CONDUIT = ITEMS.registerSimpleItem("smoke_conduit");
    public static final DeferredItem<Item> PRESERVED_SMOKE_CONDUIT = ITEMS.registerSimpleItem("preserved_smoke_conduit");
    public static final DeferredItem<Item> DOOR_ACTIVATION_TOOL = ITEMS.registerSimpleItem("door_activation_tool", new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> DOOR_LINKING_KEY = ITEMS.registerSimpleItem("door_linking_key", new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> DEVIL_SIGIL = ITEMS.registerSimpleItem("devil_sigil", new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> RITUAL_TOOL = ITEMS.registerSimpleItem("ritual_tool", new Item.Properties().stacksTo(1));


    /** Finite stored smoke; each colored label identifies the ORIGINAL producer's magic. */
    public static final DeferredItem<EmptySmokeFlaskItem> EMPTY_SMOKE_FLASK = ITEMS.register("empty_smoke_flask",
            () -> new EmptySmokeFlaskItem(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<FilledSmokeFlaskItem> MUSHROOM_SMOKE_FLASK = registerSmokeFlask(MagicType.MUSHROOM);
    public static final DeferredItem<FilledSmokeFlaskItem> REPTILE_SMOKE_FLASK = registerSmokeFlask(MagicType.REPTILE);
    public static final DeferredItem<FilledSmokeFlaskItem> DISMEMBERMENT_SMOKE_FLASK = registerSmokeFlask(MagicType.DISMEMBERMENT);
    public static final DeferredItem<FilledSmokeFlaskItem> HEALING_SMOKE_FLASK = registerSmokeFlask(MagicType.HEALING);
    public static final DeferredItem<FilledSmokeFlaskItem> CURSE_SMOKE_FLASK = registerSmokeFlask(MagicType.CURSE);
    public static final DeferredItem<FilledSmokeFlaskItem> PROTECTION_SMOKE_FLASK = registerSmokeFlask(MagicType.PROTECTION);
    public static final DeferredItem<FilledSmokeFlaskItem> TIME_SMOKE_FLASK = registerSmokeFlask(MagicType.TIME);
    public static final DeferredItem<FilledSmokeFlaskItem> DESTRUCTION_SMOKE_FLASK = registerSmokeFlask(MagicType.DESTRUCTION);
    private static DeferredItem<FilledSmokeFlaskItem> registerSmokeFlask(MagicType type) {
        return ITEMS.register(type.id() + "_smoke_flask",
                () -> new FilledSmokeFlaskItem(type, new Item.Properties().stacksTo(16)));
    }
    public static Item smokeFlask(MagicType type) {
        return switch (type) {
            case MUSHROOM -> MUSHROOM_SMOKE_FLASK.get();
            case REPTILE -> REPTILE_SMOKE_FLASK.get();
            case DISMEMBERMENT -> DISMEMBERMENT_SMOKE_FLASK.get();
            case HEALING -> HEALING_SMOKE_FLASK.get();
            case CURSE -> CURSE_SMOKE_FLASK.get();
            case PROTECTION -> PROTECTION_SMOKE_FLASK.get();
            case TIME -> TIME_SMOKE_FLASK.get();
            case DESTRUCTION -> DESTRUCTION_SMOKE_FLASK.get();
        };
    }

    /** Original owner-bound limbs can be surgically reattached using their item provenance. */
    public static final DeferredItem<com.dorohedoro.hole.item.SeveredLimbItem> SEVERED_LEFT_ARM = ITEMS.register(
            "severed_left_arm", () -> new com.dorohedoro.hole.item.SeveredLimbItem(
                com.dorohedoro.hole.magic.OtherMagicMechanics.LEFT_ARM, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<com.dorohedoro.hole.item.SeveredLimbItem> SEVERED_RIGHT_ARM = ITEMS.register(
            "severed_right_arm", () -> new com.dorohedoro.hole.item.SeveredLimbItem(
                com.dorohedoro.hole.magic.OtherMagicMechanics.RIGHT_ARM, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<com.dorohedoro.hole.item.SeveredLimbItem> SEVERED_LEFT_LEG = ITEMS.register(
            "severed_left_leg", () -> new com.dorohedoro.hole.item.SeveredLimbItem(
                com.dorohedoro.hole.magic.OtherMagicMechanics.LEFT_LEG, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<com.dorohedoro.hole.item.SeveredLimbItem> SEVERED_RIGHT_LEG = ITEMS.register(
            "severed_right_leg", () -> new com.dorohedoro.hole.item.SeveredLimbItem(
                com.dorohedoro.hole.magic.OtherMagicMechanics.RIGHT_LEG, new Item.Properties().stacksTo(1)));
    /** Consumable instruments for a Human or other non-Healing field surgeon. */
    public static final DeferredItem<com.dorohedoro.hole.item.SurgicalReattachmentKitItem> SURGICAL_REATTACHMENT_KIT = ITEMS.register(
            "surgical_reattachment_kit", () -> new com.dorohedoro.hole.item.SurgicalReattachmentKitItem(
                new Item.Properties().stacksTo(16)));

    /** Cooked limbs are food only. No owner token survives normal vanilla cooking,
     * so cooked parts can never be used for reattachment. */
    public static final DeferredItem<Item> COOKED_LEFT_ARM = cookedLimb("cooked_left_arm");
    public static final DeferredItem<Item> COOKED_RIGHT_ARM = cookedLimb("cooked_right_arm");
    public static final DeferredItem<Item> COOKED_LEFT_LEG = cookedLimb("cooked_left_leg");
    public static final DeferredItem<Item> COOKED_RIGHT_LEG = cookedLimb("cooked_right_leg");
    private static DeferredItem<Item> cookedLimb(String id) {
        return ITEMS.registerSimpleItem(id, new Item.Properties().stacksTo(16)
            .food(new FoodProperties.Builder().nutrition(6).saturationModifier(.65F).build()));
    }

    public static Item severedLimb(int limb) {
        return switch (limb) {
            case 1 -> SEVERED_LEFT_ARM.get();
            case 2 -> SEVERED_RIGHT_ARM.get();
            case 4 -> SEVERED_LEFT_LEG.get();
            case 8 -> SEVERED_RIGHT_LEG.get();
            default -> throw new IllegalArgumentException("Not a severable limb: " + limb);
        };
    }

    /** Testing utilities, Creative-only: real 1.21.1 NeoForge eggs linked to the corresponding mob types.
     * Use NeoForge's deferred egg rather than resolving entity types during static registration. */
    public static final DeferredItem<DeferredSpawnEggItem> SEWER_ROACH_SPAWN_EGG = ITEMS.register(
            "sewer_roach_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntities.SEWER_ROACH, 0x453023, 0xBC8350, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> SCRAP_RAT_SPAWN_EGG = ITEMS.register(
            "scrap_rat_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntities.SCRAP_RAT, 0x786B64, 0xD69AA5, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> SEWER_LEECH_SPAWN_EGG = ITEMS.register(
            "sewer_leech_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntities.SEWER_LEECH, 0x54283B, 0xD58B9B, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> GLOW_MOTH_SPAWN_EGG = ITEMS.register(
            "glow_moth_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntities.GLOW_MOTH, 0x829982, 0xEDF4C5, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> SLUDGE_CRAWLER_SPAWN_EGG = ITEMS.register(
            "sludge_crawler_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntities.SLUDGE_CRAWLER, 0x435443, 0xB1B96F, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> RUST_EATER_BEETLE_SPAWN_EGG = ITEMS.register(
            "rust_eater_beetle_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntities.RUST_EATER_BEETLE, 0x654B3B, 0xCB905A, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> CAVERN_BURROWER_SPAWN_EGG = ITEMS.register(
            "cavern_burrower_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntities.CAVERN_BURROWER, 0x4D5353, 0xA98A66, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> SPORE_CRAWLER_SPAWN_EGG = ITEMS.register(
            "spore_crawler_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntities.SPORE_CRAWLER, 0x534364, 0xADC681, new Item.Properties()));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("hole", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.dorohedoro.hole"))
            .icon(() -> new ItemStack(SCRAP_METAL.get()))
            .displayItems((parameters, output) -> ITEMS.getEntries().forEach(item -> output.accept(item.get())))
            .build());

    /** Dedicated, searchable testing tab. Eggs are also present under The Hole and vanilla Spawn Eggs. */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATURES_TAB = TABS.register("creatures", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.dorohedoro.creatures"))
            .icon(() -> new ItemStack(SEWER_ROACH_SPAWN_EGG.get()))
            .displayItems((parameters, output) -> {
                output.accept(SEWER_ROACH_SPAWN_EGG.get());
                output.accept(SCRAP_RAT_SPAWN_EGG.get());
                output.accept(SEWER_LEECH_SPAWN_EGG.get());
                output.accept(GLOW_MOTH_SPAWN_EGG.get());
                output.accept(SLUDGE_CRAWLER_SPAWN_EGG.get());
                output.accept(RUST_EATER_BEETLE_SPAWN_EGG.get());
                output.accept(CAVERN_BURROWER_SPAWN_EGG.get());
                output.accept(SPORE_CRAWLER_SPAWN_EGG.get());
            })
            .build());

    /** Also expose Dorohedoro creatures in the vanilla Spawn Eggs tab for easy testing. */
    private static void addToVanillaSpawnEggs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != CreativeModeTabs.SPAWN_EGGS) return;
        addEggIfMissing(event, SEWER_ROACH_SPAWN_EGG.get());
        addEggIfMissing(event, SCRAP_RAT_SPAWN_EGG.get());
        addEggIfMissing(event, SEWER_LEECH_SPAWN_EGG.get());
        addEggIfMissing(event, GLOW_MOTH_SPAWN_EGG.get());
        addEggIfMissing(event, SLUDGE_CRAWLER_SPAWN_EGG.get());
        addEggIfMissing(event, RUST_EATER_BEETLE_SPAWN_EGG.get());
        addEggIfMissing(event, CAVERN_BURROWER_SPAWN_EGG.get());
        addEggIfMissing(event, SPORE_CRAWLER_SPAWN_EGG.get());
    }

    /** Avoid duplicate items if another tab-population hook has already added the egg. */
    private static void addEggIfMissing(BuildCreativeModeTabContentsEvent event, Item egg) {
        if (event.getParentEntries().stream().noneMatch(stack -> stack.is(egg))) {
            event.accept(egg);
        }
    }

    private ModItems() {}

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        TABS.register(modBus);
        modBus.addListener(ModItems::addToVanillaSpawnEggs);
    }
}
