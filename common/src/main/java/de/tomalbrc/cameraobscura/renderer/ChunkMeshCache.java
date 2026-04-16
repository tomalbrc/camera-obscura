package de.tomalbrc.cameraobscura.renderer;

import de.tomalbrc.cameraobscura.ModConfig;
import de.tomalbrc.cameraobscura.color.BlockColors;
import de.tomalbrc.cameraobscura.model.resource.RPModel;
import de.tomalbrc.cameraobscura.model.triangle.FluidMeshBuilder;
import de.tomalbrc.cameraobscura.model.triangle.ModelTesselator;
import de.tomalbrc.cameraobscura.platform.Platforms;
import de.tomalbrc.cameraobscura.sore.model.Mesh;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import de.tomalbrc.cameraobscura.util.BuiltinModels;
import de.tomalbrc.cameraobscura.util.Constants;
import de.tomalbrc.cameraobscura.util.resource.RPHelper;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4d;
import org.jspecify.annotations.NonNull;

import java.util.*;

public class ChunkMeshCache {
    public static final int CHUNK_SIZE = 16;
    private static final int CHUNK_SHIFT = 4;
    private static final int BITS = 21;
    private static final long MASK = (1L << BITS) - 1;

    public static final Map<Level, Long2ObjectMap<ChunkMesh>> CHUNK_CACHE = Collections.synchronizedMap(new IdentityHashMap<>());
    private static final Long2ObjectMap<List<Mesh>> BLOCK_MESH_CACHE = new Long2ObjectOpenHashMap<>();
    private static final Map<Level, Long2ReferenceOpenHashMap<ChunkAccess>> VANILLA_CHUNKS = new IdentityHashMap<>();

    public static class FastBlockGetter implements BlockAndLightGetter {
        private final LevelLightEngine lightEngine;
        private final int minY;
        private final int height;
        Long2ReferenceOpenHashMap<ChunkAccess> chunkCache;

        public FastBlockGetter(Level level) {
            this.lightEngine = level.getLightEngine();
            this.minY = level.getMinY();
            this.height = level.getHeight();
            this.chunkCache = VANILLA_CHUNKS.get(level);
        }

        @Override
        public @NonNull LevelLightEngine getLightEngine() {
            return lightEngine;
        }

        @Nullable
        @Override
        public BlockEntity getBlockEntity(@NonNull BlockPos pos) {
            ChunkAccess chunk = getChunkAt(pos);
            if (chunk == null) return null;

            return chunk.getBlockEntity(pos);
        }

        @Override
        public @NonNull BlockState getBlockState(@NonNull BlockPos pos) {
            ChunkAccess chunk = getChunkAt(pos);
            if (chunk == null) return Blocks.AIR.defaultBlockState();
            return chunk.getBlockState(pos);
        }

        @Override
        public @NonNull FluidState getFluidState(@NonNull BlockPos pos) {
            ChunkAccess chunk = getChunkAt(pos);
            if (chunk == null) return Fluids.EMPTY.defaultFluidState();
            return chunk.getFluidState(pos);
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public int getMinY() {
            return minY;
        }

        private @Nullable ChunkAccess getChunkAt(BlockPos pos) {
            int cx = pos.getX() >> 4;
            int cz = pos.getZ() >> 4;
            long packed = ((long) cx & 0xFFFFFFFFL) | (((long) cz & 0xFFFFFFFFL) << 32);
            return chunkCache.get(packed);
        }
    }

    public static void onChunkLoaded(Level level, ChunkAccess chunk) {
        var packed = chunk.getPos().pack();
        VANILLA_CHUNKS.computeIfAbsent(level, key -> new Long2ReferenceOpenHashMap<>()).put(packed,  chunk);
    }

    public static void onChunkUnloaded(Level level, ChunkAccess chunk) {
        var packed = chunk.getPos().pack();
        var cache = VANILLA_CHUNKS.get(level);
        if (cache != null) {
            cache.remove(packed);
            if (cache.isEmpty()) {
                VANILLA_CHUNKS.remove(level);
            }
        }
    }

    @Nullable
    public static ChunkMeshCache.ChunkMesh getChunkMesh(Level level, BlockPos pos) {
        long key = chunkKeyFromBlock(pos);
        return getChunkMesh(level, key);
    }

