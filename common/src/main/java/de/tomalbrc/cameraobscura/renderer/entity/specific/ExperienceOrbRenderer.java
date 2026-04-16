package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.EntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import org.joml.Matrix4d;
import org.joml.Matrix4fc;

public class ExperienceOrbRenderer implements EntityRenderer<ExperienceOrb> {
    private static final String TEXTURE = "entity/experience/experience_orb";
    private static final int ICON_COUNT = 16;
    private final ModelBakery.BakedPart[] ICON_MODELS = new ModelBakery.BakedPart[ICON_COUNT];

    private ModelBakery.BakedPart getIconModel(int icon) {
        if (ICON_MODELS[icon] == null) {
            ICON_MODELS[icon] = buildIconModel(icon);
        }
        return ICON_MODELS[icon];
    }

    private ModelBakery.BakedPart buildIconModel(int icon) {
        int iconX = icon % 4;
        int iconY = icon / 4;
        ModelBakery bakery = new ModelBakery(TEXTURE, 64, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("orb",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(iconX * 16, iconY * 16)
                        .addBox(0, 0, 0, 16, 16, 0),
                ModelBakery.PartPose.ZERO);

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, ExperienceOrb entity) {
        int icon = entity.getIcon();
        ModelBakery.BakedPart orbModel = getIconModel(icon);

        Matrix4fc viewMatrix = pipeline.getCamera().getViewMatrix();
        Matrix4d viewRot = new Matrix4d(viewMatrix);
        viewRot.setTranslation(0, 0, 0);
        Matrix4d cameraWorldRot = new Matrix4d(viewRot).invert();

        double ageInTicks = entity.tickCount + 1.0f;
        double rr = ageInTicks / 2.0f;
        int rc = (int) ((Mth.sin(rr + 0.0f) + 1.0f) * 0.5f * 255.0f);
        int bc = (int) ((Mth.sin(rr + (Math.PI * 4.0 / 3.0)) + 1.0f) * 0.1f * 255.0f);
        int green = 255;
        int alpha = 255;
        int tint = (alpha << 24) | (rc << 16) | (green << 8) | bc;

        Matrix4d transform = new Matrix4d()
                .translate(entity.position().x(), (entity.position().y()), entity.position().z())
                .scale(0.3f)
                .mul(cameraWorldRot);

        renderPart(pipeline, orbModel, "root", transform, tint);
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name, Matrix4d parent, int tint) {
        Matrix4d mat = new Matrix4d(parent);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh), mat, IntList.of(tint)));
        }

        for (var child : part.children.entrySet()) {
            renderPart(pipeline, child.getValue(), child.getKey(), mat, tint);
        }
    }
}