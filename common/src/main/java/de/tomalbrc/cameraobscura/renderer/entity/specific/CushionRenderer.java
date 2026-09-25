package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.EntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.Cushion;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

import java.util.EnumMap;
import java.util.Map;

public class CushionRenderer implements EntityRenderer<Cushion> {
    private static final Map<DyeColor, ModelBakery.BakedPart> CACHE = new EnumMap<>(DyeColor.class);

    private static ModelBakery.BakedPart getModel(DyeColor color) {
        return CACHE.computeIfAbsent(color, CushionRenderer::buildModel);
    }

    private static ModelBakery.BakedPart buildModel(DyeColor color) {
        String texture = "entity/cushion/" + color.getName() + "_cushion";
        ModelBakery bakery = new ModelBakery(texture, 64, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild(
                "cushion",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-31.0F, -4.0F, -1.0F, 16.0F, 4.0F, 16.0F,
                                new ModelBakery.CubeDeformation(-0.005F)),
                ModelBakery.PartPose.offset(23.0F, 4.0F, -7.0F)
        );

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, Cushion entity) {
        Direction direction = Direction.fromYRot(entity.getYRot());
        var pos = entity.position();

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y, pos.z)
                .rotateY(Mth.DEG_TO_RAD * (180f - direction.toYRot()))
                .rotateX(Mth.PI)
                .translate(0.0, -0.25, 0.0);

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderPart(pipeline, getModel(entity.getColor()), base, block, sky);
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part, Matrix4d parent,
                            double block, double sky) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(
                    RenderType.ENTITY,
                    new Model(part.mesh, block, sky),
                    mat,
                    IntList.of(0xFFFFFFFF)
            ));
        }
        for (var child : part.children.entrySet()) {
            renderPart(pipeline, child.getValue(), mat, block, sky);
        }
    }
}