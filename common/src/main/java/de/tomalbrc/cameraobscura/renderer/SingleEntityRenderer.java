package de.tomalbrc.cameraobscura.renderer;

import de.tomalbrc.cameraobscura.ModConfig;
import de.tomalbrc.cameraobscura.json.CachedIdentifierDeserializer;
import de.tomalbrc.cameraobscura.renderer.entity.EntityRenderer;
import de.tomalbrc.cameraobscura.sore.pipeline.FrameContext;
import de.tomalbrc.cameraobscura.util.Constants;
import de.tomalbrc.cameraobscura.util.resource.RPHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;

import java.awt.image.BufferedImage;

public class SingleEntityRenderer extends WorldRenderer {
    public SingleEntityRenderer(Level level, int width, int height, int renderDistance) {
        this(level, width, height, renderDistance, ModConfig.getInstance().ssaa);
    }

    public SingleEntityRenderer(Level level, int width, int height, int renderDistance, int ssaa) {
        super(level, width, height, renderDistance, ssaa);
    }

    @Override
    public BufferedImage render() {
        if (this.entity != null) {
            updateCamera(this.entity);
        }

        if (this.entity != null) {
            double radius = 2.0;
            double x = entity.getX() + radius;
            double z = entity.getZ() + radius;
            double y = entity.getY() + 1.5;

            camera.setPosition(x, y, z);
            camera.setTarget(entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ());
            camera.setUp(0, 1, 0);
        }

        long time = level.getGameTime();
        var env = envinmentValues;
        if (env == null) {
            env = EnvironmentValues.fromLevel(level, camera.position());
        }

        Vector3d sunDir = new Vector3d(-Math.sin(env.sunAngle()), Math.cos(env.sunAngle()), 0).normalize();

        uniforms.fogStart = renderDistance * 0.85f;
        uniforms.fogEnd = renderDistance;
        uniforms.fogColor = env.fogColor();
        uniforms.pointLights.clear();

        if (entity.getType() == EntityType.PLAYER) {
            try {
                RPHelper.loadTexture(CachedIdentifierDeserializer.get(Constants.DYNAMIC_PLAYER_TEXTURE + ":" + entity.getUUID()));
            } catch (Exception _) {
                return null;
            }
        }

        pipeline.beginFrame(new FrameContext(time, sunDir, env.lightFactor()));

        EntityRenderer r = EntityRenderers.RENDERER.get(entity.getType());
        if (r != null) {
            r.render(pipeline, entity);
        }

        pipeline.endFrame();
        return rasterizer.getFramebuffer().toImage(scale);
    }

    @Override
    public void updateChunksInRange(Level level, boolean forceAll) {

    }
}