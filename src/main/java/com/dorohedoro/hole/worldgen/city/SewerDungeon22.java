package com.dorohedoro.hole.worldgen.city;

import com.dorohedoro.hole.registry.ModBlocks;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;

/**
 * Pass 22: uncommon but repeatable subterranean multi-room sewer dungeons.
 *
 * A selected street access shaft connects to the old sewers. The dungeon branches off the
 * WEST side of that existing sewer, with a public entrance, cross-shaped pumpworks,
 * 2 utility wings, an infested encounter vault, and an optional breakable secret cache.
 * Neither the normal sewers nor cave branches are replaced, and every loot item is obtainable
 * through ordinary survival. No quests, new villagers, or faction rules are introduced.
 *
 * All selection/decoration derives from world seed plus arterial indices. Canvas clips every
 * write to the current chunk. The cavern-version field is saved with the world; legacy saves
 * never call this painter. The new generator invokes it AFTER the other sewer passes.
 */
public final class SewerDungeon22 {
    private static final long SALT = 0x5EAD0A22L;
    private static final int Y = 49;

    private SewerDungeon22() {}

    /** Pure selection function for deterministic regression testing. 0 = no dungeon. */
    public static int type(long seed, int xIndex, int zIndex, boolean shaft) {
        if (!shaft) return 0;
        if (xIndex == 0 && zIndex == 0) return 1; // discoverable starter without mandatory progress
        long key = CityHash.of(seed, SALT, xIndex, zIndex);
        return CityHash.range(key, 5) == 0 ? 1 + CityHash.range(CityHash.mix(key), 4) : 0;
    }

    public static void paint(Canvas c, CityPlan plan) {
        // All structures fit inside 67 blocks west and 34 north/south of their intersection.
        List<Line> xLines = plan.xLinesNear(c.minX - 80, c.maxX + 80);
        List<Line> zLines = plan.zLinesNear(c.minZ - 42, c.maxZ + 42);
        for (Line xLine : xLines) {
            for (Line zLine : zLines) {
                if (xLine.kind() == Line.Kind.CANAL || zLine.kind() == Line.Kind.CANAL) continue;
                int type = type(plan.seed, xLine.index(), zLine.index(),
                        plan.hasShaft(xLine.index(), zLine.index()));
                if (type == 0) continue;
                int x = xLine.center(), z = zLine.center();
                Rect footprint = new Rect(x - 68, z - 34, x - 3, z + 34);
                if (!c.overlaps(footprint)) continue;
                // Flat structural plane is sampled at the intersection; it does not zig-zag
                // with street relief as it crosses several chunks.
                c.beginLot(new Rect(x - 2, z - 2, x + 2, z + 2));
                try {
                    build(c, plan.seed, x, z, type);
                } finally {
                    c.endLot();
                }
            }
        }
    }

