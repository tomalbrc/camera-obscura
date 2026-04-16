package de.tomalbrc.cameraobscura.renderer.entity.specific;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import de.tomalbrc.cameraobscura.util.resource.RPHelper;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

import java.util.HashMap;
import java.util.Map;

public class LlamaRenderer implements LivingEntityRenderer<Llama> {

    private static final Map<CacheKey, ModelBakery.BakedPart> CACHE = new HashMap<>();
    private static final Map<String, ModelBakery.BakedPart> DECOR_CACHE = new HashMap<>();

    private static final String TRADER_LLAMA_TEXTURE = "entity/equipment/llama_body/trader_llama";
    private static final String TRADER_LLAMA_BABY_TEXTURE = "entity/equipment/llama_body/trader_llama_baby";

    @Override
    public ModelBakery.BakedPart buildRoot(Llama llama) {
        Llama.Variant variant = llama.getVariant();
        boolean baby = llama.isBaby();
        CacheKey key = new CacheKey(variant, baby);
        return CACHE.computeIfAbsent(key, k -> buildModel(variant, baby));
    }

    private ModelBakery.BakedPart buildModel(Llama.Variant variant, boolean baby) {
        String texture = getTexturePath(variant, baby);
        ModelBakery bakery = new ModelBakery(texture, baby ? 64 : 128, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        if (baby) {
            buildBabyModel(root);
        } else {
            buildAdultModel(root);
        }
        return root.bake();
    }

    private String getTexturePath(Llama.Variant variant, boolean baby) {
        String base = "entity/llama/llama_" + variant.getSerializedName();
        return base + (baby ? "_baby" : "");
    }

    private void buildAdultModel(ModelBakery.PartDefinition root) {
        root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.0F, -14.0F, -10.0F, 4.0F, 4.0F, 9.0F)
                        .texOffs(0, 14).addBox("neck", -4.0F, -16.0F, -6.0F, 8.0F, 18.0F, 6.0F)
                        .texOffs(17, 0).addBox("ear", -4.0F, -19.0F, -4.0F, 3.0F, 3.0F, 2.0F)
                        .texOffs(17, 0).addBox("ear", 1.0F, -19.0F, -4.0F, 3.0F, 3.0F, 2.0F),
                ModelBakery.PartPose.offset(0.0F, 7.0F, -6.0F));

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(29, 0).addBox(-6.0F, -10.0F, -7.0F, 12.0F, 18.0F, 10.0F),
                ModelBakery.PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, Mth.PI / 2, 0.0F, 0.0F));

        root.addOrReplaceChild("right_chest",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(45, 28).addBox(-3.0F, 0.0F, 0.0F, 8.0F, 8.0F, 3.0F),
                ModelBakery.PartPose.offsetAndRotation(-8.5F, 3.0F, 3.0F, 0.0F, Mth.PI / 2, 0.0F));
        root.addOrReplaceChild("left_chest",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(45, 41).addBox(-3.0F, 0.0F, 0.0F, 8.0F, 8.0F, 3.0F),
                ModelBakery.PartPose.offsetAndRotation(5.5F, 3.0F, 3.0F, 0.0F, Mth.PI / 2, 0.0F));

        ModelBakery.CubeListBuilder leg = ModelBakery.CubeListBuilder.create()
                .texOffs(29, 29).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F);
        root.addOrReplaceChild("right_hind_leg", leg, ModelBakery.PartPose.offset(-3.5F, 10.0F, 6.0F));
        root.addOrReplaceChild("left_hind_leg", leg, ModelBakery.PartPose.offset(3.5F, 10.0F, 6.0F));
        root.addOrReplaceChild("right_front_leg", leg, ModelBakery.PartPose.offset(-3.5F, 10.0F, -5.0F));
        root.addOrReplaceChild("left_front_leg", leg, ModelBakery.PartPose.offset(3.5F, 10.0F, -5.0F));
    }

    private void buildBabyModel(ModelBakery.PartDefinition root) {
        root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0F, -9.0F, -4.0F, 6.0F, 11.0F, 4.0F)
                        .texOffs(0, 15).addBox(-1.5F, -7.0F, -7.0F, 3.0F, 3.0F, 3.0F)
                        .texOffs(20, 4).addBox(0.5F, -11.0F, -3.0F, 2.0F, 2.0F, 2.0F)
                        .texOffs(20, 0).addBox(-2.5F, -11.0F, -3.0F, 2.0F, 2.0F, 2.0F),
                ModelBakery.PartPose.offset(0.0F, 12.0F, -4.0F));

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 15).addBox(-4.0F, -3.0F, -8.5F, 8.0F, 6.0F, 13.0F),
                ModelBakery.PartPose.offset(0.0F, 14.0F, 2.5F));

        root.addOrReplaceChild("right_hind_leg",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 45).addBox(-1.4F, -0.5F, -1.5F, 3.0F, 8.0F, 3.0F),
                ModelBakery.PartPose.offset(-2.5F, 16.5F, 4.5F));
        root.addOrReplaceChild("left_hind_leg",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(12, 45).addBox(-1.6F, -0.5F, -1.5F, 3.0F, 8.0F, 3.0F),
                ModelBakery.PartPose.offset(2.5F, 16.5F, 4.5F));
        root.addOrReplaceChild("right_front_leg",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 34).addBox(-1.4F, -0.5F, -1.5F, 3.0F, 8.0F, 3.0F),
                ModelBakery.PartPose.offset(-2.5F, 16.5F, -3.5F));
        root.addOrReplaceChild("left_front_leg",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(12, 34).addBox(-1.6F, -0.5F, -1.5F, 3.0F, 8.0F, 3.0F),
                ModelBakery.PartPose.offset(2.5F, 16.5F, -3.5F));

        root.addOrReplaceChild("right_chest",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(45, 28).addBox(-3.0F, 0.0F, 0.0F, 8.0F, 8.0F, 3.0F),
                ModelBakery.PartPose.offsetAndRotation(-8.5F, 4.0F, 3.0F, 0.0F, Mth.PI / 2, 0.0F));
        root.addOrReplaceChild("left_chest",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(45, 41).addBox(-3.0F, 0.0F, 0.0F, 8.0F, 8.0F, 3.0F),
                ModelBakery.PartPose.offsetAndRotation(5.5F, 4.0F, 3.0F, 0.0F, Mth.PI / 2, 0.0F));
    }

    @Override
    public void render(RenderPipeline pipeline, Llama entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyYaw);
        double headYawRad = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYaw);
        double headPitchRad = Mth.DEG_TO_RAD * entity.getXRot(1.0f);

        boolean isBaby = entity.isBaby();
        boolean hasChest = !isBaby && entity.hasChest();

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + 1.5f, pos.z)
                .rotateY(modelYaw)
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        double animPos = entity.walkAnimation.position();
        double animSpeed = entity.walkAnimation.speed();
        double limbSwing = animPos * 0.6662F;
        double rhXRot = Mth.cos(limbSwing) * 1.4F * animSpeed;
        double lhXRot = Mth.cos(limbSwing + Mth.PI) * 1.4F * animSpeed;

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        if (!entity.isInvisible()) renderPart(pipeline, buildRoot(entity), "root", base,
                headYawRad, headPitchRad, rhXRot, lhXRot, lhXRot, rhXRot, hasChest, block, sky);

        ModelBakery.BakedPart decorModel = getDecorModel(entity);
        if (decorModel != null) {
            renderPart(pipeline, decorModel, "root", base,
                    headYawRad, headPitchRad, rhXRot, lhXRot, lhXRot, rhXRot, hasChest, block, sky);
        }
    }

    private ModelBakery.BakedPart getDecorModel(Llama llama) {
        ItemStack bodyItem = llama.getBodyArmorItem();
        String texture = null;
        boolean baby = llama.isBaby();

        if (!bodyItem.isEmpty()) {
            Equippable equippable = bodyItem.get(DataComponents.EQUIPPABLE);
            if (equippable != null && equippable.assetId().isPresent()) {
                texture = resolveDecorTexture(equippable.assetId().get());
            }
        } else if (llama.isTraderLlama()) {
            texture = baby ? TRADER_LLAMA_BABY_TEXTURE : TRADER_LLAMA_TEXTURE;
        }

        if (texture == null) return null;

        String finalTexture = texture;
        return DECOR_CACHE.computeIfAbsent(texture + (baby ? "_baby" : ""), k -> buildModelForDecor(finalTexture, baby));
    }

    private String resolveDecorTexture(ResourceKey<EquipmentAsset> key) {
        var assetId = key.identifier();
        var el = RPHelper.getEquipment(assetId);
        if (el == null) return null;

        JsonObject json = el.getAsJsonObject();
        if (json == null) return null;

        JsonObject layers = json.getAsJsonObject("layers");
        if (layers == null) return null;

        JsonArray layerArray = layers.getAsJsonArray("llama_body");
        if (layerArray == null || layerArray.isEmpty()) return null;

        String textureId = layerArray.get(0).getAsJsonObject().get("texture").getAsString();
        Identifier textureLoc = Identifier.parse(textureId);
        return textureLoc.getNamespace() + ":entity/equipment/llama_body/" + textureLoc.getPath();
    }

    private ModelBakery.BakedPart buildModelForDecor(String texture, boolean baby) {
        ModelBakery bakery = new ModelBakery(texture, baby ? 64 : 128, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        if (baby) {
            buildBabyModel(root);
        } else {
            buildAdultModel(root);
        }
        return root.bake();
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part,
                            String name, Matrix4d parent,
                            double headYaw, double headPitch,
                            double legRH, double legLH, double legRF, double legLF,
                            boolean hasChest,
                            double block, double sky) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        var ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        switch (name) {
            case "head" -> {
                mat.rotateX(headPitch);
                mat.rotateY(headYaw);
            }
            case "right_hind_leg" -> mat.rotateX(legRH);
            case "left_hind_leg" -> mat.rotateX(legLH);
            case "right_front_leg" -> mat.rotateX(legRF);
            case "left_front_leg" -> mat.rotateX(legLF);
            case "right_chest", "left_chest" -> {
                if (!hasChest) return;
            }
            default -> {
            }
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }

        for (var child : part.children.entrySet()) {
            renderPart(
                    pipeline, child.getValue(), child.getKey(), mat,
                    headYaw, headPitch, legRH, legLH, legRF, legLF, hasChest,
                    block, sky
            );
        }
    }

    private record CacheKey(Llama.Variant variant, boolean baby) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CacheKey that)) return false;
            return baby == that.baby && variant == that.variant;
        }
    }
}