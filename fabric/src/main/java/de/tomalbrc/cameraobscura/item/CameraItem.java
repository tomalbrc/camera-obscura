package de.tomalbrc.cameraobscura.item;

import de.tomalbrc.cameraobscura.Components;
import de.tomalbrc.cameraobscura.CustomContent;
import de.tomalbrc.cameraobscura.ModConfig;
import de.tomalbrc.cameraobscura.platform.Platforms;
import de.tomalbrc.cameraobscura.renderer.VideoRenderer;
import de.tomalbrc.cameraobscura.renderer.WorldRenderer;
import de.tomalbrc.cameraobscura.util.Constants;
import de.tomalbrc.cameraobscura.util.ImageUtils;
import de.tomalbrc.cameraobscura.util.ParticleUtils;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class CameraItem extends Item implements PolymerItem {
    private static final Map<ServerPlayer, VideoRenderer> ACTIVE_RECORDERS = new ConcurrentHashMap<>();
    private static final Map<ServerPlayer, PendingPhoto> PENDING_PHOTOS = new ConcurrentHashMap<>();
    private static final Map<ServerPlayer, PendingVideo> PENDING_VIDEOS = new ConcurrentHashMap<>();
    private static final Map<ServerPlayer, ShutterClose> SHUTTER_CLOSING = new ConcurrentHashMap<>();

    private static final int PHOTO_COUNTDOWN_TICKS = 30;
    private static final int VIDEO_COUNTDOWN_TICKS = 30;

    public CameraItem(Properties settings) {
        super(settings.stacksTo(1).component(DataComponents.CONSUMABLE,
                Consumable.builder().consumeSeconds(72000).animation(ItemUseAnimation.SPYGLASS).build()));
    }

    public static void registerEventHandler() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            var photoIt = PENDING_PHOTOS.entrySet().iterator();
            while (photoIt.hasNext()) {
                var entry = photoIt.next();
                ServerPlayer player = entry.getKey();
                PendingPhoto pending = entry.getValue();
                if (!player.isAlive() || player.isRemoved()) {
                    photoIt.remove();
                    continue;
                }
                int remaining = pending.countdownTicks - 1;
                if (remaining <= 0) {
                    photoIt.remove();
                    capturePhoto(player, pending.cameraStack, pending.resolution, pending.colorMode, pending.ditherMode);
                } else {
                    entry.setValue(new PendingPhoto(pending.resolution, pending.colorMode, pending.ditherMode, pending.cameraStack, remaining));
                    int sec = (int) Math.ceil(remaining / 20.0);
                    showMessage(player, Component.literal(String.valueOf(sec)));
                    int elapsed = PHOTO_COUNTDOWN_TICKS - remaining;
                    ParticleUtils.spawnCountdownParticles(player, pending.resolution, elapsed, remaining, true);
                }
            }

            var videoIt = PENDING_VIDEOS.entrySet().iterator();
            while (videoIt.hasNext()) {
                var entry = videoIt.next();
                ServerPlayer player = entry.getKey();
                PendingVideo pending = entry.getValue();
                if (!player.isAlive() || player.isRemoved()) {
                    videoIt.remove();
                    continue;
                }

                int remaining = pending.countdownTicks - 1;
                if (remaining <= 0) {
                    videoIt.remove();
                    startRecordingAfterCountdown(player, pending.resolution, pending.colorMode, pending.ditherMode, pending.videoParams);
                } else {
                    entry.setValue(new PendingVideo(pending.resolution, pending.colorMode, pending.ditherMode, pending.videoParams, remaining));
                    int sec = (int) Math.ceil(remaining / 20.0);
                    showMessage(player, Component.literal(String.valueOf(sec)));
                    int elapsed = VIDEO_COUNTDOWN_TICKS - remaining;
                    ParticleUtils.spawnCountdownParticles(player, pending.resolution, elapsed, remaining, false);
                }
            }

            for (var entry : ACTIVE_RECORDERS.entrySet()) {
                ServerPlayer player = entry.getKey();
                if (!player.isRemoved()) {
                    VideoRenderer rec = entry.getValue();
                    ParticleUtils.spawnStaticFrame(player, rec.resolution);
                }
                entry.getValue().tick();
            }

            var shutterIt = SHUTTER_CLOSING.entrySet().iterator();
            while (shutterIt.hasNext()) {
                var entry = shutterIt.next();
                ServerPlayer player = entry.getKey();
                ShutterClose state = entry.getValue();
                if (!player.isAlive() || player.isRemoved()) {
                    state.recorder.stopAndFinalizeAsync();
                    shutterIt.remove();
                    continue;
                }
                int ticksLeft = state.ticksLeft - 1;
                if (ticksLeft <= 0) {
                    state.recorder.stopAndFinalizeAsync();
                    shutterIt.remove();
                    showMessage(player, Component.literal("⏹ Recording saved"));
                } else {
                    entry.setValue(new ShutterClose(state.recorder, ticksLeft));
                    ParticleUtils.spawnShutterCloseParticles(player, state.recorder.resolution, ticksLeft);
                }
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.getPlayer();
            PENDING_PHOTOS.remove(player);
            PENDING_VIDEOS.remove(player);
            ShutterClose sc = SHUTTER_CLOSING.remove(player);
            if (sc != null) sc.recorder.stopAndFinalizeAsync();

            VideoRenderer rec = ACTIVE_RECORDERS.remove(player);
            if (rec != null) rec.stopAndFinalizeAsync();
        });
    }

    private static void capturePhoto(ServerPlayer player, ItemStack stack, Components.Resolution resolution, Components.ColorMode colorMode, Components.DitherMode ditherMode) {
        WorldRenderer renderer = new WorldRenderer(player.level(), resolution.width(), resolution.height(), ModConfig.getInstance().renderDistance);
        renderer.entity = player;
        renderer.updateCamera(player);
        CompletableFuture.supplyAsync(() -> {
            renderer.updateChunksInRange(player.level(), true);
            BufferedImage raw = renderer.render();
            return Components.processImage(raw, colorMode, ditherMode);
        }, Constants.RENDER_EXEC).thenAcceptAsync(processed -> {
            ServerLevel level = player.level();
            List<ItemStack> maps = ImageUtils.createMapItems(processed, level, null);
            for (ItemStack map : maps) {
                if (!player.addItem(map)) {
                    level.addFreshEntity(new ItemEntity(level, player.getX(), player.getY(), player.getZ(), map));
                }
            }
            level.playSound(null, player.blockPosition(), SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.PLAYERS, 1.0F, 1.0F);
            showMessage(player, Component.literal("Photo taken!"));
        }, Platforms.get().getMinecraftServer());
    }

    private static void startRecordingAfterCountdown(ServerPlayer player, Components.Resolution resolution, Components.ColorMode colorMode, Components.DitherMode ditherMode, Components.VideoParams videoParams) {
        VideoRenderer recorder = new VideoRenderer(VideoRenderer.entitySource(player), resolution, colorMode, ditherMode, videoParams);
        ACTIVE_RECORDERS.put(player, recorder);
        recorder.start();
        showMessage(player, Component.literal("⏺ Recording..."));
    }

    public static void showMessage(ServerPlayer player, Component message) {
        player.connection.send(new ClientboundSetActionBarTextPacket(message));
    }

    @Override
    public int getUseDuration(@NotNull ItemStack itemStack, @NotNull LivingEntity user) {
        return Item.APPROXIMATELY_INFINITE_USE_DURATION;
    }

    @Override
    public @NotNull ItemUseAnimation getUseAnimation(@NotNull ItemStack itemStack) {
        return ItemUseAnimation.SPYGLASS;
    }

    @Override
    public boolean releaseUsing(@NonNull ItemStack itemStack, @NonNull Level level, @NonNull LivingEntity entity, int remainingTime) {
        if (entity instanceof ServerPlayer sp) {
            sp.getCooldowns().addCooldown(itemStack, 20);
            PENDING_PHOTOS.remove(sp);
            PENDING_VIDEOS.remove(sp);
            VideoRenderer recorder = ACTIVE_RECORDERS.get(sp);
            if (recorder != null) {
                ACTIVE_RECORDERS.remove(sp);
                SHUTTER_CLOSING.put(sp, new ShutterClose(recorder, ParticleUtils.SHUTTER_CLOSE_TICKS));
            }
        }
        return false;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            ItemStack stack = serverPlayer.getItemInHand(hand);

            if (!tryConsumeRequiredItem(serverPlayer)) {
                return InteractionResult.FAIL;
            }

            Components.VideoParams video = stack.getOrDefault(CustomContent.CAMERA_VIDEO_PARAMS, Components.VideoParams.DEFAULT);
            Components.Resolution res = stack.getOrDefault(CustomContent.CAMERA_RESOLUTION, Components.Resolution.DEFAULT);
            Components.ColorMode color = stack.getOrDefault(CustomContent.CAMERA_COLOR_MODE, Components.ColorMode.COLOR);
            Components.DitherMode dither = stack.getOrDefault(CustomContent.CAMERA_DITHER_MODE, Components.DitherMode.NONE);

            if (video.frameRate() > 0) {
                startVideoCountdown(serverPlayer, res, color, dither, video);
            } else {
                startPhotoCountdown(serverPlayer, stack, res, color, dither);
            }
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    private boolean tryConsumeRequiredItem(ServerPlayer player) {
        ModConfig config = ModConfig.getInstance();
        if (!config.cameraConsumesItem || player.isCreative()) {
            return true;
        }

        var consumeItemOpt = BuiltInRegistries.ITEM.get(Objects.requireNonNull(Identifier.tryParse(config.cameraConsumeItem)));
        AtomicReference<Item> consumeItemRef = new AtomicReference<>();
        consumeItemOpt.ifPresentOrElse(x -> consumeItemRef.set(x.value()), () -> consumeItemRef.set(Items.MAP));
        var consumeItem = consumeItemRef.get();

        ItemStack consumeStack = new ItemStack(consumeItem, 1);

        if (!player.getInventory().hasAnyOf(java.util.Set.of(consumeItem))) {
            return false;
        }

        int slot = player.getInventory().findSlotMatchingItem(consumeStack);
        if (slot >= 0) {
            player.getInventory().getItem(slot).shrink(1);
            return true;
        }
        return false;
    }

    private void startPhotoCountdown(ServerPlayer player, ItemStack stack, Components.Resolution resolution, Components.ColorMode colorMode, Components.DitherMode ditherMode) {
        if (PENDING_PHOTOS.containsKey(player)) return;
        if (player.getCooldowns().isOnCooldown(stack)) return;

        player.getCooldowns().addCooldown(stack, 20);
        PENDING_PHOTOS.put(player, new PendingPhoto(resolution, colorMode, ditherMode, stack, PHOTO_COUNTDOWN_TICKS));
        showMessage(player, Component.literal(String.valueOf(3)));
        player.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 0.3f, 1.5f);
    }

    private void startVideoCountdown(ServerPlayer player, Components.Resolution resolution, Components.ColorMode colorMode, Components.DitherMode ditherMode, Components.VideoParams videoParams) {

        if (PENDING_VIDEOS.containsKey(player))
            return;
        if (ACTIVE_RECORDERS.containsKey(player))
            return;

        SHUTTER_CLOSING.remove(player);

        PENDING_VIDEOS.put(player, new PendingVideo(resolution, colorMode, ditherMode, videoParams, VIDEO_COUNTDOWN_TICKS));
        showMessage(player, Component.literal(String.valueOf(3)));
        player.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 0.3f, 1.5f);
    }

    @Override
    @NotNull
    public Component getName(@NonNull ItemStack itemStack) {
        return Component.literal("Camera");
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return Items.PAPER;
    }

    private record PendingPhoto(
            Components.Resolution resolution,
            Components.ColorMode colorMode,
            Components.DitherMode ditherMode,
            ItemStack cameraStack,
            int countdownTicks
    ) {
    }

    private record PendingVideo(
            Components.Resolution resolution,
            Components.ColorMode colorMode,
            Components.DitherMode ditherMode,
            Components.VideoParams videoParams,
            int countdownTicks
    ) {
    }

    private record ShutterClose(
            VideoRenderer recorder,
            int ticksLeft
    ) {
    }
}