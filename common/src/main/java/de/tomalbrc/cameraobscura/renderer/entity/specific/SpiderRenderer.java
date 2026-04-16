package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

import java.util.HashMap;
import java.util.Map;

public class SpiderRenderer implements LivingEntityRenderer<Spider> {
    private static final String SPIDER_TEXTURE = "entity/spider/spider";
    private static final String CAVE_SPIDER_TEXTURE = "entity/spider/cave_spider";

    private ModelBakery.BakedPart cachedSpiderRoot;
    private ModelBakery.BakedPart cachedCaveSpiderRoot;

    @Override
    public ModelBakery.BakedPart buildRoot(Spider entity) {
        String texture = entity instanceof CaveSpider ? CAVE_SPIDER_TEXTURE : SPIDER_TEXTURE;
        if (entity instanceof CaveSpider) {
            if (cachedCaveSpiderRoot == null) {
                cachedCaveSpiderRoot = buildParts(texture);
            }
            return cachedCaveSpiderRoot;
        } else {
            if (cachedSpiderRoot == null) {
                cachedSpiderRoot = buildParts(texture);
            }
            return cachedSpiderRoot;
        }
    }

    private ModelBakery.BakedPart buildParts(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 64, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(32, 4).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F),
                ModelBakery.PartPose.offset(0.0F, 15.0F, -3.0F));

        root.addOrReplaceChild("body0",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F),
                ModelBakery.PartPose.offset(0.0F, 15.0F, 0.0F));

        root.addOrReplaceChild("body1",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 12).addBox(-5.0F, -4.0F, -6.0F, 10.0F, 8.0F, 12.0F),
                ModelBakery.PartPose.offset(0.0F, 15.0F, 9.0F));

        ModelBakery.CubeListBuilder rightLeg = ModelBakery.CubeListBuilder.create()
                .texOffs(18, 0).addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F);
        ModelBakery.CubeListBuilder leftLeg = ModelBakery.CubeListBuilder.create()
                .texOffs(18, 0).mirror().addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F);

        root.addOrReplaceChild("right_hind_leg", rightLeg,
                ModelBakery.PartPose.offsetAndRotation(-4.0F, 15.0F, 2.0F, 0.0F, (Mth.PI / 4), (-Mth.PI / 4)));
        root.addOrReplaceChild("left_hind_leg", leftLeg,
                ModelBakery.PartPose.offsetAndRotation(4.0F, 15.0F, 2.0F, 0.0F, (-Mth.PI / 4), (Mth.PI / 4)));

        root.addOrReplaceChild("right_middle_hind_leg", rightLeg,
                ModelBakery.PartPose.offsetAndRotation(-4.0F, 15.0F, 1.0F, 0.0F, (Mth.PI / 8), -0.58119464F));
        root.addOrReplaceChild("left_middle_hind_leg", leftLeg,
                ModelBakery.PartPose.offsetAndRotation(4.0F, 15.0F, 1.0F, 0.0F, (-Mth.PI / 8), 0.58119464F));

        root.addOrReplaceChild("right_middle_front_leg", rightLeg,
                ModelBakery.PartPose.offsetAndRotation(-4.0F, 15.0F, 0.0F, 0.0F, (-Mth.PI / 8), -0.58119464F));
        root.addOrReplaceChild("left_middle_front_leg", leftLeg,
                ModelBakery.PartPose.offsetAndRotation(4.0F, 15.0F, 0.0F, 0.0F, (Mth.PI / 8), 0.58119464F));

        root.addOrReplaceChild("right_front_leg", rightLeg,
                ModelBakery.PartPose.offsetAndRotation(-4.0F, 15.0F, -1.0F, 0.0F, (-Mth.PI / 4), (-Mth.PI / 4)));
        root.addOrReplaceChild("left_front_leg", leftLeg,
                ModelBakery.PartPose.offsetAndRotation(4.0F, 15.0F, -1.0F, 0.0F, (Mth.PI / 4), (Mth.PI / 4)));

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, Spider entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyYaw);

        double headYawRel = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYaw);
        double headPitch = Mth.DEG_TO_RAD * entity.getXRot(1.0f);

        var cave = entity instanceof CaveSpider ? 0.7f : 1f;
        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + cave * 1.5f, pos.z)
                .rotateY(modelYaw)
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        if (cave != 1) {
            base = new Matrix4d(base).scale(cave);
        }

        double animPos = entity.walkAnimation.position();
        double animSpeed = entity.walkAnimation.speed();

        double animationPos = animPos * 0.6662F;

        double swingHind = -(Mth.cos(animationPos * 2.0F + 0.0F) * 0.4F) * animSpeed;
        double swingMiddleHind = -(Mth.cos(animationPos * 2.0F + Math.PI) * 0.4F) * animSpeed;
        double swingMiddleFront = -(Mth.cos(animationPos * 2.0F + (Math.PI / 2)) * 0.4F) * animSpeed;
        double swingFront = -(Mth.cos(animationPos * 2.0F + (Math.PI * 3.0 / 2.0)) * 0.4F) * animSpeed;

        double stepHind = Math.abs(Mth.sin(animationPos + 0.0F) * 0.4F) * animSpeed;
        double stepMiddleHind = Math.abs(Mth.sin(animationPos + Math.PI) * 0.4F) * animSpeed;
        double stepMiddleFront = Math.abs(Mth.sin(animationPos + (Math.PI / 2)) * 0.4F) * animSpeed;
        double stepFront = Math.abs(Mth.sin(animationPos + (Math.PI * 3.0 / 2.0)) * 0.4F) * animSpeed;

        LegRotations legRots = new LegRotations();
        legRots.put("right_hind_leg", swingHind, -stepHind);
        legRots.put("left_hind_leg", -swingHind, stepHind);
        legRots.put("right_middle_hind_leg", swingMiddleHind, -stepMiddleHind);
        legRots.put("left_middle_hind_leg", -swingMiddleHind, stepMiddleHind);
        legRots.put("right_middle_front_leg", swingMiddleFront, -stepMiddleFront);
        legRots.put("left_middle_front_leg", -swingMiddleFront, stepMiddleFront);
        legRots.put("right_front_leg", swingFront, -stepFront);
        legRots.put("left_front_leg", -swingFront, stepFront);

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderPart(pipeline, buildRoot(entity), "root", base, headYawRel, headPitch, legRots, block, sky);
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part,
                            String name, Matrix4d parent,
                            double headYaw, double headPitch,
                            LegRotations legRots,
                            double block, double sky) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        var ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        if (name.equals("head")) {
            mat.rotateY(headYaw);
            mat.rotateX(headPitch);
        }

        LegRotations.Entry entry = legRots.get(name);
        if (entry != null) {
            mat.rotateY(entry.yRot);
            mat.rotateZ(entry.zRot);
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }

        for (var child : part.children.entrySet()) {
            renderPart(pipeline, child.getValue(), child.getKey(), mat, headYaw, headPitch, legRots, block, sky);
        }
    }

    private static class LegRotations {
        private final Map<String, Entry> map = new HashMap<>();

        void put(String name, double yRot, double zRot) {
            map.put(name, new Entry(yRot, zRot));
        }

        Entry get(String name) {
            return map.get(name);
        }

        record Entry(double yRot, double zRot) {
        }
    }
}