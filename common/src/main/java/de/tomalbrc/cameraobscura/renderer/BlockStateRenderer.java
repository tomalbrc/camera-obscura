package de.tomalbrc.cameraobscura.renderer;

import de.tomalbrc.cameraobscura.model.resource.RPModel;
import de.tomalbrc.cameraobscura.model.triangle.ModelTesselator;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import de.tomalbrc.cameraobscura.util.resource.RPHelper;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4d;
import org.joml.Matrix4dc;

import java.util.ArrayList;
import java.util.List;

public class BlockStateRenderer {
    public static void render(RenderPipeline pipeline, BlockState state, Matrix4dc transform, double block, double sky) {
        List<DrawCommand> commands = new ArrayList<>();
        renderDeferred(commands, state, transform, block, sky);
        for (DrawCommand cmd : commands) {
            pipeline.draw(cmd);
        }
    }

    public static void renderDeferred(List<DrawCommand> commands, BlockState state, Matrix4dc transform, double block, double sky) {
        List<RPModel.View> views = getModels(state);
        if (views == null || views.isEmpty()) return;

        for (RPModel.View view : views) {
            ModelTesselator tri = new ModelTesselator(view);
            var mesh = tri.build();
            if (mesh != null) {
                Model model = new Model(mesh, block, sky);
                commands.add(new DrawCommand(model.mesh.translucent ? RenderType.TRANSLUCENT : RenderType.SOLID, model, new Matrix4d(transform).translate(-0.5f, 0.f, -0.5f), IntList.of(0xFFFFFFFF)));
            }
        }
    }

    private static List<RPModel.View> getModels(BlockState state) {
        // todo: block entities
        return RPHelper.loadBlockModelViews(state);
    }
}