package net.mehvahdjukaar.mysticaloaktree.platform;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTreeClient;
import net.minecraft.client.gui.screens.TitleScreen;

/**
 * Client-only Fabric event wiring. Kept separate so the fabric client API is never
 * classloaded on a dedicated server.
 */
public final class ClientHooks {

    private static boolean firstScreenShown;

    public static void init() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!firstScreenShown && screen instanceof TitleScreen) {
                firstScreenShown = true;
                MysticalOakTreeClient.onFirstScreen(screen);
            }
        });
    }
}
