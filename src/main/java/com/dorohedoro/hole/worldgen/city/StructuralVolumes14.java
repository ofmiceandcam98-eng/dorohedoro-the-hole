package com.dorohedoro.hole.worldgen.city;

import static com.dorohedoro.hole.worldgen.city.CityPlan.GROUND_Y;
import static com.dorohedoro.hole.worldgen.city.CityPlan.STREET_Y;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;

/**
 * Pass 14: true non-rectangular building volumes, not just a painted facade.
 * Carves above-ground northeast corner recesses, interior courtyards, and upper
 * floor terraces. The north-west staircase and centered south entrance are
 * never carved. All shaping is post-furnishing so eliminated rooms contain no
 * hidden beds, inventories, or orphaned tiles.
 */
final class StructuralVolumes14 {
    private StructuralVolumes14() {}

    static int profile(long hash, Rect b, int floors, Tower.Roof roof) {
        return BuildingMass14.profile(hash, b, floors, roof == Tower.Roof.GARDEN);
    }

    static void tower(Canvas c, Rect b, int floors, Style style, long hash, Tower.Roof roof) {
        int kind = profile(hash, b, floors, roof);
        int top = STREET_Y + floors * Tower.STOREY;
        switch (kind) {
            case 0 -> lWing(c, b, top, style);
            case 1 -> courtyard(c, b, top, floors, style);
            case 2 -> terracedUpperFloor(c, b, top, style);
            default -> { /* Plenty of intact complete buildings amid the irregular forms. */ }
        }
    }

    /** An L-shaped shell: northeast is a walkable pocket court, core/staircase remain west. */
    private static void lWing(Canvas c, Rect b, int top, Style style) {
        Rect recess = BuildingMass14.lRecess(b);
        int x0 = recess.x0();
        int z1 = recess.z1();
        // No new hole through street bed, mines, or sewers. The former rooms become yard.
        c.clear(recess, GROUND_Y, top + 3);
        for (int x = x0; x <= b.x1(); x++) {
            c.set(x, STREET_Y, b.z0(), Blocks.PACKED_MUD);
            c.set(x, STREET_Y, z1, Blocks.PACKED_MUD);
        }
        // Outside-facing new walls seal rooms instead of leaving an accidental cutaway.
        c.fill(x0 - 1, GROUND_Y, b.z0(), x0 - 1, top + 1, z1, style.wall());
        c.fill(x0 - 1, GROUND_Y, z1 + 1, b.x1(), top + 1, z1 + 1, style.wall());
        for (int y = GROUND_Y + 2; y < top; y += Tower.STOREY) {
            c.pane(x0 - 1, y, b.z0() + 2, Direction.Axis.Z, style.glass());
            c.pane(b.x1() - 2, y, z1 + 1, Direction.Axis.X, style.glass());
        }
        c.set(b.x1() - 1, GROUND_Y, b.z0() + 2, Blocks.CAULDRON);
        c.set(b.x1() - 2, GROUND_Y, b.z0() + 3, Blocks.COMPOSTER);
        for (int x = x0; x <= b.x1(); x++) c.set(x, top + 2, z1 + 1, Blocks.IRON_BARS);
    }

