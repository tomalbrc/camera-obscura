package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;

import java.util.HashMap;
import java.util.Map;

public class DonkeyRenderer extends AbstractEquineRenderer<AbstractChestedHorse> {
    public static final String DONKEY_ADULT_TEXTURE = "entity/horse/donkey";
    public static final String DONKEY_BABY_TEXTURE = "entity/horse/donkey_baby";
    public static final String MULE_ADULT_TEXTURE = "entity/horse/mule";
    public static final String MULE_BABY_TEXTURE = "entity/horse/mule_baby";

    private final boolean isMule;
    private final Map<String, ModelBakery.BakedPart> cache = new HashMap<>();

    public DonkeyRenderer(boolean isMule) {
        this.isMule = isMule;
    }

    @Override
    protected String getTexture(AbstractChestedHorse entity) {
        if (entity.isBaby()) {
            return isMule ? MULE_BABY_TEXTURE : DONKEY_BABY_TEXTURE;
        } else {
            return isMule ? MULE_ADULT_TEXTURE : DONKEY_ADULT_TEXTURE;
        }
    }

    @Override
    public ModelBakery.BakedPart buildRoot(AbstractChestedHorse entity) {
        String tex = getTexture(entity);
        return cache.computeIfAbsent(tex, key -> {
            if (entity.isBaby()) {
                return buildBaby(key);
            } else {
                return buildAdult(key);
            }
        });
    }

