package de.tomalbrc.cameraobscura;

import com.mojang.brigadier.CommandDispatcher;
import de.tomalbrc.cameraobscura.color.BlockColors;
import de.tomalbrc.cameraobscura.command.CameraCommand;
import de.tomalbrc.cameraobscura.command.PresetManager;
import de.tomalbrc.cameraobscura.item.CameraListener;
import de.tomalbrc.cameraobscura.paper.ItemFrameListener;
import de.tomalbrc.cameraobscura.paper.PaperBlockChangeListener;
import de.tomalbrc.cameraobscura.paper.PaperEntityAnimationHandler;
import de.tomalbrc.cameraobscura.paper.PaperLiveMapHandler;
import de.tomalbrc.cameraobscura.platform.PaperPlatform;
import de.tomalbrc.cameraobscura.platform.Platforms;
import de.tomalbrc.cameraobscura.util.BuiltinEntityModels;
import de.tomalbrc.cameraobscura.util.Constants;
import de.tomalbrc.cameraobscura.util.image.VideoPlaybackManager;
import net.minecraft.commands.CommandSourceStack;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.plugin.java.JavaPlugin;

public final class CameraObscuraPlugin extends JavaPlugin {
    static CameraObscuraPlugin plugin;
    PaperLiveMapHandler handler;
    PaperEntityAnimationHandler entityAnimationHandler;

    public static CameraObscuraPlugin getInstance() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;

        Platforms.set(new PaperPlatform(this));
        Platforms.get().getAssetFetcher().initialize().thenRun(() -> {
            BlockColors.init();
            BuiltinEntityModels.initModels();
        });

        getServer().getPluginManager().registerEvents(new CameraListener(), this);

        VideoPlaybackManager.startTicking();
        getServer().getPluginManager().registerEvents(new ItemFrameListener(), this);

        handler = new PaperLiveMapHandler(this);
        handler.start();

        entityAnimationHandler = new PaperEntityAnimationHandler();
        entityAnimationHandler.start();

        getServer().getPluginManager().registerEvents(new PaperBlockChangeListener(), this);

        CommandDispatcher<CommandSourceStack> dispatcher = ((CraftServer) Bukkit.getServer()).getServer().getCommands().getDispatcher();
        CameraCommand.register(dispatcher);

        PresetManager.loadPresets();

        int pluginId = 31186;
        Metrics metrics = new Metrics(this, pluginId);
    }

    @Override
    public void onDisable() {
        handler.stop();
        entityAnimationHandler.stop();
        VideoPlaybackManager.stopTicking();
        Constants.stop();
    }
}
