package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public class BatRenderer implements LivingEntityRenderer<Bat> {

    private static final String TEXTURE = "entity/bat/bat";
    private ModelBakery.BakedPart cachedModel;

    @Override
    public ModelBakery.BakedPart buildRoot(Bat entity) {
        if (cachedModel == null) {
            cachedModel = buildModel();
        }
        return cachedModel;
    }

    private ModelBakery.BakedPart buildModel() {
        ModelBakery bakery = new ModelBakery(TEXTURE, 32, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition body = root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 5.0F, 2.0F),
                ModelBakery.PartPose.offset(0.0F, 17.0F, 0.0F));

        ModelBakery.PartDefinition head = root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create().texOffs(0, 7).addBox(-2.0F, -3.0F, -1.0F, 4.0F, 3.0F, 2.0F),
                ModelBakery.PartPose.offset(0.0F, 17.0F, 0.0F));

        head.addOrReplaceChild("right_ear",
                ModelBakery.CubeListBuilder.create().texOffs(1, 15).addBox(-2.5F, -4.0F, 0.0F, 3.0F, 5.0F, 0.0F),
                ModelBakery.PartPose.offset(-1.5F, -2.0F, 0.0F));
        head.addOrReplaceChild("left_ear",
                ModelBakery.CubeListBuilder.create().texOffs(8, 15).addBox(-0.1F, -3.0F, 0.0F, 3.0F, 5.0F, 0.0F),
                ModelBakery.PartPose.offset(1.1F, -3.0F, 0.0F));

        ModelBakery.PartDefinition rightWing = body.addOrReplaceChild("right_wing",
                ModelBakery.CubeListBuilder.create().texOffs(12, 0).addBox(-2.0F, -2.0F, 0.0F, 2.0F, 7.0F, 0.0F),
                ModelBakery.PartPose.offset(-1.5F, 0.0F, 0.0F));
        rightWing.addOrReplaceChild("right_wing_tip",
                ModelBakery.CubeListBuilder.create().texOffs(16, 0).addBox(-6.0F, -2.0F, 0.0F, 6.0F, 8.0F, 0.0F),
                ModelBakery.PartPose.offset(-2.0F, 0.0F, 0.0F));

        ModelBakery.PartDefinition leftWing = body.addOrReplaceChild("left_wing",
                ModelBakery.CubeListBuilder.create().texOffs(12, 7).addBox(0.0F, -2.0F, 0.0F, 2.0F, 7.0F, 0.0F),
                ModelBakery.PartPose.offset(1.5F, 0.0F, 0.0F));
        leftWing.addOrReplaceChild("left_wing_tip",
                ModelBakery.CubeListBuilder.create().texOffs(16, 8).addBox(0.0F, -2.0F, 0.0F, 6.0F, 8.0F, 0.0F),
                ModelBakery.PartPose.offset(2.0F, 0.0F, 0.0F));

        body.addOrReplaceChild("feet",
                ModelBakery.CubeListBuilder.create().texOffs(16, 16).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 2.0F, 0.0F),
                ModelBakery.PartPose.offset(0.0F, 5.0F, 0.0F));

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, Bat entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double headYaw = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYaw);
        boolean resting = entity.isResting();
        double ageInTicks = entity.tickCount + 1.0f;

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + 1.5f, pos.z)
                .rotateY(Mth.DEG_TO_RAD * (180f - bodyYaw))
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        if (resting) {
            base.rotateX(Mth.PI);
        }

        double flap = Mth.sin(ageInTicks * 0.5f) * 0.8f;
        boolean hideWings = resting;
        AnimParams params = new AnimParams(headYaw, resting, hideWings, flap);

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderBatPart(pipeline, buildRoot(entity), "root", base, params, block, sky);
    }

    private void renderBatPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name, Matrix4d parent, AnimParams p, double block, double sky) {
        if (p.hideWings && (name.equals("right_wing") || name.equals("left_wing") ||
                name.equals("right_wing_tip") || name.equals("left_wing_tip"))) {
            return;
        }

        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        switch (name) {
            case "head" -> {
                if (p.resting) mat.rotateY(p.headYaw);
            }
            case "right_wing" -> mat.rotateZ(-p.flap);
            case "left_wing" -> mat.rotateZ(p.flap);
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }
        for (var child : part.children.entrySet()) {
            renderBatPart(pipeline, child.getValue(), child.getKey(), mat, p, block, sky);
        }
    }

    private static class AnimParams {
        final double headYaw;
        final boolean resting;
        final boolean hideWings;
        final double flap;

        AnimParams(double headYaw, boolean resting, boolean hideWings, double flap) {
            this.headYaw = headYaw;
            this.resting = resting;
            this.hideWings = hideWings;
            this.flap = flap;
        }
    }
}