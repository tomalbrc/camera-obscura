package de.tomalbrc.cameraobscura;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class RPBlockState {
    public Map<String, Variant> variants;

    public static class Variant {
        public int x, y, z;
        public ResourceLocation model;
    }
}
