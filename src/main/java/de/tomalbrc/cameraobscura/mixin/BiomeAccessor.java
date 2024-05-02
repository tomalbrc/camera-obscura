package de.tomalbrc.cameraobscura.mixin;

import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Biome.class)
public interface BiomeAccessor {
    @Invoker("getGrassColorFromTexture")
    public int invokeGetGrassColor();

    @Invoker("getFoliageColorFromTexture")
    public int invokeGetFoliageColorFromTexture();
}
