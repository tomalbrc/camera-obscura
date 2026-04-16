package de.tomalbrc.cameraobscura.renderer.entity.specific;

import com.mojang.math.Axis;
import de.tomalbrc.cameraobscura.model.resource.RPModel;
import de.tomalbrc.cameraobscura.model.triangle.ModelTesselator;
import de.tomalbrc.cameraobscura.renderer.entity.EntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ItemStackRenderer;
import de.tomalbrc.cameraobscura.sore.model.Mesh;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import de.tomalbrc.cameraobscura.util.Constants;
import de.tomalbrc.cameraobscura.util.resource.model.GeneratedItemModel;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;

public class ItemFrameRenderer implements EntityRenderer<ItemFrame> {
    public static Int2ObjectOpenHashMap<Mesh> MAP_MODELS = new Int2ObjectOpenHashMap<>();

    @Override
    public void render(RenderPipeline pipeline, ItemFrame entity) {
        var transform = new Matrix4d().translate(entity.position().toVector3f());
        var item = entity.getItem();

        Direction direction = entity.getDirection();

        double offs = 0.46875F;
        transform.translate(direction.getStepX() * offs, direction.getStepY() * offs, direction.getStepZ() * offs);

        double xRot;
        double yRot;
        if (direction.getAxis().isHorizontal()) {
            xRot = 0.0F;
            yRot = 180.0F - direction.toYRot();
        } else {
            xRot = (-90 * direction.getAxisDirection().getStep());
            yRot = 180.0F;
        }

        transform.rotate(Axis.XP.rotationDegrees((float) xRot));
        transform.rotate(Axis.YP.rotationDegrees((float) yRot));

        var itemMatrix = new Matrix4d(transform);

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        if (!entity.isInvisible()) {
            transform.translate(-0.5f, -0.5f, -0.5f);
        }

        if (entity.isInvisible()) {
            itemMatrix.translate(0, 0, 0.5f);
        } else {
            itemMatrix.translate(0, 0, 0.4375f);
        }

        boolean glow = entity.getType() == EntityType.GLOW_ITEM_FRAME;
        if (entity.hasFramedMap()) {
            var mapId = entity.getFramedMapId(item);
            if (mapId != null) {
                var m = MAP_MODELS.computeIfAbsent(mapId.id(), id -> {
                    RPModel model = GeneratedItemModel.getItem(Constants.DYNAMIC_MAP_TEXTURE + ":" + mapId.id(), null);
                    return new ModelTesselator(new RPModel.View(model)).build();
                });

                //RPModel model = GeneratedItemModel.getItem(Constants.DYNAMIC_MAP_TEXTURE + ":" + mapId.id(), null);
                //var mesh = new MeshBuilder(new RPModel.View(model), EnumSet.noneOf(Direction.class)).toRasterModel();
                //var m = new Model(mesh);

                transform.translate(0.0F, 0.0F, 0.49F);
                pipeline.draw(new DrawCommand(RenderType.ITEM, new Model(m, glow ? 1 : block, glow ? 1 : sky), transform));
            }
            return;

        } else if (!item.isEmpty()) {
            itemMatrix.rotate(Axis.ZP.rotationDegrees(entity.getRotation() * 360.0f / 8.0f));
            itemMatrix.scale(0.5f, 0.5f, 0.5f);

            ItemStackRenderer.render(pipeline, item, ItemDisplayContext.FIXED, itemMatrix, glow ? 1 : 0, glow ? 1 : 0);
        }

        renderDefault(pipeline, transform, entity, glow ? 1 : block, glow ? 1 : sky);
    }
}
