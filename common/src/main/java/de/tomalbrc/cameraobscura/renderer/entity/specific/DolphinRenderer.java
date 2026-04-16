package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.dolphin.Dolphin;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public class DolphinRenderer implements LivingEntityRenderer<Dolphin> {

    private static final String ADULT_TEXTURE = "entity/dolphin/dolphin";
    private static final String BABY_TEXTURE = "entity/dolphin/dolphin_baby";
    private static final int TEX_WIDTH = 64;
    private static final int TEX_HEIGHT = 64;

    private ModelBakery.BakedPart cachedAdultRoot;
    private ModelBakery.BakedPart cachedBabyRoot;

    private ModelBakery.BakedPart buildAdultRoot() {
        ModelBakery bakery = new ModelBakery(ADULT_TEXTURE, TEX_WIDTH, TEX_HEIGHT);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition body = root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(22, 0).addBox(-4, -7, 0, 8, 7, 13),
                ModelBakery.PartPose.offset(0, 22, -5));

        body.addOrReplaceChild("back_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(51, 0).addBox(-0.5f, 0, 8, 1, 4, 5),
                ModelBakery.PartPose.rotation(Mth.PI / 3, 0, 0));

        body.addOrReplaceChild("left_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(48, 20).mirror().addBox(-0.5f, -4, 0, 1, 4, 7),
                ModelBakery.PartPose.offsetAndRotation(2, -2, 4, Mth.PI / 3, 0, Mth.PI * 2 / 3));

        body.addOrReplaceChild("right_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(48, 20).addBox(-0.5f, -4, 0, 1, 4, 7),
                ModelBakery.PartPose.offsetAndRotation(-2, -2, 4, Mth.PI / 3, 0, -Mth.PI * 2 / 3));

        ModelBakery.PartDefinition tail = body.addOrReplaceChild("tail",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 19).addBox(-2, -2.5f, 0, 4, 5, 11),
                ModelBakery.PartPose.offsetAndRotation(0, -2.5f, 11, -0.10471976f, 0, 0));

        tail.addOrReplaceChild("tail_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(19, 20).addBox(-5, -0.5f, 0, 10, 1, 6),
                ModelBakery.PartPose.offset(0, 0, 9));

        ModelBakery.PartDefinition head = body.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4, -3, -3, 8, 7, 6),
                ModelBakery.PartPose.offset(0, -4, -3));
        head.addOrReplaceChild("nose",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 13).addBox(-1, 2, -7, 2, 2, 4),
                ModelBakery.PartPose.ZERO);

        return root.bake();
    }

    private ModelBakery.BakedPart buildBabyRoot() {
        ModelBakery bakery = new ModelBakery(BABY_TEXTURE, TEX_WIDTH, TEX_HEIGHT);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition body = root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(20, 0).addBox(-3, -2.5f, -4, 6, 5, 8),
                ModelBakery.PartPose.offset(0, 21.5f, 0));

        ModelBakery.PartDefinition head = body.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3, -3.5f, -4, 6, 5, 4),
                ModelBakery.PartPose.offset(0, 1, -4));
        head.addOrReplaceChild("nose",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 9).addBox(-1, -1, -2, 2, 2, 2),
                ModelBakery.PartPose.offset(0, 0.5f, -4));

        body.addOrReplaceChild("left_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(34, 18).addBox(-0.5f, -1.5f, -0.5f, 1, 3, 6),
                ModelBakery.PartPose.offsetAndRotation(1.8f, 0.85f, -2.6f, 0.8727f, 0, 1.7017f));

        body.addOrReplaceChild("right_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(48, 18).mirror().addBox(-0.5f, -1.5f, -0.5f, 1, 3, 6),
                ModelBakery.PartPose.offsetAndRotation(-1.8f, 0.85f, -2.6f, 0.8727f, 0, -1.7017f));

        ModelBakery.PartDefinition tail = body.addOrReplaceChild("tail",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 13).addBox(-2, -1.5f, 0, 4, 3, 7),
                ModelBakery.PartPose.offset(0, 1, 4));

        tail.addOrReplaceChild("tail_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(22, 13).addBox(-4, -0.5f, -1, 8, 1, 4),
                ModelBakery.PartPose.offset(0, 0, 6));

        body.addOrReplaceChild("back_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(42, 0).addBox(-0.5f, -1, 1, 1, 3, 4),
                ModelBakery.PartPose.offsetAndRotation(0, -1, -2.7f, 0.8727f, 0, 0));

        return root.bake();
    }

    @Override
    public ModelBakery.BakedPart buildRoot(Dolphin entity) {
        if (entity.isBaby()) {
            if (cachedBabyRoot == null) cachedBabyRoot = buildBabyRoot();
            return cachedBabyRoot;
        } else {
            if (cachedAdultRoot == null) cachedAdultRoot = buildAdultRoot();
            return cachedAdultRoot;
        }
    }

    @Override
    public void render(RenderPipeline pipeline, Dolphin entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double pitchDeg = entity.getXRot(1.0f);
        double relativeYawDeg = entity.getYHeadRot() - bodyYaw;
        double pitchRad = pitchDeg * Mth.DEG_TO_RAD;
        double yawRad = relativeYawDeg * Mth.DEG_TO_RAD;
        double ageInTicks = entity.tickCount + 1.0f;
        boolean moving = entity.getDeltaMovement().horizontalDistanceSqr() > 1.0E-7;

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + 1.5f, pos.z)
                .rotateY(Mth.DEG_TO_RAD * (180f - bodyYaw))
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        double bodyPitchExtra = 0f;
        double tailPitch = 0f;
        double tailFinPitch = 0f;
        if (moving) {
            bodyPitchExtra = -0.05f - 0.05f * Mth.cos(ageInTicks * 0.3f);
            tailPitch = -0.1f * Mth.cos(ageInTicks * 0.3f);
            tailFinPitch = -0.2f * Mth.cos(ageInTicks * 0.3f);
        }

        double bodyPitch = pitchRad + bodyPitchExtra;

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderPart(
                pipeline, buildRoot(entity), "root", base,
                bodyPitch, yawRad, moving, tailPitch, tailFinPitch,
                block, sky
        );
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part,
                            String name, Matrix4d parent,
                            double bodyPitch, double bodyYaw, boolean moving,
                            double tailPitch, double tailFinPitch,
                            double block, double sky) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        var ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        if (name.equals("body")) {
            mat.rotateY(bodyYaw);
            mat.rotateX(bodyPitch);
        } else if (moving) {
            if (name.equals("tail")) {
                mat.rotateX(tailPitch);
            } else if (name.equals("tail_fin")) {
                mat.rotateX(tailFinPitch);
            }
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }

        for (var child : part.children.entrySet()) {
            renderPart(
                    pipeline, child.getValue(), child.getKey(), mat,
                    bodyPitch, bodyYaw, moving, tailPitch, tailFinPitch,
                    block, sky
            );
        }
    }
}