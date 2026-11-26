package com.dorohedoro.hole.worldgen.city;

import com.dorohedoro.hole.registry.ModBlocks;
import static com.dorohedoro.hole.worldgen.city.CityPlan.STREET_Y;
import static com.dorohedoro.hole.worldgen.city.CityPlan.GROUND_Y;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Pass 19: original textures and lore-inspired construction for The Hole.
 *
 * <p>Based on the city language of Hayashida's manga and Kimura's anime backgrounds:
 * smoke-stained concrete tenements, water-damaged plaster, sooty red brick,
 * irregular back-street repairs, massed rooftop utilities and tangled wiring.
 * This pass does not import copyrighted builds, animation backgrounds or textures.
 *
 * <p>Everything is deterministic and written through chunk-clipped Canvas. The pass
 * only runs with worldDetailVersion >= 8, preserving existing save settings.
 */
final class LoreArchitecture19 {
    private LoreArchitecture19() {}

    private static Style style(Block wall, Block trim, Block floor, Block stairs,
                               Block door, Block glass, Block bed) {
        return new Style(Mix.of(wall, 1), Mix.of(trim, 1), Mix.of(floor, 1),
                stairs, door, glass, bed, Blocks.SPRUCE_WALL_SIGN, Blocks.IRON_BARS);
    }

