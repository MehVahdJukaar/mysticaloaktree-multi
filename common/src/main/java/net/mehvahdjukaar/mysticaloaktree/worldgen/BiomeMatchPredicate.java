package net.mehvahdjukaar.mysticaloaktree.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;

public class BiomeMatchPredicate implements BlockPredicate {
    private final ResourceKey<Biome> biome;
    public static final Codec<BiomeMatchPredicate> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            ResourceKey.codec(Registries.BIOME).fieldOf("biome").forGetter(hasSturdyFacePredicate -> hasSturdyFacePredicate.biome)
                    )
                    .apply(instance, BiomeMatchPredicate::new)
    );


    public BiomeMatchPredicate(ResourceKey<Biome> biome) {
        this.biome = biome;
    }

    public boolean test(WorldGenLevel worldGenLevel, BlockPos blockPos) {
        return biome == worldGenLevel.getBiome(blockPos).unwrapKey().get();
    }

    @Override
    public BlockPredicateType<?> type() {
        return ModFeatures.BIOME_MATCH_PREDICATE.get();
    }
}
