package de.tomalbrc.cameraobscura.renderer;

import de.tomalbrc.cameraobscura.ModConfig;
import de.tomalbrc.cameraobscura.json.CachedIdentifierDeserializer;
import de.tomalbrc.cameraobscura.model.triangle.ModelTesselator;
import de.tomalbrc.cameraobscura.platform.Platforms;
import de.tomalbrc.cameraobscura.renderer.entity.EntityRenderer;
import de.tomalbrc.cameraobscura.sore.Camera;
import de.tomalbrc.cameraobscura.sore.Uniforms;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.FrameContext;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import de.tomalbrc.cameraobscura.sore.pipeline.SoftwareRenderPipeline;
import de.tomalbrc.cameraobscura.sore.rasterizer.Framebuffer;
import de.tomalbrc.cameraobscura.sore.rasterizer.SoftwareRasterizer;
import de.tomalbrc.cameraobscura.util.BuiltinModels;
import de.tomalbrc.cameraobscura.util.Constants;
import de.tomalbrc.cameraobscura.util.resource.RPHelper;
import de.tomalbrc.cameraobscura.util.resource.TinyFontRenderer;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.FrustumIntersection;
import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.slf4j.Logger;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WorldRenderer extends AbstractRenderer<BufferedImage> {
    private static final Matrix4d skyTransform = new Matrix4d().scale(512);
    private static Model cloudModel;

    protected final SoftwareRasterizer rasterizer;
    protected final Uniforms uniforms;
    protected final Camera camera;
    protected final Level level;
    protected final SoftwareRenderPipeline pipeline;
    protected final int renderDistance;
    protected final int scale;

    public Entity entity = null;

    protected EnvironmentValues envinmentValues;

    public WorldRenderer(Level level, int width, int height, int renderDistance) {
        this(level, width, height, renderDistance, ModConfig.getInstance().ssaa);
    }

    public WorldRenderer(Level level, int width, int height, int renderDistance, int ssaa) {
        super(width, height, renderDistance);
        this.level = level;
        this.scale = ssaa;
        this.renderDistance = renderDistance;
        this.rasterizer = new SoftwareRasterizer(new Framebuffer(width * scale, height * scale));
        this.uniforms = new Uniforms();

        this.camera = new Camera();

        double fov = FOV_RAD * 2;
        camera.setPerspective(fov, (double) width / height, 0.1f, renderDistance);

        this.pipeline = new SoftwareRenderPipeline(rasterizer, uniforms, camera);
    }

    private static void renderStats(long totalStart, int drawCommandsFromChunks, int chunksRendered, List<DrawCommand> allCommands, BufferedImage result) {
        long totalEnd = System.nanoTime();

        double fps = 1000.0 / ((totalEnd - totalStart) / 1_000_000.0);
        String header = "CAMERA OBSCURA v2";
        String fpsText = String.format("%.1f FPS", fps);
        String drawCommandsChunk = String.format("%d Chunk Draw", drawCommandsFromChunks);
        String drawCommandsChunk2 = "      Commands";
        String chunks = String.format("%d Chunks", chunksRendered);
        String drawCommandsAll = String.format("%d Draw Commands", allCommands.size());

        var list = List.of(header, fpsText, chunks, drawCommandsChunk, drawCommandsChunk2, drawCommandsAll);
        for (int i = 0; i < list.size(); i++) {
            var e = list.get(i);
            TinyFontRenderer.drawString(result, e, 3, 3 + i * 6, 0xFFFFFF);
        }
    }

    public void updateCamera(double x, double y, double z, double pitch, double yaw) {
        camera.setPosition(x, y, z);
        camera.setTargetFromYawPitch(yaw, Math.clamp(pitch, -89.9f, 89.9f));
        camera.setUp(0, 1, 0);
    }

    public void updateCamera(Entity entity) {
        var eye = entity.getEyePosition().toVector3f();
        double yaw = entity.getYRot();
        double pitch = entity.getXRot();
        updateCamera(eye.x, eye.y, eye.z, pitch, yaw);
    }

    public void updateChunksInRange(Level level, boolean forceAll) {
        long start = System.nanoTime();
        int rebuiltCount = 0;
        int max = ModConfig.getInstance().maxChunkRebuildsPerTick;

        this.envinmentValues = EnvironmentValues.fromLevel(level, camera.position());

        BlockPos cameraPos = BlockPos.containing(camera.position());
        int chunkRadius = ChunkMeshCache.toChunkCoord(renderDistance + ChunkMeshCache.CHUNK_SIZE - 1);

        int cx = ChunkMeshCache.toChunkCoord(cameraPos.getX());
        int cy = ChunkMeshCache.toChunkCoord(cameraPos.getY());
        int cz = ChunkMeshCache.toChunkCoord(cameraPos.getZ());

        for (int x = -chunkRadius; x <= chunkRadius; x++) {
            for (int y = -chunkRadius; y <= chunkRadius; y++) {
                for (int z = -chunkRadius; z <= chunkRadius; z++) {
                    long key = ChunkMeshCache.chunkKeyFromCoords(cx + x, cy + y, cz + z);
                    ChunkMeshCache.ChunkMesh chunkMesh = ChunkMeshCache.getChunkMesh(level, key);
                    if (chunkMesh == null) {
                        BlockPos origin = ChunkMeshCache.chunkOriginFromKey(key);
                        chunkMesh = new ChunkMeshCache.ChunkMesh(origin, key);
                        ChunkMeshCache.putChunkMesh(level, key, chunkMesh);
                    }

                    if (chunkMesh.isDirty() && (forceAll || rebuiltCount < max)) {
                        ChunkMeshCache.rebuildChunk(level, chunkMesh);
                        rebuiltCount++;
                    }
                }
            }
        }

        if (ModConfig.getInstance().debug) {
            long duration = System.nanoTime() - start;
            double ms = duration / 1_000_000.0;
            Platforms.get().getLogger().info("updateChunksInRange: {} ms ({} chunks rebuilt)", String.format("%.2f", ms), rebuiltCount);
        }
    }

    record EnvironmentValues(
            double sunAngle,
            double lightFactor,
            int fogColor,
            double cloudHeight
    ) {
        public static EnvironmentValues fromLevel(Level level, Vec3 cameraPos) {
            double sunAngle = level.environmentAttributes().getValue(EnvironmentAttributes.SUN_ANGLE, cameraPos) * Mth.DEG_TO_RAD;
            double lightFactor = level.environmentAttributes().getValue(EnvironmentAttributes.SKY_LIGHT_FACTOR, cameraPos);
            int fogColor = level.environmentAttributes().getValue(EnvironmentAttributes.FOG_COLOR, cameraPos);
            double cloudHeight = level.environmentAttributes().getValue(EnvironmentAttributes.CLOUD_HEIGHT, cameraPos);
            return new EnvironmentValues(sunAngle, lightFactor, fogColor, cloudHeight);
        }
    }

    public CompletableFuture<BufferedImage> renderAsync() {
        return CompletableFuture.supplyAsync(this::render, Constants.RENDER_EXEC);
    }
    public CompletableFuture<Void> updateChunksAsync(Level level) {
        return CompletableFuture.runAsync(() -> {
            updateChunksInRange(level, true);
        }, Constants.CHUNK_EXEC);
    }

    @Override
    public BufferedImage render() {
        if (this.entity != null) {
            updateCamera(this.entity);
        }

        long totalStart = System.nanoTime();

        long time = level.getGameTime();
        double sunAngle = this.envinmentValues.sunAngle;
        double lightFactor = this.envinmentValues.lightFactor;
        int fogColor = this.envinmentValues.fogColor;
        double cloudHeight = this.envinmentValues.cloudHeight;

        Vector3d sunDir = new Vector3d(
                -Math.sin(sunAngle),
                Math.cos(sunAngle),
                0
        ).normalize();

        uniforms.fogStart = renderDistance * 0.85f;
        uniforms.fogEnd = renderDistance;
        uniforms.fogColor = fogColor;
        uniforms.pointLights.clear();

        pipeline.beginFrame(new FrameContext(time, sunDir,  ModConfig.getInstance().fullbright ? 1 : lightFactor));

        if (cloudModel == null)
            cloudModel = new Model(new ModelTesselator(BuiltinModels.cloudModel()).build());

        FrustumIntersection frustum = new FrustumIntersection(new Matrix4f(camera.getProjectionMatrix()).mul(camera.getViewMatrix()));

        BlockPos cameraPos = camera.blockPosition();
        int chunkRadius = ChunkMeshCache.toChunkCoord(renderDistance + ChunkMeshCache.CHUNK_SIZE - 1);
        int camChunkX = ChunkMeshCache.toChunkCoord(cameraPos.getX());
        int camChunkY = ChunkMeshCache.toChunkCoord(cameraPos.getY());
        int camChunkZ = ChunkMeshCache.toChunkCoord(cameraPos.getZ());

        List<DrawCommand> allCommands = new ObjectArrayList<>();

        long entityStart = System.nanoTime();
        if (ModConfig.getInstance().renderEntities) {
            AABB entityBounds = AABB.ofSize(camera.position(), renderDistance * 2, renderDistance * 2, renderDistance * 2);
            var entities = level.getEntities(entity, entityBounds);
            for (Entity ent : entities) {
                if (ent instanceof EnderDragonPart)
                    continue;

                EntityRenderer r = EntityRenderers.RENDERER.get(ent.getType());
                if (r != null) {
                    try {
                        r.render(pipeline, ent);
                    } catch (Exception e) {
                        Platforms.get().getLogger().error("Error rendering entity", e);
                    }
                    continue;
                }

                if (ent.getType() == EntityType.PLAYER) {
                    try {
                        RPHelper.loadTexture(CachedIdentifierDeserializer.get(Constants.DYNAMIC_PLAYER_TEXTURE + ":" + ent.getUUID()));
                    } catch (Exception e) {
                        continue;
                    }
                }

                var isItemDisplay = ent.getType() == EntityType.ITEM_DISPLAY;
                if (isItemDisplay) {
                    EntityRenderers.renderItemDisplay(pipeline, ent);
                    continue;
                }

                Platforms.get().renderEntity(ent, allCommands);
            }
        }
        long entityEnd = System.nanoTime();

        long chunkStart = System.nanoTime();
        int chunksRendered = 0;
        int drawCommandsFromChunks = 0;

        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dy = -chunkRadius; dy <= chunkRadius; dy++) {
                for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                    int chunkX = camChunkX + dx;
                    int chunkY = camChunkY + dy;
                    int chunkZ = camChunkZ + dz;
                    long key = ChunkMeshCache.chunkKeyFromCoords(chunkX, chunkY, chunkZ);
                    ChunkMeshCache.ChunkMesh chunkMesh = ChunkMeshCache.getChunkMesh(level, key);
                    if (chunkMesh == null) continue;

                    // cull chunk
                    BlockPos origin = chunkMesh.origin();
                    if (!frustum.testAab(origin.getX(), origin.getY(), origin.getZ(), origin.getX() + ChunkMeshCache.CHUNK_SIZE, origin.getY() + ChunkMeshCache.CHUNK_SIZE, origin.getZ() + ChunkMeshCache.CHUNK_SIZE)) {
                        continue;
                    }

                    List<DrawCommand> commands = chunkMesh.drawCommands();
                    allCommands.addAll(commands);
                    drawCommandsFromChunks += commands.size();
                    chunksRendered++;
                }
            }
        }
        long chunkEnd = System.nanoTime();

        long weatherStart = System.nanoTime();
        if (level.canHaveWeather()) {
            //WeatherRenderer.renderWeather(pipeline, level, camera.position(), (int) time, 1.0f);
            allCommands.add(new DrawCommand(RenderType.SKY, Model.SKY, skyTransform));

            double CLOUD_TILE_SIZE = 2048;
            int CLOUD_RENDER_RANGE = renderDistance;

            int camTileX = Mth.floor(camera.position().x / CLOUD_TILE_SIZE);
            int camTileZ = Mth.floor(camera.position().z / CLOUD_TILE_SIZE);
            int halfRange = (int) Math.ceil(CLOUD_RENDER_RANGE / CLOUD_TILE_SIZE);

            for (int tx = camTileX - halfRange; tx <= camTileX + halfRange; tx++) {
                for (int tz = camTileZ - halfRange; tz <= camTileZ + halfRange; tz++) {
                    double worldX = tx * CLOUD_TILE_SIZE;
                    double worldZ = tz * CLOUD_TILE_SIZE;

                    Matrix4d cloudTransform = new Matrix4d()
                            .translate(worldX, cloudHeight, worldZ)
                            .scale(256);

                    allCommands.add(new DrawCommand(
                            RenderType.ENTITY,
                            cloudModel,
                            cloudTransform,
                            IntList.of()
                    ));
                }
            }
        }
        long weatherEnd = System.nanoTime();

        for (DrawCommand cmd : allCommands) {
            pipeline.draw(cmd);
        }

        pipeline.endFrame();

        BufferedImage result = rasterizer.getFramebuffer().toImage(scale);

        long totalEnd = System.nanoTime();

        if (ModConfig.getInstance().debug && Platforms.get().getMinecraftServer().getTickCount() % 1000 == 0) {
            Logger logger = Platforms.get().getLogger();
            double entityMs = (entityEnd - entityStart) / 1_000_000.0;
            double chunkMs = (chunkEnd - chunkStart) / 1_000_000.0;
            double weatherMs = (weatherEnd - weatherStart) / 1_000_000.0;
            double totalMs = (totalEnd - totalStart) / 1_000_000.0;

            logger.info("Entity rendering: {} ms", String.format("%.2f", entityMs));
            logger.info("Chunk processing: {} ms (chunks: {}, commands: {})", String.format("%.2f", chunkMs), chunksRendered, drawCommandsFromChunks);
            logger.info("Weather/sky/clouds: {} ms", String.format("%.2f", weatherMs));
            logger.info("Total render time: {} ms", String.format("%.2f", totalMs));
        }

        if (ModConfig.getInstance().debug) {
            renderStats(totalStart, drawCommandsFromChunks, chunksRendered, allCommands, result);
        }

        return result;
    }
}