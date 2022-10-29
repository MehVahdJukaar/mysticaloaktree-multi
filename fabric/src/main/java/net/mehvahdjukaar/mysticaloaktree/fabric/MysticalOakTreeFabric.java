package net.mehvahdjukaar.mysticaloaktree.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTree;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTreeClient;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.fabric.FabricSetupCallbacks;
import net.mehvahdjukaar.mysticaloaktree.worldgen.ModFeatures;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public class MysticalOakTreeFabric implements ModInitializer {

    @Override
    public void onInitialize() {

        MysticalOakTree.commonInit();

        if (PlatformHelper.getEnv().isClient()) {
            FabricSetupCallbacks.CLIENT_SETUP.add(MysticalOakTreeClient::init);
            FabricSetupCallbacks.CLIENT_SETUP.add(MysticalOakTreeClient::setup);
        }
        FabricSetupCallbacks.COMMON_SETUP.add(()->        BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_OVERWORLD), GenerationStep.Decoration.VEGETAL_DECORATION,
                ModFeatures.PLACED_WISE_OAK.getHolder().unwrapKey().get()));

    }
}
