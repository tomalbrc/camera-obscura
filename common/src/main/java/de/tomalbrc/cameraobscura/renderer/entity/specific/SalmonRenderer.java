package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.fish.Salmon;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public class SalmonRenderer implements LivingEntityRenderer<Salmon> {
    private static final String TEXTURE = "entity/fish/salmon";
    private static final int TEX_WIDTH = 32;
    private static final int TEX_HEIGHT = 32;

    private ModelBakery.BakedPart cachedRoot;

    @Override
    public ModelBakery.BakedPart buildRoot(Salmon entity) {
        if (cachedRoot == null) {
            ModelBakery bakery = new ModelBakery(TEXTURE, TEX_WIDTH, TEX_HEIGHT);
            ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
            ModelBakery.PartDefinition root = model.root();

            ModelBakery.PartDefinition bodyFront = root.addOrReplaceChild("body_front",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(0, 0).addBox(-1.5f, -2.5f, 0, 3, 5, 8),
                    ModelBakery.PartPose.offset(0, 20, -7.2f));

            ModelBakery.PartDefinition bodyBack = root.addOrReplaceChild("body_back",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(0, 13).addBox(-1.5f, -2.5f, 0, 3, 5, 8),
                    ModelBakery.PartPose.offset(0, 20, 0.8f));

            bodyBack.addOrReplaceChild("back_fin",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(20, 10).addBox(0, -2.5f, 0, 0, 5, 6),
                    ModelBakery.PartPose.offset(0, 0, 8));

            bodyBack.addOrReplaceChild("top_back_fin",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(0, 2).addBox(0, 0, 0, 0, 2, 4),
                    ModelBakery.PartPose.offset(0, -4.5f, -1));

            bodyFront.addOrReplaceChild("top_front_fin",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(2, 1).addBox(0, 0, 0, 0, 2, 3),
                    ModelBakery.PartPose.offset(0, -4.5f, 5));

            root.addOrReplaceChild("head",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(22, 0).addBox(-1, -2, -3, 2, 4, 3),
                    ModelBakery.PartPose.offset(0, 20, -7.2f));

            root.addOrReplaceChild("right_fin",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(-4, 0).addBox(-2, 0, 0, 2, 0, 2),
                    ModelBakery.PartPose.offsetAndRotation(-1.5f, 21.5f, -7.2f, 0, 0, -Mth.PI / 4));

            root.addOrReplaceChild("left_fin",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(0, 0).addBox(0, 0, 0, 2, 0, 2),
                    ModelBakery.PartPose.offsetAndRotation(1.5f, 21.5f, -7.2f, 0, 0, Mth.PI / 4));

            cachedRoot = root.bake();
        }
        return cachedRoot;
    }

    @Override
    public void render(RenderPipeline pipeline, Salmon entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);

        double ageInTicks = entity.tickCount + 1.0f;
        boolean inWater = entity.isInWater();

        double amplitudeMultiplier = inWater ? 1.0f : 1.3f;
        double angleMultiplier = inWater ? 1.0f : 1.7f;

        double bodyZRotDeg = amplitudeMultiplier * 4.3f * Mth.sin(angleMultiplier * 0.6f * ageInTicks);
        double bodyZRotRad = bodyZRotDeg * Mth.DEG_TO_RAD;

        double scale = switch (entity.getVariant()) {
            case SMALL -> 0.5f;
            case LARGE -> 1.5f;
            default -> 1.0f;
        };

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y, pos.z)
                .rotateY(Mth.DEG_TO_RAD * (180f - bodyYaw))
                .rotateY(bodyZRotRad);

        if (!inWater) {
            base.translate(0.2f, 0.1f, 0.0f);
            base.rotateZ(Mth.PI / 2);
        }

        if (scale != 1.0f) {
            base.scale(scale);
        }

        base.translate(0, 1.5f, 0);
        base.rotateY(Mth.PI);
        base.rotateX(Mth.PI);

        double bodyBackYRot = -amplitudeMultiplier * 0.25f * Mth.sin(angleMultiplier * 0.6f * ageInTicks);

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderPart(pipeline, buildRoot(entity), "root", base, bodyBackYRot, block, sky);
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part,
                            String name, Matrix4d parent, double bodyBackYRot,
                            double block, double sky) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        if (name.equals("body_back")) {
            mat.rotateY(bodyBackYRot);
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
            renderPart(pipeline, child.getValue(), child.getKey(), mat, bodyBackYRot, block, sky);
        }
    }
}