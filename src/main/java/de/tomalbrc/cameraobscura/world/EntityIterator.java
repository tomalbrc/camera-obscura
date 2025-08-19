package de.tomalbrc.cameraobscura.world;

import com.mojang.logging.LogUtils;
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
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Comparator;
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
            List<Entity> lst = this.level.getEntities(this.entity, this.entity.getBoundingBox().inflate(ModConfig.getInstance().renderDistance));
            lst.sort(Comparator.comparingDouble(a -> a.position().distanceTo(entity1.position())));

            for (int i = 0; i < lst.size() && this.allEntities.size() <= ModConfig.getInstance().renderEntitiesAmount; i++) {
                Entity entity = lst.get(i);
                EntityHit hit;

                if (!isInFrustum(entity1.getViewVector(1.f), entity1.position(), entity.position(), ModConfig.getInstance().fov+10) || !entity1.hasLineOfSight(entity)) continue;

                switch (entity) {
                    case LivingEntity livingEntity ->
                            hit = new EntityHit(entity.getType(), entity.getBoundingBox().inflate(1), entity.position().toVector3f(), new Vector3f(0, livingEntity.yBodyRot, 0), entity.getUUID(), null);
                    case ItemFrame itemFrame -> {
                        var rot = itemFrame.getDirection().getRotation();
                        hit = new EntityHit(entity.getType(), entity.getBoundingBox().inflate(1), entity.position().toVector3f(), rot.getEulerAnglesXYZ(new Vector3f()).mul(Mth.RAD_TO_DEG), entity.getUUID(), null);
                    }
                    case ItemEntity itemEntity ->
                            hit = new EntityHit(entity.getType(), entity.getBoundingBox(), entity.position().toVector3f(), new Vector3f(0, itemEntity.getVisualRotationYInDegrees(), 0), entity.getUUID(), itemEntity.getItem().copy());
                    default ->
                            hit = new EntityHit(entity.getType(), entity.getBoundingBox().inflate(1), entity.position().toVector3f(), new Vector3f(entity.getXRot(), entity.getYRot(), 0), entity.getUUID(), null);
                }

                // Cache player textures
                if (entity.getType() == EntityType.PLAYER) {
                    try {
                        RPHelper.loadTextureImage(CachedResourceLocationDeserializer.get(Constants.DYNAMIC_PLAYER_TEXTURE +":"+ entity.getUUID().toString().replace("-", "")));
                    } catch (Exception e) {
                        LogUtils.getLogger().info("Could not render player");
                        continue;
                    }
                }

                this.allEntities.add(hit);
            }
        }
    }

    public boolean isInFrustum(Vec3 viewVector, Vec3 cameraPosition, Vec3 targetPosition, float fov) {
        Vec3 normalizedDirection = targetPosition.subtract(cameraPosition).normalize();
        Vec3 normalizedViewVector = viewVector.normalize();
        double dotProduct = normalizedViewVector.dot(normalizedDirection);
        return dotProduct >= Math.cos(Math.toRadians(fov) / 2.0);
    }

    @Override
    public List<EntityHit> raycast(ClipContext clipContext) {
        List<EntityHit> hits = new ObjectArrayList<>();
        for (int i = 0; i < this.allEntities.size(); i++) {
            EntityHit entityHit = this.allEntities.get(i);
            if (entityHit.boundingBox().intersects(clipContext.getFrom(), clipContext.getTo())) {
                hits.add(entityHit);
            }
        }
        return hits;
    }

    public record EntityHit(EntityType type, AABB boundingBox, Vector3fc position, Vector3fc rotation, UUID uuid, Object data) {}
}
