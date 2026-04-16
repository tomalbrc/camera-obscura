package de.tomalbrc.cameraobscura.sore.shader;

import de.tomalbrc.cameraobscura.sore.PointLight;
import de.tomalbrc.cameraobscura.sore.Texture;
import de.tomalbrc.cameraobscura.sore.Uniforms;
import de.tomalbrc.cameraobscura.sore.model.Model;
import org.joml.Vector3d;
import org.joml.Vector4d;

public final class LitShader implements Shader {
    private static double specular(double nx, double ny, double nz, double wx, double wy, double wz, Vector3d lightDir, Vector3d cameraPos, double shininess) {
        double vx = cameraPos.x - wx;
        double vy = cameraPos.y - wy;
        double vz = cameraPos.z - wz;
        double vLen = Math.sqrt(vx * vx + vy * vy + vz * vz);
        if (vLen != 0.0f) {
            vx /= vLen;
            vy /= vLen;
            vz /= vLen;
        }
        double hx = lightDir.x + vx;
        double hy = lightDir.y + vy;
        double hz = lightDir.z + vz;
        double hLen = Math.sqrt(hx * hx + hy * hy + hz * hz);
        if (hLen != 0.0f) {
            hx /= hLen;
            hy /= hLen;
            hz /= hLen;
        }
        double ndoth = Math.max(0.0f, nx * hx + ny * hy + nz * hz);
        return Math.pow(ndoth, shininess);
    }

    private static double specularFromLight(Vector3d normal, Vector3d fragPos, Vector3d lightDir, Vector3d cameraPos, double shininess) {
        Vector3d viewDir = new Vector3d(cameraPos).sub(fragPos).normalize();
        Vector3d halfVec = new Vector3d(lightDir).add(viewDir).normalize();
        double ndoth = Math.max(0.0f, normal.dot(halfVec));
        return Math.pow(ndoth, shininess);
    }

    public void vertex(Model model, int index, Uniforms uniforms, VertexOut out) {
        int p = index * 3;
        Vector4d localPos = new Vector4d(model.positions()[p], model.positions()[p + 1], model.positions()[p + 2], 1.0f);
        Shader.populateVertexCommon(model, index, uniforms, out, localPos);
    }

    public int fragment(FragmentIn in, Uniforms uniforms) {
        double nx = Math.abs(in.localNormalX);
        double ny = in.localNormalY;
        double nz = Math.abs(in.localNormalZ);
        double faceLight;
        if (ny > 0.5f) faceLight = 1.0f;
        else if (ny < -0.5f) faceLight = 0.5f;
        else if (nz > nx) faceLight = 0.8f;
        else faceLight = 0.6f;
        double ambientLight = faceLight * uniforms.skyLight * in.ao;

        double sunSpec = specular(
                in.normalX, in.normalY, in.normalZ,
                in.worldX, in.worldY, in.worldZ,
                uniforms.sunDir,
                uniforms.cameraPos,
                uniforms.shininess
        ) * uniforms.specularStrength * uniforms.skyLight;

        double rAcc = ambientLight + sunSpec;
        double gAcc = ambientLight + sunSpec;
        double bAcc = ambientLight + sunSpec;

        Vector3d worldNormal = new Vector3d(in.normalX, in.normalY, in.normalZ);
        Vector3d fragPos = new Vector3d(in.worldX, in.worldY, in.worldZ);
        for (PointLight light : uniforms.pointLights) {
            Vector3d toLight = new Vector3d(light.position()).sub(fragPos);
            double dist = toLight.length();
            if (dist < 0.001f) continue;
            double att = 1.0f / (1.0f + light.linearAttenuation() * dist + light.quadraticAttenuation() * dist * dist);
            if (att <= 0.001f) continue;
            toLight.div(dist);
            double diff = Math.max(0.0f, worldNormal.dot(toLight));
            double pointSpec = specularFromLight(worldNormal, fragPos, toLight, uniforms.cameraPos, uniforms.shininess)
                    * uniforms.specularStrength;
            rAcc += (diff + pointSpec) * att * light.color().x;
            gAcc += (diff + pointSpec) * att * light.color().y;
            bAcc += (diff + pointSpec) * att * light.color().z;
        }

        Texture texture = in.texture != null ? in.texture : Texture.DEFAULT_TEXTURE;
        int tex = texture.sample(in.u, in.v);
        double texR = ((tex >> 16) & 0xFF) / 255f;
        double texG = ((tex >> 8) & 0xFF) / 255f;
        double texB = (tex & 0xFF) / 255f;
        int a = (tex >>> 24) & 0xFF;
        double finalR = texR * Math.min(rAcc, 2.0f);
        double finalG = texG * Math.min(gAcc, 2.0f);
        double finalB = texB * Math.min(bAcc, 2.0f);
        return (a << 24) | (Shader.clamp255(finalR) << 16) | (Shader.clamp255(finalG) << 8) | Shader.clamp255(finalB);
    }
}
