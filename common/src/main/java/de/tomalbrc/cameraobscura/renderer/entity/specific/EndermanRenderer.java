package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.EnderMan;
import org.joml.Matrix4d;

public class EndermanRenderer extends HumanoidRenderer<EnderMan> {

    @Override
    protected String getTexturePath(EnderMan entity) {
        return "entity/enderman/enderman";
    }

    @Override
    protected int getTexWidth() {
        return 64;
    }

    @Override
    protected int getTexHeight() {
        return 32;
    }

    @Override
    protected void buildModelParts(ModelBakery.PartDefinition root, ModelBakery bakery) {
        var head = root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8),
                ModelBakery.PartPose.offset(0, -13, 0));
        head.addOrReplaceChild("hat",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-4, -8, -4, 8, 8, 8, new ModelBakery.CubeDeformation(-0.5F)),
                ModelBakery.PartPose.ZERO);

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(32, 16).addBox(-4, 0, -2, 8, 12, 4),
                ModelBakery.PartPose.offset(0, -14, 0));

        root.addOrReplaceChild("right_arm",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(56, 0).addBox(-1, -2, -1, 2, 30, 2),
                ModelBakery.PartPose.offset(-5, -12, 0));
        root.addOrReplaceChild("left_arm",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(56, 0).mirror().addBox(-1, -2, -1, 2, 30, 2),
                ModelBakery.PartPose.offset(5, -12, 0));

        root.addOrReplaceChild("right_leg",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(56, 0).addBox(-1, 0, -1, 2, 30, 2),
                ModelBakery.PartPose.offset(-2, -5, 0));
        root.addOrReplaceChild("left_leg",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(56, 0).mirror().addBox(-1, 0, -1, 2, 30, 2),
                ModelBakery.PartPose.offset(2, -5, 0));
    }

    @Override
    protected ArmPose getArmPose(EnderMan entity, HumanoidArm arm) {
        return ArmPose.EMPTY;
    }

    @Override
    protected LimbAngles getLimbAngles(EnderMan entity, double animPos, double animSpeed,
                                       double headYaw, double headPitch,
                                       double swim, boolean flying, boolean crouch,
                                       LimbAngles base) {
        double limbSwing = animPos * 0.6662F;
        double limbBase = 1.4F * animSpeed;

        double rawRightLeg = Mth.cos(limbSwing) * limbBase;
        double rawRightArm = Mth.cos(limbSwing + Mth.PI) * limbBase;
        double rawLeftArm = Mth.cos(limbSwing) * limbBase;

        base.rightLegAngle = Mth.clamp(rawRightLeg * 0.5F, -0.4F, 0.4F);
        base.leftLegAngle = Mth.clamp(-rawRightLeg * 0.5F, -0.4F, 0.4F);
        base.rightArmAngle = Mth.clamp(rawRightArm * 0.5F, -0.4F, 0.4F);
        base.leftArmAngle = Mth.clamp(rawLeftArm * 0.5F, -0.4F, 0.4F);
        base.rightArmYaw = base.leftArmYaw = 0;
        base.rightArmZ = base.leftArmZ = 0;

        if (entity.getCarriedBlock() != null) {
            base.rightArmAngle = -0.5F;
            base.leftArmAngle = -0.5F;
            base.rightArmZ = 0.05F;
            base.leftArmZ = -0.05F;
        }

        return base;
    }

    @Override
    protected void renderWithAngles(RenderPipeline pipeline, EnderMan entity, Matrix4d base, LimbAngles angles, double swim, boolean flying, boolean crouch, double animPos, double block, double sky) {
        super.renderWithAngles(pipeline, entity, base, angles, swim, flying, crouch, animPos, block, sky);

        //if (entity.getCarriedBlock() != null) {
        //    BlockStateRenderer.render(pipeline, entity.getCarriedBlock(), base);
        //}
    }

    @Override
    protected void transformPart(Matrix4d mat, String name, EnderMan entity,
                                 LimbAngles angles, double swim, boolean fly, boolean crouch) {
        if (!entity.isCreepy()) return;

        if (name.equals("head")) {
            mat.translate(0, -5.0F / 16.0F, 0);
        } else if (name.equals("hat")) {
            mat.translate(0, 5.0F / 16.0F, 0);
        }
    }
}