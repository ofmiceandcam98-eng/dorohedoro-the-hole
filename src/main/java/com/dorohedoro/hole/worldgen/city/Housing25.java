package com.dorohedoro.hole.worldgen.city;

import static com.dorohedoro.hole.worldgen.city.CityPlan.GROUND_Y;
import static com.dorohedoro.hole.worldgen.city.CityPlan.STREET_Y;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import com.dorohedoro.hole.registry.ModBlocks;

/**
 * Pass 25 residential grammar. Original geometry, no prefabs or imported worlds.
 * Size -> suitable height; seeded hash -> palette/accent, type of home, utilities,
 * furnishing and lighting. Absolute-coordinate, immutable inputs only.
 * 
 * Each tower keeps its shared north stair flight and south main door. Exterior
 * details remain in the lot's 1-block perimeter; no road or sewer obstruction.
 * World detail v14 ONLY; earlier saved profiles retain their original output.
 */
final class Housing25 {
    private Housing25() {}

    static int floors(boolean apartment, Rect box, long hash, int maximum) {
        int w = box.width(), d = box.depth();
        int area = w * d;
        int base, range;
        if (w <= 11 || d <= 10 || area <= 126) {
            base = apartment ? 2 : 1; range = apartment ? 2 : 2;
        } else if (w <= 15 || d <= 13 || area <= 210) {
            base = apartment ? 3 : 2; range = apartment ? 3 : 2;
        } else if (area <= 360) {
            base = apartment ? 4 : 2; range = apartment ? 3 : 3;
        } else {
            base = apartment ? 6 : 3; range = apartment ? 4 : 3;
        }
        return Math.max(1, Math.min(maximum, base + CityHash.range(hash >>> 24, range)));
    }

    static boolean compact(Rect box, int floors) {
        return floors <= 2 || box.width() <= 12 || box.depth() <= 11;
    }

    /** Furnishes actual small residences, including ground floors (not empty lobbies). */
    static void residence(Canvas c, Rect in, int floorY, int storey, int total, long house) {
        if (in.width() < 6 || in.depth() < 5) return;
        long h = CityHash.of(CityHash.of(house, 0x2500AB1L, storey), in.centerX(), in.centerZ());
        int kind = CityHash.range(h, 5);
        int zBack = in.z0() + 2; // north wall shared flight is at z0; keep clear
        int zFront = in.z1() - 1; // entrance at z1 + 1; no block in door approach
        // A kitchenette and wash area on one side, from genuine usable blocks.
        place(c, in.x1(), floorY + 1, zBack, kind % 2 == 0 ? Blocks.SMOKER : Blocks.FURNACE);
        if (zBack + 1 < zFront) place(c, in.x1(), floorY + 1, zBack + 1, Blocks.CAULDRON);
        // Each room has real storage and a place to sleep; small single-storey
        // homes must not accidentally be generated as lobby-only shells.
        if (storey > 0 || total == 1) {
            int bedZ = Math.max(zBack, zFront - 1);
            if (bedZ + 1 < in.z1() + 1) {
                c.bed(in.x0(), floorY + 1, bedZ, Direction.SOUTH,
                        kind % 2 == 0 ? Blocks.GRAY_BED : Blocks.BROWN_BED);
            }
        } else {
            // Two-storey dwelling: entry living room with a small seating nook.
            if (in.depth() >= 7) {
                place(c, in.x0(), floorY + 1, zFront - 1, Blocks.SPRUCE_STAIRS);
                place(c, in.x0() + 1, floorY + 1, zFront - 1, Blocks.SPRUCE_SLAB);
            }
        }
        place(c, in.x1(), floorY + 1, zFront, kind == 0 ? Blocks.BOOKSHELF : Blocks.BARREL);
        if (in.width() >= 9 && in.depth() >= 8) {
            int rugX = in.centerX() + 1;
            int rugZ = in.centerZ() + 1;
            place(c, rugX, floorY + 1, rugZ, switch (kind) {
                case 0 -> Blocks.RED_CARPET;
                case 1 -> Blocks.GRAY_CARPET;
                case 2 -> Blocks.BROWN_CARPET;
                case 3 -> Blocks.GREEN_CARPET;
                default -> Blocks.LIGHT_GRAY_CARPET;
            });
            if (kind == 1 || kind == 4) place(c, in.x0(), floorY + 1, zFront, Blocks.LOOM);
        }
        // Warm central lamp plus a second fixture at one outer wall in large homes.
        // Signature hanging luminaire in purpose-built residential rooms.
        if (c.contains(in.centerX(), in.centerZ())
                && c.get(in.centerX(), floorY + Tower.STOREY - 1, in.centerZ()).isAir()) {
            if (c.hasAtmosphere28()) {
                ResidentialAtmosphere28.hang(c, in.centerX(), floorY, in.centerZ(), h);
            } else {
                c.set(in.centerX(), floorY + Tower.STOREY - 1, in.centerZ(),
                        ModBlocks.CAVERN_LAMP.get().defaultBlockState().setValue(LanternBlock.HANGING, true));
            }
        }
        if (in.width() >= 11 && in.depth() >= 10) ceilingLight(c, in.x1() - 1, floorY, in.z1() - 2);
    }

