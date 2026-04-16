package de.tomalbrc.cameraobscura.platform;

import de.tomalbrc.cameraobscura.NexoHook;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.util.MojangAssetFetcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class PaperPlatform implements Platform {
    private final JavaPlugin plugin;
    private final ItemDataStore itemDataStore;
    private final AssetFetcher assetFetcher;

    public PaperPlatform(JavaPlugin plugin) {
        this.plugin = plugin;
        this.itemDataStore = createItemDataStore(plugin);
        this.assetFetcher = new MojangAssetFetcher(Bukkit.getMinecraftVersion(), plugin.getDataPath(), plugin.getSLF4JLogger());
    }

    private static ItemDataStore createItemDataStore(JavaPlugin plugin) {
        ItemDataStore store = new PaperItemDataStore(plugin);

        if (Bukkit.getPluginManager().isPluginEnabled("Nexo")) {
            store = NexoHook.getItemDataStore(plugin);
        }

        if (Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")) {
            store = new ItemsAdderAwareItemDataStore(plugin);
        }

        return store;
    }

    @Override
    public Path getConfigDir() {
        return plugin.getDataFolder().toPath();
    }

    @Override
    public String getMinecraftVersion() {
        return Bukkit.getMinecraftVersion();
    }

    @Override
    public AssetFetcher getAssetFetcher() {
        return assetFetcher;
    }

    @Override
    public Scheduler getScheduler() {
        return new PaperScheduler();
    }

    @Override
    public Logger getLogger() {
        return plugin.getSLF4JLogger();
    }

    @Override
    public MinecraftServer getMinecraftServer() {
        return MinecraftServer.getServer();
    }

    @Override
    public RegistryAccess getRegistryAccess() {
        return getMinecraftServer().registryAccess();
    }

    @Override
    public ItemDataStore getItemDataStore() {
        return itemDataStore;
    }

    @Override
    public Predicate<CommandSourceStack> getPermission(String permission, int defaultLevel) {
        PermissionLevel level = switch (defaultLevel) {
            case 1 -> PermissionLevel.MODERATORS;
            case 2 -> PermissionLevel.GAMEMASTERS;
            case 3 -> PermissionLevel.ADMINS;
            case 4 -> PermissionLevel.OWNERS;
            default -> PermissionLevel.ALL;
        };
        return source -> source.hasPermission(new Permission.HasCommandLevel(level), permission);
    }

    @Override
    public void renderBlock(Level level, BlockPos.MutableBlockPos pos, List<DrawCommand> commands) {

    }

    @Override
    public void renderEntity(Entity ent, List<DrawCommand> allCommands) {

    }

    private class PaperScheduler implements Scheduler {
        @Override
        public ScheduledTask runTaskTimer(Consumer<ScheduledTask> task, long delayTicks, long periodTicks) {
            BukkitTask[] ref = new BukkitTask[1];
            ref[0] = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                task.accept(ref[0]::cancel);
            }, delayTicks, periodTicks);
            return ref[0]::cancel;
        }
    }
}