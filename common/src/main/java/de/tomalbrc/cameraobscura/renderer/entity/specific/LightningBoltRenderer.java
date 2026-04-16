package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.EntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LightningBolt;
import org.joml.Matrix4d;

import java.util.List;

public class LightningBoltRenderer implements EntityRenderer<LightningBolt> {
    private static final int NUM_MODELS = 8;
    private static final List<ModelBakery.BakedPart> MODELS = new ObjectArrayList<>(NUM_MODELS);
    private static final int COLOR = 0xFaFaFF;

    static {
        RandomSource random = RandomSource.createThreadLocalInstance(42L);
        for (int m = 0; m < NUM_MODELS; m++) {
            float[] xOffs = new float[8];
            float[] zOffs = new float[8];
            float x = 0, z = 0;
            for (int h = 7; h >= 0; h--) {
                xOffs[h] = x;
                zOffs[h] = z;
                x += random.nextInt(11) - 5;
                z += random.nextInt(11) - 5;
            }
            MODELS.add(buildBoltModel(xOffs, zOffs));
        }
    }

    private static ModelBakery.BakedPart buildBoltModel(float[] xOffs, float[] zOffs) {
        ModelBakery bakery = new ModelBakery("block/white_concrete", 1, 1);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        float thickness = 0.05f;
        for (int i = 0; i < 7; i++) {
            float x0 = xOffs[i];
            float z0 = zOffs[i];
            float x1 = xOffs[i + 1];
            float z1 = zOffs[i + 1];
            float dx = x1 - x0;
            float dz = z1 - z0;
            float len = Mth.sqrt(dx * dx + dz * dz);
            float yaw = (float) Mth.atan2(dx, dz);

            root.addOrReplaceChild(
                    "seg" + i,
                    ModelBakery.CubeListBuilder.create()
                            .addBox(-thickness / 2f, 0, 0, thickness, 16, len),
                    ModelBakery.PartPose.offsetAndRotation(x0, i * 16, z0, 0, yaw, 0)
            );
        }
        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, LightningBolt entity) {
        long seed = entity.seed;
        int idx = (int) ((seed ^ (seed >>> 16)) & 0x7FFFFFFF) % NUM_MODELS;
        ModelBakery.BakedPart boltModel = MODELS.get(idx);

        Matrix4d transform = new Matrix4d()
                .translate(entity.position().toVector3f());

        renderPart(pipeline, boltModel, transform);
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part, Matrix4d parent) {
        Matrix4d mat = new Matrix4d(parent);
        mat.scale(2);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh), mat, IntList.of(COLOR)));
        }
        for (var child : part.children.entrySet()) {
            renderPart(pipeline, child.getValue(), mat);
        }
    }
}