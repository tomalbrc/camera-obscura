package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

import java.util.Map;

public class ZombieVillagerRenderer extends HumanoidRenderer<ZombieVillager> {

    private static final String ADULT_TEXTURE = "entity/zombie_villager/zombie_villager";
    private static final String BABY_TEXTURE = "entity/zombie_villager/zombie_villager_baby";
    private final Map<String, ModelBakery.BakedPart> overlayCache = new Object2ObjectOpenHashMap<>();
    private ModelBakery.BakedPart cachedAdultRoot;
    private ModelBakery.BakedPart cachedBabyRoot;
    private double lastRightLegAngle, lastLeftLegAngle, lastRightArmAngle, lastLeftArmAngle;

    private static String typeTextureKey(Holder<VillagerType> type) {
        return type.unwrapKey().map(key -> "entity/zombie_villager/type/" + key.identifier().getPath()).orElse("entity/zombie_villager/type/plains");
    }

    private static String professionTextureKey(Holder<VillagerProfession> prof) {
        return prof.unwrapKey().map(key -> "entity/zombie_villager/profession/" + key.identifier().getPath()).orElse("entity/zombie_villager/profession/none");
    }

    private static String levelTextureKey(int level) {
        return switch (level) {
            case 2 -> "entity/zombie_villager/profession_level/iron";
            case 3 -> "entity/zombie_villager/profession_level/gold";
            case 4 -> "entity/zombie_villager/profession_level/emerald";
            case 5 -> "entity/zombie_villager/profession_level/diamond";
            default -> "entity/zombie_villager/profession_level/stone";
        };
    }

    @Override
    protected String getTexturePath(ZombieVillager entity) {
        return entity.isBaby() ? BABY_TEXTURE : ADULT_TEXTURE;
    }

    @Override
    protected int getTexWidth() {
        return 64;
    }

    @Override
    protected int getTexHeight() {
        return 64;
    }

    @Override
    public ModelBakery.BakedPart buildRoot(ZombieVillager entity) {
        if (entity.isBaby()) {
            if (cachedBabyRoot == null) cachedBabyRoot = buildModel(BABY_TEXTURE, true);
            return cachedBabyRoot;
        } else {
            if (cachedAdultRoot == null) cachedAdultRoot = buildModel(ADULT_TEXTURE, false);
            return cachedAdultRoot;
        }
    }

    private ModelBakery.BakedPart buildModel(String texture, boolean baby) {
        ModelBakery bakery = new ModelBakery(texture, 64, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        if (baby) {
            root.addOrReplaceChild("body",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(0, 15).addBox(-2, -2.75f, -1.5f, 4, 5, 3)
                            .texOffs(16, 22).addBox(-2, -2.75f, -1.5f, 4, 6, 3, new ModelBakery.CubeDeformation(0.1f)),
                    ModelBakery.PartPose.offset(0, 18.75f, 0));

            ModelBakery.PartDefinition head = root.addOrReplaceChild("head",
                    ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-4, -8, -3.5f, 8, 8, 7),
                    ModelBakery.PartPose.offset(0, 16, 0));
            head.addOrReplaceChild("hat",
                    ModelBakery.CubeListBuilder.create().texOffs(0, 31).addBox(-4, -4, -3.5f, 8, 8, 7, new ModelBakery.CubeDeformation(0.3f)),
                    ModelBakery.PartPose.offset(0, -4, 0));
            head.addOrReplaceChild("hat_rim",
                    ModelBakery.CubeListBuilder.create().texOffs(0, 46).addBox(-7, -0.5f, -6, 14, 1, 12),
                    ModelBakery.PartPose.offset(0, -4.5f, 0));
            head.addOrReplaceChild("nose",
                    ModelBakery.CubeListBuilder.create().texOffs(23, 0).addBox(-1, -1, -0.5f, 2, 2, 1),
                    ModelBakery.PartPose.offset(0, -1, -4));

            root.addOrReplaceChild("right_arm",
                    ModelBakery.CubeListBuilder.create().texOffs(24, 15).addBox(-1, -0.5f, -1, 2, 5, 2),
                    ModelBakery.PartPose.offset(-3, 15.5f, 0));
            root.addOrReplaceChild("left_arm",
                    ModelBakery.CubeListBuilder.create().texOffs(16, 15).addBox(-1, -0.5f, -1, 2, 5, 2),
                    ModelBakery.PartPose.offset(3, 15.5f, 0));

            root.addOrReplaceChild("right_leg",
                    ModelBakery.CubeListBuilder.create().texOffs(8, 23).addBox(-1, -0.5f, -1, 2, 3, 2),
                    ModelBakery.PartPose.offset(-1, 21.5f, 0));
            root.addOrReplaceChild("left_leg",
                    ModelBakery.CubeListBuilder.create().texOffs(0, 23).addBox(-1, -0.5f, -1, 2, 3, 2),
                    ModelBakery.PartPose.offset(1, 21.5f, 0));
        } else {
            root.addOrReplaceChild("body",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(16, 20).addBox(-4, 0, -3, 8, 12, 6)
                            .texOffs(0, 38).addBox(-4, 0, -3, 8, 20, 6, new ModelBakery.CubeDeformation(0.05f)),
                    ModelBakery.PartPose.offset(0, 0, 0));

            ModelBakery.PartDefinition head = root.addOrReplaceChild("head",
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(0, 0).addBox(-4, -10, -4, 8, 10, 8)
                            .texOffs(24, 0).addBox(-1, -3, -6, 2, 4, 2),
                    ModelBakery.PartPose.ZERO);

            ModelBakery.PartDefinition hat = head.addOrReplaceChild("hat",
                    ModelBakery.CubeListBuilder.create().texOffs(32, 0).addBox(-4, -10, -4, 8, 10, 8, new ModelBakery.CubeDeformation(0.5f)),
                    ModelBakery.PartPose.ZERO);
            hat.addOrReplaceChild("hat_rim",
                    ModelBakery.CubeListBuilder.create().texOffs(30, 47).addBox(-8, -8, -6, 16, 16, 1),
                    ModelBakery.PartPose.rotation(-Mth.PI / 2, 0, 0));

            root.addOrReplaceChild("right_arm",
                    ModelBakery.CubeListBuilder.create().texOffs(44, 22).addBox(-3, -2, -2, 4, 12, 4),
                    ModelBakery.PartPose.offset(-5, 2, 0));
            root.addOrReplaceChild("left_arm",
                    ModelBakery.CubeListBuilder.create().texOffs(44, 22).mirror().addBox(-1, -2, -2, 4, 12, 4),
                    ModelBakery.PartPose.offset(5, 2, 0));

            root.addOrReplaceChild("right_leg",
                    ModelBakery.CubeListBuilder.create().texOffs(0, 22).addBox(-2, 0, -2, 4, 12, 4),
                    ModelBakery.PartPose.offset(-2, 12, 0));
            root.addOrReplaceChild("left_leg",
                    ModelBakery.CubeListBuilder.create().texOffs(0, 22).mirror().addBox(-2, 0, -2, 4, 12, 4),
                    ModelBakery.PartPose.offset(2, 12, 0));
        }

        return root.bake();
    }