    @Nullable
    public static ChunkMeshCache.ChunkMesh getChunkMesh(Level level, long key) {
        return CHUNK_CACHE.computeIfAbsent(level, k -> new Long2ObjectArrayMap<>()).get(key);
    }

    @Nullable
    public static ChunkMeshCache.ChunkMesh putChunkMesh(Level level, long key, ChunkMesh chunkMesh) {
        return CHUNK_CACHE.computeIfAbsent(level, k -> new Long2ObjectArrayMap<>()).put(key, chunkMesh);
    }

    public static void onBlockChanged(Level level, BlockPos pos) {
        long key = chunkKeyFromBlock(pos);
        ChunkMesh chunkMesh = getChunkMesh(level, key);
        if (chunkMesh != null) {
            chunkMesh.markDirty();
        }

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            long neighborKey = ChunkMeshCache.chunkKeyFromBlock(neighborPos);
            if (neighborKey != key) {
                ChunkMesh neighbor = getChunkMesh(level, neighborKey);
                if (neighbor != null && neighbor != chunkMesh) {
                    neighbor.markDirty();
                }
            }
        }
    }

    public static void onUnloadChunk(LevelChunk chunk, int x, int z) {

    }

    public static void onLoadChunk(LevelChunk chunk, int x, int z) {

    }

    private static long chunkKeyFromBlock(BlockPos pos) {
        long x = (long) (pos.getX() >> CHUNK_SHIFT) & MASK;
        long y = (long) (pos.getY() >> CHUNK_SHIFT) & MASK;
        long z = (long) (pos.getZ() >> CHUNK_SHIFT) & MASK;
        return x | (y << BITS) | (z << (BITS * 2));
    }

    public static BlockPos chunkOriginFromKey(long key) {
        int x = (int) (key & MASK);
        int y = (int) ((key >> BITS) & MASK);
        int z = (int) ((key >> (BITS * 2)) & MASK);

        x = (x << (32 - BITS)) >> (32 - BITS);
        y = (y << (32 - BITS)) >> (32 - BITS);
        z = (z << (32 - BITS)) >> (32 - BITS);

        return new BlockPos(x << CHUNK_SHIFT, y << CHUNK_SHIFT, z << CHUNK_SHIFT);
    }

    public static long chunkKeyFromCoords(int cx, int cy, int cz) {
        long lx = (long) cx & MASK;
        long ly = (long) cy & MASK;
        long lz = (long) cz & MASK;
        return lx | (ly << BITS) | (lz << (BITS * 2));
    }

    public static int toChunkCoord(int i) {
        return i >> CHUNK_SHIFT;
    }

    static boolean isOccluded(BlockGetter level, int wx, int wy, int wz) {
        BlockState state = level.getBlockState(new BlockPos(wx, wy, wz));
        if (!state.isSolidRender()) return false;
        for (Direction dir : Direction.values()) {
            BlockState neighbor = level.getBlockState(new BlockPos(wx + dir.getStepX(), wy + dir.getStepY(), wz + dir.getStepZ()));
            if (!neighbor.isSolidRender()) return false;
        }
        return true;
    }

    static Model buildModelWithLighting(BlockAndLightGetter level, int wx, int wy, int wz, Mesh mesh, BlockState state) {
        int vertexCount = mesh.positions.length / 3;
        double[] ao = new double[vertexCount];
        double[] skyLight = new double[vertexCount];
        double[] blockLight = new double[vertexCount];

        boolean solidRender = state.isSolidRender();

        for (int i = 0; i < vertexCount; i++) {
            double x = mesh.positions[i * 3];
            double y = mesh.positions[i * 3 + 1];
            double z = mesh.positions[i * 3 + 2];
            double nx = mesh.normals[i * 3];
            double ny = mesh.normals[i * 3 + 1];
            double nz = mesh.normals[i * 3 + 2];

            Direction faceDir = Direction.getApproximateNearest(nx, ny, nz);
            Direction[] sides = getAOSideDirections(faceDir, x, y, z);
            ao[i] = mesh.ao ? computeVertexAO(level, wx, wy, wz, faceDir, sides[0], sides[1]) : 1.f;

            double worldX = wx + x;
            double worldY = wy + y;
            double worldZ = wz + z;

            int sky, block;
            if (solidRender) {
                sky = getSmoothLight(level, worldX, worldY, worldZ, LightLayer.SKY, faceDir);
                block = getSmoothLight(level, worldX, worldY, worldZ, LightLayer.BLOCK, faceDir);
            } else {
                BlockPos pos = new BlockPos(wx, wy, wz);
                sky = level.getBrightness(LightLayer.SKY, pos);
                block = level.getBrightness(LightLayer.BLOCK, pos);
            }

            skyLight[i] = sky / 15.0f;
            blockLight[i] = block / 15.0f;
        }

        return new Model(mesh, skyLight, blockLight, ao);
    }

