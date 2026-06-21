package net.mehvahdjukaar.mysticaloaktree.client.llm;

import net.mehvahdjukaar.mysticaloaktree.MysticalOakTree;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTreeClient;
import net.mehvahdjukaar.mysticaloaktree.configs.ClientConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Test harness for the local LLM. When {@link ClientConfigs#isLlmTestMode()} is on, anything the
 * player types into chat is captured client-side, fed to the model, and the reply is printed back
 * to the local chat — nothing is sent to the server. Wired from each platform's chat event.
 */
public final class LLMChatTest {

    private static final int MAX_TOKENS = 80;
    private static final String SYSTEM_PROMPT =
            "You are a wise, ancient and slightly cryptic oak tree in a Minecraft world. " +
            "Reply to the traveller in one or two short sentences, staying in character.\n";

    // only one generation at a time; the process is slow and we don't want a queue piling up
    private static final AtomicBoolean BUSY = new AtomicBoolean(false);

    private LLMChatTest() {
    }

    /**
     * Handles a chat message the player just tried to send.
     *
     * @return true if the test harness consumed the message (the caller should cancel the real send),
     * false to let the message go through normally.
     */
    public static boolean handleChat(String message) {
        if (!ClientConfigs.isLlmTestMode() || message.isBlank()) {
            return false;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;

        LLM llm = MysticalOakTreeClient.getLLM();
        if (llm == null) {
            mc.player.displayClientMessage(Component.literal("[Wise Oak] ...the model is not ready yet.")
                    .withStyle(ChatFormatting.GRAY), false);
            return true;
        }
        if (!BUSY.compareAndSet(false, true)) {
            mc.player.displayClientMessage(Component.literal("[Wise Oak] ...still thinking, give me a moment.")
                    .withStyle(ChatFormatting.GRAY), false);
            return true;
        }

        // echo what the player "said" to the tree, then generate asynchronously
        mc.player.displayClientMessage(Component.literal("You whisper: " + message)
                .withStyle(ChatFormatting.DARK_GRAY), false);

        String prompt = SYSTEM_PROMPT + "Traveller: " + message + "\nOak:";
        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return llm.generateOneShot(prompt, MAX_TOKENS);
                    } catch (Exception e) {
                        MysticalOakTree.LOGGER.error("LLM test generation failed", e);
                        return "*the oak is silent* (" + e.getMessage() + ")";
                    }
                })
                .thenAccept(reply -> mc.execute(() -> {
                    BUSY.set(false);
                    if (mc.player != null) {
                        mc.player.displayClientMessage(
                                Component.literal("[Wise Oak] ").withStyle(ChatFormatting.GREEN)
                                        .append(Component.literal(reply).withStyle(ChatFormatting.RESET)),
                                false);
                    }
                }));
        return true;
    }
}
