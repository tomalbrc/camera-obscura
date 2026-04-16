package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

import java.util.Set;

public class WardenRenderer implements LivingEntityRenderer<Warden> {

    private static final String MAIN_TEXTURE = "entity/warden/warden";
    private static final String BIOLUMINESCENT_TEXTURE = "entity/warden/warden_bioluminescent_layer";
    private static final String PULSATING_SPOTS_1_TEXTURE = "entity/warden/warden_pulsating_spots_1";
    private static final String PULSATING_SPOTS_2_TEXTURE = "entity/warden/warden_pulsating_spots_2";
    private static final String HEART_TEXTURE = "entity/warden/warden_heart";

    private ModelBakery.BakedPart cachedMainModel;
    private ModelBakery.BakedPart cachedBioluminescentModel;
    private ModelBakery.BakedPart cachedPulsatingSpotsModel;
    private ModelBakery.BakedPart cachedTendrilsModel;
    private ModelBakery.BakedPart cachedHeartModel;

    @Override
    public ModelBakery.BakedPart buildRoot(Warden entity) {
        if (cachedMainModel == null) {
            cachedMainModel = buildFullModel(MAIN_TEXTURE);
        }
        return cachedMainModel;
    }

    private ModelBakery.BakedPart getBioluminescentModel() {
        if (cachedBioluminescentModel == null) {
            cachedBioluminescentModel = buildPartialModel(BIOLUMINESCENT_TEXTURE,
                    Set.of("head", "left_arm", "right_arm", "left_leg", "right_leg"));
        }
        return cachedBioluminescentModel;
    }

    private ModelBakery.BakedPart getPulsatingSpotsModel() {
        if (cachedPulsatingSpotsModel == null) {
            cachedPulsatingSpotsModel = buildPartialModel(PULSATING_SPOTS_1_TEXTURE,
                    Set.of("body", "head", "left_arm", "right_arm", "left_leg", "right_leg"));
        }
        return cachedPulsatingSpotsModel;
    }

    private ModelBakery.BakedPart getTendrilsModel() {
        if (cachedTendrilsModel == null) {
            cachedTendrilsModel = buildPartialModel(MAIN_TEXTURE,
                    Set.of("left_tendril", "right_tendril"));
        }
        return cachedTendrilsModel;
    }

    private ModelBakery.BakedPart getHeartModel() {
        if (cachedHeartModel == null) {
            cachedHeartModel = buildPartialModel(HEART_TEXTURE,
                    Set.of("body"));
        }
        return cachedHeartModel;
    }

    private ModelBakery.BakedPart buildFullModel(String texture) {
        return buildModelInternal(texture, null);
    }

    private ModelBakery.BakedPart buildPartialModel(String texture, Set<String> keepParts) {
        return buildModelInternal(texture, keepParts);
    }

