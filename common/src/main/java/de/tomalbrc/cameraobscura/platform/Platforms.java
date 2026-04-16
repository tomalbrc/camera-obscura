package de.tomalbrc.cameraobscura.platform;

public final class Platforms {
    private static Platform instance;

    public static void set(Platform platform) {
        if (instance != null) throw new IllegalStateException("Platform already set");
        instance = platform;
    }

    public static Platform get() {
        if (instance == null) throw new IllegalStateException("Platform not set");
        return instance;
    }
}