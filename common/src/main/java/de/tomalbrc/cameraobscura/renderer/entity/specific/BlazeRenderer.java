package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Blaze;
import org.joml.Matrix4d;

public class BlazeRenderer implements LivingEntityRenderer<Blaze> {
    private final ModelBakery.BakedPart[] RODS = new ModelBakery.BakedPart[12];
    private ModelBakery.BakedPart CACHED_ROOT;

    @Override
    public ModelBakery.BakedPart buildRoot(Blaze blaze) {
        if (CACHED_ROOT == null) {
            CACHED_ROOT = buildParts();
            for (int i = 0; i < 12; i++) {
                RODS[i] = CACHED_ROOT.children.get("part" + i);
            }
        }
        return CACHED_ROOT;
    }

    private ModelBakery.BakedPart buildParts() {
        ModelBakery bakery = new ModelBakery("entity/blaze/blaze", 64, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition rootDef = model.root();

        rootDef.addOrReplaceChild(
                "head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                ModelBakery.PartPose.ZERO
        );

        ModelBakery.CubeListBuilder rodBuilder = ModelBakery.CubeListBuilder.create()
                .texOffs(0, 16)
                .addBox(0.0F, 0.0F, 0.0F, 2.0F, 8.0F, 2.0F);

        for (int i = 0; i < 12; i++) {
            rootDef.addOrReplaceChild(
                    "part" + i,
                    rodBuilder,
                    ModelBakery.PartPose.ZERO
            );
        }

        return rootDef.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, Blaze blaze) {
        ModelBakery.BakedPart root = buildRoot(blaze);

        var pos = blaze.position();
        double bodyYawDeg = blaze.getPreciseBodyRotation(1.f);
        double modelYawRad = Mth.DEG_TO_RAD * (180f - bodyYawDeg);

        double headYawRad = Mth.DEG_TO_RAD * (blaze.getYHeadRot() - bodyYawDeg);
        double headPitchRad = Mth.DEG_TO_RAD * blaze.getXRot(1.f);

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + 1.5f, pos.z)
                .rotateY(modelYawRad)
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        double age = blaze.tickCount;

        renderHead(pipeline, root.children.get("head"), base, headYawRad, headPitchRad);

        for (int i = 0; i < 12; i++) {
            ModelBakery.BakedPart rod = RODS[i];
            if (rod.mesh == null) continue;

            double x, y, z;
            if (i < 4) {
                double angle = age * Mth.PI * -0.1F + i;
                y = -2.0F + Mth.cos((i * 2 + age) * 0.25F);
                x = Mth.cos(angle) * 9.0F;
                z = Mth.sin(angle) * 9.0F;
            } else if (i < 8) {
                double angle = Mth.PI / 4 + age * Mth.PI * 0.03F + (i - 4);
                y = 2.0F + Mth.cos((i * 2 + age) * 0.25F);
                x = Mth.cos(angle) * 7.0F;
                z = Mth.sin(angle) * 7.0F;
            } else {
                double angle = 0.47123894F + age * Mth.PI * -0.05F + (i - 8);
                y = 11.0F + Mth.cos((i * 1.5F + age) * 0.5F);
                x = Mth.cos(angle) * 5.0F;
                z = Mth.sin(angle) * 5.0F;
            }

            Matrix4d mat = new Matrix4d(base)
                    .translate(x / 16f, y / 16f, z / 16f);

            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(rod.mesh), mat, IntList.of(0xFFFFFFFF)));
        }
    }

    private void renderHead(RenderPipeline pipeline, ModelBakery.BakedPart part, Matrix4d parentMat,
                            double yaw, double pitch) {
        Matrix4d mat = new Matrix4d(parentMat);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose pose = part.initialPose;
        if (pose.xRot() != 0 || pose.yRot() != 0 || pose.zRot() != 0)
            mat.rotateZYX(pose.zRot(), pose.yRot(), pose.xRot());
        if (pose.xScale() != 1 || pose.yScale() != 1 || pose.zScale() != 1)
            mat.scale(pose.xScale(), pose.yScale(), pose.zScale());

        mat.rotateY(yaw);
        mat.rotateX(pitch);

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh), mat, IntList.of(0xFFFFFFFF)));
        }

        for (var child : part.children.entrySet()) {
            renderHead(pipeline, child.getValue(), mat, yaw, pitch);
        }
    }
}