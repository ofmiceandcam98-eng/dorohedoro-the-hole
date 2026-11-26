package com.dorohedoro.hole.worldgen.sorcerer;

import static com.dorohedoro.hole.worldgen.sorcerer.TownArchitecture.beams;
import static com.dorohedoro.hole.worldgen.sorcerer.TownArchitecture.chair;
import static com.dorohedoro.hole.worldgen.sorcerer.TownArchitecture.chimney;
import static com.dorohedoro.hole.worldgen.sorcerer.TownArchitecture.flat;
import static com.dorohedoro.hole.worldgen.sorcerer.TownArchitecture.flight;
import static com.dorohedoro.hole.worldgen.sorcerer.TownArchitecture.frontDoor;
import static com.dorohedoro.hole.worldgen.sorcerer.TownArchitecture.gable;
import static com.dorohedoro.hole.worldgen.sorcerer.TownArchitecture.pot;
import static com.dorohedoro.hole.worldgen.sorcerer.TownArchitecture.pyramid;
import static com.dorohedoro.hole.worldgen.sorcerer.TownArchitecture.rug;
import static com.dorohedoro.hole.worldgen.sorcerer.TownArchitecture.shell;
import static com.dorohedoro.hole.worldgen.sorcerer.TownArchitecture.shelves;
import static com.dorohedoro.hole.worldgen.sorcerer.TownArchitecture.site;
import static com.dorohedoro.hole.worldgen.sorcerer.TownArchitecture.table;
import static com.dorohedoro.hole.worldgen.sorcerer.TownArchitecture.windows;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Architecture and interiors of every Sorcerers' World building. Each design is original. Every storey has a
 * purpose, furniture and light; upper floors are reached by real stair flights that alternate between the east wall
 * (even storeys, climbing towards the front) and the west wall (odd storeys, climbing towards the back), so a
 * stairwell never lands under the next flight.
 *
 * QA probes: "door" at each front door, "room" at the floor centre of every lit room, "abandoned" for rooms that are
 * deliberately dark.
 */
public final class SorcererBuildings {
    private SorcererBuildings() {}

    static final Direction N = Direction.NORTH, S = Direction.SOUTH, E = Direction.EAST, W = Direction.WEST;

    public static void paint(String id, Painter p) {
        switch (id) {
            case "townhouse" -> townhouse(p);
            case "crooked_house" -> crookedHouse(p);
            case "tenement" -> tenement(p);
            case "mask_shop" -> maskShop(p);
            case "smoke_parlor" -> smokeParlor(p);
            case "eatery" -> eatery(p);
            case "boarding_house" -> boardingHouse(p);
            case "repair_workshop" -> workshop(p);
            case "sorcerer_manor" -> manor(p);
            case "mushroom_villa" -> villa(p);
            case "shanty" -> shanty(p);
            case "clinic" -> clinic(p);
            case "market_stalls" -> market(p);
            case "courtyard" -> courtyard(p);
            case "herb_garden" -> herbGarden(p);
            case "victorian_exchange" -> exchange(p);
            default -> throw new IllegalArgumentException(id);
        }
    }

    // ------------------------------------------------------------ shared storey logic

    /** The flight that leaves storey {@code k} (base y {@code base}) for the storey above. */
    static void upstairs(Painter p, TownPalette pal, int k, int base, int sh) {
        int w = p.w(), d = p.d();
        if (k % 2 == 0) {
            flight(p, w - 2, d - 2, base, N, sh, pal.planksStairs(), pal.planks(), pal.fence(), W);
        } else {
            flight(p, 1, 2, base, S, sh, pal.planksStairs(), pal.planks(), pal.fence(), E);
        }
    }

    /** True if (x, z) on storey {@code k} is kept clear for stairs arriving from, or leaving to, another storey. */
    static boolean stairCell(Painter p, int k, int storeys, int sh, int x, int z) {
        int w = p.w(), d = p.d();
        // Leaving upwards from this storey.
        if (k < storeys - 1) {
            if (k % 2 == 0 && x == w - 2 && z >= d - 1 - sh && z <= d - 2) return true;
            if (k % 2 == 1 && x == 1 && z >= 2 && z <= 1 + sh) return true;
        }
        // Arriving from below: the well, its railing and the landing.
        if (k > 0) {
            if ((k - 1) % 2 == 0 && x >= w - 3 && x <= w - 2 && z >= d - 2 - sh && z <= d - 2) return true;
            if ((k - 1) % 2 == 1 && x >= 1 && x <= 2 && z >= 2 && z <= 2 + sh) return true;
        }
        return false;
    }

    static void hangingLantern(Painter p, int x, int y, int z) {
        p.lantern(x, y, z, true);
    }

    /** A lantern standing on a small fence table: light for rooms whose ceiling is a roof. */
    static void tableLantern(Painter p, TownPalette pal, int x, int y, int z) {
        p.set(x, y, z, pal.fence());
        p.lantern(x, y + 1, z, false);
    }

    static Block bed(TownRandom rng) {
        Block[] beds = {Blocks.RED_BED, Blocks.GRAY_BED, Blocks.BROWN_BED, Blocks.CYAN_BED, Blocks.PURPLE_BED,
                Blocks.LIGHT_GRAY_BED, Blocks.GREEN_BED, Blocks.BLUE_BED};
        return beds[rng.nextInt(beds.length)];
    }

    static Block carpet(TownRandom rng) {
        Block[] carpets = {Blocks.RED_CARPET, Blocks.GRAY_CARPET, Blocks.BROWN_CARPET, Blocks.CYAN_CARPET,
                Blocks.PURPLE_CARPET, Blocks.GREEN_CARPET, Blocks.BLACK_CARPET, Blocks.BLUE_CARPET};
        return carpets[rng.nextInt(carpets.length)];
    }

    static void kitchenRow(Painter p, TownPalette pal, int x0, int y, int z, Direction face, int length) {
        Block[] parts = {Blocks.SMOKER, Blocks.CRAFTING_TABLE, Blocks.BARREL, Blocks.CAULDRON, Blocks.FURNACE};
        for (int i = 0; i < length; i++) {
            Block b = parts[i % parts.length];
            if (b == Blocks.BARREL) p.barrel(x0 + i, y, z, Direction.UP, TownLoot.PANTRY);
            else p.set(x0 + i, y, z, Painter.facing(b, face));
        }
    }

    // ------------------------------------------------------------ residential

