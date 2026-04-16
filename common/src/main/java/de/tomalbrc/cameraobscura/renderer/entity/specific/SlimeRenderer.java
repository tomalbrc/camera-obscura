package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public class SlimeRenderer implements LivingEntityRenderer<Slime> {

    private static final String TEXTURE = "entity/slime/slime";
    private ModelBakery.BakedPart cachedOuterModel;
    private ModelBakery.BakedPart cachedInnerModel;

    @Override
    public ModelBakery.BakedPart buildRoot(Slime entity) {
        return null;
    }

    private ModelBakery.BakedPart getOuterModel() {
        if (cachedOuterModel == null) {
            cachedOuterModel = buildOuterModel();
        }
        return cachedOuterModel;
    }

    private ModelBakery.BakedPart getInnerModel() {
        if (cachedInnerModel == null) {
            cachedInnerModel = buildInnerModel();
        }
        return cachedInnerModel;
    }

    private ModelBakery.BakedPart buildOuterModel() {
        ModelBakery bakery = new ModelBakery(TEXTURE, 64, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();
        root.addOrReplaceChild("cube",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, 16.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                ModelBakery.PartPose.ZERO);
        return root.bake();
    }

    private ModelBakery.BakedPart buildInnerModel() {
        ModelBakery bakery = new ModelBakery(TEXTURE, 64, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();
        root.addOrReplaceChild("cube",
                ModelBakery.CubeListBuilder.create().texOffs(0, 16).addBox(-3.0F, 17.0F, -3.0F, 6.0F, 6.0F, 6.0F),
                ModelBakery.PartPose.ZERO);
        root.addOrReplaceChild("right_eye",
                ModelBakery.CubeListBuilder.create().texOffs(32, 0).addBox(-3.25F, 18.0F, -3.5F, 2.0F, 2.0F, 2.0F),
                ModelBakery.PartPose.ZERO);
        root.addOrReplaceChild("left_eye",
                ModelBakery.CubeListBuilder.create().texOffs(32, 4).addBox(1.25F, 18.0F, -3.5F, 2.0F, 2.0F, 2.0F),
                ModelBakery.PartPose.ZERO);
        root.addOrReplaceChild("mouth",
                ModelBakery.CubeListBuilder.create().texOffs(32, 8).addBox(0.0F, 21.0F, -3.5F, 1.0F, 1.0F, 1.0F),
                ModelBakery.PartPose.ZERO);
        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, Slime entity) {
        double squish = Mth.lerp(1.0f, entity.oSquish, entity.squish);
        int size = entity.getSize();

        double ss = squish / (size * 0.5f + 1.0f);
        double w = 1.0f / (ss + 1.0f);
        double scaleXZ = w * size;
        double scaleY = (1.0f / w) * size;

        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y, pos.z)
                .scale(scaleXZ, scaleY, scaleXZ)
                .rotateY(Mth.DEG_TO_RAD * (180f - bodyYaw))
                .translate(0, 1.501f, 0)
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);


        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        int tint = LivingEntityRenderer.hurtTint(entity);
        renderPart(pipeline, getOuterModel(), base, block, sky, tint);
        renderPart(pipeline, getInnerModel(), base, block, sky, tint);
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part, Matrix4d parent, double block, double sky, int tint) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(tint)));
        }
        for (var child : part.children.entrySet()) {
            renderPart(pipeline, child.getValue(), mat, block, sky, tint);
        }
    }
}