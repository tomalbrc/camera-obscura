package de.tomalbrc.cameraobscura.renderer.entity;

import de.tomalbrc.cameraobscura.model.resource.RPModel;
import de.tomalbrc.cameraobscura.model.triangle.ModelTesselator;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import de.tomalbrc.cameraobscura.util.resource.ItemModelPathResolver;
import de.tomalbrc.cameraobscura.util.resource.RPHelper;
import de.tomalbrc.cameraobscura.util.resource.model.GeneratedItemModel;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.joml.Matrix4d;
import org.joml.Matrix4dc;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class ItemStackRenderer {
    public static final Map<Identifier, RPModel> ITEM_MODELS = new IdentityHashMap<>();

    public static void renderDeferred(List<DrawCommand> pipeline, ItemStack itemStack, ItemDisplayContext displayContext, Matrix4dc transform) {
        List<RPModel.View> views = getModels(itemStack);
        if (views != null && !views.isEmpty()) {
            for (RPModel.View view : views) {
                ModelTesselator tri = new ModelTesselator(view);
                var model = new Model(tri.build());

                var displayTransform = view.model().display(displayContext, transform.get(new Matrix4d()));
                if (displayTransform == null)
                    displayTransform = transform;

                var col = IntList.of(0xFF_000000 | itemStack.getOrDefault(DataComponents.DYED_COLOR, new DyedItemColor(0xFFFFFF)).rgb());
                pipeline.add(new DrawCommand(RenderType.ITEM, model, displayTransform, col));
            }
        }
    }

    public static void render(RenderPipeline pipeline, ItemStack itemStack, ItemDisplayContext displayContext, Matrix4dc transform) {
        render(pipeline, itemStack, displayContext, transform, 1, 1);
    }

    public static void render(RenderPipeline pipeline, ItemStack itemStack, ItemDisplayContext displayContext, Matrix4dc transform, double block, double sky) {
        List<RPModel.View> views = getModels(itemStack);
        if (views != null && !views.isEmpty()) {
            for (RPModel.View view : views) {
                ModelTesselator tri = new ModelTesselator(view);
                var mesh = tri.build();

                if (mesh != null) {
                    var model = new Model(mesh, block, sky);

                    var displayTransform = view.model().display(displayContext, transform.get(new Matrix4d()));
                    if (displayTransform == null)
                        displayTransform = transform;

                    var col = IntList.of(0xFF_000000 | itemStack.getOrDefault(DataComponents.DYED_COLOR, new DyedItemColor(0xFFFFFF)).rgb());
                    pipeline.draw(new DrawCommand(RenderType.ITEM, model, displayTransform, col));
                }
            }
        }
    }

    public static List<RPModel.View> getModels(ItemStack itemStack) {
        List<RPModel> models = loadItemModel(itemStack);
        if (models.isEmpty())
            return null;

        List<RPModel.View> modelViews = new ObjectArrayList<>();
        for (RPModel model : models) {
            if (model.parent != null && model.parent.getPath().equals("item/generated")) {
                var obj = model.textures.getOrDefault("layer0", null);
                var obj2 = model.textures.getOrDefault("layer1", null);
                modelViews.add(new RPModel.View(GeneratedItemModel.getItem(obj == null ? null : obj.sprite(), obj2 == null ? null : obj2.sprite())));
            } else {
                modelViews.add(new RPModel.View(model));
            }
        }

        return modelViews;
    }

    public static List<RPModel> loadItemModel(ItemStack itemStack) {
        var paths = ItemModelPathResolver.getModelPaths(itemStack);
        if (paths.isEmpty())
            return List.of();

        List<RPModel> models = new ObjectArrayList<>();
        for (Identifier path : paths) {
            models.add(ITEM_MODELS.computeIfAbsent(path, RPHelper::loadModel));
        }

        return models;
    }
}
