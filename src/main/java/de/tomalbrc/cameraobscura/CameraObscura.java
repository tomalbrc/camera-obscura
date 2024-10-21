package de.tomalbrc.cameraobscura;

import de.tomalbrc.cameraobscura.color.BlockColors;
import de.tomalbrc.cameraobscura.command.CameraCommand;
import de.tomalbrc.cameraobscura.item.CameraItem;
import de.tomalbrc.cameraobscura.util.BuiltinEntityModels;
import de.tomalbrc.cameraobscura.util.RPHelper;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class CameraObscura implements ModInitializer {

    @Override
    public void onInitialize() {
        BuiltinEntityModels.initModels();
        BlockColors.init();

        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("camera-obscura", "camera"));
        Registry.register(
                BuiltInRegistries.ITEM,
                key,
                new CameraItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).setId(key))
        );

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> CameraCommand.register(dispatcher));

        PolymerResourcePackUtils.RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT.register(resourcePackBuilder -> RPHelper.resourcePackBuilder = resourcePackBuilder);
    }
}
