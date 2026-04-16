package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Markings;
import net.minecraft.world.entity.animal.equine.Variant;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

import java.util.HashMap;
import java.util.Map;

public class HorseRenderer extends AbstractEquineRenderer<Horse> {
    private static final Map<Variant, String[]> VARIANT_TEXTURES = Map.of(
            Variant.WHITE, new String[]{"entity/horse/horse_white", "entity/horse/horse_white_baby"},
            Variant.CREAMY, new String[]{"entity/horse/horse_creamy", "entity/horse/horse_creamy_baby"},
            Variant.CHESTNUT, new String[]{"entity/horse/horse_chestnut", "entity/horse/horse_chestnut_baby"},
            Variant.BROWN, new String[]{"entity/horse/horse_brown", "entity/horse/horse_brown_baby"},
            Variant.BLACK, new String[]{"entity/horse/horse_black", "entity/horse/horse_black_baby"},
            Variant.GRAY, new String[]{"entity/horse/horse_gray", "entity/horse/horse_gray_baby"},
            Variant.DARK_BROWN, new String[]{"entity/horse/horse_darkbrown", "entity/horse/horse_darkbrown_baby"}
    );

    private static final Map<Markings, String> MARKING_TEXTURES = Map.of(
            Markings.WHITE, "entity/horse/horse_markings_white",
            Markings.WHITE_FIELD, "entity/horse/horse_markings_whitefield",
            Markings.WHITE_DOTS, "entity/horse/horse_markings_whitedots",
            Markings.BLACK_DOTS, "entity/horse/horse_markings_blackdots"
    );
    private static final Map<Markings, String> BABY_MARKING_TEXTURES = Map.of(
            Markings.WHITE, "entity/horse/horse_markings_white_baby",
            Markings.WHITE_FIELD, "entity/horse/horse_markings_whitefield_baby",
            Markings.WHITE_DOTS, "entity/horse/horse_markings_whitedots_baby",
            Markings.BLACK_DOTS, "entity/horse/horse_markings_blackdots_baby"
    );

    private final Map<String, ModelBakery.BakedPart> cache = new HashMap<>();
    private final Map<String, ModelBakery.BakedPart> markingCache = new HashMap<>();

    @Override
    protected String getTexture(Horse entity) {
        Variant variant = entity.getVariant();
        String[] paths = VARIANT_TEXTURES.get(variant);
        if (paths == null) return "entity/horse/horse_white";
        return entity.isBaby() ? paths[1] : paths[0];
    }

    @Override
    public ModelBakery.BakedPart buildRoot(Horse entity) {
        String tex = getTexture(entity);
        return cache.computeIfAbsent(tex, k -> {
            if (entity.isBaby()) {
                return buildBabyBase(tex);
            } else {
                return buildAdultBase(tex);
            }
        });
    }

    private ModelBakery.BakedPart getMarkingModel(Horse entity) {
        Markings marking = entity.getMarkings();
        if (marking == Markings.NONE) return null;
        boolean baby = entity.isBaby();
        String tex = baby ? BABY_MARKING_TEXTURES.get(marking) : MARKING_TEXTURES.get(marking);
        if (tex == null) return null;
        return markingCache.computeIfAbsent(tex, t -> {
            if (baby) {
                return buildBabyBase(t);
            } else {
                return buildAdultBase(t);
            }
        });
    }

    @Override
    public void render(RenderPipeline pipeline, Horse entity) {
        super.render(pipeline, entity);

        ModelBakery.BakedPart markingModel = getMarkingModel(entity);
        if (markingModel == null) return;

        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyYaw);
        double headYawRel = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYaw);
        double headPitch = Mth.DEG_TO_RAD * entity.getXRot(1.0f);
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

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + 1.5f, pos.z)
                .rotateY(modelYaw)
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;
        renderEquinePart(pipeline, markingModel, "root", base, legs, headPose, animateTail, entity.isVehicle(), false, block, sky);
    }
}