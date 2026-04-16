package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.EntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public class LlamaSpitRenderer implements EntityRenderer<LlamaSpit> {
    private static final String TEXTURE = "entity/llama/llama_spit";
    private ModelBakery.BakedPart cachedModel;

    @Override
    public void render(RenderPipeline pipeline, LlamaSpit entity) {
        if (cachedModel == null) {
            cachedModel = buildModel();
        }

        double yRot = entity.getYRot(1.0f);
        double xRot = entity.getXRot(1.0f);

        Matrix4d transform = new Matrix4d()
                .translate(entity.position().toVector3f())
                .translate(0.0f, 0.15f, 0.0f)
                .rotateY(Mth.DEG_TO_RAD * (yRot - 90.0f))
                .rotateX(Mth.DEG_TO_RAD * xRot);

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;
        renderPart(pipeline, cachedModel, transform, block, sky);
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part, Matrix4d parent, double block, double sky) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose pose = part.initialPose;
        if (pose.xRot() != 0 || pose.yRot() != 0 || pose.zRot() != 0)
            mat.rotateZYX(pose.zRot(), pose.yRot(), pose.xRot());
        if (pose.xScale() != 1 || pose.yScale() != 1 || pose.zScale() != 1)
            mat.scale(pose.xScale(), pose.yScale(), pose.zScale());

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }

        for (ModelBakery.BakedPart entry : part.children.values()) {
            renderPart(pipeline, entry, mat, block, sky);
        }
    }

    private ModelBakery.BakedPart buildModel() {
        ModelBakery bakery = new ModelBakery(TEXTURE, 64, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("main",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0f, 0.0f, 0.0f, 2.0f, 2.0f, 2.0f)
                        .addBox(0.0f, -4.0f, 0.0f, 2.0f, 2.0f, 2.0f)
                        .addBox(0.0f, 0.0f, -4.0f, 2.0f, 2.0f, 2.0f)
                        .addBox(0.0f, 0.0f, 0.0f, 2.0f, 2.0f, 2.0f)
                        .addBox(2.0f, 0.0f, 0.0f, 2.0f, 2.0f, 2.0f)
                        .addBox(0.0f, 2.0f, 0.0f, 2.0f, 2.0f, 2.0f)
                        .addBox(0.0f, 0.0f, 2.0f, 2.0f, 2.0f, 2.0f),
                ModelBakery.PartPose.ZERO
        );

        return root.bake();
    }
}