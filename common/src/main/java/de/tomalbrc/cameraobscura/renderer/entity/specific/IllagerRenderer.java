package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public abstract class IllagerRenderer<T extends AbstractIllager> implements LivingEntityRenderer<T> {

    private ModelBakery.BakedPart cachedModel;

    protected abstract String getTexture(T entity);

    protected boolean showHat() {
        return false;
    }

    protected Matrix4d computeBaseMatrix(T entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        return new Matrix4d()
                .translate(pos.x, pos.y + 1.5f, pos.z)
                .rotateY(Mth.DEG_TO_RAD * (180f - bodyYaw))
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);
    }

    @Override
    public ModelBakery.BakedPart buildRoot(T entity) {
        if (cachedModel == null) {
            cachedModel = buildModel(getTexture(entity));
        }
        return cachedModel;
    }

    private ModelBakery.BakedPart buildModel(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 64, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition head = root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-4, -10, -4, 8, 10, 8),
                ModelBakery.PartPose.offset(0, 0, 0));
        head.addOrReplaceChild("hat",
                ModelBakery.CubeListBuilder.create().texOffs(32, 0).addBox(-4, -10, -4, 8, 12, 8, new ModelBakery.CubeDeformation(0.45f)),
                ModelBakery.PartPose.ZERO);
        head.addOrReplaceChild("nose",
                ModelBakery.CubeListBuilder.create().texOffs(24, 0).addBox(-1, -1, -6, 2, 4, 2),
                ModelBakery.PartPose.offset(0, -2, 0));

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(16, 20).addBox(-4, 0, -3, 8, 12, 6)
                        .texOffs(0, 38).addBox(-4, 0, -3, 8, 20, 6, new ModelBakery.CubeDeformation(0.5f)),
                ModelBakery.PartPose.offset(0, 0, 0));

        root.addOrReplaceChild("arms",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(44, 22).addBox(-8, -2, -2, 4, 8, 4)
                        .texOffs(40, 38).addBox(-4, 2, -2, 8, 4, 4)
                        .texOffs(44, 22).mirror().addBox(4, -2, -2, 4, 8, 4),
                ModelBakery.PartPose.offsetAndRotation(0, 3, -1, -0.75f, 0, 0));

        root.addOrReplaceChild("right_arm",
                ModelBakery.CubeListBuilder.create().texOffs(40, 46).addBox(-3, -2, -2, 4, 12, 4),
                ModelBakery.PartPose.offset(-5, 2, 0));
        root.addOrReplaceChild("left_arm",
                ModelBakery.CubeListBuilder.create().texOffs(40, 46).mirror().addBox(-1, -2, -2, 4, 12, 4),
                ModelBakery.PartPose.offset(5, 2, 0));

        root.addOrReplaceChild("right_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 22).addBox(-2, 0, -2, 4, 12, 4),
                ModelBakery.PartPose.offset(-2, 12, 0));
        root.addOrReplaceChild("left_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 22).mirror().addBox(-2, 0, -2, 4, 12, 4),
                ModelBakery.PartPose.offset(2, 12, 0));

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, T entity) {
        renderSingle(pipeline, entity, computeBaseMatrix(entity));
    }

    protected void renderSingle(RenderPipeline pipeline, T entity, Matrix4d base) {
        double headYawRel = Mth.DEG_TO_RAD * (entity.getYHeadRot() - entity.getPreciseBodyRotation(1.0f));
        double headPitch = Mth.DEG_TO_RAD * entity.getXRot(1.0f);
        boolean isRiding = entity.isPassenger();
        double animPos = entity.walkAnimation.position();
        double animSpeed = entity.walkAnimation.speed();
        double attackAnim = entity.getAttackAnim(1.0f);
        double ageInTicks = entity.tickCount + 1.0f;
        AbstractIllager.IllagerArmPose armPose = entity.getArmPose();
        boolean isAggressive = entity.isAggressive();
        int maxCrossbowChargeDuration = (armPose == AbstractIllager.IllagerArmPose.CROSSBOW_CHARGE)
                ? net.minecraft.world.item.CrossbowItem.getChargeDuration(entity.getUseItem(), entity) : 0;
        double ticksUsingItem = entity.getTicksUsingItem(1.0f);

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderIllagerPart(pipeline, buildRoot(entity), "root", base,
                headYawRel, headPitch, isRiding, animPos, animSpeed,
                attackAnim, ageInTicks, armPose, isAggressive,
                maxCrossbowChargeDuration, ticksUsingItem,
                block, sky);
    }

    private void renderIllagerPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                                   Matrix4d parent,
                                   double headYaw, double headPitch, boolean isRiding,
                                   double animPos, double animSpeed,
                                   double attackAnim, double ageInTicks,
                                   AbstractIllager.IllagerArmPose armPose, boolean isAggressive,
                                   int maxCrossbowChargeDuration, double ticksUsingItem,
                                   double block, double sky) {

        if (name.equals("hat") && !showHat()) return;
        boolean crossedArms = armPose == AbstractIllager.IllagerArmPose.CROSSED;
        if (name.equals("arms") && !crossedArms) return;
        if ((name.equals("left_arm") || name.equals("right_arm")) && crossedArms) return;

        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        var ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        switch (name) {
            case "head":
                mat.rotateY(headYaw);
                mat.rotateX(headPitch);
                break;
            case "right_leg":
            case "left_leg":
                if (isRiding) {
                    double xRot = -1.4137167F;
                    double yRot = name.equals("right_leg") ? (Math.PI / 10) : (-Math.PI / 10);
                    double zRot = name.equals("right_leg") ? 0.07853982F : -0.07853982F;
                    mat.rotateX(xRot);
                    mat.rotateY(yRot);
                    mat.rotateZ(zRot);
                } else {
                    double legXRot = Mth.cos(animPos * 0.6662F + (name.equals("left_leg") ? Mth.PI : 0)) * 1.4F * animSpeed * 0.5F;
                    mat.rotateX(legXRot);
                }
                break;
            case "right_arm":
            case "left_arm":
                if (isRiding) {
                    mat.rotateX(-Math.PI / 5);
                } else {
                    double armXRot = Mth.cos(animPos * 0.6662F + (name.equals("right_arm") ? Mth.PI : 0)) * 2.0F * animSpeed * 0.5F;
                    mat.rotateX(armXRot);
                }
                applyIllagerArmPose(mat, name, armPose, isAggressive, attackAnim, ageInTicks, headYaw, headPitch, maxCrossbowChargeDuration, ticksUsingItem);
                break;
            default:
                break;
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }
        for (var child : part.children.entrySet()) {
            renderIllagerPart(
                    pipeline, child.getValue(), child.getKey(), mat,
                    headYaw, headPitch, isRiding, animPos, animSpeed,
                    attackAnim, ageInTicks, armPose, isAggressive,
                    maxCrossbowChargeDuration, ticksUsingItem,
                    block, sky
            );
        }
    }

    private void applyIllagerArmPose(Matrix4d mat, String armName,
                                     AbstractIllager.IllagerArmPose pose, boolean isAggressive,
                                     double attackAnim, double ageInTicks,
                                     double headYaw, double headPitch,
                                     int maxCrossbowChargeDuration, double ticksUsingItem) {
        boolean isRight = armName.equals("right_arm");
        switch (pose) {
            case ATTACKING:
                if (/* state.getMainHandItemState().isEmpty() */true) {
                    double attackTime = attackAnim;
                    double armDrop = -Math.PI / 1.5f;
                    double attackY = Mth.sin(attackTime * Mth.PI) * 1.2f - attackTime * attackTime * 0.4f;
                    double yRot = -(0.1f - attackTime * 0.6f) * (isRight ? 1 : -1);
                    double zRot = (isRight ? 1 : -1) * (Mth.cos(ageInTicks * 0.09f) * 0.05f + 0.05f);
                    mat.rotateX(armDrop + attackY);
                    mat.rotateY(yRot);
                    mat.rotateZ(zRot);
                } else {
                    double swing = Mth.sin(attackAnim * Mth.PI);
                    mat.rotateX(-1.2f + swing * 0.5f);
                }
                break;
            case SPELLCASTING:
                mat.translate(0, 0, 0);
                mat.rotateX(Mth.cos(ageInTicks * 0.6662f) * 0.25f);
                mat.rotateZ(isRight ? (Math.PI * 3.0 / 4.0) : (-Math.PI * 3.0 / 4.0));
                break;
            case BOW_AND_ARROW:
                if (isRight) {
                    mat.rotateY(-0.1f + headYaw);
                    mat.rotateX((-Math.PI / 2) + headPitch);
                } else {
                    mat.rotateX(-0.9424779f + headPitch);
                    mat.rotateY(headYaw - 0.4f);
                    mat.rotateZ(Math.PI / 2);
                }
                break;
            case CROSSBOW_HOLD:
                if (isRight) {
                    mat.rotateX(-1.0f);
                    mat.rotateY(0.5f);
                } else {
                    mat.rotateX(-1.0f);
                    mat.rotateY(-0.5f);
                }
                break;
            case CROSSBOW_CHARGE:
                // AnimationUtils.animateCrossbowCharge
                double chargeProgress = ticksUsingItem / (double) maxCrossbowChargeDuration;
                double xRot = -0.9f + chargeProgress * 0.5f;
                double yRot = isRight ? 0.2f : -0.2f;
                mat.rotateX(xRot);
                mat.rotateY(yRot);
                break;
            case CELEBRATING:
                mat.translate(isRight ? -5 : 5, 0, 0);
                mat.rotateX(Mth.cos(ageInTicks * 0.6662f) * 0.05f);
                mat.rotateZ(isRight ? 2.670354f : (-Math.PI * 3.0 / 4.0));
                break;
            default:
                break;
        }
    }
}