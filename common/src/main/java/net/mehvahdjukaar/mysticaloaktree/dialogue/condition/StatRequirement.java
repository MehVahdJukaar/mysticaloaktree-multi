package net.mehvahdjukaar.mysticaloaktree.dialogue.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.mysticaloaktree.dialogue.DialogueContext;
import net.mehvahdjukaar.mysticaloaktree.dialogue.stat.Stat;
import net.mehvahdjukaar.mysticaloaktree.dialogue.stat.Stats;

import java.util.Optional;

/**
 * Requires a player stat to lie within [min, max] (both inclusive, both optional).
 * Generalizes the old {@code trust_required} field: {@code {"type":"stat","stat":"trust","min":40}}.
 */
public record StatRequirement(Stat stat, Optional<Integer> min, Optional<Integer> max) implements Requirement {

    public static final Codec<Stat> STAT_CODEC = Codec.STRING.comapFlatMap(
            id -> {
                Stat s = Stats.get(id);
                return s != null ? DataResult.success(s) : DataResult.error(() -> "Unknown stat: " + id);
            },
            Stat::id);

    public static final MapCodec<StatRequirement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            STAT_CODEC.fieldOf("stat").forGetter(StatRequirement::stat),
            Codec.INT.optionalFieldOf("min").forGetter(StatRequirement::min),
            Codec.INT.optionalFieldOf("max").forGetter(StatRequirement::max)
    ).apply(instance, StatRequirement::new));

    public static StatRequirement atLeast(Stat stat, int min) {
        return new StatRequirement(stat, Optional.of(min), Optional.empty());
    }

    @Override
    public boolean test(DialogueContext context) {
        int value = context.stat(stat);
        return (min.isEmpty() || value >= min.get()) && (max.isEmpty() || value <= max.get());
    }

    @Override
    public MapCodec<? extends Requirement> codec() {
        return CODEC;
    }
}
