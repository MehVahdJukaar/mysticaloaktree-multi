package net.mehvahdjukaar.mysticaloaktree.forge;


import net.mehvahdjukaar.mysticaloaktree.MysticalOakTree;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTreeClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = MysticalOakTree.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MysticalOakTreeForgeClient {

    @SubscribeEvent
    public static void init(final FMLClientSetupEvent event) {
        event.enqueueWork(MysticalOakTreeClient::setup);
    }

}
