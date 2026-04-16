package de.tomalbrc.cameraobscura.item;

import de.tomalbrc.cameraobscura.Components;
import de.tomalbrc.cameraobscura.ModConfig;
import de.tomalbrc.cameraobscura.platform.Platforms;
import io.papermc.paper.event.player.PlayerStopUsingItemEvent;
import net.minecraft.world.item.ItemStack;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.EquipmentSlot;

public class CameraListener implements Listener {

    private boolean isCameraItem(org.bukkit.inventory.ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return false;
        return Platforms.get().getItemDataStore().hasResolution(CraftItemStack.unwrap(stack));
    }

    private Material getConsumeMaterial() {
        return Material.matchMaterial(ModConfig.getInstance().cameraConsumeItem);
    }

    private boolean tryConsumeItem(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE || !ModConfig.getInstance().cameraConsumesItem) return true;

        Material required = getConsumeMaterial();
        if (!player.getInventory().contains(required)) return false;

        int slot = player.getInventory().first(required);
        if (slot < 0) return false;

        org.bukkit.inventory.ItemStack stack = player.getInventory().getItem(slot);
        if (stack == null) return false;
        stack.setAmount(stack.getAmount() - 1);
        return true;
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        CraftingInventory inventory = event.getInventory();
        for (org.bukkit.inventory.ItemStack item : inventory.getMatrix()) {
            if (isCameraItem(item)) {
                inventory.setResult(null);
                return;
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isCameraItem(event.getItemInHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        org.bukkit.inventory.ItemStack hand = player.getInventory().getItem(event.getHand());
        if (isCameraItem(hand)) {
            event.setCancelled(true);
            CameraManager.stopRecordingIfActive(player);
        }
    }

    @EventHandler
    public void onStopUsingItem(PlayerStopUsingItemEvent event) {
        Player player = event.getPlayer();
        if (!isCameraItem(event.getItem())) return;
        CameraManager.cancelPending(player);
        CameraManager.stopRecordingIfActive(player);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        org.bukkit.inventory.ItemStack handItem = event.getItem();

        if (!isCameraItem(handItem)) return;

        event.setCancelled(true);

        if (!tryConsumeItem(player)) {
            player.sendActionBar(net.kyori.adventure.text.Component.text("Missing required item."));
            return;
        }

        ItemStack nmsStack = CraftItemStack.unwrap(handItem);
        Components.Resolution resolution = Platforms.get().getItemDataStore().getResolution(nmsStack);
        Components.ColorMode colorMode = Platforms.get().getItemDataStore().getColorMode(nmsStack);
        Components.DitherMode ditherMode = Platforms.get().getItemDataStore().getDitherMode(nmsStack);
        Components.VideoParams videoParams = Platforms.get().getItemDataStore().getVideoParams(nmsStack);

        CameraManager.handleCameraUse(player, handItem, resolution, colorMode, ditherMode, videoParams);
    }

    @EventHandler
    public void onItemChange(PlayerItemHeldEvent event) {
        CameraManager.stopRecordingIfActive(event.getPlayer());
    }

    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        CameraManager.stopRecordingIfActive(event.getPlayer());
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        if (CameraManager.isRecording(event.getPlayer().getUniqueId()) &&
                isCameraItem(event.getItemDrop().getItemStack())) {
            CameraManager.stopRecordingIfActive(event.getPlayer());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        CameraManager.cleanupPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        CameraManager.cleanupPlayer(event.getEntity().getUniqueId());
    }
}