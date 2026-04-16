package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.BlockStateRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.golem.CopperGolem;
import net.minecraft.world.entity.animal.golem.CopperGolemOxidationLevels;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4d;

import java.util.HashMap;
import java.util.Map;

public class CopperGolemRenderer implements LivingEntityRenderer<CopperGolem> {

    private static final Map<String, ModelBakery.BakedPart> MODEL_CACHE = new HashMap<>();
    private static final Map<String, ModelBakery.BakedPart> EYE_CACHE = new HashMap<>();

    @Override
    public ModelBakery.BakedPart buildRoot(CopperGolem entity) {
        var id = CopperGolemOxidationLevels.getOxidationLevel(entity.getWeatherState()).texture();
        var path = id.getPath().substring("textures/".length(), id.getPath().length() - 4);
        var key = id.getNamespace() + ":" + path;
        return MODEL_CACHE.computeIfAbsent(key, this::buildModel);
    }

    private ModelBakery.BakedPart getEyeModel(CopperGolem entity) {
        var id = CopperGolemOxidationLevels.getOxidationLevel(entity.getWeatherState()).eyeTexture();
        var path = id.getPath().substring("textures/".length(), id.getPath().length() - 4);
        var key = id.getNamespace() + ":" + path;
        return EYE_CACHE.computeIfAbsent(key, k -> buildModel(k));
    }

    private ModelBakery.BakedPart buildModel(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 64, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition body = root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(0, 15).addBox(-4, -6, -3, 8, 6, 6),
                ModelBakery.PartPose.offset(0, -5, 0));

