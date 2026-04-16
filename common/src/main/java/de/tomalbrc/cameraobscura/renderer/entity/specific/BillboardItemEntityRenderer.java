package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.EntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ItemStackRenderer;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4d;
import org.joml.Matrix4fc;

import java.util.function.Function;

public class BillboardItemEntityRenderer<T extends Entity> implements EntityRenderer<T> {
    protected final Function<T, ItemStack> itemExtractor;
    protected final double scale;

    public BillboardItemEntityRenderer(Function<T, ItemStack> itemExtractor, double scale) {
        this.itemExtractor = itemExtractor;
        this.scale = scale;
    }

    @Override
    public void render(RenderPipeline pipeline, T entity) {
        ItemStack item = itemExtractor.apply(entity);
        if (item.isEmpty()) return;

        Matrix4fc viewMatrix = pipeline.getCamera().getViewMatrix();

        Matrix4d viewRot = new Matrix4d(viewMatrix);
        viewRot.setTranslation(0, 0, 0);

        Matrix4d cameraWorldRot = new Matrix4d(viewRot).invert();

        Matrix4d modelMatrix = new Matrix4d()
                .translate(entity.position().toVector3f())
                .mul(cameraWorldRot)
                .scale(scale);

        ItemStackRenderer.render(pipeline, item, ItemDisplayContext.FIXED, modelMatrix);
    }
}