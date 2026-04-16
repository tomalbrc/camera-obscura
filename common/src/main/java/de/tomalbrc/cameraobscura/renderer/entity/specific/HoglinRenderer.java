package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

import java.util.Map;

public class HoglinRenderer<T extends LivingEntity> implements LivingEntityRenderer<T> {
    private static final Map<EntityType<?>, String[]> TEXTURES = Map.of(
            EntityType.HOGLIN, new String[]{"entity/hoglin/hoglin", "entity/hoglin/hoglin_baby"},
            EntityType.ZOGLIN, new String[]{"entity/hoglin/zoglin", "entity/hoglin/zoglin_baby"}
    );

    private ModelBakery.BakedPart cachedAdult;
    private ModelBakery.BakedPart cachedBaby;

    @Override
    public ModelBakery.BakedPart buildRoot(T entity) {
        String[] paths = TEXTURES.get(entity.getType());
        if (paths == null) paths = new String[]{"entity/hoglin/hoglin", "entity/hoglin/hoglin_baby"};
        String adultTex = paths[0];
        String babyTex = paths[1];
        if (entity.isBaby()) {
            if (cachedBaby == null) cachedBaby = buildBabyModel(babyTex);
            return cachedBaby;
        } else {
            if (cachedAdult == null) cachedAdult = buildAdultModel(adultTex);
            return cachedAdult;
        }
    }

    private ModelBakery.BakedPart buildAdultModel(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 128, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition body = root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(1, 1).addBox(-8, -7, -13, 16, 14, 26),
                ModelBakery.PartPose.offset(0, 7, 0));

        body.addOrReplaceChild("mane",
                ModelBakery.CubeListBuilder.create().texOffs(90, 33).addBox(0, 0, -9, 0, 10, 19, new ModelBakery.CubeDeformation(0.001f)),
                ModelBakery.PartPose.offset(0, -14, -7));

