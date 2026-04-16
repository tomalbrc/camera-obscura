package de.tomalbrc.cameraobscura.util;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaterniondc;
import org.joml.Vector3dc;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class DisplayAccessor {
    private static final Field DATA_TRANSLATION_ID;
    private static final Field DATA_LEFT_ROTATION_ID;
    private static final Field DATA_SCALE_ID;
    private static final Field DATA_RIGHT_ROTATION_ID;

    private static final Method GET_ITEM_STACK;
    private static final Method GET_ITEM_TRANSFORM;

    static {
        try {
            DATA_TRANSLATION_ID = Display.class.getDeclaredField("DATA_TRANSLATION_ID");
            DATA_TRANSLATION_ID.setAccessible(true);
            DATA_LEFT_ROTATION_ID = Display.class.getDeclaredField("DATA_LEFT_ROTATION_ID");
            DATA_LEFT_ROTATION_ID.setAccessible(true);
            DATA_SCALE_ID = Display.class.getDeclaredField("DATA_SCALE_ID");
            DATA_SCALE_ID.setAccessible(true);
            DATA_RIGHT_ROTATION_ID = Display.class.getDeclaredField("DATA_RIGHT_ROTATION_ID");
            DATA_RIGHT_ROTATION_ID.setAccessible(true);

            GET_ITEM_STACK = Display.ItemDisplay.class.getDeclaredMethod("getItemStack");
            GET_ITEM_STACK.setAccessible(true);
            GET_ITEM_TRANSFORM = Display.ItemDisplay.class.getDeclaredMethod("getItemTransform");
            GET_ITEM_TRANSFORM.setAccessible(true);
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to initialize DisplayAccessor reflection", e);
        }
    }

    public static EntityDataAccessor<Vector3dc> getDataTranslationId() {
        try {
            return (EntityDataAccessor<Vector3dc>) DATA_TRANSLATION_ID.get(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot read Display.DATA_TRANSLATION_ID", e);
        }
    }

    public static EntityDataAccessor<Quaterniondc> getDataLeftRotationId() {
        try {
            return (EntityDataAccessor<Quaterniondc>) DATA_LEFT_ROTATION_ID.get(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot read Display.DATA_LEFT_ROTATION_ID", e);
        }
    }

    public static EntityDataAccessor<Vector3dc> getDataScaleId() {
        try {
            return (EntityDataAccessor<Vector3dc>) DATA_SCALE_ID.get(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot read Display.DATA_SCALE_ID", e);
        }
    }

    public static EntityDataAccessor<Quaterniondc> getDataRightRotationId() {
        try {
            return (EntityDataAccessor<Quaterniondc>) DATA_RIGHT_ROTATION_ID.get(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot read Display.DATA_RIGHT_ROTATION_ID", e);
        }
    }

    public static ItemStack getItemStack(Entity entity) {
        if (!(entity instanceof Display.ItemDisplay)) {
            throw new IllegalArgumentException("Entity is not an ItemDisplay");
        }
        try {
            return (ItemStack) GET_ITEM_STACK.invoke(entity);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke getItemStack on " + entity, e);
        }
    }

    public static ItemDisplayContext getItemTransform(Entity entity) {
        if (!(entity instanceof Display.ItemDisplay)) {
            throw new IllegalArgumentException("Entity is not an ItemDisplay");
        }
        try {
            return (ItemDisplayContext) GET_ITEM_TRANSFORM.invoke(entity);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke getItemTransform on " + entity, e);
        }
    }
}