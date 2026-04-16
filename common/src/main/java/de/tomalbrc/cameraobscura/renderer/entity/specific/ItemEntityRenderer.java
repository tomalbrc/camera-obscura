package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.EntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ItemStackRenderer;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public class ItemEntityRenderer implements EntityRenderer<ItemEntity> {

    @Override
    public void render(RenderPipeline pipeline, ItemEntity entity) {
        var item = entity.getItem();
        if (!item.isEmpty()) {
            var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
            var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

            var transform = new Matrix4d().translate(entity.position().toVector3f()).rotateY((entity.tickCount % 3600) * 2.5f * Mth.DEG_TO_RAD).translate(0, (double) Math.sin(entity.tickCount / 10f) * 0.1f + 0.25f, 0);
            ItemStackRenderer.render(pipeline, item, ItemDisplayContext.GROUND, transform, block, sky);
        }
    }
}
