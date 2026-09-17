package de.tomalbrc.cameraobscura.paper;

import de.tomalbrc.cameraobscura.platform.Platforms;
import de.tomalbrc.cameraobscura.platform.ScheduledTask;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.lang.reflect.Method;

public class PaperEntityAnimationHandler {
    private ScheduledTask task;
    private static final Method AIR_MOVER_METHOD;

    static {
        try {
            Method m = LivingEntity.class.getDeclaredMethod("omnidirectionalAirMover");
            m.setAccessible(true);
            AIR_MOVER_METHOD = m;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

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
                    livingEntity.calculateEntityAnimation(isFlying(livingEntity));
                }
            }
        }
    }

    private boolean isFlying(LivingEntity livingEntity) {
        try {
            Object airMover = AIR_MOVER_METHOD.invoke(livingEntity);
            return airMover != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}