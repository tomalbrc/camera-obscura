package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;
import net.minecraft.world.item.CrossbowItem;
import org.joml.Matrix4d;

public class PiglinRenderer<T extends AbstractPiglin> extends HumanoidRenderer<T> {
    private static final String ADULT_TEXTURE = "entity/piglin/piglin";
    private static final String BABY_TEXTURE = "entity/piglin/piglin_baby";
    private static final String BRUTE_TEXTURE = "entity/piglin/piglin_brute";

    private ModelBakery.BakedPart cachedAdultRoot;
    private ModelBakery.BakedPart cachedBabyRoot;

    @Override
    protected String getTexturePath(T entity) {
        if (entity.isBaby()) return BABY_TEXTURE;
        return entity.getType() == net.minecraft.world.entity.EntityType.PIGLIN_BRUTE ? BRUTE_TEXTURE : ADULT_TEXTURE;
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
    public ModelBakery.BakedPart buildRoot(T entity) {
        if (entity.isBaby()) {
            if (cachedBabyRoot == null) cachedBabyRoot = buildBabyModel();
            return cachedBabyRoot;
        } else {
            if (cachedAdultRoot == null) cachedAdultRoot = buildAdultModel(getTexturePath(entity));
            return cachedAdultRoot;
        }
    }

    private ModelBakery.BakedPart buildAdultModel(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 64, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition head = root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-5, -8, -4, 10, 8, 8)
                        .texOffs(31, 1).addBox(-2, -4, -5, 4, 4, 1)
                        .texOffs(2, 4).addBox(2, -2, -5, 1, 2, 1)
                        .texOffs(2, 0).addBox(-3, -2, -5, 1, 2, 1),
                ModelBakery.PartPose.offset(0, 0, 0));

        head.addOrReplaceChild("left_ear",
                ModelBakery.CubeListBuilder.create().texOffs(51, 6).addBox(0, 0, -2, 1, 5, 4),
                ModelBakery.PartPose.offsetAndRotation(4.5f, -6, 0, 0, 0, -30.0f * Mth.DEG_TO_RAD));
        head.addOrReplaceChild("right_ear",
                ModelBakery.CubeListBuilder.create().texOffs(39, 6).addBox(-1, 0, -2, 1, 5, 4),
                ModelBakery.PartPose.offsetAndRotation(-4.5f, -6, 0, 0, 0, 30.0f * Mth.DEG_TO_RAD));

