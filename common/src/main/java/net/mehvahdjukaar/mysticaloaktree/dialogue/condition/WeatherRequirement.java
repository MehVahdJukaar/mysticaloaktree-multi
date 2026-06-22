package net.mehvahdjukaar.mysticaloaktree.dialogue.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.mysticaloaktree.dialogue.DialogueContext;
import net.minecraft.util.StringRepresentable;

/** Requires the current weather at the tree. */
public record WeatherRequirement(Weather value) implements Requirement {

    public static final MapCodec<WeatherRequirement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Weather.CODEC.fieldOf("value").forGetter(WeatherRequirement::value)
    ).apply(instance, WeatherRequirement::new));

    @Override
    public boolean test(DialogueContext context) {
        return switch (value) {
            case CLEAR -> !context.isRaining() && !context.isThundering();
            case RAIN -> context.isRaining();
            case THUNDER -> context.isThundering();
        };
    }

    @Override
    public MapCodec<? extends Requirement> codec() {
        return CODEC;
    }

    public enum Weather implements StringRepresentable {
        CLEAR("clear"),
        RAIN("rain"),
        THUNDER("thunder");

        public static final StringRepresentable.EnumCodec<Weather> CODEC = StringRepresentable.fromEnum(Weather::values);

        private final String name;

        Weather(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
