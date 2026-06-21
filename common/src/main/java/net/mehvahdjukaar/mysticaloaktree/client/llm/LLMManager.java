package net.mehvahdjukaar.mysticaloaktree.client.llm;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.mehvahdjukaar.moonlight.api.util.ArchiveUtils;
import net.mehvahdjukaar.moonlight.api.util.FileDownloadUtils;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTree;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Downloads and manages a local LLM runner executable plus its model weights, mirroring
 * vista's FFmpegManager. The download urls and file names live in
 * {@code mysticaloaktree_llm_sources.json} (copied next to the game on first run) so they
 * can be edited without recompiling.
 *
 * <p>The runner download may be a direct executable or an archive containing it; archives
 * are extracted and the named executable is fished out, exactly like vista pulls
 * ffmpeg/ffprobe. The model (a portable .gguf) is downloaded as a plain file shared across
 * all platforms.
 */
public final class LLMManager {

    private static final Path SOURCES_CONFIG_PATH = Paths.get("mysticaloaktree_llm_sources.json");
    private static final String SOURCES_RESOURCE_PATH = "/mysticaloaktree_llm_sources.json";
    private static final Path PROGRAM_FOLDER = Paths.get("mysticaloaktree_llm_bin");
    // remembers which file was installed (names are not known ahead of time, unlike ffmpeg/ffprobe)
    private static final Path INSTALLED_MARKER = PROGRAM_FOLDER.resolve(".installed");

    private static final OsType OS_TYPE = OsType.detect();

    private static volatile int downloadProgress = -1;

    private LLMManager() {
    }

    public static CompletableFuture<LLM> getOrDownload(@Nullable String customUrl) {
        return CompletableFuture.supplyAsync(() -> initialize(customUrl));
    }

    /** -1 when idle, otherwise 0-100 while downloading (executable then model). */
    public static int getDownloadProgress() {
        return downloadProgress;
    }

    /** True once the runner executable has been installed (consent given and obtained). */
    public static boolean hasRequiredFiles() {
        Path exe = installedExecutable();
        return exe != null && Files.exists(exe);
    }

    @Nullable
    private static Path installedExecutable() {
        try {
            if (!Files.exists(INSTALLED_MARKER)) return null;
            String name = Files.readString(INSTALLED_MARKER, StandardCharsets.UTF_8).trim();
            if (name.isEmpty()) return null;
            return PROGRAM_FOLDER.resolve(name);
        } catch (IOException e) {
            return null;
        }
    }

    private static LLM initialize(@Nullable String customUrl) {
        try {
            Files.createDirectories(PROGRAM_FOLDER);
            JsonObject config = readSourcesConfig();

            Path executable = ensureExecutable(customUrl, config);
            Path model = ensureModel(config);

            downloadProgress = -1;
            return new LLM(executable, model);
        } catch (Exception e) {
            downloadProgress = -1;
            throw new RuntimeException("LLM setup failed. Aborting.", e);
        }
    }

    // ---- executable ----

    private static Path ensureExecutable(@Nullable String customUrl, JsonObject config) throws IOException, InterruptedException {
        Path executable = installedExecutable();
        if (executable != null && Files.exists(executable)) {
            MysticalOakTree.LOGGER.info("LLM executable found at {}", executable);
            return executable;
        }

        Source source = customUrl != null ? Source.fromUrl(customUrl) : readExecutableSource(config);

        downloadProgress = -1;
        Path archive = PROGRAM_FOLDER.resolve(ArchiveUtils.extractFileNameFromUrl(source.url));

        if (Files.exists(archive) && !ArchiveUtils.isProbablyValid(archive)) {
            Files.deleteIfExists(archive);
        }
        if (!Files.exists(archive)) {
            //await
            FileDownloadUtils.download(source.url, archive, null, percent -> downloadProgress = percent);
        }

        executable = extractAndInstall(archive, source.executableName);
        downloadProgress = -1;
        Files.writeString(INSTALLED_MARKER, executable.getFileName().toString(), StandardCharsets.UTF_8);
        return executable;
    }

