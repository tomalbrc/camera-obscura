package de.tomalbrc.cameraobscura.command;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import de.tomalbrc.cameraobscura.platform.Platforms;
import de.tomalbrc.cameraobscura.util.resource.RPHelper;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.RegistryOps;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PresetManager {
    private static final Map<String, CameraPreset> PRESETS = new HashMap<>();

    public static void loadPresets() {
        PRESETS.clear();
        Path configDir = Platforms.get().getConfigDir();
        Path presetsFile = configDir.resolve("presets.json");
        if (!Files.exists(presetsFile)) {

            try {
                Files.createDirectories(configDir);
                try (Writer writer = new OutputStreamWriter(Files.newOutputStream(presetsFile), StandardCharsets.UTF_8)) {
                    writer.write(DEFAULT_PRESETS_JSON);
                }
            } catch (IOException e) {
                Platforms.get().getLogger().error("Failed to create default presets file", e);
            }
        }

        try (Reader reader = new FileReader(presetsFile.toFile())) {
            var t = new TypeToken<Map<String, CameraPreset>>() {
            }.getType();

            Map<String, CameraPreset> root = RPHelper.gson.fromJson(reader, t);
            PRESETS.putAll(root);

            Platforms.get().getLogger().info("Loaded {} camera presets", PRESETS.size());
        } catch (IOException e) {
            Platforms.get().getLogger().error("Failed to load presets file", e);
        }
    }

    public static CameraPreset getPreset(String name) {
        return PRESETS.get(name);
    }

    public static Set<String> getPresetNames() {
        return PRESETS.keySet();
    }

    private static final String DEFAULT_PRESETS_JSON = """
        {
          "camera": {
            "material": "paper",
            "camera": {
              "resolution": { "width": 128, "height": 128 },
              "color_mode": "COLOR",
              "dither_mode": "NONE",
              "video_params": { "frame_rate": 0, "max_frames": 1, "loop_playback": false }
            },
            "components": {
              "minecraft:item_name": { "text": "Camera", "color": "gold" },
              "minecraft:max_stack_size": 1,
              "minecraft:item_model": "spyglass"
            }
          },
          "camcorder": {
            "material": "paper",
            "camera": {
              "resolution": { "width": 128, "height": 128 },
              "color_mode": "COLOR",
              "dither_mode": "NONE",
              "video_params": { "frame_rate": 20, "max_frames": 200, "loop_playback": false }
            },
            "components": {
              "minecraft:item_name": { "text": "Camcorder", "color": "gold" },
              "minecraft:max_stack_size": 1,
              "minecraft:item_model": "spyglass"
            }
          },
          "instant": {
            "material": "paper",
            "camera": {
              "resolution": { "width": 256, "height": 256 },
              "color_mode": "SEPIA"
            },
            "components": {
              "minecraft:item_model": "spyglass",
              "minecraft:item_name": { "text": "Instant Camera", "italic": false }
            }
          },
          "polaroid": {
            "material": "paper",
            "camera": {
              "resolution": { "width": 512, "height": 512 },
              "color_mode": "GRAYSCALE"
            },
            "components": {
              "minecraft:item_name": { "text": "Polaroid", "color": "white" },
              "minecraft:item_model": "spyglass",
              "minecraft:custom_model_data": 1
            }
          },
          "pro_camera": {
            "material": "ender_eye",
            "camera": {
              "resolution": { "width": 128, "height": 128 },
              "color_mode": "COLOR",
              "dither_mode": "NONE",
              "video_params": { "frame_rate": 30, "max_frames": 600, "loop_playback": false }
            },
            "components": {
              "minecraft:item_name": { "text": "Pro Camera", "color": "light_purple", "bold": true },
              "minecraft:max_stack_size": 1,
              "minecraft:custom_model_data": 2
            }
          },
          "handycam": {
            "material": "clock",
            "camera": {
              "resolution": { "width": 128, "height": 128 },
              "color_mode": "COLOR",
              "dither_mode": "BAYER_2X2",
              "video_params": { "frame_rate": 15, "max_frames": 450, "loop_playback": true }
            },
            "components": {
              "minecraft:item_name": { "text": "Handycam", "color": "aqua" },
              "minecraft:max_stack_size": 1
            }
          },
          "cctv": {
            "material": "observer",
            "camera": {
              "resolution": { "width": 128, "height": 128 },
              "color_mode": "MONOCHROME",
              "dither_mode": "SIERRA",
              "video_params": { "frame_rate": 5, "max_frames": 1000, "loop_playback": false }
            },
            "components": {
              "minecraft:item_name": { "text": "CCTV Camera", "color": "dark_gray" },
              "minecraft:max_stack_size": 1
            }
          }
        }
        """;

    public static class ComponentMapDeserializer implements JsonDeserializer<DataComponentMap> {
        @Override
        public DataComponentMap deserialize(JsonElement json, java.lang.reflect.Type type, JsonDeserializationContext ctx) throws JsonParseException {
            if (json == null) return DataComponentMap.EMPTY;

            var ops = RegistryOps.create(JsonOps.INSTANCE, Platforms.get().getRegistryAccess());
            var res = DataComponentMap.CODEC.decode(ops, json);
            if (res.hasResultOrPartial()) {
                return res.resultOrPartial().orElseThrow().getFirst();
            }

            return null;
        }
    }
}