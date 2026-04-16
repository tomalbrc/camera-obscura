package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public class TadpoleRenderer implements LivingEntityRenderer<Tadpole> {
    private static final String TEXTURE = "entity/tadpole/tadpole";
    private ModelBakery.BakedPart cachedModel;

    @Override
    public ModelBakery.BakedPart buildRoot(Tadpole entity) {
        if (cachedModel == null) {
            cachedModel = buildModel();
        }
        return cachedModel;
    }

    private ModelBakery.BakedPart buildModel() {
        ModelBakery bakery = new ModelBakery(TEXTURE, 16, 16);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-1.5f, -1, 0, 3, 2, 3),
                ModelBakery.PartPose.offset(0, 22, -3));

        root.addOrReplaceChild("tail",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(0, -1, 0, 0, 2, 7),
                ModelBakery.PartPose.offset(0, 22, 0));

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, Tadpole entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyYaw);
        boolean inWater = entity.isInWater();
        double age = entity.tickCount + 1.0f;

        double amplitudeMultiplier = inWater ? 1.0f : 1.5f;
        double tailYRot = -amplitudeMultiplier * 0.25f * Mth.sin(0.3f * age);

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + 1.5f, pos.z)
                .rotateY(modelYaw)
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderPart(pipeline, buildRoot(entity), "root", base, tailYRot, block, sky);
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                            Matrix4d parent, double tailYRot,
                            double block, double sky) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        if (name.equals("tail")) {
            mat.rotateY(tailYRot);
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh), mat, IntList.of(0xFFFFFFFF)));
        }

        for (var child : part.children.entrySet()) {
            renderPart(pipeline, child.getValue(), child.getKey(), mat, tailYRot, block, sky);
        }
    }
}
