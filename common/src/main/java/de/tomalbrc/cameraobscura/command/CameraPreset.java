package de.tomalbrc.cameraobscura.command;

import com.google.gson.annotations.SerializedName;
import de.tomalbrc.cameraobscura.Components;
import de.tomalbrc.cameraobscura.platform.Platforms;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record CameraPreset(
        Identifier material,
        CameraOptions camera,
        DataComponentMap components
) {
    public record CameraOptions(
            Components.Resolution resolution,
            @SerializedName("color_mode")
            Components.ColorMode colorMode,
            @SerializedName("dither_mode")
            Components.DitherMode ditherMode,
            @SerializedName("video_params")
            Components.VideoParams videoParams
    ) {
        public static CameraOptions DEFAULT = new CameraOptions(
                Components.Resolution.DEFAULT,
                Components.ColorMode.COLOR,
                Components.DitherMode.NONE,
                Components.VideoParams.DEFAULT
        );
    }

    public ItemStack createItemStack() {
        ItemStack stack = BuiltInRegistries.ITEM.getValue(material).getDefaultInstance();

        for (var entry : components) {
            DataComponentType type = entry.type();
            stack.set(type, entry.value());
        }

        if (camera.resolution != null)Platforms.get().getItemDataStore().setResolution(stack, camera.resolution);
        if (camera.colorMode != null) Platforms.get().getItemDataStore().setColorMode(stack, camera.colorMode);
        if (camera.ditherMode != null) Platforms.get().getItemDataStore().setDitherMode(stack, camera.ditherMode);
        if (camera.videoParams != null && camera.videoParams.frameRate() > 0) {
            Platforms.get().getItemDataStore().setVideoParams(stack, camera.videoParams);
        }
        return stack;
    }
}