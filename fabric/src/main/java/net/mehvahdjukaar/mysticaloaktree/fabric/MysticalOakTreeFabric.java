package net.mehvahdjukaar.mysticaloaktree.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.mehvahdjukaar.moonlight3.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight3.api.platform.fabric.RegHelperImpl;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTree;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTreeClient;
import net.mehvahdjukaar.mysticaloaktree.worldgen.ModFeatures;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;

public class MysticalOakTreeFabric implements ModInitializer {

    @Override
    public void onInitialize() {

        MysticalOakTree.commonInit();

        //registers stuff
        RegHelperImpl.registerEntries();

        if (PlatformHelper.getEnv().isClient()) {
            MysticalOakTreeClient.init();
            MysticalOakTreeClient.setup();
        }
        BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.HAS_MINESHAFT), GenerationStep.Decoration.VEGETAL_DECORATION,
                ModFeatures.PLACED_WISE_OAK.getHolder().unwrapKey().get());

    }
}