    /** Open courtyard with access from the first floor; roof ring keeps a continuous walkway. */
    private static void courtyard(Canvas c, Rect b, int top, int floors, Style style) {
        Rect court = BuildingMass14.courtyard(b);
        int x0 = court.x0(), x1 = court.x1();
        int z0 = court.z0(), z1 = court.z1();
        if (x1 - x0 < 3 || z1 - z0 < 3) return;
        c.clear(court, GROUND_Y, top + 3);
        c.fill(x0, STREET_Y, z0, x1, STREET_Y, z1, Blocks.PACKED_MUD);
        // Courtyard walls have the same storey rhythm as the exterior.
        c.fill(x0 - 1, GROUND_Y, z0 - 1, x0 - 1, top, z1 + 1, style.wall());
        c.fill(x1 + 1, GROUND_Y, z0 - 1, x1 + 1, top, z1 + 1, style.wall());
        c.fill(x0, GROUND_Y, z0 - 1, x1, top, z0 - 1, style.wall());
        c.fill(x0, GROUND_Y, z1 + 1, x1, top, z1 + 1, style.wall());
        for (int k = 0; k < floors; k++) {
            int y = STREET_Y + k * Tower.STOREY + 2;
            c.pane(x0 - 1, y, (z0 + z1) / 2, Direction.Axis.Z, style.glass());
            c.pane(x1 + 1, y, (z0 + z1) / 2, Direction.Axis.Z, style.glass());
            c.pane((x0 + x1) / 2, y, z1 + 1, Direction.Axis.X, style.glass());
        }
        int passageX = (x0 + x1) / 2;
        // Leave a walkable doorless aperture on the ground floor, with a continuous
        // interior route to the existing entrance, not a permanently sealed pit.
        c.clear(new Rect(passageX, z1 + 1, passageX + 1, z1 + 1), GROUND_Y, GROUND_Y + 2);
        c.clear(new Rect(passageX, z1 + 2, passageX + 1, b.z1() - 1), GROUND_Y, GROUND_Y + 2);
        for (int x = x0; x <= x1; x++) {
            c.set(x, top + 1, z0 - 1, Blocks.IRON_BARS);
            c.set(x, top + 1, z1 + 1, Blocks.IRON_BARS);
        }
        for (int z = z0; z <= z1; z++) {
            c.set(x0 - 1, top + 1, z, Blocks.IRON_BARS);
            c.set(x1 + 1, top + 1, z, Blocks.IRON_BARS);
        }
        c.set(x1 - 1, GROUND_Y, z0 + 1, Blocks.COMPOSTER);
    }

    /** Recessed upper floors create real terraced silhouettes without cutting the lower rooms. */
    private static void terracedUpperFloor(Canvas c, Rect b, int top, Style style) {
        int terraceY = STREET_Y + 2 * Tower.STOREY;
        if (top <= terraceY + 4) return;
        Rect recess = BuildingMass14.upperTerrace(b);
        int x0 = recess.x0(), z0 = recess.z0();
        c.clear(recess, terraceY + 1, top + 3);
        // The second-floor ceiling becomes a usable terrace deck.
        c.fill(x0, terraceY, z0, b.x1(), terraceY, b.z1(), Blocks.POLISHED_ANDESITE);
        c.fill(x0 - 1, terraceY + 1, z0, x0 - 1, top + 1, b.z1(), style.wall());
        c.fill(x0, terraceY + 1, z0 - 1, b.x1(), top + 1, z0 - 1, style.wall());
        for (int x = x0; x <= b.x1(); x++) c.set(x, terraceY + 1, b.z1(), Blocks.IRON_BARS);
        for (int z = z0; z < b.z1(); z++) c.set(b.x1(), terraceY + 1, z, Blocks.IRON_BARS);
        c.pane(x0 - 1, terraceY + 3, z0 + 2, Direction.Axis.Z, style.glass());
        c.set(b.x1() - 2, terraceY + 1, b.z1() - 2, Blocks.FLOWER_POT);
    }

    /** Reserve a true side yard for a freestanding shack in larger industrial plots. */
    static Rect industrialFootprint(Canvas c, Lot lot, Rect original) {
        if (!c.hasIrregularCity() || lot.rect().width() < 36 || lot.rect().depth() < 26
                || original.width() < 31 || original.depth() < 20) return original;
        // Seven blocks of the original warehouse become a service yard, retaining
        // a 1-block separation and a wide, ordinary walkable cart entrance.
        // Fresh v39 worlds reserve a three-wide service connection beside the
        // existing detached workshop. Older saves retain their +8 footprint.
        int setback = c.hasLinkedServiceYards56() ? 10 : 8;
        return new Rect(original.x0() + setback, original.z0(), original.x1(), original.z1());
    }

