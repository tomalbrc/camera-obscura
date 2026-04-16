package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.BlockStateRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Crackiness;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import org.joml.Matrix4d;

import java.util.EnumMap;
import java.util.Map;

public class IronGolemRenderer implements LivingEntityRenderer<IronGolem> {

    private static final String MAIN_TEXTURE = "entity/iron_golem/iron_golem";
    private static final Map<Crackiness.Level, String> CRACKINESS_TEXTURES = new EnumMap<>(Crackiness.Level.class);

    static {
        CRACKINESS_TEXTURES.put(Crackiness.Level.LOW, "entity/iron_golem/iron_golem_crackiness_low");
        CRACKINESS_TEXTURES.put(Crackiness.Level.MEDIUM, "entity/iron_golem/iron_golem_crackiness_medium");
        CRACKINESS_TEXTURES.put(Crackiness.Level.HIGH, "entity/iron_golem/iron_golem_crackiness_high");
    }

    private final Map<Crackiness.Level, ModelBakery.BakedPart> cachedCrackModels = new EnumMap<>(Crackiness.Level.class);
    private ModelBakery.BakedPart cachedMainModel;

    @Override
    public ModelBakery.BakedPart buildRoot(IronGolem entity) {
        if (cachedMainModel == null) {
            cachedMainModel = buildModel(MAIN_TEXTURE);
        }
        return cachedMainModel;
    }

