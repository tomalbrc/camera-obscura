package de.tomalbrc.cameraobscura.world;

import de.tomalbrc.cameraobscura.ModConfig;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EntityIterator extends AbstractWorldIterator<EntityIterator.EntityHit> {
    private final LivingEntity entity;

    private final List<EntityHit> allEntities;

    public EntityIterator(ServerLevel level, Map<Vector2i, LevelChunk> cachedChunks, LivingEntity entity1) {
        super(level, cachedChunks);
        this.entity = entity1;

        this.allEntities = new ObjectArrayList<>();
        if (ModConfig.getInstance().renderEntities) {
            for (Entity entity : this.level.getEntities(this.entity, this.entity.getBoundingBox().inflate(ModConfig.getInstance().renderDistance))) {
                if (entity instanceof LivingEntity livingEntity) {
                    this.allEntities.add(new EntityHit(entity.getType(), entity.getBoundingBox().inflate(1), entity.position().toVector3f(), new Vector3f(entity.getXRot(), livingEntity.yBodyRot, 0), entity.getUUID()));
                } else if (entity instanceof ItemFrame itemFrame) {
                    Vec3 off = new Vec3(0,0.5,0.5).yRot(Mth.DEG_TO_RAD * itemFrame.getDirection().toYRot());
                    this.allEntities.add(new EntityHit(entity.getType(), entity.getBoundingBox().inflate(1), entity.position().subtract(off).toVector3f(), new Vector3f(itemFrame.getXRot(), itemFrame.getYRot(), 0), entity.getUUID()));
                } else {
                    this.allEntities.add(new EntityHit(entity.getType(), entity.getBoundingBox().inflate(1), entity.position().toVector3f(), new Vector3f(entity.getXRot(), entity.getYRot(), 0), entity.getUUID()));
                }
            }
        }
    }

    @Override
    public List<EntityHit> raycast(ClipContext clipContext) {
        List<EntityHit> hits = new ObjectArrayList<>();
        for (EntityHit entity: this.allEntities) {
            if (entity.boundingBox().intersects(clipContext.getFrom(), clipContext.getTo())/* && this.type.hasLineOfSight(type.getA())*/) {
                hits.add(entity);
            }
        }

        return hits;
    }

    public record EntityHit(EntityType type, AABB boundingBox, Vector3fc position, Vector3fc rotation, UUID uuid) {}
}
