package de.tomalbrc.cameraobscura.mixin;

import de.tomalbrc.cameraobscura.CustomContent;
import de.tomalbrc.cameraobscura.ModConfig;
import de.tomalbrc.cameraobscura.platform.Platforms;
import de.tomalbrc.cameraobscura.renderer.WorldRenderer;
import de.tomalbrc.cameraobscura.util.ImageUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.UniquelyIdentifyable;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends LivingEntity {
    @Shadow
    public ServerGamePacketListenerImpl connection;
    @Unique
    WorldRenderer co$renderer;
    @Unique
    CompletableFuture<Void> co$future;
    @Unique
    double axo, ayo, azo;

    protected ServerPlayerMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @Shadow
    public abstract @NonNull ServerLevel level();

    @Inject(method = "synchronizeSpecialItemUpdates", at = @At("TAIL"))
    protected void co$syncMap(ItemStack itemStack, CallbackInfo ci) {
        if (itemStack.has(CustomContent.DATA))
            return;

        if (itemStack.has(CustomContent.ENTITY_REF) && itemStack.has(CustomContent.LIVE_MAP) && itemStack.has(DataComponents.MAP_ID)) {
            var mapId = itemStack.get(DataComponents.MAP_ID);

            var e = itemStack.get(CustomContent.ENTITY_REF).getEntity(level(), UniquelyIdentifyable.class);
            if (e == null) {
                itemStack.remove(CustomContent.ENTITY_REF);
                return;
            }

            if (e instanceof LivingEntity entity && (co$renderer == null || co$renderer.entity != e)) {
                co$renderer = new WorldRenderer(entity.level(), 128, 128, ModConfig.getInstance().renderDistance);
                co$renderer.entity = entity;
            }

            if (e instanceof LivingEntity entity && (co$future == null || co$future.isDone())) {
                co$renderer.updateChunksAsync(entity.level());
                co$future = co$renderer.renderAsync().thenAccept(img -> {
                    try {
                        if (img == null) {
                            return;
                        }

                        if (false) {
                            List<Integer> colors = new ArrayList<>();
                            for (int i = img.getHeight() - 1; i >= 0; i--) {
                                for (int j = 0; j < img.getWidth(); j++) {
                                    colors.add(img.getRGB(j, i));
                                }
                            }
                            var cmd = new CustomModelData(List.of(), List.of(), List.of(), colors);
                            itemStack.set(DataComponents.CUSTOM_MODEL_DATA, cmd);

                            return;
                        }

                        var image = ImageUtils.imageToMapColors(img);

                        MapItemSavedData mapData = level().getMapData(mapId);
                        if (mapData == null) {
                            return;
                        }

                        byte[] mapColors = new byte[128 * 128];
                        for (int x = 0; x < 128; x++) {
                            for (int y = 0; y < 128; y++) {
                                var index = x + y * 128;
                                var c = image[index];
                                mapColors[index] = c;
                                mapData.setColor(x, y, c);
                            }
                        }

                        Packet<?> packet = new ClientboundMapItemDataPacket(mapId, mapData.scale, false, Optional.empty(), Optional.of(new MapItemSavedData.MapPatch(0, 0, 128, 128, mapColors)));
                        this.connection.send(packet);
                    } catch (Throwable throwable) {
                        Platforms.get().getLogger().error("Error while updating map", throwable);
                        throwable.printStackTrace();
                    }
                });
            }
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void co$tick(CallbackInfo ci) {
        this.co$calculateEntityAnimation(false);
    }

    @Unique
    public void co$calculateEntityAnimation(final boolean useY) {
        float distance = (float) Mth.length(this.trackingPosition().x - this.axo, useY ? this.trackingPosition().y - this.ayo : 0.0, this.trackingPosition().z - this.azo);
        if (!this.isPassenger() && this.isAlive()) {
            this.updateWalkAnimation(distance);
        } else {
            this.walkAnimation.stop();
        }

        axo = this.trackingPosition().x;
        ayo = this.trackingPosition().y;
        azo = this.trackingPosition().z;
    }

}
