package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.specific.block.TntRenderer;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.minecart.MinecartTNT;
import org.joml.Matrix4d;

public class TntMinecartRenderer extends MinecartRenderer<MinecartTNT> {
    @Override
    protected void renderDisplayBlock(RenderPipeline pipeline, MinecartTNT entity, Matrix4d parent, double block, double sky) {
        double fuse = entity.getFuse() > -1 ? entity.getFuse() - 1.0f + 1.0f : -1.0f;

        double fuseRemaining = fuse + 1;
        if (fuseRemaining < 10.0f) {
            double g = 1.0f - fuseRemaining / 10.0f;
            g = Mth.clamp(g, 0.0f, 1.0f);
            g *= g;
            g *= g;
            double scale = 0.75f + g * 0.3f;
            parent.scale(scale, scale, scale);
        }

        boolean white = (int) fuseRemaining / 5 % 2 == 0;

        if (!white) {
            Matrix4d blockMat = new Matrix4d(parent);
            blockMat.scale(0.75f);
            blockMat.translate(-0.5f, (entity.getDisplayOffset() - 8) / 16.0f, 0.5f);
            blockMat.rotateY(Mth.DEG_TO_RAD * 90.0f);

            pipeline.draw(new DrawCommand(RenderType.ENTITY, TntRenderer.get(), blockMat, IntList.of(0xFFFFFFFF)));
        } else {
            super.renderDisplayBlock(pipeline, entity, parent, block, sky);
        }
    }
}