package de.tomalbrc.cameraobscura.paper;

import de.tomalbrc.cameraobscura.platform.Platforms;
import de.tomalbrc.cameraobscura.util.image.VideoPlaybackManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.entity.CraftItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

public class ItemFrameListener implements Listener {

    @EventHandler
    public void onItemFrameInteract(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        if (!(event.getRightClicked() instanceof CraftItemFrame craftFrame)) return;

        ItemFrame nmsFrame = craftFrame.getHandle();
        ItemStack item = nmsFrame.getItem();

        var data = Platforms.get().getItemDataStore().getMediaData(item);
        if (data != null && item.has(DataComponents.MAP_ID)) {
            event.setCancelled(true);
            VideoPlaybackManager.togglePlayback(nmsFrame);
        }
    }
}