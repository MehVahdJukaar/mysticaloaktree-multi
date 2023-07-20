package net.mehvahdjukaar.mysticaloaktree.worldgen;

//
// Source code recreated from a .class file by Quiltflower
//


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTree;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer.FoliageAttachment;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class WiseOakTrunkPlacer extends TrunkPlacer {
    public static final Codec<WiseOakTrunkPlacer> CODEC = RecordCodecBuilder.create(
            instance -> trunkPlacerParts(instance).apply(instance, WiseOakTrunkPlacer::new)
    );

    public WiseOakTrunkPlacer(int i, int j, int k) {
        super(i, j, k);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return MysticalOakTree.WISE_OAK_TRUNK_PLACER.get();
    }

    @Override
    public List<FoliageAttachment> placeTrunk(
            LevelSimulatedReader level,
            BiConsumer<BlockPos, BlockState> blockSetter,
            RandomSource random,
            int freeTreeHeight,
            BlockPos pos,
            TreeConfiguration config
    ) {
        List<FoliageAttachment> foliageAttachments = new ArrayList<>();
        BlockPos blockPos = pos.below();

        if(config.forceDirt) {
            setDirtAt(level, blockSetter, random, blockPos, config);
        }
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int py = y + freeTreeHeight - 1;

        for (int h = 0; h < freeTreeHeight; ++h) {
            int r = y + h;
            BlockPos blockPos2 = new BlockPos(x, r, z);
            if (TreeFeature.isAirOrLeaves(level, blockPos2)) {
                this.placeLog(level, blockSetter, random, blockPos2, config);
            }
        }
        foliageAttachments.add(new FoliageAttachment(new BlockPos(x, py, z), 0, false));


        int branches = 1 + random.nextInt(0, 3) + random.nextInt(2);
        List<BlockPos> dirs = new ArrayList<>(BRANCH_POS);
        List<BlockPos> selected = new ArrayList<>();

        for (int j = 0; j < branches && !dirs.isEmpty(); j++) {
            int i = random.nextInt(dirs.size());
            BlockPos pp = dirs.remove(i);
            dirs.removeIf(b -> b.distManhattan(pp) == 1);
            selected.add(pp);
        }
        for (var v : selected) {
            Direction.Axis axis = v.getX() != 0 ? Direction.Axis.X : Direction.Axis.Z;
            int branchH = -2 - random.nextInt(3);
            this.placeLog(level, blockSetter, random, new BlockPos(x + v.getX(), py + branchH, z + v.getZ()), config,
                    blockState -> blockState.setValue(RotatedPillarBlock.AXIS, axis));

               foliageAttachments.add(new FoliageAttachment(new BlockPos(x + v.getX(), py - 1, z + v.getZ()), 0, false));

        }
        return foliageAttachments;
    }


    private static final List<BlockPos> BRANCH_POS = BlockPos.betweenClosedStream(-1, 0, -1, 1, 0, 1)
            .filter(blockPos -> !(blockPos.getZ() == 0 && blockPos.getX() == 0))
            .map(BlockPos::immutable)
            .toList();
}

