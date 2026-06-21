package net.mehvahdjukaar.mysticaloaktree.platform;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.platform.neoforge.RegHelperImpl;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTree;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTreeClient;
import net.mehvahdjukaar.mysticaloaktree.client.llm.LLMChatTest;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Author: MehVahdJukaar
 */
@Mod(MysticalOakTree.MOD_ID)
public class MysticalOakTreeForge {

    public MysticalOakTreeForge(IEventBus bus) {
        RegHelper.startRegisteringFor(bus);
        MysticalOakTree.commonInit();

        if (PlatHelper.getPhysicalSide().isClient()) {
            MysticalOakTreeClient.init();
            NeoForge.EVENT_BUS.addListener(MysticalOakTreeForge::onFirstScreen);
            NeoForge.EVENT_BUS.addListener(MysticalOakTreeForge::onClientChat);
        }
    }

    private static boolean firstScreenShown;

    private static void onFirstScreen(ScreenEvent.Init.Post event) {
        if (!firstScreenShown && event.getScreen() instanceof TitleScreen) {
            firstScreenShown = true;
            MysticalOakTreeClient.onFirstScreen(event.getScreen());
        }
    }

    private static void onClientChat(ClientChatEvent event) {
        // cancel the real send when the test harness replies locally instead
        if (LLMChatTest.handleChat(event.getMessage())) {
            event.setCanceled(true);
        }
    }


}