    /** Narrow Gothic townhouse, gable to the street: parlour and kitchen, bedroom above, a study in the third storey. */
    static void townhouse(Painter p) {
        TownRandom r = p.rng;
        TownPalette pal = TownPalette.pick(r, TownPalette.GOTHIC, TownPalette.TIMBER, TownPalette.VERDIGRIS, TownPalette.BRICK);
        int storeys = r.range(2, 3), sh = 4, w = p.w(), d = p.d();
        site(p, pal, storeys * sh + 9);
        int top = shell(p, pal, storeys, sh);
        for (int k = 0; k < storeys; k++) windows(p, pal, k * sh, sh, 3, 1, 2, k == 0);
        frontDoor(p, pal, 3, false);
        p.lantern(2, 2, -1, false);
        p.set(2, 1, -1, pal.accent());
        for (int k = 0; k + 1 < storeys; k++) upstairs(p, pal, k, k * sh, sh);

        // Ground: parlour at the front, kitchen at the back.
        table(p, 2, 0, 3, pal.fence(), Blocks.OAK_PRESSURE_PLATE);
        chair(p, 1, 0, 3, pal.planksStairs(), W);
        chair(p, 2, 0, 4, pal.planksStairs(), S);
        pot(p, 1, 0, 1, r);
        kitchenRow(p, pal, 1, 0, d - 2, N, 3);
        rug(p, 1, 0, 5, 3, 6, carpet(r));
        hangingLantern(p, 2, sh - 2, 3);
        p.probe("room", 2, 0, 4);
        p.villager(3, 0, 4, VillagerProfession.NONE);

        // First storey: bedroom.
        int b1 = sh;
        p.bed(2, b1, d - 3, S, bed(r));
        p.chest(3, b1, d - 2, N, TownLoot.HOUSE);
        rug(p, 3, b1, 2, 3, 4, carpet(r));
        if (storeys == 3) hangingLantern(p, 3, b1 + sh - 2, 2);
        else tableLantern(p, pal, 1, b1, d - 2);
        p.set(storeys == 3 ? 3 : 1, b1, 1, Blocks.BARREL);
        p.probe("room", 3, b1, 3);

        if (storeys == 3) {
            int b2 = 2 * sh;
            shelves(p, 4, b2, d - 2, 4, d - 2);
            p.set(3, b2, d - 2, Painter.facing(Blocks.LECTERN, N));
            chair(p, 3, b2, d - 3, pal.planksStairs(), N);
            p.chest(5, b2, 3, W, TownLoot.STUDY);
            tableLantern(p, pal, 4, b2, 1);
            p.probe("room", 3, b2, 4);
        }
        int ridge = gable(p, pal, top, true, true);
        chimney(p, 4, d - 1, 0, ridge + 1);
    }

    /**
     * A house that looks magicked into shape: stone ground floor, an upper storey of a different material set back on
     * one side under a lopsided hipped roof, a corner turret and mismatched windows.
     */
    static void crookedHouse(Painter p) {
        TownRandom r = p.rng;
        TownPalette low = TownPalette.pick(r, TownPalette.TUFF, TownPalette.GOTHIC, TownPalette.BRICK);
        TownPalette up = TownPalette.pick(r, TownPalette.TIMBER, TownPalette.VERDIGRIS, TownPalette.MUSHROOM);
        int w = p.w(), d = p.d();
        site(p, low, 19);
        // Ground storey, five tall.
        p.walls(0, 0, w - 1, d - 1, 0, 4, low.wall());
        for (int y = 0; y < 5; y++) for (int[] c : new int[][]{{0, 0}, {w - 1, 0}, {0, d - 1}, {w - 1, d - 1}}) p.set(c[0], y, c[1], low.trim());
        p.fill(1, 4, 1, w - 1, 4, d - 2, up.floor());
        windows(p, low, 0, 5, 3, 1, 2, true);
        frontDoor(p, low, 3, false);
        p.lantern(4, 2, -1, false);
        p.set(4, 1, -1, low.accent());
        // Upper storey, set in from the west, different materials.
        p.walls(1, 0, w - 1, d - 1, 5, 8, up.wall());
        for (int y = 5; y <= 8; y++) for (int[] c : new int[][]{{1, 0}, {w - 1, 0}, {1, d - 1}, {w - 1, d - 1}}) p.set(c[0], y, c[1], up.trim());
        for (int z = 0; z < d; z++) p.set(0, 5, z, Painter.stair(low.roofStairs(), E, false));
        for (int i = 0; i < 4; i++) {
            int x = 2 + r.nextInt(w - 4), z = 2 + r.nextInt(d - 4);
            p.set(x, 6 + r.nextInt(2), 0, up.window());
            p.set(x, 6, d - 1, up.window());
            p.set(w - 1, 6 + r.nextInt(2), z, up.window());
            p.set(1, 6, z, up.window());
        }
        // Stairs, then the rooms.
        flight(p, w - 2, d - 2, 0, N, 5, low.planksStairs(), low.planks(), low.fence(), W);
        table(p, 2, 0, 3, low.fence(), Blocks.OAK_PRESSURE_PLATE);
        chair(p, 1, 0, 3, low.planksStairs(), W);
        chair(p, 3, 0, 3, low.planksStairs(), E);
        kitchenRow(p, low, 1, 0, d - 2, N, 3);
        p.set(4, 0, d - 2, Blocks.CAULDRON);
        hangingLantern(p, 3, 3, 4);
        p.probe("room", 3, 0, 5);
        p.villager(2, 0, 5, VillagerProfession.NONE);

        p.bed(3, 5, d - 3, S, bed(r));
        p.chest(4, 5, d - 2, N, TownLoot.HOUSE);
        p.set(2, 5, 1, Blocks.BOOKSHELF);
        p.set(3, 5, 1, Blocks.BOOKSHELF);
        tableLantern(p, up, 2, 5, d - 2);
        rug(p, 3, 5, 2, 4, 4, carpet(r));
        p.probe("room", 3, 5, 4);

        int peak = pyramid(p, low, 1, 0, w - 1, d - 1, 9);
        // Turret on the front east corner.
        p.walls(w - 2, 0, w - 1, 1, 9, 11, up.wall());
        p.set(w - 2, 10, 0, up.window());
        p.fill(w - 2, 12, 0, w - 1, 12, 1, low.roofFull());
        p.set(w - 2, 13, 0, Painter.slab(low.roofSlab(), false));
        chimney(p, 2, d - 1, 0, Math.max(peak, 12) + 1);
    }

    /** City tenement: lobby and laundry, then one flat per storey, balconies and a rooftop water tank. */
    static void tenement(Painter p) {
        TownRandom r = p.rng;
        TownPalette pal = TownPalette.pick(r, TownPalette.BRICK, TownPalette.TUFF, TownPalette.VERDIGRIS);
        int storeys = r.range(3, 4), sh = 4, w = p.w(), d = p.d(), door = 5;
        site(p, pal, storeys * sh + 6);
        int top = shell(p, pal, storeys, sh);
        for (int k = 0; k < storeys; k++) windows(p, pal, k * sh, sh, door, 1, 2, k == 0);
        frontDoor(p, pal, door, false);
        p.sign(door, 3, -1, pal.wallSign(), N, "", "TENEMENT", "rooms by the week", "");
        for (int k = 0; k + 1 < storeys; k++) upstairs(p, pal, k, k * sh, sh);

        for (int k = 0; k < storeys; k++) {
            int b = k * sh;
            // Partition between the front and back rooms, open at both stair landings and a central doorway.
            for (int x = 2; x <= w - 3; x++) if (x != door) p.fill(x, b, 6, x, b + sh - 2, 6, pal.planks());
            hangingLantern(p, 4, b + sh - 2, 3);
            hangingLantern(p, 6, b + sh - 2, 8);
            if (k == 0) {
                // Lobby: benches, plants, a notice board, the laundry behind.
                chair(p, 2, 0, 2, pal.planksStairs(), W);
                chair(p, 2, 0, 3, pal.planksStairs(), W);
                pot(p, 8, 0, 1, r);
                pot(p, 2, 0, 1, r);
                p.set(7, 0, 4, Blocks.CARTOGRAPHY_TABLE);
                p.set(2, 0, 9, Blocks.CAULDRON);
                p.barrel(3, 0, 10, N, TownLoot.HOUSE);
                p.set(4, 0, 10, Painter.facing(Blocks.COMPOSTER, N));
                p.villager(6, 0, 3, VillagerProfession.CARTOGRAPHER);
                p.probe("room", 5, 0, 3);
                p.probe("room", 4, 0, 8);
                continue;
            }
            // A flat: living room at the front, bedroom behind.
            int tx = 5;
            table(p, tx, b, 3, pal.fence(), Blocks.OAK_PRESSURE_PLATE);
            chair(p, tx - 1, b, 3, pal.planksStairs(), W);
            chair(p, tx + 1, b, 3, pal.planksStairs(), E);
            p.set(8, b, 1, Painter.facing(Blocks.SMOKER, S));
            p.barrel(7, b, 1, S, TownLoot.PANTRY);
            p.bed(4, b, d - 3, S, bed(r));
            p.chest(3, b, d - 2, N, TownLoot.HOUSE);
            if (!stairCell(p, k, storeys, sh, 6, d - 2)) p.set(6, b, d - 2, Blocks.BOOKSHELF);
            rug(p, 4, b, 7, 6, 8, carpet(r));
            p.probe("room", 4, b, 3);
            p.probe("room", 5, b, 8);
            p.villager(4, b, 8, VillagerProfession.NONE);
            // Balcony on the street side.
            p.fill(door - 1, b - 1, -1, door + 1, b - 1, -1, Painter.slab(pal.planksSlab(), true));
            for (int x = door - 1; x <= door + 1; x++) p.set(x, b, -1, pal.fence());
            p.set(door, b, 0, pal.window());
            p.set(door, b + 1, 0, pal.window());
        }
        int y = flat(p, pal, top);
        // Water tank on legs.
        for (int[] leg : new int[][]{{6, 7}, {8, 7}, {6, 9}, {8, 9}}) p.set(leg[0], y, leg[1], pal.fence());
        p.fill(6, y + 1, 7, 8, y + 2, 9, pal.planks());
        p.fill(6, y + 3, 7, 8, y + 3, 9, Painter.slab(pal.planksSlab(), false));
        chimney(p, 2, d - 1, 0, y + 1);
        chimney(p, w - 3, d - 1, 0, y + 1);
    }

