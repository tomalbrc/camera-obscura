package de.tomalbrc.cameraobscura.util.resource.model;

import de.tomalbrc.cameraobscura.model.resource.RPModel;
import de.tomalbrc.cameraobscura.util.resource.RPHelper;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.joml.Vector3f;

public class ChestModel {
    private static String chestTexture(BlockState blockState) {
        if (blockState.is(Blocks.ENDER_CHEST)) {
            return "ender";
        }

        String prefix = "normal";
        if (blockState.is(Blocks.TRAPPED_CHEST)) prefix = "trapped";

        if (blockState.getBlock() instanceof WeatheringCopperChestBlock copperChestBlock)
            prefix = copperChestBlock.getAge() == WeatheringCopper.WeatherState.UNAFFECTED ? "copper" : "copper_" + copperChestBlock.getAge().getSerializedName();
        else if (blockState.getBlock() instanceof CopperChestBlock)
            prefix = "copper";

        if (blockState.hasProperty(ChestBlock.TYPE)) {
            return blockState.getValue(ChestBlock.TYPE) == ChestType.LEFT ? prefix + "_left" : blockState.getValue(ChestBlock.TYPE) == ChestType.RIGHT ? prefix + "_right" : prefix;
        }

        return prefix;
    }

    public static RPModel.View get(BlockState chestBlockState) {
        RPModel rpModel;
        if (!chestBlockState.is(Blocks.ENDER_CHEST) && chestBlockState.getValue(ChestBlock.TYPE) == ChestType.RIGHT)
            rpModel = RPHelper.loadModel(ChestModel.class.getResourceAsStream("/builtin/chest_right.json"));
        else if (!chestBlockState.is(Blocks.ENDER_CHEST) && chestBlockState.getValue(ChestBlock.TYPE) == ChestType.LEFT)
            rpModel = RPHelper.loadModel(ChestModel.class.getResourceAsStream("/builtin/chest_left.json"));
        else
            rpModel = RPHelper.loadModel(ChestModel.class.getResourceAsStream("/builtin/chest.json"));

        rpModel.textures.put("0", RPModel.TextureEntry.of("minecraft:entity/chest/" + chestTexture(chestBlockState)));

        return new RPModel.View(rpModel, new Vector3f(0, (chestBlockState.getValue(ChestBlock.FACING).toYRot() + 180) % 360, 0));
    }
}
