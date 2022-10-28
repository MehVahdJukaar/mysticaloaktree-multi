package net.mehvahdjukaar.mysticaloaktree.worldgen;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTree;
import net.mehvahdjukaar.mysticaloaktree.block.WiseOakBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

import java.util.List;

public class WiseOakDecorator extends TreeDecorator {
    public static final WiseOakDecorator INSTANCE = new WiseOakDecorator();
    public static final Codec<WiseOakDecorator> CODEC = Codec.unit(() -> INSTANCE);

    private WiseOakDecorator(){}

    @Override
    protected TreeDecoratorType<?> type() {
        return ModFeatures.WISE_OAK_DECORATOR.get();
    }

    @Override
    public void place(TreeDecorator.Context context) {
        RandomSource randomSource = context.random();
        List<BlockPos> leaves = context.leaves();
        List<BlockPos> logs = context.logs();

        if (logs.size() > 2) {
            BlockPos pos = logs.get(2);
            Direction chosen = null;
            for (Direction d : Direction.Plane.HORIZONTAL.shuffledCopy(randomSource)) {
                if (!leaves.contains(pos.relative(d))) {
                    chosen = d;
                    BlockPos above = pos.relative(d).above();
                    if(!leaves.contains(above) && logs.contains(above)) {
                        break;
                    }
                }
            }
            if(chosen != null) {
                context.setBlock(pos, MysticalOakTree.BLOCK.get().defaultBlockState()
                        .setValue(WiseOakBlock.STATE, WiseOakBlock.State.SLEEPING)
                        .setValue(WiseOakBlock.FACING, chosen));
            }
        }
    }
}
