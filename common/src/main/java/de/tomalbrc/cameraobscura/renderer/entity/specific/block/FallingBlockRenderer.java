package de.tomalbrc.cameraobscura.renderer.entity.specific.block;

import de.tomalbrc.cameraobscura.renderer.BlockStateRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.EntityRenderer;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public class FallingBlockRenderer implements EntityRenderer<FallingBlockEntity> {
    @Override
    public void render(RenderPipeline pipeline, FallingBlockEntity entity) {
        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        Matrix4d transform = new Matrix4d().translate(entity.position().toVector3f());
        BlockStateRenderer.render(pipeline, entity.getBlockState(), transform, block, sky);
    }
}