        ModelBakery.PartDefinition body = root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(16, 16).addBox(-4, 0, -2, 8, 12, 4),
                ModelBakery.PartPose.offset(0, 0, 0));

        body.addOrReplaceChild("jacket",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(16, 32).addBox(-4, 0, -2, 8, 12, 4, new ModelBakery.CubeDeformation(0.25F)),
                ModelBakery.PartPose.ZERO);

        ModelBakery.PartDefinition rightArm = root.addOrReplaceChild("right_arm",
                ModelBakery.CubeListBuilder.create().texOffs(40, 16).addBox(-3, -2, -2, 4, 12, 4),
                ModelBakery.PartPose.offset(-5, 2, 0));
        rightArm.addOrReplaceChild("right_sleeve",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(40, 32).addBox(-3, -2, -2, 4, 12, 4, new ModelBakery.CubeDeformation(0.25F)),
                ModelBakery.PartPose.ZERO);

        ModelBakery.PartDefinition leftArm = root.addOrReplaceChild("left_arm",
                ModelBakery.CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1, -2, -2, 4, 12, 4),
                ModelBakery.PartPose.offset(5, 2, 0));
        leftArm.addOrReplaceChild("left_sleeve",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(48, 48).addBox(-1, -2, -2, 4, 12, 4, new ModelBakery.CubeDeformation(0.25F)),
                ModelBakery.PartPose.ZERO);

        ModelBakery.PartDefinition rightLeg = root.addOrReplaceChild("right_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 16).addBox(-2, 0, -2, 4, 12, 4),
                ModelBakery.PartPose.offset(-1.9f, 12, 0));
        rightLeg.addOrReplaceChild("right_pants",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 32).addBox(-2, 0, -2, 4, 12, 4, new ModelBakery.CubeDeformation(0.25F)),
                ModelBakery.PartPose.ZERO);

        ModelBakery.PartDefinition leftLeg = root.addOrReplaceChild("left_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2, 0, -2, 4, 12, 4),
                ModelBakery.PartPose.offset(1.9f, 12, 0));
        leftLeg.addOrReplaceChild("left_pants",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 48).addBox(-2, 0, -2, 4, 12, 4, new ModelBakery.CubeDeformation(0.25F)),
                ModelBakery.PartPose.ZERO);

        return root.bake();
    }

    private ModelBakery.BakedPart buildBabyModel() {
        ModelBakery bakery = new ModelBakery(PiglinRenderer.BABY_TEXTURE, 64, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(0, 13).addBox(-3, -3, -1, 6, 5, 3),
                ModelBakery.PartPose.offset(0, 18, -0.5f));

        ModelBakery.PartDefinition head = root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(21, 30).addBox(-1.5f, -3, -4.5f, 3, 3, 1)
                        .texOffs(0, 0).addBox(-4.5f, -6, -3.5f, 9, 6, 7),
                ModelBakery.PartPose.offset(0, 15, 0));
        head.addOrReplaceChild("hat", ModelBakery.CubeListBuilder.create(), ModelBakery.PartPose.offset(0, 0, 0));

        ModelBakery.PartDefinition leftEar = head.addOrReplaceChild("left_ear",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(4.2f, -4, 0));
        leftEar.addOrReplaceChild("left_ear_r1",
                ModelBakery.CubeListBuilder.create().texOffs(0, 21).addBox(-0.5f, -3, -2, 1, 6, 4),
                ModelBakery.PartPose.offsetAndRotation(1, 1.75f, 0, 0, 0, -0.6109f));

        ModelBakery.PartDefinition rightEar = head.addOrReplaceChild("right_ear",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(-4.2f, -4, 0));
        rightEar.addOrReplaceChild("right_ear_r1",
                ModelBakery.CubeListBuilder.create().texOffs(18, 13).addBox(-0.5f, -3, -2, 1, 6, 4),
                ModelBakery.PartPose.offsetAndRotation(-1, 1.75f, 0, 0, 0, 0.6109f));

        root.addOrReplaceChild("left_arm",
                ModelBakery.CubeListBuilder.create().texOffs(28, 13).addBox(-1, 0, -1.5f, 2, 5, 3),
                ModelBakery.PartPose.offset(4, 15, 0));
        root.addOrReplaceChild("right_arm",
                ModelBakery.CubeListBuilder.create().texOffs(10, 30).addBox(-1, 0, -1.5f, 2, 5, 3),
                ModelBakery.PartPose.offset(-4, 15, 0));

        root.addOrReplaceChild("right_leg",
                ModelBakery.CubeListBuilder.create().texOffs(22, 23).addBox(-1.5f, 0, -1.5f, 3, 4, 3),
                ModelBakery.PartPose.offset(-1.5f, 20, 0));
        root.addOrReplaceChild("left_leg",
                ModelBakery.CubeListBuilder.create().texOffs(10, 23).addBox(-1.5f, 0, -1.5f, 3, 4, 3),
                ModelBakery.PartPose.offset(1.5f, 20, 0));

        return root.bake();
    }

    @Override
    protected void buildModelParts(ModelBakery.PartDefinition root, ModelBakery bakery) {
    }

    @Override
    protected LimbAngles getLimbAngles(T entity, double animPos, double animSpeed,
                                       double headYaw, double headPitch,
                                       double swim, boolean flying, boolean crouch,
                                       LimbAngles base) {
        PiglinArmPose armPose = entity.getArmPose();
        double attackTime = entity.getAttackAnim(1f);
        boolean mainRight = entity.getMainArm() == HumanoidArm.RIGHT;

        if (armPose == PiglinArmPose.CROSSBOW_HOLD || armPose == PiglinArmPose.CROSSBOW_CHARGE ||
                armPose == PiglinArmPose.ADMIRING_ITEM || armPose == PiglinArmPose.DANCING) {
            base.rightArmAngle = 0;
            base.leftArmAngle = 0;
        }

        if (armPose == PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON) {
            double age = entity.tickCount;
            if (attackTime <= 0.0F) {
                if (mainRight) {
                    base.rightArmAngle = -1.8F;
                    base.leftArmAngle = 0;
                } else {
                    base.leftArmAngle = -1.8F;
                    base.rightArmAngle = 0;
                }
            } else {
                double attack2 = Mth.sin(attackTime * Mth.PI);
                double attack = Mth.sin((1.0F - (1.0F - attackTime) * (1.0F - attackTime)) * Mth.PI);
                double bobR = Mth.cos(age * 0.09F) * 0.05F + 0.05F;
                double bobL = -bobR;

                if (mainRight) {
                    base.rightArmAngle = -1.8849558F + Mth.cos(age * 0.09F) * 0.15F;
                    base.leftArmAngle = -0.0F + Mth.cos(age * 0.19F) * 0.5F;
                    base.rightArmAngle += attack2 * 2.2F - attack * 0.4F;
                    base.leftArmAngle += attack2 * 1.2F - attack * 0.4F;
                } else {
                    base.rightArmAngle = -0.0F + Mth.cos(age * 0.19F) * 0.5F;
                    base.leftArmAngle = -1.8849558F + Mth.cos(age * 0.09F) * 0.15F;
                    base.rightArmAngle += attack2 * 1.2F - attack * 0.4F;
                    base.leftArmAngle += attack2 * 2.2F - attack * 0.4F;
                }

                base.rightArmYaw = Mth.PI / 20;
                base.leftArmYaw = -Mth.PI / 20;
                base.rightArmZ = bobR;
                base.leftArmZ = bobL;
            }
        }

        return super.getLimbAngles(entity, animPos, animSpeed, headYaw, headPitch, swim, flying, crouch, base);
    }

    @Override
    protected void transformPart(Matrix4d mat, String name, T entity, LimbAngles angles,
                                 double swim, boolean fly, boolean crouch) {
        PiglinArmPose armPose = entity.getArmPose();
        double ageInTicks = entity.tickCount + 1f;
        double attackTime = entity.getAttackAnim(1f);
        boolean mainRight = entity.getMainArm() == HumanoidArm.RIGHT;

        if (armPose == PiglinArmPose.DANCING) {
            double dancePos = ageInTicks / 60.0F;
            switch (name) {
                case "head" ->
                        mat.translate(Mth.sin(dancePos * 10.0F) / 16f, (Mth.sin(dancePos * 40.0F) + 0.4F) / 16f, 0);
                case "body" -> mat.translate(0, Mth.sin(dancePos * 40.0F) * 0.35f / 16f, 0);
                case "right_arm" -> {
                    mat.rotateZ((Math.PI / 180.0) * (70.0F + Mth.cos(dancePos * 40.0F) * 10.0F));
                    mat.translate(0, (Mth.sin(dancePos * 40.0F) * 0.5F - 0.5F) / 16f, 0);
                }
                case "left_arm" -> {
                    mat.rotateZ(-((double) (Math.PI / 180.0) * (70.0F + Mth.cos(dancePos * 40.0F) * 10.0F)));
                    mat.translate(0, (Mth.sin(dancePos * 40.0F) * 0.5F + 0.5F) / 16f, 0);
                }
                case "left_ear" -> mat.rotateZ((-Math.PI / 6) - (Math.PI / 180.0) * Mth.cos(dancePos * 30.0F) * 10.0F);
                case "right_ear" -> mat.rotateZ((Math.PI / 6) + (Math.PI / 180.0) * Mth.sin(dancePos * 30.0F) * 10.0F);
            }
            return;
        }

        if (armPose == PiglinArmPose.CROSSBOW_HOLD) {
            switch (name) {
                case "right_arm" -> {
                    if (mainRight) {
                        mat.rotateY(-0.3F + angles.headYaw);
                        mat.rotateX((double) (-Math.PI / 2) + angles.headPitch + 0.1F);
                    } else {
                        mat.rotateY(0.6F + angles.headYaw);
                        mat.rotateX(-1.5F + angles.headPitch);
                    }
                }
                case "left_arm" -> {
                    if (!mainRight) {
                        mat.rotateY(0.3F + angles.headYaw);
                        mat.rotateX((double) (-Math.PI / 2) + angles.headPitch + 0.1F);
                    } else {
                        mat.rotateY(-0.6F + angles.headYaw);
                        mat.rotateX(-1.5F + angles.headPitch);
                    }
                }
            }
            return;
        }

        if (armPose == PiglinArmPose.CROSSBOW_CHARGE) {
            double chargeTime = entity.getTicksUsingItem(1f);
            double maxCharge = CrossbowItem.getChargeDuration(entity.getUseItem(), entity);
            double alpha = maxCharge > 0 ? Mth.clamp(chargeTime / maxCharge, 0.0F, 1.0F) : 0;
            double pullingYRot = Mth.lerp(alpha, 0.4F, 0.85F) * (mainRight ? 1 : -1);

            switch (name) {
                case "right_arm" -> {
                    if (mainRight) {
                        mat.rotateY(-0.8F);
                        mat.rotateX(-0.97079635F);
                    } else {
                        mat.rotateY(pullingYRot);
                        mat.rotateX(Mth.lerp(alpha, -0.97079635F, (-Math.PI / 2)));
                    }
                }
                case "left_arm" -> {
                    if (!mainRight) {
                        mat.rotateY(0.8F);
                        mat.rotateX(-0.97079635F);
                    } else {
                        mat.rotateY(pullingYRot);
                        mat.rotateX(Mth.lerp(alpha, -0.97079635F, (-Math.PI / 2)));
                    }
                }
            }
            return;
        }

        if (armPose == PiglinArmPose.ADMIRING_ITEM) {
            switch (name) {
                case "head" -> {
                    mat.rotateX(0.5F);
                    mat.rotateY(0.0F);
                }
                case "right_arm" -> {
                    if (!mainRight) {
                        mat.rotateY(-0.5F);
                        mat.rotateX(-0.9F);
                    }
                }
                case "left_arm" -> {
                    if (mainRight) {
                        mat.rotateY(0.5F);
                        mat.rotateX(-0.9F);
                    }
                }
            }
            return;
        }

        if (name.equals("left_ear")) {
            double frequency = ageInTicks * 0.1f + entity.walkAnimation.position() * 0.5f;
            double amplitude = 0.08f + entity.walkAnimation.speed() * 0.4f;
            mat.rotateZ(-Mth.cos(frequency * 1.2f) * amplitude);
        } else if (name.equals("right_ear")) {
            double frequency = ageInTicks * 0.1f + entity.walkAnimation.position() * 0.5f;
            double amplitude = 0.08f + entity.walkAnimation.speed() * 0.4f;
            mat.rotateZ(Mth.cos(frequency) * amplitude);
        }
    }
}