    /**
     * Installs the downloaded file. If it is a supported archive it is extracted and the
     * named executable is located inside it; otherwise the download itself is the executable.
     * Returns the path to the runnable executable.
     */
    private static Path extractAndInstall(Path archive, String executableName) throws IOException, InterruptedException {
        Path executable = PROGRAM_FOLDER.resolve(executableName);

        if (ArchiveUtils.isSupported(archive)) {
            ArchiveUtils.extract(archive, PROGRAM_FOLDER);
            Path found = findExecutableInProgramFolder(executableName);
            if (!found.equals(executable)) {
                Files.move(found, executable, StandardCopyOption.REPLACE_EXISTING);
            }
            Files.deleteIfExists(archive);
        } else {
            // direct executable download: the archive file IS the executable
            if (!archive.equals(executable)) {
                Files.move(archive, executable, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        if (OS_TYPE.requiresExecutableBit && !executable.toFile().setExecutable(true)) {
            throw new IOException("Could not mark LLM executable as runnable: " + executable);
        }
        return executable;
    }

    private static Path findExecutableInProgramFolder(String executableName) throws IOException {
        try (Stream<Path> stream = Files.walk(PROGRAM_FOLDER)) {
            return stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().equals(executableName))
                    .findFirst()
                    .orElseThrow(() -> new IOException(
                            "Archive does not contain expected executable: " + executableName));
        }
    }

    // ---- model ----

    /** Downloads the model weights if configured and not already present. Null when no model is configured. */
    @Nullable
    private static Path ensureModel(JsonObject config) throws IOException {
        ModelSource model = readModelSource(config);
        if (model == null) return null;

        Path modelPath = PROGRAM_FOLDER.resolve(model.file);
        if (Files.exists(modelPath) && Files.size(modelPath) > 0) {
            MysticalOakTree.LOGGER.info("LLM model found at {}", modelPath);
            return modelPath;
        }

        downloadProgress = -1;
        // the model is a plain .gguf file, not an archive: download straight to its final name
        FileDownloadUtils.download(model.url, modelPath, null, percent -> downloadProgress = percent);
        downloadProgress = -1;
        return modelPath;
    }

    // ---- config ----

    private static JsonObject readSourcesConfig() throws IOException {
        ensureSourcesConfigExists();
        String json = Files.readString(SOURCES_CONFIG_PATH, StandardCharsets.UTF_8);
        try {
            return JsonParser.parseString(json).getAsJsonObject();
        } catch (IllegalStateException | JsonParseException e) {
            throw new IOException("Invalid JSON in " + SOURCES_CONFIG_PATH, e);
        }
    }

    private static Source readExecutableSource(JsonObject root) throws IOException {
        String key = OS_TYPE.jsonKey;
        if (!root.has(key) || !root.get(key).isJsonObject()) {
            throw new IOException("Missing entry '" + key + "' in " + SOURCES_CONFIG_PATH);
        }
        JsonObject entry = root.getAsJsonObject(key);

        String url = entry.has("url") ? entry.get("url").getAsString().trim() : "";
        if (url.isEmpty()) {
            throw new IOException("No download url set for '" + key + "' in " + SOURCES_CONFIG_PATH);
        }
        String executable = entry.has("executable") ? entry.get("executable").getAsString().trim() : "";
        return Source.fromUrl(url, executable);
    }

    @Nullable
    private static ModelSource readModelSource(JsonObject root) {
        if (!root.has("model") || !root.get("model").isJsonObject()) return null;
        JsonObject entry = root.getAsJsonObject("model");
        String url = entry.has("url") ? entry.get("url").getAsString().trim() : "";
        if (url.isEmpty()) return null;
        if (!url.startsWith("http")) url = "https://" + url;
        String file = entry.has("file") ? entry.get("file").getAsString().trim() : "";
        if (file.isEmpty()) file = ArchiveUtils.extractFileNameFromUrl(url);
        return new ModelSource(url, file);
    }

    private static void ensureSourcesConfigExists() throws IOException {
        if (Files.exists(SOURCES_CONFIG_PATH)) return;
        try (InputStream in = LLMManager.class.getResourceAsStream(SOURCES_RESOURCE_PATH)) {
            if (in == null) {
                throw new IOException("Resource not found: " + SOURCES_RESOURCE_PATH);
            }
            Files.copy(in, SOURCES_CONFIG_PATH);
        }
    }

    private record Source(String url, String executableName) {
        static Source fromUrl(String url) {
            return fromUrl(url, "");
        }

        static Source fromUrl(String url, String executableName) {
            String normalizedUrl = url.startsWith("http") ? url : "https://" + url;
            String name = executableName == null ? "" : executableName.trim();
            if (name.isEmpty()) {
                name = ArchiveUtils.extractFileNameFromUrl(normalizedUrl);
            }
            return new Source(normalizedUrl, name);
        }
    }

    private record ModelSource(String url, String file) {
    }

    private enum OsType {
        LINUX("linux", true),
        MACOS("macos", true),
        WINDOWS("windows", false);

        private final String jsonKey;
        private final boolean requiresExecutableBit;

        OsType(String jsonKey, boolean requiresExecutableBit) {
            this.jsonKey = jsonKey;
            this.requiresExecutableBit = requiresExecutableBit;
        }

        private static OsType detect() {
            if (Minecraft.ON_OSX) return MACOS;
            String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
            return os.contains("win") ? WINDOWS : LINUX;
        }
    }
}
