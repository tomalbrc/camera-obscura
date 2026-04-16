package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractEquineRenderer<T extends AbstractHorse> implements LivingEntityRenderer<T> {

    private final Map<String, ModelBakery.BakedPart> modelCache = new HashMap<>();
    private final Map<String, ModelBakery.BakedPart> saddleCache = new HashMap<>();
    private final Map<String, ModelBakery.BakedPart> armorCache = new HashMap<>();

    protected abstract String getTexture(T entity);

    protected String getSaddleTexture(T entity) {
        return HumanoidRenderer.resolveEquipmentTexture(entity.getItemBySlot(EquipmentSlot.SADDLE), "horse_saddle");
    }

    protected String getArmorTexture(T entity) {
        return HumanoidRenderer.resolveEquipmentTexture(entity.getBodyArmorItem(), "horse_body");
    }

    protected boolean hasSaddle(T entity) {
        String tex = getSaddleTexture(entity);
        return tex != null;
    }

    protected boolean hasArmor(T entity) {
        String tex = getArmorTexture(entity);
        return tex != null;
    }

    protected ModelBakery.BakedPart buildAdultBase(String texture) {
        return buildAdultBasePartDefinition(texture).bake();
    }

    protected ModelBakery.PartDefinition buildAdultBasePartDefinition(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 64, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        var body = root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 32).addBox(-5.0F, -8.0F, -17.0F, 10.0F, 10.0F, 22.0F),
                ModelBakery.PartPose.offset(0.0F, 11.0F, 5.0F));

        ModelBakery.PartDefinition headParts = root.addOrReplaceChild("head_parts",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 35).addBox(-2.05F, -6.0F, -2.0F, 4.0F, 12.0F, 7.0F),
                ModelBakery.PartPose.offsetAndRotation(0.0F, 4.0F, -12.0F, Mth.PI / 6, 0.0F, 0.0F));

        headParts.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 13).addBox(-3.0F, -11.0F, -2.0F, 6.0F, 5.0F, 7.0F),
                ModelBakery.PartPose.ZERO);

        headParts.addOrReplaceChild("mane",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(56, 36).addBox(-1.0F, -11.0F, 5.01F, 2.0F, 16.0F, 2.0F),
                ModelBakery.PartPose.ZERO);

        headParts.addOrReplaceChild("upper_mouth",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 25).addBox(-2.0F, -11.0F, -7.0F, 4.0F, 5.0F, 5.0F),
                ModelBakery.PartPose.ZERO);

        ModelBakery.PartDefinition head = headParts.getChildren().get("head");
        head.addOrReplaceChild("left_ear",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(19, 16).addBox(0.55F, -13.0F, 4.0F, 2.0F, 3.0F, 1.0F),
                ModelBakery.PartPose.ZERO);
        head.addOrReplaceChild("right_ear",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(19, 16).addBox(-2.55F, -13.0F, 4.0F, 2.0F, 3.0F, 1.0F),
                ModelBakery.PartPose.ZERO);

        ModelBakery.CubeListBuilder legLeft = ModelBakery.CubeListBuilder.create()
                .texOffs(48, 21).mirror().addBox(-3.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F);
        ModelBakery.CubeListBuilder legRight = ModelBakery.CubeListBuilder.create()
                .texOffs(48, 21).addBox(-1.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F);

        root.addOrReplaceChild("left_hind_leg", legLeft, ModelBakery.PartPose.offset(4.0F, 14.0F, 7.0F));
        root.addOrReplaceChild("right_hind_leg", legRight, ModelBakery.PartPose.offset(-4.0F, 14.0F, 7.0F));
        root.addOrReplaceChild("left_front_leg", legLeft, ModelBakery.PartPose.offset(4.0F, 14.0F, -10.0F));
        root.addOrReplaceChild("right_front_leg", legRight, ModelBakery.PartPose.offset(-4.0F, 14.0F, -10.0F));

        body.addOrReplaceChild("tail",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(42, 36).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 14.0F, 4.0F),
                ModelBakery.PartPose.offsetAndRotation(0.0F, -5.0F, 2.0F, Mth.PI / 6, 0.0F, 0.0F));

        return root;
    }

    protected ModelBakery.BakedPart buildBabyBase(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 64, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 13).addBox(-4.0F, -3.5F, -7.0F, 8.0F, 7.0F, 14.0F),
                ModelBakery.PartPose.offset(0.0F, 12.5F, 0.0F)).addOrReplaceChild("tail",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(24, 34).addBox(-1.5F, -1.5F, -1.0F, 3.0F, 3.0F, 8.0F),
                ModelBakery.PartPose.offsetAndRotation(0.0F, -1.0F, 7.0F, -0.7418F, 0.0F, 0.0F));

        ModelBakery.CubeListBuilder babyLeg = ModelBakery.CubeListBuilder.create()
                .texOffs(12, 46).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 9.0F, 3.0F);
        root.addOrReplaceChild("left_hind_leg", babyLeg,
                ModelBakery.PartPose.offset(2.4F, 16.0F, 5.4F));
        root.addOrReplaceChild("right_hind_leg", babyLeg,
                ModelBakery.PartPose.offset(-2.4F, 16.0F, 5.4F));
        root.addOrReplaceChild("left_front_leg", babyLeg,
                ModelBakery.PartPose.offset(2.4F, 16.0F, -5.4F));
        root.addOrReplaceChild("right_front_leg", babyLeg,
                ModelBakery.PartPose.offset(-2.4F, 16.0F, -5.4F));

        ModelBakery.PartDefinition neck = root.addOrReplaceChild("head_parts",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(30, 0).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 8.0F, 4.0F),
                ModelBakery.PartPose.offsetAndRotation(0.0F, 10.0F, -6.0F, 0.6109F, 0.0F, 0.0F));

        ModelBakery.PartDefinition head = neck.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0F, -3.9484F, -6.705F, 6.0F, 4.0F, 9.0F),
                ModelBakery.PartPose.offset(0.0F, -6.0516F, -0.2951F));

        head.addOrReplaceChild("left_ear",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 4).addBox(-1.0F, -2.5F, -0.8F, 2.0F, 3.0F, 1.0F),
                ModelBakery.PartPose.offsetAndRotation(2.0F, -4.2484F, 1.9451F, 0.0F, 0.0F, 0.2618F));
        head.addOrReplaceChild("right_ear",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-1.0F, -2.5F, -0.5F, 2.0F, 3.0F, 1.0F),
                ModelBakery.PartPose.offsetAndRotation(-2.0F, -4.2484F, 1.645F, 0.0F, 0.0F, -0.2618F));

        return root.bake();
    }

    protected ModelBakery.BakedPart buildSaddleModel(String texture) {
        ModelBakery.PartDefinition root = buildAdultBasePartDefinition(texture);
        ModelBakery.PartDefinition body = root.getChildren().get("body");
        ModelBakery.PartDefinition headParts = root.getChildren().get("head_parts");

        body.addOrReplaceChild("saddle",
                ModelBakery.CubeListBuilder.create().texOffs(26, 0)
                        .addBox(-5.0F, -8.0F, -9.0F, 10.0F, 9.0F, 9.0F, new ModelBakery.CubeDeformation(0.5F)),
                ModelBakery.PartPose.ZERO);

        headParts.addOrReplaceChild("left_saddle_mouth",
                ModelBakery.CubeListBuilder.create().texOffs(29, 5)
                        .addBox(2.0F, -9.0F, -6.0F, 1.0F, 2.0F, 2.0F),
                ModelBakery.PartPose.ZERO);
        headParts.addOrReplaceChild("right_saddle_mouth",
                ModelBakery.CubeListBuilder.create().texOffs(29, 5)
                        .addBox(-3.0F, -9.0F, -6.0F, 1.0F, 2.0F, 2.0F),
                ModelBakery.PartPose.ZERO);

        headParts.addOrReplaceChild("left_saddle_line",
                ModelBakery.CubeListBuilder.create().texOffs(32, 2)
                        .addBox(3.1F, -6.0F, -8.0F, 0.0F, 3.0F, 16.0F),
                ModelBakery.PartPose.rotation((float) (-Math.PI / 6), 0.0F, 0.0F));
        headParts.addOrReplaceChild("right_saddle_line",
                ModelBakery.CubeListBuilder.create().texOffs(32, 2)
                        .addBox(-3.1F, -6.0F, -8.0F, 0.0F, 3.0F, 16.0F),
                ModelBakery.PartPose.rotation((float) (-Math.PI / 6), 0.0F, 0.0F));

        headParts.addOrReplaceChild("head_saddle",
                ModelBakery.CubeListBuilder.create().texOffs(1, 1)
                        .addBox(-3.0F, -11.0F, -1.9F, 6.0F, 5.0F, 6.0F, new ModelBakery.CubeDeformation(0.22F)),
                ModelBakery.PartPose.ZERO);
        headParts.addOrReplaceChild("mouth_saddle_wrap",
                ModelBakery.CubeListBuilder.create().texOffs(19, 0)
                        .addBox(-2.0F, -11.0F, -4.0F, 4.0F, 5.0F, 2.0F, new ModelBakery.CubeDeformation(0.2F)),
                ModelBakery.PartPose.ZERO);

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, T entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyYaw);
        double headYawRel = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYaw);
        double headPitch = Mth.DEG_TO_RAD * entity.getXRot(1.0f);

        boolean ridden = entity.isVehicle();
        boolean saddle = hasSaddle(entity);
        boolean armor = hasArmor(entity);

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + 1.5f, pos.z)
                .rotateY(modelYaw)
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        double animPos = entity.walkAnimation.position();
        double animSpeed = entity.walkAnimation.speed();
        double eatAnim = entity.getEatAnim(1.0f);
        double standAnim = entity.getStandAnim(1.0f);
        double feedingAnim = entity.getMouthAnim(1.0f);
        boolean animateTail = entity.tailCounter > 0;
        double ageInTicks = entity.tickCount;

        boolean inWater = entity.isInWater();
        double waterMultiplier = inWater ? 0.2f : 1.0f;
        double legAnim1 = Mth.cos(waterMultiplier * animPos * 0.6662f + Mth.PI);

        LegPose legs = new LegPose(legAnim1, animSpeed, standAnim, inWater, ageInTicks);
        HeadPose headPose = new HeadPose(headYawRel, headPitch, eatAnim, standAnim, feedingAnim, ageInTicks);

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        if (!entity.isInvisible())
            renderEquinePart(pipeline, buildRoot(entity), "root", base, legs, headPose, animateTail, ridden, false, block, sky);

        if (saddle) {
            String saddleTex = getSaddleTexture(entity);
            ModelBakery.BakedPart saddleModel = saddleCache.computeIfAbsent(saddleTex, this::buildSaddleModel);
            renderEquinePart(pipeline, saddleModel, "root", base, legs, headPose, animateTail, ridden, true, block, sky);
        }

        if (armor) {
            String armorTex = getArmorTexture(entity);
            ModelBakery.BakedPart armorModel = armorCache.computeIfAbsent(armorTex, this::buildAdultBase);
            renderEquinePart(pipeline, armorModel, "root", base, legs, headPose, animateTail, ridden, false, block, sky);
        }
    }

    protected void renderEquinePart(RenderPipeline pipeline, ModelBakery.BakedPart part,
                                    String name, Matrix4d parent,
                                    LegPose legs, HeadPose headPose, boolean animateTail,
                                    boolean ridden, boolean isSaddle,
                                    double block, double sky) {

        if (isSaddle && (name.equals("left_saddle_line") || name.equals("right_saddle_line")) && !ridden) {
            return;
        }

        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        var ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        double stand = legs.standAnim;
        double eat = headPose.eatAnim;
        double feed = headPose.feedingAnim;
        double ageTicks = headPose.ageInTicks;

        switch (name) {
            case "body" -> {
                double bodyStandRot = stand * (-Mth.PI / 4);
                mat.rotateX(bodyStandRot);
            }
            case "head_parts" -> {
                double headRotXRad = headPose.headPitch;
                double clampedYRot = Mth.clamp(headPose.headYawRel, -20.0f * Mth.DEG_TO_RAD, 20.0f * Mth.DEG_TO_RAD);
                if (legs.animSpeed > 0.2f) {
                    headRotXRad += Mth.cos(legs.legAnim1 * 0.8f) * 0.15f * legs.animSpeed;
                }

                double yOffset = Mth.lerp(eat, Mth.lerp(stand, 0.0f, -8.0f), 7.0f);
                double zOffset = stand * -4.0f;
                mat.translate(0.0f, yOffset / 16.0f, zOffset / 16.0f);

                double baseHeadAngle = (1.0f - Math.max(stand, eat)) *
                        (headRotXRad + feed * Mth.sin(ageTicks) * 0.05f);
                double headXRot = stand * (Mth.PI / 12 + headRotXRad)
                        + eat * (2.1816616f + Mth.sin(ageTicks) * 0.05f)
                        + baseHeadAngle;

                mat.rotateY(clampedYRot);
                mat.rotateX(headXRot);
            }
            case "tail" -> {
                double tailXRotOffset = getTailXRotOffset();
                double tailBaseRot = tailXRotOffset + legs.animSpeed * 0.75f;
                if (animateTail) {
                    mat.rotateY(Mth.cos(ageTicks * 0.7f));
                }
                mat.translate(0.0f, legs.animSpeed / 16.0f,
                        legs.animSpeed * 2.0f / 16.0f);
                mat.rotateX(tailBaseRot);
            }
            case "right_hind_leg", "left_hind_leg", "right_front_leg", "left_front_leg" -> {
                double iStand = 1.0f - stand;
                double legAnimStand = legs.legAnim1 * 0.8f * legs.animSpeed * iStand;
                double standAngle = Mth.PI / 12 * stand;

                if (name.contains("hind")) {
                    double hindLegXRot = standAngle + (name.contains("right") ? legAnimStand : -legAnimStand);
                    mat.rotateX(hindLegXRot);
                } else {
                    double legStandY = getLegStandingYOffset() * stand;
                    double legStandZ = getLegStandingZOffset() * stand;
                    mat.translate(0.0f, -legStandY / 16.0f, legStandZ / 16.0f);

                    double bob = Mth.cos(ageTicks * 0.6f + Mth.PI);
                    double legStandXRotOffset = -Mth.PI / 3;
                    double xRot;
                    if (name.contains("right")) {
                        xRot = (legStandXRotOffset + bob) * stand + legAnimStand;
                    } else {
                        xRot = (legStandXRotOffset - bob) * stand - legAnimStand;
                    }
                    mat.rotateX(xRot);
                }
            }
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }

        for (var child : part.children.entrySet()) {
            renderEquinePart(
                    pipeline, child.getValue(), child.getKey(), mat,
                    legs, headPose, animateTail, ridden, isSaddle,
                    block, sky
            );
        }
    }

    protected double getTailXRotOffset() {
        return 0.0f;
    }

    protected double getLegStandingYOffset() {
        return 12.0f;
    }

    protected double getLegStandingZOffset() {
        return 4.0f;
    }

    protected record LegPose(double legAnim1, double animSpeed, double standAnim, boolean inWater, double ageInTicks) {
    }

    protected record HeadPose(double headYawRel, double headPitch, double eatAnim, double standAnim, double feedingAnim,
                              double ageInTicks) {
    }
}