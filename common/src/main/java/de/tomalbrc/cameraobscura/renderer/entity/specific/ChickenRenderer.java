package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.platform.Platforms;
import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.chicken.ChickenVariant;
import net.minecraft.world.entity.animal.chicken.ChickenVariants;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

public class ChickenRenderer implements LivingEntityRenderer<Chicken> {
    private static final Map<CacheKey, ModelBakery.BakedPart> CACHE = new IdentityHashMap<>();

    @Override
    public ModelBakery.BakedPart buildRoot(Chicken chicken) {
        return CACHE.computeIfAbsent(
                new CacheKey(chicken.getVariant(), chicken.isBaby()),
                key -> buildParts(key.variant, key.baby)
        );
    }

    private ModelBakery.BakedPart buildParts(Holder<ChickenVariant> variant, boolean baby) {
        String tex = texture(variant);
        if (baby) {
            return buildBabyParts(tex + "_baby");
        } else {
            return buildAdultParts(tex, variant);
        }
    }

    protected String texture(Holder<ChickenVariant> variant) {
        return variant.value().modelAndTexture().asset().id().toString();
    }

    private ModelBakery.BakedPart buildBabyParts(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 16, 16);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild(
                "body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.0F, -2.25F, -0.75F, 4.0F, 4.0F, 4.0F)
                        .texOffs(10, 8).addBox(-1.0F, -0.25F, -1.75F, 2.0F, 1.0F, 1.0F),
                ModelBakery.PartPose.offset(0.0F, 20.25F, -1.25F)
        );

