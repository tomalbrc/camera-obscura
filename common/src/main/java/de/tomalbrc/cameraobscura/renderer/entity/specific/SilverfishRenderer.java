package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public class SilverfishRenderer implements LivingEntityRenderer<Silverfish> {
    private static final String TEXTURE = "entity/silverfish/silverfish";
    private static final int TEX_WIDTH = 64;
    private static final int TEX_HEIGHT = 32;

    private static final int BODY_COUNT = 7;
    private static final int[][] BODY_SIZES = {
            {3, 2, 2}, {4, 3, 2}, {6, 4, 3}, {3, 3, 3}, {2, 2, 3}, {2, 1, 2}, {1, 1, 2}
    };
    private static final int[][] BODY_TEXS = {
            {0, 0}, {0, 4}, {0, 9}, {0, 16}, {0, 22}, {11, 0}, {13, 4}
    };

    private ModelBakery.BakedPart cachedRoot;

    @Override
    public ModelBakery.BakedPart buildRoot(Silverfish entity) {
        if (cachedRoot == null) {
            ModelBakery bakery = new ModelBakery(TEXTURE, TEX_WIDTH, TEX_HEIGHT);
            ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
            ModelBakery.PartDefinition root = model.root();

            float[] zPlacement = new float[BODY_COUNT];
            float placement = -3.5F;

            for (int i = 0; i < BODY_COUNT; i++) {
                root.addOrReplaceChild(
                        "segment" + i,
                        ModelBakery.CubeListBuilder.create()
                                .texOffs(BODY_TEXS[i][0], BODY_TEXS[i][1])
                                .addBox(BODY_SIZES[i][0] * -0.5F, 0.0F, BODY_SIZES[i][2] * -0.5F,
                                        BODY_SIZES[i][0], BODY_SIZES[i][1], BODY_SIZES[i][2]),
                        ModelBakery.PartPose.offset(0.0F, 24.0F - BODY_SIZES[i][1], placement)
                );
                zPlacement[i] = placement;
                if (i < BODY_COUNT - 1) {
                    placement += (BODY_SIZES[i][2] + BODY_SIZES[i + 1][2]) * 0.5F;
                }
            }

            root.addOrReplaceChild("layer0",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(20, 0).addBox(-5.0F, 0.0F, BODY_SIZES[2][2] * -0.5F, 10.0F, 8.0F, BODY_SIZES[2][2]),
                    ModelBakery.PartPose.offset(0.0F, 16.0F, zPlacement[2])
            );
            root.addOrReplaceChild("layer1",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(20, 11).addBox(-3.0F, 0.0F, BODY_SIZES[4][2] * -0.5F, 6.0F, 4.0F, BODY_SIZES[4][2]),
                    ModelBakery.PartPose.offset(0.0F, 20.0F, zPlacement[4])
            );
            root.addOrReplaceChild("layer2",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(20, 18).addBox(-3.0F, 0.0F, BODY_SIZES[4][2] * -0.5F, 6.0F, 5.0F, BODY_SIZES[1][2]),
                    ModelBakery.PartPose.offset(0.0F, 19.0F, zPlacement[1])
            );

            cachedRoot = root.bake();
        }
        return cachedRoot;
    }

    @Override
    public void render(RenderPipeline pipeline, Silverfish entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyYaw);

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + 1.5f, pos.z)
                .rotateY(modelYaw)
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        double ageInTicks = entity.tickCount + 1.0f;

        double[] segYRots = new double[BODY_COUNT];
        double[] segXOffs = new double[BODY_COUNT];
        for (int i = 0; i < BODY_COUNT; i++) {
            double factor = Mth.abs(i - 2);
            segYRots[i] = Mth.cos(ageInTicks * 0.9F + i * 0.15F * Mth.PI) * Mth.PI * 0.05F * (1.0F + factor);
            segXOffs[i] = Mth.sin(ageInTicks * 0.9F + i * 0.15F * Mth.PI) * Mth.PI * 0.2F * factor;
        }

        double[] layerYRots = {segYRots[2], segYRots[4], segYRots[1]};
        double[] layerXOffs = {0.0F, segXOffs[4], segXOffs[1]};

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderPart(pipeline, buildRoot(entity), "root", base,
                segYRots, segXOffs, layerYRots, layerXOffs, block, sky);
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part,
                            String name, Matrix4d parent,
                            double[] segYRots, double[] segXOffs,
                            double[] layerYRots, double[] layerXOffs,
                            double block, double sky) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        if (name.startsWith("segment")) {
            int idx = Integer.parseInt(name.substring(7));
            if (segXOffs[idx] != 0) mat.translate(segXOffs[idx] / 16f, 0, 0);
            if (segYRots[idx] != 0) mat.rotateY(segYRots[idx]);
        } else if (name.startsWith("layer")) {
            int idx = Integer.parseInt(name.substring(5));
            if (layerXOffs[idx] != 0) mat.translate(layerXOffs[idx] / 16f, 0, 0);
            if (layerYRots[idx] != 0) mat.rotateY(layerYRots[idx]);
        }

        var ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }

        for (var child : part.children.entrySet()) {
            renderPart(
                    pipeline, child.getValue(), child.getKey(), mat,
                    segYRots, segXOffs, layerYRots, layerXOffs,
                    block, sky);
        }
    }
}