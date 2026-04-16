package de.tomalbrc.cameraobscura.sore.shader;

import de.tomalbrc.cameraobscura.sore.Texture;

public final class FragmentIn {
    public double worldX, worldY, worldZ;
    public double normalX, normalY, normalZ;
    public double localNormalX, localNormalY, localNormalZ;
    public double u, v, ao;
    public Texture texture;
    public double skyLight;
    public double blockLight;
    public boolean shade;
    public int tint;
}