        ModelBakery.PartDefinition head = root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create().texOffs(61, 1).addBox(-7, -3, -19, 14, 6, 19),
                ModelBakery.PartPose.offset(0, 2, -12));
        head.addOrReplaceChild("right_ear",
                ModelBakery.CubeListBuilder.create().texOffs(1, 1).addBox(-6, -1, -2, 6, 1, 4),
                ModelBakery.PartPose.offsetAndRotation(-6, -2, -3, 0, 0, (-Mth.PI * 2.0f / 9.0f)));
        head.addOrReplaceChild("left_ear",
                ModelBakery.CubeListBuilder.create().texOffs(1, 6).addBox(0, -1, -2, 6, 1, 4),
                ModelBakery.PartPose.offsetAndRotation(6, -2, -3, 0, 0, (Mth.PI * 2.0f / 9.0f)));

        head.addOrReplaceChild("right_horn",
                ModelBakery.CubeListBuilder.create().texOffs(10, 13).addBox(-1, -11, -1, 2, 11, 2),
                ModelBakery.PartPose.offset(-7, 2, -12));
        head.addOrReplaceChild("left_horn",
                ModelBakery.CubeListBuilder.create().texOffs(1, 13).addBox(-1, -11, -1, 2, 11, 2),
                ModelBakery.PartPose.offset(7, 2, -12));


        root.addOrReplaceChild("right_front_leg",
                ModelBakery.CubeListBuilder.create().texOffs(66, 42).addBox(-3, 0, -3, 6, 14, 6),
                ModelBakery.PartPose.offset(-4, 10, -8.5f));
        root.addOrReplaceChild("left_front_leg",
                ModelBakery.CubeListBuilder.create().texOffs(41, 42).addBox(-3, 0, -3, 6, 14, 6),
                ModelBakery.PartPose.offset(4, 10, -8.5f));
        root.addOrReplaceChild("right_hind_leg",
                ModelBakery.CubeListBuilder.create().texOffs(21, 45).addBox(-2.5f, 0, -2.5f, 5, 11, 5),
                ModelBakery.PartPose.offset(-5, 13, 10));
        root.addOrReplaceChild("left_hind_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 45).addBox(-2.5f, 0, -2.5f, 5, 11, 5),
                ModelBakery.PartPose.offset(5, 13, 10));

        return root.bake();
    }

    private ModelBakery.BakedPart buildBabyModel(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 64, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();


        ModelBakery.PartDefinition head = root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-5, -2.2605f, -10.547f, 10, 4, 12)
                        .texOffs(44, 29).addBox(-7, -4.0981f, -8.4879f, 2, 5, 2)
                        .texOffs(52, 29).addBox(5, -4.0981f, -8.4879f, 2, 5, 2),
                ModelBakery.PartPose.offset(0, 13, -7));

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-4, -14, -7, 8, 8, 14, new ModelBakery.CubeDeformation(0.02f))
                        .texOffs(24, 39).addBox(0, -18, -8, 0, 6, 11, new ModelBakery.CubeDeformation(0.02f)),
                ModelBakery.PartPose.offset(0, 24, 0));

        head.addOrReplaceChild("right_ear",
                ModelBakery.CubeListBuilder.create().texOffs(32, 5).addBox(-5.1f, -0.5f, -2, 6, 1, 4),
                ModelBakery.PartPose.offsetAndRotation(-5, -1, -1.5f, 0, 0, -0.8727f));
        head.addOrReplaceChild("left_ear",
                ModelBakery.CubeListBuilder.create().texOffs(32, 0).mirror().addBox(-0.9f, -0.5f, -2, 6, 1, 4).mirror(false),
                ModelBakery.PartPose.offsetAndRotation(5, -1, -1.5f, 0, 0, 0.8727f));

        root.addOrReplaceChild("right_hind_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 47).addBox(-1.5f, 0, -1.5f, 3, 6, 3),
                ModelBakery.PartPose.offset(-2.5f, 18, 4.5f));
        root.addOrReplaceChild("left_hind_leg",
                ModelBakery.CubeListBuilder.create().texOffs(12, 47).addBox(-1.5f, 0, -1.5f, 3, 6, 3),
                ModelBakery.PartPose.offset(2.5f, 18, 4.5f));
        root.addOrReplaceChild("right_front_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 38).addBox(-1.5f, 0, -1.5f, 3, 6, 3),
                ModelBakery.PartPose.offset(-2.5f, 18, -4.5f));
        root.addOrReplaceChild("left_front_leg",
                ModelBakery.CubeListBuilder.create().texOffs(12, 38).addBox(-1.5f, 0, -1.5f, 3, 6, 3),
                ModelBakery.PartPose.offset(2.5f, 18, -4.5f));

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, T entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyYaw);
        double headYaw = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYaw);
        boolean baby = entity.isBaby();
        double animPos = entity.walkAnimation.position();
        double animSpeed = entity.walkAnimation.speed();
        double attackTicks = 0;
        if (entity instanceof Hoglin hoglin) {
            attackTicks = hoglin.getAttackAnimationRemainingTicks();
        } else if (entity instanceof Zoglin zoglin) {
            attackTicks = zoglin.getAttackAnimationRemainingTicks();
        }
        double headbuttFactor = 1.0f - Math.abs(10 - 2 * attackTicks) / 10.0f;

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y, pos.z)
                .rotateY(modelYaw);

        base.translate(0.0f, 1.5f, 0.0f);
        base.rotateY(Mth.PI);
        base.rotateX(Mth.PI);

        double frontLeftAngle = Mth.cos(animPos * 0.6662f) * 1.2f * animSpeed;
        double frontRightAngle = Mth.cos(animPos * 0.6662f + Mth.PI) * 1.2f * animSpeed;

        double earZRot = Mth.sin(animPos) * animSpeed;

        AnimParams params = new AnimParams(
                headYaw, headbuttFactor, baby,
                frontLeftAngle, frontRightAngle, frontRightAngle, frontLeftAngle,
                earZRot, animSpeed
        );

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderPart(pipeline, buildRoot(entity), "root", base, params, block, sky);
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
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
            case "head":
                mat.rotateY(p.headYaw);

                double headXRot = Mth.lerp(p.headbuttFactor, 0.87266463f, (-Math.PI / 9));
                if (p.baby && p.headbuttFactor > 0) {

                    mat.translate(0.0f, p.headbuttFactor * 2.5f / 16.0f, 0.0f);
                }
                mat.rotateX(headXRot);
                break;
            case "right_ear":
                double baseRightEarZ = p.baby ? -0.8727f : (-Math.PI * 2.0 / 9.0);
                mat.rotateZ(baseRightEarZ - p.earZRot * p.animSpeed);
                break;
            case "left_ear":
                double baseLeftEarZ = p.baby ? 0.8727f : (Math.PI * 2.0 / 9.0);
                mat.rotateZ(baseLeftEarZ + p.earZRot * p.animSpeed);
                break;
            case "right_front_leg":
                mat.rotateX(p.frAng);
                break;
            case "left_front_leg":
                mat.rotateX(p.flAng);
                break;
            case "right_hind_leg":
                mat.rotateX(p.hrAng);
                break;
            case "left_hind_leg":
                mat.rotateX(p.hlAng);
                break;

            default:
                break;
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }

        for (var child : part.children.entrySet()) {
            renderPart(pipeline, child.getValue(), child.getKey(), mat, p, block, sky);
        }
    }

    private static class AnimParams {
        final double headYaw, headbuttFactor;
        final boolean baby;
        final double flAng, frAng, hlAng, hrAng;
        final double earZRot, animSpeed;

        AnimParams(double headYaw, double headbuttFactor, boolean baby,
                   double flAng, double frAng, double hlAng, double hrAng,
                   double earZRot, double animSpeed) {
            this.headYaw = headYaw;
            this.headbuttFactor = headbuttFactor;
            this.baby = baby;
            this.flAng = flAng;
            this.frAng = frAng;
            this.hlAng = hlAng;
            this.hrAng = hrAng;
            this.earZRot = earZRot;
            this.animSpeed = animSpeed;
        }
    }
}