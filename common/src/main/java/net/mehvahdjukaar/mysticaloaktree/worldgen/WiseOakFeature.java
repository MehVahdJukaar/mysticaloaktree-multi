package net.mehvahdjukaar.mysticaloaktree.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class WiseOakFeature extends Feature<WiseOakFeature.Configuration> {
    public WiseOakFeature() {
        super(Configuration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<Configuration> context) {
        var config = context.config();
        RandomSource randomSource = context.random();
        WorldGenLevel level = context.level();
        ChunkGenerator generator = context.chunkGenerator();
        BlockPos pos = context.origin();
        Holder<PlacedFeature> feature;
        Holder<Biome> biome = level.getBiome(pos);
        if (biome.is(Biomes.MANGROVE_SWAMP)) {
            feature = config.mangrove;
        } else if (biome.is(BiomeTags.HAS_SWAMP_HUT) || biome.is(BiomeTags.IS_JUNGLE)) {
            feature = config.vines;
        } else feature = config.main;
        if (feature.value().place(level, generator, randomSource, pos)) {

            config.flowers.value().place(level, generator, randomSource, pos);
            return true;
        }
        return false;
    }

    public record Configuration(Holder<PlacedFeature> main,
                                Holder<PlacedFeature> vines,
                                Holder<PlacedFeature> mangrove,
                                Holder<PlacedFeature> flowers) implements FeatureConfiguration {

        public static final Codec<Configuration> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                PlacedFeature.CODEC.fieldOf("main_feature").forGetter(Configuration::main),
                                PlacedFeature.CODEC.fieldOf("vines_feature").forGetter(Configuration::vines),
                                PlacedFeature.CODEC.fieldOf("mangrove_feature").forGetter(Configuration::mangrove),
                                PlacedFeature.CODEC.fieldOf("flower_patch").forGetter(Configuration::flowers)
                        )
                        .apply(instance, Configuration::new)
        );
    }
}
