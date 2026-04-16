package de.tomalbrc.cameraobscura.platform;

import java.util.function.Consumer;

public interface Scheduler {
    ScheduledTask runTaskTimer(Consumer<ScheduledTask> task, long delayTicks, long periodTicks);
}