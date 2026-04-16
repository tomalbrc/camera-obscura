package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.nautilus.ZombieNautilus;
import net.minecraft.world.entity.animal.nautilus.ZombieNautilusVariant;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

import java.util.HashMap;
import java.util.Map;

public class ZombieNautilusRenderer implements LivingEntityRenderer<ZombieNautilus> {
    private static final String SADDLE_TEXTURE = "entity/nautilus/nautilus_saddle";
    private static final String ARMOR_TEXTURE = "entity/nautilus/nautilus_armor";

    private final Map<ZombieNautilusVariant, ModelBakery.BakedPart> baseCache = new HashMap<>();
    private ModelBakery.BakedPart cachedSaddleModel;
    private ModelBakery.BakedPart cachedArmorModel;

    @Override
    public ModelBakery.BakedPart buildRoot(ZombieNautilus entity) {
        ZombieNautilusVariant variant = entity.getVariant().value();
        return baseCache.computeIfAbsent(variant, this::buildBaseModel);
    }

    private ModelBakery.BakedPart buildBaseModel(ZombieNautilusVariant variant) {
        String texture = variant.modelAndTexture().asset().id().toString();
        boolean warm = variant.modelAndTexture().model() == ZombieNautilusVariant.ModelType.WARM;

        ModelBakery bakery = new ModelBakery(texture, 128, 128);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition nautilus = root.addOrReplaceChild("root",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(0, 29, -6));

        ModelBakery.PartDefinition shell = nautilus.addOrReplaceChild("shell",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-7, -10, -7, 14, 10, 16)
                        .texOffs(0, 26).addBox(-7, 0, -7, 14, 8, 20)
                        .texOffs(48, 26).addBox(-7, 0, 6, 14, 8, 0),
                ModelBakery.PartPose.offset(0, -13, 5));

