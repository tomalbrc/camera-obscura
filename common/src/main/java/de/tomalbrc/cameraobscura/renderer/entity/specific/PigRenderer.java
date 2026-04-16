package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.platform.Platforms;
import de.tomalbrc.cameraobscura.renderer.entity.FourLeggedRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.pig.PigVariant;
import net.minecraft.world.entity.animal.pig.PigVariants;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

import java.util.IdentityHashMap;
import java.util.Map;

public class PigRenderer extends FourLeggedRenderer<Pig> {
    private static final String SADDLE_TEXTURE = "entity/equipment/pig_saddle/saddle";
    private static final Map<Holder<PigVariant>, ModelBakery.BakedPart> SADDLE_CACHE = new IdentityHashMap<>();
    private static final Map<Holder<PigVariant>, ModelBakery.BakedPart> ADULT_CACHE = new IdentityHashMap<>();
    private static final Map<Holder<PigVariant>, ModelBakery.BakedPart> BABY_CACHE = new IdentityHashMap<>();

    @Override
    public ModelBakery.BakedPart buildRoot(Pig pig) {
        Holder<PigVariant> variant = pig.getVariant();
        if (pig.isBaby()) {
            return BABY_CACHE.computeIfAbsent(variant, this::buildBabyParts);
        } else {
            return ADULT_CACHE.computeIfAbsent(variant, this::buildAdultParts);
        }
    }

    protected String texture(Holder<PigVariant> variant) {
        return variant.value().modelAndTexture().asset().id().toString();
    }

    private ModelBakery.BakedPart getSaddleModel(Holder<PigVariant> variant) {
        return SADDLE_CACHE.computeIfAbsent(variant, v -> {
            var mesh = FourLeggedRenderer.createDefaultBody(6, true, false, ModelBakery.CubeDeformation.NONE, SADDLE_TEXTURE, 64, 64);
            ModelBakery.PartDefinition root = mesh.root();

            root.addOrReplaceChild(
                    "head",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(0, 0).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F)
                            .texOffs(16, 16).addBox(-2.0F, 0.0F, -9.0F, 4.0F, 3.0F, 1.0F),
                    ModelBakery.PartPose.offset(0.0F, 12.0F, -6.0F)
            );

            return root.bake();
        });
    }

    private ModelBakery.BakedPart buildAdultParts(Holder<PigVariant> variant) {
        var mesh = FourLeggedRenderer.createDefaultBody(6, true, false, ModelBakery.CubeDeformation.NONE, texture(variant), 64, 64);
        ModelBakery.PartDefinition root = mesh.root();

        root.addOrReplaceChild(
                "head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F)
                        .texOffs(16, 16).addBox(-2.0F, 0.0F, -9.0F, 4.0F, 3.0F, 1.0F),
                ModelBakery.PartPose.offset(0.0F, 12.0F, -6.0F)
        );

        if (variant == Platforms.get().getRegistryAccess().getOrThrow(PigVariants.COLD)) {
            root.addOrReplaceChild(
                    "body",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(28, 8).addBox(-5.0F, -10.0F, -7.0F, 10.0F, 16.0F, 8.0F)
                            .texOffs(28, 32).addBox(-5.0F, -10.0F, -7.0F, 10.0F, 16.0F, 8.0F, new ModelBakery.CubeDeformation(0.5F)),
                    ModelBakery.PartPose.offsetAndRotation(0.0F, 11.0F, 2.0F, (Mth.PI / 2), 0.0F, 0.0F)
            );
        }

        return root.bake();
    }

    private ModelBakery.BakedPart buildBabyParts(Holder<PigVariant> variant) {
        ModelBakery bakery = new ModelBakery(texture(variant) + "_baby", 32, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -3.0F, -4.5F, 7.0F, 6.0F, 9.0F),
                ModelBakery.PartPose.offset(0.0F, 19.0F, 0.5F));

        root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 15).addBox(-3.5F, -5.0F, -5.0F, 7.0F, 6.0F, 6.0F, new ModelBakery.CubeDeformation(0.025F))
                        .texOffs(6, 27).addBox(-1.5F, -1.975F, -6.0F, 3.0F, 2.0F, 1.0F, new ModelBakery.CubeDeformation(0.015F)),
                ModelBakery.PartPose.offset(0.0F, 19.0F, -2.0F));

        root.addOrReplaceChild("left_front_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                ModelBakery.PartPose.offset(2.5F, 22.0F, -3.0F));
        root.addOrReplaceChild("right_front_leg",
                ModelBakery.CubeListBuilder.create().texOffs(23, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                ModelBakery.PartPose.offset(-2.5F, 22.0F, -3.0F));
        root.addOrReplaceChild("left_hind_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 4).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                ModelBakery.PartPose.offset(2.5F, 22.0F, 4.0F));
        root.addOrReplaceChild("right_hind_leg",
                ModelBakery.CubeListBuilder.create().texOffs(23, 4).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                ModelBakery.PartPose.offset(-2.5F, 22.0F, 4.0F));

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, Pig entity) {
        super.render(pipeline, entity);

        if (!entity.isBaby() && !entity.getItemBySlot(EquipmentSlot.SADDLE).isEmpty()) {
            Holder<PigVariant> variant = entity.getVariant();
            ModelBakery.BakedPart saddleModel = getSaddleModel(variant);

            var pos = entity.position();
            double bodyYawDeg = entity.getPreciseBodyRotation(1.f);
            double modelYawRad = Mth.DEG_TO_RAD * (180f - bodyYawDeg);
            double headYawRad = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYawDeg);
            double headPitchRad = Mth.DEG_TO_RAD * entity.getXRot(1.f);

            Matrix4d base = new Matrix4d()
                    .translate(pos.x, pos.y, pos.z)
                    .rotateY(modelYawRad);
            base.translate(0, 1.5f, 0);
            base.rotateY(Mth.PI);
            base.rotateX(Mth.PI);

            double limbSwing = entity.walkAnimation.position() * 0.6662f;
            double limbSpeed = entity.walkAnimation.speed();
            double limbAngle = Mth.cos(limbSwing) * 1.4f * limbSpeed;

            var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
            var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

            renderSaddlePart(pipeline, saddleModel, "root", base, limbAngle, headYawRad, headPitchRad, block, sky);
        }
    }

    private void renderSaddlePart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name, Matrix4d parentMat, double limbAngle, double headYawRad, double headPitchRad, double block, double sky) {
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
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }

        for (var childEntry : part.children.entrySet()) {
            renderSaddlePart(pipeline, childEntry.getValue(), childEntry.getKey(), mat, limbAngle, headYawRad, headPitchRad, block, sky);
        }
    }
}