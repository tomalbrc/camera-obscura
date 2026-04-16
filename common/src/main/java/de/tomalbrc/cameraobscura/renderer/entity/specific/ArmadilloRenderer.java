package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.animation.AnimationChannel;
import de.tomalbrc.cameraobscura.renderer.animation.AnimationDefinition;
import de.tomalbrc.cameraobscura.renderer.animation.ArmadilloAnimation;
import de.tomalbrc.cameraobscura.renderer.animation.Keyframe;
import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;
import org.joml.Vector3d;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArmadilloRenderer implements LivingEntityRenderer<Armadillo> {

    private static final String ADULT_TEXTURE = "entity/armadillo/armadillo";
    private static final String BABY_TEXTURE = "entity/armadillo/armadillo_baby";
    private final Map<Boolean, ModelBakery.BakedPart> cache = new HashMap<>();

    private static Map<String, PartTransform> evaluateAnim(AnimationDefinition def, double time) {
        Map<String, PartTransform> result = new HashMap<>();
        for (var entry : def.channels.entrySet()) {
            String bone = entry.getKey();
            List<AnimationChannel> channels = entry.getValue();
            Vector3d pos = new Vector3d();
            Vector3d rot = new Vector3d();
            boolean hasPos = false, hasRot = false;

            double clampedTime = def.looping ? time % def.length : Math.min(time, def.length);
            for (AnimationChannel channel : channels) {
                Keyframe[] keyframes = channel.keyframes();
                if (keyframes.length == 0) continue;
                int idx = 0;
                while (idx < keyframes.length - 1 && keyframes[idx + 1].timestamp() <= clampedTime) idx++;
                int next = Math.min(idx + 1, keyframes.length - 1);
                Keyframe prev = keyframes[idx];
                Keyframe nextKf = keyframes[next];
                double delta = (clampedTime - prev.timestamp()) / (nextKf.timestamp() - prev.timestamp() + 1e-6f);
                delta = Mth.clamp(delta, 0, 1);

                Vector3d value = new Vector3d();
                nextKf.interpolation().apply(value, delta, keyframes, idx, next, 1.0f);

                if (channel.target() == AnimationChannel.Targets.POSITION) {
                    pos = value;
                    hasPos = true;
                } else if (channel.target() == AnimationChannel.Targets.ROTATION) {
                    rot = value;
                    hasRot = true;
                }
            }
            if (hasPos || hasRot) {
                result.put(bone, new PartTransform(
                        hasPos ? pos : new Vector3d(),
                        hasRot ? rot : new Vector3d()
                ));
            }
        }
        return result;
    }

    @Override
    public ModelBakery.BakedPart buildRoot(Armadillo entity) {
        boolean baby = entity.isBaby();
        return cache.computeIfAbsent(baby, b -> b ? buildBaby() : buildAdult());
    }

    private ModelBakery.BakedPart buildAdult() {
        ModelBakery bakery = new ModelBakery(ADULT_TEXTURE, 64, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition body = root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 20).addBox(-4, -7, -10, 8, 8, 12, new ModelBakery.CubeDeformation(0.3F))
                        .texOffs(0, 40).addBox(-4, -7, -10, 8, 8, 12),
                ModelBakery.PartPose.offset(0, 21, 4));

        body.addOrReplaceChild("tail",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(44, 53).addBox(-0.5f, -0.0865f, 0.0933f, 1, 6, 1),
                ModelBakery.PartPose.offsetAndRotation(0, -3, 1, 0.5061f, 0, 0));

        ModelBakery.PartDefinition head = body.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create(), ModelBakery.PartPose.offset(0, -2, -11));
        head.addOrReplaceChild("head_cube",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(43, 15).addBox(-1.5f, -1, -1, 3, 5, 2),
                ModelBakery.PartPose.offsetAndRotation(0, 0, 0, -0.3927f, 0, 0));

        ModelBakery.PartDefinition rightEar = head.addOrReplaceChild("right_ear",
                ModelBakery.CubeListBuilder.create(), ModelBakery.PartPose.offset(-1, -1, 0));
        rightEar.addOrReplaceChild("right_ear_cube",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(43, 10).addBox(-2, -3, 0, 2, 5, 0),
                ModelBakery.PartPose.offsetAndRotation(-0.5f, 0, -0.6f, 0.1886f, -0.3864f, -0.0718f));

        ModelBakery.PartDefinition leftEar = head.addOrReplaceChild("left_ear",
                ModelBakery.CubeListBuilder.create(), ModelBakery.PartPose.offset(1, -2, 0));
        leftEar.addOrReplaceChild("left_ear_cube",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(47, 10).addBox(0, -3, 0, 2, 5, 0),
                ModelBakery.PartPose.offsetAndRotation(0.5f, 1, -0.6f, 0.1886f, 0.3864f, 0.0718f));

        root.addOrReplaceChild("right_hind_leg",
                ModelBakery.CubeListBuilder.create().texOffs(51, 31).addBox(-1, 0, -1, 2, 3, 2),
                ModelBakery.PartPose.offset(-2, 21, 4));
        root.addOrReplaceChild("left_hind_leg",
                ModelBakery.CubeListBuilder.create().texOffs(42, 31).addBox(-1, 0, -1, 2, 3, 2),
                ModelBakery.PartPose.offset(2, 21, 4));
        root.addOrReplaceChild("right_front_leg",
                ModelBakery.CubeListBuilder.create().texOffs(51, 43).addBox(-1, 0, -1, 2, 3, 2),
                ModelBakery.PartPose.offset(-2, 21, -4));
        root.addOrReplaceChild("left_front_leg",
                ModelBakery.CubeListBuilder.create().texOffs(42, 43).addBox(-1, 0, -1, 2, 3, 2),
                ModelBakery.PartPose.offset(2, 21, -4));

        root.addOrReplaceChild("cube",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-5, -10, -6, 10, 10, 10),
                ModelBakery.PartPose.offset(0, 24, 0));

        return root.bake();
    }

    private ModelBakery.BakedPart buildBaby() {
        ModelBakery bakery = new ModelBakery(BABY_TEXTURE, 64, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition body = root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.5f, -2, -3.5f, 5, 4, 7, new ModelBakery.CubeDeformation(0.3F))
                        .texOffs(0, 11).addBox(-2.5f, -2, -3, 5, 4, 6),
                ModelBakery.PartPose.offset(0, 20, 0.5f));

        body.addOrReplaceChild("tail", ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(0, 0, 3.4f));

        ModelBakery.PartDefinition head = body.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create(), ModelBakery.PartPose.offset(0, 0, -3.2f));
        head.addOrReplaceChild("head_cube",
                ModelBakery.CubeListBuilder.create().texOffs(20, 17).addBox(-1, -2, -4, 2, 2, 4),
                ModelBakery.PartPose.offsetAndRotation(0, 0, 0, 0.7417649f, 0, 0));

        head.addOrReplaceChild("right_ear",
                ModelBakery.CubeListBuilder.create().texOffs(28, 8).mirror().addBox(-1.8f, -2, 0, 2, 3, 0).mirror(false),
                ModelBakery.PartPose.offsetAndRotation(-1, -2, -0.3f, -0.4363f, -0.1134f, 0.0524f));
        head.addOrReplaceChild("left_ear",
                ModelBakery.CubeListBuilder.create().texOffs(28, 8).addBox(-0.2f, -2, 0, 2, 3, 0),
                ModelBakery.PartPose.offsetAndRotation(1, -2, -0.3f, -0.4363f, 0.1134f, -0.0524f));

        root.addOrReplaceChild("right_hind_leg",
                ModelBakery.CubeListBuilder.create().texOffs(20, 27).mirror().addBox(-1, 0, -1, 2, 2, 2).mirror(false),
                ModelBakery.PartPose.offset(-1.5f, 22, 2.5f));
        root.addOrReplaceChild("left_hind_leg",
                ModelBakery.CubeListBuilder.create().texOffs(20, 27).addBox(-1, 0, -1, 2, 2, 2),
                ModelBakery.PartPose.offset(1.5f, 22, 2.5f));
        root.addOrReplaceChild("right_front_leg",
                ModelBakery.CubeListBuilder.create().texOffs(20, 23).addBox(-1, 0, -1, 2, 2, 2),
                ModelBakery.PartPose.offset(1.5f, 22, -1.5f));
        root.addOrReplaceChild("left_front_leg",
                ModelBakery.CubeListBuilder.create().texOffs(24, 0).mirror().addBox(-1, 0, -1, 2, 2, 2).mirror(false),
                ModelBakery.PartPose.offset(-1.5f, 22, -1.5f));

        root.addOrReplaceChild("cube",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 25).addBox(-3, -3, -3, 6, 6, 6, new ModelBakery.CubeDeformation(0.3F)),
                ModelBakery.PartPose.offset(0, 20.7f, 0.5f));

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, Armadillo entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1f);
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyYaw);
        double headYawRel = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYaw);
        double headPitch = Mth.DEG_TO_RAD * entity.getXRot(1f);

        boolean baby = entity.isBaby();
        boolean hiding = entity.shouldHideInShell();

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + 1.5f, pos.z)
                .rotateY(modelYaw)
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        double animPos = entity.walkAnimation.position();
        double animSpeed = entity.walkAnimation.speed();
        float partialTicks = 1.0f;

        AnimationDefinition activeAnim = null;
        double animTime = 0;
        if (entity.rollUpAnimationState.isStarted()) {
            activeAnim = ArmadilloAnimation.ARMADILLO_ROLL_UP;
            animTime = entity.rollUpAnimationState.getTimeInMillis(partialTicks) / 1000f;
        } else if (entity.rollOutAnimationState.isStarted()) {
            activeAnim = ArmadilloAnimation.ARMADILLO_ROLL_OUT;
            animTime = entity.rollOutAnimationState.getTimeInMillis(partialTicks) / 1000f;
        } else if (entity.peekAnimationState.isStarted()) {
            activeAnim = ArmadilloAnimation.ARMADILLO_PEEK;
            animTime = entity.peekAnimationState.getTimeInMillis(partialTicks) / 1000f;
        }

        Map<String, PartTransform> animTransforms = Collections.emptyMap();
        //        Map<String, PartTransform> animTransforms = activeAnim != null
        //                ? evaluateAnim(activeAnim, animTime)
        //                : Collections.emptyMap();

        double limbSwing = animPos * 0.6662f;
        double rh = Mth.cos(limbSwing) * 1.4f * animSpeed;
        double lh = Mth.cos(limbSwing + Mth.PI) * 1.4f * animSpeed;
        double rf = Mth.cos(limbSwing + Mth.PI) * 1.4f * animSpeed;
        double lf = Mth.cos(limbSwing) * 1.4f * animSpeed;

        double bodyZRot = 0.05f * animSpeed * Mth.sin(limbSwing * 2);
        Matrix4d walkBase = new Matrix4d(base).rotateZ(bodyZRot);

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        ModelBakery.BakedPart rootPart = buildRoot(entity);
        renderPart(
                pipeline, rootPart, "root", walkBase,
                headYawRel, headPitch,
                rh, lh, rf, lf,
                hiding, animTransforms,
                block, sky
        );
    }

    private void renderPart(RenderPipeline pipeline,
                            ModelBakery.BakedPart part,
                            String name,
                            Matrix4d parent,
                            double headYaw, double headPitch,
                            double rh, double lh, double rf, double lf,
                            boolean hiding,
                            Map<String, PartTransform> animTransforms,
                            double block, double sky) {

        if (hiding && !name.equals("cube") && !name.equals("root")) return;
        else if (!hiding && name.equals("cube")) return;

        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        PartTransform anim = animTransforms.get(name);
        if (anim != null) {
            mat.translate(anim.pos.x / 16f, anim.pos.y / 16f, anim.pos.z / 16f);
            mat.rotateZYX(anim.rot.z, anim.rot.y, anim.rot.x);
        }

        switch (name) {
            case "head" -> {
                mat.rotateY(headYaw);
                mat.rotateX(headPitch);
            }
            case "right_hind_leg" -> mat.rotateX(rh);
            case "left_hind_leg" -> mat.rotateX(lh);
            case "right_front_leg" -> mat.rotateX(rf);
            case "left_front_leg" -> mat.rotateX(lf);
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }

        for (var child : part.children.entrySet()) {
            renderPart(pipeline, child.getValue(), child.getKey(), mat,
                    headYaw, headPitch, rh, lh, rf, lf, hiding, animTransforms, block, sky);
        }
    }

    public record PartTransform(Vector3d pos, Vector3d rot) {
    }
}