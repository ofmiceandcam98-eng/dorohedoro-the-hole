package com.dorohedoro.hole.worldgen.city;

import static com.dorohedoro.hole.worldgen.city.CityPlan.GROUND_Y;
import static com.dorohedoro.hole.worldgen.city.CityPlan.STREET_Y;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A multi-storey building: walls, floors, a stair route from the street to the roof, windows and a door. What goes in
 * each storey is left to a {@link Furnisher}, so every building type shares the same guaranteed walkable skeleton.
 *
 * <p>Flights are stacked directly above each other along the north wall, with a corridor row beside them, so a player
 * always reaches the next flight by walking back along the corridor.
 */
final class Tower {
    static final int STOREY = 4;

    @FunctionalInterface
    interface Furnisher {
        /** @param floorY the block players stand on for storey {@code index} */
        void furnish(Canvas c, int index, Rect interior, int floorY);
    }

    enum Roof { PLAIN, TANK, GARDEN, ANTENNA }

    private Tower() {}

    /** Smallest footprint that fits a stair flight and its corridor. */
    static boolean fits(Rect box) {
        return box.width() >= STOREY + 5 && box.depth() >= 7;
    }

    static void build(Canvas c, Rect box, int floors, Style style, long hash, Roof roof, Furnisher furnisher) {
        build(c, box, floors, style, hash, roof, furnisher, k -> true);
    }

    /**
     * @param dressStoreys in v55 worlds, the storeys {@link RoomDressing71} furnishes. Homes dress every storey; shops
     *                     only the flats above the shop; landmarks and clinics none, because they have their own.
     */
    static void build(Canvas c, Rect box, int floors, Style style, long hash, Roof roof, Furnisher furnisher,
                      java.util.function.IntPredicate dressStoreys) {
        int top = STREET_Y + floors * STOREY;
        Rect interior = box.inset(1);

        c.fill(box, STREET_Y, STREET_Y, style.floor());
        c.walls(box, GROUND_Y, top + 1, style.wall());
        for (int k = 1; k <= floors; k++) {
            c.fill(interior, STREET_Y + k * STOREY, STREET_Y + k * STOREY, style.floor());
        }
        corners(c, box, top, style);

        for (int k = 0; k < floors; k++) {
            int floorY = STREET_Y + k * STOREY;
            flight(c, interior, floorY, style.stairs());
            windows(c, box, floorY, k == 0, style, hash);
            furnisher.furnish(c, k, interior, floorY);
            if (c.hasWorldDetail()) WorldDetail.roomDetails(c, interior, floorY, k, hash);
        }
        // The south door is cut last so nothing above can wall it back in.
        int doorX = box.centerX();
        // Keep new-world entrances interactable without requiring a powered iron door.
        Block entrance = c.hasWorldDetail() && style.door() == Blocks.IRON_DOOR
                ? Blocks.SPRUCE_DOOR : style.door();
        c.door(doorX, GROUND_Y, box.z1(), Direction.NORTH, entrance);
        c.clear(new Rect(doorX, box.z1() - 1, doorX, box.z1() - 1), GROUND_Y, GROUND_Y + 1);

        roofDetail(c, box, top, hash, roof);
        pipes(c, box, top, hash);
        if (c.hasWorldDetail()) {
            WorldDetail.tower(c, box, floors, style, hash, roof);
            if (c.hasArchitectureVariation()) ArchitecturePass13.tower(c, box, floors, style, hash, roof);
        }
        if (c.hasIrregularCity()) StructuralVolumes14.tower(c, box, floors, style, hash, roof);
        if (c.hasLoreArchitecture()) LoreArchitecture19.tower(c, box, floors, style, hash, roof);
        if (c.hasUrbanFabric()) UrbanFabric25.roof(c, box, top, hash, roof);
        if (c.hasOccupiedDetail39()) OccupiedRoofs39.decorate(c, box, floors, hash, roof);
        if (c.hasSkyline40()) Skyline40.rooftop(c, box, floors, hash, roof);
        if (c.hasRoomDressing71()) {
            RoomDressing71.dress(c, box, interior, floors, hash, c.hasCuratedInteriors55() ? dressStoreys : k -> true);
        }
        // Last, so no decoration pass above can block a flight.
        keepStairsWalkable(c, interior, floors, style.stairs());
    }

