package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.Monster;

public class ZombieRenderer<T extends Monster> extends HumanoidRenderer<T> {
    @Override
    protected String getTexturePath(T z) {
        return z.isBaby() ? "entity/zombie/zombie_baby" : "entity/zombie/zombie";
    }

    @Override
    protected int getTexWidth() {
        return 64;
    }

    @Override
    protected int getTexHeight() {
        return 64;
    }

    @Override
    protected void buildModelParts(ModelBakery.PartDefinition root, ModelBakery bakery) {
        var head = root.addOrReplaceChild("head", ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8), ModelBakery.PartPose.offset(0, 0, 0));
        head.addOrReplaceChild("hat", ModelBakery.CubeListBuilder.create().texOffs(32, 0).addBox(-4, -8, -4, 8, 8, 8, new ModelBakery.CubeDeformation(0.5F)), ModelBakery.PartPose.ZERO);

        root.addOrReplaceChild("body", ModelBakery.CubeListBuilder.create().texOffs(16, 16).addBox(-4, 0, -2, 8, 12, 4), ModelBakery.PartPose.offset(0, 0, 0));

        root.addOrReplaceChild("right_arm", ModelBakery.CubeListBuilder.create().texOffs(40, 16).addBox(-3, -2, -2, 4, 12, 4), ModelBakery.PartPose.offset(-5, 2, 0));
        root.addOrReplaceChild("left_arm", ModelBakery.CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1, -2, -2, 4, 12, 4), ModelBakery.PartPose.offset(5, 2, 0));

        root.addOrReplaceChild("right_leg", ModelBakery.CubeListBuilder.create().texOffs(0, 16).addBox(-2, 0, -2, 4, 12, 4), ModelBakery.PartPose.offset(-1.9f, 12, 0));
        root.addOrReplaceChild("left_leg", ModelBakery.CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2, 0, -2, 4, 12, 4), ModelBakery.PartPose.offset(1.9f, 12, 0));
    }

    @Override
    protected ArmPose getArmPose(T entity, HumanoidArm arm) {
        return ArmPose.ITEM;
    }

    @Override
    protected LimbAngles getLimbAngles(T entity, double animPos, double animSpeed, double headYaw, double headPitch, double swim, boolean flying, boolean crouch, LimbAngles base) {
        boolean aggressive = entity.isAggressive();
        double attackTime = entity.getAttackAnim(1f);

        double armDrop = (double) -Math.PI / (aggressive ? 1.5F : 2.25F);
        double attackYRotModifier = Mth.sin(attackTime * (double) Math.PI);
        double attackXRotModifier = Mth.sin((1.0F - (1.0F - attackTime) * (1.0F - attackTime)) * (double) Math.PI);
        base.rightArmZ = 0.0F;
        base.rightArmYaw = -(0.1F - attackYRotModifier * 0.6F);
        base.rightArmAngle = armDrop;
        base.rightArmAngle += attackYRotModifier * 1.2F - attackXRotModifier * 0.4F;
        base.leftArmZ = 0.0F;
        base.leftArmYaw = 0.1F - attackYRotModifier * 0.6F;
        base.leftArmAngle = armDrop;
        base.leftArmAngle += attackYRotModifier * 1.2F - attackXRotModifier * 0.4F;

        return super.getLimbAngles(entity, animPos, animSpeed, headYaw, headPitch, swim, flying, crouch, base);
    }

}