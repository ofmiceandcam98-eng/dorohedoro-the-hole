package com.dorohedoro.hole.worldgen.city;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Seeded v17 domestic architecture. All geometry is written as absolute world
 * coordinates, clipped to one Canvas. No fixed house prefabs or chance streams.
 * Old world-detail profiles never invoke these additions.
 *
 * Only secondary roof decor and air-backed furniture are added: the Tower
 * skeleton, doors, stairs, roof routes and primary lighting are left intact.
 */
final class HousingDetail29 {
    private HousingDetail29() {}

    static Tower.Roof chooseRoof(Rect b, int floors, long hash, Tower.Roof previous) {
        // A one/two-storey 10x9 rowhouse should not have a 10-block aerial mast.
        return HousingPlan29.compact(b, floors) ? Tower.Roof.PLAIN : previous;
    }

    static void furnish(Canvas c, Rect r, int y, int storey, long house) {
        if (r.width() < 7 || r.depth() < 8) return;
        int type = HousingPlan29.household(house, r, storey);
        // Stair flight / hallway is along the north side; the threshold and
        // center aisle are intentionally clear. Furnish only perimeter alcoves.
        int rear = r.z0() + 4;
        int front = r.z1() - 2;
        if (rear >= front) return;
        int west = r.x0() + 1;
        int east = r.x1() - 1;
        switch (type) {
            case 0 -> { // tiny kitchen / pantry
                place(c, east, y + 1, rear, Blocks.SMOKER);
                place(c, east, y + 1, front, Blocks.BARREL);
            }
            case 1 -> { // shelves and personal effects
                place(c, west, y + 1, front, Blocks.BOOKSHELF);
                place(c, east, y + 1, rear, Blocks.FLOWER_POT);
            }
            case 2 -> { // sewing / laundry
                place(c, west, y + 1, front, Blocks.LOOM);
                place(c, east, y + 1, rear, Blocks.CAULDRON);
            }
            case 3 -> { // improvised handyperson's room
                place(c, east, y + 1, rear, Blocks.CRAFTING_TABLE);
                place(c, west, y + 1, front, Blocks.BARREL);
            }
            case 4 -> { // reading nook
                place(c, west, y + 1, front, Blocks.LECTERN);
                place(c, west, y + 1, front - 1, Blocks.BOOKSHELF);
            }
            case 5 -> { // washroom / storage
                place(c, east, y + 1, rear, Blocks.CAULDRON);
                place(c, east, y + 1, front, Blocks.COMPOSTER);
            }
            default -> { // shared household repairs
                place(c, west, y + 1, front, Blocks.STONECUTTER);
                place(c, east, y + 1, rear, Blocks.BARREL);
            }
        }
        // Floor rug is intentionally behind the center aisle, not on a door.
        if (r.width() >= 11 && r.depth() >= 12 && (type == 1 || type == 4)) {
            place(c, r.centerX() + 2, y + 1, r.centerZ() + 1,
                    type == 1 ? Blocks.BROWN_CARPET : Blocks.GRAY_CARPET);
        }
    }

