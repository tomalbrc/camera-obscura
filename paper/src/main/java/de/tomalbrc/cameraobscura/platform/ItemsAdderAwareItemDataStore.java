package de.tomalbrc.cameraobscura.platform;

import de.tomalbrc.cameraobscura.Components;
import dev.lone.itemsadder.api.CustomStack;
import net.minecraft.world.item.ItemStack;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemsAdderAwareItemDataStore extends PaperItemDataStore {
    public ItemsAdderAwareItemDataStore(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean hasResolution(ItemStack stack) {
        if (super.hasResolution(stack)) return true;
        return getCameraConfig(stack) != null;
    }

    @Override
    public Components.Resolution getResolution(ItemStack stack) {
        if (super.hasResolution(stack)) return super.getResolution(stack);
        FileConfiguration cfg = getCameraConfig(stack);
        if (cfg != null) {
            int w = cfg.getInt("resolution_width", 128);
            int h = cfg.getInt("resolution_height", 128);
            return new Components.Resolution(w, h);
        }
        return Components.Resolution.DEFAULT;
    }

    @Override
    public Components.ColorMode getColorMode(ItemStack stack) {
        if (super.hasResolution(stack)) return super.getColorMode(stack);
        FileConfiguration cfg = getCameraConfig(stack);
        if (cfg != null) {
            return Components.ColorMode.valueOf(
                    cfg.getString("color_mode", "COLOR"));
        }
        return Components.ColorMode.COLOR;
    }

    @Override
    public Components.DitherMode getDitherMode(ItemStack stack) {
        if (super.hasResolution(stack)) return super.getDitherMode(stack);
        FileConfiguration cfg = getCameraConfig(stack);
        if (cfg != null) {
            return Components.DitherMode.valueOf(cfg.getString("dither_mode", "NONE"));
        }
        return Components.DitherMode.NONE;
    }

    @Override
    public Components.VideoParams getVideoParams(ItemStack stack) {
        if (super.hasResolution(stack)) return super.getVideoParams(stack);
        FileConfiguration cfg = getCameraConfig(stack);
        if (cfg != null) {
            int fr = cfg.getInt("video_frame_rate", 0);
            int mf = cfg.getInt("video_max_frames", 0);
            return new Components.VideoParams(fr, mf, false);
        }
        return Components.VideoParams.DEFAULT;
    }

    /**
     * Returns the "camera" section of an ItemsAdder item config, or null.
     */
    private FileConfiguration getCameraConfig(ItemStack nmsStack) {
        org.bukkit.inventory.ItemStack bukkitItem = CraftItemStack.asBukkitCopy(nmsStack);
        CustomStack cs = CustomStack.byItemStack(bukkitItem);
        if (cs == null) return null;
        FileConfiguration cfg = cs.getConfig();
        if (cfg == null || !cfg.contains("camera")) return null;
        return cfg.getConfigurationSection("camera") != null
                ? cfg
                : null;
    }
}