package com.dorohedoro.hole.worldgen.city;

import com.dorohedoro.hole.Dorohedoro;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Draws into one chunk, silently dropping anything outside it. A structure drawn through a canvas produces the same
 * result however it is split across chunks, so generators can place blocks in absolute coordinates and never think
 * about borders. Variation must come from {@link #hash}, never from a stateful random, for that to stay true.
 */
public final class Canvas {
    private final ChunkAccess chunk;
    private final long seed;
    private final boolean terrainRelief;
    private final int worldDetailVersion;
    private Integer fixedLotHeight;
    private final CityPlan terrainPlan;
    private final boolean protectDeepDark63;
    private final List<VanillaStructureBuffer64.Box> protectedVanillaStructures64;
    private final byte[] columnHeights = new byte[256];
    private final byte[] oceanColumns65 = new byte[256];
    private final byte[] dryCoastColumns66 = new byte[256];
    private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
    public final int minX;
    public final int minZ;
    public final int maxX;
    public final int maxZ;

    public Canvas(ChunkAccess chunk, long seed, boolean terrainRelief) {
        this(chunk, seed, terrainRelief, 0);
    }

    public Canvas(ChunkAccess chunk, long seed, boolean terrainRelief, boolean worldDetail) {
        this(chunk, seed, terrainRelief, worldDetail ? 1 : 0);
    }

    public Canvas(ChunkAccess chunk, CityPlan plan) {
        this(chunk, plan, List.of());
    }

    public Canvas(ChunkAccess chunk, CityPlan plan, List<VanillaStructureBuffer64.Box> structures) {
        this(chunk, plan.seed, plan.settings.terrainRelief(), plan.settings.worldDetailVersion(), plan, structures);
    }

    public Canvas(ChunkAccess chunk, long seed, boolean terrainRelief, int worldDetailVersion) {
        this(chunk, seed, terrainRelief, worldDetailVersion, null, List.of());
    }

    private Canvas(ChunkAccess chunk, long seed, boolean terrainRelief, int worldDetailVersion, CityPlan terrainPlan, List<VanillaStructureBuffer64.Box> structures) {
        this.terrainPlan = terrainPlan;
        this.protectedVanillaStructures64 = List.copyOf(structures);
        this.chunk = chunk;
        this.seed = seed;
        this.terrainRelief = terrainRelief;
        this.worldDetailVersion = worldDetailVersion;
        ChunkPos pos = chunk.getPos();
        this.minX = pos.getMinBlockX();
        this.minZ = pos.getMinBlockZ();
        this.maxX = pos.getMaxBlockX();
        this.maxZ = pos.getMaxBlockZ();
        this.protectDeepDark63 = terrainPlan != null && terrainPlan.protectsDeepDarkNearChunk(minX, minZ);
        // Most city blocks write dozens of blocks in each (x,z) column.
        // Cache relief once per chunk instead of performing four coordinate hashes per placement.
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int index = (x - minX) + ((z - minZ) << 4);
                columnHeights[index] = (byte) (terrainRelief ? terrainAt(x, z) : 0);
                // Refuse urban structures and their tile entities in new ocean columns
                // BEFORE the ocean painter runs; this prevents ghost block-entity NBT.
                if (terrainPlan != null && worldDetailVersion >= 47) {
                    oceanColumns65[index] = (byte) terrainPlan.oceanDepthAt(x, z);
                    if (worldDetailVersion >= 48 && oceanColumns65[index] == 0) {
                        int shoreDistance = terrainPlan.coastalDistanceAt(x, z);
                        if (shoreDistance > 0) dryCoastColumns66[index] = (byte) shoreDistance;
                    }
                }
            }
        }
    }

    /** World- and version-aware terrain, recomputed for off-chunk queries too. */
    private int terrainAt(int x, int z) {
        return terrainPlan != null ? CityTerrain40.offset(terrainPlan, x, z)
                : CityElevation.offset(seed, x, z);
    }

    /** Pass 71: every tower storey furnished as a themed room. */
    public boolean hasRoomDressing71() { return worldDetailVersion >= 53; }

    /** Pass 60: industrial loading approaches and connected workshop circulation. */
    public boolean hasFreightApproaches60() { return worldDetailVersion >= 43; }

    /** Pass 59: access from industrial side workshops to public frontage, plus working courts. */
    public boolean hasWorkingServiceCourts59() { return worldDetailVersion >= 42; }

    /** Pass 58: non-rectangular working industrial halls with genuinely attached, furnished low bays. */
    public boolean hasIndustrialAnnex58() { return worldDetailVersion >= 41; }

    /** Pass 56: three-wide covered routes connect industrial workshops with their factory halls. */
    public boolean hasLinkedServiceYards56() { return worldDetailVersion >= 39; }

    /** Pass 55: real attached, connected service rooms and reachable stepped maintenance roofs. */
    public boolean hasAttachedServiceWings55() { return worldDetailVersion >= 38; }

    /** Pass 44: infrastructure-based residential and industrial silhouettes. */
    public boolean hasFunctionalFacades44() { return worldDetailVersion >= 28; }
    public boolean hasInhabitedCourts45() { return worldDetailVersion >= 29; }
    /** Fresh v30 worlds cluster habitable rooflines over several city blocks. */
    public boolean hasNeighborhoodSkyline46() { return worldDetailVersion >= 30; }
    boolean isHoleCity(int x, int z) {
        return terrainPlan != null && (worldDetailVersion >= 35
                ? UrbanPattern52.at(seed, x, z) == UrbanPattern52.Fabric.DENSE
                : terrainPlan.regionAt(x, z) == HoleRegions42.Region.CITY);
    }

    /** Pass 43: occupied civic spaces and nonuniform vendor frontages. */
    public boolean hasOccupiedCommons43() { return worldDetailVersion >= 27; }

    /** Pass 41: street-matched foundations and conditional facade relief. */
    public boolean hasEntranceGrade41() { return worldDetailVersion >= 25; }

    public boolean hasSkyline40() {
        return worldDetailVersion >= 24;
    }

    public boolean hasMacroRelief40() {
        return terrainRelief && terrainPlan != null && worldDetailVersion >= 24;
    }

    /** Per-lot planar foundation: prevents slopes from cutting across rooms, stairs and doors. */
    public void beginLot(Rect lot) {
        fixedLotHeight = !terrainRelief ? 0
                : terrainPlan != null && worldDetailVersion >= 25
                ? CityTerrain40.frontageOffset(terrainPlan, lot)
                : terrainAt(lot.centerX(), lot.centerZ());
    }

    public void endLot() {
        fixedLotHeight = null;
    }

    public boolean hasWorldDetail() {
        return worldDetailVersion > 0;
    }

    /** New geometry is saved per world; existing Pass 12 worlds retain byte-for-byte old detail. */
    public boolean hasArchitectureVariation() {
        return worldDetailVersion >= 2;
    }

    /** Versioned non-grid streets and multivolume building shells; existing saves keep their layouts. */
    public boolean hasIrregularCity() {
        return worldDetailVersion >= 3;
    }


    /** Version 4 introduces varied indoor partitions, without changing older saves. */
    public boolean hasInteriorVariety() {
        return worldDetailVersion >= 4;
    }

    /** Tree scarcity is versioned so existing worlds keep their vegetation and chunk boundaries. */
    public boolean hasSparseHoleTrees() {
        return worldDetailVersion >= 5;
    }

    public boolean hasColdDistricts() {
        return worldDetailVersion >= 6;
    }

    /** Version 7 strengthens district readability with signature lots and stronger material cues. */
    public boolean hasDistrictIdentity() {
        return worldDetailVersion >= 7;
    }

    /** Version 8 applies original concrete/brick/plaster architecture and coherent street/lot materials. */
    public boolean hasLoreArchitecture() {
        return worldDetailVersion >= 8;
    }

    /** Naturalized rivers within protected municipal waterways on fresh saves. */
    public boolean hasRiverMorphology() { return worldDetailVersion >= 11; }

    /** Source-only Pass 25: seeded neighborhood fabric; no fixed city blueprint. */
    public boolean hasUrbanFabric() { return worldDetailVersion >= 12; }

    /** Pass 25 housing: compact homes, differentiated residential facades and layered lights. */
    public boolean hasHousing25() { return worldDetailVersion >= 14; }
    public boolean hasResidential26() { return worldDetailVersion >= 15; }
    /** Pass 28: mixed Cavern Lamp / vanilla lantern / torch placement and room furnishing. */
    public boolean hasAtmosphere28() { return worldDetailVersion >= 16; }
    /** Pass 29: small-home roof shapes, facade details and additional domestic use. */
    public boolean hasHousing29() { return worldDetailVersion >= 17; }
    /** Pass31: fixes residential wall fixtures without changing v0-v17 saved worlds. */
    public boolean hasHousingLighting31() { return worldDetailVersion >= 18; }
    /** Pass 35: inhabited rooms and reduced exposed storage; saved worlds stay unchanged. */
    public boolean hasOccupiedInteriors35() { return worldDetailVersion >= 20; }
    /** Pass 36: additional layered facades and larger river reservations in new worlds. */
    public boolean hasFacadeDetail36() { return worldDetailVersion >= 21; }
    /** Pass 37: occupied room use and small parcel-contained earthworks; no changes to canals. */
    public boolean hasHillside37() { return worldDetailVersion >= 22; }
    /** Pass 39: purpose-led microfurnishings and inhabited plain roof corners. */
    public boolean hasOccupiedDetail39() { return worldDetailVersion >= 23; }


    /** Richer interior furnishing in new-world profiles only. */
    /** New high-detail furniture palette; no retroactive edits in pre-24 world profiles. */
    public boolean hasPass24Interiors() {
        return worldDetailVersion >= 10;
    }

    public boolean hasExpandedFurniture() {
        return worldDetailVersion >= 9;
    }

    public long seed() {
        return seed;
    }

    public boolean hasTerrainRelief() {
        return terrainRelief;
    }

    public int heightOffset(int x, int z) {
        if (fixedLotHeight != null) return fixedLotHeight;
        // A caller may query another column when a lot crosses chunk edges.
        // Cache access is valid only for the 16x16 columns of this chunk;
        // elsewhere recompute the same deterministic relief without indexing
        // the local cache. All actual world writes remain chunk-clipped.
        if (!contains(x, z)) return terrainRelief ? terrainAt(x, z) : 0;
        return columnHeights[(x - minX) + ((z - minZ) << 4)];
    }

    private int worldY(int x, int y, int z) {
        return y + heightOffset(x, z);
    }

    public boolean contains(int x, int z) {
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    public boolean overlaps(Rect rect) {
        return rect.x0() <= maxX && rect.x1() >= minX && rect.z0() <= maxZ && rect.z1() >= minZ;
    }

    public long hash(int x, int y, int z) {
        return CityHash.of(seed, x, y, z);
    }

    private long mixHash(Mix mix, int x, int y, int z) {
        return mix.patchy() && worldDetailVersion >= 53 ? patchHash(x, y, z) : hash(x, y, z);
    }

    /**
     * v53 ground: one material per irregular patch, about 3 to 6 blocks across, instead of a per-block checkerboard.
     * The patches are cells around jittered points, and the lookup position wobbles by a block so edges are
     * ragged, not straight lines. Pure absolute-coordinate math, so patches continue across chunk borders.
     */
    private long patchHash(int x, int y, int z) {
        final int cell = 5;
        long wobble = CityHash.of(seed, 0x9A7C4L, x, z);
        int px = x + (int) (wobble & 3) - 1;
        int pz = z + (int) ((wobble >>> 2) & 3) - 1;
        int gx = Math.floorDiv(px, cell);
        int gz = Math.floorDiv(pz, cell);
        long best = 0;
        int bestDistance = Integer.MAX_VALUE;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                long h = CityHash.of(seed, 0x9A7C5L, gx + dx, gz + dz);
                int fx = (gx + dx) * cell + CityHash.range(h, cell);
                int fz = (gz + dz) * cell + CityHash.range(h >>> 16, cell);
                int d = (px - fx) * (px - fx) + (pz - fz) * (pz - fz);
                if (d < bestDistance) {
                    bestDistance = d;
                    best = h;
                }
            }
        }
        return CityHash.of(best, y);
    }

    /** Suppress all custom write paths, including tile entity/NBT writes, near actual vanilla starts. */
    private boolean protectsVanillaWrite(int x, int adjustedY, int z) {
        return DeepDarkBuffer63.protects(worldDetailVersion, adjustedY, protectDeepDark63)
                || VanillaStructureBuffer64.protects(worldDetailVersion, x, adjustedY, z, protectedVanillaStructures64)
                || VanillaStructureBuffer64.protectsSurface(worldDetailVersion, x, adjustedY, z,
                        worldY(x, CityPlan.STREET_Y, z), protectedVanillaStructures64);
    }

    private boolean isOceanCityWrite(int x, int worldY, int z) {
        int index = (x - minX) + ((z - minZ) << 4);
        return worldY >= 28 && ((oceanColumns65[index] & 0xff) > 0 || (dryCoastColumns66[index] & 0xff) > 0);
    }

    public void set(int x, int y, int z, BlockState state) {
        if (!contains(x, z)) {
            return;
        }
        int adjustedY = worldY(x, y, z);
        // Preserve the deep-dark habitat and future Ancient City footprints from our
        // own sewer/gallery/cavern painter. Vanilla structures place later in FEATURES.
        if (protectsVanillaWrite(x, adjustedY, z)) return;
        if (isOceanCityWrite(x, adjustedY, z)) return;
        BlockState previous = chunk.setBlockState(cursor.set(x, adjustedY, z), state, false);
        // A later write can replace a container. Its pending block entity must go with it, or the chunk
        // logs "Invalid block entity" on load and the replacement block is left with stray data.
        if (!state.hasBlockEntity() && previous != null && previous.hasBlockEntity()) {
            chunk.removeBlockEntity(cursor);
        }
        if (state.hasBlockEntity()) {
            CompoundTag tag = positioned(x, adjustedY, z);
            tag.putString("id", "DUMMY");
            chunk.setBlockEntityNbt(tag);
        }
    }

    /** Absolute world-Y placement for ocean terrain, without terraced city elevation.
     * Retains all vanilla structure and deep-dark protections from normal writes.
     */
    public void setRaw(int x, int worldY, int z, Block block) {
        if (!contains(x, z) || protectsVanillaWrite(x, worldY, z)) return;
        chunk.setBlockState(cursor.set(x, worldY, z), block.defaultBlockState(), false);
    }

    public void set(int x, int y, int z, Block block) {
        set(x, y, z, block.defaultBlockState());
    }

    public void set(int x, int y, int z, Mix mix) {
        if (contains(x, z)) {
            set(x, y, z, mix.pick(mixHash(mix, x, y, z)));
        }
    }

    /**
     * Read only from this chunk. Neighbor chunks may not have generated yet,
     * so outside reads are deliberately reported as void air rather than
     * querying an absent neighbor or indexing the local height cache.
     * Callers making placement decisions about neighbor support must still
     * use contains() for every queried column.
     */
    public BlockState get(int x, int y, int z) {
        if (!contains(x, z)) return Blocks.VOID_AIR.defaultBlockState();
        return chunk.getBlockState(cursor.set(x, worldY(x, y, z), z));
    }

    public void fill(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
        int fromX = Math.max(x0, minX);
        int toX = Math.min(x1, maxX);
        int fromZ = Math.max(z0, minZ);
        int toZ = Math.min(z1, maxZ);
        for (int x = fromX; x <= toX; x++) {
            for (int z = fromZ; z <= toZ; z++) {
                for (int y = y0; y <= y1; y++) {
                    set(x, y, z, state);
                }
            }
        }
    }

    public void fill(int x0, int y0, int z0, int x1, int y1, int z1, Block block) {
        fill(x0, y0, z0, x1, y1, z1, block.defaultBlockState());
    }

    public void fill(int x0, int y0, int z0, int x1, int y1, int z1, Mix mix) {
        int fromX = Math.max(x0, minX);
        int toX = Math.min(x1, maxX);
        int fromZ = Math.max(z0, minZ);
        int toZ = Math.min(z1, maxZ);
        for (int x = fromX; x <= toX; x++) {
            for (int z = fromZ; z <= toZ; z++) {
                for (int y = y0; y <= y1; y++) {
                    set(x, y, z, mix.pick(mixHash(mix, x, y, z)));
                }
            }
        }
    }

    public void fill(Rect rect, int y0, int y1, Mix mix) {
        fill(rect.x0(), y0, rect.z0(), rect.x1(), y1, rect.z1(), mix);
    }

    public void fill(Rect rect, int y0, int y1, Block block) {
        fill(rect.x0(), y0, rect.z0(), rect.x1(), y1, rect.z1(), block);
    }

    public void clear(Rect rect, int y0, int y1) {
        fill(rect, y0, y1, Blocks.AIR);
    }

    /** The four outer walls of a rectangle. */
    public void walls(Rect rect, int y0, int y1, Mix mix) {
        fill(rect.x0(), y0, rect.z0(), rect.x1(), y1, rect.z0(), mix);
        fill(rect.x0(), y0, rect.z1(), rect.x1(), y1, rect.z1(), mix);
        fill(rect.x0(), y0, rect.z0(), rect.x0(), y1, rect.z1(), mix);
        fill(rect.x1(), y0, rect.z0(), rect.x1(), y1, rect.z1(), mix);
    }

    /** A pane or bar run along one axis, with each piece's connections set directly so no neighbour update is needed. */
    public void paneRun(int x0, int y0, int z0, int x1, int y1, int z1, Block pane) {
        boolean alongX = x1 != x0;
        boolean alongZ = z1 != z0;
        BlockState state = pane.defaultBlockState()
                .setValue(CrossCollisionBlock.EAST, alongX)
                .setValue(CrossCollisionBlock.WEST, alongX)
                .setValue(CrossCollisionBlock.NORTH, alongZ)
                .setValue(CrossCollisionBlock.SOUTH, alongZ);
        fill(x0, y0, z0, x1, y1, z1, state);
    }

    /** A pane in a wall running along {@code axis}. */
    public void pane(int x, int y, int z, Direction.Axis axis, Block pane) {
        boolean alongX = axis == Direction.Axis.X;
        set(x, y, z, pane.defaultBlockState()
                .setValue(CrossCollisionBlock.EAST, alongX)
                .setValue(CrossCollisionBlock.WEST, alongX)
                .setValue(CrossCollisionBlock.NORTH, !alongX)
                .setValue(CrossCollisionBlock.SOUTH, !alongX));
    }

    public void fenceRun(int x0, int y, int z0, int x1, int z1, Block fence) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                set(x, y, z, fence.defaultBlockState()
                        .setValue(CrossCollisionBlock.EAST, x < x1)
                        .setValue(CrossCollisionBlock.WEST, x > x0)
                        .setValue(CrossCollisionBlock.SOUTH, z < z1)
                        .setValue(CrossCollisionBlock.NORTH, z > z0));
            }
        }
    }

    /** A rectangular fence with a one-block gap at {@code (gapX, gapZ)}. */
    public void fenceRing(Rect rect, int y, Block fence, int gapX, int gapZ) {
        fenceRun(rect.x0(), y, rect.z0(), rect.x1(), rect.z0(), fence);
        fenceRun(rect.x0(), y, rect.z1(), rect.x1(), rect.z1(), fence);
        fenceRun(rect.x0(), y, rect.z0(), rect.x0(), rect.z1(), fence);
        fenceRun(rect.x1(), y, rect.z0(), rect.x1(), rect.z1(), fence);
        set(gapX, y, gapZ, Blocks.AIR);
    }

    public void door(int x, int y, int z, Direction facing, Block door) {
        BlockState lower = door.defaultBlockState()
                .setValue(DoorBlock.FACING, facing)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
        set(x, y, z, lower);
        set(x, y + 1, z, lower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
    }

    /** A bed whose foot is at {@code (x, y, z)}, with the head one block toward {@code facing}. */
    public void bed(int x, int y, int z, Direction facing, Block bed) {
        BlockState foot = bed.defaultBlockState().setValue(BedBlock.FACING, facing).setValue(BedBlock.PART, BedPart.FOOT);
        set(x, y, z, foot);
        set(x + facing.getStepX(), y, z + facing.getStepZ(), foot.setValue(BedBlock.PART, BedPart.HEAD));
    }

    public void ladder(int x, int y0, int y1, int z, Direction facing) {
        BlockState state = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, facing);
        for (int y = y0; y <= y1; y++) {
            set(x, y, z, state);
        }
    }

    public void stair(int x, int y, int z, Direction facing, Block stair) {
        set(x, y, z, stair.defaultBlockState().setValue(StairBlock.FACING, facing));
    }

    public void facing(int x, int y, int z, Direction facing, Block block) {
        set(x, y, z, block.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing));
    }

    /** Preconfigured vanilla spawner, with a fixed custom-mob entity id and vanilla NBT schema. */
    public void spawner(int x, int y, int z, String entityId) {
        if (!contains(x, z)) return;
        int adjustedY = worldY(x, y, z);
        // The spawner block AND its NBT must be suppressed together in protected habitat.
        if (protectsVanillaWrite(x, adjustedY, z)) return;
        if (isOceanCityWrite(x, adjustedY, z)) return;
        chunk.setBlockState(cursor.set(x, adjustedY, z), Blocks.SPAWNER.defaultBlockState(), false);
        CompoundTag tag = positioned(x, adjustedY, z);
        tag.putString("id", "minecraft:mob_spawner");
        CompoundTag spawnData = new CompoundTag();
        CompoundTag entity = new CompoundTag();
        entity.putString("id", entityId);
        spawnData.put("entity", entity);
        tag.put("SpawnData", spawnData);
        tag.putShort("MinSpawnDelay", (short) 240);
        tag.putShort("MaxSpawnDelay", (short) 600);
        boolean ambient = entityId.endsWith("scrap_rat") || entityId.endsWith("glow_moth")
                || entityId.endsWith("rust_eater_beetle");
        tag.putShort("SpawnCount", (short) (ambient ? 1 : 2));
        tag.putShort("MaxNearbyEntities", (short) (ambient ? 3 : 5));
        tag.putShort("RequiredPlayerRange", (short) 14);
        tag.putShort("SpawnRange", (short) 3);
        chunk.setBlockEntityNbt(tag);
    }

    /** A chest that rolls {@code table} when a player first opens it. */
    public void chest(int x, int y, int z, Direction facing, String table) {
        container(x, y, z, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), "minecraft:chest", table);
    }

    public void barrel(int x, int y, int z, String table) {
        container(x, y, z, Blocks.BARREL.defaultBlockState(), "minecraft:barrel", table);
    }

    private void container(int x, int y, int z, BlockState state, String id, String table) {
        if (!contains(x, z)) {
            return;
        }
        int adjustedY = worldY(x, y, z);
        // Preserve the deep-dark habitat and future Ancient City footprints from our
        // own sewer/gallery/cavern painter. Vanilla structures place later in FEATURES.
        if (protectsVanillaWrite(x, adjustedY, z)) return;
        if (isOceanCityWrite(x, adjustedY, z)) return;
        chunk.setBlockState(cursor.set(x, adjustedY, z), state, false);
        CompoundTag tag = positioned(x, adjustedY, z);
        tag.putString("id", id);
        ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "chests/" + table));
        tag.putString("LootTable", key.location().toString());
        tag.putLong("LootTableSeed", hash(x, y, z));
        chunk.setBlockEntityNbt(tag);
    }

    /** A wall sign on the face of the block behind it, reading up to four lines. */
    public void sign(int x, int y, int z, Direction facing, Block wallSign, String... lines) {
        if (!contains(x, z)) {
            return;
        }
        int adjustedY = worldY(x, y, z);
        if (protectsVanillaWrite(x, adjustedY, z)) return;
        if (isOceanCityWrite(x, adjustedY, z)) return;
        chunk.setBlockState(cursor.set(x, adjustedY, z), wallSign.defaultBlockState().setValue(WallSignBlock.FACING, facing), false);
        ListTag messages = new ListTag();
        for (int i = 0; i < 4; i++) {
            String line = i < lines.length ? lines[i] : "";
            messages.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(line), net.minecraft.core.RegistryAccess.EMPTY)));
        }
        CompoundTag side = new CompoundTag();
        side.put("messages", messages);
        side.putString("color", "black");
        side.putBoolean("has_glowing_text", false);
        CompoundTag tag = positioned(x, adjustedY, z);
        tag.putString("id", "minecraft:sign");
        tag.put("front_text", side);
        tag.putBoolean("is_waxed", true);
        chunk.setBlockEntityNbt(tag);
    }

    private static CompoundTag positioned(int x, int y, int z) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", x);
        tag.putInt("y", y);
        tag.putInt("z", z);
        return tag;
    }
}
