package net.mehvahdjukaar.mysticaloaktree.forge;

import net.mehvahdjukaar.mysticaloaktree.MysticalOakTree;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTreeClient;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.minecraftforge.fml.common.Mod;

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
    }



}

