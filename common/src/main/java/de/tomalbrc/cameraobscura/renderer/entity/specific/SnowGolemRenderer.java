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
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import org.joml.Matrix4d;

public class SnowGolemRenderer implements LivingEntityRenderer<SnowGolem> {
    private static final String TEXTURE = "entity/snow_golem/snow_golem";
    private ModelBakery.BakedPart cachedModel;

    @Override
    public ModelBakery.BakedPart buildRoot(SnowGolem entity) {
        if (cachedModel == null) {
            cachedModel = buildModel();
        }
        return cachedModel;
    }

    private ModelBakery.BakedPart buildModel() {
        ModelBakery bakery = new ModelBakery(TEXTURE, 64, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.CubeDeformation def = new ModelBakery.CubeDeformation(-0.5f);
        root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8, def),
                ModelBakery.PartPose.offset(0, 4, 0));
        ModelBakery.CubeListBuilder arm = ModelBakery.CubeListBuilder.create()
                .texOffs(32, 0).addBox(-1, 0, -1, 12, 2, 2, def);
        root.addOrReplaceChild("left_arm", arm,
                ModelBakery.PartPose.offsetAndRotation(5, 6, 1, 0, 0, 1.0f));
        root.addOrReplaceChild("right_arm", arm,
                ModelBakery.PartPose.offsetAndRotation(-5, 6, -1, 0, (float) Math.PI, -1.0f));
        root.addOrReplaceChild("upper_body",
                ModelBakery.CubeListBuilder.create().texOffs(0, 16).addBox(-5, -10, -5, 10, 10, 10, def),
                ModelBakery.PartPose.offset(0, 13, 0));
        root.addOrReplaceChild("lower_body",
                ModelBakery.CubeListBuilder.create().texOffs(0, 36).addBox(-6, -12, -6, 12, 12, 12, def),
                ModelBakery.PartPose.offset(0, 24, 0));

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, SnowGolem entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyYaw);
        double headYaw = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYaw);
        double headPitch = Mth.DEG_TO_RAD * entity.getXRot(1.0f);

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + 1.5f, pos.z)
                .rotateY(modelYaw)
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        double upperYaw = headYaw * 0.25f;
        double cos = Mth.cos(upperYaw);
        double sin = Mth.sin(upperYaw);

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderGolemPart(pipeline, buildRoot(entity), "root", base, headYaw, headPitch, upperYaw, cos, sin, block, sky);

        if (entity.hasPumpkin()) {
            Matrix4d headMat = computePartMatrix(base, buildRoot(entity), headYaw, headPitch, upperYaw, cos, sin);

            Matrix4d pumpkinMat = new Matrix4d(headMat);
            pumpkinMat.rotateY(Mth.PI);
            pumpkinMat.scale(0.625f, -0.625f, -0.625f);
            pumpkinMat.translate(0, -0.333f, 0);
            BlockStateRenderer.render(pipeline, Blocks.CARVED_PUMPKIN.defaultBlockState(), pumpkinMat, block, sky);
        }
    }

    private Matrix4d computePartMatrix(Matrix4d base, ModelBakery.BakedPart root,
                                       double headYaw, double headPitch, double upperYaw, double cos, double sin) {
        return computePartMatrixRecursive(base, root, "root", "head", headYaw, headPitch, upperYaw, cos, sin);
    }

    private Matrix4d computePartMatrixRecursive(Matrix4d parent, ModelBakery.BakedPart part, String name,
                                                String target,
                                                double headYaw, double headPitch, double upperYaw, double cos, double sin) {
        Matrix4d mat = new Matrix4d(parent);
        if (name.equals(target)) {
            return mat;
        }

        applyPartTransforms(mat, part, name, headYaw, headPitch, upperYaw, cos, sin);
        for (var child : part.children.entrySet()) {
            Matrix4d childMat = computePartMatrixRecursive(mat, child.getValue(), child.getKey(), target,
                    headYaw, headPitch, upperYaw, cos, sin);
            if (childMat != null) return childMat;
        }
        return null;
    }

    private void applyPartTransforms(Matrix4d mat, ModelBakery.BakedPart part, String name, double headYaw, double headPitch, double upperYaw, double cos, double sin) {
        if (name.equals("left_arm")) {
            mat.translate(cos * 5f / 16f, part.localPivot.y, -sin * 5f / 16f);
        } else if (name.equals("right_arm")) {
            mat.translate(-cos * 5f / 16f, part.localPivot.y, sin * 5f / 16f);
        } else {
            mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);
        }

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
            case "upper_body", "left_arm" -> mat.rotateY(upperYaw);
            case "right_arm" -> mat.rotateY(upperYaw + Mth.PI);
        }
    }

    private void renderGolemPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                                 Matrix4d parent,
                                 double headYaw, double headPitch, double upperYaw, double cos, double sin,
                                 double block, double sky) {

        Matrix4d mat = new Matrix4d(parent);
        applyPartTransforms(mat, part, name, headYaw, headPitch, upperYaw, cos, sin);

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }

        for (var child : part.children.entrySet()) {
            renderGolemPart(pipeline, child.getValue(), child.getKey(), mat,
                    headYaw, headPitch, upperYaw, cos, sin, block, sky);
        }
    }
}