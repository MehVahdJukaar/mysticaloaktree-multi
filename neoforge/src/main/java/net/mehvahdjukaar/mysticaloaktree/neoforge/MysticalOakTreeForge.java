package net.mehvahdjukaar.mysticaloaktree.neoforge;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.platform.neoforge.RegHelperImpl;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTree;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTreeClient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/**
 * Author: MehVahdJukaar
 */
@Mod(MysticalOakTree.MOD_ID)
public class MysticalOakTreeForge {

    public MysticalOakTreeForge(IEventBus bus) {
        RegHelper.startRegisteringFor(bus);
        MysticalOakTree.commonInit();

        if (PlatHelper.getPhysicalSide().isClient()) {
            MysticalOakTreeClient.init();
        }
    }


}

