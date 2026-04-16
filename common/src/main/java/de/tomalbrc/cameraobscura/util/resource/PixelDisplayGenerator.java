package de.tomalbrc.cameraobscura.util.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.joml.Matrix4d;
import org.jspecify.annotations.NonNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class PixelDisplayGenerator {
    private static final int WIDTH = 32;
    private static final int HEIGHT = 32;
    private static final String NAMESPACE = "cameraobscura";

    private static final double PIXEL_SIZE = 1f / 10f;
    private static final double SCALE = 2f;

    public static byte[] generatePixelModel() {
        JsonObject model = new JsonObject();
        model.addProperty("parent", "minecraft:item/handheld");

        JsonArray elements = new JsonArray();
        JsonObject element = new JsonObject();
        element.addProperty("light_emission", 15);
        element.addProperty("shade", false);

        JsonArray from = new JsonArray();
        from.add(0);
        from.add(0);
        from.add(0);
        element.add("from", from);

        JsonArray to = new JsonArray();
        to.add(0.5);
        to.add(0.5);
        to.add(0);
        element.add("to", to);

        JsonObject faces = new JsonObject();
        JsonObject faceTemplate = new JsonObject();
        faceTemplate.addProperty("texture", "#all");
        faceTemplate.addProperty("tintindex", 0);

        JsonArray uv = new JsonArray();
        uv.add(0);
        uv.add(0);
        uv.add(16);
        uv.add(16);
        faceTemplate.add("uv", uv);

        faces.add("south", faceTemplate);
        faces.add("north", faceTemplate);
        element.add("faces", faces);
        elements.add(element);
        model.add("elements", elements);

        JsonObject textures = new JsonObject();
        textures.addProperty("all", NAMESPACE + ":item/display_white");
        model.add("textures", textures);

        JsonObject display = new JsonObject();

        JsonObject hand = new JsonObject();
        hand.add("rotation", createArray(0, 0, 0));
        hand.add("translation", createArray(-4, 0, 0));

        JsonObject third = new JsonObject();
        third.add("rotation", createArray(0, -90, 0));
        third.add("scale", createArray(0.75f, 0.75f, 0.75f));
        third.add("translation", createArray(-4, 0, -4));

        JsonObject gui = new JsonObject();
        gui.add("rotation", createArray(0, 0, 0));
        gui.add("scale", createArray(1, 1, 1));
        gui.add("translation", createArray(0, 0, 0));

        display.add("gui", gui);
        display.add("thirdperson_righthand", third);
        display.add("thirdperson_lefthand", third);
        display.add("firstperson_righthand", hand);
        display.add("firstperson_lefthand", hand);

        model.add("display", display);

        return model.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static @NonNull JsonArray createArray(double x, double y, double z) {
        JsonArray arr = new JsonArray();
        arr.add(x);
        arr.add(y);
        arr.add(z);
        return arr;
    }

    public static byte[] generateItemDefinition() {
        JsonObject definition = new JsonObject();

        JsonObject composite = new JsonObject();
        composite.addProperty("type", "minecraft:composite");
        JsonArray children = new JsonArray();

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                children.add(createPixelInstance(x, y));
            }
        }
        composite.add("models", children);

        definition.add("model", composite);
        definition.addProperty("hand_animation_on_swap", false);
        definition.addProperty("oversized_in_gui", true);

        return definition.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static JsonObject createPixelInstance(int x, int y) {
        JsonObject instance = new JsonObject();
        instance.addProperty("type", "minecraft:model");
        instance.addProperty("model", NAMESPACE + ":item/pixel");

        double px = (x / 2.0f);
        double py = (y / 2.0f);

        Matrix4d matrix = new Matrix4d();
        matrix.identity()
                .scale(1f)
                .translate(px / 16f, py / 16f, 0);

        double[] colMaj = new double[16];
        matrix.get(colMaj);
        double[] rowMaj = new double[16];
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                rowMaj[j * 4 + i] = colMaj[i * 4 + j];

        JsonArray matrixArray = new JsonArray();
        for (double v : rowMaj) matrixArray.add(v);
        instance.add("transformation", matrixArray);

        int pixelIndex = y * WIDTH + x;
        JsonObject tintSource = new JsonObject();
        tintSource.addProperty("type", "minecraft:custom_model_data");
        tintSource.addProperty("index", pixelIndex);

        JsonArray defaultColor = new JsonArray();
        defaultColor.add(1.0f);
        defaultColor.add(1.0f);
        defaultColor.add(1.0f);
        tintSource.add("default", defaultColor);

        JsonArray tints = new JsonArray();
        tints.add(tintSource);
        instance.add("tints", tints);

        return instance;
    }

    public static byte[] generateWhiteImage() {
        BufferedImage img = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 128; y++)
            for (int x = 0; x < 128; x++)
                img.setRGB(x, y, 0xFFFFFFFF);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, "PNG", baos);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate white PNG", e);
        }
        return baos.toByteArray();
    }
}