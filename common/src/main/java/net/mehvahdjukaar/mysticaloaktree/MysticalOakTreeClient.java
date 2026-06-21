package net.mehvahdjukaar.mysticaloaktree;

import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.mysticaloaktree.client.TreeLoreManager;
import net.mehvahdjukaar.mysticaloaktree.client.WindParticle;
import net.mehvahdjukaar.mysticaloaktree.client.llm.LLM;
import net.mehvahdjukaar.mysticaloaktree.client.llm.LLMManager;
import net.mehvahdjukaar.mysticaloaktree.client.llm.LLMWelcomeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class MysticalOakTreeClient {

    private static CompletableFuture<LLM> llmFuture;

    public static void init() {
        ClientHelper.addClientSetup(MysticalOakTreeClient::setup);

        ClientHelper.addClientReloadListener(TreeLoreManager::new, MysticalOakTree.res("tree_lore"));
        ClientHelper.addParticleRegistration(MysticalOakTreeClient::registerParticles);

        // if the executable is already installed, start it up right away
        if (LLMManager.hasRequiredFiles()) instantiateLLM(null);
    }

    public static void setup() {
        ClientHelper.registerRenderType(MysticalOakTree.BLOCK.get(), RenderType.cutout());
    }

    @EventCalled
    private static void registerParticles(ClientHelper.ParticleEvent event) {
        event.register(MysticalOakTree.WIND.get(), WindParticle.Factory::new);
    }

    // ---- LLM management (mirrors vista's FFmpeg wiring) ----

    private static void instantiateLLM(@Nullable String url) {
        llmFuture = LLMManager.getOrDownload(url);
    }

    @Nullable
    public static synchronized LLM getLLM() {
        if (llmFuture == null || llmFuture.isCompletedExceptionally()) {
            return null;
        }
        return llmFuture.isDone() ? llmFuture.resultNow() : null;
    }

    public static synchronized boolean isLLMDownloading() {
        return llmFuture != null && !llmFuture.isDone();
    }

    public static int getLLMDownloadProgress() {
        if (!isLLMDownloading()) {
            return -1;
        }
        return LLMManager.getDownloadProgress();
    }

    /** Shown once over the title screen on first launch, asking the player about the LLM download. */
    @EventCalled
    public static void onFirstScreen(Screen screen) {
        if (llmFuture == null && !LLMManager.isDisabled() && !LLMManager.hasRequiredFiles()) {
            Minecraft.getInstance().setScreen(new LLMWelcomeScreen(screen,
                    MysticalOakTreeClient::instantiateLLM,
                    () -> instantiateLLM(null),
                    LLMManager::setDisabled
            ));
        }
    }
}
