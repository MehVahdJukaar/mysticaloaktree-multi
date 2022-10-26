package net.mehvahdjukaar.mysticaloaktree;

import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.mysticaloaktree.client.TreeLoreManager;
import net.mehvahdjukaar.mysticaloaktree.client.WindParticle;
import net.minecraft.client.renderer.RenderType;

public class MysticalOakTreeClient {

    public static void init() {
        ClientConfigs.init();
        ClientPlatformHelper.addClientReloadListener(TreeLoreManager.INSTANCE, MysticalOakTree.res("tree_lore"));
        ClientPlatformHelper.addParticleRegistration(MysticalOakTreeClient::registerParticles);
    }

    public static void setup() {
        ClientPlatformHelper.registerRenderType(MysticalOakTree.BLOCK.get(), RenderType.cutout());
    }

    @EventCalled
    private static void registerParticles(ClientPlatformHelper.ParticleEvent event) {
        event.register(MysticalOakTree.WIND.get(), WindParticle.Factory::new);
    }
}
