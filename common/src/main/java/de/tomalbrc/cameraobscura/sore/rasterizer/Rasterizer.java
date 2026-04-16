package de.tomalbrc.cameraobscura.sore.rasterizer;

import de.tomalbrc.cameraobscura.sore.ScreenVertex;
import de.tomalbrc.cameraobscura.sore.Triangle;
import de.tomalbrc.cameraobscura.sore.Uniforms;
import de.tomalbrc.cameraobscura.sore.shader.FragmentIn;
import de.tomalbrc.cameraobscura.sore.shader.Shader;
import de.tomalbrc.cameraobscura.sore.shader.VertexOut;

public interface Rasterizer {
    void clear(int color);

    void drawTriangle(Shader shader, Triangle tri, FragmentIn fIn, boolean writeDepth, boolean doubleSided, Uniforms uniforms);

    ScreenVertex project(VertexOut v);

    Framebuffer getFramebuffer();
}