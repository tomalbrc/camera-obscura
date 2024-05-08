package de.tomalbrc.cameraobscura;

import de.tomalbrc.cameraobscura.color.BlockColors;
import de.tomalbrc.cameraobscura.command.CameraCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.joml.Vector2i;

public class CameraObscura implements ModInitializer {

    @Override
    public void onInitialize() {
        BlockColors.init();

        if (new Vector2i(1,1) == new Vector2i(1,1)) System.exit(0);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> CameraCommand.register(dispatcher));
    }
}
