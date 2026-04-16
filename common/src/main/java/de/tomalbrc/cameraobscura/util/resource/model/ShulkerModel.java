package de.tomalbrc.cameraobscura.util.resource.model;

import de.tomalbrc.cameraobscura.model.resource.RPModel;
import de.tomalbrc.cameraobscura.util.resource.RPHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import java.util.Optional;

public class ShulkerModel {
    public static RPModel.View get(BlockState blockState, Optional<DyeColor> dyeColor) {
        RPModel rpModel = RPHelper.loadModel(ChestModel.class.getResourceAsStream("/builtin/shulker.json"));

        dyeColor.ifPresentOrElse(
                color -> rpModel.textures.put("0", RPModel.TextureEntry.of("minecraft:entity/shulker/shulker_" + color.getName())),
                () -> rpModel.textures.put("0", RPModel.TextureEntry.of("minecraft:entity/shulker/shulker")));

        var rot = blockState.getValue(ShulkerBoxBlock.FACING).getRotation();
        return new RPModel.View(rpModel, rot.getEulerAnglesZXY(new Vector3f()).mul(-Mth.RAD_TO_DEG));
    }
}
