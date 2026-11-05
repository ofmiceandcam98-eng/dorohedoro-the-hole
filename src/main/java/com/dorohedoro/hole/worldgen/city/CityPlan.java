package com.dorohedoro.hole.worldgen.city;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Climate;

/**
 * The deterministic city layout for one world.
 *
 * <p>Two families of arterials, one per axis, cut the plane into superblocks. Each superblock is subdivided by a binary
 * space partition: every cut leaves a street, so any lot touches a street on all four sides and every street joins an
 * arterial. Nothing here reads the world. Results depend only on the seed and coordinates, and the cache only saves
 * recomputation, so a chunk's content cannot depend on generation order.
 */
public final class CityPlan {
    /** Original equal street pitch, retained for saved v0-v2 worlds. */
    public static final int LINE_PITCH = 120;
    // Unequal pitches in newly generated v3 worlds remove the repeated square-block macro grid.
    private static final int IRREGULAR_X_PITCH = 144;
    private static final int IRREGULAR_Z_PITCH = 104;
    private static final int LINE_JITTER = 32;
    private static final int START_LINE_JITTER = 12;

    /** Top block of the road surface. Players walk at {@link #GROUND_Y}. */
    public static final int STREET_Y = 64;
    public static final int GROUND_Y = STREET_Y + 1;

    /** Landmark regions are this many superblocks across, which spaces landmarks apart by construction. */
    public static final int LANDMARK_REGION = 8;

    private static final long SALT_X = 0x51ED27L;
    private static final long SALT_Z = 0x2F0B1DL;
    private static final long SALT_BLOCK = 0x7A11E5L;
    private static final long SALT_SHAFT = 0x5A4F7DL;
    private static final long SALT_LANDMARK = 0x1A9D3AL;

    public static final Line.Kind[] KIND_ROLLS = {
            Line.Kind.AVENUE, Line.Kind.AVENUE, Line.Kind.AVENUE, Line.Kind.TRAM, Line.Kind.TRAM, Line.Kind.CANAL
    };

    public final long seed;
    public final CitySettings settings;
    private final Climate.Sampler climate;
    private final LoadingCache<Long, Superblock> superblocks;
    private volatile StartingRegions47.Site selectedStart;

    public CityPlan(long seed, Climate.Sampler climate, CitySettings settings) {
        this.seed = seed;
        this.climate = climate;
        this.settings = settings;
        this.superblocks = CacheBuilder.newBuilder().maximumSize(2048).build(new CacheLoader<>() {
            @Override
            public Superblock load(Long key) {
                return Superblock.build(CityPlan.this, (int) (key >> 32), key.intValue());
            }
        });
    }

    public Line xLine(int index) {
        return line(SALT_X, index);
    }

    public Line zLine(int index) {
        return line(SALT_Z, index);
    }

    private int pitch(long salt) {
        if (settings.worldDetailVersion() < 3) return LINE_PITCH;
        return salt == SALT_X ? IRREGULAR_X_PITCH : IRREGULAR_Z_PITCH;
    }

    private Line line(long salt, int index) {
        long hash = CityHash.of(seed, salt, index);
        int period = pitch(salt);
        // Lines -1 and 0 frame the starting quarter, which must keep its size and stay walkable on every seed.
        boolean startLine = index == -1 || index == 0;
        int reach = startLine ? START_LINE_JITTER : LINE_JITTER;
        int jitter = CityHash.range(hash, reach * 2 + 1) - reach;
        int center = index * period + period / 2 + jitter;
        Line.Kind kind = KIND_ROLLS[CityHash.range(hash >>> 16, KIND_ROLLS.length)];
        if (startLine && kind == Line.Kind.CANAL) {
            kind = Line.Kind.TRAM;
        }
        // Pass 19: reduce overscaled boulevard widths in *new* worlds so dense
        // tenements and storefronts sit closer to the roadway. Tram tracks retain
        // their clear gauge and canals retain all flood-control clearance.
        int halfWidth = switch (kind) {
            case AVENUE -> settings.worldDetailVersion() >= 8 ? 4 : 6;
            case TRAM -> 7;
            case CANAL -> settings.worldDetailVersion() >= 24 ? 24 : settings.worldDetailVersion() >= 21 ? 17 : 9;
        };
        if (kind == Line.Kind.CANAL && settings.worldDetailVersion() >= 24) {
            // Irregular streets occasionally put two adjacent corridors close
            // together. Keep a public passage between their reservations,
            // rather than letting the widened river delete nearby lots.
            int gap = Math.min(center - rawLineCenter(salt, index - 1),
                    rawLineCenter(salt, index + 1) - center);
            halfWidth = Math.min(halfWidth, Math.max(12, (gap - 16) / 2));
        }
        return new Line(index, center, halfWidth, kind);
    }

