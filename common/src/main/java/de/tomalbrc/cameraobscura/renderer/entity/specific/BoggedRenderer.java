package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.skeleton.Bogged;

public class BoggedRenderer extends SkeletonRenderer {

    private static final String TEXTURE = "entity/skeleton/bogged";

    private ModelBakery.BakedPart cachedRootSheared;
    private ModelBakery.BakedPart cachedRootNormal;

    @Override
    protected String getTexturePath(AbstractSkeleton e) {
        return TEXTURE;
    }

    @Override
    public ModelBakery.BakedPart buildRoot(AbstractSkeleton entity) {
        if (entity instanceof Bogged bogged) {
            boolean sheared = bogged.isSheared();
            if (sheared) {
                if (cachedRootSheared == null) cachedRootSheared = buildModel(false);
                return cachedRootSheared;
            } else {
                if (cachedRootNormal == null) cachedRootNormal = buildModel(true);
                return cachedRootNormal;
            }
        }
        return super.buildRoot(entity);
    }

    private ModelBakery.BakedPart buildModel(boolean withMushrooms) {
        ModelBakery bakery = new ModelBakery(TEXTURE, getTexWidth(), getTexHeight());
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        var head = root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8),
                ModelBakery.PartPose.offset(0, 0, 0));
        head.addOrReplaceChild("hat",
                ModelBakery.CubeListBuilder.create().texOffs(32, 0).addBox(-4, -8, -4, 8, 8, 8, new ModelBakery.CubeDeformation(0.5F)),
                ModelBakery.PartPose.ZERO);

        root.addOrReplaceChild("body", ModelBakery.CubeListBuilder.create().texOffs(16, 16).addBox(-4, 0, -2, 8, 12, 4), ModelBakery.PartPose.offset(0, 0, 0));
        root.addOrReplaceChild("right_arm", ModelBakery.CubeListBuilder.create().texOffs(40, 16).addBox(-1, -2, -1, 2, 12, 2), ModelBakery.PartPose.offset(-5, 2, 0));
        root.addOrReplaceChild("left_arm", ModelBakery.CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1, -2, -1, 2, 12, 2), ModelBakery.PartPose.offset(5, 2, 0));
        root.addOrReplaceChild("right_leg", ModelBakery.CubeListBuilder.create().texOffs(0, 16).addBox(-1, 0, -1, 2, 12, 2), ModelBakery.PartPose.offset(-2, 12, 0));
        root.addOrReplaceChild("left_leg", ModelBakery.CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-1, 0, -1, 2, 12, 2), ModelBakery.PartPose.offset(2, 12, 0));

        if (withMushrooms) {
            ModelBakery.PartDefinition mushrooms = head.addOrReplaceChild("mushrooms",
                    ModelBakery.CubeListBuilder.create(), ModelBakery.PartPose.ZERO);

            mushrooms.addOrReplaceChild("red_mushroom_1",
                    ModelBakery.CubeListBuilder.create().texOffs(50, 16).addBox(-3, -3, 0, 6, 4, 0),
                    ModelBakery.PartPose.offsetAndRotation(3, -8, 3, 0, (Mth.PI / 4), 0));
            mushrooms.addOrReplaceChild("red_mushroom_2",
                    ModelBakery.CubeListBuilder.create().texOffs(50, 16).addBox(-3, -3, 0, 6, 4, 0),
                    ModelBakery.PartPose.offsetAndRotation(3, -8, 3, 0, (Mth.PI * 3.0f / 4.0f), 0));

            mushrooms.addOrReplaceChild("brown_mushroom_1",
                    ModelBakery.CubeListBuilder.create().texOffs(50, 22).addBox(-3, -3, 0, 6, 4, 0),
                    ModelBakery.PartPose.offsetAndRotation(-3, -8, -3, 0, (Mth.PI / 4), 0));
            mushrooms.addOrReplaceChild("brown_mushroom_2",
                    ModelBakery.CubeListBuilder.create().texOffs(50, 22).addBox(-3, -3, 0, 6, 4, 0),
                    ModelBakery.PartPose.offsetAndRotation(-3, -8, -3, 0, (Mth.PI * 3.0f / 4.0f), 0));
            mushrooms.addOrReplaceChild("brown_mushroom_3",
                    ModelBakery.CubeListBuilder.create().texOffs(50, 28).addBox(-3, -4, 0, 6, 4, 0),
                    ModelBakery.PartPose.offsetAndRotation(-2, -1, 4, (-Mth.PI / 2), 0, (Mth.PI / 4)));
            mushrooms.addOrReplaceChild("brown_mushroom_4",
                    ModelBakery.CubeListBuilder.create().texOffs(50, 28).addBox(-3, -4, 0, 6, 4, 0),
                    ModelBakery.PartPose.offsetAndRotation(-2, -1, 4, (-Mth.PI / 2), 0, (Mth.PI * 3.0f / 4.0f)));
        }

        return root.bake();
    }
}
