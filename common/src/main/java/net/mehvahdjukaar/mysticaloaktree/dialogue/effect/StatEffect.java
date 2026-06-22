package net.mehvahdjukaar.mysticaloaktree.dialogue.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.mysticaloaktree.dialogue.condition.StatRequirement;
import net.mehvahdjukaar.mysticaloaktree.dialogue.stat.Stat;
import net.mehvahdjukaar.mysticaloaktree.dialogue.stat.StatMap;

/** Adds a (possibly negative) delta to a stat: {@code {"type":"stat","stat":"romance","delta":2}}. */
public record StatEffect(Stat stat, int delta) implements Effect {

    public static final MapCodec<StatEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            StatRequirement.STAT_CODEC.fieldOf("stat").forGetter(StatEffect::stat),
            Codec.INT.fieldOf("delta").forGetter(StatEffect::delta)
    ).apply(instance, StatEffect::new));

    @Override
    public void apply(StatMap stats) {
        stats.add(stat, delta);
    }

    @Override
    public MapCodec<? extends Effect> codec() {
        return CODEC;
    }
}
