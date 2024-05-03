package de.tomalbrc.cameraobscura;

import de.tomalbrc.cameraobscura.color.BlockColors;
import de.tomalbrc.cameraobscura.command.CameraCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class CameraObscura implements ModInitializer {

    @Override
    public void onInitialize() {
        BlockColors.init();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> CameraCommand.register(dispatcher));
    }
}
