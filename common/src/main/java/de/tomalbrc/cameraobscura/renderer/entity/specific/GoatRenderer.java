package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.FourLeggedRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public class GoatRenderer extends FourLeggedRenderer<Goat> {

    private static final String ADULT_TEXTURE = "entity/goat/goat";
    private static final String BABY_TEXTURE = "entity/goat/goat_baby";

    private ModelBakery.BakedPart cachedAdultModel;
    private ModelBakery.BakedPart cachedBabyModel;

    @Override
    public ModelBakery.BakedPart buildRoot(Goat entity) {
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
        ModelBakery bakery = new ModelBakery(texture, 64, 64);
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
        ModelBakery.PartDefinition head = root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(2, 61).addBox("right_ear", -6, -11, -10, 3, 2, 1)
                        .texOffs(2, 61).mirror().addBox("left_ear", 2, -11, -10, 3, 2, 1)
                        .texOffs(23, 52).addBox("goatee", -0.5f, -3, -14, 0, 7, 5),
                ModelBakery.PartPose.offset(1, 14, 0));
        head.addOrReplaceChild("left_horn",
                ModelBakery.CubeListBuilder.create().texOffs(12, 55).addBox(-0.01f, -16, -10, 2, 7, 2),
                ModelBakery.PartPose.offset(0, 0, 0));
        head.addOrReplaceChild("right_horn",
                ModelBakery.CubeListBuilder.create().texOffs(12, 55).addBox(-2.99f, -16, -10, 2, 7, 2),
                ModelBakery.PartPose.offset(0, 0, 0));
        head.addOrReplaceChild("nose",
                ModelBakery.CubeListBuilder.create().texOffs(34, 46).addBox(-3, -4, -8, 5, 7, 10),
                ModelBakery.PartPose.offsetAndRotation(0, -8, -8, 0.9599f, 0, 0));

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(1, 1).addBox(-4, -17, -7, 9, 11, 16)
                        .texOffs(0, 28).addBox(-5, -18, -8, 11, 14, 11),
                ModelBakery.PartPose.offset(0, 24, 0));

        root.addOrReplaceChild("left_hind_leg",
                ModelBakery.CubeListBuilder.create().texOffs(36, 29).addBox(0, 4, 0, 3, 6, 3),
                ModelBakery.PartPose.offset(1, 14, 4));
        root.addOrReplaceChild("right_hind_leg",
                ModelBakery.CubeListBuilder.create().texOffs(49, 29).addBox(0, 4, 0, 3, 6, 3),
                ModelBakery.PartPose.offset(-3, 14, 4));
        root.addOrReplaceChild("left_front_leg",
                ModelBakery.CubeListBuilder.create().texOffs(49, 2).addBox(0, 0, 0, 3, 10, 3),
                ModelBakery.PartPose.offset(1, 14, -6));
        root.addOrReplaceChild("right_front_leg",
                ModelBakery.CubeListBuilder.create().texOffs(35, 2).addBox(0, 0, 0, 3, 10, 3),
                ModelBakery.PartPose.offset(-3, 14, -6));
    }

    private void buildBabyParts(ModelBakery.PartDefinition root) {
        root.addOrReplaceChild("left_hind_leg",
                ModelBakery.CubeListBuilder.create().texOffs(29, 12).addBox(-1, -0.5f, -1, 2, 5, 2),
                ModelBakery.PartPose.offset(1.5f, 19.5f, 3));
        root.addOrReplaceChild("right_hind_leg",
                ModelBakery.CubeListBuilder.create().texOffs(21, 12).addBox(-1, -0.5f, -1, 2, 5, 2),
                ModelBakery.PartPose.offset(-1.5f, 19.5f, 3));
        root.addOrReplaceChild("right_front_leg",
                ModelBakery.CubeListBuilder.create().texOffs(21, 5).addBox(-1, -0.5f, -1, 2, 5, 2),
                ModelBakery.PartPose.offset(-1.5f, 19.5f, -2));
        root.addOrReplaceChild("left_front_leg",
                ModelBakery.CubeListBuilder.create().texOffs(29, 5).addBox(-1, -0.5f, -1, 2, 5, 2),
                ModelBakery.PartPose.offset(1.5f, 19.5f, -2));
        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 10).addBox(-3, -2.3f, -4.5f, 6, 5, 9)
                        .texOffs(0, 24).addBox(-2.5f, -2.2f, -4, 5, 4, 8),
                ModelBakery.PartPose.offset(0, 17.8f, 0));
        ModelBakery.PartDefinition head = root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-2, -3.8126f, -5.1548f, 4, 4, 6),
                ModelBakery.PartPose.offsetAndRotation(0, 15.5f, -3, 0.4363f, 0, 0));
        head.addOrReplaceChild("right_horn",
                ModelBakery.CubeListBuilder.create().texOffs(24, 0).mirror().addBox(0, -4.5f, 0, 1, 2, 1).mirror(false),
                ModelBakery.PartPose.offsetAndRotation(-1.5f, -1.5f, -1, -Mth.PI / 8, 0, 0));
        head.addOrReplaceChild("left_horn",
                ModelBakery.CubeListBuilder.create().texOffs(24, 0).addBox(2, -4.5f, 0, 1, 2, 1),
                ModelBakery.PartPose.offsetAndRotation(-1.5f, -1.5f, -1, -Mth.PI / 8, 0, 0));
        head.addOrReplaceChild("right_ear",
                ModelBakery.CubeListBuilder.create().texOffs(0, 12).mirror().addBox(-2, -0.5f, -0.5f, 2, 1, 1).mirror(false),
                ModelBakery.PartPose.offsetAndRotation(-1.7f, -2.3126f, 0.1452f, 0, -0.5236f, 0));
        head.addOrReplaceChild("left_ear",
                ModelBakery.CubeListBuilder.create().texOffs(0, 12).addBox(0, -0.5f, -0.5f, 2, 1, 1),
                ModelBakery.PartPose.offsetAndRotation(1.7f, -2.3126f, 0.1452f, 0, 0.5236f, 0));
        head.addOrReplaceChild("HeadMain",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-2, -2.5f, -4, 4, 4, 6),
                ModelBakery.PartPose.offset(0, -1.3126f, -1.1548f));
    }

    @Override
    public void render(RenderPipeline pipeline, Goat entity) {
        ModelBakery.BakedPart root = buildRoot(entity);
        var pos = entity.position();
        double bodyYawDeg = entity.getPreciseBodyRotation(1.0f);
        double modelYawRad = Mth.DEG_TO_RAD * (180f - bodyYawDeg);
        double headYawRad = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYawDeg);
        double headPitchRad = Mth.DEG_TO_RAD * entity.getXRot(1.0f);
        double rammingXHeadRot = entity.getRammingXHeadRot();
        boolean hasLeft = entity.hasLeftHorn();
        boolean hasRight = entity.hasRightHorn();

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

        renderGoatPart(pipeline, root, "root", base, limbAngle,
                headYawRad, (rammingXHeadRot != 0 ? Mth.DEG_TO_RAD * rammingXHeadRot : headPitchRad),
                hasLeft, hasRight,
                block, sky);
    }

    private void renderGoatPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                                Matrix4d parent, double limbAngle, double headYawRad, double headPitchRad,
                                boolean hasLeftHorn, boolean hasRightHorn,
                                double block, double sky) {

        if (name.equals("left_horn") && !hasLeftHorn) return;
        if (name.equals("right_horn") && !hasRightHorn) return;

        Matrix4d mat = new Matrix4d(parent);
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
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }

        for (var childEntry : part.children.entrySet()) {
            renderGoatPart(
                    pipeline, childEntry.getValue(), childEntry.getKey(), mat,
                    limbAngle, headYawRad, headPitchRad, hasLeftHorn, hasRightHorn,
                    block, sky
            );
        }
    }
}