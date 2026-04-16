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
import net.minecraft.world.entity.animal.nautilus.AbstractNautilus;
import net.minecraft.world.entity.animal.nautilus.ZombieNautilus;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

import java.util.HashMap;
import java.util.Map;

public class NautilusRenderer<T extends AbstractNautilus> implements LivingEntityRenderer<T> {
    private static final String NAUTILUS_ADULT = "entity/nautilus/nautilus";
    private static final String NAUTILUS_BABY = "entity/nautilus/nautilus_baby";
    private static final String SADDLE_TEXTURE = "entity/nautilus/nautilus_saddle";
    private static final String ARMOR_TEXTURE = "entity/nautilus/nautilus_armor";

    private final Map<String, ModelBakery.BakedPart> modelCache = new HashMap<>();

    @Override
    public ModelBakery.BakedPart buildRoot(T entity) {
        String texture = getTexture(entity);
        return modelCache.computeIfAbsent(texture, t -> entity.isBaby() ? buildBabyModel(t) : buildAdultModel(t));
    }

    private ModelBakery.BakedPart getSaddleModel() {
        return modelCache.computeIfAbsent(SADDLE_TEXTURE, this::buildSaddleArmorModel);
    }

    private ModelBakery.BakedPart getArmorModel() {
        return modelCache.computeIfAbsent(ARMOR_TEXTURE, this::buildSaddleArmorModel);
    }

    private String getTexture(T entity) {
        if (entity instanceof ZombieNautilus zombie) {
            return zombie.getVariant().value().modelAndTexture().asset().texturePath().toString();
        }
        return entity.isBaby() ? NAUTILUS_BABY : NAUTILUS_ADULT;
    }

    private ModelBakery.BakedPart buildAdultModel(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 128, 128);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition nautilus = root.addOrReplaceChild("root",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(0, 29, -6));

        nautilus.addOrReplaceChild("shell",
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

        return root.bake();
    }

    private ModelBakery.BakedPart buildBabyModel(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 64, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition nautilus = root.addOrReplaceChild("root",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(-0.5f, 28, -0.5f));

        nautilus.addOrReplaceChild("shell",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-6, -4, -1, 7, 4, 7)
                        .texOffs(0, 11).addBox(-6, 0, -1, 7, 4, 9)
                        .texOffs(23, 11).addBox(-6, 0, 5, 7, 4, 0),
                ModelBakery.PartPose.offset(3, -8, -2));

        ModelBakery.PartDefinition body = nautilus.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 24).addBox(-2.5f, -3.01f, -1, 5, 4, 7)
                        .texOffs(0, 35).addBox(-2.5f, -3.01f, 4.1f, 5, 4, 0),
                ModelBakery.PartPose.offset(0.5f, -5, 3));

        body.addOrReplaceChild("upper_mouth",
                ModelBakery.CubeListBuilder.create().texOffs(24, 24).addBox(-2.5f, -1, 0, 5, 2, 2, new ModelBakery.CubeDeformation(-0.001f)),
                ModelBakery.PartPose.offset(0, -2.01f, 3.9f));
        body.addOrReplaceChild("inner_mouth",
                ModelBakery.CubeListBuilder.create().texOffs(24, 32).addBox(-1.5f, -1, -1, 3, 2, 2),
                ModelBakery.PartPose.offset(0, -1.01f, 4.9f));
        body.addOrReplaceChild("lower_mouth",
                ModelBakery.CubeListBuilder.create().texOffs(24, 28).addBox(-2.5f, -1, 0, 5, 2, 2, new ModelBakery.CubeDeformation(-0.001f)),
                ModelBakery.PartPose.offset(0, -0.01f, 3.9f));

        return root.bake();
    }

    private ModelBakery.BakedPart buildSaddleArmorModel(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 128, 128);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition nautilus = root.addOrReplaceChild("root",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(0, 29, -6));

        nautilus.addOrReplaceChild("shell",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-7, -10, -7, 14, 10, 16, new ModelBakery.CubeDeformation(0.2f))
                        .texOffs(0, 26).addBox(0, 0, 0, 0, 0, 0)
                        .texOffs(48, 26).addBox(0, 0, 0, 0, 0, 0),
                ModelBakery.PartPose.offset(0, -13, 5));

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, T entity) {
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
                .translate(pos.x, pos.y + 1.5f + swimBob, pos.z)
                .rotateY(modelYaw)
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderPart(pipeline, buildRoot(entity), "root", base, bodyYRotRad, bodyXRotRad, block, sky);

        if (hasSaddle) {
            renderPart(pipeline, getSaddleModel(), "root", base, bodyYRotRad, bodyXRotRad, block, sky);
        }

        if (hasArmor) {
            renderPart(pipeline, getArmorModel(), "root", base, bodyYRotRad, bodyXRotRad, block, sky);
        }
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