package de.tomalbrc.cameraobscura.sore.shader;

import de.tomalbrc.cameraobscura.sore.Uniforms;
import de.tomalbrc.cameraobscura.sore.model.Model;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.joml.Vector4d;

public class SkyShader implements Shader {
    private static int hash(int x, int y, int z) {
        int h = x * 374761393 + y * 668265263 + z * 1168477447;
        h = (h ^ (h >>> 13)) * 1274126177;
        return h ^ (h >>> 16);
    }

    private static int lerpColor(int c1, int c2, double t) {
        t = clamp01(t);

        int r1 = (c1 >> 16) & 0xFF;
        int g1 = (c1 >> 8) & 0xFF;
        int b1 = c1 & 0xFF;

        int r2 = (c2 >> 16) & 0xFF;
        int g2 = (c2 >> 8) & 0xFF;
        int b2 = c2 & 0xFF;

        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static int blend(int src, int dst, double a) {
        a = clamp01(a);

        int sr = (src >> 16) & 0xFF;
        int sg = (src >> 8) & 0xFF;
        int sb = src & 0xFF;

        int dr = (dst >> 16) & 0xFF;
        int dg = (dst >> 8) & 0xFF;
        int db = dst & 0xFF;

        int r = (int) (sr * a + dr * (1.0f - a));
        int g = (int) (sg * a + dg * (1.0f - a));
        int b = (int) (sb * a + db * (1.0f - a));

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static double hash01(int seed) {
        seed = ((seed >>> 16) ^ seed) * 0x45d9f3b;
        seed = ((seed >>> 16) ^ seed) * 0x45d9f3b;
        seed = (seed >>> 16) ^ seed;
        return (seed & 0x7fffffff) / (double) 0x7fffffff;
    }

    private static double clamp01(double v) {
        return v < 0.0f ? 0.0f : (Math.min(v, 1.0f));
    }

    @Override
    public void vertex(Model model, int index, Uniforms uniforms, VertexOut out) {
        int p = index * 3;
        Vector4d localPos = new Vector4d(model.positions()[p], model.positions()[p + 1], model.positions()[p + 2], 1.0f);
        Shader.populateVertexCommon(model, index, uniforms, out, localPos);

        out.worldX = localPos.x;
        out.worldY = localPos.y;
        out.worldZ = localPos.z;

        Matrix4d viewRot = new Matrix4d(uniforms.view);
        viewRot.setTranslation(0, 0, 0);
        Matrix4d mvp = new Matrix4d(uniforms.proj).mul(viewRot).mul(uniforms.model);

        Vector4d clipPos = new Vector4d(localPos);
        mvp.transform(clipPos);

        out.clipX = clipPos.x;
        out.clipY = clipPos.y;
        out.clipZ = clipPos.z;
        out.clipW = clipPos.w;
    }

    @Override
    public int fragment(FragmentIn in, Uniforms uniforms) {
        double len = Math.sqrt(in.worldX * in.worldX + in.worldY * in.worldY + in.worldZ * in.worldZ);
        double dirX = in.worldX / len;
        double dirY = in.worldY / len;
        double dirZ = in.worldZ / len;

        Vector3d sunDir = new Vector3d(uniforms.sunDir).normalize();
        double sunY = sunDir.y;

        double dayFactor = clamp01(sunY * 2.0f + 0.2f);
        double sunsetFactor = clamp01(1.0f - Math.abs(sunY * 4.0f));

        int topDay = 0xFF78A7FF;
        int botDay = 0xFFC6D8FF;
        int topNight = 0xFF050510;
        int botNight = 0xFF000000;

        int skyTop = lerpColor(topNight, topDay, dayFactor);
        int skyBot = lerpColor(botNight, botDay, dayFactor);
        skyBot = lerpColor(skyBot, 0xFFFF7722, sunsetFactor * 0.8f);

        double t = (dirY + 1.0f) * 0.5f;
        int skyColor = lerpColor(skyBot, skyTop, t);

        Vector3d upRef = Math.abs(sunDir.y) > 0.99f
                ? new Vector3d(1.0f, 0.0f, 0.0f)
                : new Vector3d(0.0f, 1.0f, 0.0f);

        Vector3d sunRight = new Vector3d();
        sunDir.cross(upRef, sunRight).normalize();

        Vector3d sunUp = new Vector3d();
        sunRight.cross(sunDir, sunUp).normalize();

        double sx = dirX * sunRight.x + dirY * sunRight.y + dirZ * sunRight.z;
        double sy = dirX * sunUp.x + dirY * sunUp.y + dirZ * sunUp.z;
        double sz = dirX * sunDir.x + dirY * sunDir.y + dirZ * sunDir.z;

        double sunSize = 0.075f;

        double glowSize = sunSize * 4.5f; // size of halo
        double max = Math.max(Math.abs(sx), Math.abs(sy));
        double glowDist = max / glowSize;

        if (sz > 0.0f && glowDist < 1.0f) {
            double glow = 1.0f - glowDist;
            glow = glow * glow * 0.6f;
            int glowColor = 0xFFFFC870;
            skyColor = blend(glowColor, skyColor, glow * dayFactor);
        }

        if (sz > 0.0f && Math.abs(sx) < sunSize && Math.abs(sy) < sunSize) {
            double edge = max / sunSize;

            int sunColor = (edge > 0.88f)
                    ? 0xFFFFA23A   // orange border
                    : 0xFFFFE27A;

            skyColor = blend(sunColor, skyColor, 1.0f);
        }

        // Moon
        Vector3d moonDir = new Vector3d(sunDir).negate();

        Vector3d moonRight = new Vector3d();
        moonDir.cross(upRef, moonRight).normalize();

        Vector3d moonUp = new Vector3d();
        moonRight.cross(moonDir, moonUp).normalize();

        double mx = dirX * moonRight.x + dirY * moonRight.y + dirZ * moonRight.z;
        double my = dirX * moonUp.x + dirY * moonUp.y + dirZ * moonUp.z;
        double mz = dirX * moonDir.x + dirY * moonDir.y + dirZ * moonDir.z;

        double moonSize = 0.0375f;
        if (mz > 0.0f && Math.abs(mx) < moonSize && Math.abs(my) < moonSize) {
            double edge = Math.max(Math.abs(mx), Math.abs(my)) / moonSize;

            int moonColor = (edge > 0.88f)
                    ? 0xFFB8B8B8   // outline
                    : 0xFFE8E8E8;  // fill

            skyColor = blend(moonColor, skyColor, 1.0f);
        }

        if (sunY < 0.2f && dirY > 0.0f) {
            double starAlpha = 1.0f - clamp01((sunY + 0.1f) / 0.3f);

            double scale = 50.0f;
            double gx = Math.floor(dirX * scale);
            double gy = Math.floor(dirY * scale);
            double gz = Math.floor(dirZ * scale);

            int starSeed = hash((int) gx, (int) gy, (int) gz);
            double starNoise = hash01(starSeed);

            if (starNoise > 0.9f) {
                double dx = Math.abs(dirX * scale - (gx + 0.5f));
                double dy = Math.abs(dirY * scale - (gy + 0.5f));
                double dz = Math.abs(dirZ * scale - (gz + 0.5f));

                if (dx < 0.12f && dy < 0.12f && dz < 0.12f) {
                    skyColor = blend(0xFFFFFFFF, skyColor, starAlpha);
                }
            }
        }

        return skyColor;
    }
}