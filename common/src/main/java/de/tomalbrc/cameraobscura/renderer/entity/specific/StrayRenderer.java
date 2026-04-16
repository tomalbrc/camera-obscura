package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public class StrayRenderer extends SkeletonRenderer {
    private static final String STRAY_TEXTURE = "entity/skeleton/stray";
    private static final String OVERLAY_TEXTURE = "entity/skeleton/stray_overlay";

    private ModelBakery.BakedPart cachedOverlay;

    @Override
    protected String getTexturePath(AbstractSkeleton entity) {
        return STRAY_TEXTURE;
    }

    private ModelBakery.BakedPart getOverlayModel() {
        if (cachedOverlay == null) {
            ModelBakery bakery = new ModelBakery(OVERLAY_TEXTURE, getTexWidth(), getTexHeight());
            ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
            buildModelParts(model.root(), bakery);
            cachedOverlay = model.root().bake();
        }
        return cachedOverlay;
    }

    @Override
    public void render(RenderPipeline pipeline, AbstractSkeleton entity) {
        super.render(pipeline, entity);

        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyYaw);
        double headYaw = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYaw);
        double headPitch = Mth.DEG_TO_RAD * entity.getXRot(1.0f);
        double animPos = entity.walkAnimation.position();
        double animSpeed = entity.walkAnimation.speed();
        boolean baby = entity.isBaby();

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y, pos.z)
                .rotateY(modelYaw)
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        if (baby) base.scale(0.5f);

        double swim = entity.getSwimAmount(1.0f);
        boolean flying = entity.isFallFlying() && entity.getFallFlyingTicks() > 0;
        boolean crouch = entity.isCrouching();
        boolean passenger = entity.isPassenger();

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
        angles = getLimbAngles(entity, animPos, animSpeed, headYaw, headPitch, swim, flying, crouch, angles);
        if (passenger) applyPassengerPose(angles);

        ArmPose poseR = getArmPose(entity, HumanoidArm.RIGHT);
        ArmPose poseL = getArmPose(entity, HumanoidArm.LEFT);

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        Matrix4d[] dummy = new Matrix4d[1];
        renderPart(pipeline, getOverlayModel(), "root", base,
                angles, poseR, poseL,
                entity.getMainHandItem(), entity.getOffhandItem(), entity,
                swim, flying, crouch, animPos,
                dummy, dummy, dummy, block, sky);
    }
}