    private ModelBakery.BakedPart buildModel(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 128, 128);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4, -12, -5.5f, 8, 10, 8)
                        .texOffs(24, 0).addBox(-1, -5, -7.5f, 2, 4, 2),
                ModelBakery.PartPose.offset(0, -7, -2));
        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 40).addBox(-9, -2, -6, 18, 12, 11)
                        .texOffs(0, 70).addBox(-4.5f, 10, -3, 9, 5, 6, new ModelBakery.CubeDeformation(0.5f)),
                ModelBakery.PartPose.offset(0, -7, 0));
        root.addOrReplaceChild("right_arm",
                ModelBakery.CubeListBuilder.create().texOffs(60, 21).addBox(-13, -2.5f, -3, 4, 30, 6),
                ModelBakery.PartPose.offset(0, -7, 0));
        root.addOrReplaceChild("left_arm",
                ModelBakery.CubeListBuilder.create().texOffs(60, 58).addBox(9, -2.5f, -3, 4, 30, 6),
                ModelBakery.PartPose.offset(0, -7, 0));
        root.addOrReplaceChild("right_leg",
                ModelBakery.CubeListBuilder.create().texOffs(37, 0).addBox(-3.5f, -3, -3, 6, 16, 5),
                ModelBakery.PartPose.offset(-4, 11, 0));
        root.addOrReplaceChild("left_leg",
                ModelBakery.CubeListBuilder.create().texOffs(60, 0).mirror().addBox(-3.5f, -3, -3, 6, 16, 5),
                ModelBakery.PartPose.offset(5, 11, 0));

        return root.bake();
    }

    private ModelBakery.BakedPart getCrackinessModel(Crackiness.Level level) {
        return cachedCrackModels.computeIfAbsent(level, lvl -> buildModel(CRACKINESS_TEXTURES.get(lvl)));
    }

    @Override
    public void render(RenderPipeline pipeline, IronGolem entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyYaw);
        double headYaw = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYaw);
        double headPitch = Mth.DEG_TO_RAD * entity.getXRot(1.0f);
        double walkPos = entity.walkAnimation.position();
        double walkSpeed = entity.walkAnimation.speed();
        double attackTick = entity.getAttackAnimationTick() > 0 ? entity.getAttackAnimationTick() - 1.0f : 0;
        int offerFlowerTick = entity.getOfferFlowerTick();
        Crackiness.Level crackiness = entity.getCrackiness();

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + 1.5f, pos.z)
                .rotateY(modelYaw);

        if (walkSpeed >= 0.01f) {
            double p = 13.0f;
            double wp = walkPos + 6.0f;
            double triangleWave = (Math.abs(wp % p - 6.5f) - 3.25f) / 3.25f;
            base.rotateZ(Mth.DEG_TO_RAD * 6.5f * triangleWave);
        }

        base.rotateY(Mth.PI);
        base.rotateX(Mth.PI);

        double armRightXRot, armLeftXRot;
        if (attackTick > 0) {
            armRightXRot = -2.0f + 1.5f * Mth.triangleWave((float) attackTick, 10.0f);
            armLeftXRot = armRightXRot;
        } else if (offerFlowerTick > 0) {
            armRightXRot = -0.8f + 0.025f * Mth.triangleWave(offerFlowerTick, 70.0f);
            armLeftXRot = 0;
        } else {
            armRightXRot = (-0.2f + 1.5f * Mth.triangleWave((float) walkPos, 13.0f)) * walkSpeed;
            armLeftXRot = (-0.2f - 1.5f * Mth.triangleWave((float) walkPos, 13.0f)) * walkSpeed;
        }
        double legRightXRot = -1.5f * Mth.triangleWave((float) walkPos, 13.0f) * walkSpeed;
        double legLeftXRot = 1.5f * Mth.triangleWave((float) walkPos, 13.0f) * walkSpeed;

        AnimParams params = new AnimParams(headYaw, headPitch, armRightXRot, armLeftXRot, legRightXRot, legLeftXRot);

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderPart(pipeline, buildRoot(entity), "root", base, params, block, sky);

        if (crackiness != Crackiness.Level.NONE) {
            renderPart(pipeline, getCrackinessModel(crackiness), "root", base, params, block, sky);
        }

        if (offerFlowerTick > 0) {
            Matrix4d armMat = findPartMatrix(buildRoot(entity), "root", base, params, "right_arm");
            Matrix4d flowerMat = new Matrix4d(armMat)
                    .translate(-1.1875f, 1.0625f, -0.9375f)
                    .translate(0.5f, 0.5f, 0.5f)
                    .scale(0.5f)
                    .rotateX(-Mth.PI / 2)
                    .translate(-0.5f, -0.5f, -0.5f);
            BlockStateRenderer.render(pipeline, Blocks.POPPY.defaultBlockState(), flowerMat, block, sky);
        }
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                            Matrix4d parent, AnimParams params,
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
                mat.rotateY(params.headYaw);
                mat.rotateX(params.headPitch);
            }
            case "right_arm" -> mat.rotateX(params.armRightXRot);
            case "left_arm" -> mat.rotateX(params.armLeftXRot);
            case "right_leg" -> mat.rotateX(params.legRightXRot);
            case "left_leg" -> mat.rotateX(params.legLeftXRot);
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }

        for (var child : part.children.entrySet()) {
            renderPart(pipeline, child.getValue(), child.getKey(), mat, params, block, sky);
        }
    }

    private Matrix4d findPartMatrix(ModelBakery.BakedPart part, String name, Matrix4d parent,
                                    AnimParams params, String target) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        switch (name) {
            case "head" -> {
                mat.rotateY(params.headYaw);
                mat.rotateX(params.headPitch);
            }
            case "right_arm" -> mat.rotateX(params.armRightXRot);
            case "left_arm" -> mat.rotateX(params.armLeftXRot);
            case "right_leg" -> mat.rotateX(params.legRightXRot);
            case "left_leg" -> mat.rotateX(params.legLeftXRot);
        }

        if (name.equals(target)) {
            return mat;
        }

        for (var child : part.children.entrySet()) {
            Matrix4d result = findPartMatrix(child.getValue(), child.getKey(), mat, params, target);
            if (result != null) return result;
        }
        return null;
    }

    private static class AnimParams {
        final double headYaw, headPitch;
        final double armRightXRot, armLeftXRot;
        final double legRightXRot, legLeftXRot;

        AnimParams(double headYaw, double headPitch, double armRightXRot, double armLeftXRot,
                   double legRightXRot, double legLeftXRot) {
            this.headYaw = headYaw;
            this.headPitch = headPitch;
            this.armRightXRot = armRightXRot;
            this.armLeftXRot = armLeftXRot;
            this.legRightXRot = legRightXRot;
            this.legLeftXRot = legLeftXRot;
        }
    }
}