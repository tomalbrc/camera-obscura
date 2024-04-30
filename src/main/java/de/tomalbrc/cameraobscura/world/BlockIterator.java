package de.tomalbrc.cameraobscura.world;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
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
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector2i;

import java.util.List;
import java.util.Map;


public class BlockIterator {
    private final Level level;

    private final Map<Vector2i, LevelChunk> cachedChunks;

    public BlockIterator(Level level) {
        this.level = level;
        this.cachedChunks = new Object2ObjectArrayMap<>();
    }

    private LevelChunk getChunkAt(Vector2i pos) {
        LevelChunk chunk = null;
        if (this.cachedChunks.containsKey(pos)) {
            chunk = this.cachedChunks.get(pos);
        } else {
            chunk = this.level.getChunk(pos.x, pos.y);
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

    public BlockHitResult raycast(ClipContext clipContext) {
        return BlockGetter.traverseBlocks(clipContext.getFrom(), clipContext.getTo(), clipContext, (context, blockPos) -> {
            BlockState blockState = this.cachedBlockState(blockPos);
            FluidState fluidState = this.cachedFluidState(blockPos);
            Vec3 vec3 = context.getFrom();
            Vec3 vec32 = context.getTo();
            VoxelShape voxelShape = context.getBlockShape(blockState, this.level, blockPos);
            BlockHitResult blockHitResult = this.level.clipWithInteractionOverride(vec3, vec32, blockPos, voxelShape, blockState);
            VoxelShape voxelShape2 = context.getFluidShape(fluidState, this.level, blockPos);
            BlockHitResult blockHitResult2 = voxelShape2.clip(vec3, vec32, blockPos);
            double d = blockHitResult == null ? Double.MAX_VALUE : context.getFrom().distanceToSqr(blockHitResult.getLocation());
            double e = blockHitResult2 == null ? Double.MAX_VALUE : context.getFrom().distanceToSqr(blockHitResult2.getLocation());
            return d <= e ? blockHitResult : blockHitResult2;
        }, (clipContextx) -> {
            Vec3 vec3 = clipContextx.getFrom().subtract(clipContextx.getTo());
            return BlockHitResult.miss(clipContextx.getTo(), Direction.getNearest(vec3.x, vec3.y, vec3.z), BlockPos.containing(clipContextx.getTo()));
        });
    }

    record TestResult(BlockState blockState, FluidState fluidState) {
    }
}
