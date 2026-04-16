package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.EntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

import java.lang.reflect.Field;

public class EvokerFangsRenderer implements EntityRenderer<EvokerFangs> {
    private static final String TEXTURE = "entity/illager/evoker_fangs";
    private ModelBakery.BakedPart cachedModel;

    public double getAnimationProgress(EvokerFangs fangs, final double a) {
        int remainingLife = EvokerFangsAccess.getLifeTicks(fangs) - 2;
        return remainingLife <= 0 ? 1.0F : 1.0F - (remainingLife - a) / 20.0F;
    }

    @Override
    public void render(RenderPipeline pipeline, EvokerFangs entity) {
        double biteProgress = getAnimationProgress(entity, 1f);
        if (biteProgress == 0.0f) return;

        if (cachedModel == null) {
            cachedModel = buildModel();
        }

        double yRot = entity.getYRot();

        double biteAmount = Math.min(biteProgress * 2.0f, 1.0f);
        biteAmount = 1.0f - biteAmount * biteAmount * biteAmount;

        double upperJawInitialZRot = 2.042035f;
        double lowerJawInitialZRot = 4.2411504f;
        double targetUpperZRot = Math.PI - biteAmount * 0.35f * Math.PI;
        double targetLowerZRot = Math.PI - biteAmount * 0.35f * Math.PI;
        double upperJawExtraZ = targetUpperZRot - upperJawInitialZRot;
        double lowerJawExtraZ = targetLowerZRot + lowerJawInitialZRot;

        double baseYOffset = -(biteProgress + Mth.sin(biteProgress * 2.7f)) * 7.2f;

        double preScale = 1.0f;
        if (biteProgress > 0.9f) {
            preScale *= (1.0f - biteProgress) / 0.1f;
        }
        double rootY = 24.0f - 20.0f * preScale;

        Matrix4d base = new Matrix4d()
                .translate(entity.position().toVector3f())
                .rotateY(Mth.DEG_TO_RAD * (90.0f - yRot))
                .scale(-1.0f, -1.0f, 1.0f)
                .translate(0.0f, -1.501f, 0.0f)
                .translate(0.0f, rootY / 16.0f, 0.0f)
                .scale(preScale, preScale, preScale);

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderPart(pipeline, cachedModel, "root", base, baseYOffset, upperJawExtraZ, lowerJawExtraZ, block, sky);
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                            Matrix4d parent, double baseYOffset, double upperJawExtraZ, double lowerJawExtraZ,
                            double block, double sky) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        switch (name) {
            case "base" -> {
                mat.translate(0.0f, baseYOffset / 16.0f, 0.0f);
            }
            case "upper_jaw" -> {
                mat.rotateZ(upperJawExtraZ);
            }
            case "lower_jaw" -> {
                mat.rotateZ(lowerJawExtraZ);
            }
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }

        for (var child : part.children.entrySet()) {
            renderPart(pipeline, child.getValue(), child.getKey(), mat, baseYOffset, upperJawExtraZ, lowerJawExtraZ, block, sky);
        }
    }

    private ModelBakery.BakedPart buildModel() {
        ModelBakery bakery = new ModelBakery(TEXTURE, 64, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition base = root.addOrReplaceChild("base",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(0, 0, 0, 10, 12, 10),
                ModelBakery.PartPose.offset(-5, 24, -5));

        ModelBakery.CubeListBuilder jaw = ModelBakery.CubeListBuilder.create()
                .texOffs(40, 0).addBox(0, 0, 0, 4, 14, 8);

        base.addOrReplaceChild("upper_jaw", jaw,
                ModelBakery.PartPose.offsetAndRotation(6.5f, 0, 1, 0, 0, 2.042035f));
        base.addOrReplaceChild("lower_jaw", jaw,
                ModelBakery.PartPose.offsetAndRotation(3.5f, 0, 9, 0, Mth.PI, 4.2411504f));

        return root.bake();
    }

    public static class EvokerFangsAccess {
        private static final Field LIFE_TICKS_FIELD;

        static {
            try {
                LIFE_TICKS_FIELD = EvokerFangs.class.getDeclaredField("lifeTicks"); // Mojang name
                LIFE_TICKS_FIELD.setAccessible(true);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("EvokerFangs lifeTicks field not found", e);
            }
        }


        public static int getLifeTicks(EvokerFangs fangs) {
            try {
                return LIFE_TICKS_FIELD.getInt(fangs);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Could not read EvokerFangs lifeTicks", e);
            }
        }

        public static void setLifeTicks(EvokerFangs fangs, int ticks) {
            try {
                LIFE_TICKS_FIELD.setInt(fangs, ticks);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Could not write EvokerFangs lifeTicks", e);
            }
        }
    }
}
