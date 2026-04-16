package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.EntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import org.joml.Matrix4d;

public class ShulkerBulletRenderer implements EntityRenderer<ShulkerBullet> {
    private static final String TEXTURE = "entity/shulker/spark";
    private ModelBakery.BakedPart cachedModel;

    @Override
    public void render(RenderPipeline pipeline, ShulkerBullet entity) {
        if (cachedModel == null) {
            cachedModel = buildModel();
        }

        double ageInTicks = entity.tickCount + 1.0f; // partialTick = 1

        Matrix4d transform = new Matrix4d()
                .translate(entity.position().toVector3f())
                .translate(0.0f, 0.15f, 0.0f)
                .rotateY(Mth.DEG_TO_RAD * Mth.sin(ageInTicks * 0.1f) * 180.0f)
                .rotateX(Mth.DEG_TO_RAD * Mth.cos(ageInTicks * 0.1f) * 180.0f)
                .rotateZ(Mth.DEG_TO_RAD * Mth.sin(ageInTicks * 0.15f) * 360.0f)
                .scale(-0.5f, -0.5f, 0.5f);

        renderPart(pipeline, cachedModel, transform);
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part, Matrix4d parent) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh), mat, IntList.of(0xFFFFFFFF)));
        }
        for (var child : part.children.entrySet()) {
            renderPart(pipeline, child.getValue(), mat);
        }
    }

    private ModelBakery.BakedPart buildModel() {
        ModelBakery bakery = new ModelBakery(TEXTURE, 64, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("main",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0f, -4.0f, -1.0f, 8.0f, 8.0f, 2.0f)
                        .texOffs(0, 10).addBox(-1.0f, -4.0f, -4.0f, 2.0f, 8.0f, 8.0f)
                        .texOffs(20, 0).addBox(-4.0f, -1.0f, -4.0f, 8.0f, 2.0f, 8.0f),
                ModelBakery.PartPose.ZERO);

        return root.bake();
    }
}