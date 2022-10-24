package net.mehvahdjukaar.mysticaloaktree;

import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.mysticaloaktree.client.TreeLoreManager;
import net.minecraft.client.renderer.RenderType;

public class MysticalOakTreeClient {

    public static void init() {
        ClientConfigs.init();
        ClientPlatformHelper.addClientReloadListener(TreeLoreManager.INSTANCE, MysticalOakTree.res("tree_lore"));
    }

    public static void setup() {
        ClientPlatformHelper.registerRenderType(MysticalOakTree.BLOCK.get(), RenderType.cutout());
    }
}
