package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public class TurtleRenderer implements LivingEntityRenderer<Turtle> {

    private static final String ADULT_TEXTURE = "entity/turtle/turtle";
    private static final String BABY_TEXTURE = "entity/turtle/turtle_baby";

    private ModelBakery.BakedPart cachedAdultRoot;
    private ModelBakery.BakedPart cachedBabyRoot;

    private ModelBakery.BakedPart buildAdultRoot() {
        ModelBakery bakery = new ModelBakery(ADULT_TEXTURE, 128, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(3, 0).addBox(-3, -1, -3, 6, 5, 6),
                ModelBakery.PartPose.offset(0, 19, -10));

        ModelBakery.PartDefinition body = root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offsetAndRotation(0, 11, -10, Mth.PI / 2, 0, 0));
        body.addOrReplaceChild("shell",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(7, 37).addBox(-9.5f, 3, -10, 19, 20, 6), ModelBakery.PartPose.ZERO);
        body.addOrReplaceChild("belly",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(31, 1).addBox(-5.5f, 3, -13, 11, 18, 3), ModelBakery.PartPose.ZERO);

        root.addOrReplaceChild("egg_belly",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(70, 33).addBox(-4.5f, 3, -14, 9, 18, 1),
                ModelBakery.PartPose.offsetAndRotation(0, 11, -10, Mth.PI / 2, 0, 0));

        root.addOrReplaceChild("right_hind_leg",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(1, 23).addBox(-2, 0, 0, 4, 1, 10),
                ModelBakery.PartPose.offset(-3.5f, 22, 11));
        root.addOrReplaceChild("left_hind_leg",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(1, 12).addBox(-2, 0, 0, 4, 1, 10),
                ModelBakery.PartPose.offset(3.5f, 22, 11));
        root.addOrReplaceChild("right_front_leg",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(27, 30).addBox(-13, 0, -2, 13, 1, 5),
                ModelBakery.PartPose.offset(-5, 21, -4));
        root.addOrReplaceChild("left_front_leg",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(27, 24).addBox(0, 0, -2, 13, 1, 5),
                ModelBakery.PartPose.offset(5, 21, -4));

        return root.bake();
    }

    private ModelBakery.BakedPart buildBabyRoot() {
        ModelBakery bakery = new ModelBakery(BABY_TEXTURE, 16, 16);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2, -1, -2, 4, 2, 4),
                ModelBakery.PartPose.offset(0, 22.9f, 1));

        root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 6).addBox(-1.5f, -2, -3, 3, 3, 3),
                ModelBakery.PartPose.offset(0, 22.9f, -1));

        root.addOrReplaceChild("right_hind_leg",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(-1, 0).addBox(-2, 0, -0.5f, 2, 0, 1),
                ModelBakery.PartPose.offset(-2, 23.9f, 2.5f));
        root.addOrReplaceChild("left_hind_leg",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(-1, 1).addBox(0, 0, -0.5f, 2, 0, 1),
                ModelBakery.PartPose.offset(2, 23.9f, 2.5f));
        root.addOrReplaceChild("right_front_leg",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(8, 6).addBox(-2, 0, -0.5f, 2, 0, 1),
                ModelBakery.PartPose.offset(-2, 23.9f, -0.5f));
        root.addOrReplaceChild("left_front_leg",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(8, 7).addBox(0, 0, -0.5f, 2, 0, 1),
                ModelBakery.PartPose.offset(2, 23.9f, -0.5f));

        return root.bake();
    }

    @Override
    public ModelBakery.BakedPart buildRoot(Turtle entity) {
        if (entity.isBaby()) {
            if (cachedBabyRoot == null) cachedBabyRoot = buildBabyRoot();
            return cachedBabyRoot;
        } else {
            if (cachedAdultRoot == null) cachedAdultRoot = buildAdultRoot();
            return cachedAdultRoot;
        }
    }

    @Override
    public void render(RenderPipeline pipeline, Turtle entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyYaw);

        double headYawRel = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYaw);
        double headPitch = Mth.DEG_TO_RAD * entity.getXRot(1.0f);

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + 1.5f, pos.z)
                .rotateY(Mth.PI + modelYaw)
                .rotateX(Mth.PI);

        if (!entity.isBaby() && entity.hasEgg()) {
            base.translate(0, -1f / 16f, 0);
        }

        double animPos = entity.walkAnimation.position();
        double animSpeed = entity.walkAnimation.speed();
        boolean onLand = !entity.isInWater() && entity.onGround();
        boolean layingEgg = entity.isLayingEgg();

        double rfY = 0, lfY = 0, rhY = 0, lhY = 0;
        double rhX = 0, lhX = 0, rfZ = 0, lfZ = 0;

        if (onLand) {
            double layEggScale = layingEgg ? 4.0f : 1.0f;
            double layEggAmp = layingEgg ? 2.0f : 1.0f;
            double swingPos = animPos * 5.0f;
            double frontSwing = Mth.cos(layEggScale * swingPos);
            double hindSwing = Mth.cos(swingPos);
            rfY = -frontSwing * 8.0f * animSpeed * layEggAmp;
            lfY = frontSwing * 8.0f * animSpeed * layEggAmp;
            rhY = -hindSwing * 3.0f * animSpeed;
            lhY = hindSwing * 3.0f * animSpeed;
        } else {
            double swingScale = 0.5f * animSpeed;
            double swing = Mth.cos(animPos * 0.6662f * 0.6f) * swingScale;
            rhX = swing;
            lhX = -swing;
            rfZ = -swing;
            lfZ = swing;
        }

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderPart(pipeline, buildRoot(entity), "root", base,
                rfY, lfY, rhY, lhY, rhX, lhX, rfZ, lfZ,
                entity, headYawRel, headPitch,
                block, sky);
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part,
                            String name, Matrix4d parent,
                            double rfY, double lfY, double rhY, double lhY,
                            double rhX, double lhX, double rfZ, double lfZ,
                            Turtle entity,
                            double headYaw, double headPitch,
                            double block, double sky) {

        if (name.equals("egg_belly") && !entity.hasEgg()) return;

        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        switch (name) {
            case "head" -> {
                mat.rotateY(headYaw);
                mat.rotateX(headPitch);
            }
            case "right_front_leg" -> {
                if (rfY != 0) mat.rotateY(rfY);
                if (rfZ != 0) mat.rotateZ(rfZ);
            }
            case "left_front_leg" -> {
                if (lfY != 0) mat.rotateY(lfY);
                if (lfZ != 0) mat.rotateZ(lfZ);
            }
            case "right_hind_leg" -> {
                if (rhY != 0) mat.rotateY(rhY);
                if (rhX != 0) mat.rotateX(rhX);
            }
            case "left_hind_leg" -> {
                if (lhY != 0) mat.rotateY(lhY);
                if (lhX != 0) mat.rotateX(lhX);
            }
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
            renderPart(pipeline, child.getValue(), child.getKey(), mat,
                    rfY, lfY, rhY, lhY, rhX, lhX, rfZ, lfZ,
                    entity, headYaw, headPitch, block, sky);
        }
    }
}