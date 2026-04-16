package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.BlockStateRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.EntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public class MinecartRenderer<T extends AbstractMinecart> implements EntityRenderer<T> {
    private static final String TEXTURE = "entity/minecart/minecart";
    private ModelBakery.BakedPart cachedModel;

    @Override
    public void render(RenderPipeline pipeline, T entity) {
        if (cachedModel == null) {
            cachedModel = buildModel();
        }

        double yRot = 180f - entity.getYRot(1.0f);
        double xRot = entity.getXRot(1.0f);
        double hurtTime = entity.getHurtTime() - 1.0f; // partialTick=1
        double damageTime = Math.max(entity.getDamage() - 1.0f, 0.0f);
        double hurtDir = entity.getHurtDir();

        long seed = entity.getId() * 493286711L;
        seed = seed * seed * 4392167121L + seed * 98761L;
        double offsetX = (((seed >> 16 & 7L) + 0.5f) / 8.0f - 0.5f) * 0.004f;
        double offsetY = (((seed >> 20 & 7L) + 0.5f) / 8.0f - 0.5f) * 0.004f;
        double offsetZ = (((seed >> 24 & 7L) + 0.5f) / 8.0f - 0.5f) * 0.004f;

        Matrix4d base = new Matrix4d()
                .translate(entity.position().toVector3f())
                .translate(offsetX, offsetY, offsetZ)
                .rotateY(Mth.DEG_TO_RAD * yRot)
                .rotateX(Mth.DEG_TO_RAD * -xRot)
                .translate(0.0f, 0.375f, 0.0f);

        if (hurtTime > 0.0f) {
            double hurtRot = Mth.sin(hurtTime) * hurtTime * damageTime / 10.0f * hurtDir;
            base.rotateX(hurtRot * Mth.DEG_TO_RAD);
        }

        Matrix4d cartMat = new Matrix4d(base);
        cartMat.scale(-1.0f, -1.0f, 1.0f);

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderPart(pipeline, cachedModel, cartMat, block, sky);

        renderDisplayBlock(pipeline, entity, base, block, sky);
    }

    protected void renderDisplayBlock(RenderPipeline pipeline, T entity, Matrix4d parent, double block, double sky) {
        var blockState = entity.getDisplayBlockState();
        if (blockState.isAir()) return;

        double displayOffset = entity.getDisplayOffset();

        Matrix4d blockMat = new Matrix4d(parent);
        blockMat.scale(0.75f);
        blockMat.translate(0, (displayOffset - 8) / 16.0f, 0);
        blockMat.rotateY(Mth.DEG_TO_RAD * 90.0f);

        BlockStateRenderer.render(pipeline, blockState, blockMat, block, sky);
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part, Matrix4d parent, double block, double sky) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        var ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }
        for (var child : part.children.entrySet()) {
            renderPart(pipeline, child.getValue(), mat, block, sky);
        }
    }

    private ModelBakery.BakedPart buildModel() {
        ModelBakery bakery = new ModelBakery(TEXTURE, 64, 32);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("bottom",
                ModelBakery.CubeListBuilder.create().texOffs(0, 10).addBox(-10, -8, -1, 20, 16, 2),
                ModelBakery.PartPose.offsetAndRotation(0, 4, 0, Mth.PI / 2, 0, 0));
        root.addOrReplaceChild("front",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-8, -9, -1, 16, 8, 2),
                ModelBakery.PartPose.offsetAndRotation(-9, 4, 0, 0, Mth.PI * 3.0f / 2.0f, 0));
        root.addOrReplaceChild("back",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-8, -9, -1, 16, 8, 2),
                ModelBakery.PartPose.offsetAndRotation(9, 4, 0, 0, Mth.PI / 2, 0));
        root.addOrReplaceChild("left",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-8, -9, -1, 16, 8, 2),
                ModelBakery.PartPose.offsetAndRotation(0, 4, -7, 0, Mth.PI, 0));
        root.addOrReplaceChild("right",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-8, -9, -1, 16, 8, 2),
                ModelBakery.PartPose.offset(0, 4, 7));

        return root.bake();
    }
}