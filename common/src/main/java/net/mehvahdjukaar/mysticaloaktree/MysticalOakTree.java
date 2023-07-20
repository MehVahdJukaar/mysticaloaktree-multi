package net.mehvahdjukaar.mysticaloaktree;

import net.mehvahdjukaar.moonlight.api.events.ILightningStruckBlockEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.mysticaloaktree.block.WiseOakBlock;
import net.mehvahdjukaar.mysticaloaktree.block.WiseOakTile;
import net.mehvahdjukaar.mysticaloaktree.client.TreeLoreManager;
import net.mehvahdjukaar.mysticaloaktree.worldgen.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.material.PushReaction;
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

        MoonlightEventsHelper.addListener(MysticalOakTree::onLightningStrike, ILightningStruckBlockEvent.class);

        RegHelper.addItemsToTabsRegistration(event -> {
            event.add(CreativeModeTabs.NATURAL_BLOCKS, BLOCK.get());
            event.add(CreativeModeTabs.FUNCTIONAL_BLOCKS, BLOCK.get());
        });

        //TODO: add chatGTP
    }

    private static void onLightningStrike(ILightningStruckBlockEvent event) {
        BlockPos pos = event.getPos().above();
        BlockState state = event.getLevel().getBlockState(pos);
        if (state.getBlock() == Blocks.OAK_SAPLING) {
            ServerLevel level = (ServerLevel) event.getLevel();
            BlockState blockState = level.getFluidState(pos).createLegacyBlock();
            level.setBlock(pos, blockState, 4);
            var feature = event.getLevel().registryAccess().registry(Registries.PLACED_FEATURE)
                    .get().get(res("wise_oak"));
            if (feature.place(level, level.getChunkSource().getGenerator(), level.random, pos)) {
                if (level.getBlockState(pos) == blockState) {
                    level.sendBlockUpdated(pos, state, blockState, 2);
                }
            } else {
                level.setBlock(pos, state, 4);
            }
        }
    }

    public static final Supplier<SimpleParticleType> WIND = RegHelper.registerParticle(res("wind"));

    public static final Supplier<Block> BLOCK = RegHelper.registerBlockWithItem(
            res("wise_oak"),
            () -> new WiseOakBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)
                    .pushReaction(PushReaction.BLOCK)
                    .strength(4.0F, 4.0F)),
            new Item.Properties().rarity(Rarity.EPIC), 1000);


    public static final Supplier<BlockEntityType<WiseOakTile>> TILE = RegHelper.registerBlockEntityType(
            res("wise_oak"),
            () -> PlatHelper.newBlockEntityType(
                    WiseOakTile::new, BLOCK.get()));



    public static final RegSupplier<BlockPredicateType<BiomeMatchPredicate>> BIOME_MATCH_PREDICATE = RegHelper.register(
            MysticalOakTree.res("biome_match"), () -> () -> BiomeMatchPredicate.CODEC,
            Registries.BLOCK_PREDICATE_TYPE
    );

    public static final RegSupplier<Feature<WiseOakFeature.Configuration>> WISE_OAK_FEATURE = RegHelper.registerFeature(
            MysticalOakTree.res("wise_oak"), WiseOakFeature::new
    );

    public static final RegSupplier<TrunkPlacerType<WiseOakTrunkPlacer>> WISE_OAK_TRUNK_PLACER = RegHelper.register(
            MysticalOakTree.res("wise_oak_trunk_placer"),
            () -> new TrunkPlacerType<>(WiseOakTrunkPlacer.CODEC),
            Registries.TRUNK_PLACER_TYPE
    );

    public static final RegSupplier<FoliagePlacerType<WiseOakFoliagePlacer>> WISE_OAK_FOLIAGE_PLACER = RegHelper.register(
            MysticalOakTree.res("wise_oak_foliage_placer"),
            () -> new FoliagePlacerType<>(WiseOakFoliagePlacer.CODEC),
            Registries.FOLIAGE_PLACER_TYPE
    );

    public static final RegSupplier<TreeDecoratorType<WiseOakDecorator>> WISE_OAK_DECORATOR = RegHelper.register(
            MysticalOakTree.res("wise_oak_decorator"),
            () -> new TreeDecoratorType<>(WiseOakDecorator.CODEC),
            Registries.TREE_DECORATOR_TYPE
    );

}