    private static int getSmoothLight(BlockAndLightGetter level, double wx, double wy, double wz, LightLayer layer, Direction face) {
        double eps = 0.001f;
        int fx = (int) Math.floor(wx + face.getStepX() * eps);
        int fy = (int) Math.floor(wy + face.getStepY() * eps);
        int fz = (int) Math.floor(wz + face.getStepZ() * eps);

        Direction[] tangents = Constants.FACE_TANGENTS[face.ordinal()];
        Direction t1 = tangents[0];
        Direction t2 = tangents[1];

        return (level.getBrightness(layer, new BlockPos(fx, fy, fz)) +
                level.getBrightness(layer, new BlockPos(fx - t1.getStepX(), fy - t1.getStepY(), fz - t1.getStepZ())) +
                level.getBrightness(layer, new BlockPos(fx - t2.getStepX(), fy - t2.getStepY(), fz - t2.getStepZ())) +
                level.getBrightness(layer, new BlockPos(fx - t1.getStepX() - t2.getStepX(), fy - t1.getStepY() - t2.getStepY(), fz - t1.getStepZ() - t2.getStepZ()))) / 4;
    }

    private static Direction[] getAOSideDirections(Direction faceDir, double x, double y, double z) {
        boolean hX = x >= 0.5f;
        boolean hY = y >= 0.5f;
        boolean hZ = z >= 0.5f;
        return switch (faceDir) {
            case UP, DOWN ->
                    new Direction[]{hX ? Direction.EAST : Direction.WEST, hZ ? Direction.SOUTH : Direction.NORTH};
            case NORTH, SOUTH ->
                    new Direction[]{hX ? Direction.EAST : Direction.WEST, hY ? Direction.UP : Direction.DOWN};
            case EAST, WEST ->
                    new Direction[]{hZ ? Direction.SOUTH : Direction.NORTH, hY ? Direction.UP : Direction.DOWN};
        };
    }

    private static double computeVertexAO(BlockGetter level, int wx, int wy, int wz, Direction faceDir, Direction side1, Direction side2) {
        int ox = wx + faceDir.getStepX();
        int oy = wy + faceDir.getStepY();
        int oz = wz + faceDir.getStepZ();

        boolean s1 = isSolidForAO(level, ox + side1.getStepX(), oy + side1.getStepY(), oz + side1.getStepZ());
        boolean s2 = isSolidForAO(level, ox + side2.getStepX(), oy + side2.getStepY(), oz + side2.getStepZ());
        boolean c = isSolidForAO(level, ox + side1.getStepX() + side2.getStepX(), oy + side1.getStepY() + side2.getStepY(), oz + side1.getStepZ() + side2.getStepZ());

        int val = (s1 && s2) ? 3 : (s1 ? 1 : 0) + (s2 ? 1 : 0) + (c ? 1 : 0);
        return 1.0f - val * 0.025f;
    }

    private static boolean isSolidForAO(BlockGetter level, int x, int y, int z) {
        return level.getBlockState(new BlockPos(x, y, z)).isSolidRender();
    }

    static List<Model> getModelForState(BlockAndLightGetter level, int wx, int wy, int wz, BlockState state) {
        List<RPModel.View> views = getModelViewForState(level, wx, wy, wz, state);
        if (views == null || views.isEmpty()) return null;

        EnumSet<Direction> culledFaces = computeCulledFaces(level, wx, wy, wz, state);

        long key = ((long) state.hashCode() << 32) | (culledFaces.hashCode() & 0xFFFFFFFFL);
        List<Mesh> meshes = BLOCK_MESH_CACHE.computeIfAbsent(key, k -> {
            List<Mesh> list = new ObjectArrayList<>(views.size());
            for (RPModel.View view : views) {
                var m = new ModelTesselator(view, culledFaces).build();
                if (m != null) list.add(m);
            }
            return list;
        });

        List<Model> result = new ObjectArrayList<>(meshes.size());
        for (Mesh mesh : meshes) {
            result.add(buildModelWithLighting(level, wx, wy, wz, mesh, state));
        }
        return result;
    }

