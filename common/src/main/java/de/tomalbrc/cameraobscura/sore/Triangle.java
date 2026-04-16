package de.tomalbrc.cameraobscura.sore;

public final class Triangle {
    public final ScreenVertex a, b, c;
    public final double avgDepth;

    public Triangle(ScreenVertex a, ScreenVertex b, ScreenVertex c) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.avgDepth = (a.v().clipW + b.v().clipW + c.v().clipW) / 3.0f;
    }
}