    /** A *building*, not a single wall block, chooses its color and materials. */
    static Style styleFor(Canvas c, Lot lot) {
        long h = CityHash.of(c.seed(), 0xD019A2E19L, lot.rect().centerX(), lot.rect().centerZ());
        int roll = CityHash.range(h, 6);
        return switch (lot.district()) {
            case RESIDENTIAL -> switch (roll) {
                case 0, 1, 2 -> style(ModBlocks.WATER_DAMAGED_PLASTER.get(), ModBlocks.PATCHWORK_MASONRY.get(),
                        Blocks.SPRUCE_PLANKS, Blocks.STONE_BRICK_STAIRS, Blocks.SPRUCE_DOOR,
                        Blocks.GRAY_STAINED_GLASS_PANE, Blocks.GRAY_BED);
                case 3 -> style(ModBlocks.PATCHWORK_MASONRY.get(), Blocks.DARK_OAK_PLANKS,
                        Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_STAIRS, Blocks.DARK_OAK_DOOR,
                        Blocks.BROWN_STAINED_GLASS_PANE, Blocks.BROWN_BED);
                default -> style(ModBlocks.SMOKE_STAINED_CONCRETE.get(), Blocks.GRAY_CONCRETE,
                        Blocks.DARK_OAK_PLANKS, Blocks.STONE_BRICK_STAIRS, Blocks.SPRUCE_DOOR,
                        Blocks.GRAY_STAINED_GLASS_PANE, Blocks.GRAY_BED);
            };
            case MARKET -> switch (roll) {
                case 0, 1 -> style(ModBlocks.WATER_DAMAGED_PLASTER.get(), Blocks.BRICKS,
                        Blocks.SPRUCE_PLANKS, Blocks.STONE_BRICK_STAIRS, Blocks.SPRUCE_DOOR,
                        Blocks.BROWN_STAINED_GLASS_PANE, Blocks.BROWN_BED);
                case 2, 3 -> style(ModBlocks.SOOT_BLACKENED_BRICK.get(), Blocks.BROWN_TERRACOTTA,
                        Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_STAIRS, Blocks.DARK_OAK_DOOR,
                        Blocks.GRAY_STAINED_GLASS_PANE, Blocks.BROWN_BED);
                default -> style(ModBlocks.PATCHWORK_MASONRY.get(), Blocks.STRIPPED_SPRUCE_LOG,
                        Blocks.DARK_OAK_PLANKS, Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_DOOR,
                        Blocks.BROWN_STAINED_GLASS_PANE, Blocks.BROWN_BED);
            };
            case INDUSTRIAL -> roll < 4
                    ? style(ModBlocks.SOOT_BLACKENED_BRICK.get(), ModBlocks.RUSTED_INDUSTRIAL_SHEET.get(),
                            Blocks.POLISHED_BLACKSTONE, Blocks.STONE_BRICK_STAIRS, Blocks.SPRUCE_DOOR,
                            Blocks.GRAY_STAINED_GLASS_PANE, Blocks.BLACK_BED)
                    : style(ModBlocks.SMOKE_STAINED_CONCRETE.get(), ModBlocks.RUSTED_INDUSTRIAL_SHEET.get(),
                            Blocks.POLISHED_BLACKSTONE, Blocks.STONE_BRICK_STAIRS, Blocks.SPRUCE_DOOR,
                            Blocks.GRAY_STAINED_GLASS_PANE, Blocks.BLACK_BED);
            case WATERFRONT -> roll < 3
                    ? style(ModBlocks.SEEPAGE_BRICK.get(), Blocks.SPRUCE_PLANKS, Blocks.DARK_OAK_PLANKS,
                            Blocks.STONE_BRICK_STAIRS, Blocks.SPRUCE_DOOR, Blocks.BROWN_STAINED_GLASS_PANE, Blocks.BROWN_BED)
                    : style(ModBlocks.SMOKE_STAINED_CONCRETE.get(), ModBlocks.RUSTED_INDUSTRIAL_SHEET.get(),
                            Blocks.SPRUCE_PLANKS, Blocks.STONE_BRICK_STAIRS, Blocks.SPRUCE_DOOR,
                            Blocks.GRAY_STAINED_GLASS_PANE, Blocks.GRAY_BED);
            case DERELICT -> roll < 3
                    ? style(ModBlocks.PATCHWORK_MASONRY.get(), ModBlocks.SOOT_BLACKENED_BRICK.get(),
                            Blocks.DARK_OAK_PLANKS, Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_DOOR,
                            Blocks.GRAY_STAINED_GLASS_PANE, Blocks.BROWN_BED)
                    : style(ModBlocks.WATER_DAMAGED_PLASTER.get(), ModBlocks.SMOKE_STAINED_CONCRETE.get(),
                            Blocks.SPRUCE_PLANKS, Blocks.STONE_BRICK_STAIRS, Blocks.SPRUCE_DOOR,
                            Blocks.BROWN_STAINED_GLASS_PANE, Blocks.GRAY_BED);
            case GRAVEYARD -> style(roll < 3 ? ModBlocks.SOOT_BLACKENED_BRICK.get() : ModBlocks.SMOKE_STAINED_CONCRETE.get(),
                    Blocks.CRACKED_DEEPSLATE_BRICKS, Blocks.DARK_OAK_PLANKS,
                    Blocks.STONE_BRICK_STAIRS, Blocks.DARK_OAK_DOOR, Blocks.GRAY_STAINED_GLASS_PANE, Blocks.BLACK_BED);
            case OUTSKIRTS -> style(roll < 3 ? ModBlocks.PATCHWORK_MASONRY.get() : ModBlocks.SEEPAGE_BRICK.get(),
                    Blocks.STRIPPED_SPRUCE_LOG, Blocks.SPRUCE_PLANKS,
                    Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_DOOR, Blocks.BROWN_STAINED_GLASS_PANE, Blocks.BROWN_BED);
        };
    }

    /**
     * Start each lot on a legible base. Previous versions hashed every block into
     * a patchwork mosaic; keep wear patches for actual *damage*, not whole surfaces.
     */
    static void foundation(Canvas c, Lot lot) {
        Block ground = switch (lot.district()) {
            case MARKET -> Blocks.POLISHED_ANDESITE;
            case RESIDENTIAL -> Blocks.PACKED_MUD;
            case INDUSTRIAL -> Blocks.POLISHED_BLACKSTONE;
            case WATERFRONT -> Blocks.STONE_BRICKS;
            case DERELICT -> Blocks.COARSE_DIRT;
            case GRAVEYARD -> Blocks.CRACKED_STONE_BRICKS;
            case OUTSKIRTS -> Blocks.GRAVEL;
        };
        c.fill(lot.rect(), STREET_Y - 3, STREET_Y, ground);
        // Large, sparse patches read as erosion instead of a uniformly noisy pattern.
        for (int x = lot.rect().x0(); x <= lot.rect().x1(); x++) {
            for (int z = lot.rect().z0(); z <= lot.rect().z1(); z++) {
                long patch = CityHash.of(c.seed(), 0x19F00D7L,
                        Math.floorDiv(x, 5), Math.floorDiv(z, 5));
                if (CityHash.range(patch, 19) == 0 && CityHash.range(c.hash(x, STREET_Y, z), 3) != 0) {
                    c.set(x, STREET_Y, z, lot.district() == District.WATERFRONT
                            ? Blocks.MOSSY_STONE_BRICKS : Blocks.PACKED_MUD);
                }
            }
        }
    }

