package com.dorohedoro.hole.worldgen.city;

import java.util.List;

/**
 * World-coordinate safety envelope around critical vanilla underground structures.
 * Envelopes are built from actual registered StructureStart bounding boxes (Pass 64),
 * not a seed guessed from the world's biome or a fabricated structure location.
 * Only our own city/sewer canvas writes are suppressed; vanilla structure placement
 * and structure loot tables are left intact.
 */
public final class VanillaStructureBuffer64 {
    private VanillaStructureBuffer64() {}

    public static final int HORIZONTAL_MARGIN = 4;
    public static final int VERTICAL_MARGIN = 6;

    public record Box(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        public Box {
            if (minX > maxX || minY > maxY || minZ > maxZ)
                throw new IllegalArgumentException("Reversed vanilla structure bounding box");
        }
        public boolean near(int x, int y, int z) {
            // Subtraction avoids overflow when guarding extreme Minecraft coordinates.
            return (long) x >= (long) minX - HORIZONTAL_MARGIN
                && (long) x <= (long) maxX + HORIZONTAL_MARGIN
                && (long) y >= (long) minY - VERTICAL_MARGIN
                && (long) y <= (long) maxY + VERTICAL_MARGIN
                && (long) z >= (long) minZ - HORIZONTAL_MARGIN
                && (long) z <= (long) maxZ + HORIZONTAL_MARGIN;
        }
        public boolean nearChunk(int minX, int minZ) {
            return (long) maxX + HORIZONTAL_MARGIN >= minX
                && (long) this.minX - HORIZONTAL_MARGIN <= (long) minX + 15
                && (long) maxZ + HORIZONTAL_MARGIN >= minZ
                && (long) this.minZ - HORIZONTAL_MARGIN <= (long) minZ + 15;
        }
    }

    public static boolean protects(int worldDetailVersion, int x, int y, int z, List<Box> starts) {
        if (worldDetailVersion < 46 || starts.isEmpty()) return false;
        for (Box box : starts) if (box.near(x, y, z)) return true;
        return false;
    }
}
