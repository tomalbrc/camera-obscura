package de.tomalbrc.cameraobscura.paper;

import de.tomalbrc.cameraobscura.ModConfig;
import de.tomalbrc.cameraobscura.platform.Platforms;
import de.tomalbrc.cameraobscura.platform.ScheduledTask;
import de.tomalbrc.cameraobscura.renderer.WorldRenderer;
import de.tomalbrc.cameraobscura.util.ImageUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.entity.UniquelyIdentifyable;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PaperLiveMapHandler implements Listener {
    private final JavaPlugin plugin;
    private final Map<UUID, WorldRenderer> renderers = new HashMap<>();
    private final Map<UUID, CompletableFuture<Void>> futures = new HashMap<>();
    private ScheduledTask task;

    public PaperLiveMapHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        task = Platforms.get().getScheduler().runTaskTimer(this::processPlayers, 0L, 1L);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }

        PlayerQuitEvent.getHandlerList().unregister(this);
        renderers.clear();
        futures.clear();
    }

    private void processPlayers(ScheduledTask task) {
        for (Player bukkitPlayer : Bukkit.getOnlinePlayers()) {
            ServerPlayer player = ((CraftPlayer) bukkitPlayer).getHandle();
            checkItem(player, player.getMainHandItem());
            checkItem(player, player.getOffhandItem());
        }
    }

    private void checkItem(ServerPlayer player, ItemStack itemStack) {
        if (itemStack.isEmpty()) return;

        var store = Platforms.get().getItemDataStore();

        if (store.hasMediaData(itemStack))
            return;

        if (store.hasEntityRef(itemStack) && store.hasLiveMap(itemStack) && itemStack.has(DataComponents.MAP_ID)) {
            MapId mapId = itemStack.get(DataComponents.MAP_ID);
            var e = store.getEntityRef(itemStack).getEntity(player.level(), UniquelyIdentifyable.class);
            if (e == null) {
                store.setEntityRef(itemStack, null);
                return;
            }

            UUID uuid = player.getUUID();
            WorldRenderer renderer = renderers.get(uuid);
            CompletableFuture<Void> future = futures.get(uuid);

            if (e instanceof LivingEntity entity && (renderer == null || renderer.entity != e)) {
                renderer = new WorldRenderer(entity.level(), 128, 128, ModConfig.getInstance().renderDistance);
                renderer.entity = entity;
                renderers.put(uuid, renderer);
                futures.remove(uuid);
            }

            if (e instanceof LivingEntity entity && (future == null || future.isDone())) {
                WorldRenderer finalRenderer = renderer;
                future = CompletableFuture.runAsync(() -> {
                    try {
                        finalRenderer.updateChunksInRange(entity.level(), false);
                        var img = finalRenderer.render();
                        var mapColors = ImageUtils.imageToMapColors(img);

                        // TODO: setcolor in mapData?

                        Packet<?> packet = new ClientboundMapItemDataPacket(
                                mapId, (byte) 0, false,
                                Optional.empty(),
                                Optional.of(new MapItemSavedData.MapPatch(0, 0, 128, 128, mapColors))
                        );
                        player.connection.send(packet);
                    } catch (Throwable t) {
                        Platforms.get().getLogger().error("Error while rendering", t);
                    }
                });
                
                futures.put(uuid, future);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        renderers.remove(uuid);
        futures.remove(uuid);
    }
}