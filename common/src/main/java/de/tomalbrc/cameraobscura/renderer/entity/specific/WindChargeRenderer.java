package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.EntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.AbstractWindCharge;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public class WindChargeRenderer implements EntityRenderer<AbstractWindCharge> {

    private static final String TEXTURE = "entity/projectiles/wind_charge";
    private ModelBakery.BakedPart cachedModel;

    @Override
    public void render(RenderPipeline pipeline, AbstractWindCharge entity) {
        if (cachedModel == null) {
            cachedModel = buildModel();
        }

        double ageInTicks = entity.tickCount + 1.0f;  // partialTicks=1
        double windYRot = ageInTicks * 16.0f * Mth.DEG_TO_RAD;
        double windChargeYRot = -ageInTicks * 16.0f * Mth.DEG_TO_RAD;

        Matrix4d transform = new Matrix4d().translate(entity.position().toVector3f());


        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderWindPart(pipeline, cachedModel, "bone", transform, windYRot, windChargeYRot, block, sky);
    }

    private void renderWindPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                                Matrix4d parent, double windYRot, double windChargeYRot,
                                double block, double sky) {

        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        if (name.equals("wind")) {
            mat.rotateY(windYRot);
        } else if (name.equals("wind_charge")) {
            mat.rotateY(windChargeYRot);
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }

        for (var child : part.children.entrySet()) {
            renderWindPart(pipeline, child.getValue(), child.getKey(), mat, windYRot, windChargeYRot, block, sky);
        }
    }

    private ModelBakery.BakedPart buildModel() {
        ModelBakery bakery = new ModelBakery(TEXTURE, 64, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition bone = root.addOrReplaceChild("bone",
                ModelBakery.CubeListBuilder.create(),
                ModelBakery.PartPose.offset(0, 0, 0));

        bone.addOrReplaceChild("wind",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(15, 20).addBox(-4, -1, -4, 8, 2, 8)
                        .texOffs(0, 9).addBox(-3, -2, -3, 6, 4, 6),
                ModelBakery.PartPose.offsetAndRotation(0, 0, 0, 0, -0.7854f, 0));

        bone.addOrReplaceChild("wind_charge",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-2, -2, -2, 4, 4, 4),
                ModelBakery.PartPose.offset(0, 0, 0));

        return root.bake();
    }
}
