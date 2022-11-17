package net.mehvahdjukaar.mysticaloaktree.forge;

import net.mehvahdjukaar.moonlight3.api.platform.PlatformHelper;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTree;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTreeClient;
import net.mehvahdjukaar.mysticaloaktree.worldgen.ModFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;

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

        ResourceLocation res = event.getName();
        if (res != null && event.getCategory() != Biome.BiomeCategory.UNDERGROUND) {
            ResourceKey<Biome> key = ResourceKey.create(ForgeRegistries.Keys.BIOMES, res);
            Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(key);
            if(types.contains(BiomeDictionary.Type.OVERWORLD)){
                event.getGeneration().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModFeatures.PLACED_WISE_OAK.getHolder());
            }
        }
    }


}

