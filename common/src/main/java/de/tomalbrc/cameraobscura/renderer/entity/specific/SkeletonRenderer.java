package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Items;
import org.joml.Vector3d;

public class SkeletonRenderer extends HumanoidRenderer<AbstractSkeleton> {
    @Override
    protected String getTexturePath(AbstractSkeleton e) {
        return "entity/skeleton/skeleton";
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
        var head = root.addOrReplaceChild("head", ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8), ModelBakery.PartPose.offset(0, 0, 0));
        head.addOrReplaceChild("hat", ModelBakery.CubeListBuilder.create().texOffs(32, 0).addBox(-4, -8, -4, 8, 8, 8, new ModelBakery.CubeDeformation(0.5F)), ModelBakery.PartPose.ZERO);

        root.addOrReplaceChild("body", ModelBakery.CubeListBuilder.create().texOffs(16, 16).addBox(-4, 0, -2, 8, 12, 4), ModelBakery.PartPose.offset(0, 0, 0));

        root.addOrReplaceChild("right_arm", ModelBakery.CubeListBuilder.create().texOffs(40, 16).addBox(-1, -2, -1, 2, 12, 2), ModelBakery.PartPose.offset(-5, 2, 0));
        root.addOrReplaceChild("left_arm", ModelBakery.CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1, -2, -1, 2, 12, 2), ModelBakery.PartPose.offset(5, 2, 0));

        root.addOrReplaceChild("right_leg", ModelBakery.CubeListBuilder.create().texOffs(0, 16).addBox(-1, 0, -1, 2, 12, 2), ModelBakery.PartPose.offset(-2, 12, 0));
        root.addOrReplaceChild("left_leg", ModelBakery.CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-1, 0, -1, 2, 12, 2), ModelBakery.PartPose.offset(2, 12, 0));
    }

    @Override
    protected LimbAngles getLimbAngles(AbstractSkeleton entity, double animPos, double animSpeed, double headYaw, double headPitch, double swim, boolean flying, boolean crouch, LimbAngles base) {
        boolean bow = entity.getMainHandItem().getItem() instanceof BowItem;
        double age = entity.tickCount;
        if (entity.isAggressive() && !bow) {
            double armDrop = -(double) Math.PI / 2;
            double attackTime = (age * 0.1f) % 2.0f;
            if (attackTime > 1.0f) attackTime = 2.0f - attackTime;
            double atkY = Mth.sin(attackTime * Mth.PI);
            double atkX = Mth.sin((1f - (1f - attackTime) * (1f - attackTime)) * Mth.PI);
            base.rightArmAngle = base.leftArmAngle = armDrop + atkY * 1.2f - atkX * 0.4f;
            base.rightArmYaw = -(0.1f - atkY * 0.6f);
            base.leftArmYaw = 0.1f - atkY * 0.6f;
            double bob = Mth.cos(age * 0.09f) * 0.05f + 0.05f;
            base.rightArmZ = bob;
            base.leftArmZ = -bob;
        }
        return base;
    }

    @Override
    protected ArmPose getArmPose(AbstractSkeleton mob, HumanoidArm arm) {
        return mob.getMainArm() == arm && mob.isAggressive() && mob.getMainHandItem().is(Items.BOW)
                ? ArmPose.BOW_AND_ARROW
                : super.getArmPose(mob, arm);
    }

    @Override
    protected Vector3d getHandItemOffset(AbstractSkeleton e, HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ? new Vector3d(1, 0, 0) : new Vector3d(-1, 0, 0);
    }
}
