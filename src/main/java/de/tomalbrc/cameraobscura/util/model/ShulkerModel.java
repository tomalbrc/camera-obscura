package de.tomalbrc.cameraobscura.util.model;

import de.tomalbrc.cameraobscura.render.model.resource.RPElement;
import de.tomalbrc.cameraobscura.render.model.resource.RPModel;
import de.tomalbrc.cameraobscura.util.RPHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import java.util.Map;
import java.util.Optional;

public class ShulkerModel {
    public static RPModel.View get(BlockState blockState, Optional<DyeColor> dyeColor) {
        RPModel rpModel = RPHelper.loadModel(ChestModel.class.getResourceAsStream("/builtin/shulker.json"));

        dyeColor.ifPresentOrElse(
                color -> rpModel.textures.put("0", "minecraft:entity/shulker/shulker_" + color.getName()),
                () -> rpModel.textures.put("0", "minecraft:entity/shulker/shulker"));

        for (RPElement element : rpModel.elements) {
            for (Map.Entry<String, RPElement.TextureInfo> entry : element.faces.entrySet()) {
                entry.getValue().uv.mul(4.f);
            }
        }

        var rot = blockState.getValue(ShulkerBoxBlock.FACING).getRotation();
        return new RPModel.View(rpModel, rot.getEulerAnglesZXY(new Vector3f()).mul(-Mth.RAD_TO_DEG));
    }
}
