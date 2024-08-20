package de.tomalbrc.cameraobscura.world;

import de.tomalbrc.cameraobscura.ModConfig;
import de.tomalbrc.cameraobscura.json.CachedResourceLocationDeserializer;
import de.tomalbrc.cameraobscura.util.Constants;
import de.tomalbrc.cameraobscura.util.RPHelper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
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
                EntityHit hit;

                if (!entity1.hasLineOfSight(entity)) continue;

                if (entity instanceof LivingEntity livingEntity) {
                    hit = new EntityHit(entity.getType(), entity.getBoundingBox().inflate(1), entity.position().toVector3f(), new Vector3f(entity.getXRot(), livingEntity.yBodyRot, 0), entity.getUUID(), null);
                } else if (entity instanceof ItemFrame itemFrame) {
                    var rot = itemFrame.getDirection().getRotation();
                    hit = new EntityHit(entity.getType(), entity.getBoundingBox().inflate(1), entity.position().toVector3f(), rot.getEulerAnglesXYZ(new Vector3f()).mul(Mth.RAD_TO_DEG), entity.getUUID(), null);
                } else if (entity instanceof ItemEntity itemEntity) {
                    hit = new EntityHit(entity.getType(), entity.getBoundingBox(), entity.position().toVector3f(), new Vector3f(0, itemEntity.getVisualRotationYInDegrees(), 0), entity.getUUID(), itemEntity.getItem().copy());
                } else {
                    hit = new EntityHit(entity.getType(), entity.getBoundingBox().inflate(1), entity.position().toVector3f(), new Vector3f(entity.getXRot(), entity.getYRot(), 0), entity.getUUID(), null);
                }

                // Cache player textures
                if (entity.getType() == EntityType.PLAYER) {
                    RPHelper.loadTextureImage(CachedResourceLocationDeserializer.get(Constants.DYNAMIC_PLAYER_TEXTURE +":"+ entity.getUUID().toString().replace("-", "")));
                }

                this.allEntities.add(hit);
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

    public record EntityHit(EntityType type, AABB boundingBox, Vector3fc position, Vector3fc rotation, UUID uuid, Object data) {}
}
