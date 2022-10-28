package net.mehvahdjukaar.mysticaloaktree.worldgen;

//
// Source code recreated from a .class file by Quiltflower
//


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer.FoliageAttachment;
import net.minecraft.world.level.levelgen.feature.trunkplacers.FancyTrunkPlacer;
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
        return ModFeatures.WISE_OAK_TRUNK_PLACER.get();
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

        setDirtAt(level, blockSetter, random, blockPos, config);
        //Direction direction = Plane.HORIZONTAL.getRandomDirection(random);
        int curveH = freeTreeHeight - random.nextInt(4);
        int curve = 2 - random.nextInt(3);
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int px = x;
        int pz = z;
        int py = y + freeTreeHeight - 1;

        for (int h = 0; h < freeTreeHeight; ++h) {
            //if (h >= curveH && curve > 0) {
            //    px += direction.getStepX();
            //    pz += direction.getStepZ();
            //    --curve;
            // }

            int r = y + h;
            BlockPos blockPos2 = new BlockPos(px, r, pz);
            if (TreeFeature.isAirOrLeaves(level, blockPos2)) {
                this.placeLog(level, blockSetter, random, blockPos2, config);
            }
        }

        foliageAttachments.add(new FoliageAttachment(new BlockPos(px, py, pz), 0, false));

        var dirList = Plane.HORIZONTAL.shuffledCopy(random);
        dirList = dirList.subList(0, random.nextInt(1, 4));

        for (Direction d : dirList) {

            int branchH = random.nextInt(3)+1;

            var v = d.getNormal();

           // for (int by = 0; by < branchH; ++by) {
                this.placeLog(level, blockSetter, random, new BlockPos(x + v.getX(), py - 2 -random.nextInt(3), z + v.getZ()), config,
                        blockState -> blockState.setValue(RotatedPillarBlock.AXIS, d.getAxis()));
           // }
            foliageAttachments.add(new FoliageAttachment(new BlockPos(px + v.getX(), py-1, pz + v.getZ()), 0, false));

        }


        return foliageAttachments;
    }
}

