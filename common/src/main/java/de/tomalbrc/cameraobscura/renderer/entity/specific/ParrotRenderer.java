package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

import java.util.EnumMap;
import java.util.Map;

public class ParrotRenderer implements LivingEntityRenderer<Parrot> {

    private static final Map<Parrot.Variant, ModelBakery.BakedPart> CACHE = new EnumMap<>(Parrot.Variant.class);

    private static String getVariantTexture(Parrot.Variant variant) {
        return switch (variant) {
            case RED_BLUE -> "entity/parrot/parrot_red_blue";
            case BLUE -> "entity/parrot/parrot_blue";
            case GREEN -> "entity/parrot/parrot_green";
            case YELLOW_BLUE -> "entity/parrot/parrot_yellow_blue";
            case GRAY -> "entity/parrot/parrot_grey";
        };
    }

    @Override
    public ModelBakery.BakedPart buildRoot(Parrot entity) {
        Parrot.Variant variant = entity.getVariant();
        return CACHE.computeIfAbsent(variant, this::buildModel);
    }

    private ModelBakery.BakedPart buildModel(Parrot.Variant variant) {
        String texture = getVariantTexture(variant);
        ModelBakery bakery = new ModelBakery(texture, 32, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(2, 8).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                ModelBakery.PartPose.offsetAndRotation(0.0F, 16.5F, -3.0F, 0.4937F, 0.0F, 0.0F));

        root.addOrReplaceChild("tail",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(22, 1).addBox(-1.5F, -1.0F, -1.0F, 3.0F, 4.0F, 1.0F),
                ModelBakery.PartPose.offsetAndRotation(0.0F, 21.07F, 1.16F, 1.015F, 0.0F, 0.0F));

        root.addOrReplaceChild("left_wing",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(19, 8).addBox(-0.5F, 0.0F, -1.5F, 1.0F, 5.0F, 3.0F),
                ModelBakery.PartPose.offsetAndRotation(1.5F, 16.94F, -2.76F, -0.6981F, -Mth.PI, 0.0F));

        root.addOrReplaceChild("right_wing",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(19, 8).addBox(-0.5F, 0.0F, -1.5F, 1.0F, 5.0F, 3.0F),
                ModelBakery.PartPose.offsetAndRotation(-1.5F, 16.94F, -2.76F, -0.6981F, -Mth.PI, 0.0F));

        ModelBakery.PartDefinition head = root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(2, 2).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F),
                ModelBakery.PartPose.offset(0.0F, 15.69F, -2.76F));
        head.addOrReplaceChild("head2",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(10, 0).addBox(-1.0F, -0.5F, -2.0F, 2.0F, 1.0F, 4.0F),
                ModelBakery.PartPose.offset(0.0F, -2.0F, -1.0F));
        head.addOrReplaceChild("beak1",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(11, 7).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                ModelBakery.PartPose.offset(0.0F, -0.5F, -1.5F));
        head.addOrReplaceChild("beak2",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(16, 7).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                ModelBakery.PartPose.offset(0.0F, -1.75F, -2.45F));
        head.addOrReplaceChild("feather",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(2, 18).addBox(0.0F, -4.0F, -2.0F, 0.0F, 5.0F, 4.0F),
                ModelBakery.PartPose.offsetAndRotation(0.0F, -2.15F, 0.15F, -0.2214F, 0.0F, 0.0F));

        ModelBakery.CubeListBuilder leg = ModelBakery.CubeListBuilder.create()
                .texOffs(14, 18).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F);
        root.addOrReplaceChild("left_leg", leg,
                ModelBakery.PartPose.offsetAndRotation(1.0F, 22.0F, -1.05F, -0.0299F, 0.0F, 0.0F));
        root.addOrReplaceChild("right_leg", leg,
                ModelBakery.PartPose.offsetAndRotation(-1.0F, 22.0F, -1.05F, -0.0299F, 0.0F, 0.0F));

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, Parrot entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyYaw);
        double headYawRel = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYaw);
        double headPitch = Mth.DEG_TO_RAD * entity.getXRot(1.0f);

        double partialTicks = 1.0f;
        double flap = Mth.lerp(partialTicks, entity.oFlap, entity.flap);
        double flapSpeed = Mth.lerp(partialTicks, entity.oFlapSpeed, entity.flapSpeed);
        double flapAngle = (Mth.sin(flap) + 1.0F) * flapSpeed;

        Pose pose;
        if (entity.isPartyParrot()) {
            pose = Pose.PARTY;
        } else if (entity.isInSittingPose()) {
            pose = Pose.SITTING;
        } else if (entity.isFlying()) {
            pose = Pose.FLYING;
        } else {
            pose = Pose.STANDING;
        }

        double animPos = entity.walkAnimation.position();
        double animSpeed = entity.walkAnimation.speed();
        double ageInTicks = entity.tickCount + partialTicks;

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + 1.51f, pos.z)
                .rotateY(modelYaw)
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderPart(
                pipeline, buildRoot(entity), "root", base,
                headYawRel, headPitch, flapAngle, animPos, animSpeed, ageInTicks, pose,
                block, sky);
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part,
                            String name, Matrix4d parent,
                            double headYaw, double headPitch,
                            double flapAngle, double animPos, double animSpeed,
                            double ageInTicks, Pose pose,
                            double block, double sky) {

        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        applyPoseTransform(mat, name, pose, headYaw, headPitch, flapAngle, animPos, animSpeed, ageInTicks);

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }

        for (var child : part.children.entrySet()) {
            renderPart(
                    pipeline, child.getValue(), child.getKey(), mat,
                    headYaw, headPitch, flapAngle, animPos, animSpeed, ageInTicks, pose,
                    block, sky
            );
        }
    }

    private void applyPoseTransform(Matrix4d mat, String name, Pose pose,
                                    double headYaw, double headPitch,
                                    double flapAngle, double animPos, double animSpeed,
                                    double ageInTicks) {


        double bobbingBody = flapAngle * 0.3f;

        switch (pose) {
            case STANDING:
                switch (name) {
                    case "head":
                        mat.rotateY(headYaw);
                        mat.rotateX(headPitch);
                        break;
                    case "left_leg":
                        mat.rotateX(Mth.cos(animPos * 0.6662F) * 1.4F * animSpeed);
                        break;
                    case "right_leg":
                        mat.rotateX(Mth.cos(animPos * 0.6662F + Mth.PI) * 1.4F * animSpeed);
                        break;
                    case "tail":
                        mat.rotateX(Mth.cos(animPos * 0.6662F) * 0.3F * animSpeed);
                        break;
                    case "left_wing":
                        mat.translate(0, bobbingBody / 16.0f, 0);
                        mat.rotateZ(0.0873f + flapAngle);
                        break;
                    case "right_wing":
                        mat.translate(0, bobbingBody / 16.0f, 0);
                        mat.rotateZ(-0.0873f - flapAngle);
                        break;
                    case "body":
                        mat.translate(0, bobbingBody / 16.0f, 0);
                        break;
                    default:
                        break;
                }
                break;

            case FLYING:
                switch (name) {
                    case "head":
                        mat.rotateY(headYaw);
                        mat.rotateX(headPitch);
                        mat.translate(0, bobbingBody / 16.0f, 0);
                        break;
                    case "left_leg":
                        mat.rotateX(Mth.cos(animPos * 0.6662F) * 1.4F * animSpeed + (Math.PI * 2.0 / 9.0));
                        break;
                    case "right_leg":
                        mat.rotateX(Mth.cos(animPos * 0.6662F + Mth.PI) * 1.4F * animSpeed + (Math.PI * 2.0 / 9.0));
                        break;
                    case "tail":
                        mat.rotateX(Mth.cos(animPos * 0.6662F) * 0.3F * animSpeed);
                        mat.translate(0, bobbingBody / 16.0f, 0);
                        break;
                    case "left_wing":
                        mat.translate(0, bobbingBody / 16.0f, 0);
                        mat.rotateZ(-0.0873f + flapAngle);
                        break;
                    case "right_wing":
                        mat.translate(0, bobbingBody / 16.0f, 0);
                        mat.rotateZ(0.0873f - flapAngle);
                        break;
                    case "body":
                        mat.translate(0, bobbingBody / 16.0f, 0);
                        break;
                    default:
                        break;
                }
                break;

            case SITTING:
                switch (name) {
                    case "head":
                        mat.translate(0, 1f / 16.0f, 0);
                        mat.rotateY(headYaw);
                        mat.rotateX(headPitch);
                        break;
                    case "body":
                        mat.translate(0, 1f / 16.0f, 0);
                        break;
                    case "tail":
                        mat.translate(0, 1f / 16.0f, 0);
                        mat.rotateX(Math.PI / 6);
                        break;
                    case "left_wing":
                        mat.translate(0, 1f / 16.0f, 0);
                        mat.rotateZ(0.0873f);
                        break;
                    case "right_wing":
                        mat.translate(0, 1f / 16.0f, 0);
                        mat.rotateZ(-0.0873f);
                        break;
                    case "left_leg", "right_leg":
                        mat.translate(0, 1f / 16.0f, 0);
                        mat.rotateX(1f);
                        break;
                    default:
                        break;
                }
                break;

            case PARTY:
                double xPos = Mth.cos(ageInTicks);
                double yPos = Mth.sin(ageInTicks);

                switch (name) {
                    case "head":
                        mat.translate(xPos / 16.0f, yPos / 16.0f, 0);
                        mat.rotateZ(Mth.sin(ageInTicks) * 0.4f);
                        break;
                    case "body", "tail":
                        mat.translate(xPos / 16.0f, yPos / 16.0f, 0);
                        break;
                    case "left_wing":
                        mat.translate(xPos / 16.0f, yPos / 16.0f, 0);
                        mat.rotateZ(-0.0873f - flapAngle);
                        break;
                    case "right_wing":
                        mat.translate(xPos / 16.0f, yPos / 16.0f, 0);
                        mat.rotateZ(0.0873f + flapAngle);
                        break;
                    case "left_leg":
                        mat.translate(xPos / 16.0f, yPos / 16.0f, 0);
                        mat.rotateZ(-Math.PI / 9);
                        break;
                    case "right_leg":
                        mat.translate(xPos / 16.0f, yPos / 16.0f, 0);
                        mat.rotateZ(Math.PI / 9);
                        break;
                    default:
                        break;
                }
                break;

            default:
                break;
        }

    }

    private enum Pose {
        FLYING, STANDING, SITTING, PARTY
    }
}