package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.ItemStackRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4d;

public class AllayRenderer implements LivingEntityRenderer<Allay> {
    private static ModelBakery.BakedPart CACHED_ROOT;

    @Override
    public ModelBakery.BakedPart buildRoot(Allay allay) {
        if (CACHED_ROOT == null) {
            CACHED_ROOT = buildParts();
        }
        return CACHED_ROOT;
    }

    private ModelBakery.BakedPart buildParts() {
        ModelBakery bakery = new ModelBakery("entity/allay/allay", 32, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition rootDef = model.root();

        var root = rootDef.addOrReplaceChild("root",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(0.0F, 23.5F, 0.0F));

        root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F, ModelBakery.CubeDeformation.NONE),
                ModelBakery.PartPose.offset(0.0F, -3.99F, 0.0F));

        var body = root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 10).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 4.0F, 2.0F, ModelBakery.CubeDeformation.NONE)
                        .texOffs(0, 16).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 5.0F, 2.0F, new ModelBakery.CubeDeformation(-0.2F)),
                ModelBakery.PartPose.offset(0.0F, -4.0F, 0.0F));

        body.addOrReplaceChild("right_arm",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(23, 0).addBox(-0.75F, -0.5F, -1.0F, 1.0F, 4.0F, 2.0F, new ModelBakery.CubeDeformation(-0.01F)),
                ModelBakery.PartPose.offset(-1.75F, 0.5F, 0.0F));
        body.addOrReplaceChild("left_arm",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(23, 6).addBox(-0.25F, -0.5F, -1.0F, 1.0F, 4.0F, 2.0F, new ModelBakery.CubeDeformation(-0.01F)),
                ModelBakery.PartPose.offset(1.75F, 0.5F, 0.0F));
        body.addOrReplaceChild("right_wing",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(16, 14).addBox(0.0F, 1.0F, 0.0F, 0.0F, 5.0F, 8.0F, ModelBakery.CubeDeformation.NONE),
                ModelBakery.PartPose.offset(-0.5F, 0.0F, 0.6F));
        body.addOrReplaceChild("left_wing",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(16, 14).addBox(0.0F, 1.0F, 0.0F, 0.0F, 5.0F, 8.0F, ModelBakery.CubeDeformation.NONE),
                ModelBakery.PartPose.offset(0.5F, 0.0F, 0.6F));

        return rootDef.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, Allay allay) {
        var bakedRoot = buildRoot(allay);

        var pos = allay.position();
        double bodyYawDeg = allay.getPreciseBodyRotation(1.f);
        double modelYawRad = Mth.DEG_TO_RAD * (180f - bodyYawDeg);
        double headYawRad = Mth.DEG_TO_RAD * (allay.getYHeadRot() - bodyYawDeg);
        double headPitchRad = Mth.DEG_TO_RAD * allay.getXRot(1.f);

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + 1.5f, pos.z)
                .rotateY(modelYawRad)
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        double ageInTicks = allay.tickCount;
        double animPos = allay.walkAnimation.position();
        double animSpeed = allay.walkAnimation.speed();
        boolean isDancing = allay.isDancing();
        boolean isSpinning = allay.isSpinning();
        double spinningProgress = allay.getSpinningProgress(1.0f);
        double holdingItemFactor = allay.getHoldingItemAnimationProgress(1.0f);

        double flapSpeed = ageInTicks * 20.0F * (Math.PI / 180.0) + animPos;
        double flapAmount = Mth.cos(flapSpeed) * Math.PI * 0.15F + animSpeed;
        double idleBobSpeed = ageInTicks * 9.0F * (Math.PI / 180.0);
        double flyingFactor = Math.min(animSpeed / 0.3F, 1.0F);
        double idleBobFactor = 1.0F - flyingFactor;

        double danceSpeed = ageInTicks * 8.0F * (Math.PI / 180.0) + animSpeed;
        double danceFrequency = Mth.cos(danceSpeed) * 16.0F * (Math.PI / 180.0);
        double headTiltZ = Mth.cos(danceSpeed) * 14.0F * (Math.PI / 180.0);
        double headTiltY = Mth.cos(danceSpeed) * 30.0F * (Math.PI / 180.0);

        double rootYRot = isDancing ? (isSpinning ? (Math.PI * 4) * spinningProgress : 0.0F) : 0.0F;
        double rootZRot = isDancing ? danceFrequency * (1.0F - spinningProgress) : 0.0F;

        double headYRot, headXRot, headZRot;
        if (isDancing) {
            headYRot = headTiltY * (1.0F - spinningProgress);
            headZRot = headTiltZ * (1.0F - spinningProgress);
            headXRot = 0.0F;
        } else {
            headYRot = headYawRad;
            headXRot = headPitchRad;
            headZRot = 0.0F;
        }

        double rootBobY = Mth.cos(idleBobSpeed) * 0.25F * idleBobFactor;
        base = new Matrix4d(base).translate(0, rootBobY / 16f, 0);

        double armFlyingRotX = holdingItemFactor * Mth.lerp(flyingFactor, (-Math.PI / 3), -1.134464F);

        Matrix4d bodyMatrix = new Matrix4d();

        renderAllayPart(pipeline, bakedRoot, "root", base,
                flyingFactor, holdingItemFactor, idleBobSpeed, flapAmount, animSpeed,
                headYRot, headXRot, headZRot,
                rootYRot, rootZRot,
                bodyMatrix);

        ItemStack held = allay.getItemInHand(InteractionHand.MAIN_HAND);
        if (!held.isEmpty()) {

            bodyMatrix.translate(0.0F, 0.0625F, -0.1875F);
            bodyMatrix.rotateX(-armFlyingRotX);
            bodyMatrix.scale(0.7F, 0.7F, 0.7F);
            bodyMatrix.translate(0.0625F, 0.0F, 0.0F);

            ItemStackRenderer.render(pipeline, held, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, bodyMatrix);
        }
    }

    private void renderAllayPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                                 Matrix4d parentMat,
                                 double flyingFactor, double holdingItemFactor, double idleBobSpeed,
                                 double flapAmount, double animSpeed,
                                 double headYRot, double headXRot, double headZRot,
                                 double rootYRot, double rootZRot,
                                 Matrix4d bodyMatrixOut) {
        Matrix4d mat = new Matrix4d(parentMat);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose pose = part.initialPose;
        if (pose.xRot() != 0 || pose.yRot() != 0 || pose.zRot() != 0)
            mat.rotateZYX(pose.zRot(), pose.yRot(), pose.xRot());
        if (pose.xScale() != 1 || pose.yScale() != 1 || pose.zScale() != 1)
            mat.scale(pose.xScale(), pose.yScale(), pose.zScale());

        switch (name) {
            case "root" -> {
                mat.rotateY(rootYRot);
                mat.rotateZ(rootZRot);
            }
            case "head" -> {
                mat.rotateY(headYRot);
                mat.rotateX(headXRot);
                mat.rotateZ(headZRot);
            }
            case "body" -> {
                mat.rotateX(flyingFactor * (Math.PI / 4));
                bodyMatrixOut.set(mat);
            }
            case "right_wing" -> {
                mat.rotateX(0.43633232F * (1.0F - flyingFactor));
                mat.rotateY((-Math.PI / 4) + flapAmount);
            }
            case "left_wing" -> {
                mat.rotateX(0.43633232F * (1.0F - flyingFactor));
                mat.rotateY((Math.PI / 4) - flapAmount);
            }
            case "right_arm", "left_arm" -> {
                double armFlyingRotX = holdingItemFactor * Mth.lerp(flyingFactor, (-Math.PI / 3), -1.134464F);
                mat.rotateX(armFlyingRotX);
                double armIdleBobFactor = (1.0F - flyingFactor) * (1.0F - holdingItemFactor);
                double armIdleBobAmount = 0.43633232F - Mth.cos(idleBobSpeed + (Math.PI * 3.0 / 2.0)) * Math.PI * 0.075F * armIdleBobFactor;
                if (name.equals("right_arm")) {
                    mat.rotateZ(armIdleBobAmount);
                    mat.rotateY(0.27925268F * holdingItemFactor);
                } else {
                    mat.rotateZ(-armIdleBobAmount);
                    mat.rotateY(-0.27925268F * holdingItemFactor);
                }
            }
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh), mat, IntList.of(0xFFFFFFFF)));
        }

        for (var child : part.children.entrySet()) {
            renderAllayPart(pipeline, child.getValue(), child.getKey(), mat,
                    flyingFactor, holdingItemFactor, idleBobSpeed, flapAmount, animSpeed,
                    headYRot, headXRot, headZRot,
                    rootYRot, rootZRot,
                    bodyMatrixOut);
        }
    }
}