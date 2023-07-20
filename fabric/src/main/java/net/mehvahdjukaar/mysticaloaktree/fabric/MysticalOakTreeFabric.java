package net.mehvahdjukaar.mysticaloaktree.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTree;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTreeClient;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;

public class MysticalOakTreeFabric implements ModInitializer {

    @Override
    public void onInitialize() {

        MysticalOakTree.commonInit();

        if (PlatHelper.getPhysicalSide().isClient()) {
            MysticalOakTreeClient.init();

        }
        PlatHelper.addCommonSetup(() ->
                BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_OVERWORLD), GenerationStep.Decoration.VEGETAL_DECORATION,
                ResourceKey.create(Registries.PLACED_FEATURE, MysticalOakTree.res("wise_oak"))));

    }
}
