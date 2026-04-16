package de.tomalbrc.cameraobscura.renderer.entity;

import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import net.minecraft.world.entity.LivingEntity;

public interface LivingEntityRenderer<T extends LivingEntity> extends EntityRenderer<T> {
    void render(RenderPipeline pipeline, T obj);

    ModelBakery.BakedPart buildRoot(T cow);

    static int hurtTint(LivingEntity entity) {
        return entity.hurtTime > 0 ? 0xFF6666 : 0xFFFFFF;
    }
}
