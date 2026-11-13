package de.tomalbrc.cameraobscura;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.tomalbrc.cameraobscura.json.CachedIdentifierDeserializer;
import de.tomalbrc.cameraobscura.platform.Platforms;
import net.minecraft.resources.Identifier;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    private static final Path CONFIG_FILE_PATH = Platforms.get().getConfigDir().resolve("config.json");
    private static final Gson gson = new GsonBuilder()
            .registerTypeHierarchyAdapter(Identifier.class, new CachedIdentifierDeserializer())
            .setPrettyPrinting()
            .create();
    private static ModConfig instance;
    public int renderDistance = 32;

    public boolean showSystemMessages = false;

    public boolean renderEntities = true;

    public boolean fullbright = false;
    public int fov = 70;
    public int biomeBlend = 1;
    public int ssaa = 2;
    public int maxChunkRebuildsPerTick = 5;
    public boolean debug = false;
    public boolean cameraConsumesItem = true;
    public String cameraConsumeItem = "map";

    public int commandPermissionLevel = 2;

    public static ModConfig getInstance() {
        if (instance == null) {
            if (!load())
                save();
        }
        return instance;
    }

    public static boolean load() {
        try {
            Path parent = CONFIG_FILE_PATH.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (!Files.exists(CONFIG_FILE_PATH)) {
            instance = new ModConfig();
            save();
            return true;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE_PATH.toFile())) {
            ModConfig loaded = gson.fromJson(reader, ModConfig.class);
            instance = loaded != null ? loaded : new ModConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    private static void save() {
        try (FileOutputStream stream = new FileOutputStream(CONFIG_FILE_PATH.toFile())) {
            stream.write(gson.toJson(instance).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}