package de.tomalbrc.cameraobscura.renderer;

import de.tomalbrc.cameraobscura.Components;
import de.tomalbrc.cameraobscura.ModConfig;
import de.tomalbrc.cameraobscura.platform.Platforms;
import de.tomalbrc.cameraobscura.util.ImageUtils;
import de.tomalbrc.cameraobscura.util.image.VideoFileManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoRenderer {
    public final Components.Resolution resolution;
    final Components.ColorMode colorMode;
    final WorldRenderer renderer;
    private final Components.DitherMode ditherMode;
    private final Components.VideoParams videoParams;
    private final ExecutorService serialExecutor;
    private final List<BufferedImage> rawFrames;
    private final RecordingSource source;
    private final UUID id;
    private final int captureInterval;
    private int tickCounter;
    private int frameCount;
    private boolean active;
    private BufferedImage thumbnail;

    public VideoRenderer(
            RecordingSource source,
            Components.Resolution resolution,
            Components.ColorMode colorMode,
            Components.DitherMode ditherMode,
            Components.VideoParams videoParams
    ) {
        this.id = UUID.randomUUID();
        this.source = source;
        this.resolution = resolution;
        this.colorMode = colorMode;
        this.ditherMode = ditherMode;
        this.videoParams = videoParams;
        this.renderer = new WorldRenderer(
                source.getLevel(),
                resolution.width(),
                resolution.height(),
                ModConfig.getInstance().renderDistance
        );
        renderer.entity = source.getEntity();

        this.serialExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "CameraVideo-" + id.toString().substring(0, 8));
            t.setDaemon(true);
            return t;
        });
        this.rawFrames = new ArrayList<>();
        this.captureInterval = Math.max(1, 20 / videoParams.frameRate());
    }

    public WorldRenderer renderer() {
        return renderer;
    }

    public static RecordingSource entitySource(Entity entity) {
        return new RecordingSource() {
            @Override
            public Level getLevel() {
                return entity.level();
            }

            @Override
            public Vec3 getCameraPos() {
                return entity.getEyePosition();
            }

            @Override
            public double getPitch() {
                return entity.getXRot();
            }

            @Override
            public double getYaw() {
                return entity.getYRot();
            }

            @Override
            public boolean isStillValid() {
                return !entity.isRemoved();
            }

            @Override
            public void playStartSound() {
                entity.level().playSound(null, entity.position().x(), entity.position().y(), entity.position().z(), SoundEvents.SPYGLASS_USE, SoundSource.PLAYERS, 0.8f, 1.0F);
            }

            @Override
            public void playStopSound() {
                entity.level().playSound(null, entity.position().x(), entity.position().y(), entity.position().z(), SoundEvents.SPYGLASS_STOP_USING, SoundSource.PLAYERS, 0.8f, 1.0F);
            }

            @Override
            public void showMessage(String message) {
                if (entity instanceof ServerPlayer player)
                    player.connection.send(new ClientboundSetActionBarTextPacket(Component.literal(message)));
            }

            @Override
            public void giveItem(ItemStack stack) {
                if (entity instanceof ServerPlayer player) {
                    if (!player.addItem(stack)) player.spawnAtLocation(player.level(), stack);
                }
                if (entity.level() instanceof ServerLevel serverLevel) entity.spawnAtLocation(serverLevel, stack);
            }

            @Override
            public @Nullable Entity getEntity() {
                return entity;
            }
        };
    }

    public static RecordingSource blockEntitySource(BlockEntity be, Vec3 cameraPos, double pitch, double yaw) {
        return new RecordingSource() {
            @Override
            public Level getLevel() {
                return be.getLevel();
            }

            @Override
            public Vec3 getCameraPos() {
                return cameraPos;
            }

            @Override
            public double getPitch() {
                return pitch;
            }

            @Override
            public double getYaw() {
                return yaw - 180f;
            }

            @Override
            public boolean isStillValid() {
                return !be.isRemoved();
            }

            @Override
            public void playStartSound() {
                Level level = getLevel();
                if (level != null)
                    level.playSound(null, be.getBlockPos(), SoundEvents.SPYGLASS_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            @Override
            public void playStopSound() {
                Level level = getLevel();
                if (level != null)
                    level.playSound(null, be.getBlockPos(), SoundEvents.SPYGLASS_STOP_USING, SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            @Override
            public void showMessage(String message) {
            }

            @Override
            public void giveItem(ItemStack stack) {
                Level level = getLevel();
                if (level != null) {
                    BlockPos pos = be.getBlockPos();
                    ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, stack);
                    level.addFreshEntity(itemEntity);
                }
            }

            @Override
            public @Nullable Entity getEntity() {
                return null;
            }
        };
    }

    public boolean isActive() {
        return active;
    }

    public void start() {
        active = true;
        tickCounter = 0;
        source.playStartSound();
        source.showMessage("Recording started");
    }

    public void tick() {
        if (serialExecutor.isShutdown()) return;

        if (!active || !source.isStillValid()) {
            stopAndFinalizeAsync();
            serialExecutor.shutdown();
            return;
        }
        tickCounter++;
        if (tickCounter % captureInterval == 0) {
            var pos = source.getCameraPos().toVector3f();
            renderer.updateCamera(pos.x, pos.y, pos.z, source.getPitch(), source.getYaw());
            serialExecutor.submit(() -> {
                if (!active) return;
                try {
                    renderer.updateChunksInRange(source.getLevel(), true);

                    BufferedImage rawFrame = renderer.render();
                    BufferedImage processed = Components.processImage(rawFrame, colorMode, ditherMode);
                    if (thumbnail == null) thumbnail = processed;
                    rawFrames.add(processed);

                    frameCount++;

                    source.showMessage("Recording... " + frameCount + "/" + videoParams.maxFrames() + " frames");

                    if (videoParams.maxFrames() > 0 && frameCount >= videoParams.maxFrames()) {
                        stopAndFinalize();
                    }
                } catch (Exception e) {
                    Platforms.get().getLogger().error("Error while rendering", e);
                }
            });
        }
    }

    public void stopAndFinalizeAsync() {
        if (!serialExecutor.isShutdown()) serialExecutor.execute(this::stopAndFinalize);
    }

    public void stopAndFinalize() {
        if (!active) return;
        active = false;
        finalizeVideo();
        serialExecutor.shutdownNow();
    }

    private void finalizeVideo() {
        if (rawFrames.isEmpty()) return;
        VideoFileManager.saveVideo(id.toString(), rawFrames, videoParams);
        Components.MediaData data = new Components.MediaData(id, videoParams);
        Level level = source.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) return;
        ItemStack sdCard = ImageUtils.createMapItem(thumbnail, serverLevel);
        Platforms.get().getItemDataStore().setMediaData(sdCard, data);
        Platforms.get().getMinecraftServer().execute(() -> {
            source.playStopSound();
            source.giveItem(sdCard);
        });
    }

    public interface RecordingSource {
        Level getLevel();

        Vec3 getCameraPos();

        double getPitch();

        double getYaw();

        boolean isStillValid();

        void playStartSound();

        void playStopSound();

        void showMessage(String message);

        void giveItem(ItemStack stack);

        @Nullable Entity getEntity();
    }
}