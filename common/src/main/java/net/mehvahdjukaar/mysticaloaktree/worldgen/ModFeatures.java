package net.mehvahdjukaar.mysticaloaktree.worldgen;

import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTree;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.RootSystemConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;
import java.util.OptionalInt;

public class ModFeatures {

    public static void init() {

    }

    public static final RegSupplier<TrunkPlacerType<WiseOakTrunkPlacer>> WISE_OAK_TRUNK_PLACER = RegHelper.register(
            MysticalOakTree.res("wise_oak_trunk_placer"),
            () -> new TrunkPlacerType<>(WiseOakTrunkPlacer.CODEC),
            Registry.TRUNK_PLACER_TYPES
    );

    public static final RegSupplier<FoliagePlacerType<WiseOakFoliagePlacer>> WISE_OAK_FOLIAGE_PLACER = RegHelper.register(
            MysticalOakTree.res("wise_oak_foliage_placer"),
            () -> new FoliagePlacerType<>(WiseOakFoliagePlacer.CODEC),
            Registry.FOLIAGE_PLACER_TYPES
    );

    public static final RegSupplier<TreeDecoratorType<WiseOakDecorator>> WISE_OAK_DECORATOR = RegHelper.register(
            MysticalOakTree.res("wise_oak_decorator"),
            () -> new TreeDecoratorType<>(WiseOakDecorator.CODEC),
            Registry.TREE_DECORATOR_TYPES
    );

    public static final RegSupplier<ConfiguredFeature<TreeConfiguration, Feature<TreeConfiguration>>> WISE_OAK =
            RegHelper.registerConfiguredFeature(
                    MysticalOakTree.res("wise_oak"),
                    () -> Feature.TREE,
                    () -> new TreeConfiguration.TreeConfigurationBuilder(
                            BlockStateProvider.simple(Blocks.OAK_LOG),
                            new WiseOakTrunkPlacer(7, 1, 0),
                            BlockStateProvider.simple(Blocks.OAK_LEAVES),
                            new WiseOakFoliagePlacer(ConstantInt.of(2), ConstantInt.of(1), 4),
                            new TwoLayersFeatureSize(0, 0, 0, OptionalInt.of(4))
                    ).decorators(List.of(WiseOakDecorator.INSTANCE))
                            .dirt(BlockStateProvider.simple(Blocks.ROOTED_DIRT)).build());


    public static final RegSupplier<ConfiguredFeature<RootSystemConfiguration, Feature<RootSystemConfiguration>>> ROOTED_WISE_OAK =
            RegHelper.registerConfiguredFeature(MysticalOakTree.res("rooted_wise_oak_tree"),
                    () -> Feature.ROOT_SYSTEM,
                    () -> new RootSystemConfiguration(
                            PlacementUtils.inlinePlaced(WISE_OAK.getHolder()),
                            7,
                            3,
                            BlockTags.AZALEA_ROOT_REPLACEABLE,
                            BlockStateProvider.simple(Blocks.ROOTED_DIRT),
                            5,
                            10,
                            3,
                            2,
                            BlockStateProvider.simple(Blocks.HANGING_ROOTS),
                            8,
                            2,
                            BlockPredicate.allOf(
                                    BlockPredicate.anyOf(
                                            BlockPredicate.matchesBlocks(List.of(Blocks.AIR, Blocks.CAVE_AIR, Blocks.VOID_AIR, Blocks.WATER)),
                                            BlockPredicate.matchesTag(BlockTags.LEAVES),
                                            BlockPredicate.matchesTag(BlockTags.REPLACEABLE_PLANTS)
                                    ),
                                    BlockPredicate.matchesTag(Direction.DOWN.getNormal(), BlockTags.AZALEA_GROWS_ON)
                            )
                    )
            );

    public static final RegSupplier<PlacedFeature> PLACED_WISE_OAK = RegHelper.registerPlacedFeature(
            MysticalOakTree.res("wise_oak"),
            WISE_OAK,
            () -> VegetationPlacements.treePlacement(RarityFilter.onAverageOnceEvery(200), Blocks.OAK_SAPLING)
    );
}
