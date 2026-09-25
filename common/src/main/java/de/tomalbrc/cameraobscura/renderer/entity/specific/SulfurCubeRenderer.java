package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.BlockStateRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.cubemob.SulfurCube;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4d;

public class SulfurCubeRenderer implements LivingEntityRenderer<SulfurCube> {

    private static final String ADULT_OUTER_TEXTURE = "entity/sulfur_cube/sulfur_cube_outer";
    private static final String BABY_OUTER_TEXTURE = "entity/sulfur_cube/sulfur_cube_outer_small";
    private static final String ADULT_INNER_TEXTURE = "entity/sulfur_cube/sulfur_cube_inner";
    private static final String BABY_INNER_TEXTURE = "entity/sulfur_cube/sulfur_cube_inner_small";

    private static final int ADULT_ATLAS = 128;
    private static final int BABY_ATLAS = 64;

    private static final double BLOCK_FIT_RATIO = 0.8;

    private ModelBakery.BakedPart cachedAdultOuter;
    private ModelBakery.BakedPart cachedAdultInner;
    private ModelBakery.BakedPart cachedBabyOuter;
    private ModelBakery.BakedPart cachedBabyInner;

    @Override
    public ModelBakery.BakedPart buildRoot(SulfurCube entity) {
        if (entity.isBaby()) {
            if (cachedBabyOuter == null) cachedBabyOuter = buildCube(true, false);
            return cachedBabyOuter;
        } else {
            if (cachedAdultOuter == null) cachedAdultOuter = buildCube(false, false);
            return cachedAdultOuter;
        }
    }

    private ModelBakery.BakedPart getInner(SulfurCube entity) {
        if (entity.isBaby()) {
            if (cachedBabyInner == null) cachedBabyInner = buildCube(true, true);
            return cachedBabyInner;
        } else {
            if (cachedAdultInner == null) cachedAdultInner = buildCube(false, true);
            return cachedAdultInner;
        }
    }

    private ModelBakery.BakedPart buildCube(boolean baby, boolean inner) {
        String texture;
        if (inner) {
            texture = baby ? BABY_INNER_TEXTURE : ADULT_INNER_TEXTURE;
        } else {
            texture = baby ? BABY_OUTER_TEXTURE : ADULT_OUTER_TEXTURE;
        }
        int atlas = baby ? BABY_ATLAS : ADULT_ATLAS;
        float half = inner ? (baby ? 4.0f : 8.0f) : (baby ? 5.0f : 9.0f);
        int v = inner ? (baby ? 20 : 36) : 0;

        ModelBakery bakery = new ModelBakery(texture, atlas, atlas);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        root.addOrReplaceChild("cube", ModelBakery.CubeListBuilder.create().texOffs(0, v).addBox(-half, -half, -half, half * 2.0f, half * 2.0f, half * 2.0f), ModelBakery.PartPose.ZERO);

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, SulfurCube entity) {
        boolean baby = entity.isBaby();
        int size = entity.getSize();
        boolean invisible = entity.isInvisible();

        float outerHalfModel = baby ? 5.0f : 9.0f;
        float extraDownscale = baby ? 1.0f : 0.5f;
        float vOffset = baby ? 1.24f : 0.98f;

        ItemStack contained = entity.getBodyArmorItem();
        boolean hasBlock = isRenderableBlock(contained);

        double ss = hasBlock ? 0.0 : (Mth.lerp(1.0f, entity.oSquish, entity.squish) / (size * 0.5f + 1.0f));
        double w = 1.0 / (ss + 1.0);
        double scaleXZ = w * size;
        double scaleY = (1.0 / w) * size;

        float fuse = entity.isPrimed() ? (float) entity.getFuse() : 0.0f;
        float swell = (fuse < 10.0f && fuse > 0.0f) ? 1.0f + getSwellAmount(fuse) : 1.0f;

        float onePixelUp = invisible ? 0.0f : 1.0f / 16.0f;
        float K = 1.501f - vOffset + onePixelUp;

        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);

        Matrix4d base = new Matrix4d().translate(pos.x, pos.y, pos.z).scale(scaleXZ, scaleY, scaleXZ).scale(swell, swell, swell).scale(extraDownscale, extraDownscale, extraDownscale).rotateY(Mth.DEG_TO_RAD * (180f - bodyYaw)).translate(0.0f, K, 0.0f).rotateY(Mth.PI).rotateX(Mth.PI);

        var blockLight = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var skyLight = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;
        int tint = LivingEntityRenderer.hurtTint(entity);

        if (hasBlock) {
            double shellHalfWorld = (outerHalfModel / 16.0) * scaleY * swell * extraDownscale;
            double worldCenterY = pos.y + scaleY * swell * extraDownscale * K;
            double blockScale = shellHalfWorld * 2.0 * BLOCK_FIT_RATIO;

            BlockItem blockItem = (BlockItem) contained.getItem();
            BlockState state = contained.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY).apply(blockItem.getBlock().defaultBlockState());

            Matrix4d blockMat = new Matrix4d()
                    .translate(pos.x, worldCenterY, pos.z)
                    .rotateY(Mth.DEG_TO_RAD * (180f - bodyYaw))
                    .scale(blockScale, blockScale, blockScale)
                    .translate(0.0, -0.5, 0.0);

            BlockStateRenderer.render(pipeline, state, blockMat, blockLight, skyLight);
        } else if (!invisible) {
            renderPart(pipeline, getInner(entity), base, blockLight, skyLight, tint, RenderType.ENTITY);
        }

        renderPart(pipeline, buildRoot(entity), base, blockLight, skyLight, tint, RenderType.ENTITY);
    }

    private static boolean isRenderableBlock(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!(stack.getItem() instanceof BlockItem blockItem)) return false;
        return !(blockItem.getBlock() instanceof AbstractSkullBlock);
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part, Matrix4d parent, double block, double sky, int tint, RenderType type) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0) mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1) mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(type, new Model(part.mesh, block, sky), mat, IntList.of(tint)));
        }
        for (var child : part.children.entrySet()) {
            renderPart(pipeline, child.getValue(), mat, block, sky, tint, type);
        }
    }

    private static float getSwellAmount(float fuse) {
        float g = 1.0f - fuse / 10.0f;
        return g * g * 0.3f;
    }
}