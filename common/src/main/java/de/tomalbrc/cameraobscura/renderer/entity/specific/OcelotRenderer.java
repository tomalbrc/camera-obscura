package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public class OcelotRenderer implements LivingEntityRenderer<Ocelot> {

    private static final String ADULT_TEXTURE = "entity/cat/ocelot";
    private static final String BABY_TEXTURE = "entity/cat/ocelot_baby";

    private ModelBakery.BakedPart cachedAdultRoot;
    private ModelBakery.BakedPart cachedBabyRoot;

    @Override
    public ModelBakery.BakedPart buildRoot(Ocelot entity) {
        if (entity.isBaby()) {
            if (cachedBabyRoot == null) cachedBabyRoot = buildBabyModel(BABY_TEXTURE);
            return cachedBabyRoot;
        } else {
            if (cachedAdultRoot == null) cachedAdultRoot = buildAdultModel(ADULT_TEXTURE);
            return cachedAdultRoot;
        }
    }

    private ModelBakery.BakedPart buildAdultModel(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 64, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .addBox("main", -2.5f, -2.0f, -3.0f, 5.0f, 4.0f, 5.0f, ModelBakery.CubeDeformation.NONE, 0, 0)
                        .addBox("nose", -1.5f, -0.001f, -4.0f, 3, 2, 2, ModelBakery.CubeDeformation.NONE, 0, 24)
                        .addBox("ear1", -2.0f, -3.0f, 0.0f, 1, 1, 2, ModelBakery.CubeDeformation.NONE, 0, 10)
                        .addBox("ear2", 1.0f, -3.0f, 0.0f, 1, 1, 2, ModelBakery.CubeDeformation.NONE, 6, 10),
                ModelBakery.PartPose.offset(0.0f, 15.0f, -9.0f));

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(20, 0).addBox(-2.0f, 3.0f, -8.0f, 4.0f, 16.0f, 6.0f),
                ModelBakery.PartPose.offsetAndRotation(0.0f, 12.0f, -10.0f, Mth.PI / 2, 0.0f, 0.0f));

        root.addOrReplaceChild("tail1",
                ModelBakery.CubeListBuilder.create().texOffs(0, 15).addBox(-0.5f, 0.0f, 0.0f, 1.0f, 8.0f, 1.0f),
                ModelBakery.PartPose.offset(0.0f, 15.0f, 8.0f));
        root.addOrReplaceChild("tail2",
                ModelBakery.CubeListBuilder.create().texOffs(4, 15).addBox(-0.5f, 0.0f, 0.0f, 1.0f, 8.0f, 1.0f, new ModelBakery.CubeDeformation(-0.02f)),
                ModelBakery.PartPose.offset(0.0f, 20.0f, 14.0f));

        ModelBakery.CubeListBuilder hindLeg = ModelBakery.CubeListBuilder.create().texOffs(8, 13).addBox(-1.0f, 0.0f, 1.0f, 2.0f, 6.0f, 2.0f);
        root.addOrReplaceChild("left_hind_leg", hindLeg, ModelBakery.PartPose.offset(1.1f, 18.0f, 5.0f));
        root.addOrReplaceChild("right_hind_leg", hindLeg, ModelBakery.PartPose.offset(-1.1f, 18.0f, 5.0f));
        ModelBakery.CubeListBuilder frontLeg = ModelBakery.CubeListBuilder.create().texOffs(40, 0).addBox(-1.0f, 0.0f, 0.0f, 2.0f, 10.0f, 2.0f);
        root.addOrReplaceChild("left_front_leg", frontLeg, ModelBakery.PartPose.offset(1.2f, 14.1f, -5.0f));
        root.addOrReplaceChild("right_front_leg", frontLeg, ModelBakery.PartPose.offset(-1.2f, 14.1f, -5.0f));

        return root.bake();
    }

    private ModelBakery.BakedPart buildBabyModel(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 32, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.5f, -3.0f, -2.875f, 5.0f, 4.0f, 4.0f)
                        .texOffs(18, 0).addBox(-2.0f, -4.0f, -0.875f, 1.0f, 1.0f, 2.0f)
                        .texOffs(24, 0).addBox(1.0f, -4.0f, -0.875f, 1.0f, 1.0f, 2.0f)
                        .texOffs(18, 3).addBox(-1.5f, -1.0f, -3.875f, 3.0f, 2.0f, 1.0f),
                ModelBakery.PartPose.offset(0.0f, 20.0f, -3.125f));

        root.addOrReplaceChild("left_front_leg", ModelBakery.CubeListBuilder.create().texOffs(18, 18).addBox(-0.5f, 0.0f, -1.0f, 1.0f, 2.0f, 2.0f), ModelBakery.PartPose.offset(1.0f, 22.0f, -1.5f));
        root.addOrReplaceChild("right_front_leg", ModelBakery.CubeListBuilder.create().texOffs(12, 18).addBox(-0.5f, 0.0f, -1.0f, 1.0f, 2.0f, 2.0f), ModelBakery.PartPose.offset(-1.0f, 22.0f, -1.5f));
        root.addOrReplaceChild("left_hind_leg", ModelBakery.CubeListBuilder.create().texOffs(18, 22).addBox(-0.5f, 0.0f, -1.0f, 1.0f, 2.0f, 2.0f), ModelBakery.PartPose.offset(1.0f, 22.0f, 2.5f));
        root.addOrReplaceChild("right_hind_leg", ModelBakery.CubeListBuilder.create().texOffs(12, 22).addBox(-0.5f, 0.0f, -1.0f, 1.0f, 2.0f, 2.0f), ModelBakery.PartPose.offset(-1.0f, 22.0f, 2.5f));
        root.addOrReplaceChild("body", ModelBakery.CubeListBuilder.create().texOffs(0, 8).addBox(-2.0f, -1.5f, -3.5f, 4.0f, 3.0f, 7.0f), ModelBakery.PartPose.offset(0.0f, 20.5f, 0.5f));
        root.addOrReplaceChild("tail1", ModelBakery.CubeListBuilder.create().texOffs(0, 18).addBox(-0.5f, -0.107f, 0.0849f, 1.0f, 1.0f, 5.0f), ModelBakery.PartPose.offset(0.0f, 19.107f, 3.9151f));
        root.addOrReplaceChild("tail2", ModelBakery.CubeListBuilder.create(), ModelBakery.PartPose.ZERO);

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, Ocelot entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyYaw);
        double headYaw = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYaw);
        double headPitch = Mth.DEG_TO_RAD * entity.getXRot(1.0f);
        boolean baby = entity.isBaby();
        boolean crouching = entity.isCrouching();
        boolean sprinting = entity.isSprinting();
        double animPos = entity.walkAnimation.position();
        double animSpeed = entity.walkAnimation.speed();

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y, pos.z)
                .rotateY(modelYaw)
                .translate(0.0f, 1.5f, 0.0f)
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        AnimState state = new AnimState(
                headYaw, headPitch, animPos, animSpeed, crouching, sprinting, false, 0f, 0f, 0f, baby ? 0.5f : 1.0f, baby
        );

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        int tint = LivingEntityRenderer.hurtTint(entity);
        var tints = IntList.of(tint);

        renderCatPart(pipeline, buildRoot(entity), "root", base, state, tints, block, sky);
    }

    private void renderCatPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                               Matrix4d parent, AnimState s,
                               IntList tints, double block, double sky) {

        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        switch (name) {
            case "head" -> applyHeadAnimation(mat, s);
            case "body" -> applyBodyAnimation(mat, s);
            case "tail1" -> applyTail1Animation(mat, s);
            case "tail2" -> applyTail2Animation(mat, s);
        }
        if (name.contains("leg")) {
            applyLegAnimation(mat, name, s);
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, tints));
        }
        for (var child : part.children.entrySet()) {
            renderCatPart(pipeline, child.getValue(), child.getKey(), mat, s, tints, block, sky);
        }
    }

    private void applyHeadAnimation(Matrix4d mat, AnimState s) {
        if (s.crouching) {
            mat.translate(0, 2.0f * s.ageScale / 16f, 0);
        }
        mat.rotateY(s.headYaw);
        mat.rotateX(s.headPitch);
    }

    private void applyBodyAnimation(Matrix4d mat, AnimState s) {
        if (s.crouching) {
            mat.translate(0, s.ageScale / 16f, 0);
        }
    }

    private void applyTail1Animation(Matrix4d mat, AnimState s) {
        double targetXRot = s.baby ? -0.567232f : 0.9f;

        if (s.crouching) {
            mat.translate(0, s.ageScale / 16f, 0);
            targetXRot = (Math.PI / 2);
        } else if (s.sprinting) {
            targetXRot = (Math.PI / 2);
        }

        mat.rotateX(targetXRot);
    }

    private void applyTail2Animation(Matrix4d mat, AnimState s) {
        double baseXRot = 1.7278761f;
        if (s.crouching) {
            mat.translate(0, -4.0f * s.ageScale / 16f, 2.0f * s.ageScale / 16f);
            mat.rotateX((Math.PI / 2));
        } else if (s.sprinting) {
            mat.translate(0, 0, 2.0f * s.ageScale / 16f);
            mat.rotateX((Math.PI / 2));
        } else {
            baseXRot += (Math.PI / 4) * Mth.cos(s.walkPos) * s.walkSpeed;
            mat.rotateX(baseXRot);
        }
    }

    private void applyLegAnimation(Matrix4d mat, String name, AnimState s) {
        boolean right = name.contains("right");
        boolean hind = name.contains("hind");
        double walkAngle = 0;
        if (s.sprinting) {
            if (right && hind) walkAngle = Mth.cos(s.walkPos * 0.6662f + 0.3f) * s.walkSpeed;
            else if (!right && hind) walkAngle = Mth.cos(s.walkPos * 0.6662f) * s.walkSpeed;
            else if (right) walkAngle = Mth.cos(s.walkPos * 0.6662f + Mth.PI) * s.walkSpeed;
            else walkAngle = Mth.cos(s.walkPos * 0.6662f + Mth.PI + 0.3f) * s.walkSpeed;
        } else {
            if ((hind && right) || (!hind && !right)) {
                walkAngle = Mth.cos(s.walkPos * 0.6662f) * s.walkSpeed;
            } else {
                walkAngle = Mth.cos(s.walkPos * 0.6662f + Mth.PI) * s.walkSpeed;
            }
        }
        mat.rotateX(walkAngle);
    }

    private static class AnimState {
        final double headYaw, headPitch, walkPos, walkSpeed;
        final boolean crouching, sprinting, sitting;
        final double lieDown, lieDownTail, relaxOne, ageScale;
        final boolean baby;

        AnimState(double headYaw, double headPitch, double walkPos, double walkSpeed,
                  boolean crouching, boolean sprinting, boolean sitting,
                  double lieDown, double lieDownTail, double relaxOne, double ageScale, boolean baby) {
            this.headYaw = headYaw;
            this.headPitch = headPitch;
            this.walkPos = walkPos;
            this.walkSpeed = walkSpeed;
            this.crouching = crouching;
            this.sprinting = sprinting;
            this.sitting = sitting;
            this.lieDown = lieDown;
            this.lieDownTail = lieDownTail;
            this.relaxOne = relaxOne;
            this.ageScale = ageScale;
            this.baby = baby;
        }
    }
}