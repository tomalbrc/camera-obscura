package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.EntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public class TridentRenderer implements EntityRenderer<ThrownTrident> {
    private static final String TEXTURE = "entity/trident/trident";
    private static ModelBakery.BakedPart CACHED_MODEL;

    @Override
    public void render(RenderPipeline pipeline, ThrownTrident entity) {
        if (CACHED_MODEL == null) {
            CACHED_MODEL = buildModel();
        }

        double yRot = entity.getYRot(1.0f);
        double xRot = entity.getXRot(1.0f);

        Matrix4d transform = new Matrix4d()
                .translate(entity.position().toVector3f())
                .rotateY(Mth.DEG_TO_RAD * (180 - yRot))
                .rotateZ(Mth.DEG_TO_RAD * 90f)
                .rotateX(Mth.DEG_TO_RAD * (xRot + 90.0f));

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderPart(pipeline, CACHED_MODEL, transform, block, sky);
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part, Matrix4d parent, double block, double sky) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }

        for (var child : part.children.entrySet()) {
            renderPart(pipeline, child.getValue(), mat, block, sky);
        }
    }

    private ModelBakery.BakedPart buildModel() {
        ModelBakery bakery = new ModelBakery(TEXTURE, 32, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition pole = root.addOrReplaceChild("pole",
                ModelBakery.CubeListBuilder.create().texOffs(0, 6).addBox(-0.5f, 2.0f, -0.5f, 1.0f, 25.0f, 1.0f),
                ModelBakery.PartPose.ZERO);

        pole.addOrReplaceChild("base",
                ModelBakery.CubeListBuilder.create().texOffs(4, 0).addBox(-1.5f, 0.0f, -0.5f, 3.0f, 2.0f, 1.0f),
                ModelBakery.PartPose.ZERO);

        pole.addOrReplaceChild("left_spike",
                ModelBakery.CubeListBuilder.create().texOffs(4, 3).addBox(-2.5f, -3.0f, -0.5f, 1.0f, 4.0f, 1.0f),
                ModelBakery.PartPose.ZERO);

        pole.addOrReplaceChild("middle_spike",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-0.5f, -4.0f, -0.5f, 1.0f, 4.0f, 1.0f),
                ModelBakery.PartPose.ZERO);

        pole.addOrReplaceChild("right_spike",
                ModelBakery.CubeListBuilder.create().texOffs(4, 3).mirror().addBox(1.5f, -3.0f, -0.5f, 1.0f, 4.0f, 1.0f),
                ModelBakery.PartPose.ZERO);

        return root.bake();
    }
}