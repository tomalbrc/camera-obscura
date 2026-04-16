package de.tomalbrc.cameraobscura.model.resource.state;

import com.google.gson.annotations.SerializedName;
import net.minecraft.resources.Identifier;

public class Variant {
    @SerializedName("x")
    public int x;
    @SerializedName("y")
    public int y;
    @SerializedName("z")
    public int z;

    @SerializedName("uvlock")
    public boolean uvlock;

    @SerializedName("model")
    public Identifier model;
}
