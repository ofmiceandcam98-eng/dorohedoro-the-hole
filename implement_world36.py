from pathlib import Path
import json
r=Path('/mnt/data/doro_pass36_work')
root=r/'src/main/java/com/dorohedoro/hole/worldgen/city'
def edit(name,before,after):
 p=root/name
 s=p.read_text()
 assert before in s, f'{name}: missing {before[:100]}'
 p.write_text(s.replace(before,after))
# Only new worlds have larger municipal reservation. Existing world profile geometries unchanged.
edit('CityPlan.java','case CANAL -> 9;', 'case CANAL -> settings.worldDetailVersion() >= 21 ? 17 : 9;')
edit('RiverChannels24.java','''    private static final int ROW_RADIUS = 9;
''','''    // The ROW itself is now supplied by Line.halfWidth(). Older profiles keep nine.
''')
edit('RiverChannels24.java','''        return (int)Math.round(3.0 * station(seed, alongZ ? BEND_X : BEND_Z, line.index(), along, 80));''','''        long salt = alongZ ? BEND_X : BEND_Z;
        if (line.halfWidth() < 15) {
            return (int)Math.round(3.0 * station(seed, salt, line.index(), along, 80));
        }
        // New v21 waterway reserve: bends travel 8-10 blocks laterally, with a
        // separate smaller wave so a whole street-length is not one straight run.
        // This is a fixed world-coordinate field, never a random walk or chunk RNG.
        double broad = station(seed, salt, line.index(), along, 112);
        double local = station(seed, salt ^ 0x36B1EC0L, line.index(), along, 47);
        return Math.max(-10, Math.min(10, (int)Math.round(8.4 * broad + 2.0 * local)));''')
edit('RiverChannels24.java','''        int desired = 5 + (int)Math.round(2.3 * broad + 0.7 * local);
        int radius = Math.max(3, Math.min(7, desired));
        // The nine-block municipal corridor reserves a walkable outer lip;
        // bends necessarily constrain the outer-bank width to avoid building lots.
        int corridorLimit = 7 - Math.abs(centerOffset(seed, line, alongZ, along));
        return Math.max(3, Math.min(radius, corridorLimit));''','''        int desired = 5 + (int)Math.round(2.3 * broad + 0.7 * local);
        int radius = Math.max(3, Math.min(line.halfWidth() >= 15 ? 8 : 7, desired));
        // Preserve at least a two-block bank on both edges of the reservation.
        // This is the key difference from merely snaking water across existing lots.
        int corridorLimit = line.halfWidth() - 2 - Math.abs(centerOffset(seed, line, alongZ, along));
        return Math.max(3, Math.min(radius, corridorLimit));''')
