package de.tomalbrc.cameraobscura;

import de.tomalbrc.cameraobscura.platform.ItemDataStore;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.entity.UniquelyIdentifyable;
import org.jetbrains.annotations.Nullable;

public class FabricItemDataStore implements ItemDataStore {
    @Override
    public boolean isLiveMap(ItemStack stack) {
        return stack.has(CustomContent.LIVE_MAP);
    }

    @Override
    public void setLiveMap(ItemStack stack, boolean live) {
        if (live) stack.set(CustomContent.LIVE_MAP, Unit.INSTANCE);
        else stack.remove(CustomContent.LIVE_MAP);
    }

    @Override
    @Nullable
    public EntityReference<UniquelyIdentifyable> getEntityRef(ItemStack stack) {
        return stack.get(CustomContent.ENTITY_REF);
    }

    @Override
    public void setEntityRef(ItemStack stack, @Nullable EntityReference<UniquelyIdentifyable> ref) {
        if (ref == null) stack.remove(CustomContent.ENTITY_REF);
        else stack.set(CustomContent.ENTITY_REF, ref);
    }

    @Override
    public Components.ColorMode getColorMode(ItemStack stack) {
        return stack.getOrDefault(CustomContent.CAMERA_COLOR_MODE, Components.ColorMode.COLOR);
    }

    @Override
    public void setColorMode(ItemStack stack, Components.ColorMode mode) {
        stack.set(CustomContent.CAMERA_COLOR_MODE, mode);
    }

    @Override
    public Components.Resolution getResolution(ItemStack stack) {
        return stack.getOrDefault(CustomContent.CAMERA_RESOLUTION, Components.Resolution.DEFAULT);
    }

    @Override
    public void setResolution(ItemStack stack, Components.Resolution resolution) {
        stack.set(CustomContent.CAMERA_RESOLUTION, resolution);
    }

    @Override
    public Components.VideoParams getVideoParams(ItemStack stack) {
        return stack.getOrDefault(CustomContent.CAMERA_VIDEO_PARAMS, Components.VideoParams.DEFAULT);
    }

    @Override
    public void setVideoParams(ItemStack stack, Components.VideoParams params) {
        stack.set(CustomContent.CAMERA_VIDEO_PARAMS, params);
    }

    @Override
    public Components.DitherMode getDitherMode(ItemStack stack) {
        return stack.getOrDefault(CustomContent.CAMERA_DITHER_MODE, Components.DitherMode.NONE);
    }

    @Override
    public void setDitherMode(ItemStack stack, Components.DitherMode mode) {
        stack.set(CustomContent.CAMERA_DITHER_MODE, mode);
    }

    @Override
    @Nullable
    public Components.MediaData getMediaData(ItemStack stack) {
        return stack.get(CustomContent.DATA);
    }

    @Override
    public void setMediaData(ItemStack stack, @Nullable Components.MediaData data) {
        if (data == null) stack.remove(CustomContent.DATA);
        else stack.set(CustomContent.DATA, data);
    }

    @Override
    public boolean hasLiveMap(ItemStack stack) {
        return stack.has(CustomContent.LIVE_MAP);
    }

    @Override
    public boolean hasEntityRef(ItemStack stack) {
        return stack.has(CustomContent.ENTITY_REF);
    }

    @Override
    public boolean hasMediaData(ItemStack stack) {
        return stack.has(CustomContent.DATA);
    }

    @Override
    public boolean hasResolution(ItemStack stack) {
        return stack.has(CustomContent.CAMERA_RESOLUTION);
    }

    @Override
    public boolean hasDitherMode(ItemStack stack) {
        return stack.has(CustomContent.CAMERA_DITHER_MODE);
    }

    @Override
    public boolean hasVideoParams(ItemStack stack) {
        return stack.has(CustomContent.CAMERA_VIDEO_PARAMS);
    }

    @Override
    public boolean hasColorMode(ItemStack stack) {
        return stack.has(CustomContent.CAMERA_COLOR_MODE);
    }
}