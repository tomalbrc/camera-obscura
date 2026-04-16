package de.tomalbrc.cameraobscura.util.image;

import de.tomalbrc.cameraobscura.Components;
import de.tomalbrc.cameraobscura.apng.StreamingAPNGReader;
import de.tomalbrc.cameraobscura.platform.Platforms;
import de.tomalbrc.cameraobscura.platform.ScheduledTask;
import de.tomalbrc.cameraobscura.util.ImageUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class VideoPlaybackManager {
    private static final Map<ItemFrame, PlaybackInstance> ACTIVE = new ConcurrentHashMap<>();
    private static ScheduledTask tickTask;

    public static void startTicking() {
        stopTicking();

        tickTask = Platforms.get()
                .getScheduler()
                .runTaskTimer(task -> {
                    Iterator<Map.Entry<ItemFrame, PlaybackInstance>> it = ACTIVE.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<ItemFrame, PlaybackInstance> entry = it.next();
                        ItemFrame frame = entry.getKey();
                        PlaybackInstance instance = entry.getValue();

                        if (frame.isRemoved() || !Platforms.get().getItemDataStore().hasMediaData(frame.getItem()) || !frame.getItem().has(DataComponents.MAP_ID)) {
                            it.remove();
                            continue;
                        }
                        instance.tick(frame, it);
                    }
                }, 0L, 1L); // 0 tick delay, 1 tick period
    }

    public static void stopTicking() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
    }

    public static void togglePlayback(ItemFrame frame) {
        if (ACTIVE.containsKey(frame)) {
            ACTIVE.remove(frame);
            return;
        }

        ItemStack item = frame.getItem();

        Components.MediaData data = Platforms.get().getItemDataStore().getMediaData(item);
        MapId mapId = item.get(DataComponents.MAP_ID);
        if (data != null && mapId != null) {
            PlaybackInstance instance = new PlaybackInstance(data, mapId);
            ACTIVE.put(frame, instance);
        }
    }

    private static class PlaybackInstance implements AutoCloseable {
        private final String videoId;
        private final Components.VideoParams videoParams;
        private final MapId mapId;
        private final int frameInterval;
        private StreamingAPNGReader reader;
        private int tickCounter = 0;

        PlaybackInstance(Components.MediaData data, MapId mapId) {
            this.videoId = data.id().toString();
            this.videoParams = data.videoParams();
            this.mapId = mapId;
            int fps = videoParams.frameRate();
            this.frameInterval = fps > 0 ? Math.max(1, 20 / fps) : 1;

            openReader();
        }

        private void openReader() {
            try {
                if (reader != null) reader.close();
                reader = VideoFileManager.openStreamingReader(videoId);
            } catch (IOException e) {
                Platforms.get().getLogger().error("Failed to open streaming reader", e);
                reader = null;
            }
        }

        void tick(ItemFrame frame, Iterator<Map.Entry<ItemFrame, PlaybackInstance>> it) {
            if (frame.level().isClientSide()) return;

            tickCounter++;
            if (tickCounter % frameInterval == 0) {
                if (!sendNextFrame(frame)) {
                    it.remove();
                }
            }
        }

        private boolean sendNextFrame(ItemFrame frame) {
            if (frame.isRemoved()) return false;

            if (reader == null || !reader.hasNext()) {
                if (videoParams.loopPlayback()) {
                    openReader();
                    if (reader == null || !reader.hasNext()) return false;
                } else {
                    return false;
                }
            }

            BufferedImage img = reader.next();
            CompletableFuture.runAsync(() -> {
                try {
                    byte[] mapColors = ImageUtils.imageToMapColors(img);
                    Packet<?> packet = new ClientboundMapItemDataPacket(
                            mapId, (byte) 0, false,
                            Optional.empty(),
                            Optional.of(new MapItemSavedData.MapPatch(0, 0, 128, 128, mapColors))
                    );

                    for (ServerPlayer player : ((ServerLevel) frame.level()).players()) {
                        if (player.distanceToSqr(frame) < 64*64)
                            player.connection.send(packet);
                    }
                } catch (Exception e) {
                    Platforms.get().getLogger().error("Error while sending playback frame", e);
                }
            });

            return true;
        }

        @Override
        public void close() {
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
                Platforms.get().getLogger().error("Error while closing streaming reader", e);
            }
        }
    }
}