    private static void build(Canvas c, long seed, int x, int z, int kind) {
        int hx = x - 22;
        int vx = x - 47;
        // Four enclosed, purpose-built chambers with readable junctions and distinct palettes.
        room(c, hx, z, 7, 7, kind, seed);
        room(c, hx, z - 24, 6, 7, 1, seed);
        room(c, hx, z + 24, 6, 7, 2, seed);
        room(c, vx, z, 10, 9, 3, seed);

        // Corridors cross both wall thicknesses. The first joins the EXISTING sewer, rather
        // than appearing as a separate inaccessible chamber inside the solid city fill.
        corridorX(c, x - 16, x - 3, z);
        corridorZ(c, hx, z - 18, z - 6);
        corridorZ(c, hx, z + 6, z + 18);
        corridorX(c, x - 38, x - 28, z);
        // Short dry bridge at the junction: crosses the municipal central drainage runnel.
        c.fill(x - 4, Y, z - 1, x - 2, Y, z + 1, Blocks.POLISHED_ANDESITE);

        // Hub: maps, tool benches, storage and inset warm lighting, with an open cross aisle.
        c.set(hx - 5, Y + 1, z - 5, Blocks.CARTOGRAPHY_TABLE);
        c.set(hx - 4, Y + 1, z - 5, Blocks.LECTERN);
        c.barrel(hx + 5, Y + 1, z - 5, "sewer_maintenance");
        c.set(hx + 5, Y + 1, z + 5, Blocks.CRAFTING_TABLE);
        c.set(hx - 5, Y + 1, z + 5, Blocks.CAULDRON);
        c.sign(hx + 6, Y + 2, z + 3, Direction.WEST, Blocks.OAK_WALL_SIGN,
                "PUMP / NORTH", "FILTER / SOUTH", "DEEP VAULT / W", "EXIT / EAST");
        pendant(c, hx + 4, z + 3);
        pendant(c, hx - 4, z - 3);

        // North machinery: copper pipes, service pistons, elevated gauges and salvage.
        int nz = z - 24;
        for (int side : new int[] {-4, 4}) {
            c.fill(hx + side, Y + 1, nz - 4, hx + side, Y + 4, nz - 4, Blocks.WAXED_WEATHERED_COPPER);
            c.set(hx + side, Y + 2, nz - 3, Blocks.PISTON);
            c.set(hx + side, Y + 1, nz + 2, Blocks.CAULDRON);
        }
        c.barrel(hx - 4, Y + 1, nz + 4, "sewer_maintenance");
        c.set(hx + 4, Y + 1, nz + 4, Blocks.BLAST_FURNACE);
        c.set(hx, Y + 1, nz - 4, Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE);
        // No scripted mobs are necessary; vanilla spawners keep the machinery actively inhabited.
        c.spawner(hx, Y + 1, nz - 2, switch (kind) {
            case 1 -> "dorohedoro:scrap_rat";
            case 2 -> "dorohedoro:rust_eater_beetle";
            case 3 -> "dorohedoro:glow_moth";
            default -> "dorohedoro:sewer_roach";
        });
        pendant(c, hx - 2, nz - 2);

        // South filtration plant: real water troughs, moss, waste, and rare filter salvage.
        int sz = z + 24;
        for (int xx = hx - 3; xx <= hx + 3; xx++) {
            c.set(xx, Y, sz + 3, Blocks.MUD_BRICKS);
            c.set(xx, Y + 1, sz + 3, Blocks.WATER);
            if (Math.floorMod(xx, 2) == 0) c.set(xx, Y + 1, sz - 3, Blocks.MOSS_BLOCK);
        }
        c.set(hx - 4, Y + 1, sz, Blocks.COMPOSTER);
        c.set(hx + 4, Y + 1, sz, Blocks.HOPPER);
        c.barrel(hx - 4, Y + 1, sz + 5, "sewer_filters");
        c.barrel(hx + 4, Y + 1, sz + 5, "sewer_maintenance");
        if ((kind & 1) == 1) {
            c.set(hx - 3, Y + 1, sz - 5, Blocks.COBWEB);
            c.set(hx + 3, Y + 1, sz - 5, Blocks.COBWEB);
        }
        pendant(c, hx + 2, sz + 1);
        c.spawner(hx, Y + 1, sz - 1, switch (kind) {
            case 1, 3 -> "dorohedoro:sewer_leech";
            case 2, 4 -> "dorohedoro:sludge_crawler";
            default -> "dorohedoro:sewer_leech";
        });

        // Final encounter chamber: not an unbeatable boss or an enforced quest gate.
        // Small, breakable spawner(s), cover, manageable water hazards, and a strong cache.
        for (int dx : new int[] {-7, 7}) {
            for (int dz : new int[] {-6, 6}) {
                c.fill(vx + dx, Y + 1, z + dz, vx + dx, Y + 3, z + dz, Blocks.CRACKED_DEEPSLATE_BRICKS);
                c.set(vx + dx, Y + 4, z + dz, Blocks.WAXED_OXIDIZED_COPPER);
            }
        }
        c.fill(vx - 3, Y, z - 6, vx + 3, Y, z - 5, Blocks.MUD);
        c.fill(vx - 3, Y + 1, z - 6, vx + 3, Y + 1, z - 5, Blocks.WATER);
        c.set(vx + 5, Y + 1, z - 2, Blocks.COBWEB);
        c.set(vx + 5, Y + 1, z + 2, Blocks.COBWEB);
        c.spawner(vx - 2, Y + 1, z - 2, switch (kind) {
            case 1 -> "dorohedoro:sewer_roach";
            case 2 -> "dorohedoro:cavern_burrower";
            case 3, 4 -> "dorohedoro:spore_crawler";
            default -> "dorohedoro:sewer_roach";
        });
        c.chest(vx - 7, Y + 1, z + 6, Direction.EAST, "sewer_vault");
        c.barrel(vx + 6, Y + 1, z + 6, "sewer_filters");
        pendant(c, vx + 3, z + 4);
        pendant(c, vx - 6, z - 5);

        // Optional discoverable mining shortcut: cracked west wall hides a dry utility niche.
        // It is always accessible with a pickaxe; no keys or puzzles lock survival progression.
        if (CityHash.range(CityHash.of(seed, SALT + 1, x, z), 2) == 0) {
            int edge = vx - 11;
            c.fill(edge - 5, Y - 1, z - 2, edge, Y + 5, z + 2, Blocks.DEEPSLATE_BRICKS);
            c.fill(edge - 4, Y + 1, z - 1, edge - 1, Y + 4, z + 1, Blocks.AIR);
            c.fill(edge - 4, Y, z - 1, edge - 1, Y, z + 1, Blocks.DEEPSLATE_TILES);
            c.barrel(edge - 3, Y + 1, z, "sewer_vault");
            c.fill(edge, Y + 1, z, edge, Y + 3, z, Blocks.CRACKED_DEEPSLATE_BRICKS);
            c.sign(vx - 9, Y + 2, z - 3, Direction.EAST, Blocks.OAK_WALL_SIGN,
                    "MAINT. BAY", "SEALED 2076", "CHECK WEST", "");
        }
    }