    static List<Model> getFluidModel(FastBlockGetter level, int wx, int wy, int wz, FluidState fluidState) {
        BlockState blockState = level.getBlockState(new BlockPos(wx, wy, wz));
        BlockPos pos = new BlockPos(wx, wy, wz);

        var mesh = FluidMeshBuilder.createFluidMesh(level, pos, fluidState);
        if (mesh == null) return List.of();

        Model model = buildModelWithLighting(level, wx, wy, wz, mesh, blockState);
        return List.of(model);
    }

    private static EnumSet<Direction> computeCulledFaces(BlockGetter level, int wx, int wy, int wz, BlockState state) {
        EnumSet<Direction> culled = EnumSet.noneOf(Direction.class);
        if (state.isSolidRender()) {
            for (Direction dir : Direction.values()) {
                BlockState neighbor = level.getBlockState(new BlockPos(wx + dir.getStepX(), wy + dir.getStepY(), wz + dir.getStepZ()));
                if (neighbor.isSolidRender() && !neighbor.isAir()) {
                    culled.add(dir);
                }
            }
        }
        return culled;
    }

    private static List<RPModel.View> getModelViewForState(BlockGetter level, int wx, int wy, int wz, BlockState state) {
        BlockPos pos = new BlockPos(wx, wy, wz);
        RPModel.View model = getSpecialModel(level, wx, wy, wz, pos, state);
        if (model != null)
            return List.of(model);

        return RPHelper.loadBlockModelViews(state);
    }

    private static RPModel.View getSpecialModel(BlockGetter level, int wx, int wy, int wz, BlockPos pos, BlockState state) {
        if (state.is(Blocks.END_PORTAL)) return BuiltinModels.portalModel(true);
        if (state.is(Blocks.END_GATEWAY)) return BuiltinModels.portalModel(false);
        if (state.getBlock() instanceof AbstractChestBlock<?>) return BuiltinModels.chestModel(state);
        if (state.getBlock() instanceof ShulkerBoxBlock shulkerBoxBlock)
            return BuiltinModels.shulkerModel(state, Optional.ofNullable(shulkerBoxBlock.getColor()));

        if (state.is(BlockTags.BEDS)) return BuiltinModels.bedModel(state, Optional.empty());
        if (state.is(Blocks.DECORATED_POT)) return BuiltinModels.decoratedPotModel();
        if (state.is(Blocks.CONDUIT)) return BuiltinModels.conduitModel();
        if (state.is(BlockTags.ALL_SIGNS)) return BuiltinModels.signModel(state);
        if (state.is(Blocks.BELL)) return BuiltinModels.bellModel(state);
        return null;
    }

