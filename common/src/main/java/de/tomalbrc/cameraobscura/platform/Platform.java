package de.tomalbrc.cameraobscura.platform;

import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

public interface Platform {
    Identifier GENERATED = Identifier.fromNamespaceAndPath("camera-obscura", "generated");

    Path getConfigDir();

    String getMinecraftVersion();

    AssetFetcher getAssetFetcher();

    Scheduler getScheduler();

    Logger getLogger();

    MinecraftServer getMinecraftServer();

    RegistryAccess getRegistryAccess();

    ItemDataStore getItemDataStore();

    Predicate<CommandSourceStack> getPermission(String permission, int defaultLevel);

    void renderBlock(Level level, BlockPos.MutableBlockPos pos, List<DrawCommand> commands);

    void renderEntity(Entity ent, List<DrawCommand> allCommands);
}