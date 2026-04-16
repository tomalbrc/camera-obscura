package de.tomalbrc.cameraobscura.sore.shader;

import de.tomalbrc.cameraobscura.sore.Uniforms;
import de.tomalbrc.cameraobscura.sore.model.Model;
import net.minecraft.util.ARGB;
import org.joml.Vector3d;
import org.joml.Vector4d;

public final class MatteShader implements Shader {
    @Override
    public void vertex(Model model, int index, Uniforms uniforms, VertexOut out) {
        int p = index * 3;
        Vector4d localPos = new Vector4d(model.positions()[p], model.positions()[p + 1], model.positions()[p + 2], 1.0f);
        Shader.populateVertexCommon(model, index, uniforms, out, localPos);
    }

    @Override
    public int fragment(FragmentIn in, Uniforms uniforms) {
        double nx = Math.abs(in.localNormalX);
        double ny = in.localNormalY;
        double nz = Math.abs(in.localNormalZ);
        double faceLight = 0.9f;

        if (in.shade) {
            if (ny > 0.5f) faceLight = 0.9f;
            else if (ny < -0.5f) faceLight = 0.6f;
            else if (nz > nx) faceLight = 0.8f;
            else faceLight = 0.7f;
        }

        double effectiveSky = Math.max(uniforms.skyLight * in.skyLight, 0.0f);
        double ambientLight = Math.min(1.0, in.blockLight + effectiveSky);
        ambientLight = Math.max(ambientLight, 0.1);

        double light = (ambientLight - Math.min(1.0, 0.8f - 0.9f * in.ao)) * faceLight;
        int tex = in.texture.sampleAnimated(in.u, in.v, uniforms.time);

        int alpha = (tex >>> 24) & 0xFF;
        double texR = ((tex >> 16) & 0xFF) / 255.0;
        double texG = ((tex >> 8) & 0xFF) / 255.0;
        double texB = (tex & 0xFF) / 255.0;

        double r = texR * light * (ARGB.red(in.tint) / 255.0);
        double g = texG * light * (ARGB.green(in.tint) / 255.0);
        double b = texB * light * (ARGB.blue(in.tint) / 255.0);

        Vector3d fragPos = new Vector3d(in.worldX, in.worldY, in.worldZ);
        double dist = fragPos.distance(uniforms.cameraPos);

        double fogFactor = (uniforms.fogEnd - dist) / (uniforms.fogEnd - uniforms.fogStart);
        fogFactor = Math.clamp(fogFactor, 0.0f, 1.0f);

        int fogColor = uniforms.fogColor;
        double fogR = ARGB.red(fogColor) / 255.0f;
        double fogG = ARGB.green(fogColor) / 255.0f;
        double fogB = ARGB.blue(fogColor) / 255.0f;

        double finalR = (r * fogFactor + fogR * (1.0f - fogFactor));
        double finalG = (g * fogFactor + fogG * (1.0f - fogFactor));
        double finalB = (b * fogFactor + fogB * (1.0f - fogFactor));

        int finalR255 = Shader.clamp255(finalR);
        int finalG255 = Shader.clamp255(finalG);
        int finalB255 = Shader.clamp255(finalB);

        return (alpha << 24) | (finalR255 << 16) | (finalG255 << 8) | finalB255;
    }
}