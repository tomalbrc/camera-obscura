package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

import java.util.HashMap;
import java.util.Map;

public class GhastRenderer<T extends LivingEntity> implements LivingEntityRenderer<T> {
    private static final Map<String, ModelBakery.BakedPart> CACHE = new HashMap<>();
    private static final Map<String, ModelBakery.BakedPart> harnessCache = new HashMap<>();

    private static final String ROPES_TEXTURE = "entity/ghast/happy_ghast_ropes";

    private ModelBakery.BakedPart cachedRopesAdult, cachedRopesBaby;

    @Override
    public ModelBakery.BakedPart buildRoot(T entity) {
        String key = getCacheKey(entity);
        return CACHE.computeIfAbsent(key, k -> buildModel(k, entity.isBaby()));
    }

    private String getCacheKey(T entity) {
        if (entity instanceof HappyGhast hg) {
            return "happy_" + (hg.isBaby() ? "baby" : "adult");
        } else if (entity instanceof Ghast g) {
            return "ghast_" + (g.isCharging() ? "shooting" : "normal");
        }
        return "ghast_normal";
    }

    private ModelBakery.BakedPart buildModel(String key, boolean baby) {
        String texture;
        boolean happy = key.startsWith("happy");
        if (happy) {
            texture = baby ? "entity/ghast/happy_ghast_baby" : "entity/ghast/happy_ghast";
        } else {
            texture = key.equals("ghast_shooting") ? "entity/ghast/ghast_shooting" : "entity/ghast/ghast";
        }

        ModelBakery bakery = new ModelBakery(texture, 64, happy ? 64 : 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        if (happy) {
            buildHappyGhast(root, baby);
        } else {
            buildNormalGhast(root);
        }

        return root.bake();
    }

    private void buildNormalGhast(ModelBakery.PartDefinition root) {
        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-8, -8, -8, 16, 16, 16),
                ModelBakery.PartPose.offset(0, 17.6f, 0));