    private ModelBakery.BakedPart buildAdult(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 64, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition body = root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 32).addBox(-5.0F, -8.0F, -17.0F, 10.0F, 10.0F, 22.0F),
                ModelBakery.PartPose.offset(0.0F, 11.0F, 5.0F));

        body.addOrReplaceChild("tail",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(42, 36).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 14.0F, 4.0F),
                ModelBakery.PartPose.offsetAndRotation(0.0F, -5.0F, 2.0F, Mth.PI / 6, 0.0F, 0.0F));

        ModelBakery.CubeListBuilder chestBuilder = ModelBakery.CubeListBuilder.create()
                .texOffs(26, 21).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 8.0F, 3.0F);

        body.addOrReplaceChild("left_chest", chestBuilder,
                ModelBakery.PartPose.offsetAndRotation(6.0F, -8.0F, 0.0F, 0.0F, -Mth.PI / 2, 0.0F));
        body.addOrReplaceChild("right_chest", chestBuilder,
                ModelBakery.PartPose.offsetAndRotation(-6.0F, -8.0F, 0.0F, 0.0F, Mth.PI / 2, 0.0F));

        ModelBakery.PartDefinition headParts = root.addOrReplaceChild("head_parts",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 35).addBox(-2.05F, -6.0F, -2.0F, 4.0F, 12.0F, 7.0F),
                ModelBakery.PartPose.offsetAndRotation(0.0F, 4.0F, -12.0F, Mth.PI / 6, 0.0F, 0.0F));

        ModelBakery.PartDefinition head = headParts.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 13).addBox(-3.0F, -11.0F, -2.0F, 6.0F, 5.0F, 7.0F),
                ModelBakery.PartPose.ZERO);

        headParts.addOrReplaceChild("mane",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(56, 36).addBox(-1.0F, -11.0F, 5.01F, 2.0F, 16.0F, 2.0F),
                ModelBakery.PartPose.ZERO);

        headParts.addOrReplaceChild("upper_mouth",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 25).addBox(-2.0F, -11.0F, -7.0F, 4.0F, 5.0F, 5.0F),
                ModelBakery.PartPose.ZERO);

        head.addOrReplaceChild("left_ear",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 12).addBox(-1.0F, -7.0F, 0.0F, 2.0F, 7.0F, 1.0F),
                ModelBakery.PartPose.offsetAndRotation(1.25F, -10.0F, 4.0F, Mth.PI / 12, 0.0F, Mth.PI / 12));
        head.addOrReplaceChild("right_ear",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 12).mirror().addBox(-1.0F, -7.0F, 0.0F, 2.0F, 7.0F, 1.0F),
                ModelBakery.PartPose.offsetAndRotation(-1.25F, -10.0F, 4.0F, Mth.PI / 12, 0.0F, -Mth.PI / 12));

        ModelBakery.CubeListBuilder leftLeg = ModelBakery.CubeListBuilder.create()
                .texOffs(48, 21).mirror().addBox(-3.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F);
        ModelBakery.CubeListBuilder rightLeg = ModelBakery.CubeListBuilder.create()
                .texOffs(48, 21).addBox(-1.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F);

        root.addOrReplaceChild("left_hind_leg", leftLeg,
                ModelBakery.PartPose.offset(4.0F, 14.0F, 7.0F));
        root.addOrReplaceChild("right_hind_leg", rightLeg,
                ModelBakery.PartPose.offset(-4.0F, 14.0F, 7.0F));
        root.addOrReplaceChild("left_front_leg", leftLeg,
                ModelBakery.PartPose.offset(4.0F, 14.0F, -10.0F));
        root.addOrReplaceChild("right_front_leg", rightLeg,
                ModelBakery.PartPose.offset(-4.0F, 14.0F, -10.0F));

        return root.bake();
    }

    private ModelBakery.BakedPart buildBaby(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 64, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition body = root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 13).addBox(-5.0F, -3.0F, -7.0F, 8.0F, 6.0F, 14.0F),
                ModelBakery.PartPose.offset(1.0F, 14.0F, 0.0F));

        ModelBakery.PartDefinition tail = body.addOrReplaceChild("tail",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(0.0F, -1.5F, 6.5F));
        tail.addOrReplaceChild("tail_r1",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(24, 33).addBox(-2.5F, -1.0F, -0.5F, 3.0F, 3.0F, 8.0F),
                ModelBakery.PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7418F, 0.0F, 0.0F));

        body.addOrReplaceChild("left_hind_leg",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(12, 44).addBox(-2.5F, -1.5F, -1.5F, 3.0F, 8.0F, 3.0F),
                ModelBakery.PartPose.offset(2.25F, 3.5F, 5.25F));
        body.addOrReplaceChild("right_hind_leg",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 44).addBox(-2.5F, -1.5F, -1.5F, 3.0F, 8.0F, 3.0F),
                ModelBakery.PartPose.offset(-2.4F, 3.5F, 5.4F));
        body.addOrReplaceChild("left_front_leg",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(12, 33).addBox(-2.5F, -1.5F, -1.5F, 3.0F, 8.0F, 3.0F),
                ModelBakery.PartPose.offset(2.4F, 3.5F, -5.3F));
        body.addOrReplaceChild("right_front_leg",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 33).addBox(-2.5F, -1.5F, -1.5F, 3.0F, 8.0F, 3.0F),
                ModelBakery.PartPose.offset(-2.4F, 3.5F, -5.4F));

        body.addOrReplaceChild("left_chest", ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(-1.0F, 10.0F, 0.0F));
        body.addOrReplaceChild("right_chest", ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(-1.0F, 10.0F, 0.0F));

        ModelBakery.PartDefinition neck = body.addOrReplaceChild("head_parts",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(0.0F, -3.0F, -5.0F));
        neck.addOrReplaceChild("neck_r1",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(30, 9).addBox(-3.0F, -6.0F, -3.0F, 4.0F, 8.0F, 4.0F),
                ModelBakery.PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.0F));

        ModelBakery.PartDefinition head = neck.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(0.0F, -5.0F, -3.0F));
        head.addOrReplaceChild("head_r1",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -3.6F, -8.4F, 6.0F, 4.0F, 9.0F),
                ModelBakery.PartPose.offsetAndRotation(0.0F, -1.0F, 1.0F, 0.3927F, 0.0F, 0.0F));

        head.addOrReplaceChild("left_ear",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.0F, -6.5F, -0.3F, 2.0F, 7.0F, 1.0F),
                ModelBakery.PartPose.offsetAndRotation(2.0F, -3.5F, -1.0F, 0.48F, 0.0F, 0.48F));
        head.addOrReplaceChild("right_ear",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(22, 0).mirror().addBox(-2.0F, -6.5F, -0.3F, 2.0F, 7.0F, 1.0F).mirror(false),
                ModelBakery.PartPose.offsetAndRotation(-2.0F, -3.5F, -1.0F, 0.48F, 0.0F, -0.48F));

        return root.bake();
    }
}