    /** Enclosed municipal masonry room; deliberate dry perimeter and unobstructed interior. */
    private static void room(Canvas c, int cx, int cz, int rx, int rz, int theme, long seed) {
        for (int xx = Math.max(c.minX, cx - rx - 1); xx <= Math.min(c.maxX, cx + rx + 1); xx++) {
            for (int zz = Math.max(c.minZ, cz - rz - 1); zz <= Math.min(c.maxZ, cz + rz + 1); zz++) {
                boolean wall = Math.abs(xx - cx) > rx || Math.abs(zz - cz) > rz;
                Block masonry = switch (theme) {
                    case 1 -> Blocks.TUFF_BRICKS;
                    case 2 -> ModBlocks.SEEPAGE_BRICK.get();
                    case 3 -> Blocks.DEEPSLATE_BRICKS;
                    default -> ModBlocks.SEWER_BRICKWORK.get();
                };
                c.fill(xx, Y - 1, zz, xx, Y + 5, zz, masonry);
                if (!wall) {
                    c.fill(xx, Y + 1, zz, xx, Y + 4, zz, Blocks.AIR);
                    long h = CityHash.of(seed, SALT + 5, xx, zz);
                    c.set(xx, Y, zz, CityHash.range(h, 15) == 0
                            ? Blocks.CRACKED_DEEPSLATE_TILES : Blocks.DEEPSLATE_TILES);
                    if ((Math.abs(xx - cx) == rx || Math.abs(zz - cz) == rz)
                            && CityHash.range(h, 17) == 0) {
                        c.set(xx, Y + 1, zz, Blocks.MOSS_CARPET);
                    }
                } else if ((xx == cx - rx - 1 || xx == cx + rx + 1)
                        && Math.floorMod(zz - cz, 5) == 0) {
                    c.fill(xx, Y + 1, zz, xx, Y + 4, zz, Blocks.CRACKED_STONE_BRICKS);
                }
            }
        }
    }

    /** Side-walled 3-wide link. No end caps: it opens correctly into both adjoining rooms. */
    private static void corridorX(Canvas c, int x0, int x1, int z) {
        for (int x = Math.max(c.minX, x0); x <= Math.min(c.maxX, x1); x++) {
            for (int dz = -2; dz <= 2; dz++) {
                c.fill(x, Y - 1, z + dz, x, Y + 5, z + dz, ModBlocks.SEWER_BRICKWORK.get());
                if (Math.abs(dz) <= 1) {
                    c.set(x, Y, z + dz, Blocks.POLISHED_ANDESITE);
                    c.fill(x, Y + 1, z + dz, x, Y + 4, z + dz, Blocks.AIR);
                }
            }
            if (Math.floorMod(x, 9) == 0) c.set(x, Y + 4, z, Blocks.LANTERN.defaultBlockState()
                    .setValue(LanternBlock.HANGING, true));
        }
    }

    private static void corridorZ(Canvas c, int x, int z0, int z1) {
        for (int z = Math.max(c.minZ, z0); z <= Math.min(c.maxZ, z1); z++) {
            for (int dx = -2; dx <= 2; dx++) {
                c.fill(x + dx, Y - 1, z, x + dx, Y + 5, z, ModBlocks.SEWER_BRICKWORK.get());
                if (Math.abs(dx) <= 1) {
                    c.set(x + dx, Y, z, Blocks.POLISHED_ANDESITE);
                    c.fill(x + dx, Y + 1, z, x + dx, Y + 4, z, Blocks.AIR);
                }
            }
            if (Math.floorMod(z, 9) == 0) c.set(x, Y + 4, z, Blocks.LANTERN.defaultBlockState()
                    .setValue(LanternBlock.HANGING, true));
        }
    }

    private static void pendant(Canvas c, int x, int z) {
        c.set(x, Y + 4, z, Blocks.CHAIN);
        c.set(x, Y + 3, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
    }
}