    /** Legible surfacing: tar road, brick lane, cobbled alleys, and real curb gutters. */
    static void streetSurface(Canvas c, CityPlan plan, int x, int z, Line xb, Line zb) {
        District district = plan.districtAt(x, z);
        long patch = CityHash.of(plan.seed, 0x19B0A1DL, Math.floorDiv(x, 5), Math.floorDiv(z, 5));
        boolean worn = CityHash.range(patch, 23) == 0;
        if (xb == null && zb == null) {
            Block lane = switch (district) {
                case MARKET -> Blocks.STONE_BRICKS;
                case RESIDENTIAL -> Blocks.COBBLED_DEEPSLATE;
                case INDUSTRIAL -> Blocks.POLISHED_BLACKSTONE;
                case WATERFRONT -> Blocks.STONE_BRICKS;
                case DERELICT -> Blocks.COARSE_DIRT;
                case GRAVEYARD -> Blocks.CRACKED_STONE_BRICKS;
                case OUTSKIRTS -> Blocks.PACKED_MUD;
            };
            c.set(x, STREET_Y, z, worn ? Blocks.GRAVEL : lane);
            return;
        }
        if (xb != null && zb != null) {
            // Plain junctions instead of chessboard striping; never overwrite rail blocks at GROUND_Y.
            if (xb.kind() != Line.Kind.CANAL && zb.kind() != Line.Kind.CANAL)
                c.set(x, STREET_Y, z, worn ? Blocks.GRAY_TERRACOTTA : Blocks.GRAY_CONCRETE);
            return;
        }
        Line band = xb != null ? xb : zb;
        if (band.kind() == Line.Kind.CANAL) return;
        int offset = xb != null ? Math.abs(x - xb.center() - plan.xAvenueOffset(xb, z))
                : Math.abs(z - zb.center() - plan.zAvenueOffset(zb, x));
        int curb = band.halfWidth() - 2;
        if (offset > curb) {
            Block pavement = district == District.WATERFRONT ? Blocks.STONE_BRICKS :
                    district == District.INDUSTRIAL ? Blocks.POLISHED_BLACKSTONE_BRICKS : Blocks.POLISHED_ANDESITE;
            c.set(x, STREET_Y, z, worn ? Blocks.CRACKED_STONE_BRICKS : pavement);
        } else {
            c.set(x, STREET_Y, z, worn ? Blocks.GRAY_TERRACOTTA : Blocks.GRAY_CONCRETE);
        }
        // Sparse drains rather than a uniform patchwork of manholes.
        int along = xb != null ? z : x;
        if (offset == curb && Math.floorMod(along + band.center(), 21) == 7) {
            c.set(x, STREET_Y, z, Blocks.IRON_TRAPDOOR);
        }
    }

