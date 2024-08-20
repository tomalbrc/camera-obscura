package de.tomalbrc.cameraobscura.util.model;

import de.tomalbrc.cameraobscura.render.model.resource.RPElement;
import de.tomalbrc.cameraobscura.render.model.resource.RPModel;
import de.tomalbrc.cameraobscura.util.RPHelper;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import org.joml.Vector3f;

import java.util.Map;

public class SignModel {
    public static RPModel.View get(BlockState blockState) {
        RPModel rpModel;
        if (blockState.is(BlockTags.SIGNS))
            rpModel = RPHelper.loadModel(SignModel.class.getResourceAsStream("/builtin/sign.json"));
        else
            rpModel = RPHelper.loadModel(SignModel.class.getResourceAsStream("/builtin/hanging_sign.json"));

        //rpModel.textures.put("0", blockState.is(BlockTags.SIGNS) ? "minecraft:entity/chest/"+chestTexture(blockState));
        for (RPElement element : rpModel.elements) {
            for (Map.Entry<String, RPElement.TextureInfo> entry : element.faces.entrySet()) {
                entry.getValue().uv.mul(4,2,4,2);
            }
        }

        var rot = blockState.hasProperty(StandingSignBlock.ROTATION) ? (RotationSegment.convertToDegrees(blockState.getValue(StandingSignBlock.ROTATION))+180)%360 :
                blockState.hasProperty(HorizontalDirectionalBlock.FACING) ? (blockState.getValue(HorizontalDirectionalBlock.FACING).toYRot()+180)%360 :
                        0;
        return new RPModel.View(rpModel, new Vector3f(0, rot,0), new Vector3f(0,0.7f,0));
    }
}
