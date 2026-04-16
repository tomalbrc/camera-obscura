package de.tomalbrc.cameraobscura.sore;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.joml.Matrix4d;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class Uniforms {
    public final Matrix4d model = new Matrix4d();
    public final Matrix4d view = new Matrix4d();
    public final Matrix4d proj = new Matrix4d();
    public final Matrix4d mvp = new Matrix4d();
    public final Matrix4d normalTransform = new Matrix4d();

    public final Vector3d sunDir = new Vector3d();
    public final Vector3d cameraPos = new Vector3d();
    public final List<PointLight> pointLights = new ArrayList<>();
    public long time;
    public double skyLight;
    public double specularStrength;
    public double shininess;
    public IntList tints = new IntArrayList();
    public double fogEnd;
    public double fogStart;
    public int fogColor;
}