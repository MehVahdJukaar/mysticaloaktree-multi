package net.mehvahdjukaar.mysticaloaktree.configs;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTree;

import java.util.function.Supplier;

/**
 * Client config for the Mystical Oak Tree, modeled on vista's ClientConfigs. Holds the toggles
 * that gate the local LLM the Wise Oak uses to talk.
 */
public class ClientConfigs {

    public static final ModConfigHolder SPEC;

    public static final Supplier<Boolean> ENABLE_LLM;
    public static final Supplier<Boolean> LLM_TEST_MODE;

    static {
        ConfigBuilder builder = ConfigBuilder.create(MysticalOakTree.MOD_ID, ConfigType.CLIENT);

        builder.push("ai");
        ENABLE_LLM = builder
                .comment("Enable the local AI model that lets the Wise Oak talk. When on, a small LLM runner and model file are downloaded onto your machine on first launch. Generation runs entirely locally; nothing is sent to any external server you don't control.")
                .define("enable_llm", true);
        LLM_TEST_MODE = builder
                .gameRestart()
                .comment("Test/debug toggle: when on, interacting with the Wise Oak sends a prompt to the local AI and prints its reply to chat, instead of showing the canned dialogue. For development only.")
                .define("llm_test_mode", false);
        builder.pop();

        SPEC = builder.build();
        SPEC.forceLoad();
    }

    /** Builds and loads the config the first time this is referenced. Call once on client init. */
    public static void init() {
    }

    public static boolean canUseLLM() {
        return ENABLE_LLM.get();
    }

    public static boolean isLlmTestMode() {
        return LLM_TEST_MODE.get();
    }

    /** Used by the welcome screen's "disable" button so the prompt never appears again. */
    public static void turnOffLLM() {
        SPEC.manuallySetValue(ENABLE_LLM, false);
    }
}
