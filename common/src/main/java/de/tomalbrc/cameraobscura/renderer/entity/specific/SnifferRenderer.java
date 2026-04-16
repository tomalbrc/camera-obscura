package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public class SnifferRenderer implements LivingEntityRenderer<Sniffer> {

    private static final String ADULT_TEXTURE = "entity/sniffer/sniffer";
    private static final String BABY_TEXTURE = "entity/sniffer/snifflet";

    private ModelBakery.BakedPart cachedAdultModel;
    private ModelBakery.BakedPart cachedBabyModel;

    @Override
    public ModelBakery.BakedPart buildRoot(Sniffer entity) {
        if (entity.isBaby()) {
            if (cachedBabyModel == null) cachedBabyModel = buildBabyModel();
            return cachedBabyModel;
        } else {
            if (cachedAdultModel == null) cachedAdultModel = buildAdultModel();
            return cachedAdultModel;
        }
    }

    private ModelBakery.BakedPart buildAdultModel() {
        ModelBakery bakery = new ModelBakery(ADULT_TEXTURE, 192, 192);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition bone = root.addOrReplaceChild("bone",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(0, 5, 0));

        ModelBakery.PartDefinition body = bone.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(62, 68).addBox(-12.5f, -14, -20, 25, 29, 40)
                        .texOffs(62, 0).addBox(-12.5f, -14, -20, 25, 24, 40, new ModelBakery.CubeDeformation(0.5f))
                        .texOffs(87, 68).addBox(-12.5f, 12, -20, 25, 0, 40),
                ModelBakery.PartPose.offset(0, 0, 0));

        ModelBakery.PartDefinition head = body.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(8, 15).addBox(-6.5f, -7.5f, -11.5f, 13, 18, 11)
                        .texOffs(8, 4).addBox(-6.5f, 7.5f, -11.5f, 13, 0, 11),
                ModelBakery.PartPose.offset(0, 6.5f, -19.48f));
        head.addOrReplaceChild("left_ear",
                ModelBakery.CubeListBuilder.create().texOffs(2, 0).addBox(0, 0, -3, 1, 19, 7),
                ModelBakery.PartPose.offset(6.51f, -7.5f, -4.51f));
        head.addOrReplaceChild("right_ear",
                ModelBakery.CubeListBuilder.create().texOffs(48, 0).addBox(-1, 0, -3, 1, 19, 7),
                ModelBakery.PartPose.offset(-6.51f, -7.5f, -4.51f));
        head.addOrReplaceChild("nose",
                ModelBakery.CubeListBuilder.create().texOffs(10, 45).addBox(-6.5f, -2, -9, 13, 2, 9),
                ModelBakery.PartPose.offset(0, -4.5f, -11.5f));
        head.addOrReplaceChild("lower_beak",
                ModelBakery.CubeListBuilder.create().texOffs(10, 57).addBox(-6.5f, -7, -8, 13, 12, 9),
                ModelBakery.PartPose.offset(0, 2.5f, -12.5f));

        bone.addOrReplaceChild("right_front_leg",
                ModelBakery.CubeListBuilder.create().texOffs(32, 87).addBox(-3.5f, -1, -4, 7, 10, 8),
                ModelBakery.PartPose.offset(-7.5f, 10, -15));
        bone.addOrReplaceChild("right_mid_leg",
                ModelBakery.CubeListBuilder.create().texOffs(32, 105).addBox(-3.5f, -1, -4, 7, 10, 8),
                ModelBakery.PartPose.offset(-7.5f, 10, 0));
        bone.addOrReplaceChild("right_hind_leg",
                ModelBakery.CubeListBuilder.create().texOffs(32, 123).addBox(-3.5f, -1, -4, 7, 10, 8),
                ModelBakery.PartPose.offset(-7.5f, 10, 15));
        bone.addOrReplaceChild("left_front_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 87).addBox(-3.5f, -1, -4, 7, 10, 8),
                ModelBakery.PartPose.offset(7.5f, 10, -15));
        bone.addOrReplaceChild("left_mid_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 105).addBox(-3.5f, -1, -4, 7, 10, 8),
                ModelBakery.PartPose.offset(7.5f, 10, 0));
        bone.addOrReplaceChild("left_hind_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 123).addBox(-3.5f, -1, -4, 7, 10, 8),
                ModelBakery.PartPose.offset(7.5f, 10, 15));

        return root.bake();
    }

    private ModelBakery.BakedPart buildBabyModel() {
        ModelBakery bakery = new ModelBakery(BABY_TEXTURE, 128, 128);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition bone = root.addOrReplaceChild("bone",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(0, 24, 0));

        ModelBakery.PartDefinition body = bone.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 35).addBox(-13, -14, -0.5f, 14, 14, 20, new ModelBakery.CubeDeformation(0.25f))
                        .texOffs(0, 0).addBox(-13, -14, -0.5f, 14, 15, 20)
                        .texOffs(68, 0).addBox(-13, 0, -0.5f, 14, 0, 20),
                ModelBakery.PartPose.offset(6, -3, -9.5f));

        ModelBakery.PartDefinition head = body.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(68, 20).addBox(-5, -4.25f, -7.5f, 10, 9, 9)
                        .texOffs(88, 20).addBox(-5, 3.75f, -7.5f, 10, 0, 9),
                ModelBakery.PartPose.offset(-6, -4.75f, 0));
        head.addOrReplaceChild("left_ear",
                ModelBakery.CubeListBuilder.create().texOffs(104, 38).addBox(0, 0, -2, 1, 11, 3),
                ModelBakery.PartPose.offset(5, -4.25f, -1.5f));
        head.addOrReplaceChild("right_ear",
                ModelBakery.CubeListBuilder.create().texOffs(96, 38).addBox(-1, 0, -2, 1, 11, 3),
                ModelBakery.PartPose.offset(-5, -4.25f, -1.5f));
        head.addOrReplaceChild("nose",
                ModelBakery.CubeListBuilder.create().texOffs(68, 47).addBox(-5, -3, -2, 10, 3, 4),
                ModelBakery.PartPose.offset(0, -1.25f, -9.5f));
        head.addOrReplaceChild("lower_beak",
                ModelBakery.CubeListBuilder.create().texOffs(68, 38).addBox(-5, -2.5f, -2, 10, 5, 4),
                ModelBakery.PartPose.offset(0, 1.25f, -9.5f));

        bone.addOrReplaceChild("right_front_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 69).addBox(-2, -1, -2, 4, 5, 4),
                ModelBakery.PartPose.offset(-4, -4, -7));
        bone.addOrReplaceChild("right_mid_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 78).addBox(-2, -1, -2, 4, 5, 4),
                ModelBakery.PartPose.offset(-4, -4, 0));
        bone.addOrReplaceChild("right_hind_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 87).addBox(-2, -1, -2, 4, 5, 4),
                ModelBakery.PartPose.offset(-4, -4, 7));
        bone.addOrReplaceChild("left_front_leg",
                ModelBakery.CubeListBuilder.create().texOffs(16, 69).addBox(-2, -1, -2, 4, 5, 4),
                ModelBakery.PartPose.offset(4, -4, -7));
        bone.addOrReplaceChild("left_mid_leg",
                ModelBakery.CubeListBuilder.create().texOffs(16, 78).addBox(-2, -1, -2, 4, 5, 4),
                ModelBakery.PartPose.offset(4, -4, 0));
        bone.addOrReplaceChild("left_hind_leg",
                ModelBakery.CubeListBuilder.create().texOffs(16, 87).addBox(-2, -1, -2, 4, 5, 4),
                ModelBakery.PartPose.offset(4, -4, 7));

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, Sniffer entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyYaw);
        double headYaw = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYaw);
        double headPitch = Mth.DEG_TO_RAD * entity.getXRot(1.0f);
        boolean baby = entity.isBaby();
        double ageInTicks = entity.tickCount + 1.0f;
        double walkPos = entity.walkAnimation.position();
        double walkSpeed = entity.walkAnimation.speed();

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y, pos.z)
                .rotateY(modelYaw);
        if (baby) base.scale(0.5f);
        base.translate(0, 1.5f, 0);
        base.rotateY(Mth.PI);
        base.rotateX(Mth.PI);

        double frontAng = Mth.cos(walkPos * 0.6662f) * 1.4f * walkSpeed;
        double midAng = Mth.cos(walkPos * 0.6662f + Math.PI) * 1.4f * walkSpeed;
        double hindAng = Mth.cos(walkPos * 0.6662f) * 1.4f * walkSpeed;

        double earWiggle = Mth.cos(ageInTicks * 0.1f) * 0.1f;

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        ModelBakery.BakedPart model = buildRoot(entity);
        renderParts(pipeline, model, "root", base, headYaw, headPitch, frontAng, midAng, hindAng, earWiggle, block, sky);
    }

    private void renderParts(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                             Matrix4d parent,
                             double headYaw, double headPitch,
                             double frontAng, double midAng, double hindAng,
                             double earWiggle,
                             double block, double sky) {
        for (var child : part.children.entrySet()) {
            String childName = child.getKey();
            ModelBakery.BakedPart childPart = child.getValue();

            Matrix4d mat = new Matrix4d(parent);
            mat.translate(childPart.localPivot.x, childPart.localPivot.y, childPart.localPivot.z);

            ModelBakery.PartPose ip = childPart.initialPose;
            if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
                mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
            if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
                mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

            switch (childName) {
                case "head" -> {
                    mat.rotateY(headYaw);
                    mat.rotateX(headPitch);
                }
                case "right_front_leg", "left_hind_leg" -> mat.rotateX(frontAng);
                case "left_front_leg", "right_hind_leg" -> mat.rotateX(-frontAng);
                case "right_mid_leg" -> mat.rotateX(midAng);
                case "left_mid_leg" -> mat.rotateX(-midAng);
                case "left_ear" -> mat.rotateZ(-earWiggle);
                case "right_ear" -> mat.rotateZ(earWiggle);
            }

            if (childPart.mesh != null) {
                pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(childPart.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
            }

            renderParts(
                    pipeline, childPart, childName, mat, headYaw, headPitch,
                    frontAng, midAng, hindAng, earWiggle,
                    block, sky);
        }
    }
}