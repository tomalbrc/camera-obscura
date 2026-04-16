package de.tomalbrc.cameraobscura;

import com.nexomc.nexo.mechanics.MechanicsManager;
import de.tomalbrc.cameraobscura.item.NexoCameraMechanic;
import de.tomalbrc.cameraobscura.platform.ItemDataStore;
import de.tomalbrc.cameraobscura.platform.NexoAwareItemDataStore;
import org.bukkit.plugin.java.JavaPlugin;

public class NexoHook {
    public static ItemDataStore getItemDataStore(JavaPlugin plugin) {
        var factory = new NexoCameraMechanic.Factory();
        MechanicsManager.INSTANCE.registerMechanicFactory(factory, true);
        return new NexoAwareItemDataStore(plugin, factory);
    }
}
