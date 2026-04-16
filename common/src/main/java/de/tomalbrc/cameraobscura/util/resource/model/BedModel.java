package de.tomalbrc.cameraobscura.util.resource.model;

import de.tomalbrc.cameraobscura.model.resource.RPModel;
import de.tomalbrc.cameraobscura.util.resource.RPHelper;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import org.joml.Vector3f;

import java.util.Optional;

public class BedModel {
    public static RPModel.View get(BlockState blockState, Optional<DyeColor> color) {
        RPModel rpModel = RPHelper.loadModel(ChestModel.class.getResourceAsStream(blockState.getValue(BedBlock.PART) == BedPart.HEAD ? "/builtin/bed_top.json" : "/builtin/bed_bottom.json"));

        color.ifPresentOrElse(
                dyeColor -> rpModel.textures.put("0", RPModel.TextureEntry.of("minecraft:entity/bed/" + dyeColor.getName())),
                () -> rpModel.textures.put("0", RPModel.TextureEntry.of("minecraft:entity/bed/red"))
        );

        return new RPModel.View(rpModel, new Vector3f(0, (blockState.getValue(BedBlock.FACING).toYRot() + 180) % 360, 0));
    }
}
