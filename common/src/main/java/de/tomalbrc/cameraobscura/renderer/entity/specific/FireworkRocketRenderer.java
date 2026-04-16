package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.ItemStackRenderer;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4d;
import org.joml.Matrix4fc;

public class FireworkRocketRenderer extends BillboardItemEntityRenderer<FireworkRocketEntity> {
    public FireworkRocketRenderer() {
        super(FireworkRocketEntity::getItem, 1);
    }

    @Override
    public void render(RenderPipeline pipeline, FireworkRocketEntity entity) {
        ItemStack item = entity.getItem();
        if (item.isEmpty()) return;

        Matrix4fc viewMatrix = pipeline.getCamera().getViewMatrix();
        Matrix4d viewRot = new Matrix4d(viewMatrix);
        viewRot.setTranslation(0, 0, 0);
        Matrix4d cameraWorldRot = new Matrix4d(viewRot).invert();

        if (entity.isShotAtAngle()) {
            cameraWorldRot.rotateZ((double) Math.toRadians(180));
            cameraWorldRot.rotateY((double) Math.toRadians(180));
            cameraWorldRot.rotateX((double) Math.toRadians(90));
        }

        Matrix4d modelMatrix = new Matrix4d()
                .translate(entity.position().toVector3f())
                .mul(cameraWorldRot)
                .scale(scale);

        ItemStackRenderer.render(pipeline, item, ItemDisplayContext.FIXED, modelMatrix);
    }
}