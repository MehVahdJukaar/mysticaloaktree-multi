package net.mehvahdjukaar.mysticaloaktree.fabric;

import net.fabricmc.api.ModInitializer;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTree;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTreeClient;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.fabric.FabricSetupCallbacks;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public class MysticalOakTreeFabric implements ModInitializer {

    @Override
    public void onInitialize() {

        MysticalOakTree.commonInit();

        if (PlatformHelper.getEnv().isClient()) {
            FabricSetupCallbacks.CLIENT_SETUP.add(MysticalOakTreeClient::init);
            FabricSetupCallbacks.CLIENT_SETUP.add(MysticalOakTreeClient::setup);
        }
    }
}
