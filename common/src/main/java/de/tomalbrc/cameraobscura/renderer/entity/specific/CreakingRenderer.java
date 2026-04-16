package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public class CreakingRenderer implements LivingEntityRenderer<Creaking> {
    private static final String MAIN_TEXTURE = "entity/creaking/creaking";
    private static final String EYES_TEXTURE = "entity/creaking/creaking_eyes";

    private ModelBakery.BakedPart cachedMainModel;
    private ModelBakery.BakedPart cachedEyesModel;

    @Override
    public ModelBakery.BakedPart buildRoot(Creaking entity) {
        if (cachedMainModel == null) {
            cachedMainModel = buildFullModel(MAIN_TEXTURE);
        }
        return cachedMainModel;
    }

    private ModelBakery.BakedPart getEyesModel() {
        if (cachedEyesModel == null) {
            cachedEyesModel = buildFullModel(EYES_TEXTURE);
        }
        return cachedEyesModel;
    }

    private ModelBakery.BakedPart buildFullModel(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 64, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition rootPart = model.root();

        ModelBakery.PartDefinition root = rootPart.addOrReplaceChild("root",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(0, 24, 0));

        ModelBakery.PartDefinition upperBody = root.addOrReplaceChild("upper_body",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(-1, -19, 0));

        upperBody.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3, -10, -3, 6, 10, 6)
                        .texOffs(28, 31).addBox(-3, -13, -3, 6, 3, 6)
                        .texOffs(12, 40).addBox(3, -13, 0, 9, 14, 0)
                        .texOffs(34, 12).addBox(-12, -14, 0, 9, 14, 0),
                ModelBakery.PartPose.offset(-3, -11, 0));

        upperBody.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 16).addBox(0, -3, -3, 6, 13, 5)
                        .texOffs(24, 0).addBox(-6, -4, -3, 6, 7, 5),
                ModelBakery.PartPose.offset(0, -7, 1));

        upperBody.addOrReplaceChild("right_arm",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(22, 13).addBox(-2, -1.5f, -1.5f, 3, 21, 3)
                        .texOffs(46, 0).addBox(-2, 19.5f, -1.5f, 3, 4, 3),
                ModelBakery.PartPose.offset(-7, -9.5f, 1.5f));

        upperBody.addOrReplaceChild("left_arm",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(30, 40).addBox(0, -1, -1.5f, 3, 16, 3)
                        .texOffs(52, 12).addBox(0, -5, -1.5f, 3, 4, 3)
                        .texOffs(52, 19).addBox(0, 15, -1.5f, 3, 4, 3),
                ModelBakery.PartPose.offset(6, -9, 0.5f));

        root.addOrReplaceChild("left_leg",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(42, 40).addBox(-1.5f, 0, -1.5f, 3, 16, 3)
                        .texOffs(45, 55).addBox(-1.5f, 15.7f, -4.5f, 5, 0, 9),
                ModelBakery.PartPose.offset(1.5f, -16, 0.5f));

        root.addOrReplaceChild("right_leg",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 34).addBox(-3, -1.5f, -1.5f, 3, 19, 3)
                        .texOffs(45, 46).addBox(-5, 17.2f, -4.5f, 5, 0, 9)
                        .texOffs(12, 34).addBox(-3, -4.5f, -1.5f, 3, 3, 3),
                ModelBakery.PartPose.offset(-1, -17.5f, 0.5f));

        return rootPart.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, Creaking entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyYaw);
        double headYaw = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYaw);
        double headPitch = Mth.DEG_TO_RAD * entity.getXRot(1.0f);
        double walkPos = entity.walkAnimation.position();
        double walkSpeed = entity.walkAnimation.speed();
        boolean canMove = entity.canMove();
        boolean eyesGlowing = entity.isActive();

        double legRight = canMove ? Mth.cos(walkPos * 0.6662f) * 1.4f * walkSpeed : 0;
        double legLeft = canMove ? Mth.cos(walkPos * 0.6662f + Mth.PI) * 1.4f * walkSpeed : 0;
        double armRight = canMove ? Mth.cos(walkPos * 0.6662f + Mth.PI) * 0.8f * walkSpeed : 0;
        double armLeft = canMove ? Mth.cos(walkPos * 0.6662f) * 0.8f * walkSpeed : 0;

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + 1.5f, pos.z)
                .rotateY(modelYaw)
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        AnimParams params = new AnimParams(headYaw, headPitch, legRight, legLeft, armRight, armLeft);

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderCreakingPart(pipeline, buildRoot(entity), "root", base, params, 0xFFFFFFFF, block, sky);

        if (eyesGlowing && cachedEyesModel == null) {
            cachedEyesModel = buildFullModel(EYES_TEXTURE);
        }

        if (eyesGlowing) {
            renderCreakingPart(pipeline, cachedEyesModel, "root", base, params, 0xFFFFFFFF, 1, 1);
        }
    }

    private void renderCreakingPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                                    Matrix4d parent, AnimParams p, int color,
                                    double block, double sky) {

        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        switch (name) {
            case "head":
                mat.rotateY(p.headYaw);
                mat.rotateX(p.headPitch);
                break;
            case "right_leg":
                mat.rotateX(p.legRight);
                break;
            case "left_leg":
                mat.rotateX(p.legLeft);
                break;
            case "right_arm":
                mat.rotateX(p.armRight);
                break;
            case "left_arm":
                mat.rotateX(p.armLeft);
                break;
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(color)));
        }

        for (var child : part.children.entrySet()) {
            renderCreakingPart(pipeline, child.getValue(), child.getKey(), mat, p, color, block, sky);
        }
    }

    private static class AnimParams {
        final double headYaw, headPitch;
        final double legRight, legLeft;
        final double armRight, armLeft;

        AnimParams(double headYaw, double headPitch, double legRight, double legLeft, double armRight, double armLeft) {
            this.headYaw = headYaw;
            this.headPitch = headPitch;
            this.legRight = legRight;
            this.legLeft = legLeft;
            this.armRight = armRight;
            this.armLeft = armLeft;
        }
    }
}