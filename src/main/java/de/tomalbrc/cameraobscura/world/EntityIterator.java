package de.tomalbrc.cameraobscura.world;

import de.tomalbrc.cameraobscura.ModConfig;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.chunk.LevelChunk;
import org.joml.Vector2i;
import org.joml.Vector3f;
import oshi.util.tuples.Triplet;

import java.util.List;
import java.util.Map;

public class EntityIterator extends AbstractWorldIterator<EntityIterator.EntityHit> {
    private final LivingEntity entity;

    private List<Triplet<Entity, Vector3f, Vector3f>> allEntities;

    public EntityIterator(ServerLevel level, Map<Vector2i, LevelChunk> cachedChunks, LivingEntity entity) {
        super(level, cachedChunks);
        this.entity = entity;
    }

    @Override
    public List<EntityHit> raycast(ClipContext clipContext) {
        if (this.allEntities == null) {
            // only query all nearby entities once
            this.allEntities = new ObjectArrayList<>();
            if (ModConfig.getInstance().renderEntities) {
                for (Entity entity : this.level.getEntities(this.entity, this.entity.getBoundingBox().inflate(ModConfig.getInstance().renderDistance))) {
                    if (entity instanceof LivingEntity livingEntity) {
                        this.allEntities.add(new Triplet<>(entity, entity.position().toVector3f(), new Vector3f(entity.getXRot(), livingEntity.yBodyRot, 0)));
                    } else {
                        this.allEntities.add(new Triplet<>(entity, entity.position().toVector3f(), new Vector3f(entity.getXRot(), entity.getYRot(), 0)));
                    }
                }
            }
        }

        List<EntityHit> hits = new ObjectArrayList<>();
        for (Triplet<Entity, Vector3f, Vector3f> entity: this.allEntities) {
            if (entity.getA().getBoundingBox().inflate(2).intersects(clipContext.getFrom(), clipContext.getTo())/* && this.entity.hasLineOfSight(entity.getA())*/) {
                hits.add(new EntityHit(entity.getA(), entity.getB(), entity.getC()));
            }
        }

        return hits;
    }

    public record EntityHit(Entity entity, Vector3f position, Vector3f rotation) {}
}