        body.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4, -5, -5, 8, 5, 10, new ModelBakery.CubeDeformation(0.015f))
                        .texOffs(56, 0).addBox(-1, -2, -6, 2, 3, 2)
                        .texOffs(37, 8).addBox(-1, -9, -1, 2, 4, 2, new ModelBakery.CubeDeformation(-0.015f))
                        .texOffs(37, 0).addBox(-2, -13, -2, 4, 4, 4, new ModelBakery.CubeDeformation(-0.015f)),
                ModelBakery.PartPose.offset(0, -6, 0));

        body.addOrReplaceChild("right_arm",
                ModelBakery.CubeListBuilder.create().texOffs(36, 16).addBox(-3, -1, -2, 3, 10, 4),
                ModelBakery.PartPose.offset(-4, -6, 0));
        body.addOrReplaceChild("left_arm",
                ModelBakery.CubeListBuilder.create().texOffs(50, 16).addBox(0, -1, -2, 3, 10, 4),
                ModelBakery.PartPose.offset(4, -6, 0));

        root.addOrReplaceChild("right_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 27).addBox(-4, 0, -2, 4, 5, 4),
                ModelBakery.PartPose.offset(0, -5, 0));
        root.addOrReplaceChild("left_leg",
                ModelBakery.CubeListBuilder.create().texOffs(16, 27).addBox(0, 0, -2, 4, 5, 4),
                ModelBakery.PartPose.offset(0, -5, 0));

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, CopperGolem entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyYaw);
        double headYaw = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYaw);
        double headPitch = Mth.DEG_TO_RAD * entity.getXRot(1.0f);
        boolean holdingItem = !entity.getMainHandItem().isEmpty() || !entity.getOffhandItem().isEmpty();
        double walkPos = entity.walkAnimation.position();
        double walkSpeed = entity.walkAnimation.speed();

        double legRight = Mth.cos(walkPos * 0.6662f) * 1.4f * walkSpeed * 0.5f;
        double legLeft = Mth.cos(walkPos * 0.6662f + Mth.PI) * 1.4f * walkSpeed * 0.5f;

        double armRight, armLeft;
        if (holdingItem) {
            armRight = -0.87266463f;
            armLeft = -0.87266463f;
        } else {
            armRight = Mth.cos(walkPos * 0.6662f + Mth.PI) * 1.4f * walkSpeed * 0.5f;
            armLeft = Mth.cos(walkPos * 0.6662f) * 1.4f * walkSpeed * 0.5f;
        }

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y, pos.z)
                .rotateY(modelYaw)
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        ModelBakery.BakedPart mainModel = buildRoot(entity);
        renderPart(pipeline, mainModel, "root", base, headYaw, headPitch, legRight, legLeft, armRight, armLeft, block, sky);

        ModelBakery.BakedPart eyeModel = getEyeModel(entity);
        ModelBakery.BakedPart eyeHead = eyeModel.children.get("body").children.get("head");
        if (eyeHead != null) {
            Matrix4d eyeBase = new Matrix4d(base);

            eyeBase.translate(mainModel.children.get("body").localPivot.x, mainModel.children.get("body").localPivot.y, mainModel.children.get("body").localPivot.z);
            ModelBakery.PartPose bodyIp = mainModel.children.get("body").initialPose;
            if (bodyIp.xRot() != 0 || bodyIp.yRot() != 0 || bodyIp.zRot() != 0)
                eyeBase.rotateZYX(bodyIp.zRot(), bodyIp.yRot(), bodyIp.xRot());
            if (bodyIp.xScale() != 1 || bodyIp.yScale() != 1 || bodyIp.zScale() != 1)
                eyeBase.scale(bodyIp.xScale(), bodyIp.yScale(), bodyIp.zScale());

            renderHeadPart(pipeline, eyeHead, eyeBase, headYaw, headPitch, 1, 1);
        }

        ItemStack antennaItem = entity.getItemBySlot(CopperGolem.EQUIPMENT_SLOT_ANTENNA);
        if (!antennaItem.isEmpty() && antennaItem.getItem() instanceof BlockItem blockItem) {
            Matrix4d headMat = getPartWorldMatrix(mainModel, "head", base, headYaw, headPitch, legRight, legLeft, armRight, armLeft);
            if (headMat != null) {
                BlockState state = blockItem.getBlock().defaultBlockState();
                Matrix4d blockMat = new Matrix4d(headMat)
                        .translate(0, -1.75f / 16f, 0)
                        .translate(-0.5f, -0.5f, -0.5f)
                        .scale(0.625f, 0.625f, 0.625f);
                BlockStateRenderer.render(pipeline, state, blockMat, block, sky);
            }
        }
    }

    private void renderHeadPart(RenderPipeline pipeline, ModelBakery.BakedPart part, Matrix4d parent, double headYaw, double headPitch, double block, double sky) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);
        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());
        mat.rotateY(headYaw);
        mat.rotateX(headPitch);

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }
        for (var child : part.children.entrySet()) {
            renderHeadPart(pipeline, child.getValue(), mat, headYaw, headPitch, block, sky);
        }
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                            Matrix4d parent, double headYaw, double headPitch,
                            double legRight, double legLeft, double armRight, double armLeft,
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
            case "right_arm" -> mat.rotateX(armRight);
            case "left_arm" -> mat.rotateX(armLeft);
            case "right_leg" -> mat.rotateX(legRight);
            case "left_leg" -> mat.rotateX(legLeft);
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }
        for (var child : part.children.entrySet()) {
            renderPart(pipeline, child.getValue(), child.getKey(), mat, headYaw, headPitch, legRight, legLeft, armRight, armLeft, block, sky);
        }
    }

    private Matrix4d getPartWorldMatrix(ModelBakery.BakedPart part, String target,
                                        Matrix4d parent, double headYaw, double headPitch,
                                        double legRight, double legLeft, double armRight, double armLeft) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);
        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        switch (target) {
            case "head" -> {
                mat.rotateY(headYaw);
                mat.rotateX(headPitch);
            }
            case "right_arm" -> mat.rotateX(armRight);
            case "left_arm" -> mat.rotateX(armLeft);
            case "right_leg" -> mat.rotateX(legRight);
            case "left_leg" -> mat.rotateX(legLeft);
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
                Matrix4d found = getPartWorldMatrix(child.getValue(), target, mat, headYaw, headPitch, legRight, legLeft, armRight, armLeft);
                if (found != null) return found;
            }
        }
        return null;
    }
}