    private ModelBakery.BakedPart buildModelInternal(String texture, Set<String> keepParts) {
        ModelBakery bakery = new ModelBakery(texture, 128, 128);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition bone = root.addOrReplaceChild("bone",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(0, 24, 0));

        ModelBakery.PartDefinition body = bone.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-9, -13, -4, 18, 21, 11),
                ModelBakery.PartPose.offset(0, -21, 0));

        if (keepParts == null || keepParts.contains("right_ribcage")) {
            body.addOrReplaceChild("right_ribcage",
                    ModelBakery.CubeListBuilder.create().texOffs(90, 11).addBox(-2, -11, -0.1f, 9, 21, 0),
                    ModelBakery.PartPose.offset(-7, -2, -4));
        }
        if (keepParts == null || keepParts.contains("left_ribcage")) {
            body.addOrReplaceChild("left_ribcage",
                    ModelBakery.CubeListBuilder.create().texOffs(90, 11).mirror().addBox(-7, -11, -0.1f, 9, 21, 0).mirror(false),
                    ModelBakery.PartPose.offset(7, -2, -4));
        }

        ModelBakery.PartDefinition head = body.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create().texOffs(0, 32).addBox(-8, -16, -5, 16, 16, 10),
                ModelBakery.PartPose.offset(0, -13, 0));

        if (keepParts == null || keepParts.contains("right_tendril")) {
            head.addOrReplaceChild("right_tendril",
                    ModelBakery.CubeListBuilder.create().texOffs(52, 32).addBox(-16, -13, 0, 16, 16, 0),
                    ModelBakery.PartPose.offset(-8, -12, 0));
        }
        if (keepParts == null || keepParts.contains("left_tendril")) {
            head.addOrReplaceChild("left_tendril",
                    ModelBakery.CubeListBuilder.create().texOffs(58, 0).addBox(0, -13, 0, 16, 16, 0),
                    ModelBakery.PartPose.offset(8, -12, 0));
        }

        if (keepParts == null || keepParts.contains("right_arm")) {
            body.addOrReplaceChild("right_arm",
                    ModelBakery.CubeListBuilder.create().texOffs(44, 50).addBox(-4, 0, -4, 8, 28, 8),
                    ModelBakery.PartPose.ZERO);
        }
        if (keepParts == null || keepParts.contains("left_arm")) {
            body.addOrReplaceChild("left_arm",
                    ModelBakery.CubeListBuilder.create().texOffs(0, 58).addBox(-4, 0, -4, 8, 28, 8), ModelBakery.PartPose.ZERO);
        }

        if (keepParts == null || keepParts.contains("right_leg")) {
            bone.addOrReplaceChild("right_leg",
                    ModelBakery.CubeListBuilder.create().texOffs(76, 48).addBox(-3.1f, 0, -3, 6, 13, 6),
                    ModelBakery.PartPose.offset(-5.9f, -13, 0));
        }
        if (keepParts == null || keepParts.contains("left_leg")) {
            bone.addOrReplaceChild("left_leg",
                    ModelBakery.CubeListBuilder.create().texOffs(76, 76).addBox(-2.9f, 0, -3, 6, 13, 6),
                    ModelBakery.PartPose.offset(5.9f, -13, 0));
        }

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, Warden entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyYaw);
        double headYaw = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYaw);
        double headPitch = Mth.DEG_TO_RAD * entity.getXRot(1.0f);
        double animPos = entity.walkAnimation.position();
        double animSpeed = entity.walkAnimation.speed();
        double age = entity.tickCount + 1.0f;
        double tendrilAnim = entity.getTendrilAnimation(1.0f);
        double heartAnim = entity.getHeartAnimation(1.0f);

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + 1.5f, pos.z)
                .rotateY(modelYaw)
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        double speedModifier = Math.min(0.5f, 3.0f * animSpeed);
        double adjustedPos = animPos * 0.8662f;
        double adjCos = Mth.cos(adjustedPos);
        double adjSin = Mth.sin(adjustedPos);
        double speedModWithMin = Math.min(0.35f, speedModifier);
        double headZRot = 0.3f * adjSin * speedModifier + 0.06f * Mth.cos(age * 0.1f);
        double headXRot = headPitch + 1.2f * Mth.cos(adjustedPos + (double) (Math.PI / 2)) * speedModWithMin + 0.06f * Mth.sin(age * 0.1f);
        double bodyZRot = 0.1f * adjSin * speedModifier + 0.025f * Mth.sin(age * 0.1f);
        double bodyXRot = 1.0f * adjCos * speedModWithMin + 0.025f * Mth.cos(age * 0.1f);
        double leftLegXRot = 1.0f * adjCos * speedModifier;
        double rightLegXRot = 1.0f * Mth.cos(adjustedPos + Mth.PI) * speedModifier;
        double leftArmXRot = -(0.8f * adjCos * speedModifier);
        double rightArmXRot = -(0.8f * adjSin * speedModifier);
        double tendrilXRot = tendrilAnim * (double) (Math.cos(age * 2.25) * Math.PI * 0.1f);

        AnimData data = new AnimData(
                headYaw, headXRot, headZRot,
                bodyXRot, bodyZRot,
                leftLegXRot, rightLegXRot,
                leftArmXRot, rightArmXRot,
                tendrilXRot
        );

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        ModelBakery.BakedPart mainModel = buildRoot(entity);

        renderWardenPart(pipeline, mainModel, "root", base, data, 0xFFFFFFFF, block, sky);
        renderWardenPart(pipeline, getBioluminescentModel(), "root", base, data, 0xFFFFFFFF, block, sky);

        double alpha1 = Math.max(0.0f, Mth.cos(age * 0.045f) * 0.25f);
        int color1 = ARGB.gray((float) alpha1);
        renderWardenPart(pipeline, getPulsatingSpotsModel(), "root", base, data, color1, block, sky);

        double alpha2 = Math.max(0.0f, Mth.cos(age * 0.045f + Mth.PI) * 0.25f);
        int color2 = ARGB.gray((float) alpha2);
        renderWardenPart(pipeline, getPulsatingSpotsModel(), "root", base, data, color2, block, sky);

        int tendrilColor = 0xFFFFFF;
        renderWardenPart(pipeline, getTendrilsModel(), "root", base, data, tendrilColor, block, sky);

        int heartColor = ARGB.gray((float) heartAnim);
        renderWardenPart(pipeline, getHeartModel(), "root", base, data, heartColor, block, sky);
    }

    private void renderWardenPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                                  Matrix4d parent, AnimData data, int color,
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
                mat.rotateY(data.headYaw);
                mat.rotateX(data.headXRot);
                mat.rotateZ(data.headZRot);
                break;
            case "body":
                mat.rotateX(data.bodyXRot);
                mat.rotateZ(data.bodyZRot);
                break;
            case "left_leg":
                mat.rotateX(data.leftLegXRot);
                break;
            case "right_leg":
                mat.rotateX(data.rightLegXRot);
                break;
            case "left_arm":
                mat.rotateX(data.leftArmXRot);
                mat.translate(13.0f / 16f, -13.0f / 16f, 1.0f / 16f);
                break;
            case "right_arm":
                mat.rotateX(data.rightArmXRot);
                mat.translate(-13.0f / 16f, -13.0f / 16f, 1.0f / 16f);
                break;
            case "right_tendril":
                mat.rotateX(-data.tendrilXRot);
                break;
            case "left_tendril":
                mat.rotateX(data.tendrilXRot);
                break;
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(color)));
        }

        for (var child : part.children.entrySet()) {
            renderWardenPart(pipeline, child.getValue(), child.getKey(), mat, data, color, block, sky);
        }
    }

    private static class AnimData {
        final double headYaw, headXRot, headZRot;
        final double bodyXRot, bodyZRot;
        final double leftLegXRot, rightLegXRot;
        final double leftArmXRot, rightArmXRot;
        final double tendrilXRot;

        AnimData(double headYaw, double headXRot, double headZRot,
                 double bodyXRot, double bodyZRot,
                 double leftLegXRot, double rightLegXRot,
                 double leftArmXRot, double rightArmXRot,
                 double tendrilXRot) {
            this.headYaw = headYaw;
            this.headXRot = headXRot;
            this.headZRot = headZRot;
            this.bodyXRot = bodyXRot;
            this.bodyZRot = bodyZRot;
            this.leftLegXRot = leftLegXRot;
            this.rightLegXRot = rightLegXRot;
            this.leftArmXRot = leftArmXRot;
            this.rightArmXRot = rightArmXRot;
            this.tendrilXRot = tendrilXRot;
        }
    }
}
