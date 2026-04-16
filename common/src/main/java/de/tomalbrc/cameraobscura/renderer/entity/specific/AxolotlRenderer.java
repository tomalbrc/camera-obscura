package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

import java.util.HashMap;
import java.util.Map;

public class AxolotlRenderer implements LivingEntityRenderer<Axolotl> {

    private static final String TEXTURE_PREFIX = "entity/axolotl/axolotl_";
    private static final Map<Axolotl.Variant, ModelBakery.BakedPart> CACHE = new HashMap<>();

    private static String getTexture(Axolotl.Variant variant, boolean baby) {
        String suffix = baby ? "_baby" : "";
        return TEXTURE_PREFIX + variant.getName() + suffix;
    }

    @Override
    public ModelBakery.BakedPart buildRoot(Axolotl entity) {
        return CACHE.computeIfAbsent(entity.getVariant(), v -> buildModel(getTexture(v, entity.isBaby())));
    }

    private ModelBakery.BakedPart buildModel(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 64, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition body = root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 11).addBox(-4, -2, -9, 8, 4, 10)
                        .texOffs(2, 17).addBox(0, -3, -8, 0, 5, 9),
                ModelBakery.PartPose.offset(0, 19.5f, 5));

        ModelBakery.CubeDeformation fudge = new ModelBakery.CubeDeformation(0.001f);
        ModelBakery.PartDefinition head = body.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create().texOffs(0, 1).addBox(-4, -3, -5, 8, 5, 5, fudge),
                ModelBakery.PartPose.offset(0, 0, -9));
        head.addOrReplaceChild("top_gills",
                ModelBakery.CubeListBuilder.create().texOffs(3, 37).addBox(-4, -3, 0, 8, 3, 0, fudge),
                ModelBakery.PartPose.offset(0, -3, -1));
        head.addOrReplaceChild("left_gills",
                ModelBakery.CubeListBuilder.create().texOffs(0, 40).addBox(-3, -5, 0, 3, 7, 0, fudge),
                ModelBakery.PartPose.offset(-4, 0, -1));
        head.addOrReplaceChild("right_gills",
                ModelBakery.CubeListBuilder.create().texOffs(11, 40).addBox(0, -5, 0, 3, 7, 0, fudge),
                ModelBakery.PartPose.offset(4, 0, -1));

        ModelBakery.CubeListBuilder leftLegBuilder = ModelBakery.CubeListBuilder.create()
                .texOffs(2, 13).addBox(-1, 0, 0, 3, 5, 0, fudge);
        ModelBakery.CubeListBuilder rightLegBuilder = ModelBakery.CubeListBuilder.create()
                .texOffs(2, 13).addBox(-2, 0, 0, 3, 5, 0, fudge);
        body.addOrReplaceChild("right_hind_leg", rightLegBuilder, ModelBakery.PartPose.offset(-3.5f, 1, -1));
        body.addOrReplaceChild("left_hind_leg", leftLegBuilder, ModelBakery.PartPose.offset(3.5f, 1, -1));
        body.addOrReplaceChild("right_front_leg", rightLegBuilder, ModelBakery.PartPose.offset(-3.5f, 1, -8));
        body.addOrReplaceChild("left_front_leg", leftLegBuilder, ModelBakery.PartPose.offset(3.5f, 1, -8));

        body.addOrReplaceChild("tail",
                ModelBakery.CubeListBuilder.create().texOffs(2, 19).addBox(0, -3, 0, 0, 5, 12),
                ModelBakery.PartPose.offset(0, 0, 1));

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, Axolotl entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double headYaw = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYaw);
        double headPitch = Mth.DEG_TO_RAD * entity.getXRot(1.0f);
        boolean inWater = entity.isInWater();
        boolean onGround = entity.onGround();
        boolean moving = entity.getDeltaMovement().horizontalDistanceSqr() > 1.0E-7;
        boolean baby = entity.isBaby();
        double age = entity.tickCount + 1.0f;

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + 1.5f, pos.z)
                .rotateY(Mth.DEG_TO_RAD * (180f - bodyYaw))
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        if (baby) base = new Matrix4d(base).scale(0.5f);

        boolean swimming = inWater && !onGround;
        boolean crawling = onGround && moving;

        double bodyWave = 0;
        double legAngle = 0;
        double tailWave = 0;
        double gillFlap = 0.3f;
        double bodyXAngle = 0;

        if (swimming) {
            bodyWave = Mth.sin(age * 0.33f) * 0.13f;
            bodyXAngle = headPitch + bodyWave;
            legAngle = 1.8849558f;  // PI*3/5
            tailWave = 0.3f * Mth.cos(age * 0.3f);
            gillFlap = -0.5f * Mth.sin(age * 0.33f) - 0.8f;
        } else if (crawling) {
            double walkPos = age * 0.11f;
            legAngle = 0.9424779f;
            bodyWave = 0.09f * Mth.cos(walkPos);
            tailWave = bodyWave;
            gillFlap = 0.5f;
        } else {
            gillFlap = 0.6f;
        }

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        AnimParams params = new AnimParams(headYaw, bodyXAngle, bodyWave, legAngle, tailWave, gillFlap, swimming);
        renderPart(pipeline, buildRoot(entity), "root", base, params, block, sky);
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                            Matrix4d parent, AnimParams p,
                            double block, double sky) {

        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        switch (name) {
            case "body":
                mat.rotateX(p.bodyXRot);
                if (p.swimming) mat.translate(0, -0.45f * Mth.cos(p.bodyWave) / 16f, 0);
                break;
            case "head":
                mat.rotateY(p.headYaw);
                if (p.swimming) mat.rotateX(-p.bodyWave * 1.8f);
                break;
            case "tail":
                mat.rotateY(p.tailWave);
                break;
            case "right_hind_leg", "left_hind_leg", "right_front_leg", "left_front_leg":
                boolean right = name.contains("right");
                boolean hind = name.contains("hind");
                double angle = p.swimming ? p.legAngle : (hind ? p.legAngle : 1.0995574f);
                if (!p.swimming) {
                    double walk = -(right ? -1 : 1) * Mth.sin(p.bodyWave) * 0.5f;
                    if (hind) angle = 0.9424779f + walk;
                    else angle = 1.0995574f + walk;
                }
                mat.rotateX(angle);
                if (p.swimming) {
                    mat.rotateZ(right ? -Mth.PI / 2 : Mth.PI / 2);
                }
                break;
            case "top_gills":
                mat.rotateX(p.gillFlap);
                break;
            case "left_gills":
                mat.rotateY(-p.gillFlap);
                break;
            case "right_gills":
                mat.rotateY(p.gillFlap);
                break;
            default:
                break;
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }
        for (var child : part.children.entrySet()) {
            renderPart(pipeline, child.getValue(), child.getKey(), mat, p, block, sky);
        }
    }

    private static class AnimParams {
        final double headYaw, bodyXRot, bodyWave, legAngle, tailWave, gillFlap;
        final boolean swimming;

        AnimParams(double hy, double bx, double bw, double la, double tw, double gf, boolean sw) {
            headYaw = hy;
            bodyXRot = bx;
            bodyWave = bw;
            legAngle = la;
            tailWave = tw;
            gillFlap = gf;
            swimming = sw;
        }
    }
}
