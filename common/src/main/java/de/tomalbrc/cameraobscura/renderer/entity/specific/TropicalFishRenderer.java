package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class TropicalFishRenderer implements LivingEntityRenderer<TropicalFish> {

    private static final String SMALL_TEXTURE = "entity/fish/tropical_a";
    private static final String LARGE_TEXTURE = "entity/fish/tropical_b";
    private static final int TEX_WIDTH = 32;
    private static final int TEX_HEIGHT = 32;

    private static final Map<TropicalFish.Pattern, String> PATTERN_TEXTURES = new EnumMap<>(TropicalFish.Pattern.class);

    static {
        PATTERN_TEXTURES.put(TropicalFish.Pattern.KOB, "entity/fish/tropical_a_pattern_1");
        PATTERN_TEXTURES.put(TropicalFish.Pattern.SUNSTREAK, "entity/fish/tropical_a_pattern_2");
        PATTERN_TEXTURES.put(TropicalFish.Pattern.SNOOPER, "entity/fish/tropical_a_pattern_3");
        PATTERN_TEXTURES.put(TropicalFish.Pattern.DASHER, "entity/fish/tropical_a_pattern_4");
        PATTERN_TEXTURES.put(TropicalFish.Pattern.BRINELY, "entity/fish/tropical_a_pattern_5");
        PATTERN_TEXTURES.put(TropicalFish.Pattern.SPOTTY, "entity/fish/tropical_a_pattern_6");
        PATTERN_TEXTURES.put(TropicalFish.Pattern.FLOPPER, "entity/fish/tropical_b_pattern_1");
        PATTERN_TEXTURES.put(TropicalFish.Pattern.STRIPEY, "entity/fish/tropical_b_pattern_2");
        PATTERN_TEXTURES.put(TropicalFish.Pattern.GLITTER, "entity/fish/tropical_b_pattern_3");
        PATTERN_TEXTURES.put(TropicalFish.Pattern.BLOCKFISH, "entity/fish/tropical_b_pattern_4");
        PATTERN_TEXTURES.put(TropicalFish.Pattern.BETTY, "entity/fish/tropical_b_pattern_5");
        PATTERN_TEXTURES.put(TropicalFish.Pattern.CLAYFISH, "entity/fish/tropical_b_pattern_6");
    }

    private final Map<TropicalFish.Pattern, ModelBakery.BakedPart> patternCache = new HashMap<>();
    private ModelBakery.BakedPart cachedSmallRoot;
    private ModelBakery.BakedPart cachedLargeRoot;

    private ModelBakery.BakedPart buildSmallRoot(String texture) {
        ModelBakery bakery = new ModelBakery(texture, TEX_WIDTH, TEX_HEIGHT);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-1, -1.5f, -3, 2, 3, 6),
                ModelBakery.PartPose.offset(0, 22, 0));

        root.addOrReplaceChild("tail",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(22, -6).addBox(0, -1.5f, 0, 0, 3, 6),
                ModelBakery.PartPose.offset(0, 22, 3));

        root.addOrReplaceChild("right_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(2, 16).addBox(-2, -1, 0, 2, 2, 0),
                ModelBakery.PartPose.offsetAndRotation(-1, 22.5f, 0, 0, Mth.PI / 4, 0));

        root.addOrReplaceChild("left_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(2, 12).addBox(0, -1, 0, 2, 2, 0),
                ModelBakery.PartPose.offsetAndRotation(1, 22.5f, 0, 0, -Mth.PI / 4, 0));

        root.addOrReplaceChild("top_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(10, -5).addBox(0, -3, 0, 0, 3, 6),
                ModelBakery.PartPose.offset(0, 20.5f, -3));

        return root.bake();
    }

    private ModelBakery.BakedPart buildLargeRoot(String texture) {
        ModelBakery bakery = new ModelBakery(texture, TEX_WIDTH, TEX_HEIGHT);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 20).addBox(-1, -3, -3, 2, 6, 6),
                ModelBakery.PartPose.offset(0, 19, 0));

        root.addOrReplaceChild("tail",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(21, 16).addBox(0, -3, 0, 0, 6, 5),
                ModelBakery.PartPose.offset(0, 19, 3));

        root.addOrReplaceChild("right_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(2, 16).addBox(-2, 0, 0, 2, 2, 0),
                ModelBakery.PartPose.offsetAndRotation(-1, 20, 0, 0, Mth.PI / 4, 0));

        root.addOrReplaceChild("left_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(2, 12).addBox(0, 0, 0, 2, 2, 0),
                ModelBakery.PartPose.offsetAndRotation(1, 20, 0, 0, -Mth.PI / 4, 0));

        root.addOrReplaceChild("top_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(20, 11).addBox(0, -4, 0, 0, 4, 6),
                ModelBakery.PartPose.offset(0, 16, -3));

        root.addOrReplaceChild("bottom_fin",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(20, 21).addBox(0, 0, 0, 0, 4, 6),
                ModelBakery.PartPose.offset(0, 22, -3));

        return root.bake();
    }

    @Override
    public ModelBakery.BakedPart buildRoot(TropicalFish entity) {
        if (entity.getPattern().base() == TropicalFish.Base.LARGE) {
            if (cachedLargeRoot == null) cachedLargeRoot = buildLargeRoot(LARGE_TEXTURE);
            return cachedLargeRoot;
        } else {
            if (cachedSmallRoot == null) cachedSmallRoot = buildSmallRoot(SMALL_TEXTURE);
            return cachedSmallRoot;
        }
    }

    private ModelBakery.BakedPart getPatternModel(TropicalFish.Pattern pattern) {
        return patternCache.computeIfAbsent(pattern, p -> {
            String tex = PATTERN_TEXTURES.get(p);
            if (tex == null) return null;
            return p.base() == TropicalFish.Base.LARGE
                    ? buildLargeRoot(tex)
                    : buildSmallRoot(tex);
        });
    }

    @Override
    public void render(RenderPipeline pipeline, TropicalFish entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);

        double ageInTicks = entity.tickCount + 1.0f;
        double bodyZRotDeg = 4.3f * Mth.sin(0.6f * ageInTicks);
        double bodyZRotRad = bodyZRotDeg * Mth.DEG_TO_RAD;

        boolean inWater = entity.isInWater();

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y, pos.z)
                .rotateY(Mth.DEG_TO_RAD * (180f - bodyYaw))
                .rotateY(bodyZRotRad);

        if (!inWater) {
            base.translate(0.2f, 0.1f, 0.0f);
            base.rotateZ(Mth.PI / 2);
        }

        base.translate(0, 1.5f, 0);
        base.rotateY(Mth.PI);
        base.rotateX(Mth.PI);

        double amplitudeMultiplier = inWater ? 1.0f : 1.5f;
        double tailYRot = -amplitudeMultiplier * 0.45f * Mth.sin(0.6f * ageInTicks);

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        int baseColor = 0xFF000000 | entity.getBaseColor().getTextureDiffuseColor();
        ModelBakery.BakedPart baseModel = buildRoot(entity);
        renderPart(pipeline, baseModel, "root", base, tailYRot, baseColor, block, sky);

        TropicalFish.Pattern pattern = entity.getPattern();
        ModelBakery.BakedPart patternModel = getPatternModel(pattern);
        int patternColor = 0xFF000000 | entity.getPatternColor().getTextureDiffuseColor();
        if (patternModel != null) {
            renderPart(pipeline, patternModel, "root", base, tailYRot, patternColor, block, sky);
        }
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part,
                            String name, Matrix4d parent, double tailYRot, int color,
                            double block, double sky) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        if (name.equals("tail")) {
            mat.rotateY(tailYRot);
        }

        var ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(color)));
        }

        for (var child : part.children.entrySet()) {
            renderPart(pipeline, child.getValue(), child.getKey(), mat, tailYRot, color, block, sky);
        }
    }
}