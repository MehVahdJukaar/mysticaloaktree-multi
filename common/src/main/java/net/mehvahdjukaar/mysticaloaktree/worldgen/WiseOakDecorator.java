package net.mehvahdjukaar.mysticaloaktree.worldgen;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTree;
import net.mehvahdjukaar.mysticaloaktree.block.WiseOakBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.ArrayList;
import java.util.List;

public class WiseOakDecorator extends TreeDecorator {
    public static final WiseOakDecorator INSTANCE = new WiseOakDecorator();
    public static final Codec<WiseOakDecorator> CODEC = Codec.unit(() -> INSTANCE);

    private WiseOakDecorator() {
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return MysticalOakTree.WISE_OAK_DECORATOR.get();
    }

    @Override
    public void place(TreeDecorator.Context context) {
        RandomSource randomSource = context.random();
        List<BlockPos> leaves = context.leaves();
        List<BlockPos> logs = context.logs();

        if (logs.size() > 2) {
            BlockPos pos = logs.get(2);
            Direction chosen = null;
            List<Direction> leafAbove = new ArrayList<>();
            List<Direction> logAbove = new ArrayList<>();
            for (Direction d : Direction.Plane.HORIZONTAL.shuffledCopy(randomSource)) {
                if (!leaves.contains(pos.relative(d))) {
                    BlockPos above = pos.relative(d).above();
                    if (logs.contains(above)) {
                        logAbove.add(d);
                        continue;
                    }
                    if (leaves.contains(above)) {
                        leafAbove.add(d);
                        continue;
                    }
                    chosen = d;
                    break;
                }
            }
            if (chosen == null) {
                if (!leafAbove.isEmpty()) chosen = leafAbove.get(0);
                else if (!logAbove.isEmpty()) chosen = logAbove.get(0);
            }

            if (chosen != null) {

                context.setBlock(pos, MysticalOakTree.BLOCK.get().defaultBlockState()
                        .setValue(WiseOakBlock.STATE, WiseOakBlock.State.SLEEPING)
                        .setValue(WiseOakBlock.FACING, chosen));
            }
        }
    }
}
