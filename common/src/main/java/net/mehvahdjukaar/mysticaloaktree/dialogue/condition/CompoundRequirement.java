package net.mehvahdjukaar.mysticaloaktree.dialogue.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.mysticaloaktree.dialogue.DialogueContext;

import java.util.List;

/** Boolean combinators over other requirements. */
public final class CompoundRequirement {

    private CompoundRequirement() {
    }

    public record AllOf(List<Requirement> terms) implements Requirement {
        public static final MapCodec<AllOf> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Requirement.CODEC.listOf().fieldOf("terms").forGetter(AllOf::terms)
        ).apply(instance, AllOf::new));

        @Override
        public boolean test(DialogueContext context) {
            return terms.stream().allMatch(r -> r.test(context));
        }

        @Override
        public MapCodec<? extends Requirement> codec() {
            return CODEC;
        }
    }

    public record AnyOf(List<Requirement> terms) implements Requirement {
        public static final MapCodec<AnyOf> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Requirement.CODEC.listOf().fieldOf("terms").forGetter(AnyOf::terms)
        ).apply(instance, AnyOf::new));

        @Override
        public boolean test(DialogueContext context) {
            return terms.stream().anyMatch(r -> r.test(context));
        }

        @Override
        public MapCodec<? extends Requirement> codec() {
            return CODEC;
        }
    }

    public record Not(Requirement term) implements Requirement {
        public static final MapCodec<Not> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Requirement.CODEC.fieldOf("term").forGetter(Not::term)
        ).apply(instance, Not::new));

        @Override
        public boolean test(DialogueContext context) {
            return !term.test(context);
        }

        @Override
        public MapCodec<? extends Requirement> codec() {
            return CODEC;
        }
    }
}
