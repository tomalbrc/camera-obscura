package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.EntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

import java.util.HashMap;
import java.util.Map;

public class PaintingRenderer implements EntityRenderer<Painting> {
    private static final Map<CacheKey, ModelBakery.BakedPart> MODEL_CACHE = new HashMap<>();

    @Override
    public void render(RenderPipeline pipeline, Painting entity) {
        PaintingVariant variant = entity.getVariant().value();

        ModelBakery.BakedPart baked = getOrCreateModel(variant);
        Direction direction = entity.getDirection();

        var pos = entity.position();
        double yaw = switch (direction) {
            case NORTH -> 180.0f;
            case WEST -> 90.0f;
            case EAST -> 270.0f;
            default -> 0.0f;
        };

        Matrix4d transform = new Matrix4d()
                .translate(pos.x, pos.y, pos.z + 0.03125f)
                .rotateY(Mth.PI + Mth.DEG_TO_RAD * yaw)
                .rotateX(Mth.PI);

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderPart(pipeline, baked, transform, block, sky);
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part, Matrix4d parent, double block, double sky) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }

        for (var child : part.children.entrySet()) {
            renderPart(pipeline, child.getValue(), mat, block, sky);
        }
    }

    private ModelBakery.BakedPart getOrCreateModel(PaintingVariant variant) {
        CacheKey key = new CacheKey(variant.width(), variant.height(), variant.assetId());
        return MODEL_CACHE.computeIfAbsent(key, k -> buildModel(variant));
    }

    private ModelBakery.BakedPart buildModel(PaintingVariant variant) {
        Identifier asset = variant.assetId().withPrefix("painting/");
        String texPath = asset.toString();

        ModelBakery bakery = new ModelBakery(texPath, 16 * variant.width(), 16 * variant.height());

        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        float w = variant.width() * 16f;
        float h = variant.height() * 16f;
        float halfW = w / 2.0f;
        float halfH = h / 2.0f;

        root.addOrReplaceChild("painting",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-halfW, -halfH, 0.0f, w, h, 0.0f),
                ModelBakery.PartPose.offset(0.0f, 0.0f, 0.0f));

        return root.bake();
    }

    private record CacheKey(int width, int height, Identifier assetId) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CacheKey key)) return false;
            return width == key.width && height == key.height && assetId.equals(key.assetId);
        }

    }
}