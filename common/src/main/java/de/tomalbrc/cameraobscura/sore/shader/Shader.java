package de.tomalbrc.cameraobscura.sore.shader;

import de.tomalbrc.cameraobscura.sore.Uniforms;
import de.tomalbrc.cameraobscura.sore.model.Model;
import org.joml.Vector4d;

public interface Shader {
    static int clamp255(double v) {
        return (int) (Math.clamp(v, 0f, 1f) * 255f);
    }

    static void populateVertexCommon(Model model, int index, Uniforms uniforms, VertexOut out, Vector4d localPos) {
        int p = index * 3;

        Vector4d worldPos = new Vector4d(localPos);
        uniforms.model.transform(worldPos);

        Vector4d clipPos = new Vector4d(localPos);
        uniforms.mvp.transform(clipPos);

        Vector4d normal = new Vector4d(model.mesh.normals[p], model.mesh.normals[p + 1], model.mesh.normals[p + 2], 0.0f);
        uniforms.normalTransform.transform(normal);

        out.clipX = clipPos.x;
        out.clipY = clipPos.y;
        out.clipZ = clipPos.z;
        out.clipW = clipPos.w;

        out.worldX = worldPos.x;
        out.worldY = worldPos.y;
        out.worldZ = worldPos.z;

        out.normalX = normal.x;
        out.normalY = normal.y;
        out.normalZ = normal.z;

        out.localNormalX = model.mesh.normals[p];
        out.localNormalY = model.mesh.normals[p + 1];
        out.localNormalZ = model.mesh.normals[p + 2];

        out.u = model.mesh.uvs[index * 2];
        out.v = model.mesh.uvs[index * 2 + 1];
        out.texture = model.textureForVertex(index);

        out.ao = model.ao == null ? 1 : model.ao[index];
        out.skyLight = model.skyLight == null ? 1 : model.skyLight.length == 1 ? model.skyLight[0] : model.skyLight[index];
        out.blockLight = model.blockLight == null ? 1 : model.blockLight.length == 1 ? model.blockLight[0] : model.blockLight[index];

        out.tint = model.mesh.tintIndices == null || model.mesh.tintIndices[index] == -1 ? 0xFF__FF_FF_FF : uniforms.tints.getInt(model.mesh.tintIndices[index]);

        out.shade = model.mesh.shade[index];
    }

    void vertex(Model model, int index, Uniforms uniforms, VertexOut out);

    int fragment(FragmentIn in, Uniforms uniforms);
}