        root.addOrReplaceChild(
                "left_hind_leg",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(2, 2).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 2.0F, 0.0F)
                        .texOffs(0, 1).addBox(-0.5F, 2.0F, -1.0F, 1.0F, 0.0F, 1.0F),
                ModelBakery.PartPose.offset(1.0F, 22.0F, 0.5F)
        );
        root.addOrReplaceChild(
                "right_hind_leg",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 2).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 2.0F, 0.0F)
                        .texOffs(0, 0).addBox(-0.5F, 2.0F, -1.0F, 1.0F, 0.0F, 1.0F),
                ModelBakery.PartPose.offset(-1.0F, 22.0F, 0.5F)
        );

        root.addOrReplaceChild(
                "right_wing",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(6, 8).addBox(0.0F, 0.0F, -1.0F, 1.0F, 0.0F, 2.0F),
                ModelBakery.PartPose.offset(2.0F, 20.0F, 0.0F)
        );
        root.addOrReplaceChild(
                "left_wing",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(4, 8).addBox(-1.0F, 0.0F, -1.0F, 1.0F, 0.0F, 2.0F),
                ModelBakery.PartPose.offset(-2.0F, 20.0F, 0.0F)
        );

        return root.bake();
    }

    private ModelBakery.BakedPart buildAdultParts(String texture, Holder<ChickenVariant> variant) {
        ModelBakery bakery = new ModelBakery(texture, 64, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition head = root.addOrReplaceChild(
                "head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 6.0F, 3.0F),
                ModelBakery.PartPose.offset(0.0F, 15.0F, -4.0F)
        );
        head.addOrReplaceChild(
                "beak",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(14, 0).addBox(-2.0F, -4.0F, -4.0F, 4.0F, 2.0F, 2.0F),
                ModelBakery.PartPose.ZERO
        );
        head.addOrReplaceChild(
                "red_thing",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(14, 4).addBox(-1.0F, -2.0F, -3.0F, 2.0F, 2.0F, 2.0F),
                ModelBakery.PartPose.ZERO
        );

        root.addOrReplaceChild(
                "body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 9).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 8.0F, 6.0F),
                ModelBakery.PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, (Mth.PI / 2), 0.0F, 0.0F)
        );

        if (variant == Platforms.get().getRegistryAccess().getOrThrow(ChickenVariants.COLD)) {
            ModelBakery.PartDefinition coldHead = root.addOrReplaceChild(
                    "head",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(0, 0).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 6.0F, 3.0F)
                            .texOffs(44, 0).addBox(-3.0F, -7.0F, -2.015F, 6.0F, 3.0F, 4.0F),
                    ModelBakery.PartPose.offset(0.0F, 15.0F, -4.0F)
            );
            coldHead.addOrReplaceChild(
                    "beak",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(14, 0).addBox(-2.0F, -4.0F, -4.0F, 4.0F, 2.0F, 2.0F),
                    ModelBakery.PartPose.ZERO
            );
            coldHead.addOrReplaceChild(
                    "red_thing",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(14, 4).addBox(-1.0F, -2.0F, -3.0F, 2.0F, 2.0F, 2.0F),
                    ModelBakery.PartPose.ZERO
            );

            root.addOrReplaceChild(
                    "body",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(0, 9).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 8.0F, 6.0F)
                            .texOffs(38, 9).addBox(0.0F, 3.0F, -1.0F, 0.0F, 3.0F, 5.0F),
                    ModelBakery.PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, (Mth.PI / 2), 0.0F, 0.0F)
            );
        }

        ModelBakery.CubeListBuilder leg = ModelBakery.CubeListBuilder.create()
                .texOffs(26, 0).addBox(-1.0F, 0.0F, -3.0F, 3.0F, 5.0F, 3.0F);
        root.addOrReplaceChild("right_hind_leg", leg,
                ModelBakery.PartPose.offset(-2.0F, 19.0F, 1.0F));
        root.addOrReplaceChild("left_hind_leg", leg,
                ModelBakery.PartPose.offset(1.0F, 19.0F, 1.0F));

        root.addOrReplaceChild(
                "right_wing",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(24, 13).addBox(0.0F, 0.0F, -3.0F, 1.0F, 4.0F, 6.0F),
                ModelBakery.PartPose.offset(-4.0F, 13.0F, 0.0F)
        );
        root.addOrReplaceChild(
                "left_wing",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(24, 13).addBox(-1.0F, 0.0F, -3.0F, 1.0F, 4.0F, 6.0F),
                ModelBakery.PartPose.offset(4.0F, 13.0F, 0.0F)
        );

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, Chicken entity) {
        ModelBakery.BakedPart root = buildRoot(entity);

        var pos = entity.position();
        double bodyYawDeg = entity.getPreciseBodyRotation(1.f);
        double modelYawRad = Mth.DEG_TO_RAD * (180f - bodyYawDeg);

        double headYawRelDeg = entity.getYHeadRot() - bodyYawDeg;
        double headYawRad = Mth.DEG_TO_RAD * headYawRelDeg;
        double headPitchRad = Mth.DEG_TO_RAD * entity.getXRot(1.f);

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + 1.5f + 0.02f, pos.z)
                .rotateY(modelYawRad);

        base.rotateY(Mth.PI);
        base.rotateX(Mth.PI);

        double limbSwing = entity.walkAnimation.position() * 0.6662f;
        double limbSpeed = entity.walkAnimation.speed();
        double limbAngle = Mth.cos(limbSwing) * 1.4f * limbSpeed;

        double flap = Mth.sin(entity.flap) + 1.0F;
        double flapAngle = flap * entity.flapSpeed;

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        int tint = LivingEntityRenderer.hurtTint(entity);
        var tints = IntList.of(tint);

        renderChickenPart(pipeline, root, "root", base, limbAngle, headYawRad, headPitchRad, flapAngle, tints, block, sky);
    }

    private void renderChickenPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                                   Matrix4d parentMat, double limbAngle, double headYawRad, double headPitchRad,
                                   double flapAngle,
                                   IntList tints, double block, double sky) {
        Matrix4d mat = new Matrix4d(parentMat);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose pose = part.initialPose;
        if (pose.xRot() != 0 || pose.yRot() != 0 || pose.zRot() != 0)
            mat.rotateZYX(pose.zRot(), pose.yRot(), pose.xRot());
        if (pose.xScale() != 1 || pose.yScale() != 1 || pose.zScale() != 1)
            mat.scale(pose.xScale(), pose.yScale(), pose.zScale());

        switch (name) {
            case "head" -> {
                mat.rotateY(headYawRad);
                mat.rotateX(headPitchRad);
            }
            case "right_hind_leg", "left_front_leg" -> mat.rotateX(limbAngle);
            case "left_hind_leg", "right_front_leg" -> mat.rotateX(-limbAngle);
            case "right_wing" -> mat.rotateZ(flapAngle);
            case "left_wing" -> mat.rotateZ(-flapAngle);
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, tints));
        }

        for (var childEntry : part.children.entrySet()) {
            renderChickenPart(
                    pipeline, childEntry.getValue(), childEntry.getKey(), mat,
                    limbAngle, headYawRad, headPitchRad, flapAngle,
                    tints, block, sky
            );
        }
    }

    private record CacheKey(Holder<ChickenVariant> variant, boolean baby) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CacheKey(Holder<ChickenVariant> variant1, boolean baby1))) return false;
            return baby == baby1 && variant == variant1;
        }

        @Override
        public int hashCode() {
            return Objects.hash(System.identityHashCode(variant), baby);
        }
    }
}