    /** Same seed-derived position as line(), without constructing neighbors recursively. */
    private int rawLineCenter(long salt, int index) {
        int period = pitch(salt);
        int reach = index == -1 || index == 0 ? START_LINE_JITTER : LINE_JITTER;
        long hash = CityHash.of(seed, salt, index);
        return index * period + period / 2 + CityHash.range(hash, reach * 2 + 1) - reach;
    }

    /** Index of the superblock column containing {@code x}: the last line whose center is at or before it. */
    public int xIndex(int x) {
        return index(SALT_X, x);
    }

    public int zIndex(int z) {
        return index(SALT_Z, z);
    }

    private int index(long salt, int coordinate) {
        int period = pitch(salt);
        int guess = Math.floorDiv(coordinate - period / 2, period);
        for (int i = guess + 1; i >= guess - 1; i--) {
            if (line(salt, i).center() <= coordinate) {
                return i;
            }
        }
        return guess - 2;
    }

    /** Lines of one axis whose band could touch {@code [min, max]}. */
    public List<Line> xLinesNear(int min, int max) {
        return linesNear(SALT_X, min, max);
    }

    public List<Line> zLinesNear(int min, int max) {
        return linesNear(SALT_Z, min, max);
    }

    private List<Line> linesNear(long salt, int min, int max) {
        List<Line> lines = new ArrayList<>(3);
        int period = pitch(salt);
        int first = Math.floorDiv(min - period / 2, period) - 1;
        int last = Math.floorDiv(max - period / 2, period) + 2;
        for (int i = first; i <= last; i++) {
            Line line = line(salt, i);
            if (line.center() + line.halfWidth() >= min && line.center() - line.halfWidth() <= max) {
                lines.add(line);
            }
        }
        return lines;
    }

    /** Pass 42 terrain/biome identity; legacy saves retain the old city. */
    public HoleRegions42.Region regionAt(int x, int z) {
        return settings.worldDetailVersion() >= 35 ? HoleRegions42.atAnywhere(climate, x, z)
                : settings.worldDetailVersion() >= 26 ? HoleRegions42.at(climate, x, z)
                : HoleRegions42.Region.CITY;
    }

    /** Broad vanilla-ocean habitat independent of neighborhood density and Hole frontier climate. */
    public int oceanDepthAt(int x, int z) {
        return settings.worldDetailVersion() >= 47 ? OceanRegions65.depthAt(climate, x, z) : 0;
    }

    /** v48 coastline reservation; earlier ocean worlds retain their exact old coastline. */
    public int coastalDistanceAt(int x, int z) {
        return settings.worldDetailVersion() >= 48 ? CoastalShelf66.distanceAt(climate, x, z) : -1;
    }

    /** Protect vanilla deep-dark terrain from our own deep excavations in new worlds. */
    public boolean protectsDeepDarkNearChunk(int minX, int minZ) {
        return settings.worldDetailVersion() >= 45
                && DeepDarkBuffer63.nearDeepDark(climate, minX, minZ);
    }

    /** Preserve old 512-cell terrain grading in v0..31 saves. */
    public double terrainBlendAt(int x, int z) {
        return settings.worldDetailVersion() >= 35
                ? HoleRegions42.terrainBlendAnywhere(climate, x, z)
                : settings.worldDetailVersion() >= 32
                ? HoleRegions42.terrainBlend(climate, x, z)
                : HoleRegions42.terrainBlend(x, z);
    }

