package de.tomalbrc.cameraobscura.paper;

import de.tomalbrc.cameraobscura.renderer.ChunkMeshCache;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class PaperBlockChangeListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        ChunkMeshCache.onBlockChanged(((CraftWorld)event.getBlock().getWorld()).getHandle(), new BlockPos(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        ChunkMeshCache.onChunkLoaded(((CraftWorld)event.getWorld()).getHandle(), ((CraftChunk)event.getChunk()).getHandle(ChunkStatus.FULL));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent event) {
        ChunkMeshCache.onChunkUnloaded(((CraftWorld)event.getWorld()).getHandle(), ((CraftChunk)event.getChunk()).getHandle(ChunkStatus.FULL));
    }
}