package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.EntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import org.joml.Matrix4d;
import org.joml.Quaterniond;

public class EndCrystalRenderer implements EntityRenderer<EndCrystal> {
    private static final String TEXTURE = "entity/end_crystal/end_crystal";
    private static final double SIN_45 = Math.sin(Math.PI / 4);
    private ModelBakery.BakedPart cachedModel;

    @Override
    public void render(RenderPipeline pipeline, EndCrystal entity) {
        if (cachedModel == null) {
            cachedModel = buildModel();
        }

        double ageInTicks = entity.time + 1.0f; // partialTicks=1
        boolean showsBottom = entity.showsBottom();

        double hh = Mth.sin(ageInTicks * 0.2f) / 2.0f + 0.5f;
        hh = (hh * hh + hh) * 0.4f;
        double crystalY = (hh - 1.4f) * 16.0f;

        double animationSpeed = ageInTicks * 3.0f;

        Matrix4d base = new Matrix4d()
                .translate(entity.position().toVector3f())
                .scale(2.0f)
                .translate(0.0f, -0.5f, 0.0f);

        renderPart(pipeline, cachedModel, "root", base, showsBottom, crystalY, animationSpeed);
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                            Matrix4d parent, boolean showsBottom,
                            double crystalY, double animationSpeed) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        switch (name) {
            case "base" -> {
                if (!showsBottom) return;
            }
            case "outer_glass" -> {
                mat.translate(0.0f, crystalY / 2.0f / 16.0f, 0.0f);
                mat.rotate(new Quaterniond()
                        .rotateY(animationSpeed * Mth.DEG_TO_RAD)
                        .rotateAxis((Math.PI / 3), SIN_45, 0.0f, SIN_45));
            }
            case "inner_glass", "cube" -> {
                mat.rotate(new Quaterniond()
                        .setAngleAxis((Math.PI / 3), SIN_45, 0.0f, SIN_45)
                        .rotateY(animationSpeed * Mth.DEG_TO_RAD));
            }
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh), mat, IntList.of(0xFFFFFFFF)));
        }

        for (var child : part.children.entrySet()) {
            renderPart(pipeline, child.getValue(), child.getKey(), mat,
                    showsBottom, crystalY, animationSpeed);
        }
    }

    private ModelBakery.BakedPart buildModel() {
        ModelBakery bakery = new ModelBakery(TEXTURE, 64, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("base",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-6.0f, 0.0f, -6.0f, 12.0f, 4.0f, 12.0f),
                ModelBakery.PartPose.ZERO);

        ModelBakery.CubeListBuilder glassCube = ModelBakery.CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f);
        ModelBakery.PartDefinition outerGlass = root.addOrReplaceChild("outer_glass", glassCube,
                ModelBakery.PartPose.offset(0.0f, 24.0f, 0.0f));

        ModelBakery.PartDefinition innerGlass = outerGlass.addOrReplaceChild("inner_glass", glassCube,
                new ModelBakery.PartPose(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                        0.875f, 0.875f, 0.875f));

        innerGlass.addOrReplaceChild("cube",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f),
                new ModelBakery.PartPose(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                        0.765625f, 0.765625f, 0.765625f));

        return root.bake();
    }
}