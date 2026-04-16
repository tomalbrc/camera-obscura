package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

import java.util.HashMap;
import java.util.Map;

public class CatRenderer implements LivingEntityRenderer<Cat> {
    private static final Map<String, ModelBakery.BakedPart> CACHE = new HashMap<>();

    @Override
    public ModelBakery.BakedPart buildRoot(Cat entity) {
        String texture = entity.getVariant().value().assetInfo(entity.isBaby()).id().toString();
        String key = (entity.isBaby() ? "baby_" : "") + texture;
        return CACHE.computeIfAbsent(key, k -> entity.isBaby()
                ? buildBabyModel(texture)
                : buildAdultModel(texture));
    }

    private ModelBakery.BakedPart buildAdultModel(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 64, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .addBox("main", -2.5f, -2.0f, -3.0f, 5.0f, 4.0f, 5.0f, ModelBakery.CubeDeformation.NONE, 0, 0)
                        .addBox("nose", -1.5f, -0.001f, -4.0f, 3, 2, 2, ModelBakery.CubeDeformation.NONE, 0, 24)
                        .addBox("ear1", -2.0f, -3.0f, 0.0f, 1, 1, 2, ModelBakery.CubeDeformation.NONE, 0, 10)
                        .addBox("ear2", 1.0f, -3.0f, 0.0f, 1, 1, 2, ModelBakery.CubeDeformation.NONE, 6, 10),
                ModelBakery.PartPose.offset(0.0f, 15.0f, -9.0f));

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(20, 0).addBox(-2.0f, 3.0f, -8.0f, 4.0f, 16.0f, 6.0f),
                ModelBakery.PartPose.offsetAndRotation(0.0f, 12.0f, -10.0f, Mth.PI / 2, 0.0f, 0.0f));

        root.addOrReplaceChild("tail1",
                ModelBakery.CubeListBuilder.create().texOffs(0, 15).addBox(-0.5f, 0.0f, 0.0f, 1.0f, 8.0f, 1.0f),
                ModelBakery.PartPose.offset(0.0f, 15.0f, 8.0f));
        root.addOrReplaceChild("tail2",
                ModelBakery.CubeListBuilder.create().texOffs(4, 15).addBox(-0.5f, 0.0f, 0.0f, 1.0f, 8.0f, 1.0f, new ModelBakery.CubeDeformation(-0.02f)),
                ModelBakery.PartPose.offset(0.0f, 20.0f, 14.0f));

        ModelBakery.CubeListBuilder hindLeg = ModelBakery.CubeListBuilder.create().texOffs(8, 13).addBox(-1.0f, 0.0f, 1.0f, 2.0f, 6.0f, 2.0f);
        root.addOrReplaceChild("left_hind_leg", hindLeg, ModelBakery.PartPose.offset(1.1f, 18.0f, 5.0f));
        root.addOrReplaceChild("right_hind_leg", hindLeg, ModelBakery.PartPose.offset(-1.1f, 18.0f, 5.0f));
        ModelBakery.CubeListBuilder frontLeg = ModelBakery.CubeListBuilder.create().texOffs(40, 0).addBox(-1.0f, 0.0f, 0.0f, 2.0f, 10.0f, 2.0f);
        root.addOrReplaceChild("left_front_leg", frontLeg, ModelBakery.PartPose.offset(1.2f, 14.1f, -5.0f));
        root.addOrReplaceChild("right_front_leg", frontLeg, ModelBakery.PartPose.offset(-1.2f, 14.1f, -5.0f));

        return root.bake();
    }

    private ModelBakery.BakedPart buildBabyModel(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 32, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.5f, -3.0f, -2.875f, 5.0f, 4.0f, 4.0f)
                        .texOffs(18, 0).addBox(-2.0f, -4.0f, -0.875f, 1.0f, 1.0f, 2.0f)
                        .texOffs(24, 0).addBox(1.0f, -4.0f, -0.875f, 1.0f, 1.0f, 2.0f)
                        .texOffs(18, 3).addBox(-1.5f, -1.0f, -3.875f, 3.0f, 2.0f, 1.0f),
                ModelBakery.PartPose.offset(0.0f, 20.0f, -3.125f));


        root.addOrReplaceChild("left_front_leg", ModelBakery.CubeListBuilder.create().texOffs(18, 18).addBox(-0.5f, 0.0f, -1.0f, 1.0f, 2.0f, 2.0f), ModelBakery.PartPose.offset(1.0f, 22.0f, -1.5f));
        root.addOrReplaceChild("right_front_leg", ModelBakery.CubeListBuilder.create().texOffs(12, 18).addBox(-0.5f, 0.0f, -1.0f, 1.0f, 2.0f, 2.0f), ModelBakery.PartPose.offset(-1.0f, 22.0f, -1.5f));
        root.addOrReplaceChild("left_hind_leg", ModelBakery.CubeListBuilder.create().texOffs(18, 22).addBox(-0.5f, 0.0f, -1.0f, 1.0f, 2.0f, 2.0f), ModelBakery.PartPose.offset(1.0f, 22.0f, 2.5f));
        root.addOrReplaceChild("right_hind_leg", ModelBakery.CubeListBuilder.create().texOffs(12, 22).addBox(-0.5f, 0.0f, -1.0f, 1.0f, 2.0f, 2.0f), ModelBakery.PartPose.offset(-1.0f, 22.0f, 2.5f));

        root.addOrReplaceChild("body", ModelBakery.CubeListBuilder.create().texOffs(0, 8).addBox(-2.0f, -1.5f, -3.5f, 4.0f, 3.0f, 7.0f), ModelBakery.PartPose.offset(0.0f, 20.5f, 0.5f));

        root.addOrReplaceChild("tail1", ModelBakery.CubeListBuilder.create().texOffs(0, 18).addBox(-0.5f, -0.107f, 0.0849f, 1.0f, 1.0f, 5.0f), ModelBakery.PartPose.offset(0.0f, 19.107f, 3.9151f));
        root.addOrReplaceChild("tail2", ModelBakery.CubeListBuilder.create(), ModelBakery.PartPose.ZERO);

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, Cat entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyYaw);
        double headYaw = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYaw);
        double headPitch = Mth.DEG_TO_RAD * entity.getXRot(1.0f);
        boolean baby = entity.isBaby();
        boolean crouching = entity.isCrouching();
        boolean sprinting = entity.isSprinting();
        boolean sitting = entity.isInSittingPose();
        double lieDownAmount = entity.getLieDownAmount(1.0f);
        double lieDownTail = entity.getLieDownAmountTail(1.0f);
        double relaxOne = entity.getRelaxStateOneAmount(1.0f);
        double animPos = entity.walkAnimation.position();
        double animSpeed = entity.walkAnimation.speed();

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y, pos.z)
                .rotateY(modelYaw);
        base.translate(0.0f, 1.5f, 0.0f);
        base.rotateY(Mth.PI);
        base.rotateX(Mth.PI);

        AnimState state = new AnimState(
                headYaw, headPitch, animPos, animSpeed, crouching, sprinting, sitting,
                lieDownAmount, lieDownTail, relaxOne, 1, baby
        );

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        int tint = LivingEntityRenderer.hurtTint(entity);
        var tints = IntList.of(tint);

        renderCatPart(pipeline, buildRoot(entity), "root", base, state, tints, block, sky);
    }

    private void renderCatPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                               Matrix4d parent, AnimState s,
                               IntList tints, double block, double sky) {

        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        if (name.equals("head")) {
            applyHeadAnimation(mat, s);
        } else if (name.equals("body")) {
            applyBodyAnimation(mat, s);
        } else if (name.equals("tail1")) {
            applyTail1Animation(mat, s);
        } else if (name.equals("tail2")) {
            applyTail2Animation(mat, s);
        } else if (name.contains("leg")) {
            applyLegAnimation(mat, name, s);
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, tints));
        }
        for (var child : part.children.entrySet()) {
            renderCatPart(pipeline, child.getValue(), child.getKey(), mat, s, tints, block, sky);
        }
    }

    private void applyHeadAnimation(Matrix4d mat, AnimState s) {
        double headXRot = s.headPitch;
        if (!s.sitting) {
            if (s.lieDown > 0) {
                headXRot = Mth.rotLerp(s.lieDown, headXRot, (Math.PI / 18));
                mat.rotateZ(Mth.rotLerp(s.lieDown, 0, (-Math.PI * 5.0 / 12.0)));
                mat.translate(1.0f / 16f, 0.75f / 16f, -0.5f / 16f);
            } else {
                if (s.crouching) {
                    mat.translate(0, 2.0f * s.ageScale / 16f, 0); // y += 2.0F * ageScale
                }
                mat.rotateY(s.headYaw);
                mat.rotateX(headXRot);
            }
            if (s.relaxOne > 0) {
                mat.rotateX(Mth.rotLerp(s.relaxOne, headXRot, -0.58177644f));
            }
        } else {
            if (!s.baby) {
                mat.translate(0, -3.3f * s.ageScale / 16f, s.ageScale / 16f);
            } else {
                mat.translate(0, 0, 0.75f);
            }
            mat.rotateY(s.headYaw);
            mat.rotateX(headXRot);
        }
    }

    private void applyBodyAnimation(Matrix4d mat, AnimState s) {
        if (s.crouching) {
            mat.translate(0, s.ageScale / 16f, 0);
        }

        if (s.sitting) {
            if (!s.baby) {
                mat.translate(0, 4.0f * s.ageScale / 16f, 5.0f * s.ageScale / 16f);
                mat.rotateX(-Math.PI / 4);
            } else {
                mat.rotateX(-0.43633232f);
                mat.translate(0, 1.0f / 16f, 0);
            }
        }
    }

    private void applyTail1Animation(Matrix4d mat, AnimState s) {
        double targetXRot = s.baby ? -0.567232f : 0.9f;

        if (s.crouching) {
            mat.translate(0, s.ageScale / 16f, 0);
            targetXRot = Math.PI / 2;
        } else if (s.sprinting) {
            targetXRot = Math.PI / 2;
        } else if (s.sitting) {
            if (s.baby) {
                targetXRot = 0.5454154f;
                mat.translate(0, 4.0f / 16f, -0.9f);
            } else {
                targetXRot = 1.7278761f;
                mat.translate(0, 8.0f * s.ageScale / 16f, -2.0f * s.ageScale / 16f);
            }
        }

        mat.rotateX(targetXRot);

        if (s.lieDown > 0) {
            double lieDownAdd = Mth.rotLerp(s.lieDownTail, 0, (-Math.PI / 6));
            mat.rotateX(lieDownAdd);

            mat.rotateY(Mth.rotLerp(s.lieDownTail, 0, 0));
            mat.rotateZ(Mth.rotLerp(s.lieDownTail, 0, (-Math.PI / 18)));
            mat.translate(1.0f / 16f, 0.5f / 16f, -0.25f / 16f);
        }
    }

    private void applyTail2Animation(Matrix4d mat, AnimState s) {
        double baseXRot = 1.7278761f;
        if (s.crouching) {
            mat.translate(0, -4.0f * s.ageScale / 16f, 2.0f * s.ageScale / 16f);
            mat.rotateX((Math.PI / 2));
        } else if (s.sprinting) {
            mat.translate(0, 0, 2.0f * s.ageScale / 16f);
            mat.rotateX((Math.PI / 2));
        } else if (s.sitting) {
            if (!s.baby) {
                mat.translate(0, 2.0f * s.ageScale / 16f, -0.8f * s.ageScale / 16f);
                mat.rotateX(2.670354f);
            }
        } else {
            baseXRot += (Math.PI / 4) * Mth.cos(s.walkPos) * s.walkSpeed;
            mat.rotateX(baseXRot);
        }
        if (s.lieDown > 0) {
            mat.rotateX(Mth.rotLerp(s.lieDownTail, baseXRot, -0.4f));
        }
    }

    private void applyLegAnimation(Matrix4d mat, String name, AnimState s) {
        boolean right = name.contains("right");
        boolean hind = name.contains("hind");
        double walkAngle = 0;
        if (!s.sitting) {
            if (s.sprinting) {
                if (right && hind) walkAngle = Mth.cos(s.walkPos * 0.6662f + 0.3f) * s.walkSpeed;
                else if (!right && hind) walkAngle = Mth.cos(s.walkPos * 0.6662f) * s.walkSpeed;
                else if (right) walkAngle = Mth.cos(s.walkPos * 0.6662f + Mth.PI) * s.walkSpeed;
                else walkAngle = Mth.cos(s.walkPos * 0.6662f + Mth.PI + 0.3f) * s.walkSpeed;
            } else {
                if ((hind && right) || (!hind && !right)) {
                    walkAngle = Mth.cos(s.walkPos * 0.6662f) * s.walkSpeed;
                } else {
                    walkAngle = Mth.cos(s.walkPos * 0.6662f + Mth.PI) * s.walkSpeed;
                }
            }
        }

        if (s.sitting) {
            if (!s.baby) {
                if (hind) {
                    mat.translate(0, 3.0f * s.ageScale / 16f, -4.0f * s.ageScale / 16f);
                    mat.rotateX(-Math.PI / 2);
                } else {
                    mat.rotateX(-Math.PI / 20);
                    mat.translate(0, 2.0f * s.ageScale / 16f, -2.0f * s.ageScale / 16f);
                }
            } else {
                if (hind) {
                    mat.translate(0, 0, -0.9f);
                }
            }
        } else if (s.lieDown > 0) {
            if (!right) {
                if (hind) {
                    mat.rotateX(-0.4f);
                } else {
                    mat.rotateX(-1.2707963f);
                    mat.translate(1.0f / 16f, -1.0f / 16f, -2.0f / 16f);
                }
            } else {
                if (hind) {
                    mat.rotateX(0.5f);
                    mat.rotateZ(-0.5f);
                    mat.translate(2.5f / 16f, -0.25f / 16f, 0.5f / 16f); // x+=2.5, y-=0.25, z+=0.5
                } else {
                    mat.rotateX(-0.47079635f);
                    mat.rotateZ(-0.2f);
                    mat.translate(s.ageScale / 16f, 0, 0);
                }
            }
        } else {
            mat.rotateX(walkAngle);
        }
    }

    private record AnimState(double headYaw, double headPitch, double walkPos, double walkSpeed, boolean crouching,
                             boolean sprinting, boolean sitting, double lieDown, double lieDownTail, double relaxOne,
                             double ageScale, boolean baby) {
    }
}