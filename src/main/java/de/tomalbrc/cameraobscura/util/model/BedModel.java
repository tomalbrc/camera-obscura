package de.tomalbrc.cameraobscura.util.model;

import de.tomalbrc.cameraobscura.render.model.resource.RPElement;
import de.tomalbrc.cameraobscura.render.model.resource.RPModel;
import de.tomalbrc.cameraobscura.util.RPHelper;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import org.joml.Vector3f;

import java.util.Map;
import java.util.Optional;

public class BedModel {

    public static RPModel.View get(BlockState blockState, Optional<DyeColor> color) {
        RPModel rpModel = RPHelper.loadModelView(ChestModel.class.getResourceAsStream(blockState.getValue(BedBlock.PART) == BedPart.HEAD ? "/builtin/bed_top.json" : "/builtin/bed_bottom.json"));

        color.ifPresentOrElse(
                dyeColor -> rpModel.textures.put("0", "minecraft:entity/bed/" + dyeColor.getName()),
                () -> rpModel.textures.put("0", "minecraft:entity/bed/red")
        );

        for (RPElement element : rpModel.elements) {
            for (Map.Entry<String, RPElement.TextureInfo> entry : element.faces.entrySet()) {
                entry.getValue().uv.mul(4.f);
            }
        }

        return new RPModel.View(rpModel, new Vector3f(0, (blockState.getValue(BedBlock.FACING).toYRot() + 180) % 360, 0));
    }
}
