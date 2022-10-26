package net.mehvahdjukaar.mysticaloaktree.forge;

import net.mehvahdjukaar.mysticaloaktree.MysticalOakTree;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTreeClient;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
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

