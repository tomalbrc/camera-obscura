package de.tomalbrc.cameraobscura.item;

import de.tomalbrc.cameraobscura.ModConfig;
import de.tomalbrc.cameraobscura.command.CameraCommand;
import de.tomalbrc.cameraobscura.render.renderer.CanvasImageRenderer;
import eu.pb4.mapcanvas.api.core.CanvasImage;
import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class CameraItem extends SimplePolymerItem {
    public CameraItem(Properties settings) {
        super(settings, BuiltInRegistries.ITEM.get(ModConfig.getInstance().cameraItem).orElseThrow().value());
    }

    @Override
    @NotNull
    public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
        player.getCooldowns().addCooldown(player.getItemInHand(interactionHand), 30);

        var consumeItem = BuiltInRegistries.ITEM.get(ModConfig.getInstance().cameraConsumeItem);
        if (player instanceof ServerPlayer serverPlayer && (!ModConfig.getInstance().cameraConsumesItem || player.getInventory().hasAnyOf(Set.of(consumeItem.orElseThrow().value())) || player.isCreative())) {
            if (!player.isCreative() && ModConfig.getInstance().cameraConsumesItem) {
                int slot = player.getInventory().findSlotMatchingItem(consumeItem.orElseThrow().value().getDefaultInstance());
                var itemStack = player.getInventory().getItem(slot);
                itemStack.shrink(1);
            }

            player.playSound(SoundEvents.SPYGLASS_USE, 1.0F, 1.0F);
            player.awardStat(Stats.ITEM_USED.get(this));

            var renderer = new CanvasImageRenderer(player, 128, 128, ModConfig.getInstance().renderDistance);
            CompletableFuture.supplyAsync(renderer::render).thenAcceptAsync(mapImage -> finalize(mapImage, serverPlayer), level.getServer());

            return InteractionResult.SUCCESS_SERVER;
        }

        return InteractionResult.FAIL;
    }

    private void finalize(CanvasImage canvasImage, ServerPlayer player) {
        if (player != null && !player.isRemoved()) {
            player.playSound(SoundEvents.SPYGLASS_STOP_USING, 1.0F, 1.0F);

            var items = CameraCommand.mapItems(canvasImage, player.level());
            items.forEach(x -> {
                if (!player.addItem(x)) {
                    player.spawnAtLocation(player.level(), x);
                }
            });
        }
    }

    @Override
    @NotNull
    public Component getName(ItemStack itemStack) {
        return Component.literal("Camera");
    }
}
