package com.dorohedoro.hole.registry;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.portal.PortalKind;
import com.dorohedoro.hole.world.light.GlowMothLightBlock;
import com.dorohedoro.hole.portal.PortalCoreBlock;
import com.dorohedoro.hole.portal.PortalSurfaceBlock;
import java.util.function.Supplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The Hole's own blocks. Each drops something useful, so salvage is a real early-game resource. */
public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Dorohedoro.MOD_ID);

    /** Heaps of machine scrap. The early-game source of metal, drops scrap and sometimes iron nuggets. */
    public static final DeferredBlock<Block> INDUSTRIAL_DEBRIS = register("industrial_debris", () -> new Block(
            BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).strength(1.5F, 3.0F).sound(SoundType.CHAIN).requiresCorrectToolForDrops()));

    /** Corroded wall sheet used across the industrial districts. Weathered copper does not scavenge into anything useful. */
    public static final DeferredBlock<Block> RUSTED_PLATING = register("rusted_plating", () -> new Block(
            BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).strength(2.5F, 5.0F).sound(SoundType.COPPER).requiresCorrectToolForDrops()));

    /** Custom dark stone of the barren Hell dimension; ordinary Nether blocks are deliberately not reused. */
    public static final DeferredBlock<Block> HELL_ROCK = register("hell_rock", () -> new Block(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(2.0F, 6.0F)
                    .sound(SoundType.DEEPSLATE).requiresCorrectToolForDrops()));

    /** Loose, mineable surface ash. Useful as a dark building material; no natural trees grow here. */
    public static final DeferredBlock<Block> ASH_SOIL = register("ash_soil", () -> new Block(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(0.6F, 0.8F)
                    .sound(SoundType.GRAVEL)));

    /** Dark, stained flood-control masonry. Recoverable from sewer walls and craftable from local materials. */
    public static final DeferredBlock<Block> SEWER_BRICKWORK = register("sewer_brickwork", () -> new Block(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(1.5F, 6.0F)
                    .sound(SoundType.STONE).requiresCorrectToolForDrops()));

    /** Mineable real-world sulfur and salt minerals, distinct from magical smoke. */
    public static final DeferredBlock<Block> SULFUR_ORE = register("sulfur_ore", () -> new Block(
            BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_YELLOW).strength(3.0F, 5.0F)
                    .sound(SoundType.STONE).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> ROCK_SALT_ORE = register("rock_salt_ore", () -> new Block(
            BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_WHITE).strength(2.6F, 4.5F)
                    .sound(SoundType.STONE).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> LIMESTONE = register("limestone", () -> new Block(
            BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(1.8F, 5.0F)
                    .sound(SoundType.STONE).requiresCorrectToolForDrops()));

    /** Zinc-bearing stone; proper pickaxe drops the raw ore, preserving vanilla metal progression. */
    public static final DeferredBlock<Block> ZINC_ORE = register("zinc_ore", () -> new Block(
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0F, 5.0F)
                    .sound(SoundType.STONE).requiresCorrectToolForDrops()));
    /** Oil-rich sediment, processed into bitumen instead of fabricated by coal alone. */
    public static final DeferredBlock<Block> OIL_SHALE_ORE = register("oil_shale_ore", () -> new Block(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(2.4F, 5.0F)
                    .sound(SoundType.STONE).requiresCorrectToolForDrops()));
    /** Galvanized sheet is a durable decorative/utility building material, not tier-breaking armor. */
    public static final DeferredBlock<Block> GALVANIZED_PLATING = register("galvanized_plating", () -> new Block(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5F, 8.0F)
                    .sound(SoundType.METAL).requiresCorrectToolForDrops()));

    /** Pass 19: original urban construction materials. Palette follows the anime/manga, not extracted textures. */
    public static final DeferredBlock<Block> SMOKE_STAINED_CONCRETE = register("smoke_stained_concrete", () -> new Block(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(2.0F, 5.0F)
                    .sound(SoundType.STONE).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> WATER_DAMAGED_PLASTER = register("water_damaged_plaster", () -> new Block(
            BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_WHITE).strength(1.4F, 3.4F)
                    .sound(SoundType.STONE).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> SOOT_BLACKENED_BRICK = register("soot_blackened_brick", () -> new Block(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(2.2F, 5.0F)
                    .sound(SoundType.STONE).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> PATCHWORK_MASONRY = register("patchwork_masonry", () -> new Block(
            BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).strength(1.8F, 4.0F)
                    .sound(SoundType.STONE).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> RUSTED_INDUSTRIAL_SHEET = register("rusted_industrial_sheet", () -> new Block(
            BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).strength(2.0F, 5.0F)
                    .sound(SoundType.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> SEEPAGE_BRICK = register("seepage_brick", () -> new Block(
            BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).strength(1.8F, 4.0F)
                    .sound(SoundType.STONE).requiresCorrectToolForDrops()));

    /** Permanent light uses a renewable moth resource and ordinary glass/iron. */
    public static final DeferredBlock<Block> CAVERN_LAMP = register("cavern_lamp", () -> new net.minecraft.world.level.block.LanternBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.6F, 5.0F)
                    .lightLevel(state -> 13).sound(SoundType.GLASS).requiresCorrectToolForDrops()));

    /** Durable masonry from cockroach chitin and scavenged concrete. */
    public static final DeferredBlock<Block> CHITIN_PLATING = register("chitin_plating", () -> new Block(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(3.5F, 8.0F)
                    .sound(SoundType.COPPER).requiresCorrectToolForDrops()));

    /** Four-by-five smoke-powered doorway ring, built by ordinary survivors. */
    public static final DeferredBlock<Block> REINFORCED_DOOR_FRAME = register("reinforced_door_frame", () -> new Block(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(4.0F, 12.0F)
                    .sound(SoundType.METAL).requiresCorrectToolForDrops()));
    /** Five-by-five Hell entrance; deliberately distinct from the Nether portal. */
    public static final DeferredBlock<Block> ETCHED_GATE_STONE = register("etched_gate_stone", () -> new Block(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(4.0F, 12.0F)
                    .sound(SoundType.DEEPSLATE).requiresCorrectToolForDrops()));
    public static final DeferredBlock<PortalCoreBlock> ARTIFICIAL_DOOR_CORE = register("artificial_door_core", () -> new PortalCoreBlock(
            PortalKind.ARTIFICIAL, BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                    .strength(5.0F, 12.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<PortalCoreBlock> INFERNAL_DOOR_CORE = register("infernal_door_core", () -> new PortalCoreBlock(
            PortalKind.DEVIL, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK)
                    .strength(5.0F, 12.0F).sound(SoundType.DEEPSLATE).requiresCorrectToolForDrops()));
    /** Return-end frames are intentionally non-dropping. A newly crafted gate cannot manufacture free iron/bone. */
    public static final DeferredBlock<Block> GENERATED_DOOR_FRAME = BLOCKS.register("generated_door_frame", ignored -> new Block(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(4.0F, 12.0F)
                    .sound(SoundType.METAL).requiresCorrectToolForDrops().noLootTable()));
    public static final DeferredBlock<Block> GENERATED_GATE_STONE = BLOCKS.register("generated_gate_stone", ignored -> new Block(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(4.0F, 12.0F)
                    .sound(SoundType.DEEPSLATE).requiresCorrectToolForDrops().noLootTable()));
    public static final DeferredBlock<PortalCoreBlock> GENERATED_ARTIFICIAL_CORE = BLOCKS.register("generated_artificial_core", ignored -> new PortalCoreBlock(
            PortalKind.ARTIFICIAL, BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                    .strength(5.0F, 12.0F).sound(SoundType.METAL).requiresCorrectToolForDrops().noLootTable()));
    public static final DeferredBlock<PortalCoreBlock> GENERATED_INFERNAL_CORE = BLOCKS.register("generated_infernal_core", ignored -> new PortalCoreBlock(
            PortalKind.DEVIL, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK)
                    .strength(5.0F, 12.0F).sound(SoundType.DEEPSLATE).requiresCorrectToolForDrops().noLootTable()));

    /** Non-solid activated door plane, with no item or drops. */
    public static final DeferredBlock<PortalSurfaceBlock> PORTAL_SURFACE = BLOCKS.register("portal_surface", ignored -> new PortalSurfaceBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).noCollission().noOcclusion()
                    .lightLevel(state -> 7).strength(-1.0F, 3600000.0F).noLootTable()));

    /** Pass 20.2: solid, non-dropping magic-generated condensed smoke. Not obtainable as an item.
     * Protection barriers have real block collision, but never overwrite existing blocks. */
    public static final DeferredBlock<Block> CONDENSED_SMOKE_BARRIER = BLOCKS.register("condensed_smoke_barrier", ignored -> new Block(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(0.8F, 1.5F)
                    .sound(SoundType.WOOL).noOcclusion().noLootTable()));

    /** Internal transient light: no item and no worldgen. Scheduled cleanup avoids permanent lamps. */
    public static final DeferredBlock<GlowMothLightBlock> GLOW_MOTH_LIGHT = BLOCKS.register("glow_moth_light", ignored ->
            new GlowMothLightBlock(BlockBehaviour.Properties.of().noCollission().noOcclusion()
                    .replaceable().lightLevel(state -> GlowMothLightBlock.LIGHT_LEVEL)
                    .noLootTable().strength(0.0F)));

    private ModBlocks() {}

    private static <T extends Block> DeferredBlock<T> register(String name, Supplier<T> block) {
        DeferredBlock<T> holder = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> new BlockItem(holder.get(), new Item.Properties()));
        return holder;
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
