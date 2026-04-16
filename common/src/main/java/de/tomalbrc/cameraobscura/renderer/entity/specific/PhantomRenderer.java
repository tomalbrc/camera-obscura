package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public class PhantomRenderer implements LivingEntityRenderer<Phantom> {
    private static final String TEXTURE = "entity/phantom/phantom";
    private static final String EYES_TEXTURE = "entity/phantom/phantom_eyes";

    private ModelBakery.BakedPart cachedModel;
    private ModelBakery.BakedPart cachedEyesModel;

    @Override
    public ModelBakery.BakedPart buildRoot(Phantom entity) {
        if (cachedModel == null) {
            cachedModel = buildModel(TEXTURE);
        }
        return cachedModel;
    }

    private ModelBakery.BakedPart getEyesModel() {
        if (cachedEyesModel == null) {
            cachedEyesModel = buildModel(EYES_TEXTURE);
        }
        return cachedEyesModel;
    }

    private ModelBakery.BakedPart buildModel(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 64, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition body = root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(0, 8).addBox(-3, -2, -8, 5, 3, 9),
                ModelBakery.PartPose.rotation(-0.1f, 0, 0));

        body.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-4, -2, -5, 7, 3, 5),
                ModelBakery.PartPose.offsetAndRotation(0, 1, -7, 0.2f, 0, 0));

        ModelBakery.PartDefinition tailBase = body.addOrReplaceChild("tail_base",
                ModelBakery.CubeListBuilder.create().texOffs(3, 20).addBox(-2, 0, 0, 3, 2, 6),
                ModelBakery.PartPose.offset(0, -2, 1));
        tailBase.addOrReplaceChild("tail_tip",
                ModelBakery.CubeListBuilder.create().texOffs(4, 29).addBox(-1, 0, 0, 1, 1, 6),
                ModelBakery.PartPose.offset(0, 0.5f, 6));

        ModelBakery.PartDefinition leftWingBase = body.addOrReplaceChild("left_wing_base",
                ModelBakery.CubeListBuilder.create().texOffs(23, 12).addBox(0, 0, 0, 6, 2, 9),
                ModelBakery.PartPose.offsetAndRotation(2, -2, -8, 0, 0, 0.1f));
        leftWingBase.addOrReplaceChild("left_wing_tip",
                ModelBakery.CubeListBuilder.create().texOffs(16, 24).addBox(0, 0, 0, 13, 1, 9),
                ModelBakery.PartPose.offsetAndRotation(6, 0, 0, 0, 0, 0.1f));

        ModelBakery.PartDefinition rightWingBase = body.addOrReplaceChild("right_wing_base",
                ModelBakery.CubeListBuilder.create().texOffs(23, 12).mirror().addBox(-6, 0, 0, 6, 2, 9),
                ModelBakery.PartPose.offsetAndRotation(-3, -2, -8, 0, 0, -0.1f));
        rightWingBase.addOrReplaceChild("right_wing_tip",
                ModelBakery.CubeListBuilder.create().texOffs(16, 24).mirror().addBox(-13, 0, 0, 13, 1, 9),
                ModelBakery.PartPose.offsetAndRotation(-6, 0, 0, 0, 0, -0.1f));

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, Phantom entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyYaw);
        double headPitch = Mth.DEG_TO_RAD * entity.getXRot(1.0f);
        int size = entity.getPhantomSize();
        double flapTime = entity.getUniqueFlapTickOffset() + entity.tickCount + 1.0f;

        double anim = flapTime * 7.448451f * Mth.DEG_TO_RAD;
        double leftWingZRot = Mth.cos(anim) * 16.0f * Mth.DEG_TO_RAD;
        double rightWingZRot = -leftWingZRot;
        double tailXRot = -(5.0f + Mth.cos(anim * 2.0f) * 5.0f) * Mth.DEG_TO_RAD;

        double scale = 1.0f + 0.15f * size;

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + 1.5f, pos.z)
                .rotateY(modelYaw)
                .rotateX(headPitch)
                .rotateY(Mth.PI)
                .rotateX(Mth.PI)
                .scale(scale)
                .translate(0, 1.3125f, 0.1875f);

        AnimData data = new AnimData(leftWingZRot, rightWingZRot, tailXRot);

        renderPhantomPart(pipeline, buildRoot(entity), "root", base, data, 0xFFFFFFFF, block, sky);
        renderPhantomPart(pipeline, getEyesModel(), "root", base, data, 0xFFFFFFFF, 1, 1);
    }

    private void renderPhantomPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                                   Matrix4d parent, AnimData data, int color, double block, double sky) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        switch (name) {
            case "left_wing_base":
            case "left_wing_tip":
                mat.rotateZ(data.leftWingZRot);
                break;
            case "right_wing_base":
            case "right_wing_tip":
                mat.rotateZ(data.rightWingZRot);
                break;
            case "tail_base":
            case "tail_tip":
                mat.rotateX(data.tailXRot);
                break;
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(color)));
        }

        for (var child : part.children.entrySet()) {
            renderPhantomPart(pipeline, child.getValue(), child.getKey(), mat, data, color, block, sky);
        }
    }

    private static class AnimData {
        final double leftWingZRot, rightWingZRot, tailXRot;

        AnimData(double leftWingZRot, double rightWingZRot, double tailXRot) {
            this.leftWingZRot = leftWingZRot;
            this.rightWingZRot = rightWingZRot;
            this.tailXRot = tailXRot;
        }
    }
}
