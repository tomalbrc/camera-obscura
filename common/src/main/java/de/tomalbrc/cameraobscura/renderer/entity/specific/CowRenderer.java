package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.platform.Platforms;
import de.tomalbrc.cameraobscura.renderer.entity.FourLeggedRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.cow.CowVariant;
import net.minecraft.world.entity.animal.cow.CowVariants;

import java.util.IdentityHashMap;
import java.util.Map;

public class CowRenderer extends FourLeggedRenderer<Cow> {
    private static final Map<Holder<CowVariant>, ModelBakery.BakedPart> ADULT_CACHE = new IdentityHashMap<>();
    private static final Map<Holder<CowVariant>, ModelBakery.BakedPart> BABY_CACHE = new IdentityHashMap<>();

    @Override
    public ModelBakery.BakedPart buildRoot(Cow cow) {
        Holder<CowVariant> variant = cow.getVariant();
        if (cow.isBaby()) {
            return BABY_CACHE.computeIfAbsent(variant, v -> buildBabyParts(texture(v) + "_baby"));
        } else {
            return ADULT_CACHE.computeIfAbsent(variant, v -> buildAdultParts(texture(v), v));
        }
    }

    protected String texture(Holder<CowVariant> variant) {
        return variant.value().modelAndTexture().asset().id().toString();
    }