    /** A detached yard workshop with usable storage; never overwrites the main hall. */
    static void serviceOutbuilding(Canvas c, Lot lot, Rect main) {
        if (!c.hasIrregularCity() || lot.rect().width() < 36 || lot.rect().depth() < 26) return;
        Rect yard = lot.rect();
        Rect shack = LinkedServiceYards56.shack(yard, main, c.hasLinkedServiceYards56());
        if (shack.width() < 5 || shack.depth() < 8) return;
        int roof = GROUND_Y + 5;
        c.fill(shack, STREET_Y, STREET_Y, Blocks.POLISHED_ANDESITE);
        c.walls(shack, GROUND_Y, roof, Mix.of(Blocks.MUD_BRICKS, 3, Blocks.CRACKED_STONE_BRICKS, 2));
        c.fill(shack, roof + 1, roof + 1, Blocks.WAXED_WEATHERED_CUT_COPPER);
        int entranceX = shack.centerX();
        c.door(entranceX, GROUND_Y, shack.z1(), Direction.NORTH, Blocks.SPRUCE_DOOR);
        c.clear(new Rect(entranceX, shack.z1() - 1, entranceX, shack.z1() - 1), GROUND_Y, GROUND_Y + 1);
        c.pane(shack.x0(), GROUND_Y + 2, shack.z0() + 2, Direction.Axis.Z, Blocks.IRON_BARS);
        c.set(shack.x0() + 1, GROUND_Y, shack.z0() + 1, Blocks.CRAFTING_TABLE);
        c.barrel(shack.x1() - 1, GROUND_Y, shack.z0() + 1, "warehouse");
        c.set(shack.x1() - 1, GROUND_Y, shack.z0() + 3, Blocks.SMITHING_TABLE);
        c.fill(shack.x1() + 1, STREET_Y, shack.z0(), main.x0() - 1, STREET_Y, shack.z1(), Blocks.GRAVEL);
        // Connect the workshop's usable side room to the hall without
        // obstructing either building's street-facing cart door.
        if (c.hasLinkedServiceYards56()) LinkedServiceYards56.connect(c, lot, shack, main);
    }

    /** A separate, accessible raised machine office that breaks up the factory roofline. */
    static void industrialOffice(Canvas c, Rect b, int roofY, long hash) {
        if (b.width() < 25 || b.depth() < 23 || CityHash.range(CityHash.mix(hash ^ 0xFACC714L), 3) == 0) return;
        Rect office = new Rect(b.x1() - 11, b.z1() - 11, b.x1(), b.z1() - 1);
        c.fill(office, roofY + 1, roofY + 1, Blocks.POLISHED_ANDESITE);
        c.walls(office, roofY + 2, roofY + 6, Mix.of(Blocks.BRICKS, 3, Blocks.WAXED_WEATHERED_CUT_COPPER, 2));
        c.fill(office, roofY + 7, roofY + 7, Blocks.WAXED_OXIDIZED_CUT_COPPER);
        c.clear(office.inset(1), roofY + 2, roofY + 6);
        int ladderX = b.x1() - 1;
        int ladderZ = b.z1() - 5;
        c.set(ladderX, roofY, ladderZ, Blocks.AIR);
        c.ladder(ladderX, GROUND_Y, roofY + 5, ladderZ, Direction.WEST);
        // Hall wall provides ladder backing, office wall provides upper backing.
        c.pane(office.x0() + 3, roofY + 4, office.z0(), Direction.Axis.X, Blocks.GLASS_PANE);
        c.pane(office.x0() + 6, roofY + 4, office.z0(), Direction.Axis.X, Blocks.GLASS_PANE);
        c.set(office.x0() + 2, roofY + 2, office.z0() + 2, Blocks.LECTERN);
        c.set(office.x0() + 3, roofY + 2, office.z0() + 2, Blocks.CARTOGRAPHY_TABLE);
        c.set(office.x0() + 4, roofY + 2, office.z0() + 2, Blocks.BARREL);
        c.set(office.x0() + 5, roofY + 6, office.z0() + 5,
                Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
    }
}
