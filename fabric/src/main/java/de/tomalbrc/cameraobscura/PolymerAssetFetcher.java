package de.tomalbrc.cameraobscura;

import de.tomalbrc.cameraobscura.platform.AssetFetcher;
import eu.pb4.polymer.autohost.impl.AutoHost;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;

import java.util.concurrent.CompletableFuture;

public class PolymerAssetFetcher implements AssetFetcher {
    private final ResourcePackBuilder builder;

    public PolymerAssetFetcher(ResourcePackBuilder resourcePackBuilder) {
        this.builder = resourcePackBuilder;
    }

    public static boolean isEnabled() {
        return AutoHost.config == null || AutoHost.config.enabled;
    }

    @Override
    public byte[] getAsset(String path) {
        return builder.getDataOrSource(path);
    }

    @Override
    public CompletableFuture<Void> initialize() {
        return CompletableFuture.completedFuture(null);
    }
}
