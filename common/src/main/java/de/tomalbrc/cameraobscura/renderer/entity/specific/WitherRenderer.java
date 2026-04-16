package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import org.joml.Matrix4d;

public class WitherRenderer implements LivingEntityRenderer<WitherBoss> {

    private static final String NORMAL_TEXTURE = "entity/wither/wither";
    private static final String INVULNERABLE_TEXTURE = "entity/wither/wither_invulnerable";
    private static final String ARMOR_TEXTURE = "entity/wither/wither_armor";

    private ModelBakery.BakedPart cachedNormalModel;
    private ModelBakery.BakedPart cachedInvulnerableModel;
    private ModelBakery.BakedPart cachedArmorModel;

    @Override
    public ModelBakery.BakedPart buildRoot(WitherBoss entity) {
        if (cachedNormalModel == null) cachedNormalModel = buildModel(NORMAL_TEXTURE);
        return cachedNormalModel;
    }

    private ModelBakery.BakedPart getInvulnerableModel() {
        if (cachedInvulnerableModel == null) cachedInvulnerableModel = buildModel(INVULNERABLE_TEXTURE);
        return cachedInvulnerableModel;
    }

    private ModelBakery.BakedPart getArmorModel() {
        if (cachedArmorModel == null) cachedArmorModel = buildModel(ARMOR_TEXTURE);
        return cachedArmorModel;
    }

    private ModelBakery.BakedPart buildModel(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 64, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition rootPart = model.root();

        rootPart.addOrReplaceChild("shoulders",
                ModelBakery.CubeListBuilder.create().texOffs(0, 16).addBox(-10, 3.9f, -0.5f, 20, 3, 3),
                ModelBakery.PartPose.ZERO);

        rootPart.addOrReplaceChild("ribcage",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 22).addBox(0, 0, 0, 3, 10, 3)
                        .texOffs(24, 22).addBox(-4, 1.5f, 0.5f, 11, 2, 2)
                        .texOffs(24, 22).addBox(-4, 4, 0.5f, 11, 2, 2)
                        .texOffs(24, 22).addBox(-4, 6.5f, 0.5f, 11, 2, 2),
                ModelBakery.PartPose.offsetAndRotation(-2, 6.9f, -0.5f, 0.20420352f, 0, 0));

        float staticRx = 0.20420352f;
        float tailY = 6.9f + Mth.cos(staticRx) * 10.0f;
        float tailZ = -0.5f + Mth.sin(staticRx) * 10.0f;
        rootPart.addOrReplaceChild("tail",
                ModelBakery.CubeListBuilder.create().texOffs(12, 22).addBox(0, 0, 0, 3, 6, 3),
                ModelBakery.PartPose.offsetAndRotation(-2, tailY, tailZ, 0.83252203f, 0, 0));

        rootPart.addOrReplaceChild("center_head",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-4, -4, -4, 8, 8, 8),
                ModelBakery.PartPose.ZERO);
        ModelBakery.CubeListBuilder sideHead = ModelBakery.CubeListBuilder.create()
                .texOffs(32, 0).addBox(-4, -4, -4, 6, 6, 6);
        rootPart.addOrReplaceChild("right_head", sideHead, ModelBakery.PartPose.offset(-8, 4, 0));
        rootPart.addOrReplaceChild("left_head", sideHead, ModelBakery.PartPose.offset(10, 4, 0));

        return rootPart.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, WitherBoss entity) {
        float partialTicks = 1.0f;
        int invulTicksEntity = entity.getInvulnerableTicks();
        double invulTicks = invulTicksEntity > 0 ? invulTicksEntity - partialTicks : 0.0f;
        double scale = 2.0f - Math.min(invulTicks / 220.0f, 1.0f) * 0.5f;
        double bodyRot = entity.getYRot(partialTicks);
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyRot);
        double age = entity.tickCount + partialTicks;
        float[] headXRots = entity.getHeadXRots();
        float[] headYRots = entity.getHeadYRots();

        boolean useInvul = invulTicks > 0 && (invulTicks > 80 || ((int) invulTicks / 5 % 2 != 1));
        ModelBakery.BakedPart mainModel = useInvul ? getInvulnerableModel() : buildRoot(entity);

        Matrix4d base = new Matrix4d()
                .translate((double) entity.getX(), (double) entity.getY() + 1.5f * scale, (double) entity.getZ())
                .scale(scale)
                .rotateY(modelYaw)
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        double anim = Mth.cos(age * 0.1f);
        double ribcageXRot = (0.065f + 0.05f * anim) * (double) Math.PI;
        double tailXRot = (0.265f + 0.1f * anim) * (double) Math.PI;
        double tailY = 6.9f + Mth.cos(ribcageXRot) * 10.0f;
        double tailZ = -0.5f + Mth.sin(ribcageXRot) * 10.0f;

        double centerPitch = Mth.DEG_TO_RAD * entity.getXRot(partialTicks);

        AnimData data = new AnimData(ribcageXRot, tailXRot, tailY, tailZ,
                centerPitch, headXRots, headYRots, bodyRot);

        renderPart(pipeline, mainModel, "root", base, data, 0xFFFFFFFF);

        if (entity.isPowered()) {
            renderPart(pipeline, getArmorModel(), "root", base.scale(1.25f), data, 0xFFFFFFFF);
        }
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                            Matrix4d parent, AnimData data, int color) {
        Matrix4d mat = new Matrix4d(parent);

        switch (name) {
            case "ribcage":
                mat.translate(-2f / 16f, 6.9f / 16f, -0.5f / 16f);
                mat.rotateX(data.ribcageXRot);
                break;
            case "tail":
                mat.translate(-2f / 16f, data.tailY / 16f, data.tailZ / 16f);
                mat.rotateX(data.tailXRot);
                break;
            case "center_head":
                mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z); // (0,0,0)

                mat.rotateX(data.centerPitch);
                break;
            case "right_head":
                mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z); // (-8,4,0)
                mat.rotateY(Mth.DEG_TO_RAD * (data.headYRots[0] - data.bodyRot));
                mat.rotateX(Mth.DEG_TO_RAD * data.headXRots[0]);
                break;
            case "left_head":
                mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z); // (10,4,0)
                mat.rotateY(Mth.DEG_TO_RAD * (data.headYRots[1] - data.bodyRot));
                mat.rotateX(Mth.DEG_TO_RAD * data.headXRots[1]);
                break;
            default:
                mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);
                break;
        }

        if (!name.equals("ribcage") && !name.equals("tail")) {
            ModelBakery.PartPose ip = part.initialPose;
            if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
                mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
            if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
                mat.scale(ip.xScale(), ip.yScale(), ip.zScale());
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh), mat, IntList.of(color)));
        }

        for (var child : part.children.entrySet()) {
            renderPart(pipeline, child.getValue(), child.getKey(), mat, data, color);
        }
    }

    private static class AnimData {
        final double ribcageXRot, tailXRot, tailY, tailZ;
        final double centerPitch;
        final float[] headXRots, headYRots;
        final double bodyRot;

        AnimData(double rx, double tx, double ty, double tz,
                 double cp, float[] hx, float[] hy, double br) {
            ribcageXRot = rx;
            tailXRot = tx;
            tailY = ty;
            tailZ = tz;
            centerPitch = cp;
            headXRots = hx;
            headYRots = hy;
            bodyRot = br;
        }
    }
}