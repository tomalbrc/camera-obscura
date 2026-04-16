package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

import java.util.HashMap;
import java.util.Map;

public class StriderRenderer implements LivingEntityRenderer<Strider> {

    private static final String NORMAL_ADULT = "entity/strider/strider";
    private static final String NORMAL_BABY = "entity/strider/strider_baby";
    private static final String COLD_ADULT = "entity/strider/strider_cold";
    private static final String COLD_BABY = "entity/strider/strider_cold_baby";
    private static final String SADDLE_TEXTURE = "entity/strider/strider_saddle";

    private final Map<String, ModelBakery.BakedPart> cache = new HashMap<>();
    private ModelBakery.BakedPart saddleModel;

    @Override
    public ModelBakery.BakedPart buildRoot(Strider entity) {
        boolean baby = entity.isBaby();
        boolean cold = entity.isSuffocating();
        String key = (baby ? "baby_" : "adult_") + (cold ? "cold" : "normal");
        return cache.computeIfAbsent(key, k -> {
            String tex = baby ? (cold ? COLD_BABY : NORMAL_BABY) : (cold ? COLD_ADULT : NORMAL_ADULT);
            return baby ? buildBabyModel(tex) : buildAdultModel(tex);
        });
    }

    private ModelBakery.BakedPart getSaddleModel() {
        if (saddleModel == null) {
            saddleModel = buildAdultModel(SADDLE_TEXTURE);
        }
        return saddleModel;
    }

    private ModelBakery.BakedPart buildAdultModel(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 64, 128);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("right_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 32).addBox(-2, 0, -2, 4, 16, 4),
                ModelBakery.PartPose.offset(-4, 8, 0));
        root.addOrReplaceChild("left_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 55).addBox(-2, 0, -2, 4, 16, 4),
                ModelBakery.PartPose.offset(4, 8, 0));

