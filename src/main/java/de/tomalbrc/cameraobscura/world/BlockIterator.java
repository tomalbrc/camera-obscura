package de.tomalbrc.cameraobscura.world;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.joml.Vector2i;

import java.util.List;
import java.util.Map;

public class BlockIterator {
    public record WorldHit(BlockPos blockPos, BlockState blockState, FluidState fluidState, FluidState fluidStateAbove) {
        public boolean isWater() {
            if (fluidState != null && !fluidState.isEmpty()) {
                return fluidState.is(FluidTags.WATER);
            }
            return false;
        }
        public boolean isLava() {
            if (fluidState != null && !fluidState.isEmpty()) {
                return fluidState.is(FluidTags.LAVA);
            }
            return false;
        }
    }

    private final Level level;

    private final Map<Vector2i, LevelChunk> cachedChunks;

    public BlockIterator(Level level) {
        this.level = level;
        this.cachedChunks = new Object2ObjectOpenHashMap<>();
    }

    public void preloadChunks(BlockPos center, int distance) {
        int xc = SectionPos.blockToSectionCoord(center.getX());
        int zc = SectionPos.blockToSectionCoord(center.getZ());

        int radius = distance/16+1;
        for (int z = -radius; z <= radius; z++) {
            for (int x = -radius; x <= radius; x++) {
                this.getChunkAt(new Vector2i(x+xc, z+zc));
            }
        }
    }

    private LevelChunk getChunkAt(Vector2i pos) {
        LevelChunk chunk = null;
        if (this.cachedChunks.containsKey(pos)) {
            chunk = this.cachedChunks.get(pos);
        } else {
            chunk = this.level.getChunk(pos.x, pos.y);
            cachedChunks.put(pos, chunk);
        }
        return chunk;
    }

    private LevelChunk getChunkAt(BlockPos blockPos) {
        return this.getChunkAt(new Vector2i(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ())));
    }

    private FluidState cachedFluidState(BlockPos blockPos) {
        if (this.level.isOutsideBuildHeight(blockPos)) {
            return Fluids.EMPTY.defaultFluidState();
        } else {
            LevelChunk levelChunk = this.getChunkAt(blockPos);
            return levelChunk.getFluidState(blockPos);
        }
    }

    public BlockState cachedBlockState(BlockPos blockPos) {
        if (this.level.isOutsideBuildHeight(blockPos)) {
            return Blocks.AIR.defaultBlockState();
        } else {
            LevelChunk levelChunk = this.getChunkAt(new Vector2i(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ())));
            return levelChunk.getBlockState(blockPos);
        }
    }

    public List<WorldHit> raycast(ClipContext clipContext) {
        List<WorldHit> list = new ObjectArrayList<>();

        WorldHit hitResult = BlockGetter.traverseBlocks(clipContext.getFrom(), clipContext.getTo(), clipContext, (context, blockPos) -> {
            BlockState blockState = this.cachedBlockState(blockPos);
            FluidState fluidState = this.cachedFluidState(blockPos);
            FluidState fluidStateAbove = null;
            if (!fluidState.isEmpty()) {
                fluidStateAbove = this.cachedFluidState(blockPos.above());
            }

            if (!blockState.isSolidRender(level, blockPos) || blockState.isAir()) {
                if (!blockState.isAir()) {
                    list.add(new WorldHit(new BlockPos(blockPos), blockState, fluidState, fluidStateAbove));
                }

                return null; // keep searching
            }

            Vec3 from = context.getFrom();
            Vec3 to = context.getTo();

            BlockHitResult blockHitResult = this.level.clipWithInteractionOverride(from, to, blockPos, Shapes.block(), blockState);

            return blockHitResult != null ? new WorldHit(new BlockPos(blockPos), blockState, fluidState, fluidStateAbove) : null;
        }, (clipContextx) -> null);

        if (hitResult != null)
            list.add(hitResult);

        return list;
    }
}
