package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.FourLeggedRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

import java.awt.*;

public class SheepRenderer extends FourLeggedRenderer<Sheep> {

    private ModelBakery.BakedPart cachedAdultBody;
    private ModelBakery.BakedPart cachedBabyBody;
    private ModelBakery.BakedPart cachedFur;

    @Override
    public ModelBakery.BakedPart buildRoot(Sheep entity) {
        if (entity.isBaby()) {
            if (cachedBabyBody == null) {
                cachedBabyBody = buildBabyBody();
            }
            return cachedBabyBody;
        } else {
            if (cachedAdultBody == null) {
                cachedAdultBody = buildAdultBody();
            }
            return cachedAdultBody;
        }
    }

    private ModelBakery.BakedPart getFurRoot() {
        if (cachedFur == null) {
            cachedFur = buildFur();
        }
        return cachedFur;
    }

    private ModelBakery.BakedPart buildAdultBody() {
        ModelBakery bakery = new ModelBakery("entity/sheep/sheep", 64, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0F, -4.0F, -6.0F, 6.0F, 6.0F, 8.0F),
                ModelBakery.PartPose.offset(0.0F, 6.0F, -8.0F));

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(28, 8).addBox(-4.0F, -10.0F, -7.0F, 8.0F, 16.0F, 6.0F),
                ModelBakery.PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, (Mth.PI / 2), 0.0F, 0.0F));

        ModelBakery.CubeListBuilder leg = ModelBakery.CubeListBuilder.create()
                .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F);
        root.addOrReplaceChild("right_hind_leg", leg, ModelBakery.PartPose.offset(-3.0F, 12.0F, 7.0F));
        root.addOrReplaceChild("left_hind_leg", leg, ModelBakery.PartPose.offset(3.0F, 12.0F, 7.0F));
        root.addOrReplaceChild("right_front_leg", leg, ModelBakery.PartPose.offset(-3.0F, 12.0F, -5.0F));
        root.addOrReplaceChild("left_front_leg", leg, ModelBakery.PartPose.offset(3.0F, 12.0F, -5.0F));

        return root.bake();
    }

    private ModelBakery.BakedPart buildBabyBody() {
        ModelBakery bakery = new ModelBakery("entity/sheep/sheep_baby", 64, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.5F, -4.5F, -3.5F, 5.0F, 5.0F, 5.0F),
                ModelBakery.PartPose.offset(0.0F, 15.5F, -2.5F));

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 10).addBox(-3.0F, -2.0F, -4.5F, 6.0F, 4.0F, 9.0F),
                ModelBakery.PartPose.offset(0.0F, 17.0F, 0.5F));

        root.addOrReplaceChild("right_hind_leg",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 23).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F),
                ModelBakery.PartPose.offset(-2.0F, 19.0F, 3.0F));
        root.addOrReplaceChild("left_hind_leg",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(24, 12).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F),
                ModelBakery.PartPose.offset(2.0F, 19.0F, 3.0F));
        root.addOrReplaceChild("right_front_leg",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(8, 23).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F),
                ModelBakery.PartPose.offset(-2.0F, 19.0F, -2.0F));
        root.addOrReplaceChild("left_front_leg",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(24, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F),
                ModelBakery.PartPose.offset(2.0F, 19.0F, -2.0F));

        return root.bake();
    }

    private ModelBakery.BakedPart buildFur() {
        ModelBakery bakery = new ModelBakery("entity/sheep/sheep_wool", 64, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0F, -4.0F, -4.0F, 6.0F, 6.0F, 6.0F, new ModelBakery.CubeDeformation(0.6F)),
                ModelBakery.PartPose.offset(0.0F, 6.0F, -8.0F));

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(28, 8).addBox(-4.0F, -10.0F, -7.0F, 8.0F, 16.0F, 6.0F, new ModelBakery.CubeDeformation(1.75F)),
                ModelBakery.PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, Mth.PI / 2, 0.0F, 0.0F));

        ModelBakery.CubeListBuilder leg = ModelBakery.CubeListBuilder.create()
                .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new ModelBakery.CubeDeformation(0.5F));
        root.addOrReplaceChild("right_hind_leg", leg, ModelBakery.PartPose.offset(-3.0F, 12.0F, 7.0F));
        root.addOrReplaceChild("left_hind_leg", leg, ModelBakery.PartPose.offset(3.0F, 12.0F, 7.0F));
        root.addOrReplaceChild("right_front_leg", leg, ModelBakery.PartPose.offset(-3.0F, 12.0F, -5.0F));
        root.addOrReplaceChild("left_front_leg", leg, ModelBakery.PartPose.offset(3.0F, 12.0F, -5.0F));

        return root.bake();
    }

    private static int applyHurtTint(int baseColor, int hurtColor) {
        if (hurtColor == 0xFFFFFFFF) return baseColor;
        int br = (baseColor >> 16) & 0xFF;
        int bg = (baseColor >> 8) & 0xFF;
        int bb = baseColor & 0xFF;
        int hr = (hurtColor >> 16) & 0xFF;
        int hg = (hurtColor >> 8) & 0xFF;
        int hb = hurtColor & 0xFF;
        int r = (br * hr) / 255;
        int g = (bg * hg) / 255;
        int b = (bb * hb) / 255;
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static int getWoolColor(Sheep sheep) {
        if (sheep.hasCustomName() && "jeb_".equals(sheep.getName().getString())) {
            float hue = ((sheep.tickCount / 500.0f) + sheep.getId()) % 1.0f;
            int rgb = Color.HSBtoRGB(hue, 0.8f, 0.8f);
            return 0xFF000000 | rgb;
        }
        int wool = sheep.getColor().getTextureDiffuseColor();
        return 0xFF000000 | wool;
    }

    @Override
    public void render(RenderPipeline pipeline, Sheep entity) {
        var pos = entity.position();
        double bodyYawDeg = entity.getPreciseBodyRotation(1.0f);
        double modelYawRad = Mth.DEG_TO_RAD * (180f - bodyYawDeg);

        double headYawRelDeg = entity.getYHeadRot() - bodyYawDeg;
        double headYawRad = Mth.DEG_TO_RAD * headYawRelDeg;
        double headPitchRad = Mth.DEG_TO_RAD * entity.getXRot(1.0f);
        double ageScale = entity.isBaby() ? 0.5f : 1.0f;
        double headExtraPitch = entity.getHeadEatAngleScale(1.0f);
        double headEatPos = entity.getHeadEatPositionScale(1.0f);
        double headExtraY = headEatPos * 9.0f * ageScale;

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y, pos.z)
                .rotateY(modelYawRad);
        base.translate(0.0f, 1.5f, 0.0f);
        base.rotateY(Mth.PI);
        base.rotateX(Mth.PI);

        double limbSwing = entity.walkAnimation.position() * 0.6662f;
        double limbSpeed = entity.walkAnimation.speed();
        double limbAngle = Mth.cos(limbSwing) * 1.4f * limbSpeed;

        float block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        float sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        int hurtColor = LivingEntityRenderer.hurtTint(entity);
        int woolColor = getWoolColor(entity);

        int bodyColor = applyHurtTint(0xFFFFFFFF, hurtColor);
        int furColor = applyHurtTint(woolColor, hurtColor);

        renderPart(pipeline, buildRoot(entity), "root", base, limbAngle, headYawRad, headPitchRad + headExtraPitch, headExtraY, bodyColor, block, sky);

        if (!entity.isBaby() && !entity.isSheared()) {
            renderPart(pipeline, getFurRoot(), "root", base, limbAngle, headYawRad, headPitchRad + headExtraPitch, headExtraY, furColor, block, sky);
        }
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                            Matrix4d parent, double limbAngle,
                            double headYawRad, double headPitchRad, double headExtraY,
                            int color, double block, double sky) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        var ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0) {
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        }
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1) {
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());
        }

        switch (name) {
            case "head" -> {
                if (headExtraY != 0) {
                    mat.translate(0.0F, headExtraY / 16.0F, 0.0F);
                }
                mat.rotateY(headYawRad);
                mat.rotateX(headPitchRad);
            }
            case "right_hind_leg", "left_front_leg" -> mat.rotateX(limbAngle);
            case "left_hind_leg", "right_front_leg" -> mat.rotateX(-limbAngle);
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(color)));
        }

        for (var entry : part.children.entrySet()) {
            renderPart(pipeline, entry.getValue(), entry.getKey(), mat,
                    limbAngle, headYawRad, headPitchRad, headExtraY, color, block, sky);
        }
    }
}