    private static void corners(Canvas c, Rect box, int top, Style style) {
        int[] xs = {box.x0(), box.x1()};
        int[] zs = {box.z0(), box.z1()};
        for (int x : xs) {
            for (int z : zs) {
                c.fill(x, GROUND_Y, z, x, top + 1, z, style.trim());
            }
        }
    }

    private static void flight(Canvas c, Rect interior, int floorY, Block stairs) {
        int x = interior.x0() + 1;
        int z = interior.z0();
        for (int i = 0; i < STOREY; i++) {
            c.stair(x + i, floorY + 1 + i, z, Direction.EAST, stairs);
        }
        // Headroom through the slab above: the last step sits in the slab, the three before it need it gone.
        c.fill(x, floorY + STOREY, z, x + STOREY - 2, floorY + STOREY, z, Blocks.AIR);
    }

    /**
     * Re-asserts every flight after all decoration. A player moving between steps is half a block up, with their
     * head 2.3 blocks above the lower step while still overlapping its column. So each step needs three clear
     * blocks above it, not two. Before this, the slab over the first step stopped players going up or down.
     * The bottom approach and the top landing are kept clear too, so furniture can't plug either end.
     */
    private static void keepStairsWalkable(Canvas c, Rect interior, int floors, Block stairs) {
        int x = interior.x0() + 1;
        int z = interior.z0();
        for (int k = 0; k < floors; k++) {
            int floorY = STREET_Y + k * STOREY;
            for (int i = 0; i < STOREY; i++) {
                int step = floorY + 1 + i;
                c.stair(x + i, step, z, Direction.EAST, stairs);
                c.fill(x + i, step + 1, z, x + i, step + 3, z, Blocks.AIR);
            }
            c.fill(x - 1, floorY + 1, z, x - 1, floorY + 2, z, Blocks.AIR);
            c.fill(x + STOREY, floorY + STOREY + 1, z, x + STOREY, floorY + STOREY + 2, z, Blocks.AIR);
        }
    }

    private static void windows(Canvas c, Rect box, int floorY, boolean ground, Style style, long hash) {
        int spacing = 3 + CityHash.range(hash, 2);
        int low = floorY + 2;
        int high = floorY + 3;
        for (int x = box.x0() + 2; x <= box.x1() - 2; x += spacing) {
            windowPair(c, x, low, high, box.z0(), Direction.Axis.X, style);
            if (!ground || Math.abs(x - box.centerX()) > 1) {
                windowPair(c, x, low, high, box.z1(), Direction.Axis.X, style);
            }
        }
        for (int z = box.z0() + 2; z <= box.z1() - 2; z += spacing) {
            windowPair(c, box.x0(), low, high, z, Direction.Axis.Z, style);
            windowPair(c, box.x1(), low, high, z, Direction.Axis.Z, style);
        }
    }

    private static void windowPair(Canvas c, int x, int low, int high, int z, Direction.Axis axis, Style style) {
        if (!c.contains(x, z)) {
            return;
        }
        int roll = CityHash.range(c.hash(x, low, z), 11);
        Block pane = switch (roll) {
            case 0 -> Blocks.AIR;
            case 1, 2 -> Blocks.IRON_BARS;
            case 3, 4 -> Blocks.YELLOW_STAINED_GLASS_PANE;
            default -> style.glass();
        };
        for (int y = low; y <= high; y++) {
            if (pane == Blocks.AIR) {
                c.set(x, y, z, Blocks.AIR);
            } else {
                c.pane(x, y, z, axis, pane);
            }
        }
    }

