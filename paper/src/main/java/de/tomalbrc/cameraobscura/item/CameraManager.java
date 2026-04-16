package de.tomalbrc.cameraobscura.item;

import de.tomalbrc.cameraobscura.CameraObscuraPlugin;
import de.tomalbrc.cameraobscura.Components;
import de.tomalbrc.cameraobscura.ModConfig;
import de.tomalbrc.cameraobscura.platform.Platforms;
import de.tomalbrc.cameraobscura.renderer.VideoRenderer;
import de.tomalbrc.cameraobscura.renderer.WorldRenderer;
import de.tomalbrc.cameraobscura.util.ImageUtils;
import de.tomalbrc.cameraobscura.util.ParticleUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CameraManager {
    private static final Map<UUID, PendingPhoto> pendingPhotos = new HashMap<>();
    private static final Map<UUID, PendingVideo> pendingVideos = new HashMap<>();
    private static final Map<UUID, VideoRenderer> activeRecorders = new HashMap<>();
    private static final Map<UUID, ShutterClose> shutterClosings = new HashMap<>();
    private static final int PHOTO_COUNTDOWN_TICKS = 30;
    private static final int VIDEO_COUNTDOWN_TICKS = 30;
    private static final int SHUTTER_CLOSE_TICKS = 10;

    public static void handleCameraUse(Player player, org.bukkit.inventory.ItemStack handItem,
                                       Components.Resolution resolution, Components.ColorMode colorMode,
                                       Components.DitherMode ditherMode, Components.VideoParams videoParams) {
        if (handItem == null) return;
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        nmsPlayer.startUsingItem(((CraftPlayer) player).getHandle().getUsedItemHand());

        if (videoParams != null && videoParams.frameRate() > 0) {
            startVideoCountdown(player, nmsPlayer, handItem, resolution, colorMode, ditherMode, videoParams);
        } else {
            startPhotoCountdown(player, nmsPlayer, handItem, resolution, colorMode, ditherMode);
        }
    }

    public static void cancelPending(Player player) {
        UUID id = player.getUniqueId();
        PendingPhoto photo = pendingPhotos.remove(id);
        PendingVideo video = pendingVideos.remove(id);
        if (photo != null || video != null) {
            ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
            showActionBar(nmsPlayer, Component.literal("Cancelled"));
        }
    }

    public static void stopRecordingIfActive(Player player) {
        if (activeRecorders.containsKey(player.getUniqueId())) {
            stopRecording(player);
        }
    }

    public static boolean isRecording(UUID id) {
        return activeRecorders.containsKey(id);
    }

    public static void cleanupPlayer(UUID id) {
        pendingPhotos.remove(id);
        pendingVideos.remove(id);
        ShutterClose sc = shutterClosings.remove(id);
        if (sc != null) sc.recorder.stopAndFinalizeAsync();
        VideoRenderer rec = activeRecorders.remove(id);
        if (rec != null) rec.stopAndFinalizeAsync();
    }

    private static void startPhotoCountdown(Player player, ServerPlayer nmsPlayer,
                                            org.bukkit.inventory.ItemStack cameraItem,
                                            Components.Resolution resolution, Components.ColorMode colorMode,
                                            Components.DitherMode ditherMode) {
        UUID id = player.getUniqueId();
        if (pendingPhotos.containsKey(id)) return;
        if (player.hasCooldown(cameraItem.getType())) return;

        player.setCooldown(cameraItem.getType(), 20);
        pendingPhotos.put(id, new PendingPhoto(resolution, colorMode, ditherMode, cameraItem.clone(), PHOTO_COUNTDOWN_TICKS));
        showActionBar(nmsPlayer, Component.literal("3"));
        nmsPlayer.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 0.3f, 1.5f);
        nmsPlayer.startUsingItem(nmsPlayer.getUsedItemHand());

        Platforms.get().getScheduler().runTaskTimer(scheduledTask -> {
            PendingPhoto pending = pendingPhotos.get(id);
            if (pending == null || !player.isOnline()) {
                scheduledTask.cancel();
                pendingPhotos.remove(id);
                return;
            }
            int remaining = pending.countdownTicks - 1;
            if (remaining <= 0) {
                scheduledTask.cancel();
                pendingPhotos.remove(id);
                capturePhoto(player, nmsPlayer, pending.cameraItem, pending.resolution, pending.colorMode, pending.ditherMode);
            } else {
                pendingPhotos.put(id, new PendingPhoto(pending.resolution, pending.colorMode, pending.ditherMode,
                        pending.cameraItem, remaining));
                int sec = (int) Math.ceil(remaining / 20.0);
                showActionBar(nmsPlayer, Component.literal(String.valueOf(sec)));
                int elapsed = PHOTO_COUNTDOWN_TICKS - remaining;
                ParticleUtils.spawnCountdownParticles(nmsPlayer, pending.resolution, elapsed, remaining, true);
            }
        }, 1, 1);
    }

    private static void capturePhoto(Player player, ServerPlayer nmsPlayer,
                                     org.bukkit.inventory.ItemStack cameraItem,
                                     Components.Resolution resolution, Components.ColorMode colorMode,
                                     Components.DitherMode ditherMode) {
        ServerLevel level = ((CraftWorld) player.getWorld()).getHandle();
        int renderDistance = ModConfig.getInstance().renderDistance;

        WorldRenderer renderer = new WorldRenderer(level, resolution.width(), resolution.height(), renderDistance);
        renderer.entity = nmsPlayer;
        renderer.updateCamera(nmsPlayer);

        CompletableFuture.supplyAsync(() -> {
            renderer.updateChunksInRange(level, true);
            BufferedImage raw = renderer.render();
            return Components.processImage(raw, colorMode, ditherMode);
        }).thenAcceptAsync(image -> {
            if (!player.isOnline()) return;
            List<ItemStack> maps = ImageUtils.createMapItems(image, level, null);
            for (ItemStack map : maps) {
                if (!player.getInventory().addItem(CraftItemStack.asBukkitCopy(map)).isEmpty()) {
                    player.getWorld().dropItem(player.getLocation(), CraftItemStack.asBukkitCopy(map));
                }
            }
            level.playSound(null, nmsPlayer.blockPosition(),
                    SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.PLAYERS, 1.0F, 1.0F);
            showActionBar(nmsPlayer, Component.literal("Photo taken!"));
        }, task -> Bukkit.getScheduler().runTask(CameraObscuraPlugin.getInstance(), task));
    }

    private static void startVideoCountdown(Player player, ServerPlayer nmsPlayer,
                                            org.bukkit.inventory.ItemStack cameraItem,
                                            Components.Resolution resolution, Components.ColorMode colorMode,
                                            Components.DitherMode ditherMode, Components.VideoParams videoParams) {
        UUID id = player.getUniqueId();
        if (pendingVideos.containsKey(id) || activeRecorders.containsKey(id)) return;

        player.setCooldown(cameraItem.getType(), 20);
        pendingVideos.put(id, new PendingVideo(resolution, colorMode, ditherMode, videoParams, VIDEO_COUNTDOWN_TICKS));
        showActionBar(nmsPlayer, Component.literal("3"));
        nmsPlayer.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 0.3f, 1.5f);

        Platforms.get().getScheduler().runTaskTimer(task -> {
            PendingVideo pending = pendingVideos.get(id);
            if (pending == null || !player.isOnline()) {
                task.cancel();
                pendingVideos.remove(id);
                return;
            }
            int remaining = pending.countdownTicks - 1;
            if (remaining <= 0) {
                task.cancel();
                pendingVideos.remove(id);
                startRecording(player, nmsPlayer, pending.resolution, pending.colorMode, pending.ditherMode, pending.videoParams);
            } else {
                pendingVideos.put(id, new PendingVideo(pending.resolution, pending.colorMode, pending.ditherMode,
                        pending.videoParams, remaining));
                int sec = (int) Math.ceil(remaining / 20.0);
                showActionBar(nmsPlayer, Component.literal(String.valueOf(sec)));
                int elapsed = VIDEO_COUNTDOWN_TICKS - remaining;
                ParticleUtils.spawnCountdownParticles(nmsPlayer, pending.resolution, elapsed, remaining, false);
            }
        }, 1, 1);
    }

    private static void startRecording(Player player, ServerPlayer nmsPlayer,
                                       Components.Resolution resolution, Components.ColorMode colorMode,
                                       Components.DitherMode ditherMode, Components.VideoParams videoParams) {
        UUID id = player.getUniqueId();
        VideoRenderer recorder = new VideoRenderer(VideoRenderer.entitySource(nmsPlayer), resolution, colorMode, ditherMode, videoParams);
        activeRecorders.put(id, recorder);
        recorder.start();
        showActionBar(nmsPlayer, Component.literal("⏺ Recording..."));

        Platforms.get().getScheduler().runTaskTimer(task -> {
            if (!player.isOnline() || !isCameraItem(player.getInventory().getItemInMainHand())) {
                stopRecording(player);
                task.cancel();
                return;
            }
            VideoRenderer r = activeRecorders.get(id);
            if (r == null) {
                task.cancel();
                return;
            }
            r.tick();
            ParticleUtils.spawnStaticFrame(nmsPlayer, resolution);
        }, 1, 1);
    }

    private static void stopRecording(Player player) {
        UUID id = player.getUniqueId();
        VideoRenderer rec = activeRecorders.remove(id);
        if (rec == null) return;
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        shutterClosings.put(id, new ShutterClose(rec, SHUTTER_CLOSE_TICKS));

        Platforms.get().getScheduler().runTaskTimer(task -> {
            ShutterClose sc = shutterClosings.get(id);
            if (sc == null || !player.isOnline()) {
                task.cancel();
                shutterClosings.remove(id);
                if (sc != null) sc.recorder.stopAndFinalizeAsync();
                return;
            }
            int remaining = sc.ticksLeft - 1;
            if (remaining <= 0) {
                sc.recorder.stopAndFinalizeAsync();
                shutterClosings.remove(id);
                showActionBar(nmsPlayer, Component.literal("⏹ Recording saved"));
                task.cancel();
            } else {
                shutterClosings.put(id, new ShutterClose(sc.recorder, remaining));
                ParticleUtils.spawnShutterCloseParticles(nmsPlayer, sc.recorder.resolution, remaining);
            }
        }, 1, 1);
    }

    private static boolean isCameraItem(org.bukkit.inventory.ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return false;

        return Platforms.get().getItemDataStore().hasResolution(CraftItemStack.unwrap(stack));
    }

    private static void showActionBar(ServerPlayer nmsPlayer, Component message) {
        nmsPlayer.connection.send(new ClientboundSetActionBarTextPacket(message));
    }

    private record PendingPhoto(Components.Resolution resolution, Components.ColorMode colorMode,
                                Components.DitherMode ditherMode, org.bukkit.inventory.ItemStack cameraItem,
                                int countdownTicks) {
    }

    private record PendingVideo(Components.Resolution resolution, Components.ColorMode colorMode,
                                Components.DitherMode ditherMode, Components.VideoParams videoParams,
                                int countdownTicks) {
    }

    private record ShutterClose(VideoRenderer recorder, int ticksLeft) {
    }
}