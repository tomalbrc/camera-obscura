package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public class RavagerRenderer implements LivingEntityRenderer<Ravager> {

    private static final String TEXTURE = "entity/illager/ravager";
    private ModelBakery.BakedPart cachedModel;

    @Override
    public ModelBakery.BakedPart buildRoot(Ravager entity) {
        if (cachedModel == null) {
            cachedModel = buildModel();
        }
        return cachedModel;
    }

    private ModelBakery.BakedPart buildModel() {
        ModelBakery bakery = new ModelBakery(TEXTURE, 128, 128);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition neck = root.addOrReplaceChild("neck",
                ModelBakery.CubeListBuilder.create().texOffs(68, 73).addBox(-5, -1, -18, 10, 10, 18),
                ModelBakery.PartPose.offset(0, -7, 5.5f));

        ModelBakery.PartDefinition head = neck.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-8, -20, -14, 16, 20, 16)
                        .texOffs(0, 0).addBox(-2, -6, -18, 4, 8, 4),
                ModelBakery.PartPose.offset(0, 16, -17));

        head.addOrReplaceChild("right_horn",
                ModelBakery.CubeListBuilder.create().texOffs(74, 55).addBox(0, -14, -2, 2, 14, 4),
                ModelBakery.PartPose.offsetAndRotation(-10, -14, -8, 1.0995574f, 0, 0));
        head.addOrReplaceChild("left_horn",
                ModelBakery.CubeListBuilder.create().texOffs(74, 55).mirror().addBox(0, -14, -2, 2, 14, 4),
                ModelBakery.PartPose.offsetAndRotation(8, -14, -8, 1.0995574f, 0, 0));

        head.addOrReplaceChild("mouth",
                ModelBakery.CubeListBuilder.create().texOffs(0, 36).addBox(-8, 0, -16, 16, 3, 16),
                ModelBakery.PartPose.offset(0, -2, 2));

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 55).addBox(-7, -10, -7, 14, 16, 20)
                        .texOffs(0, 91).addBox(-6, 6, -7, 12, 13, 18),
                ModelBakery.PartPose.offsetAndRotation(0, 1, 2, Mth.PI / 2, 0, 0));

        ModelBakery.CubeListBuilder leg = ModelBakery.CubeListBuilder.create()
                .texOffs(96, 0).addBox(-4, 0, -4, 8, 37, 8);
        root.addOrReplaceChild("right_hind_leg", leg, ModelBakery.PartPose.offset(-8, -13, 18));
        root.addOrReplaceChild("left_hind_leg", leg.mirror(), ModelBakery.PartPose.offset(8, -13, 18));
        ModelBakery.CubeListBuilder frontLeg = ModelBakery.CubeListBuilder.create()
                .texOffs(64, 0).addBox(-4, 0, -4, 8, 37, 8);
        root.addOrReplaceChild("right_front_leg", frontLeg, ModelBakery.PartPose.offset(-8, -13, -5));
        root.addOrReplaceChild("left_front_leg", frontLeg.mirror(), ModelBakery.PartPose.offset(8, -13, -5));

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, Ravager entity) {
        float partialTicks = 1.0f;
        double stunnedTick = entity.getStunnedTick() > 0 ? entity.getStunnedTick() - partialTicks : 0;
        double attackTick = entity.getAttackTick() > 0 ? entity.getAttackTick() - partialTicks : 0;
        double roarAnim = 0;
        if (entity.getRoarTick() > 0) {
            roarAnim = (20 - entity.getRoarTick() + partialTicks) / 20.0f;
        }
        double animPos = entity.walkAnimation.position();
        double animSpeed = entity.walkAnimation.speed();
        double headYaw = Mth.DEG_TO_RAD * (entity.getYHeadRot() - entity.getPreciseBodyRotation(partialTicks));
        double headPitch = Mth.DEG_TO_RAD * entity.getXRot(partialTicks);

        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(partialTicks);
        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + 1.5f, pos.z)
                .rotateY(Mth.DEG_TO_RAD * (180f - bodyYaw))
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        double legRot = 0.4f * animSpeed;
        double rightHindAngle = Mth.cos(animPos * 0.6662f) * legRot;
        double leftHindAngle = Mth.cos(animPos * 0.6662f + Mth.PI) * legRot;
        double rightFrontAngle = Mth.cos(animPos * 0.6662f + Mth.PI) * legRot;
        double leftFrontAngle = Mth.cos(animPos * 0.6662f) * legRot;

        AnimParams params = new AnimParams(stunnedTick, attackTick, roarAnim,
                rightHindAngle, leftHindAngle, rightFrontAngle, leftFrontAngle,
                headYaw, headPitch);

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderRavagerPart(pipeline, buildRoot(entity), "root", base, params, block, sky);
    }

    private void renderRavagerPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
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
                mat.rotateX(p.headPitch);
                mat.rotateY(p.headYaw);
                break;
            case "mouth":
                if (p.attackTick > 0.0f) {
                    if (p.attackTick > 5.0f) {
                        double angle = Mth.sin((-4.0f + p.attackTick) / 4.0f) * Mth.PI * 0.4f;
                        mat.rotateX(angle);
                    } else {
                        double angle = (Math.PI / 20) * Mth.sin(Mth.PI * p.attackTick / 10.0f);
                        mat.rotateX(angle);
                    }
                } else {
                    boolean stunned = p.stunnedTick > 0.0f;
                    double mouthAngle = Mth.PI * (stunned ? 0.05f : 0.01f);
                    if (p.roarAnim > 0.0f) {
                        mouthAngle = (Math.PI / 2) * Mth.sin(p.roarAnim * Mth.PI * 0.25f);
                    }
                    mat.rotateX(mouthAngle);
                }
                break;
            case "neck":
                if (p.attackTick > 0.0f) {
                    double headAnim = Mth.triangleWave((float) p.attackTick, 10.0f);
                    double scaled = (1.0f + headAnim) * 0.5f;
                    double headPos = scaled * scaled * scaled * 12.0f;
                    double yOffset = headPos * Mth.sin(0);
                    mat = new Matrix4d(parent); // reset
                    mat.translate(0, -7.0f - yOffset, -6.5f + headPos);
                } else {
                    boolean stunned = p.stunnedTick > 0.0f;
                    double xRot = stunned ? 0.21991149f : 0.0f;
                    mat.rotateX(xRot);
                    if (stunned) {
                        double speed = p.stunnedTick / 40.0;
                        double neckX = Math.sin(speed * 10.0) * 3.0f;
                        mat.translate(neckX / 16.0f, 0, 0);
                    }
                }
                break;
            case "right_hind_leg":
                mat.rotateX(p.rhAng);
                break;
            case "left_hind_leg":
                mat.rotateX(p.lhAng);
                break;
            case "right_front_leg":
                mat.rotateX(p.rfAng);
                break;
            case "left_front_leg":
                mat.rotateX(p.lfAng);
                break;
            default:
                break;
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }
        for (var child : part.children.entrySet()) {
            renderRavagerPart(pipeline, child.getValue(), child.getKey(), mat, p, block, sky);
        }
    }

    private static class AnimParams {
        final double stunnedTick, attackTick, roarAnim;
        final double rhAng, lhAng, rfAng, lfAng;
        final double headYaw, headPitch;

        AnimParams(double st, double at, double roar,
                   double rh, double lh, double rf, double lf,
                   double hy, double hp) {
            this.stunnedTick = st;
            this.attackTick = at;
            this.roarAnim = roar;
            this.rhAng = rh;
            this.lhAng = lh;
            this.rfAng = rf;
            this.lfAng = lf;
            this.headYaw = hy;
            this.headPitch = hp;
        }
    }
}