    public District districtAt(int x, int z) {
        return District.at(climate, x, z);
    }

    public boolean isColdDistrict(int x, int z) {
        return settings.worldDetailVersion() >= 6 && ColdDistricts17.isCold(climate, x, z);
    }

    public Superblock superblock(int i, int j) {
        return superblocks.getUnchecked(((long) i << 32) | (j & 0xFFFFFFFFL));
    }

    public long blockHash(int i, int j) {
        return CityHash.of(seed, SALT_BLOCK, i, j);
    }

    /** Avenues bend softly only on fresh v3 worlds; tram rails and canals never change course. */
    public int xAvenueOffset(Line avenue, int z) {
        if (settings.worldDetailVersion() < 3 || avenue == null || avenue.kind() != Line.Kind.AVENUE) return 0;
        int i = zIndex(z);
        int distance = Math.min(Math.abs(z - zLine(i).center()), Math.abs(z - zLine(i + 1).center()));
        return IrregularBlocks14.avenueOffset(seed, SALT_X + 0x14L, avenue.index(), z, distance);
    }

    public int zAvenueOffset(Line avenue, int x) {
        if (settings.worldDetailVersion() < 3 || avenue == null || avenue.kind() != Line.Kind.AVENUE) return 0;
        int i = xIndex(x);
        int distance = Math.min(Math.abs(x - xLine(i).center()), Math.abs(x - xLine(i + 1).center()));
        return IrregularBlocks14.avenueOffset(seed, SALT_Z + 0x14L, avenue.index(), x, distance);
    }


    /** All lots overlapping the chunk. */
    public List<Lot> lotsIn(ChunkPos chunk) {
        Rect area = new Rect(chunk.getMinBlockX(), chunk.getMinBlockZ(), chunk.getMaxBlockX(), chunk.getMaxBlockZ());
        List<Lot> lots = new ArrayList<>();
        int i0 = xIndex(area.x0());
        int i1 = xIndex(area.x1());
        int j0 = zIndex(area.z0());
        int j1 = zIndex(area.z1());
        for (int i = i0; i <= i1; i++) {
            for (int j = j0; j <= j1; j++) {
                for (Lot lot : superblock(i, j).lots()) {
                    if (lot.rect().intersects(area)) {
                        lots.add(lot);
                    }
                }
            }
        }
        return lots;
    }

    /** Whether the crossing of lines {@code i} and {@code j} carries a mine shaft to the underground levels. */
    public boolean hasShaft(int i, int j) {
        boolean atStart = (i == -1 || i == 0) && (j == -1 || j == 0);
        return atStart || CityHash.range(CityHash.of(seed, SALT_SHAFT, i, j), 3) == 0;
    }

    /** The superblock holding this landmark region's landmark. */
    public LandmarkSite landmarkSite(int regionX, int regionZ) {
        long hash = CityHash.of(seed, SALT_LANDMARK, regionX, regionZ);
        int blockI = regionX * LANDMARK_REGION + 1 + CityHash.range(hash, LANDMARK_REGION - 2);
        int blockJ = regionZ * LANDMARK_REGION + 1 + CityHash.range(hash >>> 12, LANDMARK_REGION - 2);
        if (settings.worldDetailVersion() >= 7 && DistrictIdentity18.useDistrictLandmark(seed, regionX, regionZ)) {
            Line west = xLine(blockI);
            Line east = xLine(blockI + 1);
            Line north = zLine(blockJ);
            Line south = zLine(blockJ + 1);
            District district = districtAt((west.center() + east.center()) >> 1, (north.center() + south.center()) >> 1);
            return new LandmarkSite(blockI, blockJ, DistrictIdentity18.landmarkType(district, hash));
        }
        LotType[] rotation = {LotType.HOSPITAL, LotType.DEPARTMENT_STORE, LotType.RESEARCH_MANOR, LotType.HOSPITAL,
                LotType.DEPARTMENT_STORE, LotType.LAKE_OF_REFUSE};
        return new LandmarkSite(blockI, blockJ, rotation[CityHash.range(hash >>> 24, rotation.length)]);
    }

