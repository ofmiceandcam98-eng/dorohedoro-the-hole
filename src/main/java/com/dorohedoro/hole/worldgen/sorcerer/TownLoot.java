package com.dorohedoro.hole.worldgen.sorcerer;

import com.dorohedoro.hole.Dorohedoro;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Loot tables for Sorcerers' World buildings, under {@code data/dorohedoro/loot_table/chests/sorcerer_town/}. They hold
 * what such a room would really contain: food in pantries, tools in workshops, medicine in the clinic. Smoke goods
 * stay scarce and nothing outruns vanilla progression; emeralds remain useful currency.
 */
public final class TownLoot {
    private TownLoot() {}

    private static ResourceKey<LootTable> key(String name) {
        return ResourceKey.create(Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "chests/sorcerer_town/" + name));
    }

    public static final ResourceKey<LootTable> HOUSE = key("house");
    public static final ResourceKey<LootTable> PANTRY = key("pantry");
    public static final ResourceKey<LootTable> MASK_SHOP = key("mask_shop");
    public static final ResourceKey<LootTable> SMOKE_PARLOR = key("smoke_parlor");
    public static final ResourceKey<LootTable> KITCHEN = key("kitchen");
    public static final ResourceKey<LootTable> WORKSHOP = key("workshop");
    public static final ResourceKey<LootTable> CLINIC = key("clinic");
    public static final ResourceKey<LootTable> STUDY = key("study");
    public static final ResourceKey<LootTable> SHANTY = key("shanty");
    public static final ResourceKey<LootTable> LODGING = key("lodging");
    public static final ResourceKey<LootTable> MARKET = key("market");
}
