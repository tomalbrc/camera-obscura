package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.ItemStackRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.Map;

public class ArmorStandRenderer implements LivingEntityRenderer<ArmorStand> {
    private static final String TEXTURE = "entity/armorstand/armorstand";
    private final Map<String, ModelBakery.BakedPart> armorModelOuterCache = new HashMap<>();
    private final Map<String, ModelBakery.BakedPart> armorModelInnerCache = new HashMap<>();
    private final Map<String, ModelBakery.BakedPart> elytraModelCache = new HashMap<>();
    private ModelBakery.BakedPart cachedBigModel;
    private ModelBakery.BakedPart cachedSmallModel;

    @Override
    public ModelBakery.BakedPart buildRoot(ArmorStand entity) {
        if (entity.isSmall()) {
            if (cachedSmallModel == null) cachedSmallModel = buildModel(TEXTURE);
            return cachedSmallModel;
        } else {
            if (cachedBigModel == null) cachedBigModel = buildModel(TEXTURE);
            return cachedBigModel;
        }
    }

    private ModelBakery.BakedPart buildModel(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 64, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-1, -7, -1, 2, 7, 2),
                ModelBakery.PartPose.offset(0, 1, 0));
        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(0, 26).addBox(-6, 0, -1.5f, 12, 3, 3),
                ModelBakery.PartPose.ZERO);
        root.addOrReplaceChild("right_arm",
                ModelBakery.CubeListBuilder.create().texOffs(24, 0).addBox(-2, -2, -1, 2, 12, 2),
                ModelBakery.PartPose.offset(-5, 2, 0));
        root.addOrReplaceChild("left_arm",
                ModelBakery.CubeListBuilder.create().texOffs(32, 16).mirror().addBox(0, -2, -1, 2, 12, 2),
                ModelBakery.PartPose.offset(5, 2, 0));
        root.addOrReplaceChild("right_leg",
                ModelBakery.CubeListBuilder.create().texOffs(8, 0).addBox(-1, 0, -1, 2, 11, 2),
                ModelBakery.PartPose.offset(-1.9f, 12, 0));
        root.addOrReplaceChild("left_leg",
                ModelBakery.CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1, 0, -1, 2, 11, 2),
                ModelBakery.PartPose.offset(1.9f, 12, 0));
        root.addOrReplaceChild("right_body_stick",
                ModelBakery.CubeListBuilder.create().texOffs(16, 0).addBox(-3, 3, -1, 2, 7, 2),
                ModelBakery.PartPose.ZERO);
        root.addOrReplaceChild("left_body_stick",
                ModelBakery.CubeListBuilder.create().texOffs(48, 16).addBox(1, 3, -1, 2, 7, 2),
                ModelBakery.PartPose.ZERO);
        root.addOrReplaceChild("shoulder_stick",
                ModelBakery.CubeListBuilder.create().texOffs(0, 48).addBox(-4, 10, -1, 8, 2, 2),
                ModelBakery.PartPose.ZERO);
        root.addOrReplaceChild("base_plate",
                ModelBakery.CubeListBuilder.create().texOffs(0, 32).addBox(-6, 11, -6, 12, 1, 12),
                ModelBakery.PartPose.offset(0, 12, 0));

        return root.bake();
    }

    private ModelBakery.BakedPart buildHumanoidArmorModel(String texture, float grow) {
        ModelBakery bakery = new ModelBakery(texture, 64, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();
        ModelBakery.CubeDeformation def = new ModelBakery.CubeDeformation(grow);

        root.addOrReplaceChild("head", ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8, def), ModelBakery.PartPose.offset(0, 0, 0));
        root.addOrReplaceChild("body", ModelBakery.CubeListBuilder.create().texOffs(16, 16).addBox(-4, 0, -2, 8, 12, 4, def), ModelBakery.PartPose.offset(0, 0, 0));
        root.addOrReplaceChild("right_arm", ModelBakery.CubeListBuilder.create().texOffs(40, 16).addBox(-3, -2, -2, 4, 12, 4, def), ModelBakery.PartPose.offset(-5, 2, 0));
        root.addOrReplaceChild("left_arm", ModelBakery.CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1, -2, -2, 4, 12, 4, def), ModelBakery.PartPose.offset(5, 2, 0));
        root.addOrReplaceChild("right_leg", ModelBakery.CubeListBuilder.create().texOffs(0, 16).addBox(-2, 0, -2, 4, 12, 4, def), ModelBakery.PartPose.offset(-1.9f, 12, 0));
        root.addOrReplaceChild("left_leg", ModelBakery.CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2, 0, -2, 4, 12, 4, def), ModelBakery.PartPose.offset(1.9f, 12, 0));

        return root.bake();
    }

    private ModelBakery.BakedPart getArmorModelOuter(String texture) {
        return armorModelOuterCache.computeIfAbsent(texture, t -> buildHumanoidArmorModel(t, 0.8f));
    }

    private ModelBakery.BakedPart getArmorModelInner(String texture) {
        return armorModelInnerCache.computeIfAbsent(texture, t -> buildHumanoidArmorModel(t, 0.5f));
    }

    private ModelBakery.BakedPart getElytraModel(String texture) {
        return elytraModelCache.computeIfAbsent(texture, this::buildElytraModel);
    }

    private ModelBakery.BakedPart buildElytraModel(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 64, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();
        root.addOrReplaceChild("left_wing",
                ModelBakery.CubeListBuilder.create().texOffs(22, 0).addBox(-10, 0, 0, 10, 20, 2, new ModelBakery.CubeDeformation(1.0f)),
                ModelBakery.PartPose.offsetAndRotation(5, 0, 0, (Mth.PI / 12), 0, (-Mth.PI / 12)));
        root.addOrReplaceChild("right_wing",
                ModelBakery.CubeListBuilder.create().texOffs(22, 0).mirror().addBox(0, 0, 0, 10, 20, 2, new ModelBakery.CubeDeformation(1.0f)),
                ModelBakery.PartPose.offsetAndRotation(-5, 0, 0, (Mth.PI / 12), 0, (Mth.PI / 12)));
        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, ArmorStand entity) {
        var pos = entity.position();
        double bodyYaw = Mth.rotLerp(1.0f, entity.yRotO, entity.getYRot());
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyYaw);
        boolean small = entity.isSmall();
        boolean showArms = entity.showArms();
        boolean showBase = entity.showBasePlate();

        Vector3d bodyPose = new Vector3d(entity.getBodyPose().x(), entity.getBodyPose().y(), entity.getBodyPose().z());
        Vector3d headPose = new Vector3d(entity.getHeadPose().x(), entity.getHeadPose().y(), entity.getHeadPose().z());
        Vector3d leftArmPose = new Vector3d(entity.getLeftArmPose().x(), entity.getLeftArmPose().y(), entity.getLeftArmPose().z());
        Vector3d rightArmPose = new Vector3d(entity.getRightArmPose().x(), entity.getRightArmPose().y(), entity.getRightArmPose().z());
        Vector3d leftLegPose = new Vector3d(entity.getLeftLegPose().x(), entity.getLeftLegPose().y(), entity.getLeftLegPose().z());
        Vector3d rightLegPose = new Vector3d(entity.getRightLegPose().x(), entity.getRightLegPose().y(), entity.getRightLegPose().z());

        double wiggle = (entity.level().getGameTime() - entity.lastHit) + 1.0f;

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y, pos.z)
                .rotateY(modelYaw)
                .rotateY(Mth.PI);

        if (small) base.scale(0.5f);

        if (wiggle < 5.0f) {
            base.rotateY(Mth.DEG_TO_RAD * Mth.sin(wiggle / 1.5f * Mth.PI) * 3.0f);
        }

        base.translate(0, 1.5f, 0);
        base.rotateX(Mth.PI);

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        ModelBakery.BakedPart model = buildRoot(entity);
        if (!entity.isInvisible()) {
            for (var child : model.children.entrySet()) {
                String name = child.getKey();
                ModelBakery.BakedPart part = child.getValue();

                if (!showArms && (name.equals("right_arm") || name.equals("left_arm"))) continue;
                if (!showBase && name.equals("base_plate")) continue;

                Matrix4d mat = new Matrix4d(base);
                mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);
                ModelBakery.PartPose ip = part.initialPose;
                if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
                    mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
                if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
                    mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

                switch (name) {
                    case "head" -> applyPose(mat, headPose);
                    case "body" -> applyPose(mat, bodyPose);
                    case "right_arm" -> applyPose(mat, rightArmPose);
                    case "left_arm" -> applyPose(mat, leftArmPose);
                    case "right_leg" -> applyPose(mat, rightLegPose);
                    case "left_leg" -> applyPose(mat, leftLegPose);
                    case "base_plate" -> mat.rotateY(Mth.DEG_TO_RAD * -bodyYaw);
                    case "right_body_stick", "left_body_stick", "shoulder_stick" -> applyPose(mat, bodyPose);
                }

                if (part.mesh != null) {
                    pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
                }
            }
        }

        renderArmor(pipeline, entity, base, small, block, sky);

        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.get(DataComponents.EQUIPPABLE) != null) {
            String elytraTex = HumanoidRenderer.resolveEquipmentTexture(chest, "wings");
            if (elytraTex != null) {
                Matrix4d elytraBase = new Matrix4d(base).translate(0, 0, 0.125f);
                ModelBakery.BakedPart elytra = getElytraModel(elytraTex);
                ModelBakery.BakedPart leftWing = elytra.children.get("left_wing");
                ModelBakery.BakedPart rightWing = elytra.children.get("right_wing");
                if (leftWing != null && leftWing.mesh != null) {
                    Matrix4d lm = new Matrix4d(elytraBase).translate(leftWing.localPivot.x, leftWing.localPivot.y, leftWing.localPivot.z);
                    ModelBakery.PartPose lpose = leftWing.initialPose;
                    if (lpose.xRot() != 0 || lpose.yRot() != 0 || lpose.zRot() != 0)
                        lm.rotateZYX(lpose.zRot(), lpose.yRot(), lpose.xRot());
                    if (lpose.xScale() != 1 || lpose.yScale() != 1 || lpose.zScale() != 1)
                        lm.scale(lpose.xScale(), lpose.yScale(), lpose.zScale());
                    pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(leftWing.mesh, block, sky), lm, IntList.of(0xFFFFFFFF)));
                }
                if (rightWing != null && rightWing.mesh != null) {
                    Matrix4d rm = new Matrix4d(elytraBase).translate(rightWing.localPivot.x, rightWing.localPivot.y, rightWing.localPivot.z);
                    ModelBakery.PartPose rpose = rightWing.initialPose;
                    if (rpose.xRot() != 0 || rpose.yRot() != 0 || rpose.zRot() != 0)
                        rm.rotateZYX(rpose.zRot(), rpose.yRot(), rpose.xRot());
                    if (rpose.xScale() != 1 || rpose.yScale() != 1 || rpose.zScale() != 1)
                        rm.scale(rpose.xScale(), rpose.yScale(), rpose.zScale());
                    pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(rightWing.mesh, block, sky), rm, IntList.of(0xFFFFFFFF)));
                }
            }
        }

        ItemStack headItem = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (!headItem.isEmpty() && !headItem.has(DataComponents.EQUIPPABLE)) {
            Matrix4d headMat = computePartWorldMatrix(model, "head", base, headPose);
            headMat.translate(0.3f, 0.05f, -0.3f);
            headMat.rotateY(Math.toRadians(180));
            headMat.scale(0.625f, -0.625f, -0.625f);
            ItemStackRenderer.render(pipeline, headItem, ItemDisplayContext.HEAD, headMat);
        }
    }

    private void applyPose(Matrix4d mat, Vector3d poseDeg) {
        mat.rotateX(poseDeg.x() * Mth.DEG_TO_RAD);
        mat.rotateY(poseDeg.y() * Mth.DEG_TO_RAD);
        mat.rotateZ(poseDeg.z() * Mth.DEG_TO_RAD);
    }

    private void renderArmor(RenderPipeline pipeline, ArmorStand entity, Matrix4d base, boolean small, double block, double sky) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;
            ItemStack stack = entity.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            String layer = (slot == EquipmentSlot.LEGS) ? "humanoid_leggings" : "humanoid";
            String tex = HumanoidRenderer.resolveEquipmentTexture(stack, layer);
            if (tex == null) continue;

            ModelBakery.BakedPart armor = (slot == EquipmentSlot.LEGS)
                    ? getArmorModelInner(tex)
                    : getArmorModelOuter(tex);

            renderArmorPart(pipeline, armor, "head", base, new Vector3d(entity.getHeadPose().x(), entity.getHeadPose().y(), entity.getHeadPose().z()), block, sky);
            renderArmorPart(pipeline, armor, "body", base, new Vector3d(entity.getBodyPose().x(), entity.getBodyPose().y(), entity.getBodyPose().z()), block, sky);
            renderArmorPart(pipeline, armor, "right_arm", base, new Vector3d(entity.getRightArmPose().x(), entity.getRightArmPose().y(), entity.getRightArmPose().z()), block, sky);
            renderArmorPart(pipeline, armor, "left_arm", base, new Vector3d(entity.getLeftArmPose().x(), entity.getLeftArmPose().y(), entity.getLeftArmPose().z()), block, sky);
            renderArmorPart(pipeline, armor, "right_leg", base, new Vector3d(entity.getRightLegPose().x(), entity.getRightLegPose().y(), entity.getRightLegPose().z()), block, sky);
            renderArmorPart(pipeline, armor, "left_leg", base, new Vector3d(entity.getLeftLegPose().x(), entity.getLeftLegPose().y(), entity.getLeftLegPose().z()), block, sky);
        }
    }

    private void renderArmorPart(RenderPipeline pipeline, ModelBakery.BakedPart armor, String partName, Matrix4d base, Vector3d poseDeg, double block, double sky) {
        ModelBakery.BakedPart part = armor.children.get(partName);
        if (part == null || part.mesh == null) return;
        Matrix4d mat = new Matrix4d(base);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);
        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());
        applyPose(mat, poseDeg);
        pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
    }

    private Matrix4d computePartWorldMatrix(ModelBakery.BakedPart root, String target, Matrix4d parent, Vector3d poseDeg) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(root.localPivot.x, root.localPivot.y, root.localPivot.z);
        if (root.children.containsKey(target)) {
            ModelBakery.BakedPart part = root.children.get(target);
            mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);
            ModelBakery.PartPose ip = part.initialPose;
            if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0) mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
            applyPose(mat, poseDeg);
            return mat;
        }
        return mat;
    }
}