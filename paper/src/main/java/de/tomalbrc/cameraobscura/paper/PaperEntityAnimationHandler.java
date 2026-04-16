package de.tomalbrc.cameraobscura.paper;

import de.tomalbrc.cameraobscura.platform.Platforms;
import de.tomalbrc.cameraobscura.platform.ScheduledTask;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.FlyingAnimal;

public class PaperEntityAnimationHandler {
    private ScheduledTask task;

    public void start() {
        task = Platforms.get().getScheduler().runTaskTimer(this::tick, 0L, 1L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void tick(ScheduledTask task) {
        for (ServerLevel level : MinecraftServer.getServer().getAllLevels()) {
            for (Entity entity : level.getEntities().getAll()) {
                if (entity instanceof ServerPlayer serverPlayer) {
                    serverPlayer.xo = serverPlayer.getX();
                    serverPlayer.yo = serverPlayer.getY();
                    serverPlayer.zo = serverPlayer.getZ();
                }

                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.calculateEntityAnimation(livingEntity instanceof FlyingAnimal);
                }
            }
        }
    }
}