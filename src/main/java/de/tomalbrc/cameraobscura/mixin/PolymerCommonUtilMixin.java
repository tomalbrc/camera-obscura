package de.tomalbrc.cameraobscura.mixin;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Objects;

@Mixin(value = PolymerCommonUtils.class, remap = false)
public class PolymerCommonUtilMixin {
    @ModifyArg(method = "getClientJar", at = @At(value = "INVOKE", target = "Ljava/net/URL;<init>(Ljava/lang/String;)V"))
    private static String cameraobscura$latestJar(String spec) {
        if (!Objects.equals(spec, "https://piston-data.mojang.com/v1/objects/b88808bbb3da8d9f453694b5d8f74a3396f1a533/client.jar"))
            return "https://piston-data.mojang.com/v1/objects/b88808bbb3da8d9f453694b5d8f74a3396f1a533/client.jar";

        return spec;
    }
}
