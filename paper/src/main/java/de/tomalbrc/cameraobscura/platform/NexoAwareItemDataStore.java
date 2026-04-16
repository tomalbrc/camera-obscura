package de.tomalbrc.cameraobscura.platform;

import com.nexomc.nexo.api.NexoItems;
import de.tomalbrc.cameraobscura.Components;
import de.tomalbrc.cameraobscura.item.NexoCameraMechanic;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public class NexoAwareItemDataStore extends PaperItemDataStore {
    private final NexoCameraMechanic.Factory nexoFactory;

    public NexoAwareItemDataStore(JavaPlugin plugin, NexoCameraMechanic.Factory nexoFactory) {
        super(plugin);
        this.nexoFactory = nexoFactory;
    }

    @Override
    public boolean hasResolution(ItemStack stack) {
        if (super.hasResolution(stack)) return true;
        return getNexoMechanic(stack) != null;
    }

    @Override
    public Components.Resolution getResolution(ItemStack stack) {
        if (super.hasResolution(stack)) return super.getResolution(stack);
        NexoCameraMechanic mech = getNexoMechanic(stack);
        return mech != null ? mech.getResolution() : Components.Resolution.DEFAULT;
    }

    @Override
    public Components.ColorMode getColorMode(ItemStack stack) {
        if (super.hasResolution(stack)) return super.getColorMode(stack);
        NexoCameraMechanic mech = getNexoMechanic(stack);
        return mech != null ? mech.getColorMode() : Components.ColorMode.COLOR;
    }

    @Override
    public Components.DitherMode getDitherMode(ItemStack stack) {
        if (super.hasResolution(stack)) return super.getDitherMode(stack);
        NexoCameraMechanic mech = getNexoMechanic(stack);
        return mech != null ? mech.getDitherMode() : Components.DitherMode.NONE;
    }

    @Override
    public Components.VideoParams getVideoParams(ItemStack stack) {
        if (super.hasResolution(stack)) return super.getVideoParams(stack);
        NexoCameraMechanic mech = getNexoMechanic(stack);
        return mech != null ? mech.getVideoParams() : Components.VideoParams.DEFAULT;
    }

    @Nullable
    private NexoCameraMechanic getNexoMechanic(ItemStack nmsStack) {
        org.bukkit.inventory.ItemStack bukkitStack = CraftItemStack.asBukkitCopy(nmsStack);
        String id = NexoItems.idFromItem(bukkitStack);
        return id != null ? nexoFactory.getMechanic(id) : null;
    }
}