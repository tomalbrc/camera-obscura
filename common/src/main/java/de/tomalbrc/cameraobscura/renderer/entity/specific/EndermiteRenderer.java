package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public class EndermiteRenderer implements LivingEntityRenderer<Endermite> {

    private static final String TEXTURE = "entity/endermite/endermite";
    private static final int TEX_WIDTH = 64;
    private static final int TEX_HEIGHT = 32;

    private static final int BODY_COUNT = 4;
    private static final int[][] BODY_SIZES = {
            {4, 3, 2}, {6, 4, 5}, {3, 3, 1}, {1, 2, 1}
    };
    private static final int[][] BODY_TEXS = {
            {0, 0}, {0, 5}, {0, 14}, {0, 18}
    };

    private ModelBakery.BakedPart cachedRoot;

    @Override
    public ModelBakery.BakedPart buildRoot(Endermite entity) {
        if (cachedRoot == null) {
            ModelBakery bakery = new ModelBakery(TEXTURE, TEX_WIDTH, TEX_HEIGHT);
            ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
            ModelBakery.PartDefinition root = model.root();

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
                if (i < BODY_COUNT - 1) {
                    placement += (BODY_SIZES[i][2] + BODY_SIZES[i + 1][2]) * 0.5F;
                }
            }

            cachedRoot = root.bake();
        }
        return cachedRoot;
    }

    @Override
    public void render(RenderPipeline pipeline, Endermite entity) {
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
            segYRots[i] = Mth.cos(ageInTicks * 0.9F + i * 0.15F * Mth.PI) * Mth.PI * 0.01F * (1.0F + factor);
            segXOffs[i] = Mth.sin(ageInTicks * 0.9F + i * 0.15F * Mth.PI) * Mth.PI * 0.1F * factor;
        }

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderPart(pipeline, buildRoot(entity), "root", base, segYRots, segXOffs, block, sky);
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name, Matrix4d parent, double[] segYRots, double[] segXOffs, double block, double sky) {

        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        if (name.startsWith("segment")) {
            int idx = Integer.parseInt(name.substring(7));
            if (segXOffs[idx] != 0) mat.translate(segXOffs[idx] / 16f, 0, 0);
            if (segYRots[idx] != 0) mat.rotateY(segYRots[idx]);
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
            renderPart(pipeline, child.getValue(), child.getKey(), mat, segYRots, segXOffs, block, sky);
        }
    }
}