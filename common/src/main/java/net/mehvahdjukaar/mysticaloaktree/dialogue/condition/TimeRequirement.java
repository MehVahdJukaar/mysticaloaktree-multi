package net.mehvahdjukaar.mysticaloaktree.dialogue.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.mysticaloaktree.dialogue.DialogueContext;
import net.minecraft.util.StringRepresentable;

/** Requires it to currently be day or night. */
public record TimeRequirement(DayPart value) implements Requirement {

    public static final MapCodec<TimeRequirement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DayPart.CODEC.fieldOf("value").forGetter(TimeRequirement::value)
    ).apply(instance, TimeRequirement::new));

    @Override
    public boolean test(DialogueContext context) {
        return (value == DayPart.NIGHT) == context.isNight();
    }

    @Override
    public MapCodec<? extends Requirement> codec() {
        return CODEC;
    }

    public enum DayPart implements StringRepresentable {
        DAY("day"),
        NIGHT("night");

        public static final StringRepresentable.EnumCodec<DayPart> CODEC = StringRepresentable.fromEnum(DayPart::values);

        private final String name;

        DayPart(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
