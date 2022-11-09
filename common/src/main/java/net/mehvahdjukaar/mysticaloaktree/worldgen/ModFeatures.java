package net.mehvahdjukaar.mysticaloaktree.worldgen;

import com.google.common.collect.ImmutableList;
import net.mehvahdjukaar.moonlight3.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight3.api.platform.RegHelper;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTree;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.LeaveVineDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.levelgen.placement.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.OptionalInt;

import static net.minecraft.data.worldgen.placement.VegetationPlacements.TREE_THRESHOLD;

public class ModFeatures {

    public static void init() {

    }

    public static final RegSupplier<BlockPredicateType<BiomeMatchPredicate>> BIOME_MATCH_PREDICATE = RegHelper.register(
            MysticalOakTree.res("biome_match"), () -> () -> BiomeMatchPredicate.CODEC,
            Registry.BLOCK_PREDICATE_TYPES
    );

    public static final RegSupplier<Feature<WiseOakFeature.Configuration>> WISE_OAK_FEATURE = RegHelper.registerFeature(
            MysticalOakTree.res("wise_oak_trunk_placer"), WiseOakFeature::new
    );

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

    @NotNull
    private static Holder<PlacedFeature> makeTree(boolean hasVines, boolean hasRoots) {
        int h = hasRoots ? 1 : 0;
        var builder = new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(Blocks.OAK_LOG),
                new WiseOakTrunkPlacer(7, 1 + h, 0 + h),
                BlockStateProvider.simple(Blocks.OAK_LEAVES),
                new WiseOakFoliagePlacer(ConstantInt.of(2), ConstantInt.of(1), 4),
                new TwoLayersFeatureSize(0, 0, 0, OptionalInt.of(4))
        );
        if (hasVines) {
            builder.ignoreVines();
            builder.decorators(List.of(WiseOakDecorator.INSTANCE, new LeaveVineDecorator()));
        } else builder.decorators(List.of(WiseOakDecorator.INSTANCE));
        if (!hasRoots) {
            builder.dirt(BlockStateProvider.simple(Blocks.ROOTED_DIRT)).forceDirt();
        }

        return PlacementUtils.inlinePlaced(Feature.TREE, builder.build());
    }

    public static final RegSupplier<ConfiguredFeature<WiseOakFeature.Configuration, Feature<WiseOakFeature.Configuration>>> WISE_OAK =
            RegHelper.registerConfiguredFeature(
                    MysticalOakTree.res("wise_oak"),
                    WISE_OAK_FEATURE,
                    () -> new WiseOakFeature.Configuration(
                            makeTree(false, false),
                            makeTree(true, false),
                            makeTree(true, true),
                            PlacementUtils.inlinePlaced(
                                    Feature.RANDOM_PATCH,
                                    new RandomPatchConfiguration(50, 4, 2,
                                            PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(
                                                            new WeightedStateProvider(SimpleWeightedRandomList.<BlockState>builder()
                                                                    .add(Blocks.POPPY.defaultBlockState(), 3)
                                                                    .add(Blocks.ROSE_BUSH.defaultBlockState(), 1)
                                                                    .add(Blocks.BROWN_MUSHROOM.defaultBlockState(), 16)
                                                                    .add(Blocks.RED_MUSHROOM.defaultBlockState(), 19)
                                                            )
                                                    )
                                            ))
                            )
                    ));

    public static final RegSupplier<PlacedFeature> PLACED_WISE_OAK = RegHelper.registerPlacedFeature(
            MysticalOakTree.res("wise_oak"),
            WISE_OAK,
            () -> treePlacementBase().build()
    );

    private static ImmutableList.Builder<PlacementModifier> treePlacementBase() {
        return ImmutableList.<PlacementModifier>builder()
                .add(RarityFilter.onAverageOnceEvery(200))
                .add(InSquarePlacement.spread())
                .add(TREE_THRESHOLD)
                .add(PlacementUtils.HEIGHTMAP_OCEAN_FLOOR)
                .add(BiomeFilter.biome())
                .add(BlockPredicateFilter.forPredicate(
                        BlockPredicate.wouldSurvive(Blocks.OAK_SAPLING.defaultBlockState(), BlockPos.ZERO)
                ));
    }

}
