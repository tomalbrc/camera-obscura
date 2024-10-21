package de.tomalbrc.cameraobscura.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.chunk.LevelChunk;
import org.joml.Vector2i;

import java.util.List;
import java.util.Map;

abstract public class AbstractWorldIterator<T> {
    protected final ServerLevel level;

    private final Map<Vector2i, LevelChunk> cachedChunks;

    public AbstractWorldIterator(ServerLevel level, Map<Vector2i, LevelChunk> cachedChunks) {
        this.level = level;
        this.cachedChunks = cachedChunks;
    }

    public void preloadChunks(BlockPos center, int distance) {
        int xc = SectionPos.blockToSectionCoord(center.getX());
        int zc = SectionPos.blockToSectionCoord(center.getZ());

        int radius = distance / 16 + 1;
        for (int z = -radius; z <= radius; z++) {
            for (int x = -radius; x <= radius; x++) {
                this.getChunkAt(new Vector2i(x + xc, z + zc));
            }
        }
    }

    protected LevelChunk getChunkAt(Vector2i pos) {
        LevelChunk levelChunk = this.cachedChunks.get(pos);
        if (levelChunk != null) {
            return levelChunk;
        } else {
            levelChunk = this.level.getChunk(pos.x, pos.y);
            cachedChunks.put(pos, levelChunk);
            return levelChunk;
        }
    }

    public LevelChunk getChunkAt(BlockPos blockPos) {
        return this.getChunkAt(new Vector2i(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ())));
    }

    abstract public List<T> raycast(ClipContext clipContext);
}