    /** Adds variety to partitioned multi-unit floors without overwriting their doors. */
    static void residentialDetails(Canvas c, Rect in, int floorY, int storey, long house) {
        long h = CityHash.of(CityHash.of(house, 0x2500AB2L, storey), in.centerX(), in.centerZ());
        int z = Math.max(in.z0() + 3, in.z1() - 3);
        if (storey == 0 && in.width() >= 13) {
            // Reception/lobby details against eastern perimeter, never entry axis.
            place(c, in.x1(), floorY + 1, z, Blocks.SPRUCE_SLAB);
            place(c, in.x1(), floorY + 2, z, Blocks.FLOWER_POT);
        }
        if (storey > 0 && in.width() >= 14 && in.depth() >= 13) {
            if (CityHash.range(h, 3) == 0) {
                place(c, in.x1(), floorY + 1, in.z1() - 4, Blocks.BOOKSHELF);
            }
            ceilingLight(c, in.x1() - 2, floorY, in.z1() - 2);
        }
    }

    /** Architectural variety without disturbing functional doors and stair interiors. */
    static void facade(Canvas c, Rect b, int floors, long h, District district) {
        int kind = CityHash.range(h ^ 0x2500FACADEL, 6);
        int top = STREET_Y + floors * Tower.STOREY;
        Block accent = switch (district) {
            case RESIDENTIAL -> kind % 2 == 0 ? Blocks.TUFF_BRICKS : Blocks.MUD_BRICKS;
            case WATERFRONT -> Blocks.WAXED_WEATHERED_CUT_COPPER;
            case MARKET -> Blocks.DARK_OAK_PLANKS;
            case INDUSTRIAL -> Blocks.CRACKED_DEEPSLATE_BRICKS;
            case DERELICT, GRAVEYARD -> Blocks.CRACKED_STONE_BRICKS;
            case OUTSKIRTS -> Blocks.MOSSY_COBBLESTONE;
        };
        // Distinct entrances: brick lintel, shop-house awning, or patchwork cornice.
        int frontZ = b.z1() + 1;
        int center = b.centerX();
        int sideX = b.x0() + 1;
        int otherSide = b.x1() - 1;
        for (int x = b.x0() + 1; x <= b.x1() - 1; x++) {
            if (kind % 3 != 0 || Math.abs(x - center) < 2) c.set(x, GROUND_Y + 3, frontZ, accent);
        }
        if (b.width() >= 11) {
            // Small exterior porch light rather than a floating full-height street lantern.
            int lightX = center - 2;
            c.set(lightX, GROUND_Y + 2, frontZ,
                    c.hasAtmosphere28() ? ResidentialAtmosphere28.lightState(h) :
                            Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            c.set(lightX, GROUND_Y + 3, frontZ, Blocks.DEEPSLATE_TILE_SLAB);
            if (kind % 2 == 0) {
                c.set(sideX, GROUND_Y + 1, frontZ, Blocks.BARREL);
                c.set(otherSide, GROUND_Y + 1, frontZ, Blocks.FLOWER_POT);
            }
        }
        // Patches of plaster/concrete and thin vertical external downpipes.
        if (kind == 1 || kind == 3 || kind == 5) {
            int pipeX = kind == 1 ? b.x0() : b.x1();
            for (int y = GROUND_Y + 2; y <= top + 1; y++) {
                c.set(pipeX, y, b.z0() - 1, Blocks.CHAIN);
            }
        }
        // Varied balcony positions: sparse, upper storeys only, never door-height.
        if (floors >= 3 && b.width() >= 14 && kind % 2 == 0) {
            int balconyX = (kind == 0) ? b.x0() + 2 : b.x1() - 5;
            for (int k = 1; k < floors; k += 2) {
                int y = STREET_Y + k * Tower.STOREY;
                c.fill(balconyX, y + 1, frontZ, balconyX + 3, y + 1, frontZ, Blocks.DARK_OAK_SLAB);
                c.set(balconyX, y + 2, frontZ, Blocks.IRON_BARS);
                c.set(balconyX + 3, y + 2, frontZ, Blocks.IRON_BARS);
            }
        }
        // Distinct roof caps on small homes, with an uncluttered north stair opening.
        if (floors <= 3 && b.width() >= 9 && b.depth() >= 7) {
            for (int x = b.x0() + 1; x < b.x1(); x++) {
                if (kind == 0 || kind == 2 || kind == 5) {
                    int raise = kind == 2 && Math.abs(x - center) <= 2 ? 2 : 1;
                    c.fill(x, top + 3, b.z1(), x, top + 2 + raise, b.z1(), accent);
                }
            }
            if (kind == 3 && b.width() >= 12) {
                c.fill(b.x1() - 3, top + 2, b.z1() - 3, b.x1() - 2, top + 3, b.z1() - 2, Blocks.WAXED_WEATHERED_CUT_COPPER);
            }
        }
    }

    private static void place(Canvas c, int x, int y, int z, Block block) {
        if (c.contains(x,z) && c.get(x,y,z).isAir() && !c.get(x,y-1,z).isAir()) c.set(x,y,z,block);
    }

    private static void ceilingLight(Canvas c, int x, int floorY, int z) {
        if (!c.contains(x,z)) return;
        if (!c.get(x,floorY + 3,z).isAir()) return;
        if (c.get(x,floorY + 4,z).isAir()) return;
        if (c.hasAtmosphere28()) ResidentialAtmosphere28.hang(c, x, floorY, z, c.hash(x, floorY, z));
        else c.set(x,floorY + 3,z, ModBlocks.CAVERN_LAMP.get().defaultBlockState().setValue(LanternBlock.HANGING, true));
    }
}
