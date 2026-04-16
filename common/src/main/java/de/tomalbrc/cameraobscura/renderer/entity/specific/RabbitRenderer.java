package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class RabbitRenderer implements LivingEntityRenderer<Rabbit> {
    private static final Map<Rabbit.Variant, String> ADULT_TEXTURES = new EnumMap<>(Rabbit.Variant.class);
    private static final Map<Rabbit.Variant, String> BABY_TEXTURES = new EnumMap<>(Rabbit.Variant.class);
    private static final String TOAST_ADULT = "entity/rabbit/rabbit_toast";
    private static final String TOAST_BABY = "entity/rabbit/rabbit_toast_baby";

    static {
        ADULT_TEXTURES.put(Rabbit.Variant.BROWN, "entity/rabbit/rabbit_brown");
        ADULT_TEXTURES.put(Rabbit.Variant.WHITE_SPLOTCHED, "entity/rabbit/rabbit_white_splotched");
        ADULT_TEXTURES.put(Rabbit.Variant.EVIL, "entity/rabbit/rabbit_caerbannog");
        ADULT_TEXTURES.put(Rabbit.Variant.WHITE, "entity/rabbit/rabbit_white");
        ADULT_TEXTURES.put(Rabbit.Variant.GOLD, "entity/rabbit/rabbit_gold");
        ADULT_TEXTURES.put(Rabbit.Variant.BLACK, "entity/rabbit/rabbit_black");
        ADULT_TEXTURES.put(Rabbit.Variant.SALT, "entity/rabbit/rabbit_salt");

        BABY_TEXTURES.put(Rabbit.Variant.BROWN, "entity/rabbit/rabbit_brown_baby");
        BABY_TEXTURES.put(Rabbit.Variant.WHITE_SPLOTCHED, "entity/rabbit/rabbit_white_splotched_baby");
        BABY_TEXTURES.put(Rabbit.Variant.EVIL, "entity/rabbit/rabbit_caerbannog_baby");
        BABY_TEXTURES.put(Rabbit.Variant.WHITE, "entity/rabbit/rabbit_white_baby");
        BABY_TEXTURES.put(Rabbit.Variant.GOLD, "entity/rabbit/rabbit_gold_baby");
        BABY_TEXTURES.put(Rabbit.Variant.BLACK, "entity/rabbit/rabbit_black_baby");
        BABY_TEXTURES.put(Rabbit.Variant.SALT, "entity/rabbit/rabbit_salt_baby");
    }

    private final Map<String, ModelBakery.BakedPart> cache = new HashMap<>();

    @Override
    public ModelBakery.BakedPart buildRoot(Rabbit entity) {
        String key = getCacheKey(entity);
        return cache.computeIfAbsent(key, k -> buildModel(k, entity));
    }

    private String getCacheKey(Rabbit entity) {
        Rabbit.Variant variant = entity.getVariant();
        boolean baby = entity.isBaby();
        boolean toast = "Toast".equals(entity.getName().getString());
        if (toast) {
            return "toast_" + (baby ? "baby" : "adult");
        }
        return variant.name() + (baby ? "_baby" : "_adult");
    }

    private ModelBakery.BakedPart buildModel(String key, Rabbit entity) {
        String texture;
        boolean baby = entity.isBaby();
        boolean toast = "Toast".equals(entity.getName().getString());
        if (toast) {
            texture = baby ? TOAST_BABY : TOAST_ADULT;
        } else {
            Rabbit.Variant variant = entity.getVariant();
            texture = baby ? BABY_TEXTURES.getOrDefault(variant, BABY_TEXTURES.get(Rabbit.Variant.BROWN))
                    : ADULT_TEXTURES.getOrDefault(variant, ADULT_TEXTURES.get(Rabbit.Variant.BROWN));
        }

        ModelBakery bakery = new ModelBakery(texture, baby ? 32 : 64, baby ? 32 : 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        if (baby) {
            buildBabyModel(root);
        } else {
            buildAdultModel(root);
        }
        return root.bake();
    }

    private void buildAdultModel(ModelBakery.PartDefinition root) {
        ModelBakery.PartDefinition body = root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-4, -6, -9, 8, 6, 10),
                ModelBakery.PartPose.offsetAndRotation(0, 23, 4, -0.3927f, 0, 0));

        body.addOrReplaceChild("tail",
                ModelBakery.CubeListBuilder.create().texOffs(20, 16).addBox(-2, -3.0084f, -1.0125f, 4, 4, 4),
                ModelBakery.PartPose.offset(0, -4.9916f, 0.0125f));

        ModelBakery.PartDefinition head = body.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create().texOffs(0, 16).addBox(-2.5f, -3, -4, 5, 5, 5),
                ModelBakery.PartPose.offsetAndRotation(0, -5.2929f, -8.1213f, 0.3927f, 0, 0));
        head.addOrReplaceChild("left_ear",
                ModelBakery.CubeListBuilder.create().texOffs(32, 0).addBox(-1, -4.2929f, -0.1213f, 2, 5, 1),
                ModelBakery.PartPose.offset(1.5f, -3.7071f, -0.8787f));
        head.addOrReplaceChild("right_ear",
                ModelBakery.CubeListBuilder.create().texOffs(26, 0).addBox(-1, -4.2929f, -0.1213f, 2, 5, 1),
                ModelBakery.PartPose.offset(-1.5f, -3.7071f, -0.8787f));

        ModelBakery.PartDefinition frontLegs = body.addOrReplaceChild("frontlegs",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(0, -1.5349f, -6.3108f));
        frontLegs.addOrReplaceChild("right_front_leg",
                ModelBakery.CubeListBuilder.create().texOffs(36, 18).addBox(-0.9f, -1, -0.9f, 2, 4, 2),
                ModelBakery.PartPose.offsetAndRotation(-2, 1.9239f, 0.3827f, 0.3927f, 0, 0));
        frontLegs.addOrReplaceChild("left_front_leg",
                ModelBakery.CubeListBuilder.create().texOffs(44, 18).addBox(-1, -1, -1, 2, 4, 2),
                ModelBakery.PartPose.offsetAndRotation(2, 1.9239f, 0.4827f, 0.3927f, 0, 0));

        ModelBakery.PartDefinition backLegs = root.addOrReplaceChild("backlegs",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(0, 23, 4));
        ModelBakery.PartDefinition rightHind = backLegs.addOrReplaceChild("right_hind_leg",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(-3, 0.5f, 0));
        rightHind.addOrReplaceChild("right_haunch",
                ModelBakery.CubeListBuilder.create().texOffs(20, 24).addBox(-1, 0, -5, 2, 1, 6),
                ModelBakery.PartPose.offsetAndRotation(0, -0.5f, 0, 0, 0.3927f, 0));

        ModelBakery.PartDefinition leftHind = backLegs.addOrReplaceChild("left_hind_leg",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(3, 0.5f, 0));
        leftHind.addOrReplaceChild("left_haunch",
                ModelBakery.CubeListBuilder.create().texOffs(36, 24).addBox(-1, 0, -5, 2, 1, 6),
                ModelBakery.PartPose.offsetAndRotation(0, -0.5f, 0, 0, -0.3927f, 0));
    }

    private void buildBabyModel(ModelBakery.PartDefinition root) {
        ModelBakery.PartDefinition body = root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(0, 23, 1.6f));
        body.addOrReplaceChild("body_r1",
                ModelBakery.CubeListBuilder.create().texOffs(0, 8).addBox(-2, -2, -3, 4, 3, 6),
                ModelBakery.PartPose.offsetAndRotation(0, -2, -1.6f, -0.5236f, 0, 0));
        ModelBakery.PartDefinition tail = body.addOrReplaceChild("tail",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(0, -2.2f, 2));
        tail.addOrReplaceChild("tail_r1",
                ModelBakery.CubeListBuilder.create().texOffs(0, 21).addBox(-1.4f, -2.0268f, -1.0177f, 3, 3, 3),
                ModelBakery.PartPose.offsetAndRotation(-0.1f, 0, 0, -0.5236f, 0, 0));
        ModelBakery.PartDefinition head = body.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-2.5f, -3, -3, 5, 4, 4),
                ModelBakery.PartPose.offset(0, -5, -2.6f));
        head.addOrReplaceChild("right_ear",
                ModelBakery.CubeListBuilder.create().texOffs(18, 0).addBox(-1, -3.5f, -0.5f, 2, 4, 1),
                ModelBakery.PartPose.offset(-1.5f, -3.5f, -0.5f));
        head.addOrReplaceChild("left_ear",
                ModelBakery.CubeListBuilder.create().texOffs(24, 0).addBox(-1, -3.5f, -0.5f, 2, 4, 1),
                ModelBakery.PartPose.offset(1.5f, -3.5f, -0.5f));

        ModelBakery.PartDefinition frontLegs = body.addOrReplaceChild("frontlegs",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(0, -2.5f, -2.6f));
        frontLegs.addOrReplaceChild("left_front_leg",
                        ModelBakery.CubeListBuilder.create(),
                        ModelBakery.PartPose.offsetAndRotation(1, 1, -0.5f, 0.3927f, 0, 0))
                .addOrReplaceChild("left_front_leg_r1",
                        ModelBakery.CubeListBuilder.create().texOffs(18, 8).addBox(-0.5f, -1.5f, -0.5f, 1, 3, 1),
                        ModelBakery.PartPose.offsetAndRotation(0, 1, 0, -0.3927f, 0, 0));
        frontLegs.addOrReplaceChild("right_front_leg",
                        ModelBakery.CubeListBuilder.create(),
                        ModelBakery.PartPose.offsetAndRotation(-1, 1, -0.5f, 0.3927f, 0, 0))
                .addOrReplaceChild("right_front_leg_r1",
                        ModelBakery.CubeListBuilder.create().texOffs(14, 8).addBox(-0.5f, -1.5f, -0.5f, 1, 3, 1),
                        ModelBakery.PartPose.offsetAndRotation(0, 1, 0, -0.3927f, 0, 0));

        ModelBakery.PartDefinition backLegs = root.addOrReplaceChild("backlegs",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(0, 23, 2));
        backLegs.addOrReplaceChild("left_hind_leg",
                        ModelBakery.CubeListBuilder.create(),
                        ModelBakery.PartPose.offsetAndRotation(1.5f, 0.5f, 0.5f, 0, Mth.PI, 0))
                .addOrReplaceChild("left_haunch",
                        ModelBakery.CubeListBuilder.create().texOffs(10, 17).addBox(-2, -0.5f, 0, 2, 1, 3),
                        ModelBakery.PartPose.offsetAndRotation(1, 0, 0.5f, 0, -0.7854f, 0));
        backLegs.addOrReplaceChild("right_hind_leg",
                        ModelBakery.CubeListBuilder.create(),
                        ModelBakery.PartPose.offsetAndRotation(-1.5f, 0.5f, 0.5f, 0, Mth.PI, 0))
                .addOrReplaceChild("right_haunch",
                        ModelBakery.CubeListBuilder.create().texOffs(0, 17).addBox(-2, -0.5f, 0, 2, 1, 3),
                        ModelBakery.PartPose.offsetAndRotation(0.5f, 0, -0.9f, 0, 0.7854f, 0));
    }

    @Override
    public void render(RenderPipeline pipeline, Rabbit entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyYaw);
        double headYaw = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYaw);
        double headPitch = Mth.DEG_TO_RAD * entity.getXRot(1.0f);
        boolean baby = entity.isBaby();
        double jumpCompletion = entity.getJumpCompletion(1.0f);
        double ageInTicks = entity.tickCount + 1.0f;

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y, pos.z)
                .rotateY(modelYaw);

        base.translate(0, 1.5f, 0);
        base.rotateY(Mth.PI);
        base.rotateX(Mth.PI);

        double hopScale = jumpCompletion > 0 ? 1.0f : 0;
        double squish = jumpCompletion * 0.5f;
        double legAngle = Mth.sin(jumpCompletion * Mth.PI) * 0.8f;
        double earFlop = Mth.sin(ageInTicks * 0.1f) * 0.1f;

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        ModelBakery.BakedPart model = buildRoot(entity);
        renderParts(pipeline, model, "root", base, headYaw, headPitch, legAngle, squish, earFlop, ageInTicks, block, sky);
    }

    private void renderParts(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                             Matrix4d parent,
                             double headYaw, double headPitch,
                             double legAngle, double squish, double earFlop, double ageInTicks,
                             double block, double sky) {

        for (var child : part.children.entrySet()) {
            String childName = child.getKey();
            ModelBakery.BakedPart childPart = child.getValue();

            Matrix4d mat = new Matrix4d(parent);
            mat.translate(childPart.localPivot.x, childPart.localPivot.y, childPart.localPivot.z);

            ModelBakery.PartPose ip = childPart.initialPose;
            if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
                mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
            if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
                mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

            switch (childName) {
                case "head" -> {
                    mat.rotateY(headYaw);
                    mat.rotateX(headPitch);
                }
                case "left_ear", "right_ear" -> mat.rotateZ(childName.equals("left_ear") ? earFlop : -earFlop);
                case "body" -> mat.scale(1.0f + squish * 0.2f, 1.0f - squish * 0.1f, 1.0f + squish * 0.2f);
                case "left_front_leg", "right_front_leg" ->
                        mat.rotateX(legAngle * (childName.equals("left_front_leg") ? 1 : -1));
                case "left_hind_leg", "right_hind_leg" ->
                        mat.rotateX(legAngle * (childName.equals("left_hind_leg") ? -1 : 1));
                case "tail" -> mat.rotateZ(Mth.sin(ageInTicks * 0.2f) * 0.1f);
            }

            if (childPart.mesh != null) {
                pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(childPart.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
            }

            renderParts(pipeline, childPart, childName, mat, headYaw, headPitch, legAngle, squish, earFlop, ageInTicks, block, sky);
        }
    }
}
