package de.tomalbrc.cameraobscura.render;

import de.tomalbrc.cameraobscura.util.RPHelper;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class RPModel {
    ResourceLocation parent;

    Map<String, String> textures;

    public Map<String, ResourceLocation> collectTextures() {
        Map<String, ResourceLocation> textures = new Object2ObjectOpenHashMap<>();
        this.textures.forEach((key,value) -> {
            if (!value.startsWith("#")) {
                textures.put(key, new ResourceLocation(value));
            }
        });

        ResourceLocation parent = this.parent;
        while (parent != null && parent.getPath() != null && !parent.getPath().isEmpty()) {
            var child = RPHelper.loadModel(parent.getPath());
            if (child != null) {
                if (child.textures != null) child.textures.forEach((key,value) -> {
                    if (!value.startsWith("#")) {
                        textures.put(key, new ResourceLocation(value));
                    }
                });

                parent = child.parent;
            } else {
                break;
            }
        }
        return textures;
    }
}