    private static void pipes(Canvas c, Rect box, int top, long hash) {
        if (box.x1() - box.x0() < 8) {
            return;
        }
        int x = box.x0() + 2 + CityHash.range(hash >>> 8, box.width() - 4);
        BlockState chain = Blocks.CHAIN.defaultBlockState();
        for (int y = GROUND_Y + 1; y <= top; y += 1) {
            c.set(x, y, box.z0() - 1, chain);
        }
    }

    private static void roofDetail(Canvas c, Rect box, int top, long hash, Roof roof) {
        int y = top + 1;
        Rect deck = box.inset(1);
        switch (roof) {
            case PLAIN -> {
                int vents = 2 + CityHash.range(hash >>> 16, 3);
                for (int i = 0; i < vents; i++) {
                    long h = CityHash.mix(hash + i);
                    int x = deck.x0() + 6 + CityHash.range(h, Math.max(1, deck.width() - 7));
                    int z = deck.z0() + 2 + CityHash.range(h >>> 20, Math.max(1, deck.depth() - 3));
                    c.set(x, y, z, Blocks.IRON_TRAPDOOR);
                }
            }
            case TANK -> {
                int x = deck.x1() - 3;
                int z = deck.z1() - 3;
                if (deck.width() > 11 && deck.depth() > 7) {
                    c.set(x, y, z, Blocks.SPRUCE_FENCE);
                    c.set(x + 2, y, z, Blocks.SPRUCE_FENCE);
                    c.set(x, y, z + 2, Blocks.SPRUCE_FENCE);
                    c.set(x + 2, y, z + 2, Blocks.SPRUCE_FENCE);
                    c.fill(x, y + 1, z, x + 2, y + 2, z + 2, Blocks.SPRUCE_PLANKS);
                    c.fill(x + 1, y + 2, z + 1, x + 1, y + 2, z + 1, Blocks.WATER);
                }
            }
            case GARDEN -> garden(c, deck, top);
            case ANTENNA -> {
                int x = deck.x1() - 2;
                int z = deck.z1() - 2;
                c.fill(x, y, z, x, y + 9, z, Blocks.IRON_BARS);
                c.set(x, y + 10, z, Blocks.LIGHTNING_ROD);
            }
        }
    }

    /** Rooftop plots: the "urban farms" that keep wheat, carrots, potatoes and beetroot obtainable. */
    private static void garden(Canvas c, Rect deck, int top) {
        int y = top;
        int x0 = deck.x0() + 7;
        int z0 = deck.z0() + 2;
        int x1 = Math.min(deck.x1(), x0 + 8);
        int z1 = Math.min(deck.z1(), z0 + 6);
        if (x1 - x0 < 4 || z1 - z0 < 2) {
            return;
        }
        BlockState soil = Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE);
        int waterX = (x0 + x1) >> 1;
        Block[] crops = {Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.BEETROOTS};
        // A beam under the channel, or the water would drain through the roof slab into the top floor.
        c.fill(waterX, y - 1, z0, waterX, y - 1, z1, Blocks.SMOOTH_STONE);
        for (int x = x0; x <= x1; x++) {
            Block crop = crops[Math.floorMod(x - x0, crops.length)];
            for (int z = z0; z <= z1; z++) {
                if (x == waterX) {
                    c.set(x, y, z, Blocks.WATER);
                    continue;
                }
                c.set(x, y, z, soil);
                if (crop instanceof CropBlock cropBlock) {
                    int age = cropBlock.getMaxAge();
                    c.set(x, y + 1, z, cropBlock.getStateForAge(CityHash.range(c.hash(x, y, z), age + 1)));
                }
            }
        }
    }

    /** Ceiling light for a room. */
    static void lamp(Canvas c, int x, int floorY, int z) {
        if (c.hasAtmosphere28()) {
            ResidentialAtmosphere28.hang(c, x, floorY, z, c.hash(x, floorY, z));
        } else {
            c.set(x, floorY + STOREY - 1, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        }
    }
}
