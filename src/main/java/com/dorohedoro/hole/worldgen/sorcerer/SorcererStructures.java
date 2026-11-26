package com.dorohedoro.hole.worldgen.sorcerer;

import com.dorohedoro.hole.Dorohedoro;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registry entries for Sorcerers' World settlements. The structure itself, its placement and its biomes are data:
 * {@code worldgen/structure/sorcerer_settlement.json}, {@code worldgen/structure_set/sorcerer_settlements.json} and
 * the biome tag {@code has_structure/sorcerer_settlement}, which lists only Sorcerers' World biomes, so settlements
 * can never appear in the Hole, Hell or vanilla dimensions.
 */
public final class SorcererStructures {
    private static final DeferredRegister<StructureType<?>> TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, Dorohedoro.MOD_ID);
    private static final DeferredRegister<StructurePieceType> PIECES = DeferredRegister.create(Registries.STRUCTURE_PIECE, Dorohedoro.MOD_ID);

    public static final DeferredHolder<StructureType<?>, StructureType<SorcererTownStructure>> SETTLEMENT =
            TYPES.register("sorcerer_settlement", () -> () -> SorcererTownStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> ROADS_PIECE =
            PIECES.register("sw_town_roads", () -> (StructurePieceType.ContextlessType) TownRoadsPiece::new);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> BUILDING_PIECE =
            PIECES.register("sw_town_building", () -> (StructurePieceType.ContextlessType) TownBuildingPiece::new);

    public static final ResourceKey<Structure> SETTLEMENT_KEY = ResourceKey.create(Registries.STRUCTURE,
            ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "sorcerer_settlement"));

    private SorcererStructures() {}

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        PIECES.register(modBus);
    }
}
