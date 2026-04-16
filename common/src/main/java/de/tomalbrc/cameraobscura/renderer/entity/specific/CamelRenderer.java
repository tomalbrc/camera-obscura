package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public class CamelRenderer implements LivingEntityRenderer<Camel> {
    private static final String ADULT_TEXTURE = "entity/camel/camel";
    private static final String BABY_TEXTURE = "entity/camel/camel_baby";
    private static final String SADDLE_TEXTURE = "entity/equipment/camel_saddle/saddle";

    private ModelBakery.BakedPart cachedAdultModel;
    private ModelBakery.BakedPart cachedBabyModel;
    private ModelBakery.BakedPart cachedSaddleModel;

    @Override
    public ModelBakery.BakedPart buildRoot(Camel entity) {
        if (entity.isBaby()) {
            if (cachedBabyModel == null) cachedBabyModel = buildBabyModel(BABY_TEXTURE);
            return cachedBabyModel;
        } else {
            if (cachedAdultModel == null) cachedAdultModel = buildAdultModel(ADULT_TEXTURE);
            return cachedAdultModel;
        }
    }

    protected ModelBakery.BakedPart getSaddleModel() {
        if (cachedSaddleModel == null) cachedSaddleModel = buildSaddleModel(SADDLE_TEXTURE);
        return cachedSaddleModel;
    }

    ModelBakery.BakedPart buildAdultModel(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 128, 128);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition body = root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(0, 25).addBox(-7.5f, -12, -23.5f, 15, 12, 27),
                ModelBakery.PartPose.offset(0, 4, 9.5f));

        body.addOrReplaceChild("hump",
                ModelBakery.CubeListBuilder.create().texOffs(74, 0).addBox(-4.5f, -5, -5.5f, 9, 5, 11),
                ModelBakery.PartPose.offset(0, -12, -10));

        body.addOrReplaceChild("tail",
                ModelBakery.CubeListBuilder.create().texOffs(122, 0).addBox(-1.5f, 0, 0, 3, 14, 0),
                ModelBakery.PartPose.offset(0, -9, 3.5f));

        ModelBakery.PartDefinition head = body.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(60, 24).addBox(-3.5f, -7, -15, 7, 8, 19)
                        .texOffs(21, 0).addBox(-3.5f, -21, -15, 7, 14, 7)
                        .texOffs(50, 0).addBox(-2.5f, -21, -21, 5, 5, 6),
                ModelBakery.PartPose.offset(0, -3, -19.5f));

        head.addOrReplaceChild("right_ear",
                ModelBakery.CubeListBuilder.create().texOffs(67, 0).addBox(-2.5f, 0.5f, -1, 3, 1, 2),
                ModelBakery.PartPose.offset(-2.5f, -21, -9.5f));
        head.addOrReplaceChild("left_ear",
                ModelBakery.CubeListBuilder.create().texOffs(45, 0).addBox(-0.5f, 0.5f, -1, 3, 1, 2),
                ModelBakery.PartPose.offset(2.5f, -21, -9.5f));

        root.addOrReplaceChild("left_hind_leg",
                ModelBakery.CubeListBuilder.create().texOffs(58, 16).addBox(-2.5f, 2, -2.5f, 5, 21, 5),
                ModelBakery.PartPose.offset(4.9f, 1, 9.5f));
        root.addOrReplaceChild("right_hind_leg",
                ModelBakery.CubeListBuilder.create().texOffs(94, 16).addBox(-2.5f, 2, -2.5f, 5, 21, 5),
                ModelBakery.PartPose.offset(-4.9f, 1, 9.5f));
        root.addOrReplaceChild("left_front_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-2.5f, 2, -2.5f, 5, 21, 5),
                ModelBakery.PartPose.offset(4.9f, 1, -10.5f));
        root.addOrReplaceChild("right_front_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 26).addBox(-2.5f, 2, -2.5f, 5, 21, 5),
                ModelBakery.PartPose.offset(-4.9f, 1, -10.5f));

        return root.bake();
    }

    private ModelBakery.BakedPart buildBabyModel(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 64, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition body = root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(0, 14).addBox(-4.5f, -4, -8, 9, 8, 16),
                ModelBakery.PartPose.offset(0, 7, 0));

        body.addOrReplaceChild("tail",
                ModelBakery.CubeListBuilder.create().texOffs(50, 38).addBox(-1.5f, -0.5f, 0, 3, 9, 0),
                ModelBakery.PartPose.offset(0, -1.5f, 8.05f));

        ModelBakery.PartDefinition head = body.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(20, 0).addBox(-2.5f, -3, -7.5f, 5, 5, 7)
                        .texOffs(0, 0).addBox(-2.5f, -12, -7.5f, 5, 9, 5)
                        .texOffs(0, 14).addBox(-2.5f, -12, -10.5f, 5, 4, 3),
                ModelBakery.PartPose.offset(0, 1, -7.5f));

        head.addOrReplaceChild("right_ear",
                ModelBakery.CubeListBuilder.create().texOffs(37, 0).addBox(-3, -0.5f, -1, 3, 1, 2),
                ModelBakery.PartPose.offset(-2.5f, -11, -4));
        head.addOrReplaceChild("left_ear",
                ModelBakery.CubeListBuilder.create().texOffs(47, 0).addBox(0, -0.5f, -1, 3, 1, 2),
                ModelBakery.PartPose.offset(2.5f, -11, -4));

        root.addOrReplaceChild("right_front_leg",
                ModelBakery.CubeListBuilder.create().texOffs(36, 14).addBox(-1.5f, -0.5f, -1.5f, 3, 13, 3),
                ModelBakery.PartPose.offset(-3, 11.5f, -5.5f));
        root.addOrReplaceChild("left_front_leg",
                ModelBakery.CubeListBuilder.create().texOffs(48, 14).addBox(-1.5f, -0.5f, -1.5f, 3, 13, 3),
                ModelBakery.PartPose.offset(3, 11.5f, -5.5f));
        root.addOrReplaceChild("left_hind_leg",
                ModelBakery.CubeListBuilder.create().texOffs(12, 38).addBox(-1.5f, -0.5f, -1.5f, 3, 13, 3),
                ModelBakery.PartPose.offset(3, 11.5f, 5.5f));
        root.addOrReplaceChild("right_hind_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 38).addBox(-1.5f, -0.5f, -1.5f, 3, 13, 3),
                ModelBakery.PartPose.offset(-3, 11.5f, 5.5f));

        return root.bake();
    }

    ModelBakery.BakedPart buildSaddleModel(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 128, 128);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition body = root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(0, 4, 9.5f));

        body.addOrReplaceChild("saddle",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(74, 64).addBox(-4.5f, -17, -15.5f, 9, 5, 11, new ModelBakery.CubeDeformation(0.05f))
                        .texOffs(92, 114).addBox(-3.5f, -20, -15.5f, 7, 3, 11, new ModelBakery.CubeDeformation(0.05f))
                        .texOffs(0, 89).addBox(-7.5f, -12, -23.5f, 15, 12, 27, new ModelBakery.CubeDeformation(0.05f)),
                ModelBakery.PartPose.offset(0, 0, 0));

        ModelBakery.PartDefinition head = body.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(0, -3, -19.5f));

        head.addOrReplaceChild("reins",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(98, 42).addBox(3.51f, -18, -17, 0, 7, 15)
                        .texOffs(84, 57).addBox(-3.5f, -18, -2, 7, 7, 0)
                        .texOffs(98, 42).addBox(-3.51f, -18, -17, 0, 7, 15),
                ModelBakery.PartPose.offset(0, 0, 0));

        head.addOrReplaceChild("bridle",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(60, 87).addBox(-3.5f, -7, -15, 7, 8, 19, new ModelBakery.CubeDeformation(0.05f))
                        .texOffs(21, 64).addBox(-3.5f, -21, -15, 7, 14, 7, new ModelBakery.CubeDeformation(0.05f))
                        .texOffs(50, 64).addBox(-2.5f, -21, -21, 5, 5, 6, new ModelBakery.CubeDeformation(0.05f))
                        .texOffs(74, 70).addBox(2.5f, -19, -18, 1, 2, 2)
                        .texOffs(74, 70).mirror().addBox(-3.5f, -19, -18, 1, 2, 2),
                ModelBakery.PartPose.offset(0, 0, 0));

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, Camel entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyYaw);
        double headYaw = entity.getYHeadRot() - bodyYaw;
        double headPitch = entity.getXRot(1.0f);
        headYaw = Mth.clamp(headYaw, -30, 30);
        headPitch = Mth.clamp(headPitch, -25, 45);

        boolean sitting = entity.isCamelVisuallySitting();
        boolean isRidden = entity.isVehicle();
        double jumpCooldown = Math.max(entity.getJumpCooldown() - 1.0f, 0);
        if (jumpCooldown > 0) {
            headPitch = Mth.clamp(headPitch + 45 * jumpCooldown / 55, -25, 70);
        }

        double walkPos = entity.walkAnimation.position();
        double walkSpeed = entity.walkAnimation.speed();

        double legRightFront = Mth.cos(walkPos * 0.6662f) * 1.4f * walkSpeed;
        double legLeftFront = Mth.cos(walkPos * 0.6662f + Mth.PI) * 1.4f * walkSpeed;
        double legRightHind = Mth.cos(walkPos * 0.6662f + Mth.PI) * 1.4f * walkSpeed;
        double legLeftHind = Mth.cos(walkPos * 0.6662f) * 1.4f * walkSpeed;

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y, pos.z)
                .rotateY(modelYaw);

        base.translate(0, 1.5f, 0);
        base.rotateY(Mth.PI);
        base.rotateX(Mth.PI);

        AnimParams params = new AnimParams(
                headYaw * Mth.DEG_TO_RAD, headPitch * Mth.DEG_TO_RAD,
                legRightFront, legLeftFront, legRightHind, legLeftHind,
                sitting, isRidden
        );

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderPart(pipeline, buildRoot(entity), "root", base, params, block, sky);

        if (!entity.getItemBySlot(EquipmentSlot.SADDLE).isEmpty()) {
            renderPart(pipeline, getSaddleModel(), "root", base, params, block, sky);
        }
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                            Matrix4d parent, AnimParams p,
                            double block, double sky) {

        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        switch (name) {
            case "head" -> {
                mat.rotateY(p.headYawRad);
                mat.rotateX(p.headPitchRad);
            }
            case "right_front_leg" -> mat.rotateX(p.rfAng);
            case "left_front_leg" -> mat.rotateX(p.lfAng);
            case "right_hind_leg" -> mat.rotateX(p.rhAng);
            case "left_hind_leg" -> mat.rotateX(p.lhAng);
            case "body" -> {
                if (p.sitting) {
                    mat.translate(0, -6f / 16f, 0);
                    mat.rotateX(-0.3f);
                }
            }
            case "reins" -> {
                if (!p.ridden) return;
            }
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }
        for (var child : part.children.entrySet()) {
            renderPart(pipeline, child.getValue(), child.getKey(), mat, p, block, sky);
        }
    }

    private static class AnimParams {
        final double headYawRad, headPitchRad;
        final double rfAng, lfAng, rhAng, lhAng;
        final boolean sitting, ridden;

        AnimParams(double headYawRad, double headPitchRad,
                   double rfAng, double lfAng, double rhAng, double lhAng,
                   boolean sitting, boolean ridden) {
            this.headYawRad = headYawRad;
            this.headPitchRad = headPitchRad;
            this.rfAng = rfAng;
            this.lfAng = lfAng;
            this.rhAng = rhAng;
            this.lhAng = lhAng;
            this.sitting = sitting;
            this.ridden = ridden;
        }
    }
}