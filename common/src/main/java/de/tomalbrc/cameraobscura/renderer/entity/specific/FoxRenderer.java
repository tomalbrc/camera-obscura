package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FoxRenderer implements LivingEntityRenderer<Fox> {

    private static final Map<FoxModelKey, ModelBakery.BakedPart> CACHE = new HashMap<>();

    @Override
    public ModelBakery.BakedPart buildRoot(Fox entity) {
        Fox.Variant variant = entity.getVariant();
        boolean baby = entity.isBaby();
        boolean sleeping = entity.isSleeping();
        FoxModelKey key = new FoxModelKey(variant, baby, sleeping);
        return CACHE.computeIfAbsent(key, this::buildModel);
    }

    private ModelBakery.BakedPart buildModel(FoxModelKey key) {
        String texture = getTexture(key);
        boolean baby = key.baby;
        return baby ? buildBabyModel(texture) : buildAdultModel(texture);
    }

    private String getTexture(FoxModelKey key) {
        String base = "entity/fox/";
        if (key.variant == Fox.Variant.SNOW) {
            base += "fox_snow";
        } else {
            base += "fox";
        }
        if (key.sleeping) {
            base += "_sleep";
        }
        if (key.baby) {
            base += "_baby";
        }
        return base;
    }

    private ModelBakery.BakedPart buildAdultModel(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 48, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition head = root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create().texOffs(1, 5).addBox(-3, -2, -5, 8, 6, 6),
                ModelBakery.PartPose.offset(-1, 16.5f, -3));
        head.addOrReplaceChild("right_ear",
                ModelBakery.CubeListBuilder.create().texOffs(8, 1).addBox(-3, -4, -4, 2, 2, 1),
                ModelBakery.PartPose.ZERO);
        head.addOrReplaceChild("left_ear",
                ModelBakery.CubeListBuilder.create().texOffs(15, 1).addBox(3, -4, -4, 2, 2, 1),
                ModelBakery.PartPose.ZERO);
        head.addOrReplaceChild("nose",
                ModelBakery.CubeListBuilder.create().texOffs(6, 18).addBox(-1, 2.01f, -8, 4, 2, 3),
                ModelBakery.PartPose.ZERO);

        ModelBakery.PartDefinition body = root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(24, 15).addBox(-3, 3.999f, -3.5f, 6, 11, 6),
                ModelBakery.PartPose.offsetAndRotation(0, 16, -6, Mth.PI / 2, 0, 0));

        ModelBakery.CubeDeformation fudge = new ModelBakery.CubeDeformation(0.001f);
        ModelBakery.CubeListBuilder leftLeg = ModelBakery.CubeListBuilder.create()
                .texOffs(4, 24).addBox(2, 0.5f, -1, 2, 6, 2, fudge);
        ModelBakery.CubeListBuilder rightLeg = ModelBakery.CubeListBuilder.create()
                .texOffs(13, 24).addBox(2, 0.5f, -1, 2, 6, 2, fudge);
        root.addOrReplaceChild("right_hind_leg", rightLeg, ModelBakery.PartPose.offset(-5, 17.5f, 7));
        root.addOrReplaceChild("left_hind_leg", leftLeg, ModelBakery.PartPose.offset(-1, 17.5f, 7));
        root.addOrReplaceChild("right_front_leg", rightLeg, ModelBakery.PartPose.offset(-5, 17.5f, 0));
        root.addOrReplaceChild("left_front_leg", leftLeg, ModelBakery.PartPose.offset(-1, 17.5f, 0));

        body.addOrReplaceChild("tail",
                ModelBakery.CubeListBuilder.create().texOffs(30, 0).addBox(2, 0, -1, 4, 9, 5),
                ModelBakery.PartPose.offsetAndRotation(-4, 15, -1, -0.05235988f, 0, 0));

        return root.bake();
    }

    private ModelBakery.BakedPart buildBabyModel(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 32, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3, -2.125f, -5.125f, 6, 5, 5)
                        .texOffs(18, 20).addBox(-1, 0.875f, -7.125f, 2, 2, 2)
                        .texOffs(22, 8).addBox(-3, -4.125f, -4.125f, 2, 2, 1)
                        .texOffs(22, 11).addBox(1, -4.125f, -4.125f, 2, 2, 1),
                ModelBakery.PartPose.offset(0, 18.125f, 0.125f));

        root.addOrReplaceChild("right_hind_leg",
                ModelBakery.CubeListBuilder.create().texOffs(22, 4).addBox(-1, 0, -1, 2, 2, 2),
                ModelBakery.PartPose.offset(-1.5f, 22, 4));
        root.addOrReplaceChild("left_hind_leg",
                ModelBakery.CubeListBuilder.create().texOffs(22, 0).addBox(-1, 0, -1, 2, 2, 2),
                ModelBakery.PartPose.offset(1.5f, 22, 4));
        root.addOrReplaceChild("right_front_leg",
                ModelBakery.CubeListBuilder.create().texOffs(22, 4).addBox(-1, 0, -1, 2, 2, 2),
                ModelBakery.PartPose.offset(-1.5f, 22, 0));
        root.addOrReplaceChild("left_front_leg",
                ModelBakery.CubeListBuilder.create().texOffs(22, 0).addBox(-1, 0, -1, 2, 2, 2),
                ModelBakery.PartPose.offset(1.5f, 22, 0));

        ModelBakery.PartDefinition body = root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(0, 10).addBox(-2.5f, -2, -3, 5, 4, 6),
                ModelBakery.PartPose.offset(0, 20, 2));
        body.addOrReplaceChild("tail",
                ModelBakery.CubeListBuilder.create().texOffs(0, 20).addBox(-1.5f, -1.48f, -1, 3, 3, 6),
                ModelBakery.PartPose.offset(0, -0.5f, 3));

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, Fox entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyYaw);
        double headYaw = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYaw);
        double headPitch = Mth.DEG_TO_RAD * entity.getXRot(1.0f);
        boolean baby = entity.isBaby();
        double ageScale = baby ? 0.5f : 1.0f;
        boolean crouching = entity.isCrouching();
        boolean sleeping = entity.isSleeping();
        boolean sitting = entity.isSitting();
        boolean faceplanted = entity.isFaceplanted();
        boolean pouncing = entity.isPouncing();
        double crouchAmount = entity.getCrouchAmount(1.0f);
        double headRoll = entity.getHeadRollAngle(1.0f);
        double walkPos = entity.walkAnimation.position();
        double walkSpeed = entity.walkAnimation.speed();
        double ageInTicks = entity.tickCount + 1.0f;

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y, pos.z)
                .rotateY(modelYaw);

        base.translate(0, 1.5f, 0);
        base.rotateY(Mth.PI);
        base.rotateX(Mth.PI);

        AnimParams params = new AnimParams(
                headYaw, headPitch, baby, ageScale,
                crouching, sleeping, sitting, faceplanted, pouncing,
                crouchAmount, headRoll, walkPos, walkSpeed, ageInTicks
        );

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderFoxPart(pipeline, buildRoot(entity), "root", base, params, block, sky);
    }

    private void renderFoxPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                               Matrix4d parent, AnimParams p,
                               double block, double sky) {

        if (p.sleeping && (name.contains("leg"))) return;

        double extraPivotX = 0, extraPivotY = 0, extraPivotZ = 0;
        if (p.sleeping) {
            switch (name) {
                case "body" -> {
                    if (!p.baby) {
                        extraPivotY = 5.0f / 16f;
                    } else {
                        extraPivotX = -1.0f / 16f;
                        extraPivotY = 1.0f / 16f;
                        extraPivotZ = -1.0f / 16f;
                    }
                }
                case "head" -> {
                    if (!p.baby) {
                        extraPivotX = 2.0f / 16f;
                        extraPivotY = 2.99f / 16f;
                    } else {
                        extraPivotX = -2.0f / 16f;
                        extraPivotY = 2.8f / 16f;
                        extraPivotZ = -4.0f / 16f;
                    }
                }
                case "tail" -> {
                    if (p.baby) {
                        extraPivotX = -0.7f / 16f;
                        extraPivotY = 0.9f / 16f;
                        extraPivotZ = 0.6f / 16f;
                    }
                }
            }
        }

        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x + extraPivotX,
                part.localPivot.y + extraPivotY,
                part.localPivot.z + extraPivotZ);

        if (p.sleeping) {
            switch (name) {
                case "body" -> mat.rotateZ((-Math.PI / 2));
                case "head" -> {
                    mat.rotateY(-Math.PI * 2.0 / 3.0);
                    mat.rotateZ(Mth.cos(p.ageInTicks * 0.027f) / 22.0f);
                }
                case "tail" -> {
                    if (!p.baby) {
                        mat.rotateX(-Math.PI * 5.0 / 6.0);
                    } else {
                        mat.rotateX(-2.1816616f);
                    }
                }
            }
        }

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        applyFoxPose(mat, name, p);

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }

        for (var child : part.children.entrySet()) {
            renderFoxPart(pipeline, child.getValue(), child.getKey(), mat, p, block, sky);
        }
    }

    private void applyFoxPose(Matrix4d mat, String name, AnimParams p) {
        if (name.equals("head")) {
            if (!p.sleeping) {
                if (p.sitting) {
                    mat.rotateY(0);
                    mat.rotateX(0);
                } else if (!p.faceplanted && !p.crouching) {
                    mat.rotateY(p.headYaw);
                    mat.rotateX(p.headPitch);
                    mat.rotateZ(p.headRoll);
                } else if (p.faceplanted) {
                    mat.rotateY(0);
                    mat.rotateX(0);
                } else if (p.crouching) {
                    mat.rotateY(p.headYaw);
                    mat.rotateX(p.headPitch);
                    mat.rotateZ(p.headRoll);
                    mat.translate(0, p.crouchAmount * p.ageScale / 16f, 0);
                }
            }
        } else if (name.equals("body")) {
            if (p.crouching) {
                mat.rotateX(0.10471976f);
                mat.rotateY(Mth.cos(p.ageInTicks) * 0.05f);
                if (!p.baby) {
                    mat.translate(0, p.crouchAmount / 16f, 0);
                } else {
                    mat.translate(0, p.crouchAmount / 6f / 16f, 0);
                }
            } else if (p.sitting) {
                if (!p.baby) {
                    mat.rotateX(Math.PI / 6);
                    mat.translate(0, -7.0f / 16f, 3.0f / 16f);
                } else {
                    mat.rotateX(-0.959931f);
                    mat.translate(0, 3.0f * p.ageScale / 16f, -4.5f * p.ageScale / 16f);
                }
            } else if (p.pouncing) {
                if (!p.baby) {
                    double crouch = p.crouchAmount / 2.0f;
                    mat.translate(0, -crouch / 16f, 0);
                }
            }
        } else if (name.equals("tail")) {
            if (!p.sleeping) {
                if (p.sitting) {
                    if (!p.baby) {
                        mat.rotateX(Math.PI / 4);
                        mat.translate(0, 0, -1.0f / 16f);
                    } else {
                        mat.rotateX(0.95993114f);
                        mat.translate(0, -0.6f / 16f, -2.0f * p.ageScale / 16f);
                    }
                }
            }
        } else if (name.contains("leg")) {
            if (p.sitting) {
                if (!p.baby) {
                    if (name.contains("front")) {
                        mat.rotateX(-Math.PI / 12);
                    } else {
                        mat.rotateX(-Math.PI * 5.0 / 12.0);
                        mat.translate(0, 4.0f / 16f, -0.25f / 16f);
                    }
                } else {
                    if (name.contains("front")) {
                        mat.rotateX(-Math.PI / 12);
                        mat.translate(0, 0, -1.0f / 16f);
                    } else {
                        mat.translate(0, 0, -3.75f / 16f);
                    }
                    if (name.contains("right")) mat.translate(0.01f / 16f, 0, 0);
                    else mat.translate(-0.01f / 16f, 0, 0);
                }
            } else if (!p.sleeping) {
                double swingPos = p.walkPos;
                double speed = p.walkSpeed;
                if (p.faceplanted) {
                    speed = 0.1f;
                    swingPos += p.ageInTicks * 0.67f;
                }

                double angleRightHind, angleLeftHind, angleRightFront, angleLeftFront;
                angleRightHind = Mth.cos(swingPos * 0.6662f) * 1.4f * speed;
                angleLeftHind = Mth.cos(swingPos * 0.6662f + Mth.PI) * 1.4f * speed;
                angleRightFront = Mth.cos(swingPos * 0.6662f + Mth.PI) * 1.4f * speed;
                angleLeftFront = Mth.cos(swingPos * 0.6662f) * 1.4f * speed;

                switch (name) {
                    case "right_hind_leg" -> mat.rotateX(angleRightHind);
                    case "left_hind_leg" -> mat.rotateX(angleLeftHind);
                    case "right_front_leg" -> mat.rotateX(angleRightFront);
                    case "left_front_leg" -> mat.rotateX(angleLeftFront);
                }

                if (p.crouching) {
                    double wiggle = Mth.cos(p.ageInTicks) * 0.05f;
                    if (name.contains("hind")) mat.rotateZ(wiggle);
                    else mat.rotateZ(wiggle / 2.0f);
                }
            }
        }

        if (p.pouncing && name.equals("head")) {
            mat.translate(0, -p.crouchAmount / 2f / 16f, 0);
        }

        if (p.sitting && name.equals("head") && p.baby) {
            mat.translate(0, -0.75f / 16f, 0);
        }
    }

    private static class AnimParams {
        final double headYaw, headPitch;
        final boolean baby;
        final double ageScale;
        final boolean crouching, sleeping, sitting, faceplanted, pouncing;
        final double crouchAmount, headRoll;
        final double walkPos, walkSpeed, ageInTicks;

        AnimParams(double headYaw, double headPitch, boolean baby, double ageScale,
                   boolean crouching, boolean sleeping, boolean sitting, boolean faceplanted, boolean pouncing,
                   double crouchAmount, double headRoll, double walkPos, double walkSpeed, double ageInTicks) {
            this.headYaw = headYaw;
            this.headPitch = headPitch;
            this.baby = baby;
            this.ageScale = ageScale;
            this.crouching = crouching;
            this.sleeping = sleeping;
            this.sitting = sitting;
            this.faceplanted = faceplanted;
            this.pouncing = pouncing;
            this.crouchAmount = crouchAmount;
            this.headRoll = headRoll;
            this.walkPos = walkPos;
            this.walkSpeed = walkSpeed;
            this.ageInTicks = ageInTicks;
        }
    }

    private static class FoxModelKey {
        final Fox.Variant variant;
        final boolean baby;
        final boolean sleeping;

        FoxModelKey(Fox.Variant variant, boolean baby, boolean sleeping) {
            this.variant = variant;
            this.baby = baby;
            this.sleeping = sleeping;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FoxModelKey key)) return false;
            return baby == key.baby && sleeping == key.sleeping && variant == key.variant;
        }

        @Override
        public int hashCode() {
            return Objects.hash(variant, baby, sleeping);
        }
    }
}