    // ------------------------------------------------------------ commerce

    /** Mask maker: masks on display shelves and a cutting and dyeing workshop behind the counter; the maker lives above. */
    static void maskShop(Painter p) {
        TownRandom r = p.rng;
        TownPalette pal = TownPalette.pick(r, TownPalette.VERDIGRIS, TownPalette.GOTHIC, TownPalette.TIMBER);
        int sh = 4, w = p.w(), d = p.d(), door = 4;
        site(p, pal, 2 * sh + 6);
        int top = shell(p, pal, 2, sh);
        windows(p, pal, sh, sh, door, 1, 2, false);
        // Shopfront: two wide display windows.
        p.fill(1, 1, 0, 3, 2, 0, pal.window());
        p.fill(5, 1, 0, 7, 2, 0, pal.window());
        frontDoor(p, pal, door, false);
        p.sign(door, 3, -1, pal.wallSign(), N, "", "MASKS", "made to measure", "");
        upstairs(p, pal, 0, 0, sh);

        // Display shelves of masks: carved faces and skulls on slab shelves.
        for (int z = 2; z <= 6; z++) {
            p.set(1, 0, z, Painter.slab(pal.planksSlab(), true));
            p.set(1, 1, z, mask(r, E));
        }
        for (int x = 2; x <= 3; x++) {
            p.set(x, 0, 1, Painter.slab(pal.planksSlab(), true));
            p.set(x, 1, 1, mask(r, S));
        }
        // Counter across the shop, gap at the west end.
        for (int x = 3; x <= 6; x++) p.set(x, 0, 5, Painter.stair(pal.planksStairs(), N, true));
        p.set(5, 1, 5, Painter.facing(Blocks.LOOM, N));
        // Workshop behind the counter.
        p.set(2, 0, d - 2, Painter.facing(Blocks.LOOM, N));
        p.set(3, 0, d - 2, Blocks.CAULDRON);
        p.set(4, 0, d - 2, Blocks.CRAFTING_TABLE);
        p.chest(5, 0, d - 2, N, TownLoot.MASK_SHOP);
        p.set(1, 0, 7, Blocks.WHITE_WOOL);
        hangingLantern(p, 3, sh - 2, 3);
        hangingLantern(p, 4, sh - 2, 7);
        p.probe("room", 4, 0, 3);
        p.villager(4, 0, 6, VillagerProfession.LEATHERWORKER);

        // The maker's flat.
        int b = sh;
        p.bed(2, b, d - 3, S, bed(r));
        p.chest(1, b, d - 2, N, TownLoot.HOUSE);
        table(p, 3, b, 3, pal.fence(), Blocks.OAK_PRESSURE_PLATE);
        chair(p, 2, b, 3, pal.planksStairs(), W);
        tableLantern(p, pal, 1, b, 1);
        p.set(4, b, d - 2, mask(r, N));
        p.probe("room", 3, b, 4);
        int ridge = gable(p, pal, top, false, false);
        chimney(p, 2, d - 1, 0, ridge + 1);
    }

