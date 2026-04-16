package de.tomalbrc.cameraobscura.util.image;

import de.tomalbrc.cameraobscura.Components;
import de.tomalbrc.cameraobscura.apng.StreamingAPNGReader;
import de.tomalbrc.cameraobscura.platform.Platforms;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class VideoFileManager {
    public static final Path VIDEO_DIR = Platforms.get().getConfigDir().resolve("camera_videos");

    static {
        try {
            Files.createDirectories(VIDEO_DIR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Path getFile(String videoId) {
        return VIDEO_DIR.resolve(videoId + ".apng");
    }

    public static StreamingAPNGReader openStreamingReader(String videoId) throws IOException {
        Path file = getFile(videoId);
        if (!Files.exists(file)) throw new FileNotFoundException(videoId);
        return new StreamingAPNGReader(Files.newInputStream(file));
    }

    public static void saveVideo(String videoId, List<BufferedImage> frames, Components.VideoParams params) {
        try {
            byte[] apngData = APNGHelper.buildAPNG(frames, params.frameRate(), params.loopPlayback());
            Files.write(getFile(videoId), apngData);
        } catch (IOException e) {
            Platforms.get().getLogger().error("Error while saving video", e);
        }
    }

    // delete video and clear its cache entry
    public static void deleteVideo(String videoId) {
        try {
            Files.deleteIfExists(getFile(videoId));
        } catch (IOException e) {
            Platforms.get().getLogger().error("Error deleting video", e);
        }
    }
}