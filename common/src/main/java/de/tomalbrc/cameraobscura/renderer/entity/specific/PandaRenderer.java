package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.FourLeggedRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ItemStackRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.panda.Panda;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class PandaRenderer extends FourLeggedRenderer<Panda> {

    private static final Map<Panda.Gene, String> TEXTURES = new EnumMap<>(Panda.Gene.class);
    private static final Map<Panda.Gene, String> BABY_TEXTURES = new EnumMap<>(Panda.Gene.class);

    static {
        TEXTURES.put(Panda.Gene.NORMAL, "entity/panda/panda");
        TEXTURES.put(Panda.Gene.LAZY, "entity/panda/panda_lazy");
        TEXTURES.put(Panda.Gene.WORRIED, "entity/panda/panda_worried");
        TEXTURES.put(Panda.Gene.PLAYFUL, "entity/panda/panda_playful");
        TEXTURES.put(Panda.Gene.BROWN, "entity/panda/panda_brown");
        TEXTURES.put(Panda.Gene.WEAK, "entity/panda/panda_weak");
        TEXTURES.put(Panda.Gene.AGGRESSIVE, "entity/panda/panda_aggressive");

        BABY_TEXTURES.put(Panda.Gene.NORMAL, "entity/panda/panda_baby");
        BABY_TEXTURES.put(Panda.Gene.LAZY, "entity/panda/lazy_panda_baby");
        BABY_TEXTURES.put(Panda.Gene.WORRIED, "entity/panda/worried_panda_baby");
        BABY_TEXTURES.put(Panda.Gene.PLAYFUL, "entity/panda/playful_panda_baby");
        BABY_TEXTURES.put(Panda.Gene.BROWN, "entity/panda/brown_panda_baby");
        BABY_TEXTURES.put(Panda.Gene.WEAK, "entity/panda/weak_panda_baby");
        BABY_TEXTURES.put(Panda.Gene.AGGRESSIVE, "entity/panda/aggressive_panda_baby");
    }

    private final Map<String, ModelBakery.BakedPart> cache = new HashMap<>();

    @Override
    public ModelBakery.BakedPart buildRoot(Panda entity) {
        Panda.Gene gene = entity.getVariant();
        boolean baby = entity.isBaby();
        String key = gene.name() + (baby ? "_baby" : "");
        return cache.computeIfAbsent(key, k -> {
            String tex = baby ? BABY_TEXTURES.getOrDefault(gene, BABY_TEXTURES.get(Panda.Gene.NORMAL)) :
                    TEXTURES.getOrDefault(gene, TEXTURES.get(Panda.Gene.NORMAL));
            return baby ? buildBabyModel(tex) : buildAdultModel(tex);
        });
    }

    private ModelBakery.BakedPart buildAdultModel(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 64, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 6).addBox(-6.5f, -5, -4, 13, 10, 9)
                        .texOffs(45, 16).addBox("nose", -3.5f, 0, -6, 7, 5, 2)
                        .texOffs(52, 25).addBox("left_ear", 3.5f, -8, -1, 5, 4, 1)
                        .texOffs(52, 25).addBox("right_ear", -8.5f, -8, -1, 5, 4, 1),
                ModelBakery.PartPose.offset(0, 11.5f, -17));

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(0, 25).addBox(-9.5f, -13, -6.5f, 19, 26, 13),
                ModelBakery.PartPose.offsetAndRotation(0, 10, 0, Mth.PI / 2, 0, 0));

        ModelBakery.CubeListBuilder leg = ModelBakery.CubeListBuilder.create()
                .texOffs(40, 0).addBox(-3, 0, -3, 6, 9, 6);
        root.addOrReplaceChild("right_hind_leg", leg, ModelBakery.PartPose.offset(-5.5f, 15, 9));
        root.addOrReplaceChild("left_hind_leg", leg, ModelBakery.PartPose.offset(5.5f, 15, 9));
        root.addOrReplaceChild("right_front_leg", leg, ModelBakery.PartPose.offset(-5.5f, 15, -9));
        root.addOrReplaceChild("left_front_leg", leg, ModelBakery.PartPose.offset(5.5f, 15, -9));

        return root.bake();
    }

    private ModelBakery.BakedPart buildBabyModel(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 64, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(0, 11).addBox(-4.5f, -3.5f, -5.5f, 9, 7, 11),
                ModelBakery.PartPose.offset(0, 18.5f, 2.5f));

        root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.5f, -3, -5, 7, 6, 5)
                        .texOffs(24, 6).addBox(-2, 1, -6, 4, 2, 1)
                        .texOffs(24, 0).addBox(-4.5f, -4, -3.5f, 3, 3, 1)
                        .texOffs(33, 0).addBox(1.5f, -4, -3.5f, 3, 3, 1),
                ModelBakery.PartPose.offset(0, 19, -3));

        root.addOrReplaceChild("right_hind_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 34).addBox(-1.5f, 0, -1.5f, 3, 2, 3),
                ModelBakery.PartPose.offset(-3, 22, 6.5f));
        root.addOrReplaceChild("left_hind_leg",
                ModelBakery.CubeListBuilder.create().texOffs(12, 34).addBox(-1.5f, 0, -1.5f, 3, 2, 3),
                ModelBakery.PartPose.offset(3, 22, 6.5f));
        root.addOrReplaceChild("right_front_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 29).addBox(-1.5f, 0, -1.5f, 3, 2, 3),
                ModelBakery.PartPose.offset(-3, 22, -1.5f));
        root.addOrReplaceChild("left_front_leg",
                ModelBakery.CubeListBuilder.create().texOffs(12, 29).addBox(-1.5f, 0, -1.5f, 3, 2, 3),
                ModelBakery.PartPose.offset(3, 22, -1.5f));

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, Panda entity) {
        var pos = entity.position();
        double bodyYawDeg = entity.getPreciseBodyRotation(1.0f);
        double modelYawRad = Mth.DEG_TO_RAD * (180f - bodyYawDeg);
        double headYawRelDeg = entity.getYHeadRot() - bodyYawDeg;
        float headYawRad = (float) (Mth.DEG_TO_RAD * headYawRelDeg);
        float headPitchRad = Mth.DEG_TO_RAD * entity.getXRot(1.0f);

        boolean baby = entity.isBaby();
        double ageInTicks = entity.tickCount + 1.0f;

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y, pos.z)
                .rotateY(modelYawRad);

        float rollTime = entity.rollCounter > 0 ? entity.rollCounter + 1.0f : 0.0f;
        if (rollTime > 0.0f) {
            applyRollingTransform(base, entity, rollTime);
        }
        float sitAmount = entity.getSitAmount(1.0f);
        if (sitAmount > 0.0f) {
            double sitY = baby ? 0.5f : 0.8f;
            base.translate(0, sitY * sitAmount, 0);
            double xRot = entity.getXRot(1.0f);
            base.rotateX(Mth.DEG_TO_RAD * Mth.lerp(sitAmount, xRot, xRot + 90.0f));
            base.translate(0, -1.0f * sitAmount, 0);
            if (entity.isScared()) {
                double shakeRot = (Math.cos(ageInTicks * 1.25f) * Math.PI * 0.05f);
                base.rotateY(shakeRot);
                if (baby) {
                    base.translate(0, 0.8f, 0.55f);
                }
            }
        }
        float lieOnBack = entity.getLieOnBackAmount(1.0f);
        if (lieOnBack > 0.0f) {
            double yOffset = baby ? 0.5f : 1.3f;
            base.translate(0, yOffset * lieOnBack, 0);
            base.rotateX(Mth.DEG_TO_RAD * Mth.lerp(lieOnBack, entity.getXRot(1.0f), entity.getXRot(1.0f) + 180.0f));
        }

        base.translate(0, 1.5f, 0);
        base.rotateY(Mth.PI);
        base.rotateX(Mth.PI);

        double animPos = entity.walkAnimation.position();
        double animSpeed = entity.walkAnimation.speed();
        double limbSwing = animPos * 0.6662f;
        double limbAngle = Mth.cos(limbSwing) * 1.4f * animSpeed;

        double rightHindAngle = limbAngle;
        double leftHindAngle = Mth.cos(limbSwing + Mth.PI) * 1.4f * animSpeed;
        double rightFrontAngle = Mth.cos(limbSwing + Mth.PI) * 1.4f * animSpeed;
        double leftFrontAngle = limbAngle;

        if (entity.getUnhappyCounter() > 0) {
            headYawRad += 0.35f * Mth.sin(0.6f * ageInTicks);
            headPitchRad += 0.35f * Mth.sin(0.6f * ageInTicks);
            rightFrontAngle = -0.75f * Mth.sin(0.3f * ageInTicks);
            leftFrontAngle = 0.75f * Mth.sin(0.3f * ageInTicks);
        }
        if (entity.isSneezing()) {
            int sneezeTime = entity.getSneezeCounter();
            if (sneezeTime < 15) {
                headPitchRad += (-Math.PI / 4) * sneezeTime / 14.0f;
            } else if (sneezeTime < 20) {
                double internal = (sneezeTime - 15) / 5.0f;
                headPitchRad += (-Math.PI / 4) + (Math.PI / 4) * internal;
            }
        }
        if (entity.isEating()) {
            headPitchRad += (Math.PI / 2) + 0.2f * Mth.sin(ageInTicks * 0.6f);
            rightFrontAngle = -0.4f - 0.2f * Mth.sin(ageInTicks * 0.6f);
            leftFrontAngle = -0.4f - 0.2f * Mth.sin(ageInTicks * 0.6f);
        }
        if (entity.isScared()) {
            headPitchRad += 2.1707964f;
            rightFrontAngle = -0.9f;
            leftFrontAngle = -0.9f;
        }

        if (sitAmount > 0.0f) {
            double bodyXRot = Mth.rotLerpRad(sitAmount, 0, 1.7407963f);
            base.rotateX(bodyXRot);
        }

        if (lieOnBack > 0.0f) {
            rightHindAngle = -0.6f * Mth.sin(ageInTicks * 0.15f);
            leftHindAngle = 0.6f * Mth.sin(ageInTicks * 0.15f);
            rightFrontAngle = 0.3f * Mth.sin(ageInTicks * 0.25f);
            leftFrontAngle = -0.3f * Mth.sin(ageInTicks * 0.25f);
            headPitchRad = Mth.rotLerpRad(lieOnBack, headPitchRad, (float) (Math.PI / 2));
        }

        if (rollTime > 0.0f) {
            headPitchRad = Mth.rotLerpRad(Math.min(rollTime, 1.0f), headPitchRad, 2.0561945f);
            rightHindAngle = -0.5f * Mth.sin(ageInTicks * 0.5f);
            leftHindAngle = 0.5f * Mth.sin(ageInTicks * 0.5f);
            rightFrontAngle = 0.5f * Mth.sin(ageInTicks * 0.5f);
            leftFrontAngle = -0.5f * Mth.sin(ageInTicks * 0.5f);
        }

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        ModelBakery.BakedPart rootModel = buildRoot(entity);
        renderPandaPart(
                pipeline, rootModel, "root", base,
                headYawRad, headPitchRad,
                rightHindAngle, leftHindAngle, rightFrontAngle, leftFrontAngle,
                sitAmount,
                block, sky
        );

        if (entity.isEating()) {
            var itemStack = entity.getItemInHand(InteractionHand.MAIN_HAND);
            if (!itemStack.isEmpty()) {
                Matrix4d mouthMat = getPartWorldMatrix(rootModel, base, headYawRad, headPitchRad,
                        rightHindAngle, leftHindAngle, rightFrontAngle, leftFrontAngle,
                        sitAmount, lieOnBack);

                mouthMat.translate(0.0f, -0.4f, -1.2f);
                mouthMat.rotateX(Math.PI / 2);
                ItemStackRenderer.render(pipeline, itemStack, ItemDisplayContext.GROUND, mouthMat, block, sky);
            }
        }
    }

    private void renderPandaPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                                 Matrix4d parent,
                                 double headYaw, double headPitch,
                                 double rhAngle, double lhAngle, double rfAngle, double lfAngle,
                                 double sitAmount, double block, double sky) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose pose = part.initialPose;
        if (pose.xRot() != 0 || pose.yRot() != 0 || pose.zRot() != 0)
            mat.rotateZYX(pose.zRot(), pose.yRot(), pose.xRot());
        if (pose.xScale() != 1 || pose.yScale() != 1 || pose.zScale() != 1)
            mat.scale(pose.xScale(), pose.yScale(), pose.zScale());

        if (sitAmount > 0.0f) {
            switch (name) {
                case "head":
                    mat.translate(0, 0, -11.5f * sitAmount / 16f);
                    mat.translate(0, 17.5f * sitAmount / 16f, 0);
                    break;
                case "body":
                    mat.translate(0, 0, -1.5f * sitAmount / 16f);
                    break;
                case "right_front_leg", "left_front_leg":
                    mat.translate(0, 0, -5.0f * sitAmount / 16f);
                    mat.rotateZ(name.equals("right_front_leg") ? -0.27079642f : 0.27079642f);
                    break;
                case "right_hind_leg", "left_hind_leg":
                    mat.translate(0, 0, 3.0f * sitAmount / 16f);
                    mat.rotateZ(name.equals("right_hind_leg") ? 0.5707964f : -0.5707964f);
                    break;
            }
        }

        switch (name) {
            case "head" -> {
                mat.rotateY(headYaw);
                mat.rotateX(headPitch);
            }
            case "right_hind_leg" -> mat.rotateX(rhAngle);
            case "left_hind_leg" -> mat.rotateX(lhAngle);
            case "right_front_leg" -> mat.rotateX(rfAngle);
            case "left_front_leg" -> mat.rotateX(lfAngle);
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }

        for (var child : part.children.entrySet()) {
            renderPandaPart(pipeline, child.getValue(), child.getKey(), mat,
                    headYaw, headPitch,
                    rhAngle, lhAngle, rfAngle, lfAngle,
                    sitAmount, block, sky);
        }
    }

    private void applyRollingTransform(Matrix4d base, Panda entity, double rollTime) {
        boolean baby = entity.isBaby();
        double y = baby ? 0.3f : 0.8f;
        int rollPos = Mth.floor(rollTime);
        double rollProgress = Mth.frac(rollTime);
        int nextRollPos = rollPos + 1;
        double divider = 7.0f;
        double thisAngle, nextAngle;
        if (rollPos < 8) {
            thisAngle = 90.0f * rollPos / divider;
            nextAngle = 90.0f * nextRollPos / divider;
        } else if (rollPos < 16) {
            double internal = (rollPos - 8.0f) / divider;
            thisAngle = 90.0f + 90.0f * internal;
            nextAngle = 90.0f + 90.0f * (nextRollPos - 8.0f) / divider;
        } else if (rollPos < 24) {
            double internal = (rollPos - 16.0f) / divider;
            thisAngle = 180.0f + 90.0f * internal;
            nextAngle = 180.0f + 90.0f * (nextRollPos - 16.0f) / divider;
        } else {
            double internal = (rollPos - 24.0f) / divider;
            thisAngle = 270.0f + 90.0f * internal;
            nextAngle = 270.0f + 90.0f * (nextRollPos - 24.0f) / divider;
        }
        double angle = nextRollPos < (rollPos < 8 ? 8 : rollPos < 16 ? 16 : rollPos < 24 ? 24 : 32) ?
                Mth.lerp(rollProgress, thisAngle, nextAngle) : thisAngle;

        if (rollPos < 8) {
            base.translate(0, (y + 0.2f) * (angle / 90.0f), 0);
        } else if (rollPos < 16) {
            base.translate(0, y + 0.2f + (y - 0.2f) * (angle - 90.0f) / 90.0f, 0);
        } else if (rollPos < 24) {
            base.translate(0, y + y * (270.0f - angle) / 90.0f, 0);
        } else {
            base.translate(0, y * ((360.0f - angle) / 90.0f), 0);
        }
        base.rotateX(-angle * Mth.DEG_TO_RAD);
    }

    private Matrix4d getPartWorldMatrix(ModelBakery.BakedPart root,
                                        Matrix4d parent,
                                        double headYaw, double headPitch,
                                        double rhAngle, double lhAngle, double rfAngle, double lfAngle,
                                        double sitAmount, double lieOnBack) {
        return findPartMatrix(root, "root", parent, "head",
                headYaw, headPitch, rhAngle, lhAngle, rfAngle, lfAngle, sitAmount, lieOnBack);
    }

    private Matrix4d findPartMatrix(ModelBakery.BakedPart part, String name, Matrix4d parent,
                                    String target,
                                    double headYaw, double headPitch,
                                    double rhAngle, double lhAngle, double rfAngle, double lfAngle,
                                    double sitAmount, double lieOnBack) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);
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
            case "right_hind_leg" -> mat.rotateX(rhAngle);
            case "left_hind_leg" -> mat.rotateX(lhAngle);
            case "right_front_leg" -> mat.rotateX(rfAngle);
            case "left_front_leg" -> mat.rotateX(lfAngle);
        }

        if (name.equals(target)) return mat;

        for (var child : part.children.entrySet()) {
            Matrix4d found = findPartMatrix(child.getValue(), child.getKey(), mat, target,
                    headYaw, headPitch, rhAngle, lhAngle, rfAngle, lfAngle, sitAmount, lieOnBack);
            if (found != null) return found;
        }
        return null;
    }
}