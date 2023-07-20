package net.mehvahdjukaar.mysticaloaktree.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTree;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FancyFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public class WiseOakFoliagePlacer extends FancyFoliagePlacer {
    public static final Codec<WiseOakFoliagePlacer> CODEC = RecordCodecBuilder.create(instance -> blobParts(instance).apply(instance, WiseOakFoliagePlacer::new));

    public WiseOakFoliagePlacer(IntProvider intProvider, IntProvider intProvider2, int i) {
        super(intProvider, intProvider2, i);
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return MysticalOakTree.WISE_OAK_FOLIAGE_PLACER.get();
    }


    @Override
    protected boolean shouldSkipLocation(RandomSource random, int localX, int localY, int localZ, int range, boolean large) {
        var dist = Mth.square(localX + 0.5F) + Mth.square(localZ + 0.5F);
        double maxDist = (range * range);

        if (dist > maxDist) return true;
        if (dist > maxDist * 0.9 && random.nextInt(3) == 0) {
            return localY == 0 || localY == -this.height + 2;
        }
        return false;
    }
}
