package de.tomalbrc.cameraobscura.platform;

import com.google.common.collect.ImmutableMap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import de.tomalbrc.cameraobscura.Components;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.entity.UniquelyIdentifyable;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface ItemDataStore {
    static ResolvableProfile createCameraProfile() {
        UUID id = UUID.fromString("7c066556-e0bd-4006-a424-707d7436ebeb");

        var map = ImmutableMap.<String, Property>builder().put("textures", new Property(
                "textures",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDZkMTVlNzJhY2YyMTliYzFkYThhZTc2ODZmNGY4M2M3NzNhZGNiNGY4ZmFjYzg3Y2JiZDQzZWU3OTA1N2YzIn19fQ=="
        ));
        GameProfile profile = new GameProfile(id, "camera", new PropertyMap(map.build().asMultimap()));

        return ResolvableProfile.createResolved(profile);
    }

    boolean isLiveMap(ItemStack stack);

    void setLiveMap(ItemStack stack, boolean live);

    @Nullable
    EntityReference<UniquelyIdentifyable> getEntityRef(ItemStack stack);

    void setEntityRef(ItemStack stack, @Nullable EntityReference<UniquelyIdentifyable> ref);

    Components.ColorMode getColorMode(ItemStack stack);

    void setColorMode(ItemStack stack, Components.ColorMode mode);

    Components.Resolution getResolution(ItemStack stack);

    void setResolution(ItemStack stack, Components.Resolution resolution);

    Components.VideoParams getVideoParams(ItemStack stack);

    void setVideoParams(ItemStack stack, Components.VideoParams params);

    Components.DitherMode getDitherMode(ItemStack stack);

    void setDitherMode(ItemStack stack, Components.DitherMode mode);

    @Nullable
    Components.MediaData getMediaData(ItemStack stack);

    void setMediaData(ItemStack stack, @Nullable Components.MediaData data);

    boolean hasLiveMap(ItemStack stack);

    boolean hasEntityRef(ItemStack stack);

    boolean hasMediaData(ItemStack stack);

    boolean hasResolution(ItemStack stack);

    boolean hasDitherMode(ItemStack stack);

    boolean hasVideoParams(ItemStack stack);

    boolean hasColorMode(ItemStack stack);

    default ItemStack createCameraItem() {
        var item = Items.PAPER.getDefaultInstance();
        item.set(DataComponents.ITEM_MODEL, Identifier.withDefaultNamespace("spyglass"));
        item.set(DataComponents.ITEM_NAME, net.minecraft.network.chat.Component.literal("Camera").withStyle(ChatFormatting.GOLD));
        item.set(DataComponents.MAX_STACK_SIZE, 1);
        Platforms.get().getItemDataStore().setResolution(item, new Components.Resolution(128, 128));
        Platforms.get().getItemDataStore().setVideoParams(item, new Components.VideoParams(0, 1, false));
        return item;
    }

    default ItemStack createCamcorderItem() {
        var item = Items.PAPER.getDefaultInstance();
        item.set(DataComponents.ITEM_MODEL, Identifier.withDefaultNamespace("spyglass"));
        item.set(DataComponents.ITEM_NAME, net.minecraft.network.chat.Component.literal("Camcorder").withStyle(ChatFormatting.GOLD));
        item.set(DataComponents.MAX_STACK_SIZE, 1);
        Platforms.get().getItemDataStore().setResolution(item, new Components.Resolution(128, 128));
        Platforms.get().getItemDataStore().setVideoParams(item, new Components.VideoParams(20, 20 * 10, false));
        return item;
    }
}