    public record LandmarkSite(int blockI, int blockJ, LotType type) {}

    /**
     * One deterministic, climate-verified starting superblock for fresh worlds.
     * Legacy versions preserve the exact original (0,65,0) spawn and old lots.
     * The selected landing stays within one climate province; no temporary
     * teleportation, exploration-order state or player join-time relocation.
     */
    public StartingRegions47.Site startingSite() {
        if (settings.worldDetailVersion() < 31) return new StartingRegions47.Site(-1, -1, StartingRegions47.Origin.CITY);
        StartingRegions47.Site result = selectedStart;
        if (result == null) synchronized (this) {
            result = selectedStart;
            if (result != null) return result;
            StartingRegions47.Probe probe = (i, j, preferred) -> {
                Rect area = Superblock.areaFor(this, i, j);
                if (area.width() < 92 || area.depth() < 72) return false;
                int x = area.centerX(), z = area.centerZ();
                HoleRegions42.Region region = HoleRegions42.Region.valueOf(preferred.name());
                // Keep the entire landing and its nearby shelter within a real
                // province, not at the edge of a 512-block climate cell.
                return regionAt(x, z) == region && regionAt(x - 30, z - 24) == region
                        && regionAt(x + 38, z - 24) == region
                        && regionAt(x - 30, z + 24) == region
                        && regionAt(x + 38, z + 24) == region
                        // The landing must not erase a protected unique landmark.
                        && !isReservedLandmark(i, j)
                        // Only the new v33 preset filters out hard terrain steps
                        // between the two furnished starting parcels.
                        && (settings.worldDetailVersion() < 33
                            || !settings.terrainRelief()
                            || SpawnApproach50.isWalkable(x, z,
                                (px, pz) -> CityTerrain40.offset(this, px, pz)))
                        // v34 additionally traces the SOUTH entrance path (not
                        // just the parallel alley); older saved starts do not move.
                        && (settings.worldDetailVersion() < 34
                            || !settings.terrainRelief()
                            || SpawnRoute51.accessible(x, z,
                                (px, pz) -> CityTerrain40.offset(this, px, pz)));
            };
            result = settings.worldDetailVersion() >= 33
                    ? StartingRegions47.chooseWithRegionalFallback(seed, probe)
                    : StartingRegions47.choose(seed, probe);
            selectedStart = result;
        }
        return result;
    }

    private boolean isReservedLandmark(int i, int j) {
        LandmarkSite site = landmarkSite(Math.floorDiv(i, LANDMARK_REGION), Math.floorDiv(j, LANDMARK_REGION));
        return site.blockI() == i && site.blockJ() == j;
    }

    public boolean isRegionalStartingBlock(int i, int j) {
        if (settings.worldDetailVersion() < 31) return false;
        StartingRegions47.Site site = startingSite();
        return !site.downtown() && site.blockI() == i && site.blockJ() == j;
    }

    public boolean isRegionalStartingPlaza(Lot lot) {
        if (settings.worldDetailVersion() < 31 || lot.type() != LotType.PLAZA) return false;
        StartingRegions47.Site site = startingSite();
        if (site.downtown()) return false;
        Rect area = Superblock.areaFor(this, site.blockI(), site.blockJ());
        return lot.rect().centerX() == area.centerX() && lot.rect().centerZ() == area.centerZ();
    }

    /** Street-safe and flat in the landing's lot grading, even in mountain terrain. */
    public BlockPos spawnPoint() {
        StartingRegions47.Site site = startingSite();
        if (site.downtown()) return new BlockPos(0, GROUND_Y, 0);
        Rect area = Superblock.areaFor(this, site.blockI(), site.blockJ());
        int x = area.centerX(), z = area.centerZ();
        Rect landing = new Rect(x - 13, z - 13, x + 13, z + 13);
        int elevation = settings.terrainRelief() ? CityTerrain40.frontageOffset(this, landing) : 0;
        return new BlockPos(x, GROUND_Y + elevation, z);
    }
}
