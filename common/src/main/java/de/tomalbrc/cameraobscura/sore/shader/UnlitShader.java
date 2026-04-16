package de.tomalbrc.cameraobscura.sore.shader;

import de.tomalbrc.cameraobscura.sore.Texture;
import de.tomalbrc.cameraobscura.sore.Uniforms;
import de.tomalbrc.cameraobscura.sore.model.Model;
import org.joml.Vector4d;

public final class UnlitShader implements Shader {
    public void vertex(Model model, int index, Uniforms uniforms, VertexOut out) {
        int p = index * 3;
        Vector4d localPos = new Vector4d(model.positions()[p], model.positions()[p + 1], model.positions()[p + 2], 1.0f);
        Shader.populateVertexCommon(model, index, uniforms, out, localPos);
    }

    public int fragment(FragmentIn in, Uniforms uniforms) {
        Texture texture = in.texture != null ? in.texture : Texture.DEFAULT_TEXTURE;
        return texture.sample(in.u, in.v);
    }
}
