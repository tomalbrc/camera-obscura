package de.tomalbrc.cameraobscura.platform;

import java.util.concurrent.CompletableFuture;

public interface AssetFetcher {
    /**
     * Returns the raw bytes of an asset.
     *
     * @param path Full asset path, e.g. "assets/minecraft/textures/block/stone.png"
     * @return file content or null if not found / not yet initialized
     */
    byte[] getAsset(String path);

    CompletableFuture<Void> initialize();
}