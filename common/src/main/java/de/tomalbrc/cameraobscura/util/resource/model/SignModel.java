package de.tomalbrc.cameraobscura.util.resource.model;

import de.tomalbrc.cameraobscura.model.resource.RPModel;
import de.tomalbrc.cameraobscura.util.resource.RPHelper;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import org.joml.Vector3f;

public class SignModel {
    public static RPModel.View get(BlockState blockState) {
        RPModel rpModel;
        if (blockState.is(BlockTags.SIGNS))
            rpModel = RPHelper.loadModel(SignModel.class.getResourceAsStream("/builtin/sign.json"));
        else
            rpModel = RPHelper.loadModel(SignModel.class.getResourceAsStream("/builtin/hanging_sign.json"));

        var rot = blockState.hasProperty(StandingSignBlock.ROTATION) ? (RotationSegment.convertToDegrees(blockState.getValue(StandingSignBlock.ROTATION)) + 180) % 360 :
                blockState.hasProperty(HorizontalDirectionalBlock.FACING) ? (blockState.getValue(HorizontalDirectionalBlock.FACING).toYRot() + 180) % 360 : 0;
        return new RPModel.View(rpModel, new Vector3f(0, rot, 0), new Vector3f(0, 0.0f, 0));
    }
}
