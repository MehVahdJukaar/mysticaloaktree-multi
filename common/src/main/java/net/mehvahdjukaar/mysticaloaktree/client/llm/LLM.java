package net.mehvahdjukaar.mysticaloaktree.client.llm;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Thin wrapper around a locally installed LLM runner executable (e.g. a llamafile / llama.cpp style binary)
 * plus the model weights it loads. Mirrors the role of vista's FFmpeg wrapper: it only knows how to launch
 * the managed executable.
 */
public final class LLM {

    private final Path executablePath;
    @Nullable
    private final Path modelPath;

    public LLM(Path executablePath, @Nullable Path modelPath) {
        this.executablePath = executablePath;
        this.modelPath = modelPath;
    }

    public Path getExecutablePath() {
        return executablePath;
    }

    /** The downloaded model weights (.gguf), or null if the runner is self-contained. */
    @Nullable
    public Path getModelPath() {
        return modelPath;
    }

    /**
     * Launches the LLM executable with the given arguments and returns the live process.
     * The caller is responsible for wiring up stdin/stdout and for destroying the process.
     */
    public Process run(String... args) throws IOException {
        String[] cmd = new String[args.length + 1];
        cmd[0] = executablePath.toString();
        System.arraycopy(args, 0, cmd, 1, args.length);
        return new ProcessBuilder(cmd)
                .redirectErrorStream(true)
                .start();
    }

    /**
     * EXPERIMENTAL one-shot generation helper used by the test harness. Spawns the runner, feeds it
     * the prompt, waits for it to finish and returns whatever it printed. Assumes a llama.cpp-style
     * CLI (-m model -p prompt -n tokens); adjust the args to match the runner you configure.
     * Blocking — never call this on the render thread.
     */
    public String generateOneShot(String prompt, int maxTokens) throws IOException, InterruptedException {
        if (modelPath == null) {
            throw new IOException("No model installed; cannot generate.");
        }
        Process process = run(
                "-m", modelPath.toString(),
                "-p", prompt,
                "-n", Integer.toString(maxTokens),
                "--no-display-prompt"
        );
        String output;
        try (InputStream in = process.getInputStream()) {
            output = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        process.waitFor();
        return output.trim();
    }
}
