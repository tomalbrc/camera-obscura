package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.fish.Cod;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public class CodRenderer implements LivingEntityRenderer<Cod> {

    private static final String TEXTURE = "entity/fish/cod";
    private static final int TEX_WIDTH = 32;
    private static final int TEX_HEIGHT = 32;

    private ModelBakery.BakedPart cachedRoot;

    @Override
    public ModelBakery.BakedPart buildRoot(Cod entity) {
        if (cachedRoot == null) {
            ModelBakery bakery = new ModelBakery(TEXTURE, TEX_WIDTH, TEX_HEIGHT);
            ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
            ModelBakery.PartDefinition root = model.root();

            root.addOrReplaceChild("body",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(0, 0).addBox(-1, -2, 0, 2, 4, 7),
                    ModelBakery.PartPose.offset(0, 22, 0));

            root.addOrReplaceChild("head",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(11, 0).addBox(-1, -2, -3, 2, 4, 3),
                    ModelBakery.PartPose.offset(0, 22, 0));

            root.addOrReplaceChild("nose",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(0, 0).addBox(-1, -2, -1, 2, 3, 1),
                    ModelBakery.PartPose.offset(0, 22, -3));

            root.addOrReplaceChild("right_fin",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(22, 1).addBox(-2, 0, -1, 2, 0, 2),
                    ModelBakery.PartPose.offsetAndRotation(-1, 23, 0, 0, 0, -Mth.PI / 4));

            root.addOrReplaceChild("left_fin",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(22, 4).addBox(0, 0, -1, 2, 0, 2),
                    ModelBakery.PartPose.offsetAndRotation(1, 23, 0, 0, 0, Mth.PI / 4));

            root.addOrReplaceChild("tail_fin",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(22, 3).addBox(0, -2, 0, 0, 4, 4),
                    ModelBakery.PartPose.offset(0, 22, 7));

            root.addOrReplaceChild("top_fin",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(20, -6).addBox(0, -1, -1, 0, 1, 6),
                    ModelBakery.PartPose.offset(0, 20, 0));

            cachedRoot = root.bake();
        }
        return cachedRoot;
    }

    @Override
    public void render(RenderPipeline pipeline, Cod entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);

        double ageInTicks = entity.tickCount + 1.0f;
        double bodyZRotDeg = 4.3f * Mth.sin(0.6f * ageInTicks);
        double bodyZRotRad = bodyZRotDeg * Mth.DEG_TO_RAD;

        boolean inWater = entity.isInWater();

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y, pos.z)
                .rotateY(Mth.DEG_TO_RAD * (180f - bodyYaw))
                .rotateY(bodyZRotRad);

        if (!inWater) {
            base.translate(0.1f, 0.1f, -0.1f);
            base.rotateZ(Mth.PI / 2);
        }

        base.translate(0, 1.5f, 0);
        base.rotateY(Mth.PI);
        base.rotateX(Mth.PI);

        double amplitudeMultiplier = inWater ? 1.0f : 1.5f;
        double tailFinYRot = -amplitudeMultiplier * 0.45f * Mth.sin(0.6f * ageInTicks);


        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderPart(pipeline, buildRoot(entity), "root", base, tailFinYRot, block, sky);
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part,
                            String name, Matrix4d parent, double tailFinYRot,
                            double block, double sky) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        if (name.equals("tail_fin")) {
            mat.rotateY(tailFinYRot);
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
            renderPart(pipeline, child.getValue(), child.getKey(), mat, tailFinYRot, block, sky);
        }
    }
}