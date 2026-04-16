package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BeeRenderer implements LivingEntityRenderer<Bee> {

    private static final Map<CacheKey, ModelBakery.BakedPart> CACHE = new HashMap<>();

    @Override
    public ModelBakery.BakedPart buildRoot(Bee entity) {
        CacheKey key = new CacheKey(entity.isBaby(), entity.isAngry(), entity.hasNectar());
        return CACHE.computeIfAbsent(key, this::buildModel);
    }

    private ModelBakery.BakedPart buildModel(CacheKey key) {
        String texture = getTexture(key);
        boolean baby = key.baby;
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

    private String getTexture(CacheKey key) {
        String base = "entity/bee/bee";
        if (key.angry) {
            base += "_angry";
        }
        if (key.nectar) {
            base += "_nectar";
        }
        if (key.baby) {
            base += "_baby";
        }
        return base;
    }

    private void buildAdultModel(ModelBakery.PartDefinition root) {
        ModelBakery.PartDefinition bone = root.addOrReplaceChild("bone",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(0.0f, 19.0f, 0.0f));
        ModelBakery.PartDefinition body = bone.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-3.5f, -4.0f, -5.0f, 7.0f, 7.0f, 10.0f),
                ModelBakery.PartPose.ZERO);
        body.addOrReplaceChild("stinger",
                ModelBakery.CubeListBuilder.create().texOffs(26, 7).addBox(0.0f, -1.0f, 5.0f, 0.0f, 1.0f, 2.0f),
                ModelBakery.PartPose.ZERO);
        body.addOrReplaceChild("left_antenna",
                ModelBakery.CubeListBuilder.create().texOffs(2, 0).addBox(1.5f, -2.0f, -3.0f, 1.0f, 2.0f, 3.0f),
                ModelBakery.PartPose.offset(0.0f, -2.0f, -5.0f));
        body.addOrReplaceChild("right_antenna",
                ModelBakery.CubeListBuilder.create().texOffs(2, 3).addBox(-2.5f, -2.0f, -3.0f, 1.0f, 2.0f, 3.0f),
                ModelBakery.PartPose.offset(0.0f, -2.0f, -5.0f));
        ModelBakery.CubeDeformation wingDef = new ModelBakery.CubeDeformation(0.001f);
        bone.addOrReplaceChild("right_wing",
                ModelBakery.CubeListBuilder.create().texOffs(0, 18).addBox(-9.0f, 0.0f, 0.0f, 9.0f, 0.0f, 6.0f, wingDef),
                ModelBakery.PartPose.offsetAndRotation(-1.5f, -4.0f, -3.0f, 0.0f, -0.2618f, 0.0f));
        bone.addOrReplaceChild("left_wing",
                ModelBakery.CubeListBuilder.create().texOffs(0, 18).mirror().addBox(0.0f, 0.0f, 0.0f, 9.0f, 0.0f, 6.0f, wingDef),
                ModelBakery.PartPose.offsetAndRotation(1.5f, -4.0f, -3.0f, 0.0f, 0.2618f, 0.0f));

        bone.addOrReplaceChild("front_legs",
                ModelBakery.CubeListBuilder.create().texOffs(26, 1).addBox(-5.0f, 0.0f, 0.0f, 7, 2, 0),
                ModelBakery.PartPose.offset(1.5f, 3.0f, -2.0f));
        bone.addOrReplaceChild("middle_legs",
                ModelBakery.CubeListBuilder.create().texOffs(26, 3).addBox(-5.0f, 0.0f, 0.0f, 7, 2, 0),
                ModelBakery.PartPose.offset(1.5f, 3.0f, 0.0f));
        bone.addOrReplaceChild("back_legs",
                ModelBakery.CubeListBuilder.create().texOffs(26, 5).addBox(-5.0f, 0.0f, 0.0f, 7, 2, 0),
                ModelBakery.PartPose.offset(1.5f, 3.0f, 2.0f));
    }

    private void buildBabyModel(ModelBakery.PartDefinition root) {
        ModelBakery.PartDefinition bone = root.addOrReplaceChild("bone",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(6, 12).addBox(1.0f, -1.6667f, -2.1633f, 1.0f, 2.0f, 2.0f)
                        .texOffs(0, 12).addBox(-2.0f, -1.6667f, -2.1933f, 1.0f, 2.0f, 2.0f),
                ModelBakery.PartPose.offset(0.0f, 19.6667f, -1.8567f));
        ModelBakery.PartDefinition body = bone.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-2.0f, -2.0f, -2.5f, 4.0f, 4.0f, 5.0f),
                ModelBakery.PartPose.offset(0.0f, 1.3333f, 2.3567f));
        body.addOrReplaceChild("stinger",
                ModelBakery.CubeListBuilder.create().texOffs(13, 2).addBox(0.0f, -0.5f, 0.0f, 0.0f, 1.0f, 1.0f),
                ModelBakery.PartPose.offset(0.0f, 0.5f, 2.5f));
        bone.addOrReplaceChild("right_wing",
                ModelBakery.CubeListBuilder.create().texOffs(3, 9).addBox(-3.0f, 0.0f, 0.0f, 3.0f, 0.0f, 3.0f),
                ModelBakery.PartPose.offsetAndRotation(-1.0f, -0.6667f, 0.8567f, 0.2182f, 0.3491f, 0.0f));
        bone.addOrReplaceChild("left_wing",
                ModelBakery.CubeListBuilder.create().texOffs(-3, 9).mirror().addBox(0.0f, 0.0f, 0.0f, 3.0f, 0.0f, 3.0f).mirror(false),
                ModelBakery.PartPose.offsetAndRotation(1.0f, -0.6667f, 0.8567f, 0.2182f, -0.3491f, 0.0f));
        bone.addOrReplaceChild("front_legs",
                ModelBakery.CubeListBuilder.create().texOffs(13, 0).addBox(-1.5f, 0.0f, 0.0f, 3.0f, 1.0f, 0.0f),
                ModelBakery.PartPose.offset(0.0f, 3.3333f, 1.8567f));
        bone.addOrReplaceChild("middle_legs",
                ModelBakery.CubeListBuilder.create().texOffs(13, 1).addBox(-1.5f, 0.0f, 0.0f, 3.0f, 1.0f, 0.0f),
                ModelBakery.PartPose.offset(0.0f, 3.3333f, 2.8567f));
        bone.addOrReplaceChild("back_legs",
                ModelBakery.CubeListBuilder.create().texOffs(13, 2).addBox(-1.5f, 0.0f, 0.0f, 3.0f, 1.0f, 0.0f),
                ModelBakery.PartPose.offset(0.0f, 3.3333f, 3.8567f));
    }

    @Override
    public void render(RenderPipeline pipeline, Bee entity) {
        boolean baby = entity.isBaby();
        boolean angry = entity.isAngry();
        boolean onGround = entity.onGround() && entity.getDeltaMovement().lengthSqr() < 1.0E-7;
        boolean hasStinger = !entity.hasStung();
        float roll = entity.getRollAmount(1.0f);
        double age = entity.tickCount + 1.0f;
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double headYaw = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYaw);

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + 1.5f, pos.z)
                .rotateY(Mth.DEG_TO_RAD * (180f - bodyYaw))
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        double wingFlap = 0;
        if (!onGround) {
            double speed = age * 120.32113f * Mth.DEG_TO_RAD;
            wingFlap = Mth.cos(speed) * Mth.PI * 0.15f;
        }

        float bobSpeed = Mth.cos(age * 0.18f);
        float boneXRot = 0;
        double boneYOffset = 0;
        double frontLegXRot = 0;
        double backLegXRot = 0;
        if (!angry && !onGround) {
            boneXRot = 0.1f + bobSpeed * Mth.PI * 0.025f;
            boneYOffset = -Mth.cos(age * 0.18f) * 0.9f;
            frontLegXRot = -bobSpeed * Mth.PI * 0.1f + (Math.PI / 8);
            backLegXRot = -bobSpeed * Mth.PI * 0.05f + (Math.PI / 4);
        }
        if (roll > 0) {
            boneXRot = Mth.rotLerpRad(roll, boneXRot, 3.0915928f);
        }

        AnimParams params = new AnimParams(
                headYaw, onGround, angry, hasStinger, wingFlap,
                boneXRot, boneYOffset, frontLegXRot, backLegXRot, baby
        );

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;
        renderPart(pipeline, buildRoot(entity), "root", base, params, block, sky);
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                            Matrix4d parent, AnimParams p,
                            double block, double sky) {

        if (name.equals("stinger") && !p.hasStinger) return;

        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        switch (name) {
            case "bone":
                mat.rotateX(p.boneXRot);
                mat.translate(0.0f, p.boneYOffset / 16.0f, 0.0f);
                break;
            case "right_wing":
                if (!p.onGround) {
                    mat.rotateZ(p.wingFlap);
                }
                break;
            case "left_wing":
                if (!p.onGround) {
                    mat.rotateZ(-p.wingFlap);
                }
                break;
            case "front_legs":
                if (!p.onGround) {
                    mat.rotateX((Math.PI / 4));
                } else {
                    mat.rotateX(p.frontLegXRot);
                }
                break;
            case "middle_legs":
                if (!p.onGround) {
                    mat.rotateX((Math.PI / 4));
                }
                break;
            case "back_legs":
                if (!p.onGround) {
                    mat.rotateX((Math.PI / 4));
                } else {
                    mat.rotateX(p.backLegXRot);
                }
                break;
            case "head":
                mat.rotateY(p.headYaw);
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
        final double headYaw;
        final boolean onGround, angry, hasStinger;
        final double wingFlap, boneXRot, boneYOffset;
        final double frontLegXRot, backLegXRot;
        final boolean baby;

        AnimParams(double hy, boolean og, boolean ag, boolean st,
                   double wf, double bx, double by, double fl, double bl,
                   boolean bb) {
            headYaw = hy;
            onGround = og;
            angry = ag;
            hasStinger = st;
            wingFlap = wf;
            boneXRot = bx;
            boneYOffset = by;
            frontLegXRot = fl;
            backLegXRot = bl;
            baby = bb;
        }
    }

    private static class CacheKey {
        final boolean baby, angry, nectar;

        CacheKey(boolean baby, boolean angry, boolean nectar) {
            this.baby = baby;
            this.angry = angry;
            this.nectar = nectar;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CacheKey key)) return false;
            return baby == key.baby && angry == key.angry && nectar == key.nectar;
        }

        @Override
        public int hashCode() {
            return Objects.hash(baby, angry, nectar);
        }
    }
}