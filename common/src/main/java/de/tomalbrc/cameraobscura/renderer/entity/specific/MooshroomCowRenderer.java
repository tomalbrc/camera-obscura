package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.BlockStateRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.FourLeggedRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4d;

import java.util.IdentityHashMap;
import java.util.Map;

public class MooshroomCowRenderer extends FourLeggedRenderer<MushroomCow> {

    private final Map<MushroomCow.Variant, ModelBakery.BakedPart> adultCache = new IdentityHashMap<>();
    private final Map<MushroomCow.Variant, ModelBakery.BakedPart> babyCache = new IdentityHashMap<>();

    @Override
    public ModelBakery.BakedPart buildRoot(MushroomCow entity) {
        MushroomCow.Variant variant = entity.getVariant();
        if (entity.isBaby()) {
            return babyCache.computeIfAbsent(variant, v -> buildBabyModel(getTexture(v, true)));
        } else {
            return adultCache.computeIfAbsent(variant, v -> buildAdultModel(getTexture(v, false)));
        }
    }

    private String getTexture(MushroomCow.Variant variant, boolean baby) {
        String name = variant == MushroomCow.Variant.RED ? "mooshroom_red" : "mooshroom_brown";
        return "entity/cow/" + name + (baby ? "_baby" : "");
    }

    private ModelBakery.BakedPart buildAdultModel(String texture) {
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

        return root.bake();
    }

    private ModelBakery.BakedPart buildBabyModel(String texture) {
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

    @Override
    public void render(RenderPipeline pipeline, MushroomCow entity) {
        super.render(pipeline, entity);

        if (!entity.isBaby()) {
            BlockState mushroomBlock = entity.getVariant().getBlockState();
            ModelBakery.BakedPart rootModel = buildRoot(entity);

            var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
            var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

            var pos = entity.position();
            double bodyYawDeg = entity.getPreciseBodyRotation(1.f);
            double modelYawRad = Mth.DEG_TO_RAD * (180f - bodyYawDeg);
            Matrix4d base = new Matrix4d()
                    .translate(pos.x, pos.y, pos.z)
                    .rotateY(modelYawRad);

            base.translate(0, 1.5f, 0);
            base.rotateY(Mth.PI);
            base.rotateX(Mth.PI);

            Matrix4d headMatrix = computePartWorldMatrix(rootModel, "head", base);

            Matrix4d mushroom1 = new Matrix4d(base)
                    .translate(0.2f, -0.35f, 0.5f)
                    .rotateY(Mth.DEG_TO_RAD * -48.0f)
                    .scale(-1, -1, 1)
                    .translate(0, -0.5f, 0);
            BlockStateRenderer.render(pipeline, mushroomBlock, mushroom1, block, sky);

            Matrix4d mushroom2 = new Matrix4d(base)
                    .translate(0.2f, -0.35f, 0.5f)
                    .rotateY(Mth.DEG_TO_RAD * 42.0f)
                    .translate(0.1f, 0, -0.6f)
                    .rotateY(Mth.DEG_TO_RAD * -48.0f)
                    .scale(-1, -1, 1)
                    .translate(0, -0.5f, 0);
            BlockStateRenderer.render(pipeline, mushroomBlock, mushroom2, block, sky);

            if (headMatrix != null) {
                Matrix4d mushroom3 = new Matrix4d(headMatrix)
                        .translate(0, -0.7f, -0.2f)
                        .rotateY(Mth.DEG_TO_RAD * -78.0f)
                        .scale(-1, -1, 1)
                        .translate(0, -0.5f, 0);
                BlockStateRenderer.render(pipeline, mushroomBlock, mushroom3, block, sky);
            }
        }
    }

    private Matrix4d computePartWorldMatrix(ModelBakery.BakedPart root, String targetName, Matrix4d parent) {
        return findPartMatrix(root, "root", parent, targetName);
    }

    private Matrix4d findPartMatrix(ModelBakery.BakedPart part, String name, Matrix4d parent, String target) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);
        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        if (name.equals(target)) {
            return mat;
        }
        for (var child : part.children.entrySet()) {
            Matrix4d result = findPartMatrix(child.getValue(), child.getKey(), mat, target);
            if (result != null) return result;
        }
        return null;
    }
}