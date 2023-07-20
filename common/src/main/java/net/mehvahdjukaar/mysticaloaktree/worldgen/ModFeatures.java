package net.mehvahdjukaar.mysticaloaktree.worldgen;

import com.google.common.collect.ImmutableList;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTree;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
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
            Registries.BLOCK_PREDICATE_TYPE
    );

    public static final RegSupplier<Feature<WiseOakFeature.Configuration>> WISE_OAK_FEATURE = RegHelper.registerFeature(
            MysticalOakTree.res("wise_oak_trunk_placer"), WiseOakFeature::new
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