    private static int blendBlockColor(FastBlockGetter level, BlockPos pos, BlockState state, int radius) {
        if (radius <= 0) {
            ChunkAccess chunk = level.getChunkAt(pos);
            return 0xFF000000 | BlockColors.get(chunk, state, pos);
        }
        long r = 0, g = 0, b = 0;
        int samples = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                BlockPos samplePos = new BlockPos(pos.getX() + dx, pos.getY(), pos.getZ() + dz);
                ChunkAccess chunk = level.getChunkAt(samplePos);
                int color = BlockColors.get(chunk, state, samplePos); // returns RGB
                r += (color >> 16) & 0xFF;
                g += (color >> 8) & 0xFF;
                b += color & 0xFF;
                samples++;
            }
        }
        int avgR = (int) (r / samples);
        int avgG = (int) (g / samples);
        int avgB = (int) (b / samples);
        return 0xFF000000 | (avgR << 16) | (avgG << 8) | avgB;
    }

    private static int blendWaterColor(FastBlockGetter level, BlockPos pos, int radius) {
        if (radius <= 0) {
            ChunkAccess chunk = level.getChunkAt(pos);
            return 0xFF000000 | BlockColors.biomeWaterColor(chunk, pos);
        }
        long r = 0, g = 0, b = 0;
        int samples = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                BlockPos samplePos = new BlockPos(pos.getX() + dx, pos.getY(), pos.getZ() + dz);
                ChunkAccess chunk = level.getChunkAt(samplePos);
                int color = BlockColors.biomeWaterColor(chunk, samplePos);
                r += (color >> 16) & 0xFF;
                g += (color >> 8) & 0xFF;
                b += color & 0xFF;
                samples++;
            }
        }
        int avgR = (int) (r / samples);
        int avgG = (int) (g / samples);
        int avgB = (int) (b / samples);
        return 0xFF000000 | (avgR << 16) | (avgG << 8) | avgB;
    }

    static List<DrawCommand> buildChunkCommands(Level level, ChunkMesh chunkMesh) {
        FastBlockGetter fastBlockGetter = new FastBlockGetter(level);

        List<DrawCommand> commands = new ArrayList<>();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        int minX = chunkMesh.origin().getX();
        int minY = chunkMesh.origin().getY();
        int minZ = chunkMesh.origin().getZ();

        for (int dx = 0; dx < CHUNK_SIZE; dx++) {
            for (int dy = 0; dy < CHUNK_SIZE; dy++) {
                for (int dz = 0; dz < CHUNK_SIZE; dz++) {
                    int wx = minX + dx;
                    int wy = minY + dy;
                    int wz = minZ + dz;
                    pos.set(wx, wy, wz);

                    BlockState state = fastBlockGetter.getBlockState(pos);
                    FluidState fluidState = fastBlockGetter.getFluidState(pos);

                    if (state.isAir() && fluidState.isEmpty()) continue;

                    if (isOccluded(fastBlockGetter, wx, wy, wz)) continue;

                    int radius = ModConfig.getInstance().biomeBlend;
                    int blockColor = blendBlockColor(fastBlockGetter, pos, state, radius);
                    int waterColor = fluidState.is(FluidTags.WATER) ? blendWaterColor(fastBlockGetter, pos, radius) : 0xFF_FFFFFF;
                    IntList tints = IntList.of(0xFF000000 | blockColor, 0xFF000000 | waterColor);

                    if (!state.isAir()) {
                        if (state.getBlock() != Blocks.BARRIER && state.getBlock() != Blocks.STRUCTURE_BLOCK) {
                            List<Model> models = getModelForState(fastBlockGetter, wx, wy, wz, state);
                            if (models != null) {
                                Matrix4d transform = new Matrix4d().translate(wx, wy, wz);
                                for (Model model : models) {
                                    commands.add(new DrawCommand(model.mesh.translucent ? RenderType.TRANSLUCENT : RenderType.SOLID, model, transform, tints));
                                }
                            }
                        }

                        Platforms.get().renderBlock(level, pos, commands);
                    }

                    if (!fluidState.isEmpty()) {
                        List<Model> fluidModels = getFluidModel(fastBlockGetter, wx, wy, wz, fluidState);
                        IntList fluidTints = IntList.of(0xFF000000 | waterColor);
                        RenderType fluidType = RenderType.WATER;
                        Matrix4d transform = new Matrix4d().translate(wx, wy, wz);
                        for (Model model : fluidModels) {
                            commands.add(new DrawCommand(fluidType, model, transform, fluidTints));
                        }
                    }
                }
            }
        }
        return commands;
    }

    static void rebuildChunk(Level level, ChunkMesh chunkMesh) {
        try {
            chunkMesh.setDrawCommands(buildChunkCommands(level, chunkMesh));
        } catch (Exception e) {
            Platforms.get().getLogger().error("Failed to build chunk {}", chunkMesh.key());
        }
    }

    public static void clear() {
        CHUNK_CACHE.clear();
    }

    public static class ChunkMesh {
        private final BlockPos origin;
        private final long key;
        private boolean dirty = true;
        private volatile List<DrawCommand> drawCommands = List.of();

        ChunkMesh(BlockPos origin, long key) {
            this.origin = origin;
            this.key = key;
        }

        public void markDirty() {
            dirty = true;
        }

        public BlockPos origin() {
            return origin;
        }

        public long key() {
            return key;
        }

        public boolean isDirty() {
            return dirty;
        }

        public List<DrawCommand> drawCommands() {
            return drawCommands;
        }

        public void setDrawCommands(List<DrawCommand> drawCommands) {
            this.drawCommands = drawCommands;
            this.dirty = false;
        }
    }
}
