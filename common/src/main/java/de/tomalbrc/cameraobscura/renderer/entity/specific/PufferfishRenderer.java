package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.fish.Pufferfish;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public class PufferfishRenderer implements LivingEntityRenderer<Pufferfish> {
    private static final String TEXTURE = "entity/fish/pufferfish";
    private static final int TEX_WIDTH = 32;
    private static final int TEX_HEIGHT = 32;

    private ModelBakery.BakedPart cachedSmallRoot;
    private ModelBakery.BakedPart cachedMidRoot;
    private ModelBakery.BakedPart cachedBigRoot;

    private ModelBakery.BakedPart buildSmallRoot() {
        ModelBakery bakery = new ModelBakery(TEXTURE, TEX_WIDTH, TEX_HEIGHT);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 27).addBox(-1.5f, -2, -1.5f, 3, 2, 3),
                ModelBakery.PartPose.offset(0, 23, 0));

        root.addOrReplaceChild("right_eye",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(24, 6).addBox(-1.5f, 0, -1.5f, 1, 1, 1),
                ModelBakery.PartPose.offset(0, 20, 0));

        root.addOrReplaceChild("left_eye",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(28, 6).addBox(0.5f, 0, -1.5f, 1, 1, 1),
                ModelBakery.PartPose.offset(0, 20, 0));

        root.addOrReplaceChild("back_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(-3, 0).addBox(-1.5f, 0, 0, 3, 0, 3),
                ModelBakery.PartPose.offset(0, 22, 1.5f));

        root.addOrReplaceChild("right_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(25, 0).addBox(-1, 0, 0, 1, 0, 2),
                ModelBakery.PartPose.offset(-1.5f, 22, -1.5f));

        root.addOrReplaceChild("left_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(25, 0).addBox(0, 0, 0, 1, 0, 2),
                ModelBakery.PartPose.offset(1.5f, 22, -1.5f));

        return root.bake();
    }

    private ModelBakery.BakedPart buildMidRoot() {
        ModelBakery bakery = new ModelBakery(TEXTURE, TEX_WIDTH, TEX_HEIGHT);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(12, 22).addBox(-2.5f, -5, -2.5f, 5, 5, 5),
                ModelBakery.PartPose.offset(0, 22, 0));

        root.addOrReplaceChild("right_blue_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(24, 0).addBox(-2, 0, 0, 2, 0, 2),
                ModelBakery.PartPose.offset(-2.5f, 18, -1.5f));

        root.addOrReplaceChild("left_blue_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(24, 3).addBox(0, 0, 0, 2, 0, 2),
                ModelBakery.PartPose.offset(2.5f, 18, -1.5f));

        root.addOrReplaceChild("top_front_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(19, 17).addBox(-2.5f, -1, 0, 5, 1, 0),
                ModelBakery.PartPose.offsetAndRotation(0, 17, -2.5f, Mth.PI / 4, 0, 0));

        root.addOrReplaceChild("top_back_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(11, 17).addBox(-2.5f, -1, 0, 5, 1, 0),
                ModelBakery.PartPose.offsetAndRotation(0, 17, 2.5f, -Mth.PI / 4, 0, 0));

        root.addOrReplaceChild("right_front_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(5, 17).addBox(-1, -5, 0, 1, 5, 0),
                ModelBakery.PartPose.offsetAndRotation(-2.5f, 22, -2.5f, 0, -Mth.PI / 4, 0));

        root.addOrReplaceChild("right_back_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(9, 17).addBox(-1, -5, 0, 1, 5, 0),
                ModelBakery.PartPose.offsetAndRotation(-2.5f, 22, 2.5f, 0, Mth.PI / 4, 0));

        root.addOrReplaceChild("left_back_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(1, 17).addBox(0, -5, 0, 1, 5, 0),
                ModelBakery.PartPose.offsetAndRotation(2.5f, 22, 2.5f, 0, -Mth.PI / 4, 0));

        root.addOrReplaceChild("left_front_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(1, 17).addBox(0, -5, 0, 1, 5, 0),
                ModelBakery.PartPose.offsetAndRotation(2.5f, 22, -2.5f, 0, Mth.PI / 4, 0));

        root.addOrReplaceChild("bottom_back_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(18, 20).addBox(0, 0, 0, 5, 1, 0),
                ModelBakery.PartPose.offsetAndRotation(-2.5f, 22, 2.5f, Mth.PI / 4, 0, 0));

        root.addOrReplaceChild("bottom_front_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(17, 19).addBox(-2.5f, 0, 0, 5, 1, 1),
                ModelBakery.PartPose.offsetAndRotation(0, 22, -2.5f, -Mth.PI / 4, 0, 0));

        return root.bake();
    }

    private ModelBakery.BakedPart buildBigRoot() {
        ModelBakery bakery = new ModelBakery(TEXTURE, TEX_WIDTH, TEX_HEIGHT);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8),
                ModelBakery.PartPose.offset(0, 22, 0));

        root.addOrReplaceChild("right_blue_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(24, 0).addBox(-2, 0, -1, 2, 1, 2),
                ModelBakery.PartPose.offset(-4, 15, -2));

        root.addOrReplaceChild("left_blue_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(24, 3).addBox(0, 0, -1, 2, 1, 2),
                ModelBakery.PartPose.offset(4, 15, -2));

        root.addOrReplaceChild("top_front_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(15, 17).addBox(-4, -1, 0, 8, 1, 0),
                ModelBakery.PartPose.offsetAndRotation(0, 14, -4, Mth.PI / 4, 0, 0));

        root.addOrReplaceChild("top_middle_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(14, 16).addBox(-4, -1, 0, 8, 1, 1),
                ModelBakery.PartPose.offset(0, 14, 0));

        root.addOrReplaceChild("top_back_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(23, 18).addBox(-4, -1, 0, 8, 1, 0),
                ModelBakery.PartPose.offsetAndRotation(0, 14, 4, -Mth.PI / 4, 0, 0));

        root.addOrReplaceChild("right_front_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(5, 17).addBox(-1, -8, 0, 1, 8, 0),
                ModelBakery.PartPose.offsetAndRotation(-4, 22, -4, 0, -Mth.PI / 4, 0));

        root.addOrReplaceChild("left_front_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(1, 17).addBox(0, -8, 0, 1, 8, 0),
                ModelBakery.PartPose.offsetAndRotation(4, 22, -4, 0, Mth.PI / 4, 0));

        root.addOrReplaceChild("bottom_front_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(15, 20).addBox(-4, 0, 0, 8, 1, 0),
                ModelBakery.PartPose.offsetAndRotation(0, 22, -4, -Mth.PI / 4, 0, 0));

        root.addOrReplaceChild("bottom_middle_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(15, 20).addBox(-4, 0, 0, 8, 1, 0),
                ModelBakery.PartPose.offset(0, 22, 0));

        root.addOrReplaceChild("bottom_back_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(15, 20).addBox(-4, 0, 0, 8, 1, 0),
                ModelBakery.PartPose.offsetAndRotation(0, 22, 4, Mth.PI / 4, 0, 0));

        root.addOrReplaceChild("right_back_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(9, 17).addBox(-1, -8, 0, 1, 8, 0),
                ModelBakery.PartPose.offsetAndRotation(-4, 22, 4, 0, Mth.PI / 4, 0));

        root.addOrReplaceChild("left_back_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(9, 17).addBox(0, -8, 0, 1, 8, 0),
                ModelBakery.PartPose.offsetAndRotation(4, 22, 4, 0, -Mth.PI / 4, 0));

        return root.bake();
    }

    @Override
    public ModelBakery.BakedPart buildRoot(Pufferfish entity) {
        int puffState = entity.getPuffState();
        return switch (puffState) {
            case 0 -> {
                if (cachedSmallRoot == null) cachedSmallRoot = buildSmallRoot();
                yield cachedSmallRoot;
            }
            case 1 -> {
                if (cachedMidRoot == null) cachedMidRoot = buildMidRoot();
                yield cachedMidRoot;
            }
            default -> {
                if (cachedBigRoot == null) cachedBigRoot = buildBigRoot();
                yield cachedBigRoot;
            }
        };
    }

    @Override
    public void render(RenderPipeline pipeline, Pufferfish entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double ageInTicks = entity.tickCount + 1.0f;

        double bob = Mth.cos(ageInTicks * 0.05f) * 0.08f;

        Matrix4d base = new Matrix4d()
                .translate((double) pos.x, (double) pos.y + 1.5f + bob, (double) pos.z)
                .rotateY(Mth.DEG_TO_RAD * (180f - bodyYaw))
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        double rightFinZRot = -0.2f + 0.4f * Mth.sin(ageInTicks * 0.2f);
        double leftFinZRot = 0.2f - 0.4f * Mth.sin(ageInTicks * 0.2f);

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        ModelBakery.BakedPart model = buildRoot(entity);
        renderPart(pipeline, model, "root", base, rightFinZRot, leftFinZRot, block, sky);
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part,
                            String name, Matrix4d parent,
                            double rightFinZRot, double leftFinZRot,
                            double block, double sky) {

        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        var ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        if (name.equals("right_fin") || name.equals("right_blue_fin"))
            mat.rotateZ(rightFinZRot);
        else if (name.equals("left_fin") || name.equals("left_blue_fin"))
            mat.rotateZ(leftFinZRot);

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }

        for (var child : part.children.entrySet()) {
            renderPart(pipeline, child.getValue(), child.getKey(), mat, rightFinZRot, leftFinZRot, block, sky);
        }
    }
}
