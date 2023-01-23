package net.mehvahdjukaar.mysticaloaktree;

import net.mehvahdjukaar.moonlight.api.events.ILightningStruckBlockEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.events.SimpleEvent;
import net.mehvahdjukaar.moonlight.api.item.WoodBasedBlockItem;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.mysticaloaktree.block.WiseOakBlock;
import net.mehvahdjukaar.mysticaloaktree.block.WiseOakTile;
import net.mehvahdjukaar.mysticaloaktree.client.TreeLoreManager;
import net.mehvahdjukaar.mysticaloaktree.worldgen.ModFeatures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

/**
 * Author: MehVahdJukaar
 */
public class MysticalOakTree {

    //TODO: blow smoke rings
    //TODO: mushrooms
    //play flute

    public static final String MOD_ID = "mysticaloaktree";
    public static final Logger LOGGER = LogManager.getLogger();

    public static ResourceLocation res(String name) {
        return new ResourceLocation(MOD_ID, name);
    }

    public static void commonInit() {
        TreeLoreManager.init();
        ModFeatures.init();

        MoonlightEventsHelper.addListener(MysticalOakTree::onLightningStrike, ILightningStruckBlockEvent.class);
    }

    private static void onLightningStrike(ILightningStruckBlockEvent event) {
        BlockPos pos = event.getPos().above();
        BlockState state = event.getLevel().getBlockState(pos);
        if(state.getBlock() == Blocks.OAK_SAPLING){
            ServerLevel level = (ServerLevel) event.getLevel();
            BlockState blockState = level.getFluidState(pos).createLegacyBlock();
            level.setBlock(pos, blockState, 4);
            if (ModFeatures.WISE_OAK.get().place(level, level.getChunkSource().getGenerator(), level.random, pos)) {
                if (level.getBlockState(pos) == blockState) {
                    level.sendBlockUpdated(pos, state, blockState, 2);
                }
            } else {
                level.setBlock(pos, state, 4);
            }
        }
    }

    public static final Supplier<SimpleParticleType> WIND = RegHelper.registerParticle(res("wind"));

    public static final Supplier<Block> BLOCK = regWithItem("wise_oak",
            () -> new WiseOakBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)
                    .strength(4.0F, 4.0F)),
            new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS).rarity(Rarity.EPIC), 1000);


    public static final Supplier<BlockEntityType<WiseOakTile>> TILE = regTile(
            "wise_oak", () -> PlatformHelper.newBlockEntityType(
                    WiseOakTile::new, BLOCK.get()));


    public static <T extends Block> Supplier<T> regWithItem(String name, Supplier<T> blockFactory, Item.Properties properties, int burnTime) {
        Supplier<T> block = RegHelper.registerBlock(MysticalOakTree.res(name), blockFactory);;
        regBlockItem(name, block, properties, burnTime);
        return block;
    }


    public static Supplier<BlockItem> regBlockItem(String name, Supplier<? extends Block> blockSup, Item.Properties properties, int burnTime) {
        return RegHelper.registerItem(MysticalOakTree.res(name), () -> burnTime == 0 ? new BlockItem(blockSup.get(), properties) :
                new WoodBasedBlockItem(blockSup.get(), properties, burnTime));
    }

    public static <T extends BlockEntityType<E>, E extends BlockEntity> Supplier<T> regTile(String name, Supplier<T> sup) {
        return RegHelper.registerBlockEntityType(MysticalOakTree.res(name), sup);
    }
}
