package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public class CreeperRenderer implements LivingEntityRenderer<Creeper> {
    private ModelBakery.BakedPart cachedRoot;
    private ModelBakery.BakedPart cachedArmorModel;

    @Override
    public ModelBakery.BakedPart buildRoot(Creeper entity) {
        if (cachedRoot == null) {
            ModelBakery bakery = new ModelBakery("entity/creeper/creeper", 64, 32);
            ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
            ModelBakery.PartDefinition root = model.root();

            root.addOrReplaceChild("head",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8),
                    ModelBakery.PartPose.offset(0, 6, 0));

            root.addOrReplaceChild("body",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(16, 16).addBox(-4, 0, -2, 8, 12, 4),
                    ModelBakery.PartPose.offset(0, 6, 0));

            ModelBakery.CubeListBuilder legBuilder = ModelBakery.CubeListBuilder.create()
                    .texOffs(0, 16).addBox(-2, 0, -2, 4, 6, 4);

            root.addOrReplaceChild("right_hind_leg", legBuilder,
                    ModelBakery.PartPose.offset(-2, 18, 4));
            root.addOrReplaceChild("left_hind_leg", legBuilder,
                    ModelBakery.PartPose.offset(2, 18, 4));
            root.addOrReplaceChild("right_front_leg", legBuilder,
                    ModelBakery.PartPose.offset(-2, 18, -4));
            root.addOrReplaceChild("left_front_leg", legBuilder,
                    ModelBakery.PartPose.offset(2, 18, -4));

            cachedRoot = root.bake();
        }
        return cachedRoot;
    }

    private ModelBakery.BakedPart getArmorModel() {
        if (cachedArmorModel == null) {
            ModelBakery bakery = new ModelBakery("entity/creeper/creeper_armor", 64, 32);
            ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
            ModelBakery.PartDefinition root = model.root();

            root.addOrReplaceChild("head",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8),
                    ModelBakery.PartPose.offset(0, 6, 0));
            root.addOrReplaceChild("body",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(16, 16).addBox(-4, 0, -2, 8, 12, 4),
                    ModelBakery.PartPose.offset(0, 6, 0));
            ModelBakery.CubeListBuilder legBuilder = ModelBakery.CubeListBuilder.create()
                    .texOffs(0, 16).addBox(-2, 0, -2, 4, 6, 4);
            root.addOrReplaceChild("right_hind_leg", legBuilder,
                    ModelBakery.PartPose.offset(-2, 18, 4));
            root.addOrReplaceChild("left_hind_leg", legBuilder,
                    ModelBakery.PartPose.offset(2, 18, 4));
            root.addOrReplaceChild("right_front_leg", legBuilder,
                    ModelBakery.PartPose.offset(-2, 18, -4));
            root.addOrReplaceChild("left_front_leg", legBuilder,
                    ModelBakery.PartPose.offset(2, 18, -4));

            cachedArmorModel = root.bake();
        }
        return cachedArmorModel;
    }

    @Override
    public void render(RenderPipeline pipeline, Creeper entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double headYawRel = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYaw);
        double headPitch = Mth.DEG_TO_RAD * entity.getXRot(1.0f);

        double swelling = entity.getSwelling(1.0f);
        double wobble = 1.0f + Mth.sin(swelling * 100.0f) * swelling * 0.01f;
        swelling = Mth.clamp(swelling, 0.0f, 1.0f);
        swelling *= swelling;
        swelling *= swelling;
        double scaleXZ = (1.0f + swelling * 0.4f) * wobble;
        double scaleY = (1.0f + swelling * 0.1f) / wobble;

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + 1.5f, pos.z)
                .rotateY(Mth.DEG_TO_RAD * (180f - bodyYaw))
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        base.scale(scaleXZ, scaleY, scaleXZ);

        double animPos = entity.walkAnimation.position();
        double animSpeed = entity.walkAnimation.speed();
        double limbSwing = animPos * 0.6662f;
        double legAngle = Mth.cos(limbSwing) * 1.4f * animSpeed;
        double legAngleAlt = Mth.cos(limbSwing + Mth.PI) * 1.4f * animSpeed;

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderPart(pipeline, buildRoot(entity), "root", base, legAngle, legAngleAlt, legAngleAlt, legAngle, headYawRel, headPitch, block, sky);

        if (entity.isPowered()) {
            renderPart(pipeline, getArmorModel(), "root", base.scale(1.25f), legAngle, legAngleAlt, legAngleAlt, legAngle, headYawRel, headPitch, 1, 1);
        }
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part,
                            String name, Matrix4d parent,
                            double rhX, double lhX, double rfX, double lfX,
                            double headYaw, double headPitch,
                            double block, double sky) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        if (name.equals("head")) {
            mat.rotateY(headYaw);
            mat.rotateX(headPitch);
        }

        switch (name) {
            case "right_hind_leg" -> mat.rotateX(rhX);
            case "left_hind_leg" -> mat.rotateX(lhX);
            case "right_front_leg" -> mat.rotateX(rfX);
            case "left_front_leg" -> mat.rotateX(lfX);
        }

        var ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }

        for (var child : part.children.entrySet()) {
            renderPart(pipeline, child.getValue(), child.getKey(), mat, rhX, lhX, rfX, lfX, headYaw, headPitch, block, sky);
        }
    }
}
