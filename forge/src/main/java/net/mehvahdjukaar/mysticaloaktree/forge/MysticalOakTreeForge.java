package net.mehvahdjukaar.mysticaloaktree.forge;

import net.mehvahdjukaar.mysticaloaktree.MysticalOakTree;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTreeClient;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

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

    public abstract static class  e extends Block{
        public e(Properties arg) {
            super(arg);
        }

        @Override
        protected void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state) {
            super.spawnDestroyParticles(level, player, pos, state);
        }

        @Override
        public boolean canBeHydrated(BlockState state, BlockGetter getter, BlockPos pos, FluidState fluid, BlockPos fluidPos) {
            return super.canBeHydrated(state, getter, pos, fluid, fluidPos);
        }

        @Override
        public void onCaughtFire(BlockState state, Level level, BlockPos pos, @Nullable Direction direction, @Nullable LivingEntity igniter) {
            super.onCaughtFire(state, level, pos, direction, igniter);
        }


    }


}