    /** A mask: carved face or skull, looking towards {@code face}. */
    static BlockState mask(TownRandom r, Direction face) {
        return switch (r.nextInt(4)) {
            case 0 -> Painter.facing(Blocks.CARVED_PUMPKIN, face);
            case 1 -> Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, rotationOf(face));
            case 2 -> Blocks.WITHER_SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, rotationOf(face));
            default -> Blocks.ZOMBIE_HEAD.defaultBlockState().setValue(SkullBlock.ROTATION, rotationOf(face));
        };
    }

    private static int rotationOf(Direction face) {
        return switch (face) {
            case SOUTH -> 0;
            case WEST -> 4;
            case NORTH -> 8;
            default -> 12;
        };
    }

    /** Smoke dealer: bottled smoke behind glass, a condensing still and a vented chimney. */
    static void smokeParlor(Painter p) {
        TownRandom r = p.rng;
        TownPalette pal = TownPalette.pick(r, TownPalette.GOTHIC, TownPalette.TUFF, TownPalette.VERDIGRIS);
        int sh = 4, w = p.w(), d = p.d(), door = 4;
        site(p, pal, 2 * sh + 9);
        int top = shell(p, pal, 2, sh);
        windows(p, pal, 0, sh, door, 1, 2, true);
        windows(p, pal, sh, sh, door, 1, 2, false);
        frontDoor(p, pal, door, false);
        p.sign(door, 3, -1, pal.wallSign(), N, "", "SMOKE", "bottled & fresh", "");
        upstairs(p, pal, 0, 0, sh);

        // Jars of smoke behind glass along the west wall.
        for (int z = 1; z <= 5; z++) {
            p.set(1, 0, z, Blocks.TINTED_GLASS);
            p.set(1, 1, z, z % 2 == 0 ? Blocks.DECORATED_POT.defaultBlockState() : Blocks.GLASS.defaultBlockState());
        }
        // Counter with the still.
        for (int x = 3; x <= 5; x++) p.set(x, 0, 4, Painter.stair(pal.planksStairs(), N, true));
        p.set(3, 1, 4, Blocks.BREWING_STAND);
        p.set(5, 1, 4, Blocks.BREWING_STAND);
        p.set(2, 0, 6, Blocks.CAULDRON);
        p.barrel(3, 0, 6, N, TownLoot.SMOKE_PARLOR);
        p.chest(4, 0, 6, N, TownLoot.SMOKE_PARLOR);
        chair(p, 4, 0, 2, pal.planksStairs(), N);
        hangingLantern(p, 3, sh - 2, 2);
        p.soulLantern(4, sh - 2, 5, true);
        p.probe("room", 4, 0, 3);
        p.villager(4, 0, 5, VillagerProfession.CLERIC);

        // Storeroom and a cot upstairs.
        int b = sh;
        for (int x = 1; x <= 3; x++) p.barrel(x, b, d - 2, N, x == 2 ? TownLoot.SMOKE_PARLOR : null);
        p.set(1, b, d - 3, Blocks.BARREL);
        p.bed(4, b, 2, N, bed(r));
        tableLantern(p, pal, 2, b, 1);
        p.probe("room", 3, b, 4);
        int ridge = gable(p, pal, top, true, true);
        chimney(p, w - 3, d - 1, 0, ridge + 2);
    }

    /** Noodle bar: dining room, counter, and a working kitchen with smokers and a grill. */
    static void eatery(Painter p) {
        TownRandom r = p.rng;
        TownPalette pal = TownPalette.pick(r, TownPalette.TIMBER, TownPalette.BRICK, TownPalette.VERDIGRIS, TownPalette.PATCHWORK);
        int sh = 5, w = p.w(), d = p.d(), door = 5;
        site(p, pal, sh + 7);
        int top = shell(p, pal, 1, sh);
        windows(p, pal, 0, sh, door, 2, 2, true);
        frontDoor(p, pal, door, true);
        p.sign(door - 1, 3, -1, pal.wallSign(), N, "", "EATERY", "noodles - dumplings", "");
        p.lantern(door + 2, 2, -1, false);
        p.set(door + 2, 1, -1, pal.accent());

        // Dining room: three tables.
        for (int[] t : new int[][]{{2, 2}, {8, 2}, {2, 4}}) {
            table(p, t[0], 0, t[1], pal.fence(), Blocks.OAK_PRESSURE_PLATE);
            chair(p, t[0] - 1 < 1 ? t[0] + 1 : t[0] - 1, 0, t[1], pal.planksStairs(), t[0] - 1 < 1 ? E : W);
        }
        // Counter with stools.
        for (int x = 3; x <= 8; x++) p.set(x, 0, 5, Painter.stair(pal.planksStairs(), S, true));
        for (int x = 4; x <= 7; x += 3) p.set(x, 0, 4, Painter.slab(pal.planksSlab(), false));
        // Kitchen.
        p.set(1, 0, d - 2, Painter.facing(Blocks.SMOKER, N));
        p.set(2, 0, d - 2, Painter.facing(Blocks.SMOKER, N));
        p.set(3, 0, d - 2, Painter.facing(Blocks.FURNACE, N));
        p.set(4, 0, d - 2, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        p.set(5, 0, d - 2, Blocks.CAULDRON);
        p.barrel(6, 0, d - 2, N, TownLoot.KITCHEN);
        p.chest(7, 0, d - 2, N, TownLoot.KITCHEN);
        p.set(8, 0, d - 2, Blocks.CRAFTING_TABLE);
        p.set(9, 0, d - 2, Blocks.BARREL);
        p.set(1, 0, 6, Blocks.HAY_BLOCK);
        beams(p, pal, sh - 1, 3, 6);
        for (int x = 2; x <= 8; x += 3) hangingLantern(p, x, sh - 2, 3);
        hangingLantern(p, 6, sh - 2, 6);
        p.probe("room", 5, 0, 3);
        p.probe("room", 6, 0, 6);
        p.villager(6, 0, 6, VillagerProfession.BUTCHER);
        p.villager(5, 0, 3, VillagerProfession.NONE);
        int ridge = gable(p, pal, top, false, false);
        chimney(p, 4, d - 1, 0, ridge + 1);
    }

    /** Boarding house: reception and lounge, then two connected rooms per storey for travellers. */
    static void boardingHouse(Painter p) {
        TownRandom r = p.rng;
        TownPalette pal = TownPalette.pick(r, TownPalette.TIMBER, TownPalette.VERDIGRIS, TownPalette.BRICK);
        int storeys = 3, sh = 4, w = p.w(), d = p.d(), door = 5;
        site(p, pal, storeys * sh + 7);
        int top = shell(p, pal, storeys, sh);
        for (int k = 0; k < storeys; k++) windows(p, pal, k * sh, sh, door, 1, 2, k == 0);
        frontDoor(p, pal, door, false);
        p.sign(door, 3, -1, pal.wallSign(), N, "", "BOARDING", "beds - meals", "");
        for (int k = 0; k + 1 < storeys; k++) upstairs(p, pal, k, k * sh, sh);

        // Reception desk and lounge.
        for (int x = 7; x <= 9; x++) p.set(x, 0, 3, Painter.stair(pal.planksStairs(), N, true));
        p.set(8, 1, 3, Painter.facing(Blocks.LECTERN, N));
        chair(p, 2, 0, 2, pal.planksStairs(), W);
        chair(p, 2, 0, 3, pal.planksStairs(), W);
        table(p, 3, 0, 2, pal.fence(), Blocks.OAK_PRESSURE_PLATE);
        shelves(p, 1, 0, d - 2, 3, d - 2);
        kitchenRow(p, pal, 4, 0, d - 2, N, 4);
        hangingLantern(p, 3, sh - 2, 3);
        hangingLantern(p, 7, sh - 2, 6);
        p.probe("room", 5, 0, 3);
        p.villager(8, 0, 4, VillagerProfession.LIBRARIAN);

        for (int k = 1; k < storeys; k++) {
            int b = k * sh;
            // Partition with a doorway near the front, so both stair landings reach both rooms.
            for (int z = 1; z <= d - 2; z++) if (z < 2 || z > 3) p.fill(6, b, z, 6, b + sh - 2, z, pal.planks());
            p.bed(3, b, d - 3, S, bed(r));
            p.chest(4, b, d - 2, N, TownLoot.LODGING);
            p.bed(8, b, 2, N, bed(r));
            p.bed(8, b, 4, S, bed(r));
            p.chest(9, b, 3, W, TownLoot.LODGING);
            hangingLantern(p, 3, b + sh - 2, 4);
            hangingLantern(p, 8, b + sh - 2, 6);
            p.probe("room", 3, b, 4);
            p.probe("room", 8, b, 6);
        }
        p.villager(3, sh, 4, VillagerProfession.NONE);
        int ridge = gable(p, pal, top, false, false);
        chimney(p, 2, d - 1, 0, ridge + 1);
    }

    // ------------------------------------------------------------ work and civic

    /** Repair shop: forge, anvil, grinding and smithing benches, racks and a hoist chain under a tall roof. */
    static void workshop(Painter p) {
        TownRandom r = p.rng;
        TownPalette pal = TownPalette.pick(r, TownPalette.WORKS, TownPalette.BRICK, TownPalette.TUFF);
        int sh = 6, w = p.w(), d = p.d(), door = 5;
        site(p, pal, sh + 7);
        int top = shell(p, pal, 1, sh);
        windows(p, pal, 0, sh, door, 2, 3, true);
        frontDoor(p, pal, door, true);
        p.sign(door - 1, 3, -1, pal.wallSign(), N, "", "REPAIRS", "tools - doors - locks", "");

        p.set(2, 0, d - 2, Painter.facing(Blocks.BLAST_FURNACE, N));
        p.set(3, 0, d - 2, Painter.facing(Blocks.FURNACE, N));
        p.set(4, 0, d - 2, Blocks.CAULDRON);
        p.set(5, 0, d - 3, Blocks.ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, E));
        p.set(1, 0, 3, Blocks.SMITHING_TABLE);
        p.set(1, 0, 4, Blocks.GRINDSTONE.defaultBlockState().setValue(GrindstoneBlock.FACE, AttachFace.FLOOR)
                .setValue(GrindstoneBlock.FACING, E));
        p.set(1, 0, 5, Painter.facing(Blocks.STONECUTTER, E));
        p.set(1, 0, 6, Blocks.CRAFTING_TABLE);
        p.chest(w - 2, 0, d - 2, W, TownLoot.WORKSHOP);
        p.barrel(w - 2, 0, d - 3, W, TownLoot.WORKSHOP);
        p.set(w - 2, 0, d - 4, Blocks.BARREL);
        p.set(w - 2, 1, d - 2, Blocks.BARREL);
        for (int z = 2; z <= 4; z++) p.set(w - 2, 0, z, Blocks.IRON_BARS);
        // Hoist: a chain hanging from the ridge with a lantern.
        p.fill(5, 3, 5, 5, sh + 5, 5, Blocks.CHAIN);
        beams(p, pal, sh - 1, 3, 7);
        p.lantern(5, 2, 5, true);
        hangingLantern(p, 3, sh - 1, 3);
        hangingLantern(p, 8, sh - 1, 7);
        p.probe("room", 4, 0, 4);
        p.villager(3, 0, 5, VillagerProfession.TOOLSMITH);
        p.villager(6, 0, 7, VillagerProfession.ARMORER);
        int ridge = gable(p, pal, top, false, false);
        chimney(p, 2, d - 1, 0, ridge + 2);
    }

    /** Clinic: waiting room, treatment room with patient beds; the doctor lives upstairs. */
    static void clinic(Painter p) {
        TownRandom r = p.rng;
        TownPalette pal = TownPalette.pick(r, TownPalette.TUFF, TownPalette.VERDIGRIS);
        int sh = 4, w = p.w(), d = p.d(), door = 4;
        site(p, pal, 2 * sh + 7);
        int top = shell(p, pal, 2, sh);
        windows(p, pal, 0, sh, door, 1, 2, true);
        windows(p, pal, sh, sh, door, 1, 2, false);
        frontDoor(p, pal, door, false);
        p.sign(door, 3, -1, pal.wallSign(), N, "", "CLINIC", "bones set - wounds", "smoke sickness");
        // A red cross over the door.
        p.set(door, sh + 2, 0, Blocks.RED_CONCRETE);
        p.set(door, sh + 1, 0, Blocks.RED_CONCRETE);
        p.set(door, sh + 3, 0, Blocks.RED_CONCRETE);
        p.set(door - 1, sh + 2, 0, Blocks.RED_CONCRETE);
        p.set(door + 1, sh + 2, 0, Blocks.RED_CONCRETE);
        upstairs(p, pal, 0, 0, sh);

        // Waiting room.
        for (int z = 1; z <= 3; z++) chair(p, 1, 0, z, pal.planksStairs(), W);
        pot(p, 1, 0, 4, r);
        // Partition with a doorway.
        for (int x = 1; x <= w - 3; x++) if (x != 3) p.fill(x, 0, 5, x, sh - 2, 5, Blocks.WHITE_CONCRETE);
        // Treatment room.
        p.bed(1, 0, 6, S, Blocks.WHITE_BED);
        p.bed(4, 0, 6, S, Blocks.WHITE_BED);
        p.set(2, 0, d - 2, Blocks.BREWING_STAND);
        p.set(3, 0, d - 2, Blocks.CAULDRON);
        p.chest(5, 0, d - 2, N, TownLoot.CLINIC);
        p.set(6, 0, d - 2, Blocks.BARREL);
        hangingLantern(p, 4, sh - 2, 2);
        hangingLantern(p, 3, sh - 2, 7);
        p.probe("room", 4, 0, 2);
        p.probe("room", 3, 0, 7);
        p.villager(3, 0, 3, VillagerProfession.CLERIC);

        int b = sh;
        p.bed(2, b, d - 3, S, bed(r));
        p.chest(1, b, d - 2, N, TownLoot.CLINIC);
        shelves(p, 3, b, d - 2, 4, d - 2);
        table(p, 3, b, 3, pal.fence(), Blocks.OAK_PRESSURE_PLATE);
        tableLantern(p, pal, 2, b, 1);
        p.probe("room", 4, b, 4);
        int ridge = gable(p, pal, top, false, false);
        chimney(p, 2, d - 1, 0, ridge + 1);
    }

    // ------------------------------------------------------------ wealth

    /**
     * A sorcerer's manor: entrance hall, dining room and kitchen; a library and study; bedrooms under a steep roof;
     * a corner tower rising above it all.
     */
    static void manor(Painter p) {
        TownRandom r = p.rng;
        TownPalette pal = TownPalette.pick(r, TownPalette.GOTHIC, TownPalette.VERDIGRIS, TownPalette.TUFF);
        int storeys = 3, sh = 5, w = p.w(), d = p.d(), door = 7;
        site(p, pal, 30);
        int top = shell(p, pal, storeys, sh);
        for (int k = 0; k < storeys; k++) windows(p, pal, k * sh, sh, door, 1, 2, k == 0);
        frontDoor(p, pal, door, false);
        p.set(door - 1, 0, -1, pal.accent());
        p.set(door + 1, 0, -1, pal.accent());
        p.lantern(door - 1, 1, -1, false);
        p.lantern(door + 1, 1, -1, false);
        for (int k = 0; k + 1 < storeys; k++) upstairs(p, pal, k, k * sh, sh);

        // Ground: hall in the middle, dining to the west, kitchen and pantry at the back east.
        rug(p, 6, 0, 1, 8, 6, Blocks.RED_CARPET);
        for (int x = 2; x <= 4; x++) table(p, x, 0, 4, pal.fence(), Blocks.OAK_PRESSURE_PLATE);
        for (int x = 2; x <= 4; x++) {
            chair(p, x, 0, 3, pal.planksStairs(), N);
            chair(p, x, 0, 5, pal.planksStairs(), S);
        }
        kitchenRow(p, pal, 7, 0, d - 2, N, 5);
        p.chest(6, 0, d - 2, N, TownLoot.KITCHEN);
        pot(p, 1, 0, 1, r);
        hangingLantern(p, 3, sh - 2, 4);
        hangingLantern(p, 7, sh - 2, 3);
        hangingLantern(p, 9, sh - 2, 9);
        p.probe("room", 7, 0, 3);
        p.probe("room", 3, 0, 7);
        p.villager(7, 0, 5, VillagerProfession.NONE);

        // First storey: library.
        int b1 = sh;
        for (int x = 3; x <= w - 4; x++) shelves(p, x, b1, d - 2, x, d - 2);
        for (int z = 6; z <= d - 3; z++) shelves(p, w - 4, b1, z, w - 4, z);
        p.set(5, b1, 5, Painter.facing(Blocks.LECTERN, S));
        chair(p, 5, b1, 6, pal.planksStairs(), S);
        p.chest(4, b1, 1, S, TownLoot.STUDY);
        rug(p, 4, b1, 3, 9, 5, carpet(r));
        hangingLantern(p, 6, b1 + sh - 2, 4);
        hangingLantern(p, 6, b1 + sh - 2, 9);
        p.probe("room", 6, b1, 6);
        p.villager(6, b1, 7, VillagerProfession.LIBRARIAN);

        // Second storey: two bedrooms.
        int b2 = 2 * sh;
        p.bed(5, b2, d - 3, S, bed(r));
        p.bed(6, b2, d - 3, S, bed(r));
        p.chest(4, b2, d - 2, N, TownLoot.HOUSE);
        p.bed(w - 4, b2, 2, N, bed(r));
        p.chest(w - 5, b2, 1, S, TownLoot.HOUSE);
        rug(p, 4, b2, 5, 9, 8, carpet(r));
        tableLantern(p, pal, 3, b2, d - 2);
        tableLantern(p, pal, w - 3, b2, 4);
        tableLantern(p, pal, 7, b2, 3);
        p.probe("room", 6, b2, 6);

        int ridge = gable(p, pal, top, false, false);
        // Tower at the front west corner.
        int tTop = ridge + 2;
        p.walls(0, 0, 3, 3, top, tTop, pal.wall());
        for (int y = top; y <= tTop; y++) {
            p.set(0, y, 0, pal.trim());
            p.set(3, y, 0, pal.trim());
            p.set(0, y, 3, pal.trim());
            p.set(3, y, 3, pal.trim());
        }
        p.air(1, top, 1, 2, tTop, 2);
        p.set(1, top + 2, 0, pal.window());
        p.set(2, tTop - 1, 0, pal.window());
        p.set(0, top + 2, 1, pal.window());
        pyramid(p, pal, 0, 0, 3, 3, tTop + 1);
        chimney(p, w - 3, d - 1, 0, ridge + 1);
    }

    /**
     * A mushroom villa, the architecture of En's sphere of influence: chamfered stem walls under a great red cap,
     * shroomlight ceilings and pale birch interiors.
     */
    static void villa(Painter p) {
        TownRandom r = p.rng;
        TownPalette pal = TownPalette.MUSHROOM;
        int storeys = 2, sh = 4, w = p.w(), d = p.d(), door = 5;
        site(p, pal, 20);
        int top = shell(p, pal, storeys, sh);
        for (int y = 0; y < top; y++) {
            p.set(0, y, 0, Blocks.AIR);
            p.set(w - 1, y, 0, Blocks.AIR);
            p.set(0, y, d - 1, Blocks.AIR);
            p.set(w - 1, y, d - 1, Blocks.AIR);
        }
        for (int k = 0; k < storeys; k++) windows(p, pal, k * sh, sh, door, 1, 2, k == 0);
        frontDoor(p, pal, door, false);
        upstairs(p, pal, 0, 0, sh);
        // Shroomlights in the ceilings.
        p.set(3, sh - 1, 3, Blocks.SHROOMLIGHT);
        p.set(5, sh - 1, 7, Blocks.SHROOMLIGHT);

        rug(p, 3, 0, 2, 7, 4, Blocks.WHITE_CARPET);
        table(p, 5, 0, 3, pal.fence(), Blocks.BIRCH_PRESSURE_PLATE);
        chair(p, 4, 0, 3, pal.planksStairs(), W);
        chair(p, 6, 0, 3, pal.planksStairs(), E);
        kitchenRow(p, pal, 1, 0, d - 2, N, 4);
        p.set(2, sh - 2, 2, Blocks.SPORE_BLOSSOM.defaultBlockState());
        shelves(p, 1, 0, 4, 1, 6);
        p.probe("room", 5, 0, 5);
        p.villager(4, 0, 6, VillagerProfession.NONE);

        int b = sh;
        p.bed(3, b, d - 3, S, Blocks.WHITE_BED);
        p.bed(4, b, d - 3, S, Blocks.WHITE_BED);
        p.chest(5, b, d - 2, N, TownLoot.STUDY);
        p.set(2, b, 1, Painter.facing(Blocks.LECTERN, S));
        rug(p, 3, b, 3, 6, 6, Blocks.PINK_CARPET);
        p.probe("room", 4, b, 5);

        // The cap: a dome of red mushroom block, wider than the house, with a stem chimney.
        double cxm = (w - 1) / 2.0, czm = (d - 1) / 2.0;
        for (int dy = 0; dy <= 5; dy++) {
            double rad = Math.sqrt(Math.max(0, 36 - dy * dy * 1.4)) - 0.2;
            for (int z = -1; z <= d; z++) {
                for (int x = 0; x < w; x++) {
                    double dist = Math.hypot(x - cxm, z - czm);
                    if (dist <= rad && (dist > rad - 1.3 || dy >= 4)) {
                        boolean spot = TownRandom.hash(0x5EEDL, x * 31 + dy, z) % 9 == 0;
                        p.set(x, top + dy, z, spot ? Blocks.MUSHROOM_STEM.defaultBlockState() : Blocks.RED_MUSHROOM_BLOCK.defaultBlockState());
                    }
                }
            }
        }
        // A ceiling for the upper floor under the cap, with its own shroomlight.
        p.fill(1, top, 1, w - 2, top, d - 2, Blocks.BIRCH_PLANKS);
        p.set(5, top, 5, Blocks.SHROOMLIGHT);
        chimney(p, w - 3, d - 2, top, top + 6);
    }

    // ------------------------------------------------------------ poverty

    /** A patched-together shack. A third are abandoned: no door, broken walls, cobwebs, nothing lit. */
    static void shanty(Painter p) {
        TownRandom r = p.rng;
        TownPalette pal = TownPalette.PATCHWORK;
        boolean abandoned = r.chance(0.33);
        int w = p.w(), d = p.d(), door = 2;
        site(p, pal, 8);
        BlockState[] patch = {Blocks.MUD_BRICKS.defaultBlockState(), Blocks.COBBLESTONE.defaultBlockState(),
                Blocks.OAK_PLANKS.defaultBlockState(), Blocks.PACKED_MUD.defaultBlockState(),
                Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.MOSSY_COBBLESTONE.defaultBlockState()};
        for (int y = 0; y <= 3; y++) {
            for (int x = 0; x < w; x++) for (int z = 0; z < d; z++) {
                if (x != 0 && z != 0 && x != w - 1 && z != d - 1) continue;
                if (abandoned && y >= 1 && r.chance(0.12)) continue;
                p.set(x, y, z, patch[r.nextInt(patch.length)]);
            }
        }
        p.fill(1, -1, 1, w - 2, -1, d - 2, Blocks.PACKED_MUD);
        p.set(1, 1, 0, Blocks.AIR);
        p.set(w - 2, 1, d - 1, Blocks.AIR);
        p.set(0, 1, 3, abandoned ? Blocks.AIR.defaultBlockState() : Blocks.GLASS_PANE.defaultBlockState());
        if (abandoned) {
            p.air(door, 0, 0, door, 1, 0);
        } else {
            frontDoor(p, pal, door, false);
        }
        // Plank deck with a raised, patched front half and slab eaves front and back.
        for (int z = -1; z <= d; z++) {
            for (int x = 0; x < w; x++) {
                if (z == -1 || z == d) {
                    p.set(x, 3, z, Painter.slab(Blocks.OAK_SLAB, true));
                    continue;
                }
                if (abandoned && x > 0 && x < w - 1 && z > 0 && z < d - 1 && r.chance(0.1)) continue;
                p.set(x, 4, z, z % 3 == 0 ? Blocks.SPRUCE_PLANKS : Blocks.OAK_PLANKS);
                if (z <= d / 2) p.set(x, 5, z, Painter.slab(x % 2 == 0 ? Blocks.OAK_SLAB : Blocks.MOSSY_COBBLESTONE_SLAB, false));
            }
        }
        p.bed(w - 2, 0, d - 3, S, abandoned ? Blocks.BROWN_BED : bed(r));
        p.barrel(1, 0, d - 2, N, TownLoot.SHANTY);
        p.set(1, 0, d - 3, Painter.facing(Blocks.FURNACE, E));
        p.set(3, 0, d - 2, Blocks.CRAFTING_TABLE);
        if (abandoned) {
            p.set(1, 2, 1, Blocks.COBWEB);
            p.set(w - 2, 2, d - 2, Blocks.COBWEB);
            p.set(2, 0, 3, Blocks.MOSS_CARPET);
            p.probe("abandoned", 2, 0, 3);
        } else {
            p.lantern(1, 1, d - 2, false);
            p.set(3, 0, 1, Painter.facing(Blocks.COMPOSTER, N));
            p.probe("room", 2, 0, 3);
            p.villager(2, 0, 3, VillagerProfession.NONE);
        }
    }

    // ------------------------------------------------------------ community builds

    /**
     * The Exchange: "Victorian Town Building" by Frit (free to use and modify; see the asset manifest), converted to
     * 1.21.1 and fitted out here. Ground floor: a smoke and mask market hall with three stalls and back storage. A new
     * staircase (the original shell had none) leads to offices and a resident's apartment. The shell's plinth is
     * filled under the floor, and the plot is founded to natural ground like every other building.
     */
    static void exchange(Painter p) {
        TownRandom r = p.rng;
        int w = p.w(), d = p.d();
        for (int z = 0; z < d; z++) for (int x = 0; x < w; x++) p.foundation(x, z, -1, Blocks.STONE_BRICKS.defaultBlockState());
        p.fill(0, -1, 0, w - 1, -1, d - 1, Blocks.STONE_BRICKS);
        p.air(0, 0, 0, w - 1, 29, d - 1);
        for (int z = 0; z < d; z++) for (int x = 0; x < w; x++) p.clearTrees(x, z, 30, 44);
        // Half of the Exchanges keep Frit's bright Victorian colours; the rest are soot-dark, like the Gothic city.
        if (r.chance(0.5)) {
            p.template("victorian_exchange", java.util.Map.of(
                    Blocks.YELLOW_TERRACOTTA, Blocks.LIGHT_GRAY_TERRACOTTA,
                    Blocks.ORANGE_TERRACOTTA, Blocks.BROWN_TERRACOTTA,
                    Blocks.END_STONE, Blocks.TUFF,
                    Blocks.END_STONE_BRICK_WALL, Blocks.TUFF_BRICK_WALL,
                    Blocks.END_STONE_BRICK_STAIRS, Blocks.TUFF_BRICK_STAIRS,
                    Blocks.END_STONE_BRICK_SLAB, Blocks.TUFF_BRICK_SLAB,
                    Blocks.CLAY, Blocks.PACKED_MUD));
        } else {
            p.template("victorian_exchange");
        }
        // Solid ground under the hall floor, inside the plinth ring.
        boolean[][] floor = new boolean[w][d], low = new boolean[w * d][3];
        for (TemplateLibrary.Block b : TemplateLibrary.get("victorian_exchange").blocks()) {
            if (b.y() == 3) floor[b.x()][b.z()] = true;
            if (b.y() < 3) low[b.x() * d + b.z()][b.y()] = true;
        }
        for (int x = 0; x < w; x++) for (int z = 0; z < d; z++) {
            if (!floor[x][z]) continue;
            for (int y = 0; y < 3; y++) if (!low[x * d + z][y]) p.set(x, y, z, Blocks.STONE_BRICKS);
        }

        // Staircase to the upper floor along the front wall, with a railing round the well.
        flight(p, 3, 7, 4, E, 8, Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_FENCE, S);

        // Ground floor: three stalls with their keepers, benches by the door, storage at the back.
        for (int x = 4; x <= 19; x++) {
            if (x == 8 || x == 9 || x == 10 || x == 15) continue;
            p.set(x, 4, 11, Painter.slab(Blocks.SPRUCE_SLAB, true));
        }
        p.set(4, 5, 11, Blocks.TINTED_GLASS);
        p.set(5, 5, 11, Blocks.BREWING_STAND);
        p.set(6, 5, 11, Blocks.DECORATED_POT);
        p.set(7, 5, 11, Blocks.GLASS);
        for (int x = 11; x <= 14; x++) p.set(x, 5, 11, mask(r, N));
        p.set(16, 5, 11, Blocks.MELON);
        p.set(17, 5, 11, Blocks.HAY_BLOCK);
        p.set(18, 5, 11, Blocks.PUMPKIN);
        p.set(19, 5, 11, Painter.facing(Blocks.COMPOSTER, N));
        p.set(13, 4, 12, Blocks.CAULDRON);
        p.set(12, 4, 12, Painter.facing(Blocks.LOOM, N));
        p.set(17, 4, 12, Painter.facing(Blocks.SMOKER, N));
        p.villager(5, 4, 12, VillagerProfession.CLERIC);
        p.villager(14, 4, 12, VillagerProfession.LEATHERWORKER);
        p.villager(18, 4, 12, VillagerProfession.BUTCHER);
        for (int x = 18; x <= 20; x++) chair(p, x, 4, 7, Blocks.SPRUCE_STAIRS, N);
        p.barrel(11, 4, 14, N, TownLoot.SMOKE_PARLOR);
        p.barrel(12, 4, 14, N, TownLoot.MASK_SHOP);
        p.chest(17, 4, 14, N, TownLoot.MARKET);
        p.barrel(18, 4, 14, N, TownLoot.PANTRY);
        p.set(14, 4, 14, Blocks.BARREL);
        for (int[] l : new int[][]{{6, 9}, {10, 9}, {14, 9}, {18, 9}, {10, 13}, {14, 13}, {18, 13}}) hangingLantern(p, l[0], 9, l[1]);
        p.probe("room", 12, 4, 9);
        p.probe("room", 15, 4, 13);

        // Upper floor: an office and library at the front, an apartment at the back.
        for (int x = 13; x <= 19; x++) shelves(p, x, 12, 6, x, 6);
        p.set(16, 12, 8, Painter.facing(Blocks.LECTERN, S));
        table(p, 14, 12, 9, Blocks.SPRUCE_FENCE, Blocks.SPRUCE_PRESSURE_PLATE);
        chair(p, 14, 12, 10, Blocks.SPRUCE_STAIRS, S);
        p.chest(19, 12, 8, W, TownLoot.STUDY);
        p.bed(11, 12, 13, S, bed(r));
        p.bed(13, 12, 13, S, bed(r));
        p.chest(12, 12, 14, N, TownLoot.HOUSE);
        p.barrel(15, 12, 14, N, TownLoot.HOUSE);
        rug(p, 11, 12, 11, 15, 12, carpet(r));
        for (int[] l : new int[][]{{6, 10}, {14, 9}, {14, 13}, {19, 11}}) hangingLantern(p, l[0], 18, l[1]);
        p.villager(16, 12, 9, VillagerProfession.LIBRARIAN);
        p.villager(15, 12, 11, VillagerProfession.NONE);
        p.probe("room", 15, 12, 10);
        p.probe("room", 13, 12, 11);
    }

    // ------------------------------------------------------------ open lots

    static void openSite(Painter p, BlockState ground, int top) {
        int w = p.w(), d = p.d();
        for (int z = 0; z < d; z++) for (int x = 0; x < w; x++) p.foundation(x, z, -2, Blocks.DIRT.defaultBlockState());
        p.fill(0, -1, 0, w - 1, -1, d - 1, ground);
        p.air(0, 0, 0, w - 1, top, d - 1);
        for (int z = 0; z < d; z++) for (int x = 0; x < w; x++) p.clearTrees(x, z, top + 1, top + 14);
    }

    /** Market stalls under striped awnings: produce, fish and dry goods. */
    static void market(Painter p) {
        TownRandom r = p.rng;
        int w = p.w(), d = p.d();
        openSite(p, Blocks.POLISHED_ANDESITE.defaultBlockState(), 6);
        for (int z = 0; z < d; z++) for (int x = 0; x < w; x++) {
            if (TownRandom.hash(0x3A7L, x, z) % 4 == 0) p.set(x, -1, z, Blocks.ANDESITE);
        }
        Block[][] wares = {{Blocks.MELON, Blocks.PUMPKIN, Blocks.HAY_BLOCK}, {Blocks.BARREL, Blocks.BARREL, Blocks.BARREL},
                {Blocks.DRIED_KELP_BLOCK, Blocks.COMPOSTER, Blocks.BARREL}};
        Block[] awnings = {Blocks.RED_WOOL, Blocks.WHITE_WOOL, Blocks.CYAN_WOOL, Blocks.YELLOW_WOOL, Blocks.PURPLE_WOOL};
        int[] stallX = {0, 4, 8};
        for (int s = 0; s < 3; s++) {
            int x0 = stallX[s];
            if (x0 + 2 >= w) break;
            int z0 = 3, z1 = 5;
            for (int[] post : new int[][]{{x0, z0}, {x0 + 2, z0}, {x0, z1}, {x0 + 2, z1}}) p.fill(post[0], 0, post[1], post[0], 2, post[1], Blocks.SPRUCE_FENCE);
            Block a1 = awnings[r.nextInt(awnings.length)], a2 = awnings[r.nextInt(awnings.length)];
            for (int x = x0; x <= x0 + 2; x++) for (int z = z0 - 1; z <= z1; z++) p.set(x, 3, z, (x + z) % 2 == 0 ? a1 : a2);
            Block[] goods = wares[r.nextInt(wares.length)];
            for (int i = 0; i < 3; i++) {
                Block g = goods[i];
                if (g == Blocks.BARREL) p.barrel(x0 + i, 0, z0, N, i == 1 ? TownLoot.MARKET : null);
                else p.set(x0 + i, 0, z0, g == Blocks.COMPOSTER ? Painter.facing(g, N) : g.defaultBlockState());
            }
            p.lantern(x0 + 1, 2, z0, true);
            p.set(x0 + 1, 0, z1, Blocks.CRAFTING_TABLE);
        }
        p.probe("room", w / 2, 0, 2);
        p.villager(1, 0, 4, VillagerProfession.FARMER);
        p.villager(5, 0, 4, VillagerProfession.FISHERMAN);
    }

    /** A walled courtyard: a tree, flower beds, benches and a lamp. */
    static void courtyard(Painter p) {
        TownRandom r = p.rng;
        int w = p.w(), d = p.d(), door = 4;
        openSite(p, Blocks.GRASS_BLOCK.defaultBlockState(), 8);
        Block wall = r.chance(0.5) ? Blocks.STONE_BRICK_WALL : Blocks.MOSSY_COBBLESTONE_WALL;
        for (int x = 0; x < w; x++) for (int z = 0; z < d; z++) {
            boolean edge = x == 0 || z == 0 || x == w - 1 || z == d - 1;
            if (edge && !(z == 0 && x >= door - 1 && x <= door + 1)) p.set(x, 0, z, wall);
        }
        for (int z = 0; z < d - 1; z++) p.set(door, -1, z, Blocks.STONE_BRICKS);
        for (int x = 1; x < w - 1; x++) p.set(x, -1, 4, Blocks.STONE_BRICKS);
        // Tree.
        p.fill(2, 0, 2, 2, 3, 2, Blocks.OAK_LOG);
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        for (int y = 3; y <= 5; y++) for (int x = 1; x <= 3; x++) for (int z = 1; z <= 3; z++) {
            if (y == 5 && (x != 2 || z != 2) && (x != 2 && z != 2)) continue;
            if (p.get(x, y, z).isAir()) p.set(x, y, z, leaves);
        }
        Block[] flowers = {Blocks.POPPY, Blocks.CORNFLOWER, Blocks.ALLIUM, Blocks.OXEYE_DAISY, Blocks.AZURE_BLUET};
        for (int x = 6; x <= 7; x++) for (int z = 1; z <= 2; z++) p.set(x, 0, z, flowers[r.nextInt(flowers.length)]);
        for (int x = 1; x <= 3; x++) p.set(x, 0, d - 2, flowers[r.nextInt(flowers.length)]);
        chair(p, 6, 0, d - 2, Blocks.SPRUCE_STAIRS, S);
        chair(p, 7, 0, d - 2, Blocks.SPRUCE_STAIRS, S);
        p.fill(w - 2, 0, 5, w - 2, 1, 5, Blocks.SPRUCE_FENCE);
        p.lantern(w - 2, 2, 5, false);
        p.probe("room", 5, 0, 5);
    }

    /** A fenced kitchen garden: watered rows of ripe crops, a composter and a scarecrow. */
    static void herbGarden(Painter p) {
        TownRandom r = p.rng;
        int w = p.w(), d = p.d(), door = 3;
        openSite(p, Blocks.GRASS_BLOCK.defaultBlockState(), 5);
        for (int x = 0; x < w; x++) for (int z = 0; z < d; z++) {
            boolean edge = x == 0 || z == 0 || x == w - 1 || z == d - 1;
            if (edge && !(z == 0 && x == door)) p.set(x, 0, z, Blocks.OAK_FENCE);
        }
        p.set(door, 0, 0, Painter.facing(Blocks.OAK_FENCE_GATE, N));
        Block[] crops = {Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.BEETROOTS};
        for (int x = 1; x <= w - 2; x++) {
            for (int z = 2; z <= d - 2; z++) {
                if (x == door) {
                    p.set(x, -1, z, Blocks.COARSE_DIRT);
                    continue;
                }
                if (x == 2 && z == 3) {
                    p.set(x, -1, z, Blocks.WATER);
                    p.set(x, 0, z, Blocks.LILY_PAD);
                    continue;
                }
                p.set(x, -1, z, Blocks.FARMLAND.defaultBlockState().setValue(BlockStateProperties.MOISTURE, 7));
                Block crop = crops[(x + r.nextInt(2)) % crops.length];
                BlockState state = crop.defaultBlockState();
                if (state.hasProperty(BlockStateProperties.AGE_7)) state = state.setValue(BlockStateProperties.AGE_7, 7);
                if (state.hasProperty(BlockStateProperties.AGE_3)) state = state.setValue(BlockStateProperties.AGE_3, 3);
                p.set(x, 0, z, state);
            }
        }
        p.set(w - 2, 0, 1, Painter.facing(Blocks.COMPOSTER, N));
        p.set(1, 0, 1, Blocks.OAK_FENCE);
        p.set(1, 1, 1, Blocks.HAY_BLOCK);
        p.set(1, 2, 1, Painter.facing(Blocks.CARVED_PUMPKIN, N));
        p.set(w - 1, 1, 0, Blocks.LANTERN);
        p.probe("room", door, 0, 2);
        p.villager(door, 0, 1, VillagerProfession.FARMER);
    }
}
