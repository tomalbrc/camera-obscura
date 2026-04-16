package de.tomalbrc.cameraobscura.platform;

import com.mojang.datafixers.util.Pair;
import de.tomalbrc.cameraobscura.Components;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.entity.UniquelyIdentifyable;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class PaperItemDataStore implements ItemDataStore {
    private final NamespacedKey RESOLUTION_KEY; // identifies camera items
    private final NamespacedKey LIVE_MAP_KEY;
    private final NamespacedKey ENTITY_REF_KEY;
    private final NamespacedKey COLOR_MODE_KEY;
    private final NamespacedKey VIDEO_PARAMS_KEY;
    private final NamespacedKey DITHER_MODE_KEY;
    private final NamespacedKey DATA_KEY;

    public PaperItemDataStore(JavaPlugin plugin) {
        LIVE_MAP_KEY = new NamespacedKey(plugin, "live_map");
        ENTITY_REF_KEY = new NamespacedKey(plugin, "entity_ref");
        COLOR_MODE_KEY = new NamespacedKey(plugin, "color_mode");
        RESOLUTION_KEY = new NamespacedKey(plugin, "resolution");
        VIDEO_PARAMS_KEY = new NamespacedKey(plugin, "video_params");
        DITHER_MODE_KEY = new NamespacedKey(plugin, "dither_mode");
        DATA_KEY = new NamespacedKey(plugin, "data");
    }

    private String entityRefToSnbt(EntityReference<UniquelyIdentifyable> ref) {
        if (ref == null) return null;
        Tag tag = EntityReference.codec()
                .encodeStart(NbtOps.INSTANCE, ref)
                .result().orElse(null);
        if (tag == null) return null;
        return tag.toString();
    }

    @Nullable
    private EntityReference<UniquelyIdentifyable> entityRefFromSnbt(String snbt) {
        if (snbt == null || snbt.isEmpty()) return null;
        try {
            Tag tag = TagParser.create(NbtOps.INSTANCE).parseFully(snbt);
            return EntityReference.codec()
                    .decode(NbtOps.INSTANCE, tag)
                    .result()
                    .map(Pair::getFirst)
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean isLiveMap(ItemStack nmsStack) {
        CraftItemStack bukkit = CraftItemStack.asCraftMirror(nmsStack);
        PersistentDataContainer pdc = bukkit.getItemMeta().getPersistentDataContainer();
        return pdc.getOrDefault(LIVE_MAP_KEY, PersistentDataType.BOOLEAN, false);
    }

    @Override
    public void setLiveMap(ItemStack nmsStack, boolean live) {
        CraftItemStack bukkit = CraftItemStack.asCraftMirror(nmsStack);
        ItemMeta meta = bukkit.getItemMeta();
        meta.getPersistentDataContainer().set(LIVE_MAP_KEY, PersistentDataType.BOOLEAN, live);
        bukkit.setItemMeta(meta);
    }

    @Override
    @Nullable
    public EntityReference<UniquelyIdentifyable> getEntityRef(ItemStack nmsStack) {
        CraftItemStack bukkit = CraftItemStack.asCraftMirror(nmsStack);
        String snbt = bukkit.getItemMeta().getPersistentDataContainer().get(ENTITY_REF_KEY, PersistentDataType.STRING);
        return entityRefFromSnbt(snbt);
    }

    @Override
    public void setEntityRef(ItemStack nmsStack, @Nullable EntityReference<UniquelyIdentifyable> ref) {
        CraftItemStack bukkit = CraftItemStack.asCraftMirror(nmsStack);
        ItemMeta meta = bukkit.getItemMeta();
        if (ref == null) {
            meta.getPersistentDataContainer().remove(ENTITY_REF_KEY);
        } else {
            String snbt = entityRefToSnbt(ref);
            if (snbt != null) {
                meta.getPersistentDataContainer().set(ENTITY_REF_KEY, PersistentDataType.STRING, snbt);
            }
        }
        bukkit.setItemMeta(meta);
    }

    @Override
    public Components.ColorMode getColorMode(ItemStack nmsStack) {
        String raw = getString(nmsStack, COLOR_MODE_KEY);
        return raw != null ? Components.ColorMode.valueOf(raw.toUpperCase(Locale.ROOT)) : Components.ColorMode.COLOR;
    }

    @Override
    public void setColorMode(ItemStack nmsStack, Components.ColorMode mode) {
        setString(nmsStack, COLOR_MODE_KEY, mode.name());
    }

    @Override
    public Components.Resolution getResolution(ItemStack nmsStack) {
        String raw = getString(nmsStack, RESOLUTION_KEY);
        if (raw != null) {
            String[] parts = raw.split("x");
            if (parts.length == 2) {
                return new Components.Resolution(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            }
        }
        return Components.Resolution.DEFAULT;
    }

    @Override
    public void setResolution(ItemStack nmsStack, Components.Resolution resolution) {
        setString(nmsStack, RESOLUTION_KEY, resolution.width() + "x" + resolution.height());
    }

    @Override
    public Components.VideoParams getVideoParams(ItemStack nmsStack) {
        String raw = getString(nmsStack, VIDEO_PARAMS_KEY);
        if (raw != null) {
            String[] parts = raw.split(",");
            if (parts.length == 3) {
                int fr = Integer.parseInt(parts[0]);
                int mf = Integer.parseInt(parts[1]);
                boolean lp = Boolean.parseBoolean(parts[2]);
                return new Components.VideoParams(fr, mf, lp);
            }
        }
        return null;
    }

    @Override
    public void setVideoParams(ItemStack nmsStack, Components.VideoParams params) {
        setString(nmsStack, VIDEO_PARAMS_KEY, params.frameRate() + "," + params.maxFrames() + "," + params.loopPlayback());
    }

    @Override
    public Components.DitherMode getDitherMode(ItemStack nmsStack) {
        String raw = getString(nmsStack, DITHER_MODE_KEY);
        return raw != null ? Components.DitherMode.valueOf(raw.toUpperCase(Locale.ROOT)) : Components.DitherMode.NONE;
    }

    @Override
    public void setDitherMode(ItemStack nmsStack, Components.DitherMode mode) {
        setString(nmsStack, DITHER_MODE_KEY, mode.name());
    }

    @Override
    @Nullable
    public Components.MediaData getMediaData(ItemStack nmsStack) {
        String raw = getString(nmsStack, DATA_KEY);
        if (raw != null) {
            String[] mainParts = raw.split("\\|");
            if (mainParts.length == 2) {
                java.util.UUID id = java.util.UUID.fromString(mainParts[0]);
                String[] vidParts = mainParts[1].split(",");
                if (vidParts.length == 3) {
                    int fr = Integer.parseInt(vidParts[0]);
                    int mf = Integer.parseInt(vidParts[1]);
                    boolean lp = Boolean.parseBoolean(vidParts[2]);
                    return new Components.MediaData(id, new Components.VideoParams(fr, mf, lp));
                }
            }
        }
        return null;
    }

    @Override
    public void setMediaData(ItemStack nmsStack, @Nullable Components.MediaData data) {
        if (data == null) {
            remove(nmsStack, DATA_KEY);
            return;
        }
        String encoded = data.id().toString() + "|"
                + data.videoParams().frameRate() + ","
                + data.videoParams().maxFrames() + ","
                + data.videoParams().loopPlayback();
        setString(nmsStack, DATA_KEY, encoded);
    }

    private String getString(ItemStack nmsStack, NamespacedKey key) {
        CraftItemStack bukkit = CraftItemStack.asCraftMirror(nmsStack);
        if (bukkit.getItemMeta() == null) {
            return null;
        }

        return bukkit.getItemMeta().getPersistentDataContainer()
                .get(key, PersistentDataType.STRING);
    }

    private void setString(ItemStack nmsStack, NamespacedKey key, String value) {
        CraftItemStack bukkit = CraftItemStack.asCraftMirror(nmsStack);
        ItemMeta meta = bukkit.getItemMeta();
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, value);
        bukkit.setItemMeta(meta);
    }

    private void remove(ItemStack nmsStack, NamespacedKey key) {
        CraftItemStack bukkit = CraftItemStack.asCraftMirror(nmsStack);
        ItemMeta meta = bukkit.getItemMeta();
        meta.getPersistentDataContainer().remove(key);
        bukkit.setItemMeta(meta);
    }

    @Override
    public boolean hasLiveMap(ItemStack nmsStack) {
        CraftItemStack bukkit = CraftItemStack.asCraftMirror(nmsStack);
        return bukkit.getItemMeta().getPersistentDataContainer().has(LIVE_MAP_KEY);
    }

    @Override
    public boolean hasEntityRef(ItemStack nmsStack) {
        CraftItemStack bukkit = CraftItemStack.asCraftMirror(nmsStack);
        return bukkit.getItemMeta().getPersistentDataContainer().has(ENTITY_REF_KEY);
    }

    @Override
    public boolean hasMediaData(ItemStack nmsStack) {
        return CraftItemStack.asCraftMirror(nmsStack).getItemMeta().getPersistentDataContainer().has(DATA_KEY);
    }

    @Override
    public boolean hasResolution(ItemStack stack) {
        return CraftItemStack.asCraftMirror(stack).getItemMeta().getPersistentDataContainer().has(RESOLUTION_KEY);
    }

    @Override
    public boolean hasDitherMode(ItemStack stack) {
        return CraftItemStack.asCraftMirror(stack).getItemMeta().getPersistentDataContainer().has(DITHER_MODE_KEY);
    }

    @Override
    public boolean hasVideoParams(ItemStack stack) {
        return CraftItemStack.asCraftMirror(stack).getItemMeta().getPersistentDataContainer().has(VIDEO_PARAMS_KEY);
    }

    @Override
    public boolean hasColorMode(ItemStack stack) {
        return CraftItemStack.asCraftMirror(stack).getItemMeta().getPersistentDataContainer().has(COLOR_MODE_KEY);
    }
}