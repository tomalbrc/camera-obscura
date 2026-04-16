package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import org.joml.Matrix4d;

public class ZombifiedPiglinRenderer extends HumanoidRenderer<ZombifiedPiglin> {

    private static final String ADULT_TEXTURE = "entity/piglin/zombified_piglin";
    private static final String BABY_TEXTURE = "entity/piglin/zombified_piglin_baby";

    private ModelBakery.BakedPart cachedAdultRoot;
    private ModelBakery.BakedPart cachedBabyRoot;

    @Override
    protected String getTexturePath(ZombifiedPiglin entity) {
        return entity.isBaby() ? BABY_TEXTURE : ADULT_TEXTURE;
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
    public ModelBakery.BakedPart buildRoot(ZombifiedPiglin entity) {
        if (entity.isBaby()) {
            if (cachedBabyRoot == null) cachedBabyRoot = buildModel(true);
            return cachedBabyRoot;
        } else {
            if (cachedAdultRoot == null) cachedAdultRoot = buildModel(false);
            return cachedAdultRoot;
        }
    }

    private ModelBakery.BakedPart buildModel(boolean baby) {
        String texture = baby ? BABY_TEXTURE : ADULT_TEXTURE;
        ModelBakery bakery = new ModelBakery(texture, 64, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition head = root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-5, -8, -4, 10, 8, 8)
                        .texOffs(31, 1).addBox(-2, -4, -5, 4, 4, 1)
                        .texOffs(2, 4).addBox(2, -2, -5, 1, 2, 1)
                        .texOffs(2, 0).addBox(-3, -2, -5, 1, 2, 1),
                ModelBakery.PartPose.ZERO);

        float earZ = baby ? -5.0f * Mth.DEG_TO_RAD : -30.0f * Mth.DEG_TO_RAD;
        head.addOrReplaceChild("left_ear",
                ModelBakery.CubeListBuilder.create().texOffs(51, 6).addBox(0, 0, -2, 1, 5, 4),
                ModelBakery.PartPose.offsetAndRotation(4.5f, -6, 0, 0, 0, earZ));
        head.addOrReplaceChild("right_ear",
                ModelBakery.CubeListBuilder.create().texOffs(39, 6).addBox(-1, 0, -2, 1, 5, 4),
                ModelBakery.PartPose.offsetAndRotation(-4.5f, -6, 0, 0, 0, -earZ));

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(16, 16).addBox(-4, 0, -2, 8, 12, 4),
                ModelBakery.PartPose.offset(0, 0, 0));

        root.addOrReplaceChild("right_arm",
                ModelBakery.CubeListBuilder.create().texOffs(40, 16).addBox(-3, -2, -2, 4, 12, 4),
                ModelBakery.PartPose.offset(-5, 2, 0));
        root.addOrReplaceChild("left_arm",
                ModelBakery.CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1, -2, -2, 4, 12, 4),
                ModelBakery.PartPose.offset(5, 2, 0));

        root.addOrReplaceChild("right_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 16).addBox(-2, 0, -2, 4, 12, 4),
                ModelBakery.PartPose.offset(-1.9f, 12, 0));
        root.addOrReplaceChild("left_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2, 0, -2, 4, 12, 4),
                ModelBakery.PartPose.offset(1.9f, 12, 0));

        return root.bake();
    }

    @Override
    protected void buildModelParts(ModelBakery.PartDefinition root, ModelBakery bakery) {

    }

    @Override
    protected LimbAngles getLimbAngles(ZombifiedPiglin entity, double animPos, double animSpeed,
                                       double headYaw, double headPitch,
                                       double swim, boolean flying, boolean crouch,
                                       LimbAngles base) {
        boolean aggressive = entity.isAggressive();
        double attackTime = entity.getAttackAnim(1f);
        double armDrop = -Math.PI / (aggressive ? 1.5F : 2.25F);
        double attackYRotModifier = Mth.sin(attackTime * Math.PI);
        double attackXRotModifier = Mth.sin((1.0F - (1.0F - attackTime) * (1.0F - attackTime)) * Math.PI);
        double angle = armDrop + attackYRotModifier * 1.2F - attackXRotModifier * 0.4F;
        base.rightArmAngle = angle;
        base.leftArmAngle = angle;
        base.rightArmYaw = -(0.1F - attackYRotModifier * 0.6F);
        base.leftArmYaw = 0.1F - attackYRotModifier * 0.6F;
        double bob = Mth.cos(entity.tickCount * 0.09F) * 0.05F + 0.05F;
        base.rightArmZ = bob;
        base.leftArmZ = -bob;
        return base;
    }

    @Override
    protected void transformPart(Matrix4d mat, String name, ZombifiedPiglin entity, LimbAngles angles,
                                 double swim, boolean fly, boolean crouch) {
        if (name.equals("left_ear") || name.equals("right_ear")) {
            double defaultAngle = (entity.isBaby() ? 5.0f : 30.0f) * Mth.DEG_TO_RAD;
            double frequency = entity.tickCount * 0.1f + entity.walkAnimation.position() * 0.5f;
            double amplitude = 0.08f + entity.walkAnimation.speed() * 0.4f;
            double earZRot = name.equals("left_ear")
                    ? -defaultAngle - Mth.cos(frequency * 1.2f) * amplitude
                    : defaultAngle + Mth.cos(frequency) * amplitude;
            mat.rotateZ(earZRot);
        }
    }
}