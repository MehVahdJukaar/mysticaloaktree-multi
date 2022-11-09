package net.mehvahdjukaar.mysticaloaktree.forge;

import net.mehvahdjukaar.moonlight3.api.platform.PlatformHelper;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTree;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTreeClient;
import net.mehvahdjukaar.mysticaloaktree.worldgen.ModFeatures;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Author: MehVahdJukaar
 */
@Mod(MysticalOakTree.MOD_ID)
public class MysticalOakTreeForge {

    public MysticalOakTreeForge() {
        MysticalOakTree.commonInit();

        if (PlatformHelper.getEnv().isClient()) {
            MysticalOakTreeClient.init();
        }

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void addStuffToBiomes(BiomeLoadingEvent event) {

        if (ForgeRegistries.BIOMES.getHolder(event.getName()).get().is(BiomeTags.HAS_MINESHAFT)) {
            event.getGeneration().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModFeatures.PLACED_WISE_OAK.getHolder());

        }
    }


}