        RandomSource rand = RandomSource.createThreadLocalInstance(1660L);
        for (int i = 0; i < 9; i++) {
            float xo = ((i % 3 - i / 3 % 2 * 0.5f + 0.25f) / 2.0f * 2.0f - 1.0f) * 5.0f;
            float yo = (i / 3 / 2.0f * 2.0f - 1.0f) * 5.0f;
            int len = rand.nextInt(7) + 8;
            root.addOrReplaceChild("tentacle" + i,
                    ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-1, 0, -1, 2, len, 2),
                    ModelBakery.PartPose.offset(xo, 24.6f, yo));
        }
    }

    private void buildHappyGhast(ModelBakery.PartDefinition root, boolean baby) {
        var body = root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-8, -8, -8, 16, 16, 16),
                ModelBakery.PartPose.offset(0, 16, 0));

        if (baby) {
            body.addOrReplaceChild("inner_body",
                    ModelBakery.CubeListBuilder.create().texOffs(0, 32).addBox(-8, -16, -8, 16, 16, 16, new ModelBakery.CubeDeformation(-0.5f)),
                    ModelBakery.PartPose.offset(0, 8, 0));
        }

        body.addOrReplaceChild("tentacle0",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-1, 0, -1, 2, 5, 2),
                ModelBakery.PartPose.offset(-3.75f, 7, -5));
        body.addOrReplaceChild("tentacle1",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-1, 0, -1, 2, 7, 2),
                ModelBakery.PartPose.offset(1.25f, 7, -5));
        body.addOrReplaceChild("tentacle2",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-1, 0, -1, 2, 4, 2),
                ModelBakery.PartPose.offset(6.25f, 7, -5));
        body.addOrReplaceChild("tentacle3",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-1, 0, -1, 2, 5, 2),
                ModelBakery.PartPose.offset(-6.25f, 7, 0));
        body.addOrReplaceChild("tentacle4",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-1, 0, -1, 2, 5, 2),
                ModelBakery.PartPose.offset(-1.25f, 7, 0));
        body.addOrReplaceChild("tentacle5",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-1, 0, -1, 2, 7, 2),
                ModelBakery.PartPose.offset(3.75f, 7, 0));
        body.addOrReplaceChild("tentacle6",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-1, 0, -1, 2, 8, 2),
                ModelBakery.PartPose.offset(-3.75f, 7, 5));
        body.addOrReplaceChild("tentacle7",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-1, 0, -1, 2, 8, 2),
                ModelBakery.PartPose.offset(1.25f, 7, 5));
        body.addOrReplaceChild("tentacle8",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-1, 0, -1, 2, 5, 2),
                ModelBakery.PartPose.offset(6.25f, 7, 5));
    }

    private ModelBakery.BakedPart getHarnessModel(HappyGhast entity) {
        String tex = HumanoidRenderer.resolveEquipmentTexture(entity.getItemBySlot(EquipmentSlot.BODY), "happy_ghast_body");
        if (tex == null) return null;
        boolean baby = entity.isBaby();
        String key = tex + (baby ? "_baby" : "");
        return harnessCache.computeIfAbsent(key, k -> buildHarnessModel(tex, baby));
    }

    private ModelBakery.BakedPart buildHarnessModel(String texture, boolean baby) {
        ModelBakery bakery = new ModelBakery(texture, 64, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("harness",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-8, -16, -8, 16, 16, 16, new ModelBakery.CubeDeformation(0.25f)),
                ModelBakery.PartPose.offset(0, 24, 0));

        root.addOrReplaceChild("goggles",
                ModelBakery.CubeListBuilder.create().texOffs(0, 32).addBox(-8, -2.5f, -2.5f, 16, 5, 5, new ModelBakery.CubeDeformation(0.35f)),
                ModelBakery.PartPose.offset(0, 14, -5.5f));

        return root.bake();
    }

    private ModelBakery.BakedPart getRopesModel(boolean baby) {
        if (baby) {
            if (cachedRopesBaby == null) cachedRopesBaby = buildRopesModel(true);
            return cachedRopesBaby;
        } else {
            if (cachedRopesAdult == null) cachedRopesAdult = buildRopesModel(false);
            return cachedRopesAdult;
        }
    }

    private ModelBakery.BakedPart buildRopesModel(boolean baby) {
        ModelBakery bakery = new ModelBakery(ROPES_TEXTURE, 64, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        var body = root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-8, -8, -8, 16, 16, 16, new ModelBakery.CubeDeformation(0.15f)),
                ModelBakery.PartPose.offset(0, 16, 0));

        if (baby) {
            body.addOrReplaceChild("inner_body",
                    ModelBakery.CubeListBuilder.create().texOffs(0, 32).addBox(-8, -16, -8, 16, 16, 16, new ModelBakery.CubeDeformation(-0.5f)),
                    ModelBakery.PartPose.offset(0, 8, 0));
        }

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, T entity) {
        var pos = entity.position();
        boolean baby = entity instanceof HappyGhast hg && hg.isBaby();
        double ageInTicks = entity.tickCount + 1.0f;
        double bodyScale = 1.0f;
        boolean hasHarness = entity instanceof HappyGhast hg && !hg.getItemBySlot(EquipmentSlot.BODY).isEmpty();
        boolean isRidden = entity instanceof HappyGhast hg && hg.isVehicle();

        if (entity instanceof HappyGhast hg && !hg.getItemBySlot(EquipmentSlot.BODY).isEmpty()) {
            bodyScale = 0.9375f;
        }

        Matrix4d transform = new Matrix4d()
                .translate(pos.x, pos.y, pos.z)
                .scale(4.0f);

        if (baby) {
            transform.scale(0.2375f);
        }

        if (bodyScale != 1.0f) {
            transform.scale(bodyScale, bodyScale, bodyScale);
        }

        transform.translate(0, 1.5f, 0);
        transform.rotateX(Mth.PI);

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        if (!entity.isInvisible()) {
            ModelBakery.BakedPart model = buildRoot(entity);
            renderParts(pipeline, model, transform, ageInTicks, block, sky);
        }

        if (hasHarness) {
            ModelBakery.BakedPart harness = null;
            if (!entity.getItemBySlot(EquipmentSlot.BODY).isEmpty()) {
                harness = getHarnessModel((HappyGhast) entity);
            }

            Matrix4d harnessTransform = new Matrix4d(transform);
            if (harness != null) {
                renderHarness(pipeline, harness, harnessTransform, isRidden, block, sky);
            }

            if (((HappyGhast) entity).isLeashHolder()) {
                ModelBakery.BakedPart ropes = getRopesModel(baby);
                renderParts(pipeline, ropes, harnessTransform, ageInTicks, block, sky);
            }
        }
    }

    private void renderParts(RenderPipeline pipeline, ModelBakery.BakedPart part, Matrix4d parent, double age, double block, double sky) {
        for (var child : part.children.entrySet()) {
            String name = child.getKey();
            ModelBakery.BakedPart childPart = child.getValue();

            Matrix4d mat = new Matrix4d(parent);
            mat.translate(childPart.localPivot.x, childPart.localPivot.y, childPart.localPivot.z);

            ModelBakery.PartPose ip = childPart.initialPose;
            if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
                mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
            if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
                mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

            if (name.startsWith("tentacle")) {
                int idx = Integer.parseInt(name.substring(8));
                mat.rotateX(0.2f * Mth.sin(age * 0.3f + idx) + 0.4f);
            }

            if (childPart.mesh != null) {
                pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(childPart.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
            }

            renderParts(pipeline, childPart, mat, age, block, sky);
        }
    }

    private void renderHarness(RenderPipeline pipeline, ModelBakery.BakedPart harness, Matrix4d parent, boolean isRidden, double block, double sky) {
        for (var child : harness.children.entrySet()) {
            String name = child.getKey();
            ModelBakery.BakedPart part = child.getValue();

            Matrix4d mat = new Matrix4d(parent);
            mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

            ModelBakery.PartPose ip = part.initialPose;
            if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
                mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
            if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
                mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

            if (name.equals("goggles")) {
                if (isRidden) {
                    mat.translate(0, 0, 0);
                } else {
                    mat.translate(0, (9 - 14) / 16f, 0);
                    mat.rotateX(-Mth.DEG_TO_RAD * 45);
                }
            }

            if (part.mesh != null) {
                pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
            }
        }
    }
}
