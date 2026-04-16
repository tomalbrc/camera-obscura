package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.EntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.arrow.SpectralArrow;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

import java.util.HashMap;
import java.util.Map;

public class ArrowRenderer<T extends AbstractArrow> implements EntityRenderer<T> {

    private static final Map<Class<?>, String> TEXTURES = new HashMap<>();

    static {
        TEXTURES.put(Arrow.class, "entity/projectiles/arrow");
        TEXTURES.put(SpectralArrow.class, "entity/projectiles/arrow_spectral");
    }

    private ModelBakery.BakedPart cachedModel;
    private String cachedTexture;

    @Override
    public void render(RenderPipeline pipeline, T entity) {
        String texture = TEXTURES.get(entity.getClass());
        if (texture == null) texture = "entity/projectiles/arrow";

        if (cachedModel == null || !texture.equals(cachedTexture)) {
            cachedTexture = texture;
            cachedModel = buildModel(texture);
        }

        double yRot = entity.getYRot(1.0f);
        double xRot = entity.getXRot(1.0f);
        double shake = entity.shakeTime - 1.0f;
        if (shake < 0) shake = 0;

        Matrix4d transform = new Matrix4d()
                .translate(entity.position().toVector3f())
                .rotateY(Mth.DEG_TO_RAD * (yRot - 90.0f))
                .rotateX(Mth.DEG_TO_RAD * xRot);

        if (shake > 0.0f) {
            double pow = -Mth.sin(shake * 3.0f) * shake;
            transform.rotateZ(pow * Mth.DEG_TO_RAD);
        }

        transform.scale(0.9f);

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        for (var entry : cachedModel.children.entrySet()) {
            renderPart(pipeline, entry.getValue(), transform, block, sky);
        }
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part, Matrix4d parent, double block, double sky) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose pose = part.initialPose;
        if (pose.xRot() != 0 || pose.yRot() != 0 || pose.zRot() != 0) {
            mat.rotateZYX(pose.zRot(), pose.yRot(), pose.xRot());
        }
        if (pose.xScale() != 1 || pose.yScale() != 1 || pose.zScale() != 1) {
            mat.scale(pose.xScale(), pose.yScale(), pose.zScale());
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }
    }

    private ModelBakery.BakedPart buildModel(String texture) {
        ModelBakery bakery = new ModelBakery(texture, 32, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("back",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(0.0f, -2.5f, -2.5f, 0.0f, 5.0f, 5.0f),
                new ModelBakery.PartPose(-11.0f, 0.0f, 0.0f,
                        Mth.PI / 4, 0.0f, 0.0f,
                        0.8f, 0.8f, 0.8f));

        ModelBakery.CubeListBuilder crossBuilder = ModelBakery.CubeListBuilder.create()
                .texOffs(0, 0).addBox(-12.0f, -2.0f, 0.0f, 16.0f, 4.0f, 0.001f);

        root.addOrReplaceChild("cross_1", crossBuilder,
                new ModelBakery.PartPose(0.0f, 0.0f, 0.0f,
                        Mth.PI / 4, 0.0f, 0.0f,
                        1.0f, 1.0f, 1.0f));

        root.addOrReplaceChild("cross_2", crossBuilder,
                new ModelBakery.PartPose(0.0f, 0.0f, 0.0f,
                        Mth.PI * 3.0f / 4.0f, 0.0f, 0.0f,
                        1.0f, 1.0f, 1.0f));

        return root.bake();
    }
}