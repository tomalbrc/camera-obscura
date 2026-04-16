package de.tomalbrc.cameraobscura.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.tomalbrc.cameraobscura.platform.AssetFetcher;
import org.slf4j.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MojangAssetFetcher implements AssetFetcher {
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.of(10, ChronoUnit.SECONDS))
            .build();

    private static final String VERSION_MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";

    private final Logger logger;
    private final Path assetsDir;      // extracted 'assets/' directory root
    private final Path clientJar;      // cached client JAR
    private final String version;

    private CompletableFuture<Void> ready = null;

    public MojangAssetFetcher(String version, Path pluginDataFolder, Logger logger) {
        this.logger = logger;
        this.version = version;
        Path cacheDir = pluginDataFolder.resolve("client_asset_cache");
        this.assetsDir = cacheDir;
        this.clientJar = cacheDir.resolve("client.jar");
    }

    public CompletableFuture<Void> initialize() {
        if (ready == null) {
            ready = CompletableFuture.runAsync(this::downloadAndExtract);
        }
        return ready;
    }

    public byte[] getAsset(String path) {
        if (ready == null || !ready.isDone()) {
            logger.warn("Asset fetcher not initialized yet, cannot get asset: {}", path);
            return null;
        }

        String relative = path;
        Path file = assetsDir.resolve(relative);
        if (!Files.exists(file)) {
            return null;
        }
        try {
            return Files.readAllBytes(file);
        } catch (IOException e) {
            logger.warn("Failed to read cached asset {}", relative, e);
            return null;
        }
    }

    private void downloadAndExtract() {
        try {
            String manifestJson = downloadString(VERSION_MANIFEST_URL);
            JsonObject manifest = JsonParser.parseString(manifestJson).getAsJsonObject();

            String versionUrl = null;
            for (JsonElement elem : manifest.getAsJsonArray("versions")) {
                JsonObject ver = elem.getAsJsonObject();
                if (ver.get("id").getAsString().equals(version)) {
                    versionUrl = ver.get("url").getAsString();
                    break;
                }
            }
            if (versionUrl == null) {
                logger.error("Could not find version {} in manifest.", version);
                return;
            }

            String versionJsonStr = downloadString(versionUrl);
            JsonObject versionJson = JsonParser.parseString(versionJsonStr).getAsJsonObject();
            JsonObject clientInfo = versionJson.getAsJsonObject("downloads").getAsJsonObject("client");
            String clientUrl = clientInfo.get("url").getAsString();
            long size = clientInfo.get("size").getAsLong();
            String sha1 = clientInfo.get("sha1").getAsString();

            if (!Files.exists(clientJar) || !sha1.equals(hashFile(clientJar))) {
                logger.info("Downloading Minecraft client JAR ({} MiB)...", size / 1024 / 1024);
                downloadFile(clientUrl, clientJar);
                if (!sha1.equals(hashFile(clientJar))) {
                    logger.error("Client JAR hash mismatch. Aborting asset extraction.");
                    return;
                }
            }

            if (!Files.exists(assetsDir.resolve("assets"))) {
                logger.info("Extracting client assets...");
                extractAssets();
                logger.info("Client assets ready ({})", assetsDir);
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to prepare client assets", e);
        }
    }

    private void extractAssets() throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(Files.newInputStream(clientJar)))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();

                if (!name.startsWith("assets/")) {
                    continue;
                }

                Path outFile = assetsDir.resolve(name);
                if (entry.isDirectory()) {
                    Files.createDirectories(outFile);
                } else {
                    Files.createDirectories(outFile.getParent());
                    Files.copy(zis, outFile, StandardCopyOption.REPLACE_EXISTING);
                }

                zis.closeEntry();
            }
        }
    }

    private String downloadString(String url) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).build();
        HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new IOException("HTTP " + resp.statusCode() + " fetching " + url);
        }
        return resp.body();
    }

    private void downloadFile(String url, Path dest) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).build();
        HttpResponse<InputStream> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofInputStream());
        if (resp.statusCode() != 200) {
            throw new IOException("HTTP " + resp.statusCode() + " fetching file " + url);
        }
        Files.createDirectories(dest.getParent());
        try (InputStream in = resp.body()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private String hashFile(Path file) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] bytes = Files.readAllBytes(file);
            byte[] digest = md.digest(bytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            return "";
        }
    }
}