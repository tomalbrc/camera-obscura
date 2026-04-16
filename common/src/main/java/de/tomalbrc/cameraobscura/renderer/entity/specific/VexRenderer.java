package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Vex;
import org.joml.Matrix4d;

import java.util.HashMap;
import java.util.Map;

public class VexRenderer implements LivingEntityRenderer<Vex> {
    private static final String NORMAL_TEXTURE = "entity/illager/vex";
    private static final String CHARGING_TEXTURE = "entity/illager/vex_charging";

    private final Map<Boolean, ModelBakery.BakedPart> cache = new HashMap<>();

    @Override
    public ModelBakery.BakedPart buildRoot(Vex entity) {
        boolean charging = entity.isCharging();
        return cache.computeIfAbsent(charging, k -> buildModel(charging ? CHARGING_TEXTURE : NORMAL_TEXTURE));
    }

    private ModelBakery.BakedPart buildModel(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 32, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition partdef = model.root();

        ModelBakery.PartDefinition root = partdef.addOrReplaceChild("root",
                ModelBakery.CubeListBuilder.create(),
                new ModelBakery.PartPose(0.0f, -2.5f, 0.0f, 0, 0, 0, 1, 1, 1));

        root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-2.5f, -5.0f, -2.5f, 5.0f, 5.0f, 5.0f),
                ModelBakery.PartPose.offset(0.0f, 20.0f, 0.0f));

        ModelBakery.PartDefinition body = root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 10).addBox(-1.5f, 0.0f, -1.0f, 3.0f, 4.0f, 2.0f)
                        .texOffs(0, 16).addBox(-1.5f, 1.0f, -1.0f, 3.0f, 5.0f, 2.0f, new ModelBakery.CubeDeformation(-0.2f)),
                ModelBakery.PartPose.offset(0.0f, 20.0f, 0.0f));

        body.addOrReplaceChild("right_arm",
                ModelBakery.CubeListBuilder.create().texOffs(23, 0).addBox(-1.25f, -0.5f, -1.0f, 2.0f, 4.0f, 2.0f, new ModelBakery.CubeDeformation(-0.1f)),
                ModelBakery.PartPose.offset(-1.75f, 0.25f, 0.0f));

        body.addOrReplaceChild("left_arm",
                ModelBakery.CubeListBuilder.create().texOffs(23, 6).addBox(-0.75f, -0.5f, -1.0f, 2.0f, 4.0f, 2.0f, new ModelBakery.CubeDeformation(-0.1f)),
                ModelBakery.PartPose.offset(1.75f, 0.25f, 0.0f));

        body.addOrReplaceChild("left_wing",
                ModelBakery.CubeListBuilder.create().texOffs(16, 14).mirror().addBox(0.0f, 0.0f, 0.0f, 0.0f, 5.0f, 8.0f).mirror(false),
                ModelBakery.PartPose.offset(0.5f, 1.0f, 1.0f));

        body.addOrReplaceChild("right_wing",
                ModelBakery.CubeListBuilder.create().texOffs(16, 14).addBox(0.0f, 0.0f, 0.0f, 0.0f, 5.0f, 8.0f),
                ModelBakery.PartPose.offset(-0.5f, 1.0f, 1.0f));

        return partdef.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, Vex entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double headYawRel = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYaw);
        double headPitch = Mth.DEG_TO_RAD * entity.getXRot(1.0f);
        boolean charging = entity.isCharging();
        boolean hasRightItem = !entity.getMainHandItem().isEmpty();
        boolean hasLeftItem = !entity.getOffhandItem().isEmpty();
        double ageInTicks = entity.tickCount + 1.0f;

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + 1.5f, pos.z)
                .rotateY(Mth.DEG_TO_RAD * (180f - bodyYaw))
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        double movingArmZBob = Mth.cos(ageInTicks * 5.5f * Mth.DEG_TO_RAD) * 0.1f;
        double wingYRotBase = 1.0995574f;
        double wingOsc = Mth.cos(ageInTicks * 45.836624f * Mth.DEG_TO_RAD) * Mth.DEG_TO_RAD * 16.2f;
        double leftWingYRot = wingYRotBase + wingOsc;
        double rightWingYRot = -leftWingYRot;

        AnimParams params = new AnimParams(
                headYawRel, headPitch, charging, hasRightItem, hasLeftItem,
                movingArmZBob, leftWingYRot, rightWingYRot
        );

        renderVexPart(pipeline, buildRoot(entity), "root", base, params);
    }

    private void renderVexPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                               Matrix4d parent, AnimParams p) {
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
                mat.rotateX(p.headPitch);
                break;
            case "body":
                mat.rotateX(p.charging ? 0.0f : (double) (Math.PI / 20));
                break;
            case "right_arm":
                mat.rotateZ((double) (Math.PI / 5) + p.movingArmZBob);
                if (p.charging) {
                    applyChargingArm(mat, true, p.hasRightItem, p.hasLeftItem, p.movingArmZBob);
                }
                break;
            case "left_arm":
                mat.rotateZ(-((double) (Math.PI / 5) + p.movingArmZBob));
                if (p.charging) {
                    applyChargingArm(mat, false, p.hasRightItem, p.hasLeftItem, p.movingArmZBob);
                }
                break;
            case "left_wing":
                mat.rotateY(p.leftWingYRot);
                mat.rotateX(0.47123888f);
                mat.rotateZ(-0.47123888f);
                break;
            case "right_wing":
                mat.rotateY(p.rightWingYRot);
                mat.rotateX(0.47123888f);
                mat.rotateZ(0.47123888f);
                break;
            default:
                break;
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh), mat, IntList.of(0xFFFFFFFF)));
        }
        for (var child : part.children.entrySet()) {
            renderVexPart(pipeline, child.getValue(), child.getKey(), mat, p);
        }
    }

    private void applyChargingArm(Matrix4d mat, boolean isRight, boolean hasRightItem, boolean hasLeftItem, double movingArmZBob) {
        boolean hasItem = isRight ? hasRightItem : hasLeftItem;
        if (!hasRightItem && !hasLeftItem) {
            if (isRight) {
                mat.rotateX(-1.2217305f);
                mat.rotateY((double) (Math.PI / 12));
                mat.rotateZ(-0.47123888f - movingArmZBob);
            } else {
                mat.rotateX(-1.2217305f);
                mat.rotateY((double) (-Math.PI / 12));
                mat.rotateZ(0.47123888f + movingArmZBob);
            }
        } else if (hasItem) {
            if (isRight) {
                mat.rotateX((double) (Math.PI * 7.0 / 6.0));
                mat.rotateY((double) (Math.PI / 12));
                mat.rotateZ(-0.47123888f - movingArmZBob);
            } else {
                mat.rotateX((double) (Math.PI * 7.0 / 6.0));
                mat.rotateY((double) (-Math.PI / 12));
                mat.rotateZ(0.47123888f + movingArmZBob);
            }
        }
    }

    private static class AnimParams {
        final double headYaw, headPitch;
        final boolean charging;
        final boolean hasRightItem, hasLeftItem;
        final double movingArmZBob;
        final double leftWingYRot, rightWingYRot;

        AnimParams(double headYaw, double headPitch, boolean charging,
                   boolean hasRightItem, boolean hasLeftItem,
                   double movingArmZBob, double leftWingYRot, double rightWingYRot) {
            this.headYaw = headYaw;
            this.headPitch = headPitch;
            this.charging = charging;
            this.hasRightItem = hasRightItem;
            this.hasLeftItem = hasLeftItem;
            this.movingArmZBob = movingArmZBob;
            this.leftWingYRot = leftWingYRot;
            this.rightWingYRot = rightWingYRot;
        }
    }
}