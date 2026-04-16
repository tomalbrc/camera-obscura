package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.EntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball;
import org.joml.Matrix4d;
import org.joml.Matrix4fc;

public class DragonFireballRenderer implements EntityRenderer<DragonFireball> {
    private static final String TEXTURE = "entity/enderdragon/dragon_fireball";
    private ModelBakery.BakedPart cachedQuad;

    @Override
    public void render(RenderPipeline pipeline, DragonFireball entity) {
        if (cachedQuad == null) {
            cachedQuad = buildQuad();
        }

        Matrix4fc viewMatrix = pipeline.getCamera().getViewMatrix();
        Matrix4d viewRot = new Matrix4d(viewMatrix);
        viewRot.setTranslation(0, 0, 0);

        Matrix4d cameraWorldRot = new Matrix4d(viewRot).invert();

        Matrix4d transform = new Matrix4d()
                .translate(entity.position().toVector3f())
                .scale(2.0f)
                .mul(cameraWorldRot);

        renderPart(pipeline, cachedQuad, transform);
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part, Matrix4d parent) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh), mat, IntList.of(0xFFFFFFFF)));
        }
        for (var child : part.children.entrySet()) {
            renderPart(pipeline, child.getValue(), mat);
        }
    }

    private ModelBakery.BakedPart buildQuad() {
        ModelBakery bakery = new ModelBakery(TEXTURE, 1, 1);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("fireball",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-0.5f, -0.25f, 0.0f, 1.0f, 0.5f, 0.0f),
                ModelBakery.PartPose.ZERO);

        return root.bake();
    }
}