        ModelBakery.PartDefinition body = root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-8, -6, -8, 16, 14, 16),
                ModelBakery.PartPose.offset(0, 1, 0));

        body.addOrReplaceChild("right_bottom_bristle",
                ModelBakery.CubeListBuilder.create().texOffs(16, 65).addBox(-12, 0, 0, 12, 0, 16),
                ModelBakery.PartPose.offsetAndRotation(-8, 4, -8, 0, 0, -1.2217305F));
        body.addOrReplaceChild("right_middle_bristle",
                ModelBakery.CubeListBuilder.create().texOffs(16, 49).addBox(-12, 0, 0, 12, 0, 16),
                ModelBakery.PartPose.offsetAndRotation(-8, -1, -8, 0, 0, -1.134464F));
        body.addOrReplaceChild("right_top_bristle",
                ModelBakery.CubeListBuilder.create().texOffs(16, 33).addBox(-12, 0, 0, 12, 0, 16),
                ModelBakery.PartPose.offsetAndRotation(-8, -5, -8, 0, 0, -0.87266463F));
        body.addOrReplaceChild("left_top_bristle",
                ModelBakery.CubeListBuilder.create().texOffs(16, 33).addBox(0, 0, 0, 12, 0, 16),
                ModelBakery.PartPose.offsetAndRotation(8, -6, -8, 0, 0, 0.87266463F));
        body.addOrReplaceChild("left_middle_bristle",
                ModelBakery.CubeListBuilder.create().texOffs(16, 49).addBox(0, 0, 0, 12, 0, 16),
                ModelBakery.PartPose.offsetAndRotation(8, -2, -8, 0, 0, 1.134464F));
        body.addOrReplaceChild("left_bottom_bristle",
                ModelBakery.CubeListBuilder.create().texOffs(16, 65).addBox(0, 0, 0, 12, 0, 16),
                ModelBakery.PartPose.offsetAndRotation(8, 3, -8, 0, 0, 1.2217305F));

        return root.bake();
    }

    private ModelBakery.BakedPart buildBabyModel(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 32, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("right_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 24).addBox(-1, 0, -1, 2, 4, 2),
                ModelBakery.PartPose.offset(-1.5f, 20, 0));
        root.addOrReplaceChild("left_leg",
                ModelBakery.CubeListBuilder.create().texOffs(8, 24).addBox(-1, 0, -1, 2, 4, 2),
                ModelBakery.PartPose.offset(1.5f, 20, 0));

        ModelBakery.PartDefinition body = root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-3.5f, -3.75f, -4, 7, 7, 8),
                ModelBakery.PartPose.offset(0, 16.75f, 0));

        body.addOrReplaceChild("bristle0",
                ModelBakery.CubeListBuilder.create().texOffs(0, 21).addBox(-3.5f, -2.5f, 0, 7, 3, 0),
                ModelBakery.PartPose.offset(0, -4.25f, 2));
        body.addOrReplaceChild("bristle1",
                ModelBakery.CubeListBuilder.create().texOffs(0, 18).addBox(-3.5f, -2.5f, 0, 7, 3, 0),
                ModelBakery.PartPose.offset(0, -4.25f, 0));
        body.addOrReplaceChild("bristle2",
                ModelBakery.CubeListBuilder.create().texOffs(0, 15).addBox(-3.5f, -2.5f, 0, 7, 3, 0),
                ModelBakery.PartPose.offset(0, -4.25f, -2));

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, Strider entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double headYawRel = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYaw);
        double headPitch = Mth.DEG_TO_RAD * entity.getXRot(1.0f);
        boolean ridden = entity.isVehicle();
        double animPos = entity.walkAnimation.position();
        double animSpeed = Math.min(entity.walkAnimation.speed(), 0.25f);
        double ageInTicks = entity.tickCount + 1.0f;
        boolean baby = entity.isBaby();
        boolean saddled = !entity.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.SADDLE).isEmpty();

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + 1.5f, pos.z)
                .rotateY(Mth.DEG_TO_RAD * (180f - bodyYaw))
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        double bodyZRot = 0.1f * Mth.sin(animPos * 1.5f) * 4.0f * animSpeed;
        double leftLegXRot = Mth.sin(animPos * 1.5f * 0.5f) * 2.0f * animSpeed;
        double rightLegXRot = Mth.sin(animPos * 1.5f * 0.5f + Mth.PI) * 2.0f * animSpeed;
        double leftLegZRot = (Math.PI / 18) * Mth.cos(animPos * 1.5f * 0.5f) * animSpeed;
        double rightLegZRot = (Math.PI / 18) * Mth.cos(animPos * 1.5f * 0.5f + Mth.PI) * animSpeed;
        double bristleFlow = Mth.cos(animPos * 1.5f + Mth.PI) * animSpeed;

        AnimParams params = new AnimParams(
                ridden, headYawRel, headPitch,
                bodyZRot, leftLegXRot, rightLegXRot, leftLegZRot, rightLegZRot,
                bristleFlow, ageInTicks, baby,
                animPos, animSpeed
        );

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderStriderPart(pipeline, buildRoot(entity), "root", base, params, block, sky);

        if (saddled) {
            ModelBakery.BakedPart saddle = getSaddleModel();
            renderStriderPart(pipeline, saddle, "root", base, params, block, sky);
        }
    }

    private void renderStriderPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                                   Matrix4d parent, AnimParams p,
                                   double block, double sky) {

        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        switch (name) {
            case "body":
                if (!p.ridden) {
                    mat.rotateX(p.headPitch);
                    mat.rotateY(p.headYaw);
                }
                mat.rotateZ(p.bodyZRot);
                double yBob = (p.baby ? -1.0f : -2.0f) * Mth.cos(p.animPos * 1.5f) * 2.0f * p.animSpeed;
                mat.translate(0, yBob / 16.0f, 0);
                break;
            case "right_leg":
                mat.rotateX(p.rightLegXRot);
                mat.rotateZ(p.rightLegZRot);
                double rightBob = 2.0f * Mth.sin(p.animPos * 1.5f * 0.5f) * 2.0f * p.animSpeed;
                mat.translate(0, rightBob / 16.0f, 0);
                break;
            case "left_leg":
                mat.rotateX(p.leftLegXRot);
                mat.rotateZ(p.leftLegZRot);
                double leftBob = 2.0f * Mth.sin(p.animPos * 1.5f * 0.5f + Mth.PI) * 2.0f * p.animSpeed;
                mat.translate(0, leftBob / 16.0f, 0);
                break;
            default:
                if (name.contains("bristle")) {
                    double mult = getBristleMultiplier(name, p.baby);
                    double additive = p.bristleFlow * mult;
                    double wiggle = 0;
                    if (name.equals("right_top_bristle") || name.equals("left_top_bristle") || name.equals("bristle2"))
                        wiggle = 0.1f * Mth.sin(p.ageInTicks * 0.4f);
                    else if (name.equals("right_middle_bristle") || name.equals("left_middle_bristle") || name.equals("bristle1"))
                        wiggle = 0.1f * Mth.sin(p.ageInTicks * 0.2f);
                    else
                        wiggle = 0.05f * Mth.sin(p.ageInTicks * -0.4f);
                    mat.rotateZ(additive + wiggle);
                }
                break;
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }
        for (var child : part.children.entrySet()) {
            renderStriderPart(pipeline, child.getValue(), child.getKey(), mat, p, block, sky);
        }
    }

    private double getBristleMultiplier(String name, boolean baby) {
        if (baby) {
            return switch (name) {
                case "bristle2" -> 0.6f;
                case "bristle1" -> 1.2f;
                case "bristle0" -> 1.3f;
                default -> 0f;
            };
        } else {
            if (name.startsWith("right_top") || name.startsWith("left_top")) return 0.6f;
            if (name.startsWith("right_middle") || name.startsWith("left_middle")) return 1.2f;
            if (name.startsWith("right_bottom") || name.startsWith("left_bottom")) return 1.3f;
            return 0f;
        }
    }

    private static class AnimParams {
        final boolean ridden;
        final double headYaw, headPitch;
        final double bodyZRot;
        final double leftLegXRot, rightLegXRot, leftLegZRot, rightLegZRot;
        final double bristleFlow, ageInTicks;
        final boolean baby;
        final double animPos, animSpeed;

        AnimParams(boolean ridden, double headYaw, double headPitch,
                   double bodyZRot, double leftLegXRot, double rightLegXRot,
                   double leftLegZRot, double rightLegZRot,
                   double bristleFlow, double ageInTicks, boolean baby,
                   double animPos, double animSpeed) {
            this.ridden = ridden;
            this.headYaw = headYaw;
            this.headPitch = headPitch;
            this.bodyZRot = bodyZRot;
            this.leftLegXRot = leftLegXRot;
            this.rightLegXRot = rightLegXRot;
            this.leftLegZRot = leftLegZRot;
            this.rightLegZRot = rightLegZRot;
            this.bristleFlow = bristleFlow;
            this.ageInTicks = ageInTicks;
            this.baby = baby;
            this.animPos = animPos;
            this.animSpeed = animSpeed;
        }
    }
}