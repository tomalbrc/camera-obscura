package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public class BreezeRenderer implements LivingEntityRenderer<Breeze> {
    private static final String BASE_TEXTURE = "entity/breeze/breeze";
    private static final String WIND_TEXTURE = "entity/breeze/breeze_wind";
    private static final String EYES_TEXTURE = "entity/breeze/breeze_eyes";

    private ModelBakery.BakedPart cachedBaseModel;
    private ModelBakery.BakedPart cachedWindModel;
    private ModelBakery.BakedPart cachedEyesModel;

    @Override
    public ModelBakery.BakedPart buildRoot(Breeze entity) {
        if (cachedBaseModel == null) {
            cachedBaseModel = buildBaseModel();
        }
        return cachedBaseModel;
    }

    private ModelBakery.BakedPart buildBaseModel() {
        ModelBakery bakery = new ModelBakery(BASE_TEXTURE, 32, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition body = root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(0, 0, 0));

        ModelBakery.PartDefinition rods = body.addOrReplaceChild("rods",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(0, 8, 0));
        rods.addOrReplaceChild("rod_1",
                ModelBakery.CubeListBuilder.create().texOffs(0, 17).addBox(-1, 0, -3, 2, 8, 2),
                ModelBakery.PartPose.offsetAndRotation(2.5981f, -3, 1.5f, -2.7489f, -1.0472f, Mth.PI));
        rods.addOrReplaceChild("rod_2",
                ModelBakery.CubeListBuilder.create().texOffs(0, 17).addBox(-1, 0, -3, 2, 8, 2),
                ModelBakery.PartPose.offsetAndRotation(-2.5981f, -3, 1.5f, -2.7489f, 1.0472f, Mth.PI));
        rods.addOrReplaceChild("rod_3",
                ModelBakery.CubeListBuilder.create().texOffs(0, 17).addBox(-1, 0, -3, 2, 8, 2),
                ModelBakery.PartPose.offsetAndRotation(0, -3, -3, 0.3927f, 0, 0));

        ModelBakery.PartDefinition head = body.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(4, 24).addBox(-5, -5, -4.2f, 10, 3, 4)
                        .texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8),
                ModelBakery.PartPose.offset(0, 4, 0));

        return root.bake();
    }

    private ModelBakery.BakedPart buildWindModel() {
        ModelBakery bakery = new ModelBakery(WIND_TEXTURE, 128, 128);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition windBody = root.addOrReplaceChild("wind_body",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(0, 0, 0));
        ModelBakery.PartDefinition windBottom = windBody.addOrReplaceChild("wind_bottom",
                ModelBakery.CubeListBuilder.create().texOffs(1, 83).addBox(-2.5f, -7, -2.5f, 5, 7, 5),
                ModelBakery.PartPose.offset(0, 24, 0));
        ModelBakery.PartDefinition windMid = windBottom.addOrReplaceChild("wind_mid",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(74, 28).addBox(-6, -6, -6, 12, 6, 12)
                        .texOffs(78, 32).addBox(-4, -6, -4, 8, 6, 8)
                        .texOffs(49, 71).addBox(-2.5f, -6, -2.5f, 5, 6, 5),
                ModelBakery.PartPose.offset(0, -7, 0));
        windMid.addOrReplaceChild("wind_top",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-9, -8, -9, 18, 8, 18)
                        .texOffs(6, 6).addBox(-6, -8, -6, 12, 8, 12)
                        .texOffs(105, 57).addBox(-2.5f, -8, -2.5f, 5, 8, 5),
                ModelBakery.PartPose.offset(0, -6, 0));

        return root.bake();
    }

    private ModelBakery.BakedPart buildEyesModel() {
        ModelBakery bakery = new ModelBakery(EYES_TEXTURE, 32, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(4, 24).addBox(-5, -5, -4.2f, 10, 3, 4)
                        .texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8),
                ModelBakery.PartPose.offset(0, 4, 0));

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, Breeze entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyYaw);
        double headYaw = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYaw);
        double headPitch = Mth.DEG_TO_RAD * entity.getXRot(1.0f);
        double ageInTicks = entity.tickCount + 1.0f;

        double bob = Mth.sin(ageInTicks * 0.1f) * 0.1f;

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + 1.5f + bob, pos.z)
                .rotateY(modelYaw)
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        double windRot = ageInTicks * 0.02f;


        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderBreezePart(pipeline, buildRoot(entity), base, headYaw, headPitch, windRot, block, sky);

        if (cachedWindModel == null) cachedWindModel = buildWindModel();
        renderBreezePart(pipeline, cachedWindModel, base, headYaw, headPitch, windRot, block, sky);

        if (cachedEyesModel == null) cachedEyesModel = buildEyesModel();
        Matrix4d eyesBase = new Matrix4d(base);

        renderEyesPart(pipeline, cachedEyesModel, "root", eyesBase, headYaw, headPitch);
    }

    private void renderBreezePart(RenderPipeline pipeline, ModelBakery.BakedPart part,
                                  Matrix4d parent, double headYaw, double headPitch, double windRot,
                                  double block, double sky) {

        for (var child : part.children.entrySet()) {
            String childName = child.getKey();
            ModelBakery.BakedPart childPart = child.getValue();

            Matrix4d mat = new Matrix4d(parent);
            mat.translate(childPart.localPivot.x, childPart.localPivot.y, childPart.localPivot.z);

            ModelBakery.PartPose ip = childPart.initialPose;
            if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
                mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
            if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
                mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

            if (childName.equals("head")) {
                mat.rotateY(headYaw);
                mat.rotateX(headPitch);
            }

            if (childName.startsWith("wind")) {
                mat.rotateY(windRot);
            }

            if (childPart.mesh != null) {
                pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(childPart.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
            }

            renderBreezePart(pipeline, childPart, mat, headYaw, headPitch, windRot, block, sky);
        }
    }

    private void renderEyesPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                                Matrix4d parent, double headYaw, double headPitch) {
        for (var child : part.children.entrySet()) {
            String childName = child.getKey();
            ModelBakery.BakedPart childPart = child.getValue();

            Matrix4d mat = new Matrix4d(parent);
            mat.translate(childPart.localPivot.x, childPart.localPivot.y, childPart.localPivot.z);

            ModelBakery.PartPose ip = childPart.initialPose;
            if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
                mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
            if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
                mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

            if (childName.equals("head")) {
                mat.rotateY(headYaw);
                mat.rotateX(headPitch);
            }

            if (childPart.mesh != null) {
                pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(childPart.mesh), mat, IntList.of(0xFFFFFFFF)));
            }

            renderEyesPart(pipeline, childPart, childName, mat, headYaw, headPitch);
        }
    }
}