    static void facade(Canvas c, Rect b, int floors, long house, District district) {
        if (b.width() < 9 || b.depth() < 7) return;
        boolean compact = HousingPlan29.compact(b, floors);
        int variant = HousingPlan29.frontage(house, b, floors);
        int roofline = HousingPlan29.roofline(house, b, floors);
        int top = CityPlan.STREET_Y + floors * Tower.STOREY;
        int faceZ = b.z1() + 1;
        int door = b.centerX();
        Block accent = switch (district) {
            case RESIDENTIAL -> variant % 2 == 0 ? Blocks.MUD_BRICKS : Blocks.TUFF_BRICKS;
            case MARKET -> Blocks.DARK_OAK_PLANKS;
            case WATERFRONT -> Blocks.WAXED_WEATHERED_CUT_COPPER;
            case INDUSTRIAL -> Blocks.POLISHED_DEEPSLATE;
            case DERELICT, GRAVEYARD -> Blocks.CRACKED_STONE_BRICKS;
            case OUTSKIRTS -> Blocks.SPRUCE_PLANKS;
        };
        Block cap = switch (variant) {
            case 0 -> Blocks.BRICK_SLAB;
            case 1 -> Blocks.DEEPSLATE_TILE_SLAB;
            case 2 -> Blocks.DARK_OAK_SLAB;
            case 3 -> Blocks.TUFF_BRICK_SLAB;
            default -> Blocks.SPRUCE_SLAB;
        };

        // A low welcome mat sits in the lot's own one-block setback, leaving
        // vanilla doors and navigation untouched (not a full obstruction).
        if (compact) place(c, door, CityPlan.GROUND_Y, faceZ,
                variant % 2 == 0 ? Blocks.BROWN_CARPET : Blocks.GRAY_CARPET);

        // Window shutters are actually aligned to generated window panes,
        // never blindly pasted on solid walls or across the entrance light.
        int spacing = HousingPlan29.shutterSpacing(house, b);
        for (int storey = 0; storey < floors; storey++) {
            int y = CityPlan.STREET_Y + storey * Tower.STOREY + 2;
            for (int x = b.x0() + 2; x <= b.x1() - 2; x += spacing) {
                if (Math.abs(x - door) <= 1 && storey == 0) continue;
                // Skip seam-spanning pairs even on a large test Canvas; a
                // single-chunk Canvas would lack the neighbor window state.
                if (Math.floorDiv(x - 1, 16) != Math.floorDiv(x + 1, 16)) continue;
                if (Math.floorDiv(b.z1(), 16) != Math.floorDiv(faceZ, 16)) continue;
                if (!c.contains(x, b.z1())) continue;
                if (!(c.get(x, y, b.z1()).getBlock() instanceof CrossCollisionBlock)) continue;
                if (CityHash.range(CityHash.of(house, x, storey), 5) == 0) continue;
                shutter(c, x - 1, y, faceZ, Direction.WEST);
                shutter(c, x + 1, y, faceZ, Direction.EAST);
            }
        }
        // Side pilaster only on small buildings where it reads as an entire
        // house, leaving the door opening and pedestrian path clear.
        if (compact && variant % 2 == 0) {
            for (int y = CityPlan.GROUND_Y + 1; y <= top; y++) {
                setIfAir(c, b.x0(), y, faceZ, accent);
                setIfAir(c, b.x1(), y, faceZ, accent);
            }
        }
        if (!compact) return;

        // Real variation at skyline level: stepped gable, lean-to cap,
        // recessed parapet, copper repair or small masonry chimney. Roof
        // decorations only fill air above the roof and avoid the north stair.
        int south = b.z1() - 1;
        switch (roofline) {
            case 0 -> { // narrow stepped gable at the front roof edge
                for (int x = b.x0() + 1; x < b.x1(); x++) {
                    setIfAir(c, x, top + 3, south, cap);
                    if (Math.abs(x - door) <= Math.max(1, b.width() / 5))
                        setIfAir(c, x, top + 4, south - 1, cap);
                }
            }
            case 1 -> { // lean-to roofline with roof boards
                for (int x = b.x0() + 2; x < b.x1() - 1; x++) {
                    setIfAir(c, x, top + 3, south, Blocks.DARK_OAK_SLAB);
                    if ((x - b.x0()) % 3 != 0)
                        setIfAir(c, x, top + 3, south - 1, cap);
                }
            }
            case 2 -> { // patched parapet, shorter towards one corner
                for (int x = b.x0() + 1; x <= b.x1() - 1; x++) {
                    if (x % 4 != 0) setIfAir(c, x, top + 3, b.z1(), accent);
                }
            }
            case 3 -> { // modest rooftop chimney, not a towering antenna
                int x = b.x1() - 2, z = b.z1() - 2;
                if (c.contains(x,z) && c.get(x,top+2,z).isAir()) {
                    for (int y = top + 2; y <= top + 4; y++) setIfAir(c,x,y,z,Blocks.BRICKS);
                    setIfAir(c,x,top+5,z,Blocks.IRON_TRAPDOOR);
                }
            }
            default -> { // copper-finished shared roof gutter
                for (int x = b.x0() + 2; x <= b.x1() - 2; x++) {
                    if (x % 5 != 0) setIfAir(c,x,top+3,south,Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB);
                }
            }
        }
    }

    private static void shutter(Canvas c, int x, int y, int z, Direction side) {
        if (!c.contains(x, z) || !c.get(x,y,z).isAir()) return;
        BlockState state = Blocks.DARK_OAK_TRAPDOOR.defaultBlockState()
                .setValue(TrapDoorBlock.OPEN, true).setValue(TrapDoorBlock.FACING, side);
        c.set(x,y,z,state);
    }

    private static void setIfAir(Canvas c, int x, int y, int z, Block block) {
        if (c.contains(x,z) && c.get(x,y,z).isAir()) c.set(x,y,z,block);
    }

    private static void place(Canvas c, int x, int y, int z, Block block) {
        if (!c.contains(x,z)) return;
        if (c.get(x,y,z).isAir() && !c.get(x,y-1,z).isAir()) c.set(x,y,z,block);
    }
}
