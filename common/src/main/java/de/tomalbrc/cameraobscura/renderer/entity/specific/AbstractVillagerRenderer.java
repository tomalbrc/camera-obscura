package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractVillagerRenderer<T extends AbstractVillager> implements LivingEntityRenderer<T> {
    protected static final int TEX_WIDTH = 64;
    protected static final int TEX_HEIGHT = 64;

    private static final Map<String, ModelBakery.BakedPart> modelCache = new HashMap<>();

    protected abstract String getAdultTexture(T entity);

    protected abstract String getBabyTexture(T entity);

    protected boolean includeHat(T entity) {
        return true;
    }

    @Override
    public ModelBakery.BakedPart buildRoot(T entity) {
        String texture = entity.isBaby() ? getBabyTexture(entity) : getAdultTexture(entity);
        return modelCache.computeIfAbsent(texture, t -> buildGenericModel(entity.isBaby(), includeHat(entity), t));
    }


    protected ModelBakery.BakedPart buildGenericModel(boolean baby, boolean hat, String texture) {
        ModelBakery bakery = new ModelBakery(texture, TEX_WIDTH, TEX_HEIGHT);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        if (baby) {
            buildBabyParts(root, hat);
        } else {
            buildAdultParts(root, hat);
        }

        return root.bake();
    }

    private void buildAdultParts(ModelBakery.PartDefinition root, boolean hat) {
        ModelBakery.PartDefinition head = root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-4, -10, -4, 8, 10, 8),
                ModelBakery.PartPose.ZERO);
        if (hat) {
            ModelBakery.PartDefinition hatPart = head.addOrReplaceChild("hat",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(32, 0).addBox(-4, -10, -4, 8, 10, 8, new ModelBakery.CubeDeformation(0.51f)),
                    ModelBakery.PartPose.ZERO);

            hatPart.addOrReplaceChild("hat_rim",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(30, 47).addBox(-8, -8, -6, 16, 16, 1),
                    ModelBakery.PartPose.rotation(-Mth.PI / 2, 0, 0));
        }

        head.addOrReplaceChild("nose",
                ModelBakery.CubeListBuilder.create().texOffs(24, 0).addBox(-1, -1, -6, 2, 4, 2),
                ModelBakery.PartPose.offset(0, -2, 0));

        ModelBakery.PartDefinition body = root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(16, 20).addBox(-4, 0, -3, 8, 12, 6),
                ModelBakery.PartPose.ZERO);
        body.addOrReplaceChild("jacket",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 38).addBox(-4, 0, -3, 8, 20, 6, new ModelBakery.CubeDeformation(0.5f)),
                ModelBakery.PartPose.ZERO);

        root.addOrReplaceChild("arms",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(44, 22).addBox(-8, -2, -2, 4, 8, 4)
                        .texOffs(44, 22).addBox(4, -2, -2, 4, 8, 4)
                        .texOffs(40, 38).addBox(-4, 2, -2, 8, 4, 4),
                ModelBakery.PartPose.offsetAndRotation(0, 3, -1, -0.75f, 0, 0));

        root.addOrReplaceChild("right_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 22).addBox(-2, 0, -2, 4, 12, 4),
                ModelBakery.PartPose.offset(-2, 12, 0));
        root.addOrReplaceChild("left_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 22).mirror().addBox(-2, 0, -2, 4, 12, 4),
                ModelBakery.PartPose.offset(2, 12, 0));
    }

    private void buildBabyParts(ModelBakery.PartDefinition root, boolean hat) {
        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(0, 15).addBox(-2, -2.75f, -1.5f, 4, 5, 3),
                ModelBakery.PartPose.offset(0, 18.75f, 0));
        root.addOrReplaceChild("bb_main",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(16, 21).addBox(-2.5f, -8, -1.5f, 4, 6, 3, new ModelBakery.CubeDeformation(0.2f)),
                ModelBakery.PartPose.offset(0.5f, 24, 0));

        ModelBakery.PartDefinition arms = root.addOrReplaceChild("arms",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(0, 17.5f, 0));
        arms.addOrReplaceChild("right_hand",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(36, 15).addBox(-1, -2.4925f, -1.8401f, 2, 4, 2)
                        .texOffs(16, 15).addBox(5, -2.4925f, -1.8401f, 2, 4, 2),
                ModelBakery.PartPose.offsetAndRotation(-3, 1.4025f, -0.9599f, -1.0472f, 0, 0));
        arms.addOrReplaceChild("middlearm_r1",
                ModelBakery.CubeListBuilder.create().texOffs(24, 17).addBox(-2, -0.9924f, -0.9825f, 4, 2, 2),
                ModelBakery.PartPose.offsetAndRotation(0, 0.9024f, -1.8175f, -1.0472f, 0, 0));

        root.addOrReplaceChild("right_leg",
                ModelBakery.CubeListBuilder.create().texOffs(8, 23).addBox(-1, -0.5f, -1, 2, 3, 2),
                ModelBakery.PartPose.offset(-1, 21.5f, 0));
        root.addOrReplaceChild("left_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 23).addBox(-1, -0.5f, -1, 2, 3, 2),
                ModelBakery.PartPose.offset(1, 21.5f, 0));

        ModelBakery.PartDefinition head = root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-4, -8, -3.5f, 8, 8, 7),
                ModelBakery.PartPose.offset(0, 16, 0));
        if (hat) {
            head.addOrReplaceChild("hat",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(0, 30).addBox(-4, -4, -3.5f, 8, 8, 7, new ModelBakery.CubeDeformation(0.3f)),
                    ModelBakery.PartPose.offset(0, -4, 0));
        }
        head.addOrReplaceChild("nose",
                ModelBakery.CubeListBuilder.create().texOffs(23, 0).addBox(-1, 0, -0.5f, 2, 2, 1),
                ModelBakery.PartPose.offset(0, -2, -4));
    }

    @Override
    public void render(RenderPipeline pipeline, T entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double headYawRel = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYaw);
        double headPitch = Mth.DEG_TO_RAD * entity.getXRot(1.0f);
        boolean unhappy = entity.getUnhappyCounter() > 0;

        double ageInTicks = entity.tickCount + 1.0f;

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + 1.5f, pos.z)
                .rotateY(Mth.DEG_TO_RAD * (180f - bodyYaw))
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        double animPos = entity.walkAnimation.position();
        double animSpeed = entity.walkAnimation.speed();
        double rightLegXRot = Mth.cos(animPos * 0.6662f) * 1.4f * animSpeed * 0.5f;
        double leftLegXRot = Mth.cos(animPos * 0.6662f + Mth.PI) * 1.4f * animSpeed * 0.5f;

        double headZRot = unhappy ? 0.3f * Mth.sin(0.45f * ageInTicks) : 0f;
        if (unhappy) headPitch = 0.4f;

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderModel(pipeline, buildRoot(entity), base, headYawRel, headPitch, headZRot, rightLegXRot, leftLegXRot, block, sky);
        renderAdditionalLayers(pipeline, entity, base, headYawRel, headPitch, headZRot, rightLegXRot, leftLegXRot, block, sky);
    }

    protected void renderModel(RenderPipeline pipeline, ModelBakery.BakedPart part, Matrix4d parent,
                               double headYaw, double headPitch, double headZRot,
                               double rightLegXRot, double leftLegXRot,
                               double block, double sky) {

        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        var ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        renderPart(pipeline, part, "root", mat, headYaw, headPitch, headZRot, rightLegXRot, leftLegXRot, block, sky);
    }

    protected void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part,
                              String name, Matrix4d mat,
                              double headYaw, double headPitch, double headZRot,
                              double rightLegXRot, double leftLegXRot,
                              double block, double sky) {

        switch (name) {
            case "head" -> {
                mat.rotateY(headYaw);
                mat.rotateX(headPitch);
                if (headZRot != 0) mat.rotateZ(headZRot);
            }
            case "right_leg" -> mat.rotateX(rightLegXRot);
            case "left_leg" -> mat.rotateX(leftLegXRot);
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }

        for (var child : part.children.entrySet()) {
            Matrix4d childMat = new Matrix4d(mat);
            childMat.translate(child.getValue().localPivot.x, child.getValue().localPivot.y, child.getValue().localPivot.z);

            var cip = child.getValue().initialPose;
            if (cip.xRot() != 0 || cip.yRot() != 0 || cip.zRot() != 0)
                childMat.rotateZYX(cip.zRot(), cip.yRot(), cip.xRot());
            if (cip.xScale() != 1 || cip.yScale() != 1 || cip.zScale() != 1)
                childMat.scale(cip.xScale(), cip.yScale(), cip.zScale());

            renderPart(
                    pipeline, child.getValue(), child.getKey(), childMat,
                    headYaw, headPitch, headZRot, rightLegXRot, leftLegXRot,
                    block, sky
            );
        }
    }

    protected void renderAdditionalLayers(RenderPipeline pipeline, T entity, Matrix4d base,
                                          double headYaw, double headPitch, double headZRot,
                                          double rightLegXRot, double leftLegXRot,
                                          double block, double sky) {

    }
}