    /** Original, generic urban elements inspired by real architecture and art-direction references. */
    static void tower(Canvas c, Rect b, int floors, Style style, long hash, Tower.Roof roof) {
        int top = STREET_Y + floors * Tower.STOREY;
        BlockState masonry = style.trim().pick(hash);
        int pattern = CityHash.range(hash >>> 18, 4);
        // A consistent street-front base of darker material grounds the facade.
        for (int x = b.x0() + 1; x < b.x1(); x++) {
            if (Math.abs(x - b.centerX()) < 2) continue; // entry remains clear
            c.set(x, GROUND_Y, b.z1(), masonry);
        }
        // Window lintels and spandrels follow each storey rather than per-block random noise.
        for (int k = 1; k < floors; k++) {
            int y = STREET_Y + k * Tower.STOREY;
            for (int x = b.x0() + 1; x < b.x1(); x++) {
                if (Math.floorMod(x - b.x0(), 6) != 0) c.set(x, y + 1, b.z1() + 1, masonry);
            }
        }
        // Narrow service risers and rainwater drains attach to side walls.
        if (b.depth() >= 10) {
            int z = b.z0() + 2;
            BlockState metal = Blocks.CHAIN.defaultBlockState();
            for (int y = GROUND_Y + 2; y < top; y++) c.set(b.x1() + 1, y, z, metal);
            c.set(b.x1() + 1, top, z, Blocks.IRON_TRAPDOOR);
        }
        // Exposed utility trunking and old ventilation boxes distinguish dense Hole tenements.
        // All fixtures are outside the north wall and above walking height, away from the south entrance.
        if (b.width() >= 14 && floors >= 2 && pattern != 0) {
            int fx = b.x0() + 4;
            for (int k = 1; k < floors; k += 2) {
                int y = STREET_Y + k * Tower.STOREY + 1;
                c.set(fx, y + 1, b.z0() - 1, Blocks.IRON_TRAPDOOR);
                c.set(fx + 1, y + 1, b.z0() - 1, Blocks.IRON_BARS);
            }
        }
        if (b.width() >= 17 && floors >= 3 && pattern == 1) {
            // Rusting corner downpipe and elevated rainwater reservoir.
            for (int y = GROUND_Y + 3; y < top; y++)
                c.set(b.x0() - 1, y, b.z0() + 3, Blocks.CHAIN);
            c.set(b.x0() - 1, GROUND_Y + 2, b.z0() + 3, Blocks.CAULDRON);
        }
        // Rooftop aerials, water tanks, vents; avoid the stair landing on the north-west corner.
        if (b.width() >= 15 && b.depth() >= 12) {
            int x = b.x1() - 4;
            int z = b.z1() - 4;
            if (roof == Tower.Roof.PLAIN && pattern < 2) {
                c.fill(x, top + 2, z, x + 2, top + 3, z + 2,
                        pattern == 0 ? Blocks.CAULDRON : Blocks.WAXED_EXPOSED_CUT_COPPER);
                for (int xx : new int[]{x, x+2})
                    for (int zz : new int[]{z,z+2}) c.set(xx, top + 1, zz, Blocks.IRON_BARS);
            }
            if (pattern == 3) {
                c.fill(x + 1, top + 2, z, x + 1, top + 6, z, Blocks.IRON_BARS);
                c.set(x + 1, top + 7, z, Blocks.LIGHTNING_ROD);
            }
        }
        // New balconies are sparse and sit on the street-facing elevation at alternate storeys.
        if (floors >= 4 && b.width() >= 15 && pattern == 2) {
            for (int k = 1; k < floors; k += 2) {
                int y = STREET_Y + k * Tower.STOREY;
                c.fill(b.x1() - 5, y + 1, b.z1() + 1, b.x1() - 2, y + 1, b.z1() + 1, Blocks.DARK_OAK_SLAB);
                for (int xx = b.x1() - 5; xx <= b.x1() - 2; xx++)
                    c.set(xx, y + 2, b.z1() + 1, Blocks.IRON_BARS);
            }
        }
    }

    /** Street-market facade with faded signs and mostly muted fabric, not rainbow stripes. */
    static void shopfront(Canvas c, Rect b, long hash) {
        if (b.width() < 12) return;
        int profile = CityHash.range(hash >>> 20, 5);
        Block awning = switch (profile) {
            case 0 -> Blocks.RED_WOOL;
            case 1 -> Blocks.BROWN_WOOL;
            case 2 -> Blocks.GRAY_WOOL;
            case 3 -> Blocks.GREEN_WOOL;
            default -> Blocks.GREEN_WOOL;
        };
        for (int x = b.x0() + 2; x <= b.x1() - 2; x++) {
            c.set(x, GROUND_Y + 4, b.z1() + 2, awning);
        }
        int x = b.x0() + 2;
        String[] labels = {"DINER", "SCRAP", "REPAIR", "NOODLES", "SUPPLY"};
        // Sign is attached outside the facade; never fill over its window or doorway.
        c.sign(x, GROUND_Y + 2, b.z1() + 1, Direction.SOUTH, Blocks.SPRUCE_WALL_SIGN,
                labels[profile], "", "", "");
    }
}
