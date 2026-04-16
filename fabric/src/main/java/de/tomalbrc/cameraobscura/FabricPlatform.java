package de.tomalbrc.cameraobscura;

import de.tomalbrc.cameraobscura.platform.*;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FabricPlatform implements Platform {
    private final Scheduler scheduler = new FabricScheduler();
    private final ItemDataStore itemDataStore = new FabricItemDataStore();
    private AssetFetcher assetFetcher;
    private MinecraftServer server;

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir().resolve("cameraobscura");
    }

    @Override
    public String getMinecraftVersion() {
        return FabricLoader.getInstance().getRawGameVersion();
    }

    @Override
    public AssetFetcher getAssetFetcher() {
        return assetFetcher;
    }

    public void setAssetFetcher(AssetFetcher assetFetcher) {
        this.assetFetcher = assetFetcher;
    }

    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    public Logger getLogger() {
        return CameraObscura.logger();
    }

    @Override
    public MinecraftServer getMinecraftServer() {
        return server;
    }

    public void setMinecraftServer(MinecraftServer server) {
        this.server = server;
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
        return Permissions.require(permission, defaultLevel);
    }

    @Override
    public void renderBlock(Level level, BlockPos.MutableBlockPos pos, List<DrawCommand> commands) {
        var attachment = BlockAwareAttachment.get(level, pos);
        if (attachment != null && attachment.holder() != null) {
            PolymerHolderRenderer.render(attachment.holder(), commands, 0);
        }
    }

    @Override
    public void renderEntity(Entity ent, List<DrawCommand> allCommands) {
        var holders = ((HolderAttachmentHolder) ent).polymerVE$getHolders();
        if (holders != null) {
            for (HolderAttachment holder : holders) {
                if (holder.holder() != null)
                    PolymerHolderRenderer.render(holder.holder(), allCommands, ent.getBbHeight());
            }
        }
    }

    private static class FabricScheduler implements Scheduler {
        private final List<Task> tasks = new LinkedList<>();
        private boolean registered = false;

        @Override
        public ScheduledTask runTaskTimer(Consumer<ScheduledTask> runnable, long delayTicks, long periodTicks) {
            Task task = new Task(runnable, delayTicks, periodTicks);
            tasks.add(task);
            ensureListening();
            return task::cancel;
        }

        private void ensureListening() {
            if (!registered) {
                ServerTickEvents.END_SERVER_TICK.register(server -> tick());
                registered = true;
            }
        }

        private void tick() {
            tasks.removeIf(task -> {
                if (task.cancelled) return true;

                if (task.delay > 0) {
                    task.delay--;
                    return false;
                }

                if (task.period == 0) {
                    task.runnable.accept(task::cancel);
                    return true;
                }

                task.ticksUntilNext--;
                if (task.ticksUntilNext <= 0) {
                    task.runnable.accept(task::cancel);
                    task.ticksUntilNext = task.period;
                }
                return false;
            });
        }

        private static class Task {
            final Consumer<ScheduledTask> runnable;
            final long period;
            long delay;
            long ticksUntilNext;
            volatile boolean cancelled = false;

            Task(Consumer<ScheduledTask> runnable, long delay, long period) {
                this.runnable = runnable;
                this.delay = delay;
                this.period = period;
                this.ticksUntilNext = period;
            }

            void cancel() {
                cancelled = true;
            }
        }
    }
}