edit('RiverChannels24.java','''                Math.abs(coord - line.center()) <= ROW_RADIUS) return line;''','''                Math.abs(coord - line.center()) <= line.halfWidth()) return line;''')
edit('CitySettings.java','''worldDetailVersion < 0 || worldDetailVersion > 20''','''worldDetailVersion < 0 || worldDetailVersion > 21''')
edit('CitySettings.java','''"worldDetailVersion must be 0..20"''','''"worldDetailVersion must be 0..21"''')
edit('CitySettings.java','''Codec.intRange(0, 20).optionalFieldOf("world_detail_version", 0)''','''Codec.intRange(0, 21).optionalFieldOf("world_detail_version", 0)''')
edit('CitySettings.java','''@param worldDetailVersion 20 adds''','''@param worldDetailVersion 21 adds a wider reserved meandering waterway and additional facade detail; 20 adds''')
p=r/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json'
s=p.read_text();assert '"world_detail_version": 20' in s
p.write_text(s.replace('"world_detail_version": 20','"world_detail_version": 21'))
# New construction facade layer. Avoid doors, windows, fragile frame states and the
# interior wall itself. Exterior fixtures placed only in air and away from entry.
p=root/'FacadeDetail36.java'
p.write_text('''package com.dorohedoro.hole.worldgen.city;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Pass 36: sparse purposeful facade depth on the building side of the front
 * setback: rainwater downpipes, window lintels and patchwork repair plates.
 * Only air blocks are filled. No doors, windows, stairs or ceiling shapes are
 * removed, and there are no random external barrels/chests or extra lamps.
 */
final class FacadeDetail36 {
    private FacadeDetail36() {}

    private static void air(Canvas c, int x, int y, int z, Block block) {
        if (c.contains(x, z) && c.get(x, y, z).isAir()) c.set(x, y, z, block);
    }

    static void residence(Canvas c, Rect box, int floors, long hash, District district) {
        if (box.width() < 11 || box.depth() < 9) return;
        int front = box.z1() + 1;
        int wall = box.z1();
        int top = CityPlan.STREET_Y + floors * Tower.STOREY;
        int doorX = box.centerX();
        Block trim = switch (district) {
            case INDUSTRIAL, WATERFRONT -> Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB;
            case DERELICT, GRAVEYARD -> Blocks.DEEPSLATE_TILE_SLAB;
            case MARKET -> Blocks.DARK_OAK_SLAB;
            case OUTSKIRTS -> Blocks.SPRUCE_SLAB;
            default -> Blocks.TUFF_BRICK_SLAB;
        };
        // Eaves / narrow sills: only on floors with existing window openings.
        // Never cover the main entrance with a full-size solid block.
        for (int floor = 0; floor < floors; floor++) {
            int windowY = CityPlan.STREET_Y + floor * Tower.STOREY + 2;
            for (int x = box.x0() + 2; x <= box.x1() - 2; x++) {
                if (Math.abs(x - doorX) <= 1 && floor == 0) continue;
                if (CityHash.range(CityHash.of(hash, x, floor), 6) != 0) continue;
                // A lintel is at least two blocks off the ground: no sidewalk obstruction.
                if (!c.contains(x, wall) || !c.contains(x, front)) continue;
                if (!c.get(x, windowY, wall).isAir()) {
                    air(c, x, windowY + 1, front, trim);
                }
            }
        }
        // Downpipes at the outside corners create a long 3D vertical instead
        // of disconnected micro-noise. They are purely ornamental, not loot.
        int edgeX = CityHash.range(hash >>> 10, 2) == 0 ? box.x0() : box.x1();
        if (Math.abs(edgeX - doorX) >= 3) {
            for (int y = CityPlan.GROUND_Y + 2; y < top + 1; y++) {
                if (y % 5 != 0) air(c, edgeX, y, front, Blocks.IRON_BARS);
            }
        }
        // One matching roof maintenance vent, not a pile of chest props.
        if (floors >= 2 && box.width() >= 14 && box.depth() >= 12
                && CityHash.range(hash >>> 20, 3) == 0) {
            int x = box.x0() + 3;
            int z = box.z1() - 3;
            air(c, x, top + 3, z, Blocks.WAXED_WEATHERED_CUT_COPPER);
            air(c, x, top + 4, z, Blocks.IRON_TRAPDOOR);
        }
    }

    static void shop(Canvas c, Rect box, long hash) {
        if (box.width() < 10 || box.depth() < 8) return;
        int z = box.z1() + 1;
        // Thin metal awning above an accessible 2-high door; no bright lanterns.
        for (int x = box.x0() + 1; x <= box.x1() - 1; x++) {
            if (CityHash.range(CityHash.of(hash, x, 36), 8) == 0) continue; // repaired/absent sections
            air(c, x, CityPlan.GROUND_Y + 3, z, Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB);
        }
    }
}
''')
p=root/'Canvas.java';s=p.read_text();before='''    /** Pass 35: inhabited rooms and reduced exposed storage; saved worlds stay unchanged. */
    public boolean hasOccupiedInteriors35() { return worldDetailVersion >= 20; }''';assert before in s
p.write_text(s.replace(before,before+'''
    /** Pass 36: additional layered facades and larger river reservations in new worlds. */
    public boolean hasFacadeDetail36() { return worldDetailVersion >= 21; }
'''))
p=root/'LotPainter.java';s=p.read_text()
# Additional facade calls on tenement and apartment, outside Tower when full footprint known.
pattern='''        if (c.hasHousing29()) HousingDetail29.facade(c, box, floors, lot.hash(), lot.district());''';assert s.count(pattern)>=2
s=s.replace(pattern,pattern+'''
        if (c.hasFacadeDetail36()) FacadeDetail36.residence(c, box, floors, lot.hash(), lot.district());''',2)
pattern='''        if (c.hasLoreArchitecture()) LoreArchitecture19.shopfront(c, box, lot.hash());''';assert pattern in s
s=s.replace(pattern,pattern+'''
        if (c.hasFacadeDetail36()) FacadeDetail36.shop(c, box, lot.hash());''',1)
p.write_text(s)
print('Pass36 geometry/facade code modified')
