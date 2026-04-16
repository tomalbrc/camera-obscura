package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public class DrownedRenderer extends ZombieRenderer<Drowned> {
    private static final String ADULT_TEXTURE = "entity/zombie/drowned";
    private static final String BABY_TEXTURE = "entity/zombie/drowned_baby";
    private static final String OUTER_ADULT_TEXTURE = "entity/zombie/drowned_outer_layer";
    private static final String OUTER_BABY_TEXTURE = "entity/zombie/drowned_outer_layer_baby";

    private ModelBakery.BakedPart cachedOuterAdult;
    private ModelBakery.BakedPart cachedOuterBaby;

    @Override
    protected String getTexturePath(Drowned entity) {
        return entity.isBaby() ? BABY_TEXTURE : ADULT_TEXTURE;
    }

    private ModelBakery.BakedPart getOuterModel(boolean baby) {
        if (baby) {
            if (cachedOuterBaby == null)
                cachedOuterBaby = buildModel(OUTER_BABY_TEXTURE, true);
            return cachedOuterBaby;
        } else {
            if (cachedOuterAdult == null)
                cachedOuterAdult = buildModel(OUTER_ADULT_TEXTURE, false);
            return cachedOuterAdult;
        }
    }

    private ModelBakery.BakedPart buildModel(String texture, boolean baby) {
        ModelBakery bakery = new ModelBakery(texture, 64, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();
        buildModelParts(root, bakery); // same parts as zombie
        return root.bake();
    }

    @Override
    protected ArmPose getArmPose(Drowned entity, HumanoidArm arm) {
        return (entity.getMainArm() == arm && entity.isAggressive() && entity.getMainHandItem().is(Items.TRIDENT))
                ? ArmPose.THROW_TRIDENT
                : super.getArmPose(entity, arm);
    }

    @Override
    protected LimbAngles getLimbAngles(Drowned entity, double animPos, double animSpeed,
                                       double headYaw, double headPitch,
                                       double swim, boolean flying, boolean crouch,
                                       LimbAngles base) {
        LimbAngles angles = super.getLimbAngles(entity, animPos, animSpeed,
                headYaw, headPitch, swim, flying, crouch, base);

        float swimAmount = entity.getSwimAmount(1.0f);
        if (swimAmount > 0.0F) {
            double age = entity.tickCount;

            angles.rightArmAngle = Mth.rotLerpRad(swimAmount, (float) angles.rightArmAngle,
                    (float) (-Math.PI * 4.0 / 5.0))
                    + swimAmount * 0.35F * Mth.sin(0.1F * age);
            angles.leftArmAngle = Mth.rotLerpRad(swimAmount, (float) angles.leftArmAngle,
                    (float) (-Math.PI * 4.0 / 5.0))
                    - swimAmount * 0.35F * Mth.sin(0.1F * age);
            angles.rightArmZ = Mth.rotLerpRad(swimAmount, (float) angles.rightArmZ, -0.15F);
            angles.leftArmZ = Mth.rotLerpRad(swimAmount, (float) angles.leftArmZ, 0.15F);
            angles.rightLegAngle -= swimAmount * 0.55F * Mth.sin(0.1F * age);
            angles.leftLegAngle += swimAmount * 0.55F * Mth.sin(0.1F * age);
            angles.headPitch = 0;
        }
        return angles;
    }

    @Override
    public void render(RenderPipeline pipeline, Drowned entity) {
        super.render(pipeline, entity);

        boolean baby = entity.isBaby();
        ModelBakery.BakedPart outerModel = getOuterModel(baby);

        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyYaw);
        double headYaw = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYaw);
        double headPitch = Mth.DEG_TO_RAD * entity.getXRot(1.0f);
        double animPos = entity.walkAnimation.position();
        double animSpeed = entity.walkAnimation.speed();
        double swim = entity.getSwimAmount(1.0f);
        boolean flying = entity.isFallFlying() && entity.getFallFlyingTicks() > 0;
        boolean crouch = entity.isCrouching();
        boolean passenger = entity.isPassenger();

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y, pos.z)
                .rotateY(modelYaw)
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        if (baby) base.scale(0.5f);

        double bodyRotX = getBodyRotX(entity, swim, flying);
        double zOffset = getSwimZOffset(entity, swim);
        if (bodyRotX != 0) base.rotateX(bodyRotX);
        if (zOffset != 0) base.translate(0, 0, zOffset);
        else base.translate(0, -1.5f, 0);

        double limbSwing = animPos * 0.6662F;
        double speedValue = getSpeedValue(entity);
        LimbAngles angles = new LimbAngles();
        angles.headYaw = headYaw;
        angles.headPitch = headPitch;
        angles.rightLegAngle = Mth.cos(limbSwing) * 1.4F * animSpeed / speedValue;
        angles.leftLegAngle = Mth.cos(limbSwing + Mth.PI) * 1.4F * animSpeed / speedValue;
        double armWalkR = Mth.cos(limbSwing + Mth.PI) * 2.0F * animSpeed * 0.5F / speedValue;
        double armWalkL = Mth.cos(limbSwing) * 2.0F * animSpeed * 0.5F / speedValue;
        angles.rightArmAngle = armWalkR;
        angles.leftArmAngle = armWalkL;
        angles = getLimbAngles(entity, animPos, animSpeed, headYaw, headPitch,
                swim, flying, crouch, angles);
        if (passenger) applyPassengerPose(angles);

        ArmPose poseR = getArmPose(entity, HumanoidArm.RIGHT);
        ArmPose poseL = getArmPose(entity, HumanoidArm.LEFT);

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        Matrix4d[] dummy = new Matrix4d[1];
        renderPart(
                pipeline, outerModel, "root", base,
                angles, poseR, poseL,
                entity.getMainHandItem(), entity.getOffhandItem(), entity,
                swim, flying, crouch, animPos,
                dummy, dummy, dummy,
                block, sky
        );
    }
}