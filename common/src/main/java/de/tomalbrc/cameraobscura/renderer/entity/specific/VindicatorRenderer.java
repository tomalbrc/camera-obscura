package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.ItemStackRenderer;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import net.minecraft.world.entity.monster.illager.Vindicator;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public class VindicatorRenderer extends IllagerRenderer<Vindicator> {

    @Override
    protected String getTexture(Vindicator entity) {
        return "entity/illager/vindicator";
    }

    @Override
    public void render(RenderPipeline pipeline, Vindicator entity) {
        super.render(pipeline, entity);
        if (entity.isAggressive()) {
            ItemStack held = entity.getMainHandItem();
            if (!held.isEmpty()) {
                var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
                var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

                Matrix4d base = computeBaseMatrix(entity);

                Matrix4d handMat = new Matrix4d(base)
                        .translate(-5f / 16f, 2f / 16f, 0)
                        .rotateX(-Math.PI / 5)
                        .translate(0, -0.55f, -0.15f)
                        .rotateX(-Math.PI / 2)
                        .rotateY(Math.PI);

                ItemStackRenderer.render(pipeline, held, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, handMat, block, sky);
            }
        }
    }
}