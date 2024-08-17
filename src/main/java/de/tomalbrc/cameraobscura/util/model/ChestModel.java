package de.tomalbrc.cameraobscura.util.model;

import de.tomalbrc.cameraobscura.render.model.resource.RPElement;
import de.tomalbrc.cameraobscura.render.model.resource.RPModel;
import de.tomalbrc.cameraobscura.util.RPHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.joml.Vector3f;

import java.util.Map;

public class ChestModel {
    private static String chestTexture(BlockState blockState) {
        if (blockState.is(Blocks.CHEST)) {
            return blockState.getValue(ChestBlock.TYPE) == ChestType.LEFT ?
                    "normal_left" :
                    blockState.getValue(ChestBlock.TYPE) == ChestType.RIGHT ?
                            "normal_right" : "normal";
        }

        return blockState.is(Blocks.ENDER_CHEST) ? "ender" : "normal";
    }

    public static RPModel.View get(BlockState chestBlockState) {
        RPModel rpModel;
        if (chestBlockState.getValue(ChestBlock.TYPE) == ChestType.RIGHT)
            rpModel = RPHelper.loadModel(ChestModel.class.getResourceAsStream("/builtin/chest_right.json"));
        else if (chestBlockState.getValue(ChestBlock.TYPE) == ChestType.LEFT)
            rpModel = RPHelper.loadModel(ChestModel.class.getResourceAsStream("/builtin/chest_left.json"));
        else
            rpModel = RPHelper.loadModel(ChestModel.class.getResourceAsStream("/builtin/chest.json"));

        rpModel.textures.put("0", "minecraft:entity/chest/"+chestTexture(chestBlockState));
        for (RPElement element : rpModel.elements) {
            for (Map.Entry<String, RPElement.TextureInfo> entry : element.faces.entrySet()) {
                entry.getValue().uv.mul(4.f);
            }
        }

        return new RPModel.View(rpModel, new Vector3f(0, (chestBlockState.getValue(ChestBlock.FACING).toYRot()+180)%360,0));
    }
}
