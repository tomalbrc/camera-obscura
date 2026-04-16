package de.tomalbrc.cameraobscura.renderer.entity;

import de.tomalbrc.cameraobscura.model.triangle.ModelTesselator;
import de.tomalbrc.cameraobscura.renderer.EntityRenderers;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import de.tomalbrc.cameraobscura.util.BuiltinEntityModels;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4d;

public interface EntityRenderer<T extends Entity> {
    void render(RenderPipeline pipeline, T obj);

    default void renderDefault(RenderPipeline pipeline, Matrix4d transform, T ent, double block, double sky) {
        if (ent.isInvisible()) return;

        var cachedModel = EntityRenderers.ENTITY_TYPE_MODELS.get(ent.getType());
        if (cachedModel == null) {
            var view = BuiltinEntityModels.getModel(ent.getType(), ent.getUUID());
            if (view != null) {
                ModelTesselator tri = new ModelTesselator(view);
                var raster = tri.build();
                if (raster != null) {
                    cachedModel = new Model(tri.build(), block, sky);
                    EntityRenderers.ENTITY_TYPE_MODELS.put(ent.getType(), cachedModel);
                }
            }
        }

        if (cachedModel != null) {
            cachedModel.blockLight = new double[]{block};
            cachedModel.skyLight = new double[]{sky};

            if (transform == null) transform = new Matrix4d()
                    .translate(ent.position().toVector3f())
                    .rotateY(Mth.DEG_TO_RAD * (180f - ent.getPreciseBodyRotation(0.5f)));

            pipeline.draw(new DrawCommand(RenderType.ENTITY, cachedModel, transform, IntList.of(0xFFFFFFFF)));
        }
    }
}
