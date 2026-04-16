package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.FourLeggedRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.polarbear.PolarBear;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public class PolarBearRenderer extends FourLeggedRenderer<PolarBear> {
    private static final String ADULT_TEXTURE = "entity/bear/polarbear";
    private static final String BABY_TEXTURE = "entity/bear/polarbear_baby";

    private ModelBakery.BakedPart cachedAdultModel;
    private ModelBakery.BakedPart cachedBabyModel;

    @Override
    public ModelBakery.BakedPart buildRoot(PolarBear entity) {
        if (entity.isBaby()) {
            if (cachedBabyModel == null) {
                cachedBabyModel = buildModel(BABY_TEXTURE, true);
            }
            return cachedBabyModel;
        } else {
            if (cachedAdultModel == null) {
                cachedAdultModel = buildModel(ADULT_TEXTURE, false);
            }
            return cachedAdultModel;
        }
    }

    private ModelBakery.BakedPart buildModel(String texture, boolean baby) {
        ModelBakery bakery = new ModelBakery(texture, baby ? 64 : 128, baby ? 64 : 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        if (baby) {
            buildBabyParts(root);
        } else {
            buildAdultParts(root);
        }
        return root.bake();
    }

    private void buildAdultParts(ModelBakery.PartDefinition root) {
        root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.5f, -3.0f, -3.0f, 7.0f, 7.0f, 7.0f)
                        .texOffs(0, 44).addBox(-2.5f, 1.0f, -6.0f, 5.0f, 3.0f, 3.0f)
                        .texOffs(26, 0).addBox(-4.5f, -4.0f, -1.0f, 2.0f, 2.0f, 1.0f)
                        .texOffs(26, 0).mirror().addBox(2.5f, -4.0f, -1.0f, 2.0f, 2.0f, 1.0f),
                ModelBakery.PartPose.offset(0.0f, 10.0f, -16.0f));

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 19).addBox(-5.0f, -13.0f, -7.0f, 14.0f, 14.0f, 11.0f)
                        .texOffs(39, 0).addBox(-4.0f, -25.0f, -7.0f, 12.0f, 12.0f, 10.0f),
                ModelBakery.PartPose.offsetAndRotation(-2.0f, 9.0f, 12.0f, Mth.PI / 2, 0.0f, 0.0f));

        ModelBakery.CubeListBuilder hindLeg = ModelBakery.CubeListBuilder.create()
                .texOffs(50, 22).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 10.0f, 8.0f);
        root.addOrReplaceChild("right_hind_leg", hindLeg, ModelBakery.PartPose.offset(-4.5f, 14.0f, 6.0f));
        root.addOrReplaceChild("left_hind_leg", hindLeg, ModelBakery.PartPose.offset(4.5f, 14.0f, 6.0f));
        ModelBakery.CubeListBuilder frontLeg = ModelBakery.CubeListBuilder.create()
                .texOffs(50, 40).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 10.0f, 6.0f);
        root.addOrReplaceChild("right_front_leg", frontLeg, ModelBakery.PartPose.offset(-3.5f, 14.0f, -8.0f));
        root.addOrReplaceChild("left_front_leg", frontLeg, ModelBakery.PartPose.offset(3.5f, 14.0f, -8.0f));
    }

    private void buildBabyParts(ModelBakery.PartDefinition root) {
        root.addOrReplaceChild("body", ModelBakery.CubeListBuilder.create()
                .texOffs(0, 9).addBox(-4.0f, -3.5f, -6.0f, 8.0f, 7.0f, 12.0f), ModelBakery.PartPose.offset(0.0f, 17.5f, 0.0f));
        root.addOrReplaceChild("head", ModelBakery.CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.0f, -2.625f, -4.25f, 6.0f, 5.0f, 4.0f)
                .texOffs(20, 3).addBox(-2.0f, 0.375f, -6.25f, 4.0f, 2.0f, 2.0f)
                .texOffs(20, 0).addBox(-4.0f, -3.625f, -2.75f, 2.0f, 2.0f, 1.0f)
                .texOffs(26, 0).addBox(2.0f, -3.625f, -2.75f, 2.0f, 2.0f, 1.0f), ModelBakery.PartPose.offset(0.0f, 18.625f, -5.75f));
        ModelBakery.CubeListBuilder leg = ModelBakery.CubeListBuilder.create()
                .texOffs(0, 34).addBox(-1.5f, -0.5f, -1.5f, 3.0f, 3.0f, 3.0f);
        root.addOrReplaceChild("right_hind_leg", leg, ModelBakery.PartPose.offset(-2.5f, 21.5f, 4.5f));
        root.addOrReplaceChild("left_hind_leg", leg, ModelBakery.PartPose.offset(2.5f, 21.5f, 4.5f));
        root.addOrReplaceChild("right_front_leg", leg, ModelBakery.PartPose.offset(-2.5f, 21.5f, -4.5f));
        root.addOrReplaceChild("left_front_leg", leg, ModelBakery.PartPose.offset(2.5f, 21.5f, -4.5f));
    }

    @Override
    public void render(RenderPipeline pipeline, PolarBear entity) {
        ModelBakery.BakedPart root = buildRoot(entity);
        var pos = entity.position();
        double bodyYawDeg = entity.getPreciseBodyRotation(1.0f);
        double modelYawRad = Mth.DEG_TO_RAD * (180f - bodyYawDeg);
        double headYawRad = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYawDeg);
        double headPitchRad = Mth.DEG_TO_RAD * entity.getXRot(1.0f);
        boolean baby = entity.isBaby();
        double ageScale = baby ? 0.5f : 1.0f;
        double standScale = entity.getStandingAnimationScale(1.0f) * entity.getStandingAnimationScale(1.0f);

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y, pos.z)
                .rotateY(modelYawRad);

        base.translate(0.0f, 1.5f, 0.0f);
        base.rotateY(Mth.PI);
        base.rotateX(Mth.PI);

        double limbSwing = entity.walkAnimation.position() * 0.6662f;
        double limbSpeed = entity.walkAnimation.speed();
        double limbAngle = Mth.cos(limbSwing) * 1.4f * limbSpeed;

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        int tint = LivingEntityRenderer.hurtTint(entity);
        var tints = IntList.of(tint);

        renderWithStanding(pipeline, root, "root", base, limbAngle, headYawRad, headPitchRad, standScale, ageScale, tints, block, sky);
    }

    private void renderWithStanding(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                                    Matrix4d parentMat, double limbAngle, double headYawRad, double headPitchRad,
                                    double standScale, double ageScale,
                                    IntList tints, double block, double sky) {
        Matrix4d mat = new Matrix4d(parentMat);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose pose = part.initialPose;
        if (pose.xRot() != 0 || pose.yRot() != 0 || pose.zRot() != 0)
            mat.rotateZYX(pose.zRot(), pose.yRot(), pose.xRot());
        if (pose.xScale() != 1 || pose.yScale() != 1 || pose.zScale() != 1)
            mat.scale(pose.xScale(), pose.yScale(), pose.zScale());

        switch (name) {
            case "head" -> {
                mat.translate(0.0f, -24.0f * standScale / 16.0f, 13.0f * standScale / 16.0f);
                mat.rotateX(headPitchRad + standScale * (double) (Math.PI * 0.15));
                mat.rotateY(headYawRad);
            }
            case "body" -> {
                mat.rotateX(-standScale * (double) Math.PI * 0.35f);
                mat.translate(0.0f, standScale * ageScale * 2.0f / 16.0f, 0.0f);
            }
            case "right_front_leg", "left_front_leg" -> {
                mat.translate(0.0f, -20.0f * standScale * ageScale / 16.0f, 4.0f * standScale * ageScale / 16.0f);
                mat.rotateX(limbAngle - standScale * (double) Math.PI * 0.45f);
            }
            case "right_hind_leg", "left_hind_leg" -> {
                mat.rotateX(limbAngle);
            }
            default -> {
                if (name.contains("leg")) {
                    mat.rotateX(-limbAngle);
                }
            }
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, tints));
        }

        for (var childEntry : part.children.entrySet()) {
            renderWithStanding(
                    pipeline, childEntry.getValue(), childEntry.getKey(), mat,
                    limbAngle, headYawRad, headPitchRad, standScale, ageScale,
                    tints, block, sky
            );
        }
    }
}