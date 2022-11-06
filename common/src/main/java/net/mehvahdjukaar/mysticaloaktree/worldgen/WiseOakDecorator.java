package net.mehvahdjukaar.mysticaloaktree.worldgen;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTree;
import net.mehvahdjukaar.mysticaloaktree.block.WiseOakBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

public class WiseOakDecorator extends TreeDecorator {
    public static final WiseOakDecorator INSTANCE = new WiseOakDecorator();
    public static final Codec<WiseOakDecorator> CODEC = Codec.unit(() -> INSTANCE);

    private WiseOakDecorator() {
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return ModFeatures.WISE_OAK_DECORATOR.get();
    }

    @Override
    public void place(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer,
                      Random random, List<BlockPos> logs, List<BlockPos> leaves) {

        if (logs.size() > 2) {
            BlockPos pos = logs.get(2);
            Direction chosen = null;
            List<Direction> leafAbove = new ArrayList<>();
            List<Direction> logAbove = new ArrayList<>();
            var dd = new ArrayList<>(Direction.Plane.HORIZONTAL.stream().toList());
            Collections.shuffle(dd, random);
            for (Direction d : dd) {
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

                biConsumer.accept(pos, MysticalOakTree.BLOCK.get().defaultBlockState()
                        .setValue(WiseOakBlock.STATE, WiseOakBlock.State.SLEEPING)
                        .setValue(WiseOakBlock.FACING, chosen));
            }
        }
    }
}
