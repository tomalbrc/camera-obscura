package de.tomalbrc.cameraobscura.sore.pipeline;

import de.tomalbrc.cameraobscura.sore.Camera;

public interface RenderPipeline {
    void beginFrame(FrameContext ctx);

    void draw(DrawCommand cmd);

    void endFrame();

    Camera getCamera();

    void setCamera(Camera camera);
}
