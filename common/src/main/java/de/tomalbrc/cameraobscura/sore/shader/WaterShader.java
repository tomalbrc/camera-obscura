package de.tomalbrc.cameraobscura.sore.shader;

import de.tomalbrc.cameraobscura.sore.Uniforms;
import de.tomalbrc.cameraobscura.sore.model.Model;
import org.joml.Vector4d;

// very outdated
// TODO: something
public final class WaterShader implements Shader {
    public void vertex(Model model, int index, Uniforms uniforms, VertexOut out) {
        int p = index * 3;
        double wx = model.positions()[p];
        double wy = model.positions()[p + 1];
        double wz = model.positions()[p + 2];
        //wy += (double) Math.sin(uniforms.time + wx * 0.5f + wz * 0.5f) * 0.5f;
        //Vector4d localPos = new Vector4d(wx, wy, wz, 1.0f);
        //Shader.populateVertexCommon(model, index, uniforms, out, localPos);
        //out.normalX = 0.0f;
        //out.normalY = 1.0f;
        //out.normalZ = 0.0f;
        //out.ao = 1.0f;

        Vector4d localPos = new Vector4d(model.positions()[p], model.positions()[p + 1], model.positions()[p + 2], 1.0f);
        Shader.populateVertexCommon(model, index, uniforms, out, localPos);

    }

    public int fragment(FragmentIn in, Uniforms uniforms) {
        int r = (int) (0x00 * uniforms.skyLight);
        int g = (int) (0x55 * uniforms.skyLight);
        int b = (int) (0xAA * uniforms.skyLight);
        return 0x88000000 | (r << 16) | (g << 8) | b;
    }
}
