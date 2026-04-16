package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

import java.util.HashMap;
import java.util.Map;

public class FrogRenderer implements LivingEntityRenderer<Frog> {

    private static final Map<String, ModelBakery.BakedPart> MODEL_CACHE = new HashMap<>();

    @Override
    public ModelBakery.BakedPart buildRoot(Frog entity) {
        String texture = entity.getVariant().value().assetInfo().id().toString();
        return MODEL_CACHE.computeIfAbsent(texture, t -> buildModel(t));
    }

    private ModelBakery.BakedPart buildModel(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 48, 48);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition modelRoot = root.addOrReplaceChild("root",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(0, 24, 0));

        ModelBakery.PartDefinition body = modelRoot.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(3, 1).addBox(-3.5f, -2, -8, 7, 3, 9)
                        .texOffs(23, 22).addBox(-3.5f, -1, -8, 7, 0, 9),
                ModelBakery.PartPose.offset(0, -2, 4));

        ModelBakery.PartDefinition head = body.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(23, 13).addBox(-3.5f, -1, -7, 7, 0, 9)
                        .texOffs(0, 13).addBox(-3.5f, -2, -7, 7, 3, 9),
                ModelBakery.PartPose.offset(0, -2, -1));

        ModelBakery.PartDefinition eyes = head.addOrReplaceChild("eyes", ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(-0.5f, 0, 2));
        eyes.addOrReplaceChild("right_eye",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-1.5f, -1, -1.5f, 3, 2, 3),
                ModelBakery.PartPose.offset(-1.5f, -3, -6.5f));
        eyes.addOrReplaceChild("left_eye",
                ModelBakery.CubeListBuilder.create().texOffs(0, 5).addBox(-1.5f, -1, -1.5f, 3, 2, 3),
                ModelBakery.PartPose.offset(2.5f, -3, -6.5f));

        body.addOrReplaceChild("croaking_body",
                ModelBakery.CubeListBuilder.create().texOffs(26, 5).addBox(-3.5f, -0.1f, -2.9f, 7, 2, 3, new ModelBakery.CubeDeformation(-0.1f)),
                ModelBakery.PartPose.offset(0, -1, -5));

        body.addOrReplaceChild("tongue",
                ModelBakery.CubeListBuilder.create().texOffs(17, 13).addBox(-2, 0, -7.1f, 4, 0, 7),
                ModelBakery.PartPose.offset(0, -1.01f, 1));

        ModelBakery.PartDefinition leftArm = body.addOrReplaceChild("left_arm",
                ModelBakery.CubeListBuilder.create().texOffs(0, 32).addBox(-1, 0, -1, 2, 3, 3),
                ModelBakery.PartPose.offset(4, -1, -6.5f));
        leftArm.addOrReplaceChild("left_hand",
                ModelBakery.CubeListBuilder.create().texOffs(18, 40).addBox(-4, 0.01f, -4, 8, 0, 8),
                ModelBakery.PartPose.offset(0, 3, -1));

        ModelBakery.PartDefinition rightArm = body.addOrReplaceChild("right_arm",
                ModelBakery.CubeListBuilder.create().texOffs(0, 38).addBox(-1, 0, -1, 2, 3, 3),
                ModelBakery.PartPose.offset(-4, -1, -6.5f));
        rightArm.addOrReplaceChild("right_hand",
                ModelBakery.CubeListBuilder.create().texOffs(2, 40).addBox(-4, 0.01f, -5, 8, 0, 8),
                ModelBakery.PartPose.offset(0, 3, 0));

        ModelBakery.PartDefinition leftLeg = modelRoot.addOrReplaceChild("left_leg",
                ModelBakery.CubeListBuilder.create().texOffs(14, 25).addBox(-1, 0, -2, 3, 3, 4),
                ModelBakery.PartPose.offset(3.5f, -3, 4));
        leftLeg.addOrReplaceChild("left_foot",
                ModelBakery.CubeListBuilder.create().texOffs(2, 32).addBox(-4, 0.01f, -4, 8, 0, 8),
                ModelBakery.PartPose.offset(2, 3, 0));

        ModelBakery.PartDefinition rightLeg = modelRoot.addOrReplaceChild("right_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 25).addBox(-2, 0, -2, 3, 3, 4),
                ModelBakery.PartPose.offset(-3.5f, -3, 4));
        rightLeg.addOrReplaceChild("right_foot",
                ModelBakery.CubeListBuilder.create().texOffs(18, 32).addBox(-4, 0.01f, -4, 8, 0, 8),
                ModelBakery.PartPose.offset(-2, 3, 0));

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, Frog entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyYaw);
        double walkPos = entity.walkAnimation.position();
        double walkSpeed = entity.walkAnimation.speed();
        boolean swimming = entity.isInWater();
        boolean croaking = entity.croakAnimationState.isStarted();
        boolean tongueOut = entity.tongueAnimationState.isStarted();
        boolean jumping = entity.jumpAnimationState.isStarted();
        double age = entity.tickCount + 1.0f;

        double limbSwing = walkPos * 0.6662f;
        double limbSpeed = walkSpeed;
        double rightLegAngle = Mth.cos(limbSwing) * 1.4f * limbSpeed;
        double leftLegAngle = Mth.cos(limbSwing + Mth.PI) * 1.4f * limbSpeed;
        double rightArmAngle = Mth.cos(limbSwing + Mth.PI) * 1.0f * limbSpeed;
        double leftArmAngle = Mth.cos(limbSwing) * 1.0f * limbSpeed;

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + 1.5f, pos.z)
                .rotateY(modelYaw)
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        AnimState state = new AnimState(
                rightLegAngle, leftLegAngle, rightArmAngle, leftArmAngle,
                swimming, croaking, tongueOut, jumping, age
        );

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderPart(pipeline, buildRoot(entity), "root", base, state, block, sky);
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                            Matrix4d parent, AnimState state,
                            double block, double sky) {

        if (name.equals("croaking_body") && !state.croaking) return;
        if (name.equals("tongue") && !state.tongueOut) return;

        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        switch (name) {
            case "body":
                double bob = state.swimming ? 0 : Mth.cos(state.age * 0.06f) * 0.15f;
                mat.translate(0, bob / 16f, 0);
                if (state.jumping) mat.scale(1.1f, 0.9f, 1.1f);
                break;
            case "head":
                if (state.croaking) mat.rotateX(Mth.sin(state.age * 0.2f) * 0.1f);
                break;
            case "croaking_body":
                double scale = 1.0f + 0.05f * Mth.sin(state.age * 0.3f);
                mat.scale(scale, 1, scale);
                break;
            case "left_arm":
                mat.rotateX(state.leftArmAngle);
                break;
            case "right_arm":
                mat.rotateX(state.rightArmAngle);
                break;
            case "left_leg":
                mat.rotateX(state.leftLegAngle);
                break;
            case "right_leg":
                mat.rotateX(state.rightLegAngle);
                break;
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }

        for (var child : part.children.entrySet()) {
            renderPart(pipeline, child.getValue(), child.getKey(), mat, state, block, sky);
        }
    }

    private static class AnimState {
        final double rightLegAngle, leftLegAngle, rightArmAngle, leftArmAngle;
        final boolean swimming, croaking, tongueOut, jumping;
        final double age;

        AnimState(double rightLegAngle, double leftLegAngle, double rightArmAngle, double leftArmAngle,
                  boolean swimming, boolean croaking, boolean tongueOut, boolean jumping, double age) {
            this.rightLegAngle = rightLegAngle;
            this.leftLegAngle = leftLegAngle;
            this.rightArmAngle = rightArmAngle;
            this.leftArmAngle = leftArmAngle;
            this.swimming = swimming;
            this.croaking = croaking;
            this.tongueOut = tongueOut;
            this.jumping = jumping;
            this.age = age;
        }
    }
}