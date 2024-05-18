package de.tomalbrc.cameraobscura;

import de.tomalbrc.cameraobscura.color.BlockColors;
import de.tomalbrc.cameraobscura.command.CameraCommand;
import de.tomalbrc.cameraobscura.item.CameraItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class CameraObscura implements ModInitializer {

    @Override
    public void onInitialize() {
        ModConfig.load();
        BlockColors.init();

        Registry.register(
                BuiltInRegistries.ITEM,
                new ResourceLocation("camera-obscura:camera"),
                new CameraItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE))
        );

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> CameraCommand.register(dispatcher));
    }
}
