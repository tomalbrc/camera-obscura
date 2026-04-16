package de.tomalbrc.cameraobscura.renderer.entity;

import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public abstract class FourLeggedRenderer<T extends LivingEntity> implements LivingEntityRenderer<T> {

    public static ModelBakery.ModelDefinition createDefaultBody(final int legSize, final boolean mirrorLeftLeg, final boolean mirrorRightLeg, final ModelBakery.CubeDeformation g, String texture, int w, int h) {
        ModelBakery bakery = new ModelBakery(texture, w, h);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);

        ModelBakery.PartDefinition root = model.root();
        root.addOrReplaceChild(
                "head", ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox("cube", -4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F, g), ModelBakery.PartPose.offset(0.0F, 18 - legSize, -6.0F)
        );
        root.addOrReplaceChild(
                "body",
                ModelBakery.CubeListBuilder.create().texOffs(28, 8).addBox("cube", -5.0F, -10.0F, -7.0F, 10.0F, 16.0F, 8.0F, g),
                ModelBakery.PartPose.offsetAndRotation(0.0F, 17 - legSize, 2.0F, (Mth.PI / 2), 0.0F, 0.0F)
        );
        createDefaultLegs(root, mirrorLeftLeg, mirrorRightLeg, legSize, g);

        return model;
    }

    static void createDefaultLegs(final ModelBakery.PartDefinition root, final boolean mirrorLeftLeg, final boolean mirrorRightLeg, final int legSize, final ModelBakery.CubeDeformation g) {
        ModelBakery.CubeListBuilder rightLeg = ModelBakery.CubeListBuilder.create().mirror(mirrorRightLeg).texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, legSize, 4.0F, g);
        ModelBakery.CubeListBuilder leftLeg = ModelBakery.CubeListBuilder.create().mirror(mirrorLeftLeg).texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, legSize, 4.0F, g);
        root.addOrReplaceChild("right_hind_leg", rightLeg, ModelBakery.PartPose.offset(-3.0F, 24 - legSize, 7.0F));
        root.addOrReplaceChild("left_hind_leg", leftLeg, ModelBakery.PartPose.offset(3.0F, 24 - legSize, 7.0F));
        root.addOrReplaceChild("right_front_leg", rightLeg, ModelBakery.PartPose.offset(-3.0F, 24 - legSize, -5.0F));
        root.addOrReplaceChild("left_front_leg", leftLeg, ModelBakery.PartPose.offset(3.0F, 24 - legSize, -5.0F));
    }

    @Override
    public void render(RenderPipeline pipeline, T entity) {
        ModelBakery.BakedPart root = buildRoot(entity);

        var pos = entity.position();
        double bodyYawDeg = entity.getPreciseBodyRotation(1.f);
        double modelYawRad = Mth.DEG_TO_RAD * (180f - bodyYawDeg);

        double headYawRelDeg = entity.getYHeadRot() - bodyYawDeg;
        double headYawRad = Mth.DEG_TO_RAD * headYawRelDeg;
        double headPitchRad = Mth.DEG_TO_RAD * entity.getXRot(1.f);

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + 1.5f, pos.z)
                .rotateY(Mth.PI + modelYawRad)
                .rotateX(Mth.PI);

        double limbSwing = entity.walkAnimation.position() * 0.6662f;
        double limbSpeed = entity.walkAnimation.speed();
        double limbAngle = Mth.cos(limbSwing) * 1.4f * limbSpeed;

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        int tint = LivingEntityRenderer.hurtTint(entity);
        var tints = IntList.of(tint);

        renderPart(pipeline, root, "root", base, limbAngle, headYawRad, headPitchRad, tints, block, sky);
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name, Matrix4d parentMat, double limbAngle, double headYawRad, double headPitchRad, IntList tints, double block, double sky) {
        Matrix4d mat = new Matrix4d(parentMat);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose pose = part.initialPose;
        if (pose.xRot() != 0 || pose.yRot() != 0 || pose.zRot() != 0)
            mat.rotateZYX(pose.zRot(), pose.yRot(), pose.xRot());
        if (pose.xScale() != 1 || pose.yScale() != 1 || pose.zScale() != 1)
            mat.scale(pose.xScale(), pose.yScale(), pose.zScale());

        switch (name) {
            case "head" -> {
                mat.rotateY(headYawRad);
                mat.rotateX(headPitchRad);
            }
            case "right_hind_leg", "left_front_leg" -> mat.rotateX(limbAngle);
            case "left_hind_leg", "right_front_leg" -> mat.rotateX(-limbAngle);
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, tints));
        }

        for (var childEntry : part.children.entrySet()) {
            renderPart(pipeline, childEntry.getValue(), childEntry.getKey(), mat, limbAngle, headYawRad, headPitchRad, tints, block, sky);
        }
    }
}