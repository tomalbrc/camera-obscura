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
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public class WitchRenderer implements LivingEntityRenderer<Witch> {

    private static final String TEXTURE = "entity/witch/witch";
    private ModelBakery.BakedPart cachedModel;

    @Override
    public ModelBakery.BakedPart buildRoot(Witch entity) {
        if (cachedModel == null) {
            cachedModel = buildModel();
        }
        return cachedModel;
    }

    private ModelBakery.BakedPart buildModel() {
        ModelBakery bakery = new ModelBakery(TEXTURE, 64, 128);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition rootPart = model.root();

        ModelBakery.PartDefinition head = rootPart.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-4, -10, -4, 8, 10, 8),
                ModelBakery.PartPose.ZERO);

        ModelBakery.PartDefinition hat = head.addOrReplaceChild("hat",
                ModelBakery.CubeListBuilder.create().texOffs(0, 64).addBox(0, 0, 0, 10, 2, 10),
                ModelBakery.PartPose.offset(-5, -10.03125f, -5));
        ModelBakery.PartDefinition hat2 = hat.addOrReplaceChild("hat2",
                ModelBakery.CubeListBuilder.create().texOffs(0, 76).addBox(0, 0, 0, 7, 4, 7),
                ModelBakery.PartPose.offsetAndRotation(1.75f, -4, 2, -0.05235988f, 0, 0.02617994f));
        ModelBakery.PartDefinition hat3 = hat2.addOrReplaceChild("hat3",
                ModelBakery.CubeListBuilder.create().texOffs(0, 87).addBox(0, 0, 0, 4, 4, 4),
                ModelBakery.PartPose.offsetAndRotation(1.75f, -4, 2, -0.10471976f, 0, 0.05235988f));
        hat3.addOrReplaceChild("hat4",
                ModelBakery.CubeListBuilder.create().texOffs(0, 95).addBox(0, 0, 0, 1, 2, 1, new ModelBakery.CubeDeformation(0.25f)),
                ModelBakery.PartPose.offsetAndRotation(1.75f, -2, 2, (-Mth.PI / 15), 0, 0.10471976f));

        ModelBakery.PartDefinition nose = head.addOrReplaceChild("nose",
                ModelBakery.CubeListBuilder.create().texOffs(24, 0).addBox(-1, -1, -6, 2, 4, 2),
                ModelBakery.PartPose.offset(0, -2, 0));
        nose.addOrReplaceChild("mole",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(0, 3, -6.75f, 1, 1, 1, new ModelBakery.CubeDeformation(-0.25f)),
                ModelBakery.PartPose.offset(0, -2, 0));

        ModelBakery.PartDefinition body = rootPart.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(16, 20).addBox(-4, 0, -3, 8, 12, 6),
                ModelBakery.PartPose.ZERO);
        body.addOrReplaceChild("jacket",
                ModelBakery.CubeListBuilder.create().texOffs(0, 38).addBox(-4, 0, -3, 8, 20, 6, new ModelBakery.CubeDeformation(0.5f)),
                ModelBakery.PartPose.ZERO);

        rootPart.addOrReplaceChild("arms",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(44, 22).addBox(-8, -2, -2, 4, 8, 4)
                        .texOffs(44, 22).addBox(4, -2, -2, 4, 8, 4)
                        .texOffs(40, 38).addBox(-4, 2, -2, 8, 4, 4),
                ModelBakery.PartPose.offsetAndRotation(0, 3, -1, -0.75f, 0, 0));

        rootPart.addOrReplaceChild("right_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 22).addBox(-2, 0, -2, 4, 12, 4),
                ModelBakery.PartPose.offset(-2, 12, 0));
        rootPart.addOrReplaceChild("left_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 22).mirror().addBox(-2, 0, -2, 4, 12, 4),
                ModelBakery.PartPose.offset(2, 12, 0));

        return rootPart.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, Witch entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyYaw);
        double headYaw = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYaw);
        double headPitch = Mth.DEG_TO_RAD * entity.getXRot(1.0f);
        double walkPos = entity.walkAnimation.position();
        double walkSpeed = entity.walkAnimation.speed();
        int entityId = entity.getId();

        boolean holdingItem = !entity.getMainHandItem().isEmpty();
        ItemStack mainHandItem = entity.getMainHandItem();

        double legRightAngle = Mth.cos(walkPos * 0.6662f) * 1.4f * walkSpeed * 0.5f;
        double legLeftAngle = Mth.cos(walkPos * 0.6662f + Mth.PI) * 1.4f * walkSpeed * 0.5f;

        double speed = 0.01f * (entityId % 10);
        double ageInTicks = entity.tickCount + 1.0f;
        double noseXRot = Mth.sin(ageInTicks * speed) * 4.5f * Mth.DEG_TO_RAD;
        double noseZRot = Mth.cos(ageInTicks * speed) * 2.5f * Mth.DEG_TO_RAD;
        double noseY = 0;
        double noseZ = 0;
        if (holdingItem) {
            noseY = 1.0f;
            noseZ = -1.5f;
            noseXRot = -0.9f;
        }

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + 1.5f, pos.z)
                .rotateY(modelYaw)
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        ModelBakery.BakedPart rootModel = buildRoot(entity);
        renderPart(pipeline, rootModel, "root", base, headYaw, headPitch,
                legRightAngle, legLeftAngle, noseXRot, noseZRot, noseY, noseZ, block, sky);

        if (!mainHandItem.isEmpty()) {
            Matrix4d armsWorld = getPartWorldMatrix(rootModel, "arms", base,
                    headYaw, headPitch, legRightAngle, legLeftAngle,
                    noseXRot, noseZRot, noseY, noseZ);
            if (armsWorld != null) {
                Matrix4d itemMat = new Matrix4d(armsWorld);
                itemMat.translate(0.0f, -0.05f, -0.25f);
                itemMat.rotateX(Mth.PI / 2);
                ItemStackRenderer.render(pipeline, mainHandItem, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, itemMat, block, sky);
            }
        }
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                            Matrix4d parent,
                            double headYaw, double headPitch,
                            double legRight, double legLeft,
                            double noseXRot, double noseZRot, double noseY, double noseZ,
                            double block, double sky) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        switch (name) {
            case "head" -> {
                mat.rotateY(headYaw);
                mat.rotateX(headPitch);
            }
            case "right_leg" -> mat.rotateX(legRight);
            case "left_leg" -> mat.rotateX(legLeft);
            case "nose" -> {
                if (noseY != 0 || noseZ != 0) {
                    mat.translate(0, noseY / 16f, noseZ / 16f);
                }
                mat.rotateX(noseXRot);
                mat.rotateZ(noseZRot);
            }
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }

        for (var child : part.children.entrySet()) {
            renderPart(pipeline, child.getValue(), child.getKey(), mat,
                    headYaw, headPitch, legRight, legLeft, noseXRot, noseZRot, noseY, noseZ,
                    block, sky);
        }
    }

    private Matrix4d getPartWorldMatrix(ModelBakery.BakedPart part, String target,
                                        Matrix4d parent,
                                        double headYaw, double headPitch,
                                        double legRight, double legLeft,
                                        double noseXRot, double noseZRot, double noseY, double noseZ) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        if (target.equals("head")) mat.rotateY(headYaw).rotateX(headPitch);
        if (target.equals("right_leg")) mat.rotateX(legRight);
        if (target.equals("left_leg")) mat.rotateX(legLeft);
        if (target.equals("nose")) {
            if (noseY != 0 || noseZ != 0) mat.translate(0, noseY / 16f, noseZ / 16f);
            mat.rotateX(noseXRot);
            mat.rotateZ(noseZRot);
        }

        for (var child : part.children.entrySet()) {
            if (child.getKey().equals(target)) {
                Matrix4d childMat = new Matrix4d(mat);
                childMat.translate(child.getValue().localPivot.x, child.getValue().localPivot.y, child.getValue().localPivot.z);
                ModelBakery.PartPose cip = child.getValue().initialPose;
                if (cip.xRot() != 0 || cip.yRot() != 0 || cip.zRot() != 0)
                    childMat.rotateZYX(cip.zRot(), cip.yRot(), cip.xRot());
                if (cip.xScale() != 1 || cip.yScale() != 1 || cip.zScale() != 1)
                    childMat.scale(cip.xScale(), cip.yScale(), cip.zScale());
                return childMat;
            } else {
                Matrix4d found = getPartWorldMatrix(child.getValue(), target, mat,
                        headYaw, headPitch, legRight, legLeft, noseXRot, noseZRot, noseY, noseZ);
                if (found != null) return found;
            }
        }
        return null;
    }
}
