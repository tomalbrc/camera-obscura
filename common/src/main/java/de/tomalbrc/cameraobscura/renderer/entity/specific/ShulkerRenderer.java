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
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4d;
import org.joml.Quaterniond;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class ShulkerRenderer implements EntityRenderer<Shulker> {
    private static final String DEFAULT_TEXTURE = "entity/shulker/shulker";

    private static final Map<DyeColor, String> COLOR_TEXTURES = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor color : DyeColor.values()) {
            COLOR_TEXTURES.put(color, "entity/shulker/shulker_" + color.getSerializedName());
        }
    }

    private final Map<String, ModelBakery.BakedPart> modelCache = new java.util.HashMap<>();

    private ModelBakery.BakedPart getModel(DyeColor color) {
        String tex = color == null ? DEFAULT_TEXTURE : COLOR_TEXTURES.getOrDefault(color, DEFAULT_TEXTURE);
        return modelCache.computeIfAbsent(tex, this::buildModel);
    }

    private ModelBakery.BakedPart buildModel(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 64, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("lid",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-8.0f, -16.0f, -8.0f, 16.0f, 12.0f, 16.0f),
                ModelBakery.PartPose.offset(0.0f, 24.0f, 0.0f));
        root.addOrReplaceChild("base",
                ModelBakery.CubeListBuilder.create().texOffs(0, 28).addBox(-8.0f, -8.0f, -8.0f, 16.0f, 8.0f, 16.0f),
                ModelBakery.PartPose.offset(0.0f, 24.0f, 0.0f));
        root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create().texOffs(0, 52).addBox(-3.0f, 0.0f, -3.0f, 6.0f, 6.0f, 6.0f),
                ModelBakery.PartPose.offset(0.0f, 12.0f, 0.0f));

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, Shulker entity) {
        DyeColor color = entity.getColor();
        ModelBakery.BakedPart bakedRoot = getModel(color);

        double peek = entity.getClientPeekAmount(1.0f);
        double age = entity.tickCount + 1.0f;
        double yHeadRot = entity.yHeadRot;
        double yBodyRot = entity.yBodyRot;
        double headYaw = (yHeadRot - 180.0f - yBodyRot) * Mth.DEG_TO_RAD;
        double headPitch = entity.getXRot(1.0f) * Mth.DEG_TO_RAD;

        double bs = (0.5f + peek) * Math.PI;
        double q = -1.0f + Mth.sin(bs);
        double extra = 0.0f;
        if (bs > Math.PI) {
            extra = Mth.sin(age * 0.1f) * 0.7f;
        }
        double lidYOffset = (16.0f + Mth.sin(bs) * 8.0f + extra) - 24.0f;
        double lidYRot = 0.0f;
        if (peek > 0.3f) {
            lidYRot = q * q * q * q * Math.PI * 0.125f;
        }

        Vec3 renderOffset = Objects.requireNonNullElse(entity.getRenderPosition(1.0f), Vec3.ZERO);
        Vec3 pos = entity.position().add(renderOffset);

        Direction attachFace = entity.getAttachFace();
        Direction targetDir = attachFace.getOpposite();
        Quaterniond attachRotation = new Quaterniond().rotateTo(0f, 1f, 0f,
                targetDir.getStepX(), targetDir.getStepY(), targetDir.getStepZ());


        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y, pos.z)

                .translate(0.0f, 0.5f, 0.0f)
                .rotate(attachRotation)
                .translate(0.0f, -0.5f, 0.0f)

                .translate(0, 1.51f, 0)
                .rotateY(Mth.DEG_TO_RAD * (180.0f - entity.getYRot(1.0f)))
                .rotateX(Mth.PI);

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderPart(pipeline, bakedRoot.children.get("lid"), "lid", base, lidYOffset, lidYRot, 0, 0, block, sky);
        renderPart(pipeline, bakedRoot.children.get("base"), "base", base, 0, 0, 0, 0, block, sky);
        renderPart(pipeline, bakedRoot.children.get("head"), "head", base, 0, 0, headYaw, headPitch, block, sky);
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                            Matrix4d parent, double yOffset, double yRot,
                            double headYaw, double headPitch,
                            double block, double sky) {
        if (part == null) return;

        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        if (name.equals("lid")) {
            mat.translate(0.0f, yOffset / 16.0f, 0.0f);
            if (yRot != 0.0f) mat.rotateY(yRot);
        } else if (name.equals("head")) {
            mat.rotateY(headYaw);
            mat.rotateX(headPitch);
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }

        for (var child : part.children.entrySet()) {
            renderPart(pipeline, child.getValue(), child.getKey(), mat, 0, 0, 0, 0, block, sky);
        }
    }
}