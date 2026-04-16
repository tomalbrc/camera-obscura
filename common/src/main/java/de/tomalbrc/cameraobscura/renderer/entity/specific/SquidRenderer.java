package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public class SquidRenderer implements LivingEntityRenderer<Squid> {
    private static final String ADULT_TEXTURE = "entity/squid/squid";
    private static final String BABY_TEXTURE = "entity/squid/squid_baby";

    private static final String GLOW_ADULT_TEXTURE = "entity/squid/glow_squid";
    private static final String GLOW_BABY_TEXTURE = "entity/squid/glow_squid_baby";
    final boolean glow;
    private ModelBakery.BakedPart cachedAdultRoot;
    private ModelBakery.BakedPart cachedBabyRoot;

    public SquidRenderer(boolean glow) {
        this.glow = glow;
    }

    private ModelBakery.BakedPart buildAdultRoot() {
        ModelBakery bakery = new ModelBakery(glow ? GLOW_ADULT_TEXTURE : ADULT_TEXTURE, 64, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-6, -8, -6, 12, 16, 12),
                ModelBakery.PartPose.offset(0, 8, 0));

        for (int i = 0; i < 8; i++) {
            float angle = i * Mth.PI * 2.0f / 8.0f;
            float x = Mth.cos(angle) * 5.0F;
            float z = Mth.sin(angle) * 5.0F;
            angle = i * Mth.PI * -2.0f / 8.0f + (Mth.PI / 2);
            float yRot = angle;

            root.addOrReplaceChild("tentacle" + i,
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(48, 0).addBox(-1, 0, -1, 2, 18, 2),
                    ModelBakery.PartPose.offsetAndRotation(x, 15, z, 0, yRot, 0));
        }

        return root.bake();
    }

    private ModelBakery.BakedPart buildBabyRoot() {
        ModelBakery bakery = new ModelBakery(glow ? GLOW_BABY_TEXTURE : BABY_TEXTURE, 32, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4, -5, -4, 8, 10, 8),
                ModelBakery.PartPose.offset(0, 13, 0));

        for (int i = 0; i < 8; i++) {
            float angle = i * Mth.PI * 2.0f / 8.0f;
            float x = Mth.cos(angle) * 3.0F;
            float z = Mth.sin(angle) * 3.0F;
            angle = i * Mth.PI * -2.0f / 8.0f + (Mth.PI / 2);
            float yRot = angle;

            root.addOrReplaceChild("tentacle" + i,
                    ModelBakery.CubeListBuilder.create()
                            .texOffs(0, 18).addBox(-1, -0.5f, -1, 2, 6, 2),
                    ModelBakery.PartPose.offsetAndRotation(x, 18.5f, z, 0, yRot, 0));
        }

        return root.bake();
    }

    @Override
    public ModelBakery.BakedPart buildRoot(Squid entity) {
        if (entity.isBaby()) {
            if (cachedBabyRoot == null) cachedBabyRoot = buildBabyRoot();
            return cachedBabyRoot;
        } else {
            if (cachedAdultRoot == null) cachedAdultRoot = buildAdultRoot();
            return cachedAdultRoot;
        }
    }

    @Override
    public void render(RenderPipeline pipeline, Squid entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);

        double yOffset = entity.isBaby() ? 0.25f : 0.5f;
        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + yOffset, pos.z)
                .rotateY(Mth.DEG_TO_RAD * (180f - bodyYaw))
                .rotateX(Mth.DEG_TO_RAD * Mth.lerp(1.0f, entity.xBodyRotO, entity.xBodyRot))
                .rotateY(Mth.DEG_TO_RAD * Mth.lerp(1.0f, entity.zBodyRotO, entity.zBodyRot))

                .rotateY(Mth.PI)
                .rotateX(Mth.PI)

                .translate(0, entity.isBaby() ? -0.6f : -1.2f, 0);

        double tentacleAngle = Mth.lerp(1.0f, entity.oldTentacleAngle, entity.tentacleAngle);

        var block = glow ? 1 : entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = glow ? 1 : entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderPart(pipeline, buildRoot(entity), "root", base, tentacleAngle, block, sky);
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part,
                            String name, Matrix4d parent, double tentacleAngle,
                            double block, double sky) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        var ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        if (name.startsWith("tentacle")) {
            mat.rotateX(tentacleAngle);
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }

        for (var child : part.children.entrySet()) {
            renderPart(pipeline, child.getValue(), child.getKey(), mat, tentacleAngle, block, sky);
        }
    }
}