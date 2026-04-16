package de.tomalbrc.cameraobscura.renderer;

public abstract class AbstractRenderer<T> implements Renderer<T> {
    protected final int width;
    protected final int height;
    protected final int renderDistance;

    public AbstractRenderer(int width, int height, int renderDistance) {
        this.width = width;
        this.height = height;
        this.renderDistance = renderDistance;
    }
}
