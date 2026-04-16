package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.animal.wolf.WolfVariant;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class WolfRenderer implements LivingEntityRenderer<Wolf> {

    private static final Map<String, ModelBakery.BakedPart> CACHE = new HashMap<>();
    private static final Map<String, ModelBakery.BakedPart> COLLAR_CACHE = new HashMap<>();
    private static final Map<String, ModelBakery.BakedPart> ARMOR_CACHE = new HashMap<>();

    private static final Identifier ADULT_COLLAR_LOCATION = Identifier.withDefaultNamespace("textures/entity/wolf/wolf_collar.png");
    private static final Identifier BABY_COLLAR_LOCATION = Identifier.withDefaultNamespace("textures/entity/wolf/wolf_collar_baby.png");

    @Override
    public ModelBakery.BakedPart buildRoot(Wolf entity) {
        String key = getTexture(entity).toString();
        return CACHE.computeIfAbsent(key, k -> entity.isBaby() ? buildBabyModel(entity) : buildAdultModel(entity));
    }

    private ModelBakery.BakedPart buildAdultModel(Wolf entity) {
        ModelBakery bakery = new ModelBakery(getTexture(entity).toString(), 64, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition head = root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(-1.0f, 13.5f, -7.0f));
        head.addOrReplaceChild("real_head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2, -3, -2, 6, 6, 4)
                        .texOffs(16, 14).addBox(-2, -5, 0, 2, 2, 1)
                        .texOffs(16, 14).addBox(2, -5, 0, 2, 2, 1)
                        .texOffs(0, 10).addBox(-0.5f, -0.001f, -5, 3, 3, 4),
                ModelBakery.PartPose.ZERO);

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(18, 14).addBox(-3, -2, -3, 6, 9, 6),
                ModelBakery.PartPose.offsetAndRotation(0, 14, 2, Mth.PI / 2, 0, 0));

        root.addOrReplaceChild("upper_body",
                ModelBakery.CubeListBuilder.create().texOffs(21, 0).addBox(-3, -3, -3, 8, 6, 7),
                ModelBakery.PartPose.offsetAndRotation(-1, 14, -3, Mth.PI / 2, 0, 0));

        ModelBakery.CubeListBuilder leftLeg = ModelBakery.CubeListBuilder.create()
                .texOffs(0, 18).addBox(0, 0, -1, 2, 8, 2);
        ModelBakery.CubeListBuilder rightLeg = ModelBakery.CubeListBuilder.create().mirror()
                .texOffs(0, 18).addBox(0, 0, -1, 2, 8, 2);
        root.addOrReplaceChild("right_hind_leg", rightLeg, ModelBakery.PartPose.offset(-2.5f, 16, 7));
        root.addOrReplaceChild("left_hind_leg", leftLeg, ModelBakery.PartPose.offset(0.5f, 16, 7));
        root.addOrReplaceChild("right_front_leg", rightLeg, ModelBakery.PartPose.offset(-2.5f, 16, -4));
        root.addOrReplaceChild("left_front_leg", leftLeg, ModelBakery.PartPose.offset(0.5f, 16, -4));

        ModelBakery.PartDefinition tail = root.addOrReplaceChild("tail",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(-1, 12, 8));
        tail.addOrReplaceChild("real_tail",
                ModelBakery.CubeListBuilder.create().texOffs(9, 18).addBox(0, 0, -1, 2, 8, 2),
                ModelBakery.PartPose.ZERO);

        return root.bake();
    }

    public Identifier getTexture(Wolf entity) {
        WolfVariant variant = entity.getEntityData().get(WolfVariantAccess.getDataVariantId()).value();
        WolfVariant.AssetInfo assetInfo = entity.isBaby() ? variant.babyInfo() : variant.adultInfo();
        if (entity.isTame()) {
            return assetInfo.tame().id();
        } else {
            return entity.isAngry() ? assetInfo.angry().id() : assetInfo.wild().id();
        }
    }

    private ModelBakery.BakedPart buildBabyModel(Wolf entity) {
        ModelBakery bakery = new ModelBakery(getTexture(entity).toString(), 32, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition head = root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 12).addBox(-2.99f, -3.25f, -3, 6, 5, 5, new ModelBakery.CubeDeformation(0.025f))
                        .texOffs(17, 12).addBox(-1.5f, -0.24f, -5, 3, 2, 2),
                ModelBakery.PartPose.offset(0, 18.25f, -4));
        head.addOrReplaceChild("right_ear",
                ModelBakery.CubeListBuilder.create().texOffs(0, 5).addBox(-1, -1, -0.5f, 2, 2, 1),
                ModelBakery.PartPose.offset(-2, -4.25f, -0.5f));
        head.addOrReplaceChild("left_ear",
                ModelBakery.CubeListBuilder.create().texOffs(20, 5).addBox(-1, -1, -0.5f, 2, 2, 1),
                ModelBakery.PartPose.offset(2, -4.25f, -0.5f));

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-3, -2, -4, 6, 4, 8),
                ModelBakery.PartPose.offset(0, 19, 0));

        ModelBakery.CubeListBuilder leg = ModelBakery.CubeListBuilder.create()
                .texOffs(0, 22).addBox(-1, 0, -1, 2, 3, 2);
        root.addOrReplaceChild("right_hind_leg", leg, ModelBakery.PartPose.offset(-1.5f, 21, 3));
        root.addOrReplaceChild("left_hind_leg", leg, ModelBakery.PartPose.offset(1.5f, 21, 3));
        root.addOrReplaceChild("right_front_leg", leg, ModelBakery.PartPose.offset(-1.5f, 21, -3));
        root.addOrReplaceChild("left_front_leg", leg, ModelBakery.PartPose.offset(1.5f, 21, -3));

        ModelBakery.PartDefinition tail = root.addOrReplaceChild("tail",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offsetAndRotation(0, 19, 3, -0.5236f, 0, 0));
        tail.addOrReplaceChild("tail_r1",
                ModelBakery.CubeListBuilder.create().texOffs(22, 16).addBox(-1, -5.7f, -1, 2, 6, 2),
                ModelBakery.PartPose.offsetAndRotation(0, -0.6f, 0.2f, -3.1f, 0, 0));

        return root.bake();
    }

    private ModelBakery.BakedPart getCollarModel(boolean baby) {
        Identifier collarTex = baby ? BABY_COLLAR_LOCATION : ADULT_COLLAR_LOCATION;
        return COLLAR_CACHE.computeIfAbsent(collarTex.toString(), k -> buildCollarModel(k, baby));
    }

    private ModelBakery.BakedPart buildCollarModel(String texture, boolean baby) {
        if (baby) {
            ModelBakery bakery = new ModelBakery(texture, 32, 32);
            ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
            ModelBakery.PartDefinition root = model.root();

            root.addOrReplaceChild("head",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(0, 12).addBox(-2.99f, -3.25f, -3, 6, 5, 5, new ModelBakery.CubeDeformation(0.025f))
                            .texOffs(17, 12).addBox(-1.5f, -0.24f, -5, 3, 2, 2),
                    ModelBakery.PartPose.offset(0, 18.25f, -4));
        }

        if (baby) {
            return buildGenericModel(texture, true);
        } else {
            return buildGenericModel(texture, false);
        }
    }

    private ModelBakery.BakedPart buildGenericModel(String texture, boolean baby) {
        if (baby) {
            return buildBabyModelWithTexture(texture);
        } else {
            return buildAdultModelWithTexture(texture);
        }
    }

    private ModelBakery.BakedPart buildAdultModelWithTexture(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 64, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition head = root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(-1.0f, 13.5f, -7.0f));
        head.addOrReplaceChild("real_head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2, -3, -2, 6, 6, 4)
                        .texOffs(16, 14).addBox(-2, -5, 0, 2, 2, 1)
                        .texOffs(16, 14).addBox(2, -5, 0, 2, 2, 1)
                        .texOffs(0, 10).addBox(-0.5f, -0.001f, -5, 3, 3, 4),
                ModelBakery.PartPose.ZERO);

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(18, 14).addBox(-3, -2, -3, 6, 9, 6),
                ModelBakery.PartPose.offsetAndRotation(0, 14, 2, Mth.PI / 2, 0, 0));

        root.addOrReplaceChild("upper_body",
                ModelBakery.CubeListBuilder.create().texOffs(21, 0).addBox(-3, -3, -3, 8, 6, 7),
                ModelBakery.PartPose.offsetAndRotation(-1, 14, -3, Mth.PI / 2, 0, 0));

        ModelBakery.CubeListBuilder leftLeg = ModelBakery.CubeListBuilder.create()
                .texOffs(0, 18).addBox(0, 0, -1, 2, 8, 2);
        ModelBakery.CubeListBuilder rightLeg = ModelBakery.CubeListBuilder.create().mirror()
                .texOffs(0, 18).addBox(0, 0, -1, 2, 8, 2);
        root.addOrReplaceChild("right_hind_leg", rightLeg, ModelBakery.PartPose.offset(-2.5f, 16, 7));
        root.addOrReplaceChild("left_hind_leg", leftLeg, ModelBakery.PartPose.offset(0.5f, 16, 7));
        root.addOrReplaceChild("right_front_leg", rightLeg, ModelBakery.PartPose.offset(-2.5f, 16, -4));
        root.addOrReplaceChild("left_front_leg", leftLeg, ModelBakery.PartPose.offset(0.5f, 16, -4));

        ModelBakery.PartDefinition tail = root.addOrReplaceChild("tail",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(-1, 12, 8));
        tail.addOrReplaceChild("real_tail",
                ModelBakery.CubeListBuilder.create().texOffs(9, 18).addBox(0, 0, -1, 2, 8, 2),
                ModelBakery.PartPose.ZERO);

        return root.bake();
    }

    private ModelBakery.BakedPart buildBabyModelWithTexture(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 32, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition head = root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 12).addBox(-2.99f, -3.25f, -3, 6, 5, 5, new ModelBakery.CubeDeformation(0.025f))
                        .texOffs(17, 12).addBox(-1.5f, -0.24f, -5, 3, 2, 2),
                ModelBakery.PartPose.offset(0, 18.25f, -4));
        head.addOrReplaceChild("right_ear",
                ModelBakery.CubeListBuilder.create().texOffs(0, 5).addBox(-1, -1, -0.5f, 2, 2, 1),
                ModelBakery.PartPose.offset(-2, -4.25f, -0.5f));
        head.addOrReplaceChild("left_ear",
                ModelBakery.CubeListBuilder.create().texOffs(20, 5).addBox(-1, -1, -0.5f, 2, 2, 1),
                ModelBakery.PartPose.offset(2, -4.25f, -0.5f));

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-3, -2, -4, 6, 4, 8),
                ModelBakery.PartPose.offset(0, 19, 0));

        ModelBakery.CubeListBuilder leg = ModelBakery.CubeListBuilder.create()
                .texOffs(0, 22).addBox(-1, 0, -1, 2, 3, 2);
        root.addOrReplaceChild("right_hind_leg", leg, ModelBakery.PartPose.offset(-1.5f, 21, 3));
        root.addOrReplaceChild("left_hind_leg", leg, ModelBakery.PartPose.offset(1.5f, 21, 3));
        root.addOrReplaceChild("right_front_leg", leg, ModelBakery.PartPose.offset(-1.5f, 21, -3));
        root.addOrReplaceChild("left_front_leg", leg, ModelBakery.PartPose.offset(1.5f, 21, -3));

        ModelBakery.PartDefinition tail = root.addOrReplaceChild("tail",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offsetAndRotation(0, 19, 3, -0.5236f, 0, 0));
        tail.addOrReplaceChild("tail_r1",
                ModelBakery.CubeListBuilder.create().texOffs(22, 16).addBox(-1, -5.7f, -1, 2, 6, 2),
                ModelBakery.PartPose.offsetAndRotation(0, -0.6f, 0.2f, -3.1f, 0, 0));

        return root.bake();
    }

    private String resolveArmorTexture(ItemStack armorItem) {
        if (armorItem.isEmpty()) return null;
        Equippable equippable = armorItem.get(DataComponents.EQUIPPABLE);
        if (equippable == null || equippable.assetId().isEmpty()) return null;
        var aid = equippable.assetId().get().identifier();
        return aid.getNamespace() + ":entity/equipment/wolf_body/" + aid.getPath();
    }

    @Override
    public void render(RenderPipeline pipeline, Wolf entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyYaw);
        double headYaw = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYaw);
        double headPitch = Mth.DEG_TO_RAD * entity.getXRot(1.0f);
        boolean baby = entity.isBaby();
        double ageScale = baby ? 0.5f : 1.0f;
        boolean angry = entity.isAngry();
        boolean sitting = entity.isInSittingPose();
        double tailAngle = entity.getTailAngle();
        double headRoll = entity.getHeadRollAngle(1.0f);
        double shakeAnim = entity.getShakeAnim(1.0f);
        double animPos = entity.walkAnimation.position();
        double animSpeed = entity.walkAnimation.speed();
        DyeColor collarColor = entity.isTame() ? entity.getCollarColor() : null;
        ItemStack armorItem = entity.getBodyArmorItem();

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y, pos.z)
                .rotateY(modelYaw)
                .translate(0, 1.5f, 0)
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        double rhAngle, lhAngle, rfAngle, lfAngle;
        if (sitting) {
            rhAngle = lhAngle = rfAngle = lfAngle = 0;
        } else {
            rhAngle = Mth.cos(animPos * 0.6662f) * 1.4f * animSpeed;
            lhAngle = Mth.cos(animPos * 0.6662f + Mth.PI) * 1.4f * animSpeed;
            rfAngle = Mth.cos(animPos * 0.6662f + Mth.PI) * 1.4f * animSpeed;
            lfAngle = Mth.cos(animPos * 0.6662f) * 1.4f * animSpeed;
        }

        double tailYaw = 0;
        if (!angry) {
            tailYaw = Mth.cos(animPos * 0.6662f) * 1.4f * animSpeed;
        }

        double bodyRoll = shakeAnim > 0 ? (double) Math.sin(shakeAnim * 10) * 0.1f : 0;

        AnimParams params = new AnimParams(
                headYaw, headPitch, baby, ageScale, angry, sitting,
                rhAngle, lhAngle, rfAngle, lfAngle,
                tailAngle, tailYaw, headRoll, bodyRoll
        );

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        int tint = LivingEntityRenderer.hurtTint(entity);
        if (!entity.isInvisible()) renderWolfPart(pipeline, buildRoot(entity), "root", base, params, tint, block, sky);

        if (collarColor != null) {
            ModelBakery.BakedPart collarModel = getCollarModel(baby);
            int collarTint = collarColor.getTextureDiffuseColor();
            renderWolfPart(pipeline, collarModel, "root", base, params, collarTint, block, sky);
        }

        if (!baby && !armorItem.isEmpty()) {
            String armorTex = resolveArmorTexture(armorItem);
            if (armorTex != null) {
                ModelBakery.BakedPart armorModel = ARMOR_CACHE.computeIfAbsent(armorTex, tex -> buildGenericModel(tex, false));
                renderWolfPart(pipeline, armorModel, "root", base, params, 0xFFFFFFFF, block, sky);
            }
        }
    }

    private void renderWolfPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                                Matrix4d parent, AnimParams p, int color, double block, double sky) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        applyPose(mat, name, p);

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(color)));
        }
        for (var child : part.children.entrySet()) {
            renderWolfPart(pipeline, child.getValue(), child.getKey(), mat, p, color, block, sky);
        }
    }

    private void applyPose(Matrix4d mat, String name, AnimParams p) {
        switch (name) {
            case "head", "real_head" -> {
                mat.rotateY(p.headYaw);
                mat.rotateX(p.headPitch);
                if (p.headRoll != 0) mat.rotateZ(p.headRoll + p.bodyRoll);
            }
            case "body" -> {
                if (p.bodyRoll != 0) mat.rotateZ(p.bodyRoll);
            }
            case "upper_body" -> {
                if (p.bodyRoll != 0)
                    mat.rotateZ(p.bodyRoll - 0.08f * p.bodyRoll);
                if (p.sitting) {
                    mat.translate(0, 2.0f / 16.0f, 0);
                    mat.rotateX((double) (Math.PI * 2.0 / 5.0));
                }
            }
            case "tail" -> {
                if (p.sitting) {
                    mat.translate(0, 9.0f * p.ageScale / 16f, -2.0f * p.ageScale / 16f);
                }
                if (!p.angry) mat.rotateY(p.tailYaw);
                mat.rotateX(p.tailAngle);
                if (p.bodyRoll != 0) {
                    mat.rotateZ(p.bodyRoll * (-0.2f));
                }
            }
            case "real_tail", "tail_r1" -> {

            }
            case "right_hind_leg", "left_hind_leg", "right_front_leg", "left_front_leg" -> {
                if (p.sitting) {
                    if (name.contains("hind")) {
                        mat.translate(0, 6.7f * p.ageScale / 16f, -5.0f * p.ageScale / 16f);
                        mat.rotateX((double) (Math.PI * 3.0 / 2.0));
                    } else {
                        mat.rotateX(5.811947f);
                        if (name.contains("right")) mat.translate(0.01f * p.ageScale / 16f, p.ageScale / 16f, 0);
                        else mat.translate(-0.01f * p.ageScale / 16f, p.ageScale / 16f, 0);
                    }
                } else {
                    switch (name) {
                        case "right_hind_leg" -> mat.rotateX(p.rhAng);
                        case "left_hind_leg" -> mat.rotateX(p.lhAng);
                        case "right_front_leg" -> mat.rotateX(p.rfAng);
                        case "left_front_leg" -> mat.rotateX(p.lfAng);
                    }
                }
            }
        }
    }

    private static class AnimParams {
        final double headYaw, headPitch;
        final boolean baby;
        final double ageScale;
        final boolean angry, sitting;
        final double rhAng, lhAng, rfAng, lfAng;
        final double tailAngle, tailYaw;
        final double headRoll, bodyRoll;

        AnimParams(double hy, double hp, boolean bb, double as,
                   boolean angry, boolean sitting,
                   double rh, double lh, double rf, double lf,
                   double ta, double ty, double hr, double br) {
            headYaw = hy;
            headPitch = hp;
            baby = bb;
            ageScale = as;
            this.angry = angry;
            this.sitting = sitting;
            rhAng = rh;
            lhAng = lh;
            rfAng = rf;
            lfAng = lf;
            tailAngle = ta;
            tailYaw = ty;
            headRoll = hr;
            bodyRoll = br;
        }
    }

    public static class WolfVariantAccess {
        private static final Field DATA_VARIANT_ID_FIELD;

        static {
            try {
                DATA_VARIANT_ID_FIELD = Wolf.class.getDeclaredField("DATA_VARIANT_ID");
                DATA_VARIANT_ID_FIELD.setAccessible(true);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("Could not find Wolf DATA_VARIANT_ID field", e);
            }
        }

        @SuppressWarnings("unchecked")
        public static EntityDataAccessor<Holder<WolfVariant>> getDataVariantId() {
            try {
                return (EntityDataAccessor<Holder<WolfVariant>>) DATA_VARIANT_ID_FIELD.get(null);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Could not access Wolf DATA_VARIANT_ID", e);
            }
        }
    }
}