    @Override
    protected void buildModelParts(ModelBakery.PartDefinition root, ModelBakery bakery) {
    }

    @Override
    protected LimbAngles getLimbAngles(ZombieVillager entity, double animPos, double animSpeed,
                                       double headYaw, double headPitch,
                                       double swim, boolean flying, boolean crouch,
                                       LimbAngles base) {
        boolean aggressive = entity.isAggressive();
        double attackTime = entity.getAttackAnim(1f);
        double armDrop = (double) -Math.PI / (aggressive ? 1.5F : 2.25F);
        double attackYRotModifier = Mth.sin(attackTime * (double) Math.PI);
        double attackXRotModifier = Mth.sin((1.0F - (1.0F - attackTime) * (1.0F - attackTime)) * (double) Math.PI);
        double angle = armDrop + attackYRotModifier * 1.2F - attackXRotModifier * 0.4F;
        base.rightArmAngle = angle;
        base.leftArmAngle = angle;
        base.rightArmYaw = -(0.1F - attackYRotModifier * 0.6F);
        base.leftArmYaw = 0.1F - attackYRotModifier * 0.6F;
        double bob = Mth.cos(entity.tickCount * 0.09F) * 0.05F + 0.05F;
        base.rightArmZ = bob;
        base.leftArmZ = -bob;

        lastRightLegAngle = base.rightLegAngle;
        lastLeftLegAngle = base.leftLegAngle;
        lastRightArmAngle = base.rightArmAngle;
        lastLeftArmAngle = base.leftArmAngle;

        return base;
    }

    @Override
    public void render(RenderPipeline pipeline, ZombieVillager entity) {
        super.render(pipeline, entity);
        if (!entity.isBaby()) {
            renderProfessionLayers(pipeline, entity);
        }
    }

    private void renderProfessionLayers(RenderPipeline pipeline, ZombieVillager entity) {
        VillagerData data = entity.getVillagerData();

        Matrix4d base = computeBaseTransform(entity);

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        String typeTex = typeTextureKey(data.type());
        ModelBakery.BakedPart typeModel = overlayCache.computeIfAbsent(typeTex, t -> buildModel(t, false));
        renderOverlayModel(pipeline, typeModel, base, entity, block, sky);

        Holder<VillagerProfession> prof = data.profession();
        if (!prof.is(VillagerProfession.NONE)) {
            String profTex = professionTextureKey(prof);
            ModelBakery.BakedPart profModel = overlayCache.computeIfAbsent(profTex, t -> buildModel(t, false));
            renderOverlayModel(pipeline, profModel, base, entity, block, sky);

            if (!prof.is(VillagerProfession.NITWIT) && data.level() > 1) {
                String levelTex = levelTextureKey(data.level());
                ModelBakery.BakedPart levelModel = overlayCache.computeIfAbsent(levelTex, t -> buildModel(t, false));
                renderOverlayModel(pipeline, levelModel, base, entity, block, sky);
            }
        }
    }

    private Matrix4d computeBaseTransform(ZombieVillager entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyYaw);

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + 1.5f, pos.z)
                .rotateY(modelYaw)
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);
        return base;
    }

    private void renderOverlayModel(RenderPipeline pipeline, ModelBakery.BakedPart overlay,
                                    Matrix4d base, ZombieVillager entity,
                                    double block, double sky) {

        double animPos = entity.walkAnimation.position();

        LimbAngles angles = new LimbAngles();
        angles.headYaw = Mth.DEG_TO_RAD * (entity.getYHeadRot() - entity.getPreciseBodyRotation(1.0f));
        angles.headPitch = Mth.DEG_TO_RAD * entity.getXRot(1.0f);
        angles.rightLegAngle = lastRightLegAngle;
        angles.leftLegAngle = lastLeftLegAngle;
        angles.rightArmAngle = lastRightArmAngle;
        angles.leftArmAngle = lastLeftArmAngle;

        ArmPose empty = ArmPose.EMPTY;
        Matrix4d[] dummy = new Matrix4d[1];
        renderPart(pipeline, overlay, "root", base, angles, empty, empty,
                entity.getMainHandItem(), entity.getOffhandItem(), entity,
                0, false, false, animPos, dummy, dummy, dummy, block, sky);
    }
}
