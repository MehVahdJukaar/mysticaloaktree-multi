package net.mehvahdjukaar.mysticaloaktree;

import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.mysticaloaktree.client.TreeLoreManager;
import net.mehvahdjukaar.mysticaloaktree.client.WindParticle;
import net.minecraft.client.renderer.RenderType;

public class MysticalOakTreeClient {

    public static void init() {
        ClientHelper.addClientSetup(MysticalOakTreeClient::setup);

        ClientHelper.addClientReloadListener(TreeLoreManager::new, MysticalOakTree.res("tree_lore"));
        ClientHelper.addParticleRegistration(MysticalOakTreeClient::registerParticles);
    }

    public static void setup() {
        ClientHelper.registerRenderType(MysticalOakTree.BLOCK.get(), RenderType.cutout());
    }

    @EventCalled
    private static void registerParticles(ClientHelper.ParticleEvent event) {
        event.register(MysticalOakTree.WIND.get(), WindParticle.Factory::new);
    }
}
