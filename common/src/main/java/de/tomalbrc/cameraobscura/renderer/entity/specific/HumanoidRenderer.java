package de.tomalbrc.cameraobscura.renderer.entity.specific;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.tomalbrc.cameraobscura.renderer.entity.ItemStackRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import de.tomalbrc.cameraobscura.util.resource.RPHelper;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwingAnimationType;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.Map;

public abstract class HumanoidRenderer<T extends LivingEntity> implements LivingEntityRenderer<T> {

    private static final double PIXEL = 1.0F / 16.0F;
    private final Map<String, ModelBakery.BakedPart> armorModelCache = new HashMap<>();
    private final Map<String, ModelBakery.BakedPart> elytraModelCache = new HashMap<>();
    private ModelBakery.BakedPart cachedRoot;

    public static String resolveEquipmentTexture(ItemStack stack, String layer) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable == null || equippable.assetId().isEmpty()) return null;
        Identifier assetId = equippable.assetId().get().identifier();
        var eq = RPHelper.getEquipment(assetId);
        if (eq == null) return null;

        JsonObject json = eq.getAsJsonObject();
        if (json == null) return null;
        JsonObject layers = json.getAsJsonObject("layers");
        if (layers == null) return null;
        JsonArray layerArray = layers.getAsJsonArray(layer);
        if (layerArray == null || layerArray.isEmpty()) return null;
        String textureId = layerArray.get(0).getAsJsonObject().get("texture").getAsString();
        Identifier textureLoc = Identifier.parse(textureId);
        return textureLoc.getNamespace() + ":entity/equipment/" + layer + "/" + textureLoc.getPath();
    }

    private static double quadraticArmUpdate(double x) {
        return -65.0F * x + x * x;
    }

    private ModelBakery.BakedPart getArmorModel(String texture, boolean baby) {
        String key = texture + (baby ? "_baby" : "");
        return armorModelCache.computeIfAbsent(key, k -> {
            var bakery = new ModelBakery(texture, getTexWidth(), getTexHeight());
            var model = new ModelBakery.ModelDefinition(bakery);
            buildModelParts(model.root(), bakery);
            return model.root().bake();
        });
    }

    private ModelBakery.BakedPart getElytraModel(String texture, boolean baby) {
        String key = texture + (baby ? "_baby" : "");
        return elytraModelCache.computeIfAbsent(key, k -> buildElytraModel(texture, baby));
    }

    private ModelBakery.BakedPart buildElytraModel(String texture, boolean baby) {
        ModelBakery bakery = new ModelBakery(texture, 64, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("left_wing",
                ModelBakery.CubeListBuilder.create().texOffs(22, 0)
                        .addBox(-10, 0, 0, 10, 20, 2, new ModelBakery.CubeDeformation(1.0F)),
                ModelBakery.PartPose.offsetAndRotation(5, 0, 0,
                        (Mth.PI / 12), 0, (-Mth.PI / 12)));

        root.addOrReplaceChild("right_wing",
                ModelBakery.CubeListBuilder.create().texOffs(22, 0).mirror()
                        .addBox(0, 0, 0, 10, 20, 2, new ModelBakery.CubeDeformation(1.0F)),
                ModelBakery.PartPose.offsetAndRotation(-5, 0, 0,
                        (Mth.PI / 12), 0, (Mth.PI / 12)));

        return root.bake();
    }

    protected abstract String getTexturePath(T entity);

    protected abstract int getTexWidth();

    protected abstract int getTexHeight();

    protected abstract void buildModelParts(ModelBakery.PartDefinition root, ModelBakery bakery);

    protected boolean isBaby(T entity) {
        return entity.isBaby();
    }

    protected boolean isSlim(T entity) {
        return false;
    }

    protected boolean isOverlayPartVisible(String partName, T entity) {
        return true;
    }

    protected ArmPose getArmPose(final T mob, final HumanoidArm arm) {
        ItemStack itemHeldByArm = mob.getItemHeldByArm(arm);
        SwingAnimation anim = itemHeldByArm.get(DataComponents.SWING_ANIMATION);
        if (anim != null && anim.type() == SwingAnimationType.STAB && mob.swinging) {
            return ArmPose.SPEAR;
        } else {
            return itemHeldByArm.is(ItemTags.SPEARS) ? ArmPose.SPEAR : ArmPose.EMPTY;
        }
    }

    protected Vector3d getHandItemOffset(T entity, HumanoidArm arm) {
        return new Vector3d();
    }

    @Override
    public ModelBakery.BakedPart buildRoot(T entity) {
        if (cachedRoot == null) {
            var bakery = new ModelBakery(getTexturePath(entity), getTexWidth(), getTexHeight());
            var model = new ModelBakery.ModelDefinition(bakery);
            buildModelParts(model.root(), bakery);
            cachedRoot = model.root().bake();
        }
        return cachedRoot;
    }

    @Override
    public void render(RenderPipeline pipeline, T entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.f);
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyYaw);
        double headYaw = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYaw);
        double headPitch = Mth.DEG_TO_RAD * entity.getXRot(1.f);

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y, pos.z)
                .rotateY(modelYaw)
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        var scale = scale();
        if (scale != 1.0f) {
            base.scale(scale);
        }

        double animPos = entity.walkAnimation.position();
        double animSpeed = entity.walkAnimation.speed();
        double swim = entity.getSwimAmount(1.f);
        boolean flying = entity.isFallFlying() && entity.getFallFlyingTicks() > 0;
        boolean crouch = entity.isCrouching();
        boolean passenger = entity.isPassenger();

        double bodyRotX = getBodyRotX(entity, swim, flying);
        double zOffset = getSwimZOffset(entity, swim);
        if (bodyRotX != 0) base.rotateX(bodyRotX);
        if (zOffset != 0) base.translate(0, 0, zOffset);
        else base.translate(0, -1.5f, 0);

        double limbSwing = animPos * 0.6662F;
        double speedValue = getSpeedValue(entity);
        LimbAngles angles = new LimbAngles();
        angles.headYaw = headYaw;
        angles.headPitch = headPitch;

        angles.rightLegAngle = Mth.cos(limbSwing) * 1.4F * animSpeed / speedValue;
        angles.leftLegAngle = Mth.cos(limbSwing + Mth.PI) * 1.4F * animSpeed / speedValue;

        angles.rightLegYaw = 0.0F;
        angles.leftLegYaw = 0.0F;
        angles.rightLegZ = 0.0F;
        angles.leftLegZ = 0.0F;

        double armWalkR = Mth.cos(limbSwing + Mth.PI) * 2.0F * animSpeed * 0.5F / speedValue;
        double armWalkL = Mth.cos(limbSwing) * 2.0F * animSpeed * 0.5F / speedValue;
        angles.rightArmAngle = armWalkR;
        angles.leftArmAngle = armWalkL;

        angles = getLimbAngles(entity, animPos, animSpeed, headYaw, headPitch, swim, flying, crouch, angles);

        if (passenger) {
            applyPassengerPose(angles);
        }


        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        if (!entity.isInvisible())
            renderWithAngles(pipeline, entity, base, angles, swim, flying, crouch, animPos, block, sky);

        renderArmorLayer(pipeline, entity, base, angles, swim, flying, crouch, block, sky);
        renderElytraLayer(pipeline, entity, base, angles, swim, flying, crouch, block, sky);
    }

    protected double getBodyRotX(T entity, double swimAmount, boolean isFallFlying) {
        double bodyXRotDeg = 0;
        if (isFallFlying && !entity.isAutoSpinAttack()) {
            double fallFlyingScale = Mth.clamp((entity.getFallFlyingTicks() + 1f) / 20f, 0, 1);
            bodyXRotDeg = fallFlyingScale * (-90f - entity.getXRot(1f));
        } else if (swimAmount > 0) {
            double target = entity.isInWater() ? -90f - entity.getXRot(1f) : -90f;
            bodyXRotDeg = Mth.lerp(swimAmount, 0, target);
        }
        return -bodyXRotDeg * Mth.DEG_TO_RAD;
    }

    private void renderArmorLayer(RenderPipeline pipeline, T entity, Matrix4d base,
                                  LimbAngles angles, double swim, boolean flying, boolean crouch,
                                  double block, double sky) {
        boolean baby = isBaby(entity);
        EquipmentSlot[] slots = {
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        };
        for (EquipmentSlot slot : slots) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
            if (equippable == null || equippable.assetId().isEmpty()) continue;
            if (equippable.slot() != slot) continue;

            String layer = (slot == EquipmentSlot.LEGS) ? "humanoid_leggings" : "humanoid";
            if (baby) layer = "humanoid_baby";
            String texture = resolveEquipmentTexture(stack, layer);
            if (texture == null) continue;

            ModelBakery.BakedPart armorModel = getArmorModel(texture, baby);
            renderPart(pipeline, armorModel, "root", base,
                    angles,
                    ArmPose.EMPTY, ArmPose.EMPTY,
                    ItemStack.EMPTY, ItemStack.EMPTY,
                    entity, swim, flying, crouch, 0,
                    new Matrix4d[1], new Matrix4d[1], new Matrix4d[1],
                    block, sky);
        }
    }

    private void renderElytraLayer(RenderPipeline pipeline, T entity, Matrix4d base, LimbAngles angles, double swim, boolean flying, boolean crouch, double block, double sky) {
        ItemStack chestStack = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (chestStack.isEmpty()) return;

        Equippable equippable = chestStack.get(DataComponents.EQUIPPABLE);
        if (equippable == null || equippable.assetId().isEmpty()) return;
        String texture = resolveEquipmentTexture(chestStack, "wings");
        if (texture == null) return;

        boolean baby = isBaby(entity);
        ModelBakery.BakedPart elytraModel = getElytraModel(texture, baby);

        Matrix4d elytraBase = new Matrix4d(base);
        elytraBase.translate(0, 0, 0.125F);

        double wingSpread = flying ? (Math.PI / 4) : 0.0F;

        ModelBakery.BakedPart leftWing = elytraModel.children.get("left_wing");
        ModelBakery.BakedPart rightWing = elytraModel.children.get("right_wing");
        if (leftWing != null && leftWing.mesh != null) {
            Matrix4d leftMat = new Matrix4d(elytraBase)
                    .translate(leftWing.localPivot.x, leftWing.localPivot.y, leftWing.localPivot.z);

            ModelBakery.PartPose lp = leftWing.initialPose;
            if (lp.xRot() != 0 || lp.yRot() != 0 || lp.zRot() != 0)
                leftMat.rotateZYX(lp.zRot(), lp.yRot(), lp.xRot());
            if (lp.xScale() != 1 || lp.yScale() != 1 || lp.zScale() != 1)
                leftMat.scale(lp.xScale(), lp.yScale(), lp.zScale());

            leftMat.rotateZ(-wingSpread);

            if (crouch) leftMat.translate(0, 3.0F / 16.0F, 0);

            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(leftWing.mesh, block, sky), leftMat, IntList.of(0xFFFFFFFF)));
        }
        if (rightWing != null && rightWing.mesh != null) {
            Matrix4d rightMat = new Matrix4d(elytraBase)
                    .translate(rightWing.localPivot.x, rightWing.localPivot.y, rightWing.localPivot.z);
            ModelBakery.PartPose rp = rightWing.initialPose;
            if (rp.xRot() != 0 || rp.yRot() != 0 || rp.zRot() != 0)
                rightMat.rotateZYX(rp.zRot(), rp.yRot(), rp.xRot());
            if (rp.xScale() != 1 || rp.yScale() != 1 || rp.zScale() != 1)
                rightMat.scale(rp.xScale(), rp.yScale(), rp.zScale());
            rightMat.rotateZ(wingSpread);

            if (crouch) rightMat.translate(0, 3.0F / 16.0F, 0);

            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(rightWing.mesh, block, sky), rightMat, IntList.of(0xFFFFFFFF)));
        }
    }

    protected double scale() {
        return 1f;
    }

    protected double getSwimZOffset(T entity, double swimAmount) {
        return entity.isVisuallySwimming() && swimAmount > 0 ? 0.3f : 0f;
    }

    protected double getSpeedValue(T entity) {
        double speedValue = 1.0F;
        if (entity.isFallFlying()) {
            speedValue = entity.getDeltaMovement().lengthSqr();
            speedValue /= 0.2F;
            speedValue = speedValue * (speedValue * speedValue);
        }
        if (speedValue < 1.0F) speedValue = 1.0F;
        return speedValue;
    }

    protected LimbAngles getLimbAngles(T entity, double animPos, double animSpeed,
                                       double headYaw, double headPitch,
                                       double swim, boolean flying, boolean crouch,
                                       LimbAngles base) {
        bobModelPart(base, entity.tickCount, 0.5f);
        return base;
    }

    protected void bobModelPart(LimbAngles angles, final double ageInTicks, final double scale) {
        var a = (scale * (Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F));
        var b = (scale * (Mth.sin(ageInTicks * 0.067F) * 0.05F));
        angles.leftArmZ += a;
        angles.leftArmAngle += b;
        angles.rightArmZ += -a;
        angles.rightArmAngle += -b;
    }

    protected void applyPassengerPose(LimbAngles angles) {
        angles.rightArmAngle += -Mth.PI / 5;
        angles.leftArmAngle += -Mth.PI / 5;

        angles.rightLegAngle = -1.4137167F;
        angles.rightLegYaw = Mth.PI / 10;
        angles.rightLegZ = 0.07853982F;

        angles.leftLegAngle = -1.4137167F;
        angles.leftLegYaw = -Mth.PI / 10;
        angles.leftLegZ = -0.07853982F;
    }

    protected void renderWithAngles(RenderPipeline pipeline, T entity, Matrix4d base,
                                    LimbAngles angles, double swim, boolean flying, boolean crouch,
                                    double animPos,
                                    double block, double sky) {
        var root = buildRoot(entity);

        ArmPose poseR = getArmPose(entity, HumanoidArm.RIGHT);
        ArmPose poseL = getArmPose(entity, HumanoidArm.LEFT);

        Matrix4d[] handMatRight = new Matrix4d[1];
        Matrix4d[] handMatLeft = new Matrix4d[1];
        Matrix4d[] headMatOut = new Matrix4d[1];

        renderPart(pipeline, root, "root", base,
                angles, poseR, poseL,
                entity.getItemInHand(InteractionHand.MAIN_HAND),
                entity.getItemInHand(InteractionHand.OFF_HAND),
                entity, swim, flying, crouch, animPos,
                handMatRight, handMatLeft, headMatOut,
                block, sky);

        ItemStack mainHand = entity.getMainHandItem();
        if (!mainHand.isEmpty() && handMatRight[0] != null) {
            renderHandItem(pipeline, mainHand, handMatRight[0],
                    entity.getMainArm() == HumanoidArm.RIGHT ? HumanoidArm.RIGHT : HumanoidArm.LEFT,
                    entity,
                    block, sky);
        }

        ItemStack offHand = entity.getOffhandItem();
        if (!offHand.isEmpty() && handMatLeft[0] != null) {
            renderHandItem(pipeline, offHand, handMatLeft[0],
                    entity.getMainArm() == HumanoidArm.RIGHT ? HumanoidArm.LEFT : HumanoidArm.RIGHT,
                    entity,
                    block, sky);
        }

        if (headMatOut[0] != null) {
            ItemStack headItem = entity.getItemBySlot(EquipmentSlot.HEAD);
            if (!headItem.isEmpty()) {
                Matrix4d headMat = new Matrix4d(headMatOut[0]);
                headMat.translate(0.3f, 0.05f, -0.3f);
                headMat.rotateY(Math.toRadians(180));
                headMat.scale(0.625f, -0.625f, -0.625f);
                ItemStackRenderer.render(pipeline, headItem, ItemDisplayContext.HEAD, headMat, block, sky);
            }
        }
    }

    private void renderHandItem(RenderPipeline pipeline, ItemStack stack, Matrix4d armMatrix,
                                HumanoidArm arm, T entity, double block, double sky) {
        Matrix4d mat = new Matrix4d(armMatrix);
        mat.rotateX(-Mth.PI / 2);
        mat.rotateY(Mth.PI);

        double sign = (arm == HumanoidArm.RIGHT) ? 1f : -1f;
        Vector3d extra = getHandItemOffset(entity, arm);

        mat.translate((sign + extra.x) / 16f,
                (2f + extra.y) / 16f,
                (-10f + extra.z) / 16f);

        ItemDisplayContext ctx = arm == HumanoidArm.RIGHT ?
                ItemDisplayContext.THIRD_PERSON_RIGHT_HAND : ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
        ItemStackRenderer.render(pipeline, stack, ctx, mat, block, sky);
    }

    void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part,
                    String name, Matrix4d parent,
                    LimbAngles angles,
                    ArmPose poseR, ArmPose poseL,
                    ItemStack main, ItemStack off,
                    T entity, double swim, boolean fly, boolean crouch,
                    double animPos,
                    Matrix4d[] handMatRight, Matrix4d[] handMatLeft,
                    Matrix4d[] headMatOut,
                    double block, double sky) {
        if (name.equals("hat") || name.equals("jacket") ||
                name.equals("left_sleeve") || name.equals("right_sleeve") ||
                name.equals("left_pants") || name.equals("right_pants")) {
            if (!isOverlayPartVisible(name, entity)) return;
        }

        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        // TODO: slim arm pivot

        transformPart(mat, name, entity, angles, swim, fly, crouch);

        if (crouch) {
            switch (name) {
                case "head" -> mat.translate(0, 4.2f * PIXEL, 0);
                case "body" -> mat.translate(0, 3.2f * PIXEL, 0);
                case "right_arm", "left_arm" -> mat.translate(0, 3.2f * PIXEL, 0);
                case "right_leg", "left_leg" -> mat.translate(0, 0, 4.0f * PIXEL);
            }
        }

        var ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        switch (name) {
            case "head" -> {
                double pitch = angles.headPitch;
                if (fly) pitch = -Mth.PI / 4;
                else if (swim > 0) pitch = Mth.rotLerpRad((float) swim, (float) pitch, -Mth.PI / 4);
                mat.rotateY(angles.headYaw);
                mat.rotateX(pitch);
                headMatOut[0] = new Matrix4d(mat);
            }
            case "body" -> {
                if (crouch) mat.rotateX(0.5f);
            }
            case "right_leg" -> {
                mat.rotateX(angles.rightLegAngle);
                if (angles.rightLegYaw != 0) mat.rotateY(angles.rightLegYaw);
                if (angles.rightLegZ != 0) mat.rotateZ(angles.rightLegZ);
            }
            case "left_leg" -> {
                mat.rotateX(angles.leftLegAngle);
                if (angles.leftLegYaw != 0) mat.rotateY(angles.leftLegYaw);
                if (angles.leftLegZ != 0) mat.rotateZ(angles.leftLegZ);
            }
            case "right_arm" -> {
                applyArm(mat, true, angles.rightArmAngle, angles.rightArmYaw, angles.rightArmZ,
                        swim, fly, crouch, poseR, angles.headYaw, angles.headPitch, main, animPos);
                handMatRight[0] = new Matrix4d(mat);
            }
            case "left_arm" -> {
                applyArm(mat, false, angles.leftArmAngle, angles.leftArmYaw, angles.leftArmZ,
                        swim, fly, crouch, poseL, angles.headYaw, angles.headPitch, off, animPos);
                handMatLeft[0] = new Matrix4d(mat);
            }
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }

        for (var child : part.children.entrySet()) {
            renderPart(pipeline, child.getValue(), child.getKey(), mat,
                    angles, poseR, poseL, main, off, entity,
                    swim, fly, crouch, animPos,
                    handMatRight, handMatLeft, headMatOut,
                    block, sky);
        }
    }

    protected void transformPart(Matrix4d mat, String name, T entity, LimbAngles angles,
                                 double swim, boolean fly, boolean crouch) {

    }

    private void applyArm(Matrix4d mat, boolean isRight,
                          double armAngle, double yaw, double zRot,
                          double swim, boolean fly, boolean crouch,
                          ArmPose pose, double headYaw, double headPitch,
                          ItemStack item, double animPos) {
        if (swim > 0.0F || fly) {
            double swimPos = animPos % 26.0F;
            double swimAmount = swim;
            double baseXRot = armAngle;

            double targetXRot, targetYRot, targetZRot;
            if (swimPos < 14.0F) {
                targetXRot = 0.0F;
                targetYRot = Math.PI;
                double q14 = quadraticArmUpdate(14.0F);
                double qSwim = quadraticArmUpdate(swimPos);
                double zOff = 1.8707964F * qSwim / q14;
                targetZRot = isRight ? Math.PI - zOff : Math.PI + zOff;
            } else if (swimPos < 22.0F) {
                double internal = (swimPos - 14.0F) / 8.0F;
                targetXRot = (Math.PI / 2) * internal;
                targetYRot = Math.PI;
                targetZRot = isRight ? 1.2707963F + 1.8707964F * internal
                        : 5.012389F - 1.8707964F * internal;
            } else {
                double internal = (swimPos - 22.0F) / 4.0F;
                targetXRot = (Math.PI / 2) - (Math.PI / 2) * internal;
                targetYRot = Math.PI;
                targetZRot = Math.PI;
            }

            double finalXRot = Mth.rotLerpRad((float) swimAmount, (float) baseXRot, (float) targetXRot);
            double finalYRot = Mth.rotLerpRad((float) swimAmount, 0.0F, (float) targetYRot);
            double finalZRot = -Mth.lerp(swimAmount, 0.0F, targetZRot);

            mat.rotateX(finalXRot);
            mat.rotateY(finalYRot);
            mat.rotateZ(finalZRot);
        } else {
            if (!doesPoseOverrideWalking(pose)) {
                mat.rotateX(armAngle);
                if (crouch) mat.rotateX(0.4f);
                if (yaw != 0) mat.rotateY(yaw);
                if (zRot != 0) mat.rotateZ(zRot);
            }
            applyPose(mat, isRight, pose, headYaw, headPitch, crouch, item);
        }
    }

    protected boolean doesPoseOverrideWalking(ArmPose pose) {
        return switch (pose) {
            case BOW_AND_ARROW, THROW_TRIDENT, CROSSBOW_CHARGE, CROSSBOW_HOLD, SPEAR -> true;
            default -> false;
        };
    }

    private void applyPose(Matrix4d mat, boolean isRight, ArmPose pose,
                           double headYaw, double headPitch, boolean crouch, ItemStack item) {
        double sign = isRight ? 1f : -1f;
        switch (pose) {
            case EMPTY -> {
            }
            case ITEM -> mat.rotateX(-Mth.PI / 10);
            case BLOCK -> {
                double xRot = -0.9424779f + Mth.clamp(headPitch, -Mth.PI * 4 / 9, 0.43633232f);
                double yRot = sign * -30f * Mth.DEG_TO_RAD + Mth.clamp(headYaw, -Mth.PI / 6, Mth.PI / 6);
                mat.rotateX(xRot);
                mat.rotateY(yRot);
            }
            case BOW_AND_ARROW -> {
                if (isRight) {
                    mat.rotateY(headYaw - 0.1f);
                } else {
                    mat.rotateY(headYaw + 0.1f + 0.4f);
                }
                mat.rotateX(-Mth.PI / 2 + headPitch);
            }
            case THROW_TRIDENT -> mat.rotateX(-Mth.PI);
            case CROSSBOW_HOLD -> {
                mat.rotateX(-1f);
                mat.rotateY(sign * 0.5f);
            }
            case CROSSBOW_CHARGE -> {
                mat.rotateX(-1.3f);
                mat.rotateY(sign * 0.3f);
            }
            case SPYGLASS -> {
                double xRot = Mth.clamp(headPitch - 1.9198622f - (crouch ? Mth.PI / 12 : 0), -2.4f, 3.3f);
                double yRot = headYaw - sign * Mth.PI / 12;
                mat.rotateX(xRot);
                mat.rotateY(yRot);
            }
            case TOOT_HORN -> {
                double xRot = Mth.clamp(headPitch, -1.2f, 1.2f) - 1.4835298f;
                double yRot = headYaw - sign * Mth.PI / 6;
                mat.rotateX(xRot);
                mat.rotateY(yRot);
            }
            case BRUSH -> mat.rotateX(-Mth.PI / 5);
            case SPEAR -> {
                mat.rotateX(-1.2f);
                mat.rotateY(sign * 0.3f);
            }
        }
    }

    public enum ArmPose {
        EMPTY, ITEM, BLOCK, BOW_AND_ARROW, THROW_TRIDENT,
        CROSSBOW_CHARGE, CROSSBOW_HOLD, SPYGLASS, TOOT_HORN, BRUSH, SPEAR
    }

    protected static class LimbAngles {
        double headYaw, headPitch;

        double rightLegAngle, leftLegAngle;
        double rightLegYaw, leftLegYaw;
        double rightLegZ, leftLegZ;

        double rightArmAngle, rightArmYaw, rightArmZ;
        double leftArmAngle, leftArmYaw, leftArmZ;
    }
}
