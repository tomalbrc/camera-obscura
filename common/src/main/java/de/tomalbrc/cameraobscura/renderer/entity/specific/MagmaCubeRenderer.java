package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.MagmaCube;
import org.joml.Matrix4d;

public class MagmaCubeRenderer implements LivingEntityRenderer<MagmaCube> {
    private static final String TEXTURE = "entity/slime/magmacube";
    private ModelBakery.BakedPart cachedModel;

    @Override
    public ModelBakery.BakedPart buildRoot(MagmaCube entity) {
        if (cachedModel == null) {
            cachedModel = buildModel();
        }
        return cachedModel;
    }

    private ModelBakery.BakedPart buildModel() {
        ModelBakery bakery = new ModelBakery(TEXTURE, 64, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        for (int i = 0; i < 8; i++) {
            int u = 0, v = 0;
            if (i > 0 && i < 4) {
                v = 9 * i;
            } else if (i > 3) {
                u = 32;
                v = 9 * i - 36;
            }
            root.addOrReplaceChild("cube" + i,
                    ModelBakery.CubeListBuilder.create().texOffs(u, v)
                            .addBox(-4.0f, 16 + i, -4.0f, 8.0f, 1.0f, 8.0f),
                    ModelBakery.PartPose.ZERO);
        }

        root.addOrReplaceChild("inside_cube",
                ModelBakery.CubeListBuilder.create().texOffs(24, 40)
                        .addBox(-2.0f, 18.0f, -2.0f, 4.0f, 4.0f, 4.0f),
                ModelBakery.PartPose.ZERO);

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, MagmaCube entity) {
        double squish = Mth.lerp(1.0f, entity.oSquish, entity.squish);
        int size = entity.getSize();

        double ss = squish / (size * 0.5f + 1.0f);
        double w = 1.0f / (ss + 1.0f);
        double scaleXZ = w * size;
        double scaleY = (1.0f / w) * size;

        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + 1.5f, pos.z)
                .scale(scaleXZ, scaleY, scaleXZ)
                .rotateY(Mth.DEG_TO_RAD * (180f - bodyYaw))
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        ModelBakery.BakedPart root = buildRoot(entity);

        for (var entry : root.children.entrySet()) {
            renderPart(pipeline, entry.getValue(), entry.getKey(), base, squish);
        }
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                            Matrix4d parent, double squish) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());


        if (name.startsWith("cube")) {
            int idx = Integer.parseInt(name.substring(4));

            double deltaY = -(4 - idx) * squish * 1.7f;
            mat.translate(0.0f, deltaY / 16.0f, 0.0f);
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, 0.8f, 0.8f), mat, IntList.of(0xFFFFFFFF)));
        }
        for (var child : part.children.entrySet()) {
            renderPart(pipeline, child.getValue(), child.getKey(), mat, squish);
        }
    }
}