    private void addCustom(ModelBakery.PartDefinition root, Holder<CowVariant> variant) {
        if (variant == Platforms.get().getRegistryAccess().getOrThrow(CowVariants.WARM)) {
            root.addOrReplaceChild(
                    "head",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(0, 0)
                            .addBox(-4.0F, -4.0F, -6.0F, 8.0F, 8.0F, 6.0F)
                            .texOffs(1, 33)
                            .addBox(-3.0F, 1.0F, -7.0F, 6.0F, 3.0F, 1.0F)
                            .texOffs(27, 0)
                            .addBox(-8.0F, -3.0F, -5.0F, 4.0F, 2.0F, 2.0F)
                            .texOffs(39, 0)
                            .addBox(-8.0F, -5.0F, -5.0F, 2.0F, 2.0F, 2.0F)
                            .texOffs(27, 0)
                            .mirror()
                            .addBox(4.0F, -3.0F, -5.0F, 4.0F, 2.0F, 2.0F)
                            .mirror(false)
                            .texOffs(39, 0)
                            .mirror()
                            .addBox(6.0F, -5.0F, -5.0F, 2.0F, 2.0F, 2.0F)
                            .mirror(false),
                    ModelBakery.PartPose.offset(0.0F, 4.0F, -8.0F)
            );
        }

        if (variant == Platforms.get().getRegistryAccess().getOrThrow(CowVariants.COLD)) {
            root.addOrReplaceChild(
                    "body",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(20, 32)
                            .addBox(-6.0F, -10.0F, -7.0F, 12.0F, 18.0F, 10.0F, new ModelBakery.CubeDeformation(0.5F))
                            .texOffs(18, 4)
                            .addBox(-6.0F, -10.0F, -7.0F, 12.0F, 18.0F, 10.0F)
                            .texOffs(52, 0)
                            .addBox(-2.0F, 2.0F, -8.0F, 4.0F, 6.0F, 1.0F),
                    ModelBakery.PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, (Mth.PI / 2), 0.0F, 0.0F)
            );

            var head = root.addOrReplaceChild(
                    "head",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(0, 0).addBox(-4.0F, -4.0F, -6.0F, 8.0F, 8.0F, 6.0F)
                            .texOffs(9, 33).addBox(-3.0F, 1.0F, -7.0F, 6.0F, 3.0F, 1.0F),
                    ModelBakery.PartPose.offset(0.0F, 4.0F, -8.0F)
            );
            head.addOrReplaceChild(
                    "h1",
                    ModelBakery.CubeListBuilder.create().texOffs(0, 40).addBox(-1.5F, -4.5F, -0.5F, 2.0F, 6.0F, 2.0F),
                    ModelBakery.PartPose.offsetAndRotation(-4.5F, -2.5F, -3.5F, 1.5708F, 0.0F, 0.0F)
            );
            head.addOrReplaceChild(
                    "h2",
                    ModelBakery.CubeListBuilder.create().texOffs(0, 32).addBox(-1.5F, -3.0F, -0.5F, 2.0F, 6.0F, 2.0F),
                    ModelBakery.PartPose.offsetAndRotation(5.5F, -2.5F, -5.0F, 1.5708F, 0.0F, 0.0F)
            );
        }
    }

    private ModelBakery.BakedPart buildAdultParts(String texture, Holder<CowVariant> variant) {
        ModelBakery bakery = new ModelBakery(texture, 64, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4, -4, -6, 8, 8, 6)
                        .texOffs(1, 33).addBox(-3, 1, -7, 6, 3, 1)
                        .texOffs(22, 0).addBox("right_horn", -5, -5, -5, 1, 3, 1)
                        .texOffs(22, 0).addBox("left_horn", 4, -5, -5, 1, 3, 1),
                ModelBakery.PartPose.offset(0, 4, -8));
        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(18, 4).addBox(-6, -10, -7, 12, 18, 10)
                        .texOffs(52, 0).addBox(-2, 2, -8, 4, 6, 1),
                ModelBakery.PartPose.offsetAndRotation(0, 5, 2, (Mth.PI / 2), 0, 0));

        ModelBakery.CubeListBuilder leftLeg = ModelBakery.CubeListBuilder.create().mirror().texOffs(0, 16).addBox(-2, 0, -2, 4, 12, 4);
        ModelBakery.CubeListBuilder rightLeg = ModelBakery.CubeListBuilder.create().texOffs(0, 16).addBox(-2, 0, -2, 4, 12, 4);
        root.addOrReplaceChild("right_hind_leg", rightLeg, ModelBakery.PartPose.offset(-4, 12, 7));
        root.addOrReplaceChild("left_hind_leg", leftLeg, ModelBakery.PartPose.offset(4, 12, 7));
        root.addOrReplaceChild("right_front_leg", rightLeg, ModelBakery.PartPose.offset(-4, 12, -5));
        root.addOrReplaceChild("left_front_leg", leftLeg, ModelBakery.PartPose.offset(4, 12, -5));

        addCustom(root, variant);

        return root.bake();
    }

    private ModelBakery.BakedPart buildBabyParts(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 64, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 18).addBox(-3.0F, -4.569F, -4.8333F, 6.0F, 6.0F, 5.0F)
                        .texOffs(8, 29).addBox(3.0F, -5.569F, -3.8333F, 1.0F, 2.0F, 1.0F)
                        .texOffs(4, 29).mirror().addBox(-4.0F, -5.569F, -3.8333F, 1.0F, 2.0F, 1.0F).mirror(false)
                        .texOffs(12, 29).addBox(-2.0F, -1.569F, -5.8333F, 4.0F, 3.0F, 1.0F),
                ModelBakery.PartPose.offset(0.0F, 13.569F, -5.1667F));

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -7.0F, -1.0F, 8.0F, 6.0F, 12.0F),
                ModelBakery.PartPose.offset(3.0F, 19.0F, -5.0F));

        root.addOrReplaceChild("right_front_leg",
                ModelBakery.CubeListBuilder.create().texOffs(22, 18).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                ModelBakery.PartPose.offset(-2.5F, 18.0F, -3.5F));
        root.addOrReplaceChild("left_front_leg",
                ModelBakery.CubeListBuilder.create().texOffs(34, 18).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                ModelBakery.PartPose.offset(2.5F, 18.0F, -3.5F));
        root.addOrReplaceChild("right_hind_leg",
                ModelBakery.CubeListBuilder.create().texOffs(22, 27).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                ModelBakery.PartPose.offset(-2.5F, 18.0F, 3.5F));
        root.addOrReplaceChild("left_hind_leg",
                ModelBakery.CubeListBuilder.create().texOffs(34, 27).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                ModelBakery.PartPose.offset(2.5F, 18.0F, 3.5F));

        return root.bake();
    }
}
