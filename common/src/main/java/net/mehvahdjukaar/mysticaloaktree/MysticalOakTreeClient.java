package net.mehvahdjukaar.mysticaloaktree;

import net.mehvahdjukaar.moonlight3.api.platform.ClientPlatformHelper;
import net.mehvahdjukaar.mysticaloaktree.client.TreeLoreManager;
import net.mehvahdjukaar.mysticaloaktree.client.WindParticle;
import net.minecraft.client.renderer.RenderType;

public class MysticalOakTreeClient {

    public static void init() {
        ClientPlatformHelper.addClientReloadListener(TreeLoreManager.INSTANCE, MysticalOakTree.res("tree_lore"));
        ClientPlatformHelper.addParticleRegistration(MysticalOakTreeClient::registerParticles);
    }

    public static void setup() {
        ClientPlatformHelper.registerRenderType(MysticalOakTree.BLOCK.get(), RenderType.cutout());
    }

    private static void registerParticles(ClientPlatformHelper.ParticleEvent event) {
        event.register(MysticalOakTree.WIND.get(), WindParticle.Factory::new);
    }
}