        ModelBakery.PartDefinition body = nautilus.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 54).addBox(-5, -4.51f, -3, 10, 8, 14)
                        .texOffs(0, 76).addBox(-5, -4.51f, 7, 10, 8, 0),
                ModelBakery.PartPose.offset(0, -8.5f, 12.3f));

        body.addOrReplaceChild("upper_mouth",
                ModelBakery.CubeListBuilder.create().texOffs(54, 54).addBox(-5, -2, 0, 10, 4, 4, new ModelBakery.CubeDeformation(-0.001f)),
                ModelBakery.PartPose.offset(0, -2.51f, 7));
        body.addOrReplaceChild("inner_mouth",
                ModelBakery.CubeListBuilder.create().texOffs(54, 70).addBox(-3, -2, -0.5f, 6, 4, 4),
                ModelBakery.PartPose.offset(0, -0.51f, 7.5f));
        body.addOrReplaceChild("lower_mouth",
                ModelBakery.CubeListBuilder.create().texOffs(54, 62).addBox(-5, -1.98f, 0, 10, 4, 4, new ModelBakery.CubeDeformation(-0.001f)),
                ModelBakery.PartPose.offset(0, 1.49f, 7));

        if (warm) {
            addCorals(shell);
        }

        return root.bake();
    }

    private void addCorals(ModelBakery.PartDefinition shell) {
        ModelBakery.PartDefinition corals = shell.addOrReplaceChild("corals",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(8, 4.5f, -8));

        ModelBakery.PartDefinition yellow = corals.addOrReplaceChild("yellow_coral",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(0, -11, 11));
        yellow.addOrReplaceChild("yellow_coral_first",
                ModelBakery.CubeListBuilder.create().texOffs(0, 85).addBox(-4.5f, -3.5f, 0, 6, 8, 0),
                ModelBakery.PartPose.offsetAndRotation(0, 0, 0, 0, 0.7854f, 0));
        yellow.addOrReplaceChild("yellow_coral_second",
                ModelBakery.CubeListBuilder.create().texOffs(0, 85).addBox(-4.5f, -3.5f, 0, 6, 8, 0),
                ModelBakery.PartPose.offsetAndRotation(0, 0, 2, 0, -0.7854f, 0));

        ModelBakery.PartDefinition pink = corals.addOrReplaceChild("pink_coral",
                ModelBakery.CubeListBuilder.create().texOffs(-8, 94).addBox(-4.5f, 4.5f, 0, 6, 0, 8),
                ModelBakery.PartPose.offset(-12.5f, -18, 11));
        pink.addOrReplaceChild("pink_coral_second",
                ModelBakery.CubeListBuilder.create().texOffs(-8, 94).addBox(-3, 0, -4, 6, 0, 8),
                ModelBakery.PartPose.offsetAndRotation(-1.5f, 4.5f, 4, 0, 0, 1.5708f));

        ModelBakery.PartDefinition blue = corals.addOrReplaceChild("blue_coral",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(-14, 0, 5.5f));
        blue.addOrReplaceChild("blue_first",
                ModelBakery.CubeListBuilder.create().texOffs(0, 102).addBox(-3.5f, -5.5f, 0, 5, 10, 0),
                ModelBakery.PartPose.offsetAndRotation(0, 0, 0, 0, -0.7854f, 0));
        blue.addOrReplaceChild("blue_second",
                ModelBakery.CubeListBuilder.create().texOffs(0, 102).addBox(-3.5f, -5.5f, 0, 5, 10, 0),
                ModelBakery.PartPose.offsetAndRotation(0, 0, -2, 0, 0.7854f, 0));

        ModelBakery.PartDefinition red = corals.addOrReplaceChild("red_coral",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(0, 0, 0));
        red.addOrReplaceChild("red_coral_first",
                ModelBakery.CubeListBuilder.create().texOffs(0, 112).addBox(-4.5f, -5.5f, 0, 6, 10, 0),
                ModelBakery.PartPose.offsetAndRotation(0, 0, 0, 0, 0.7854f, 0));
        red.addOrReplaceChild("red_coral_second",
                ModelBakery.CubeListBuilder.create().texOffs(0, 112).addBox(-2.5f, -5.5f, 0, 4, 10, 0),
                ModelBakery.PartPose.offsetAndRotation(-0.5f, -1, 1.5f, 0, -0.829f, 0));
    }

    private ModelBakery.BakedPart getSaddleModel() {
        if (cachedSaddleModel == null) {
            cachedSaddleModel = buildOverlayModel(SADDLE_TEXTURE, 0.2f);
        }
        return cachedSaddleModel;
    }

    private ModelBakery.BakedPart getArmorModel() {
        if (cachedArmorModel == null) {
            cachedArmorModel = buildOverlayModel(ARMOR_TEXTURE, 0.01f);
        }
        return cachedArmorModel;
    }

    private ModelBakery.BakedPart buildOverlayModel(String texture, float deformation) {
        ModelBakery bakery = new ModelBakery(texture, 128, 128);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("root",
                        ModelBakery.CubeListBuilder.create(),
                        ModelBakery.PartPose.offset(0, 29, -6))
                .addOrReplaceChild("shell",
                        ModelBakery.CubeListBuilder.create()
                                .texOffs(0, 0).addBox(-7, -10, -7, 14, 10, 16, new ModelBakery.CubeDeformation(deformation)),
                        ModelBakery.PartPose.offset(0, -13, 5));

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, ZombieNautilus entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyYaw);
        double yRot = entity.getYHeadRot() - bodyYaw;
        double xRot = entity.getXRot(1.0f);

        yRot = Mth.clamp(yRot, -10, 10);
        xRot = Mth.clamp(xRot, -10, 10);
        double bodyYRotRad = yRot * Mth.DEG_TO_RAD;
        double bodyXRotRad = xRot * Mth.DEG_TO_RAD;

        double age = entity.tickCount + 1.0f;
        double swimBob = Mth.cos(age * 0.2f) * 0.1f;

        boolean hasSaddle = !entity.getItemBySlot(EquipmentSlot.SADDLE).isEmpty();
        boolean hasArmor = !entity.getBodyArmorItem().isEmpty();

        Matrix4d base = new Matrix4d()
                .translate((double) pos.x, (double) pos.y + 1.5f + swimBob, (double) pos.z)
                .rotateY(modelYaw)
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        ModelBakery.BakedPart baseModel = buildRoot(entity);
        renderPart(pipeline, baseModel, "root", base, bodyYRotRad, bodyXRotRad, block, sky);

        if (hasSaddle)
            renderPart(pipeline, getSaddleModel(), "root", base, bodyYRotRad, bodyXRotRad, block, sky);
        if (hasArmor)
            renderPart(pipeline, getArmorModel(), "root", base, bodyYRotRad, bodyXRotRad, block, sky);
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                            Matrix4d parent, double bodyYRot, double bodyXRot,
                            double block, double sky) {

        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        if (name.equals("body")) {
            mat.rotateY(bodyYRot);
            mat.rotateX(bodyXRot);
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }

        for (var child : part.children.entrySet()) {
            renderPart(pipeline, child.getValue(), child.getKey(), mat, bodyYRot, bodyXRot, block, sky);
        }
    }
}