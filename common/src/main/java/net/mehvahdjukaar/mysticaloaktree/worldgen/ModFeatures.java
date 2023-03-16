package net.mehvahdjukaar.mysticaloaktree.worldgen;

import com.google.common.collect.ImmutableList;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTree;
import net.minecraft.core.*;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RootSystemConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.rootplacers.AboveRootPlacement;
import net.minecraft.world.level.levelgen.feature.rootplacers.MangroveRootPlacement;
import net.minecraft.world.level.levelgen.feature.rootplacers.MangroveRootPlacer;
import net.minecraft.world.level.levelgen.feature.rootplacers.RootPlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.LeaveVineDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.levelgen.placement.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
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
                new WiseOakTrunkPlacer(7, 1+h, 0+h),
                BlockStateProvider.simple(Blocks.OAK_LEAVES),
                new WiseOakFoliagePlacer(ConstantInt.of(2), ConstantInt.of(1), 4),
                !hasRoots ? Optional.empty() : Optional.of(
                        new MangroveRootPlacer(
                                UniformInt.of(1, 3),
                                BlockStateProvider.simple(Blocks.MANGROVE_ROOTS),
                                Optional.of(new AboveRootPlacement(BlockStateProvider.simple(Blocks.MOSS_CARPET), 0.5F)),
                                new MangroveRootPlacement(
                                        Registry.BLOCK.getOrCreateTag(BlockTags.MANGROVE_ROOTS_CAN_GROW_THROUGH),
                                        HolderSet.direct(Block::builtInRegistryHolder, Blocks.MUD, Blocks.MUDDY_MANGROVE_ROOTS),
                                        BlockStateProvider.simple(Blocks.MUDDY_MANGROVE_ROOTS),
                                        6,
                                        11,
                                        0.2F
                                )
                        )
                ),
                new TwoLayersFeatureSize(0, 0, 0, OptionalInt.of(4))
        );
        if (hasVines) {
            builder.ignoreVines();
            builder.decorators(List.of(WiseOakDecorator.INSTANCE, new LeaveVineDecorator(0.125F)));
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
                                    new RandomPatchConfiguration(60, 4, 2,
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
            () -> treePlacementBase().build()
    );

    private static ImmutableList.Builder<PlacementModifier> treePlacementBase() {
        return ImmutableList.<PlacementModifier>builder()
                .add(RarityFilter.onAverageOnceEvery(MysticalOakTree.TREE_RARITY.get()))
                .add(InSquarePlacement.spread())
                .add(TREE_THRESHOLD)
                .add(PlacementUtils.HEIGHTMAP_OCEAN_FLOOR)
                .add(BiomeFilter.biome())
                .add(BlockPredicateFilter.forPredicate(
                        BlockPredicate.anyOf(
                                BlockPredicate.allOf(
                                        new BiomeMatchPredicate(Biomes.MANGROVE_SWAMP),
                                        BlockPredicate.wouldSurvive(Blocks.MANGROVE_PROPAGULE.defaultBlockState(), BlockPos.ZERO)
                                ),
                                BlockPredicate.wouldSurvive(Blocks.OAK_SAPLING.defaultBlockState(), BlockPos.ZERO)
                        )));
    }

}
