package net.mehvahdjukaar.mysticaloaktree.neoforge;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTree;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTreeClient;
import net.neoforged.fml.common.Mod;

/**
 * Author: MehVahdJukaar
 */
@Mod(MysticalOakTree.MOD_ID)
public class MysticalOakTreeForge {

    public MysticalOakTreeForge() {
        MysticalOakTree.commonInit();

        if (PlatHelper.getPhysicalSide().isClient()) {
            MysticalOakTreeClient.init();
        }
    }


}

