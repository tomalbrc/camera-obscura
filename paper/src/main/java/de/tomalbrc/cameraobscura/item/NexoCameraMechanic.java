package de.tomalbrc.cameraobscura.item;

import com.nexomc.nexo.mechanics.Mechanic;
import com.nexomc.nexo.mechanics.MechanicFactory;
import de.tomalbrc.cameraobscura.Components;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

public class NexoCameraMechanic extends Mechanic {

    private final Components.Resolution resolution;
    private final Components.ColorMode colorMode;
    private final Components.DitherMode ditherMode;
    private final Components.VideoParams videoParams;

    public NexoCameraMechanic(MechanicFactory factory, ConfigurationSection section,
                              Components.Resolution resolution,
                              Components.ColorMode colorMode,
                              Components.DitherMode ditherMode,
                              Components.VideoParams videoParams) {
        super(factory, section);

        this.resolution = resolution;
        this.colorMode = colorMode;
        this.ditherMode = ditherMode;
        this.videoParams = videoParams;
    }

    public Components.Resolution getResolution() {
        return resolution;
    }

    public Components.ColorMode getColorMode() {
        return colorMode;
    }

    public Components.DitherMode getDitherMode() {
        return ditherMode;
    }

    public Components.VideoParams getVideoParams() {
        return videoParams;
    }

    public static class Factory extends MechanicFactory {

        public Factory() {
            super("camera");
        }

        @Override
        public @Nullable NexoCameraMechanic getMechanic(@Nullable String itemID) {
            return (NexoCameraMechanic) super.getMechanic(itemID);
        }

        @Override
        public @Nullable NexoCameraMechanic getMechanic(@Nullable ItemStack itemStack) {
            return (NexoCameraMechanic) super.getMechanic(itemStack);
        }

        @Override
        public NexoCameraMechanic parse(ConfigurationSection section) {
            int width = section.getInt("resolution.width", 128);
            int height = section.getInt("resolution.height", 128);
            Components.Resolution resolution = new Components.Resolution(width, height);

            Components.ColorMode colorMode = Components.ColorMode.valueOf(
                    section.getString("color_mode", "COLOR"));

            Components.DitherMode ditherMode = Components.DitherMode.valueOf(
                    section.getString("dither_mode", "NONE"));

            int frameRate = section.getInt("video_params.frame_rate", 0);
            int maxFrames = section.getInt("video_params.max_frames", 0);

            Components.VideoParams videoParams = new Components.VideoParams(frameRate, maxFrames, false);

            return new NexoCameraMechanic(this, section, resolution, colorMode, ditherMode, videoParams);
        }
    }
}